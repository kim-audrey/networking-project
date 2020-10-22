import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class ClientConnectionData {
    private Socket socket;
    private ObjectInputStream objectIn;
    private ObjectOutputStream objectOut;
    private BufferedReader input;
    private PrintWriter out;
    private String name;        // like localhost, ip address etc...
    private String username ="";
    private ArrayList<ClientConnectionData> blockedList = new ArrayList<>();


    public ClientConnectionData(Socket socket, BufferedReader input, PrintWriter out, String name) {
        this.socket = socket;
        this.input = input;
        this.out = out;
        this.name = name;
    }

    public ClientConnectionData(Socket socket, ObjectInputStream objectIn, ObjectOutputStream objectOut, BufferedReader input, PrintWriter out, String name) {
        this.socket = socket;
        this.objectIn = objectIn;
        this.objectOut = objectOut;
        this.input = input;
        this.out = out;
        this.name = name;
    }

    public ObjectInputStream getObjectIn() {
        return objectIn;
    }

    public void setObjectIn(ObjectInputStream objectIn) {
        this.objectIn = objectIn;
    }

    public ObjectOutputStream getObjectOut() {
        return objectOut;
    }

    public void setObjectOut(ObjectOutputStream objectOut) {
        this.objectOut = objectOut;
    }

    public void addBlock(ClientConnectionData offender){
        blockedList.add(offender);
    }

    public ArrayList<ClientConnectionData> getBlockedList(){
        return blockedList;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public BufferedReader getInput() {
        return input;
    }

    public void setInput(BufferedReader input) {
        this.input = input;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    
}
