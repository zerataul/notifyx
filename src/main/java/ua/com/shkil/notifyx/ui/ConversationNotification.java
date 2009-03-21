package ua.com.shkil.notifyx.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import com.swtdesigner.SwingResourceManager;

import ua.com.shkil.notifyx.Application;
import ua.com.shkil.notifyx.ISender;
import ua.com.shkil.jxui.notification.BaseNotification;

@SuppressWarnings("serial")
public class ConversationNotification extends BaseNotification {

	private final static SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm");

	private JLabel label;

	private final String senders;
	private final String subject;
	private final String snippet;
	private final int messages;


	public ConversationNotification(String subject, String snippet, Collection<? extends ISender> senders, int messages) {
		super(NO_TIMEOUT);
		this.subject = subject;
		this.snippet = snippet.replaceAll("\\s…$", "<nbsp/>…");
		this.messages = messages;
		label = new JLabel() {
			@Override
			public Dimension getPreferredSize() {
				View view = (View) getClientProperty(BasicHTML.propertyKey);
				view.setSize(super.getSize().width, 0);
				float w = view.getPreferredSpan(View.X_AXIS);  
				float h = view.getPreferredSpan(View.Y_AXIS);  
				return new Dimension((int) Math.ceil(w), (int) Math.ceil(h));  
			}
		};
		StringBuilder sb = new StringBuilder();
		Iterator<? extends ISender> it = senders.iterator();
		while (it.hasNext()) {
			ISender sender = it.next();
			String senderName = sender.getName();
			if (senderName == null) {
				senderName = sender.getAddress();
			}
			if (sender.isUnread()) {
				sb.append("<b>" + senderName + "</b>");
			}
			else {
				sb.append(senderName);
			}
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		this.senders = sb.toString();
		initComponents();
		updateView();
	}

	private void initComponents() {
		setBorder(new EmptyBorder(3, 3, 3, 3));
		BorderLayout layout = new BorderLayout();
		setLayout(layout);
		JLabel envelopeLabel = new JLabel(SwingResourceManager.getIcon(Application.class, "images/gmail-bleu_32x32x32.png"));
		envelopeLabel.setBorder(new EmptyBorder(3, 2, 3, 5));
		add(envelopeLabel, BorderLayout.WEST);
		add(label, BorderLayout.CENTER);
	}

	protected void updateView() {
		StringBuilder sb = new StringBuilder("<html>");
		sb.append(timeFormat.format(new Date()) + " - ");
		sb.append(senders);
		if (messages > 1) {
			sb.append(" (" + messages + ")");
		}
		sb.append("<p><b>");
		sb.append(subject);
		sb.append("</b></p><p>");
		sb.append(snippet);
		sb.append("</p></html>");
		label.setText(sb.toString());
		label.setSize(300, 1000); // FIXME use proper width from parent
		adjustPreferredSize();
	}

	@Override
	public void setShownTime(Date showTime) {
		//timeLabel.setText(timeFormat.format(showTime));
		super.setShownTime(showTime);
	}

}