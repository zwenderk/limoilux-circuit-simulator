package com.limoilux.circuit.techno;

import com.limoilux.circuit.core.CircuitElm;

public class StopDump
{
	public String msg;
	public CircuitElm ce;
	
	public StopDump(String msg, CircuitElm ce)
	{
		this.msg = msg;
		this.ce = ce;
	}
	
	public StopDump(String msg)
	{
		this(msg, null);
	}

}
