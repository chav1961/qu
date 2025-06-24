package chav1961.qu.api.interfaces;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.CalculationException;

/**
 * <p>This interface describes quantum gate matrix</p>
 * @author achernomyrdin
 * @since 0.0.1
 */
public interface GateMatrix extends AutoCloseable {
	/**
	 * <p>This enumeration describes supported matrix types</p>
	 * @author achernomyrdin
	 * @since 0.0.1
	 */
	public static enum MatrixType {
		/**
		 * <p>Commutational matrix contains only real 0 and 1 values</p>
		 */
		COMMITATIONAL_MATRIX(true),
		/**
		 * <p>Sparse matrix contains complex values with it's location inside the matrix. All missing values defaults 0</p>
		 */
		SPARSE_MATRIX(true),
		/**
		 * <p>DEnde matrix contains all the complex values.</p>
		 */
		DENSE_MATRIX(false);
		
		private final boolean	fastMode;
		
		private MatrixType(final boolean fastMode) {
			this.fastMode = fastMode;
		}
		
		public boolean isFastModeSupported() {
			return fastMode;
		}
	}
	
	/**
	 * <p>This interface describes piece of the matrix content</p>
	 * @author achernomyrdin
	 * @since 0.0.1
	 */
	public static interface Piece {
		/**
		 * <p>Gets X-coordinate of the top left piece</p> 
		 * @return X-coordinate. Can't be less than 0
		 */
		long x();
		/**
		 * <p>Gets Y-coordinate of the top left piece</p> 
		 * @return Y-coordinate. Can't be less than 0
		 */
		long y();
		/**
		 * <p>Gets width of the piece</p> 
		 * @return width of the piece. Always greater than 0
		 */
		long width();
		/**
		 * <p>Gets height of the piece</p> 
		 * @return height of the piece. Always greater than 0
		 */
		long height();
		/**
		 * <p>Is point inside the piece</p> 
		 * @param x X-coordinate of the point.
		 * @param y Y-coordinate of the point.
		 * @return true in point inside the piece, false otherwise.
		 */
		boolean inside(long x, long y);
		
		/**
		 * <p>Make piece instance</p>
		 * @param x X-xoordinate of the piece. Must be greater or equals than 0
		 * @param y Y-xoordinate of the piece. Must be greater or equals than 0
		 * @param width width of the piece. Must be greater than 0
		 * @param height height of the piece. Must be greater than 0
		 * @return piece implementation. Can not be null.
		 */
		static Piece of(long x, long y, long width, long height) {
			return new Piece() {
				@Override public long x() {return x;}
				@Override public long y() {return y;}
				@Override public long width() {return width;}
				@Override public long height() {return height;}
				
				@Override
				public boolean inside(final long x, final long y) {
					return x >= x() && x < x()+width() && y >= y() && y < y()+height();
				}
				
				@Override
				public String toString() {
					return "Piece[x="+x()+",y="+y()+",width="+width()+",height="+height()+"]";
				}
			};
		}
	}

	/**
	 * <p>Get matrix width</p>
	 * @return matrix width. Always greater than 0
	 */
	long getWidth();

	/**
	 * <p>Get matrix height</p>
	 * @return matrix height. Always greater than 0
	 */
	long getHeight();
	
	/**
	 * <p>Get matrix type</p>
	 * @return matrix type. Can not be null
	 */
	MatrixType getType();
	
	/**
	 * <p>Is matrix in fast mode. Fast mode modifies behavior of the {@linkplain #download(DataInput)} and {@linkplain #upload(DataOutput)} methods</p> 
	 * @return true when fast node on, false otherwise.
	 */
	boolean isFastMode();
	
	/**
	 * <p>Set fast mode for the matrix</p>
	 * @param on true - fast mode, false otherwise
	 * @return previous value of the fast mode.
	 */
	boolean setFastMode(boolean on);
	
	/**
	 * <p>Is matrix in parallel mode. Parallel mode splits calculation of all arithmetic operations on a few threads</p></p>
	 * @return true - when parallel mode is on, false otherwise
	 */
	boolean isParallelMode();
	
