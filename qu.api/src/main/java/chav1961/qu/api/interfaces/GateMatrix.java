package chav1961.qu.api.interfaces;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.CalculationException;

public interface GateMatrix {
	public static enum MatrixType {
		COMMITATIONAL_MATRIX,
		SPARSE_MATRIX,
		DENSE_MATRIX,
	}
	
	public static interface Piece {
		long x();
		long y();
		long width();
		long height();
		
		static Piece of(long x, long y, long width, long height) {
			return new Piece() {
				@Override public long x() {return x;}
				@Override public long y() {return y;}
				@Override public long width() {return width;}
				@Override public long height() {return height;}
				
				@Override
				public String toString() {
					return "Piece[x="+x()+",y="+y()+",width="+width()+",height="+height()+"]";
				}
			};
		}
	}
	
	long width();
	long height();
	
	void download(Piece piece, DataInput in) throws IOException;
	default void download(DataInput in) throws IOException {
		download(totalPiece(), in);
	}
	
	void upload(Piece piece, DataOutput out) throws IOException;
	default void upload(DataOutput out) throws IOException {
		upload(totalPiece(), out);
	}
	
	GateMatrix multiply(GateMatrix another) throws CalculationException;
	GateMatrix reduce(int qibitNo, int qubitValue) throws CalculationException;
	GateMatrix cast(MatrixType type) throws CalculationException;

	@FunctionalInterface
	public interface ForEachCallback {
		boolean process(long x, long y, double real, double image) throws CalculationException;
	}
	
	void forEach(Piece piece, ForEachCallback callback) throws CalculationException;
	default void forEach(ForEachCallback callback) throws CalculationException {
		forEach(totalPiece(), callback);
	}
	
	private Piece totalPiece() {
		return Piece.of(0, 0, width(), height());
	}
}
