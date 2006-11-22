// $Id: JobPath.java,v 1.2 2006/11/22 09:08:01 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   P A T H                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The path from the root job of a {@link JQueueJobViewerPanel JQueueJobViewerPanel} to a 
 * specific upstream job.
 */
public
class JobPath
  implements Cloneable, Comparable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct new root job path from the jobID of the root job. <P> 
   * 
   * @param jobID 
   *   The unique job identifier.
   */
  public
  JobPath
  ( 
   Long jobID
  ) 
  {
    pJobIDs = new LinkedList<Long>();
    pJobIDs.add(jobID);

    buildCache();
  }

  /** 
   * Construct job path which extends an existing job path by adding the given job.<P>
   * 
   * @param path
   *   The path to extend.
   * 
   * @param jobID 
   *   The unique job identifier.
   */
  public
  JobPath
  ( 
   JobPath path,
   Long jobID
  ) 
  {
    pJobIDs = new LinkedList<Long>(path.getJobIDs());
    pJobIDs.add(jobID);

    buildCache();
  }

  /** 
   * Copy constructor.
   */
  public
  JobPath
  (
   JobPath path
  ) 
  {
    pJobIDs = new LinkedList<Long>(path.getJobIDs());

    buildCache();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the number of jobs in the path. 
   */ 
  public int 
  getNumJobs() 
  {
    return pJobIDs.size();
  }

  /** 
   * Get the IDs of the jobs on the path from the root job to the current job.
   */ 
  public Collection<Long>
  getJobIDs() 
  {
    return Collections.unmodifiableCollection(pJobIDs);
  }

  /** 
   * Get the ID of the root job.
   */
  public Long
  getRootJobID() 
  {
    return pJobIDs.getFirst();
  }

  /** 
   * Get the ID of the current job.
   */
  public Long
  getCurrentJobID() 
  {
    return pJobIDs.getLast();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof JobPath)) {
      JobPath path = (JobPath) obj;
      return ((pHashCode == path.pHashCode) && 
	      pStringRep.equals(path.pStringRep));
    }
    return false;
  }

  /**
   * Returns a hash code value for the object.
   */
  public int 
  hashCode() 
  {
    assert(pStringRep != null);
    return pHashCode;
  }

  /**
   * Returns a string representation of the object. <P> 
   */
  public String
  toString() 
  {
    assert(pStringRep != null);
    return pStringRep;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this object with the specified object for order.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be compared.
   */
  public int
  compareTo
  (
   Object obj
  )
  {
    if(obj == null) 
      throw new NullPointerException();
    
    if(!(obj instanceof JobPath))
      throw new IllegalArgumentException("The object to compare was NOT a JobPath!");

    return compareTo((JobPath) obj);
  }


  /**
   * Compares this <CODE>JobPath</CODE> with the given <CODE>JobPath</CODE> for order.
   * 
   * @param id 
   *   The <CODE>JobPath</CODE> to be compared.
   */
  public int
  compareTo
  (
   JobPath path
  )
  {
    return pStringRep.compareTo(path.pStringRep);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
    throws CloneNotSupportedException
  {
    return new JobPath(this);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compute the cached string representation and hash code for the job path.
   */
  private void
  buildCache() 
  {
    StringBuilder buf = new StringBuilder();
    buf.append(":");
    for(Long jobID : pJobIDs) {
      buf.append(jobID + ":");
    }
    pStringRep = buf.toString();
    pHashCode  = pStringRep.hashCode();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The jobIDs of the jobs on the path from the root job to the current job
   */
  private LinkedList<Long> pJobIDs;


  /** 
   * The cached string representation.
   */
  private String  pStringRep;
 
  /** 
   * The cached hash code.
   */
  private int  pHashCode;
 
}

