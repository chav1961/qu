package chav1961.qu.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.qu.api.interfaces.GateMatrix;

public class DenseInMemoryGateVector extends AbstractInMemoryGateMatrix {
	private final double[]	content;
	private final boolean	isRow;
	
	DenseInMemoryGateVector(final long width, final long height, final boolean parallelMode) {
		this(width, height, parallelMode, new double[(int) (2*height*width)]);
	}

	private DenseInMemoryGateVector(final long width, final long height, final boolean parallelMode, final double[] content) {
		super(MatrixType.DENSE_MATRIX, width, height, parallelMode);
		this.content = new double[(int) (2*height*width)];
		this.isRow = height == 1;
	}
	
	@Override
	protected void downloadInternal(final Piece piece, final DataInput in, final ForEachCallback callback) throws IOException {
		final double[]	temp = content;
		final int		start = (int) (isRow ? piece.x() : piece.y());
		final int		count = (int) (isRow ? piece.width() : piece.height());
		
		for(int index = 0; index <  count; index++) {
			final double	real = in.readDouble();
			final double	image = in.readDouble();
			
			try {
				if (callback.process(isRow ? 0 : index, isRow ? index : 0, start, count)) {
					temp[2*(start + index)] = real;
					temp[2*(start + index) + 1] = image;
				}
			} catch (CalculationException e) {
				throw new IOException(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	protected void uploadInternal(final Piece piece, final DataOutput out, final ForEachCallback callback) throws IOException {
		final double[]	temp = content;
		final int		start = (int) (isRow ? piece.x() : piece.y());
		final int		count = (int) (isRow ? piece.width() : piece.height());
		
		for(int index = 0; index <  count; index++) {
			final double	real = temp[2*(start + index)];
			final double	image = temp[2*(start + index) + 1];

			try {
				if (callback.process(isRow ? 0 : index, isRow ? index : 0, start, count)) {
					out.writeDouble(real);
					out.writeDouble(image);
				}
			} catch (CalculationException e) {
				throw new IOException(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	protected GateMatrix multiplyInternal(final GateMatrix another) throws CalculationException {
		return new DenseInMemoryGateVector(getHeight(), getWidth(), isParallelMode(), multiplyInternal0(another));
	}

	@Override
	protected GateMatrix multiplyInternalP(GateMatrix another) throws CalculationException {
		return new DenseInMemoryGateVector(getHeight(), getWidth(), isParallelMode(), multiplyInternalP0(another));
	}

	@Override
	protected GateMatrix multiplyAndTransposeInternal(GateMatrix another) throws CalculationException {
		return new DenseInMemoryGateVector(getWidth(), getHeight(), isParallelMode(), multiplyInternal0(another));
	}

	@Override
	protected GateMatrix multiplyAndTransposeInternalP(GateMatrix another) throws CalculationException {
		return new DenseInMemoryGateVector(getWidth(), getHeight(), isParallelMode(), multiplyInternalP0(another));
	}

	@Override
	protected GateMatrix transposeInternal() throws CalculationException {
		return new DenseInMemoryGateVector(getHeight(), getWidth(), isParallelMode(), content.clone());
	}

	@Override
	protected GateMatrix transposeInternalP() throws CalculationException {
		return transposeInternal();
	}
	
	@Override
	protected GateMatrix reduceInternal(final int qubitNo, final int qubitValue) throws CalculationException {
		final double[]	source = content;
		final double[]	target = new double[source.length >> 1];
		final int		step = (int) toBitMask(qubitNo);
		final int		value = qubitValue == 0 ? 0 : step;
		
		for(int index = value, maxIndex = source.length >> 1, where = 0; index < maxIndex; index += step) {
			if ((index & step) == value) {
				target[where++] = source[2*index];
				target[where++] = source[2*index + 1];
			}
		}
		return new DenseInMemoryGateVector(getWidth(), getHeight(), isParallelMode(), target);
	}
	
	@Override
	protected GateMatrix reduceInternalP(final int qubitNo, final int qubitValue) throws CalculationException {
		return reduceInternal(qubitNo, qubitValue);
	}

	@Override
	protected void forEachInternal(final Piece piece, final ForEachCallback callback) throws CalculationException {
		final double[]	temp = content;
		
		if (piece.width() == 1) {
			final int	start = (int) piece.y();
			final int	count = (int) piece.height();
			
			for(int index = 0; index <  count; index++) {
				callback.process(0, index, temp[2*(start + index)], temp[2*(start + index) + 1]);
			}
		}
		else {
			final int	start = (int) piece.x();
			final int	count = (int) piece.width();
			
			for(int index = 0; index <  count; index++) {
				callback.process(index, 0, temp[2*(start + index)], temp[2*(start + index) + 1]);
			}
		}
	}
	
	@Override
	protected void forEachInternalP(final Piece piece, final ForEachCallback callback) throws CalculationException {
		final List<Callable<Void>>	list = new ArrayList<>();
		
		for(Piece part : parallelSplit(totalPiece(this))) {
			final Piece	current = part;
			list.add(()->{
				forEachInternal(current, callback);
				return null;
			});
		}
		try {
			for(Future<Void> item : ForkJoinPool.commonPool().invokeAll(list)) {
				item.get();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CalculationException(e);
		} catch (ExecutionException e) {
			throw new CalculationException(e.getCause());
		}
	}
	
	private double[] multiplyInternal0(GateMatrix another) throws CalculationException {
		final double[]	source = content;
		final double[]	target = new double[(int) another.getWidth()];
		final boolean	oldFastMode = another.getType().isFastModeSupported() ? another.setFastMode(true) : false;
			
		another.forEach((x,y,real,image)->{
			calcAndAdd(source[(int) (2*x)], source[(int) (2*x+1)], ComplexOp.MULTIPLY, real, image, target, (int)y);
			return true;
		});
		another.setFastMode(oldFastMode);
		return target;
	}

	private double[] multiplyInternalP0(GateMatrix another) throws CalculationException {
		final double[]	source = content;
		final double[]	target = new double[(int) another.getWidth()];
		final boolean	oldFastMode = another.getType().isFastModeSupported() ? another.setFastMode(true) : false;
		final List<Callable<Void>>	list = new ArrayList<>();
		
		for(Piece part : parallelSplit(totalPiece(this))) {
			final Piece	current = part;
			list.add(()->{
				multiplyInternal(another, current, source, target);
				return null;
			});
		}
		try {
			for(Future<Void> item : ForkJoinPool.commonPool().invokeAll(list)) {
				item.get();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CalculationException(e);
		} catch (ExecutionException e) {
			throw new CalculationException(e.getCause());
		}
		another.setFastMode(oldFastMode);
		return target;
	}

	private void multiplyInternal(final GateMatrix another, final Piece piece, final double[] source, final double[] target) throws CalculationException {
		another.forEach(piece, (x,y,real,image)->{
			calcAndAdd(source[(int) (2*x)], source[(int) (2*x+1)], ComplexOp.MULTIPLY, real, image, target, (int)y);
			return true;
		});
	}
}
