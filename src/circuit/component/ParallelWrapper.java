/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuit.component;

import java.util.Set;
import javafx.beans.binding.Binding;

/**
 *
 * @author root
 * @param <T>
 */
public class ParallelWrapper<T extends CircuitElement> extends Wrapper<T>
{
	public final T source;
	public final Resistor resistor;
	private final Node node1, node2;
	
	public ParallelWrapper(T source, Binding resistance)
	{
		super(source);
		this.node1 = new Node(3);
		this.node2 = new Node(3);
		this.source = source;
		this.resistor = new Resistor();
		this.resistor.getProperty("resistance").bind(resistance);
		
		this.pins[0] = node1.getPin(0);
		this.pins[1] = node2.getPin(0);
		node1.getPin(1).connect(source.getPin(0));
		node1.getPin(2).connect(resistor.getPin(0));
		node2.getPin(1).connect(source.getPin(1));
		node2.getPin(2).connect(resistor.getPin(1));
	}

	@Override
	public void tick(double elapsedTime)
	{
		this.source.getPin(0).voltageProperty().set(this.resistor.getPin(0).voltageProperty().get());
		this.source.getPin(1).voltageProperty().set(this.resistor.getPin(1).voltageProperty().get());
		
		super.tick(elapsedTime); //To change body of generated methods, choose Tools | Templates.
	}
	
	@Override
	public Set<CircuitComponent> getElements() { return makeSet(node1, node2, source, resistor); }
}
