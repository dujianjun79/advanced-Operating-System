import java.io.*;
import java.util.*;
import java.net.*;

public class Parse
{   
	private String path; //path of configuration text
	private int number; //total number of nodes
	private int nodeNumber; //the idenfier of a specific node
	private int port; // port number of a host
	private List<Integer> connection; // nodes of a specific host can connect to
	private Map<Integer, String[]> serverlist; //list of all nodes
	private Map<Integer, List<Integer>> connectionlist; //list of connections of all nodes

	Parse(String path)
	{
		this.number=0;
		this.nodeNumber=Integer.MIN_VALUE;
		this.path=path;
		this.connection=new LinkedList<>();
		this.serverlist = new HashMap<>();
		this.connectionlist = new HashMap<>();
	}

	public int getNodenumber()
	{
		return this.nodeNumber;
	}

	public int getTotalnumber()
	{
		return this.number;
	}

	public int getPort()
	{
		return this.port;
	}

	public List<Integer> getConnection()
	{
		return this.connection;
	}

	public Map<Integer, String[]> getServerlist()
	{
		return this.serverlist;
	}

	public void parseFile(String Hostname)
	{
		Scanner sc = null;

		try
		{
			File file = new File(path);
			sc = new Scanner(file);
			
		}
		catch(FileNotFoundException e)
		{
			System.out.println("The file does not exist");
			System.exit(-1);
		}
		
		List<String> parsed = new ArrayList<>();

		while(sc.hasNextLine()){
			char[] line = sc.nextLine().trim().toCharArray();
			if(line.length==0||line[0]>57||line[0]<48) 
				continue;
            else 
            {
            	StringBuffer temp = new StringBuffer();
            	for(int i=0; i<line.length; i++)
            	{
            		if(line[i]=='#')
            			break;
            		else 
            			temp.append(line[i]);
            	}
            	if(temp.length()>0) parsed.add(temp.toString());
            }			
		} 

		try
		{
			number = Integer.parseInt(parsed.get(0).trim());
		}
        catch(NumberFormatException nef)
        {
        	System.out.println("The format of number of nodes is not correct!"+nef.getMessage());
        	System.exit(-1);
        }


        for(int i=1; i<number+1; i++)
        {
        	String line = parsed.get(i).trim();
        	String[] server = line.split("\\s+");
        	if(server.length!=3)
        	{
        		System.out.println("The format of node address and port is not correct!");
        		System.exit(-1);
        	}

        	int name=Integer.parseInt(server[0].trim());
        	String[] connector = {server[1], server[2]};
        	serverlist.put(name,connector);
        }

        for(Map.Entry<Integer, String[]> entry: serverlist.entrySet())
      	{
        	int identifier = entry.getKey();
        	String[] address =entry.getValue();
        	if(address[0].equals(Hostname))
        	{
          		nodeNumber=identifier;
          		port=Integer.parseInt(address[1]);
          		break;
        	}
      	}
        
        if(nodeNumber==Integer.MIN_VALUE){
        	System.out.println("This machine is not in the configuration file!");
        	System.exit(-1);
        }

        for(int i=number+1; i<parsed.size(); i++)
        {
        	String line = parsed.get(i).trim();
        	String[] linked = line.split("\\s+");
        	int name = Integer.parseInt(linked[0]);

        	List<Integer> temp = new ArrayList<>();
        	for(int j=1; j<linked.length; j++)
        	{
        		temp.add(Integer.parseInt(linked[j]));
        	}

        	connectionlist.put(name, temp);
        }

        List<Integer> tmp = connectionlist.get(nodeNumber);
        for(int e: tmp)
        {
        	if(e<nodeNumber)
        	{
        		connection.add(e);
        	}
        }
	}

	public static void main(String[] args) throws UnknownHostException
	{
		Parse test = new Parse("configuration.txt");
		String Hostname = InetAddress.getLocalHost().getHostName();
		System.out.println(Hostname);
		test.parseFile(Hostname);
	}
}