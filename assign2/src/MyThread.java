/*import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MyThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Game game;

    public MyThread(Socket socket, Game game) throws IOException {
        this.socket = socket;
        this.game = game;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                String message = in.readLine();
                if (message == null) {
                    break;
                }
                System.out.println("Received message: " + message);
                game.sendToAllClients(message);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e);
        } finally {
            game.removeThread(this);
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e);
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public Socket getSocket() {
        return socket;
    }
}
*/