package cs101.project0;

import java.text.DecimalFormat;

/**
 * A simple class to represent a point on the XY-plane
 */
public class Point {

	private double x, y;

	private final DecimalFormat DF = new DecimalFormat("0.0");

	/**
	 * Create point (0,0)
	 */
	public Point() {
		this(0, 0);
	}

	/**
	 * Create point (x,y)
	 */
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x-coordinate of this point
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y coordinate of this point
	 */
	public double getY() {
		return y;
	}

	/**
	 * sets the location of the point to the specified location
	 */
	public void setLocation(Point p) {
		x = p.x;
		y = p.y;
	}

	/**
	 * set this point to (x,y)
	 */
	public void move(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Return a string representation of this point in the format: "(x,y)" where
	 * x and y have exactly 1 digit after the decimal point and at least one
	 * digit before it. There should not be any spaces or other number
	 * formatting.
	 */
	public String toString() {
		return "(" + DF.format(x) + "," + DF.format(y) + ")";
	}

	/**
	 * translate this point by the specified deltas to location: (x+dx, y+dy)
	 */
	public void translate(double dx, double dy) {
		x += dx;
		y += dy;
	}

	/**
	 * find the distance (not displacement) between this point and the specified
	 * point
	 */
	public double distance(Point p) {
		return Math.sqrt(Math.pow((p.x - x), 2) + Math.pow((p.y - y), 2));
	}

	/**
	 * perform the PointUtilities.foo operation on this point and the specified
	 * point, and move this point to the resulting point
	 */
	public void foo(Point p) {
		Point tmp = PointUtilities.foo(this, p);
		x = tmp.x;
		y = tmp.y;
	}

	/**
	 * perform the PointUtilities.bar operation on this point and the specified
	 * point, and move this point to the resulting point
	 */
	public void bar(Point p) {
		Point tmp = PointUtilities.bar(this, p);
		x = tmp.x;
		y = tmp.y;
	}
}
