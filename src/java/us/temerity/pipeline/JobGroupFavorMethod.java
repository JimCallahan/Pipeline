// $Id: JobGroupFavorMethod.java,v 1.1 2006/12/14 02:39:05 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   G R O U P   F A V O R   M E T H O D                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The method used to rank pending jobs for a slot based on the state of other jobs in their 
 * parent job group in order to balance the queue load between job groups.
 */
public
enum JobGroupFavorMethod
{  
  /**
   * Ignore the state of other jobs in the job group when ranking jobs.
   */
  None, 
  
  /**
   * Prefer jobs which are members of job groups with the highest percentage of Finished
   * or Running jobs.
   */
  MostEngaged, 
  
  /**
   * Prefer jobs which are members of job groups with the highest percentage of Pending 
   * or Preempted jobs.
   */
  MostPending; 
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<JobGroupFavorMethod>
  all() 
  {
    JobGroupFavorMethod values[] = values();
    ArrayList<JobGroupFavorMethod> all = new ArrayList<JobGroupFavorMethod>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }

  /**
   * Get the list of human friendly string representation for all possible values.
   */ 
  public static ArrayList<String>
  titles() 
  {
    ArrayList<String> titles = new ArrayList<String>();
    for(JobGroupFavorMethod rel : JobGroupFavorMethod.all()) 
      titles.add(rel.toTitle());
    return titles;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle() 
  {
    return sTitles[ordinal()];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static String sTitles[] = {
    "None", 
    "Most Engaged", 
    "Most Pending"
  };
}
