package ua.com.shkil.notifyx.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class TinyGridLayout implements LayoutManager {

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void layoutContainer(Container parent) {
		final int compCount = parent.getComponentCount();
		final Insets insets = parent.getInsets();
		final int width = parent.getWidth() - insets.left - insets.right;		
		int y = insets.top;
		for (int i = 0; i < compCount; i++) {
			final Component comp = parent.getComponent(i);
			if (comp.isVisible()) {
				final int compHeight = comp.getHeight();
				comp.setBounds(insets.left, y, width, compHeight);
				y += compHeight;
			}
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		final int compCount = parent.getComponentCount();
		final Insets insets = parent.getInsets();
		int width = 0;
		int height = insets.top;
		for (int i = 0; i < compCount; i++) {
			final Component comp = parent.getComponent(i);
			if (comp.isVisible()) {
				final Dimension sz = comp.getPreferredSize();
				width = Math.max(width, sz.width);
				height += sz.height;
			}
		}
		return new Dimension(insets.left + width + insets.right, height + insets.bottom);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

}
