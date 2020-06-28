package github.mjksabit.sabit.cli.ftp;

import github.mjksabit.sabit.cli.Main;
import javafx.util.Pair;

import java.io.*;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Queue;
import java.util.concurrent.*;

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
                        sendQueue.remove();
                    }
                }
                sendSemaphore.release();
            });
        }
    }

    private void send(FileData data) throws IOException {
        if (data.getFile().isFile()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(data.getFile()));

            out.writeUTF(Main.SENDING_COMMAND);
            out.writeUTF(data.getParentPath()+File.separator+data.getFile().getName());
            out.writeLong(data.getFile().length());

            int n;
            long currentProgress = 0, totalProgress = data.getFile().length();

            data.getProgress().startProgress(data.getFile());

            while ( (n =in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, n);
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
                            if (in.readUTF().equals(Main.FINISHED_COMMAND)) break;
                        } catch (IOException e) {
                            System.err.println("Command Mismatch! (from other client)");
                        }

                        try {
                            receive(savePath, updater);
                        } catch (IOException e) {
                            System.err.println("Can Not Receive : " + e.getLocalizedMessage());
                        }
                    }
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
        int n;

        updater.startProgress(file);

        for (long sizeToRead = fileSize;
             (n = in.read(buffer, 0, (int) Math.min(buffer.length, sizeToRead))) != -1 &&
                     sizeToRead > 0; sizeToRead -= n) {

            bufferedOutputStream.write(buffer, 0, n);
            updater.continueProgress(fileSize - sizeToRead, fileSize);
        }

        updater.endProgress(file);

        bufferedOutputStream.close();
    }
}
