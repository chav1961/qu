package chav1961.qu.api.interfaces;

public interface GateDescriptor {
	public static enum GateType {
		GATE_H
	}
	
	GateType getType();
	GateMatrix getMatrix();
	double[] getShortMatrix();
	QubitDescriptor getQubit();
	QubitDescriptor[] getQubits();
	void setQubit(QubitDescriptor desc);
	void setQubits(QubitDescriptor... desc);
	QubitDescriptor[] getControls();
	void setControls(QubitDescriptor... desc);
}
