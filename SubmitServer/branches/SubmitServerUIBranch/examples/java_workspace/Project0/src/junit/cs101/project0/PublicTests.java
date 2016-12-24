package cs101.project0;

import junit.framework.TestCase;

/**
 * Add Public Tests to this class. Public tests are written by the Instructor
 * and may be used for grading projects. This source code is made available to
 * students.
 */
public class PublicTests extends TestCase {

	Point p0, p1, p2;

	double testX, testY;

	/**
	 * Optionally set up any member variables. This method is called before each
	 * test is run.
	 */
	protected void setUp() throws Exception {
		p0 = new Point();
		p1 = new Point(5, -7);

		testX = 23.46;
		testY = .32;
		p2 = new Point(testX, testY);
	}

	// Test Cases

	// 1.
	public void testDefaultConstructorUsesZero() {
		assertTrue(p0.getX() == 0);
		assertTrue(p0.getX() == p0.getY());
	}
	// 2.
	public void testSetLocation() {
		p0.setLocation(p1);
		assertTrue(p0.getX() == p1.getX() && p0.getY() == p1.getY());
	}
	// 3.
	public void testSetLocationDoesNotAlterParameter() {
		double tmpX = p1.getX();
		double tmpY = p1.getY();
		p0.setLocation(p1);		
		assertTrue(p1.getX() == tmpX && p1.getY() == tmpY);
		assertTrue(p0.getX() == p1.getX() && p0.getY() == p1.getY());
	}
	// 4.
	public void testMove() {
		p0.move(testX, testY);
		assertTrue(p0.getX() == testX && p0.getY() == testY);
	}
}
