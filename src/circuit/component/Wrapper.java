
package circuit.component;

import circuit.SerializableProperty;
import java.util.Set;

/**
 *
 * @author root
 * @param <T>
 */
public abstract class Wrapper<T extends CircuitComponent> extends IntegratedCircuit
{
	private final T wrapped;
	
	public Wrapper(T wrapped)
	{
		super(wrapped.getPinCount());
		this.wrapped = wrapped;
	}
	
	public T unwrap() { return wrapped; }
}
