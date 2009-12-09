// $Id: JobRankSorter.java,v 1.3 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline.core;

import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   R A N K   S O R T E R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A class that uses the Criteria specified in a Dispatch Control to sort JobRanks.
 */ 
public
class JobRankSorter
  implements Comparator<JobRank>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public 
  JobRankSorter
  (
    DispatchControl control  
  )
  {
    List<DispatchCriteria> crits = control.getCriteria();
    pCriteria = new DispatchCriteria[crits.size()];
    int i = 0;
    for (DispatchCriteria crit : crits) {
      pCriteria[i] = crit;
      i++;
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  public DispatchCriteria[]
  getCriteria()
  {
    DispatchCriteria[] toReturn = new DispatchCriteria[pCriteria.length];
    
    int i = 0;
    for (DispatchCriteria crit : pCriteria)
      toReturn[i++] = crit;
    
    return toReturn;
  }

  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public int 
  compare
  (
    JobRank o1,
    JobRank o2
  )
  {
    for (DispatchCriteria crit : pCriteria) {
      int compare = compareHelper(crit, o1, o2);
      if (compare != 0)
        return compare;
    }
    return 0;
  }
  
  private int
  compareHelper
  (
    DispatchCriteria criteria,
    JobRank o1,
    JobRank o2
  )
  {
    switch (criteria) {
    case JobGroupPercent:
      {
        double percent1 = o1.getFavorGroupPercent();
        double percent2 = o2.getFavorGroupPercent();
        if((percent1 - percent2) >= sEpsilon) 
          return -1;
        else if((percent2 - percent1) >= sEpsilon) 
          return 1;
        return 0;
      }
      
    case JobPriority:
      {
        int priority1 = o1.getPriority();
        int priority2 = o2.getPriority();
        /* job priority is in descending order */ 
        if(priority1 > priority2) 
          return -1; 
        else if(priority1 < priority2) 
          return 1; 
        return 0;
      }
      
    case SelectionScore:
      {
        int score1 = o1.getScore();
        int score2 = o2.getScore();
        if(score1 > score2) 
          return -1; 
        else if(score1 < score2) 
          return 1; 
        return 0;
      }
      
    case BalanceGroups:
      {
        double percent1 = o1.getBalanceGroupPercent();
        double percent2 = o2.getBalanceGroupPercent();
        
        if (percent1 < percent2)
          return -1;
        else if (percent1 > percent2)
          return 1;
        else {
          int use1 = o1.getBalanceGroupUse();
          int use2 = o2.getBalanceGroupUse();
          
          if (use1 < use2)
            return -1;
          else if (use2 > use1)
            return 1;
        }
        return 0;
      }
      
    case TimeStamp:
      {
        long time1 = o1.getTimeStamp();
        long time2 = o2.getTimeStamp();
        if(time1 > time2) 
          return 1; 
        else if(time1 < time2) 
          return -1; 
        return 0;
      }
      
    default:
      throw new IllegalStateException
        ("The Dispatcher Criteria (" + criteria + ") is not a valid criteria");
    }
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The smallest difference in floating point value considered to be different.
   */ 
  private static final double sEpsilon = 0.000001;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The list of criteria that is used to determine how jobs are dispatched
   */ 
  private DispatchCriteria pCriteria[]; 
}
