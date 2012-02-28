
package com.limoilux.circuit.ui.scope;

import java.awt.Graphics;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.MemoryImageSource;
import java.awt.image.Raster;
import java.util.StringTokenizer;

import com.limoilux.circuit.LogicOutputElm;
import com.limoilux.circuit.MemristorElm;
import com.limoilux.circuit.OutputElm;
import com.limoilux.circuit.ProbeElm;
import com.limoilux.circuit.TransistorElm;
import com.limoilux.circuit.core.CirSim;
import com.limoilux.circuit.core.CoreUtil;
import com.limoilux.circuit.techno.CircuitElm;

public class Scope
{
	static final int FLAG_YELM = 32;
	static final int VAL_POWER = 1;
	public static final int VAL_IB = 1;
	public static final int VAL_IC = 2;
	public static final int VAL_IE = 3;
	public static final int VAL_VBE = 4;
	public static final int VAL_VBC = 5;
	public static final int VAL_VCE = 6;
	static final int VAL_R = 2;
	public double minV[], maxV[], minMaxV;
	public double minI[], maxI[], minMaxI;
	public int scopePointCount = 128;
	public int ptr, ctr;
	public int speed;
	public int position;
	public int value, ivalue;
	public String text;
	public Rectangle rect;
	public boolean showI, showV, showMax, showMin, showFreq, lockScale, plot2d, plotXY;
	public CircuitElm elm;
	public CircuitElm xElm;
	public CircuitElm yElm;
	public MemoryImageSource imageSource;
	public BufferedImage image;
	public int pixels[];
	public int draw_ox, draw_oy;
	public float dpixels[];
	public CirSim sim;

	public Scope(CirSim s)
	{
		this.rect = new Rectangle();
		this.reset();
		this.sim = s;
	}

	public void showCurrent(boolean b)
	{
		this.showI = b;
		this.value = this.ivalue = 0;
	}

	public void showVoltage(boolean b)
	{
		this.showV = b;
		this.value = this.ivalue = 0;
	}

	public void showMax(boolean b)
	{
		this.showMax = b;
	}

	public void showMin(boolean b)
	{
		this.showMin = b;
	}

	public void showFreq(boolean b)
	{
		this.showFreq = b;
	}

	public void setLockScale(boolean b)
	{
		this.lockScale = b;
	}

	public void resetGraph()
	{
		this.scopePointCount = 1;
		while (this.scopePointCount <= this.rect.width)
		{
			this.scopePointCount *= 2;
		}
		this.minV = new double[this.scopePointCount];
		this.maxV = new double[this.scopePointCount];
		this.minI = new double[this.scopePointCount];
		this.maxI = new double[this.scopePointCount];
		this.ptr = this.ctr = 0;
		this.allocImage();
	}

	boolean active()
	{
		return this.elm != null;
	}

	void reset()
	{
		this.resetGraph();
		this.minMaxV = 5;
		this.minMaxI = .1;
		this.speed = 64;
		this.showI = this.showV = this.showMax = true;
		this.showFreq = this.lockScale = this.showMin = false;
		this.plot2d = false;
		// no showI for Output
		if (this.elm != null
				&& (this.elm instanceof OutputElm || this.elm instanceof LogicOutputElm || this.elm instanceof ProbeElm))
		{
			this.showI = false;
		}
		this.value = this.ivalue = 0;
		if (this.elm instanceof TransistorElm)
		{
			this.value = Scope.VAL_VCE;
		}
	}

	public void setRect(Rectangle r)
	{
		this.rect = r;
		this.resetGraph();
	}

	public int getWidth()
	{
		return this.rect.width;
	}

	public int rightEdge()
	{
		return this.rect.x + this.rect.width;
	}

	public void setElm(CircuitElm ce)
	{
		this.elm = ce;
		this.reset();
	}

