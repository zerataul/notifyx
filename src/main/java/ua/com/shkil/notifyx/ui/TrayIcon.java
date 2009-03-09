package ua.com.shkil.notifyx.ui;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.RootPaneContainer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.com.shkil.notifyx.Actions;
import ua.com.shkil.notifyx.Application;

/**
 * JPopupMenu compatible TrayIcon based on Michael Bien's JPopupTrayIcon
 * (https://fishfarm.dev.java.net/source/browse/fishfarm/trunk/FishFarm/src/net/java/fishfarm/ui/JPopupTrayIcon.java?rev=198&view=markup)
 *
 * JPopupTrayIcon based on Alexander Potochkin's JXTrayIcon
 * (http://weblogs.java.net/blog/alexfromsun/archive/2008/02/jtrayicon_updat.html)
 * but uses a JWindow instead of a JDialog to workaround some bugs on linux.
 *
 * @author Dmitry Shkil
 */
public class TrayIcon extends java.awt.TrayIcon {

	private final static Logger log = LoggerFactory.getLogger(TrayIcon.class);

	private HashMap<String, Image> stateMap = new HashMap<String, Image>();

	private JPopupMenu menu;

	private Window window;
	private PopupMenuListener popupListener;

	private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
	private static final String DEFAULT_STATE = "default"; 

	private String currentState = DEFAULT_STATE;

	static Image getImage(String imagePattern) {
		Dimension traySize = SystemTray.getSystemTray().getTrayIconSize();
		int trayWidth = traySize.width;
		int trayHeight = traySize.height;
		String suffix;
		if (trayWidth <= 16 && trayHeight <= 16) {
			suffix = "_16x16x32.png";
		}
		else if (trayWidth <= 24 && trayHeight <= 24) {
			suffix = "_24x24x32.png";
		}		
		else if (trayWidth <= 32 && trayHeight <= 32) {
			suffix = "_32x32x32.png";
		}
		else {
			suffix = "_128x128x32.png";
		}
		String name = "images/" + imagePattern + suffix;
		URL iconUrl = Application.class.getResource(name);		
		return Toolkit.getDefaultToolkit().getImage(iconUrl);		
	}

	//	private long lastClickTime = 0;
	//	final int doubleClickInterval = Utils.getDoubleClickInterval();
	//	final Timer timer = new Timer(doubleClickInterval, new ActionListener() {
	//		@Override
	//		public void actionPerformed(ActionEvent e) {
	//			timer.stop();
	//			log.debug("single clicked");
	//		}
	//	});

	public TrayIcon(String image) {
		super(getImage(image));
		stateMap.put(DEFAULT_STATE, super.getImage());
		setImageAutoSize(true);		
		addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				//				int btn = e.getButton();
				//				if (btn == MouseEvent.BUTTON1) {
				//					long thisClickTime = new Date().getTime();
				//					if (thisClickTime - lastClickTime < doubleClickInterval) {
				//						timer.stop();
				//					}
				//					else {
				//						timer.start();
				//					}
				//					lastClickTime = thisClickTime;
				//				}
				//				else
				switch (e.getButton()) {
				case MouseEvent.BUTTON1: {
					fireActionPerformed(0, Actions.BRING_NOTIFICATION_FRAME_MEDIUM.command());
					break;
				}
				case MouseEvent.BUTTON2: {
					fireActionPerformed(0, Actions.TOGGLE_NOTIFICATION_ACTIVE.command());
					break;
				}
				}
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				fireActionPerformed(0, Actions.BRING_NOTIFICATION_FRAME_SHORT.command());
			}
		});
		init();
	}

	protected void fireActionPerformed(int id, String command) {
		for (ActionListener l : TrayIcon.this.getActionListeners()) {
			l.actionPerformed(new ActionEvent(TrayIcon.this, id, command));
		}
	}

	public void register() {
		try {
			SystemTray.getSystemTray().add(this);
		} catch (AWTException e) {
			log.error("Can't add to tray", e);
		}
	}

	public void addState(String state, String imagePattern) {
		stateMap.put(state, getImage(imagePattern));
	}

	public void setState(String state) {
		if (currentState.equals(state)) {
			return;
		}
		Image image = stateMap.get(state);
		if (image == null) {
			log.debug("State `{}` not found. Default state used instead.", state);
			image = stateMap.get(DEFAULT_STATE);
			this.currentState = DEFAULT_STATE;
		}
		else {
			this.currentState = state;
		}
		super.setImage(image);
	}

	private final void init() {
		popupListener = new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				if (window != null) {
					window.dispose();
					window = null;
				}
			}
			public void popupMenuCanceled(PopupMenuEvent e) {
				if (window != null) {
					window.dispose();
					window = null;
				}
			}
		};
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showJPopupMenu(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				showJPopupMenu(e);
			}
		});
	}

	private final void showJPopupMenu(MouseEvent e) {
		if (e.isPopupTrigger() && menu != null) {
			if (window == null) {
				if (IS_WINDOWS) {
					window = new JDialog((Frame) null);
					((JDialog) window).setUndecorated(true);
				} else {
					window = new JWindow((Frame) null);
				}
				window.setAlwaysOnTop(true);
				Dimension size = menu.getPreferredSize();
				Point centerPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
				if (e.getY() > centerPoint.getY()) {
					window.setLocation(e.getX(), e.getY() - size.height);
				}
				else {
					window.setLocation(e.getX(), e.getY());
				}
				window.setVisible(true);
				menu.show(((RootPaneContainer) window).getContentPane(), 0, 0);
				//popup works only for focused windows
				window.toFront();
			}
		}
	}

	public final void setJPopupMenu(JPopupMenu menu) {
		if (this.menu != null) {
			this.menu.removePopupMenuListener(popupListener);
		}
		this.menu = menu;
		menu.addPopupMenuListener(popupListener);
	}

}
