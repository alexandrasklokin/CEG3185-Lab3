import java.net.*;
import java.io.*;

public class PacketReceiver extends Thread {

    private ServerSocket server = null;
    private Socket socket = null;
    private DataInputStream in = null;

    public PacketReceiver(int port){
        try {
            server = new ServerSocket(port);
            System.out.println("__ Server is started. Waiting for client ___ ");

            socket = server.accept();
            System.out.println("__ Client accepted __ ");

            // RECEIVE THE DATA  //////////////////////////////////////////////////////////////////////////////////////////////////////
            // See this link: https://www.geeksforgeeks.org/socket-programming-in-java/
        
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) throws Exception {

        PacketReceiver pacRec = new PacketReceiver(5000);
    }
}