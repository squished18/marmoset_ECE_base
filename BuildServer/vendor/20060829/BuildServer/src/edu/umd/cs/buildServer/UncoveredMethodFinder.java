/**
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 25, 2006
 *
 * @author jspacco
 */
package edu.umd.cs.buildServer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;
import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.codeCoverage.FileWithCoverage;

/**
 * MethodFinder
 * 
 * @author jspacco
 */
public class UncoveredMethodFinder {

	private List<File> classPathEntryList = new LinkedList<File>();

	private Logger log;

	private  HashMap<String, SortedMap<Integer, MethodRef>> methodLinenumberMap = new  HashMap<String, SortedMap<Integer, MethodRef>>();

	static class MethodRef {
		final String className, methodName, methodSignature;

		MethodRef(String className, String methodName, String methodSignature) {
			this.className = className;
			this.methodName = methodName;
			this.methodSignature = methodSignature;
		}
	}

	private Logger getLog() {
		if (log != null)
			return log;
		log = Logger.getLogger(BuildServer.class);
		return log;
	}

	public UncoveredMethodFinder() {
		// TODO Create a log that prints to stdout
	}

	public void setLog(Logger log) {
		this.log = log;
	}

	private CodeCoverageResults codeCoverageResults;

	/**
	 * Add a classpath entry. This will allow loading of the inspected class
	 * from precisely the codebase desired. Added entries should be specified in
	 * decreasing order of priority (i.e., the first one added has highest
	 * precedence), and will have precedence over system classpath entries.
	 * 
	 * @param entry
	 *            classpath entry; filename of a jar or zip file, or the name of
	 *            a directory
	 */
	public void addClassPathEntry(File entry) {
		classPathEntryList.add(entry);
	}

	public void addClassPathEntry(String entry) {
		for (String s : entry.split(File.pathSeparator)) {
			addClassPathEntry(new File(s));
		}
	}

	/**
	 * Set class to inspect.
	 * 
	 * @param classFile
	 *            class file containing the class to inspect
	 * @throws ClassFormatException
	 * @throws IOException
	 */
	public void inspectClass(File classFile) throws ClassFormatException,
			IOException {
        getLog().trace("Inspecting class " +classFile.getAbsolutePath()+ " for uncovered methods");
		JavaClass inspectedClass = new ClassParser(classFile.getPath()).parse();
		Repository.addClass(inspectedClass);
		inspectClass(inspectedClass);
	}

	/**
	 * Set class to inspect.
	 * 
	 * @param classFile
	 *            class file containing the class to inspect
	 * @throws ClassFormatException
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public void inspectClass(String className) throws ClassFormatException,
			IOException, ClassNotFoundException {
		JavaClass inspectedClass = Repository.lookupClass(className);
		inspectClass(inspectedClass);
	}

	/** Set class to inspect.
	 * 
	 * @param classFile
	 *            class file containing the class to inspect
	 * @throws ClassFormatException
	 * @throws IOException
	 */
	public void inspectClass(JavaClass inspectedClass)
			throws ClassFormatException, IOException {

		SortedMap<Integer, MethodRef> methodMap = methodLinenumberMap
				.get(inspectedClass.getSourceFileName());
		if (methodMap == null) {
			methodMap = new TreeMap<Integer, MethodRef>();
			methodLinenumberMap.put(inspectedClass.getSourceFileName(), methodMap);
		}
		for (Method method : inspectedClass.getMethods())
			if (method.getLineNumberTable() != null) {
				for (LineNumber num : method.getLineNumberTable()
						.getLineNumberTable())
					methodMap.put(num.getLineNumber(), new MethodRef(
							inspectedClass.getClassName(), method.getName(),
							method.getSignature()));
			}
	}

