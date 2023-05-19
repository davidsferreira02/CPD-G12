import java.io.File;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Game> activeGames = new ArrayList<>();

    List<Player> unrankedqueue = new ArrayList<>();
    //Ranked Divisions
    List<Player> thirddiv = new ArrayList<>();
    List<Player> seconddiv = new ArrayList<>();
    List<Player> firstdiv = new ArrayList<>();

    //Auxiliary Lists (In order to avoid concurrent modification)
    List<Player> removePlayers = new ArrayList<>();


    public ThreadPooledServer(int port){
        this.serverPort = port;
    }

    //TODO IMPLEMENT WAITING TIME BEFORE MATCHING PLAYER WITH LOWER DIVISION PLAYERS

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        threadPool = Executors.newFixedThreadPool(MAX_QUEUE_PLAYERS);
        gamePool = Executors.newFixedThreadPool(MAX_GAMES);

        loadPlayers("src/players");
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
                System.out.println("New Connection: " + clientSocket);
                //waiting for thread pool
                this.threadPool.execute(
                        new ClientHandler(clientSocket, this.queue, this.players));

                System.out.println("Active Count: " + Thread.activeCount());
            } catch(SocketTimeoutException e){

            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    break;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }

        }
        this.gamePool.shutdown();
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

    private synchronized void checkGameStart() {

        List<Player> gamePlayers = new ArrayList<>();

        if(queue.size() >= MAX_PLAYERS && activeGames.size() < MAX_GAMES) {

            //Split players by Rank
            for(Player player : queue){
                if(player.getGamemode() == 0){
                    updateUnrankedqueue(player);
                }
                if(player.getGamemode() == 1){
                    splitPlayersByRanking(player);
                }
            }

            //Removes Players added to the unranked queue from the queue
            updateQueue(removePlayers, queue);
            removePlayers.clear();

            //Starts Unranked Games
            if(unrankedqueue.size() >= MAX_PLAYERS){
                gamePlayers = new ArrayList<>(unrankedqueue.subList(0, MAX_PLAYERS));
                unrankedqueue.subList(0, MAX_PLAYERS).clear();

                System.out.println("LAUNCHING UNRANKED MATCH");
                printQueueServerLogs();
            }

            /// ---- RANKED PLAY RELATED ---- ///

            //If there is enough players in each division start the ranked match with only players of that division
            if(thirddiv.size() >= MAX_PLAYERS){
                gamePlayers = new ArrayList<>(thirddiv.subList(0, MAX_PLAYERS));

                //Removes gamePlayers from the division queue as well as the connection queue
                thirddiv.subList(0, MAX_PLAYERS).clear();
                updateQueue(gamePlayers, queue);

                System.out.println("LAUNCHING THIRD DIV (ONLY) RANKED MATCH");
                printQueueServerLogs();

            }

            if(seconddiv.size() >= MAX_PLAYERS){

                gamePlayers = new ArrayList<>(seconddiv.subList(0, MAX_PLAYERS));

                //Removes gamePlayers from the division queue as well as the connection queue
                seconddiv.subList(0, MAX_PLAYERS).clear();
                updateQueue(gamePlayers, queue);

                System.out.println("LAUNCHING SECOND DIV (ONLY) RANKED MATCH");
                printQueueServerLogs();

            }

            if(firstdiv.size() >= MAX_PLAYERS){

                gamePlayers = new ArrayList<>(firstdiv.subList(0, MAX_PLAYERS));

                //Removes gamePlayers from the division queue as well as the connection queue
                firstdiv.subList(0, MAX_PLAYERS).clear();
                updateQueue(gamePlayers, queue);

                System.out.println("LAUNCHING FIRST DIV (ONLY) RANKED MATCH");
                printQueueServerLogs();

            }

            // Not enough players in the same division, so divisions have to be mixed
            if(firstdiv.size() + seconddiv.size() >= MAX_PLAYERS){

                int cntr = firstdiv.size();

                for(Player p : firstdiv){
                    gamePlayers.add(p);
                    removePlayers.add(p);
                }

                //Removes gamePlayers from the division queue as well as the connection queue
                updateQueue(removePlayers, firstdiv);
                updateQueue(removePlayers, queue);
                removePlayers.clear();

                for(Player p2 : seconddiv){
                    if(cntr > MAX_PLAYERS)
                        break;

                    gamePlayers.add(p2);
                    removePlayers.add(p2);
                    cntr++;
                }

                updateQueue(removePlayers, seconddiv);
                updateQueue(removePlayers, queue);
                removePlayers.clear();

                System.out.println("LAUNCHING FIRST DIV + SECOND DIV RANKED MATCH");
                printQueueServerLogs();

            }
            else if(seconddiv.size() + thirddiv.size() >= MAX_PLAYERS){

                int cntr = seconddiv.size();

                for(Player p : seconddiv){
                    gamePlayers.add(p);
                    removePlayers.add(p);
                }

                //Removes gamePlayers from the division queue as well as the connection queue
                updateQueue(removePlayers, seconddiv);
                updateQueue(removePlayers, queue);
                removePlayers.clear();

                for(Player p2 : thirddiv){
                    if(cntr > MAX_PLAYERS)
                        break;

                    gamePlayers.add(p2);
                    removePlayers.add(p2);
                    cntr++;
                }

                updateQueue(removePlayers, thirddiv);
                updateQueue(removePlayers, queue);
                removePlayers.clear();

                System.out.println("LAUNCHING SECOND DIV + THIRD DIV RANKED MATCH");
                printQueueServerLogs();

            }
            else if(firstdiv.size() + thirddiv.size() + seconddiv.size() >= MAX_PLAYERS){

                int cntr = firstdiv.size();

                for(Player p : firstdiv){
                    gamePlayers.add(p);
                    removePlayers.add(p);
                }

                updateQueue(removePlayers, firstdiv);
                updateQueue(removePlayers, queue);
                removePlayers.clear();

                cntr += seconddiv.size();

                for(Player p2 : seconddiv){
                    gamePlayers.add(p2);
                    removePlayers.add(p2);
                }

                updateQueue(removePlayers, seconddiv);
                updateQueue(removePlayers, queue);
                removePlayers.clear();

                for(Player p3 : thirddiv){
                    if(cntr > MAX_PLAYERS)
                        break;

                    gamePlayers.add(p3);
                    removePlayers.add(p3);
                }

                updateQueue(removePlayers, thirddiv);
                updateQueue(removePlayers, queue);
                removePlayers.clear();

                System.out.println("LAUNCHING FIRST DIV + SECOND DIV + THIRD DIV RANKED MATCH");
                printQueueServerLogs();

            }
            else{
                System.out.println("Not enough Players to start a ranked match! Waiting for a few more to join");
            }


            //Start game
            Game game = new Game(gamePlayers);
            activeGames.add(game);

            gamePool.execute(() -> {
                try {
                    game.run();
                    activeGames.remove(game);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
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

    //Rank Divisions
    //0-25 -> 3rd Division
    //25-50 -> 2nd Division
    //50-100 -> 1st Division
    public void splitPlayersByRanking(Player player){

        if(player.getRank() >= 0 && player.getRank() < 25){
            //3rd Division
            thirddiv.add(player);
        }
        else if(player.getRank() >= 25 && player.getRank() < 50){
            //2nd Division
            seconddiv.add(player);
        }
        else{
            //1st Division
            firstdiv.add(player);
        }

    }

    public void updateQueue(List<Player> players, List<Player> updatequeue){
        for(Player player: players){
            updatequeue.remove(player);
        }
    }

    public void updateUnrankedqueue(Player player){
        unrankedqueue.add(player);
        removePlayers.add(player);
    }

    public void printQueueServerLogs(){
        System.out.println("QUEUE LIST STAUS" + queue.size());
        System.out.println("QUEUE: " + queue.size());
        System.out.println("UNRANKED QUEUE: " + queue.size());
        System.out.println("THIRD DIV QUEUE: "  + thirddiv.size());
        System.out.println("SECOND DIV QUEUE: " + seconddiv.size());
        System.out.println("FIRST DIV QUEUE: " + firstdiv.size());
    }

}