
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    protected Socket clientSocket = null;


    public static void main(String[] args) throws IOException {
        try {
            Socket clientSocket = new Socket("localhost", 9000);


            String userInput = "";
            while(!userInput.equals("END")) {
                OutputStream output = clientSocket.getOutputStream();
                InputStream input = clientSocket.getInputStream();
                Scanner userInputScanner = new Scanner(System.in);
                System.out.print("Input: ");
                userInput = userInputScanner.nextLine();

                DataOutputStream data = new DataOutputStream(output);
                data.writeUTF(userInput);

                DataInputStream dataReceived = new DataInputStream(input);
                System.out.println("Received: " + dataReceived.readUTF());
            }



        } catch (IOException e) {
            throw new IOException("Failed to connect to Server", e);
        }

    }
}
