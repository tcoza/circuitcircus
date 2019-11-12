
package circuit.component;

import circuit.Units;
import circuit.Breadboard;
import circuit.SerializableDoubleProperty;
import circuit.SerializableStringProperty;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import circuit.ComponentGUI;
import static circuit.component.CurrentSource.CURRENT_SOURCE;

public class BJT extends IntegratedCircuit implements ComponentGUI
{
	public static final int BASE = 0, COLLECTOR = 1, EMITTER = 2;
	public static final String FW_ACTIVE = "forward-active";
	public static final String RV_ACTIVE = "reverse-active";
	public static final String SATURATION = "saturation";
	public static final String CUT_OFF = "cut-off";
	
	private final CurrentSource collectorSource, emitterSource;
	private final Diode collectorDiode, emitterDiode;
	private final Node collectorNode, emitterNode, baseNode;
	private final Node collectorDummy, emitterDummy;
	
	protected final SerializableDoubleProperty forwardBeta;
	protected final SerializableDoubleProperty reverseBeta;
	protected final SerializableDoubleProperty baseCollectorVoltageDrop;
	protected final SerializableDoubleProperty baseEmitterVoltageDrop;
	protected final SerializableDoubleProperty baseCurrent;
	protected final SerializableDoubleProperty collectorCurrent;
	protected final SerializableDoubleProperty emitterCurrent;
	protected final SerializableStringProperty stateOfOperation;
	
	public BJT()
	{
		super(3);
		
		this.collectorDiode = new Diode();
		this.emitterDiode = new Diode();
		this.collectorSource = new CurrentSource()
		{
			@Override
			public CircuitElement.ElementInfo getInfo(int pin1, int pin2)
			{
				final double alpha = BJT.this.forwardBeta.get() / (BJT.this.forwardBeta.get() + 1);
				CircuitElement.ElementInfo info = new CircuitElement.ElementInfo(0, CircuitElement.ElementInfo.TYPE.CURRENT);
				info.dependencies.put(emitterDiode.getPin(Diode.ANODE).getPinConnected(), alpha * (pin2 == CURRENT_SOURCE ? +1 : -1));
				return info;
			}
		};
		this.emitterSource = new CurrentSource()
		{
			@Override
			public CircuitElement.ElementInfo getInfo(int pin1, int pin2)
			{
				final double alpha = BJT.this.reverseBeta.get() / (BJT.this.reverseBeta.get() + 1);
				CircuitElement.ElementInfo info = new CircuitElement.ElementInfo(0, CircuitElement.ElementInfo.TYPE.CURRENT);
				info.dependencies.put(collectorDiode.getPin(Diode.ANODE).getPinConnected(), alpha * (pin2 == CURRENT_SOURCE ? +1 : -1));
				return info;
			}
		};
		this.collectorDummy = new Node(2);
		this.emitterDummy = new Node(2);
		this.collectorNode = new Node(3);
		this.emitterNode = new Node(3);
		this.baseNode = new Node(5);
		
		this.collectorNode.getPin(0).connect(this.collectorDummy.getPin(1));
		this.collectorNode.getPin(1).connect(this.collectorSource.getPin(CurrentSource.CURRENT_SINK));
		this.collectorNode.getPin(2).connect(this.collectorDiode.getPin(Diode.CATHODE));
		this.emitterNode.getPin(0).connect(this.emitterDummy.getPin(1));
		this.emitterNode.getPin(1).connect(this.emitterSource.getPin(CurrentSource.CURRENT_SINK));
		this.emitterNode.getPin(2).connect(this.emitterDiode.getPin(Diode.CATHODE));
		this.baseNode.getPin(1).connect(this.collectorDiode.getPin(Diode.ANODE));
		this.baseNode.getPin(2).connect(this.collectorSource.getPin(CurrentSource.CURRENT_SOURCE));
		this.baseNode.getPin(3).connect(this.emitterDiode.getPin(Diode.ANODE));
		this.baseNode.getPin(4).connect(this.emitterSource.getPin(CurrentSource.CURRENT_SOURCE));
		
		this.pins[BJT.COLLECTOR] = this.collectorDummy.getPin(0);
		this.pins[BJT.EMITTER] = this.emitterDummy.getPin(0);
		this.pins[BJT.BASE] = this.baseNode.getPin(0);
		
		this.forwardBeta = new SerializableDoubleProperty(this, "forward beta", Units.NONE, 100);
		this.reverseBeta = new SerializableDoubleProperty(this, "reverse beta", Units.NONE, 30);
		this.baseCollectorVoltageDrop = new SerializableDoubleProperty(this, "base-collector voltage drop", Units.VOLTS, collectorDiode.voltageDrop.get());
		this.baseEmitterVoltageDrop = new SerializableDoubleProperty(this, "base-emitter voltage drop", Units.VOLTS, emitterDiode.voltageDrop.get());
		this.baseCurrent = new SerializableDoubleProperty(this, "base current", Units.AMPS);
		this.collectorCurrent = new SerializableDoubleProperty(this, "collector current", Units.AMPS);
		this.emitterCurrent = new SerializableDoubleProperty(this, "emitter current", Units.AMPS);
		this.stateOfOperation = new SerializableStringProperty(this, "state of operation", CUT_OFF);
		this.collectorDiode.voltageDrop.bind(this.baseCollectorVoltageDrop);
		this.emitterDiode.voltageDrop.bind(this.baseEmitterVoltageDrop);
		this.baseCurrent.bind(this.pins[BASE].currentProperty().negate());
		this.collectorCurrent.bind(this.pins[COLLECTOR].currentProperty().negate());
		this.emitterCurrent.bind(this.pins[EMITTER].currentProperty());
		this.baseCurrent.setTransient(true);
		this.collectorCurrent.setTransient(true);
		this.emitterCurrent.setTransient(true);
		this.stateOfOperation.setTransient(true);
	}
	
