import java.io.ObjectOutputStream;

public class Client {
	
	private ObjectOutputStream messageOutputStream;
	private ObjectOutputStream onlineListOutputStream;
	private String name;
	
	public Client(ObjectOutputStream mo, ObjectOutputStream ol){
		this.messageOutputStream = mo;
		this.onlineListOutputStream = ol;
		name = "";
	}

	public synchronized ObjectOutputStream getMessageOutputStream() {
		return messageOutputStream;
	}

	public synchronized ObjectOutputStream getOnlineListOutputStream() {
		return onlineListOutputStream;
	}

	public synchronized String getName() {
		return name;
	}

	public synchronized void setName(String name) {
		this.name = name;
	}
}
