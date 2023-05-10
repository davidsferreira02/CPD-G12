import java.net.Socket;
import java.util.List;

public class Game {

        private int players;
        private List<Socket> userSockets;

        Server server=new Server(9000);
        public Game(int players, List<Socket> userSockets) {
            this.userSockets = userSockets;

        }


        public void start() {
            // Code to start the game
            server.run();
            System.out.println("Starting game with " + userSockets.size() + " players");

        }

}

