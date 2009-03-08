package ua.com.shkil.ui.notification;

import java.awt.Component;
import java.util.Date;

public interface Notification extends Prioritized {
	
	enum Type {
		Message		
	};
	
	Integer getId();
	Component getContentPane();
	boolean isAlive();
	int getPriority();
	void setShownTime(Date creationTime);
	void dispose();
}
