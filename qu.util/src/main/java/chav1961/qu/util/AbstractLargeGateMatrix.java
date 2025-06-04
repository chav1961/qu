package chav1961.qu.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.qu.api.interfaces.GateMatrix;

class AbstractLargeGateMatrix extends AbstractGateMatrix {
	private final File			file;
	private final FileChannel	channel;
	
	protected AbstractLargeGateMatrix(final MatrixType type, final long width, final long height) throws CalculationException {
		super(type, width, height);
		try {
			this.file = File.createTempFile("temp", ".matrix");
			this.channel = FileChannel.open(file.toPath(), StandardOpenOption.CREATE);
			this.file.deleteOnExit();
		} catch (IOException e) {
			throw new CalculationException(e);
		}
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
	protected GateMatrix multiplyInternal(GateMatrix another) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected GateMatrix multiplyAndTransposeInternal(GateMatrix another) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected GateMatrix transposeInternal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected GateMatrix reduceInternal(int qubitNo, int qubitValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void forEachInternal(Piece piece, ForEachCallback callback) throws CalculationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws CalculationException {
		// TODO Auto-generated method stub
		try {
			channel.close();
			file.delete();
		} catch (IOException e) {
			throw new CalculationException(e);
		}
	}
	
	protected MappedByteBuffer getMapping(final Piece piece) throws CalculationException {
		return null;
	}
}