	/**
	 * <p>Set parallel mode for the matrix</p>
	 * @param on true - parallel mode, false otherwise
	 * @return previous value of the parallel mode.
	 */
	boolean setParallelMode(boolean on);

	/**
	 * <p>This interface is used to process matrix contents</p>
	 * @author achernomyrdin
	 * @since 0.0.1
	 */
	@FunctionalInterface
	public interface ForEachCallback {
		/**
		 * <p>Process matrix content</p>
		 * @param x X-coordinate of the matrix cell to process.
		 * @param y Y-coordinate of the matrix cell to process.
		 * @param real real value in the matrix cell.
		 * @param image image value in the matrix cell.
		 * @return true- continue processing, false - terminate processing
		 * @throws CalculationException on any calculation errors
		 */
		boolean process(long x, long y, double real, double image) throws CalculationException;
	}

	/**
	 * <p>Download data into matrix. When fast mode is off, loads boolean (for commutational matrices) of complex
	 * values from stream. When fast mode is on, then:</p>
	 * <ul>
	 * <li>for commutational matrices reads X- and Y- coordinates for non-zero values as integers</li>  
	 * <li>for sparse matrices reads X- and Y- coordinates for non-zero values as integers, then real and image of the complex values</li>  
	 * </ul>
	 * <p>Dense matrix doesn't support fast mode</p>   
	 * @param piece piece to load data into. Can not be null. Data will be filled row-by-row and column-by-column inside the row
	 * @param in source to load data. Can not be null.
	 * @param callback callback that grants to load current value into matrix. Can not be null
	 * @throws IOException on any I/O errors
	 */
	void download(Piece piece, DataInput in, ForEachCallback callback) throws IOException;
	
	/**
	 * <p>Download data into matrix. When fast mode is off, loads boolean (for commutational matrices) of complex
	 * values from stream. When fast mode is on, then:</p>
	 * <ul>
	 * <li>for commutational matrices reads X- and Y- coordinates for non-zero values as integers</li>  
	 * <li>for sparse matrices reads X- and Y- coordinates for non-zero values as integers, then real and image of the complex values</li>  
	 * </ul>
	 * <p>Dense matrix doesn't support fast mode</p>   
	 * @param piece piece to load data into. Can not be null. Data will be filled row-by-row and column-by-column inside the row
	 * @param in source to load data. Can not be null.
	 * @throws IOException on any I/O errors
	 */
	default void download(Piece piece, DataInput in) throws IOException {
		download(piece, in, (x,y,r,i)->true);
	}

	/**
	 * <p>Download data into matrix. When fast mode is off, loads boolean (for commutational matrices) of complex
	 * values from stream. When fast mode is on, then:</p>
	 * <ul>
	 * <li>for commutational matrices reads X- and Y- coordinates for non-zero values as integers</li>  
	 * <li>for sparse matrices reads X- and Y- coordinates for non-zero values as integers, then real and image of the complex values</li>  
	 * </ul>
	 * <p>Dense matrix doesn't support fast mode</p>   
	 * @param in source to load data. Can not be null.
	 * @param callback callback that grants to load current value into matrix. Can not be null
	 * @throws IOException on any I/O errors
	 */
	default void download(DataInput in) throws IOException {
		download(totalPiece(), in);
	}
	
	/**
	 * <p>Upload data from matrix. When fast mode is off, stores each cell row-by-row and column-by-column inside the row.
	 * When fast mode is on, then:</p> 
	 * <ul>
	 * <li>for commutational matrices writes X- and Y- coordinates for non-zero values as integers</li>  
	 * <li>for sparse matrices writes X- and Y- coordinates for non-zero values as integers, then real and image of the complex values</li>  
	 * </ul>
	 * @param piece piece to store data into. Can not be null. Data will be filled row-by-row and column-by-column inside the row
	 * @param out target to load data. Can not be null.
	 * @param callback callback that grants to store current value into matrix. Can not be null
	 * @throws IOException on any I/O errors
	 */
	void upload(Piece piece, DataOutput out, ForEachCallback callback) throws IOException;

