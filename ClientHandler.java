import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;


class ClientHandler implements Runnable {
    // Maintain data about the client serviced by this thread
    ClientConnectionData client;

    public ClientHandler(ClientConnectionData client) {
        this.client = client;
    }

    
	// Broadcasts a message to all clients connected to the server.
	
    public void broadcast(String msg) {
        try {
            System.out.println("Broadcasting -- " + msg);
            synchronized (ChatServer.clientList) {
                for (ClientConnectionData c : ChatServer.clientList){
                    c.getOut().println(msg);
                    // c.getOut().flush();
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
            BufferedReader in = client.getInput();
            //get userName, first message from user
            String username = in.readLine().trim();
            client.setUsername(username);
            //notify all that client has joined
            broadcast(String.format("WELCOME %s", client.getUsername()));

            
            String incoming = "";

            while( (incoming = in.readLine()) != null) {
                if (incoming.startsWith("CHAT")) {
                    String chat = incoming.substring(4).trim();
                    if (chat.length() > 0) {
                        String msg = String.format("CHAT %s %s", client.getUsername(), chat);
                        broadcast(msg);    
                    }
                } else if (incoming.startsWith("PCHAT")){       // I think this is where it's supposed to go ;-;
                    String recipientName = incoming.strip().split("\\s+")[1];   // should be the 2nd "word" in incoming
                    ClientConnectionData recipient = client;    // as default until I think of something better

                    for(ClientConnectionData c : ChatServer.clientList){
                        if (c.getName().equals(recipientName)){
                            recipient = c;
                            break;
                        }
                    }

                    if(recipient.equals(client)) {    
                        client.getOut().printf("Sorry... %s does not exist, it was all a dream", recipientName);       // check if getOut() is the correct one... it must be right!!!! What is printwriter ;-
                    } else {
                        recipient.getOut().printf("PCHAT %s: %s", client.getName(), incoming.substring("PCHAT ".length() + recipientName.length()));
                    }

                } else if (incoming.startsWith("QUIT")){
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