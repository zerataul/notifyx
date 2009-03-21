package ua.com.shkil.notifyx;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.UIManager;

import net.jcip.annotations.GuardedBy;

import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.JXLoginPane.JXLoginFrame;
import org.jdesktop.swingx.JXLoginPane.SaveMode;
import org.jdesktop.swingx.JXLoginPane.Status;
import org.jdesktop.swingx.auth.DefaultUserNameStore;
import org.jdesktop.swingx.auth.LoginService;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.com.shkil.notifyx.packet.Mailbox.MailThreadInfo;
import ua.com.shkil.notifyx.ui.ConversationNotification;
import ua.com.shkil.notifyx.ui.TrayIcon;
import ua.com.shkil.notifyx.ui.TrayMenu;
import ua.com.shkil.jxui.notification.NotificationFrame;
import ua.com.shkil.jxui.notification.NotificationModel;
import ua.com.shkil.jxui.notification.NotificationPanel;

import com.swtdesigner.SwingResourceManager;

public class Application extends SingleFrameApplication {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	private NotificationModel notificationModel = new NotificationModel();
	private NotificationFrame notificationFrame = new NotificationFrame(new NotificationPanel(notificationModel));

	private GmailNotifySource gnotifyService = new GmailNotifySource();
	private TrayIcon trayIcon;

	public static void main(String[] args) {
		launch(Application.class, args);
	}

	public static Application getApplication() {
		return org.jdesktop.application.Application.getInstance(Application.class);
	}

	public final ActionListener actionPerformer = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			javax.swing.Action action = getContext().getActionMap().get(command);
			if (action == null) {
				log.warn("Action {} not found", command);
			}
			else {
				Object selectedValue = action.getValue(javax.swing.Action.SELECTED_KEY);
				if (selectedValue instanceof Boolean) { //FIXME
					action.putValue(javax.swing.Action.SELECTED_KEY, !((Boolean) selectedValue));
				}
				action.actionPerformed(e);
			}
		}
	};

	@Override
	protected void initialize(String[] args) {
	}

	@Override
	protected void startup() {
		trayIcon = new TrayIcon("gmail-kuro");
		trayIcon.addState("empty", "gmail-orangish");
		trayIcon.addState("hasmail", "gmail-bleu");
		trayIcon.addState("important", "gmail-classic");
		trayIcon.setToolTip("NotiFyX");
		trayIcon.setJPopupMenu(new TrayMenu(getContext().getActionMap()));
		trayIcon.addActionListener(actionPerformer);
		trayIcon.register();
		gnotifyService.addListener(new NotifySourceListener() {
			@GuardedBy("self")
			private Map<String,WeakReference<ConversationNotification>> notifsMap = new HashMap<String,WeakReference<ConversationNotification>>(); 
			@Override
			public void mailboxUpdated(int totalMatched, List<MailThreadInfo> threads, String url) {
				if (totalMatched > 0) {
					HashSet<String> newThreadIdSet = new HashSet<String>();
					for (MailThreadInfo thread : threads) {
						newThreadIdSet.add(thread.getThreadId());
					}
					synchronized (notifsMap) {
						Iterator<Entry<String, WeakReference<ConversationNotification>>> it = notifsMap.entrySet().iterator();
						while (it.hasNext()) {
							Entry<String, WeakReference<ConversationNotification>> entry = it.next();
							if (!newThreadIdSet.remove(entry.getKey())) {
								ConversationNotification notif = entry.getValue().get();
								it.remove();
								if (notif != null) {
									notificationModel.remove(notif);
								}
							}
						}
						for (MailThreadInfo thread : threads) {
							if (newThreadIdSet.contains(thread.getThreadId())) {
								ConversationNotification notif = new ConversationNotification(
										thread.getSubject(), thread.getSnippet(),
										thread.getSenders(), thread.getMessages()
								);
								notifsMap.put(thread.getThreadId(), new WeakReference<ConversationNotification>(notif));
								notificationModel.add(notif);
							}
						}
					}
					trayIcon.setState("hasmail");
					java.awt.Toolkit.getDefaultToolkit().beep();
				}
				else {
					notificationModel.removeAll();
					synchronized (notifsMap) {
						notifsMap.clear();
					}
					trayIcon.setState("empty");
				}
			}
			@Override
			public void activated() {
				trayIcon.setState("empty");
			}
		});
	}

	@Override
	protected void ready() {
		notificationFrame.setWidth(350);
		notificationFrame.setAutosize(true);

		UIManager.put("JXLoginPane.titleString", "NotiFyX");

		final JXLoginPane loginPanel = new JXLoginPane();
		loginPanel.setBannerText("Login");
		loginPanel.setMessage("Please log in to your Google Account");
		loginPanel.setLoginService(
				new LoginService() {
					@Override
					public boolean authenticate(String username, char[] password, String server) throws Exception {
						if ("".equals(username) || password.length == 0) {
							loginPanel.setErrorMessage("Empty login or password");
							return false;
						}
						try {
							gnotifyService.connect();
							gnotifyService.login(username, password);
							return true;
						}
						catch (XMPPException ex) {
							loginPanel.setErrorMessage("Couldn't login " + ex.getLocalizedMessage());
						}
						return false;
					}
				}
		);
		DefaultUserNameStore userNameStore = new DefaultUserNameStore();
		userNameStore.setPreferences(Preferences.userNodeForPackage(Application.class).node("google-login"));
		loginPanel.setUserNameStore(userNameStore);
		loginPanel.setSaveMode(SaveMode.USER_NAME);
		JXLoginFrame loginFrame = JXLoginPane.showLoginFrame(loginPanel);
		loginFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				if (loginPanel.getStatus() != Status.SUCCEEDED) {
					exit();
				}
			}
		});
		loginFrame.setIconImage(SwingResourceManager.getImage(Application.class, "images/gmail-bleu_16x16x32.png"));
		loginFrame.setVisible(true);
	}

	@Override
	protected void shutdown() {
		if (gnotifyService != null) {
			gnotifyService.close();
		}
	}

	public boolean isNotificationInactive() {
		return !notificationModel.isActive();
	}

	public void setNotificationInactive(boolean inactive) {
		notificationModel.setActive(!inactive);
	}

	@Action(selectedProperty = "notificationInactive")
	public void toggleNotificationActive() {
	}

	@Action
	public void bringNotificationFrameShort() {
		notificationFrame.bringShort();
	}

	@Action
	public void bringNotificationFrameMedium() {
		notificationFrame.bringMedium();
	}

}
