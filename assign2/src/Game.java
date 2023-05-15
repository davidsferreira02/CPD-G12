import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game {

    private int numPlayers;
    private List<MyThread> threads;
    private List<Socket> userSockets;

    public Game(int numPlayers, List<Socket> userSockets) throws IOException {
        this.numPlayers = numPlayers;
        this.userSockets = userSockets;
        threads = new ArrayList<MyThread>();
        for (Socket socket : userSockets) {
            addPlayer(socket);
        }
    }

    public synchronized void addPlayer(Socket socket) throws IOException {
        if (threads.size() < numPlayers) {
            MyThread thread = new MyThread(socket,this);
            threads.add(thread);
            userSockets.add(socket);
            thread.start();
            System.out.println("Player connected: " + socket.getInetAddress());
        } else {
            System.out.println("Game is full, cannot add more players");
        }
    }


    public void start() {
        // Code to start the game
        System.out.println("Starting game with " + threads.size() + " players");
        // ...
    }

    public void stop() {
        // Stop all threads
        for (MyThread thread : threads) {
            thread.interrupt();
        }
    }

    public synchronized void removeThread(MyThread thread) {
        threads.remove(thread);
    }


    public void sendToAllClients(String message) {
        for (MyThread thread : threads) {
            thread.sendMessage(message);
        }
    }


}