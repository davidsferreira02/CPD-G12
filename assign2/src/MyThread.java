public class MyThread extends Thread{

    int n=0;


    public MyThread(int n){

        this.n=n;
        run();

    }

    public void run() {
        int i = 1;
        while (i >= n) {
            System.out.println("MyThread " + i + " running");
            i++;
        }
    }

}
