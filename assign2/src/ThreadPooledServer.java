import java.io.File;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPooledServer implements Runnable{

    private static final int MAX_PLAYERS = 1;
    private static final int MAX_QUEUE_PLAYERS = 20;
    private static final int MAX_GAMES = 5;


    protected int          serverPort   = 27277;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected ExecutorService threadPool;
    protected ExecutorService gamePool;


    private ArrayList<Player> queue = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Game> activeGames = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();


    public ThreadPooledServer(int port){
        this.serverPort = port;
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        threadPool = Executors.newFixedThreadPool(MAX_QUEUE_PLAYERS);
        gamePool = Executors.newFixedThreadPool(MAX_GAMES);

        loadPlayers("assign2/src/players");
        /*for( Player player : players) {
            player.printPlayer();
        }*/
        openServerSocket();
        while(!isStopped()){
            checkGameStart();
            Socket clientSocket = null;
            try {
                serverSocket.setSoTimeout(5000);
                clientSocket = this.serverSocket.accept();
                System.out.println("[SERVER] New Connection: " + clientSocket);
                //waiting for thread pool
                this.threadPool.execute(
                        new ClientHandler(clientSocket, this.queue, this.players, lock));

            } catch(SocketTimeoutException e){

            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("[SERVER] Server Stopped.") ;
                    break;
                }
                throw new RuntimeException(
                        "[SERVER] Error accepting client connection", e);
            }

        }
        this.gamePool.shutdown();
        this.threadPool.shutdown();
        System.out.println("[SERVER] Server Stopped.") ;
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

    private synchronized void checkGameStart() {
        //user lock to protect concurrency in queue and players ArrayLists

        try {
            //verify if all players all still in queue - remove disconnected
            checkPlayersAlive();

            //lock to protect queue and players
            lock.lock();
            System.out.println("[SERVER] QUEUE: " + queue.size());
            System.out.println("[SERVER] Active Games: " + activeGames.size());

            System.out.println("\n-------------- Queue --------------\n");
            for (Player player : queue) {
                System.out.println("User: " + player.getUsername() + " - Waiting: " + (Instant.now().getEpochSecond() - player.getTimestampQueue()) / 60 + " Minutes");
            }
            System.out.println("\n-----------------------------------\n");

            System.out.print("\n[CHECKGAMESTART] ");

            if (queue.size() >= MAX_PLAYERS && activeGames.size() <= MAX_GAMES) {
                List<Player> gamePlayers = new ArrayList<>(queue.subList(0, MAX_PLAYERS));
                queue.subList(0, MAX_PLAYERS).clear();
                Game game = new Game(gamePlayers);
                activeGames.add(game);
                if(activeGames.size() >= MAX_GAMES)
                    System.out.println("Game Server is full, Waiting for Server..");

                //unlock
                lock.unlock();
                gamePool.execute(() -> {
                    try {
                        game.setLockObject(lock);
                        game.run();
                        activeGames.remove(game);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                System.out.print("Not enough players: " + queue.size() + "\n");
            }
        } finally {
            if(lock.isHeldByCurrentThread())
                lock.unlock();
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
                        /*if(player.getToken().equals("0")) {
                            player.generateToken(60);
                        }
                        if(player.getTokenLimit() >= Instant.now().getEpochSecond()/60)*/
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

    public void checkPlayersAlive(){
        Iterator<Player> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            player.getOutputStream().println("CHECKALIVE");
            String message;
            try {
                player.getSocket().setSoTimeout(1);
                message = player.getInputStream().readLine();
            } catch (IOException e) {
                System.out.println("Player Disconnected: " + player.getUsername());
                message = null;
                player.setConnnectionDEAD();
                iterator.remove();  // Remove the player using the iterator
            } finally {
                try {
                    player.getSocket().setSoTimeout(0);
                } catch (SocketException e) {
                }
            }
        }
    }
}