package p2psip.dht;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class ChannelManager extends TimerTask{

	private static final Logger log4j = Logger.getLogger(ChannelManager.class);

	private Timer chanTimer;
	private long monitorTimer=3000;
	private String url;
	private XmlRpcClient client;
	private XmlRpcClientConfigImpl config ;
	private ChannelObserver observer;
	private String channelObserved;

	public ChannelManager(String url,ChannelObserver observer) throws MalformedURLException{
		this.url = url;
	    config = new XmlRpcClientConfigImpl();
	    config.setServerURL(new URL(this.url));
	    client = new XmlRpcClient();
	    client.setConfig(config);
	    this.observer = observer;
	    this.chanTimer = new Timer("Channel Monitor Timer");
	}
	
	public void start(){
	    this.chanTimer.schedule(this, 0,this.monitorTimer);
	}

	public List<String> getChannelNames(){
		return getKey("channels");
	}

	public List<String> getChannelUsers(String name){
		return getKey(name);
	}
	
	public List<String> getKey(String what){
		LinkedList<String> result=new LinkedList<String>();
		Object params[];
		try {
			params = new Object[]{what.getBytes(),new Integer(200),"".getBytes(),"P2pSip"};
			Object resp = doXmlRpc("get",params);
			if(resp != null){
				if (resp instanceof Object[]){
					Object[] responses = (Object[]) resp;
					for(int i=0;i<responses.length;i++){
						if(responses[i] instanceof Object[]){
							Object resp2[] = (Object[])responses[i];
							for(int j=0;j<resp2.length;j++){
								if(resp2[j] instanceof byte[]){
									result.add(new String((byte[]) resp2[j]));
								}
							}
						}
					}
				}
			}
		} catch (XmlRpcException e) {
			log4j.error("RPC exception !",e);
		}
		return result;
	}
	
	public List<String> getUserInfo(String uri){
		return getKey(uri);
	}
	
	public Object doXmlRpc(String method,Object[] params) throws  XmlRpcException{
	    Object result = client.execute(method, params);
	    return result;
	}
	
	public static void main(String args[]){
		ChannelManager cm;
		try {
			cm = new ChannelManager("http://localhost:5861/RPC2",new ChannelObserver(){
				public void addChannel(String name) {
					log4j.debug("Channel added:"+name);}
				public void removeChannel(String name) {
					log4j.debug("Channel removed:"+name);}
				public void updateChannels(List<String> channels) {
					log4j.debug("Channels changed:"+channels);}
				public void updateUsers(List<String> chatters) {
					log4j.debug("Users at channel:"+chatters);
				}
			});
		} catch (MalformedURLException e) {
			log4j.error("Error:",e);
		}
	}

	public void run() {
		List<String> chans = getChannelNames();
		if(chans != null){
			log4j.debug("Channel list:");
			for(String chan : chans){
				log4j.debug(chan);
			}
			this.observer.updateChannels(chans);
		}else{
			log4j.debug("(no channels)");
		}
		
		if(channelObserved != null){
			List<String> chatters = getChannelUsers(channelObserved);
			if(chatters != null){
				log4j.debug("Users watching "+channelObserved+":");
				for(String user:chatters){
					log4j.debug(user);
				}
				observer.updateUsers(chatters);
			}else{
				log4j.debug("No users watching "+channelObserved);
			}
		}
	}

	public String getChannelObserved() {
		return channelObserved;
	}

	public void setChannelObserved(String channelObserved) {
		this.channelObserved = channelObserved;
//		this.chanTimer.cancel();
//	    this.chanTimer.schedule(this, 0,this.monitorTimer);
	}
}
