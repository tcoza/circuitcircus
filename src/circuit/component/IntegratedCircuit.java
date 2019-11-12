package circuit.component;

import circuit.Tickable;
import java.util.HashSet;
import java.util.Set;

/**
 * Parent class of any integrated circuit
 * @author root
 */
public abstract class IntegratedCircuit extends CircuitComponent implements Tickable
{
	/**
	 * Creates new IntegratedCircuit with specified number of pins
	 * @param numOfPins 
	 */
	public IntegratedCircuit(int numOfPins)
	{
		super(0);
		this.pins = new Pin[numOfPins];
	}
	
	/** Return all sub-components of integrated circuit */
	public abstract Set<CircuitComponent> getElements();
	
	protected static Set<CircuitComponent> makeSet(CircuitComponent... components)
	{
		Set<CircuitComponent> s = new HashSet<>();
		for (CircuitComponent c : components)
			s.add(c);
		return s;
	}
	
	@Override
	public void tick(double elapsedTime)
	{
		this.getElements().forEach(e ->
		{
			if (e instanceof Tickable)
				((Tickable)e).tick(elapsedTime);
		});
	}
}