	public void timeStep()
	{
		if (this.elm == null)
		{
			return;
		}
		double v = this.elm.getScopeValue(this.value);
		if (v < this.minV[this.ptr])
		{
			this.minV[this.ptr] = v;
		}
		if (v > this.maxV[this.ptr])
		{
			this.maxV[this.ptr] = v;
		}
		double i = 0;
		if (this.value == 0 || this.ivalue != 0)
		{
			i = this.ivalue == 0 ? this.elm.getCurrent() : this.elm.getScopeValue(this.ivalue);
			if (i < this.minI[this.ptr])
			{
				this.minI[this.ptr] = i;
			}
			if (i > this.maxI[this.ptr])
			{
				this.maxI[this.ptr] = i;
			}
		}

		if (this.plot2d && this.dpixels != null)
		{
			boolean newscale = false;
			while (v > this.minMaxV || v < -this.minMaxV)
			{
				this.minMaxV *= 2;
				newscale = true;
			}
			double yval = i;
			if (this.plotXY)
			{
				yval = this.yElm == null ? 0 : this.yElm.getVoltageDiff();
			}
			while (yval > this.minMaxI || yval < -this.minMaxI)
			{
				this.minMaxI *= 2;
				newscale = true;
			}
			if (newscale)
			{
				this.clear2dView();
			}
			double xa = v / this.minMaxV;
			double ya = yval / this.minMaxI;
			int x = (int) (this.rect.width * (1 + xa) * .499);
			int y = (int) (this.rect.height * (1 - ya) * .499);
			this.drawTo(x, y);
		}
		else
		{
			this.ctr++;
			if (this.ctr >= this.speed)
			{
				this.ptr = this.ptr + 1 & this.scopePointCount - 1;
				this.minV[this.ptr] = this.maxV[this.ptr] = v;
				this.minI[this.ptr] = this.maxI[this.ptr] = i;
				this.ctr = 0;
			}
		}
	}

	void drawTo(int x2, int y2)
	{
		if (this.draw_ox == -1)
		{
			this.draw_ox = x2;
			this.draw_oy = y2;
		}
		// need to draw a line from x1,y1 to x2,y2
		if (this.draw_ox == x2 && this.draw_oy == y2)
		{
			this.dpixels[x2 + this.rect.width * y2] = 1;
		}
		else if (CircuitElm.abs(y2 - this.draw_oy) > CircuitElm.abs(x2 - this.draw_ox))
		{
			// y difference is greater, so we step along y's
			// from min to max y and calculate x for each step
			double sgn = CircuitElm.sign(y2 - this.draw_oy);
			int x, y;
			for (y = this.draw_oy; y != y2 + sgn; y += sgn)
			{
				x = this.draw_ox + (x2 - this.draw_ox) * (y - this.draw_oy) / (y2 - this.draw_oy);
				this.dpixels[x + this.rect.width * y] = 1;
			}
		}
		else
		{
			// x difference is greater, so we step along x's
			// from min to max x and calculate y for each step
			double sgn = CircuitElm.sign(x2 - this.draw_ox);
			int x, y;
			for (x = this.draw_ox; x != x2 + sgn; x += sgn)
			{
				y = this.draw_oy + (y2 - this.draw_oy) * (x - this.draw_ox) / (x2 - this.draw_ox);
				this.dpixels[x + this.rect.width * y] = 1;
			}
		}
		this.draw_ox = x2;
		this.draw_oy = y2;
	}

	void clear2dView()
	{
		int i;
		for (i = 0; i != this.dpixels.length; i++)
		{
			this.dpixels[i] = 0;
		}
		this.draw_ox = this.draw_oy = -1;
	}

	public void adjustScale(double x)
	{
		this.minMaxV *= x;
		this.minMaxI *= x;
	}

