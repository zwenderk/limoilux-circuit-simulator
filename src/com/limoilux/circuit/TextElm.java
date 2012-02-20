package com.limoilux.circuit;
import java.awt.Checkbox;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.StringTokenizer;
import java.util.Vector;

class TextElm extends CircuitElm
{
	String text;
	Vector lines;
	int size;
	final int FLAG_CENTER = 1;
	final int FLAG_BAR = 2;

	public TextElm(int xx, int yy)
	{
		super(xx, yy);
		this.text = "hello";
		this.lines = new Vector();
		this.lines.add(this.text);
		this.size = 24;
	}

	public TextElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st)
	{
		super(xa, ya, xb, yb, f);
		this.size = new Integer(st.nextToken()).intValue();
		this.text = st.nextToken();
		while (st.hasMoreTokens())
		{
			this.text += ' ' + st.nextToken();
		}
		this.split();
	}

	void split()
	{
		int i;
		this.lines = new Vector();
		StringBuffer sb = new StringBuffer(this.text);
		for (i = 0; i < sb.length(); i++)
		{
			char c = sb.charAt(i);
			if (c == '\\')
			{
				sb.deleteCharAt(i);
				c = sb.charAt(i);
				if (c == 'n')
				{
					this.lines.add(sb.substring(0, i));
					sb.delete(0, i + 1);
					i = -1;
					continue;
				}
			}
		}
		this.lines.add(sb.toString());
	}

	@Override
	String dump()
	{
		return super.dump() + " " + this.size + " " + this.text;
	}

	@Override
	int getDumpType()
	{
		return 'x';
	}

	@Override
	void drag(int xx, int yy)
	{
		this.x = xx;
		this.y = yy;
		this.x2 = xx + 16;
		this.y2 = yy;
	}

	@Override
	void draw(Graphics g)
	{
		g.setColor(this.needsHighlight() ? CircuitElm.selectColor : CircuitElm.lightGrayColor);
		Font f = new Font("SansSerif", 0, this.size);
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		int i;
		int maxw = -1;
		for (i = 0; i != this.lines.size(); i++)
		{
			int w = fm.stringWidth((String) this.lines.elementAt(i));
			if (w > maxw)
			{
				maxw = w;
			}
		}
		int cury = this.y;
		this.setBbox(this.x, this.y, this.x, this.y);
		for (i = 0; i != this.lines.size(); i++)
		{
			String s = (String) this.lines.elementAt(i);
			if ((this.flags & this.FLAG_CENTER) != 0)
			{
				this.x = (CircuitElm.cirSim.winSize.width - fm.stringWidth(s)) / 2;
			}
			g.drawString(s, this.x, cury);
			if ((this.flags & this.FLAG_BAR) != 0)
			{
				int by = cury - fm.getAscent();
				g.drawLine(this.x, by, this.x + fm.stringWidth(s) - 1, by);
			}
			this.adjustBbox(this.x, cury - fm.getAscent(), this.x + fm.stringWidth(s), cury + fm.getDescent());
			cury += fm.getHeight();
		}
		this.x2 = this.boundingBox.x + this.boundingBox.width;
		this.y2 = this.boundingBox.y + this.boundingBox.height;
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		if (n == 0)
		{
			EditInfo ei = new EditInfo("Text", 0, -1, -1);
			ei.text = this.text;
			return ei;
		}
		if (n == 1)
		{
			return new EditInfo("Size", this.size, 5, 100);
		}
		if (n == 2)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Center", (this.flags & this.FLAG_CENTER) != 0);
			return ei;
		}
		if (n == 3)
		{
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Checkbox("Draw Bar On Top", (this.flags & this.FLAG_BAR) != 0);
			return ei;
		}
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
		if (n == 0)
		{
			this.text = ei.textf.getText();
			this.split();
		}
		if (n == 1)
		{
			this.size = (int) ei.value;
		}
		if (n == 3)
		{
			if (ei.checkbox.getState())
			{
				this.flags |= this.FLAG_BAR;
			}
			else
			{
				this.flags &= ~this.FLAG_BAR;
			}
		}
		if (n == 2)
		{
			if (ei.checkbox.getState())
			{
				this.flags |= this.FLAG_CENTER;
			}
			else
			{
				this.flags &= ~this.FLAG_CENTER;
			}
		}
	}

	@Override
	boolean isCenteredText()
	{
		return (this.flags & this.FLAG_CENTER) != 0;
	}

	@Override
	void getInfo(String arr[])
	{
		arr[0] = this.text;
	}

	@Override
	int getPostCount()
	{
		return 0;
	}
}
