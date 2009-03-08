package ua.com.shkil.ui.notification;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.com.shkil.ui.notification.NotificationModel.NotificationModelListener;


@SuppressWarnings("serial")
public class NotificationPanel extends JPanel {

	final static Logger log = LoggerFactory.getLogger(NotificationPanel.class);

	private int maxVisibleCount = 2;
	private int skipCount;
	private ArrayList<Notification> list = new ArrayList<Notification>();
	private final NotificationModel model;

	public interface NotificationPaneListener extends EventListener {
		void notificationPaneChanged(NotificationPanel pane);
	}


	private static Comparator<Object> comparator = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			int p1, p2;
			if (o1 instanceof Prioritized) {
				p1 = ((Prioritized) o1).getPriority();
			}
			else {
				p1 = 0;
			}
			if (o2 instanceof Prioritized) {
				p2 = ((Prioritized) o2).getPriority();
			}
			else {
				p2 = 0;
			}			
			return p2 - p1;
		}
	};

	private final LayoutManager layout = new LayoutManager() {
		@Override
		public void addLayoutComponent(String name, Component comp) {
		}
		@Override
		public void layoutContainer(Container parent) {
			process(parent, true);
		}
		@Override
		public Dimension minimumLayoutSize(Container parent) {
			return null;
		}
		@Override
		public Dimension preferredLayoutSize(Container parent) {
			return process(parent, false);
		}
		protected Dimension process(Container parent, boolean apply) {
			final Insets insets = parent.getInsets();			
			final int width = parent.getWidth() - insets.left - insets.right;
			int y = insets.top;			
			int skipped = 0;
			int displayed = 0;
			int maxWidth = 0;
			Component[] all = parent.getComponents();
			Arrays.sort(all, comparator);
			for (Component comp : all) {				
				if (comp instanceof Notification) {
					if (skipped < skipCount) {
						++skipped;
						comp.setVisible(false);
					}
					else if (displayed >= maxVisibleCount) {
						comp.setVisible(false);
					}
					else {
						++displayed;
						comp.setVisible(true);
					}
				}
				if (comp.isVisible()) {
					final Dimension sz = comp.getPreferredSize();
					maxWidth = Math.max(maxWidth, sz.width);
					if (apply) {
						comp.setBounds(insets.left, y, width, sz.height);
					}
					y += sz.height;
				}
			}
			return new Dimension(insets.left + maxWidth + insets.right, y + insets.bottom);			
		}
		@Override
		public void removeLayoutComponent(Component comp) {
		}
	};

	public NotificationPanel(NotificationModel model) {
		this.model = model;
		setLayout(layout);
		model.addNotificationModelListener(new NotificationModelListener() {
			@Override
			public void notificationAdded(final Notification notif) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {						
						NotificationPanel.this.add(notif);	
					}
				});				
			}
			@Override
			public void notificationRemoved(final Notification notif) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {						
						NotificationPanel.this.remove(notif);	
					}
				});
			}
			@Override
			public void notificationModelChanged(NotificationModel model) {
			}
		});
		this.addContainerListener(new ContainerListener() {
			@Override
			public void componentAdded(ContainerEvent e) {
				NotificationPanel.this.changed();
			}
			@Override
			public void componentRemoved(ContainerEvent e) {
				NotificationPanel.this.changed();
			}
		});
	}

	protected void add(Notification notif) {
		Component pane = notif.getContentPane();
		list.add(notif);
		Collections.sort(list, comparator);
		super.add(pane);
	}

	protected void remove(Notification notif) {
		Component pane = notif.getContentPane();
		if (list.indexOf(notif) < skipCount) {
			--skipCount;
		}
		else if (model.count() - skipCount < maxVisibleCount) {
			skipCount = model.count() - maxVisibleCount;
		}
		if (skipCount < 0) {
			skipCount = 0;
		}
		list.remove(notif);
		super.remove(pane);
	}

	public NotificationModel getModel() {
		return model;
	}

	public void addNotificationPaneListener(NotificationPaneListener l) {
		listenerList.add(NotificationPaneListener.class, l);
	}

	public void removeNotificationPaneListener(NotificationPaneListener l) {
		listenerList.remove(NotificationPaneListener.class, l);
	}

	public void fireNotificationPaneChanged() {
		NotificationPaneListener[] listeners = listenerList.getListeners(NotificationPaneListener.class);
		for (NotificationPaneListener l : listeners) {
			l.notificationPaneChanged(this);
		}
	}

	protected void changed() {		
		Dimension psz = layout.preferredLayoutSize(this);
		setPreferredSize(psz);		
		fireNotificationPaneChanged();
	}

	public boolean canBackward() {
		return skipCount > 0;
	}

	public boolean canForward() {
		return skipCount < (model.count() - maxVisibleCount);
	}

	public boolean goBackward() {
		if (canBackward()) {
			--skipCount;
			changed();
			return true;
		}
		return false;
	}

	public boolean goForward() {
		if (canForward()) {
			++skipCount;
			changed();
			return true;
		}
		return false;
	}

	public int getMaxVisibleCount() {
		return maxVisibleCount;
	}

	public void setMaxVisibleCount(int maxVisibleCount) {
		if (this.maxVisibleCount != maxVisibleCount) {
			this.maxVisibleCount = maxVisibleCount;
			changed();
		}
	}

	public int getSkipCount() {
		return skipCount;
	}	
}