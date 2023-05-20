import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 27277;

    public Socket socket;

    public BufferedReader in;
    public PrintWriter out;

//    private static String userInput = null;
    private static final Object lock = new Object();
    private static boolean inputReceived = false;


    public boolean isLoggedIn = false;

    private String token = "0";
    public boolean failedConnection = false;

    public Client() {
        boolean retry = true;
        int retries = 0;
        int maxRetries = 5;
        while(retry){
            try {
                socket =  new Socket(SERVER_ADDRESS, SERVER_PORT);
                retry = false;
            } catch (IOException e) {

                retries++;
                System.out.println("[" + retries + "/" + maxRetries +  "] Cannot connect to Server. Is Server up? Waiting 5 seconds to retry again!");
                if( retries == maxRetries){
                    System.out.println("Max Connection Retries Exceeded. Is Server up?");
                    retry = false;
                    failedConnection = true;
                    return;
                }
                wait(5);
            }
        }

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {

        Client client = new Client();

        if(client.failedConnection)
            return;

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
            if (received == null){
                quit = true;
            }
            //handle login
            else if(received.equals("LOGIN")){
                client.login();
            }
            else if (received.equals("AUTH")) {
                client.auth();
            } else if (received.equals("CHECKALIVE")) {
                client.out.println("ALIVE");
            }

            else if(received.equals("GAME")) {
                client.out.println("PLAY");
                answers = new StringBuilder();
                inGame = true;
            }
            else if(received.equals("ENDGAME")){
                inGame = false;
            }

            else{
                if(inGame) {
                    if(received.equals("INPUT")){
                        String answer = getInputWithTimeout(5);
                        client.out.println(answer);
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

    public static String getInputWithTimeout(int timeoutSeconds) {
        final StringBuilder userInput = new StringBuilder(); // StringBuilder to hold the input
        System.out.print("\nPlease write answer number: ");
        synchronized (lock) {
            userInput.setLength(0); // Reset userInput before each invocation
            inputReceived = false;

            // Create a separate thread to wait for user input
            Thread inputThread = new Thread(() -> {
                try {
                    while (System.in.available() == 0) {
                        Thread.sleep(100); // Wait until input is available
                    }
                    while (System.in.available() > 0) {
                        userInput.append((char) System.in.read()); // Read input character by character
                    }

                    synchronized (lock) {
                        inputReceived = true;
                        lock.notify();
                    }
                } catch (IOException | InterruptedException e) {
                    // Handle exceptions

                }
            });

            // Start the input thread
            inputThread.start();

            // Wait for user input or timeout
            try {
                lock.wait(timeoutSeconds * 1000);
                if (!inputReceived) {
                    inputThread.interrupt();
                }
            } catch (InterruptedException e) {
                // Handle interruption
                Thread.currentThread().interrupt();
            }

            if (inputReceived) { // handle input
                String inputString = userInput.toString().trim();
                if (inputString.length() == 1 && Character.isDigit(inputString.charAt(0))) {
                    return inputString;
                } else {
                    return "E";
                }
            } else { // handle timeout
                System.out.println("No Answer! Remember you only have 5 seconds to answer!");
                return "T";
            }
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

    public void wait(int seconds)
    {
        try
        {
            Thread.sleep(seconds*1000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

}