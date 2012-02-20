
package com.limoilux.circuit;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.text.NumberFormat;

import com.limoilux.circuit.core.CoreUtil;
import com.limoilux.circuit.ui.DrawUtil;

public abstract class CircuitElm implements Editable
{
	private static final int COLOR_SCALE_COUNT = 32;
	@Deprecated
	public static final double PI = 3.14159265358979323846;
	public static final Font unitsFont = new Font("SansSerif", 0, 10);

	static double voltageRange = 5;

	static Color colorScale[];
	static double currentMult, powerMult;

	public static final Point ps1 = new Point();
	public static final Point ps2 = new Point();

	public static CirSim cirSim;
	public static Color whiteColor;
	public static Color selectColor;
	public static Color lightGrayColor;
	public static NumberFormat showFormat;
	public static NumberFormat shortFormat;
	public static NumberFormat noCommaFormat;

	public Rectangle boundingBox;

	public Point point1;
	public Point point2;
	public Point lead1;
	public Point lead2;

	public int x;
	public int y;
	public int x2;
	public int y2;
	public int flags;
	public int voltSource;
	public int dx;
	public int dy;
	public int dsign;

	public double dn;
	public double dpx1;
	public double dpy1;
	public double current;
	public double curcount;

	public boolean noDiagonal;
	public boolean selected;

	public int nodes[];

	public double volts[];

	public CircuitElm(int xx, int yy)
	{
		this.x = this.x2 = xx;
		this.y = this.y2 = yy;
		this.flags = this.getDefaultFlags();
		this.allocNodes();
		this.initBoundingBox();
	}

	public CircuitElm(int xa, int ya, int xb, int yb, int f)
	{
		this.x = xa;
		this.y = ya;
		this.x2 = xb;
		this.y2 = yb;
		this.flags = f;
		this.allocNodes();
		this.initBoundingBox();
	}

	public int getDumpType()
	{
		return 0;
	}

	public Class getDumpClass()
	{
		return this.getClass();
	}

	int getDefaultFlags()
	{
		return 0;
	}

	private void initBoundingBox()
	{
		this.boundingBox = new Rectangle();
		this.boundingBox.setBounds(Math.min(this.x, this.x2), Math.min(this.y, this.y2),
				Math.abs(this.x2 - this.x) + 1, Math.abs(this.y2 - this.y) + 1);
	}

	public void allocNodes()
	{
		this.nodes = new int[this.getPostCount() + this.getInternalNodeCount()];
		this.volts = new double[this.getPostCount() + this.getInternalNodeCount()];
	}

	public String dump()
	{
		int t = this.getDumpType();
		return (t < 127 ? (char) t + " " : t + " ") + this.x + " " + this.y + " " + this.x2 + " " + this.y2 + " "
				+ this.flags;
	}

	public void reset()
	{
		int i;
		for (i = 0; i != this.getPostCount() + this.getInternalNodeCount(); i++)
		{
			this.volts[i] = 0;
		}
		this.curcount = 0;
	}

	public abstract void draw(Graphics g);

	void setCurrent(int x, double c)
	{
		this.current = c;
	}

	double getCurrent()
	{
		return this.current;
	}

	public void doStep()
	{

	}

	public void delete()
	{

	}

	public void startIteration()
	{

	}

	double getPostVoltage(int x)
	{
		return this.volts[x];
	}

	public void setNodeVoltage(int n, double c)
	{
		this.volts[n] = c;
		this.calculateCurrent();
	}

	public void calculateCurrent()
	{
	}

	public void setPoints()
	{
		this.dx = this.x2 - this.x;
		this.dy = this.y2 - this.y;
		this.dn = Math.sqrt(this.dx * this.dx + this.dy * this.dy);
		this.dpx1 = this.dy / this.dn;
		this.dpy1 = -this.dx / this.dn;
		this.dsign = this.dy == 0 ? CoreUtil.sign(this.dx) : CoreUtil.sign(this.dy);
		this.point1 = new Point(this.x, this.y);
		this.point2 = new Point(this.x2, this.y2);
	}

