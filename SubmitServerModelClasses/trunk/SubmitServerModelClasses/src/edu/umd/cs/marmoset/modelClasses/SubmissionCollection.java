/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Sep 10, 2004
 *
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author jspacco
 * @deprecated Instead use the Submission.lookupAll...() methods that return List&lt;Submission&gt;
 */
public class SubmissionCollection
{
	// TODO this class is used in 2 different ways:
    // as a collection of all student submissions for a project, and as a collection
    // of all student submissions for a particular student
    // I should make subclasses for either case
    protected List<Submission> submissions;
	
	/**
	 * Constructor. 
	 */
	public SubmissionCollection()
	{
		submissions = new ArrayList<Submission>();
	}
	
	/**
	 * Gets the number of submissions in this collection.
	 * 
	 * @return
	 */
	public int size()
	{
		return submissions.size();
	}
	
	public boolean isEmpty()
	{
	    return submissions.isEmpty();
	}
	
	public void add(Submission submission)
	{
		submissions.add(submission);
	}
	
	public Submission get(int index)
	{
	    return submissions.get(index);
	}
	
	public List<Submission> getCollection()
	{
		return submissions;
	}
	
	public Iterator<Submission> iterator()
	{
		return submissions.iterator();
	}
	
	public ListIterator<Submission> listIterator()
	{
	    return submissions.listIterator();
	}
	
	public ListIterator<Submission> listIterator(int index)
	{
	    return submissions.listIterator(index);
	}
}
