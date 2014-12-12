import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerModel {
	private final int PORT = 5566;
	private final int LIST_PORT = 7788;
	private final int SOUND_PORT = 3344;
	private int SID = 0;
	private String localAddress;
	
	private ArrayList<ObjectOutputStream> clientOutputStreams;
	private ArrayList<ObjectOutputStream> clientListOutputStreams;
	private ArrayList<Thread> clientHandlerThreadList;
	private ArrayList<Message> messagePools;
	private ArrayList<String> onlineList;
	//ArrayList<PrintWriter>soundClientList;
	//ArrayList<PrintWriter>soundWriter;
	
	private ArrayList<Client> clientList;
	
	public ServerModel(){
		clientOutputStreams = new ArrayList<ObjectOutputStream>();
		clientListOutputStreams = new ArrayList<ObjectOutputStream>();
		clientList = new ArrayList<Client>();
		clientHandlerThreadList = new ArrayList<Thread>();
		messagePools = new ArrayList<Message>();
		onlineList = new ArrayList<String>();
		localAddress = detectLocalAddress();
		//this.waitForClientConnection();
		
	}
	
	public static void main(String [] args){
		ServerModel sm = new ServerModel();
		sm.waitForClientConnection();
	}
	
	private String detectLocalAddress(){
		String s1 = "";  // unknown
	    try {
	    	Socket testsocket = new Socket("www.google.com.tw",80);
	    	String s = testsocket.getLocalAddress().getHostAddress();
	    	if(!s.equals("255.255.255.255")) 
	    		s1 = s;
	    	testsocket.close();
	    } catch(SecurityException ex) {
	    	ex.printStackTrace();
	    } catch(Exception ex) {
	    	ex.printStackTrace();
	    }
	    
	    if("".equals(s1)){
			try {
				s1 = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	    return s1;
	}
	
	private void waitForClientConnection(){
		try {
			ServerSocket serverSock = new ServerSocket(PORT);
			ServerSocket serverListSock = new ServerSocket(LIST_PORT);
			new Thread(new MessageDispatcher()).start();
			int k = 1;
			while(true){
				Socket clientSocket = serverSock.accept();
				Socket clientListSocket = serverListSock.accept();
				ObjectOutputStream messageOutputStream = new ObjectOutputStream(/*new BufferedOutputStream(*/clientSocket.getOutputStream()/*)*/);
				ObjectOutputStream onlineListOutputStream = new ObjectOutputStream(/*new BufferedOutputStream(*/clientListSocket.getOutputStream()/*)*/);
				System.out.println(clientSocket.toString());
				System.out.println(clientListSocket.toString());
				Client client = new Client(messageOutputStream, onlineListOutputStream);
				clientList.add(client);
				//clientOutputStreams.add(writer);
				//clientListOutputStreams.add(listwriter);
				//if(k==1){
					/*Thread refreshHandler = new Thread(new RefreshHandler(clientSocket,0));
					threadRefreshList.add(refreshHandler);
					refreshHandler.start();*/
					//k = 0;
				//}
				Thread t = new Thread(new ClientHandler(clientSocket, client, SID++));
				clientHandlerThreadList.add(t);
				t.start();
				//System.out.println("XD2");
				//area.append("Got a connection:" + SID + "\n");
				//area.append("From IP: " + clientSocket.getInetAddress() + "\n");
				//printCurrentTime();
				//area.setCaretPosition(area.getText().length());
				//while(!updateClientList(1,""));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized boolean sendToClient(Client client, Message message){
		try {
			client.getMessageOutputStream().writeObject((Object) message);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	private synchronized void updateOnlineClientList(){
		onlineList.clear();
		for(Client client : clientList){
			onlineList.add(new String(client.getName()));
		}
		
		for(Client client : clientList){
			try {
				client.getOnlineListOutputStream().writeObject((Object) onlineList);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class MessageDispatcher implements Runnable{
		public void run(){
			while(true){
				if( !messagePools.isEmpty() ){
					Message m = messagePools.get(0);
					synchronized(this){
						for(Client client : clientList){
							for(String reciever : m.getReceivers()){
								if(client.getName().equals(reciever)){
									sendToClient(client, m);
								}
							}
						}
					}
					messagePools.remove(0);
				}
				//System.out.println("DISPATCHER");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private boolean isMultiLogin(String name){
		for(String s : this.onlineList){
			if(s.equals(name)){
				return true;
			}
		}
		return false;
	} 
	
	public class ClientHandler implements Runnable{
		private ObjectInputStream reader;
		private Socket sock;
		private int cid;
		private Client client;
		public ClientHandler(Socket clientSocket, Client c, int id){
			cid = id;
			client = c;
			try {
				sock = clientSocket;
				reader = new ObjectInputStream(/*new BufferedInputStream(*/sock.getInputStream()/*)*/);
				System.out.println(reader.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		public void run(){
			try {
				boolean first = true;
				Object obj;
				while((obj = reader.readObject()) != null){
					Message message = (Message) obj;
					if(first){
						if(isMultiLogin(message.getSender())){
							break;
						}
						else{
							Thread.currentThread().setName(message.getSender());
							client.setName(message.getSender());
							updateOnlineClientList();
						}
						first = false;
					}
					System.out.println(message.getSender());
					System.out.println(message.getMessage());
					if(!message.getReceivers().isEmpty())
						System.out.println(message.getReceivers().get(0));
					messagePools.add(message);
					System.out.println("Name: " + client.getName());
					
					//area.append("From thread: " + Thread.currentThread().getName() + "\n");
					//area.append("From user: " + name + "\n");
					
					//area.append("Send to: ");
					//for(int i = 0 ; i < sendlist.length ; i++ ){
					//	area.append(sendlist[i] + " ");
					//}
					
					//area.append("\nMessage: " + message + "\n\n");
					//area.setCaretPosition(area.getText().length());
					//sendToClient( name , sendlist , message );
				}
			} catch (ClassNotFoundException e){
				e.printStackTrace();
			} catch (IOException e) {
				Thread.currentThread().setName("NULL");
				//while(!updateClientList(1,""));
				Thread.currentThread().interrupt();
				e.printStackTrace();
			}

			clientList.remove(client);
			clientHandlerThreadList.remove(Thread.currentThread());
			Thread.currentThread().setName("NULL");
			updateOnlineClientList();
			Thread.currentThread().interrupt();
			
		}
	}
	
}
