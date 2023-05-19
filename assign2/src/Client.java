import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
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

    private String token = "0";

    public Client() throws IOException {
        socket =  new Socket(SERVER_ADDRESS, SERVER_PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public static void main(String[] args) throws IOException {

        Client client = new Client();
        client.loadToken();


        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;
        String received;

        boolean quit = false;
        boolean inGame = false;
        StringBuilder answers = new StringBuilder();
        // Send messages to the server
        while (!quit) {
            received = client.serverMessage();
            System.out.println(received);

            //handle login
            if(received.equals("LOGIN")){
                client.login();
            }
            else if (received.equals("AUTH")) {
                client.auth();
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
        //receive login success
        if(serverMessage().equals("LOGINOK")) {
            System.out.println("Login successful!");
            token = serverMessage();
            saveToken();
        }
        else{
            System.out.println("Login Failed!");
        }
        System.out.println(serverMessage());

        System.out.println();
    }

    public void loadToken() {
        String filePath = "assign2/src/token.txt";
        File file = new File(filePath);

        if (!file.exists()) {
            try {
                FileWriter writer = new FileWriter(file);
                writer.write("0"); // Write default token to the file
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create the file: " + filePath, e);
            }
        }
        else {
            Scanner scanner = null;
            try {
                scanner = new Scanner(file);
                this.token = scanner.nextLine();
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to read the file: " + filePath, e);
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        }
    }

    public void saveToken() {
        String filePath = "assign2/src/token.txt";
        File file = new File(filePath);

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(token); // Write default token to the file
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create the file: " + filePath, e);
        }

    }
    public void auth() {
        out.println(token);
        String message = serverMessage();
        if(message.equals("AUTHOK"))
            System.out.println("Auth Token Successful");
        else {
            System.out.println("Auth Token Failed");
        }
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

    public String serverMessage() {
        String message = null;
        //inputStream
        try {
            socket.setSoTimeout(60000);
            message = in.readLine();
        } catch (IOException e) {
            //TODO STOP
            //stop();
        }

        return message;
    }

}