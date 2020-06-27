package github.mjksabit.sabit.cli;

import java.io.*;

public class FileTransferProtocol {

    public interface ProgressUpdater {
        void startProgress(File file);

        void continueProgress(long currentProgress, long totalProgress);

        void endProgress(File file);
    }

    DataInputStream in;
    DataOutputStream out;

    public FileTransferProtocol(DataInputStream in, DataOutputStream out) {
        this.in = in;
        this.out = out;
    }

    void send(String parentPath, File file, ProgressUpdater updater) throws IOException {
        if (file.isFile()) {
            byte[] buffer = new byte[Main.BUFFER_SIZE];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

            out.writeUTF(Main.SENDING_COMMAND);
            out.writeUTF(parentPath+File.separator+file.getName());
            out.writeLong(file.length());

            int n;
            long currentProgress = 0, totalProgress = file.length();

            updater.startProgress(file);

            while ( (n =in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, n);
                out.flush();

                currentProgress += n;
                updater.continueProgress(currentProgress, totalProgress);
            }

            updater.endProgress(file);

            in.close();

        } else {
            File[] files = file.listFiles();

            for (File iFile : files) {
                String childPath = parentPath+File.separator+file.getName();
                send(childPath, iFile, updater);
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
