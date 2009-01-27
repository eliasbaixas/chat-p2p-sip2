package p2psip.dht;

import java.util.List;

public interface ChannelObserver {
	
	public void updateChannels(final List<String> channels);

	public void addChannel(String name);
	
	public void removeChannel(String name);

	public void updateUsers(List<String> chatters);

}
