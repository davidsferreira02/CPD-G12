import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class Authentication implements Runnable {
    private Socket clientSocket;
    private Queue<Socket> gameQueue;

    public Authentication(Socket clientSocket, Queue<Socket> gameQueue) {
        this.clientSocket = clientSocket;
        this.gameQueue=gameQueue;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {

            String username = in.readLine();
            String password = in.readLine();

            // Authenticate user
            boolean isAuthenticated = authenticateUser(username, password);


            System.out.println(isAuthenticated ? "Authenticated" : "Not authenticated");


            if (isAuthenticated) {
                gameQueue.add(clientSocket);
                System.out.println("Client added to game queue");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean authenticateUser(String username, String password) {

        Map<String, String> validUsers = new HashMap<>();
        validUsers.put("alice", "password1");
        validUsers.put("bob", "password2");
        validUsers.put("charlie", "password3");


        if (validUsers.containsKey(username) && validUsers.get(username).equals(password)) {

            return true;
        } else {

            return false;
        }
    }

}