	void calcLeads(int len)
	{
		if (this.dn < len || len == 0)
		{
			this.lead1 = this.point1;
			this.lead2 = this.point2;
			return;
		}
		this.lead1 = CoreUtil.interpPoint(this.point1, this.point2, (this.dn - len) / (2 * this.dn));
		this.lead2 = CoreUtil.interpPoint(this.point1, this.point2, (this.dn + len) / (2 * this.dn));
	}

	@Deprecated
	public static Point interpPoint(Point a, Point b, double f)
	{
		return CoreUtil.interpPoint(a, b, f);
	}

	@Deprecated
	public static void interpPoint(Point a, Point b, Point c, double f)
	{
		CoreUtil.interpPoint(a, b, c, f);
	}

	@Deprecated
	public static void interpPoint(Point a, Point b, Point c, double f, double g)
	{
		CoreUtil.interpPoint(a, b, c, f, g);
	}

	@Deprecated
	public static Point interpPoint(Point a, Point b, double f, double g)
	{
		return CoreUtil.interpPoint(a, b, f, g);
	}

	@Deprecated
	public static void interpPoint2(Point a, Point b, Point c, Point d, double f, double g)
	{
		CoreUtil.interpPoint2(a, b, c, d, f, g);
	}

	void draw2Leads(Graphics g)
	{
		// draw first lead
		this.setVoltageColor(g, this.volts[0]);
		DrawUtil.drawThickLine(g, this.point1, this.lead1);

		// draw second lead
		this.setVoltageColor(g, this.volts[1]);
		DrawUtil.drawThickLine(g, this.lead2, this.point2);
	}

	@Deprecated
	public static Point[] newPointArray(int n)
	{
		return CoreUtil.newPointArray(n);
	}

	@Deprecated
	public static void drawDots(Graphics g, Point pa, Point pb, double pos)
	{
		DrawUtil.drawDots(g, pa, pb, pos);
	}

	Polygon calcArrow(Point a, Point b, double al, double aw)
	{
		Polygon poly = new Polygon();
		Point p1 = new Point();
		Point p2 = new Point();
		int adx = b.x - a.x;
		int ady = b.y - a.y;
		double l = Math.sqrt(adx * adx + ady * ady);
		poly.addPoint(b.x, b.y);
		CoreUtil.interpPoint2(a, b, p1, p2, 1 - al / l, aw);
		poly.addPoint(p1.x, p1.y);
		poly.addPoint(p2.x, p2.y);
		return poly;
	}

	Polygon createPolygon(Point a, Point b, Point c)
	{
		Polygon p = new Polygon();
		p.addPoint(a.x, a.y);
		p.addPoint(b.x, b.y);
		p.addPoint(c.x, c.y);
		return p;
	}

	Polygon createPolygon(Point a, Point b, Point c, Point d)
	{
		Polygon p = new Polygon();
		p.addPoint(a.x, a.y);
		p.addPoint(b.x, b.y);
		p.addPoint(c.x, c.y);
		p.addPoint(d.x, d.y);
		return p;
	}

	Polygon createPolygon(Point a[])
	{
		Polygon p = new Polygon();
		int i;
		for (i = 0; i != a.length; i++)
		{
			p.addPoint(a[i].x, a[i].y);
		}
		return p;
	}

	public void drag(int xx, int yy)
	{
		xx = CircuitElm.cirSim.snapGrid(xx);
		yy = CircuitElm.cirSim.snapGrid(yy);
		if (this.noDiagonal)
		{
			if (Math.abs(this.x - xx) < Math.abs(this.y - yy))
			{
				xx = this.x;
			}
			else
			{
				yy = this.y;
			}
		}
		this.x2 = xx;
		this.y2 = yy;
		this.setPoints();
	}

	void move(int dx, int dy)
	{
		this.x += dx;
		this.y += dy;
		this.x2 += dx;
		this.y2 += dy;
		this.boundingBox.move(dx, dy);
		this.setPoints();
	}

