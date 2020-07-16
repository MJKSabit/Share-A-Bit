package github.mjksabit.sabit.core;

import github.mjksabit.autoconnect.ExceptionHandler;
import github.mjksabit.sabit.core.ftp.ExtendedFTP;
import github.mjksabit.sabit.core.ftp.IFTP;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

import static github.mjksabit.sabit.core.Constant.FINISHED_COMMAND;

public abstract class Connection implements Closeable {
    private static final String PARENT_PATH = ".";
    protected String fileSaveDirectory = PARENT_PATH;

    public static final String REGEX_SPLITTER = "\n";

    protected String name;
    protected ExceptionHandler exceptionHandler = Throwable::printStackTrace;

    protected Socket connectionSocket;
    protected DataOutputStream outputStream = null;
    protected DataInputStream inputStream = null;


    IFTP ftp = null;

    public Connection(String name) {
        this.name = name;
    }

    public Connection(String name, ExceptionHandler exceptionHandler) {
        this.name = name;
        this.exceptionHandler = exceptionHandler;
    }

    public String makeConnection(Socket connectionSocket, String firstConnectionText) throws IOException {
        this.connectionSocket = connectionSocket;

        outputStream = new DataOutputStream(connectionSocket.getOutputStream());
        inputStream = new DataInputStream(connectionSocket.getInputStream());

        outputStream.writeUTF(firstConnectionText);
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
        ftp.cancelSendingAll();
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

    public boolean isSending() {
        return ftp.isSending();
    }

    @Override
    public void close() throws IOException {
        if (outputStream != null) {
            outputStream.writeUTF(FINISHED_COMMAND);
            outputStream.close();
        }
        if (inputStream != null) inputStream.close();
    }
}
