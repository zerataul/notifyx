package ua.com.shkil.notifyx.ui;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import ua.com.shkil.notifyx.Actions;

@SuppressWarnings("serial")
public class TrayMenu extends JPopupMenu {

	private final ActionListener menuItemsActionListener;

	private class Action extends AbstractAction {
		private final Actions action;
		public Action(String name, Actions action) {
			super(name);
			this.action = action;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			menuItemsActionListener.actionPerformed(
					new ActionEvent(e.getSource(), e.getID(), action.command(), e.getWhen(), e.getModifiers())
			);
		}

	}

	public TrayMenu(ActionListener actionListener) throws HeadlessException {
		menuItemsActionListener = actionListener;
		init();
	}

	protected void init() {
		JMenuItem mi;
		mi = new JCheckBoxMenuItem(new Action("Не показывать уведомления", Actions.TOGGLE_NOTIFICATION_ACTIVE));
		add(mi);
		addSeparator();
		mi = new JMenuItem(new Action("Выход", Actions.QUIT));
		add(mi);
	}

}