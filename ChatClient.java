import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;


// focused on sending things to the Server 

public class ChatClient {
    private static Socket socket;
    static BufferedReader socketIn;    // takes info from other file (namely, ChatServer.java)
    static PrintWriter out;         // lets other files access us (namely, ChatServer.java)
    static Scanner userInput;       // taking from terminal
    static ObjectOutputStream objectOut;
    static ObjectInputStream objectIn;
    
    public static void main(String[] args) throws Exception {
        userInput = new Scanner(System.in);
        
        System.out.println("What's the server IP? ");
        String serverip = userInput.nextLine();
        System.out.println("What's the server port? ");
        int port = Integer.parseInt(userInput.nextLine());
       

        socket = new Socket(serverip, port);
        socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        objectOut = new ObjectOutputStream(socket.getOutputStream());
        objectIn=new ObjectInputStream(socket.getInputStream());

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
            objectOut.writeObject(new Message("NAME " + response));
            objectOut.flush();
            listener.connected=true;
            Thread.sleep(1000);
            if(!listener.connected)
                System.out.println("Invalid username. Enter a new one: ");

        }


        String line=userInput.nextLine().trim();
        while(!line.toLowerCase().startsWith("/quit")) {

            if(line.startsWith("@")){
                String[] spltLine=line.split(" ");
                if(spltLine.length<2){
                    System.out.println("Invalid pm syntax: @user @user .... message");
                }
                else{
                    String msgpart="";
                    int index=0;
                    boolean recipientsProcessed=false;
                    for(int i=0; i<spltLine.length;i++){
                        if(spltLine[i].startsWith("@") && !recipientsProcessed){
                            spltLine[i]=spltLine[i].substring(1);
                            index++;
                        }

                        else {
                            recipientsProcessed=true;
                            msgpart=msgpart.concat(spltLine[i]+" ");

                        }

                    }
                        String[] recipients= Arrays.copyOfRange(spltLine, 0,index);
                        PrivateMessage msg = new PrivateMessage(String.format("PCHAT %s", msgpart), recipients);
                        objectOut.writeObject(msg);
                        objectOut.flush();

                   
                }

            }

            else if(line.startsWith("/whoishere")){
                objectOut.writeObject(new Message("WHOISHERE"));
                objectOut.flush();
            }

            else if(line.startsWith("/block")){
                String[] spltLine=line.split(" ");
                if(spltLine.length<2){
                    System.out.println("Invalid block syntax: /block user");
                }
                else{
                    Message msg = new Message(String.format("BLOCK %s", spltLine[1]));
                    objectOut.writeObject(msg);
                    objectOut.flush();
                }
            }

            else{

                Message msg = new Message(String.format("CHAT %s", line));
                objectOut.writeObject(msg);
                objectOut.flush();
            }
            line = userInput.nextLine().trim();
        }
        objectOut.writeObject(new Message("QUIT"));
        out.close();

        objectOut.close();
        userInput.close();
        socketIn.close();
        socket.close();
    }
    
}
