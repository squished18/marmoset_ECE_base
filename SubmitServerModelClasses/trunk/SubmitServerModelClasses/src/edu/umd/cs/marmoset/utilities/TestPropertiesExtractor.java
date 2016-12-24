/*
 * Created on Sep 5, 2004
 */
package edu.umd.cs.marmoset.utilities;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class TestPropertiesExtractor extends ZipExtractor {
	private Set<String> extractedSet;
	
	public TestPropertiesExtractor(File projectJarFile, File buildDirectory) throws ZipExtractorException{
		super(projectJarFile, buildDirectory);
		extractedSet = new HashSet<String>();
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.ZipExtractor#shouldExtract(java.lang.String)
	 */
	protected boolean shouldExtract(String entryName) {
		return entryName.equals("test.properties") || entryName.equals("security.policy");
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.ZipExtractor#successfulFileExtraction(java.lang.String, java.lang.String)
	 */
	protected void successfulFileExtraction(String entryName, String filename) {
		extractedSet.add(entryName);
	}
	
	public boolean extractedTestProperties() {
		return extractedSet.contains("test.properties");
	}
	
	public boolean extractedSecurityPolicyFile() {
		return extractedSet.contains("security.policy");
	}
}
