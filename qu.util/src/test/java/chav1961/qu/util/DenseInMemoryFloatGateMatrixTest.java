package chav1961.qu.util;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.qu.api.interfaces.GateMatrixType;

public class DenseInMemoryFloatGateMatrixTest {
	@Test
	public void basicTest() throws CalculationException {
		try(final DenseInMemoryFloatGateMatrix	cimm = new DenseInMemoryFloatGateMatrix(3, 3, true)) {
			Assert.assertEquals(3, cimm.getHeight());
			Assert.assertEquals(3, cimm.getWidth());
			Assert.assertEquals(GateMatrixType.DENSE_MATRIX, cimm.getType());
			Assert.assertEquals(float.class, cimm.getValueClass());
		}
		
		try {new DenseInMemoryFloatGateMatrix(0, 1, true).close();
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try {new DenseInMemoryFloatGateMatrix(1, 0, true).close();
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try {new DenseInMemoryFloatGateMatrix(4, 3, true).close();
			Assert.fail("Mandatory exception was not detected (1-st and 2-nd argument are not equals)");
		} catch (IllegalArgumentException exc) {
		}
	}

}
