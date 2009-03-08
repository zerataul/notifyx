package ua.com.shkil.ui.notification;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.com.shkil.notifyx.ui.FlatButton;
import ua.com.shkil.notifyx.ui.TinyGridLayout;
import ua.com.shkil.ui.notification.NotificationModel.NotificationModelListenerAdapter;
import ua.com.shkil.ui.notification.NotificationPanel.NotificationPaneListener;

import com.swtdesigner.SwingResourceManager;

@SuppressWarnings("serial")
public class NotificationFrame extends JWindow {

	final static Logger log = LoggerFactory.getLogger(NotificationFrame.class);

	private boolean alwaysHidden;
	private boolean autosize = true;
	private final NotificationPanel notifPane;

	private Point pseudoLocation = new Point(-2, -24);

	public static final int INIT_WIDTH = 350;
	public static final Color BORDER_COLOR = new Color(255, 200, 127);
	public static final Color BG_COLOR = new Color(255, 251, 222);

	private long hideAfter;
	private Timer hideTimer;

	protected static final Border frameBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, BORDER_COLOR);

	private class BottomBar extends JPanel {

		private JButton btnHide = new FlatButton();
		private JButton btnPrev = new JButton();
		private JButton btnNext = new JButton();
		private JLabel lblNavigator;

		public BottomBar() {
			final SpringLayout layout = new SpringLayout();
			setLayout(layout);
			setSize(INIT_WIDTH, 28);
			setBackground(BG_COLOR);
			ActionListener listener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Object s = e.getSource();
					if (s == btnHide) {
						NotificationFrame.this.notifPane.getModel().setActive(false);
					}
					else if (s == btnPrev) {
						notifPane.goBackward();
					}
					else if (s == btnNext) {
						notifPane.goForward();
					}
				}
			};
			btnHide.setToolTipText("Скрыть");
			btnHide.setPreferredSize(new Dimension(26, 24));
			btnHide.setMargin(new Insets(2, 7, 2, 7));
			btnHide.setIcon(SwingResourceManager.getIcon(BottomBar.class, "images/application_put.png"));
			btnHide.addActionListener(listener);
			add(btnHide);
			layout.putConstraint(SpringLayout.SOUTH, btnHide, -2, SpringLayout.SOUTH, this);
			layout.putConstraint(SpringLayout.NORTH, btnHide, 2, SpringLayout.NORTH, this);

			btnPrev.setIcon(SwingResourceManager.getIcon(BottomBar.class, "images/resultset_previous.png"));
			btnPrev.setMargin(new Insets(1, 7, 1, 7));
			btnPrev.addActionListener(listener);
			add(btnPrev);
			btnNext.setIcon(SwingResourceManager.getIcon(BottomBar.class, "images/resultset_next.png"));
			btnNext.setMargin(new Insets(1, 7, 1, 7));
			btnNext.addActionListener(listener);
			add(btnNext);
			lblNavigator = new JLabel("", SwingConstants.TRAILING);
			lblNavigator.setPreferredSize(new Dimension(100, 0));
			add(lblNavigator);

			layout.putConstraint(SpringLayout.NORTH, btnNext, 2, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.EAST, btnNext, -3, SpringLayout.EAST, this);
			layout.putConstraint(SpringLayout.SOUTH, btnNext, -3, SpringLayout.SOUTH, this);

			layout.putConstraint(SpringLayout.NORTH, btnPrev, 2, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.EAST, btnPrev, -4, SpringLayout.WEST, btnNext);
			layout.putConstraint(SpringLayout.SOUTH, btnPrev, -3, SpringLayout.SOUTH, this);

			layout.putConstraint(SpringLayout.WEST, btnHide, 2, SpringLayout.WEST, this);

			layout.putConstraint(SpringLayout.NORTH, lblNavigator, 2, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.EAST, lblNavigator, -8, SpringLayout.WEST, btnPrev);
			layout.putConstraint(SpringLayout.SOUTH, lblNavigator, -2, SpringLayout.SOUTH, this);

			MouseAdapter mouseAdapter = new MouseAdapter() {
				private Point startMousePosition, startFrameLocation;
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						startMousePosition = e.getLocationOnScreen();
						startFrameLocation = NotificationFrame.this.getLocationOnScreen();
					}
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					startMousePosition = null;
					startFrameLocation = null;
				}
				@Override
				public void mouseDragged(final MouseEvent e) {
					if (startMousePosition != null) {
						Point eventPosition = e.getLocationOnScreen();
						int dx = eventPosition.x - startMousePosition.x;
						int dy = eventPosition.y - startMousePosition.y;
						int x = startFrameLocation.x + dx;
						int y = startFrameLocation.y + dy;
						NotificationFrame.this.pseudoLocation = new Point(0, 0);
						NotificationFrame.super.setLocation((x > 0 ? x : 0), (y > 0 ? y : 0));
					}
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					NotificationFrame.this.onMouseEntered(e);
				}
				@Override
				public void mouseExited(MouseEvent e) {
					NotificationFrame.this.onMouseExited(e);
				}
			};
			addMouseListener(mouseAdapter);
			addMouseMotionListener(mouseAdapter);
		}

		protected void update() {
			int total = notifPane.getModel().count();
			int lo = notifPane.getSkipCount();
			int hi = lo + Math.min(notifPane.getMaxVisibleCount(), total);
			String text = "<html>" + (lo + 1) + " &ndash; " + hi + " из " + total;
			lblNavigator.setText(text);
		}

	}

	private BottomBar bottomBar = new BottomBar();

	public NotificationFrame(NotificationPanel notifPane) throws HeadlessException {
		//+hack: make JWindow focusable (http://objectmix.com/java/72384-workable-solution-non-focusable-jwindow.html)
		super(new Frame() {
			@Override
			public boolean isShowing() {
				return true;
			}
		});
		setFocusableWindowState(true);
		//-hack

		this.notifPane = notifPane;

		setAlwaysOnTop(true);
		setLocation(pseudoLocation);
		setWidth(INIT_WIDTH);

		hideTimer = new Timer(100, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateVisibilityState();
			}
		});

		initComponents();

		notifPane.getModel().addNotificationModelListener(new NotificationModelListenerAdapter() {
			@Override
			public void notificationModelChanged(NotificationModel model) {
				setAlwaysHidden(!model.isActive());
			}
		});

		notifPane.addNotificationPaneListener(new NotificationPaneListener() {
			@Override
			public void notificationPaneChanged(NotificationPanel pane) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						NotificationFrame.this.changed();
					}
				});
			}
		});
	}

	private void initComponents() {
		final JPanel contentPane = new JPanel();
		contentPane.setBorder(frameBorder);
		setContentPane(contentPane);
		setLayout(new TinyGridLayout());

		contentPane.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(final MouseWheelEvent e) {
				int rotation = e.getWheelRotation();
				if (rotation < 0) {
					notifPane.goBackward();
				}
				else if (rotation > 0) {
					notifPane.goForward();
				}
			}
		});

		contentPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				NotificationFrame.this.onMouseEntered(e);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				NotificationFrame.this.onMouseExited(e);
			}
		});

		contentPane.add(notifPane);
		contentPane.add(bottomBar);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				onMouseEntered(e);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				onMouseExited(e);
			}
		});

		getRootPane().registerKeyboardAction(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				notifPane.goForward();
			}				
		}, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		getRootPane().registerKeyboardAction(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				notifPane.goBackward();
			}				
		}, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	private void onMouseEntered(MouseEvent e) {
		freeze();
	}

	private void onMouseExited(MouseEvent e) {
		adjustHideAfter(1000);
	}

	protected void changed() {
		if (autosize) {
			Dimension d = notifPane.getPreferredSize();
			int h = d.height;
			notifPane.setSize(d);
			Insets ins = getContentPane().getInsets();
			setBounds(pseudoLocation.x, pseudoLocation.y, getWidth(), ins.top + h + bottomBar.getHeight() + ins.bottom);
			bottomBar.btnPrev.setEnabled(notifPane.canBackward());
			bottomBar.btnNext.setEnabled(notifPane.canForward());
		}
		bottomBar.update();
		updateVisibilityState();
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		final Rectangle desktopBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		if (x < 0) {
			x = desktopBounds.width + x - width;
		}
		if (y < 0) {
			y = desktopBounds.height + y - height;
		}
		super.setBounds(x, y, width, height);
	}

	protected void updateVisibilityState() {
		boolean visible;
		if (hideAfter != 0 && new Date().getTime() > hideAfter) {
			hideAfter = 0;
			visible = false;
		}
		else {
			visible = notifPane.getModel().count() > 0;
		}
		setVisible(visible);
	}

	@Override
	public void setVisible(boolean visible) {
		if (alwaysHidden) {
			visible = false;
		}
		if (visible != super.isVisible()) {
			if (visible) {
				setFocusableWindowState(false);
				super.setVisible(true);
				setFocusableWindowState(true);
				if (hideAfter == 0) {
					adjustHideAfter(10000);
				}
				hideTimer.start();
			}
			else {
				hideTimer.stop();
				super.setVisible(false);
			}
		}
	}

	public boolean isAlwaysHidden() {
		return alwaysHidden;
	}

	public void setAlwaysHidden(boolean hidden) {
		this.alwaysHidden = hidden;
		updateVisibilityState();
	}

	public boolean isAutosize() {
		return autosize;
	}

	public void setAutosize(boolean autosize) {
		this.autosize = autosize;
	}

	@Override
	public void setLocation(int x, int y) {
		if (x < 0 || y < 0) {
			pseudoLocation = new Point(x, y);
		}
		super.setLocation(x, y);
	}

	public void setWidth(int width) {
		setSize(width, getHeight());
	}

	public synchronized void bringOn() {
		adjustHideAfter(1000);
		updateVisibilityState();
	}

	protected void adjustHideAfter(int timeout) {
		hideAfter = Math.max(hideAfter, new Date().getTime() + timeout);
		hideTimer.start();
	}

	protected void freeze() {
		hideAfter = 0;
		hideTimer.stop();
	}

}