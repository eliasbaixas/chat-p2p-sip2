package p2psip.ui.swt;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import p2psip.dht.ChannelManager;
import p2psip.dht.ChannelObserver;
import p2psip.sip.SipEngine;
import p2psip.tools.Estilo;

public class P2pSip implements ChatProcessor, ChannelObserver {
	
	public static Logger log4j = Logger.getLogger(P2pSip.class);

	private static ResourceBundle resP2pSip = ResourceBundle.getBundle("p2psip");

	private Shell shell;
	private Display display;
	private Chatter myself;
	private Configuration config;
	private File file;
	private Combo channelCombo;
	private SipEngine sipEngine;
	private Button registerButton ;
	private Button configureButton;
	private Text texto;
	private Button sendButton;
	private ChannelManager chanManager;
	private StyledText convers = null;
	private org.eclipse.swt.widgets.List buddies;
	private String selectedChannel;
	
	private int colornum=0;
	private Map<String,Chatter> chatters = new Hashtable<String,Chatter>();
	
	public static Color[] colors;
	
	private static final String[] columnNames = {resP2pSip.getString("Username"),
		 resP2pSip.getString("Domain"),
		 resP2pSip.getString("Server"),
		 resP2pSip.getString("Port")};

	private boolean isModified;
	
	public P2pSip() {
		config = new Configuration();
		myself = new Chatter(config.getUsername(),"sip:"+config.getUsername()+"@"+config.getDomain());

		Properties props = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream("config.properties");
			props.load(fis);
			config.populateFromProperties(props);
		} catch (FileNotFoundException e) {
			log4j.warn("Unable to load properties",e);
		} catch (IOException e) {
			log4j.warn("Unable to load properties",e);
		}
		sipEngine = new SipEngine(config,this);
		try {
			chanManager = new ChannelManager(config.getUrl(),this);
		} catch (MalformedURLException e1) {
			log4j.error("Malformed url:"+config.getUrl(),e1);
		}
	}
	

	public void initialize(Composite c, int style) {
	}

	public void say(Chatter buddy, String msg) {
		StyleRange styleRange = new StyleRange();
		styleRange.start = convers.getText().length();
		styleRange.length = msg.length();
		
		if (buddy == null) {
			styleRange.foreground = Estilo.negro;
			styleRange.fontStyle = SWT.BOLD;
		} else {
			styleRange.foreground = buddy.color;
		}

		convers.append(buddy.name +": " + msg + "\n");
		convers.setStyleRange(styleRange);
		convers.setSelection(convers.getCharCount());
	}
	
	public StyledText getConvers() {
		return convers;
	}
	
	public Shell open(Display display) {
		this.display=display;
		shell = new Shell(display,SWT.MAX | SWT.MIN | SWT.RESIZE | SWT.CLOSE);
		shell.setLayout(new GridLayout());
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				e.doit = closeP2pSip();
			}
		});
		
		shell.setText("Peer-to-Peer SIP");
		shell.setImage(new Image(null, "coolruc.gif"));

		createMenuBar();
		createTopButtons();
		createChatUI();
		createChatInput();
		
		shell.setBackgroundImage(Estilo.maderaDeRoble);
		shell.open();

		return shell;
	}
	
	private void createChatUI() {
		Composite compo = new Composite(shell,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		compo.setLayout(layout);
		compo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buddies = new org.eclipse.swt.widgets.List(compo,SWT.SINGLE|SWT.V_SCROLL);
		GridData buddiesData = new GridData(200,SWT.FILL,false,true,1,1);
		buddiesData.horizontalAlignment=GridData.BEGINNING;
		buddiesData.horizontalSpan=1;
		buddiesData.grabExcessVerticalSpace=true;
		buddiesData.widthHint = 200;
		buddies.setLayoutData(buddiesData);
		convers = new StyledText(compo, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		convers.setEditable(false);
		convers.setBackground(new Color(null, 255, 255, 255));
		GridData conversData =new GridData(SWT.FILL,SWT.FILL,true,true,1,1);
		conversData.horizontalSpan=1;
		convers.setLayoutData(conversData);
	}


	public void start(){
		try {
			sipEngine.start();
			chanManager.start();
		} catch (Exception e) {
			log4j.error("Unable to start sip engine !!");
		}
		log4j.debug("Sip Engine started successfully");
	}
	
	private boolean closeP2pSip() {
		if(isModified) {
			MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL);
			box.setText(shell.getText());
			box.setMessage(resP2pSip.getString("Close_save"));
		
			int choice = box.open();
			if(choice == SWT.CANCEL) {
				return false;
			} else if(choice == SWT.YES) {
				if (!save()) return false;
			}
		}
			
		return true;
	}
	
	private void displayError(String msg) {
		MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
		box.setMessage(msg);
		box.open();
	}
	

	private boolean save() {
		if(file == null) 
			return saveAs();
		
		Cursor waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
		shell.setCursor(waitCursor);
		
		FileWriter fileWriter = null;
		try { 
			fileWriter = new FileWriter(file.getAbsolutePath(), false);
		} catch(FileNotFoundException e) {
			displayError(resP2pSip.getString("File_not_found") + "\n" + file.getName());
			return false;
		} catch(IOException e ) {
			displayError(resP2pSip.getString("IO_error_write") + "\n" + file.getName());
			return false;
		} finally {
			shell.setCursor(null);
			waitCursor.dispose();
			
			if(fileWriter != null) {
				try {
					fileWriter.close();
				} catch(IOException e) {
					displayError(resP2pSip.getString("IO_error_close") + "\n" + file.getName());
					return false;
				}
			}
		}

		shell.setText(resP2pSip.getString("Title_bar")+file.getName());
		isModified = false;
		return true;
	}
	private boolean saveAs() {
			
		FileDialog saveDialog = new FileDialog(shell, SWT.SAVE);
		saveDialog.setFilterExtensions(new String[] {"*.adr;",  "*.*"});
		saveDialog.setFilterNames(new String[] {"Address Books (*.adr)", "All Files "});
		
		saveDialog.open();
		String name = saveDialog.getFileName();
			
		if(name.equals("")) return false;

		if(name.indexOf(".adr") != name.length() - 4) {
			name += ".adr";
		}

		File file = new File(saveDialog.getFilterPath(), name);
		if(file.exists()) {
			MessageBox box = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
			box.setText(resP2pSip.getString("Save_as_title"));
			box.setMessage(resP2pSip.getString("File") + file.getName()+" "+resP2pSip.getString("Query_overwrite"));
			if(box.open() != SWT.YES) {
				return false;
			}
		}
		this.file = file;
		return save();	
	}
	
	private void configure() {
		ConfigurationDialog dialog;
		try {
			dialog = new ConfigurationDialog(shell,config);
			dialog.open();
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
	}
	
	private void createTopButtons(){
		Composite compo = new Composite(shell,SWT.NONE);
		compo.setLayout(new FillLayout());
		registerButton = new Button(compo,SWT.NONE);
		registerButton.setText("Register");
		configureButton = new Button(compo,SWT.NONE);
		registerButton.addMouseListener(new MouseListener(){
			public void mouseDoubleClick(MouseEvent arg0) {}
			public void mouseDown(MouseEvent arg0) {
				sipEngine.sendRegister();
				registerButton.setEnabled(false);
			}
			public void mouseUp(MouseEvent arg0) {}
			
		});
		configureButton.setText("Configure");
		configureButton.addMouseListener(new MouseListener(){
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {
				configure();
			}
			public void mouseUp(MouseEvent e) {}
			});
		channelCombo = new Combo(compo,SWT.DROP_DOWN | SWT.READ_ONLY);
		channelCombo.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			public void widgetSelected(SelectionEvent arg0) {
				setSelectedChannel(channelCombo.getText());
			}
		});
	}
	
	private void createChatInput(){
		Composite compo = new Composite(shell,SWT.NONE);
		GridLayout layout = new GridLayout();
		compo.setLayout(layout);
		GridData conversData =new GridData(SWT.FILL, SWT.BOTTOM, true, true, 2,1);
		compo.setLayoutData(conversData);

		texto = new Text(compo, SWT.BORDER);
		texto.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1,1));
		
		sendButton = new Button(compo, SWT.PUSH);
		sendButton.setText("Send");
		sendButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false,1, 1));

		sendButton.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {
				if (texto.getText() != null && !texto.getText().equals("")) {
					meSays(texto.getText());
					texto.setText("");
				}
			}
			public void mouseUp(MouseEvent e) {
			}
		});

		texto.addKeyListener(new KeyListener() {
			public void keyPressed(final KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					if (texto.getText() != null && !texto.getText().equals("")) {
						meSays(texto.getText());
						texto.setText("");
					}
				}
			}

			public void keyReleased(KeyEvent e) {

			}
		});
	}
	
	/** 
	 * To be called from different threads (that's why it uses 
	 * @param channels
	 */
	public void updateChannels(final List<String> channels){
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				channelCombo.removeAll();
				int i=0,selected=-1;
				for (String chan : channels) {
					channelCombo.add(chan);
					if(chan.equals(selectedChannel))
						selected=i;
					i++;
				}
				if(i!=-1)
					channelCombo.select(selected);
			}
		});
	}

	public void addChannel(String name){
		channelCombo.add(name);
	}
	
	public void removeChannel(String name){
		channelCombo.remove(name);
	}
	
	/** 
	 * To be invoked by outsider threads (non-UI)
	 */
	public void updateUsers(final List<String> chatters) {
		getDisplay().asyncExec(new Runnable(){
			public void run() {
				buddies.removeAll();
				for(String usr:chatters){
					buddies.add(usr);
				}
			}
		});
	}
	
	private Menu createMenuBar() {
		Menu menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);
		
		createFileMenu(menuBar);
		createHelpMenu(menuBar);
		
		return menuBar;
	}

	private void createFileMenu(Menu menuBar) {
		//File menu.
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText(resP2pSip.getString("File_menu_title"));
		Menu menu = new Menu(shell, SWT.DROP_DOWN);
		item.setMenu(menu);

		menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu menu = (Menu)e.widget;
				MenuItem[] items = menu.getItems();
//				items[1].setEnabled(table.getSelectionCount() != 0); // edit contact
//				items[5].setEnabled((file != null) && isModified); // save
//				items[6].setEnabled(table.getItemCount() != 0); // save as
			}
		});


		//File -> New Contact
		MenuItem subItem = new MenuItem(menu, SWT.NONE);
		subItem.setText(resP2pSip.getString("Configure"));
		subItem.setAccelerator(SWT.MOD1 + 'N');
		subItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				configure();
			}
		});
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		//File -> Save.
		subItem = new MenuItem(menu, SWT.NONE);
		subItem.setText(resP2pSip.getString("Save_address_book"));
		subItem.setAccelerator(SWT.MOD1 + 'S');
		subItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			}
		});
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		//File -> Exit.
		subItem = new MenuItem(menu, SWT.NONE);
		subItem.setText(resP2pSip.getString("Exit"));
		subItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
	}

	private Menu createPopUpMenu() {
		Menu popUpMenu = new Menu(shell, SWT.POP_UP);

		/** 
		 * Adds a listener to handle enabling and disabling 
		 * some items in the Edit submenu.
		 */
		popUpMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu menu = (Menu)e.widget;
				MenuItem[] items = menu.getItems();
//				int count = table.getSelectionCount();
//				items[2].setEnabled(count != 0); // edit
//				items[3].setEnabled(count != 0); // copy
//				items[4].setEnabled(copyBuffer != null); // paste
//				items[5].setEnabled(count != 0); // delete
//				items[7].setEnabled(table.getItemCount() != 0); // find
			}
		});

		//New
		MenuItem item = new MenuItem(popUpMenu, SWT.PUSH);
		item.setText(resP2pSip.getString("Configure"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				configure();
			}
		});
		
		new MenuItem(popUpMenu, SWT.SEPARATOR);	
		
		//Edit
		item = new MenuItem(popUpMenu, SWT.PUSH);
		item.setText(resP2pSip.getString("Pop_up_edit"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			}
		});

		//Copy
		item = new MenuItem(popUpMenu, SWT.PUSH);
		item.setText(resP2pSip.getString("Pop_up_copy"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			}
		});
		
		//Paste
		item = new MenuItem(popUpMenu, SWT.PUSH);
		item.setText(resP2pSip.getString("Pop_up_paste"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			}
		});
		
		new MenuItem(popUpMenu, SWT.SEPARATOR);	
		
		return popUpMenu;
	}

	private void createHelpMenu(Menu menuBar) {
		
		//Help Menu
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText(resP2pSip.getString("Help_menu_title"));	
		Menu menu = new Menu(shell, SWT.DROP_DOWN);
		item.setMenu(menu);
		
		//Help -> About Text Editor
		MenuItem subItem = new MenuItem(menu, SWT.NONE);
		subItem.setText(resP2pSip.getString("About"));
		subItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MessageBox box = new MessageBox(shell, SWT.NONE);
				box.setText(resP2pSip.getString("About_1") + shell.getText());
				box.setMessage(shell.getText() + resP2pSip.getString("About_2"));
				box.open();
			}
		});
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		colors = new Color[]{
				/*naranja*/ new Color(display, 255, 128, 0),
				/*negro*/new Color(display, 0, 0, 0),
				/*verde*/new Color(display, 0, 100, 0),
				/*verde_claro*/new Color(display, 0, 128, 64),
				/*blanco*/new Color(display, 255, 255, 255),
				/*gris*/new Color(display, 190, 190, 190),
				/*grana*/new Color(display, 150, 0, 0),
				/*amarillo*/new Color(display, 255, 255, 0),
				/*rojo*/new Color(display, 255, 0, 0),
				/*amarillo_suave*/new Color(display, 255, 250, 120),
				/*azul_marino*/new Color(display, 0, 0, 128),
				/*azul_celeste*/new Color(display, 0, 128, 255),
				/*azul*/new Color(display, 35, 105, 111)
			};

		P2pSip application = new P2pSip();
		Shell shell = application.open(display);
		application.start();
		application.addChannel("TVCInt");
		application.addChannel("MTV");
		while(!shell.isDisposed()){
			if(!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public Display getDisplay() {
		return display;
	}

	public void messageReceived(String displayName, String uri, final String message) {
		Chatter buddy = this.chatters.get(uri);
		if(buddy==null){
			buddy = new Chatter(displayName,uri);
			buddy.setColor(this.colors[this.colornum++ % this.colors.length]);
			this.chatters.put(uri, buddy);
		} else {
			buddy.updateLastMsg();
		}
		final Chatter myBuddy = buddy;
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				say(myBuddy, message);
			}
		});
	}

	public void meSays(String texto) {
		say(myself, texto);
		for(Chatter usr:this.chatters.values()){
			this.sipEngine.sendRegister();
		}
	}

	public String getSelectedChannel() {
		return selectedChannel;
	}

	public void setSelectedChannel(String selectedChannel) {
		log4j.debug("Setting selected Channel to:"+selectedChannel);
		this.selectedChannel = selectedChannel;
		this.chanManager.setChannelObserved(selectedChannel);
	}
}