	@Override
	public void tick(double elapsedTime)
	{
		this.collectorDiode.tick(elapsedTime);
		this.emitterDiode.tick(elapsedTime);
		
		this.stateOfOperation.set(
				this.emitterDiode.isBlocking() ?
				this.collectorDiode.isBlocking() ? CUT_OFF : RV_ACTIVE :
				this.collectorDiode.isBlocking() ? FW_ACTIVE : SATURATION);
	}

	@Override
	public Set<CircuitComponent> getElements() { return makeSet(collectorSource, emitterSource, collectorDiode, emitterDiode, collectorNode, emitterNode, baseNode, collectorDummy, emitterDummy); }

	private circuit.GraphicalNode nodeGUI;

	@Override
	public String getSimpleName() { return "Bipolar junction transistor"; }
	
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
			super(BJT.this);
			
			this.pinsGUI[0] = null;
			this.pinsGUI[1] = this.getComponent().getPin(BJT.EMITTER);
			this.pinsGUI[2] = this.getComponent().getPin(BJT.BASE);
			this.pinsGUI[3] = this.getComponent().getPin(BJT.COLLECTOR);
			
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
			
			Line base = new Line();
			base.startXProperty().bind(this.widthProperty().multiply((1-BASE_WIDTH)/2));
			base.startYProperty().bind(this.heightProperty().multiply(BASE_POSITION+1/2d));
			base.endXProperty().bind(this.widthProperty().multiply((1+BASE_WIDTH)/2));
			base.endYProperty().bind(this.heightProperty().multiply(BASE_POSITION+1/2d));
			
			Line diag1 = new Line();
			diag1.startXProperty().bind(this.widthProperty().multiply((1-BASE_WIDTH/2)/2));
			diag1.startYProperty().bind(base.startYProperty());
			diag1.endXProperty().bind(this.widthProperty().multiply((1-3*BASE_WIDTH/2)/2));
			diag1.endYProperty().bind(this.heightProperty().divide(2));
			
			Line diag2 = new Line();
			diag2.startXProperty().bind(this.widthProperty().multiply((1+BASE_WIDTH/2)/2));
			diag2.startYProperty().bind(base.startYProperty());
			diag2.endXProperty().bind(this.widthProperty().multiply((1+3*BASE_WIDTH/2)/2));
			diag2.endYProperty().bind(this.heightProperty().divide(2));
			
			Line baseTerm = new Line();
			baseTerm.startXProperty().bind(this.widthProperty().divide(2));
			baseTerm.startYProperty().bind(this.heightProperty());
			baseTerm.endXProperty().bind(this.widthProperty().divide(2));
			baseTerm.endYProperty().bind(base.startYProperty());
			
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
			
			this.getChildren().addAll(c, base, diag1, diag2, baseTerm, term1, term2, arrow);
		}
	}
}
