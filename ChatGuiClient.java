import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.awt.Toolkit;
import java.awt.Dimension;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
/**
 * For Java 8, javafx is installed with the JRE. You can run this program normally.
 * For Java 9+, you must install JavaFX separately: https://openjfx.io/openjfx-docs/
 * If you set up an environment variable called PATH_TO_FX where JavaFX is installed
 * you can compile this program with:
 *  Mac/Linux:
 *      > javac --module-path $PATH_TO_FX --add-modules javafx.controls day10_chatgui/ChatGuiClient.java
 *  Windows CMD:
 *      > javac --module-path %PATH_TO_FX% --add-modules javafx.controls day10_chatgui/ChatGuiClient.java
 *  Windows Powershell:
 *      > javac --module-path $env:PATH_TO_FX --add-modules javafx.controls day10_chatgui/ChatGuiClient.java
 * 
 * Then, run with:
 * 
 *  Mac/Linux:
 *      > java --module-path $PATH_TO_FX --add-modules javafx.controls day10_chatgui.ChatGuiClient 
 *  Windows CMD:
 *      > java --module-path %PATH_TO_FX% --add-modules javafx.controls day10_chatgui.ChatGuiClient
 *  Windows Powershell:
 *      > java --module-path $env:PATH_TO_FX --add-modules javafx.controls day10_chatgui.ChatGuiClient
 * 
 * There are ways to add JavaFX to your to your IDE so the compile and run process is streamlined.
 * That process is a little messy for VSCode; it is easiest to do it via the command line there.
 * However, you should open  Explorer -> Java Projects and add to Referenced Libraries the javafx .jar files 
 * to have the syntax coloring and autocomplete work for JavaFX 
 * 
 */

class ServerInfo {
    public final String serverAddress;
    public final int serverPort;

    public ServerInfo(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }
}

public class ChatGuiClient extends Application {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ObjectOutputStream objectOut;
    private ObjectInputStream objectIn;
    private ArrayList<String> blocked;

    private Stage stage;
    private TextArea messageArea;
    private TextField textInput;
    private Button sendButton;

    private Stage pStage;
    private VBox pVBox;

    private ServerInfo serverInfo;
    //volatile keyword makes individual reads/writes of the variable atomic
    // Since username is accessed from multiple threads, atomicity is important 
    private volatile String username = "";
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //If ip and port provided as command line arguments, use them
        List<String> args = getParameters().getUnnamed();
        if (args.size() == 2){
            this.serverInfo = new ServerInfo(args.get(0), Integer.parseInt(args.get(1)));
        }
        else {
            //otherwise, use a Dialog.
            Optional<ServerInfo> info = getServerIpAndPort();
            if (info.isPresent()) {
                this.serverInfo = info.get();
            } 
            else{
                Platform.exit();
                return;
            }
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        blocked=new ArrayList<String>();

        pStage=new Stage();
        BorderPane pBorderPane=new BorderPane();
        
        pBorderPane.setTop(new Label("Participants"));

        pVBox=new VBox();

        ScrollPane pScrollPane=new ScrollPane();
        pScrollPane.setPannable(true);
        pScrollPane.setContent(pVBox);
        pBorderPane.setCenter(pScrollPane);

        Scene pScene = new Scene(pBorderPane, 400, 500);
        pStage.setTitle("Participants");
        pStage.setScene(pScene);
        pStage.setX(width/4);
        pStage.show();


        this.stage = primaryStage;
        BorderPane borderPane = new BorderPane();

        messageArea = new TextArea();
        messageArea.setWrapText(true);
        messageArea.setEditable(false);
        borderPane.setCenter(messageArea);

        //At first, can't send messages - wait for WELCOME!
        textInput = new TextField();
        textInput.setEditable(false);
        textInput.setOnAction(e -> sendMessage());
        sendButton = new Button("Send");
        sendButton.setDisable(true);
        sendButton.setOnAction(e -> sendMessage());

        HBox hbox = new HBox();
        hbox.getChildren().addAll(new Label("Message: "), textInput, sendButton);
        HBox.setHgrow(textInput, Priority.ALWAYS);
        borderPane.setBottom(hbox);

        Scene scene = new Scene(borderPane, 400, 500);
        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.setX(width/2);
        stage.show();

        ServerListener socketListener = new ServerListener();
        
        //Handle GUI closed event
        stage.setOnCloseRequest(e -> {
            try{
                objectOut.writeObject(new Message("QUIT"));
                objectOut.flush();
                socketListener.appRunning = false;
                socket.close(); 
            } catch (IOException ex) {}
        });
        pStage.setOnCloseRequest(e -> {
            try{
                objectOut.writeObject(new Message("QUIT"));
                objectOut.flush();
                socketListener.appRunning = false;
                socket.close(); 
            } catch (IOException ex) {}
        });
        

        new Thread(socketListener).start();
    }

