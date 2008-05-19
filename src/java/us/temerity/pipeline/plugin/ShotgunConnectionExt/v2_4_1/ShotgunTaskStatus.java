package us.temerity.pipeline.plugin.ShotgunConnectionExt.v2_4_1;


/*------------------------------------------------------------------------------------------*/
/*   S H O T G U N   T A S K   S T A T U S                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The different status settings for tasks in Shotgun.
 * <p>
 * Use the Key value, returned by {@link #toKey()}, to change the status in Shotgun.
 */
public enum 
ShotgunTaskStatus
{
  WaitingToStart, ReadyToStart, InProgress, PendingReview, Approved, Final, OnHold;
  
  /**
   * The Shotgun database code for setting the status value.
   */
  public String
  toKey()
  {
    switch(this) {
    case WaitingToStart:
      return "wtg";
    case ReadyToStart:
      return "rdy";
    case InProgress:
      return "ip";
    case PendingReview:
      return "rev";
    case Approved:
      return "apr";
    case Final:
      return "fin";
    case OnHold:
      return "hld";
    }
    return null;
  }
}
