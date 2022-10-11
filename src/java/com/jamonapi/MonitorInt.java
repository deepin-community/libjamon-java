package com.jamonapi;

import java.util.Date;

import com.jamonapi.utils.ToArray;

/**
 * MonitorInt.java
 *
 * Created on February 2, 2006, 7:54 PM
 */




interface MonitorInt  {

	
	  public double getTotal();
	  public void setTotal(double value);
	  public double getAvg();
	  public double getMin();
	  public void setMin(double value);
	  public double getMax();
	  public void setMax(double value);
	  public double getHits();
	  public void setHits(double value);
	  public double getStdDev();
	  public void setFirstAccess(Date date);
	  public Date getFirstAccess();

	  public void setLastAccess(Date date);
	  public Date getLastAccess();
	  public double getLastValue();
	  public void setLastValue(double value);
	  
	      
  /** Start a monitor.  This increments the active counter by one. Calling start is not 
      required.  If it is called stop should be called too. */
  public Monitor start();
  
  /** Stop a monitor.  The decrements the active counter by one.  Calling stop is 
   *  required if start is called.
   */
  public Monitor stop();
  
  /** This method adds a value to the monitor (and aggegates statistics on it)
   */
  public Monitor add(double value); 
  
  /** reset all values in the monitor to their defaults */
  public void reset();

  /** enable the monitor.  If the monitor is enabled all other calls to the monitor 
    * have an action
   */
  public void enable();
  
  /** Disable the monitor.  If a monitor is disabled all other calls to the monitor
   * are noops.
   **/
  public void disable();
  
  /** Is the monitor enabled. */
  public boolean isEnabled();
  
  /** Return the Range object associated with this monitor.  The range object is a compromise
   between saving all data or none
   */
  public Range getRange();
  
  /** Return the label associated with this monitor.  */
  public MonKey getMonKey();
  public double getActive();
  public void setActive(double value);
  public double getMaxActive();
  public void setMaxActive(double value);
  public void setTotalActive(double value);
  public double getAvgActive();
  public boolean isPrimary();
    /** Indicate that this a primary Monitor.  See www.jamonapi.com for an explanation of primary monitors **/
  public void setPrimary(boolean isPrimary);

  // Some jamon 2.4 introduced methods.  Mostly listener related.
  public ListenerType getListenerType(String listenerType);
  public boolean hasListeners();

  public void setActivityTracking(boolean trackActivity);
  public boolean isActivityTracking();
  
  
  public JAMonDetailValue getJAMonDetailRow();
}