	// determine if moving this element by (dx,dy) will put it on top of another
	// element
	boolean allowMove(int dx, int dy)
	{
		int nx = this.x + dx;
		int ny = this.y + dy;
		int nx2 = this.x2 + dx;
		int ny2 = this.y2 + dy;
		int i;
		for (i = 0; i != CircuitElm.cirSim.elmList.size(); i++)
		{
			CircuitElm ce = CircuitElm.cirSim.getElm(i);
			if (ce.x == nx && ce.y == ny && ce.x2 == nx2 && ce.y2 == ny2)
			{
				return false;
			}
			if (ce.x == nx2 && ce.y == ny2 && ce.x2 == nx && ce.y2 == ny)
			{
				return false;
			}
		}
		return true;
	}

	void movePoint(int n, int dx, int dy)
	{
		if (n == 0)
		{
			this.x += dx;
			this.y += dy;
		}
		else
		{
			this.x2 += dx;
			this.y2 += dy;
		}
		this.setPoints();
	}

	void drawPosts(Graphics g)
	{
		int i;
		for (i = 0; i != this.getPostCount(); i++)
		{
			Point p = this.getPost(i);
			this.drawPost(g, p.x, p.y, this.nodes[i]);
		}
	}

	public void stamp()
	{
	}

	public int getVoltageSourceCount()
	{
		return 0;
	}

	int getInternalNodeCount()
	{
		return 0;
	}

	void setNode(int p, int n)
	{
		this.nodes[p] = n;
	}

	void setVoltageSource(int n, int v)
	{
		this.voltSource = v;
	}

	int getVoltageSource()
	{
		return this.voltSource;
	}

	public double getVoltageDiff()
	{
		return this.volts[0] - this.volts[1];
	}

	public boolean nonLinear()
	{
		return false;
	}

	public int getPostCount()
	{
		return 2;
	}

	int getNode(int n)
	{
		return this.nodes[n];
	}

	public Point getPost(int n)
	{
		return n == 0 ? this.point1 : n == 1 ? this.point2 : null;
	}

	void drawPost(Graphics g, int x0, int y0, int n)
	{
		if (CircuitElm.cirSim.dragElm == null && !this.needsHighlight()
				&& CircuitElm.cirSim.getCircuitNode(n).links.size() == 2)
		{
			return;
		}
		if (CircuitElm.cirSim.mouseMode == CirSim.MODE_DRAG_ROW
				|| CircuitElm.cirSim.mouseMode == CirSim.MODE_DRAG_COLUMN)
		{
			return;
		}
		this.drawPost(g, x0, y0);
	}

	void drawPost(Graphics g, int x0, int y0)
	{
		g.setColor(CircuitElm.whiteColor);
		g.fillOval(x0 - 3, y0 - 3, 7, 7);
	}

