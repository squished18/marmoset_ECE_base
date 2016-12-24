/*
 * Copyright (C) 2004-2005 University of Maryland
 * All Rights Reserved
 * Created on Jan 20, 2005
 */
package edu.umd.cs.buildServer;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import com.cenqua.clover.CloverInstr;

import edu.umd.cs.marmoset.modelClasses.CodeMetrics;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.modelClasses.TestPropertyKeys;

/**
 * Build a Java submission.
 * 
 * @author David Hovemeyer
 */
public class JavaBuilder extends Builder implements TestPropertyKeys {
    /**
	 * Constructor.
	 * 
	 * @param testProperties    TestProperties loaded from project jarfile's test.properties
	 * @param projectSubmission the ProjectSubmission to build
	 * @param directoryFinder   DirectoryFinder used to locate build and testfiles directories
	 */
	public JavaBuilder(
			TestProperties testProperties,
			ProjectSubmission projectSubmission,
			DirectoryFinder directoryFinder,
			SubmissionExtractor submissionExtractor) {
		super(testProperties, projectSubmission, directoryFinder, submissionExtractor);
	}
	/*
	 * Get the directory prefix leading to the Java project.
	 * Returns an empty string if the project is in the
	 * root directory of the submission zipfile.
	 * 
	 * Right now, the only thing we do is to look for an Eclipse ".project"
	 * file.  If we find it, that is where the project is.
	 */
	protected String getProjectPathPrefix() throws IOException {
		String prefix = "";
        ZipFile z = new ZipFile(getProjectSubmission().getZipFile());
		try {
			Enumeration<? extends ZipEntry> e = z.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = e.nextElement();
				String entryName = entry.getName();
				// XXX Note that we're only looking for something that ends with .project
                // so it would be really easy for this algorithm to screw up!
                // Also, note that the order in which files come out of the zipfile is
                // extremely important!  E.g. if the order is
                // 
                // Images/.project
                // .project
                // 
                // Then we will get the wrong prefix ("Images") instead of an empty path.
                if (entryName.endsWith(".project")) {
					prefix = entryName.substring(0, entryName.length() - ".project".length());
					break;
				}
			}
		} finally {
			try {
				z.close();
			} catch (IOException ignore) {
				// Ignore
			}
		}
		return prefix;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.Builder#inspectSubmission()
	 */
	protected CodeMetrics inspectSubmission()
	throws BuilderException, CompileFailureException
	{
		if (getProjectSubmission().getConfig().getOptionalBooleanProperty(ConfigurationKeys.SKIP_BUILD_INFO)) {
			getLog().info("Skipping build information step");
			return null;
		}
		
	    // perform the compile with debugging turned off so that we don't have a linenumber
	    // or variable map and the md5sum of the classfiles will be the same.
	    doCompile(new String[] {"-g:none"}, false);
	    
	    // Now get a list of all the class files
	    File outputDir =
			getProjectSubmission().getBuildOutputDirectory();
	    
	    // get a list of the classfiles
	    List<File> classFileList = BuildServerUtilities.listClassFilesInDirectory(outputDir);
	    
	    for (File file : classFileList)
	    {
	        getLog().trace("classfile to inspect: " +file);
	    }

	    // Now get a list of the source files
	    List<File> sourceFileList = new LinkedList<File>();
	    // convert from Strings to files
	    for (Iterator<String> ii=getSourceFileList().iterator(); ii.hasNext();)
	    {
	        String filePath = ii.next();
	        File file = new File(getDirectoryFinder().getBuildDirectory(), filePath);
	        sourceFileList.add(file);
	    }
	    
	    try {
            CodeMetrics codeMetrics=new CodeMetrics();
            codeMetrics.setMd5sumClassfiles(classFileList);
            codeMetrics.setMd5sumSourcefiles(sourceFileList);
            codeMetrics.setCodeSegmentSize(outputDir, classFileList, outputDir.getAbsolutePath());
	        return codeMetrics;
	    } catch (IOException e) {
	        getLog().error("Unable to compute md5sum due to IOException!", e);
	    } catch (NoSuchAlgorithmException e) {
	        getLog().error("md5 algorithm not found!", e);
	    } catch (ClassNotFoundException e) {
	        getLog().error("Unable to find and load one of the classes in " +outputDir, e);
        }
	    return null;
	}
	
