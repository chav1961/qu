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
public interface GateMatrix extends AutoCloseable, SerializableContent {
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
	GateMatrixType getType();
	
	/**
	 * <p>Is matrix in fast mode. Fast mode modifies behavior of the {@linkplain #download(DataInput)}, {@linkplain #upload(DataOutput)} 
	 * and {@linkplain #forEach(ForEachCallback)} methods:</p>
	 * <ul>
	 * <li>when fast node is <b>off</b>, all matrix types are treated as dense matrices</li> 
	 * <li>when fast node is <b>on</b>, each matrix type is processed as described in the appropriative methods</li> 
	 * </ul> 
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
		 * @return true - continue processing, false - terminate processing
		 * @throws CalculationException on any calculation errors
		 */
		boolean process(long x, long y, double real, double image) throws CalculationException;
	}

	/**
	 * <p>Download data into matrix. When fast mode is off, loads boolean (for commutational matrices) of complex
	 * values from stream. When fast mode is on, then:</p>
	 * <ul>
	 * <li>for commutation matrices reads X- and Y- coordinates for non-zero values as integers</li>  
	 * <li>for sparse matrices reads X- and Y- coordinates for non-zero values as integers, then real and image of the complex values</li>  
	 * </ul>
	 * <p>Dense matrix doesn't support fast mode</p>   
	 * @param piece piece to load data into. Can not be null. Data will be filled row-by-row and column-by-column inside the row
	 * @param in source to load data. Can not be null.
	 * @param callback callback that grants to load current value into matrix. Can not be null. Must return true to download current value, or false to skip it.
	 * @throws IOException on any I/O errors
	 */
	void download(Piece piece, DataInput in, ForEachCallback callback) throws IOException;
	
	/**
	 * <p>Download data into matrix. When fast mode is off, loads boolean (for commutational matrices) of complex
	 * values from stream. When fast mode is on, then:</p>
	 * <ul>
	 * <li>for commutation matrices reads X- and Y- coordinates for non-zero values as integers</li>  
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
	 * <li>for commutation matrices reads X- and Y- coordinates for non-zero values as integers</li>  
	 * <li>for sparse matrices reads X- and Y- coordinates for non-zero values as integers, then real and image of the complex values</li>  
	 * </ul>
	 * <p>Dense matrix doesn't support fast mode</p>   
	 * @param in source to load data. Can not be null.
	 * @param callback callback that grants to load current value into matrix. Can not be null
	 * @throws IOException on any I/O errors
	 */
	default void download(DataInput in) throws IOException {
		download(Piece.of(this), in);
	}
	
	/**
	 * <p>Upload data from matrix. When fast mode is off, stores each cell row-by-row and column-by-column inside the row.
	 * When fast mode is on, then:</p> 
	 * <ul>
	 * <li>for commutation matrices writes X- and Y- coordinates for non-zero values as integers</li>  
	 * <li>for sparse matrices writes X- and Y- coordinates for non-zero values as integers, then real and image of the complex values</li>  
	 * </ul>
	 * @param piece piece to store data into. Can not be null. Data will be filled row-by-row and column-by-column inside the row
	 * @param out target to load data. Can not be null.
	 * @param callback callback that grants to load current value from matrix. Can not be null. Must return true to upload current value, or false to skip it.
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
		upload(Piece.of(this), out);
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
	GateMatrix cast(GateMatrixType type) throws CalculationException;

	/**
	 * <p>Execute callback for each matrix cell. When fast mode is off, process all matrix cells, when false node is on,
	 * process only non-zero matrix elements</p> 
	 * @param piece matrix piece to process. Can not be null
	 * @param callback callback to process matrix cell values. Can not be null. Must return true to continue processing, or false to abort continuation. 
	 * @throws CalculationException on any calculation errors.
	 */
	void forEach(Piece piece, ForEachCallback callback) throws CalculationException;

	/**
	 * <p>Execute callback for each matrix cell</p> 
	 * @param callback callback to process matrix cell values. Can not be null. Must return true to continue processing, or false to abort continuation. 
	 * @throws CalculationException on any calculation errors.
	 */
	default void forEach(ForEachCallback callback) throws CalculationException {
		forEach(Piece.of(this), callback);
	}

	@Override
	void close() throws CalculationException;
}
