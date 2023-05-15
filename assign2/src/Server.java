import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Server {

    private static final int PORT = 8080;
    private static final int NUM_PLAYERS = 4;

    private ServerSocket serverSocket;
    private List<MyThread> threads;
    private List<Socket> userSockets;

    public Server() throws IOException {
        userSockets = new ArrayList<>();
        threads = new ArrayList<>();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            Game game = new Game(NUM_PLAYERS, userSockets);

            while (threads.size() < NUM_PLAYERS) {
                Socket socket = serverSocket.accept();
                MyThread thread = new MyThread(socket,game);
                threads.add(thread);
                userSockets.add(socket);
                thread.start();
                System.out.println("Player connected: " + socket.getInetAddress());
            }

            System.out.println("All playaers connected. Starting game...");

            // Start the game

            game.start();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }

}
