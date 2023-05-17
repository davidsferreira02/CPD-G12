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
        // Send messages to the server
        while (!quit) {
            received  = client.in.readLine();

            //handle login
            if(received.equals("LOGIN")){
                client.login();
            }
            else if(received.equals("INPUT")){
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                client.out.println(input);
            }
            else{
                System.out.println(received);
            }



            /*System.out.print("Enter a message to send to the server (or 'quit' to exit): ");
            userInput = stdIn.readLine();
            client.out.println(userInput);
            if ("quit".equals(userInput)) {
                quit = true;
            }*/
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

}