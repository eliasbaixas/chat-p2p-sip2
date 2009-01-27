package p2psip.sip;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

import javax.sip.ClientTransaction;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import p2psip.ui.swt.ChatProcessor;
import p2psip.ui.swt.Chatter;
import p2psip.ui.swt.Configurable;
import p2psip.ui.swt.Configuration;

public class SipEngine implements SipListener {
	
	public final static Logger log4j = Logger.getLogger(SipEngine.class);
	
	private SipStack sipStack;

	private MessageFactory messageFactory;

	private AddressFactory addressFactory;

	private HeaderFactory headerFactory;

	private SipProvider sipProvider;
	
	private RouteHeader routeToProxy;
	
	private Configurable config;
	
	private ListeningPoint listeningPoint;
	
	private ChatProcessor chat;
	
	private boolean started = false;

	public SipEngine(Configurable config,ChatProcessor myChat){
		this.config=config;
		this.chat = myChat;
	}
	
	public void start() throws Exception {
		sipStack = null;
		sipProvider = null;

		SipFactory sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");

		headerFactory = sipFactory.createHeaderFactory();
		addressFactory = sipFactory.createAddressFactory();
		messageFactory = sipFactory.createMessageFactory();
		try {
			SipURI uri = this.addressFactory.createSipURI(null, config.getServer());
			uri.setPort(Integer.parseInt(config.getServerPort()));
			uri.setLrParam();
			Address address = this.addressFactory.createAddress(uri);
			this.routeToProxy = this.headerFactory.createRouteHeader(address);
		} catch (ParseException e) {
			log4j.error("Unexpected parse exception", e);
		}
		// Create SipStack object
		Properties properties = new Properties();

		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG","./debug/debug_im_log.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG","./debug/server_im_log.txt");

		properties.setProperty("javax.sip.STACK_NAME", "p2psip-chat");

		sipStack = sipFactory.createSipStack(properties);

		listeningPoint = sipStack.createListeningPoint(config.getMyIp(),Integer.parseInt(config.getMyPort()), "udp");
		log4j.debug("one listening point created: port:"
				+ listeningPoint.getPort() + ", " + " transport:" + listeningPoint.getTransport());
		sipProvider = sipStack.createSipProvider(listeningPoint);
		sipProvider.addSipListener(this);

		log4j.debug("log4j, Instant Messaging user agent ready to work");
		this.started = true;
	}
	
	public void sendRegister(){
		try{
			String fromName = config.getUsername();
			String fromSipAddress = config.getDomain();
			String fromDisplayName = config.getUsername();
	
			String toUser = config.getUsername();
			String toSipAddress = config.getDomain();
			String toDisplayName = config.getUsername();
	
			// From Header
			SipURI fromAddress = addressFactory.createSipURI(fromName, fromSipAddress);
			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "12345");
	
			// To Header
			SipURI toAddress = addressFactory.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);
	
			// Request URI
			SipURI requestURI = addressFactory.createSipURI(config.getUsername(), config.getServer()+":"+config.getServerPort());
			log4j.debug("Sending Register to "+config.getServer()+":"+config.getServerPort());
			
			// ViaHeaders
			ArrayList viaHeaders = new ArrayList();
	//		int port = sipProvider.getListeningPoint().getPort();
			ViaHeader viaHeader = headerFactory.createViaHeader(config.getMyIp(),
					listeningPoint.getPort(),"udp","asdf");
	
			// add via headers
			viaHeaders.add(viaHeader);
	
			// Create a new CallId header
			CallIdHeader callIdHeader = sipProvider.getNewCallId();
	
			// Create a new Cseq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1, Request.REGISTER);
	
			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
	
			// Create the request.
			Request request =
				messageFactory.createRequest(
					requestURI,
					Request.REGISTER,
					callIdHeader,
					cSeqHeader,
					fromHeader,
					toHeader,
					viaHeaders,
					maxForwards);
			
			String host = config.getMyIp();
			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(listeningPoint.getPort());
	
			SipURI contactURI = addressFactory.createSipURI(fromName, host);
			contactURI.setPort(listeningPoint.getPort());
			Address contactAddress = addressFactory.createAddress(contactURI);
	
			contactAddress.setDisplayName(fromName);
	
			ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
			request.addHeader(contactHeader);
	
			ClientTransaction registerTid = sipProvider.getNewClientTransaction(request);
	
