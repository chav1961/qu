package chav1961.qu.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.streams.byte2byte.DataOutputStream;
import chav1961.qu.api.interfaces.GateMatrix.ForEachCallback;

public class SparseFilteredOutputStream extends DataOutputStream implements ForEachCallback{
	private final double	epsilon;
	private final boolean	largeIndices;

	public SparseFilteredOutputStream(final OutputStream out, final double epsilon, final boolean largeIndices) throws NullPointerException {
		this(out, ByteOrder.BIG_ENDIAN, epsilon, largeIndices);
	}
	
	
	public SparseFilteredOutputStream(final OutputStream out, final ByteOrder order, final double epsilon, final boolean largeIndices) throws NullPointerException {
		super(out, order);
		if (epsilon <= 0) {
			throw new IllegalArgumentException("Epsilon value ["+epsilon+"] noust be greater than 0");
		}
		else {
			this.epsilon = epsilon;
			this.largeIndices = largeIndices;
		}
	}

	@Override
	public boolean process(final long x, final long y, final double real, final double image) throws CalculationException {
		if (Math.abs(real) > epsilon || Math.abs(image) > epsilon) {
			try {
				if (largeIndices) {
					writeLong(x);
					writeLong(y);
				}
				else {
					writeInt((int)x);
					writeInt((int)y);
				}
				return true;
			} catch (IOException e) {
				throw new CalculationException(e.getLocalizedMessage(), e);
			}
		}
		else {
			return false;
		}
	}
}
