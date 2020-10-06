import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// listening to server and printing them out to the user
public class ServerListener implements Runnable {
//ChatClients Thread that listens to things coming to the server and printing them out for the user
    @Override
    public void run() {
        try {
            String incoming = "";
            String header;
            String message;
            String name;
            String messageWithoutName;
            Scanner input;
            Boolean named;
            input= new Scanner(System.in);
            

            while( (incoming = ChatClient.socketIn.readLine()) != null) {
                if(incoming.equals("SUBMITNAME")){
                   named=false;
                    //if naming is sucessful return true
                    

                    
                    continue;

                }
                header=incoming.substring(0,incoming.indexOf(" "));
                message=incoming.substring(incoming.indexOf(" ")+1);

                switch(header){
                    case "WELCOME":
                        System.out.println(message + " has joined");
                        break;
                    case "CHAT":
                        name = message.substring(0,message.indexOf(" "));
                        messageWithoutName=message.substring(message.indexOf(" ")+1);
                        System.out.println(name + ": " + messageWithoutName);
                        break;
                    case "PCHAT":
                        name = message.substring(0,message.indexOf(" "));
                        messageWithoutName=message.substring(message.indexOf(" ")+1);
                        System.out.println(name + " (private): " + messageWithoutName);
                        break;
                    case "EXIT":
                        System.out.println(message + " has left");
                        break;
                }

                /*
                //handle different headers
                //WELCOME
                if(incoming.startsWith("WELCOME")){}
                //CHAT
                if(incoming.startsWith("CHAT")){}
                //PCHAT
                if(incoming.startsWith("PCHAT")){}
                //EXIT
                if(incoming.startsWith("EXIT")){}
                //BLOCK
                if(incoming.startsWith("BLOCK")){}
                System.out.println(incoming);
                */
            }
        } catch (Exception ex) {
            System.out.println("Exception caught in listener - " + ex);
        } finally{
            System.out.println("Client Listener exiting");
        }
    }
}