			registerTid.sendRequest();
			log4j.debug("Register:"+request);
		} catch (PeerUnavailableException e1) {
			log4j.error("sending register ! ",e1);
		} catch(ParseException e) {
			log4j.error("parsing register!",e);
		} catch(InvalidArgumentException e) {
			log4j.error("Invalid argument !",e);
		} catch(TransactionUnavailableException e) {
			log4j.error("Transaction Unavailable ! ",e);
		} catch(SipException e) {
			log4j.error("SIP error ! ",e);
		}
	}

	public void sendMessage(Chatter who,String channel,String message){
		try{
			String fromName = config.getUsername();
			String fromSipAddress = config.getDomain();
			String fromDisplayName = config.getUsername();
	
			String toDisplayName= who.getName();
			String toSipAddress = who.getUri();
	
			// From Header
			SipURI fromAddress = addressFactory.createSipURI(fromName, fromSipAddress);
			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "12345");
	
			// To Header
			SipURI toAddress = (SipURI) addressFactory.createURI(toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);
	
			// Request URI
			SipURI requestURI = (SipURI) addressFactory.createURI(toSipAddress);
			log4j.debug("Sending Message to "+config.getServer()+":"+config.getServerPort());
			
			// ViaHeaders
			ArrayList viaHeaders = new ArrayList();
			ViaHeader viaHeader = headerFactory.createViaHeader(config.getMyIp(),
					listeningPoint.getPort(),"udp","asdf");
	
			// add via headers
			viaHeaders.add(viaHeader);
	
			// Create ContentTypeHeader
			ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
	
			// Create a new CallId header
			CallIdHeader callIdHeader = sipProvider.getNewCallId();
	
			// Create a new Cseq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1, Request.MESSAGE);
	
			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
	
			// Create the request.
			Request request =
				messageFactory.createRequest(
					requestURI,
					Request.MESSAGE,
					callIdHeader,
					cSeqHeader,
					fromHeader,
					toHeader,
					viaHeaders,
					maxForwards);
			
			ClientTransaction registerTid = sipProvider.getNewClientTransaction(request);
	
			registerTid.sendRequest();
			log4j.debug("Message:"+request);
		} catch (PeerUnavailableException e1) {
			log4j.error("sending message ! ",e1);
		} catch(ParseException e) {
			log4j.error("parsing message!",e);
		} catch(InvalidArgumentException e) {
			log4j.error("Invalid argument !",e);
		} catch(TransactionUnavailableException e) {
			log4j.error("Transaction Unavailable ! ",e);
		} catch(SipException e) {
			log4j.error("SIP error ! ",e);
		}
	}

	public void processDialogTerminated(DialogTerminatedEvent arg0) {
		log4j.debug(arg0);
	}

	public void processIOException(IOExceptionEvent arg0) {
		log4j.debug(arg0);
	}

	public void processRequest(RequestEvent reqEv) {
		Request req = reqEv.getRequest();
		Transaction trans = reqEv.getServerTransaction();
		FromHeader fromH;
		String from;
		String displayName;
		String message;
		Object content;
		if(req.getMethod().equalsIgnoreCase("MESSAGE")){
			fromH = (FromHeader) req.getHeader("From");
			displayName = fromH.getAddress().getDisplayName();
			from = fromH.getAddress().getURI().toString();
			content = req.getContent();
			if(content instanceof byte[]){
				message = new String((byte[])content);
			}else if (content instanceof String){
				message = (String) content;
			}else{
				log4j.warn("Unknown content :"+content.getClass().getCanonicalName() + " : "+content.toString());
				message="";
			}
			chat.messageReceived(displayName,from, message);
			try {
				Response resp = messageFactory.createResponse(200, req);
				this.sipProvider.sendResponse(resp);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (SipException e) {
				e.printStackTrace();
			}
		}
	}

	public void processResponse(ResponseEvent arg0) {
		log4j.debug(arg0);
	}

	public void processTimeout(TimeoutEvent arg0) {
		log4j.debug(arg0);
	}

	public void processTransactionTerminated(TransactionTerminatedEvent arg0) {
		log4j.debug(arg0);
	}
	
	public static void main(String args[]){
		
		SipEngine sipEngine;
		Configuration config=new Configuration();
		Properties props = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream("config.properties");
			props.load(fis);
			config.populateFromProperties(props);
		} catch (FileNotFoundException e) {
			log4j.warn("Unable to load properties");
		} catch (IOException e) {
			log4j.warn("Unable to load properties");
		}
		sipEngine = new SipEngine(config,null);
		try {
			sipEngine.start();
		} catch (Exception e) {
			log4j.error("Unable to start sip engine !!");
		}
		log4j.debug("Sip Engine started successfully");
		sipEngine.sendRegister();
	}

	public boolean isStarted() {
		return started;
	}

}
