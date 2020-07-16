package github.mjksabit.sabit.cli;

import github.mjksabit.sabit.core.ftp.SimpleFTP;

import java.io.File;

public class Progress /*implements SimpleFTP.ProgressUpdater*/ {
//    final int bytePerMB = 1024*1024;
//
//    @Override
//    public void startProgress(File file) {
//        System.out.println("File\t: " + file.getName());
//    }
//
//    @Override
//    public void continueProgress(long currentProgress, long totalProgress) {
//        long percentage = currentProgress*100/totalProgress;
//        System.out.print("\r" + (currentProgress/bytePerMB) + "MB / " + (totalProgress/bytePerMB) + "MB (" + percentage + "%)");
//    }
//
//    @Override
//    public void cancelProgress(File file) {
//        System.out.println();
//        System.out.println("Canceled File: " + file.getName());
//    }
//
//    @Override
//    public void endProgress(File file) {
//        continueProgress(file.length(), file.length());
//        System.out.println();
//    }
}
