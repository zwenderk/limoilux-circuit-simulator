
package com.limoilux.circuit.core;

import com.limoilux.circuit.EditInfo;

public interface Editable
{
	public EditInfo getEditInfo(int n);

	public void setEditValue(int n, EditInfo ei);
}