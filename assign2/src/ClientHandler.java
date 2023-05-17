import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;


public class ClientHandler implements Runnable{

    protected Socket clientSocket = null;

    private BufferedReader inputStream;
    private PrintWriter outputStream;
    private ArrayList<Player> queue;

    private ArrayList<Player> players;

    boolean isLoggedIn = false;


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

            // Perform authentication logic here

            //outputStream.println("LOGIN");

            // Read and print messages from the client
            boolean isStopped = false;
            String receivedMessage;

            while(!isStopped){
                if(!isLoggedIn) {
                    System.out.println("Asking login: " + clientSocket);
                    login();
                    for(Player player : queue) {
                        System.out.println(player.getUsername() + ":" + player.getTimestampQueue());
                    }
                }
                else {

                    if(this.queue.size() == 1){
                        startGame(this.queue, inputStream, outputStream);
                    }
                }
            }



            /*while (!isStopped) {
                receivedMessage = inputStream.readLine();
                if(receivedMessage.equals("quit")){
                    isStopped = true;
                }
                System.out.println("Received message: " + receivedMessage);
            }*/

            synchronized (this.queue) {

            }

            System.out.println("Loop");
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            clientSocket.close();
            System.out.println("Finish");


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
                    addToQueue(player);
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

    public void startGame(ArrayList<Player> Playerlist, BufferedReader inputStream, PrintWriter outputStream) throws IOException {
        //Todo begin game instance on server side
        Game game = new Game(1, Playerlist, inputStream, outputStream);
        if(game.run() == -1){
            outputStream.println("OOPS! Something went wrong");
        }
        else{
            //TODO DO SOMETHING WHEN THE USER HAS COMPLETED A GAME SUCCESFULLY
        }
    }
}