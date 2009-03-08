package ua.com.shkil.notifyx.packet;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;

import ua.com.shkil.notifyx.ISender;

public class Mailbox extends IQ {

	private long resultTime;
	private int totalMatched;
	private boolean totalEstimate;
	private String url;
	private List<MailThreadInfo> mailThreadInfoList = new ArrayList<MailThreadInfo>();

	public static class Sender implements ISender {
		private final String name;
		private final String address;
		private boolean unread;
		public Sender(String name, String address) {
			this.name = name;
			this.address = address;
		}
		public String getName() {
			return name;
		}
		public String getAddress() {
			return address;
		}
		public boolean isUnread() {
			return unread;
		}
		public void setUnread(boolean unread) {
			this.unread = unread;
		}
		@Override
		public String toString() {
			String unreadString = unread ? " unread" : "";
			return "{\""+ name +"\" <" + address + ">" + unreadString + "}";
		}
	}

	public static class MailThreadInfo {
		private String subject;
		private String snippet;
		private String labels;
		private List<Sender> senders;
		private int messages;
		public String getSubject() {
			return subject;
		}
		public void setSubject(String subject) {
			this.subject = subject;
		}
		public String getSnippet() {
			return snippet;
		}
		public void setSnippet(String snippet) {
			this.snippet = snippet;
		}
		public String getLabels() {
			return labels;
		}
		public void setLabels(String labels) {
			this.labels = labels;
		}
		public List<Sender> getSenders() {
			return senders;
		}
		public void setSenders(List<Sender> senders) {
			this.senders = senders;
		}	
		public int getMessages() {
			return messages;
		}
		public void setMessages(int messages) {
			this.messages = messages;
		}
		@Override
		public String toString() {
			return "{\"" + subject + "\" | \"" + snippet + "\"}";
		}
	}

	public void addMailThreadInfo(MailThreadInfo info) {
		mailThreadInfoList.add(info);
	}
	
	public List<MailThreadInfo> getMailThreadInfoList() {
		return mailThreadInfoList;
	}	

	public long getResultTime() {
		return resultTime;
	}

	public void setResultTime(long l) {
		this.resultTime = l;
	}

	public int getTotalMatched() {
		return totalMatched;
	}

	public void setTotalMatched(int totalMatched) {
		this.totalMatched = totalMatched;
	}

	public boolean isTotalEstimate() {
		return totalEstimate;
	}

	public void setTotalEstimate(boolean totalEstimate) {
		this.totalEstimate = totalEstimate;
	}

	@Override
	public String getChildElementXML() {
		String totalString = totalEstimate ? " total-estimate='1' " : "";
		return "<mailbox result-time='" + resultTime + "' total-matched='" + totalMatched + "'" + totalString + "/>";
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

}
