import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    protected int serverPort = 2727;
    protected ServerSocket serverSocket = null;
    protected int currentSum = 0;
    protected boolean isStopped = false;

    public Server(int port){
        this.serverPort = port;
    }


    public void run() {
        openServerSocket();
        System.out.println("Server Running");

        while(!isStopped) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped) {
                    System.out.println("Server Stopped");
                    return;
                }
                throw new RuntimeException("Error accepting connections", e);
            }
            try {
                processClientRequest(clientSocket);
            } catch (Exception e) {

            }
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port" + this.serverPort, e);
        }
    }

    public void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void processClientRequest(Socket clientSocket) throws Exception {
        String received = "";
        while(!received.equals("END")) {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            DataInputStream receivedData = new DataInputStream(input);
            DataOutputStream sendData = new DataOutputStream(output);
            received = receivedData.readUTF();
            try {
                int number = Integer.parseInt(received);
                this.currentSum += number;

                sendData.writeUTF(String.valueOf(this.currentSum));
            } catch (NumberFormatException e) {
                //
                if(!received.equals("END")) {
                    sendData = new DataOutputStream(output);
                    sendData.writeUTF("Send integers or 'END'");
                }
                else {
                    sendData.writeUTF(String.valueOf(this.currentSum));
                }
            }
            System.out.println("Received: " + received);
        }

        stop();
    }

}