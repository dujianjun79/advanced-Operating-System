import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class DistributedNode{
	private ConcurrentLinkedDeque<Socket> socketlist; //store all sockets in a node
    private String Hostname; //name of the machine
    private int totalNumber; // total number of nodes in the system
    private int nodeNumber; // node number of the machine in configuration file
    private int portNumber; // port number for listening
    private Message message; 
    private List<Integer> connectionlist; // the nodes this machine connects to
    private Map<Integer, String[]> serverlist; //list of all nodes
    private ConcurrentHashMap<Integer, Integer> distance; // distance vector for RIP algorithm	
	
	DistributedNode() throws UnknownHostException
	{
		/* store all sockets a node has; which might be useful for future functions*/
		this.socketlist = new ConcurrentLinkedDeque<>(); 
		this.Hostname = InetAddress.getLocalHost().getHostName(); 

		/* extract information of a node and its connected nodes from the configuration file*/
		Parse parse = new Parse("config.txt"); 
		parse.parseFile(Hostname);
		this.totalNumber=parse.getTotalnumber();
		this.nodeNumber=parse.getNodenumber();
		this.portNumber=parse.getPort();
		this.connectionlist=parse.getConnection(); //get the nodes that this node connects to
		this.serverlist=parse.getServerlist(); // get the list of nodes number, machine name, and port

		/* Intinalize the distance vector */
	    this.distance = new ConcurrentHashMap<>();
        for(int i=0; i<totalNumber; i++)
        {
        	if(i==nodeNumber)
        		distance.put(i,0);
        	else 
        		distance.put(i, 20);
        }
		
	}
	
	public void initialize()
	{
		ServerListener sl = new ServerListener(portNumber,socketlist,distance);
		Thread t = new Thread(sl);
		t.start();
		
		ConnectOut con = new ConnectOut(connectionlist, serverlist, socketlist,distance);
		con.seek();

	}
	
	public static void main(String[] args) throws UnknownHostException 
	{ 
		DistributedNode test = new DistributedNode();
		test.initialize();
	    
	}
}

class ServerListener implements Runnable { // create a thread to listen and accept connections
	private int port;
	private ConcurrentLinkedDeque<Socket> list;
	private ConcurrentHashMap<Integer, Integer> distance;
	
	ServerListener(int port, ConcurrentLinkedDeque<Socket> list, 
							ConcurrentHashMap<Integer,Integer> distance)
	{
		this.port=port;
		this.list=list;
		this.distance=distance;
	}
	
	public void run()
	{
		try
        {
            ServerSocket listener = new ServerSocket(port);
			System.out.println("Server is running!");
            while (true) 
			{
                Socket socket = listener.accept();
				list.add(socket);

				Sender sd = new Sender(socket,distance);
				Thread thread1 = new Thread(sd);
				thread1.start();
				
				Receiver rv = new Receiver(socket,distance);
				Thread thread2 = new Thread(rv);
				thread2.start();
            }
        } 
		catch (IOException e) 
		{
            e.printStackTrace();
            System.err.println("Unable to start server logic");
        }
	}
}

class ConnectOut { // implement a class to connet to a server socket
	private ConcurrentLinkedDeque<Socket> socketlist;
	private ConcurrentHashMap<Integer,Integer> distance;
	private List<Integer> connectionlist;
	private Map<Integer, String[]> serverlist;
	
	ConnectOut(List<Integer> connectionlist, Map<Integer, String[]> serverlist, 
							ConcurrentLinkedDeque<Socket> socketlist, ConcurrentHashMap<Integer,Integer> distance)
	{
		this.connectionlist=connectionlist;
		this.serverlist=serverlist;
		this.socketlist=socketlist;
		this.distance =distance;
	}
	
	public void seek()
	{
		if(connectionlist.size()==0) 
			return;
		else 
		{
			Queue<String[]> queue = new LinkedList<>();
			
			for(int key : serverlist.keySet())
			{
				for(int e: connectionlist)
				{
					if(e==key)
					{
						queue.offer(serverlist.get(key));
					}
				}
			}
			
			while(true)
			{
				String[] machine = null; 
				try
				{   
					while(!queue.isEmpty())
					{
						machine = queue.poll();
						Socket socket = new Socket(machine[0], Integer.parseInt(machine[1]));
						socketlist.add(socket);

						Sender sd = new Sender(socket,distance);
						Thread thread1 = new Thread(sd);
						thread1.start();
				
						Receiver rv = new Receiver(socket,distance);
						Thread thread2 = new Thread(rv);
						thread2.start();
					}
					break;
				}
				catch(IOException e)
				{
					queue.offer(machine);
					continue;
				}
			}
		}
	}
}