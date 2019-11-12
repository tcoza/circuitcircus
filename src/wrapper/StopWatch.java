/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrapper;

/**
 *
 * @author root
 */
public class StopWatch
{
	private long start;

	public StopWatch()
	{
		this.start = System.nanoTime();
	}
	
	public double getTimeElapsed() { return (-this.start + (this.start = System.nanoTime())) / 1000000000d; }
}
