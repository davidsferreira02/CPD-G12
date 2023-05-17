public class Server {
    public static void main(String[] args) {
        int port = 27277;
        System.out.println("Starting Server at port: " + port);
        ThreadPooledServer server = new ThreadPooledServer(port);
        new Thread(server).start();

        try {
            Thread.sleep(20 * 10000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Stopping Server");
        server.stop();
    }
}
