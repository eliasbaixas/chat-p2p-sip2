package p2psip.ui.swt;

import java.util.Date;

import org.eclipse.swt.graphics.Color;

public class Chatter {
	
	public Color color;
	public String name;
	public String uri;
	public Date lastMsg;
	
	public Chatter() {
	}

	public Chatter(String name,String uri){
		this.name=name;
		this.uri=uri;
		this.lastMsg=new Date();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Date getLastMsg() {
		return lastMsg;
	}

	public void updateLastMsg(){
		this.lastMsg=new Date();
	}

}
