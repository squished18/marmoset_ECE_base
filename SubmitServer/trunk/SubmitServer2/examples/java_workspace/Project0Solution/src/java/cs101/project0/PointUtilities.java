package cs101.project0;

/**
 * Static functions that operate on two points
 */
public class PointUtilities {

	/**
	 * Perform a mysterious foo operation on two Points and return a new Point
	 */
	public static Point foo(Point p1, Point p2) {
		return new Point(p1.getX() * 2 + p2.getX(), p1.getY() * p2.getY() / 3);
	}

	/**
	 * Perform a mysterious bar operation on two Points and return a new Point
	 */
	public static Point bar(Point p1, Point p2) {
		return new Point( Math.pow(p1.getX()*p2.getX(), 2), Math.abs(p1.getY()) + p2.getY() / 3);
	}
}
