package github.mjksabit.sabit.core.ftp;

import java.io.File;

public interface IFTP {

    int BUFFER_SIZE = 512*1024; // 512 KB

    boolean isSending();

    void addToSend(String parentPath, File file, ProgressUpdater progress);

    void cancelSendingCurrent();

    void cancelSending();

    boolean isReceiving();

    void startReceiving(String savePath, ProgressUpdater updater);

    interface ProgressUpdater {

        void startProgress(File file);

        void continueProgress(long currentProgress, long totalProgress);

        void cancelProgress(File file);

        void endProgress(File file);
    }

    class FileData {
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
}
