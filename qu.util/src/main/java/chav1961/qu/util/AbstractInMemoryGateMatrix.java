package chav1961.qu.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.qu.api.interfaces.GateMatrix;

abstract class AbstractInMemoryGateMatrix extends AbstractGateMatrix {

	AbstractInMemoryGateMatrix(MatrixType type, long width, long height) {
		super(type, width, height);
	}

	@Override
	public void close() throws CalculationException {
		// TODO Auto-generated method stub
		
	}
}
