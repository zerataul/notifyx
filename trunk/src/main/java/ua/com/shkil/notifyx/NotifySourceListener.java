/**
 * 
 */
package ua.com.shkil.notifyx;

import java.util.List;

import ua.com.shkil.notifyx.packet.Mailbox.MailThreadInfo;

public interface NotifySourceListener {
	void mailboxUpdated(int totalMatched, List<MailThreadInfo> threads, String msg);
	void activated();
}