package chav1961.qu.util;

import java.io.EOFException;
import java.io.IOException;

import chav1961.purelib.streams.DataInputAdapter;

public class DataSource extends DataInputAdapter {
	private final int[]	intSource;
	private int	index = 0;
	
	public DataSource(final int... source) {
		this.intSource = source;
	}
	
	@Override
	public int readInt() throws IOException {
		if (index >= intSource.length) {
			throw new EOFException(); 
		}
		else {
			return intSource[index++];
		}
	}
}
