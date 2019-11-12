package circuit.component;

import circuit.SerializableProperty;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;


/**
 * Parent class of everything on a breadboard
 * @author root
 */
public abstract class CircuitComponent
{
	/** Array of pins of this Component */
	protected Pin[] pins;
	
	/** Created new component with specified number of pins */
	public CircuitComponent(int numOfPins)
	{
		pins = new Pin[numOfPins];
		for (int i = 0; i < pins.length; i++)
			pins[i] = new Pin(i);
	}
	
	public Pin getPin(int i) { return pins[i]; }
	public int getPinCount() { return pins.length; }
	
	public Set<SerializableProperty> getProperties()
	{
		Set<SerializableProperty> properties = new HashSet<>();
		for (Class<?> c = this.getClass(); !c.equals(Object.class); c = c.getSuperclass())
			for (Field f : c.getDeclaredFields())
				if (SerializableProperty.class.isAssignableFrom(f.getType()))
					try { properties.add((SerializableProperty)f.get(this)); }
					catch (IllegalAccessException | IllegalArgumentException ex) { ex.printStackTrace(); }
		return properties;
	}
	
	public final SerializableProperty getProperty(String name)
	{
		for (SerializableProperty p : this.getProperties())
			if (p.getName().equals(name))
				return p;
		return null;
	}

	/**
	 * Describes a pin on a certain component
	 */
	public final class Pin
	{
		/** Component to which the pin belongs */
		public final CircuitComponent component;
		/** Index of the pin */
		public final int pinNo;
		/** Pin connected to this pin (null if floating) */
		private Pin pinConnected;
		
		private final DoubleProperty current = new SimpleDoubleProperty(this, "current", 0);
		private final DoubleProperty voltage = new SimpleDoubleProperty(this, "voltage", 0);
		
		/** Makes new pin with specified index */
		public Pin(int pinNo)
		{
			this.component = CircuitComponent.this;
			this.pinNo = pinNo;
			pinConnected = null;
			assert !(this.component instanceof IntegratedCircuit);
		}
		
		public synchronized Pin getPinConnected() { return this.pinConnected; }
		
		/** Connects to pin on specified target Component with specified index */
		public synchronized void connect(Pin targetPin)
		{
			this.disconnect();
			targetPin.disconnect();
			this.pinConnected = targetPin;
			targetPin.pinConnected = this;
			
			this.connectStatusChanged();
		}
		
		public synchronized void disconnect()
		{
			assert this.pinConnected == null || this.pinConnected.pinConnected == this;
			
			if (this.pinConnected != null)
				this.pinConnected.pinConnected = null;
			this.pinConnected = null;
			
			this.connectStatusChanged();
		}
		
		public DoubleProperty currentProperty() { return this.current; }
		public DoubleProperty voltageProperty() { return this.voltage; }
		
		@Override
		public String toString() { return this.component + "(" + this.pinNo + ")"; }
		
		private Runnable onConnectStatusChanged = null;
		public void setOnConnectStatusChanged(Runnable onConnectStatusChanged)
			{ this.onConnectStatusChanged = onConnectStatusChanged; }
		private void connectStatusChanged()
		{
			if (this.onConnectStatusChanged != null)
				this.onConnectStatusChanged.run();
		}
	}
	
	@Override
	public final int hashCode() { return super.hashCode(); }
	
	@Override
	public final boolean equals(Object o) { return o instanceof CircuitElement && this.hashCode() == o.hashCode(); }
}
