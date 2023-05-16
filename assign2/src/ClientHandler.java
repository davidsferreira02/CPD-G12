
    import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
    import java.util.ArrayList;
    import java.util.List;

    public class ClientHandler implements Runnable {
        private Socket clientSocket;
        private Server gameServer;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;

        public ClientHandler(Server gameServer, Socket clientSocket) {
            this.gameServer = gameServer;
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                inputStream = new ObjectInputStream(clientSocket.getInputStream());

                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

                // Perform authentication logic here

                // Enter the queue for the game
                gameServer.getClients().add(this);

                // Check if enough players are available to start a game
                if (gameServer.getClients().size() >= gameServer.getMaxPlayers()) {
                    // Form a team of players
                    List<Socket> teamSockets = new ArrayList<>();
                    for (int i = 0; i < gameServer.getMaxPlayers(); i++) {
                        ClientHandler client = gameServer.getClients().remove(0);
                        teamSockets.add(client.getClientSocket());
                    }

                    // Start the game
                    Game game = new Game(gameServer.getMaxPlayers(), teamSockets);
                    game.start();
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

