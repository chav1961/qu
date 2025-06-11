package chav1961.qu.util;

import chav1961.purelib.basic.exceptions.CalculationException;

abstract class AbstractInMemoryGateMatrix extends AbstractGateMatrix {
	AbstractInMemoryGateMatrix(final MatrixType type, final long width, final long height, final boolean parallelModeOn) {
		super(type, width, height, parallelModeOn);
	}

	@Override
	public void close() throws CalculationException {
		// TODO Auto-generated method stub
		
	}

	protected static void calcAndAdd(final double leftReal, final double leftImage, final ComplexOp op, final double rightReal, final double rightImage, final double[] result, final int resultIndex) {
		switch (op) {
			case ADD		:
				result[resultIndex] += leftReal + rightReal;
				result[resultIndex + 1] += leftImage + rightImage;
				break;
			case SUBTRACT	:
				result[resultIndex] += leftReal - rightReal;
				result[resultIndex + 1] += leftImage - rightImage;
				break;
			case MULTIPLY	:
				result[resultIndex] += leftReal * rightReal - leftImage * rightImage;
				result[resultIndex + 1] += leftImage * rightReal + leftReal * rightImage;
				break;
			case DIVIDE		:
				final double	square = 1/(rightReal * rightReal + rightImage * rightImage); 

				result[resultIndex] += square * (leftReal * rightReal + leftImage * rightImage);
				result[resultIndex + 1] += square * (leftReal * rightImage - leftImage * rightReal);
				break;
			default:
				throw new UnsupportedOperationException("Calc operation ["+op+"] is not supported yet"); 
		}
	}
}
