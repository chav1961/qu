package chav1961.qu.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.qu.api.interfaces.GateMatrix;
import chav1961.qu.api.interfaces.GateMatrixType;
import chav1961.qu.api.interfaces.Piece;

public class CommutationalInMemoryMatrixTest {

	@Test
	public void basicTest() throws CalculationException {
		try(final CommutationalInMemoryMatrix	cimm = new CommutationalInMemoryMatrix(3, 3, true)) {
			Assert.assertEquals(3, cimm.getHeight());
			Assert.assertEquals(3, cimm.getWidth());
			Assert.assertEquals(GateMatrixType.COMMITATION_MATRIX, cimm.getType());
			Assert.assertEquals(int.class, cimm.getValueClass());
		}
		
		try {new CommutationalInMemoryMatrix(0, 1, true).close();
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try {new CommutationalInMemoryMatrix(1, 0, true).close();
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try {new CommutationalInMemoryMatrix(4, 3, true).close();
			Assert.fail("Mandatory exception was not detected (1-st and 2-nd argument are not equals)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	@Test
	public void serializationTest() throws CalculationException, IOException {
		final byte[]	content = new byte[] 
							{1,0,0,
							 0,1,0,
							 0,0,1
							};
		
		try(final CommutationalInMemoryMatrix	cimm = new CommutationalInMemoryMatrix(3, 3, true)) {
			try(final ByteArrayInputStream	bais = new ByteArrayInputStream(content);
				final DataInputStream		dis = new DataInputStream(bais)) {
				
				cimm.download(dis);
			}
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				try(final DataOutputStream	dos = new DataOutputStream(baos)) {
					cimm.upload(dos);
				}
				Assert.assertArrayEquals(content, baos.toByteArray());
			}
		}
		final byte[]	fastContent = new byte[] 
							{0,0,0,0,
							 0,0,0,1,
							 0,0,0,2,
							};

		try(final CommutationalInMemoryMatrix	cimm = new CommutationalInMemoryMatrix(3, 3, true)) {
			cimm.setFastMode(true);
			
			try(final ByteArrayInputStream	bais = new ByteArrayInputStream(fastContent);
				final DataInputStream		dis = new DataInputStream(bais)) {
				
				cimm.download(dis);

				try{cimm.download(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				
				try{cimm.download(null, dis);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{cimm.download(Piece.of(0,0,100,100), dis);
					Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try{cimm.download(Piece.of(cimm), null);
					Assert.fail("Mandatory exception was not detected (2-nd argument is null)");
				} catch (NullPointerException exc) {
				}
				
				try{cimm.download(null, dis, (x,y,r,i)->true);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{cimm.download(Piece.of(0,0,100,100), dis, (x,y,r,i)->true);
					Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try{cimm.download(Piece.of(cimm), null, (x,y,r,i)->true);
					Assert.fail("Mandatory exception was not detected (2-nd argument is null)");
				} catch (NullPointerException exc) {
				}
				try{cimm.download(Piece.of(cimm), dis, null);
					Assert.fail("Mandatory exception was not detected (3-rd argument is null)");
				} catch (NullPointerException exc) {
				}
			}
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				try(final DataOutputStream	dos = new DataOutputStream(baos)) {
					cimm.upload(dos);

					try{cimm.upload(null);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					
					try{cimm.upload(null, dos);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{cimm.upload(Piece.of(0,0,100,100), dos);
						Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
					} catch (IllegalArgumentException exc) {
					}
					try{cimm.upload(Piece.of(cimm), null);
						Assert.fail("Mandatory exception was not detected (2-nd argument is null)");
					} catch (NullPointerException exc) {
					}
					
					try{cimm.upload(null, dos, (x,y,r,i)->true);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (IllegalArgumentException exc) {
					}
					try{cimm.upload(Piece.of(0,0,100,100), dos, (x,y,r,i)->true);
						Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
					} catch (IllegalArgumentException exc) {
					}
					try{cimm.upload(Piece.of(cimm), null, (x,y,r,i)->true);
						Assert.fail("Mandatory exception was not detected (2-nd argument is null)");
					} catch (NullPointerException exc) {
					}
					try{cimm.upload(Piece.of(cimm), dos, null);
						Assert.fail("Mandatory exception was not detected (3-rd argument is null)");
					} catch (NullPointerException exc) {
					}
				}
				Assert.assertArrayEquals(fastContent, baos.toByteArray());
			}
		}
	}
	
	@Test
	public void calculationTest() throws CalculationException, IOException {
		try(final CommutationalInMemoryMatrix	cimm1 = new CommutationalInMemoryMatrix(3, 3, true);
			final CommutationalInMemoryMatrix	cimm2 = new CommutationalInMemoryMatrix(3, 3, true)) {
			
			cimm1.setFastMode(true);
			cimm1.download(new DataSource(0,1,2));
			cimm2.setFastMode(true);
			cimm2.download(new DataSource(0,1,2));
			
			try(final GateMatrix	cimm3 = cimm1.multiply(cimm2)) {
				Assert.assertEquals(3, cimm3.getWidth());
				Assert.assertEquals(3, cimm3.getHeight());
				Assert.assertEquals(GateMatrixType.COMMITATION_MATRIX, cimm3.getType());
				Assert.assertEquals(int.class, cimm3.getValueClass());
			}
		}
	}	
}
