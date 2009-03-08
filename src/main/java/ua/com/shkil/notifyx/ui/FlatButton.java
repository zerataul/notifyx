package ua.com.shkil.notifyx.ui;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class FlatButton extends JButton {

	static Logger log = LoggerFactory.getLogger(FlatButton.class);

	private Border normalBorder = new EmptyBorder(1, 1, 1, 1);
	private Border currentBorder = normalBorder;

	private Border hoverBorder;

	public FlatButton() {
		this(new LineBorder(new Color(242, 226, 99)));
	}
	
	public FlatButton(Border hoverBorder) {

		this.hoverBorder = hoverBorder;
		
		setContentAreaFilled(false);
		setBorder(currentBorder);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				hover();
			}
			@Override
			public void mouseExited(MouseEvent e) {
				leave();
			}
		});
		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				//hover();
			}
			@Override
			public void focusLost(FocusEvent e) {
				//leave();
			}
		});
	}
	
	protected void hover() {
		setBorder(currentBorder = hoverBorder);
	}
	
	protected void leave() {
		//if (!isFocusOwner()) {
			setBorder(currentBorder = normalBorder);
		//}
	}

	@Override
	public void setBorder(Border border) {
		Insets margin = getMargin();
		if (margin == null) {
			margin = new Insets(2, 14, 2, 14);
		}
		Border b = new CompoundBorder(border, new EmptyBorder(margin));
		super.setBorder(b);
	}

	@Override
	public void setMargin(Insets m) {
		super.setMargin(m);
		setBorder(currentBorder);
	}
}
