/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on April 18, 2005
 */
package edu.umd.cs.buildServer;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * FindBugsRunner that saves the captured BugCollection to
 * a file in a specified directory.
 * 
 * @author David Hovemeyer
 */
public class XMLProducingFindBugsRunner extends AbstractFindBugsRunner {

	protected String[] getExtraFindBugsOptions() {
		try {
			ArrayList<String> options = new ArrayList<String>();
			String fbOptions = getProjectSubmission().getConfig().getOptionalProperty("findbugs.options");
			if (fbOptions != null) {
				ArgumentParser argParser = new ArgumentParser(fbOptions);
				while (argParser.hasNext()) {
					options.add(argParser.next());
				}
			}
			
			options.add("-outputFile");
			options.add(getOutputFile().getPath());
			
			return  options.toArray(new String[options.size()]);
		} catch (MissingConfigurationPropertyException e) {
			projectSubmission.getLog().error("Error generating FindBugs command line", e);
			return null;
		}
	}
	
	private File getOutputFile() throws MissingConfigurationPropertyException {
		StringBuffer outputDir = new StringBuffer();
		outputDir.append(projectSubmission.getConfig().getRequiredProperty(
				ConfigurationKeys.FINDBUGS_OUTPUT_DIRECTORY));
		
		String submissionPK = projectSubmission.getSubmissionPK();
		int len = submissionPK.length();
		if (len >= 2 && isDigit(submissionPK.charAt(len - 1)) && isDigit(submissionPK.charAt(len - 2))) {
			// Use the hierarchical directory organization to avoid putting
			// a huge number of output files in the same directory.
			// We use the *last* two digits of the submission pk,
			// rather than the first two, because they are much more
			// evenly distributed.
			outputDir.append(File.separatorChar);
			outputDir.append(submissionPK.charAt(len - 1));
			outputDir.append(File.separatorChar);
			outputDir.append(submissionPK.charAt(len - 2));
		}
		
		File outputFile = new File(outputDir.toString(), submissionPK + ".xml");

		return outputFile;
	}
	
	protected Thread createStdoutMonitor(InputStream in) {
		return IO.monitor(in, new DevNullOutputStream());
	}

	protected Thread createStderrMonitor(InputStream err) {
		return new MonitorThread(err, new TextOutputSink());
	}

	protected void inspectFindBugsResults(Thread stdoutMonitor, Thread stderrMonitor) {
		String errorOutput = ((MonitorThread) stderrMonitor).getOutputSink().getOutput();
		if (!errorOutput.equals("")) {
			projectSubmission.getLog().warn("Error output from FindBugs process:\n" +
					errorOutput);
		}
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

}
