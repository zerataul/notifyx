package ua.com.shkil.notifyx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Session;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.provider.ProviderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.com.shkil.notifyx.packet.Mailbox;
import ua.com.shkil.notifyx.packet.MailboxProvider;
import ua.com.shkil.notifyx.packet.NewMail;
import ua.com.shkil.notifyx.packet.QueryNotify;
import ua.com.shkil.notifyx.packet.Mailbox.MailThreadInfo;

public class GmailNotifySource {

	private static final Logger log = LoggerFactory.getLogger(GmailNotifySource.class);

	private XMPPConnection connection;
	private ArrayList<NotifySourceListener> listeners = new ArrayList<NotifySourceListener>();

	static {
		XMPPConnection.DEBUG_ENABLED = "true".equalsIgnoreCase(System.getProperty("smack.debug"));
		ProviderManager providerManager = ProviderManager.getInstance();
		providerManager.addIQProvider("mailbox", "google:mail:notify", new MailboxProvider());
		providerManager.addIQProvider("new-mail", "google:mail:notify", NewMail.class);
	}

	private final ConnectionListener connectionListener = new ConnectionListener() {
		@Override
		public void connectionClosed() {
		}
		@Override
		public void connectionClosedOnError(Exception ex) {
			if (ex instanceof XMPPException) {
				StreamError streamError = ((XMPPException) ex).getStreamError();
				if ("not-authorized".equalsIgnoreCase(streamError.getCode())) {
					log.warn("not-authorized is catched");
					connection.disconnect();
					try {
						connection.connect();
					} catch (XMPPException e) {
						log.warn("Cannot connect", e);
					}
				}
			}
		}
		@Override
		public void reconnectingIn(int seconds) {
		}
		@Override
		public void reconnectionFailed(Exception ex) {
		}
		@Override
		public void reconnectionSuccessful() {
			fireActivated();
		}
	};

	public void connect() {
		if (connection != null && connection.isConnected()) {
			return;
		}
		ConnectionConfiguration config = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
		config.setSendPresence(false);
		config.setRosterLoadedAtLogin(false);
		connection = new XMPPConnection(config);
		SASLAuthentication.supportSASLMechanism("PLAIN", 0); //see http://www.igniterealtime.org/community/message/185164#185164
		try {
			connection.connect();
			connection.addConnectionListener(connectionListener);

			final HashSet<String> sessionPacketIds = new HashSet<String>();

			connection.addPacketWriterListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					sessionPacketIds.add(packet.getPacketID());
				}
			},  new PacketTypeFilter(Session.class));

			connection.addPacketListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					String packetId = packet.getPacketID();
					if (sessionPacketIds.contains(packetId)) {
						IQ iq = (IQ) packet;
						if (Type.RESULT.equals(iq.getType())) {
							connection.sendPacket(new QueryNotify());
							fireActivated();
						}
						sessionPacketIds.remove(packetId);
					}
				}
			}, new OrFilter(new IQTypeFilter(Type.RESULT), new IQTypeFilter(Type.ERROR)));

			connection.addPacketListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					log.debug(packet.toXML());
					connection.sendPacket(new QueryNotify());
				}				
			}, new PacketTypeFilter(NewMail.class));

			connection.addPacketListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					fireMailboxUpdated((Mailbox) packet);
				}
			}, new PacketTypeFilter(Mailbox.class));

		} catch (XMPPException e) {
			log.warn("Cannot connect", e);
		}
	}

	public void login(String username, char[] password) throws XMPPException {
		try {
			if (username.indexOf("@") < 0) {
				username = username.concat("@gmail.com");
			}
			connection.login(username, new String(password), "NotiFyX");
			fireActivated();
		}
		catch (XMPPException ex) {
			log.warn("Cannot login", ex);
			throw ex;
		}
	}

	public void close() {
		if (connection != null) {
			connection.disconnect();
			connection = null;
		}
	}

	public void addListener(NotifySourceListener l) {
		listeners.add(l);
	}

	public void removeListener(NotifySourceListener l) {
		listeners.remove(l);
	}

	protected void fireActivated() {
		for(NotifySourceListener l : listeners) {
			l.activated();
		}
	}

	protected void fireMailboxUpdated(Mailbox mailbox) {
		int totalMatched = mailbox.getTotalMatched();
		String url = mailbox.getUrl();
		List<MailThreadInfo> threads = mailbox.getMailThreadInfoList();
		for(NotifySourceListener l : listeners) {
			l.mailboxUpdated(totalMatched, threads, url);
		}
	}

}
