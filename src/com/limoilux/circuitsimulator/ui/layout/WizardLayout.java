
package com.limoilux.circuitsimulator.ui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class WizardLayout implements LayoutManager
{
	public WizardLayout()
	{
	}

	@Override
	public void addLayoutComponent(String name, Component c)
	{
	}

	@Override
	public void removeLayoutComponent(Component c)
	{
	}

	@Override
	public Dimension preferredLayoutSize(Container target)
	{
		return new Dimension(500, 500);
	}

	@Override
	public Dimension minimumLayoutSize(Container target)
	{
		return new Dimension(100, 100);
	}

	@Override
	public void layoutContainer(Container target)
	{
		Insets insets = null;
		Component cl = null;
		Dimension dl = null;
		Component m = null;

		if (target.getComponentCount() != 0)
		{
			insets = target.getInsets();
			cl = target.getComponent(target.getComponentCount() - 1);
			dl = cl.getPreferredSize();
			target.getComponent(0).setLocation(insets.left, insets.top);

			int cw = target.getSize().width - insets.left - insets.right;
			int ch = target.getSize().height - insets.top - insets.bottom - dl.height;
			target.getComponent(0).setSize(cw, ch);
			int h = ch + insets.top;
			int x = 0;

			for (int i = 1; i < target.getComponentCount(); i++)
			{
				m = target.getComponent(i);
				if (m.isVisible())
				{
					Dimension d = m.getPreferredSize();
					m.setLocation(insets.left + x, h);
					m.setSize(d);
					x += d.width;
				}
			}
		}
	}
}
