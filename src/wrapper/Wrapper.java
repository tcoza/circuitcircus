package wrapper;

public class Wrapper<T>
{
	public T v;
	public Wrapper() { this(null); }
	public Wrapper(T v)
	{
		this.v = v;
	}
}
