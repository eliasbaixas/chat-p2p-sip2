package p2psip.ui.swt;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConfigurationWindow extends Composite{
	
	public static final Logger log4j = Logger.getLogger(ConfigurationWindow.class);
	
	private Configurable configureme;

	private Text domainText ;
	private Text portText ;	
	private Text serverText ;
	private Text usernameText ;	
	
	public ConfigurationWindow(Configurable configureme,Composite parent,int style){
		super(parent,style);
		this.configureme=configureme;
		initialize();
	}
	
	public void initialize(){

		GridLayout layout = new GridLayout(2,false);
		setLayout(layout);
		setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

		GridData labelsData = new GridData();
		labelsData.horizontalAlignment = SWT.FILL;
		labelsData.grabExcessHorizontalSpace = true;
		
		GridData textData = new GridData();
		textData.horizontalAlignment = SWT.FILL;
		textData.grabExcessHorizontalSpace = true;
		

		Label usernameLabel = new Label(this, SWT.NONE);
		usernameLabel.setText("Username:");
		usernameLabel.setLayoutData(labelsData);

		usernameText = new Text(this, SWT.BORDER);
		usernameText.setLayoutData(textData);
		usernameText.setText(configureme.getUsername());
		
		Label serverLabel = new Label(this, SWT.NONE);
		serverLabel.setText("Server:");
		serverLabel.setLayoutData(labelsData);
		
		serverText = new Text(this, SWT.BORDER);
		serverText.setLayoutData(textData);
		serverText.setText(configureme.getServer());

		Label portLabel = new Label(this, SWT.NONE);
		portLabel.setText("Port:");
		portLabel.setLayoutData(labelsData);

		portText = new Text(this, SWT.BORDER);
		portText.setLayoutData(textData);
		portText.setText(""+configureme.getServerPort());

		Label domainLabel = new Label(this, SWT.NONE);
		domainLabel.setText("Domain:");
		domainLabel.setLayoutData(labelsData);

		domainText = new Text(this, SWT.BORDER);
		domainText.setLayoutData(textData);
		domainText.setText(configureme.getDomain());
		
		Button doneButton = new Button(this,SWT.BORDER);
		doneButton.addMouseListener(new MouseListener(){
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {
				configureme.setDomain(domainText.getText());
				configureme.setServerPort(portText.getText());
				configureme.setUsername(usernameText.getText());
				configureme.setServer(serverText.getText());
			}
			public void mouseUp(MouseEvent e) {}
			});
	}
	
	public String getMyPort() {
		return null;
	}

	public void setMyPort(String myPort) {
	}

	public String getMyIp() {
		return null;
	}

	public void setMyIp(String myIp) {
	}

	public String getUrl() {
		return null;
	}

	public void setUrl(String url) {
	}

	public static void main(String args[]){
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(2, false));
		
		Configurable conf = new Configurable(){

			public String getDomain() {
				return "proxy.com";
			}

			public String getServerPort() {
				return "5060";
			}

			public String getServer() {
				return "127.0.0.1";
			}

			public String getUsername() {
				return "jou";
			}

			public void setDomain(String domain) {
				log4j.debug("Setting domain to:"+domain);
			}

			public void setServerPort(String port) {
				log4j.debug("Setting port to:"+port);
			}

			public void setServer(String server) {
				log4j.debug("Setting server to:"+server);
			}

			public void setUsername(String username) {
				log4j.debug("Setting username to:"+username);
			}

			public String getMyIp() {
				return "127.0.0.1";
			}

			public String getMyPort() {
				return "5070";
			}

			public void setMyIp(String myIp) {
				log4j.debug("Setting myIp to :"+myIp);
			}

			public void setMyPort(String myPort) {
				log4j.debug("Setting myPort to:"+myPort);
			}
			
			public void setUrl(String myUrl) {
				log4j.debug("Setting Url to:"+myUrl);
			}
			
			public String getUrl() {
				return "http://localhost:5081/RPC2";
			}
		};
		
		new ConfigurationWindow(conf,shell,SWT.NONE);
		
		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

	}
}