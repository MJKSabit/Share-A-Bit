package github.mjksabit.sabit.core.ftp;

import java.io.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;

import static github.mjksabit.sabit.core.Connection.FINISHED_COMMAND;
import static github.mjksabit.sabit.core.Connection.SENDING_COMMAND;

public class SimpleFTP implements IFTP {

    DataInputStream in;
    DataOutputStream out;

    ExecutorService sendThread;
    volatile Semaphore sendSemaphore = new Semaphore(1);
    ExecutorService receiveThread;
    volatile Semaphore receiveSemaphore = new Semaphore(1);

    volatile Queue<FileData> sendQueue = new ConcurrentLinkedQueue<>();

    public SimpleFTP(DataInputStream in, DataOutputStream out) {
        this.in = in;
        this.out = out;

        sendThread = Executors.newSingleThreadExecutor();
        receiveThread = Executors.newSingleThreadExecutor();
    }

    @Override
    public boolean isSending() {
        return sendSemaphore.availablePermits() == 0;
    }


    public void stopSendingCurrent() {
        if (!sendQueue.isEmpty())
            sendQueue.peek().setCancelled(true);
    }

    public void stopSendAll() {
        Iterator<FileData> iterator = sendQueue.iterator();

        while (iterator.hasNext()) {
            iterator.next().setCancelled(true);

        }
    }


    @Override
    public void addToSend(String parentPath, File file, ProgressUpdater progress) {
        sendQueue.add(new FileData(parentPath, file, progress));

        if (!isSending()) {
            try {
                sendSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sendThread.execute(() -> {
                while (!sendQueue.isEmpty()) {
                    try {
                        send(sendQueue.peek());
                    } catch (IOException e) {
                        System.err.println(sendQueue.peek().getFile().getName() + " not Sent!");
                    } finally {
                        if (!sendQueue.isEmpty()) sendQueue.remove();
                    }
                }
                sendSemaphore.release();
            });
        }
    }

    @Override
    public void cancelSending() {
        synchronized (sendQueue) {
            sendQueue.clear();
        }
    }

    private void send(FileData data) throws IOException {
        if (data.getFile().isFile()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(data.getFile()));

            out.writeUTF(SENDING_COMMAND);
            out.writeUTF(data.getParentPath()+File.separator+data.getFile().getName());
            out.writeLong(data.getFile().length());

            int n;
            long currentProgress = 0, totalProgress = data.getFile().length();

            data.getProgress().startProgress(data.getFile());

            while ( (n =in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, n);
                out.writeByte(100);
                out.flush();

                currentProgress += n;
                data.getProgress().continueProgress(currentProgress, totalProgress);
            }

            data.getProgress().endProgress(data.getFile());

            in.close();

        } else {
            System.err.println("Not a File");
        }
    }

    @Override
    public boolean isReceiving() {
        return receiveSemaphore.availablePermits() == 0;
    }

    @Override
    public void startReceiving(String savePath, ProgressUpdater updater) {
        if (isReceiving()) {
            System.err.println("Receiver Thread Already Running!");
        }
        else {
            try {
                receiveSemaphore.acquire();
                receiveThread.execute(() -> {
                    while (true) {
                        try {
                            String cmd =  in.readUTF();
                            if (cmd.equals(FINISHED_COMMAND)) break;
                        } catch (EOFException e) {
                            System.out.println("Connection Closed!");
                            break;
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            receive(savePath, updater);
                        } catch (IOException e) {
                            System.err.println("Can Not Receive : " + e.getLocalizedMessage());
                            break;
                        }
                    }
                    receiveSemaphore.release();
                });
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void receive(String savePath, ProgressUpdater updater) throws IOException {
        String fileName = in.readUTF();
        long fileSize = in.readLong();

        File file = new File(savePath + File.separator + fileName);

        try {
            file.getParentFile().mkdirs();
        } catch (NullPointerException ignore) {}

        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));

        byte[] buffer = new byte[BUFFER_SIZE];
        int n, signalAway = BUFFER_SIZE;
        byte signal;

        updater.startProgress(file);

        for (long sizeToRead = fileSize; sizeToRead > 0 &&
             (n = in.read(buffer, 0, (int) Math.min(buffer.length, sizeToRead))) != -1; sizeToRead -= n) {

            if(signalAway - n < 0){
                bufferedOutputStream.write(buffer, 0, signalAway);
                signal = buffer[signalAway];
                bufferedOutputStream.write(buffer, signalAway+1, n-signalAway-1);

                sizeToRead++;
                signalAway += BUFFER_SIZE - n;
            } else {
                bufferedOutputStream.write(buffer, 0, n);
                signalAway -= n;
            }

            updater.continueProgress(fileSize - sizeToRead, fileSize);
        }

        bufferedOutputStream.close();
        updater.endProgress(file);

    }
}
