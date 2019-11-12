package circuit;

import circuit.component.CircuitElement;
import circuit.component.Node;
import circuit.component.CircuitComponent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a linear part of a circuit that has a certain current going through it starting from one Node to another
 * @author root
 */
public class CircuitSegment implements Negatable
{
	/** Pins on each end on the segment */
	public final CircuitComponent.Pin pin1, pin2;
	/** Data used to calculate current in circuit */
	public double voltage, resistance, current;
	public Map<CircuitSegment, Double> currentDependencies;
	private boolean currentSource;
	
	public final Set<Node> nodes;
	
	public CircuitSegment(CircuitComponent.Pin pin1) throws InconsistentCircuitException
	{
		assert pin1 != null;
		assert pin1.component instanceof Node;
		assert pin1.getPinConnected() != null;
		
		CircuitComponent.Pin originalPin1 = pin1;
		while (getConnectedPins(pin1.component).length == 2)
		{
			pin1 = getOtherConnectedPin(pin1).getPinConnected();			// Reverse
			if (pin1 == null || pin1 == originalPin1) break;
		}
		
		this.pin1 = pin1;
		this.nodes = new HashSet<>();

		if (this.pin1 == null)
		{
			this.pin2 = null;
			return;
		}

		this.pin2 = this.make();
	}

	public void update() throws InconsistentCircuitException
	{
		this.make();
	}
	
	private CircuitComponent.Pin make() throws InconsistentCircuitException
	{
		this.current = 0;
		this.voltage = 0;
		this.resistance = 0;
		this.currentSource = false;
		this.nodes.clear();
		
		CircuitComponent.Pin p = this.pin1;
		
		while (p != null)
		{
			CircuitElement.ElementInfo info =
					p.component instanceof CircuitElement ?
					((CircuitElement)p.component).getInfo(p.pinNo == 0 ? 1 : 0, p.pinNo) :
					new CircuitElement.ElementInfo(0, CircuitElement.ElementInfo.TYPE.VOLTAGE);
			
			switch (info.type)
			{
			case RESISTANCE:
				this.resistance += info.value;
				break;
			case VOLTAGE:
				this.voltage += info.value;
				break;
			case CURRENT:
				if (currentSource)
					throw new InconsistentCircuitException("Can't have two current sources on the same segment");
				this.current = info.value;
				this.currentDependencies = new HashMap<>();
				for (Map.Entry<CircuitComponent.Pin, Double> e : info.dependencies.entrySet())
					this.currentDependencies.put(new CircuitSegment(e.getKey()), e.getValue());
				this.currentSource = true;
				break;
			}
			
			if (p.component instanceof Node)
				this.nodes.add((Node)p.component);
			
			p = p.getPinConnected();
			
			if (p == null) break;
			if (this.pin1 != null && p.component == this.pin1.component) break;
			if (getConnectedPins(p.component).length > 2) break;
			
			p = getOtherConnectedPin(p);
		}
		
		if (this.resistance < Breadboard.WIRE)
			this.resistance = Breadboard.WIRE;
		
		if (p != null && p.component instanceof Node)
			this.nodes.add((Node)p.component);
		
		return p;
	}
	
	private CircuitSegment(CircuitComponent.Pin pin1, CircuitComponent.Pin pin2)
	{
		this.pin1 = pin1;
		this.pin2 = pin2;
		this.nodes = null;
	}
	
	public boolean isCurrentSource() { return currentSource; }
	
	@Override
	public CircuitSegment negate() { return new CircuitSegment(this.pin2, this.pin1); }
	
	/**
	 * 
	 * @return a String describing from which pin and component to which pin and component the segment spans
	 */
	@Override
	public String toString() { return this.pin1 + " -> " + this.pin2; }
	
	@Override
	public boolean equals(Object o) { return (o instanceof CircuitSegment) && o.hashCode() == this.hashCode(); }

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 61 * hash + Objects.hashCode(this.pin1);
		hash = 61 * hash + Objects.hashCode(this.pin2);
		return hash;
	}
	
	public static CircuitComponent.Pin[] getConnectedPins(CircuitComponent c)
	{
		int length = 0;
		for (int i = 0; i < c.getPinCount(); i++)
			if (c.getPin(i).getPinConnected() != null)
				length++;
		
		int index = 0;
		CircuitComponent.Pin[] pins = new CircuitComponent.Pin[length];
		for (int i = 0; i < c.getPinCount(); i++)
			if (c.getPin(i).getPinConnected() != null)
				pins[index++] = c.getPin(i);
		return pins;
	}
	
	public static CircuitComponent.Pin getOtherConnectedPin(CircuitComponent.Pin pin)
	{
		CircuitComponent.Pin[] pins = CircuitSegment.getConnectedPins(pin.component);
		assert pin.getPinConnected() != null;
		assert pins.length <= 2;
		for (CircuitComponent.Pin p : pins)
			if (p != pin) return p;
		return null;
	}
}