package com.swtdesigner;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Window;

/**
 * Cyclic focus traversal policy based on array of components.
 * 
 * This class may be freely distributed as part of any application or plugin.
 * <p>
 * Copyright (c) 2003 - 2005, Instantiations, Inc. <br>
 * All Rights Reserved
 * 
 * @author scheglov_ke
 */
public class FocusTraversalOnArray extends FocusTraversalPolicy {
	private final Component m_Components[];
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Create the focus traversal policy
	 * 
	 * @param components
	 */
	public FocusTraversalOnArray(Component components[]) {
		m_Components = components;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Utilities
	//
	////////////////////////////////////////////////////////////////////////////
	private int indexCycle(int index, int delta) {
		int size = m_Components.length;
		int next = (index + delta) % size;
		return next;
	}
	private Component cycle(Component currentComponent, int delta) {
		int index = -1;
		loop : for (int i = 0; i < m_Components.length; i++) {
			Component component = m_Components[i];
			for (Component c = currentComponent; c != null; c = c.getParent()) {
				if (component == c) {
					index = i;
					break loop;
				}
			}
		}
		// try to find visible enabled component in "delta" direction
		find : for (int iters = 0; iters < m_Components.length; iters++) {
			index = indexCycle(index, delta);
			Component component = m_Components[index];
			if (component.isEnabled() && component.isVisible()) {
				for (Component p = component.getParent(); p != null; p = p.getParent()) {
					if (p instanceof Window) {
						return component;
					}
					if (!p.isVisible()) {
						continue find; 
					}
				}
				return component;
			}
		}
		// not found
		return currentComponent;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// FocusTraversalPolicy
	//
	////////////////////////////////////////////////////////////////////////////
	public Component getComponentAfter(Container container, Component component) {
		return cycle(component, 1);
	}
	public Component getComponentBefore(Container container, Component component) {
		return cycle(component, -1);
	}
	public Component getFirstComponent(Container container) {
		return m_Components[0];
	}
	public Component getLastComponent(Container container) {
		return m_Components[m_Components.length - 1];
	}
	public Component getDefaultComponent(Container container) {
		return getFirstComponent(container);
	}
}