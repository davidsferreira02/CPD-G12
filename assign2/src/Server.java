import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 8000;
    private static final int MAX_PLAYERS = 4;
    private static final int MAX_GAMES = 5;

    private List<ClientHandler> clients;
    private ExecutorService threadPool;

    public Server() {
        clients = new ArrayList<>();
        threadPool = Executors.newFixedThreadPool(MAX_GAMES);
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a new client handler for the connected client
                ClientHandler clientHandler = new ClientHandler(this, clientSocket);
                clients.add(clientHandler);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public synchronized List<ClientHandler> getClients() {
        return clients;
    }

    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
