package edu.umd.cs.buildServer;

import junit.framework.TestCase;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Unknown;

import edu.umd.cs.marmoset.annotations.MaxTestTime;

public class TimeExtractorTest extends TestCase {
	@MaxTestTime(20)
	public void testTimeExtractor() throws Exception {
		JUnitTestCase.TimeExtractor extractor = new JUnitTestCase.TimeExtractor();
		JavaClass jClass = Repository.lookupClass("edu.umd.cs.buildServer.TimeExtractorTest");
		for(Method m : jClass.getMethods())  if (m.getName().equals("testTimeExtractor")){
			for(Attribute attribute : m.getAttributes()) {

				if (attribute instanceof Unknown) {
					extractor.setupVisitorForClass(jClass);
					attribute.accept(extractor);
					assertEquals(20, extractor.seconds());
					return;
				}
			}
					
			
		}
		fail();
	}
}

