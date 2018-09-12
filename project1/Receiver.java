import java.io.*;
import java.net.*;
import java.util.concurrent.*;

class Receiver implements Runnable {
	private Socket socket;
	private ConcurrentHashMap<Integer, Integer> distance;
	private ObjectInputStream in;

    Receiver(Socket socket, ConcurrentHashMap<Integer,Integer> distance)
    {
    	this.socket = socket;
    	this.distance = distance;
		this.in = null;
    }

    public void run()
    {
		try
		{
			in = new ObjectInputStream(socket.getInputStream());
		}
		catch(IOException e)
		{
			System.out.println("input stream failed");
			System.exit(-1);
		}
		
		while(true)
		{	
			try
			{   
				ConcurrentHashMap<Integer, Integer> neighbor = (ConcurrentHashMap<Integer,Integer>) in.readObject();
				for(int key: neighbor.keySet())
				{   
					System.out.println("received:");
					int tmp = neighbor.get(key)+1;
					System.out.print(""+tmp+" "+distance.get(key));
					System.out.println();
					if(tmp<distance.get(key))
					{
						distance.put(key, tmp);
					}
				}
			}
			catch(IOException eo)
			{
				System.out.println("read from client failed!");
				System.exit(-1);
			}
			catch (ClassNotFoundException c) 
			{
         		System.out.println("ConcurrentHashMap class not found");
         		System.exit(-1);
      		}

		}
	}
}