/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circuit;

import javafx.beans.property.Property;

public interface SerializableProperty<T> extends Property<T>
{
	public String getStringValue();
	public void setStringValue(String value) throws Exception;
	public boolean isTransient();
}