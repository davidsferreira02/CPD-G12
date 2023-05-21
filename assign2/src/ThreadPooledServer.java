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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Math.abs;

public class ThreadPooledServer implements Runnable{

    private static final int MAX_PLAYERS = 2;
    private static final int MAX_QUEUE_PLAYERS = 20;
    private static final int MAX_GAMES = 5;


    protected int          serverPort   = 27277;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected ExecutorService threadPool;
    protected ExecutorService gamePool;


    private ArrayList<Player> queue = new ArrayList<>();
    private ArrayList<Player> mergeQueue = new ArrayList<>();

    private ArrayList<Player> queueDiv1 = new ArrayList<>();
    private ArrayList<Player> queueDiv2 = new ArrayList<>();
    private ArrayList<Player> queueDiv3 = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Game> activeGames = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();
    private int queueType;


    public ThreadPooledServer(int port, int queueType){
        this.serverPort = port;
        this.queueType = queueType;
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        threadPool = Executors.newFixedThreadPool(MAX_QUEUE_PLAYERS);
        gamePool = Executors.newFixedThreadPool(MAX_GAMES);

        loadPlayers("players");
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
                        new ClientHandler(clientSocket, this.queue, this.queueDiv1, this.queueDiv2, this.queueDiv3, this.players, lock, this.queueType));

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


            if(queueType == 0){
                System.out.println("\n-------------- Queue --------------\n");
                for (Player player : queue) {
                    System.out.println("User: " + player.getUsername() + " - Waiting: " + (Instant.now().getEpochSecond() - player.getTimestampQueue()) / 60 + " Minutes");
                }
            } else if (queueType == 1) {
                System.out.println("\n-------------- Queue Division 1 --------------\n");
                for (Player player : queueDiv1) {
                    System.out.println("User: " + player.getUsername() + " - Waiting: " + (Instant.now().getEpochSecond() - player.getTimestampQueue()) / 60 + " Minutes");
                }
                System.out.println("\n-------------- Queue Division 2 --------------\n");
                for (Player player : queueDiv2) {
                    System.out.println("User: " + player.getUsername() + " - Waiting: " + (Instant.now().getEpochSecond() - player.getTimestampQueue()) / 60 + " Minutes");
                }
                System.out.println("\n-------------- Queue Division 3 --------------\n");
                for (Player player : queueDiv3) {
                    System.out.println("User: " + player.getUsername() + " - Waiting: " + (Instant.now().getEpochSecond() - player.getTimestampQueue()) / 60 + " Minutes");
                }
            }

            System.out.println("\n-----------------------------------\n");

            System.out.print("\n[CHECKGAMESTART] ");

            if(queueType == 1) {
                if(queueDiv1.size() >= MAX_PLAYERS) {
                    List<Player> gamePlayers = new ArrayList<>(queueDiv1.subList(0, MAX_PLAYERS));
                    queueDiv1.subList(0, MAX_PLAYERS).clear();
                    queue.addAll(gamePlayers);
                }
                if(queueDiv2.size() >= MAX_PLAYERS) {
                    List<Player> gamePlayers = new ArrayList<>(queueDiv2.subList(0, MAX_PLAYERS));
                    queueDiv2.subList(0, MAX_PLAYERS).clear();
                    queue.addAll(gamePlayers);
                }
                if(queueDiv3.size() >= MAX_PLAYERS) {
                    List<Player> gamePlayers = new ArrayList<>(queueDiv3.subList(0, MAX_PLAYERS));
                    queueDiv3.subList(0, MAX_PLAYERS).clear();
                    queue.addAll(gamePlayers);
                }
                checkPlayerMaxWaiting(5);

            }

            //Start game
            if (queue.size() >= MAX_PLAYERS/* && activeGames.size() <= MAX_GAMES*/) {
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
                if(this.queueType == 0)
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
                        player.updateDivision();
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

    public synchronized void checkPlayerMaxWaiting(int thresholdTime) {

        mergeQueues();

        int maxNearbyPlayers = MAX_PLAYERS-1; // Maximum number of nearby players to consider

        // Checking if there is a player with time greater than the threshold
        boolean hasPlayerWithThresholdTime = mergeQueue.stream()
                .anyMatch(player -> player.getTime() > thresholdTime);

        if (hasPlayerWithThresholdTime && mergeQueue.size() >= MAX_PLAYERS) {

            Player selectedPlayer = null;

            // Finding the player with the longest waiting time
            for (Player player : mergeQueue) {
                if (selectedPlayer == null || player.getTime() > selectedPlayer.getTime()) {
                    selectedPlayer = player;
                }
            }

            // Finding players with ranks near the selected player

            int selectedPlayerIndex = mergeQueue.indexOf(selectedPlayer);
            int rank = selectedPlayer.getRank();
            int count = 0;
            int i = 0;

            int rankDiff = 0;
            // Find nearby players with the same rank, rank difference of 1, and rank difference of 2
            while (count < maxNearbyPlayers && i < mergeQueue.size()) {
                Player player = mergeQueue.get(i);
                int playerRank = player.getRank();
                if (abs(playerRank - rank) == rankDiff && !player.equals(selectedPlayer) && !queue.contains(player)) {
                    queue.add(player);
                    removePlayerFromQueueDivision(player);
                    count++;
                }
                i++;
                if(i >= mergeQueue.size() && count < maxNearbyPlayers && rankDiff < 3){
                    i = 0;
                    rankDiff++;
                }
            }

        } else {
//            System.out.println("No player with time greater than " + thresholdTime + " found.");
        }
        mergeQueue.clear();
    }

    public void splitPlayersByRanking(Player player){

        if(player.getDivision() == 1){
            //1rd Division
            queueDiv1.add(player);
        }
        else if(player.getDivision() == 2){
            //2nd Division
            queueDiv2.add(player);
        }
        else{
            //3st Division
            queueDiv3.add(player);
        }
    }

    public void removePlayerFromQueueDivision(Player player) {
        if(player.getDivision() == 1) {
            queueDiv1.remove(player);
        }
        else if(player.getDivision() == 2) {
            queueDiv2.remove(player);
        }
        else if(player.getDivision() == 3) {
            queueDiv3.remove(player);
        }
    }

    public void mergeQueues() {
        mergeQueue.addAll(queueDiv3);
        mergeQueue.addAll(queueDiv2);
        mergeQueue.addAll(queueDiv3);
    }

    public void printQueueServerLogs(){
                if(queueType == 0)
            System.out.println("UNRANKED QUEUE: " + queue.size());
        else {
            System.out.println("FIRST DIV QUEUE: " + queueDiv1.size());
            System.out.println("SECOND DIV QUEUE: " + queueDiv2.size());
            System.out.println("THIRD DIV QUEUE: "  + queueDiv3.size());
        }

    }






}