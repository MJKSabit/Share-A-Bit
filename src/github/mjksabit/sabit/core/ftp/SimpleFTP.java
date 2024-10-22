package github.mjksabit.sabit.core.ftp;

import java.io.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;

import static github.mjksabit.sabit.core.Constant.FINISHED_COMMAND;
import static github.mjksabit.sabit.core.Constant.SENDING_COMMAND;

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
    public void cancelSendingCurrent() {
        synchronized (sendQueue) {
            System.out.println("Cancel");
            if (!sendQueue.isEmpty()) sendQueue.peek().setCancelled(true);
        }
    }

    @Override
    public void cancelSendingAll() {
        synchronized (sendQueue) {
            if (!sendQueue.isEmpty()) sendQueue.peek().setCancelled(true);
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
            int signalAway = BUFFER_SIZE;

            data.getProgress().startProgress(data.getFile(), data.getParentPath(), data.getFile().length());

            while ( (n = in.read(buffer, 0, Math.min(buffer.length, signalAway)) ) != -1) {
                out.write(buffer, 0, n);

                signalAway -= n;
                if (signalAway == 0) {
                    out.writeByte(data.isCancelled() ? 0 : 1);
                    signalAway = BUFFER_SIZE;
                }

                currentProgress += n;
                data.getProgress().continueProgress(currentProgress);

                if (data.isCancelled()) break;
            }

            in.close();
            out.flush();
            if (data.isCancelled()) data.getProgress().cancelProgress();
            else data.getProgress().endProgress();
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
                            System.err.println("Can Not Receive");
                            e.printStackTrace();
                            break;
                        }
                    }
                    receiveSemaphore.release();
                });
            } catch (InterruptedException ignored) {
            }
        }
    }

    private String pathChanger(String unknownSource) {
        StringBuilder builder = new StringBuilder();
        char replace = '\\'==File.separatorChar ? '/' : '\\';

        for (int i=0; i<unknownSource.length(); i++) {
            if (unknownSource.charAt(i) == replace)
                builder.append(File.separatorChar);
            else
                builder.append(unknownSource.charAt(i));
        }

        return builder.toString();
    }

    private void receive(String savePath, ProgressUpdater updater) throws IOException {
        String fileName = in.readUTF();
        long fileSize = in.readLong();

        // Replace File Separator with System File Separator
        fileName = pathChanger(fileName);

        File file = new File(savePath + File.separator + fileName);

        try {
            file.getParentFile().mkdirs();
        } catch (NullPointerException ignore) {}

        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));

        byte[] buffer = new byte[BUFFER_SIZE];
        int n, signalAway = BUFFER_SIZE;
        byte signal = 1;

        updater.startProgress(file, savePath, fileSize);

        for (long sizeToRead = fileSize; signal != 0 && sizeToRead > 0 &&
             (n = in.read(buffer, 0, (int) Math.min(signalAway, sizeToRead))) != -1; sizeToRead -= n) {

            bufferedOutputStream.write(buffer, 0, n);
            signalAway -= n;

            if(signalAway==0) {
                signal = in.readByte();
                signalAway = BUFFER_SIZE;
            }

            updater.continueProgress(fileSize - sizeToRead);
        }

        bufferedOutputStream.close();
        if (signal == 1) updater.endProgress();
        else updater.cancelProgress();

    }
}
