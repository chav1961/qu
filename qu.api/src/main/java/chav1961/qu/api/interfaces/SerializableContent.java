package chav1961.qu.api.interfaces;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * <p>This interface describes serializable vector/matrix content.</p>
 * @author achernomyrdin
 * @since 0.0.1
 */
public interface SerializableContent {
	/**
	 * <p>Get content value class. For commutation matrices, values need to be int.class of long.class,
	 * other matrix types are required to be float.class of double.class only.</p>
	 * @return content value class. Can't be null.
	 */
	Class<?> getValueClass();
	
	/**
	 * <p>Download piece of the matrix/vector with data. Input format of the data depends of {@linkplain GateMatrixType}
	 * value:</p>
	 * <ul>
	 * <li>{@linkplain GateMatrixType#COMMITATION_MATRIX commutation} matrix treats input as integer/long pairs (Y-coordinate of '1', X-coordinate of '1' inside the matrix).
	 * Integers are used when all sizes of the matrix (width or height) not exceeds maximum integer value {@value Integer#MAX_VALUE}, 
	 * longs are used otherwise</li>
	 * <li>{@linkplain GateMatrixType#SPARSE_MATRIX sparse} matrix treats input as structure of integer/long pairs (Y-coordinate of '1', X-coordinate of '1' inside the matrix)
	 * following with float/double pairs (real/image component of the complex value). Integers/longs are used exactly the same as in commutation matrix case. Floats/doubles are 
	 * used as a {@linkplain #getValueClass()} method call result</li>
	 * <li>{@linkplain GateMatrixType#DENSE_MATRIX dense} matrix treats input as float/double pairs (real/image component of the complex value). Floats/doubles are used as a 
	 * {@linkplain #getValueClass()} method call result</li>
	 * </ul>
	 * @param piece piece to load content to. Can't be null and must not overlaps with the matrix area. 
	 * @param in source to get content from. Can't be null
	 * @throws IOException on any I/O errors
	 */
	void download(Piece piece, DataInput in) throws IOException;
	
	/**
	 * <p>Upload piece of the matrix/vector into target. Format of the output data depends of {@linkplain GateMatrixType}
	 * value:</p>
	 * <ul>
	 * <li>{@linkplain GateMatrixType#COMMITATION_MATRIX commutation} matrix treats output as integer/long pairs (Y-coordinate of '1', X-coordinate of '1' inside the matrix).
	 * Integers are used when all sizes of the matrix (width or height) not exceeds maximum integer value {@value Integer#MAX_VALUE}, 
	 * longs are used otherwise</li>
	 * <li>{@linkplain GateMatrixType#SPARSE_MATRIX sparse} matrix treats output as structure of integer/long pairs (Y-coordinate of '1', X-coordinate of '1' inside the matrix)
	 * following with float/double pairs (real/image component of the complex value). Integers/longs are used exactly the same as in commutation matrix case. Floats/doubles are 
	 * used as a {@linkplain #getValueClass()} method call result</li>
	 * <li>{@linkplain GateMatrixType#DENSE_MATRIX dense} matrix treats output as float/double pairs (real/image component of the complex value). Floats/doubles are used as a 
	 * {@linkplain #getValueClass()} method call result</li>
	 * </ul>
	 * @param piece piece to extract content from. Can't be null and must not overlaps with the matrix area. 
	 * @param in target to store content to. Can't be null
	 * @throws IOException on any I/O errors
	 */
	void upload(Piece piece, DataOutput out) throws IOException;
}