	/**
	 * <p>Upload data from matrix. When fast mode is off, stores each cell row-by-row and column-by-column inside the row.
	 * When fast mode is on, then:</p> 
	 * <ul>
	 * <li>for commutational matrices writes X- and Y- coordinates for non-zero values as integers</li>  
	 * <li>for sparse matrices writes X- and Y- coordinates for non-zero values as integers, then real and image of the complex values</li>  
	 * </ul>
	 * @param piece piece to store data into. Can not be null. Data will be filled row-by-row and column-by-column inside the row
	 * @param out target to load data. Can not be null.
	 * @throws IOException on any I/O errors
	 */
	default void upload(Piece piece, DataOutput out) throws IOException {
		upload(piece, out, (x,y,r,i)->true);
	}

	/**
	 * <p>Upload data from matrix. When fast mode is off, stores each cell row-by-row and column-by-column inside the row.
	 * When fast mode is on, then:</p> 
	 * <ul>
	 * <li>for commutational matrices writes X- and Y- coordinates for non-zero values as integers</li>  
	 * <li>for sparse matrices writes X- and Y- coordinates for non-zero values as integers, then real and image of the complex values</li>  
	 * </ul>
	 * @param out target to load data. Can not be null.
	 * @throws IOException on any I/O errors
	 */
	default void upload(DataOutput out) throws IOException {
		upload(totalPiece(), out);
	}

	/**
	 * <p>Multiply matrix and return product</p>
	 * @param another matrix to multiply. Can not be null.
	 * @return new matrix with product values. Can not be null.
	 * @throws CalculationException on any calculation errors.
	 */
	GateMatrix multiply(GateMatrix another) throws CalculationException;
	
	/**
	 * <p>Multiply and transpose matrix and return product</p>
	 * @param another matrix to multiply. Can not be null.
	 * @return new matrix with product values. Can not be null.
	 * @throws CalculationException on any calculation errors.
	 */
	GateMatrix multiplyAndTranspose(GateMatrix another) throws CalculationException;
	
	/**
	 * <p>Transpose matrix and return new matrix</p>
	 * @return new transposed matrix. Can not be null.
	 * @throws CalculationException on any calculation errors.
	 */
	GateMatrix transpose() throws CalculationException;
	
	/**
	 * <p>Scratch rows and columns for source matrix and return new reduced matrix</p> 
	 * @param qibitNo qubit number to scratch. Can not be less than 0.
	 * @param qubitValue qubit value to scratch. Can be 0 or 1 only.
	 * @return new reduced matrix. Can not be null.
	 * @throws CalculationException on any calculation errors.
	 */
	GateMatrix reduce(int qibitNo, int qubitValue) throws CalculationException;
	
	/**
	 * <p>Cast matrix from one type to another</p>
	 * @param type target type of the casted matrix. Can not be null.
	 * @return new casted matrix. Can not be null
	 * @throws CalculationException on any calculation errors.
	 */
	GateMatrix cast(MatrixType type) throws CalculationException;

	/**
	 * <p>Execute callback for each matrix cell. When fast mode is off, process all matrix cells, when false node is on,
	 * process only non-zero matrix elements</p> 
	 * @param piece matrix piece to process. Can not be null
	 * @param callback callback to process matrix cell values. Can not be null 
	 * @throws CalculationException on any calculation errors.
	 */
	void forEach(Piece piece, ForEachCallback callback) throws CalculationException;

	/**
	 * <p>Execute callback for each matrix cell</p> 
	 * @param callback callback to process matrix cell values. Can not be null 
	 * @throws CalculationException on any calculation errors.
	 */
	default void forEach(ForEachCallback callback) throws CalculationException {
		forEach(totalPiece(), callback);
	}

	@Override
	void close() throws CalculationException;
	
	private Piece totalPiece() {
		return Piece.of(0, 0, getWidth(), getHeight());
	}
}
