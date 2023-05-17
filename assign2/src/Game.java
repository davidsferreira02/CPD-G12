import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Game {

    private String state;
    private ArrayList<Question> questions;
    private Player player;
    private int numberPlayers;
    private List<Player> players;
    private String questionsPath;
    private ArrayList<Question> gamequestions;
    private int gamequestionnumber = 30;

    private BufferedReader inputStream;
    private PrintWriter outputStream;

    public Game(int numberPlayers, List<Player> gameplayers, BufferedReader inputStream, PrintWriter outputStream){

        this.numberPlayers = gameplayers.size();
        this.players = gameplayers;

        //Socket Variables
        this.inputStream = inputStream;
        this.outputStream = outputStream;

        //Game Variables
        this.state = "MENU";
        this.questionsPath = "src/questions.txt";
        this.questions = new ArrayList<>();
        this.gamequestions = new ArrayList<>();

        //TODO ADD MORE STUFF SURELY (THIS IS A SIMPLE MOCKUP)
    }

    public int run() throws IOException {

        loadQuestions();
        selectRandomQuestions();

        while(true) {

            if (this.state == "MENU") {
                //TODO IMPLEMENT RANKED PLAY
                //TODO SHOW PLAYER LEADERBOARD AT THE END OF EACH QUESTION
                //TODO ONLY SELECT X PLAYERS (REMOVE X PLAYERS FROM THE LIST)

                outputStream.println("WRITE (PLAY) TO START PLAYING");
                outputStream.println("INPUT");
                String input = inputStream.readLine();

                if (input.toUpperCase().equals("PLAY")) {
                    this.state = "GAME";
                }
                } else if (this.state == "GAME") {
                    //Output question, answers and validate them
                    if(this.gamequestions.size() != 0){
                        for(int i=0; i<this.gamequestions.size(); i++){

                            //Output question to the client
                            outputStream.println("Question number: " + (i+1));
                            outputStream.println("Current Points: ");
                            this.gamequestions.get(i).print(outputStream);

                            outputStream.println("Please write the answer: ");
                            outputStream.println("INPUT");

                            String playeranswer = inputStream.readLine();

                            List<String>  answers = this.gamequestions.get(i).getOptions();

                            String rightanswer = answers.get(this.gamequestions.get(i).getAnswer());

                            if(rightanswer.toUpperCase().equals(playeranswer.toUpperCase())){
                                outputStream.println("Your answer is correct");
                            }
                            else{
                                outputStream.println("Your answer is wrong");
                            }

                            outputStream.println("Correct Answer Was: " + rightanswer);
                        }
                        this.state = "END";
                    }
                    else{
                        return -1;
                    }
                } else if (this.state == "END") {
                    //TODO ADD PLAYER LEADERBOARD AT THE END
                    outputStream.println("Thanks for playing !");
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
}
