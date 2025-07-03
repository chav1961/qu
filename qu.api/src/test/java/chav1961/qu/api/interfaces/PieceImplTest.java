package chav1961.qu.api.interfaces;

import org.junit.Assert;
import org.junit.Test;

public class PieceImplTest {
	@Test
	public void basicTest() {
		final Piece	p = Piece.of(0, 1, 2, 3);
		
		Assert.assertEquals(0, p.x());
		Assert.assertEquals(1, p.y());
		Assert.assertEquals(2, p.width());
		Assert.assertEquals(3, p.height());
		
		try{Piece.of(-1, 1, 2, 3);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{Piece.of(0, -1, 2, 3);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{Piece.of(0, 1, 0, 3);
			Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{Piece.of(0, 1, 2, 0);
			Assert.fail("Mandatory exception was not detected (4-th argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertTrue(p.inside(1, 1));
		Assert.assertFalse(p.inside(10, 10));
		
		Assert.assertEquals(Piece.Relation.EQUALS, p.related(Piece.of(0, 1, 2, 3)));
		Assert.assertEquals(Piece.Relation.INSIDE, p.related(Piece.of(0, 1, 2, 2)));
		Assert.assertEquals(Piece.Relation.OVERLAPS, p.related(Piece.of(0, 1, 2, 4)));
		Assert.assertEquals(Piece.Relation.OUTSIDE, p.related(Piece.of(10, 1, 2, 3)));

		try{p.related(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

}
