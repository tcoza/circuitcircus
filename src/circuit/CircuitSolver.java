package circuit;


import circuit.component.CircuitComponent;
import circuit.component.CircuitElement;
import circuit.component.Node;
import java.util.*;

/**
 * Class used by updater to calculate the current through each pin in the circuit
 * @author root
 */
class CircuitSolver
{
	/** Contains the components in the circuit */
	private final Set<SystemOfEquations> systemsOfEquations;
	private final Set<CircuitElement> elements;
	private final Breadboard breadboard;
	private final int breadboardHash;
	private final Set<Node> nodes;
	
	private boolean circuitChanged;
	
	public CircuitSolver(Breadboard board) throws InconsistentCircuitException
	{
		this.breadboard = board;
		this.elements = board.getCircuitElements();
		this.systemsOfEquations = new HashSet<>();
		this.breadboardHash = this.breadboardHash();
		this.circuitChanged = false;
		
		board.getBasicComponents().forEach(c ->
		{
			for (int i = 0; i < c.getPinCount(); i++)
			{
				c.getPin(i).setOnConnectStatusChanged(() -> this.circuitChanged = true);
				c.getPin(i).currentProperty().set(0);
				c.getPin(i).voltageProperty().set(0);
			}
		});
		
		this.nodes = board.getNodes();
		
		while (!nodes.isEmpty())
			systemsOfEquations.addAll(getSystemsOfEquations(nodes.iterator().next()));
	}
	
	public boolean isObsolete()
	{
		return this.circuitChanged ? true : this.breadboardHash != breadboardHash();
	}
	
	private int breadboardHash()
	{
		int hash = 0;
		for (CircuitComponent c : this.breadboard)
			hash += c.hashCode();
		return hash;
	}
	
	public void tick() throws InconsistentCircuitException
	{
		for (SystemOfEquations<CircuitSegment> system : this.systemsOfEquations)
		{
			for (CircuitSegment u : system.getUnknowns())
				u.update();
			solve(system);
		}
		
		declareVoltages();
	}
	
	private void declareVoltages()
	{
		for (CircuitElement e : this.elements)
			switch (e.getInfo(0, 0).type)
			{
			case RESISTANCE:
				e.getPin(0).voltageProperty().set(0);
				e.getPin(1).voltageProperty().set(e.getPin(1).currentProperty().get() * e.getInfo(0, 0).value * -1);
				break;
			case VOLTAGE:
				e.getPin(0).voltageProperty().set(0);
				e.getPin(1).voltageProperty().set(e.getInfo(0, 1).value);
				break;
			case CURRENT:
				// Nothing, the wrappers take care of this
				break;
			}
	}
	
	/**
	 * Finds the equations for the current unknowns, solves for the unknowns, and declares the currents to all components
	 * @throws InconsistentCircuitException 
	 */
	private Set<SystemOfEquations> getSystemsOfEquations(Node n) throws InconsistentCircuitException
	{
		this.nodes.remove(n);
		
		Set<List<CircuitSegment>> loops = getLoops(n);
		Set<SystemOfEquations> systems = new HashSet<>();
		
		// Separate the loops into distinct systems of equations
		while (!loops.isEmpty())
		{
			SystemOfEquations<CircuitSegment> equations = new SystemOfEquations<>();
			List<CircuitSegment> currentLoop = loops.iterator().next();
			equations.getUnknowns().addAll(currentLoop);
			systems.add(equations);
			
			while (true)
			{
				Set<List<CircuitSegment>> toRemove = new HashSet<>();
				for (List<CircuitSegment> loop : loops)
					for (CircuitSegment s : loop)
						if (equations.unknownExists(s))
						{
							toRemove.add(loop);
							break;
						}
				if (toRemove.isEmpty()) break;
				
				for (List<CircuitSegment> loop : toRemove)
				{
					boolean hasCurrentSource = false;
					for (CircuitSegment cs : loop)
						if (cs.isCurrentSource())
						{
							SystemOfEquations.CircuitCurrentEquation eq = equations.new CircuitCurrentEquation(cs);
							if (!eq.exists()) eq.add();
							hasCurrentSource = true;
						}
					if (!hasCurrentSource)
						equations.new CircuitVoltageEquation(loop).add();
					loops.remove(loop);
				}
			}
			
			getCurrentEquations(equations);
		}
		
		return systems;
	}
	
