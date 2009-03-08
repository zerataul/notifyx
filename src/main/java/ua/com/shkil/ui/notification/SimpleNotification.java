package ua.com.shkil.ui.notification;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class SimpleNotification extends BaseNotification {

	private final JLabel messageLabel;

	public static final int TYPE_PLAIN = 0;
	public static final int TYPE_ERROR = 1;
	public static final int TYPE_WARNING = 2;
	public static final int TYPE_INFORMATION = 3;

	static final Color ERROR_COLOR = new Color(254, 173, 173);
	static final Color WARNING_COLOR = new Color(232, 242, 254);

	private int priority;
	private final static SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm:ss");

	private JLabel timeLabel;
	
	public SimpleNotification(int type, String msg) {
		this(type, msg, 15000);
	}
	
	public SimpleNotification(int type, String msg, int delay) {
		super(delay);
		messageLabel = new JLabel(msg, SwingConstants.LEFT);
		initComponents();
		if (type == TYPE_ERROR) {
			setBackground(ERROR_COLOR);
			priority = 1000;
		}
		else if (type == TYPE_WARNING) {
			setBackground(WARNING_COLOR);
			priority = 500;
		}
	}

	private void initComponents() {
		setBorder(new EmptyBorder(10, 5, 10, 5));
		BorderLayout layout = new BorderLayout();
		setLayout(layout);
		add(messageLabel);

		timeLabel = new JLabel();
		timeLabel.setMinimumSize(new Dimension(60, 0));
		timeLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		timeLabel.setBorder(new EmptyBorder(0, 3, 0, 10));
		timeLabel.setText(timeFormat.format(getShowTime()));
		add(timeLabel, BorderLayout.WEST);
		
		adjustPreferredSize();
	}
	
	public void setMessage(String msg) {
		messageLabel.setText(msg);
		adjustPreferredSize();
	}

	public String getMessage() {
		return messageLabel.getText();
	}

	@Override
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public void setShownTime(Date showTime) {
		timeLabel.setText(timeFormat.format(showTime));
		super.setShownTime(showTime);
	}

}