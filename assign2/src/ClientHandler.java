import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Server gameServer;
    private BufferedReader inputStream;
    private PrintWriter outputStream;

    public ClientHandler(Server gameServer, Socket clientSocket) {
        this.gameServer = gameServer;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new PrintWriter(clientSocket.getOutputStream(), true);

            // Perform authentication logic here

            // Enter the queue for the game
            gameServer.incrementConnectedClients();

            // Read and print messages from the client
            String message;
            while ((message = inputStream.readLine()) != null) {
                System.out.println("Received message: " + message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
                clientSocket.close();
                gameServer.removeClient(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}
