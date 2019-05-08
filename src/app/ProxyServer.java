package app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ProxyServer {
 
	public int port;
	public ServerSocket serverSocket;
	ArrayList<Thread> clientHandlers;
	public ProxyServer(int i) {
		this.port=i;
		try {
			clientHandlers=new ArrayList<Thread>();
			serverSocket =new ServerSocket(port);
			System.out.println("Created Socket");
			listen();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void listen() {
		// TODO Auto-generated method stub
		while(true)
		{
			try {
				Socket s=serverSocket.accept();
				System.out.println("Incoming Connection from: "+s.getRemoteSocketAddress());
				Thread th=new Thread(new ClientHandler(s));
				clientHandlers.add(th);
				th.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	public static void main(String[] args) {
        new ProxyServer(8888);
	}

}
