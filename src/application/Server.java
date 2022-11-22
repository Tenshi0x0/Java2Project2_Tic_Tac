package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(8888);
            Socket fir = null;
            System.out.println("waiting first player");

            while (true){
                Socket sec = serverSocket.accept();

                if (fir == null) {
                    fir = sec;
                    System.out.println("OK connect 1 player!");
                }
                else {
                    new MyThread(fir, sec).start();
                    System.out.println("OK connect 2 players!");
                }
            }
        } catch (IOException e) {
            System.err.println("QwQ, Server break down!");
            throw new RuntimeException(e);
        }
    }
}