	void draw2d(Graphics g)
	{
		int i;
		if (this.pixels == null || this.dpixels == null)
		{
			return;
		}
		int col = this.sim.printableCheckItem.getState() ? 0xFFFFFFFF : 0;
		for (i = 0; i != this.pixels.length; i++)
		{
			this.pixels[i] = col;
		}
		for (i = 0; i != this.rect.width; i++)
		{
			this.pixels[i + this.rect.width * (this.rect.height / 2)] = 0xFF00FF00;
		}
		int ycol = this.plotXY ? 0xFF00FF00 : 0xFFFFFF00;
		for (i = 0; i != this.rect.height; i++)
		{
			this.pixels[this.rect.width / 2 + this.rect.width * i] = ycol;
		}
		for (i = 0; i != this.pixels.length; i++)
		{
			int q = (int) (255 * this.dpixels[i]);
			if (q > 0)
			{
				this.pixels[i] = 0xFF000000 | 0x10101 * q;
			}
			this.dpixels[i] *= .997;
		}
		g.drawImage(this.image, this.rect.x, this.rect.y, null);
		g.setColor(CircuitElm.whiteColor);
		g.fillOval(this.rect.x + this.draw_ox - 2, this.rect.y + this.draw_oy - 2, 5, 5);
		int yt = this.rect.y + 10;
		int x = this.rect.x;
		if (this.text != null && this.rect.y + this.rect.height > yt + 5)
		{
			g.drawString(this.text, x, yt);
			yt += 15;
		}
	}

