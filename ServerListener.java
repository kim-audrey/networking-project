

public class ServerListener implements Runnable {

    @Override
    public void run() {
        try {
            String incoming = "";

            while( (incoming = ChatClient.socketIn.readLine()) != null) {
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