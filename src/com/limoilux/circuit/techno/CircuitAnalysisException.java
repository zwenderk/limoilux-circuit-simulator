
package com.limoilux.circuit.techno;

/**
 * @author David Bernard
 * 
 *         An exception occuring while analysing the circuit.
 */
public class CircuitAnalysisException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4144813700108967724L;
	private final String technicalMessage;
	private final CircuitElm causeElement;

	/**
	 * @param technicalMessage A technical message about the cause of the
	 *            problem.
	 * @param causeElement The element that caused the problem or null if none.
	 */
	public CircuitAnalysisException(String technicalMessage, CircuitElm causeElement)
	{
		this.technicalMessage = technicalMessage;
		this.causeElement = causeElement;
	}

	/**
	 * @param technicalMessage A technical message about the cause of the
	 *            problem.
	 */
	public CircuitAnalysisException(String technicalMessage)
	{
		this(technicalMessage, null);
	}

	public String getTechnicalMessage()
	{
		return this.technicalMessage;
	}

	public CircuitElm getCauseElement()
	{
		return this.causeElement;
	}

}
