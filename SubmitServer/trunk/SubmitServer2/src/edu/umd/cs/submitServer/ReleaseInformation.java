/*
 * Created on Sep 7, 2004
 *
 */
package edu.umd.cs.submitServer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;

/**
 * Given a project and a set of submissions that have requested release testing,
 * this class figures out how many tokens are remaining and the regeneration schedule.
 *
 *  @author jspacco
 */
public class ReleaseInformation
{
	private int releaseTokens;
	private int regenerationTime;
	private int tokensRemaining;
	private int tokensUsed=0;
	private List<Timestamp> regenerationSchedule=new ArrayList<Timestamp>();
	private static final int MILLIS_PER_HOUR = 60*60*1000;
	private boolean releaseRequestOK=false;
	
	// TODO make this the only constructor
	public ReleaseInformation(Project project, List submissionList)
	{
		try {
			// fetch tokens and regeneration time from the project record
		    releaseTokens = Integer.parseInt(project.getReleaseTokens());
			regenerationTime = Integer.parseInt(project.getRegenerationTime()); 
			
			// create Timestamp for the regeneration cutoff
			long nowMillis = System.currentTimeMillis();
			Timestamp then = new Timestamp(nowMillis - (regenerationTime*MILLIS_PER_HOUR));
			
			tokensUsed=0;
			for (Iterator it=submissionList.iterator(); it.hasNext();)
			{
				Submission submission = (Submission)it.next();
				
				Timestamp requestTime = submission.getReleaseRequest();
				// the SubmissionCollection should be fetched with SubmissionCollection.lookupAllForReleaseTesting()
				// which only includes submissions where the release_request column is NOT NULL
				if (requestTime == null)
				    continue;

				if (requestTime.after(then))
				{
					tokensUsed++;
					//Debug.print("requstTime is after then, so tokensUsed is now: " +tokensUsed);
					regenerationSchedule.add(new Timestamp(requestTime.getTime() + regenerationTime*MILLIS_PER_HOUR));
				}
			}
			// compute remaining tokens
			tokensRemaining = releaseTokens - tokensUsed;
			if (tokensUsed > releaseTokens)
			{
			    Debug.error("Used " +tokensUsed+ " when only allowed " +releaseTokens);
			    throw new IllegalStateException("Used " +tokensUsed+ " when only should be allowed " +releaseTokens);
			}
			// if we can have at least one token remaining, set releaseRequestOK to true
			if (tokensRemaining > 0)
			    releaseRequestOK = true;
		}
		catch (NumberFormatException e)
		{
			String msg = "Corrupted data in the project table, I can't parse release_tokens into an int: " +e;
			Debug.error(msg);
			e.printStackTrace();
			// re-throw exception
			throw e;
		}
	}


    /**
     * @return Returns the regenerationSchedule.
     */
    public List<Timestamp> getRegenerationSchedule()
    {
        return regenerationSchedule;
    }
    /**
     * @param regenerationSchedule The regenerationSchedule to set.
     */
    public void setRegenerationSchedule(List<Timestamp> regenerationSchedule)
    {
        this.regenerationSchedule = regenerationSchedule;
    }
    /**
     * @return Returns the regenerationTime.
     */
    public int getRegenerationTime()
    {
        return regenerationTime;
    }
    /**
     * @param regenerationTime The regenerationTime to set.
     */
    public void setRegenerationTime(int regenerationTime)
    {
        this.regenerationTime = regenerationTime;
    }
    /**
     * @return Returns the releaseRequestOK.
     */
    public boolean isReleaseRequestOK()
    {
        return releaseRequestOK;
    }
    /**
     * @param releaseRequestOK The releaseRequestOK to set.
     */
    public void setReleaseRequestOK(boolean releaseRequestOK)
    {
        this.releaseRequestOK = releaseRequestOK;
    }
    /**
     * @return Returns the releaseTokens.
     */
    public int getReleaseTokens()
    {
        return releaseTokens;
    }
    /**
     * @param releaseTokens The releaseTokens to set.
     */
    public void setReleaseTokens(int releaseTokens)
    {
        this.releaseTokens = releaseTokens;
    }
    /**
     * @return Returns the tokensRemaining.
     */
    public int getTokensRemaining()
    {
        return tokensRemaining;
    }
    /**
     * @param tokensRemaining The tokensRemaining to set.
     */
    public void setTokensRemaining(int tokensRemaining)
    {
        this.tokensRemaining = tokensRemaining;
    }
    /**
     * @return Returns the tokensUsed.
     */
    public int getTokensUsed()
    {
        return tokensUsed;
    }
    /**
     * @param tokensUsed The tokensUsed to set.
     */
    public void setTokensUsed(int tokensUsed)
    {
        this.tokensUsed = tokensUsed;
    }
}
