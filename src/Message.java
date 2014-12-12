import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;


public class Message implements Serializable {
	private String sender;
	private ArrayList<String> recievers;
	private String message;
	private String time;
	
	public Message(String s, ArrayList<String> r, String m){
		this.sender = s;
		this.recievers = r;
		this.message = m;
		this.time = Calendar.getInstance().toString(); 
	}

	public synchronized String getSender() {
		return sender;
	}

	public synchronized ArrayList<String> getReceivers() {
		return recievers;
	}

	public synchronized String getMessage() {
		return message;
	}

	public synchronized String getTime() {
		return time;
	}
}
