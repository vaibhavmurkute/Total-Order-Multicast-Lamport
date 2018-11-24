# Totally-Ordered Multicasting with Lamport's Algorithm

#### Implementation of  Totally-Ordered Multicasting in Distributed Systems using Lamport’s algorithm.
===============================================================================
#### Usage:
- ##### Script files (Run.bat and run.sh) are used just to run all the processes simulteneously to simulate real-world distributed systems. It contains no-other code than this.
##### •	For Windows:
  - ##### Just double-click 'Run.bat' file or open Command Prompt and navigate to the directory containing Jars (Process0.jar, Process1.jar etc). Open Run.bat file.
  - > ` Run.bat `
##### •	For Linux:
  - ##### Open the Terminal and navigate to the directory containing Jars (Process0.jar, Process1.jar etc). Open run.sh file.
  - > ` ./run.sh `
  #####
  #### Description:
- ##### Each process conducts local operations and numbers them as PID.EVENT_ID. After each operation is done, a process multicasts the event to all other processes in the distributed system. The expected outcome of this assignment is that events occurred at different processes will appear in the same order at each individual process.
######
- ##### To realize such a total order of events, each process maintains a buffer for received events and follow's Lamport's algorithm before delivering events.
######
- ##### The delivery of events is simply printing them on screen, in the format of CURRENT_PID: PID.EVENT_ID.
#####
#### Output:
###### 
![Output](total-order-multicast-lamport.PNG?raw=true)
######
