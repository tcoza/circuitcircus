/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuit;

import javafx.beans.property.SimpleDoubleProperty;

public class SerializableDoubleProperty extends SimpleDoubleProperty implements SerializableProperty<Number>
{
	public SerializableDoubleProperty(Object bean, String name, String units) { this(bean, name, units, 0); }
	public SerializableDoubleProperty(Object bean, String name, String units, double initialValue) { super(bean, name, initialValue); this.units = units; }
	
	@Override
	public String getStringValue() { return Double.toString(this.get()); }
	
	@Override
	public void setStringValue(String value) throws Exception { this.set(Double.parseDouble(value)); }
	
	private boolean isTransient = false;
	
	@Override
	public boolean isTransient() { super.get(); return isTransient; }
	public void setTransient(boolean realOnly) { this.isTransient = realOnly; }
	
	private final String units;
	public String getUnits() { return this.units; }
}