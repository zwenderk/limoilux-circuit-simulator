
package com.limoilux.circuit.core;

// CirSim.java (c) 2010 by Paul Falstad

// For information about the theory behind this, see Electronic Circuit & System Simulation Methods by Pillage

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
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
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;

import com.limoilux.circuit.CapacitorElm;
import com.limoilux.circuit.CurrentElm;
import com.limoilux.circuit.GroundElm;
import com.limoilux.circuit.InductorElm;
import com.limoilux.circuit.RailElm;
import com.limoilux.circuit.ResistorElm;
import com.limoilux.circuit.SwitchElm;
import com.limoilux.circuit.TextElm;
import com.limoilux.circuit.VoltageElm;
import com.limoilux.circuit.WireElm;
import com.limoilux.circuit.techno.Circuit;
import com.limoilux.circuit.techno.CircuitAnalysisException;
import com.limoilux.circuit.techno.CircuitElm;
import com.limoilux.circuit.ui.CircuitCanvas;
import com.limoilux.circuit.ui.CircuitLayout;
import com.limoilux.circuit.ui.CircuitNode;
import com.limoilux.circuit.ui.CircuitNodeLink;
import com.limoilux.circuit.ui.DrawUtil;
import com.limoilux.circuit.ui.EditDialog;
import com.limoilux.circuit.ui.EditOptions;
import com.limoilux.circuit.ui.RowInfo;
import com.limoilux.circuit.ui.io.MigrationWizard;
import com.limoilux.circuit.ui.scope.Scope;
import com.limoilux.circuit.ui.scope.ScopeManager;