	public void draw(Graphics g)
	{
		if (this.elm == null)
		{
			return;
		}
		if (this.plot2d)
		{
			this.draw2d(g);
			return;
		}
		if (this.pixels == null)
		{
			return;
		}
		int i;
		int col = this.sim.printableCheckItem.getState() ? 0xFFFFFFFF : 0;
		for (i = 0; i != this.pixels.length; i++)
		{
			this.pixels[i] = col;
		}
		int x = 0;
		int maxy = (this.rect.height - 1) / 2;
		int y = maxy;

		boolean gotI = false;
		boolean gotV = false;
		int minRange = 4;
		double realMaxV = -1e8;
		double realMaxI = -1e8;
		double realMinV = 1e8;
		double realMinI = 1e8;
		int curColor = 0xFFFFFF00;
		int voltColor = this.value > 0 ? 0xFFFFFFFF : 0xFF00FF00;
		if (this.sim.scopeSelected == -1 && this.elm == this.sim.mouseElm)
		{
			curColor = voltColor = 0xFF00FFFF;
		}
		int ipa = this.ptr + this.scopePointCount - this.rect.width;
		for (i = 0; i != this.rect.width; i++)
		{
			int ip = i + ipa & this.scopePointCount - 1;
			while (this.maxV[ip] > this.minMaxV)
			{
				this.minMaxV *= 2;
			}
			while (this.minV[ip] < -this.minMaxV)
			{
				this.minMaxV *= 2;
			}
			while (this.maxI[ip] > this.minMaxI)
			{
				this.minMaxI *= 2;
			}
			while (this.minI[ip] < -this.minMaxI)
			{
				this.minMaxI *= 2;
			}
		}

		double gridStep = 1e-8;
		double gridMax = this.showI ? this.minMaxI : this.minMaxV;
		while (gridStep * 100 < gridMax)
		{
			gridStep *= 10;
		}
		if (maxy * gridStep / gridMax < .3)
		{
			gridStep = 0;
		}

		int ll;
		boolean sublines = maxy * gridStep / gridMax > 3;
		for (ll = -100; ll <= 100; ll++)
		{
			// don't show gridlines if plotting multiple values,
			// or if lines are too close together (except for center line)
			if (ll != 0 && (this.showI && this.showV || gridStep == 0))
			{
				continue;
			}
			int yl = maxy - (int) (maxy * ll * gridStep / gridMax);
			if (yl < 0 || yl >= this.rect.height - 1)
			{
				continue;
			}
			col = ll == 0 ? 0xFF909090 : 0xFF404040;
			if (ll % 10 != 0)
			{
				col = 0xFF101010;
				if (!sublines)
				{
					continue;
				}
			}
			for (i = 0; i != this.rect.width; i++)
			{
				this.pixels[i + yl * this.rect.width] = col;
			}
		}

		gridStep = 1e-15;
		double ts = this.sim.timer.timeStep * this.speed;
		while (gridStep < ts * 5)
		{
			gridStep *= 10;
		}
		double tstart = this.sim.timer.time - this.sim.timer.timeStep * this.speed * this.rect.width;
		double tx = this.sim.timer.time - this.sim.timer.time % gridStep;
		int first = 1;
		for (ll = 0;; ll++)
		{
			double tl = tx - gridStep * ll;
			int gx = (int) ((tl - tstart) / ts);
			if (gx < 0)
			{
				break;
			}
			if (gx >= this.rect.width)
			{
				continue;
			}
			if (tl < 0)
			{
				continue;
			}
			col = 0xFF202020;
			first = 0;
			if ((tl + gridStep / 4) % (gridStep * 10) < gridStep)
			{
				col = 0xFF909090;
				if ((tl + gridStep / 4) % (gridStep * 100) < gridStep)
				{
					col = 0xFF4040D0;
				}
			}
			for (i = 0; i < this.pixels.length; i += this.rect.width)
			{
				this.pixels[i + gx] = col;
			}
		}

		// these two loops are pretty much the same, and should be
		// combined!
		if (this.value == 0 && this.showI)
		{
			int ox = -1, oy = -1;
			int j;
			for (i = 0; i != this.rect.width; i++)
			{
				int ip = i + ipa & this.scopePointCount - 1;
				int miniy = (int) (maxy / this.minMaxI * this.minI[ip]);
				int maxiy = (int) (maxy / this.minMaxI * this.maxI[ip]);
				if (this.maxI[ip] > realMaxI)
				{
					realMaxI = this.maxI[ip];
				}
				if (this.minI[ip] < realMinI)
				{
					realMinI = this.minI[ip];
				}
				if (miniy <= maxy)
				{
					if (miniy < -minRange || maxiy > minRange)
					{
						gotI = true;
					}
					if (ox != -1)
					{
						if (miniy == oy && maxiy == oy)
						{
							continue;
						}
						for (j = ox; j != x + i; j++)
						{
							this.pixels[j + this.rect.width * (y - oy)] = curColor;
						}
						ox = oy = -1;
					}
					if (miniy == maxiy)
					{
						ox = x + i;
						oy = miniy;
						continue;
					}
					for (j = miniy; j <= maxiy; j++)
					{
						this.pixels[x + i + this.rect.width * (y - j)] = curColor;
					}
				}
			}
			if (ox != -1)
			{
				for (j = ox; j != x + i; j++)
				{
					this.pixels[j + this.rect.width * (y - oy)] = curColor;
				}
			}
		}
		if (this.value != 0 || this.showV)
		{
			int ox = -1, oy = -1, j;
			for (i = 0; i != this.rect.width; i++)
			{
				int ip = i + ipa & this.scopePointCount - 1;
				int minvy = (int) (maxy / this.minMaxV * this.minV[ip]);
				int maxvy = (int) (maxy / this.minMaxV * this.maxV[ip]);
				if (this.maxV[ip] > realMaxV)
				{
					realMaxV = this.maxV[ip];
				}
				if (this.minV[ip] < realMinV)
				{
					realMinV = this.minV[ip];
				}
				if ((this.value != 0 || this.showV) && minvy <= maxy)
				{
					if (minvy < -minRange || maxvy > minRange)
					{
						gotV = true;
					}
					if (ox != -1)
					{
						if (minvy == oy && maxvy == oy)
						{
							continue;
						}
						for (j = ox; j != x + i; j++)
						{
							this.pixels[j + this.rect.width * (y - oy)] = voltColor;
						}
						ox = oy = -1;
					}
					if (minvy == maxvy)
					{
						ox = x + i;
						oy = minvy;
						continue;
					}
					for (j = minvy; j <= maxvy; j++)
					{
						this.pixels[x + i + this.rect.width * (y - j)] = voltColor;
					}
				}
			}
			if (ox != -1)
			{
				for (j = ox; j != x + i; j++)
				{
					this.pixels[j + this.rect.width * (y - oy)] = voltColor;
				}
			}
		}
		double freq = 0;
		if (this.showFreq)
		{
			// try to get frequency
			// get average
			double avg = 0;
			for (i = 0; i != this.rect.width; i++)
			{
				int ip = i + ipa & this.scopePointCount - 1;
				avg += this.minV[ip] + this.maxV[ip];
			}
			avg /= i * 2;
			int state = 0;
			double thresh = avg * .05;
			int oi = 0;
			double avperiod = 0;
			int periodct = -1;
			double avperiod2 = 0;
			// count period lengths
			for (i = 0; i != this.rect.width; i++)
			{
				int ip = i + ipa & this.scopePointCount - 1;
				double q = this.maxV[ip] - avg;
				int os = state;
				if (q < thresh)
				{
					state = 1;
				}
				else if (q > -thresh)
				{
					state = 2;
				}
				if (state == 2 && os == 1)
				{
					int pd = i - oi;
					oi = i;
					// short periods can't be counted properly
					if (pd < 12)
					{
						continue;
					}
					// skip first period, it might be too short
					if (periodct >= 0)
					{
						avperiod += pd;
						avperiod2 += pd * pd;
					}
					periodct++;
				}
			}
			avperiod /= periodct;
			avperiod2 /= periodct;
			double periodstd = Math.sqrt(avperiod2 - avperiod * avperiod);
			freq = 1 / (avperiod * this.sim.timer.timeStep * this.speed);
			// don't show freq if standard deviation is too great
			if (periodct < 1 || periodstd > 2)
			{
				freq = 0;
				// System.out.println(freq + " " + periodstd + " " + periodct);
			}
		}
		g.drawImage(this.image, this.rect.x, this.rect.y, null);
		g.setColor(CircuitElm.whiteColor);
		int yt = this.rect.y + 10;
		x += this.rect.x;
		if (this.showMax)
		{
			if (this.value != 0)
			{
				g.drawString(CoreUtil.getUnitText(realMaxV, this.elm.getScopeUnits(this.value)), x, yt);
			}
			else if (this.showV)
			{
				g.drawString(CoreUtil.getVoltageText(realMaxV), x, yt);
			}
			else if (this.showI)
			{
				g.drawString(CoreUtil.getCurrentText(realMaxI), x, yt);
			}
			yt += 15;
		}
		if (this.showMin)
		{
			int ym = this.rect.y + this.rect.height - 5;
			if (this.value != 0)
			{
				g.drawString(CoreUtil.getUnitText(realMinV, this.elm.getScopeUnits(this.value)), x, ym);
			}
			else if (this.showV)
			{
				g.drawString(CoreUtil.getVoltageText(realMinV), x, ym);
			}
			else if (this.showI)
			{
				g.drawString(CoreUtil.getCurrentText(realMinI), x, ym);
			}
		}
		if (this.text != null && this.rect.y + this.rect.height > yt + 5)
		{
			g.drawString(this.text, x, yt);
			yt += 15;
		}
		if (this.showFreq && freq != 0 && this.rect.y + this.rect.height > yt + 5)
		{
			g.drawString(CoreUtil.getUnitText(freq, "Hz"), x, yt);
		}
		if (this.ptr > 5 && !this.lockScale)
		{
			if (!gotI && this.minMaxI > 1e-4)
			{
				this.minMaxI /= 2;
			}
			if (!gotV && this.minMaxV > 1e-4)
			{
				this.minMaxV /= 2;
			}
		}
	}

