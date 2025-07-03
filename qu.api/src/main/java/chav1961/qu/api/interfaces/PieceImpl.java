package chav1961.qu.api.interfaces;


class PieceImpl implements Piece {
	private final long	x;
	private final long	y;
	private final long	width;
	private final long	height;
	
	PieceImpl(final long x, final long y, final long width, final long height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public long x() {
		return x;
	}

	@Override
	public long y() {
		return y;
	}

	@Override
	public long width() {
		return width;
	}

	@Override
	public long height() {
		return height;
	}

	@Override
	public boolean inside(final long x, final long y) {
		return 	x >= x() 
				&& x < x()+width() 
				&& y >= y() 
				&& y < y()+height();
	}

	@Override
	public Relation related(final Piece another) {
		if (another == null) {
			throw new NullPointerException("Another piece to test can't be null"); 
		}
		else if (equals(another)) {
			return Relation.EQUALS;
		}
		else {
			final boolean ulInside = inside(another.x(),another.y());
			final boolean urInside = inside(another.x()+another.width()-1,another.y());
			final boolean dlInside = inside(another.x(),another.y()+another.height()-1);
			final boolean drInside = inside(another.x()+another.width()-1,another.y()+another.height()-1);

			if (ulInside && drInside) {
				return Relation.INSIDE;
			}
			else if (ulInside || urInside || dlInside || drInside) {
				return Relation.OVERLAPS;
			}
			else {
				return Relation.OUTSIDE;
			}
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (height ^ (height >>> 32));
		result = prime * result + (int) (width ^ (width >>> 32));
		result = prime * result + (int) (x ^ (x >>> 32));
		result = prime * result + (int) (y ^ (y >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PieceImpl other = (PieceImpl) obj;
		if (height != other.height()) return false;
		if (width != other.width()) return false;
		if (x != other.x()) return false;
		if (y != other.y()) return false;
		return true;
	}

	@Override
	public String toString() {
		return "Piece[x="+x()+",y="+y()+",width="+width()+",height="+height()+"]";
	}
}
