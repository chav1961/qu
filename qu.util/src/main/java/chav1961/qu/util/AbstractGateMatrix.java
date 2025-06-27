package chav1961.qu.util;


import java.io.DataInput;

import java.io.DataOutput;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.qu.api.interfaces.GateMatrix;
import chav1961.qu.api.interfaces.GateMatrixType;
import chav1961.qu.api.interfaces.Piece;

public abstract class AbstractGateMatrix implements GateMatrix {
	private static final int	NUMBER_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();
	
	private final GateMatrixType	type;
	private final long			width;
	private final long			height;
	private final boolean		parallelModeEnable;
	private boolean				parallelMode = false;
	private boolean				fastMode = false;

	protected static enum ComplexOp {
		ADD,
		SUBTRACT,
		MULTIPLY,
		DIVIDE
	}
	
	protected AbstractGateMatrix(final GateMatrixType type, final long width, final long height, final boolean parallelModeEnable) {
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
			this.width = width;
			this.height = height;
			this.parallelModeEnable = parallelModeEnable;
		}
	}

	public abstract void close() throws CalculationException;
	protected abstract void downloadInternal(Piece piece, DataInput in, ForEachCallback callback) throws IOException;
	protected abstract void uploadInternal(Piece piece, DataOutput out, ForEachCallback callback) throws IOException;
	
	protected abstract GateMatrix multiplyInternal(GateMatrix another) throws CalculationException;
	protected abstract GateMatrix multiplyAndTransposeInternal(GateMatrix another) throws CalculationException;
	protected abstract GateMatrix transposeInternal() throws CalculationException;
	protected abstract GateMatrix reduceInternal(int qubitNo, int qubitValue) throws CalculationException;
	protected abstract void forEachInternal(Piece piece, ForEachCallback callback) throws CalculationException;

	protected abstract GateMatrix multiplyInternalP(GateMatrix another) throws CalculationException;
	protected abstract GateMatrix multiplyAndTransposeInternalP(GateMatrix another) throws CalculationException;
	protected abstract GateMatrix transposeInternalP() throws CalculationException;
	protected abstract GateMatrix reduceInternalP(int qubitNo, int qubitValue) throws CalculationException;
	protected abstract void forEachInternalP(Piece piece, ForEachCallback callback) throws CalculationException;
	
	@Override
	public long getWidth() {
		return width;
	}

	@Override
	public long getHeight() {
		return height;
	}

	@Override
	public GateMatrixType getType() {
		return type;
	}

	@Override
	public boolean isFastMode() {
		return fastMode;
	}
	
	@Override
	public boolean setFastMode(final boolean on) {
		if (on && !getType().isFastModeSupported()) {
			throw new IllegalArgumentException("Matrix with type ["+getType()+"] doesn't support fast mode");
		}
		else {
			final boolean	result = isFastMode();
			
			this.fastMode = on;
			return result;
		}
	}
	
	@Override
	public boolean isParallelMode() {
		return parallelMode;
	}
	
	@Override
	public boolean setParallelMode(final boolean on) {
		if (on && !parallelModeEnable) {
			throw new IllegalArgumentException("Parallel mode is not enabled for this matrix");
		}
		else {
			final boolean	result = isParallelMode();
			
			this.parallelMode = on;
			return result;
		}
	}
	
	@Override
	public void download(final Piece piece, final DataInput in, final ForEachCallback callback) throws IOException {
		if (piece == null || !isPieceValid(piece)) {
			throw new IllegalArgumentException("Piece is null or not inside the matrix");
		}
		else if (in == null) {
			throw new NullPointerException("Data input can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			downloadInternal(piece, in, callback);
		}
	}

	@Override
	public void upload(final Piece piece, final DataOutput out, final ForEachCallback callback) throws IOException {
		if (piece == null || !isPieceValid(piece)) {
			throw new IllegalArgumentException("Piece is null or not inside the matrix");
		}
		else if (out == null) {
			throw new NullPointerException("Data output can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			uploadInternal(piece, out, callback);
		}
	}

	@Override
	public GateMatrix multiply(final GateMatrix another) throws CalculationException {
		if (another == null) {
			throw new NullPointerException("Another matrix can't be null");
		}
		else if (another.getHeight() != getWidth()) {
			throw new IllegalArgumentException("Another matrix height ["+another.getHeight()+"] is differ with current matrix width ["+getWidth()+"]");
		}
		else if (isParallelMode()){
			return multiplyInternalP(another);
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
		else if (another.getHeight() != getWidth()) {
			throw new IllegalArgumentException("Another matrix height ["+another.getHeight()+"] is differ with current matrix width ["+getWidth()+"]");
		}
		else if (isParallelMode()){
			return multiplyAndTransposeInternalP(another);
		}
		else {
			return multiplyAndTransposeInternal(another);
		}
	}
	
	@Override
	public GateMatrix transpose() throws CalculationException {
		return isParallelMode() ? transposeInternalP() :  transposeInternal();
	}
	
	@Override
	public GateMatrix reduce(final int qubitNo, final int qubitValue) throws CalculationException {
		if (getWidth() != getHeight()) {
			throw new IllegalStateException("This method is applicable for square matrices only");
		}
		else if (qubitNo < 0 || (1L << qubitNo) >= getWidth()) {
			throw new IllegalArgumentException("Qubit number ["+qubitNo+"] out of range 0.."+logWidth(getWidth()));
		}
		else if (qubitValue != 0 && qubitValue != 1) {
			throw new IllegalArgumentException("Qubit value ["+qubitValue+"] can be either 0 or 1 only");
		}
		else if (isParallelMode()) {
			return reduceInternalP(qubitNo, qubitValue);
		}
		else {
			return reduceInternal(qubitNo, qubitValue);
		}
	}

	@Override
	public GateMatrix cast(final GateMatrixType type) throws CalculationException {
		// TODO Auto-generated method stub
		if (type == null) {
			throw new NullPointerException("Matrix type can't be null");
		}
		else {
			switch (type) {
				case COMMITATION_MATRIX	:
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
		else if (isParallelMode()){
			forEachInternalP(piece, callback);
		}
		else {
			forEachInternal(piece, callback);
		}
	}

	protected boolean isVector() {
		return isVector(this);
	}

	protected static long toBitMask(final int qubitNo) {
		return 1L << qubitNo;
	}

	protected static Piece totalPiece(final GateMatrix matrix) {
		return Piece.of(0, 0, matrix.getWidth(), matrix.getHeight());
	}
	
	protected static Piece[] parallelSplit(final Piece source) {
		if (isVector(source)) {
			if (source.width() == 1) {
				return parallelSplitY(source, NUMBER_OF_PROCESSORS);
			}
			else {
				return parallelSplitX(source, NUMBER_OF_PROCESSORS);
			}
		}
		else {
			return parallelSplitX(source, NUMBER_OF_PROCESSORS);
		}
	}

	private static Piece[] parallelSplitX(final Piece source, final int counter) {
		final Piece[]	result = new Piece[counter];
		final long		step = (source.width() + counter - 1) / counter;
		int		where = 0;
		
		for(long index = source.x(), maxIndex = source.x() + source.width(); index < maxIndex; index += step) {
			result[where++] = Piece.of(index, source.y(), Math.min(step, maxIndex - index), source.height());
		}
		return result;
	}
	
	private static Piece[] parallelSplitY(final Piece source, final int counter) {
		final Piece[]	result = new Piece[counter];
		final long		step = (source.height() + counter - 1) / counter;
		int		where = 0;
		
		for(long index = source.y(), maxIndex = source.y() + source.height(); index < maxIndex; index += step) {
			result[where++] = Piece.of(source.x(), index, source.width(), Math.min(step, maxIndex - index));
		}
		return result;
	}
	
	protected static boolean isVector(final GateMatrix matrix) {
		return matrix.getWidth() == 1 || matrix.getHeight() == 1; 
	}

	protected static boolean isVector(final Piece piece) {
		return piece.width() == 1 || piece.height() == 1; 
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
