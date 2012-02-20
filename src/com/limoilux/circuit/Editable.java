
package com.limoilux.circuit;

public interface Editable
{
	EditInfo getEditInfo(int n);

	void setEditValue(int n, EditInfo ei);
}
