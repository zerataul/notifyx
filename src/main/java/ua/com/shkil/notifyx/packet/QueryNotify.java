package ua.com.shkil.notifyx.packet;

import org.jivesoftware.smack.packet.IQ;

public class QueryNotify extends IQ {

	@Override
	public String getChildElementXML() {
		return "<query xmlns='google:mail:notify'/>";
	}

}
