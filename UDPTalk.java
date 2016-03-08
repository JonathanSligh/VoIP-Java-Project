import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

class UDPTalk
{
	//args receiving/server port
	public static void main(String args[]) throws Exception
	{
		if (args.length < 1)
		{
			System.out.println("Incorrect Args, please use correct format (receiving port)");
			System.exit(0);
		}
		ServerThread t2 = new ServerThread(Integer.parseInt(args[0]));
		t2.start();
		System.out.println("Please enter the host IP that you want to connect to:");
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		String ip = inFromUser.readLine();
		System.out.println("Please enter the host port that you want to connect to:");
		int port = Integer.parseInt(inFromUser.readLine());
		ClientThread t1 = new ClientThread(ip, port);
		t1.start();
	}
}
class ClientThread extends Thread {
	private Thread t;
	private String IPaddr;
	private int port;
	DatagramPacket UDPPacket=null;
	private String threadName = "Client Thread";
	ClientThread(String IP, int sendingPort)
	{
		IPaddr = IP;
		port = sendingPort;
	}
	public void run() {
		System.out.println("doing things " +  threadName );
		try{
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(IPaddr);
			AudioFormat format=  new AudioFormat(8000.0f, 16, 1, true, true);
			TargetDataLine line = null;
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, 
			format); // format is an AudioFormat object
			if (!AudioSystem.isLineSupported(info)) {
				// Handle the error ... 

			}
			// Obtain and open the line.
			try {
				line = (TargetDataLine) AudioSystem.getLine(info);
				line.open(format);
			} catch (LineUnavailableException ex) {
				// Handle the error ... 
			}
			// Assume that the TargetDataLine, line, has already
			// been obtained and opened.
			ByteArrayOutputStream out  = new ByteArrayOutputStream();
			int numBytesRead = 1;
			byte[] data = new byte[1024];
			if (line != null)
			{
				data = new byte[line.getBufferSize() / 5];
			}

			// Begin audio capture.
			line.start();

			while (numBytesRead != 0) {
				// Read the next chunk of data from the TargetDataLine.
				numBytesRead =  line.read(data, 0, data.length);
				// Send this chunk of data.
				UDPPacket = new DatagramPacket(data, numBytesRead, InetAddress.getByName(IPaddr), port);
				clientSocket.send(UDPPacket);
			}
			clientSocket.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Thread " +  threadName + " exiting.");
	}

	public void start ()
	{
		System.out.println("Starting " +  threadName );
		if (t == null)
		{
			t = new Thread (this, "ClientThread");
			t.start ();
		}
	}

}
class ServerThread extends Thread {
	private Thread t;
	private int receivingPort;
	AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
	DatagramPacket recPacket=null;
	private String threadName = "Server Thread";
	ServerThread(int rPort)
	{
		receivingPort = rPort;	
	}   
	public void run() {
		System.out.println("doing things " +  threadName );
		byte[] receiveData = new byte[1024];
		try{
			DatagramSocket serverSocket = new DatagramSocket(receivingPort);
			DataLine.Info newinfo = new DataLine.Info(SourceDataLine.class, format);
			SourceDataLine newline = (SourceDataLine)AudioSystem.getLine(newinfo);
			//basically continue forever
			while(newline != null)
			{
				//receive packet
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				InputStream input = new ByteArrayInputStream(receiveData);
				AudioInputStream ais = new AudioInputStream(input, format, receiveData.length / format.getFrameSize());
				newline.open(format);
				newline.start();	
				int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
				byte buffer[] = new byte[bufferSize];

				int count;
				//basically play it
				while ((count = 
				ais.read(buffer, 0, buffer.length)) != -1) {
					if (count > 0) {
						newline.write(buffer, 0, count);
					}
				}
				newline.drain();
			}
			newline.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Thread " +  threadName + " exiting.");
	}

	public void start ()
	{
		System.out.println("Starting " +  threadName );
		if (t == null)
		{
			t = new Thread (this, "ServerThread");
			t.start ();
		}
	}

}
