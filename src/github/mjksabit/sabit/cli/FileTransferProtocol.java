package github.mjksabit.sabit.cli;

import java.io.*;

public class FileTransferProtocol {

    static void send(File file, DataOutputStream out) throws IOException {
        if (file.isFile()) {
            byte[] buffer = new byte[1024];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

            out.writeUTF(file.getName());
            out.writeLong(file.length());

            int n;
            while ( (n =in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, n);
                out.flush();
            }

            in.close();
        }
    }

    static void receive(String parentPath, DataInputStream in) throws IOException {
        String fileName = in.readUTF();
        long fileSize = in.readLong();

        File file = new File(parentPath + File.separator + fileName);
        System.out.println("Saving at: " + file.getAbsolutePath());

        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));

        byte[] buffer = new byte[1024];
        int n;
        for (long sizeToRead = fileSize;
             (n = in.read(buffer, 0, (int) Math.min(buffer.length, sizeToRead))) != -1 &&
                     sizeToRead>0; sizeToRead-=n) {

            System.out.print("\r" + (fileSize - sizeToRead) + " out of " + fileSize);

            bufferedOutputStream.write(buffer, 0, n);
        }

        bufferedOutputStream.close();
    }
}