	public void speedUp()
	{
		if (this.speed > 1)
		{
			this.speed /= 2;
			this.resetGraph();
		}
	}

	public void slowDown()
	{
		this.speed *= 2;
		this.resetGraph();
	}

	public PopupMenu getMenu()
	{
		if (this.elm == null)
		{
			return null;
		}
		if (this.elm instanceof TransistorElm)
		{
			this.sim.scopeIbMenuItem.setState(this.value == Scope.VAL_IB);
			this.sim.scopeIcMenuItem.setState(this.value == Scope.VAL_IC);
			this.sim.scopeIeMenuItem.setState(this.value == Scope.VAL_IE);
			this.sim.scopeVbeMenuItem.setState(this.value == Scope.VAL_VBE);
			this.sim.scopeVbcMenuItem.setState(this.value == Scope.VAL_VBC);
			this.sim.scopeVceMenuItem.setState(this.value == Scope.VAL_VCE && this.ivalue != Scope.VAL_IC);
			this.sim.scopeVceIcMenuItem.setState(this.value == Scope.VAL_VCE && this.ivalue == Scope.VAL_IC);
			return this.sim.transScopeMenu;
		}
		else
		{
			this.sim.scopeVMenuItem.setState(this.showV && this.value == 0);
			this.sim.scopeIMenuItem.setState(this.showI && this.value == 0);
			this.sim.scopeMaxMenuItem.setState(this.showMax);
			this.sim.scopeMinMenuItem.setState(this.showMin);
			this.sim.scopeFreqMenuItem.setState(this.showFreq);
			this.sim.scopePowerMenuItem.setState(this.value == Scope.VAL_POWER);
			this.sim.scopeVIMenuItem.setState(this.plot2d && !this.plotXY);
			this.sim.scopeXYMenuItem.setState(this.plotXY);
			this.sim.scopeSelectYMenuItem.setEnabled(this.plotXY);
			this.sim.scopeResistMenuItem.setState(this.value == Scope.VAL_R);
			this.sim.scopeResistMenuItem.setEnabled(this.elm instanceof MemristorElm);
			return this.sim.scopeMenu;
		}
	}

