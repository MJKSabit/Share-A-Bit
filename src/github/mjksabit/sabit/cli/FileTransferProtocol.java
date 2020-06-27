package github.mjksabit.sabit.cli;

import java.io.*;

public class FileTransferProtocol {

    static void send(String parentPath, File file, DataOutputStream out) throws IOException {
        if (file.isFile()) {
            byte[] buffer = new byte[Main.BUFFER_SIZE];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

            out.writeUTF(Main.SENDING_COMMAND);
            out.writeUTF(parentPath+File.separator+file.getName());
            out.writeLong(file.length());

            int n;
            while ( (n =in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, n);
                out.flush();
            }

            in.close();
        } else {
            File[] files = file.listFiles();

            for (File iFile : files)
                send(parentPath+File.separator+file.getName(), iFile, out);
        }
    }

    static void receive(String parentPath, DataInputStream in) throws IOException {
        while (!in.readUTF().equals(Main.FINISHED_COMMAND)) {
            String fileName = in.readUTF();
            long fileSize = in.readLong();

            File file = new File(parentPath + File.separator + fileName);

            file.getParentFile().mkdir();

            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));

            byte[] buffer = new byte[Main.BUFFER_SIZE];
            int n;
            for (long sizeToRead = fileSize;
                 (n = in.read(buffer, 0, (int) Math.min(buffer.length, sizeToRead))) != -1 &&
                         sizeToRead > 0; sizeToRead -= n) {

                System.out.print("\r" + (fileSize - sizeToRead) + " out of " + fileSize);

                bufferedOutputStream.write(buffer, 0, n);
            }

            System.out.println();
            System.out.println("Saved at: " + file.getAbsolutePath());

            bufferedOutputStream.close();
        }
    }
}
