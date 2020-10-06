import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static Socket socket;
    static BufferedReader socketIn;
    static PrintWriter out;
    static Scanner userInput;
    
    public static void main(String[] args) throws Exception {
        userInput = new Scanner(System.in);
        
        System.out.println("What's the server IP? ");
        String serverip = userInput.nextLine();
        System.out.println("What's the server port? ");
        int port = userInput.nextInt();
        userInput.nextLine();

        socket = new Socket(serverip, port);
        socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // start a thread to listen for server messages
        ServerListener listener = new ServerListener();
        Thread t = new Thread(listener);
        t.start();


        //WHAT WE NEEDTACHANGE FOR SUBMITNAME 
        // System.out.println("Chat sessions have started");
        // final int usernameIndex = 5;
        // String nameInput;
        // String username = "";
        // String[] input;

        // do{
        //     System.out.println("SUBMITNAME");
        //     nameInput = userInput.nextLine().trim();
        //     input = nameInput.strip().split("[\\s+]");

        //     if (input[0].equals("NAME") && input.length > 1) 
        //         username = nameInput.substring(usernameIndex);
        //     else 
        //         continue;

        // } while (ChatServer.usernameExists(username));

        // he go to ClientHandler because 
            // out = new PrintWriter(socket.getOutputStream(), true); 
        // out.println(username); //out.flush();

        // wait what i this for: 
        String line = userInput.nextLine().trim();
        while(!line.toLowerCase().startsWith("/quit")) {
            String msg = String.format("CHAT %s", line); 
            out.println(msg);
            line = userInput.nextLine().trim();
        }
        out.println("QUIT");
        out.close();
        userInput.close();
        socketIn.close();
        socket.close();
        

/*
    static class ServerListener implements Runnable {

        @Override
        public void run() {
            try {
                String incoming = "";

                while( (incoming = socketIn.readLine()) != null) {
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
    */



    }
    
}
