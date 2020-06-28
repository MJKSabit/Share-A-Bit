package github.mjksabit.sabit.core;

import github.mjksabit.sabit.core.ftp.ExtendedFTP;
import github.mjksabit.sabit.core.ftp.IFTP;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public abstract class Connection implements Closeable{
    private static final String PARENT_PATH = ".";
    protected String fileSaveDirectory = PARENT_PATH;

    String name;
    Socket connectionSocket;
    DataOutputStream outputStream = null;
    DataInputStream inputStream = null;
    IFTP ftp = null;

    public Connection(String name) {
        this.name = name;
    }

    protected String makeConnection(Socket connectionSocket, String connectionText) throws IOException {
        this.connectionSocket = connectionSocket;

        outputStream = new DataOutputStream(connectionSocket.getOutputStream());
        inputStream = new DataInputStream(connectionSocket.getInputStream());

        outputStream.writeUTF(connectionText);
        outputStream.flush();
        String responseText = inputStream.readUTF();

        ftp = new ExtendedFTP(inputStream, outputStream);
        return responseText;
    }

    public String getFileSaveDirectory() {
        return fileSaveDirectory;
    }

    public void setFileSaveDirectory(String fileSaveDirectory) {
        this.fileSaveDirectory = fileSaveDirectory;
    }

    public void stopSending(File file) {
        // NEEDS IMPLEMENTATION
    }

    public void stopSending() {
        // NEEDS IMPLEMENTATION
    }

    public void sendFile(File file, IFTP.ProgressUpdater progressUpdater) throws FileNotFoundException, SocketException {
        if (outputStream == null) throw new SocketException("Connection Not Established");

        if (!file.exists())
            throw new FileNotFoundException("Invalid File Path");

        ftp.addToSend(PARENT_PATH, file, progressUpdater);
    }

    public void startReceiving(IFTP.ProgressUpdater progressUpdater) throws SocketException {
        if (inputStream == null) throw new SocketException("Connection Not Established");
        ftp.startReceiving(fileSaveDirectory, progressUpdater);
    }


    @Override
    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
    }
}
