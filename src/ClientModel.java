import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;


public class ClientModel {
	
	private Socket sock;
	private Socket listsock;
	private Socket soundsock;
	
	private final int PORT = 5566;
	private final int LIST_PORT = 7788;
	
	private ObjectInputStream messageInputStream;
	private ObjectInputStream onlineListInputStream;
	private ObjectOutputStream messageOutputStream;
	private PrintWriter listrefresher;
	private ArrayList<String> onlineList;
	
	
	private Thread readerThread;
	private Thread listThread;
	
	public ClientModel(){
		
	}
	
	public static void main(String [] args){
		ClientModel cm = new ClientModel();
		cm.connectToServer("140.119.164.221");
		Scanner sc = new Scanner(System.in);
		String myname = sc.nextLine();
		cm.sendMessage(new Message(myname, new ArrayList<String>(), ""));
		while(sc.hasNext()){
			ArrayList<String> recievers = new ArrayList<String>();
			//System.out.println("Input Name:");
			//String name = sc.nextLine();
			System.out.println("Input N_Recievers:");
			int nRec = sc.nextInt();
			sc.nextLine();
			for(int i = 0; i < nRec; i++){
				System.out.println("Input Recievers " + i);
				String rec = sc.nextLine();
				recievers.add(rec);
			}
			System.out.println("Input Message:");
			String msg = sc.nextLine();
			Message message = new Message(myname, recievers, msg);
			cm.sendMessage(message);
		}
	}
	
	public class IncomingReader implements Runnable{
		public void run(){
			try{
				Object obj;
				while( ( obj = messageInputStream.readObject() ) != null  ){
					Message message = (Message) obj; 
					System.out.println(message.getSender());
					System.out.println(message.getMessage());
					System.out.println();
					//message.append(name+"\n");
					//message.append( messages + "\n\n" );
					//message.setCaretPosition(message.getText().length());  
					//setStat("Recieved Message.",Color.blue);
				    /*if (!frame.isFocused() && notify.isSelected() ) {
				    	frame.setExtendedState(JFrame.ICONIFIED);
				    	frame.setExtendedState(JFrame.NORMAL);
				    	frame.setExtendedState(JFrame.ICONIFIED);
				    }*/

				}
			} catch(ClassNotFoundException e){
				e.printStackTrace();
			} catch(IOException e){
				e.printStackTrace();
				//setStat("Recieving Message Failed.",Color.red);
			}
		}
	}
	
	public class ListReader implements Runnable{
		public void run(){
			ArrayList<String> tmpList;
			try{
				while( ( tmpList = (ArrayList<String>) onlineListInputStream.readObject() ) != null ){
					onlineList = tmpList;
				}
			} catch(ClassNotFoundException e){
				e.printStackTrace();
			} catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
	
	public boolean connectToServer(String host){
	
		try {
			sock = new Socket( host , PORT );
			listsock = new Socket( host , LIST_PORT );
			
			messageInputStream = new ObjectInputStream(/*new BufferedInputStream(*/sock.getInputStream()/*)*/);
			onlineListInputStream = new ObjectInputStream(/*new BufferedInputStream(*/listsock.getInputStream()/*)*/);
			
			messageOutputStream = new ObjectOutputStream(/*new BufferedOutputStream(*/sock.getOutputStream()/*)*/);
			
			System.out.println(sock.toString());
			System.out.println(listsock.toString());
			
			readerThread = new Thread(new IncomingReader());
			listThread = new Thread(new ListReader());
			readerThread.start();
			listThread.start();
			//setStat("Connected to server " + serverT.getText(),Color.blue);
			return true;
		} catch (UnknownHostException e) {
			//setStat("Connect Failed. No such IP.",Color.red);
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void sendMessage(Message msg){
		try {
			messageOutputStream.writeObject((Object) msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
