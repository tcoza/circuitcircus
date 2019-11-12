package circuit.component;

import circuit.Units;
import circuit.GraphicalNode;
import circuit.SerializableDoubleProperty;
import circuit.ComponentGUI;
import java.util.HashMap;
import java.util.Map;


/**
 * Parent class of any two-pinned atomic component in the circuit
 * @author root
 */
public abstract class CircuitElement extends CircuitComponent implements ComponentGUI
{
	protected final SerializableDoubleProperty current_through;
	protected final SerializableDoubleProperty voltage_across;
	
	/** Makes new Component with two pins */
	public CircuitElement()
	{
		super(2);		// Always 2 pins
		this.current_through = new SerializableDoubleProperty(this, "current through element", Units.AMPS);
		this.voltage_across = new SerializableDoubleProperty(this, "voltage across element", Units.VOLTS);
		this.current_through.bind(this.getPin(1).currentProperty());
		this.voltage_across.bind(this.getPin(1).voltageProperty().subtract(this.getPin(0).voltageProperty()));
		this.current_through.setTransient(true);
		this.voltage_across.setTransient(true);
	}
	
	/**
	 * Returns ElementInfo of Element with pins specified as follows:
	 * 
	 *  Standards:
	 *
	 *	If CompInfo.type is TYPE.CURRENT, the value refers to the outgoing current on pin2 (pin1 is ignored)
	 *	If CompInfo.type is TYPE.VOLTAGE, the value refers to the voltage between pin1 and pin2 (V(pin2) - V(pin1))
	 *	If CompInfo.type is TYPE.RESISTANCE, the value refers to the resistance as measured across the two-pin component (pins are ignored)
	 * 
	 * 
	 * @param pin1
	 * @param pin2
	 * @return 
	 */
	public abstract ElementInfo getInfo(int pin1, int pin2);
	
	protected circuit.GraphicalNode nodeGUI;
	
	@Override
	public circuit.GraphicalNode getGraphicalNode(int x, int y)
	{
		if (x == 0 && y == 0) return nodeGUI;
		else return null;
	}

	@Override
	public void initGUI()
	{
		try
		{
			Class<? extends GraphicalNode> graphicalNode = null;
			for (Class<?> c : this.getClass().getDeclaredClasses())
				if (GraphicalNode.class.isAssignableFrom(c))
					graphicalNode = (Class<? extends GraphicalNode>)c;
			if (graphicalNode == null)
				throw new Exception("Error: no descendants of GraphicalNode found in class " + this.getClass().getName());
			initGUI(graphicalNode.getConstructor(this.getClass()).newInstance(this), 0, 0);
		}
		catch (Exception ex) { ex.printStackTrace(); }
	}

	@Override
	public void initGUI(circuit.GraphicalNode n, int x, int y) { if (x == 0 && y == 0) nodeGUI = n; }
	
	/**
	 * Info used to calculate current and voltage in a circuit by updater
	 */
	public static class ElementInfo
	{
		/**
		 * Can either be resistance, voltage or current
		 */
		public enum TYPE
		{
			RESISTANCE,
			VOLTAGE,
			CURRENT,
		}
		
		/** value in amps, ohms, or volts */
		public final double value;
		/** Type of value */
		public final TYPE type;
		
		public final Map<CircuitComponent.Pin, Double> dependencies;
		
		/**
		 * Constructs new ElementInfo based on value and type
		 * @param value
		 * @param type 
		 */
		public ElementInfo(double value, TYPE type)
		{
			this.value = value;
			this.type = type;
			this.dependencies = new HashMap<>();
		}
	}
}
