public class Player {

   private int score;

    Player(int score){
        this.score=score;
    }

    public int getScore() {
        return score;
    }

    public void addScore(){
        score++;
    }

    public void removeScore(){
        score --;
    }

}
