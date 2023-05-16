import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8000;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;

        boolean quit = false;
        // Send messages to the server
        while (!quit) {
            System.out.print("Enter a message to send to the server (or 'quit' to exit): ");
            userInput = stdIn.readLine();
            out.println(userInput);
            if ("quit".equals(userInput)) {
                quit = true;
            }
        }

        // Close the resources
        in.close();
        out.close();
        stdIn.close();
        socket.close();
    }
}