	void setValue(int x)
	{
		this.reset();
		this.value = x;
	}

	public String dump()
	{
		if (this.elm == null)
		{
			return null;
		}
		int flags = (this.showI ? 1 : 0) | (this.showV ? 2 : 0) | (this.showMax ? 0 : 4)
				| // showMax used to be always on
				(this.showFreq ? 8 : 0) | (this.lockScale ? 16 : 0) | (this.plot2d ? 64 : 0) | (this.plotXY ? 128 : 0)
				| (this.showMin ? 256 : 0);
		flags |= Scope.FLAG_YELM; // yelm present
		int eno = this.sim.circuit.locateElement(this.elm);
		if (eno < 0)
		{
			return null;
		}
		int yno = this.yElm == null ? -1 : this.sim.circuit.locateElement(this.yElm);
		String x = "o " + eno + " " + this.speed + " " + this.value + " " + flags + " " + this.minMaxV + " "
				+ this.minMaxI + " " + this.position + " " + yno;
		if (this.text != null)
		{
			x += " " + this.text;
		}
		return x;
	}

	public void undump(StringTokenizer st)
	{
		this.reset();
		int e = new Integer(st.nextToken()).intValue();
		if (e == -1)
		{
			return;
		}
		this.elm = this.sim.circuit.getElement(e);
		this.speed = new Integer(st.nextToken()).intValue();
		this.value = new Integer(st.nextToken()).intValue();
		int flags = new Integer(st.nextToken()).intValue();
		this.minMaxV = new Double(st.nextToken()).doubleValue();
		this.minMaxI = new Double(st.nextToken()).doubleValue();
		if (this.minMaxV == 0)
		{
			this.minMaxV = .5;
		}
		if (this.minMaxI == 0)
		{
			this.minMaxI = 1;
		}
		this.text = null;
		this.yElm = null;
		try
		{
			this.position = new Integer(st.nextToken()).intValue();
			int ye = -1;
			if ((flags & Scope.FLAG_YELM) != 0)
			{
				ye = new Integer(st.nextToken()).intValue();
				if (ye != -1)
				{
					this.yElm = this.sim.circuit.getElement(ye);
				}
			}
			while (st.hasMoreTokens())
			{
				if (this.text == null)
				{
					this.text = st.nextToken();
				}
				else
				{
					this.text += " " + st.nextToken();
				}
			}
		}
		catch (Exception ee)
		{
		}
		this.showI = (flags & 1) != 0;
		this.showV = (flags & 2) != 0;
		this.showMax = (flags & 4) == 0;
		this.showFreq = (flags & 8) != 0;
		this.lockScale = (flags & 16) != 0;
		this.plot2d = (flags & 64) != 0;
		this.plotXY = (flags & 128) != 0;
		this.showMin = (flags & 256) != 0;
	}

