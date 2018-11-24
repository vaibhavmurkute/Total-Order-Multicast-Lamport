
/*
 * @author: Vaibhav Murkute
 * Project: Total-Order Multicast with Lamport's Algorithm
 * date: 11/15/2018
 */

public class LamportTotalOrder {

	public static void main(String[] args) {
		MaintainOrder totalOrder = new MaintainOrder();
		totalOrder.init();

		try {
			if(MaintainOrder.connectionThread != null){
				MaintainOrder.connectionThread.join();
			}
			if(MaintainOrder.orderThread != null){
				MaintainOrder.orderThread.join();
			}
			if(MaintainOrder.ackThread != null){
				MaintainOrder.ackThread.join();
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Process "+MaintainOrder.process_id+" ended!");

	}

}
