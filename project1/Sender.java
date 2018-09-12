import java.io.*;
import java.net.*;
import java.util.concurrent.*;

class Sender implements Runnable {
	private Socket socket;
	private ConcurrentHashMap<Integer, Integer> distance;
	//private ConcurrentHashMap<Integer, Integer> old; // store local distance, compared with distance to see if it is updated.
	private ObjectOutputStream out;

    Sender(Socket socket, ConcurrentHashMap<Integer,Integer> distance)
    {
    	this.socket = socket;
    	this.distance = distance;
		//this.old= new ConcurrentHashMap<>(distance); 
		this.out = null;
    }

    public void run()
    {
		try
		{
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(distance);
		}
		catch(IOException e)
		{
			System.out.println("output stream failed");
			System.exit(-1);
		}
		

		while(true)
		{	
			try
			{   
				//if(!old.equals(distance))
				//{
					
					for(int e: distance.keySet())
					{
						//old.put(e,distance.get(e));
						System.out.print(""+e+" has distance: "+distance.get(e));
						System.out.println();
					}
					out.reset();
					out.writeObject(distance);
					Thread.sleep(500);
				//}

			}
			catch(IOException eo)
			{
				System.out.println("read from client failed!");
				System.exit(-1);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

		}
		
		
	}
}