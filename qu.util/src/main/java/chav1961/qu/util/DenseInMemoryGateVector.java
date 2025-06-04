package chav1961.qu.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.qu.api.interfaces.GateMatrix;

public class DenseInMemoryGateVector extends AbstractInMemoryGateMatrix {
	private final double[]	content;
	
	
	DenseInMemoryGateVector(final long width, final long height) {
		this(width, height, new double[(int) (2*height*width)]);
	}

	private DenseInMemoryGateVector(final long width, final long height, final double[] content) {
		super(MatrixType.DENSE_MATRIX, width, height);
		this.content = new double[(int) (2*height*width)];
	}
	
	@Override
	protected void downloadInternal(Piece piece, DataInput in) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void uploadInternal(Piece piece, DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected GateMatrix multiplyInternal(GateMatrix another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected GateMatrix multiplyAndTransposeInternal(GateMatrix another) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected GateMatrix transposeInternal() throws CalculationException {
		return new DenseInMemoryGateVector(getHeight(), getWidth(), content.clone());
	}

	@Override
	protected GateMatrix reduceInternal(int qubitNo, int qubitValue) throws CalculationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void forEachInternal(Piece piece, ForEachCallback callback) throws CalculationException {
		// TODO Auto-generated method stub
		
	}

}
