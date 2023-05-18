import java.io.BufferedReader;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.security.SecureRandom;

public class Player {
    private int rank;
    private String username;
    private String password;
    private String token;
    private int points;

    private long tokenLimit;
    private long timestampQueue;

    //Socket Variables
    private BufferedReader inputStream;
    private PrintWriter outputStream;

    public Player(String username, String password){
        this.username = username;
        this.password = password;
        this.token = "0";
        this.points=0;
        this.timestampQueue = 0;
        this.rank = 0;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void updateRank(int rank) {
        this.rank += rank;
        if(this.rank < 0)
            this.rank = 0;
        else if(this.rank > 100)
            this.rank = 100;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void generateToken(int minutes) {
        SecureRandom random = new SecureRandom();
        this.token = random.nextLong(1000000, 10000000) + this.username;
        this.tokenLimit = Instant.now().plus(minutes, ChronoUnit.MINUTES).getEpochSecond();
    }

    public long getTimestampQueue() {
        return timestampQueue;
    }

    public void setTimestampQueue(long timestampQueue) {
        this.timestampQueue = timestampQueue;
    }

    public void generateTimestampQueue() {
        this.timestampQueue = Instant.now().getEpochSecond();
    }

    public long getTokenLimit() {
        return tokenLimit;
    }

    public void setTokenLimit(long tokenLimit) {
        this.tokenLimit = tokenLimit;
    }

    public void printPlayer() {
        System.out.println("Username: " + this.username);
        System.out.println("Password: " + this.password);
        System.out.println("Rank: " + this.rank);
        System.out.println("Token: " + this.token);
        System.out.println("TokenLimit: " + this.tokenLimit);
        System.out.println("TimestampQueue: " + this.timestampQueue);
    }

    public BufferedReader getInputStream() {
        return inputStream;
    }

    public void setInputStream(BufferedReader inputStream) {
        this.inputStream = inputStream;
    }

    public PrintWriter getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(PrintWriter outputStream) {
        this.outputStream = outputStream;
    }
    public int getPoints(){
        return points;
    }

    public void addPoints(){
        points ++;
    }
    public void removePoints(){
        points -- ;
    }
}
