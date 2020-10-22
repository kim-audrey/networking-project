// listening to server and printing them out to the user
public class ServerListener implements Runnable {
    Boolean connected;
//ChatClients Thread that listens to things coming to the server and printing them out for the user
    @Override
    public void run() {
        try {
            Message incoming = new Message("");
            String header;
            String message;
            String name;
            String messageWithoutName;
            

            while( (incoming = (Message)ChatClient.objectIn.readObject()) != null) {
                String incomingmsg=incoming.getMessage();
                if(incomingmsg.equals("SUBMITNAME")){
                   connected=false;
                    //if naming is sucessful return true
                    

                    
                    continue;

                }
                header=incomingmsg.substring(0,incomingmsg.indexOf(" "));
                message=incomingmsg.substring(incomingmsg.indexOf(" ")+1);

                switch(header){
                    case "WELCOME":
                        System.out.println(message + " has joined");
                        // added this 10/11 for aj's part of sprint ii
                        ClientHandler.broadcast(ChatServer.clientList_toString());

                        break;
                    case "CHAT":
                        name = message.substring(0,message.indexOf(" "));
                        messageWithoutName=message.substring(message.indexOf(" ")+1);
                        System.out.println(name + ": " + messageWithoutName);
                        break;
                    case "WHOISHERE":
                        System.out.println("Current Chatters:\n" + message);
                        break;
                    case "PCHAT":
                        name = message.substring(0,message.indexOf(" "));
                        messageWithoutName=message.substring(message.indexOf(" ")+1);
                        System.out.println(name + " (private): " + messageWithoutName);
                        break;
                    case "EXIT":
                        System.out.println(message + " has left");
                        break;
                    case "BLOCKCONF":
                        System.out.println("You have blocked " + message);
                        break;
                    case "BLOCKED":
                        System.out.println(message + " has blocked you");
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