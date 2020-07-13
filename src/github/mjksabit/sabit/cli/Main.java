package github.mjksabit.sabit.cli;


import github.mjksabit.autoconnect.ClientSide;
import github.mjksabit.sabit.core.Receiver;
import github.mjksabit.sabit.core.Sender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static Scanner scanner = new Scanner(System.in);

    static volatile ArrayList<Sender.ServerInfo> listReceivers;

    public static char selectOption() {
        System.out.println(">> Exit");
        System.out.println(">> Send");
        System.out.println(">> Receive");


        String input = scanner.nextLine();
        return Character.toLowerCase(input.charAt(0));
    }

    static int selectReceiver() {
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (0 > choice || choice >= listReceivers.size()) {
            System.err.println("Invalid Choice, Choose Between [0, " + listReceivers.size() + ")");
            return selectOption();
        }
        return choice;
    }

    public static void main(String[] args) {
        char option;
        String name;

        System.out.print("Enter Name: ");
        name = scanner.nextLine();

        while ((option = selectOption()) != 'e') {
            switch (option) {
                case 's': {
                    listReceivers = new ArrayList<>();
                    try (Sender sender = new Sender(name, (ClientSide.ServerInfo serverInfo) -> {
                        try {
                            Sender.ServerInfo info = new Sender.ServerInfo(serverInfo);
                            System.out.println(listReceivers.size() + "\t: " + info.getName());
                            listReceivers.add(info);
                        } catch (Exception e) {
                            System.err.println("Response Type Mismatch!");
                        }
                    });) {
                        sender.sendPresence();
                        Sender.ServerInfo data = listReceivers.get(selectReceiver());

                        Socket connectionSocket = new Socket(data.getAddress(), data.getPort());

                        String receiver = sender.makeConnection(connectionSocket, name);

                        System.out.println("Receiver: " + receiver);
                        System.out.println("==================================");

                        System.out.print("Enter File Path(s): ");

                        String filename;
                        while ((filename = scanner.nextLine()) != null) {
                            File file = new File(filename);

                            try {
                                sender.sendFile(file, new Progress());
                            } catch (FileNotFoundException e) {
                                break;
                            }
                        }

                        while (sender.isActive()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }

                    break;
                }
                case 'r': {
                    try (Receiver receiver = new Receiver(name)) {
                        System.out.println("Waiting for Receiver ...");
                        String senderName = receiver.waitForSender();

                        System.out.println("Sender: " + senderName);
                        System.out.println("====================================");

                        receiver.startReceiving(new Progress());

                        while (receiver.isActive()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }

                    break;
                }
            }
        }
    }
}
