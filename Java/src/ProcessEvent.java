import java.io.Serializable;
import java.util.Comparator;

/*
 * @author: Vaibhav Murkute
 * Project: Total-Order Multicast with Lamport's Algorithm 
 * date: 11/15/2018
 */

public class ProcessEvent implements Serializable, Comparator<ProcessEvent>{
	private static final long serialVersionUID = 1001626620L;
	private String process_id;
	private int pid;
	private int logical_time;
	private String event_id;
	private boolean ack = false;
	
	public String getProcess_id() {
		return process_id;
	}
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public void setProcess_id(String process_id) {
		this.process_id = process_id;
	}
	public int getLogical_time() {
		return logical_time;
	}
	public void setLogical_time(int logical_time) {
		this.logical_time = logical_time;
	}
	public String getEvent_id() {
		return event_id;
	}
	public void setEvent_id(String event_id) {
		this.event_id = event_id;
	}
	public boolean isAck() {
		return ack;
	}
	public void setAck(boolean ack) {
		this.ack = ack;
	}
	@Override
	public int compare(ProcessEvent event1, ProcessEvent event2) {
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
	}
	
	
}
