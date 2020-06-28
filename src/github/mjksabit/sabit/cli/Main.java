package github.mjksabit.sabit.cli;


import java.io.IOException;
import java.util.Scanner;

public class Main {

    static final int listeningPort = 21212;
    static final int clientPort = 21210;
    static final int FTPPort = 21211;

    static final String SENDING_COMMAND = "SEND_FILE";
    static final String FINISHED_COMMAND = "STOP_SHARING";

    static final int BUFFER_SIZE = 512*1024; // 512 KB

    static final String REGEX_SPLITTER = "\n";


    public static Scanner scanner = new Scanner(System.in);

    public static char selectOption() {
        System.out.println(">> Exit");
        System.out.println(">> Send");
        System.out.println(">> Receive");


        String input = scanner.nextLine();
        return Character.toLowerCase(input.charAt(0));
    }

    public static void main(String[] args) {
        char option;
        String name;

        System.out.print("Enter Name: ");
        name = scanner.nextLine();

        while ((option = selectOption()) != 'e') {
            switch (option) {
                case 's': {
                    try (Sender sender = new Sender(name)) {

                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }

                    break;
                }
                case 'r': {
                    try (Receiver receiver = new Receiver(name)) {

                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }

                    break;
                }
            }
        }
    }
}
