import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

/*
 * @author: Vaibhav Murkute
 * Project: Total-Order Multicast with Lamport's Algorithm 
 * date: 11/15/2018
 */

public class MaintainOrder {
	public static final String process_id = "P3";
	public static final int pid = 3;
	private static InetAddress SERVER_HOSTADD = InetAddress.getLoopbackAddress();
	public static int[] PROCESS_PORTLIST = new int[]{4444,5555,6666,7777};
	public static String[] PROCESS_EVENTLIST = new String[]{"a", "b", "c", "d"};
	private static final int NUM_PROCESSES = PROCESS_PORTLIST.length;
	public static volatile ArrayList<ProcessEvent> event_buffer = new ArrayList<>();
	public static volatile HashMap<String, HashSet<String>> ack_buffer = new HashMap<>();
	private static int logical_time = 0;
	private static int events_delivered = 0;
	private static ServerSocket server_socket;
	public static Thread connectionThread = null;
	public static Thread orderThread = null;
	public static Thread ackThread = null;
	public static Thread cleanerThread = null;

	public static Comparator<ProcessEvent> comp = (event1, event2)->{
		if(event1.getLogical_time() != event2.getLogical_time()){
			if(event1.getLogical_time() > event2.getLogical_time()){
				return 1;
			}
		}else{
			if(event1.getPid() > event2.getPid()){
				return 1;
			}
		}

		return -1;
	};

	public void init(){
		System.out.println("Process: " + process_id);
		
		// Thread 01: manageConnections
		connectionThread = (new Thread(){
			@Override
			public void run(){
				manageConnections();
				return;
			}
		});

		connectionThread.start();

		// Thread 02: enforceTotalOrder
		orderThread = (new Thread(){
			@Override
			public void run(){
				enforceTotalOrder();
				return;
			}
		});
		
		orderThread.start();

		// Thread 02: enforceTotalOrder
		ackThread = (new Thread(){
			@Override
			public void run(){
				manageAcknowledgements();
				return;
			}
		});

		ackThread.start();
		
		try {
			// Added this delay to wait till all other processes are in the listening state
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		ProcessEvent myEvent = new ProcessEvent();
		myEvent.setAck(false);
		myEvent.setEvent_id(PROCESS_EVENTLIST[pid]);
		myEvent.setLogical_time(logical_time);
		myEvent.setPid(pid);
		myEvent.setProcess_id(process_id);

		// Multicast event to everyone
		sendEvent(myEvent);

	}

	public void manageConnections(){
		int server_port = PROCESS_PORTLIST[pid];
		try {
			server_socket = new ServerSocket(server_port, 0, SERVER_HOSTADD);
			while(events_delivered != NUM_PROCESSES){
				Socket clientSocket = server_socket.accept();
				ObjectInputStream obj_ip = new ObjectInputStream(clientSocket.getInputStream());
				ProcessEvent event = (ProcessEvent)obj_ip.readObject();
				clientSocket.close();
				(new Thread(){
					@Override
					public void run(){
						manageBuffers(event);
						return;
					}
				}).start();
			}
			if(!server_socket.isClosed()){
				server_socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public synchronized void manageBuffers(ProcessEvent event){
		try {
			if(event.isAck()){
				if(ack_buffer.containsKey(event.getEvent_id())){
					ack_buffer.get(event.getEvent_id()).add(event.getProcess_id());
				}else{
					HashSet<String> sender_set = new HashSet<>();
					sender_set.add(event.getProcess_id());
					ack_buffer.put(event.getEvent_id(), sender_set);
				}
			}else{
				event_buffer.add(event);
				Collections.sort(event_buffer, comp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void manageAcknowledgements(){
		while(events_delivered != NUM_PROCESSES){
			if(!event_buffer.isEmpty()){
				ProcessEvent event = event_buffer.get(0);
				if(canWeSendAckToThisGuy(event)){
					ProcessEvent myEvent = new ProcessEvent();
					myEvent.setAck(true);
					myEvent.setEvent_id(event.getEvent_id());
					myEvent.setLogical_time(logical_time);
					myEvent.setPid(pid);
					myEvent.setProcess_id(process_id);
					sendEvent(myEvent);
					
					if(ack_buffer.containsKey(event.getEvent_id())){
						ack_buffer.get(event.getEvent_id()).add(process_id);
					}else{
						HashSet<String> sender_set = new HashSet<>();
						sender_set.add(process_id);
						ack_buffer.put(event.getEvent_id(), sender_set);
					}
				}
			}
		}
	}

	public void enforceTotalOrder(){
		while(events_delivered != NUM_PROCESSES){
			if(!event_buffer.isEmpty()){
				ProcessEvent event = event_buffer.get(0);
				if(ack_buffer.containsKey(event.getEvent_id())){
					if(ack_buffer.get(event.getEvent_id()).size() == NUM_PROCESSES){
						deliverEvent(event);
						event_buffer.remove(0);
					}
				}
			}
		}
	}

	public void sendEvent(ProcessEvent event){
		try {
			if(!event.isAck()){
				logical_time += 1;
			}
			event.setLogical_time(logical_time);
			Socket socket;
			ObjectOutputStream obj_op;
			for(int port : PROCESS_PORTLIST){
				socket = new Socket(SERVER_HOSTADD,port);
				obj_op = new ObjectOutputStream(socket.getOutputStream());
				obj_op.writeObject(event);

				obj_op.close();
				socket.close();
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean canWeSendAckToThisGuy(ProcessEvent event){
		String myEvent = PROCESS_EVENTLIST[pid];
		if(!ack_buffer.isEmpty() && ack_buffer.containsKey(event.getEvent_id())){
			if(ack_buffer.get(event.getEvent_id()).contains(MaintainOrder.process_id)){
				return false;
			}
		}
		if(event.getPid() == pid){
			return true;
		}
		if(ack_buffer.containsKey(myEvent) && ack_buffer.get(myEvent).size() == NUM_PROCESSES){
			return true;
		}
		if(logical_time == event.getLogical_time()){
			if(pid > event.getPid()){
				return true;
			}
		}else if(logical_time > event.getLogical_time()){
			return true;
		}

		return false;
	}

	public void deliverEvent(ProcessEvent event){
		System.out.println("Delivered: "+process_id + ":" + event.getProcess_id() + "." + event.getEvent_id());
		events_delivered += 1;
	}

}