public class CirSim extends JFrame implements ComponentListener, ActionListener, AdjustmentListener, ItemListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1165604792341204140L;
	@Deprecated
	private static final double PI = Math.PI;

	private static final int MODE_ADD_ELM = 0;
	private static final int MODE_DRAG_ALL = 1;

	private static final int MODE_DRAG_SELECTED = 4;
	private static final int MODE_DRAG_POST = 5;
	private static final int MODE_SELECT = 6;

	private static final int HINT_LC = 1;
	private static final int HINT_RC = 2;
	private static final int HINT_3DB_C = 3;
	private static final int HINT_TWINT = 4;
	private static final int HINT_3DB_L = 5;

	public static final int INFO_WIDTH = 120;
	public static final int MODE_DRAG_ROW = 2;
	public static final int MODE_DRAG_COLUMN = 3;

	public final JFrame mainContainer;

	public static String muString = "u";
	public static String ohmString = "ohm";
	public static EditDialog editDialog;
	public static MigrationWizard impDialog;

	private String startCircuit = null;
	private String startLabel = null;
	private String startCircuitText = null;
	private Image dbimage;

	public int subIterations;

	public Dimension winSize;
	public Vector<CircuitElm> elmList;
	public CircuitElm dragElm, menuElm, mouseElm, stopElm;
	private int mousePost = -1;
	public CircuitElm plotXElm, plotYElm;
	private int draggingPost;
	private SwitchElm heldSwitchElm;
	private double origMatrix[][];

	private long lastTime = 0;
	private long lastFrameTime;
	private long lastIterTime;
	private long secTime = 0;

	private Class<?> dumpTypes[];

	private int dragX, dragY, initDragX, initDragY;
	private Rectangle selectedArea;
	public int gridSize, gridMask, gridRound;
	public boolean analyzeFlag;
	private boolean dumpMatrix;
	public boolean useBufferedImage;
	private String ctrlMetaKey;
	public double t;
	private int pause = 10;
	public int scopeSelected = -1;
	private int menuScope = -1;
	private int hintType = -1, hintItem1, hintItem2;
	private String stopMessage;
	public double timeStep;

	private String clipboard;
	private Rectangle circuitArea;
	private Vector<String> undoStack, redoStack;

	private Label titleLabel;
	private Button resetButton;
	private Button dumpMatrixButton;
	private MenuItem exportItem, importItem, exitItem, undoItem, redoItem, cutItem, copyItem, pasteItem, selectAllItem,
			optionsItem;

	private Menu optionsMenu;
	public Checkbox stoppedCheck;
	public CheckboxMenuItem dotsCheckItem;
	public CheckboxMenuItem voltsCheckItem;
	public CheckboxMenuItem powerCheckItem;
	public CheckboxMenuItem smallGridCheckItem;
	public CheckboxMenuItem showValuesCheckItem;
	public CheckboxMenuItem conductanceCheckItem;
	public CheckboxMenuItem euroResistorCheckItem;
	public CheckboxMenuItem printableCheckItem;
	public CheckboxMenuItem conventionCheckItem;
	private Scrollbar speedBar;
	private Scrollbar currentBar;
	private Label powerLabel;
	private Scrollbar powerBar;
	private PopupMenu elmMenu;
	private MenuItem elmEditMenuItem;
	private MenuItem elmCutMenuItem;
	private MenuItem elmCopyMenuItem;
	private MenuItem elmDeleteMenuItem;
	private MenuItem elmScopeMenuItem;
	public PopupMenu scopeMenu;
	public PopupMenu transScopeMenu;
	private PopupMenu mainMenu;
	public CheckboxMenuItem scopeVMenuItem;
	public CheckboxMenuItem scopeIMenuItem;
	public CheckboxMenuItem scopeMaxMenuItem;
	public CheckboxMenuItem scopeMinMenuItem;
	public CheckboxMenuItem scopeFreqMenuItem;
	public CheckboxMenuItem scopePowerMenuItem;
	public CheckboxMenuItem scopeIbMenuItem;
	public CheckboxMenuItem scopeIcMenuItem;
	public CheckboxMenuItem scopeIeMenuItem;
	public CheckboxMenuItem scopeVbeMenuItem;
	public CheckboxMenuItem scopeVbcMenuItem;
	public CheckboxMenuItem scopeVceMenuItem;
	public CheckboxMenuItem scopeVIMenuItem;
	public CheckboxMenuItem scopeXYMenuItem;
	public CheckboxMenuItem scopeResistMenuItem;
	public CheckboxMenuItem scopeVceIcMenuItem;
	public MenuItem scopeSelectYMenuItem;
	private Class<?> addingClass;
	public int mouseMode = CirSim.MODE_SELECT;
	private int tempMouseMode = CirSim.MODE_SELECT;
	private String mouseModeStr = "Select";

	private final MouseMotionListener mouseMotionList;
	private final MouseListener mouseList;
	private final KeyListener keyList;

	public final Circuit circuit;
	public final ScopeManager scopeMan;

	public final CircuitCanvas circuitCanvas;

	public CirSim()
	{
		super("Limoilux Circuit Simulator v1.1");

		this.circuit = new Circuit();
		this.scopeMan = new ScopeManager();

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.mouseMotionList = new MyMouseMotionListener();
		this.mouseList = new MyMouseListener();
		this.keyList = new MyKeyListener();

		String euroResistor = null;
		boolean printable = false;
		boolean convention = true;

		CircuitElm.initClass(this);

		boolean euro = euroResistor != null && euroResistor.equalsIgnoreCase("true");

		this.mainContainer = this;

		String os = System.getProperty("os.name");
		boolean isMac = os.indexOf("Mac ") == 0;

		if (isMac)
		{
			this.ctrlMetaKey = "\u2318";
		}
		else
		{
			this.ctrlMetaKey = "Ctrl";
		}

		String jv = System.getProperty("java.class.version");
		double jvf = new Double(jv).doubleValue();

		if (jvf >= 48)
		{
			CirSim.muString = "\u03bc";
			CirSim.ohmString = "\u03a9";
			this.useBufferedImage = true;
		}

		this.dumpTypes = new Class[300];
		// these characters are reserved
		this.dumpTypes['o'] = Scope.class;
		this.dumpTypes['h'] = Scope.class;
		this.dumpTypes['$'] = Scope.class;
		this.dumpTypes['%'] = Scope.class;
		this.dumpTypes['?'] = Scope.class;
		this.dumpTypes['B'] = Scope.class;

		this.mainContainer.setLayout(new CircuitLayout());
		this.circuitCanvas = new CircuitCanvas(this);
		this.circuitCanvas.addComponentListener(this);
		this.circuitCanvas.addMouseMotionListener(this.mouseMotionList);
		this.circuitCanvas.addMouseListener(this.mouseList);
		this.circuitCanvas.addKeyListener(this.keyList);

		this.mainContainer.add(this.circuitCanvas);

		this.mainMenu = new PopupMenu();
		MenuBar menubar = null;

		menubar = new MenuBar();
		Menu m = new Menu("File");

		menubar.add(m);

		m.add(this.importItem = this.getMenuItem("Import"));
		m.add(this.exportItem = this.getMenuItem("Export"));
		m.addSeparator();
		m.add(this.exitItem = this.getMenuItem("Exit"));

		m = new Menu("Edit");
		m.add(this.undoItem = this.getMenuItem("Undo"));
		this.undoItem.setShortcut(new MenuShortcut(KeyEvent.VK_Z));
		m.add(this.redoItem = this.getMenuItem("Redo"));
		this.redoItem.setShortcut(new MenuShortcut(KeyEvent.VK_Z, true));
		m.addSeparator();
		m.add(this.cutItem = this.getMenuItem("Cut"));
		this.cutItem.setShortcut(new MenuShortcut(KeyEvent.VK_X));
		m.add(this.copyItem = this.getMenuItem("Copy"));
		this.copyItem.setShortcut(new MenuShortcut(KeyEvent.VK_C));
		m.add(this.pasteItem = this.getMenuItem("Paste"));
		this.pasteItem.setShortcut(new MenuShortcut(KeyEvent.VK_V));
		this.pasteItem.setEnabled(false);
		m.add(this.selectAllItem = this.getMenuItem("Select All"));
		this.selectAllItem.setShortcut(new MenuShortcut(KeyEvent.VK_A));

		menubar.add(m);

		m = new Menu("Scope");

		menubar.add(m);

		m.add(this.getMenuItem("Stack All", "stackAll"));
		m.add(this.getMenuItem("Unstack All", "unstackAll"));

		this.optionsMenu = m = new Menu("Options");

		menubar.add(m);

		m.add(this.dotsCheckItem = this.getCheckItem("Show Current"));
		this.dotsCheckItem.setState(true);
		m.add(this.voltsCheckItem = this.getCheckItem("Show Voltage"));
		this.voltsCheckItem.setState(true);
		m.add(this.powerCheckItem = this.getCheckItem("Show Power"));
		m.add(this.showValuesCheckItem = this.getCheckItem("Show Values"));
		this.showValuesCheckItem.setState(true);
		// m.add(conductanceCheckItem = getCheckItem("Show Conductance"));
		m.add(this.smallGridCheckItem = this.getCheckItem("Small Grid"));
		m.add(this.euroResistorCheckItem = this.getCheckItem("European Resistors"));
		this.euroResistorCheckItem.setState(euro);
		m.add(this.printableCheckItem = this.getCheckItem("White Background"));
		this.printableCheckItem.setState(printable);
		m.add(this.conventionCheckItem = this.getCheckItem("Conventional Current Motion"));
		this.conventionCheckItem.setState(convention);
		m.add(this.optionsItem = this.getMenuItem("Other Options..."));

		Menu circuitsMenu = new Menu("Circuits");

		menubar.add(circuitsMenu);

		this.mainMenu.add(this.getClassCheckItem("Add Wire", "WireElm"));
		this.mainMenu.add(this.getClassCheckItem("Add Resistor", "ResistorElm"));

		Menu passMenu = new Menu("Passive Components");
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

		Menu inputMenu = new Menu("Inputs/Outputs");
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

		Menu activeMenu = new Menu("Active Components");
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

		Menu gateMenu = new Menu("Logic Gates");
		this.mainMenu.add(gateMenu);
		gateMenu.add(this.getClassCheckItem("Add Inverter", "InverterElm"));
		gateMenu.add(this.getClassCheckItem("Add NAND Gate", "NandGateElm"));
		gateMenu.add(this.getClassCheckItem("Add NOR Gate", "NorGateElm"));
		gateMenu.add(this.getClassCheckItem("Add AND Gate", "AndGateElm"));
		gateMenu.add(this.getClassCheckItem("Add OR Gate", "OrGateElm"));
		gateMenu.add(this.getClassCheckItem("Add XOR Gate", "XorGateElm"));

		Menu chipMenu = new Menu("Chips");
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

		Menu otherMenu = new Menu("Other");
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
		this.mainContainer.add(this.mainMenu);

		this.mainContainer.add(this.resetButton = new Button("Reset"));
		this.resetButton.addActionListener(this);
		this.dumpMatrixButton = new Button("Dump Matrix");
		// main.add(dumpMatrixButton);
		this.dumpMatrixButton.addActionListener(this);
		this.stoppedCheck = new Checkbox("Stopped");
		this.stoppedCheck.addItemListener(this);
		this.mainContainer.add(this.stoppedCheck);

		this.mainContainer.add(new Label("Simulation Speed", Label.CENTER));

		// was max of 140
		this.mainContainer.add(this.speedBar = new Scrollbar(Scrollbar.HORIZONTAL, 3, 1, 0, 260));
		this.speedBar.addAdjustmentListener(this);

		this.mainContainer.add(new Label("Current Speed", Label.CENTER));
		this.currentBar = new Scrollbar(Scrollbar.HORIZONTAL, 50, 1, 1, 100);
		this.currentBar.addAdjustmentListener(this);
		this.mainContainer.add(this.currentBar);

		this.mainContainer.add(this.powerLabel = new Label("Power Brightness", Label.CENTER));
		this.mainContainer.add(this.powerBar = new Scrollbar(Scrollbar.HORIZONTAL, 50, 1, 1, 100));
		this.powerBar.addAdjustmentListener(this);
		this.powerBar.disable();
		this.powerLabel.disable();

		this.mainContainer.add(new Label("www.falstad.com"));

		this.mainContainer.add(new Label(""));
		Font f = new Font("SansSerif", 0, 10);
		Label l;
		l = new Label("Current Circuit:");
		l.setFont(f);
		this.titleLabel = new Label("Label");
		this.titleLabel.setFont(f);

		this.mainContainer.add(l);
		this.mainContainer.add(this.titleLabel);

		this.setGrid();
		this.circuit.elmList = new Vector<CircuitElm>();
		this.undoStack = new Vector<String>();
		this.redoStack = new Vector<String>();

		this.scopeMan.scopes = new Scope[20];
		this.scopeMan.scopeColCount = new int[20];
		this.scopeMan.scopeCount = 0;

		this.circuitCanvas.setBackground(Color.black);
		this.circuitCanvas.setForeground(Color.lightGray);

		this.elmMenu = new PopupMenu();
		this.elmEditMenuItem = this.getMenuItem("Edit");
		this.elmMenu.add(this.elmEditMenuItem);
		this.elmMenu.add(this.elmScopeMenuItem = this.getMenuItem("View in Scope"));
		this.elmMenu.add(this.elmCutMenuItem = this.getMenuItem("Cut"));
		this.elmMenu.add(this.elmCopyMenuItem = this.getMenuItem("Copy"));
		this.elmMenu.add(this.elmDeleteMenuItem = this.getMenuItem("Delete"));
		this.mainContainer.add(this.elmMenu);

		this.scopeMenu = this.buildScopeMenu(false);
		this.transScopeMenu = this.buildScopeMenu(true);

		this.getSetupList(circuitsMenu, false);

		this.setMenuBar(menubar);

		if (this.startCircuitText != null)
		{
			this.readSetup(this.startCircuitText);
		}
		else if (this.circuit.stopMessage == null && this.startCircuit != null)
		{
			this.readSetupFile(this.startCircuit, this.startLabel);
		}

		Dimension screen = this.getToolkit().getScreenSize();

		this.setSize(860, 640);

		this.handleResize();

		Dimension x = this.getSize();
		this.setLocation((screen.width - x.width) / 2, (screen.height - x.height) / 2);
	}

	private PopupMenu buildScopeMenu(boolean t)
	{
		PopupMenu m = new PopupMenu();
		m.add(this.getMenuItem("Remove", "remove"));
		m.add(this.getMenuItem("Speed 2x", "speed2"));
		m.add(this.getMenuItem("Speed 1/2x", "speed1/2"));
		m.add(this.getMenuItem("Scale 2x", "scale"));
		m.add(this.getMenuItem("Max Scale", "maxscale"));
		m.add(this.getMenuItem("Stack", "stack"));
		m.add(this.getMenuItem("Unstack", "unstack"));
		m.add(this.getMenuItem("Reset", "reset"));
		if (t)
		{
			m.add(this.scopeIbMenuItem = this.getCheckItem("Show Ib"));
			m.add(this.scopeIcMenuItem = this.getCheckItem("Show Ic"));
			m.add(this.scopeIeMenuItem = this.getCheckItem("Show Ie"));
			m.add(this.scopeVbeMenuItem = this.getCheckItem("Show Vbe"));
			m.add(this.scopeVbcMenuItem = this.getCheckItem("Show Vbc"));
			m.add(this.scopeVceMenuItem = this.getCheckItem("Show Vce"));
			m.add(this.scopeVceIcMenuItem = this.getCheckItem("Show Vce vs Ic"));
		}
		else
		{
			m.add(this.scopeVMenuItem = this.getCheckItem("Show Voltage"));
			m.add(this.scopeIMenuItem = this.getCheckItem("Show Current"));
			m.add(this.scopePowerMenuItem = this.getCheckItem("Show Power Consumed"));
			m.add(this.scopeMaxMenuItem = this.getCheckItem("Show Peak Value"));
			m.add(this.scopeMinMenuItem = this.getCheckItem("Show Negative Peak Value"));
			m.add(this.scopeFreqMenuItem = this.getCheckItem("Show Frequency"));
			m.add(this.scopeVIMenuItem = this.getCheckItem("Show V vs I"));
			m.add(this.scopeXYMenuItem = this.getCheckItem("Plot X/Y"));
			m.add(this.scopeSelectYMenuItem = this.getMenuItem("Select Y", "selecty"));
			m.add(this.scopeResistMenuItem = this.getCheckItem("Show Resistance"));
		}
		this.mainContainer.add(m);
		return m;
	}

	private MenuItem getMenuItem(String s)
	{
		MenuItem mi = new MenuItem(s);
		mi.addActionListener(this);
		return mi;
	}

	private MenuItem getMenuItem(String s, String ac)
	{
		MenuItem mi = new MenuItem(s);
		mi.setActionCommand(ac);
		mi.addActionListener(this);
		return mi;
	}

	private CheckboxMenuItem getCheckItem(String s)
	{
		CheckboxMenuItem mi = new CheckboxMenuItem(s);
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
	private CheckboxMenuItem getClassCheckItem(String label, String className)
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

		element = this.constructElement(classPath, 0, 0);

		this.register(classPath, element);

		if (element.needsShortcut() && element.getDumpClass() == classPath)
		{
			dt = element.getDumpType();
			label += " (" + (char) dt + ")";
		}

		element.delete();

		return this.getCheckItem(label, className);
	}

	private CheckboxMenuItem getCheckItem(String s, String t)
	{
		CheckboxMenuItem mi = new CheckboxMenuItem(s);
		mi.addItemListener(this);
		mi.setActionCommand(t);
		return mi;
	}

	private void register(Class<?> c, CircuitElm elm)
	{
		Class<Scope> dumpClass = null;
		int elementId = elm.getDumpType();
		if (elementId == 0)
		{
			System.out.println("no dump type: " + c);
			return;
		}

		dumpClass = elm.getDumpClass();
		if (this.dumpTypes[elementId] == dumpClass)
		{
			return;
		}

		if (this.dumpTypes[elementId] != null)
		{
			System.out.println("dump type conflict: " + c + " " + this.dumpTypes[elementId]);
			return;
		}

		this.dumpTypes[elementId] = dumpClass;
	}

	public String getAppletInfo()
	{
		return "Circuit by Paul Falstad";
	}

	private void handleResize()
	{
		this.winSize = this.circuitCanvas.getSize();

		if (this.winSize.width == 0)
		{
			return;
		}

		this.dbimage = this.mainContainer.createImage(this.winSize.width, this.winSize.height);
		int h = this.winSize.height / 5;
		/*
		 * if (h < 128 && winSize.height > 300) h = 128;
		 */
		this.circuitArea = new Rectangle(0, 0, this.winSize.width, this.winSize.height - h);
		int i;
		int minx = 1000, maxx = 0, miny = 1000, maxy = 0;
		for (i = 0; i != this.circuit.elmList.size(); i++)
		{
			CircuitElm ce = this.circuit.getElement(i);
			// centered text causes problems when trying to center the circuit,
			// so we special-case it here
			if (!ce.isCenteredText())
			{
				minx = Math.min(ce.x, Math.min(ce.x2, minx));
				maxx = Math.max(ce.x, Math.max(ce.x2, maxx));
			}
			miny = Math.min(ce.y, Math.min(ce.y2, miny));
			maxy = Math.max(ce.y, Math.max(ce.y2, maxy));
		}
		// center circuit; we don't use snapGrid() because that rounds
		int dx = this.gridMask & (this.circuitArea.width - (maxx - minx)) / 2 - minx;
		int dy = this.gridMask & (this.circuitArea.height - (maxy - miny)) / 2 - miny;

		if (dx + minx < 0)
		{
			dx = this.gridMask & -minx;
		}

		if (dy + miny < 0)
		{
			dy = this.gridMask & -miny;
		}

		for (i = 0; i != this.circuit.elmList.size(); i++)
		{
			CircuitElm ce = this.circuit.getElement(i);
			ce.move(dx, dy);
		}

		// after moving elements, need this to avoid singular matrix probs
		this.needAnalyze();

		this.circuit.circuitBottom = 0;
	}

	private void destroyFrame()
	{
		this.dispose();
	}

	@Override
	public boolean handleEvent(Event ev)
	{
		if (ev.id == Event.WINDOW_DESTROY)
		{
			this.destroyFrame();
			return true;
		}

		return super.handleEvent(ev);
	}

	@Override
	public void paint(Graphics g)
	{
		this.circuitCanvas.repaint();
	}

	public void updateCircuit(Graphics realg)
	{
		CircuitElm realMouseElm;
		if (this.winSize == null || this.winSize.width == 0)
		{
			return;
		}

		if (this.circuit.analyzeFlag)
		{
			try
			{
				this.circuit.analyzeCircuit();
			}
			catch (CircuitAnalysisException e)
			{
				this.handleAnalysisException(e);
			}

			this.circuit.analyzeFlag = false;
		}

		if (CirSim.editDialog != null && CirSim.editDialog.elm instanceof CircuitElm)
		{
			this.mouseElm = (CircuitElm) CirSim.editDialog.elm;
		}

		realMouseElm = this.mouseElm;
		if (this.mouseElm == null)
		{
			this.mouseElm = this.circuit.stopElm;
		}
		this.setupScopes();

		Graphics g = null;

		g = this.dbimage.getGraphics();
		CircuitElm.selectColor = Color.cyan;
		if (this.printableCheckItem.getState())
		{
			CircuitElm.whiteColor = Color.black;
			CircuitElm.lightGrayColor = Color.black;
			g.setColor(Color.white);
		}
		else
		{
			CircuitElm.whiteColor = Color.white;
			CircuitElm.lightGrayColor = Color.lightGray;
			g.setColor(Color.black);
		}

		g.fillRect(0, 0, this.winSize.width, this.winSize.height);

		if (!this.stoppedCheck.getState())
		{
			try
			{
				this.runCircuit();
			}
			catch (CircuitAnalysisException e)
			{
				this.handleAnalysisException(e);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				this.circuit.analyzeFlag = true;
				this.circuitCanvas.repaint();

				return;
			}
		}

		if (!this.stoppedCheck.getState())
		{
			long sysTime = System.currentTimeMillis();
			if (this.lastTime != 0)
			{
				int inc = (int) (sysTime - this.lastTime);
				double c = this.currentBar.getValue();
				c = java.lang.Math.exp(c / 3.5 - 14.2);
				CircuitElm.currentMult = 1.7 * inc * c;
				if (!this.conventionCheckItem.getState())
				{
					CircuitElm.currentMult = -CircuitElm.currentMult;
				}
			}
			if (sysTime - this.secTime >= 1000)
			{
				this.secTime = sysTime;
			}
			this.lastTime = sysTime;
		}
		else
		{
			this.lastTime = 0;
		}
		CircuitElm.powerMult = Math.exp(this.powerBar.getValue() / 4.762 - 7);

		int i;
		Font oldfont = g.getFont();
		for (i = 0; i != this.circuit.elmList.size(); i++)
		{
			if (this.powerCheckItem.getState())
			{
				g.setColor(Color.gray);
			}
			/*
			 * else if (conductanceCheckItem.getState())
			 * g.setColor(Color.white);
			 */
			this.circuit.getElement(i).draw(g);
		}

		if (this.tempMouseMode == CirSim.MODE_DRAG_ROW || this.tempMouseMode == CirSim.MODE_DRAG_COLUMN
				|| this.tempMouseMode == CirSim.MODE_DRAG_POST || this.tempMouseMode == CirSim.MODE_DRAG_SELECTED)
		{
			for (i = 0; i != this.circuit.elmList.size(); i++)
			{
				CircuitElm ce = this.circuit.getElement(i);
				DrawUtil.drawPost(g, ce.x, ce.y);
				DrawUtil.drawPost(g, ce.x2, ce.y2);
			}
		}

		int badnodes = 0;

		// find bad connections, nodes not connected to other elements which
		// intersect other elements' bounding boxes
		for (i = 0; i != this.circuit.nodeList.size(); i++)
		{
			CircuitNode cn = this.circuit.getCircuitNode(i);
			if (!cn.isInternal() && cn.getSize() == 1)
			{
				int bb = 0, j;
				CircuitNodeLink cnl = cn.elementAt(0);
				for (j = 0; j != this.circuit.elmList.size(); j++)
				{
					if (cnl.elm != this.circuit.getElement(j)
							&& this.circuit.getElement(j).boundingBox.contains(cn.x, cn.y))
					{
						bb++;
					}
				}
				if (bb > 0)
				{
					g.setColor(Color.red);
					g.fillOval(cn.x - 3, cn.y - 3, 7, 7);
					badnodes++;
				}
			}
		}
		/*
		 * if (mouseElm != null) { g.setFont(oldfont); g.drawString("+",
		 * mouseElm.x+10, mouseElm.y); }
		 */
		if (this.dragElm != null && (this.dragElm.x != this.dragElm.x2 || this.dragElm.y != this.dragElm.y2))
		{
			this.dragElm.draw(g);
		}

		g.setFont(oldfont);

		int ct = this.scopeMan.scopeCount;

		if (this.circuit.stopMessage != null)
		{
			ct = 0;
		}

		for (i = 0; i != ct; i++)
		{
			this.scopeMan.scopes[i].draw(g);
		}

		g.setColor(CircuitElm.whiteColor);
		if (this.circuit.stopMessage != null)
		{
			g.drawString(this.circuit.stopMessage, 10, this.circuitArea.height);
		}
		else
		{
			if (this.circuit.circuitBottom == 0)
			{
				this.calcCircuitBottom();
			}
			String info[] = new String[10];
			if (this.mouseElm != null)
			{
				if (this.mousePost == -1)
				{
					this.mouseElm.getInfo(info);
				}
				else
				{
					info[0] = "V = " + CoreUtil.getUnitText(this.mouseElm.getPostVoltage(this.mousePost), "V");
					/*
					 * //shownodes for (i = 0; i != mouseElm.getPostCount();
					 * i++) info[0] += " " + mouseElm.nodes[i]; if
					 * (mouseElm.getVoltageSourceCount() > 0) info[0] += ";" +
					 * (mouseElm.getVoltageSource()+nodeList.size());
					 */
				}

			}
			else
			{
				CircuitElm.showFormat.setMinimumFractionDigits(2);
				info[0] = "t = " + CoreUtil.getUnitText(this.t, "s");
				CircuitElm.showFormat.setMinimumFractionDigits(0);
			}
			if (this.hintType != -1)
			{
				for (i = 0; info[i] != null; i++)
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
					info[i] = s;
				}
			}
			int x = 0;
			if (ct != 0)
			{
				x = this.scopeMan.scopes[ct - 1].rightEdge() + 20;
			}

			x = Math.max(x, this.winSize.width * 2 / 3);

			// count lines of data
			for (i = 0; info[i] != null; i++)
			{

			}

			if (badnodes > 0)
			{
				info[i++] = badnodes + (badnodes == 1 ? " bad connection" : " bad connections");
			}

			// find where to show data; below circuit, not too high unless we
			// need it
			int ybase = this.winSize.height - 15 * i - 5;
			ybase = Math.min(ybase, this.circuitArea.height);
			ybase = Math.max(ybase, this.circuit.circuitBottom);

			for (i = 0; info[i] != null; i++)
			{
				g.drawString(info[i], x, ybase + 15 * (i + 1));
			}

		}
		if (this.selectedArea != null)
		{
			g.setColor(CircuitElm.selectColor);
			g.drawRect(this.selectedArea.x, this.selectedArea.y, this.selectedArea.width, this.selectedArea.height);
		}
		this.mouseElm = realMouseElm;
		/*
		 * g.setColor(Color.white); g.drawString("Framerate: " + framerate, 10,
		 * 10); g.drawString("Steprate: " + steprate, 10, 30);
		 * g.drawString("Steprate/iter: " + (steprate/getIterCount()), 10, 50);
		 * g.drawString("iterc: " + (getIterCount()), 10, 70);
		 */

		realg.drawImage(this.dbimage, 0, 0, this);
		if (!this.stoppedCheck.getState() && this.circuit.circuitMatrix != null)
		{
			// Limit to 50 fps (thanks to J�rgen Kl�tzer for this)
			long delay = 1000 / 50 - (System.currentTimeMillis() - this.lastFrameTime);
			// realg.drawString("delay: " + delay, 10, 90);
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

			this.circuitCanvas.repaint(0);
		}
		this.lastFrameTime = this.lastTime;
	}

	@Deprecated
	private void setupScopes()
	{
		this.scopeMan.setupScopes(this.circuit, this.winSize, this.circuitArea);
	}

	private String getHint()
	{
		CircuitElm c1 = this.circuit.getElement(this.hintItem1);
		CircuitElm c2 = this.circuit.getElement(this.hintItem2);
		if (c1 == null || c2 == null)
		{
			return null;
		}
		if (this.hintType == CirSim.HINT_LC)
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
					+ CoreUtil.getUnitText(1 / (2 * CirSim.PI * Math.sqrt(ie.inductance * ce.capacitance)), "Hz");
		}
		if (this.hintType == CirSim.HINT_RC)
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
		if (this.hintType == CirSim.HINT_3DB_C)
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
			return "f.3db = " + CoreUtil.getUnitText(1 / (2 * CirSim.PI * re.resistance * ce.capacitance), "Hz");
		}
		if (this.hintType == CirSim.HINT_3DB_L)
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
			return "f.3db = " + CoreUtil.getUnitText(re.resistance / (2 * CirSim.PI * ie.inductance), "Hz");
		}
		if (this.hintType == CirSim.HINT_TWINT)
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
			return "fc = " + CoreUtil.getUnitText(1 / (2 * CirSim.PI * re.resistance * ce.capacitance), "Hz");
		}
		return null;
	}

	public void toggleSwitch(int n)
	{
		for (int i = 0; i != this.circuit.elmList.size(); i++)
		{
			CircuitElm ce = this.circuit.getElement(i);
			if (ce instanceof SwitchElm)
			{
				n--;
				if (n == 0)
				{
					((SwitchElm) ce).toggle();
					this.circuit.analyzeFlag = true;

					this.circuitCanvas.repaint();
					return;
				}
			}
		}
	}

	public void needAnalyze()
	{
		this.circuit.analyzeFlag = true;
		this.circuitCanvas.repaint();
	}

	@Deprecated
	public CircuitNode getCircuitNode(int n)
	{
		return this.circuit.getCircuitNode(n);
	}

	@Deprecated
	public CircuitElm getElement(int n)
	{
		return this.circuit.getElement(n);
	}

	@Deprecated
	private void calcCircuitBottom()
	{
		this.circuit.calcCircuitBottom();
	}

	@Deprecated
	public void stop(String msg, CircuitElm ce)
	{
		this.handleAnalysisException(new CircuitAnalysisException(msg, ce));
	}

	private void handleAnalysisException(CircuitAnalysisException e)
	{
		this.circuit.stopMessage = e.getTechnicalMessage();
		this.circuit.circuitMatrix = null;
		this.circuit.stopElm = e.getCauseElement();

		this.stoppedCheck.setState(true);

		this.circuit.analyzeFlag = false;

		this.circuitCanvas.repaint();
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
		return .1 * Math.exp((this.speedBar.getValue() - 61) / 24.);
	}

	private void runCircuit() throws CircuitAnalysisException
	{
		if (this.circuit.circuitMatrix == null || this.circuit.elmList.size() == 0)
		{
			this.circuit.circuitMatrix = null;
			return;
		}

		int iter;
		// int maxIter = getIterCount();
		boolean debugprint = this.dumpMatrix;
		this.dumpMatrix = false;
		long steprate = (long) (160 * this.getIterCount());
		long tm = System.currentTimeMillis();
		long lit = this.lastIterTime;
		if (1000 >= steprate * (tm - this.lastIterTime))
		{
			return;
		}
		for (iter = 1;; iter++)
		{
			int i, j, k, subiter;
			for (i = 0; i != this.circuit.elmList.size(); i++)
			{
				CircuitElm ce = this.circuit.getElement(i);

				try
				{
					ce.startIteration();
				}
				catch (CircuitAnalysisException e)
				{
					this.handleAnalysisException(e);
				}

			}
			final int subiterCount = 5000;
			for (subiter = 0; subiter != subiterCount; subiter++)
			{
				this.circuit.converged = true;
				this.subIterations = subiter;
				for (i = 0; i != this.circuit.circuitMatrixSize; i++)
				{
					this.circuit.circuitRightSide[i] = this.circuit.origRightSide[i];
				}
				if (this.circuit.circuitNonLinear)
				{
					for (i = 0; i != this.circuit.circuitMatrixSize; i++)
					{
						for (j = 0; j != this.circuit.circuitMatrixSize; j++)
						{
							this.circuit.circuitMatrix[i][j] = this.circuit.origMatrix[i][j];
						}
					}
				}
				for (i = 0; i != this.circuit.elmList.size(); i++)
				{
					CircuitElm ce = this.circuit.getElement(i);

					try
					{
						ce.doStep();
					}
					catch (CircuitAnalysisException e)
					{
						this.handleAnalysisException(e);
					}
				}

				if (this.circuit.stopMessage != null)
				{
					return;
				}

				boolean printit = debugprint;
				debugprint = false;

				for (j = 0; j != this.circuit.circuitMatrixSize; j++)
				{
					for (i = 0; i != this.circuit.circuitMatrixSize; i++)
					{
						double x = this.circuit.circuitMatrix[i][j];
						if (Double.isNaN(x) || Double.isInfinite(x))
						{
							throw new CircuitAnalysisException("nan/infinite matrix!");
						}
					}
				}
				if (printit)
				{
					for (j = 0; j != this.circuit.circuitMatrixSize; j++)
					{
						for (i = 0; i != this.circuit.circuitMatrixSize; i++)
						{
							System.out.print(this.circuit.circuitMatrix[j][i] + ",");
						}
						System.out.print("  " + this.circuit.circuitRightSide[j] + "\n");
					}
					System.out.print("\n");
				}
				if (this.circuit.circuitNonLinear)
				{
					if (this.circuit.converged && subiter > 0)
					{
						break;
					}

					if (!CoreUtil.luFactor(this.circuit.circuitMatrix, this.circuit.circuitMatrixSize,
							this.circuit.circuitPermute))
					{
						throw new CircuitAnalysisException("Singular matrix!");
					}
				}

				CoreUtil.luSolve(this.circuit.circuitMatrix, this.circuit.circuitMatrixSize,
						this.circuit.circuitPermute, this.circuit.circuitRightSide);

				for (j = 0; j != this.circuit.circuitMatrixFullSize; j++)
				{
					RowInfo ri = this.circuit.circuitRowInfo[j];
					double res = 0;
					if (ri.type == RowInfo.ROW_CONST)
					{
						res = ri.value;
					}
					else
					{
						res = this.circuit.circuitRightSide[ri.mapCol];
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
					if (j < this.circuit.nodeList.size() - 1)
					{
						CircuitNode cn = this.circuit.getCircuitNode(j + 1);
						for (k = 0; k != cn.getSize(); k++)
						{
							CircuitNodeLink cnl = cn.elementAt(k);
							cnl.elm.setNodeVoltage(cnl.num, res);
						}
					}
					else
					{
						int ji = j - (this.circuit.nodeList.size() - 1);
						// System.out.println("setting vsrc " + ji + " to " +
						// res);
						this.circuit.voltageSources[ji].setCurrent(ji, res);
					}
				}
				if (!this.circuit.circuitNonLinear)
				{
					break;
				}
			}
			if (subiter > 5)
			{
				System.out.print("converged after " + subiter + " iterations\n");
			}
			if (subiter == subiterCount)
			{
				this.stop("Convergence failed!", null);
				break;
			}

			this.t += this.timeStep;
			for (i = 0; i != this.scopeMan.scopeCount; i++)
			{
				this.scopeMan.scopes[i].timeStep();
			}
			tm = System.currentTimeMillis();
			lit = tm;
			if (iter * 1000 >= steprate * (tm - this.lastIterTime) || tm - this.lastFrameTime > 500)
			{
				break;
			}
		}
		this.lastIterTime = lit;
		// System.out.println((System.currentTimeMillis()-lastFrameTime)/(double)
		// iter);
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

	@Deprecated
	private void stackAll()
	{
		this.scopeMan.stackAll();
	}

	@Deprecated
	private void unstackAll()
	{
		this.scopeMan.unstackAll();
	}

	private void doEdit(Editable eable)
	{
		this.circuit.clearSelection();
		this.pushUndo();
		if (CirSim.editDialog != null)
		{
			this.requestFocus();
			CirSim.editDialog.setVisible(false);
			CirSim.editDialog = null;
		}
		CirSim.editDialog = new EditDialog(eable, this);
		CirSim.editDialog.show();
	}

	/**
	 * Montre un dialog de migration.
	 */
	private void showMigrationDialog()
	{
		String dump = this.dumpCircuit();
		MigrationWizard dialog = new MigrationWizard(this.mainContainer, dump, this.winSize);

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
		String tempDump = "";

		int f = 0;

		// f = this.dotsCheckItem.getState() ? 1 : 0;
		if (this.dotsCheckItem.getState())
		{
			f = 1;
		}

		// f |= this.smallGridCheckItem.getState() ? 2 : 0;
		if (this.smallGridCheckItem.getState())
		{
			f |= 2;
		}

		// f |= this.voltsCheckItem.getState() ? 0 : 4;
		if (!this.voltsCheckItem.getState())
		{
			f |= 4;
		}

		// f |= this.powerCheckItem.getState() ? 8 : 0;
		if (this.powerCheckItem.getState())
		{
			f |= 8;
		}

		// f |= this.showValuesCheckItem.getState() ? 0 : 16;
		if (!this.showValuesCheckItem.getState())
		{
			f |= 16;
		}

		// 32 = linear scale in afilter
		// Construire le String de dump
		dump = "$ ";

		dump += f + " ";
		dump += this.timeStep + " ";
		dump += this.getIterCount() + " ";
		dump += this.currentBar.getValue() + " ";
		dump += CircuitElm.voltageRange + " ";
		dump += this.powerBar.getValue();

		dump += "\n";

		dump += this.circuit.createDump();

		for (int i = 0; i < this.scopeMan.scopeCount; i++)
		{
			tempDump = this.scopeMan.scopes[i].dump();
			if (tempDump != null)
			{
				dump += tempDump + "\n";
			}
		}

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

	private void getSetupList(Menu menu, boolean retry)
	{
		Menu stack[] = new Menu[6];
		int stackptr = 0;
		stack[stackptr++] = menu;
		try
		{
			URL url = new URL(this.getCodeBase() + "setuplist.txt");
			ByteArrayOutputStream ba = this.readUrlData(url);
			byte b[] = ba.toByteArray();
			int len = ba.size();
			int p;
			if (len == 0 || b[0] != '#')
			{
				// got a redirect, try again
				this.getSetupList(menu, true);
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
					Menu n = new Menu(line.substring(1));
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
		this.t = 0;

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

		this.titleLabel.setText(title);
	}

	private void readSetup(byte b[], int len, boolean retain)
	{
		int i;
		if (!retain)
		{
			for (i = 0; i != this.circuit.elmList.size(); i++)
			{
				CircuitElm ce = this.circuit.getElement(i);
				ce.delete();
			}

			this.circuit.elmList.removeAllElements();
			this.hintType = -1;
			this.timeStep = 5e-6;
			this.dotsCheckItem.setState(true);
			this.smallGridCheckItem.setState(false);
			this.powerCheckItem.setState(false);
			this.voltsCheckItem.setState(true);
			this.showValuesCheckItem.setState(true);
			this.setGrid();
			this.speedBar.setValue(117); // 57
			this.currentBar.setValue(50);
			this.powerBar.setValue(50);
			CircuitElm.voltageRange = 5;
			this.scopeMan.scopeCount = 0;
		}

		this.circuitCanvas.repaint();

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
					Class<?> cls = this.dumpTypes[tint];

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
					this.circuit.elmList.addElement(ce);
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
		int flags = new Integer(st.nextToken()).intValue();
		this.dotsCheckItem.setState((flags & 1) != 0);
		this.smallGridCheckItem.setState((flags & 2) != 0);
		this.voltsCheckItem.setState((flags & 4) == 0);
		this.powerCheckItem.setState((flags & 8) == 8);
		this.showValuesCheckItem.setState((flags & 16) == 0);
		this.timeStep = new Double(st.nextToken()).doubleValue();
		double sp = new Double(st.nextToken()).doubleValue();
		int sp2 = (int) (Math.log(10 * sp) * 24 + 61.5);
		// int sp2 = (int) (Math.log(sp)*24+1.5);
		this.speedBar.setValue(sp2);
		this.currentBar.setValue(new Integer(st.nextToken()).intValue());
		CircuitElm.voltageRange = new Double(st.nextToken()).doubleValue();
		try
		{
			this.powerBar.setValue(new Integer(st.nextToken()).intValue());
		}
		catch (Exception e)
		{
		}
		this.setGrid();
	}

	public int snapGrid(int x)
	{
		return x + this.gridRound & this.gridMask;
	}

	private boolean doSwitch(int x, int y)
	{
		if (this.mouseElm == null || !(this.mouseElm instanceof SwitchElm))
		{
			return false;
		}

		SwitchElm se = (SwitchElm) this.mouseElm;
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
		int dx = x - this.dragX;
		int dy = y - this.dragY;
		if (dx == 0 && dy == 0)
		{
			return;
		}
		int i;
		for (i = 0; i != this.circuit.elmList.size(); i++)
		{
			CircuitElm ce = this.circuit.getElement(i);
			ce.move(dx, dy);
		}
		this.removeZeroLengthElements();
	}

	private void dragRow(int x, int y)
	{
		int dy = y - this.dragY;
		if (dy == 0)
		{
			return;
		}
		int i;
		for (i = 0; i != this.circuit.elmList.size(); i++)
		{
			CircuitElm ce = this.circuit.getElement(i);
			if (ce.y == this.dragY)
			{
				ce.movePoint(0, 0, dy);
			}
			if (ce.y2 == this.dragY)
			{
				ce.movePoint(1, 0, dy);
			}
		}
		this.removeZeroLengthElements();
	}

	private void dragColumn(int x, int y)
	{
		int dx = x - this.dragX;
		if (dx == 0)
		{
			return;
		}
		int i;
		for (i = 0; i != this.circuit.elmList.size(); i++)
		{
			CircuitElm ce = this.circuit.getElement(i);
			if (ce.x == this.dragX)
			{
				ce.movePoint(0, dx, 0);
			}
			if (ce.x2 == this.dragX)
			{
				ce.movePoint(1, dx, 0);
			}
		}
		this.removeZeroLengthElements();
	}

	private boolean dragSelected(int x, int y)
	{
		boolean me = false;
		if (this.mouseElm != null && !this.mouseElm.isSelected())
		{
			this.mouseElm.setSelected(me = true);
		}

		// snap grid, unless we're only dragging text elements
		int i;
		for (i = 0; i != this.circuit.elmList.size(); i++)
		{
			CircuitElm ce = this.circuit.getElement(i);
			if (ce.isSelected() && !(ce instanceof TextElm))
			{
				break;
			}
		}
		if (i != this.circuit.elmList.size())
		{
			x = this.snapGrid(x);
			y = this.snapGrid(y);
		}

		int dx = x - this.dragX;
		int dy = y - this.dragY;
		if (dx == 0 && dy == 0)
		{
			// don't leave mouseElm selected if we selected it above
			if (me)
			{
				this.mouseElm.setSelected(false);
			}
			return false;
		}
		boolean allowed = true;

		// check if moves are allowed
		for (i = 0; allowed && i != this.circuit.elmList.size(); i++)
		{
			CircuitElm ce = this.circuit.getElement(i);
			if (ce.isSelected() && !ce.allowMove(dx, dy))
			{
				allowed = false;
			}
		}

		if (allowed)
		{
			for (i = 0; i != this.circuit.elmList.size(); i++)
			{
				CircuitElm ce = this.circuit.getElement(i);
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
			this.mouseElm.setSelected(false);
		}

		return allowed;
	}

	private void dragPost(int x, int y)
	{
		if (this.draggingPost == -1)
		{
			this.draggingPost = CoreUtil.distanceSq(this.mouseElm.x, this.mouseElm.y, x, y) > CoreUtil.distanceSq(
					this.mouseElm.x2, this.mouseElm.y2, x, y) ? 1 : 0;
		}
		int dx = x - this.dragX;
		int dy = y - this.dragY;
		if (dx == 0 && dy == 0)
		{
			return;
		}
		this.mouseElm.movePoint(this.draggingPost, dx, dy);
		this.needAnalyze();
	}

	private void selectArea(int x, int y)
	{
		int x1 = Math.min(x, this.initDragX);
		int x2 = Math.max(x, this.initDragX);
		int y1 = Math.min(y, this.initDragY);
		int y2 = Math.max(y, this.initDragY);
		this.selectedArea = new Rectangle(x1, y1, x2 - x1, y2 - y1);

		for (int i = 0; i != this.circuit.elmList.size(); i++)
		{
			CircuitElm ce = this.circuit.getElement(i);
			ce.selectRect(this.selectedArea);
		}
	}

	private void setSelectedElm(CircuitElm cs)
	{
		for (int i = 0; i != this.circuit.elmList.size(); i++)
		{
			CircuitElm ce = this.circuit.getElement(i);
			ce.setSelected(ce == cs);
		}
		this.mouseElm = cs;
	}

	private void removeZeroLengthElements()
	{
		this.circuit.removeZeroLengthElements();

		this.needAnalyze();
	}

	private CircuitElm constructElement(Class<?> classType, int x0, int y0)
	{
		// find element class
		Class<?> carr[] = null;
		// carr[0] = getClass();

		Object oarr[] = null;
		Constructor<?> constructor = null;
		CircuitElm elem = null;

		carr = new Class[2];
		carr[1] = int.class;
		carr[0] = int.class;

		try
		{
			System.out.println("CirSim construct:" + classType.toString());
			constructor = classType.getConstructor(carr);

			// invoke constructor with starting coordinates
			oarr = new Object[2];
			oarr[0] = new Integer(x0);
			oarr[1] = new Integer(y0);

			elem = (CircuitElm) constructor.newInstance(oarr);
		}
		catch (NoSuchMethodException e)
		{
			System.out.println("caught NoSuchMethodException " + classType);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}

		return elem;
	}

	private void doPopupMenu(MouseEvent e)
	{
		this.menuElm = this.mouseElm;
		this.menuScope = -1;

		if (this.scopeSelected != -1)
		{
			PopupMenu m = this.scopeMan.scopes[this.scopeSelected].getMenu();
			this.menuScope = this.scopeSelected;
			if (m != null)
			{
				m.show(e.getComponent(), e.getX(), e.getY());
			}
		}
		else if (this.mouseElm != null)
		{
			this.elmEditMenuItem.setEnabled(this.mouseElm.getEditInfo(0) != null);
			this.elmScopeMenuItem.setEnabled(this.mouseElm.canViewInScope());
			this.elmMenu.show(e.getComponent(), e.getX(), e.getY());
		}
		else
		{
			this.doMainMenuChecks(this.mainMenu);
			this.mainMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private void doMainMenuChecks(Menu m)
	{
		if (m == this.optionsMenu)
		{
			return;
		}

		for (int i = 0; i != m.getItemCount(); i++)
		{
			MenuItem mc = m.getItem(i);
			if (mc instanceof Menu)
			{
				this.doMainMenuChecks((Menu) mc);
			}
			if (mc instanceof CheckboxMenuItem)
			{
				CheckboxMenuItem cmi = (CheckboxMenuItem) mc;
				cmi.setState(this.mouseModeStr.compareTo(cmi.getActionCommand()) == 0);
			}
		}
	}

	private void enableItems()
	{
		if (this.powerCheckItem.getState())
		{
			this.powerBar.enable();
			this.powerLabel.enable();
		}
		else
		{
			this.powerBar.disable();
			this.powerLabel.disable();
		}
		this.enableUndoRedo();
	}

	private void setGrid()
	{
		if (this.smallGridCheckItem.getState())
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
		this.redoStack.removeAllElements();
		String s = this.dumpCircuit();
		if (this.undoStack.size() > 0 && s.compareTo(this.undoStack.lastElement()) == 0)
		{
			return;
		}
		this.undoStack.add(s);
		this.enableUndoRedo();
	}

	private void doUndo()
	{
		if (this.undoStack.size() == 0)
		{
			return;
		}

		this.redoStack.add(this.dumpCircuit());
		String s = this.undoStack.remove(this.undoStack.size() - 1);
		this.readSetup(s);
		this.enableUndoRedo();
	}

	private void doRedo()
	{
		if (this.redoStack.size() == 0)
		{
			return;
		}

		this.undoStack.add(this.dumpCircuit());

		String s = this.redoStack.remove(this.redoStack.size() - 1);

		this.readSetup(s);
		this.enableUndoRedo();
	}

	private void enableUndoRedo()
	{
		this.redoItem.setEnabled(this.redoStack.size() > 0);
		this.undoItem.setEnabled(this.undoStack.size() > 0);
	}

	private void setMenuSelection()
	{
		if (this.menuElm != null)
		{
			if (this.menuElm.selected)
			{
				return;
			}

			this.clearSelection();
			this.menuElm.setSelected(true);
		}
	}

	private void doCut()
	{
		this.pushUndo();
		this.setMenuSelection();
		this.clipboard = "";

		for (int i = this.circuit.elmList.size() - 1; i >= 0; i--)
		{
			CircuitElm ce = this.circuit.getElement(i);
			if (ce.isSelected())
			{
				this.clipboard += ce.dump() + "\n";
				ce.delete();
				this.circuit.elmList.removeElementAt(i);
			}
		}
		this.enablePaste();
		this.needAnalyze();
	}

	private void doDelete()
	{
		this.pushUndo();
		this.setMenuSelection();

		for (int i = this.circuit.elmList.size() - 1; i >= 0; i--)
		{
			CircuitElm ce = this.circuit.getElement(i);
			if (ce.isSelected())
			{
				ce.delete();
				this.circuit.elmList.removeElementAt(i);
			}
		}

		this.needAnalyze();
	}

	private void doCopy()
	{
		this.clipboard = "";
		this.setMenuSelection();
		for (int i = this.circuit.elmList.size() - 1; i >= 0; i--)
		{
			CircuitElm ce = this.circuit.getElement(i);
			if (ce.isSelected())
			{
				this.clipboard += ce.dump() + "\n";
			}

		}

		this.enablePaste();
	}

	private void enablePaste()
	{
		this.pasteItem.setEnabled(this.clipboard.length() > 0);
	}

	private void doPaste()
	{
		this.pushUndo();
		this.circuit.clearSelection();
		int i;
		Rectangle oldbb = null;
		for (i = 0; i != this.circuit.elmList.size(); i++)
		{
			CircuitElm ce = this.circuit.getElement(i);
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
		int oldsz = this.circuit.elmList.size();
		this.readSetup(this.clipboard, true);

		// select new items
		Rectangle newbb = null;
		for (i = oldsz; i != this.circuit.elmList.size(); i++)
		{
			CircuitElm ce = this.circuit.getElement(i);
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
			int spacew = this.circuitArea.width - oldbb.width - newbb.width;
			int spaceh = this.circuitArea.height - oldbb.height - newbb.height;
			if (spacew > spaceh)
			{
				dx = this.snapGrid(oldbb.x + oldbb.width - newbb.x + this.gridSize);
			}
			else
			{
				dy = this.snapGrid(oldbb.y + oldbb.height - newbb.y + this.gridSize);
			}

			for (i = oldsz; i != this.circuit.elmList.size(); i++)
			{
				CircuitElm ce = this.circuit.getElement(i);
				ce.move(dx, dy);
			}
			// center circuit
			this.handleResize();
		}
		this.needAnalyze();
	}

	@Deprecated
	private void clearSelection()
	{
		this.circuit.clearSelection();
	}

	@Deprecated
	private void doSelectAll()
	{
		this.circuit.doSelectAll();
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
		this.circuitCanvas.repaint();
	}

	@Override
	public void componentResized(ComponentEvent e)
	{
		this.handleResize();
		this.circuitCanvas.repaint(100);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String ac = e.getActionCommand();
		if (e.getSource() == this.resetButton)
		{
			int i;

			// on IE, drawImage() stops working inexplicably every once in
			// a while. Recreating it fixes the problem, so we do that here.
			this.dbimage = this.mainContainer.createImage(this.winSize.width, this.winSize.height);

			for (i = 0; i != this.circuit.elmList.size(); i++)
			{
				this.circuit.getElement(i).reset();
			}
			for (i = 0; i != this.scopeMan.scopeCount; i++)
			{
				this.scopeMan.scopes[i].resetGraph();
			}
			this.circuit.analyzeFlag = true;
			this.t = 0;
			this.stoppedCheck.setState(false);
			this.circuitCanvas.repaint();
		}

		if (e.getSource() == this.dumpMatrixButton)
		{
			this.dumpMatrix = true;
		}

		if (e.getSource() == this.exportItem)
		{
			this.showMigrationDialog();
		}

		if (e.getSource() == this.optionsItem)
		{
			this.doEdit(new EditOptions(this));
		}

		if (e.getSource() == this.importItem)
		{
			this.showMigrationDialog();
		}

		if (e.getSource() == this.undoItem)
		{
			this.doUndo();
		}

		if (e.getSource() == this.redoItem)
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
			this.doSelectAll();
		}

		if (e.getSource() == this.exitItem)
		{
			this.destroyFrame();
			return;
		}

		if (ac.compareTo("stackAll") == 0)
		{
			this.stackAll();
		}

		if (ac.compareTo("unstackAll") == 0)
		{
			this.unstackAll();
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
				if (this.scopeMan.scopes[i].elm == null)
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
			this.scopeMan.scopes[i].setElm(this.menuElm);
		}

		if (this.menuScope != -1)
		{
			if (ac.compareTo("remove") == 0)
			{
				this.scopeMan.scopes[this.menuScope].setElm(null);
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
			this.circuitCanvas.repaint();
		}

		if (ac.indexOf("setup ") == 0)
		{
			this.pushUndo();
			this.readSetupFile(ac.substring(6), ((MenuItem) e.getSource()).getLabel());
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		this.circuitCanvas.repaint(this.pause);
		Object mi = e.getItemSelectable();
		if (mi == this.stoppedCheck)
		{
			return;
		}
		if (mi == this.smallGridCheckItem)
		{
			this.setGrid();
		}
		if (mi == this.powerCheckItem)
		{
			if (this.powerCheckItem.getState())
			{
				this.voltsCheckItem.setState(false);
			}
			else
			{
				this.voltsCheckItem.setState(true);
			}
		}
		if (mi == this.voltsCheckItem && this.voltsCheckItem.getState())
		{
			this.powerCheckItem.setState(false);
		}
		this.enableItems();
		if (this.menuScope != -1)
		{
			Scope sc = this.scopeMan.scopes[this.menuScope];
			sc.handleMenu(e, mi);
		}
		if (mi instanceof CheckboxMenuItem)
		{
			MenuItem mmi = (MenuItem) mi;
			this.mouseMode = CirSim.MODE_ADD_ELM;
			String s = mmi.getActionCommand();

			if (s.length() > 0)
			{
				this.mouseModeStr = s;
			}

			if (s.compareTo("DragAll") == 0)
			{
				this.mouseMode = CirSim.MODE_DRAG_ALL;
			}
			else if (s.compareTo("DragRow") == 0)
			{
				this.mouseMode = CirSim.MODE_DRAG_ROW;
			}
			else if (s.compareTo("DragColumn") == 0)
			{
				this.mouseMode = CirSim.MODE_DRAG_COLUMN;
			}
			else if (s.compareTo("DragSelected") == 0)
			{
				this.mouseMode = CirSim.MODE_DRAG_SELECTED;
			}
			else if (s.compareTo("DragPost") == 0)
			{
				this.mouseMode = CirSim.MODE_DRAG_POST;
			}
			else if (s.compareTo("Select") == 0)
			{
				this.mouseMode = CirSim.MODE_SELECT;
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
			this.tempMouseMode = this.mouseMode;
		}
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		System.out.print(((Scrollbar) e.getSource()).getValue() + "\n");
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
				Class<?> c = CirSim.this.dumpTypes[e.getKeyChar()];
				if (c == null || c == Scope.class)
				{
					return;
				}

				CircuitElm elm = null;
				elm = CirSim.this.constructElement(c, 0, 0);
				if (elm == null || !(elm.needsShortcut() && elm.getDumpClass() == c))
				{
					return;
				}

				CirSim.this.mouseMode = CirSim.MODE_ADD_ELM;
				CirSim.this.mouseModeStr = c.getName();
				CirSim.this.addingClass = c;
			}

			if (e.getKeyChar() == ' ')
			{
				CirSim.this.mouseMode = CirSim.MODE_SELECT;
				CirSim.this.mouseModeStr = "Select";
			}

			CirSim.this.tempMouseMode = CirSim.this.mouseMode;
		}
	}

	private class MyMouseListener implements MouseListener
	{

		@Override
		public void mouseClicked(MouseEvent e)
		{
			if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
			{
				if (CirSim.this.mouseMode == CirSim.MODE_SELECT || CirSim.this.mouseMode == CirSim.MODE_DRAG_SELECTED)
				{
					CirSim.this.clearSelection();
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			CirSim.this.scopeSelected = -1;
			CirSim.this.mouseElm = CirSim.this.plotXElm = CirSim.this.plotYElm = null;
			CirSim.this.circuitCanvas.repaint();
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
				CirSim.this.doPopupMenu(e);
				return;
			}

			if ((modif & InputEvent.BUTTON1_MASK) != 0)
			{
				// left mouse
				CirSim.this.tempMouseMode = CirSim.this.mouseMode;
				if ((ex & InputEvent.ALT_DOWN_MASK) != 0 && (ex & InputEvent.META_DOWN_MASK) != 0)
				{
					CirSim.this.tempMouseMode = CirSim.MODE_DRAG_COLUMN;
				}
				else if ((ex & InputEvent.ALT_DOWN_MASK) != 0 && (ex & InputEvent.SHIFT_DOWN_MASK) != 0)
				{
					CirSim.this.tempMouseMode = CirSim.MODE_DRAG_ROW;
				}
				else if ((ex & InputEvent.SHIFT_DOWN_MASK) != 0)
				{
					CirSim.this.tempMouseMode = CirSim.MODE_SELECT;
				}
				else if ((ex & InputEvent.ALT_DOWN_MASK) != 0)
				{
					CirSim.this.tempMouseMode = CirSim.MODE_DRAG_ALL;
				}
				else if ((ex & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK)) != 0)
				{
					CirSim.this.tempMouseMode = CirSim.MODE_DRAG_POST;
				}
			}
			else if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
			{
				// right mouse
				if ((ex & InputEvent.SHIFT_DOWN_MASK) != 0)
				{
					CirSim.this.tempMouseMode = CirSim.MODE_DRAG_ROW;
				}
				else if ((ex & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK)) != 0)
				{
					CirSim.this.tempMouseMode = CirSim.MODE_DRAG_COLUMN;
				}
				else
				{
					return;
				}
			}

			if (CirSim.this.tempMouseMode != CirSim.MODE_SELECT
					&& CirSim.this.tempMouseMode != CirSim.MODE_DRAG_SELECTED)
			{
				System.out.println("clear selection");
				CirSim.this.clearSelection();
			}

			if (CirSim.this.doSwitch(x, y))
			{
				return;
			}

			CirSim.this.pushUndo();

			CirSim.this.initDragX = x;
			CirSim.this.initDragY = y;

			if (CirSim.this.tempMouseMode != CirSim.MODE_ADD_ELM || CirSim.this.addingClass == null)
			{
				return;
			}

			int x0 = CirSim.this.snapGrid(x);
			int y0 = CirSim.this.snapGrid(y);

			if (!CirSim.this.circuitArea.contains(x0, y0))
			{
				return;
			}

			CirSim.this.dragElm = CirSim.this.constructElement(CirSim.this.addingClass, x0, y0);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			int ex = e.getModifiersEx();
			boolean circuitChanged = false;

			if ((ex & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK)) == 0
					&& e.isPopupTrigger())
			{
				CirSim.this.doPopupMenu(e);
				return;
			}

			CirSim.this.tempMouseMode = CirSim.this.mouseMode;
			CirSim.this.selectedArea = null;

			if (CirSim.this.heldSwitchElm != null)
			{
				CirSim.this.heldSwitchElm.mouseUp();
				CirSim.this.heldSwitchElm = null;
				circuitChanged = true;
			}

			if (CirSim.this.dragElm != null)
			{
				// if the element is zero size then don't create it
				if (CirSim.this.dragElm.x == CirSim.this.dragElm.x2 && CirSim.this.dragElm.y == CirSim.this.dragElm.y2)
				{
					CirSim.this.dragElm.delete();
				}
				else
				{
					CirSim.this.circuit.elmList.addElement(CirSim.this.dragElm);
					circuitChanged = true;
				}

				CirSim.this.dragElm = null;
			}

			if (circuitChanged)
			{
				CirSim.this.needAnalyze();
			}

			if (CirSim.this.dragElm != null)
			{
				CirSim.this.dragElm.delete();
			}

			CirSim.this.dragElm = null;
			CirSim.this.circuitCanvas.repaint();
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

			if (CirSim.this.dragElm != null)
			{
				CirSim.this.dragElm.drag(e.getX(), e.getY());
			}

			boolean success = true;

			switch (CirSim.this.tempMouseMode)
			{
			case MODE_DRAG_ALL:
				CirSim.this.dragAll(CirSim.this.snapGrid(e.getX()), CirSim.this.snapGrid(e.getY()));
				break;
			case MODE_DRAG_ROW:
				CirSim.this.dragRow(CirSim.this.snapGrid(e.getX()), CirSim.this.snapGrid(e.getY()));
				break;
			case MODE_DRAG_COLUMN:
				CirSim.this.dragColumn(CirSim.this.snapGrid(e.getX()), CirSim.this.snapGrid(e.getY()));
				break;
			case MODE_DRAG_POST:
				if (CirSim.this.mouseElm != null)
				{
					CirSim.this.dragPost(CirSim.this.snapGrid(e.getX()), CirSim.this.snapGrid(e.getY()));
				}
				break;
			case MODE_SELECT:
				if (CirSim.this.mouseElm == null)
				{
					CirSim.this.selectArea(e.getX(), e.getY());
				}
				else
				{
					CirSim.this.tempMouseMode = CirSim.MODE_DRAG_SELECTED;
					success = CirSim.this.dragSelected(e.getX(), e.getY());
				}
				break;
			case MODE_DRAG_SELECTED:
				success = CirSim.this.dragSelected(e.getX(), e.getY());
				break;
			}

			if (success)
			{
				if (CirSim.this.tempMouseMode == CirSim.MODE_DRAG_SELECTED && CirSim.this.mouseElm instanceof TextElm)
				{
					CirSim.this.dragX = e.getX();
					CirSim.this.dragY = e.getY();
				}
				else
				{
					CirSim.this.dragX = CirSim.this.snapGrid(e.getX());
					CirSim.this.dragY = CirSim.this.snapGrid(e.getY());
				}
			}
			CirSim.this.circuitCanvas.repaint(CirSim.this.pause);
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
			CirSim.this.dragX = CirSim.this.snapGrid(x);
			CirSim.this.dragY = CirSim.this.snapGrid(y);
			CirSim.this.draggingPost = -1;
			int i;
			CircuitElm origMouse = CirSim.this.mouseElm;
			CirSim.this.mouseElm = null;
			CirSim.this.mousePost = -1;
			CirSim.this.plotXElm = CirSim.this.plotYElm = null;
			int bestDist = 100000;
			int bestArea = 100000;

			for (i = 0; i < CirSim.this.circuit.elmList.size(); i++)
			{
				CircuitElm currentElement = CirSim.this.circuit.getElement(i);
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
							CirSim.this.mouseElm = currentElement;
						}
					}

					if (currentElement.getPostCount() == 0)
					{
						CirSim.this.mouseElm = currentElement;
					}
				}
			}

			CirSim.this.scopeSelected = -1;
			if (CirSim.this.mouseElm == null)
			{
				for (i = 0; i != CirSim.this.scopeMan.scopeCount; i++)
				{
					Scope s = CirSim.this.scopeMan.scopes[i];
					if (s.rect.contains(x, y))
					{
						s.select();
						CirSim.this.scopeSelected = i;
					}
				}
				// the mouse pointer was not in any of the bounding boxes, but
				// we
				// might still be close to a post
				for (i = 0; i != CirSim.this.circuit.elmList.size(); i++)
				{
					CircuitElm ce = CirSim.this.circuit.getElement(i);
					int j;
					int jn = ce.getPostCount();
					for (j = 0; j != jn; j++)
					{
						Point pt = ce.getPost(j);
						// int dist = CoreUtil.distanceSq(x, y, pt.x, pt.y);
						if (CoreUtil.distanceSq(pt.x, pt.y, x, y) < 26)
						{
							CirSim.this.mouseElm = ce;
							CirSim.this.mousePost = j;
							break;
						}
					}
				}
			}
			else
			{
				CirSim.this.mousePost = -1;
				// look for post close to the mouse pointer
				for (i = 0; i != CirSim.this.mouseElm.getPostCount(); i++)
				{
					Point pt = CirSim.this.mouseElm.getPost(i);
					if (CoreUtil.distanceSq(pt.x, pt.y, x, y) < 26)
					{
						CirSim.this.mousePost = i;
					}

				}
			}
			if (CirSim.this.mouseElm != origMouse)
			{
				CirSim.this.circuitCanvas.repaint();
			}

		}
	}

	@Deprecated
	private static ByteArrayOutputStream readUrlData(URL url) throws IOException
	{
		return CoreUtil.readUrlData(url);
	}

	@Deprecated
	private static URL getCodeBase()
	{
		return CoreUtil.getCodeBase();
	}

	public static void main(String args[])
	{
		CirSim c = new CirSim();

		c.setVisible(true);
		c.requestFocus();

	}
}
