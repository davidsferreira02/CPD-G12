import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPooledServer implements Runnable{

    protected int          serverPort   = 27277;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected ExecutorService threadPool =
            Executors.newFixedThreadPool(4);

    private ArrayList<Player> queue = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();

    public ThreadPooledServer(int port){
        this.serverPort = port;
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        loadPlayers("assign2/src/players");
        /*for( Player player : players) {
            player.printPlayer();
        }*/
        openServerSocket();
        while(! isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
                System.out.println("New Connection: " + clientSocket);
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    break;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }
            //waiting for thread pool
            this.threadPool.execute(
                    new ClientHandler(clientSocket, this.queue, this.players));
            System.out.println("Active Count: " + Thread.activeCount());
        }
        this.threadPool.shutdown();
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }



    private void loadPlayers(String path) {

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(path))) {
            for (Path filePath : directoryStream) {
                if (Files.isRegularFile(filePath)) {
                    // Read file name
                    String fileName = filePath.getFileName().toString();
                    // Read file content
                    try {
                        List<String> lines = Files.readAllLines(filePath);
                        String[] line = lines.get(0).split(":");
                        if(line.length < 5) {
                            throw new FileNotFoundException();
                        }
                        String username = fileName.replace(".txt", "");
                        String password = line[0];
                        int rank = Integer.parseInt(line[1]);
                        String token = line[2];
                        long tokenLimit = Integer.parseInt(line[3]);
                        long timestampQueue = Long.parseLong(line[4]);

                        Player player = new Player(username, password);
                        player.setRank(rank);
                        player.setToken(token);
                        player.setTokenLimit(tokenLimit);
                        player.setTimestampQueue(timestampQueue);

                        player.generateToken(60);

                        players.add(player);
                    } catch (IOException e) {
                        System.out.println("Error reading file: " + fileName);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading directory: " + path);
        }
    }
}