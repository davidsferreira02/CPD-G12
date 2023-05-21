import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Question {

    private String question;
    private ArrayList<String> options;
    private int answer;

    public Question(ArrayList<String> questions) {
        this.question = questions.get(0);
        this.answer = Integer.parseInt(questions.get(questions.size()-1));
        questions.remove(0);
        questions.remove(questions.size()-1);
        this.options = questions;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public void addOption(String option) {
        this.options.add(option);
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public void print(PrintWriter outputStream) {
        outputStream.println(question);
        int i = 0;
        for(String option : options) {
            outputStream.println(i + ": " + option);
            i++;
        }
    }

}
