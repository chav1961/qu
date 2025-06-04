package chav1961.qu.api.interfaces;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.CalculationException;

public interface QuantumVM {
	QuantumSchemeDescriptor getScheme();
	void setScheme(QuantumSchemeDescriptor desc);
	GateMatrix calculate(double[] source) throws CalculationException;
	void download(DataInput in) throws IOException;
	void upload(DataOutput out) throws IOException;
}
