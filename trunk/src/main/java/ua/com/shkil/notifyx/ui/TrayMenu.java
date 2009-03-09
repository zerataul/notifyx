package ua.com.shkil.notifyx.ui;

import java.awt.HeadlessException;

import javax.swing.ActionMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import ua.com.shkil.notifyx.Actions;

@SuppressWarnings("serial")
public class TrayMenu extends JPopupMenu {

	public TrayMenu(ActionMap actionMap) throws HeadlessException {
		init(actionMap);
	}

	protected void init(ActionMap actionMap) {
		add(new JCheckBoxMenuItem(actionMap.get(Actions.TOGGLE_NOTIFICATION_ACTIVE.command())));
		addSeparator();
		add(new JMenuItem(actionMap.get(Actions.QUIT.command())));
	}

}