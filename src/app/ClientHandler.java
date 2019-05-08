package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
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
		
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}
   
}
