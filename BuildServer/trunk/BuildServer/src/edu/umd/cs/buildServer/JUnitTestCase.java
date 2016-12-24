/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Mar 9, 2005
 */
package edu.umd.cs.buildServer;

import java.util.Map;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.visitclass.AnnotationVisitor;

/**
 * Object identifying a single JUnit test in a class.
 * Provides enough information to execute the test using a TestRunner.s
 * 
 * @author David Hovemeyer
 * @author William Pugh
 */
public class JUnitTestCase {
	static class TimeExtractor extends AnnotationVisitor {
		private int time = 0;
		@Override
		public void visitAnnotation(String annotationClass,
				Map<String, Object> map, boolean runtimeVisible)
        {
            BuildServer.getBuildServerLog().trace("Looking for @MaxTestTime annotation in " +annotationClass);
            if (annotationClass.equals("edu.umd.cs.marmoset.annotations.MaxTestTime") && map.containsKey("value"))
				time = (Integer) map.get("value");
		
		}
		public int seconds() {
			return time;
		}
		
	}
	private final String className;
	private final String methodName;
	private final String signature;
	private final int maxTimeInSeconds;

	/**
	 * Constructor.
	 * 
	 * @param className     name of the class the test is in
	 * @param methodName    name of the test method
	 * @param signature     signature of the test method (should always be "()V")
	 */
	public JUnitTestCase(JavaClass jClass, Method method) {
	
		this.className = jClass.getClassName();
		this.methodName = method.getName();
		this.signature = method.getSignature();
        BuildServer.getBuildServerLog().trace("Trying to extract @MaxTestTime annotation...");
        TimeExtractor extractor = new TimeExtractor();
        extractor.setupVisitorForClass(jClass);
		method.accept(extractor);
        //jClass.accept(extractor);
		this.maxTimeInSeconds = extractor.seconds();
	}
	
	public int getMaxTimeInSeconds() {
		return maxTimeInSeconds;
	}
	/**
	 * Get the class name.
	 * 
	 * @return the class name
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Get the test method name.
	 * 
	 * @return the test method name
	 */
	public String getMethodName() {
		return methodName;
	}
	
	/**
	 * Get the test method signature.
	 * 
	 * @return the test method signature
	 */
	public String getSignature() {
		return signature;
	}
	
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		
		JUnitTestCase other = (JUnitTestCase) obj;
		
		return className.equals(other.className)
			&& methodName.equals(other.methodName)
			&& signature.equals(other.signature);
	}
	
	public int hashCode() {
		return className.hashCode() + methodName.hashCode() + signature.hashCode();
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("Class ");
		buf.append(className);
		buf.append(", method " );
		buf.append(methodName);
		buf.append(", signature ");
		buf.append(signature);
		
		return buf.toString();
	}
}