	void setBbox(int x1, int y1, int x2, int y2)
	{
		if (x1 > x2)
		{
			int q = x1;
			x1 = x2;
			x2 = q;
		}
		if (y1 > y2)
		{
			int q = y1;
			y1 = y2;
			y2 = q;
		}
		this.boundingBox.setBounds(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
	}

	void setBbox(Point p1, Point p2, double w)
	{
		this.setBbox(p1.x, p1.y, p2.x, p2.y);
		int gx = p2.y - p1.y;
		int gy = p1.x - p2.x;
		int dpx = (int) (this.dpx1 * w);
		int dpy = (int) (this.dpy1 * w);
		this.adjustBbox(p1.x + dpx, p1.y + dpy, p1.x - dpx, p1.y - dpy);
	}

	void adjustBbox(int x1, int y1, int x2, int y2)
	{
		if (x1 > x2)
		{
			int q = x1;
			x1 = x2;
			x2 = q;
		}
		if (y1 > y2)
		{
			int q = y1;
			y1 = y2;
			y2 = q;
		}
		x1 = CircuitElm.min(this.boundingBox.x, x1);
		y1 = CircuitElm.min(this.boundingBox.y, y1);
		x2 = CircuitElm.max(this.boundingBox.x + this.boundingBox.width - 1, x2);
		y2 = CircuitElm.max(this.boundingBox.y + this.boundingBox.height - 1, y2);
		this.boundingBox.setBounds(x1, y1, x2 - x1, y2 - y1);
	}

	void adjustBbox(Point p1, Point p2)
	{
		this.adjustBbox(p1.x, p1.y, p2.x, p2.y);
	}

	public boolean isCenteredText()
	{
		return false;
	}

	void drawCenteredText(Graphics g, String s, int x, int y, boolean cx)
	{
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(s);
		if (cx)
		{
			x -= w / 2;
		}
		g.drawString(s, x, y + fm.getAscent() / 2);
		this.adjustBbox(x, y - fm.getAscent() / 2, x + w, y + fm.getAscent() / 2 + fm.getDescent());
	}

	void drawValues(Graphics g, String s, double hs)
	{
		if (s == null)
		{
			return;
		}
		g.setFont(CircuitElm.unitsFont);
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(s);
		g.setColor(CircuitElm.whiteColor);
		int ya = fm.getAscent() / 2;
		int xc, yc;
		if (this instanceof RailElm || this instanceof SweepElm)
		{
			xc = this.x2;
			yc = this.y2;
		}
		else
		{
			xc = (this.x2 + this.x) / 2;
			yc = (this.y2 + this.y) / 2;
		}
		int dpx = (int) (this.dpx1 * hs);
		int dpy = (int) (this.dpy1 * hs);
		if (dpx == 0)
		{
			g.drawString(s, xc - w / 2, yc - CircuitElm.abs(dpy) - 2);
		}
		else
		{
			int xx = xc + CircuitElm.abs(dpx) + 2;
			if (this instanceof VoltageElm || this.x < this.x2 && this.y > this.y2)
			{
				xx = xc - (w + CircuitElm.abs(dpx) + 2);
			}
			g.drawString(s, xx, yc + dpy + ya);
		}
	}

	void drawCoil(Graphics g, int hs, Point p1, Point p2, double v1, double v2)
	{
		double len = CoreUtil.distance(p1, p2);
		int segments = 30; // 10*(int) (len/10);
		int i;
		double segf = 1. / segments;

		CircuitElm.ps1.setLocation(p1);
		for (i = 0; i != segments; i++)
		{
			double cx = (i + 1) * 6. * segf % 2 - 1;
			double hsx = Math.sqrt(1 - cx * cx);
			if (hsx < 0)
			{
				hsx = -hsx;
			}
			CoreUtil.interpPoint(p1, p2, CircuitElm.ps2, i * segf, hsx * hs);
			double v = v1 + (v2 - v1) * i / segments;
			this.setVoltageColor(g, v);
			DrawUtil.drawThickLine(g, CircuitElm.ps1, CircuitElm.ps2);
			CircuitElm.ps1.setLocation(CircuitElm.ps2);
		}
	}

	@Deprecated
	public static void drawThickLine(Graphics g, int x, int y, int x2, int y2)
	{
		DrawUtil.drawThickLine(g, x, y, x2, y2);
	}

	@Deprecated
	public static void drawThickLine(Graphics g, Point pa, Point pb)
	{
		DrawUtil.drawThickLine(g, pa, pb);

	}

	@Deprecated
	public static void drawThickPolygon(Graphics g, int xs[], int ys[], int c)
	{
		DrawUtil.drawThickPolygon(g, xs, ys, c);
	}

	@Deprecated
	public static void drawThickPolygon(Graphics g, Polygon p)
	{
		DrawUtil.drawThickPolygon(g, p);
	}

	@Deprecated
	public static void drawThickCircle(Graphics g, int cx, int cy, int ri)
	{
		DrawUtil.drawThickCircle(g, cx, cy, ri);
	}

	static String getVoltageDText(double v)
	{
		return CircuitElm.getUnitText(Math.abs(v), "V");
	}

	static String getVoltageText(double v)
	{
		return CircuitElm.getUnitText(v, "V");
	}

	static String getUnitText(double v, String u)
	{
		double va = Math.abs(v);
		if (va < 1e-14)
		{
			return "0 " + u;
		}
		if (va < 1e-9)
		{
			return CircuitElm.showFormat.format(v * 1e12) + " p" + u;
		}
		if (va < 1e-6)
		{
			return CircuitElm.showFormat.format(v * 1e9) + " n" + u;
		}
		if (va < 1e-3)
		{
			return CircuitElm.showFormat.format(v * 1e6) + " " + CirSim.muString + u;
		}
		if (va < 1)
		{
			return CircuitElm.showFormat.format(v * 1e3) + " m" + u;
		}
		if (va < 1e3)
		{
			return CircuitElm.showFormat.format(v) + " " + u;
		}
		if (va < 1e6)
		{
			return CircuitElm.showFormat.format(v * 1e-3) + " k" + u;
		}
		if (va < 1e9)
		{
			return CircuitElm.showFormat.format(v * 1e-6) + " M" + u;
		}
		return CircuitElm.showFormat.format(v * 1e-9) + " G" + u;
	}

	static String getShortUnitText(double v, String u)
	{
		double va = Math.abs(v);
		if (va < 1e-13)
		{
			return null;
		}
		if (va < 1e-9)
		{
			return CircuitElm.shortFormat.format(v * 1e12) + "p" + u;
		}
		if (va < 1e-6)
		{
			return CircuitElm.shortFormat.format(v * 1e9) + "n" + u;
		}
		if (va < 1e-3)
		{
			return CircuitElm.shortFormat.format(v * 1e6) + CirSim.muString + u;
		}
		if (va < 1)
		{
			return CircuitElm.shortFormat.format(v * 1e3) + "m" + u;
		}
		if (va < 1e3)
		{
			return CircuitElm.shortFormat.format(v) + u;
		}
		if (va < 1e6)
		{
			return CircuitElm.shortFormat.format(v * 1e-3) + "k" + u;
		}
		if (va < 1e9)
		{
			return CircuitElm.shortFormat.format(v * 1e-6) + "M" + u;
		}
		return CircuitElm.shortFormat.format(v * 1e-9) + "G" + u;
	}

	static String getCurrentText(double i)
	{
		return CircuitElm.getUnitText(i, "A");
	}

	static String getCurrentDText(double i)
	{
		return CircuitElm.getUnitText(Math.abs(i), "A");
	}

	void updateDotCount()
	{
		this.curcount = this.updateDotCount(this.current, this.curcount);
	}

	double updateDotCount(double cur, double cc)
	{
		if (CircuitElm.cirSim.stoppedCheck.getState())
		{
			return cc;
		}
		double cadd = cur * CircuitElm.currentMult;
		/*
		 * if (cur != 0 && cadd <= .05 && cadd >= -.05) cadd = (cadd < 0) ? -.05
		 * : .05;
		 */
		cadd %= 8;
		/*
		 * if (cadd > 8) cadd = 8; if (cadd < -8) cadd = -8;
		 */
		return cc + cadd;
	}

	void doDots(Graphics g)
	{
		this.updateDotCount();
		if (CircuitElm.cirSim.dragElm != this)
		{
			DrawUtil.drawDots(g, this.point1, this.point2, this.curcount);
		}
	}

	void doAdjust()
	{
	}

	void setupAdjust()
	{
	}

	public void getInfo(String arr[])
	{
	}

	int getBasicInfo(String arr[])
	{
		arr[1] = "I = " + CircuitElm.getCurrentDText(this.getCurrent());
		arr[2] = "Vd = " + CircuitElm.getVoltageDText(this.getVoltageDiff());
		return 3;
	}

	void setVoltageColor(Graphics g, double volts)
	{
		if (this.needsHighlight())
		{
			g.setColor(CircuitElm.selectColor);
			return;
		}
		if (!CircuitElm.cirSim.voltsCheckItem.getState())
		{
			if (!CircuitElm.cirSim.powerCheckItem.getState())
			{
				// !conductanceCheckItem.getState())
				g.setColor(CircuitElm.whiteColor);
			}
			return;
		}
		int c = (int) ((volts + CircuitElm.voltageRange) * (CircuitElm.COLOR_SCALE_COUNT - 1) / (CircuitElm.voltageRange * 2));
		if (c < 0)
		{
			c = 0;
		}
		if (c >= CircuitElm.COLOR_SCALE_COUNT)
		{
			c = CircuitElm.COLOR_SCALE_COUNT - 1;
		}
		g.setColor(CircuitElm.colorScale[c]);
	}

	void setPowerColor(Graphics g, boolean yellow)
	{
		/*
		 * if (conductanceCheckItem.getState()) { setConductanceColor(g,
		 * current/getVoltageDiff()); return; }
		 */
		if (!CircuitElm.cirSim.powerCheckItem.getState())
		{
			return;
		}
		this.setPowerColor(g, this.getPower());
	}

	void setPowerColor(Graphics g, double w0)
	{
		w0 *= CircuitElm.powerMult;
		// System.out.println(w);
		double w = w0 < 0 ? -w0 : w0;
		if (w > 1)
		{
			w = 1;
		}
		int rg = 128 + (int) (w * 127);
		int b = (int) (128 * (1 - w));
		/*
		 * if (yellow) g.setColor(new Color(rg, rg, b)); else
		 */
		if (w0 > 0)
		{
			g.setColor(new Color(rg, b, b));
		}
		else
		{
			g.setColor(new Color(b, rg, b));
		}
	}

	void setConductanceColor(Graphics g, double w0)
	{
		w0 *= CircuitElm.powerMult;
		// System.out.println(w);
		double w = w0 < 0 ? -w0 : w0;
		if (w > 1)
		{
			w = 1;
		}
		int rg = (int) (w * 255);
		g.setColor(new Color(rg, rg, rg));
	}

	double getPower()
	{
		return this.getVoltageDiff() * this.current;
	}

	double getScopeValue(int x)
	{
		return x == 1 ? this.getPower() : this.getVoltageDiff();
	}

	String getScopeUnits(int x)
	{
		return x == 1 ? "W" : "V";
	}

	@Override
	public EditInfo getEditInfo(int n)
	{
		return null;
	}

	@Override
	public void setEditValue(int n, EditInfo ei)
	{
	}

	public boolean getConnection(int n1, int n2)
	{
		return true;
	}

	public boolean hasGroundConnection(int n1)
	{
		return false;
	}

	boolean isWire()
	{
		return false;
	}

	boolean canViewInScope()
	{
		return this.getPostCount() <= 2;
	}

	boolean comparePair(int x1, int x2, int y1, int y2)
	{
		return x1 == y1 && x2 == y2 || x1 == y2 && x2 == y1;
	}

	boolean needsHighlight()
	{
		return CircuitElm.cirSim.mouseElm == this || this.selected;
	}

	boolean isSelected()
	{
		return this.selected;
	}

	void setSelected(boolean x)
	{
		this.selected = x;
	}

	void selectRect(Rectangle r)
	{
		this.selected = r.intersects(this.boundingBox);
	}

	public Rectangle getBoundingBox()
	{
		return this.boundingBox;
	}

	@Deprecated
	public static int abs(int x)
	{
		return Math.abs(x);
	}

	@Deprecated
	public static int sign(int x)
	{
		return CoreUtil.sign(x);
	}

	@Deprecated
	public static int min(int a, int b)
	{
		return Math.min(a, b);
	}

	@Deprecated
	public static int max(int a, int b)
	{
		return Math.max(a, b);
	}

	@Deprecated
	public static double distance(Point p1, Point p2)
	{
		return CoreUtil.distance(p1, p2);
	}

	public static void initClass(CirSim cirSim)
	{
		CircuitElm.cirSim = cirSim;

		CircuitElm.colorScale = new Color[CircuitElm.COLOR_SCALE_COUNT];

		for (int i = 0; i < colorScale.length; i++)
		{
			double v = i * 2. / CircuitElm.COLOR_SCALE_COUNT - 1;
			if (v < 0)
			{
				int n1 = (int) (128 * -v) + 127;
				int n2 = (int) (127 * (1 + v));
				CircuitElm.colorScale[i] = new Color(n1, n2, n2);
			}
			else
			{
				int n1 = (int) (128 * v) + 127;
				int n2 = (int) (127 * (1 - v));
				CircuitElm.colorScale[i] = new Color(n2, n1, n2);
			}
		}

		CircuitElm.showFormat = NumberFormat.getInstance();
		CircuitElm.showFormat.setMaximumFractionDigits(2);

		CircuitElm.shortFormat = NumberFormat.getInstance();
		CircuitElm.shortFormat.setMaximumFractionDigits(1);

		CircuitElm.noCommaFormat = NumberFormat.getInstance();
		CircuitElm.noCommaFormat.setMaximumFractionDigits(10);
		CircuitElm.noCommaFormat.setGroupingUsed(false);
	}

	public boolean needsShortcut()
	{
		return false;
	}
}
