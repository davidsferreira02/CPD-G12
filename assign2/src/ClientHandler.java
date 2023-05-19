import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ClientHandler implements Runnable{

    protected Socket clientSocket = null;

    private BufferedReader inputStream;
    private PrintWriter outputStream;
    private ArrayList<Player> queue;

    private ArrayList<Player> players;

    boolean isLoggedIn = false;

    boolean isStopped = false;

    //TODO IF GAME IS FULL PROBABLY CREATE A WAITING ROOM/LOBBY


    public ClientHandler(Socket clientSocket, ArrayList<Player> queue, ArrayList<Player> players) {
        this.clientSocket = clientSocket;
        this.queue   = queue;
        this.players = players;
    }

    public void run() {
        try {
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new PrintWriter(clientSocket.getOutputStream(), true);

            outputStream.println("Connected to Trivia Server.");


            // Read and print messages from the client

            String receivedMessage;

            //TODO HANDLE isStopped!
            while(!isStopped){
                if(!isLoggedIn) {
                    System.out.println("Asking login: " + clientSocket);
                    login();
                }
                /*else {

                    if(this.queue.size() >= Game.getNumberPlayers()){
                        startGame();
                    }
                    else{
                        //TODO ADD A WAITING PLAYERS LOBBY/MESSAGE
                    }

                }*/
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

                    validToken = true;
                    isLoggedIn = true;
                    System.out.println("[AUTH] LOGIN TOKEN SUCCESS: " + player.getUsername());
                    outputStream.println("AUTHOK");
                    addToQueue(player);
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
                    isLoggedIn = true;
                    System.out.println("LOGIN SUCCESS: " + username);
                    outputStream.println("LOGINOK");
                    //send token
                    player.generateToken(60);
                    outputStream.println(player.getToken());
                    addToQueue(player);
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
            //TODO update userfile
        }
        queue.add(player);
        orderQueueByTimeInQueue();
        player.updateUserFile("assign2/src/players/");
    }

    public synchronized void orderQueueByTimeInQueue() {
        queue.sort(Comparator.comparingLong(Player::getTimestampQueue));
    }

    public String askClientInput() {
        String input = null;
        //inputStream
        try {
            clientSocket.setSoTimeout(30000);
            input = inputStream.readLine();
        } catch (IOException e) {
            stop();
        }

        return input;
    }

    public void stop() {
        isStopped = true;
    }
}