package chav1961.qu.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.streams.DataOutputAdapter;
import chav1961.qu.api.interfaces.GateMatrix;
import chav1961.qu.api.interfaces.GateMatrix.ForEachCallback;

public class DenseInMemoryGateMatrix extends AbstractInMemoryGateMatrix {
	private final double[][]	content;

	DenseInMemoryGateMatrix(long width, long height, boolean parallelModeOn) {
		super(MatrixType.DENSE_MATRIX, width, height, parallelModeOn);
		this.content = new double[(int) height][(int) (2*width)];
	}

	private DenseInMemoryGateMatrix(long width, long height, boolean parallelModeOn, final double[][] content) {
		super(MatrixType.DENSE_MATRIX, width, height, parallelModeOn);
		this.content = content;
	}
	
	@Override
	protected void downloadInternal(final Piece piece, final DataInput in, final ForEachCallback callback) throws IOException {
		final double[][]	temp = content;
		final int	xFrom = (int) piece.x(), xTo = (int) (piece.x() + piece.width());
		
		for(int y = (int)piece.y(), maxY = (int)(piece.y() + piece.height()); y < maxY; y++) {
			final double[] line = temp[y];
			
			for(int x = xFrom; x < xTo; x++) {
				final double	real = in.readDouble();
				final double	image = in.readDouble();
				
				try {
					if (callback.process(x, y, real, image)) {
						line[2*x] = real; 
						line[2*x+1] = image; 
					}
				} catch (CalculationException e) {
					throw new IOException(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	@Override
	protected void uploadInternal(final Piece piece, final DataOutput out, final ForEachCallback callback) throws IOException {
		final double[][]	temp = content;
		final int	xFrom = (int) piece.x(), xTo = (int) (piece.x() + piece.width());
		
		for(int y = (int)piece.y(), maxY = (int)(piece.y() + piece.height()); y < maxY; y++) {
			final double[] line = temp[y];
			
			for(int x = xFrom; x < xTo; x++) {
				final double	real = line[2*x];
				final double	image = line[2*x+1];
				
				try {
					if (callback.process(x, y, real, image)) {
						out.writeDouble(real); 
						out.writeDouble(image); 
					}
				} catch (CalculationException e) {
					throw new IOException(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	@Override
	protected GateMatrix multiplyInternal(final GateMatrix another) throws CalculationException {
		final double[][]	left = content;
		final double[][]	result = new double[(int) getHeight()][(int) (2 * another.getWidth())];
		final double[]		sum = new double[2];
		
		if (another instanceof DenseInMemoryGateMatrix) {
			final DenseInMemoryGateMatrix	mat = (DenseInMemoryGateMatrix)another;
			final double[][]	right = mat.content;
			
			for(int y = 0; y < getHeight(); y++) {
				final double[]	leftLine = left[y];

				for(int x = 0; x < mat.getWidth(); x++) {
					sum[0] = sum[1] = 0;

					for(int index = 0; index < getWidth(); index++) {
						calcAndAdd(leftLine[2*index], leftLine[2*index+1], ComplexOp.MULTIPLY, right[2*index][x], right[2*index+1][x], sum, 0);
					}
					result[y][2*x] = sum[0];
					result[y][2*x+1] = sum[1];
				}
			}
			return new DenseInMemoryGateMatrix(getWidth(), mat.getHeight(), isParallelMode(), result);
		}
// TODO Auto-generated method stub
//		else if (another instanceof SparseInMemoryGateMatrix) {
//			
//		}
//		else if (another instanceof InMemoryCommutationalMatrix) {
//			
//		}
		else {
			for(int y = 0; y < getHeight(); y++) {
				final double[]	leftLine = left[y];

				for(int x = 0; x < another.getWidth(); x++) {
					sum[0] = sum[1] = 0;

					try {
						another.upload(Piece.of(0, x, getWidth(), 1), new DataOutputAdapter() {
							int	index = 0;
							double	real, image;
							
							@Override
							public void writeDouble(double v) throws IOException {
								if ((index & 0x01) == 0) {
									real = v;
								}
								else {
									image = v;
									calcAndAdd(leftLine[2*index], leftLine[2*index+1], ComplexOp.MULTIPLY, real, image, sum, 0);
									index++;
								}
							}
						});
					} catch (IOException e) {
						throw new CalculationException(e.getLocalizedMessage(), e);
					}
					result[y][2*x] = sum[0];
					result[y][2*x+1] = sum[1];
				}
			}
			return new DenseInMemoryGateMatrix(getWidth(), another.getHeight(), isParallelMode(), result);
		}
	}

	@Override
	protected GateMatrix multiplyAndTransposeInternal(final GateMatrix another) throws CalculationException {
		final double[][]	left = content;
		final double[][]	result = new double[(int) another.getWidth()][(int) (2 * getHeight())];
		final double[]		sum = new double[2];
		
		if (another instanceof DenseInMemoryGateMatrix) {
			final DenseInMemoryGateMatrix	mat = (DenseInMemoryGateMatrix)another;
			final double[][]	right = mat.content;
			
			for(int y = 0; y < getHeight(); y++) {
				final double[]	leftLine = left[y];

				for(int x = 0; x < mat.getWidth(); x++) {
					sum[0] = sum[1] = 0;

					for(int index = 0; index < getWidth(); index++) {
						calcAndAdd(leftLine[2*index], leftLine[2*index+1], ComplexOp.MULTIPLY, right[2*index][x], right[2*index+1][x], sum, 0);
					}
					result[y][2*x] = sum[0];
					result[y][2*x+1] = sum[1];
				}
			}
			return new DenseInMemoryGateMatrix(getWidth(), mat.getHeight(), isParallelMode(), result);
		}
// TODO Auto-generated method stub
//		else if (another instanceof SparseInMemoryGateMatrix) {
//			
//		}
//		else if (another instanceof InMemoryCommutationalMatrix) {
//			
//		}
		else {
			for(int y = 0; y < getHeight(); y++) {
				final double[]	leftLine = left[y];

				for(int x = 0; x < another.getWidth(); x++) {
					sum[0] = sum[1] = 0;

					try {
						another.upload(Piece.of(0, x, getWidth(), 1), new DataOutputAdapter() {
							int	index = 0;
							double	real, image;
							
							@Override
							public void writeDouble(double v) throws IOException {
								if ((index & 0x01) == 0) {
									real = v;
								}
								else {
									image = v;
									calcAndAdd(leftLine[2*index], leftLine[2*index+1], ComplexOp.MULTIPLY, real, image, sum, 0);
									index++;
								}
							}
						});
					} catch (IOException e) {
						throw new CalculationException(e.getLocalizedMessage(), e);
					}
					result[x][2*y] = sum[0];
					result[x][2*y+1] = sum[1];
				}
			}
			return new DenseInMemoryGateMatrix(another.getHeight(), getWidth(), isParallelMode(), result);
		}
	}

	@Override
	protected GateMatrix transposeInternal() throws CalculationException {
		final double[][]	result = new double[(int) getWidth()][(int) getHeight()];
		
		transposeInternal(totalPiece(this), content, result);
		return new DenseInMemoryGateMatrix(getWidth(), getHeight(), isParallelMode(), result);
	}

	@Override
	protected GateMatrix reduceInternal(final int qubitNo, final int qubitValue) throws CalculationException {
		final long			mask = toBitMask(qubitNo), val = qubitValue == 0 ? 0 : mask;
		final double[][]	temp = content; 
		final double[][]	result = new double[(int) getWidth()/2][(int) getHeight()/2];

		reduceInternal(totalPiece(this), temp, result, mask, val, 0);
		return new DenseInMemoryGateMatrix(result.length, result[0].length, isParallelMode(), result);
	}

	@Override
	protected void forEachInternal(final Piece piece, final ForEachCallback callback) throws CalculationException {
		final double[][]	temp = content;
		final int	xFrom = (int) piece.x(), xTo = (int) (piece.x() + piece.width());
		
		for(int y = (int)piece.y(), maxY = (int)(piece.y() + piece.height()); y < maxY; y++) {
			final double[] line = temp[y];
			
			for(int x = xFrom; x < xTo; x++) {
				callback.process(x, y, line[2*x], line[2*x+1]);
			}
		}
	}

	@Override
	protected GateMatrix multiplyInternalP(final GateMatrix another) throws CalculationException {
		final double[][]	left = content;
		final double[][]	result = new double[(int) getHeight()][(int) (2 * another.getWidth())];
		
		if (another instanceof DenseInMemoryGateMatrix) {
			final DenseInMemoryGateMatrix	mat = (DenseInMemoryGateMatrix)another;
			final double[][]	right = mat.content;

			ForkJoinPool.commonPool().invoke(new MultiplyDenseTask(left, right, result, parallelSplit(totalPiece(this))));
			return new DenseInMemoryGateMatrix(getWidth(), mat.getHeight(), isParallelMode(), result);
		}
// TODO Auto-generated method stub
//		else if (another instanceof SparseInMemoryGateMatrix) {
//			
//		}
//		else if (another instanceof InMemoryCommutationalMatrix) {
//			
//		}
		else {
			ForkJoinPool.commonPool().invoke(new MultiplyCommonTask(left, another, result, parallelSplit(totalPiece(this))));
			return new DenseInMemoryGateMatrix(getWidth(), another.getHeight(), isParallelMode(), result);
		}
	}

	@Override
	protected GateMatrix multiplyAndTransposeInternalP(GateMatrix another) throws CalculationException {
		final double[][]	left = content;
		final double[][]	result = new double[(int) another.getWidth()][(int) (2 * getHeight())];
		
		if (another instanceof DenseInMemoryGateMatrix) {
			final DenseInMemoryGateMatrix	mat = (DenseInMemoryGateMatrix)another;
			final double[][]	right = mat.content;
			
			ForkJoinPool.commonPool().invoke(new MultiplyAndTransposeDenseTask(left, right, result, parallelSplit(totalPiece(this))));
			return new DenseInMemoryGateMatrix(getWidth(), mat.getHeight(), isParallelMode(), result);
		}
// TODO Auto-generated method stub
//		else if (another instanceof SparseInMemoryGateMatrix) {
//			
//		}
//		else if (another instanceof InMemoryCommutationalMatrix) {
//			
//		}
		else {
			ForkJoinPool.commonPool().invoke(new MultiplyAndTransposeCommonTask(left, another, result, parallelSplit(totalPiece(this))));
			return new DenseInMemoryGateMatrix(another.getHeight(), getWidth(), isParallelMode(), result);
		}
	}

	@Override
	protected GateMatrix transposeInternalP() throws CalculationException {
		final double[][]	result = new double[(int) getWidth()][(int) getHeight()];
		
		ForkJoinPool.commonPool().invoke(new TransposeTask(content, result, parallelSplit(totalPiece(this))));
		return new DenseInMemoryGateMatrix(getWidth(), getHeight(), isParallelMode(), result);
	}

	@Override
	protected GateMatrix reduceInternalP(int qubitNo, int qubitValue) throws CalculationException {
		final long			mask = toBitMask(qubitNo), val = qubitValue == 0 ? 0 : mask;
		final double[][]	temp = content; 
		final double[][]	result = new double[(int) getWidth()/2][(int) getHeight()/2];
		final Piece[]		pieces = parallelSplit(totalPiece(this));
		final int[]			amount2Process = new int[pieces.length];
		
		for(int index = 0; index < amount2Process.length; index++) {
			amount2Process[index] = calculateNumberOfRedicedLines(pieces[index], mask, val);
		}
		ForkJoinPool.commonPool().invoke(new ReduceTask(temp, result, mask, val, amount2Process, 0, pieces));
		return new DenseInMemoryGateMatrix(result.length, result[0].length, isParallelMode(), result);
	}

	@Override
	protected void forEachInternalP(final Piece piece, final ForEachCallback callback) throws CalculationException {
		ForkJoinPool.commonPool().invoke(new ForEachTask(callback, parallelSplit(piece)));
	}

	public static void multiplyInternalDense(final double[][] left, final double[][] right, final double[][] target, final Piece piece) throws CalculationException {
		final int 		xFrom = (int) piece.x(), xTo = (int) (piece.x() + piece.width());
		final double[]	sum = new double[2];
			
		for(int y = (int) piece.y(), maxY = (int) (piece.y() + piece.height()); y < maxY; y++) {
			final double[]	leftLine = left[y];

			for(int x = xFrom; x < xTo; x++) {
				sum[0] = sum[1] = 0;

				for(int index = 0; index < leftLine.length/2; index++) {
					calcAndAdd(leftLine[2*index], leftLine[2*index+1], ComplexOp.MULTIPLY, right[2*index][x], right[2*index+1][x], sum, 0);
				}
				target[y][2*x] = sum[0];
				target[y][2*x+1] = sum[1];
			}
		}
	}	

	public static void multiplyInternalCommon(final double[][] left, final GateMatrix another, final double[][] target, final Piece piece) throws CalculationException {
		final int 		xFrom = (int) piece.x(), xTo = (int) (piece.x() + piece.width());
		final double[]	sum = new double[2];
			
		for(int y = (int) piece.y(), maxY = (int) (piece.y() + piece.height()); y < maxY; y++) {
			final double[]	leftLine = left[y];

			for(int x = xFrom; x < xTo; x++) {
				sum[0] = sum[1] = 0;

				try {
					another.upload(Piece.of(0, x, leftLine.length, 1), new DataOutputAdapter() {
						int	index = 0;
						double	real, image;
						
						@Override
						public void writeDouble(double v) throws IOException {
							if ((index & 0x01) == 0) {
								real = v;
							}
							else {
								image = v;
								calcAndAdd(leftLine[2*index], leftLine[2*index+1], ComplexOp.MULTIPLY, real, image, sum, 0);
								index++;
							}
						}
					});
				} catch (IOException e) {
					throw new CalculationException(e.getLocalizedMessage(), e);
				}
				target[y][2*x] = sum[0];
				target[y][2*x+1] = sum[1];
			}
		}
	}	

	public static void multiplyAndTransposeInternalDense(final double[][] left, final double[][] right, final double[][] target, final Piece piece) throws CalculationException {
		final int 		xFrom = (int) piece.x(), xTo = (int) (piece.x() + piece.width());
		final double[]	sum = new double[2];
			
		for(int y = (int) piece.y(), maxY = (int) (piece.y() + piece.height()); y < maxY; y++) {
			final double[]	leftLine = left[y];

			for(int x = xFrom; x < xTo; x++) {
				sum[0] = sum[1] = 0;

				for(int index = 0; index < leftLine.length/2; index++) {
					calcAndAdd(leftLine[2*index], leftLine[2*index+1], ComplexOp.MULTIPLY, right[2*index][x], right[2*index+1][x], sum, 0);
				}
				target[y][2*x] = sum[0];
				target[y][2*x+1] = sum[1];
			}
		}
	}	

	public static void multiplyAndTransposeInternalCommon(final double[][] left, final GateMatrix another, final double[][] target, final Piece piece) throws CalculationException {
		final int 		xFrom = (int) piece.x(), xTo = (int) (piece.x() + piece.width());
		final double[]	sum = new double[2];
			
		for(int y = (int) piece.y(), maxY = (int) (piece.y() + piece.height()); y < maxY; y++) {
			final double[]	leftLine = left[y];

			for(int x = xFrom; x < xTo; x++) {
				sum[0] = sum[1] = 0;

				try {
					another.upload(Piece.of(0, x, leftLine.length, 1), new DataOutputAdapter() {
						int	index = 0;
						double	real, image;
						
						@Override
						public void writeDouble(double v) throws IOException {
							if ((index & 0x01) == 0) {
								real = v;
							}
							else {
								image = v;
								calcAndAdd(leftLine[2*index], leftLine[2*index+1], ComplexOp.MULTIPLY, real, image, sum, 0);
								index++;
							}
						}
					});
				} catch (IOException e) {
					throw new CalculationException(e.getLocalizedMessage(), e);
				}
				target[y][2*x] = sum[0];
				target[y][2*x+1] = sum[1];
			}
		}
	}	
	
	private static void transposeInternal(final Piece piece, final double[][] source, final double[][] target) throws CalculationException {
		final int	xFrom = (int) piece.x(), xTo = (int) (piece.x() + piece.width());
		
		for (int y = (int) piece.y(), maxY = (int) (piece.y() + piece.height()); y < maxY; y++) {
			final double[]	line = source[y];
			
			for (int x = xFrom; x < xTo; x++) {
				target[x][2*y] = line[2*x];
				target[x][2*y+1] = line[2*x+1];
			}
		}
	}

	private static int calculateNumberOfRedicedLines(final Piece piece, final long mask, final long val) throws CalculationException {
		int	result = 0;
		
		for (int y = (int)piece.y(), maxY = (int)piece.height(), yTo = 0; y < maxY; y++) {
			if ((y & mask) == val) {
				result++;
			}
		}
		return result;
	}
	
	private static void reduceInternal(final Piece piece, final double[][] source, final double[][] target, final long mask, final long val, final int yFrom) throws CalculationException {
		final int	xStart = (int) piece.x(), xEnd = (int) (piece.x() + piece.width());
		
		for (int y = (int)piece.y(), maxY = (int)piece.height(), yTo = yFrom; y < maxY; y++) {
			if ((y & mask) == val) {
				final double[]	from = source[y];
				final double[]	to = source[yTo];
				
				for (int x = xStart, xTo = 0; x < xEnd; x++) {
					if ((x & mask) == val) {
						to[2*xTo] = from[2*x]; 
						to[2*xTo+1] = from[2*x+1];
						xTo++;
					}
				}
				yTo++;
			}
		}
	}

	private class MultiplyDenseTask extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		
		private final double[][]	left;
		private final double[][]	right;
		private final double[][]	target;
		private final Piece[]	pieces;
		
		private MultiplyDenseTask(final double[][] left, final double[][] right, final double[][] target, final Piece... pieces) {
			this.left = left;
			this.right = right;
			this.target = target;
			this.pieces = pieces;
		}

		@Override
		protected void compute() {
			if (pieces.length == 1) {
				try {
					multiplyAndTransposeInternalDense(left, right, target, pieces[0]);
				} catch (CalculationException e) {
					e.printStackTrace();
				}
			}
			else {
				final MultiplyDenseTask[]	tasks = new MultiplyDenseTask[pieces.length];
				
				for (int index = 0; index < tasks.length; index++) {
					tasks[index] = new MultiplyDenseTask(left, right, target, pieces[index]);
					tasks[index].fork();
				}
				for (MultiplyDenseTask item : tasks) {
					item.join();
				}
			}
		}
	}

	private class MultiplyCommonTask extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		
		private final double[][]	left;
		private final GateMatrix	another;
		private final double[][]	target;
		private final Piece[]	pieces;
		
		private MultiplyCommonTask(final double[][] left, final GateMatrix another, final double[][] target, final Piece... pieces) {
			this.left = left;
			this.another = another;
			this.target = target;
			this.pieces = pieces;
		}

		@Override
		protected void compute() {
			if (pieces.length == 1) {
				try {
					multiplyAndTransposeInternalCommon(left, another, target, pieces[0]);
				} catch (CalculationException e) {
					e.printStackTrace();
				}
			}
			else {
				final MultiplyCommonTask[]	tasks = new MultiplyCommonTask[pieces.length];
				
				for (int index = 0; index < tasks.length; index++) {
					tasks[index] = new MultiplyCommonTask(left, another, target, pieces[index]);
					tasks[index].fork();
				}
				for (MultiplyCommonTask item : tasks) {
					item.join();
				}
			}
		}
	}

	private class MultiplyAndTransposeDenseTask extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		
		private final double[][]	left;
		private final double[][]	right;
		private final double[][]	target;
		private final Piece[]	pieces;
		
		private MultiplyAndTransposeDenseTask(final double[][] left, final double[][] right, final double[][] target, final Piece... pieces) {
			this.left = left;
			this.right = right;
			this.target = target;
			this.pieces = pieces;
		}

		@Override
		protected void compute() {
			if (pieces.length == 1) {
				try {
					multiplyInternalDense(left, right, target, pieces[0]);
				} catch (CalculationException e) {
					e.printStackTrace();
				}
			}
			else {
				final MultiplyDenseTask[]	tasks = new MultiplyDenseTask[pieces.length];
				
				for (int index = 0; index < tasks.length; index++) {
					tasks[index] = new MultiplyDenseTask(left, right, target, pieces[index]);
					tasks[index].fork();
				}
				for (MultiplyDenseTask item : tasks) {
					item.join();
				}
			}
		}
	}

	private class MultiplyAndTransposeCommonTask extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		
		private final double[][]	left;
		private final GateMatrix	another;
		private final double[][]	target;
		private final Piece[]	pieces;
		
		private MultiplyAndTransposeCommonTask(final double[][] left, final GateMatrix another, final double[][] target, final Piece... pieces) {
			this.left = left;
			this.another = another;
			this.target = target;
			this.pieces = pieces;
		}

		@Override
		protected void compute() {
			if (pieces.length == 1) {
				try {
					multiplyInternalCommon(left, another, target, pieces[0]);
				} catch (CalculationException e) {
					e.printStackTrace();
				}
			}
			else {
				final MultiplyCommonTask[]	tasks = new MultiplyCommonTask[pieces.length];
				
				for (int index = 0; index < tasks.length; index++) {
					tasks[index] = new MultiplyCommonTask(left, another, target, pieces[index]);
					tasks[index].fork();
				}
				for (MultiplyCommonTask item : tasks) {
					item.join();
				}
			}
		}
	}
	
	private class TransposeTask extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		
		private final double[][]	source;
		private final double[][]	target;
		private final Piece[]	pieces;
		
		private TransposeTask(final double[][] source, final double[][] target, final Piece... pieces) {
			this.source = source;
			this.target = target;
			this.pieces = pieces;
		}

		@Override
		protected void compute() {
			if (pieces.length == 1) {
				try {
					transposeInternal(pieces[0], source, target);
				} catch (CalculationException e) {
					e.printStackTrace();
				}
			}
			else {
				final TransposeTask[]	tasks = new TransposeTask[pieces.length];
				
				for (int index = 0; index < tasks.length; index++) {
					tasks[index] = new TransposeTask(source, target, pieces[index]);
					tasks[index].fork();
				}
				for (TransposeTask item : tasks) {
					item.join();
				}
			}
		}
	}
	
	private class ReduceTask extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		
		private final double[][]	source;
		private final double[][]	target;
		private final long 		mask;
		private final long 		val;
		private final int[]		taskSizes;
		private final int 		from;
		private final Piece[]	pieces;
		
		private ReduceTask(final double[][] source, final double[][] target, final long mask, final long val, final int[] taskSizes, final int from, final Piece... pieces) {
			this.source = source;
			this.target = target;
			this.mask = mask;
			this.val = val;
			this.taskSizes = taskSizes;
			this.from = from;
			this.pieces = pieces;
		}

		@Override
		protected void compute() {
			if (pieces.length == 1) {
				try {
					reduceInternal(pieces[0], source, target, mask, val, from);
				} catch (CalculationException e) {
					e.printStackTrace();
				}
			}
			else {
				final ReduceTask[]	tasks = new ReduceTask[pieces.length];
				
				for (int index = 0, current = 0; index < tasks.length; current += taskSizes[index], index++) {
					tasks[index] = new ReduceTask(source, target, mask, val, taskSizes, current, pieces[index]);
					tasks[index].fork();
					
				}
				for (ReduceTask item : tasks) {
					item.join();
				}
			}
		}
	}
	
	
	private class ForEachTask extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		
		private final ForEachCallback	callback;
		private final Piece[]	pieces;
		
		private ForEachTask(final ForEachCallback callback, final Piece... pieces) {
			this.callback = callback;
			this.pieces = pieces;
		}

		@Override
		protected void compute() {
			if (pieces.length == 1) {
				try {
					forEachInternal(pieces[0], callback);
				} catch (CalculationException e) {
					e.printStackTrace();
				}
			}
			else {
				final ForEachTask[]	tasks = new ForEachTask[pieces.length];
				
				for (int index = 0; index < tasks.length; index++) {
					tasks[index] = new ForEachTask(callback, pieces[index]);
					tasks[index].fork();
				}
				for (ForEachTask item : tasks) {
					item.join();
				}
			}
		}
	}
}
