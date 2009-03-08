package ua.com.shkil.ui.notification;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Timer;
import javax.swing.event.EventListenerList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationModel {

	static final Logger log = LoggerFactory.getLogger(NotificationModel.class);

	public interface NotificationModelListener extends EventListener {
		void notificationAdded(Notification notif);
		void notificationRemoved(Notification notif);
		void notificationModelChanged(NotificationModel model);
	}

	public static class NotificationModelListenerAdapter implements NotificationModelListener {
		public void notificationAdded(Notification notif) {
		}
		public void notificationModelChanged(NotificationModel model) {
		}
		public void notificationRemoved(Notification notif) {
		}
	}

	private final LinkedHashMap<Integer, Notification> notifs = new LinkedHashMap<Integer, Notification>();
	private final EventListenerList listenerList = new EventListenerList();

	protected final Timer timer = new javax.swing.Timer(100, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			NotificationModel.this.cleanup();
		}
	});

	private boolean active = true;

	public NotificationModel() {

	}

	protected synchronized void cleanup() {
		for (Iterator<Notification> iter = notifs.values().iterator(); iter.hasNext();) {
			Notification notif = iter.next();
			if (!notif.isAlive()) {
				iter.remove();
				remove(notif);
			}
		}
	}

	public synchronized void add(Notification notif) {
		notif.setShownTime(new Date());
		boolean added = notifs.put(notif.getId(), notif) == null;
		changed();
		if (added) {
			fireNotificationAdded(notif);
		}
	}

	public void remove(Notification notif) {
		Integer id = notif.getId();
		notifs.remove(id);
		changed();
		fireNotificationRemoved(notif);
	}

	public void removeAll() {
		Notification[] notifsArray = notifs.values().toArray(new Notification[notifs.size()]);
		notifs.clear();
		changed();
		for (Notification notif : notifsArray) {
			fireNotificationRemoved(notif);
		}
	}

	protected void changed() {		
		if (active && notifs.size() > 0) {
			timer.start();
		}
		else {
			timer.stop();
		}
	}

	int count() {
		return notifs.size();
	}

	public void addNotificationModelListener(NotificationModelListener l) {
		synchronized (listenerList) {
			listenerList.add(NotificationModelListener.class, l);
		}
	}

	public void removeNotificationModelListener(NotificationModelListener l) {
		synchronized (listenerList) {
			listenerList.remove(NotificationModelListener.class, l);
		}
	}

	protected void fireNotificationAdded(Notification notif) {
		NotificationModelListener[] listeners;
		synchronized (listenerList) {
			listeners = listenerList.getListeners(NotificationModelListener.class);
		}
		for (NotificationModelListener l : listeners) {
			l.notificationAdded(notif);
		}
	}

	protected void fireNotificationRemoved(Notification notif) {
		NotificationModelListener[] listeners;
		synchronized (listenerList) {
			listeners = listenerList.getListeners(NotificationModelListener.class);
		}
		for (NotificationModelListener l : listeners) {
			l.notificationRemoved(notif);
		}
	}

	protected void fireNotificationModelChanged() {
		NotificationModelListener[] listeners;
		synchronized (listenerList) {
			listeners = listenerList.getListeners(NotificationModelListener.class);
		}
		for (NotificationModelListener l : listeners) {
			l.notificationModelChanged(this);
		}
	}

	public void setActive(boolean active) {
		if (this.active != active) {
			this.active = active;
			cleanup();
			changed();
			fireNotificationModelChanged();
		}
	}

	public boolean isActive() {
		return active;
	}

}
