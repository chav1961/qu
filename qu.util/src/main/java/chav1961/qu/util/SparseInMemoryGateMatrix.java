package chav1961.qu.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.qu.api.interfaces.GateMatrix;

public class SparseInMemoryGateMatrix extends AbstractInMemoryGateMatrix{

	SparseInMemoryGateMatrix(MatrixType type, long width, long height) {
		super(MatrixType.SPARSE_MATRIX, width, height);
		// TODO Auto-generated constructor stub
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
		// TODO Auto-generated method stub
		return null;
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