	private void solve(SystemOfEquations<CircuitSegment> equations)
	{
		double[] solutions = new double[equations.getUnknowns().size()];
		try { solutions = equations.solveSystem(false); } catch (Exception ex)
		{
			System.err.println(equations);
			ex.printStackTrace();
		}
		
		for (int i = 0; i < solutions.length; i++)
		{
			CircuitComponent.Pin pin = equations.getUnknowns().get(i).pin1;
			assert pin != null;
			
			while (true)
			{
				pin.currentProperty().set(+solutions[i]);
				pin = pin.getPinConnected();
				assert pin != null;
				pin.currentProperty().set(-solutions[i]);
				if (pin == equations.getUnknowns().get(i).pin2) break;
				pin = CircuitSegment.getOtherConnectedPin(pin);
			}
		}
	}
	
	private Set<List<CircuitSegment>> getLoops(Node n) throws InconsistentCircuitException
	{
		Set<List<CircuitSegment>> loops = new HashSet<>();
		Stack<CircuitSegment> stack = new Stack<>();
		
		nextSegment(n, -1, stack, loops);
		
		return loops;
	}
	
	private final Set<Node> visitedNodes = new HashSet<>();
	
	/**
	 * Crawls in the circuit to find loops that can be used with Kirchov's voltage law
	 * @param node
	 * @param pin
	 * @throws InconsistentCircuitException 
	 */
	private void getLoops(Stack<CircuitSegment> segmentStack, Set<List<CircuitSegment>> loops) throws InconsistentCircuitException
	{
		CircuitSegment cs = segmentStack.peek();
		this.nodes.removeAll(cs.nodes);
		
		if (cs.pin1 == null || cs.pin2 == null)
			return;
		
		if (this.visitedNodes.contains((Node)cs.pin2.component))
			return;				// Do NOT engage in iteration
		
		for (int start = 0; start < segmentStack.size(); start++)
			if (cs.pin2.component == segmentStack.get(start).pin1.component)
			{
				loops.add(new ArrayList<>(segmentStack.subList(start, segmentStack.size())));
				return;
			}
		
		nextSegment(cs.pin2.component, cs.pin2.pinNo, segmentStack, loops);
		this.visitedNodes.add((Node)cs.pin2.component);
	}
	
	private void nextSegment(CircuitComponent c, int p, Stack<CircuitSegment> segmentStack, Set<List<CircuitSegment>> loops) throws InconsistentCircuitException
	{
		List<CircuitSegment> todo = new ArrayList<>();
		
		for (CircuitComponent.Pin pin : CircuitSegment.getConnectedPins(c))
		{
			if (pin.pinNo == p)
				continue;
			
			CircuitSegment s = new CircuitSegment(pin);
			if (!todo.contains(s.negate()))
				todo.add(s.isCurrentSource() ? todo.size() : 0, s);
		}
		
		for (CircuitSegment s : todo)
		{
			segmentStack.push(s);
			getLoops(segmentStack, loops);
			segmentStack.pop();
		}
	}
	
	/**
	 * Adds all necessary Kirchoff current equations
	 * @throws InconsistentCircuitException 
	 */
	private void getCurrentEquations(SystemOfEquations<CircuitSegment> equations) throws InconsistentCircuitException
	{
		Set<Node> currentNodes = new HashSet<>();
		for (CircuitSegment u : equations.getUnknowns())
		{
			currentNodes.add((Node)u.pin1.component);
			currentNodes.add((Node)u.pin2.component);
		}
		
		while (equations.countEquations() < equations.getUnknowns().size() && !currentNodes.isEmpty())
		{
			Node n = currentNodes.iterator().next();
			
			SystemOfEquations.DiscreteEquation eq = equations.new DiscreteEquation();
			
			CircuitSegment s;
			for (CircuitComponent.Pin pin : CircuitSegment.getConnectedPins(n))
				if (equations.unknownExists(s = new CircuitSegment(pin)))
					eq.setCoefficient(s, 1);
			
			assert eq.isConsistent();
			
			eq.add();
			
			currentNodes.remove(n);
		}
		
		assert equations.countEquations() == equations.getUnknowns().size();
	}
}
