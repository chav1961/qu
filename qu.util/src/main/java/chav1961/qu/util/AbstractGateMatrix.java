package chav1961.qu.util;


import java.io.DataInput;

import java.io.DataOutput;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.qu.api.interfaces.GateMatrix;

public abstract class AbstractGateMatrix implements GateMatrix {
	private final MatrixType	type;
	private final long		width;
	private final long		height;
	private final boolean	isVector;
	private boolean			fastMode = true;

	protected AbstractGateMatrix(final MatrixType type, final long width, final long height) {
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else if (width <= 0) {
			throw new IllegalArgumentException("Matrix width ["+width+"] must be greater than 0");
		}
		else if (height <= 0) {
			throw new IllegalArgumentException("Matrix height ["+height+"] must be greater than 0");
		}
		else if (width != 1 && height != 1 && width != height) {
			throw new IllegalArgumentException("Matrix must be either vector (1*x or y*1) or square matrix(x == y)");
		}
		else {
			this.type = type;
			this.isVector = width == 1 || height == 1;
			this.width = width;
			this.height = height;
		}
	}

	public abstract void close() throws CalculationException;
	protected abstract void downloadInternal(Piece piece, DataInput in) throws IOException;
	protected abstract void uploadInternal(Piece piece, DataOutput out) throws IOException;
	protected abstract GateMatrix multiplyInternal(GateMatrix another) throws CalculationException;
	protected abstract GateMatrix multiplyAndTransposeInternal(GateMatrix another) throws CalculationException;
	protected abstract GateMatrix transposeInternal() throws CalculationException;
	protected abstract GateMatrix reduceInternal(int qubitNo, int qubitValue) throws CalculationException;
	protected abstract void forEachInternal(Piece piece, ForEachCallback callback) throws CalculationException;
	
	@Override
	public long getWidth() {
		return width;
	}

	@Override
	public long getHeight() {
		return height;
	}

	@Override
	public MatrixType getType() {
		return type;
	}

	@Override
	public boolean isFastMode() {
		return fastMode;
	}
	
	@Override
	public boolean setFastMode(final boolean on) {
		final boolean	result = isFastMode();
		
		this.fastMode = on;
		return result;
	}
	
	@Override
	public void download(final Piece piece, final DataInput in) throws IOException {
		if (piece == null || !isPieceValid(piece)) {
			throw new IllegalArgumentException("Piece is null or not inside the matrix");
		}
		else if (in == null) {
			throw new NullPointerException("Data input can't be null");
		}
		else {
			downloadInternal(piece, in);
		}
	}

	@Override
	public void upload(Piece piece, DataOutput out) throws IOException {
		if (piece == null || !isPieceValid(piece)) {
			throw new IllegalArgumentException("Piece is null or not inside the matrix");
		}
		else if (out == null) {
			throw new NullPointerException("Data output can't be null");
		}
		else {
			uploadInternal(piece, out);
		}
	}

	@Override
	public GateMatrix multiply(final GateMatrix another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Another matrix can't be null");
		}
		else if (another.getHeight() != getWidth()) {
			throw new IllegalArgumentException("Another matriz height ["+another.getHeight()+"] is differ with current matrix width ["+getWidth()+"]");
		}
		else {
			return multiplyInternal(another);
		}
	}

	@Override
	public GateMatrix multiplyAndTranspose(final GateMatrix another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Another matrix can't be null");
		}
		else {
			return multiplyAndTransposeInternal(another);
		}
	}
	
	@Override
	public GateMatrix transpose() throws CalculationException {
		return transposeInternal();
	}
	
	@Override
	public GateMatrix reduce(final int qubitNo, final int qubitValue) throws CalculationException {
		// TODO Auto-generated method stub
		if (getWidth() != getHeight()) {
			throw new IllegalStateException("This method is applicable for square matrices only");
		}
		else if (qubitNo < 0 || (1L << qubitNo) >= getWidth()) {
			throw new IllegalArgumentException("Qubit number ["+qubitNo+"] out of range 0.."+logWidth(getWidth()));
		}
		else if (qubitValue != 0 && qubitValue != 1) {
			throw new IllegalArgumentException("Qubit value ["+qubitValue+"] can be either 0 or 1 only");
		}
		else {
			return reduceInternal(qubitNo, qubitValue);
		}
	}

	@Override
	public GateMatrix cast(final MatrixType type) throws CalculationException {
		// TODO Auto-generated method stub
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else {
			switch (type) {
				case COMMITATIONAL_MATRIX	:
					break;
				case DENSE_MATRIX	:
					break;
				case SPARSE_MATRIX	:
					break;
				default :
					throw new UnsupportedOperationException("Matrix type ["+type+"] is not supported yet");
			}
			return null;
		}
	}

	@Override
	public void forEach(final Piece piece, final ForEachCallback callback) throws CalculationException {
		if (piece == null || !isPieceValid(piece)) {
			throw new IllegalArgumentException("Piece is null or not inside the matrix");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			forEachInternal(piece, callback);
		}
	}

	protected boolean isVector() {
		return isVector;
	}
	
	protected static boolean isVector(final GateMatrix matrix) {
		return matrix.getWidth() == 1 || matrix.getHeight() == 1; 
	}
	
	private boolean isPieceValid(final Piece piece) {
		if (piece.x() < 0 || piece.x() >= getWidth()) {
			return false;
		}
		else if (piece.y() < 0 || piece.y() >= getHeight()) {
			return false;
		}
		else if (piece.x() + piece.width() < 0 || piece.x() + piece.width() >= getWidth()) {
			return false;
		}
		else if (piece.y() + piece.height() < 0 || piece.y() + piece.height() >= getHeight()) {
			return false;
		}
		else {
			return true;
		}
	}

	private String logWidth(long width) {
		for(int index = 0; index < 64; index++, width >>= 1) {
			if (width == 0) {
				return ""+index;
			}
		}
		return "0";
	}
}
