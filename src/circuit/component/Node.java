package circuit.component;


/**
 * Describes a node in the circuit, with zero resistance and zero voltage across all pins
 * @author root
 */
public class Node	extends CircuitComponent
{
	public Node(int numOfPins)
	{
		super(numOfPins);
	}
}
