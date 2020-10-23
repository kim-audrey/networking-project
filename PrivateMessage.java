import java.io.Serializable;
import java.util.ArrayList;
public class PrivateMessage extends Message implements Serializable{
    public static final long serialVersionUID = 1L;
    public  String message="";
    public String[] recipientList;

    public PrivateMessage(String message, String[] list){
        super(message);
        this.recipientList = list;
    }

    public String getMessage() {
        return message;
    }
}