package chav1961.qu.api.interfaces;

/**
 * <p>This enumeration describes supported matrix types</p>
 * @author achernomyrdin
 * @since 0.0.1
 */
public enum GateMatrixType {
	/**
	 * <p>Commutation matrix contains only '0' and '1' integer values. To reduce memory, this matrix stores coordinates of '1' inside
	 * matrix instead of keep all data.</p>
	 */
	COMMITATION_MATRIX(true),

	/**
	 * <p>Sparse matrix contains complex values with it's location inside the matrix. All missing values treats as '0+0i' value. 
	 * Complex values are stored as two sequential float/double values.</p>
	 */
	SPARSE_MATRIX(true),
	
	/**
	 * <p>Dense matrix contains all the complex values. Complex values are stored as two sequential float/double values.</p>
	 */
	DENSE_MATRIX(false);
	
	private final boolean	fastMode;
	
	private GateMatrixType(final boolean fastMode) {
		this.fastMode = fastMode;
	}
	
	public boolean isFastModeSupported() {
		return fastMode;
	}
}