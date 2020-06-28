package github.mjksabit.sabit.cli;

import javafx.util.Pair;

import java.io.*;
import java.util.Queue;
import java.util.concurrent.*;

public class FileTransferProtocol {

    public interface ProgressUpdater {
        void startProgress(File file);

        void continueProgress(long currentProgress, long totalProgress);

        void endProgress(File file);
    }

    DataInputStream in;
    DataOutputStream out;

    ExecutorService sendThread;
    volatile Semaphore sendSemaphore = new Semaphore(1);
    ExecutorService receiveThread;

    public static class FileData {
        final String parentPath;
        final File file;
        final ProgressUpdater progress;
        volatile boolean isCancelled;

        public FileData(String parentPath, File file, ProgressUpdater progress) {
            this.parentPath = parentPath;
            this.file = file;
            this.progress = progress;
            isCancelled = false;
        }

        public void setCancelled(boolean cancelled) {
            isCancelled = cancelled;
        }

        public String getParentPath() {
            return parentPath;
        }

        public File getFile() {
            return file;
        }

        public ProgressUpdater getProgress() {
            return progress;
        }

        public boolean isCancelled() {
            return isCancelled;
        }
    }

    volatile Queue<FileData> sendQueue = new ConcurrentLinkedQueue<>();

    public FileTransferProtocol(DataInputStream in, DataOutputStream out) {
        this.in = in;
        this.out = out;

        sendThread = Executors.newSingleThreadExecutor();
        receiveThread = Executors.newSingleThreadExecutor();
    }

    public boolean isSending() {
        return sendSemaphore.availablePermits() == 0;
    }



    public void addToSend (String parentPath, File file, ProgressUpdater progress) {
        sendQueue.add(new FileData(parentPath, file, progress));

        if (!isSending()) {
            try {
                sendSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("New Thread Starting");
            sendThread.execute(() -> {
                System.out.println("New Thread");
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
            byte[] buffer = new byte[Main.BUFFER_SIZE];
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
            File[] files = data.getFile().listFiles();

            for (File iFile : files) {
                String childPath = data.getParentPath()+File.separator+data.getFile().getName();
                send(new FileData(childPath, iFile, data.getProgress()));
            }
        }
    }

    void receive(String parentPath, ProgressUpdater updater) throws IOException {
        while (!in.readUTF().equals(Main.FINISHED_COMMAND)) {
            String fileName = in.readUTF();
            long fileSize = in.readLong();

            File file = new File(parentPath + File.separator + fileName);

            try {
                file.getParentFile().mkdirs();
            } catch (NullPointerException ignore) {}

            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));

            byte[] buffer = new byte[Main.BUFFER_SIZE];
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
}
