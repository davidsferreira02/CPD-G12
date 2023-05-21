import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class ClientHandler implements Runnable{

    protected Socket clientSocket = null;

    private BufferedReader inputStream;
    private PrintWriter outputStream;
    private ArrayList<Player> queue;

    private ArrayList<Player> queueDiv1 = new ArrayList<>();
    private ArrayList<Player> queueDiv2 = new ArrayList<>();
    private ArrayList<Player> queueDiv3 = new ArrayList<>();

    private ArrayList<Player> players;

    boolean isLoggedIn = false;

    boolean isStopped = false;

    private Player player;

    private long startTime;
    private long nextTime;

    private ReentrantLock lock;

    private int queueType;

    public ClientHandler(Socket clientSocket, ArrayList<Player> queue, ArrayList<Player> queueDiv1, ArrayList<Player> queueDiv2, ArrayList<Player> queueDiv3, ArrayList<Player> players, ReentrantLock lock, int queueType) {
        this.clientSocket = clientSocket;
        this.queue   = queue;
        this.queueDiv1 = queueDiv1;
        this.queueDiv2 = queueDiv2;
        this.queueDiv3 = queueDiv3;
        this.players = players;
        this.lock = lock;
        this.queueType = queueType;
    }

    public void run() {
        try {
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new PrintWriter(clientSocket.getOutputStream(), true);

            outputStream.println("Connected to Trivia Server.");


            // Read and print messages from the client

            String receivedMessage;

            while(!isStopped){
                lock.lock();
                clientSocket.setSoTimeout(0);
                if(!isLoggedIn) {
                    System.out.println("Asking login: " + clientSocket);
                    login();
                }
                else if(player.getStatus().equals("QUEUE")) {
                    if(nextTime == Instant.now().getEpochSecond()){
                        outputStream.println("In queue...");
                        nextTime = Instant.now().plusSeconds(5).getEpochSecond();
                    }
                }
                else if(player.getStatus().equals("CHECKALIVE")){
                    outputStream.println("CHECKALIVE");
                    String message = clientMessage();
                    if(message == null){
                        player.setConnnectionDEAD();
                        stop();
                    }
                    else if(message.equals("ALIVE")) {
                        player.setConnnectionALIVE();
                    }
                    else {
                        player.setConnnectionDEAD();
                    }

                }
                else if(player.getStatus().equals("GAME")) {


                }
                else if(player.getStatus().equals("ADDQUEUE")) {
                    addToQueue(player);
                    player.setStatusQueue();
                }
                lock.unlock();
            }


            outputStream.flush();
            outputStream.close();
            inputStream.close();
            clientSocket.close();


        } catch (IOException e) {
            //report exception somewhere.
            stop();
        } finally {
            // Close the socket in the finally block
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // Handle IOException while closing the socket
                stop();
            }
        }
    }

    public void login() throws IOException {
        //TODO handle timeouts

        boolean validToken = false;


        //Request token
        outputStream.println("AUTH");
        String token = askClientInput();
        //if token = 0 or does not exist
        if(!token.equals("0")) {
            for(Player player : players) {
                if(player.getToken().equals(token)) {
                    this.player = player;
                    validToken = true;
                    isLoggedIn = true;
                    System.out.println("[AUTH] LOGIN TOKEN SUCCESS: " + player.getUsername());
                    outputStream.println("AUTHOK");
                    player.setStatusQueue();
                    addToQueue(player);
                    startTime = Instant.now().getEpochSecond();
                    nextTime = Instant.now().getEpochSecond();
                    player.setSocket(clientSocket);
                    player.setInputStream(inputStream);
                    player.setOutputStream(outputStream);
                    return;
                }
            }
        }
        outputStream.println("AUTHFAIL");

        //require Login
        outputStream.println("LOGIN");
        //read username
        String username = askClientInput();
        System.out.println(username);
        if(username == null){
            return;
        }
        //read password
        String password = askClientInput();
        System.out.println(password);

        //verify credentials
        for(Player player : players) {
            //Check username
            if(username.equals(player.getUsername())){
                //Check password
                if(password.equals(player.getPassword())) {
                    this.player = player;
                    isLoggedIn = true;
                    System.out.println("LOGIN SUCCESS: " + username);
                    outputStream.println("LOGINOK");
                    //send token
                    player.generateToken(60);
                    outputStream.println(player.getToken());
                    player.setStatusQueue();
                    addToQueue(player);
                    startTime = Instant.now().getEpochSecond();
                    nextTime = Instant.now().getEpochSecond();
                    player.setSocket(clientSocket);
                    player.setInputStream(inputStream);
                    player.setOutputStream(outputStream);
                    return;
                }
                //Wrong password
                else {
                    System.out.println("LOGIN FAILED: " + username);
                    return;
                }
            }
        }
        //Wrong username
        System.out.println("LOGIN FAILED: " + username);
        outputStream.println("Login Failed!");
    }

    public synchronized void addToQueue(Player player) {
        if(player.getTimestampQueue() == 0){
            player.generateTimestampQueue();
        }
        player.updateDivision();
        if(queueType == 0) {
            queue.add(player);
            orderQueueByTimeInQueue();
        }
        else {
            splitPlayersByRanking(player);
        }
        updatePlayerFile();
    }

    public void splitPlayersByRanking(Player player){

        if(player.getDivision() == 1){
            //1rd Division
            queueDiv1.add(player);
            queueDiv1.sort(Comparator.comparingLong(Player::getTimestampQueue));
        }
        else if(player.getDivision() == 2){
            //2nd Division
            queueDiv2.add(player);
            queueDiv2.sort(Comparator.comparingLong(Player::getTimestampQueue));
        }
        else{
            //3st Division
            queueDiv3.add(player);
            queueDiv3.sort(Comparator.comparingLong(Player::getTimestampQueue));
        }
    }

    public synchronized void orderQueueByTimeInQueue() {
        queue.sort(Comparator.comparingLong(Player::getTimestampQueue));
    }

    public synchronized String askClientInput() {
        String input = null;
        //inputStream
        try {
            clientSocket.setSoTimeout(30000);
            input = inputStream.readLine();
        } catch (IOException e) {
            stop();
        } finally {
            try {
                clientSocket.setSoTimeout(0);
            } catch (SocketException ignored) {
            }
        }

        return input;
    }

    public synchronized String clientMessage() {
        String message = null;
        //inputStream
        try {
            clientSocket.setSoTimeout(2000);
            message = inputStream.readLine();
        } catch (IOException e) {
            player.setConnnectionDEAD();
            queue.remove(player);
            stop();
        }

        return message;
    }

    public synchronized void stop() {
        isStopped = true;
    }

    public synchronized void updatePlayerFile() {
        player.updateUserFile("assign2/src/players/");
    }
}