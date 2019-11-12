package circuit;


import circuit.component.CircuitElement;
import circuit.component.Node;
import circuit.component.IntegratedCircuit;
import circuit.component.CircuitComponent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import javafx.application.Platform;

/**
 * A Runnable and Tickable set of CircuitComponents that are updated at regular intervals
 * Components not present in this Set will not be updated
 * @author root
 */
public final class Breadboard extends HashSet<CircuitComponent> implements Runnable, Tickable
{
	public static final double BIG = 1.0E+7;
	public static final double WIRE = 2.5E-2;
	
	/** Thread updating the circuit */
	public final Thread updateLoop;
	
	private static int count = 1;
	
	/**
	 * Makes new breadboard with specifies elements and immediately starts running in real-time
	 * @param components Components to add upon creation
	 */
	public Breadboard(CircuitComponent... components)
	{
		this.addAll(Arrays.asList(components));
		
		this.speed = new SerializableDoubleProperty(this, "simulation speed", Units.NONE, 1);
		this.speed.addListener((e, oldVal, newVal) ->
		{
			if (newVal.doubleValue() == 0)
				this.threadPauseSemaphore.acquireUninterruptibly();
			else if (oldVal.doubleValue() == 0)
				this.threadPauseSemaphore.release();
		});
		
		this.number = Breadboard.count++;
		
		(updateLoop = new Thread(this)).start();
	}
	
	public final SerializableDoubleProperty speed;
	private final Semaphore threadPauseSemaphore = new Semaphore(1, true);
	
	/** Thread run() method */
	@Override
	public void run()
	{
		long sleepInterval = 10;					// in milliseconds
		long time = System.nanoTime();
		while (true)
		{
			final long timeElapsed = -time + (time = System.nanoTime());
			
			Semaphore s = new Semaphore(0);
			Platform.runLater(() ->
			{
				this.tick((double)timeElapsed / 1000000000 * speed.get());
				s.release();
			});
			
			this.threadPauseSemaphore.acquireUninterruptibly();
			this.threadPauseSemaphore.release();
			
			try { s.acquire(); Thread.sleep(sleepInterval); }
			catch (InterruptedException ex) { break; }
		}
	}
	
	
	private CircuitSolver solver = null;
	
	@Override
	public synchronized void tick(double interval)
	{
		try
		{
			if (solver == null || solver.isObsolete())
				solver = new CircuitSolver(this);
			solver.tick();
		}
		catch (InconsistentCircuitException ex)
		{
			ex.printStackTrace();
		}
		
		for (CircuitComponent c : this)
			if (c instanceof Tickable)
				((Tickable) c).tick(interval);
	}
	
	public Set<Node> getNodes()
	{
		Set<CircuitComponent> s = this.getBasicComponents();
		s.removeIf(c -> { return !(c instanceof Node); });
		return (Set)s;
	}
	
	public Set<CircuitElement> getCircuitElements()
	{
		Set<CircuitComponent> s = this.getBasicComponents();
		s.removeIf(c -> { return !(c instanceof CircuitElement); });
		return (Set)s;
	}
	
	/** Return a set of indivisible circuit components in this breadboard */
	public Set<CircuitComponent> getBasicComponents()
	{
		Set<CircuitComponent> components = new HashSet<>();
		getBasicComponents(this, components);
		return components;
	}
	
	/** Return a set of indivisible circuit components in this breadboard */
	private void getBasicComponents(Set<CircuitComponent> source, Set<CircuitComponent> target)
	{
		for (CircuitComponent c : source)
			if (c instanceof IntegratedCircuit)
				getBasicComponents(((IntegratedCircuit)c).getElements(), target);
			else target.add(c);
	}
	
	private int number;
	
	@Override
	public int hashCode() { return number * 235132451; }
	
	@Override
	public boolean equals(Object o) { return o instanceof Breadboard && o.hashCode() == this.hashCode(); }
}

/**
 * Exception thrown when a circuit is a physical impossibility
 * @author root
 */
class InconsistentCircuitException extends Exception { public InconsistentCircuitException(String message) { super(message); } }