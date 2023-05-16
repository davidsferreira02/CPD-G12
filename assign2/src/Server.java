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

    private List<ClientHandler> clients;
    private ExecutorService threadPool;
    private int connectedClients;

    public Server() {
        clients = new ArrayList<>();
        threadPool = Executors.newFixedThreadPool(MAX_PLAYERS);
        connectedClients = 0;
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

    public synchronized void incrementConnectedClients() {
        connectedClients++;
        if (connectedClients >= MAX_PLAYERS) {
            // Form a team of players
            List<Socket> teamSockets = new ArrayList<>();
            for (int i = 0; i < MAX_PLAYERS; i++) {
                ClientHandler client = clients.remove(0);
                teamSockets.add(client.getClientSocket());
            }

            // Start the game
            Game game = new Game(MAX_PLAYERS, teamSockets);
            game.start();
        }
    }

    public synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        connectedClients--;
    }

    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
