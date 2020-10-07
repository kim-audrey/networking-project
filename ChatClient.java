import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;



// focused on sending things to the Server 

public class ChatClient {
    private static Socket socket;
    static BufferedReader socketIn;    // takes info from other file (namely, ChatServer.java)
    static PrintWriter out;         // lets other files access us (namely, ChatServer.java)
    static Scanner userInput;       // taking from terminal
    
    public static void main(String[] args) throws Exception {
        userInput = new Scanner(System.in);
        
        System.out.println("What's the server IP? ");
        String serverip = "localhost";  // userInput.nextLine();
        System.out.println("What's the server port? ");
        int port = 54321;       // Integer.parseInt(userInput.nextLine());
       

        socket = new Socket(serverip, port);
        socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // start a thread to listen for server messages
        ServerListener listener = new ServerListener();
        Thread t = new Thread(listener);
        t.start();

        while(listener.connected==null){
            System.out.print(""); // please dear god dont remove this, it will break - alex
        } //wait for submitname
        // wait what i this for: 
       String response="";
       System.out.println("Enter your username: ");

        while(!listener.connected){
            response = userInput.nextLine();
            out.println("NAME " + response);
            listener.connected=true;
            Thread.sleep(1000);
            if(!listener.connected)
                System.out.println("Invalid username. Enter a new one: ");
            //if(named){exit loop} else{System.out.println("Enter your username: ");} 

        }


        String line=userInput.nextLine().trim();
        while(!line.toLowerCase().startsWith("/quit")) {
            if(line.startsWith("@")){
                String[] spltLine=line.split(" ");
                if(spltLine.length<2){
                    System.out.println("Invalid pm syntax: @user message");
                }
                else{
                    spltLine[0]=spltLine[0].substring(1);
                    String msg = String.format("PCHAT %s %s", spltLine[0], spltLine[1]); 
                    out.println(msg);
                }
            }
            else if(line.startsWith("/block")){
                String[] spltLine=line.split(" ");
                if(spltLine.length<2){
                    System.out.println("Invalid block syntax: /block user");
                }
                else{
                    String msg = String.format("BLOCK %s", spltLine[1]);
                    out.println(msg);
                }
            }
            else{

                String msg = String.format("CHAT %s", line); 
                out.println(msg);
            }
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