	public void initRepository() {
		StringBuffer reposClassPath = new StringBuffer();

		// Add user-specified classpath entries
		for (File f : classPathEntryList) {
			if (reposClassPath.length() > 0)
				reposClassPath.append(File.pathSeparatorChar);
			reposClassPath.append(f.getPath());
		}

		// Add system classpath entries
		if (reposClassPath.length() > 0) {
			reposClassPath.append(File.pathSeparatorChar);
		}
		reposClassPath.append(ClassPath.getClassPath());

		// Create new SyntheticRepository, and make it current
		ClassPath classPath = new ClassPath(reposClassPath.toString());
		// XXX VERY IMPORTANT:
		// We need to clear the cache because the Repository caches classfiles
		// and
		// the BuildServer runs in a loop in the same JVM invocation. This bug
		// sucked to find!
		Repository.clearCache();
		SyntheticRepository repos = SyntheticRepository.getInstance(classPath);
		Repository.setRepository(repos);
	}

	public List<StackTraceElement> findUncoveredMethods() {
		List<StackTraceElement> result = new LinkedList<StackTraceElement>();
		for (Map.Entry<String, SortedMap<Integer, MethodRef>> e : methodLinenumberMap
				.entrySet()) {
			String sourceFile = e.getKey();

			FileWithCoverage fileWithCoverage = codeCoverageResults
					.getFileWithCoverage(sourceFile);
			// If for some reason we can't find coverage for the classfile
			if (fileWithCoverage == null)
				continue;
			
			// Skip source files that have zero coverage
			if (!fileWithCoverage.isAnythingCovered()) continue;
			
			SortedSet<Integer> uncoveredMethods = fileWithCoverage
					.getUncoveredMethods();

			SortedMap<Integer, MethodRef> methodMap = e.getValue();

			for (Integer uncoveredMethodLineNumber : uncoveredMethods) {
				SortedMap<Integer, MethodRef> tailMap = methodMap
						.tailMap(uncoveredMethodLineNumber);
				if (tailMap.isEmpty()) {
					getLog()
							.trace(
									"uncovered line tail map thing is empty for "
											+ " uncoveredMethodLineNumber = "
											+ uncoveredMethodLineNumber
											+ "; no idea how to figure out the filename as well");
					continue;
				}
				Integer nextLineNumber = tailMap.firstKey();
				MethodRef unconveredMethod = methodMap.get(nextLineNumber);
				result.add(new StackTraceElement(unconveredMethod.className,
						unconveredMethod.methodName, sourceFile,
						uncoveredMethodLineNumber));
			}
		}
		return result;
	}

	/**
	 * @return Returns the codeCoverageResults.
	 */
	public CodeCoverageResults getCodeCoverageResults() {
		return codeCoverageResults;
	}

	/**
	 * @param codeCoverageResults
	 *            The codeCoverageResults to set.
	 */
	public void setCodeCoverageResults(@NonNull
	CodeCoverageResults codeCoverageResults) {
		this.codeCoverageResults = codeCoverageResults;
	}

	public static void main(String[] args) throws Exception {
		UncoveredMethodFinder finder = new UncoveredMethodFinder();
		finder
				.addClassPathEntry(new File(
						"/fs/fromage/jspacco/APROJECTS/SIGCSE-2006-features/foo/testfiles"));
		finder
				.addClassPathEntry(new File(
						"/fs/fromage/jspacco/APROJECTS/SIGCSE-2006-features/foo/build/obj"));

		String coverageFile = "/fs/fromage/jspacco/APROJECTS/SIGCSE-2006-features/foo/build/release-2-testHttpPutRequest(cmsc433.p1.ReleaseTests).xml";
		CodeCoverageResults codeCoverageResults = CodeCoverageResults
				.parseFile(coverageFile);

		finder.setCodeCoverageResults(codeCoverageResults);
		// finder.setInspectedClass("cmsc433/p1/WebServer");
		finder.inspectClass("cmsc433/p1/WebServer$1");

		for (StackTraceElement e : finder.findUncoveredMethods()) {
			System.out.println(e);
		}
	}

}
