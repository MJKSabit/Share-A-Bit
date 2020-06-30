package github.mjksabit.sabit.core;

import github.mjksabit.sabit.cli.Main;
import github.mjksabit.sabit.core.ftp.ExtendedFTP;
import github.mjksabit.sabit.core.ftp.IFTP;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public abstract class Connection implements Closeable {
    private static final String PARENT_PATH = ".";
    protected String fileSaveDirectory = PARENT_PATH;

    public static final String SENDING_COMMAND = "SEND_FILE";
    public static final String FINISHED_COMMAND = "STOP_SHARING";

    public static final String REGEX_SPLITTER = "\n";

    public static final int listeningPort = 21212;
    public static final int clientPort = 21210;
    public static final int FTPPort = 21211;

    String name;
    Socket connectionSocket;
    DataOutputStream outputStream = null;
    DataInputStream inputStream = null;
    IFTP ftp = null;

    public Connection(String name) {
        this.name = name;
    }

    public String makeConnection(Socket connectionSocket, String connectionText) throws IOException {
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

    public void cancelSendingCurrent() {
        ftp.cancelSendingCurrent();
    }

    public void cancelSending() {
        ftp.cancelSending();
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

    public boolean isActive() {
        return ftp.isReceiving() || ftp.isSending();
    }

    @Override
    public void close() throws IOException {
        outputStream.writeUTF(FINISHED_COMMAND);
        inputStream.close();
        outputStream.close();
    }
}
