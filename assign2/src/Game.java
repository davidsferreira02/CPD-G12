import java.io.*;
import java.lang.reflect.Array;
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

    public Game(List<Player> players){

        this.players = players;

        //Game Variables
        this.state = "MENU";
        this.questionsPath = "src/questions.txt";
        this.questions = new ArrayList<>();
        this.gamequestions = new ArrayList<>();

        //TODO ADD MORE STUFF SURELY (THIS IS A SIMPLE MOCKUP)
    }

    public static int getNumberPlayers() {
        return numberPlayers;
    }

    public int run() throws IOException {

        gameinstances += 1;

        if(gameinstances > maxgameinstances){
            System.out.println("NUMBER OF GAME INSTANCES IS BIGGER THAN ITS MAXIMUM AMMOUNT");
        }

        loadQuestions();
        selectRandomQuestions();

        while(true) {

            if (this.state == "MENU") {
                //TODO IMPLEMENT RANKED PLAY
                //TODO SHOW PLAYER LEADERBOARD AT THE END OF EACH QUESTION
                //TODO PROCESS INPUT OF X PLAYERS AND HANDLE X PLAYERS RESPONSES

                for(Player player : this.players){
                    PrintWriter playeroutputStream = player.getOutputStream();
                    BufferedReader playerinputStream = player.getInputStream();

                    playeroutputStream.println("WRITE (PLAY) TO START PLAYING");
                    playeroutputStream.println("INPUT");

                    String input = playerinputStream.readLine();

                    if (input.toUpperCase().equals("PLAY")) {
                        this.state = "GAME";
                    }

                }

                } else if (this.state == "GAME") {
                    //Output question, answers and validate them
                    if(this.gamequestions.size() != 0){
                        for(int i=0; i<this.gamequestions.size(); i++){

                            //Output question to the clients

                            for(Player player : this.players){
                                PrintWriter playeroutputStream = player.getOutputStream();
                                BufferedReader playerinputStream = player.getInputStream();


                                playeroutputStream.println("Question number: " + (i+1));
                                playeroutputStream.println("Current Points: ");
                                this.gamequestions.get(i).print(playeroutputStream);

                                playeroutputStream.println("Please write the answer: ");
                                playeroutputStream.println("INPUT");

                                String playeranswer = playerinputStream.readLine();

                                List<String>  answers = this.gamequestions.get(i).getOptions();

                                String rightanswer = answers.get(this.gamequestions.get(i).getAnswer());

                                if(rightanswer.toUpperCase().equals(playeranswer.toUpperCase())){
                                    playeroutputStream.println("Your answer is correct");
                                }
                                else{
                                    playeroutputStream.println("Your answer is wrong");
                                }

                                playeroutputStream.println("Correct Answer Was: " + rightanswer);
                            }

                        }
                        this.state = "END";
                    }
                    else{
                        return -1;
                    }
                } else if (this.state == "END") {
                    //TODO ADD PLAYER LEADERBOARD AT THE END

                    for(Player player : this.players){
                        PrintWriter playeroutputStream = player.getOutputStream();
                        BufferedReader playerinputStream = player.getInputStream();

                        playeroutputStream.println("Thanks for playing !");
                    }
                    break;
                } else {
                    return -1;
                }
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

    public static int getMaxgameinstances() {
        return maxgameinstances;
    }

    public void setMaxgameinstances(int maxgameinstances) {
        this.maxgameinstances = maxgameinstances;
    }

    public static int getGameinstances() {
        return gameinstances;
    }

    public void setGameinstances(int gameinstances) {
        this.gameinstances = gameinstances;
    }
}
