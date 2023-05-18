import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;


public class ClientHandler implements Runnable{

    protected Socket clientSocket = null;

    private BufferedReader inputStream;
    private PrintWriter outputStream;

    private ArrayList<Player> queue = new ArrayList<>();
    private ArrayList<Player> players;

    boolean isLoggedIn = false;

    //TODO IF GAME IS FULL PROBABLY CREATE A WAITING ROOM/LOBBY


    public ClientHandler(Socket clientSocket, ArrayList<Player> queue, ArrayList<Player> players) {
        this.clientSocket = clientSocket;
        this.queue = queue;
        this.players = players;
    }

    public void run() {
        try {
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new PrintWriter(clientSocket.getOutputStream(), true);

            outputStream.println("Connected to Trivia Server.");

            // Perform authentication logic here

            //outputStream.println("LOGIN");

            // Read and print messages from the client
            boolean isStopped = false;
            String receivedMessage;

            //TODO HANDLE isStopped!
            while(!isStopped){
                if(!isLoggedIn) {
                    System.out.println("Asking login: " + clientSocket);
                    login();
                    for(Player player : queue) {
                        System.out.println(player.getUsername() + ":" + player.getTimestampQueue());
                    }
                }
            }


            outputStream.flush();
            outputStream.close();
            inputStream.close();
            clientSocket.close();


        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        } finally {
            // Close the socket in the finally block
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // Handle IOException while closing the socket
                e.printStackTrace();
            }
        }
    }

    public void login() throws IOException {
        outputStream.println("LOGIN");
        //Request token
        //if token = 0 or does not exist
        //require Authentication

        //read username
        String username = inputStream.readLine();
        //read password
        String password = inputStream.readLine();

        //verify credentials
        for(Player player : players) {
            //Check username
            if(username.equals(player.getUsername())){
                //Check password
                if(password.equals(player.getPassword())) {
                    isLoggedIn = true;
                    System.out.println("LOGIN SUCCESS: " + username);
                    outputStream.println("Login Successful!");
                    player.setInputStream(inputStream);
                    player.setOutputStream(outputStream);
                    //Asks player what game mode he is willing to play
                    selectGameMode(player);
                    return;
                }
                //Wrong password
                else {
                    System.out.println("LOGIN FAILED: " + username);
                    outputStream.println("Login Failed!");
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
            //update userfile
        }
        //TODO FIX THIS
        queue.add(player);
        orderQueueByTimeInQueue();
        updateUserFile(player);
    }

    public synchronized void orderQueueByTimeInQueue() {
        queue.sort(Comparator.comparingLong(Player::getTimestampQueue));
    }

    public synchronized void updateUserFile(Player player) {
        //username:password:rank:token:tokenLimit:timestampQueue
        try {
            String filename = "src/players/" + player.getUsername() + ".txt";
            File file = new File(filename);

            // Check if the file doesn't exist
            if (!file.exists()) {
                file.createNewFile(); // Create a new file
            }

            FileWriter writer = new FileWriter(filename);
            String separator = ":";
            writer.write(
                    player.getPassword() + separator +
                    player.getRank() + separator +
                    player.getToken() + separator +
                    player.getTokenLimit() + separator +
                    player.getTimestampQueue());
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public void selectGameMode(Player player) throws IOException {
        outputStream.println("Please select a Game Mode: ");
        outputStream.println("0 - Unranked");
        outputStream.println("1- Ranked");
        outputStream.println("INPUT");
        int gamemode = Integer.valueOf(inputStream.readLine());

        //Get Player's game mode and adds him to the queue
        if(gamemode == 0 || gamemode == 1){
            player.setGamemode(gamemode);
            System.out.println(player.getGamemode());
            addToQueue(player);
        }
        else{
            outputStream.println("Invalid Option");
            outputStream.println("Please provide a valid one!");
            outputStream.println("---------------------------");
            selectGameMode(player);
        }
    }

}