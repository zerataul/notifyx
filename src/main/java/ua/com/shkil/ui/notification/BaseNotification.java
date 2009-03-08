package ua.com.shkil.ui.notification;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class BaseNotification extends JPanel implements Notification {

	public static final Color BG_COLOR = new Color(255,239,112);
	public static final Border OUTSIDE_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 0, NotificationFrame.BORDER_COLOR);

	public static final int NO_TIMEOUT = -1;
	
	private static int lastId = 0;

	protected final Integer id;
	protected Date shownTime = new Date();
	protected long maxDelay;
	private Border insideBorder = new EmptyBorder(0, 0, 0, 0);

	public BaseNotification() {
		this(60000);
	}

	public BaseNotification(long maxDelay) {
		super();
		this.id = ++lastId;
		this.maxDelay = maxDelay;

		setBackground(BG_COLOR);
		setSize(400, 50);
		setPreferredSize(getSize());

		super.setBorder(OUTSIDE_BORDER);
	}

	@Override
	public Component getContentPane() {
		return this;
	}

	@Override
	public boolean isAlive() {
		return maxDelay < 0 || (new Date().getTime() - shownTime.getTime()) < maxDelay;
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setBorder(Border insideBorder) {
		this.insideBorder = insideBorder;		
		super.setBorder(new CompoundBorder(OUTSIDE_BORDER, insideBorder));
	}

	public Border getInsBorder() {
		return insideBorder;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	public Date getShowTime() {
		return shownTime;
	}

	@Override
	public void setShownTime(Date showTime) {
		this.shownTime = showTime;
	}

	@Override
	public void dispose() {
		maxDelay = 0;
	}
	
	protected void adjustPreferredSize() {
		setPreferredSize(getLayout().preferredLayoutSize(this));
	}
}
