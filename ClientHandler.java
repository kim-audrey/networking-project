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
            String userName = in.readLine().trim();
            client.setUserName(userName);
            //notify all that client has joined
            broadcast(String.format("WELCOME %s", client.getUserName()));

            
            String incoming = "";

            while( (incoming = in.readLine()) != null) {
                if (incoming.startsWith("CHAT")) {
                    String chat = incoming.substring(4).trim();
                    if (chat.length() > 0) {
                        String msg = String.format("CHAT %s %s", client.getUserName(), chat);
                        broadcast(msg);    
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
            broadcast(String.format("EXIT %s", client.getUserName()));
            try {
                client.getSocket().close();
            } catch (IOException ex) {}

        }
    }
    
}