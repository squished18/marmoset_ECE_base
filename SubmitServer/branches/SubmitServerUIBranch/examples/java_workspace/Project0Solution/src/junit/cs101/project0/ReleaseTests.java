package cs101.project0;

import junit.framework.TestCase;

/**
 * Add Release Tests to this class. Release tests are written by the Instructor
 * and may be used for grading projects. This source code is NOT made available
 * to students; instead they must consume a Release token to discover the
 * performance of their code against these tests.
 */
public class ReleaseTests extends TestCase {

	Point p0, p1, p2;

	double testX, testY;

	/**
	 * Optionally set up any member variables. This method is called before each
	 * test is run.
	 */
	protected void setUp() throws Exception {
		p0 = new Point();
		p1 = new Point(-9, 4);

		testX = 56.46;
		testY = .24;
		p2 = new Point(testX, testY);
	}

	// Test Cases

	// 1.
	public void testToStringUsesCorrectFormat() {
		assertEquals(p0.toString(), "(0.0,0.0)");
		assertEquals(p1.toString(), "(-9.0,4.0)");
		assertEquals(p2.toString(), "(56.5,0.2)");
	}
	// 2.
	public void testTwoParmConstructor() {
		assertTrue(p2.getX() == testX);
		assertTrue(p2.getY() == testY);
	}
	// 3.
	public void testTranslate() {
		p0.translate(testX, testY);
		assertTrue(p0.getX() == testX && p0.getY() == testY);

		p2.setLocation(p1);
		p2.translate(testX, testY);
		assertTrue(p2.getX() == p1.getX() + testX);
		assertTrue(p2.getY() == p1.getY() + testY);
	}

	/**
	 * Supporting function for calculating distance
	 */
	private double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
	}
	// 4.
	public void testDistance() {
		double tmp = distance(p0.getX(), p0.getY(), p1.getX(), p1.getY());
		assertTrue(p0.distance(p1) == tmp);

		tmp = distance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		assertTrue(p1.distance(p2) == tmp);
	}

	// 5.
	public void testDistanceToSelfIsZero() {
		assertTrue(p0.distance(p0) == 0.0);
		assertTrue(p1.distance(p1) == 0.0);
		assertTrue(p2.distance(p2) == 0.0);
	}
	// 6.
	public void testDistanceIsCommutative() {
		assertTrue(p0.distance(p1) == p1.distance(p0));
		assertTrue(p1.distance(p2) == p2.distance(p1));
	}
	// 7.
	public void testDistanceDoesNotAlterOperands() {
		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();		
		p1.distance(p2);
		
		assertTrue(p1.getX() == x1 && p1.getY() == y1);
		assertTrue(p2.getX() == x2 && p2.getY() == y2);
	}
	// 8.
	public void testFoo() {
		Point tmp = PointUtilities.foo(p0, p1);
		p0.foo(p1);
		assertTrue(p0.getX() == tmp.getX() && p0.getY() == tmp.getY());
		
		tmp = PointUtilities.foo(p1, p2);
		p1.foo(p2);
		assertTrue(p1.getX() == tmp.getX() && p1.getY() == tmp.getY());
	}
	// 9.
	public void testBar() {
		Point tmp = PointUtilities.bar(p0, p1);
		p0.bar(p1);
		assertTrue(p0.getX() == tmp.getX() && p0.getY() == tmp.getY());
		
		tmp = PointUtilities.bar(p1, p2);
		p1.bar(p2);
		assertTrue(p1.getX() == tmp.getX() && p1.getY() == tmp.getY());
	}
	// 10.
	public void testFooAndBarDoNotAlterParameter() {
		Point tmp = new Point();
		tmp.setLocation(p1);
		p2.foo(p1);
		assertTrue(p1.getX() == tmp.getX() && p1.getY() == tmp.getY());
		
		p2.bar(p1);
		assertTrue(p1.getX() == tmp.getX() && p1.getY() == tmp.getY());		
	}

}
