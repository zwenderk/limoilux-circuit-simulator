//CirSim.java (c) 2010 by Paul Falstad

package com.limoilux.circuitsimulator.core;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.limoilux.circuit.CapacitorElm;
import com.limoilux.circuit.InductorElm;
import com.limoilux.circuit.ResistorElm;
import com.limoilux.circuit.SwitchElm;
import com.limoilux.circuit.TextElm;
import com.limoilux.circuit.core.Clipboard;
import com.limoilux.circuit.core.Editable;

import com.limoilux.circuit.core.Timer;
import com.limoilux.circuit.techno.CircuitAnalysisException;
import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuit.techno.CircuitNode;
import com.limoilux.circuit.techno.CircuitNodeLink;
import com.limoilux.circuit.techno.matrix.MatrixRowInfo;
import com.limoilux.circuit.ui.ActivityListener;
import com.limoilux.circuit.ui.ActivityManager;
import com.limoilux.circuit.ui.DrawUtil;
import com.limoilux.circuit.ui.EditDialog;
import com.limoilux.circuit.ui.EditOptions;
import com.limoilux.circuit.ui.io.MigrationWizard;
import com.limoilux.circuitsimulator.circuit.Circuit;
import com.limoilux.circuitsimulator.circuit.CircuitManager;
import com.limoilux.circuitsimulator.circuit.CircuitPane;
import com.limoilux.circuitsimulator.circuit.CircuitUtil;
import com.limoilux.circuitsimulator.circuit.CircuitMouseManager;
import com.limoilux.circuitsimulator.scope.Scope;
import com.limoilux.circuitsimulator.scope.ScopeManager;

/**
 * For information about the theory behind this, see Electronic Circuit & System
 * Simulation Methods by Pillage
 * 
 * @author Paul Falstad (2011 version)
 * @author David Bernard
 * 
 */
public class CircuitSimulator implements ComponentListener, ActionListener, ItemListener
{

	private static final CircuitSimulator SINGLETON = new CircuitSimulator();
	@Deprecated
	private static final double PI = Math.PI;

	private static final int subiterCount = 5000;
	private static final int MODE_ADD_ELM = 0;
	private static final int MODE_DRAG_ALL = 1;

	private static final int MODE_DRAG_SELECTED = 4;
	private static final int MODE_DRAG_POST = 5;
	public static final int MODE_SELECT = 6;

	public double currentMultiplier;

	private static final int HINT_LC = 1;
	private static final int HINT_RC = 2;
	private static final int HINT_3DB_C = 3;
	private static final int HINT_TWINT = 4;
	private static final int HINT_3DB_L = 5;

	public static final int INFO_WIDTH = 120;
	public static final int MODE_DRAG_ROW = 2;
	public static final int MODE_DRAG_COLUMN = 3;

	public static String muString = "u";
	public static String ohmString = "ohm";
	public static EditDialog editDialog;
	public static MigrationWizard impDialog;

	private String startCircuit = null;
	private String startLabel = null;
	private String startCircuitText = null;

	public int subIterations;

	public Dimension winSize;
	public CircuitElm dragElm;
	public CircuitElm menuElm;
	public CircuitElm stopElm;

	public CircuitElm plotXElm;
	public CircuitElm plotYElm;

	private SwitchElm heldSwitchElm;

	private Rectangle selectedArea;

	public int gridSize, gridMask, gridRound;

	public boolean analyzeFlag;

	public boolean useBufferedImage;
	private String ctrlMetaKey;

	public int scopeSelected = -1;
	private int menuScope = -1;
	private int hintType = -1, hintItem1, hintItem2;
	private String stopMessage;

	private Label titleLabel;
	private JButton resetButton;
	private JMenuItem exportItem, importItem, exitItem, cutItem, copyItem, selectAllItem;

	private Menu optionsMenu;

	private JScrollBar speedBar;
	private JScrollBar currentBar;
	private Label powerLabel;
	private Scrollbar powerBar;
	private JPopupMenu elementsPopUp;
	private JMenuItem elmEditMenuItem;
	private JMenuItem elmCutMenuItem;
	private JMenuItem elmCopyMenuItem;
	private JMenuItem elmDeleteMenuItem;
	private JMenuItem elmScopeMenuItem;
	public JPopupMenu scopeMenu;
	public JPopupMenu transScopeMenu;
	private JPopupMenu mainMenu;
	public JCheckBoxMenuItem scopeVMenuItem;
	public JCheckBoxMenuItem scopeIMenuItem;
	public JCheckBoxMenuItem scopeMaxMenuItem;
	public JCheckBoxMenuItem scopeMinMenuItem;
	public JCheckBoxMenuItem scopeFreqMenuItem;
	public JCheckBoxMenuItem scopePowerMenuItem;
	public JCheckBoxMenuItem scopeIbMenuItem;
	public JCheckBoxMenuItem scopeIcMenuItem;
	public JCheckBoxMenuItem scopeIeMenuItem;
	public JCheckBoxMenuItem scopeVbeMenuItem;
	public JCheckBoxMenuItem scopeVbcMenuItem;
	public JCheckBoxMenuItem scopeVceMenuItem;
	public JCheckBoxMenuItem scopeVIMenuItem;
	public JCheckBoxMenuItem scopeXYMenuItem;
	public JCheckBoxMenuItem scopeResistMenuItem;
	public JCheckBoxMenuItem scopeVceIcMenuItem;
	public JMenuItem scopeSelectYMenuItem;
	private Class<?> addingClass;

	public JButton playButton;
	public JButton stopButton;

	private final MouseMotionListener mouseMotionList;
	private final MouseListener mouseList;
	private final KeyListener keyList;

	public final Timer timer;

	public final Circuit circuit;

	public final CircuitMouseManager mouseMan;
	public final ScopeManager scopeMan;
	public final CircuitManager circuitMan;

	public final CircuitPane circuitPanel;

	public final Clipboard clipboard;

	public final CoreWindow cirFrame;
	public final JPanel mainContainer;
	public final JToolBar toolBar;

	public final ActivityManager activityManager;
	private final ActivityListener activityListener;

	private RepaintRun repaintRun = null;

	private CircuitSimulator()
	{
		super();

		this.clipboard = new Clipboard();

		// this.mainContainer.setLayout(new BorderLayout());
		this.circuitPanel = new CircuitPane(this);

		this.circuitMan = new CircuitManager(this.circuitPanel);
		this.circuit = this.circuitMan.getCircuit();
		this.mouseMan = this.circuitMan.mouseMan;

		this.scopeMan = new ScopeManager(this.circuitMan.getCircuit());

		this.timer = new Timer();

		this.activityManager = new ActivityManager();
		this.activityListener = new ActivityList();
		this.activityManager.addActivityListener(this.activityListener);

		this.cirFrame = new CoreWindow();

		this.mainContainer = new JPanel();
		this.mainContainer.setBackground(Color.BLACK);
		this.mainContainer.setLayout(new BorderLayout());

		this.toolBar = new JToolBar();
		this.toolBar.setFloatable(false);

		this.mouseMotionList = new MyMouseMotionListener();
		this.mouseList = new MyMouseListener();
		this.keyList = new MyKeyListener();

		CircuitElm.initClass(this);

		// Gère les contrôles lier au mac.
		boolean isMac = CoreUtil.isMac();
		if (isMac)
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			this.ctrlMetaKey = "\u2318";
		}
		else
		{
			this.ctrlMetaKey = "Ctrl";
		}

		this.manageJavaVersion();

		// Add Listener
		this.circuitPanel.addComponentListener(this);

		this.circuitPanel.addMouseMotionListener(this.mouseMotionList);
		this.circuitPanel.addMouseListener(this.mouseList);
		this.circuitPanel.addKeyListener(this.keyList);

		JMenu circuitsMenu = this.buildMenuBar();

		this.buildPopUpMainMenu(isMac);

		this.initToolBar();

		this.setGrid();

		this.initPopupMenu();

		this.scopeMenu = this.buildScopeMenu(false);
		this.transScopeMenu = this.buildScopeMenu(true);

		this.fetchSetupList(circuitsMenu, false);

		this.initScreen();

		this.cirFrame.add(this.mainContainer, BorderLayout.CENTER);
		this.cirFrame.add(this.toolBar, BorderLayout.NORTH);