    private void sendMessage() {
        try{
            String message = textInput.getText().trim();
            if (message.length() == 0)
                return;
            if(message.startsWith("@")){
                String[] spltLine=message.split(" ");
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
                        messageArea.appendText("Private message sent: " + msgpart + "\n");
                   
                }

                    
                
            }
            else{

                String msg = String.format("CHAT %s", message); 
                objectOut.writeObject(new Message (msg));
                objectOut.flush();
                messageArea.appendText(username+": "+ message + "\n");
            }
            textInput.clear();
        }catch(IOException e){}
    }


    private Optional<ServerInfo> getServerIpAndPort() {
        // In a more polished product, we probably would have the ip /port hardcoded
        // But this a great way to demonstrate making a custom dialog
        // Based on Custom Login Dialog from https://code.makery.ch/blog/javafx-dialogs-official/

        // Create a custom dialog for server ip / port
        Dialog<ServerInfo> getServerDialog = new Dialog<>();
        getServerDialog.setTitle("Enter Server Info");
        getServerDialog.setHeaderText("Enter your server's IP address and port: ");

        // Set the button types.
        ButtonType connectButtonType = new ButtonType("Connect", ButtonData.OK_DONE);
        getServerDialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

        // Create the ip and port labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField ipAddress = new TextField();
        ipAddress.setPromptText("e.g. localhost, 127.0.0.1");
        grid.add(new Label("IP Address:"), 0, 0);
        grid.add(ipAddress, 1, 0);

        TextField port = new TextField();
        port.setPromptText("e.g. 54321");
        grid.add(new Label("Port number:"), 0, 1);
        grid.add(port, 1, 1);


        // Enable/Disable connect button depending on whether a address/port was entered.
        Node connectButton = getServerDialog.getDialogPane().lookupButton(connectButtonType);
        connectButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        ipAddress.textProperty().addListener((observable, oldValue, newValue) -> {
            connectButton.setDisable(newValue.trim().isEmpty());
        });

        port.textProperty().addListener((observable, oldValue, newValue) -> {
            // Only allow numeric values
            if (! newValue.matches("\\d*"))
                port.setText(newValue.replaceAll("[^\\d]", ""));

            connectButton.setDisable(newValue.trim().isEmpty());
        });

        getServerDialog.getDialogPane().setContent(grid);
        
        // Request focus on the username field by default.
        Platform.runLater(() -> ipAddress.requestFocus());


        // Convert the result to a ServerInfo object when the login button is clicked.
        getServerDialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                return new ServerInfo(ipAddress.getText(), Integer.parseInt(port.getText()));
            }
            return null;
        });

        return getServerDialog.showAndWait();
    }

    private String getName(){
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Enter Chat Name");
        nameDialog.setHeaderText("Please enter your username.");
        nameDialog.setContentText("Name: ");
        
        while(username.equals("")) {
            Optional<String> name = nameDialog.showAndWait();
            if (!name.isPresent() || name.get().trim().equals(""))
                nameDialog.setHeaderText("You must enter a nonempty name: ");
            else if (name.get().trim().contains(" "))
                nameDialog.setHeaderText("The name must have no spaces: ");
            else
            username = name.get().trim();            
        }
        return username;
    }

    private void showUsers(String[] users){
        pVBox.getChildren().clear();
        pVBox.getChildren().add(new Label("You"));
        for (String user:users){
            if(username.equals(user)) {
                continue;
            }
            if(blocked.contains(user)){
                Label userLabel=new Label(user+" \u274C");
                Button blockButton = new Button("Blocked");
                blockButton.setDisable(true);

                Pane spacer=new Pane();
                HBox userHBox=new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                userHBox.getChildren().addAll(userLabel, spacer, blockButton);
                pVBox.getChildren().add(userHBox);
            }
            else{
                Label userLabel=new Label(user);
                Button blockButton = new Button("Block");
                blockButton.setOnAction(e -> {
                    blockButton.setDisable(true);
                    userLabel.setText(userLabel.getText() + " \u274C");
                    blockButton.setText("Blocked");
                    blocked.add(user);
                    try {
                        objectOut.writeObject(new Message("BLOCK " + user));
                        objectOut.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });


                Pane spacer=new Pane();
                HBox userHBox=new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                spacer.setMinWidth(50);
                userHBox.getChildren().addAll(userLabel, spacer, blockButton);
                pVBox.getChildren().add(userHBox);
            }
        }
    }

    class ServerListener implements Runnable {

        volatile boolean appRunning = false;

        public void run() {
            try {
                // Set up the socket for the Gui
                socket = new Socket(serverInfo.serverAddress, serverInfo.serverPort);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                objectOut = new ObjectOutputStream(socket.getOutputStream());
                objectIn=new ObjectInputStream(socket.getInputStream());
                
                appRunning = true;
                //Ask the gui to show the username dialog and update username
                //Send to the server
                Platform.runLater(() -> {
                    try{
                    objectOut.writeObject(new Message("NAME " + getName()));
                    objectOut.flush();
//                    out.println("NAME " + getName());
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                });

                //handle all kinds of incoming messages
                String message;
                String header;
                String givenString;

                Message incoming = new Message("");
                while (appRunning && (incoming = (Message)objectIn.readObject()) != null) {
                    givenString=incoming.getMessage();
                    if(!givenString.contains(" "))
                        continue;
                    header=givenString.substring(0,givenString.indexOf(" "));
                    message=givenString.substring(givenString.indexOf(" ")+1);
                    switch(header){
                        case "WELCOME":{
                            String user = message.substring(0,message.indexOf(" "));
                            String userList =message.substring(message.indexOf(" ")+1);
                            
                            //got welcomed? Now you can send messages!
                            if (user.equals(username)) {
                                Platform.runLater(() -> {
                                    stage.setTitle("Chatter - " + username);
                                    textInput.setEditable(true);
                                    sendButton.setDisable(false);
                                    messageArea.appendText("Welcome to the chatroom, " + username + "!\n");
                                });
                            }
                            else {
                                Platform.runLater(() -> {
                                    messageArea.appendText(user + " has joined the chatroom.\n");
                                });
                            }
                            Platform.runLater(() -> {
                                showUsers(userList.split(" "));
                            });
                            break;
                        }
                        case "CHAT": {
                            String user = message.substring(0,message.indexOf(" "));
                            String msg= message.substring(message.indexOf(" ")+1);

                            Platform.runLater(() -> {
                                messageArea.appendText(user + ": " + msg + "\n");
                            });
                            break;
                        }
                        case "EXIT":{
                            String user = message.substring(0,message.indexOf(" "));
                            String userList= message.substring(message.indexOf(" ")+1);
                        Platform.runLater(() -> {
                            messageArea.appendText(user + " has left the chatroom.\n");
                            showUsers(userList.split(" "));
                        });

                        break;
                        }
                        case "PCHAT":{
                            String name = message.substring(0,message.indexOf(" "));
                            String messageWithoutName=message.substring(message.indexOf(" ")+1);
                            Platform.runLater(() -> {
                                messageArea.appendText(name + " (private): " + messageWithoutName + "\n");
                            });
                            break;
                        }
                        case "BLOCKED":{
                            String msg=message;
                            Platform.runLater(() -> {
                                messageArea.appendText(msg + " has blocked you\n");
                            });
                            break;
                        }
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (Exception e) {
                if (appRunning)
                    e.printStackTrace();
            }
            finally {
                Platform.runLater(() -> {
                    stage.close();
                    pStage.close();
                });
                try {
                    if (socket != null)
                        socket.close();
                }
                catch (IOException e){
                }
            }
        }
    }
}