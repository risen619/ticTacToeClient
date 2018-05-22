package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.function.Function;

public class SocketThread implements Runnable
{
	private Thread thread;
	
	private Function<byte[], Void> onEmit = null;
	private Function<Void, Void> onConnected = null;
	
	public InputStream input;
	public OutputStream output;
	
	public Socket socket = null;
	public String host;
	public int port;

	public boolean isServer = false;
	public boolean keep = true;
	
	public SocketThread() { thread = new Thread(); }
	
	private void getConnected()
	{
		try
		{
			input = socket.getInputStream();
			output = socket.getOutputStream();
			Thread toRead = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						while(socket.isConnected())
						{
							byte[] buffer = new byte[128];
							input.read(buffer);
							System.out.println("Read: " + new String(buffer));
							if(onEmit != null) onEmit.apply(buffer);
						}
					}
					catch (IOException e) { e.printStackTrace(); }
				}
			});
			toRead.start();
			if(onConnected != null)
				onConnected.apply(null);
		}
		catch (IOException e) { e.printStackTrace(); return; }
	}
	
	public void setOnEmit(Function<byte[], Void> f) { onEmit = f; }
	public void setOnConnected(Function<Void, Void> f) { onConnected = f; }
	
	public void write(String data) throws IOException
	{
		output.write(data.getBytes(), 0, data.length());
		output.flush();
		System.out.println("Written: " + data);
	}
	
	private void serve() throws IOException
	{
		ServerSocket sSocket = new ServerSocket(port);
		System.out.println("Waiting for client...");
		socket = sSocket.accept();
		System.out.println("Client connected!");
		sSocket.close();
		getConnected();
	}
	
	private void connect() throws UnknownHostException, IOException
	{
		System.out.println("Trying to connect to " + host + ":" + port);
		socket = new Socket(host, port);
		System.out.println("Connected to server!");
		getConnected();
	}
	
	public void start()
	{
		thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}
	
	@Override
	public void run()
	{
		try
		{
			if(isServer) serve();
			else connect();
		} catch(Exception e) { e.printStackTrace(); }
	}

}
