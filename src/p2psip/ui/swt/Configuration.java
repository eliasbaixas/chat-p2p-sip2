package p2psip.ui.swt;

import java.util.Properties;

public class Configuration implements Configurable {

	private String username;
	private String domain;
	private String server;
	private String serverPort;
	private String myIp;
	private String myPort;
	private String url;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMyPort() {
		return myPort;
	}
	
	public void setMyPort(String myPort) {
		this.myPort = myPort;
	}
	
	public String getMyIp() {
		return myIp;
	}
	
	public void setMyIp(String myIp) {
		this.myIp = myIp;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getServerPort() {
		return serverPort;
	}
	public void setServerPort(String port) {
		this.serverPort = port;
	}
	
	public void populateFromProperties(Properties props){
		this.username = props.getProperty("username");
		this.domain = props.getProperty("domain");
		this.serverPort=props.getProperty("ServerPort");
		this.server=props.getProperty("server");
		this.myIp=props.getProperty("myIp");
		this.myPort=props.getProperty("myPort");
		this.url = props.getProperty("url");
	}

}
