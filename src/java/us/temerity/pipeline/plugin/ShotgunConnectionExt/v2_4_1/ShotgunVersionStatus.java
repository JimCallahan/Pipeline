// $Id: ShotgunVersionStatus.java,v 1.1 2008/07/03 19:50:45 jesse Exp $

package us.temerity.pipeline.plugin.ShotgunConnectionExt.v2_4_1;

/*------------------------------------------------------------------------------------------*/
/*   S H O T G U N   V E R S I O N   S T A T U S                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The different status settings for tasks in Shotgun.
 * <p>
 * Use the Key value, returned by {@link #toKey()}, to change the status in Shotgun.
 */
public enum 
ShotgunVersionStatus
{
  Approved, NotApplicable, PendingReview, Viewed;
  
  /**
   * The Shotgun database code for setting the status value.
   */
  public String
  toKey()
  {
    switch(this) {
    case Approved:
      return "apr";
    case NotApplicable:
      return "na";
    case Viewed:
      return "vwd";
    case PendingReview:
      return "rev";
    }
    return null;
  }
}
