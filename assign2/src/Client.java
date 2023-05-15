import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try {
            // Connect to the server
            Socket socket = new Socket("localhost", 8080);
            System.out.println("Connected to server");

            // Set up input and output streams
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Start a loop to send and receive messages
            // Start a loop to send and receive messages
            Scanner scanner = new Scanner(System.in);
            String message;

            while (true) {
                // Wait for user input
                System.out.print("Enter a message to send to the server: ");
                message = scanner.nextLine();

           /*     if (message.equals("quit")) {
                    // Send a message to the server to indicate that the connection should be closed
                    output.println("quit");
                    input.close();
                    output.close();
                    socket.close();

                    break;
                }*/

                // Send the message to the server
                output.println(message);

                // Wait for a response from the server
                String response = input.readLine();
                System.out.println("Received: " + response);
            }



        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}



