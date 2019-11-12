/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package circuit.component;

import circuit.Units;
import circuit.Breadboard;
import circuit.SerializableDoubleProperty;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import circuit.ComponentGUI;

public class FET extends IntegratedCircuit implements ComponentGUI
{
	public static final int GATE = 0, DRAIN = 1, SOURCE = 2;
	public static final String FW_ACTIVE = "forward-active";
	public static final String RV_ACTIVE = "reverse-active";
	public static final String SATURATION = "saturation";
	public static final String CUT_OFF = "cut-off";
	
	private final Resistor channel;
	private final Resistor inputVoltage;
	private final Node node;
	
	protected final SerializableDoubleProperty transconductanceGain;
	protected final SerializableDoubleProperty thresholdVoltage;
	protected final SerializableDoubleProperty outputResistance;
	protected final SerializableDoubleProperty gateSourceVoltage;
	protected final SerializableDoubleProperty sourceCurrent;
	//protected final SerializableStringProperty stateOfOperation;
	
	public FET()
	{
		super(3);
		
		this.channel = new Resistor();
		this.inputVoltage = new Resistor();
		this.node = new Node(3);
		
		channel.resistance.set(Breadboard.BIG);
		inputVoltage.resistance.set(Breadboard.BIG);
		
		this.node.getPin(1).connect(this.channel.getPin(1));
		this.node.getPin(2).connect(this.inputVoltage.getPin(1));
		
		this.pins[GATE] = this.inputVoltage.getPin(0);
		this.pins[DRAIN] = this.channel.getPin(0);
		this.pins[SOURCE] = this.node.getPin(0);
		
		this.transconductanceGain = new SerializableDoubleProperty(this, "transconductance gain", Units.SIEMENS, 20);
		this.thresholdVoltage = new SerializableDoubleProperty(this, "threshold voltage", Units.VOLTS, 0.5);
		this.outputResistance = new SerializableDoubleProperty(this, "output resistance", Units.OHMS);
		this.gateSourceVoltage = new SerializableDoubleProperty(this, "gate-source voltage", Units.VOLTS);
		this.sourceCurrent = new SerializableDoubleProperty(this, "source current", Units.AMPS);
		this.outputResistance.bind(this.channel.resistance);
		this.gateSourceVoltage.bind(this.inputVoltage.getPin(0).voltageProperty().subtract(this.inputVoltage.getPin(1).voltageProperty()));
		this.sourceCurrent.bind(this.node.getPin(0).currentProperty());
		this.outputResistance.setTransient(true);
		this.gateSourceVoltage.setTransient(true);
		this.sourceCurrent.setTransient(true);
	}
	
	@Override
	public void tick(double elapsedTime)
	{
		double current = (this.gateSourceVoltage.get() - this.thresholdVoltage.get()) * this.transconductanceGain.get();
		double resistance = 
				Math.abs(this.channel.getPin(0).currentProperty().get()) *
				this.channel.resistance.get() / current;
		
		if (resistance < 0 || Breadboard.BIG < resistance || Double.isNaN(resistance))
			resistance = Breadboard.BIG;
		if (resistance < Breadboard.WIRE)
			resistance = Breadboard.WIRE;
		
		channel.resistance.set(resistance);
	}

	@Override
	public Set<CircuitComponent> getElements() { return makeSet(this.channel, this.inputVoltage, this.node); }

	private circuit.GraphicalNode nodeGUI;

	@Override
	public String getSimpleName() { return "Field effect transistor"; }
	
	@Override
	public void initGUI() { this.initGUI(new GraphicalNode(), 0, 0); }

	@Override
	public void initGUI(circuit.GraphicalNode n, int x, int y) { if (x == 0 && y == 0) nodeGUI = n; }

	@Override
	public circuit.GraphicalNode getGraphicalNode(int x, int y)
	{
		if (x == 0 && y == 0) return nodeGUI;
		return null;
	}
	