		this.mainContainer.add(this.circuitPanel, BorderLayout.CENTER);
		// this.mainContainer.add(this.scopeMan.getScopePane(),
		// BorderLayout.SOUTH);

	}

	private void start()
	{
		Runnable starter = new Starter();

		this.initStartCircuitText();

		try
		{
			SwingUtilities.invokeAndWait(starter);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}

		this.startDisplay();
	}

	private void startDisplay()
	{
		if (this.repaintRun != null)
		{
			this.repaintRun.goOn = false;
		}

		this.repaintRun = new RepaintRun();
		Thread thread = new Thread(repaintRun);
		thread.start();
	}

	private long repaint()
	{
		if (!(this.winSize == null || this.winSize.width == 0))
		{

			this.prepareRepaint();

			this.scopeMan.setupScopes(this.winSize);

			this.circuitMan.repaint();

		}

		return this.timer.calculateDelay();
	}

	private void prepareRepaint()
	{
		// Analyser le circuit si nessaire.
		if (this.circuit.needAnalysis())
		{
			try
			{
				this.stopMessage = null;
				this.stopElm = null;
				this.circuit.analyzeCircuit();
			}
			catch (CircuitAnalysisException e)
			{
				this.handleAnalysisException(e);
			}

			this.circuit.setNeedAnalysis(false);
		}

		if (CircuitSimulator.editDialog != null && CircuitSimulator.editDialog.elm instanceof CircuitElm)
		{
			this.mouseMan.mouseElm = (CircuitElm) CircuitSimulator.editDialog.elm;
		}

		if (this.mouseMan.mouseElm == null)
		{
			this.mouseMan.mouseElm = this.stopElm;
		}

	}

	private void drawCurrent()
	{
		long sysTime = System.currentTimeMillis();

		if (this.timer.lastTime != 0)
		{

			int inc = (int) (sysTime - this.timer.lastTime);
			double c = Configs.CURRENT_SPEED;
			c = Math.exp(c / 3.5 - 14.2);
			this.currentMultiplier = 1.7 * inc * c;
			if (!Configs.CONVENTIONAL_CURRENT)
			{
				this.currentMultiplier = -this.currentMultiplier;
			}
		}

		if (sysTime - this.timer.secTime >= 1000)
		{
			this.timer.secTime = sysTime;
		}

		this.timer.lastTime = sysTime;
	}

	private void drawElements(Graphics g)
	{

		for (int i = 0; i != this.circuit.getElementCount(); i++)
		{
			if (Configs.SHOW_POWER)
			{
				g.setColor(Color.gray);
			}
			/*
			 * else if (conductanceCheckItem.getState())
			 * g.setColor(Color.white);
			 */
			this.circuit.getElementAt(i).draw(g);
		}
	}

	private void drawElementForMouse(Graphics g)
	{
		int i;

		if (this.mouseMan.tempMouseMode == CircuitSimulator.MODE_DRAG_ROW
				|| this.mouseMan.tempMouseMode == CircuitSimulator.MODE_DRAG_COLUMN
				|| this.mouseMan.tempMouseMode == CircuitSimulator.MODE_DRAG_POST
				|| this.mouseMan.tempMouseMode == CircuitSimulator.MODE_DRAG_SELECTED)
		{
			for (i = 0; i != this.circuit.getElementCount(); i++)
			{
				CircuitElm ce = this.circuit.getElementAt(i);
				DrawUtil.drawPost(g, ce.x, ce.y);
				DrawUtil.drawPost(g, ce.x2, ce.y2);
			}
		}
	}

	private void drawDrag(Graphics g)
	{
		/*
		 * if (mouseElm != null) { g.setFont(oldfont); g.drawString("+",
		 * mouseElm.x+10, mouseElm.y); }
		 */
		if (this.mouseMan.dragElm != null
				&& (this.mouseMan.dragElm.x != this.mouseMan.dragElm.x2 || this.mouseMan.dragElm.y != this.mouseMan.dragElm.y2))
		{
			this.mouseMan.dragElm.draw(g);
		}
	}

	private void calcPowerMult()
	{
		CircuitElm.powerMult = Math.exp(Configs.POWER_SPEED / 4.762 - 7);
	}

	public void createCircuitImage(Image image) throws Exception
	{
		Graphics g = image.getGraphics();

		g.setColor(Color.black);

		g.fillRect(0, 0, image.getWidth(this.cirFrame), image.getHeight(this.cirFrame));

		if (this.activityManager.isPlaying())
		{
			this.runCircuit();

			this.drawCurrent();
		}
		else
		{
			this.timer.reset();
		}

		this.calcPowerMult();

		this.drawElements(g);

		this.drawElementForMouse(g);

		int badnodes = this.circuitMan.findAndDrawBadNode(g);

		this.drawDrag(g);

		// Dessinage des scopes
		if (this.stopMessage == null)
		{
			this.scopeMan.drawScope(g);
		}

		this.drawStrings(g, badnodes);

		this.drawSelectedArea(g);
	}

	private void drawStrings(Graphics g, int badnodes)
	{
		g.setColor(CircuitElm.WHITE_COLOR);
		if (this.stopMessage != null)
		{
			g.drawString(this.stopMessage, 10, this.circuit.circuitArea.height);
		}
		else
		{
			if (this.circuit.circuitBottom == 0)
			{
				this.circuit.calcCircuitBottom();
			}
			String info[] = new String[10];
			if (this.mouseMan.mouseElm != null)
			{
				if (this.mouseMan.mousePost == -1)
				{
					this.mouseMan.mouseElm.getInfo(info);
				}
				else
				{
					info[0] = "V = "
							+ CoreUtil.getUnitText(this.mouseMan.mouseElm.getPostVoltage(this.mouseMan.mousePost), "V");
				}
			}
			else
			{
				CircuitElm.showFormat.setMinimumFractionDigits(2);
				info[0] = "t = " + CoreUtil.getUnitText(this.timer.time, "s");
				CircuitElm.showFormat.setMinimumFractionDigits(0);
			}

			int nbInfo;
			if (this.hintType != -1)
			{

				for (nbInfo = 0; info[nbInfo] != null; nbInfo++)
				{
					;
				}

				String s = this.getHint();
				if (s == null)
				{
					this.hintType = -1;
				}
				else
				{
					info[nbInfo] = s;
				}
			}
			int x = 0;

			int ct = this.scopeMan.scopeCount;

			if (this.stopMessage != null)
			{
				ct = 0;
			}

			if (ct != 0)
			{
				x = this.scopeMan.scopes[ct - 1].rightEdge() + 20;
			}

			x = Math.max(x, this.winSize.width * 2 / 3);

			// count lines of data
			for (nbInfo = 0; info[nbInfo] != null; nbInfo++)
			{
				;
			}

			if (badnodes > 0)
			{
				if (badnodes == 1)
				{
					info[nbInfo++] = badnodes + " bad connection";
				}
				else
				{
					info[nbInfo++] = badnodes + " bad connections";
				}
			}

			// find where to show data; below circuit, not too high unless we
			// need it
			int ybase = this.winSize.height - 15 * nbInfo - 5;
			ybase = Math.min(ybase, this.circuit.circuitArea.height);
			ybase = Math.max(ybase, this.circuit.circuitBottom);

			for (nbInfo = 0; info[nbInfo] != null; nbInfo++)
			{
				g.drawString(info[nbInfo], x, ybase + 15 * (nbInfo + 1));
			}

		}
	}

	private void drawSelectedArea(Graphics g)
	{
		if (this.selectedArea != null)
		{
			g.setColor(CircuitElm.SELECT_COLOR);
			g.drawRect(this.selectedArea.x, this.selectedArea.y, this.selectedArea.width, this.selectedArea.height);
		}
	}

	private void runCircuit() throws CircuitAnalysisException
	{
		if (this.circuit.isEmpty())
		{
			this.circuit.matrix.clear();
			return;
		}

		int iter;

		long steprate = (long) (160 * this.getIterCount());
		long presentTime = System.currentTimeMillis();
		long lit = this.timer.lastIterTime;

		if (1000 >= steprate * (presentTime - this.timer.lastIterTime))
		{
			return;
		}

		for (iter = 1;; iter++)
		{
			int i, j, k, subiter;
			for (i = 0; i != this.circuit.getElementCount(); i++)
			{
				CircuitElm ce = this.circuit.getElementAt(i);

				try
				{
					ce.startIteration();
				}
				catch (CircuitAnalysisException e)
				{
					this.handleAnalysisException(e);
				}

			}

			for (subiter = 0; subiter != CircuitSimulator.subiterCount; subiter++)
			{
				this.circuit.converged = true;
				this.subIterations = subiter;

				this.circuit.matrix.origRightToRight();

				if (this.circuit.isNonLinear())
				{
					this.circuit.matrix.recopyMatrix();
				}

				for (i = 0; i != this.circuit.getElementCount(); i++)
				{
					CircuitElm ce = this.circuit.getElementAt(i);

					try
					{
						ce.doStep();
					}
					catch (CircuitAnalysisException e)
					{
						this.handleAnalysisException(e);
					}
				}

				if (this.stopMessage != null)
				{
					return;
				}

				if (this.circuit.matrix.matrixIsInfiniteOrNAN())
				{
					throw new CircuitAnalysisException("nan/infinite matrix!");
				}

				if (this.circuit.isNonLinear())
				{
					if (this.circuit.converged && subiter > 0)
					{
						break;
					}

					if (!this.circuit.matrix.doLowUpFactor())
					{
						throw new CircuitAnalysisException("Singular matrix!");
					}
				}

				this.circuit.matrix.doLowUpSolve();

				for (j = 0; j != this.circuit.getMatrixFullSize(); j++)
				{
					MatrixRowInfo rowInfo = this.circuit.matrix.circuitRowInfo[j];
					double res = 0;
					if (rowInfo.type == MatrixRowInfo.ROW_CONST)
					{
						res = rowInfo.value;
					}
					else
					{
						res = this.circuit.matrix.getRightSide(rowInfo.mapCol);
					}
					/*
					 * System.out.println(j + " " + res + " " + ri.type + " " +
					 * ri.mapCol);
					 */
					if (Double.isNaN(res))
					{
						this.circuit.converged = false;
						// debugprint = true;
						break;
					}
					if (j < this.circuit.getNodeCount() - 1)
					{
						CircuitNode cn = this.circuit.getNodeAt(j + 1);
						for (k = 0; k != cn.getSize(); k++)
						{
							CircuitNodeLink cnl = cn.elementAt(k);
							cnl.elm.setNodeVoltage(cnl.num, res);
						}
					}
					else
					{
						int ji = j - (this.circuit.getNodeCount() - 1);
						// System.out.println("setting vsrc " + ji + " to " +
						// res);
						this.circuit.voltageSources[ji].setCurrent(ji, res);
					}
				}
				if (!this.circuit.isNonLinear())
				{
					break;
				}
			}

			if (subiter > 5)
			{
				System.out.print("converged after " + subiter + " iterations\n");
			}

			if (subiter == CircuitSimulator.subiterCount)
			{
				this.stop("Convergence failed!", null);
				break;
			}

			this.timer.doTimeStep();

			this.scopeMan.doTimeStep();

			presentTime = System.currentTimeMillis();
			lit = presentTime;

			if (iter * 1000 >= steprate * (presentTime - this.timer.lastIterTime)
					|| presentTime - this.timer.getLastFrameTime() > 500)
			{
				break;
			}
		}

		this.timer.lastIterTime = lit;
		// System.out.println((System.currentTimeMillis()-lastFrameTime)/(double)
		// iter);
	}

	private void manageJavaVersion()
	{
		String jv = System.getProperty("java.class.version");
		double jvf = new Double(jv).doubleValue();

		if (jvf >= 48)
		{
			System.out.println(jv);
			CircuitSimulator.muString = "\u03bc";
			CircuitSimulator.ohmString = "\u03a9";
			this.useBufferedImage = true;
		}
	}

	private void initScreen()
	{
		Dimension screen = this.cirFrame.getToolkit().getScreenSize();

		Dimension x = this.cirFrame.getSize();
		this.cirFrame.setLocation((screen.width - x.width) / 2, (screen.height - x.height) / 2);
	}

	private void initStartCircuitText()
	{
		if (this.startCircuitText != null)
		{
			this.readSetup(this.startCircuitText);
		}
		else if (this.stopMessage == null && this.startCircuit != null)
		{
			this.readSetupFile(this.startCircuit, this.startLabel);
		}
	}

	private void initPopupMenu()
	{
		this.elementsPopUp = new JPopupMenu();
		this.elmEditMenuItem = this.getMenuItem("Edit");
		this.elementsPopUp.add(this.elmEditMenuItem);
		this.elementsPopUp.add(this.elmScopeMenuItem = this.getMenuItem("View in Scope"));
		this.elementsPopUp.add(this.elmCutMenuItem = this.getMenuItem("Cut"));
		this.elementsPopUp.add(this.elmCopyMenuItem = this.getMenuItem("Copy"));
		this.elementsPopUp.add(this.elmDeleteMenuItem = this.getMenuItem("Delete"));

		this.circuitPanel.add(this.elementsPopUp);
	}

	private void initToolBar()
	{
		this.toolBar.add(new ResetAction());
		this.toolBar.addSeparator();

		this.playButton = new JButton(this.activityManager.getPlayAction());
		this.toolBar.add(this.playButton);

		this.stopButton = new JButton(this.activityManager.getStopAction());
		this.toolBar.add(this.stopButton);
		this.toolBar.addSeparator();

		this.toolBar.add(new JLabel("Speed", SwingConstants.CENTER));
		this.speedBar = new JScrollBar(Adjustable.HORIZONTAL, 3, 1, 0, 260);

		this.toolBar.add(this.speedBar);

		// this.toolBar.add(new JLabel("Current Speed", JLabel.CENTER));
		// this.currentBar.addAdjustmentListener(this);
		// this.toolBar.add(this.currentBar);

		this.powerLabel = new Label("Power Brightness", Label.CENTER);
		this.powerLabel.setEnabled(false);
		// this.toolBar.add(this.powerLabel);

		// this.toolBar.add(new Label("www.falstad.com"));
	}

	private JMenu buildMenuBar()
	{
		// Artéfacte de la version Falstad.

		JMenuBar menubar = null;

		menubar = new JMenuBar();
		
		JMenu menu = null;
		
		menu = new JMenu("File");
		menu.add(new MigrationAction());
		menu.addSeparator();
		menu.add(new ExitAction());
		menubar.add(menu);

		menu = new JMenu("Edit");

		this.clipboard.undoItem = this.getMenuItem("Undo");
		this.clipboard.undoItem.setAccelerator(KeyStroke.getKeyStroke('Z'));
		menu.add(this.clipboard.undoItem);

		this.clipboard.redoItem = this.getMenuItem("Redo");
		menu.add(this.clipboard.redoItem);

		menu.addSeparator();

		this.cutItem = this.getMenuItem("Cut");

		this.cutItem.setAccelerator(KeyStroke.getKeyStroke('X'));
		menu.add(this.cutItem);

		this.copyItem = this.getMenuItem("Copy");
		this.copyItem.setAccelerator(KeyStroke.getKeyStroke('C'));
		menu.add(this.copyItem);

		this.clipboard.pasteItem = this.getMenuItem("Paste");
		this.clipboard.pasteItem.setAccelerator(KeyStroke.getKeyStroke('V'));
		this.clipboard.pasteItem.setEnabled(false);
		menu.add(this.clipboard.pasteItem);

		this.selectAllItem = this.getMenuItem("Select All");
		this.selectAllItem.setAccelerator(KeyStroke.getKeyStroke('A'));
		menu.add(this.selectAllItem);

		menubar.add(menu);

		menu = new JMenu("Scope");

		menubar.add(menu);

		menu.add(this.getMenuItem("Stack All", "stackAll"));
		menu.add(this.getMenuItem("Unstack All", "unstackAll"));

		JMenu circuitsMenu = new JMenu("Circuits");
		menubar.add(circuitsMenu);

		this.cirFrame.setJMenuBar(menubar);

		return circuitsMenu;
	}

	private void buildPopUpMainMenu(boolean isMac)
	{
		this.mainMenu = new JPopupMenu();
		this.mainMenu.add(this.getClassCheckItem("Add Wire", "WireElm"));
		this.mainMenu.add(this.getClassCheckItem("Add Resistor", "ResistorElm"));

		this.buildPassiveCompMenu();

		this.buildIOMenu();

		this.buildActiveCompMenu();

		this.buildGateMenu();

		this.buildChipMenu();

		JMenu otherMenu = new JMenu("Other");
		this.mainMenu.add(otherMenu);
		otherMenu.add(this.getClassCheckItem("Add Text", "TextElm"));
		otherMenu.add(this.getClassCheckItem("Add Scope Probe", "ProbeElm"));
		otherMenu.add(this.getCheckItem("Drag All (Alt-drag)", "DragAll"));

		otherMenu.add(this.getCheckItem(isMac ? "Drag Row (Alt-S-drag, S-right)" : "Drag Row (S-right)", "DragRow"));
		otherMenu.add(this.getCheckItem(
				isMac ? "Drag Column (Alt-\u2318-drag, \u2318-right)" : "Drag Column (C-right)", "DragColumn"));

		otherMenu.add(this.getCheckItem("Drag Selected", "DragSelected"));
		otherMenu.add(this.getCheckItem("Drag Post (" + this.ctrlMetaKey + "-drag)", "DragPost"));

		this.mainMenu.add(this.getCheckItem("Select/Drag Selected (space or Shift-drag)", "Select"));

		this.circuitPanel.add(this.mainMenu);
	}

	private void buildGateMenu()
	{
		JMenu gateMenu = new JMenu("Logic Gates");
		this.mainMenu.add(gateMenu);
		gateMenu.add(this.getClassCheckItem("Add Inverter", "InverterElm"));
		gateMenu.add(this.getClassCheckItem("Add NAND Gate", "NandGateElm"));
		gateMenu.add(this.getClassCheckItem("Add NOR Gate", "NorGateElm"));
		gateMenu.add(this.getClassCheckItem("Add AND Gate", "AndGateElm"));
		gateMenu.add(this.getClassCheckItem("Add OR Gate", "OrGateElm"));
		gateMenu.add(this.getClassCheckItem("Add XOR Gate", "XorGateElm"));
	}

	private JPopupMenu buildScopeMenu(boolean t)
	{
		JPopupMenu scopePopUp;
		scopePopUp = new JPopupMenu();
		scopePopUp.add(this.getMenuItem("Remove", "remove"));
		scopePopUp.add(this.getMenuItem("Speed 2x", "speed2"));
		scopePopUp.add(this.getMenuItem("Speed 1/2x", "speed1/2"));
		scopePopUp.add(this.getMenuItem("Scale 2x", "scale"));
		scopePopUp.add(this.getMenuItem("Max Scale", "maxscale"));
		scopePopUp.add(this.getMenuItem("Stack", "stack"));
		scopePopUp.add(this.getMenuItem("Unstack", "unstack"));
		scopePopUp.add(this.getMenuItem("Reset", "reset"));
		if (t)
		{
			scopePopUp.add(this.scopeIbMenuItem = this.getCheckItem("Show Ib"));
			scopePopUp.add(this.scopeIcMenuItem = this.getCheckItem("Show Ic"));
			scopePopUp.add(this.scopeIeMenuItem = this.getCheckItem("Show Ie"));
			scopePopUp.add(this.scopeVbeMenuItem = this.getCheckItem("Show Vbe"));
			scopePopUp.add(this.scopeVbcMenuItem = this.getCheckItem("Show Vbc"));
			scopePopUp.add(this.scopeVceMenuItem = this.getCheckItem("Show Vce"));
			scopePopUp.add(this.scopeVceIcMenuItem = this.getCheckItem("Show Vce vs Ic"));
		}
		else
		{
			scopePopUp.add(this.scopeVMenuItem = this.getCheckItem("Show Voltage"));
			scopePopUp.add(this.scopeIMenuItem = this.getCheckItem("Show Current"));
			scopePopUp.add(this.scopePowerMenuItem = this.getCheckItem("Show Power Consumed"));
			scopePopUp.add(this.scopeMaxMenuItem = this.getCheckItem("Show Peak Value"));
			scopePopUp.add(this.scopeMinMenuItem = this.getCheckItem("Show Negative Peak Value"));
			scopePopUp.add(this.scopeFreqMenuItem = this.getCheckItem("Show Frequency"));
			scopePopUp.add(this.scopeVIMenuItem = this.getCheckItem("Show V vs I"));
			scopePopUp.add(this.scopeXYMenuItem = this.getCheckItem("Plot X/Y"));
			scopePopUp.add(this.scopeSelectYMenuItem = this.getMenuItem("Select Y", "selecty"));
			scopePopUp.add(this.scopeResistMenuItem = this.getCheckItem("Show Resistance"));
		}

		this.circuitPanel.add(scopePopUp);
		return scopePopUp;
	}

	private void buildIOMenu()
	{
		JMenu inputMenu = new JMenu("Inputs/Outputs");
		this.mainMenu.add(inputMenu);
		inputMenu.add(this.getClassCheckItem("Add Ground", "GroundElm"));
		inputMenu.add(this.getClassCheckItem("Add Voltage Source (2-terminal)", "DCVoltageElm"));
		inputMenu.add(this.getClassCheckItem("Add A/C Source (2-terminal)", "ACVoltageElm"));
		inputMenu.add(this.getClassCheckItem("Add Voltage Source (1-terminal)", "RailElm"));
		inputMenu.add(this.getClassCheckItem("Add A/C Source (1-terminal)", "ACRailElm"));
		inputMenu.add(this.getClassCheckItem("Add Square Wave (1-terminal)", "SquareRailElm"));
		inputMenu.add(this.getClassCheckItem("Add Analog Output", "OutputElm"));
		inputMenu.add(this.getClassCheckItem("Add Logic Input", "LogicInputElm"));
		inputMenu.add(this.getClassCheckItem("Add Logic Output", "LogicOutputElm"));
		inputMenu.add(this.getClassCheckItem("Add Clock", "ClockElm"));
		inputMenu.add(this.getClassCheckItem("Add A/C Sweep", "SweepElm"));
		inputMenu.add(this.getClassCheckItem("Add Var. Voltage", "VarRailElm"));
		inputMenu.add(this.getClassCheckItem("Add Antenna", "AntennaElm"));
		inputMenu.add(this.getClassCheckItem("Add Current Source", "CurrentElm"));
		inputMenu.add(this.getClassCheckItem("Add LED", "LEDElm"));
		inputMenu.add(this.getClassCheckItem("Add Lamp (beta)", "LampElm"));
	}

	private void buildPassiveCompMenu()
	{
		JMenu passMenu = new JMenu("Passive Components");
		this.mainMenu.add(passMenu);
		passMenu.add(this.getClassCheckItem("Add Capacitor", "CapacitorElm"));
		passMenu.add(this.getClassCheckItem("Add Inductor", "InductorElm"));
		passMenu.add(this.getClassCheckItem("Add Switch", "SwitchElm"));
		passMenu.add(this.getClassCheckItem("Add Push Switch", "PushSwitchElm"));
		passMenu.add(this.getClassCheckItem("Add SPDT Switch", "Switch2Elm"));
		passMenu.add(this.getClassCheckItem("Add Potentiometer", "PotElm"));
		passMenu.add(this.getClassCheckItem("Add Transformer", "TransformerElm"));
		passMenu.add(this.getClassCheckItem("Add Tapped Transformer", "TappedTransformerElm"));
		passMenu.add(this.getClassCheckItem("Add Transmission Line", "TransLineElm"));
		passMenu.add(this.getClassCheckItem("Add Relay", "RelayElm"));
		passMenu.add(this.getClassCheckItem("Add Memristor", "MemristorElm"));
		passMenu.add(this.getClassCheckItem("Add Spark Gap", "SparkGapElm"));
	}

	private void buildChipMenu()
	{
		JMenu chipMenu = new JMenu("Chips");
		this.mainMenu.add(chipMenu);
		chipMenu.add(this.getClassCheckItem("Add D Flip-Flop", "DFlipFlopElm"));
		chipMenu.add(this.getClassCheckItem("Add JK Flip-Flop", "JKFlipFlopElm"));
		chipMenu.add(this.getClassCheckItem("Add 7 Segment LED", "SevenSegElm"));
		chipMenu.add(this.getClassCheckItem("Add VCO", "VCOElm"));
		chipMenu.add(this.getClassCheckItem("Add Phase Comparator", "PhaseCompElm"));
		chipMenu.add(this.getClassCheckItem("Add Counter", "CounterElm"));
		chipMenu.add(this.getClassCheckItem("Add Decade Counter", "DecadeElm"));
		chipMenu.add(this.getClassCheckItem("Add 555 Timer", "TimerElm"));
		chipMenu.add(this.getClassCheckItem("Add DAC", "DACElm"));
		chipMenu.add(this.getClassCheckItem("Add ADC", "ADCElm"));
		chipMenu.add(this.getClassCheckItem("Add Latch", "LatchElm"));
	}

	private void buildActiveCompMenu()
	{
		JMenu activeMenu = new JMenu("Active Components");
		this.mainMenu.add(activeMenu);
		activeMenu.add(this.getClassCheckItem("Add Diode", "DiodeElm"));
		activeMenu.add(this.getClassCheckItem("Add Zener Diode", "ZenerElm"));
		activeMenu.add(this.getClassCheckItem("Add Transistor (bipolar, NPN)", "NTransistorElm"));
		activeMenu.add(this.getClassCheckItem("Add Transistor (bipolar, PNP)", "PTransistorElm"));
		activeMenu.add(this.getClassCheckItem("Add Op Amp (- on top)", "OpAmpElm"));
		activeMenu.add(this.getClassCheckItem("Add Op Amp (+ on top)", "OpAmpSwapElm"));
		activeMenu.add(this.getClassCheckItem("Add MOSFET (n-channel)", "NMosfetElm"));
		activeMenu.add(this.getClassCheckItem("Add MOSFET (p-channel)", "PMosfetElm"));
		activeMenu.add(this.getClassCheckItem("Add JFET (n-channel)", "NJfetElm"));
		activeMenu.add(this.getClassCheckItem("Add JFET (p-channel)", "PJfetElm"));
		activeMenu.add(this.getClassCheckItem("Add Analog Switch (SPST)", "AnalogSwitchElm"));
		activeMenu.add(this.getClassCheckItem("Add Analog Switch (SPDT)", "AnalogSwitch2Elm"));
		activeMenu.add(this.getClassCheckItem("Add SCR", "SCRElm"));
		// activeMenu.add(getClassCheckItem("Add Varactor/Varicap",
		// "VaractorElm"));
		activeMenu.add(this.getClassCheckItem("Add Tunnel Diode", "TunnelDiodeElm"));
		activeMenu.add(this.getClassCheckItem("Add Triode", "TriodeElm"));
		// activeMenu.add(getClassCheckItem("Add Diac", "DiacElm"));
		// activeMenu.add(getClassCheckItem("Add Triac", "TriacElm"));
		// activeMenu.add(getClassCheckItem("Add Photoresistor",
		// "PhotoResistorElm"));
		// activeMenu.add(getClassCheckItem("Add Thermistor", "ThermistorElm"));
		activeMenu.add(this.getClassCheckItem("Add CCII+", "CC2Elm"));
		activeMenu.add(this.getClassCheckItem("Add CCII-", "CC2NegElm"));
	}

	private JMenuItem getMenuItem(String s)
	{
		JMenuItem mi = new JMenuItem(s);
		mi.addActionListener(this);
		return mi;
	}

	private JMenuItem getMenuItem(String s, String ac)
	{
		JMenuItem mi = new JMenuItem(s);
		mi.setActionCommand(ac);
		mi.addActionListener(this);
		return mi;
	}

	private JCheckBoxMenuItem getCheckItem(String s)
	{
		JCheckBoxMenuItem mi = new JCheckBoxMenuItem(s);
		mi.addItemListener(this);
		mi.setActionCommand("");
		return mi;
	}

	/**
	 * Fait un ckeckItem a partir du nom d'une class
	 * 
	 * @param label L'étiquette
	 * @param className Le nom cannonique de la classe.
	 * 
	 * @return un item de menu.
	 */
	private JCheckBoxMenuItem getClassCheckItem(String label, String className)
	{
		int dt = 0;
		Class<?> classPath = null;
		CircuitElm element = null;

		try
		{
			classPath = Class.forName("com.limoilux.circuit." + className);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		element = CircuitUtil.constructElement(classPath, 0, 0);

		this.register(classPath, element);

		if (element.needsShortcut() && element.getDumpClass() == classPath)
		{
			dt = element.getElementId();
			label += " (" + (char) dt + ")";
		}

		element.delete();

		return this.getCheckItem(label, className);
	}

	private JCheckBoxMenuItem getCheckItem(String s, String t)
	{
		JCheckBoxMenuItem mi = new JCheckBoxMenuItem(s);
		mi.addItemListener(this);
		mi.setActionCommand(t);
		return mi;
	}

	private void register(Class<?> c, CircuitElm elm)
	{
		Class<?> dumpClass = null;
		int elementId = elm.getElementId();

		if (elementId == 0)
		{
			System.out.println("no dump type: " + c);
			return;
		}

		dumpClass = elm.getDumpClass();
		if (this.circuitMan.dumpMan.dumpTypes[elementId] == dumpClass)
		{
			return;
		}

		if (this.circuitMan.dumpMan.dumpTypes[elementId] != null)
		{
			System.out.println("dump type conflict: " + c + " " + this.circuitMan.dumpMan.dumpTypes[elementId]);
			return;
		}

		this.circuitMan.dumpMan.dumpTypes[elementId] = dumpClass;
	}

	public String getAppletInfo()
	{
		return "Circuit by Paul Falstad";
	}

	private void handleResize()
	{
		Dimension dim = this.circuitPanel.getSize();
		this.winSize = dim;

		if (dim.width != 0)
		{
			this.circuitPanel.buildDBImage();

			int height = dim.height / 5;

			// if (h < 128 && winSize.height > 300) h = 128;

			this.circuit.circuitArea.setBounds(0, 0, dim.width, dim.height - height);

			this.circuit.centerCircuit(this.gridMask, this.circuit.circuitArea);

			this.circuit.setCircuitBottom(0);
		}
	}

	private String getHint()
	{
		CircuitElm c1 = this.circuit.getElementAt(this.hintItem1);
		CircuitElm c2 = this.circuit.getElementAt(this.hintItem2);
		if (c1 == null || c2 == null)
		{
			return null;
		}
		if (this.hintType == CircuitSimulator.HINT_LC)
		{
			if (!(c1 instanceof InductorElm))
			{
				return null;
			}
			if (!(c2 instanceof CapacitorElm))
			{
				return null;
			}
			InductorElm ie = (InductorElm) c1;
			CapacitorElm ce = (CapacitorElm) c2;
			return "res.f = "
					+ CoreUtil.getUnitText(1 / (2 * CircuitSimulator.PI * Math.sqrt(ie.inductance * ce.capacitance)),
							"Hz");
		}
		if (this.hintType == CircuitSimulator.HINT_RC)
		{
			if (!(c1 instanceof ResistorElm))
			{
				return null;
			}
			if (!(c2 instanceof CapacitorElm))
			{
				return null;
			}
			ResistorElm re = (ResistorElm) c1;
			CapacitorElm ce = (CapacitorElm) c2;
			return "RC = " + CoreUtil.getUnitText(re.resistance * ce.capacitance, "s");
		}
		if (this.hintType == CircuitSimulator.HINT_3DB_C)
		{
			if (!(c1 instanceof ResistorElm))
			{
				return null;
			}
			if (!(c2 instanceof CapacitorElm))
			{
				return null;
			}
			ResistorElm re = (ResistorElm) c1;
			CapacitorElm ce = (CapacitorElm) c2;
			return "f.3db = "
					+ CoreUtil.getUnitText(1 / (2 * CircuitSimulator.PI * re.resistance * ce.capacitance), "Hz");
		}
		if (this.hintType == CircuitSimulator.HINT_3DB_L)
		{
			if (!(c1 instanceof ResistorElm))
			{
				return null;
			}
			if (!(c2 instanceof InductorElm))
			{
				return null;
			}
			ResistorElm re = (ResistorElm) c1;
			InductorElm ie = (InductorElm) c2;
			return "f.3db = " + CoreUtil.getUnitText(re.resistance / (2 * CircuitSimulator.PI * ie.inductance), "Hz");
		}
		if (this.hintType == CircuitSimulator.HINT_TWINT)
		{
			if (!(c1 instanceof ResistorElm))
			{
				return null;
			}
			if (!(c2 instanceof CapacitorElm))
			{
				return null;
			}
			ResistorElm re = (ResistorElm) c1;
			CapacitorElm ce = (CapacitorElm) c2;
			return "fc = " + CoreUtil.getUnitText(1 / (2 * CircuitSimulator.PI * re.resistance * ce.capacitance), "Hz");
		}
		return null;
	}

	public void toggleSwitch(int n)
	{
		for (int i = 0; i != this.circuit.getElementCount(); i++)
		{
			CircuitElm ce = this.circuit.getElementAt(i);
			if (ce instanceof SwitchElm)
			{
				n--;
				if (n == 0)
				{
					((SwitchElm) ce).toggle();
					this.circuit.setNeedAnalysis(true);
					return;
				}
			}
		}
	}

	/**
	 * @deprecated Replace with {@link Circuit#setNeedAnalysis(boolean)} and
	 *             repaint() instructions
	 */
	@Deprecated
	public void needAnalyze()
	{
		this.circuit.setNeedAnalysis(true);
	}

	@Deprecated
	public CircuitNode getCircuitNode(int n)
	{
		return this.circuit.getNodeAt(n);
	}

	@Deprecated
	public CircuitElm getElement(int n)
	{
		return this.circuit.getElementAt(n);
	}

	@Deprecated
	private void stop(String msg, CircuitElm ce)
	{
		this.handleAnalysisException(new CircuitAnalysisException(msg, ce));
	}

	public void handleAnalysisException(CircuitAnalysisException e)
	{
		this.circuit.matrix.clear();

		this.stopMessage = e.getTechnicalMessage();
		this.stopElm = e.getCauseElement();

		this.activityManager.setPlaying(false);

		this.circuit.setNeedAnalysis(false);
	}

	@Deprecated
	public void stampVCCurrentSource(int cn1, int cn2, int vn1, int vn2, double g)
	{
		this.circuit.stampVCCurrentSource(cn1, cn2, vn1, vn2, g);
	}

	@Deprecated
	public void stampCurrentSource(int n1, int n2, double i)
	{
		this.circuit.stampCurrentSource(n1, n2, i);
	}

	@Deprecated
	public void stampCCCS(int n1, int n2, int vs, double gain)
	{
		this.circuit.stampCCCS(n1, n2, vs, gain);
	}

	@Deprecated
	public void stampMatrix(int i, int j, double x)
	{
		this.circuit.stampMatrix(i, j, x);
	}

	@Deprecated
	public void stampRightSide(int i, double x)
	{
		this.circuit.stampRightSide(i, x);
	}

	@Deprecated
	public void stampRightSide(int i)
	{
		this.circuit.stampRightSide(i);
	}

	@Deprecated
	public void stampNonLinear(int i)
	{
		this.circuit.stampNonLinear(i);
	}

	private double getIterCount()
	{
		if (this.speedBar.getValue() == 0)
		{
			return 0;
		}
		// return (Math.exp((speedBar.getValue()-1)/24.) + .5);
		return 0.1 * Math.exp((this.speedBar.getValue() - 61) / 24.);
	}

	@Deprecated
	private void stackScope(int s)
	{
		this.scopeMan.stackScope(s);
	}

	@Deprecated
	private void unstackScope(int s)
	{
		this.scopeMan.unstackScope(s);
	}

	private void doEdit(Editable eable)
	{
		this.circuit.clearSelection();
		this.pushUndo();
		if (CircuitSimulator.editDialog != null)
		{
			this.cirFrame.requestFocus();
			CircuitSimulator.editDialog.setVisible(false);
			CircuitSimulator.editDialog = null;
		}
		CircuitSimulator.editDialog = new EditDialog(eable, this);
		CircuitSimulator.editDialog.setVisible(true);
	}

	/**
	 * Montre un dialog de migration.
	 */
	private void showMigrationDialog()
	{
		String dump = this.dumpCircuit();
		MigrationWizard dialog = new MigrationWizard(this.cirFrame, dump, this.winSize);

		// Appel bloquand du wizard.
		dialog.setVisible(true);

		if (dialog.isImport())
		{
			dump = dialog.getContent();
			this.readSetup(dump);
		}

		dialog.dispose();

		// ????
		this.pushUndo();
	}

	private String dumpCircuit()
	{
		String dump = "";

		int f = 0;

		// f = this.dotsCheckItem.getState() ? 1 : 0;
		if (Configs.SHOW_DOTS)
		{
			f = 1;
		}

		// f |= this.smallGridCheckItem.getState() ? 2 : 0;
		if (Configs.SMALL_GRID)
		{
			f |= 2;
		}

		// f |= this.voltsCheckItem.getState() ? 0 : 4;
		if (!Configs.SHOW_VOLTAGE)
		{
			f |= 4;
		}

		// f |= this.powerCheckItem.getState() ? 8 : 0;
		if (Configs.SHOW_POWER)
		{
			f |= 8;
		}

		// f |= this.showValuesCheckItem.getState() ? 0 : 16;
		if (Configs.SHOW_VALUES)
		{
			f |= 16;
		}

		// 32 = linear scale in afilter
		// Construire le String de dump
		dump = "$ ";

		dump += f + " ";
		dump += Configs.TIME_STEP + " ";
		dump += this.getIterCount() + " ";
		dump += Configs.CURRENT_SPEED + " ";
		dump += CircuitElm.voltageRange + " ";
		dump += Configs.POWER_SPEED;

		dump += "\n";

		dump += this.circuit.createDump();

		dump += this.scopeMan.createDump();

		if (this.hintType != -1)
		{
			dump += "h ";
			dump += this.hintType + " ";
			dump += this.hintItem1 + " ";
			dump += this.hintItem2;
			dump += "\n";
		}

		return dump;
	}

	private void fetchSetupList(JMenu menu, boolean retry)
	{
		JMenu stack[] = new JMenu[6];
		int stackptr = 0;
		stack[stackptr++] = menu;
		try
		{
			URL url = new URL(CoreUtil.getCodeBase() + "setuplist.txt");
			ByteArrayOutputStream ba = CoreUtil.readUrlData(url);
			byte b[] = ba.toByteArray();
			int len = ba.size();
			int p;
			if (len == 0 || b[0] != '#')
			{
				// got a redirect, try again
				this.fetchSetupList(menu, true);
				return;
			}
			for (p = 0; p < len;)
			{
				int l;
				for (l = 0; l != len - p; l++)
				{
					if (b[l + p] == '\n')
					{
						l++;
						break;
					}
				}
				String line = new String(b, p, l - 1);
				if (line.charAt(0) == '#')
				{
					;
				}
				else if (line.charAt(0) == '+')
				{
					JMenu n = new JMenu(line.substring(1));
					menu.add(n);
					menu = stack[stackptr++] = n;
				}
				else if (line.charAt(0) == '-')
				{
					menu = stack[--stackptr - 1];
				}
				else
				{
					int i = line.indexOf(' ');
					if (i > 0)
					{
						String title = line.substring(i + 1);
						boolean first = false;
						if (line.charAt(0) == '>')
						{
							first = true;
						}
						String file = line.substring(first ? 1 : 0, i);
						menu.add(this.getMenuItem(title, "setup " + file));
						if (first && this.startCircuit == null)
						{
							this.startCircuit = file;
							this.startLabel = title;
						}
					}
				}
				p += l;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();

			this.stop("Can't read setuplist.txt!", null);
		}
	}

	public void readSetup(String text)
	{
		this.readSetup(text, false);
	}

	private void readSetup(String text, boolean retain)
	{
		this.readSetup(text.getBytes(), text.length(), retain);
		this.titleLabel.setText("untitled");
	}

	private void readSetupFile(String str, String title)
	{
		this.timer.time = 0;

		URL url;
		try
		{
			url = new URL(CoreUtil.getCodeBase() + "circuits/" + str);
			ByteArrayOutputStream ba = CoreUtil.readUrlData(url);

			this.readSetup(ba.toByteArray(), ba.size(), false);
			System.out.println("CirSim, file loaded: " + str);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			this.stop("Unable to read " + str + "!", null);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			this.stop("Unable to read " + str + "!", null);
		}

		this.cirFrame.setTitle(title);
	}

	private void readSetup(byte b[], int len, boolean retain)
	{

		if (!retain)
		{
			for (int i = 0; i != this.circuit.getElementCount(); i++)
			{
				CircuitElm ce = this.circuit.getElementAt(i);
				ce.delete();
			}

			this.circuit.removeAllElements();
			this.hintType = -1;

			this.setGrid();

			this.speedBar.setValue(Configs.DEF_SPEED);

			this.scopeMan.scopeCount = 0;
		}

		int p;
		for (p = 0; p < len;)
		{
			int l;
			int linelen = 0;
			for (l = 0; l != len - p; l++)
			{
				if (b[l + p] == '\n' || b[l + p] == '\r')
				{
					linelen = l++;
					if (l + p < b.length && b[l + p] == '\n')
					{
						l++;
					}
					break;
				}
			}
			String line = new String(b, p, linelen);
			StringTokenizer st = new StringTokenizer(line);
			while (st.hasMoreTokens())
			{
				String type = st.nextToken();
				int tint = type.charAt(0);
				try
				{
					if (tint == 'o')
					{
						Scope sc = new Scope(this);
						sc.position = this.scopeMan.scopeCount;
						sc.undump(st);
						this.scopeMan.scopes[this.scopeMan.scopeCount++] = sc;
						break;
					}
					if (tint == 'h')
					{
						this.readHint(st);
						break;
					}
					if (tint == '$')
					{
						this.readOptions(st);
						break;
					}
					if (tint == '%' || tint == '?' || tint == 'B')
					{
						// ignore afilter-specific stuff
						break;
					}

					if (tint >= '0' && tint <= '9')
					{
						tint = new Integer(type).intValue();
					}

					int x1 = new Integer(st.nextToken()).intValue();
					int y1 = new Integer(st.nextToken()).intValue();
					int x2 = new Integer(st.nextToken()).intValue();
					int y2 = new Integer(st.nextToken()).intValue();
					int f = new Integer(st.nextToken()).intValue();

					CircuitElm ce = null;
					Class<?> cls = this.circuitMan.dumpMan.dumpTypes[tint];

					if (cls == null)
					{
						System.out.println("unrecognized dump type: " + type);
						break;
					}
					// find element class
					Class<?> carr[] = new Class[6];
					// carr[0] = getClass();
					carr[0] = carr[1] = carr[2] = carr[3] = carr[4] = int.class;
					carr[5] = StringTokenizer.class;
					Constructor<?> cstr = null;
					cstr = cls.getConstructor(carr);

					// invoke constructor with starting coordinates
					Object oarr[] = new Object[6];
					// oarr[0] = this;
					oarr[0] = new Integer(x1);
					oarr[1] = new Integer(y1);
					oarr[2] = new Integer(x2);
					oarr[3] = new Integer(y2);
					oarr[4] = new Integer(f);
					oarr[5] = st;
					ce = (CircuitElm) cstr.newInstance(oarr);
					ce.setPoints();
					this.circuit.addElement(ce);
				}
				catch (java.lang.reflect.InvocationTargetException ee)
				{
					ee.getTargetException().printStackTrace();
					break;
				}
				catch (Exception ee)
				{
					ee.printStackTrace();
					break;
				}
				break;
			}
			p += l;

		}

		this.enableItems();
		if (!retain)
		{
			this.handleResize(); // for scopes
		}
		this.needAnalyze();
	}

	private void readHint(StringTokenizer st)
	{
		this.hintType = new Integer(st.nextToken()).intValue();
		this.hintItem1 = new Integer(st.nextToken()).intValue();
		this.hintItem2 = new Integer(st.nextToken()).intValue();
	}

	private void readOptions(StringTokenizer st)
	{
		// Atefact
		int flags = new Integer(st.nextToken()).intValue();

		// Dump pour garder la compatibilité, ancient timeStep.
		st.nextToken();

		double sp = new Double(st.nextToken()).doubleValue();
		int sp2 = (int) (Math.log(10 * sp) * 24 + 61.5);
		// int sp2 = (int) (Math.log(sp)*24+1.5);
		this.speedBar.setValue(sp2);

		// Dump pour garder la compatibilité, ancient currentBar.
		st.nextToken();

		// Dump pour garder la compatibilité, ancient voltage.
		st.nextToken();

		// Dump pour garder la compatibilité, ancient powerBar.
		// st.nextToken();

		this.setGrid();
	}

	public int snapGrid(int x)
	{
		return x + this.gridRound & this.gridMask;
	}

	private boolean doSwitch(int x, int y)
	{
		if (this.mouseMan.mouseElm == null || !(this.mouseMan.mouseElm instanceof SwitchElm))
		{
			return false;
		}

		SwitchElm se = (SwitchElm) this.mouseMan.mouseElm;
		se.toggle();

		if (se.momentary)
		{
			this.heldSwitchElm = se;
		}

		this.needAnalyze();

		return true;
	}

	private void dragAll(int x, int y)
	{
		int dx = x - this.mouseMan.dragX;
		int dy = y - this.mouseMan.dragY;
		if (dx == 0 && dy == 0)
		{
			return;
		}
		int i;
		for (i = 0; i != this.circuit.getElementCount(); i++)
		{
			CircuitElm ce = this.circuit.getElementAt(i);
			ce.move(dx, dy);
		}
		this.removeZeroLengthElements();
	}

	private void dragRow(int x, int y)
	{
		int dy = y - this.mouseMan.dragY;
		if (dy == 0)
		{
			return;
		}
		int i;
		for (i = 0; i != this.circuit.getElementCount(); i++)
		{
			CircuitElm ce = this.circuit.getElementAt(i);
			if (ce.y == this.mouseMan.dragY)
			{
				ce.movePoint(0, 0, dy);
			}
			if (ce.y2 == this.mouseMan.dragY)
			{
				ce.movePoint(1, 0, dy);
			}
		}
		this.removeZeroLengthElements();
	}

	private void dragColumn(int x, int y)
	{
		int dx = x - this.mouseMan.dragX;
		if (dx == 0)
		{
			return;
		}
		int i;
		for (i = 0; i != this.circuit.getElementCount(); i++)
		{
			CircuitElm ce = this.circuit.getElementAt(i);
			if (ce.x == this.mouseMan.dragX)
			{
				ce.movePoint(0, dx, 0);
			}
			if (ce.x2 == this.mouseMan.dragX)
			{
				ce.movePoint(1, dx, 0);
			}
		}
		this.removeZeroLengthElements();
	}

	private boolean dragSelected(int x, int y)
	{
		boolean me = false;
		if (this.mouseMan.mouseElm != null && !this.mouseMan.mouseElm.isSelected())
		{
			this.mouseMan.mouseElm.setSelected(me = true);
		}

		// snap grid, unless we're only dragging text elements
		int i;
		for (i = 0; i != this.circuit.getElementCount(); i++)
		{
			CircuitElm ce = this.circuit.getElementAt(i);
			if (ce.isSelected() && !(ce instanceof TextElm))
			{
				break;
			}
		}
		if (i != this.circuit.getElementCount())
		{
			x = this.snapGrid(x);
			y = this.snapGrid(y);
		}

		int dx = x - this.mouseMan.dragX;
		int dy = y - this.mouseMan.dragY;
		if (dx == 0 && dy == 0)
		{
			// don't leave mouseElm selected if we selected it above
			if (me)
			{
				this.mouseMan.mouseElm.setSelected(false);
			}
			return false;
		}
		boolean allowed = true;

		// check if moves are allowed
		for (i = 0; allowed && i != this.circuit.getElementCount(); i++)
		{
			CircuitElm ce = this.circuit.getElementAt(i);
			if (ce.isSelected() && !ce.allowMove(dx, dy))
			{
				allowed = false;
			}
		}

		if (allowed)
		{
			for (i = 0; i != this.circuit.getElementCount(); i++)
			{
				CircuitElm ce = this.circuit.getElementAt(i);
				if (ce.isSelected())
				{
					ce.move(dx, dy);
				}
			}
			this.needAnalyze();
		}

		// don't leave mouseElm selected if we selected it above
		if (me)
		{
			this.mouseMan.mouseElm.setSelected(false);
		}

		return allowed;
	}

	private void dragPost(int x, int y)
	{
		if (this.mouseMan.draggingPost == -1)
		{
			this.mouseMan.draggingPost = CoreUtil.distanceSq(this.mouseMan.mouseElm.x, this.mouseMan.mouseElm.y, x, y) > CoreUtil
					.distanceSq(this.mouseMan.mouseElm.x2, this.mouseMan.mouseElm.y2, x, y) ? 1 : 0;
		}
		int dx = x - this.mouseMan.dragX;
		int dy = y - this.mouseMan.dragY;
		if (dx == 0 && dy == 0)
		{
			return;
		}
		this.mouseMan.mouseElm.movePoint(this.mouseMan.draggingPost, dx, dy);
		this.needAnalyze();
	}

	private void selectArea(int x, int y)
	{
		int x1 = Math.min(x, this.mouseMan.initDragX);
		int x2 = Math.max(x, this.mouseMan.initDragX);
		int y1 = Math.min(y, this.mouseMan.initDragY);
		int y2 = Math.max(y, this.mouseMan.initDragY);
		this.selectedArea = new Rectangle(x1, y1, x2 - x1, y2 - y1);

		for (int i = 0; i != this.circuit.getElementCount(); i++)
		{
			CircuitElm ce = this.circuit.getElementAt(i);
			ce.selectRect(this.selectedArea);
		}
	}

	private void removeZeroLengthElements()
	{
		this.circuit.removeZeroLengthElements();

		this.cirFrame.repaint();
	}

	@Deprecated
	private static CircuitElm constructElement(Class<?> classType, int x0, int y0)
	{
		return CircuitUtil.constructElement(classType, x0, y0);
	}

	private void doPopupMenu(MouseEvent e)
	{
		this.menuElm = this.mouseMan.mouseElm;
		this.menuScope = -1;

		if (this.mouseMan.scopeSelected != -1)
		{
			JPopupMenu m = this.scopeMan.scopes[this.mouseMan.scopeSelected].getMenu();
			this.menuScope = this.mouseMan.scopeSelected;
			if (m != null)
			{
				m.show(e.getComponent(), e.getX(), e.getY());
			}
		}
		else if (this.mouseMan.mouseElm != null)
		{
			this.elmEditMenuItem.setEnabled(this.mouseMan.mouseElm.getEditInfo(0) != null);
			this.elmScopeMenuItem.setEnabled(this.mouseMan.mouseElm.canViewInScope());
			this.elementsPopUp.show(e.getComponent(), e.getX(), e.getY());
		}
		else
		{
			this.doMainMenuChecks(this.mainMenu);
			this.mainMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private void doMainMenuChecks(MenuElement m)
	{
		MenuElement[] sub = m.getSubElements();

		for (int i = 0; i != sub.length; i++)
		{
			MenuElement mc = sub[i];

			if (mc instanceof JPopupMenu)
			{
				this.doMainMenuChecks(mc);
			}

			if (mc instanceof JCheckBoxMenuItem)
			{
				JCheckBoxMenuItem cmi = (JCheckBoxMenuItem) mc;
				cmi.setState(this.mouseMan.testMouseMode(cmi.getActionCommand()));
			}
		}
	}

	private void doMainMenuChecks(JMenu m)
	{
		for (int i = 0; i != m.getItemCount(); i++)
		{
			JMenuItem mc = m.getItem(i);

			if (mc instanceof JMenu)
			{
				this.doMainMenuChecks((JMenu) mc);
			}

			if (mc instanceof JCheckBoxMenuItem)
			{
				JCheckBoxMenuItem cmi = (JCheckBoxMenuItem) mc;
				cmi.setState(this.mouseMan.testMouseMode(cmi.getActionCommand()));
			}
		}
	}

	private void enableItems()
	{
		if (Configs.SHOW_POWER)
		{

		}
		else
		{

		}
		this.clipboard.enableUndoRedo();
	}

	private void setGrid()
	{
		if (Configs.SMALL_GRID)
		{
			this.gridSize = 8;
		}
		else
		{
			this.gridSize = 16;
		}

		// le "~" est un "not" bitwise.
		this.gridMask = ~(this.gridSize - 1);
		this.gridRound = this.gridSize / 2 - 1;
	}

	private void pushUndo()
	{
		this.clipboard.redoStack.removeAllElements();
		String s = this.dumpCircuit();
		if (this.clipboard.undoStack.size() > 0 && s.compareTo(this.clipboard.undoStack.lastElement()) == 0)
		{
			return;
		}
		this.clipboard.undoStack.add(s);
		this.clipboard.enableUndoRedo();
	}

	private void doUndo()
	{
		if (this.clipboard.undoStack.size() > 0)
		{
			this.clipboard.redoStack.add(this.dumpCircuit());
			String s = this.clipboard.undoStack.remove(this.clipboard.undoStack.size() - 1);
			this.readSetup(s);
			this.clipboard.enableUndoRedo();
		}
	}

	private void doRedo()
	{
		if (this.clipboard.redoStack.size() > 0)
		{

			this.clipboard.undoStack.add(this.dumpCircuit());

			String s = this.clipboard.redoStack.remove(this.clipboard.redoStack.size() - 1);

			this.readSetup(s);
			this.clipboard.enableUndoRedo();
		}

	}

	private void setMenuSelection()
	{
		if (this.menuElm != null && !this.menuElm.selected)
		{
			this.circuit.clearSelection();
			this.menuElm.setSelected(true);

		}
	}

	private void doCut()
	{
		this.pushUndo();
		this.setMenuSelection();
		this.clipboard.cache = "";

		for (int i = this.circuit.getElementCount() - 1; i >= 0; i--)
		{
			CircuitElm ce = this.circuit.getElementAt(i);
			if (ce.isSelected())
			{
				this.clipboard.cache += ce.dump() + "\n";
				ce.delete();
				this.circuit.removeElementAt(i);
			}
		}
		this.clipboard.enablePaste();

		this.circuit.setNeedAnalysis(true);
	}

	private void doDelete()
	{
		this.pushUndo();
		this.setMenuSelection();

		for (int i = this.circuit.getElementCount() - 1; i >= 0; i--)
		{
			CircuitElm ce = this.circuit.getElementAt(i);
			if (ce.isSelected())
			{
				ce.delete();
				this.circuit.removeElementAt(i);
			}
		}

		this.circuit.setNeedAnalysis(true);
	}

	private void doCopy()
	{
		this.clipboard.cache = "";
		this.setMenuSelection();
		for (int i = this.circuit.getElementCount() - 1; i >= 0; i--)
		{
			CircuitElm ce = this.circuit.getElementAt(i);
			if (ce.isSelected())
			{
				this.clipboard.cache += ce.dump() + "\n";
			}

		}

		this.clipboard.enablePaste();
	}

	private void doPaste()
	{
		this.pushUndo();
		this.circuit.clearSelection();
		int i;
		Rectangle oldbb = null;
		for (i = 0; i != this.circuit.getElementCount(); i++)
		{
			CircuitElm ce = this.circuit.getElementAt(i);
			Rectangle bb = ce.getBoundingBox();
			if (oldbb != null)
			{
				oldbb = oldbb.union(bb);
			}
			else
			{
				oldbb = bb;
			}
		}
		int oldsz = this.circuit.getElementCount();
		this.readSetup(this.clipboard.cache, true);

		// select new items
		Rectangle newbb = null;
		for (i = oldsz; i != this.circuit.getElementCount(); i++)
		{
			CircuitElm ce = this.circuit.getElementAt(i);
			ce.setSelected(true);
			Rectangle bb = ce.getBoundingBox();

			if (newbb != null)
			{
				newbb = newbb.union(bb);
			}
			else
			{
				newbb = bb;
			}

		}

		if (oldbb != null && newbb != null && oldbb.intersects(newbb))
		{
			// find a place for new items
			int dx = 0;
			int dy = 0;
			int spacew = this.circuit.circuitArea.width - oldbb.width - newbb.width;
			int spaceh = this.circuit.circuitArea.height - oldbb.height - newbb.height;
			if (spacew > spaceh)
			{
				dx = this.snapGrid(oldbb.x + oldbb.width - newbb.x + this.gridSize);
			}
			else
			{
				dy = this.snapGrid(oldbb.y + oldbb.height - newbb.y + this.gridSize);
			}

			for (i = oldsz; i != this.circuit.getElementCount(); i++)
			{
				CircuitElm ce = this.circuit.getElementAt(i);
				ce.move(dx, dy);
			}
			// center circuit
			this.handleResize();
		}
		this.needAnalyze();

	}

	private void exit()
	{
		System.out.println("Exit at " + System.currentTimeMillis());
		System.exit(0);
	}

	@Override
	public void componentHidden(ComponentEvent e)
	{
	}

	@Override
	public void componentMoved(ComponentEvent e)
	{
	}

	@Override
	public void componentShown(ComponentEvent e)
	{
	}

	@Override
	public void componentResized(ComponentEvent e)
	{
		this.handleResize();
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		String ac = e.getActionCommand();


		if (e.getSource() == this.clipboard.undoItem)
		{
			this.doUndo();
		}

		if (e.getSource() == this.clipboard.redoItem)
		{
			this.doRedo();
		}

		if (ac.compareTo("Cut") == 0)
		{
			if (e.getSource() != this.elmCutMenuItem)
			{
				this.menuElm = null;
			}
			this.doCut();
		}

		if (ac.compareTo("Copy") == 0)
		{
			if (e.getSource() != this.elmCopyMenuItem)
			{
				this.menuElm = null;
			}
			this.doCopy();
		}

		if (ac.compareTo("Paste") == 0)
		{
			this.doPaste();
		}

		if (e.getSource() == this.selectAllItem)
		{
			this.circuit.doSelectAll();
		}



		if (ac.compareTo("stackAll") == 0)
		{
			this.scopeMan.stackAll();
		}

		if (ac.compareTo("unstackAll") == 0)
		{
			this.scopeMan.unstackAll();
		}

		if (e.getSource() == this.elmEditMenuItem)
		{
			this.doEdit(this.menuElm);
		}

		if (ac.compareTo("Delete") == 0)
		{
			if (e.getSource() != this.elmDeleteMenuItem)
			{
				this.menuElm = null;
			}
			this.doDelete();
		}

		if (e.getSource() == this.elmScopeMenuItem && this.menuElm != null)
		{
			int i;
			for (i = 0; i != this.scopeMan.scopeCount; i++)
			{
				if (this.scopeMan.scopes[i].element == null)
				{
					break;
				}
			}
			if (i == this.scopeMan.scopeCount)
			{
				if (this.scopeMan.scopeCount == this.scopeMan.scopes.length)
				{
					return;
				}
				this.scopeMan.scopeCount++;
				this.scopeMan.scopes[i] = new Scope(this);
				this.scopeMan.scopes[i].position = i;
				this.handleResize();
			}
			this.scopeMan.scopes[i].setElement(this.menuElm);
		}

		if (this.menuScope != -1)
		{
			if (ac.compareTo("remove") == 0)
			{
				this.scopeMan.scopes[this.menuScope].setElement(null);
			}
			if (ac.compareTo("speed2") == 0)
			{
				this.scopeMan.scopes[this.menuScope].speedUp();
			}
			if (ac.compareTo("speed1/2") == 0)
			{
				this.scopeMan.scopes[this.menuScope].slowDown();
			}
			if (ac.compareTo("scale") == 0)
			{
				this.scopeMan.scopes[this.menuScope].adjustScale(.5);
			}
			if (ac.compareTo("maxscale") == 0)
			{
				this.scopeMan.scopes[this.menuScope].adjustScale(1e-50);
			}
			if (ac.compareTo("stack") == 0)
			{
				this.stackScope(this.menuScope);
			}
			if (ac.compareTo("unstack") == 0)
			{
				this.unstackScope(this.menuScope);
			}
			if (ac.compareTo("selecty") == 0)
			{
				this.scopeMan.scopes[this.menuScope].selectY();
			}
			if (ac.compareTo("reset") == 0)
			{
				this.scopeMan.scopes[this.menuScope].resetGraph();
			}
		}

		if (ac.indexOf("setup ") == 0)
		{
			this.pushUndo();
			this.readSetupFile(ac.substring(6), ((JMenuItem) e.getSource()).getLabel());
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		Object mi = e.getItemSelectable();

		this.enableItems();
		if (this.menuScope != -1)
		{
			Scope sc = this.scopeMan.scopes[this.menuScope];
			sc.handleMenu(e, mi);
		}
		if (mi instanceof CheckboxMenuItem)
		{
			MenuItem mmi = (MenuItem) mi;
			this.mouseMan.mouseMode = CircuitSimulator.MODE_ADD_ELM;
			String s = mmi.getActionCommand();

			if (s.length() > 0)
			{
				this.mouseMan.mouseModeStr = s;
			}

			if (s.compareTo("DragAll") == 0)
			{
				this.mouseMan.mouseMode = CircuitSimulator.MODE_DRAG_ALL;
			}
			else if (s.compareTo("DragRow") == 0)
			{
				this.mouseMan.mouseMode = CircuitSimulator.MODE_DRAG_ROW;
			}
			else if (s.compareTo("DragColumn") == 0)
			{
				this.mouseMan.mouseMode = CircuitSimulator.MODE_DRAG_COLUMN;
			}
			else if (s.compareTo("DragSelected") == 0)
			{
				this.mouseMan.mouseMode = CircuitSimulator.MODE_DRAG_SELECTED;
			}
			else if (s.compareTo("DragPost") == 0)
			{
				this.mouseMan.mouseMode = CircuitSimulator.MODE_DRAG_POST;
			}
			else if (s.compareTo("Select") == 0)
			{
				this.mouseMan.mouseMode = CircuitSimulator.MODE_SELECT;
			}
			else if (s.length() > 0)
			{
				try
				{
					this.addingClass = Class.forName("com.limoilux.circuit." + s);
				}
				catch (Exception ee)
				{
					ee.printStackTrace();
				}
			}
			this.mouseMan.tempMouseMode = this.mouseMan.mouseMode;
		}
	}

	private void reset()
	{
		int nbElements = this.circuit.getElementCount();
		
		for (int i = 0; i < nbElements; i++)
		{
			this.circuit.getElementAt(i).reset();
		}

		for (int i = 0; i < CircuitSimulator.this.scopeMan.scopeCount; i++)
		{
			this.scopeMan.scopes[i].resetGraph();
		}

		this.circuit.setNeedAnalysis(true);
		this.timer.time = 0;
		this.activityManager.setPlaying(true);
	}

	private class ExitAction extends AbstractAction
	{
		public ExitAction()
		{
			super("exit");
		}
	
		@Override
		public void actionPerformed(ActionEvent e)
		{
			CircuitSimulator.this.exit();
		}
	}

	private class MigrationAction extends AbstractAction
	{
		public MigrationAction()
		{
			super("Import/Export");
		}
	
		@Override
		public void actionPerformed(ActionEvent e)
		{
			CircuitSimulator.this.showMigrationDialog();
		}
		
	}

	private class ResetAction extends AbstractAction
	{
		public ResetAction()
		{
			super("reset");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			CircuitSimulator.this.reset();
		}
	}

	private class Starter implements Runnable
	{
		@Override
		public void run()
		{
			CircuitSimulator.this.cirFrame.setVisible(true);
			CircuitSimulator.this.scopeMan.setupScopes(CircuitSimulator.this.winSize);
			CircuitSimulator.this.cirFrame.requestFocus();
			CircuitSimulator.this.handleResize();

		}
	}

	private class MyKeyListener implements KeyListener
	{

		@Override
		public void keyPressed(KeyEvent e)
		{
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
			if (e.getKeyChar() > ' ' && e.getKeyChar() < 127)
			{
				Class<?> c = CircuitSimulator.this.circuitMan.dumpMan.dumpTypes[e.getKeyChar()];
				if (c == null || c == Scope.class)
				{
					return;
				}

				CircuitElm elm = null;
				elm = CircuitUtil.constructElement(c, 0, 0);
				if (elm == null || !(elm.needsShortcut() && elm.getDumpClass() == c))
				{
					return;
				}

				CircuitSimulator.this.mouseMan.mouseMode = CircuitSimulator.MODE_ADD_ELM;
				CircuitSimulator.this.mouseMan.mouseModeStr = c.getName();
				CircuitSimulator.this.addingClass = c;
			}

			if (e.getKeyChar() == ' ')
			{
				CircuitSimulator.this.mouseMan.mouseMode = CircuitSimulator.MODE_SELECT;
				CircuitSimulator.this.mouseMan.mouseModeStr = "Select";
			}

			CircuitSimulator.this.mouseMan.tempMouseMode = CircuitSimulator.this.mouseMan.mouseMode;
		}
	}

	private class MyMouseListener implements MouseListener
	{

		@Override
		public void mouseClicked(MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			int x = e.getX();
			int y = e.getY();
			int modif = e.getModifiers();
			int ex = e.getModifiersEx();

			if ((ex & (InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)) == 0 && e.isPopupTrigger())
			{
				CircuitSimulator.this.doPopupMenu(e);
				return;
			}

			if ((modif & InputEvent.BUTTON1_MASK) != 0)
			{
				CircuitSimulator.this.mouseMan.leftClick(ex);
			}
			else if ((modif & InputEvent.BUTTON3_MASK) != 0)
			{
				if (CircuitSimulator.this.mouseMan.rightClick(ex))
				{
					// Trouver un moyen d'éliminer
					return;
				}
			}

			if (!CircuitSimulator.this.mouseMan.isModeSelected())
			{
				CircuitSimulator.this.circuit.clearSelection();
			}

			if (CircuitSimulator.this.doSwitch(x, y))
			{
				return;
			}

			CircuitSimulator.this.pushUndo();

			CircuitSimulator.this.mouseMan.initDragX = x;
			CircuitSimulator.this.mouseMan.initDragY = y;

			if (CircuitSimulator.this.mouseMan.tempMouseMode != CircuitSimulator.MODE_ADD_ELM
					|| CircuitSimulator.this.addingClass == null)
			{
				return;
			}

			int x0 = CircuitSimulator.this.snapGrid(x);
			int y0 = CircuitSimulator.this.snapGrid(y);

			if (!CircuitSimulator.this.circuit.circuitArea.contains(x0, y0))
			{
				return;
			}

			CircuitSimulator.this.mouseMan.dragElm = CircuitUtil.constructElement(CircuitSimulator.this.addingClass,
					x0, y0);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			int ex = e.getModifiersEx();
			boolean circuitChanged = false;

			if ((ex & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK)) == 0
					&& e.isPopupTrigger())
			{
				CircuitSimulator.this.doPopupMenu(e);
				return;
			}

			CircuitSimulator.this.mouseMan.tempMouseMode = CircuitSimulator.this.mouseMan.mouseMode;
			CircuitSimulator.this.selectedArea = null;

			if (CircuitSimulator.this.heldSwitchElm != null)
			{
				CircuitSimulator.this.heldSwitchElm.mouseUp();
				CircuitSimulator.this.heldSwitchElm = null;
				circuitChanged = true;
			}

			if (CircuitSimulator.this.mouseMan.dragElm != null)
			{
				// if the element is zero size then don't create it
				if (CircuitSimulator.this.mouseMan.dragElm.x == CircuitSimulator.this.mouseMan.dragElm.x2
						&& CircuitSimulator.this.mouseMan.dragElm.y == CircuitSimulator.this.mouseMan.dragElm.y2)
				{
					CircuitSimulator.this.mouseMan.dragElm.delete();
				}
				else
				{
					CircuitSimulator.this.circuit.addElement(CircuitSimulator.this.mouseMan.dragElm);
					circuitChanged = true;
				}

				CircuitSimulator.this.mouseMan.dragElm = null;
			}

			if (circuitChanged)
			{
				CircuitSimulator.this.circuit.setNeedAnalysis(true);
			}

			if (CircuitSimulator.this.mouseMan.dragElm != null)
			{
				CircuitSimulator.this.mouseMan.dragElm.delete();
			}

			CircuitSimulator.this.mouseMan.dragElm = null;
		}

	}

	private class MyMouseMotionListener implements MouseMotionListener
	{
		@Override
		public void mouseDragged(MouseEvent e)
		{
			// ignore right mouse button with no modifiers (needed on PC)
			if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
			{
				int ex = e.getModifiersEx();
				if ((ex & (InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK)) == 0)
				{
					return;
				}
			}

			// if (!CirSim.this.circuitArea.contains(e.getX(), e.getY()))
			// {
			// System.out.println("!!!!");
			// return;
			// }

			if (CircuitSimulator.this.mouseMan.dragElm != null)
			{
				CircuitSimulator.this.mouseMan.dragElm.drag(e.getX(), e.getY());
			}

			boolean success = true;

			switch (CircuitSimulator.this.mouseMan.tempMouseMode)
			{
			case MODE_DRAG_ALL:
				CircuitSimulator.this.dragAll(CircuitSimulator.this.snapGrid(e.getX()),
						CircuitSimulator.this.snapGrid(e.getY()));
				break;
			case MODE_DRAG_ROW:
				CircuitSimulator.this.dragRow(CircuitSimulator.this.snapGrid(e.getX()),
						CircuitSimulator.this.snapGrid(e.getY()));
				break;
			case MODE_DRAG_COLUMN:
				CircuitSimulator.this.dragColumn(CircuitSimulator.this.snapGrid(e.getX()),
						CircuitSimulator.this.snapGrid(e.getY()));
				break;
			case MODE_DRAG_POST:
				if (CircuitSimulator.this.mouseMan.mouseElm != null)
				{
					CircuitSimulator.this.dragPost(CircuitSimulator.this.snapGrid(e.getX()),
							CircuitSimulator.this.snapGrid(e.getY()));
				}
				break;
			case MODE_SELECT:
				if (CircuitSimulator.this.mouseMan.mouseElm == null)
				{
					CircuitSimulator.this.selectArea(e.getX(), e.getY());
				}
				else
				{
					CircuitSimulator.this.mouseMan.tempMouseMode = CircuitSimulator.MODE_DRAG_SELECTED;
					success = CircuitSimulator.this.dragSelected(e.getX(), e.getY());
				}
				break;
			case MODE_DRAG_SELECTED:
				success = CircuitSimulator.this.dragSelected(e.getX(), e.getY());
				break;
			}

			if (success)
			{
				if (CircuitSimulator.this.mouseMan.tempMouseMode == CircuitSimulator.MODE_DRAG_SELECTED
						&& CircuitSimulator.this.mouseMan.mouseElm instanceof TextElm)
				{
					CircuitSimulator.this.mouseMan.dragX = e.getX();
					CircuitSimulator.this.mouseMan.dragY = e.getY();
				}
				else
				{
					CircuitSimulator.this.mouseMan.dragX = CircuitSimulator.this.snapGrid(e.getX());
					CircuitSimulator.this.mouseMan.dragY = CircuitSimulator.this.snapGrid(e.getY());
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
			{
				return;
			}

			int x = e.getX();
			int y = e.getY();
			CircuitSimulator.this.mouseMan.dragX = CircuitSimulator.this.snapGrid(x);
			CircuitSimulator.this.mouseMan.dragY = CircuitSimulator.this.snapGrid(y);
			CircuitSimulator.this.mouseMan.draggingPost = -1;
			int i;

			CircuitSimulator.this.mouseMan.mouseElm = null;
			CircuitSimulator.this.mouseMan.mousePost = -1;
			CircuitSimulator.this.mouseMan.plotXElm = null;
			CircuitSimulator.this.mouseMan.plotYElm = null;
			int bestDist = 100000;
			int bestArea = 100000;

			for (i = 0; i < CircuitSimulator.this.circuit.getElementCount(); i++)
			{
				CircuitElm currentElement = CircuitSimulator.this.circuit.getElementAt(i);
				if (currentElement.boundingBox.contains(x, y))
				{

					int area = currentElement.boundingBox.width * currentElement.boundingBox.height;
					int jn = currentElement.getPostCount();

					if (jn > 2)
					{
						jn = 2;
					}

					for (int j = 0; j < jn; j++)
					{
						Point pt = currentElement.getPost(j);
						int dist = CoreUtil.distanceSq(x, y, pt.x, pt.y);

						// if multiple elements have overlapping bounding boxes,
						// we prefer selecting elements that have posts close
						// to the mouse pointer and that have a small bounding
						// box area.
						if (dist <= bestDist && area <= bestArea)
						{
							bestDist = dist;
							bestArea = area;
							CircuitSimulator.this.mouseMan.mouseElm = currentElement;
						}
					}

					if (currentElement.getPostCount() == 0)
					{
						CircuitSimulator.this.mouseMan.mouseElm = currentElement;
					}
				}
			}

			CircuitSimulator.this.mouseMan.scopeSelected = -1;
			if (CircuitSimulator.this.mouseMan.mouseElm == null)
			{
				for (i = 0; i != CircuitSimulator.this.scopeMan.scopeCount; i++)
				{
					Scope s = CircuitSimulator.this.scopeMan.scopes[i];
					if (s.rect.contains(x, y))
					{
						s.select();
						CircuitSimulator.this.mouseMan.scopeSelected = i;
					}
				}
				// the mouse pointer was not in any of the bounding boxes, but
				// we
				// might still be close to a post
				for (i = 0; i != CircuitSimulator.this.circuit.getElementCount(); i++)
				{
					CircuitElm ce = CircuitSimulator.this.circuit.getElementAt(i);
					int j;
					int jn = ce.getPostCount();
					for (j = 0; j != jn; j++)
					{
						Point pt = ce.getPost(j);
						// int dist = CoreUtil.distanceSq(x, y, pt.x, pt.y);
						if (CoreUtil.distanceSq(pt.x, pt.y, x, y) < 26)
						{
							CircuitSimulator.this.mouseMan.mouseElm = ce;
							CircuitSimulator.this.mouseMan.mousePost = j;
							break;
						}
					}
				}
			}
			else
			{
				CircuitSimulator.this.mouseMan.mousePost = -1;
				// look for post close to the mouse pointer
				for (i = 0; i != CircuitSimulator.this.mouseMan.mouseElm.getPostCount(); i++)
				{
					Point pt = CircuitSimulator.this.mouseMan.mouseElm.getPost(i);
					if (CoreUtil.distanceSq(pt.x, pt.y, x, y) < 26)
					{
						CircuitSimulator.this.mouseMan.mousePost = i;
					}

				}
			}

		}
	}

	private class ActivityList implements ActivityListener
	{
		@Override
		public void stateChanged(boolean isPlaying)
		{
			if (isPlaying)
			{
				CircuitSimulator.this.circuit.setNeedAnalysis(true);
			}
		}
	}

	private class RepaintRun implements Runnable
	{
		boolean goOn = true;

		@Override
		public void run()
		{
			long delay = 0;

			while (this.goOn)
			{

				delay = CircuitSimulator.this.repaint();

				if (delay > 0)
				{
					try
					{
						Thread.sleep(delay);
					}
					catch (InterruptedException e)
					{
					}
				}

				CircuitSimulator.this.timer.nextCycle();
			}
		}
	}

	public static CircuitSimulator getInstance()
	{
		return CircuitSimulator.SINGLETON;
	}

	public static void main(String args[])
	{
		CircuitSimulator c = CircuitSimulator.getInstance();

		c.start();
	}
}
