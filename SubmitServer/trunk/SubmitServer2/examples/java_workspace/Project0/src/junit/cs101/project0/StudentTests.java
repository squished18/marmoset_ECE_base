package cs101.project0;

import junit.framework.TestCase;

/**
 * Student tests are written by the student and are reported by the Submit
 * Server. If code coverage is enabled, the code coverage stats of student tests
 * are also reported (i.e. how well the student tests cover the code in the
 * project packages).
 */
public class StudentTests extends TestCase {

	/**
	 * Optionally set up any member variables. This method is called before each
	 * test is run.
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Optionally close any open streams or connections. This method is called
	 * after each test is run.
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Each test case is "public void" and has a name that starts with "test"
	 */
	public void testSomething() {
		// a test case should pass unless an exception is thrown or an
		// "assertion" fails. For example, any of the following conditions will
		// fail this test:
		fail("This test has failed");
		assertTrue(5 == 7);
		assertFalse(5 == 5);
		assertEquals(5, 7);
	}

}
