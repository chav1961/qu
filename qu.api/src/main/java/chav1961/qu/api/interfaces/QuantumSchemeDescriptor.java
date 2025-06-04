package chav1961.qu.api.interfaces;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface QuantumSchemeDescriptor extends Iterable<GateDescriptor> {
	String getName();
	void setName(String name);

	int numberOfGates();
	void addGate(GateDescriptor desc);
	void addGate(int before, GateDescriptor desc);
	GateDescriptor getGate(int gateNumber);
	GateDescriptor setGate(int gateNumber, GateDescriptor desc);
	GateDescriptor removeGate(int gateNumber);

	int numberOfQubits();
	void addQubit(QubitDescriptor desc);
	void addQubit(int before, QubitDescriptor desc);
	QubitDescriptor removeQubit(int qubitNumber);
	
	void download(DataInput in) throws IOException;
	void upload(DataOutput out) throws IOException;
}
