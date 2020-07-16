package github.mjksabit.sabit.core.ftp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

public class ExtendedFTP implements IFTP {

    // Add Separator for Path in Windows/Linux

    DataInputStream in;
    DataOutputStream out;

    SimpleFTP simpleFTP;

    public ExtendedFTP(DataInputStream in, DataOutputStream out) {
        this.in = in;
        this.out = out;

        simpleFTP = new SimpleFTP(in, out);
    }

    @Override
    public boolean isSending() {
        return simpleFTP.isSending();
    }

    @Override
    public void addToSend(String parentPath, File file, ProgressUpdater progress) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();

            for (File iFile : files) {
                String childPath = parentPath+File.separator+file.getName();
                addToSend(childPath, iFile, progress);
            }
        }
        else simpleFTP.addToSend(parentPath, file, progress);
    }

    @Override
    public void cancelSendingCurrent() {
        simpleFTP.cancelSendingCurrent();
    }

    @Override
    public void cancelSendingAll() {
        simpleFTP.cancelSendingAll();
    }

    @Override
    public boolean isReceiving() {
        return simpleFTP.isReceiving();
    }

    @Override
    public void startReceiving(String savePath, ProgressUpdater updater) {
        simpleFTP.startReceiving(savePath, updater);
    }
}
