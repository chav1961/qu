package chav1961.qu.api.interfaces;

import chav1961.purelib.basic.NamedValue;

public interface GateDescriptor {
	public static enum GateType {
		H,
		P,
		SWAP
	}
	
	GateType getType();
	NamedValue<Object>[] getParameters();
	GateMatrix getMatrix();
	double[] getShortMatrix();
	QubitDescriptor getQubit();
	QubitDescriptor[] getQubits();
	void setQubit(QubitDescriptor desc);
	void setQubits(QubitDescriptor... desc);
	QubitDescriptor[] getControls();
	void setControls(QubitDescriptor... desc);
}
