
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;

public class ClientHandler implements Runnable {

	public Socket localSocket;
	BufferedReader clientToProxy;
	BufferedWriter proxyToClient;
   public ClientHandler(Socket s)
   {
	this.localSocket=s;
	try {
		this.localSocket.setSoTimeout(2000);
		clientToProxy=new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
		proxyToClient=new BufferedWriter(new OutputStreamWriter(localSocket.getOutputStream()));
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
   }
   @Override
	public void run() {
		// TODO Auto-generated method stub
      String request;
      try {
		request=clientToProxy.readLine();
		System.out.println("Request from client: "+request);
		String requestType= request.substring(0, request.indexOf(' '));
		String url=request.substring(request.indexOf(' ')+1, request.lastIndexOf(' '));
		System.out.println("requestType: "+requestType+", \nurl: "+url+", ");
		if(requestType.equals("CONNECT"))
		{
			System.out.println("https connection requested");
			handleHttpsConnection(url);
		}
		else
		{
			System.out.println("Handling http connection");
			handleHttpConnection(url);
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      
	}

private void handleHttpConnection(String url) {
	// TODO Auto-generated method stub
	try {
		URL remoteUrl= new URL(url);
		HttpURLConnection proxyToServerConn= (HttpURLConnection)remoteUrl.openConnection();
		proxyToServerConn.setDoOutput(true);
		proxyToServerConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		proxyToServerConn.setRequestProperty("Content-Language", "en-US");
		proxyToServerConn.setUseCaches(false);
		BufferedReader proxyToServerReader=new BufferedReader(new InputStreamReader(proxyToServerConn.getInputStream()));
		String responseHeader="HTTP/1.1 200 OK\n"+ "Proxy-agent: ProxyServer/1.0\n"+"\r\n";
		proxyToClient.write(responseHeader);
		String serverResponse;
		while((serverResponse=proxyToServerReader.readLine())!=null)
		{
			proxyToClient.write(serverResponse);
		}
		proxyToClient.flush();
		proxyToServerReader.close();	
		proxyToClient.close();
		clientToProxy.close();
		
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		String line = "HTTP/1.0 404 Not found\n" +
				"Proxy-Agent: ProxyServer/1.0\n" +
				"\r\n";
		try{
			proxyToClient.write(line);
			proxyToClient.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}
private void handleHttpsConnection(String url) {
	// TODO Auto-generated method stub
	System.out.println("HTTPS connection intitated url: "+url);
	String[] socketAddress= url.split(":");
	url=socketAddress[0];
	int port=Integer.parseInt(socketAddress[1]); 
	try {
		for(int i=0;i<5;i++)
		   clientToProxy.readLine();
		Socket proxyServerSocket=new Socket(url, port);
		proxyServerSocket.setSoTimeout(10000);
		System.out.println("Accepted: "+proxyServerSocket.getRemoteSocketAddress());
		String responseHeader="HTTP/1.1 200 OK\n"+ "Proxy-agent: ProxyServer/1.0\n"+"\r\n";
		proxyToClient.write(responseHeader);
		proxyToClient.flush();
		ClientToProxyDataTransfer clientToProxyDataTransfer=new ClientToProxyDataTransfer(localSocket.getInputStream(), proxyServerSocket.getOutputStream());
		new Thread(clientToProxyDataTransfer).start();
		byte[] buffer= new byte[3072];
		int read;
		do {
			read=proxyServerSocket.getInputStream().read(buffer);
			if(read>0)
			 {
				localSocket.getOutputStream().write(buffer, 0, read);
				if(proxyServerSocket.getInputStream().available()<1)
					localSocket.getOutputStream().flush();
				
			 }
		}while(read>=0);
		//closing down resources
		try {
		proxyToClient.close();
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	catch (SocketTimeoutException e) {
		// TODO: handle exception
		String line = "HTTP/1.0 504 Timeout Occured after 10s\n" +
				"User-Agent: ProxyServer/1.0\n" +
				"\r\n";
		try{
			proxyToClient.write(line);
			proxyToClient.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}  
class ClientToProxyDataTransfer implements Runnable
{
   InputStream clientToProxy;
   OutputStream proxyToServer;
   
	public ClientToProxyDataTransfer(InputStream clientToProxy, OutputStream proxyToServer) {
	this.clientToProxy = clientToProxy;
	this.proxyToServer = proxyToServer;
}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			byte[] buffer =new byte[3072];
			int read;
			do {
				read=clientToProxy.read(buffer);
				if(read>0)
				{
					proxyToServer.write(buffer,0, read);
					if(clientToProxy.available()<1)
						proxyToServer.flush();
				}
				
			}while(read>0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
}
