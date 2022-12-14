package com.jamonapi;

import com.jamonapi.utils.*;

/** JAMonListener that puts jamon data into a buffer that allows you to display the last N configurble
 * detail events.  The buffer will have the detail label, value and invocation date for the monitor that 
 * was fired.
 * 
 * @author steve souza
 *
 */

//public  class JAMonBufferListener implements JAMonListener, DetailData, CopyJAMonListener {
public  class JAMonBufferListener implements JAMonListener, CopyJAMonListener {

	private BufferList list;
	private String name;
	        static final String[] DEFAULT_HEADER=new String[] {"Label","LastValue","Active","Date"};
	        static final int VALUE_COL=1;
	        static final int DATE_COL=3;
	
	public JAMonBufferListener() {
		this("JAMonBufferListener");
	}
	
	/** Pass in the jamonListener name */
	
	public JAMonBufferListener(String name){
		this(name, new BufferList(DEFAULT_HEADER,50));
	}
	
	/** Name the listener and pass in the jamon BufferList to use */
	public JAMonBufferListener(String name, BufferList list) {
		this.name=name;
		this.list=list;
	}
		
	/** When this event is fired the monitor will be added to the rolling buffer */
	public void processEvent(Monitor mon) {
		list.addRow(mon.getJAMonDetailRow());
	}
	
	/** Add a row to the buffer */
	public void addRow(ToArray row) {
		list.addRow(row);
	}
    
    /** Add a row to the buffer */
    public void addRow(Object[] row) {
        list.addRow(row);
    }
	
	/** get the underlying bufferList which can then be used to display its contents */
	public BufferList getBufferList() {
		return list;
	}

	public String getName() {
	    return name;
    }
	
	public void setName(String name) {
	   this.name=name;
	    
    }
	
	public JAMonListener copy() {
		 return new JAMonBufferListener(getName(), list.copy());
	}


    public DetailData getDetailData() {
        return new BufferListDetailData(list);
    }

//    /**
//     * Use getDetailData() instead.
//     *  @deprecated
//     * 
//     */
//	public Object[][] getData() {
//        return list.getData();
//    }
//
//
//    /**
//     * Use getDetailData() instead.
//     *  @deprecated
//     * 
//     */
//	public String[] getHeader() {
//	    return list.getHeader();
//    }

    public int getRowCount() {
        return list.getRowCount();
    }

    public boolean hasData() {
        return list.hasData();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

	



	

}