	/**
	 * Compile the project.
	 * @param options Additional options passed to javac, 
	 * 		such as -g:none that keeps debugging information out of the classfile.
	 * @param useInstrumentedSrcDir If true, then compile in the "inst-src" directory
	 * 		containing classfiles instrumented by Clover rather than the raw classfiles 
	 * 		extracted from the projectSubmission.
	 * 		<b>NOTE:</b> It's now <b>REQUIRED</b> that all source files are rooted
	 * 		in a "src" directory!
	 * @throws BuilderException thrown when the compile fails for unexpected reasons
	 * (i.e. IOException)
	 * @throws CompileFailureException thrown when the compile fails for expected reasons
	 * (i.e. syntax errors, etc.)
	 */
	private void doCompile(String[] options, boolean useInstrumentedSrcDir)
	throws BuilderException, CompileFailureException
	{	    
	    // CODE COVERAGE:
		// Use the programmic interface to Clover to instrument code for coverage
		if (useInstrumentedSrcDir)
		{
		    // TODO Put this clover database in the student's build directory
		    // TODO Also clean up this file when we're done with it!
		    String cloverDBPath;
		    try {
		        cloverDBPath = getProjectSubmission().getConfig().getRequiredProperty(CLOVER_DB);
		    } catch (MissingConfigurationPropertyException e) {
		        throw new BuilderException(e);
		    }
		        
		    File cloverDB = new File(cloverDBPath);
		    if (cloverDB.exists()) {
		        if (!cloverDB.delete())
		            getLog().warn("Unable to delete old clover DB at " +cloverDBPath);
		    }
		    // XXX Clover requires that you set BOTH jdk14 (for assert, etc)
		    // and jdk15 (for generics, etc)
		    String [] cliArgs = { "-jdk14", "-jdk15",
		    		"-i", cloverDBPath,
		            "-s", getProjectSubmission().getSrcDirectory().getAbsolutePath(),
		            "-d", getProjectSubmission().getInstSrcDirectory().getAbsolutePath()};
		    String coverageMarkupCmd = " ";
		    for (int ii=0; ii < cliArgs.length; ii++) {
		    	coverageMarkupCmd+=cliArgs[ii] + " ";
		    }
		    getLog().trace("Clover instrumentation args: " +coverageMarkupCmd);
		    int result = CloverInstr.mainImpl(cliArgs);
		    if (result != 0) {
		        throw new BuilderException("Clover was unable to instrument the source code in " +
		                getProjectSubmission().getSrcDirectory().getAbsolutePath());
		    }
		}
	    
	    if (getSourceFileList().isEmpty())
			throw new CompileFailureException(
					"Project " + getProjectSubmission().getZipFile().getPath() + " contains no source files", "");

		// Create compiler output directory
		File outputDir =  getProjectSubmission().getBuildOutputDirectory();
		if (!outputDir.isDirectory() && !outputDir.mkdir()) {
			throw new BuilderException("Could not create compiler output directory " +
					outputDir.getPath());
		}
		
		// Determine Java -source value to use.
		String javaSourceVersion = getTestProperties().getJavaSourceVersion();
		
		// Determine the classpath to be used for compiling.
		// Currently this is:
		//   - the "ambient" classpath (whatever classpath was used to invoke
		//     the build server), and
		//   - the project test jar
		//
		// The ambient classpath is used because it contains junit,
		// which the student may have used.
		//
		// We may want to revisit this in the future.
		StringBuffer cp = new StringBuffer();
		cp.append(System.getProperty("java.class.path"));
		cp.append(File.pathSeparator);
		cp.append(getProjectSubmission().getProjectJarFile().getAbsolutePath());
		// TODO add clover to the classpath
		//cp.append(File.pathSeparator);
		//cp.append(getDirectoryFinder().getBuildServerRoot().getAbsolutePath());
		
		// Specify javac command line arguments.
		LinkedList<String> args = new LinkedList<String>();
		args.add("javac");
		// Find source files in current dir (which is the build dir)
		args.add("-sourcepath");
		args.add(".");
		// Specify classpath
		args.add("-classpath");
		args.add(cp.toString());
		// Generate compiled class files in the output directory
		args.add("-d");
		args.add(outputDir.getAbsolutePath());
		// Specify Java source version.
		args.add("-source");
		args.add(javaSourceVersion);
		// add optional args
		if (options != null)
		{
		    for (int ii=0; ii < options.length; ii++) {
		        args.add(options[ii]);
		    }
		}
//        if (!System.getProperty("java.version").startsWith("1.4")) {
//            args.add("-source");
//            args.add(javaSourceVersion);
//        }
		// Compile all source files found in submission

		// XXX Code now MUST be in a "src" directory!
		if (useInstrumentedSrcDir) {
		    List<String> newSourceFileList = new LinkedList<String>();
		    for (Iterator<String> ii=getSourceFileList().iterator(); ii.hasNext();) {
		        String originalSourceFile = ii.next();
		        String newSourceFile = originalSourceFile.replaceAll("^src", INSTRUMENTED_SRC_DIR);
		        newSourceFileList.add(newSourceFile);
		    }
		    args.addAll(newSourceFileList);
		} else {
		    // TODO rewrite the source files into the appropriate directory anyway
			args.addAll(getSourceFileList());
		}

		if (true) {
			StringBuffer buf = new StringBuffer();
			for (Iterator<String> i = args.iterator(); i.hasNext(); ) {
				buf.append(i.next() + " ");
			}
			getLog().debug("Javac command: " + buf.toString());
		}
		
		// Compile all source files found in submission
		//args.addAll(getSourceFileList());
	    
	    try {
			Process javac = Runtime.getRuntime().exec(
					args.toArray(new String[args.size()]),
					null,
					getDirectoryFinder().getBuildDirectory());

			// Capture stdout and stderr from the process
			CombinedStreamMonitor monitor = new CombinedStreamMonitor(
					javac.getInputStream(), javac.getErrorStream());
			monitor.start();
			
			// Wait for process to execute, and for all process output
			// to be read
			int exitCode = javac.waitFor();
			monitor.join();
			
			// If compile failed, collect output messages
			// and throw a CompileFailureException
			if (exitCode != 0) {
				setCompilerOutput(monitor.getCombinedOutput());
				
				throw new CompileFailureException(
						"Compile failed for project " + getProjectSubmission().getZipFile().getPath(),
						this.getCompilerOutput()
						);
			}
			
			// Looks like compilation succeeded.
			// Sleep for a few seconds to try to workaround some of
			// the mysterious "file not found" problems we've been
			// seeing when trying to execute the project.
			// (These may be NFS-related.)
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// Ignore
			}
			
		} catch (IOException e) {
			throw new BuilderException("Couldn't invoke java", e);
		} catch (InterruptedException e) {
			throw new BuilderException("Javac wait was interrupted", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.buildServer.Builder#compileProject()
	 */
	protected void compileProject() throws BuilderException, CompileFailureException {

		// Don't use instrumented source directories when this compilation is for the
		// inspection step.  Otherwise FindBugs reports lots of junk introduced by Clover.
		if (isInspectionStepCompilation())
	    	doCompile(null, false);
	    else
	    	doCompile(null, getProjectSubmission().useInstrumentedSrcDir());
	}
	
	// Just a test driver
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: " + JavaBuilder.class.getName() +
					": <build dir> <submission zip file> <project jar file>");
			System.exit(1);
		}
		
		
		File buildDirectory = new File(args[0]);
		File zipFile = new File(args[1]);
		File projectJarFile = new File(args[2]);
		
		Configuration config = new Configuration();
		config.setProperty(BUILD_DIRECTORY, args[0]);

		Logger log = Logger.getLogger(Builder.class);

		ProjectSubmission projectSubmission = new ProjectSubmission(
				config,
				log,
				"submissionPK",
				"projectPK",
				"false",
                "false");
		
		// XXX: hack to allow testing with arbitrary submission zipfile
		// and project jarfile.
		projectSubmission.zipFile = zipFile;
		projectSubmission.projectJarFile = projectJarFile;

		TestProperties testProperties = new TestProperties();
		// Currently, nothing has to be specified in the test
		// properties for the builder to work.
		
		JavaSubmissionExtractor submissionExtractor = new JavaSubmissionExtractor(
				projectSubmission.getZipFile(), buildDirectory, log);

		JavaBuilder builder = new JavaBuilder(
				testProperties,
				projectSubmission,
				new JavaDirectoryFinder(config),
				submissionExtractor);
		
		try {
			builder.execute();
		} catch (CompileFailureException e) {
			e.printStackTrace(); // OK, in main()
			System.out.println(builder.getCompilerOutput());
		}
	}
}
