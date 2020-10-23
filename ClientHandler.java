import java.io.*;
import java.net.SocketException;
import java.util.*;

// server side listens for commands from client
class ClientHandler implements Runnable {
    // Maintain data about the client serviced by this thread
    ClientConnectionData client;

    public ClientHandler(ClientConnectionData client) {
        this.client = client;
    }

    
	// Broadcasts a message to all clients connected to the server.
	
    public static void broadcast(String msg) {

        try {
            System.out.println("Broadcasting -- " + msg);
            Message message=new Message(msg);
            synchronized (ChatServer.clientList) {
                for (ClientConnectionData c : ChatServer.clientList){
                    c.getObjectOut().writeObject(message);
                    c.getObjectOut().flush();
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
        
    }
   

    public static void broadcast(String msg, ClientConnectionData sender) {
        try {
            System.out.println("Broadcasting -- " + msg);
            Message message=new Message(msg);
            synchronized (ChatServer.clientList) {
                for (ClientConnectionData c : ChatServer.clientList){
                    if(!c.equals(sender)&& !c.getBlockedList().contains(sender)) {
                        c.getObjectOut().writeObject(message);
                        c.getObjectOut().flush();
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("broadcast caught exception: " + ex);
            ex.printStackTrace();
        }
        
    }

    @Override
    public void run() {
        try {

            // String incoming = ChatClient.socketIn.readLine();
            
            BufferedReader in = client.getInput();
            PrintWriter out = client.getOut();
            ObjectOutputStream objectOut=client.getObjectOut();
            ObjectInputStream objectIn=client.getObjectIn();

            System.out.println("Chat sessions have started");


            //get userName, first message from user
            
            Message nameInput;
            String username = "";
            String[] input;
            boolean usernameExists = false;

            do{
    
                objectOut.writeObject(new Message("SUBMITNAME"));
                nameInput = (Message) client.getObjectIn().readObject();
                input = nameInput.getMessage().trim().split(" ");
                
                // input = NAME name (so its making sure we're 2 items)
                if (input.length == 2) {
                    username = input[1];

                    usernameExists = false; 
                    for (ClientConnectionData client : ChatServer.clientList){
                        //this breaks because the username is initally null
                        if (client.getUsername().equals(username))
                            usernameExists = true;
                    }
                }
                else 
                    continue;
                    
            } while (usernameExists || !username.matches("[a-zA-Z0-9]+"));



            client.setUsername(username);
            //notify all that client has joined
            broadcast(String.format("WELCOME %s %s", client.getUsername(), ChatServer.clientList_toString()));
            client.getObjectOut().writeObject(new Message("WHOISHERE " + ChatServer.clientList_toString())); 


            Message incoming = new Message("");

            while( (incoming = (Message) objectIn.readObject()) != null) {
                if (incoming.getMessage().startsWith("CHAT")) {
                    String chat = incoming.getMessage().substring(4).trim();
                    if (chat.length() > 0) {
                        String msg = String.format("CHAT %s %s", client.getUsername(), chat);
                        broadcast(msg, client);    
                    }
                } 

                else if (incoming.getMessage().startsWith("WHOISHERE")){
                    objectOut.writeObject(new Message("WHOISHERE " + ChatServer.clientList_toString())); 
                    objectOut.flush();
                }
                
                else if (incoming.getMessage().startsWith("PCHAT")){
                    String[] recipientsNames = ((PrivateMessage)incoming).recipientList;  
                    ArrayList<ClientConnectionData> recipients=new ArrayList<ClientConnectionData>();
                    
                    // if client pms themselves 
                            // (also makes sure that if recipient = client, recipient doesn't exist)
                        if( Arrays.asList(recipientsNames).contains(client.getUsername())){
                            client.getObjectOut().writeObject(new Message("PCHAT SERVER You're PMing yourself"));
                            client.getObjectOut().flush();
                            continue;
                        }

                    // setting recipient
                    
                    ArrayList<String> clienListusernames=new ArrayList<String>();

                    for (int j=0; j<recipientsNames.length;j++){
                        boolean valid=false;
                        for(int i=0; i<ChatServer.clientList.size();i++){
                            System.out.println(recipientsNames[j]);
                            if(recipientsNames[j].equals(ChatServer.clientList.get(i).getUsername())){
                                recipients.add(ChatServer.clientList.get(i));
                                valid=true;
                            }
                        }
                        if (!valid){
                            client.getObjectOut().writeObject(new Message("PCHAT SERVER Sorry... user \"" + recipientsNames[j] + "\" does not exist, it was all a dream"));
                            client.getObjectOut().flush();

                        }

                    }
                    
                    for(ClientConnectionData c:recipients){
                            boolean blocked = false;
                            for(ClientConnectionData cb:c.getBlockedList()){
                                if (cb.getUsername().equals(client.getUsername())){
                                    blocked = true;
                                    break;
                                }

                            }
                            if(blocked){
                                client.getObjectOut().writeObject(new Message("BLOCKED " + c.getUsername()));
                                client.getObjectOut().flush();}
                            else{
                                System.out.println(incoming.getMessage());
                                c.getObjectOut().writeObject(new Message("PCHAT " + client.getUsername() + " " + incoming.getMessage().substring("PCHAT ".length())));
                                c.getObjectOut().flush();
                            }



                        }

                        // checks if client is blocked by recipient
                       
                       
                    
                }
                else if(incoming.getMessage().startsWith("BLOCK")){
                    String offenderUserName = incoming.getMessage().trim().split("\\s+")[1];
                    for(ClientConnectionData c: ChatServer.clientList){
                       if(c.getUsername().equals(offenderUserName)){
                        client.addBlock(c);
                        client.getObjectOut().writeObject(new Message("BLOCKCONF "+ offenderUserName));
                        client.getObjectOut().flush();
                        c.getObjectOut().writeObject(new Message("BLOCKED " + client.getUsername()));
                        c.getObjectOut().flush();
                       }

                    }
                    
                }
                
                else if (incoming.getMessage().startsWith("QUIT")){
                    break;
                }
                else {
                    System.out.println("Oh that not good");
                    System.out.println(incoming.getMessage());
                }
            }
        } catch (Exception ex) {
            if (ex instanceof SocketException) {
                System.out.println("Caught socket ex for " +
                    client.getName());
            } else {
                System.out.println(ex);
                ex.printStackTrace();
            }
        } finally {
            //Remove client from clientList, notify all
            synchronized (ChatServer.clientList) {
                ChatServer.clientList.remove(client); 
            }
            System.out.println(client.getName() + " has left.");
            broadcast(String.format("EXIT %s %s", client.getUsername(), ChatServer.clientList_toString()));
            try {
                client.getSocket().close();
            } catch (IOException ex) {}

        }
    }
    
}