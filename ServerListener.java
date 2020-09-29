import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerListener implements Runnable {

    @Override
    public void run() {
        try {
            String incoming = "";

            Socket socket = new Socket(serverip, port);
            BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            while ((incoming = socketIn.readLine()) != null) {
                //handle different headers
                //WELCOME
                //CHAT
                //EXIT
                System.out.println(incoming);
            }
        } catch (Exception ex) {
            System.out.println("Exception caught in listener - " + ex);
        } finally{
            System.out.println("Client Listener exiting");
        }
    }
}