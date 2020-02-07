package cn.roger.socket;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.view.View;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.Drawable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;


@DesignerComponent(version = SocketUtil.VERSION,
    description = "made in fan hao jie \n QQ:535607581",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png")

@SimpleObject(external = true)

public class SocketUtil extends AndroidNonvisibleComponent {
    public static final int VERSION = 1;//控件版本号
    private static final String LOG_TAG = "SocketUtil";
    private ComponentContainer container;
    private Context context;
    private ServerSocket serverSocket = null;
    OutputStream ou = null;//系统输出流
    Socket socket1 = null;
    Socket socket2 = null;
    Socket socket3 = null;
    Socket socket4 = null;
    Socket socket5 = null;
    Socket socket6 = null;
    int khd = 0;//客户端计数
    int kh = 0;//客户端标记
	
    String ip;//系统返回IP地址
    int port;//系统返回端口
    int con = 0;//控制信号
    byte[] bb = new byte[1000];//回复数据
    int[] i = new int[1000];//回复原始数据
    int k = 0;//回复数据的长度
    int DK = 0;//外部设置的端口
		
    public Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg){ GetMessage(msg.obj.toString()); }
    };
	
    public SocketUtil(ComponentContainer container) 
    {
        super(container.$form());
        this.container = container;
        context = (Context) container.$context();
    }
	
    public void getLocalIpAddress(ServerSocket serverSocket){
      try {
         for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();){
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();    enumIpAddr.hasMoreElements();){
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    String mIP = inetAddress.getHostAddress().substring(0, 3);
                    if(mIP.equals("192")){
                        ip = inetAddress.getHostAddress();    //获取本地IP
                        port = serverSocket.getLocalPort();
                    }
                }
            }
      }catch (SocketException e) {e.printStackTrace();}
   }
    
    @SimpleFunction(description = "start")//软件向控件写回复信息
    public void sendMessage(String s)
    {
	 k = s.length()/3;
	 for(int j = 0; j<k ;j++){i[j] = Integer.parseInt(s.substring(j*3,(j+1)*3));}
	 for(int j = 0; j<k+1 ;j++){bb[j+1] = (byte)i[j];}  
	 con=1;
	 
	 if(kh == 1){new ServerThread(socket1).start();}
	 if(kh == 2){new ServerThread(socket2).start();}
	 if(kh == 3){new ServerThread(socket3).start();}
	 if(kh == 4){new ServerThread(socket4).start();}
	 if(kh == 5){new ServerThread(socket5).start();}
	 if(kh == 6){new ServerThread(socket6).start();}
    }
    @SimpleFunction(description = "start")//关闭通信端口
    public void close(){ con = 2; }
	
    @SimpleEvent//向软件输出信息
    public void GetMessage(String s){ EventDispatcher.dispatchEvent(this, "GetMessage", s); }
	
    @SimpleFunction(description = "start")//打开通信端口
    public void receiveData(int PORT){
	DK = PORT;
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                try { serverSocket = new ServerSocket(DK);}
		catch (IOException e) { e.printStackTrace();}
                
                getLocalIpAddress(serverSocket);
                Message message_1 = handler.obtainMessage();
                message_1.obj = "IP:" + ip + " PORT: " + port;
                handler.sendMessage(message_1);

                while (true)
		{
                    Socket socket = null;
                    try {
                        socket = serverSocket.accept();
			    
			khd = khd + 1;
			if(kuf == 7){kud = 1;}
			if(khd == 1){socket1 = socket;}
			if(khd == 2){socket2 = socket;}
			if(khd == 3){socket3 = socket;}
			if(khd == 4){socket4 = socket;}
			if(khd == 5){socket5 = socket;}
			if(khd == 6){socket6 = socket;} 
			    
                        Message message_2 = handler.obtainMessage();
                        message_2.obj = "连上了！"+socket.getInetAddress().getHostAddress();
                        handler.sendMessage(message_2);
                   	 } 
		    catch (IOException e) {}
                    new ServerThread(socket).start();
                }
            }
        };
        thread.start();
 }
	
	class ServerThread extends Thread{
	    Socket socket; 
	    Message message_2;
	    public ServerThread(Socket socket){this.socket = socket; }	
	    @Override
	    public void run()
	    {
                while(true)
		{
		    try{
                       if(con==1){       
                       ou = socket.getOutputStream();
                       ou.write(bb , 1 , k);
                       ou.flush();
                       con=0;}}catch (IOException e) {}
			
		    try {
                	int msy = 0;  byte[] b = new byte[255]; int k = 0;
			msy = socket.getInputStream().read(b);
			if(socket1 == socket) kh = 1;
			if(socket2 == socket) kh = 2;
			if(socket3 == socket) kh = 3;
			if(socket4 == socket) kh = 4;
			if(socket5 == socket) kh = 5;
			if(socket6 == socket) kh = 6;
			if( msy >= 0)	
			{ 
			for(int j = 0; j<(b[5]+6) ; j++)
				{
				message_2 = handler.obtainMessage();
				message_2.obj = b[j]&0xff;
				handler.sendMessage(message_2);
				} 
			}
			} catch (IOException e){}
                }
            }
	}
}