	public class GraphicalNode extends circuit.GraphicalNode
	{
		public GraphicalNode()
		{
			super(FET.this);
			
			this.pinsGUI[0] = null;
			this.pinsGUI[1] = this.getComponent().getPin(FET.SOURCE);
			this.pinsGUI[2] = this.getComponent().getPin(FET.GATE);
			this.pinsGUI[3] = this.getComponent().getPin(FET.DRAIN);
			
			final double D = 0.1;
			final double RADIUS = 0.3;			// Radius of circle relative to side
			
			Circle c = new Circle();
			c.centerXProperty().bind(this.widthProperty().divide(2));
			c.centerYProperty().bind(this.heightProperty().multiply((D+1)/2d));
			c.radiusProperty().bind(this.heightProperty().multiply(RADIUS));
			c.setFill(Color.WHITE);
			c.setStroke(Color.BLACK);
			
			final double BASE_POSITION = 0.2;		// Distance from base to center
			final double BASE_WIDTH = 0.3;
			final double BASE_DIST = 0.05;
			
			Line base1 = new Line();
			base1.startXProperty().bind(this.widthProperty().multiply((1-BASE_WIDTH)/2));
			base1.startYProperty().bind(this.heightProperty().multiply(BASE_POSITION+1/2d));
			base1.endXProperty().bind(this.widthProperty().multiply((1+BASE_WIDTH)/2));
			base1.endYProperty().bind(this.heightProperty().multiply(BASE_POSITION+1/2d));
			
			Line base2 = new Line();
			base2.startXProperty().bind(base1.startXProperty().add(this.widthProperty().multiply(BASE_DIST)));
			base2.startYProperty().bind(base1.startYProperty().add(this.heightProperty().multiply(BASE_DIST)));
			base2.endXProperty().bind(base1.endXProperty().subtract(this.widthProperty().multiply(BASE_DIST)));
			base2.endYProperty().bind(base1.startYProperty().add(this.heightProperty().multiply(BASE_DIST)));
			
			Line diag1 = new Line();
			diag1.startXProperty().bind(base1.startXProperty());
			diag1.startYProperty().bind(base1.startYProperty());
			diag1.endXProperty().bind(base1.startXProperty());
			diag1.endYProperty().bind(this.heightProperty().divide(2));
			
			Line diag2 = new Line();
			diag2.startXProperty().bind(base1.endXProperty());
			diag2.startYProperty().bind(base1.endYProperty());
			diag2.endXProperty().bind(base1.endXProperty());
			diag2.endYProperty().bind(this.heightProperty().divide(2));
			
			Line baseTerm = new Line();
			baseTerm.startXProperty().bind(this.widthProperty().divide(2));
			baseTerm.startYProperty().bind(this.heightProperty());
			baseTerm.endXProperty().bind(this.widthProperty().divide(2));
			baseTerm.endYProperty().bind(base2.startYProperty());
			
			Line term1 = new Line();
			term1.startXProperty().bind(diag1.endXProperty());
			term1.startYProperty().bind(diag1.endYProperty());
			term1.endXProperty().set(0);
			term1.endYProperty().bind(this.heightProperty().divide(2));
			
			Line term2 = new Line();
			term2.startXProperty().bind(diag2.endXProperty());
			term2.startYProperty().bind(diag2.endYProperty());
			term2.endXProperty().bind(this.widthProperty());
			term2.endYProperty().bind(this.heightProperty().divide(2));
			
			Polygon arrow = new Polygon();
			
			final double LENGTH = 0.15;
			final double ANGLE = 24;
			ChangeListener<Number> listener = (e, o, n) ->
			{
				arrow.getPoints().clear();
				
				final double CENTRAL_ANGLE = new Point2D(diag2.getStartX()-diag2.getEndX(), diag2.getStartY()-diag2.getEndY()).angle(1, 0);
				final Point2D TIP = new Point2D(diag2.getEndX(), diag2.getEndY());
				arrow.getPoints().add(TIP.getX());
				arrow.getPoints().add(TIP.getY());
				
				arrow.getPoints().add(TIP.getX()+LENGTH*Math.cos(Math.toRadians(CENTRAL_ANGLE+ANGLE))*this.getWidth());
				arrow.getPoints().add(TIP.getY()+LENGTH*Math.sin(Math.toRadians(CENTRAL_ANGLE+ANGLE))*this.getHeight());
				arrow.getPoints().add(TIP.getX()+LENGTH*Math.cos(Math.toRadians(CENTRAL_ANGLE-ANGLE))*this.getWidth());
				arrow.getPoints().add(TIP.getY()+LENGTH*Math.sin(Math.toRadians(CENTRAL_ANGLE-ANGLE))*this.getHeight());
			};
			
			this.widthProperty().addListener(listener);
			this.heightProperty().addListener(listener);
			
			this.getChildren().addAll(c, base1, base2, diag1, diag2, baseTerm, term1, term2, arrow);
		}
	}
}
