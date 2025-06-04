package chav1961.qu.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.qu.api.interfaces.GateMatrix;
import chav1961.qu.api.interfaces.GateMatrix.ForEachCallback;
import chav1961.qu.api.interfaces.GateMatrix.Piece;

class InMemoryCommutationalMatrix extends AbstractInMemoryGateMatrix {
	private final int[]	columns;

	InMemoryCommutationalMatrix(final long width, final long height) {
		this(width, height, new int[(int) height]);
	}

	private InMemoryCommutationalMatrix(final long width, final long height, final int[] columns) {
		super(MatrixType.COMMITATIONAL_MATRIX, width, height);
		this.columns = columns;
	}
	
	@Override
	protected void downloadInternal(final Piece piece, final DataInput in) throws IOException {
		final int[]	target = columns;
		
		if (isFastMode()) {
			for(int y = (int) piece.y(), maxY = (int) (piece.y()+piece.height()); y < maxY; y++) {
				target[y] = in.readInt();
			}
		}
		else {
			for(int y = (int) piece.y(), maxY = (int) (piece.y()+piece.height()); y < maxY; y++) {
				for(int x = (int) piece.x(), maxX = (int) (piece.x()+piece.width()); x < maxX; x++) {
					if (in.readBoolean()) {
						target[y] = x;
					}
				}
			}
		}
	}

	@Override
	protected void uploadInternal(Piece piece, DataOutput out) throws IOException {
		final int[]	source = columns;
		
		if (isFastMode()) {
			for(int y = (int) piece.y(), maxY = (int) (piece.y()+piece.height()); y < maxY; y++) {
				out.writeInt(source[y]);
			}
		}
		else {
			for(int y = (int) piece.y(), maxY = (int) (piece.y()+piece.height()); y < maxY; y++) {
				for(int x = (int) piece.x(), maxX = (int) (piece.x()+piece.width()); x < maxX; x++) {
					out.writeBoolean(source[y] == x);
				}
			}
		}
	}

	@Override
	protected GateMatrix multiplyInternal(GateMatrix another) throws CalculationException  {
		// TODO Auto-generated method stub
		final boolean	wasFast = another.setFastMode(false);
		
		if (another instanceof AbstractInMemoryGateMatrix) {
		}
		else {
			if (isVector(another)) {
				
				another.forEach(null);
			}
			else {
			}
		}
		another.setFastMode(wasFast);
		return null;
	}

	@Override
	protected GateMatrix multiplyAndTransposeInternal(final GateMatrix another) throws CalculationException {
		return multiplyInternal(another).transpose();
	}

	@Override
	protected GateMatrix transposeInternal() {
		final int[]	source = columns;
		final int[]	target = new int[(int) getWidth()];
		
		for(int index = 0; index < source.length; index++) {
			target[source[index]] = index;
		}
		return new InMemoryCommutationalMatrix(getHeight(), getWidth(), target);
	}

	@Override
	protected GateMatrix reduceInternal(final int qubitNo, final int qubitValue) {
		final int	mask = 1 << qubitNo;
		final int	value = qubitValue == 0 ? 0 : mask;
		final int[]	source = columns;
		final int[]	target = new int[(int) (getHeight() >> 1)];
		
		for(int index = value, where = 0; index < source.length; index+= mask) {
			if ((index & mask) == value) {
				target[where++] = source[index];
			}
		}
		return new InMemoryCommutationalMatrix(getHeight(), getWidth(), target);
	}

	@Override
	protected void forEachInternal(Piece piece, ForEachCallback callback) throws CalculationException {
		final int[]	source = columns;
		
		if (isFastMode()) {
			for(int y = (int) piece.y(), maxY = (int) (piece.y()+piece.height()); y < maxY; y++) {
				callback.process(y, source[y], 1, 0);
			}
		}
		else {
			for(int y = (int) piece.y(), maxY = (int) (piece.y()+piece.height()); y < maxY; y++) {
				for(int x = (int) piece.x(), maxX = (int) (piece.x()+piece.width()); x < maxX; x++) {
					callback.process(y, source[y], source[y] == x ? 1 : 0, 0);
				}
			}
		}
	}

	@Override
	public void close() throws CalculationException {
		super.close();
	}
}
