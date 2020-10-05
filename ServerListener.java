
// listening to server and printing them out to the user
public class ServerListener implements Runnable {
//ChatClients Thread that listens to things coming to the server and printing them out for the user
    @Override
    public void run() {
        try {
            String incoming = "";

            while( (incoming = ChatClient.socketIn.readLine()) != null) {
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
            }
        } catch (Exception ex) {
            System.out.println("Exception caught in listener - " + ex);
        } finally{
            System.out.println("Client Listener exiting");
        }
    }
}