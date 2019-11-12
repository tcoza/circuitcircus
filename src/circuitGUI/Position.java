
package circuitGUI;

import circuit.ComponentGUI;

public class Position<T>
{
	public final int x, y;
	public T value = null;

	public Position(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Position(int x, int y, T value)
	{
		this(x, y);
		this.value = value;
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 97 * hash + this.x;
		hash = 97 * hash + this.y;
		return hash;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Position && this.hashCode() == obj.hashCode();
	}

	@Override
	public String toString()
	{
		return "x: " + x + " y: " + y;
	}
}