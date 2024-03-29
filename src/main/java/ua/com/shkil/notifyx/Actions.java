package ua.com.shkil.notifyx;

import ua.com.shkil.util.Strings;

public enum Actions {

	QUIT("quit"),
	TOGGLE_NOTIFICATION_ACTIVE,
	BRING_NOTIFICATION_FRAME_SHORT,
	BRING_NOTIFICATION_FRAME_MEDIUM;

	private final String command;

	private Actions() {
		command = Strings.camelize(name());		
	}

	private Actions(String command) {
		this.command = command; 
	}

	public String command() {
		return command;
	}

}