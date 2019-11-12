package circuit;


/**
 * Describes a class that can be updated via tick()
 * @author root
 */
public interface Tickable
{
	/**
	 * tick
	 * @param timeElapsed 
	 */
	public void tick(double timeElapsed);
}
