/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuit;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author root
 */
public class SerializableStringProperty extends SimpleStringProperty implements SerializableProperty<String>
{
	public SerializableStringProperty(Object bean, String name)
	{
		super(bean, name);
	}

	public SerializableStringProperty(Object bean, String name, String initialValue)
	{
		super(bean, name, initialValue);
	}

	@Override
	public String getStringValue() { return this.get(); }

	@Override
	public void setStringValue(String value) throws Exception { this.set(value); }
	
	private boolean isTransient = false;
	
	@Override
	public boolean isTransient() { return isTransient; }
	public void setTransient(boolean v) { this.isTransient = v; }
	
}
