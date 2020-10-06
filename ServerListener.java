

public class ServerListener implements Runnable {

    @Override
    public void run() {
        try {
            String incoming = "";

            while( (incoming = ChatClient.socketIn.readLine()) != null) {
                if(incoming.equals("SUBMITNAME")){
                    System.out.println("Enter your username: ");
                    ChatClient.out.println("NAME " + ChatClient.userInput.nextLine().trim());
                    continue;

                }
                String header=incoming.substring(0,incoming.indexOf(" "));
                String message=incoming.substring(incoming.indexOf(" ")+1);

                switch(header){
                    case "WELCOME":
                        System.out.println(message + " has joined");
                        break;
                    case "CHAT":
                        String name = message.substring(0,message.indexOf(" "));
                        String messageWithoutName=message.substring(message.indexOf(" ")+1);
                        System.out.println(name + ": " + messageWithoutName);
                        break;
                    case "PCHAT":
                        String name = message.substring(0,message.indexOf(" "));
                        String messageWithoutName=message.substring(message.indexOf(" ")+1);
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