import java.io.*;
import java.net.SocketException;

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
            ObjectInputStream objectIn=client.getObjectIn();
            ObjectOutputStream objectOut=client.getObjectOut();
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
            broadcast(String.format("WELCOME %s", client.getUsername()));

            // 10/27 send list of names to new member
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
                    String recipientName = incoming.getMessage().trim().split("\\s+")[1];   // should be the 2nd "word" in incoming
                    ClientConnectionData recipient = client;  

                    // if client pms themselves 
                            // (also makes sure that if recipient = client, recipient doesn't exist)
                    if(client.getUsername().equals(recipientName)){
                        recipient.getObjectOut().writeObject(new Message("PCHAT SERVER You're PMing yourself"));
                        recipient.getObjectOut().flush();
                        continue;
                    }

                    // setting recipient
                    for(ClientConnectionData c : ChatServer.clientList){
                        if (c.getUsername().equals(recipientName)){
                            recipient = c;
                            break;
                        }
                    }

                    // recipient default value was client
                    if(recipient.equals(client)) {  
                        client.getObjectOut().writeObject(new Message("PCHAT SERVER Sorry... user \"" + recipientName + "\" does not exist, it was all a dream"));
                        client.getObjectOut().flush();
                        // check if getOut() is the correct one... it must be right!!!! What is printwriter ;-
                    }
                    else {
                        // checks if client is blocked by recipient
                        boolean blocked = false;
                        for(ClientConnectionData c : recipient.getBlockedList()){
                            if (c.getUsername().equals(client.getUsername())){
                                blocked = true;
                                break;
                            }
                        }
                        if(blocked){
                            client.getObjectOut().writeObject(new Message("BLOCKED " + recipient.getUsername()));
                            client.getObjectOut().flush();}
                        else{
                            recipient.getObjectOut().writeObject(new Message("PCHAT " + client.getUsername() + " " + incoming.getMessage().substring("PCHAT ".length() + recipientName.length())));
                            recipient.getObjectOut().flush();
                        }
                    }
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
            broadcast(String.format("EXIT %s", client.getUsername()));
            try {
                client.getSocket().close();
            } catch (IOException ex) {}

        }
    }
    
}