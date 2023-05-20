import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.SocketException;
import java.time.Instant;
import java.util.*;

public class Game {

    private String state;
    private ArrayList<Question> questions;
    private static int maxgameinstances = 1;
    private static int gameinstances = 0;
    private static final int numberPlayers = 1;
    private List<Player> players;
    private String questionsPath;
    private ArrayList<Question> gamequestions;
    private int gamequestionnumber = 5;

    private Object lock;

    private boolean quit = false;

    public Game(List<Player> players){

        this.players = players;

        //Game Variables
        this.state = "MENU";
        this.questionsPath = "assign2/src/questions.txt";
        this.questions = new ArrayList<>();
        this.gamequestions = new ArrayList<>();

        //TODO ADD MORE STUFF SURELY (THIS IS A SIMPLE MOCKUP)
    }

    public static int getNumberPlayers() {
        return numberPlayers;
    }

    public int run() throws IOException {

        /*gameinstances += 1;

        if(gameinstances > maxgameinstances){
            System.out.println("NUMBER OF GAME INSTANCES IS BIGGER THAN ITS MAXIMUM AMMOUNT");
        }*/

        loadQuestions();
        selectRandomQuestions();

        while(!quit) {

            if (this.state == "MENU") {
                //TODO IMPLEMENT RANKED PLAY
                //TODO SHOW PLAYER LEADERBOARD AT THE END OF EACH QUESTION
                //TODO PROCESS INPUT OF X PLAYERS AND HANDLE X PLAYERS RESPONSES

                for(Player player : this.players){
                    PrintWriter playeroutputStream = player.getOutputStream();
                    BufferedReader playerinputStream = player.getInputStream();

                    playeroutputStream.println("GAME");

                    String input = playerinputStream.readLine();

                    if (input.toUpperCase().equals("PLAY")) {
                        this.state = "GAME";
                    }

                }

                } else if (this.state == "GAME") {
                    //Output question, answers and validate them
                    if(this.gamequestions.size() != 0){
                        for(int i=0; i<this.gamequestions.size(); i++){
                            checkPlayersAlive();
                            checkNumberPlayers();
                            if(quit)
                                break;

                            List<String>  answers = this.gamequestions.get(i).getOptions();

                            String rightanswer = answers.get(this.gamequestions.get(i).getAnswer());

                            //Output question to the clients

                            for(Player player : this.players){
                                PrintWriter playeroutputStream = player.getOutputStream();
//                                BufferedReader playerinputStream = player.getInputStream();

                                //check if player alive


//                                playeroutputStream.println("Current Points: " + player.getPoints());
                                playeroutputStream.println("\nQuestion number: " + (i+1));

                                this.gamequestions.get(i).print(playeroutputStream);

                                //playeroutputStream.println("Please write the answer: ");
                                //Ask for player input
                                playeroutputStream.println("INPUT");

//                                String playeranswer = playerinputStream.readLine();



//                                if(rightanswer.toUpperCase().equals(playeranswer.toUpperCase())){
//                                    playeroutputStream.println("Your answer is correct");
//                                }
//                                else{
//                                    playeroutputStream.println("Your answer is wrong");
//                                }

//                                playeroutputStream.println("Correct answer was: " + rightanswer);
                            }
                            wait(10);

                            //get players answers

                            for( Player player : players) {
                                //handle player input
                                String message = "";
                                Socket playerSocket = player.getSocket();
                                try {
                                    playerSocket.setSoTimeout(1);
                                    message = player.getInputStream().readLine();
                                    System.out.println("Answer: " + message);
                                } catch (SocketException e) {
                                    message = null;
                                } finally {
                                    playerSocket.setSoTimeout(0);
                                }
                                //message ok
                                if(message != null
                                        && !message.equals("E")
                                        && !message.equals("T")) {
                                    //verify answer
                                    int playerAnswer = Integer.parseInt(message);
                                    if(playerAnswer < this.gamequestions.get(i).getOptions().size())
                                        if(playerAnswer == this.gamequestions.get(i).getAnswer()){
                                            player.addPoints();
                                        }
                                }
                                player.getOutputStream().println("\nCorrect answer was: " + rightanswer);
                            }


                        }
                        this.state = "ENDGAME";
                    }
                    else{
                        return -1;
                    }
                } else if (this.state == "ENDGAME") {
                    //TODO ADD PLAYER LEADERBOARD AT THE END

                    //order playerlist by points descending
                    players.sort(Comparator.comparingInt(Player::getPoints).reversed());

                    //generate leaderboard Message
                    StringBuilder leaderboardBuilder = new StringBuilder();
                    leaderboardBuilder.append("\n---------- LeaderBoard ----------\n");
                    for(int i = 0; i  < players.size(); i++) {
                        leaderboardBuilder.append(i).append(": ")
                                .append(players.get(i).getUsername())
                                .append(" - ").append("Points: ")
                                .append(players.get(i).getPoints()).append("\n");
                    }
                    leaderboardBuilder.append("--------------------------------- \n");
                    String leaderBoard = leaderboardBuilder.toString();

                    //print LeaderBoard
                    for(Player player : this.players) {
                        //print LeaderBoard
                        PrintWriter playeroutputStream = player.getOutputStream();
                        BufferedReader playerinputStream = player.getInputStream();
                        playeroutputStream.println(leaderBoard);
                        playeroutputStream.println("Thanks for playing !");

                        //reset player queue time to now
                        player.setTimestampQueue(Instant.now().getEpochSecond());
                    }

                    break;
                } else {
                    return -1;
                }
        }

        for(Player player : players) {
            //update player points
            player.resetPoints();
            //return them to queue
            player.getOutputStream().println("ENDGAME");
        }


        return 0;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void selectRandomQuestions(){
        List addedindexes = new ArrayList();

        for(int i=0; i<this.gamequestionnumber; i++){
            while(true){
                Random rand = new Random();
                int upperbound = this.questions.size();
                int randomquestionindex = rand.nextInt(upperbound);

                if(!addedindexes.contains(randomquestionindex)){
                    addedindexes.add(randomquestionindex);
                    this.gamequestions.add(this.questions.get(randomquestionindex));
                    break;
                }
            }
        }
    }

    private void loadQuestions() {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(questionsPath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] split = line.split(",");
            Question question = new Question(new ArrayList<String>(Arrays.asList(split)));
            this.questions.add(question);
        }
        scanner.close();
    }

    public void setLockObject(Object lock) {
        this.lock = lock;
    }

    public void wait(int seconds)
    {
        try
        {
            Thread.sleep(seconds*1000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    public void checkPlayersAlive(){

        Iterator<Player> iterator = players.iterator();
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
                    checkNumberPlayers();
                } catch (SocketException e) {
                }
            }
        }
    }
    public void checkNumberPlayers() {
        if(players.size() == 0){
            stop();
        }
    }

    public void stop() {
        this.quit = true;
    }
}
