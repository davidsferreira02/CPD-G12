import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 27277;

    public Socket socket;

    public BufferedReader in;
    public PrintWriter out;

    private static String userInput = null;
    private static final Object lock = new Object();

    public boolean isLoggedIn = false;


    public Client() throws IOException {
        socket =  new Socket(SERVER_ADDRESS, SERVER_PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();


        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;
        String received;

        boolean quit = false;
        boolean inGame = false;
        StringBuilder answers = new StringBuilder();
        // Send messages to the server
        while (!quit) {
            received = client.in.readLine();

            //handle login
            if(received.equals("LOGIN")){
                client.login();
            }
            /*else if(received.equals("INPUT")){
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                client.out.println(input);
            }*/
            else if(received.equals("GAME")) {
                client.out.println("PLAY");
                answers = new StringBuilder();
                inGame = true;
            }
            else if(received.equals("ENDGAME")){
                inGame = false;
                //send answers
                client.out.println(answers);
            }

            else{
                if(inGame) {
                    if(received.equals("INPUT")){
                        String answer = handleInputTimeout();
                        answers.append(answer);
                    }

                    else {
                        System.out.println(received);
                    }
                }
                else {
                    System.out.println(received);
                }
            }

        }



        // Close the resources
        client.in.close();
        client.out.close();
        stdIn.close();
        client.socket.close();
    }

    public void login() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        out.println(username);
        out.println(password);
        System.out.println();
        System.out.println(in.readLine());
        System.out.println();
    }

    public static String handleInputTimeout() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter your input within 20 seconds:");

        Thread inputThread = new Thread(() -> {
            synchronized (lock) {
                if (scanner.hasNextLine()) {
                    userInput = scanner.nextLine();
                    lock.notifyAll();
                }
            }
        });

        inputThread.start();

        synchronized (lock) {
            try {
                lock.wait(5000); // Wait for 20 seconds or until notified
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (userInput != null) { // handle input
            if (userInput.length() == 1 && Character.isDigit(userInput.charAt(0))){
                scanner.close();
                return userInput;
            }
            else{
                scanner.close();
                return "E";
            }
        } else { //handle timeout
            scanner.close();
            return "T";
        }

    }

}