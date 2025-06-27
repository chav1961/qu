package chav1961.qu.api.interfaces;

/**
 * <p>This interface describes piece of the matrix content</p>
 * @author achernomyrdin
 * @since 0.0.1
 */
public interface Piece {
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
	 * <p>This enumeration describes relations between two pieces (another piece related to current one).</p>
	 * @author achernomyrdin
	 * @since 0.0.1
	 */
	public static enum Relation {
		/**
		 * <p>Another piece is inside current one</p>
		 */
		INSIDE,
		/**
		 * <p>Pieces are identical</p>
		 */
		EQUALS,
		/**
		 * <p>Another piece overlaps current one</p>
		 */
		OVERLAPS,
		/**
		 * <p>Another piece is outside current one</p>
		 */
		OUTSIDE
	}
	
	/**
	 * <p>Get relation between current and another pieces</p>
	 * @param another piece to get relation for. Can't be null
	 * @return relation between pieces. Can't be null
	 */
	Relation related(Piece another); 
	
	/**
	 * <p>Make piece instance by it's coordinates</p>
	 * @param x X-xoordinate of the piece. Must be greater or equals than 0
	 * @param y Y-xoordinate of the piece. Must be greater or equals than 0
	 * @param width width of the piece. Must be greater than 0
	 * @param height height of the piece. Must be greater than 0
	 * @return piece implementation. Can not be null.
	 * @throws IllegalArgumentException in any argument errors
	 */
	static Piece of(final long x, final long y, final long width, final long height) throws IllegalArgumentException {
		if (x < 0) {
			throw new IllegalArgumentException("X-coordinate ["+x+"] is less than 0");
		}
		else if (y < 0) {
			throw new IllegalArgumentException("Y-coordinate ["+y+"] is less than 0");
		}
		else if (width <= 0) {
			throw new IllegalArgumentException("Width ["+width+"] is less than or equals 0");
		}
		else if (height <= 0) {
			throw new IllegalArgumentException("Height ["+height+"] is less than or equals 0");
		}
		else {
			return new PieceImpl(x, y, width, height);
		}
	}

	/**
	 * <p>Make piece instance to mark all matrix content</p>
	 * @param matrix matrix to make piece for. Can't be null.
	 * @return piece implementation. Can not be null.
	 * @throws NullPointerException matrix parameter is null
	 */
	static Piece of(final GateMatrix matrix) throws NullPointerException {
		if (matrix == null) {
			throw new NullPointerException("Matrix to get piece for can't be null");
		}
		else {
			return new PieceImpl(0, 0, matrix.getWidth(), matrix.getHeight());
		}
	}
}