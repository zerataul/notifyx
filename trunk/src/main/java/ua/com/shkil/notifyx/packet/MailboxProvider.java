package ua.com.shkil.notifyx.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import ua.com.shkil.notifyx.packet.Mailbox.MailThreadInfo;
import ua.com.shkil.notifyx.packet.Mailbox.Sender;

public class MailboxProvider implements IQProvider {

	@Override
	public IQ parseIQ(final XmlPullParser parser) throws Exception {
		Mailbox iq = new Mailbox();
		iq.setResultTime(Long.parseLong(parser.getAttributeValue("", "result-time")));
		iq.setTotalMatched(Integer.parseInt(parser.getAttributeValue("", "total-matched")));
		iq.setTotalEstimate("1".equals(parser.getAttributeValue("", "total-estimate")));
		iq.setUrl(parser.getAttributeValue("", "url"));
		for (;;) {
			int eventType = parser.next();
			String name = parser.getName();
			if (eventType == XmlPullParser.START_TAG) {
				if ("mail-thread-info".equals(name)) {
					iq.addMailThreadInfo(parseMailThreadInfo(parser));
				}
				else {
					iq.addExtension(PacketParserUtils.parsePacketExtension(parser.getName(), parser.getNamespace(), parser));
				}
			}
			else if ((eventType == XmlPullParser.END_TAG && "mailbox".equals(name)) || eventType == XmlPullParser.END_DOCUMENT) {
				break;
			}
		}
		return iq;
	}

	public MailThreadInfo parseMailThreadInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
		MailThreadInfo threadInfo = new MailThreadInfo();
		int messagesCount = Integer.parseInt(parser.getAttributeValue("", "messages"));
		threadInfo.setMessages(messagesCount);
		for (;;) {
			int eventType = parser.next();
			String name = parser.getName();
			if (eventType == XmlPullParser.START_TAG) {
				if ("subject".equals(name)) {
					threadInfo.setSubject(parser.nextText());
				}
				else if ("snippet".equals(name)) {
					threadInfo.setSnippet(parser.nextText());
				}
				else if ("labels".equals(name)) {
					threadInfo.setLabels(parser.nextText());
				}
				else if ("senders".equals(name)) {
					threadInfo.setSenders(parseSenders(parser));
				}
				else {
					System.out.println("MailboxIQProvider.parseIQ() @ START_TAG ! unknown: " + name + " " + parser.getNamespace());
				}
			}
			else if (eventType == XmlPullParser.END_TAG) {
				if ("mail-thread-info".equals(name)) {
					break;
				}
				else {
					System.out.println("MailboxIQProvider.parseIQ() @ END_TAG ! unknown: " + name + " " + parser.getNamespace());
				}
			}
			else if (eventType == XmlPullParser.END_DOCUMENT) {
				break;
			}
		}
		return threadInfo;
	}

	public List<Sender> parseSenders(XmlPullParser parser) throws XmlPullParserException, IOException {
		ArrayList<Sender> senders = new ArrayList<Sender>();
		for (;;) {
			int eventType = parser.next();
			String name = parser.getName();
			if (eventType == XmlPullParser.START_TAG) {
				if ("sender".equals(name)) {
					Sender sender = new Sender(parser.getAttributeValue("", "name"), parser.getAttributeValue("", "address"));
					sender.setUnread("1".equals(parser.getAttributeValue("", "unread")));
					senders.add(sender);	
				}
				else {
					System.err.println("MailboxIQProvider.parseSenders() @ START_TAG ! unknown: " + name + " " + parser.getNamespace());
				}
			}
			else if ((eventType == XmlPullParser.END_TAG && "senders".equals(name)) || eventType == XmlPullParser.END_DOCUMENT) {
				break;
			}
		}
		return senders;
	}

}