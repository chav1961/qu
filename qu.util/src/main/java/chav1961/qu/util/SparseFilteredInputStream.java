package chav1961.qu.util;

import java.io.InputStream;
import java.nio.ByteOrder;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.streams.byte2byte.DataInputStream;
import chav1961.qu.api.interfaces.GateMatrix.ForEachCallback;

public class SparseFilteredInputStream extends DataInputStream implements ForEachCallback {
	private final boolean	largeIndices;

	public SparseFilteredInputStream(InputStream in, final boolean largeIndices) {
		this(in, ByteOrder.BIG_ENDIAN, largeIndices);
	}

	public SparseFilteredInputStream(final InputStream in, final ByteOrder byteOrder, final boolean largeIndices) {
		super(in, byteOrder);
		this.largeIndices = largeIndices;
	}

	@Override
	public boolean process(final long x, final long y, final double real, final double image) throws CalculationException {
		// TODO Auto-generated method stub
		return false;
	}
	
}
