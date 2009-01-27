package p2psip.ui.swt;


public interface ChatProcessor {
	
	public void messageReceived(String displayName, String uri, final String message);

	public void meSays(String texto);

}
