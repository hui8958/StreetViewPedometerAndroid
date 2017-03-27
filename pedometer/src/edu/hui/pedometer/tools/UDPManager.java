package edu.hui.pedometer.tools;

import java.io.IOException;
import java.net.*;

public class UDPManager {
	
	public void send(String message,String ipaddress,int port) {  
        message = (message == null ? "Hello IdeasAndroid!" : message);  
        int server_port = port;  
        DatagramSocket s = null;  
        try {  
            s = new DatagramSocket();  
        } catch (SocketException e) {  
            e.printStackTrace();  
        }  
        InetAddress local = null;  
        try {  
            // »»³É·þÎñÆ÷¶ËIP  
            local = InetAddress.getByName(ipaddress);  
        } catch (UnknownHostException e) {  
            e.printStackTrace();  
        }  
        int msg_length = message.length();  
        byte[] messageByte = message.getBytes();  
        DatagramPacket p = new DatagramPacket(messageByte, msg_length, local,  
                server_port);  
        try {  
            s.send(p);  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    } 
	
}