	private void allocImage()
	{
		this.pixels = null;
		int w = this.rect.width;
		int h = this.rect.height;
		int size = w * h;

		if (w > 0 && h > 0)
		{
			if (this.sim.useBufferedImage)
			{
				this.image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				Raster raster = this.image.getRaster();
				DataBuffer dbu = raster.getDataBuffer();
				DataBufferInt dbi = (DataBufferInt) dbu;
				this.pixels = dbi.getData();
			}

			if (this.pixels == null)
			{
				this.pixels = new int[size];

				for (int i = 0; i < size; i++)
				{
					this.pixels[i] = 0xFF000000;
				}

				this.imageSource = new MemoryImageSource(w, h, this.pixels, 0, w);
				this.imageSource.setAnimated(true);
				this.imageSource.setFullBufferUpdates(true);
				this.image = (BufferedImage) this.sim.circuitCanvas.createImage(this.imageSource);
			}

			this.dpixels = new float[size];
			this.draw_ox = this.draw_oy = -1;
		}
	}

	public void handleMenu(ItemEvent e, Object mi)
	{
		if (mi == this.sim.scopeVMenuItem)
		{
			this.showVoltage(this.sim.scopeVMenuItem.getState());
		}
		if (mi == this.sim.scopeIMenuItem)
		{
			this.showCurrent(this.sim.scopeIMenuItem.getState());
		}
		if (mi == this.sim.scopeMaxMenuItem)
		{
			this.showMax(this.sim.scopeMaxMenuItem.getState());
		}
		if (mi == this.sim.scopeMinMenuItem)
		{
			this.showMin(this.sim.scopeMinMenuItem.getState());
		}
		if (mi == this.sim.scopeFreqMenuItem)
		{
			this.showFreq(this.sim.scopeFreqMenuItem.getState());
		}
		if (mi == this.sim.scopePowerMenuItem)
		{
			this.setValue(Scope.VAL_POWER);
		}
		if (mi == this.sim.scopeIbMenuItem)
		{
			this.setValue(Scope.VAL_IB);
		}
		if (mi == this.sim.scopeIcMenuItem)
		{
			this.setValue(Scope.VAL_IC);
		}
		if (mi == this.sim.scopeIeMenuItem)
		{
			this.setValue(Scope.VAL_IE);
		}
		if (mi == this.sim.scopeVbeMenuItem)
		{
			this.setValue(Scope.VAL_VBE);
		}
		if (mi == this.sim.scopeVbcMenuItem)
		{
			this.setValue(Scope.VAL_VBC);
		}
		if (mi == this.sim.scopeVceMenuItem)
		{
			this.setValue(Scope.VAL_VCE);
		}
		if (mi == this.sim.scopeVceIcMenuItem)
		{
			this.plot2d = true;
			this.plotXY = false;
			this.value = Scope.VAL_VCE;
			this.ivalue = Scope.VAL_IC;
			this.resetGraph();
		}

		if (mi == this.sim.scopeVIMenuItem)
		{
			this.plot2d = this.sim.scopeVIMenuItem.getState();
			this.plotXY = false;
			this.resetGraph();
		}
		if (mi == this.sim.scopeXYMenuItem)
		{
			this.plotXY = this.plot2d = this.sim.scopeXYMenuItem.getState();
			if (this.yElm == null)
			{
				this.selectY();
			}
			this.resetGraph();
		}
		if (mi == this.sim.scopeResistMenuItem)
		{
			this.setValue(Scope.VAL_R);
		}
	}

	public void select()
	{
		this.sim.mouseElm = this.elm;
		if (this.plotXY)
		{
			this.sim.plotXElm = this.elm;
			this.sim.plotYElm = this.yElm;
		}
	}

	public void selectY()
	{
		int e = this.yElm == null ? -1 : this.sim.circuit.locateElement(this.yElm);
		int firstE = e;
		while (true)
		{
			for (e++; e < this.sim.circuit.getElementCount(); e++)
			{
				CircuitElm ce = this.sim.circuit.getElement(e);
				if ((ce instanceof OutputElm || ce instanceof ProbeElm) && ce != this.elm)
				{
					this.yElm = ce;
					return;
				}
			}
			if (firstE == -1)
			{
				return;
			}
			e = firstE = -1;
		}
	}
}
