// $Id: VersionID.java,v 1.2 2004/03/01 21:43:29 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   V E R S I O N   I D                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A unique identifier for a specific revision of a Pipeline node. <P> 
 * 
 * Revision numbers are composed of four dot seperated non-negative integer valued 
 * components: <BR> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <I>Massive</I>.<I>Major</I>.<I>Minor</I>.<I>Trivial</I> <BR>
 * </DIV> <BR> 
 * 
 * The component numbers are arranged left-to-right from most-to-least imporant.  The intial
 * revision number of a node is always (1.0.0.0).  The importance of subsequent revisions
 * can be determined by which component have been inremented as compared to the previous 
 * revision.
 * 
 * @see IncMethod
 */
public
class VersionID 
  implements Comparable, Cloneable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct an initial revision number (1.0.0.0).
   */ 
  public 
  VersionID() 
  {}

  /**
   * Construct from the given string representation. 
   * 
   * @param str [<B>in</B>]
   *   The initial version ID encoded as a <CODE>String</CODE>.
   */ 
  public 
  VersionID
  (
   String str 
  ) 
  {
    fromString(str);
  }

  /**
   * Copy constructor.
   */ 
  public
  VersionID
  (
   VersionID vid  
  ) 
  {
    pIDs = vid.pIDs.clone();
  }

    
 
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the set of revision number components.
   */ 
  public int[]
  getVersionNumbers() 
  {
    return pIDs.clone();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N C R E M E N T I N G                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Increment the fourth component of the revision number. <P> 
   * 
   * Examples include: 
   * <DIV style="margin-left: 40px;">
   *    1.0.0.0 to 1.0.0.1 <BR>
   *    1.3.2.5 to 1.3.2.6 <BR>
   *    2.4.0.2 to 2.4.0.3 <BR>
   * </DIV>
   */
  public void 
  incTrivial() 
  {
    pIDs[3]++;
  }

  /**
   * Increment the third component of the revision number. <P> 
   * 
   * Examples include: 
   * <DIV style="margin-left: 40px;">
   *    1.0.0.0 to 1.0.1.0 <BR>
   *    1.3.2.5 to 1.3.3.0 <BR>
   *    2.4.0.2 to 2.4.1.0 <BR>
   * </DIV>
   */
  public void 
  incMinor() 
  {
    pIDs[2]++;
    pIDs[3] = 0;
  }

  /**
   * Increment the second component of the revision number. <P> 
   * 
   * Examples include: 
   * <DIV style="margin-left: 40px;">
   *    1.0.0.0 to 1.1.0.0 <BR>
   *    1.3.2.5 to 1.4.0.0 <BR>
   *    2.4.0.2 to 2.5.0.0 <BR>
   * </DIV>
   */
  public void 
  incMajor() 
  {
    pIDs[1]++;
    pIDs[2] = 0;
    pIDs[3] = 0;
  }

  /**
   * Increment the first component of the revision number. <P> 
   * 
   * Examples include: 
   * <DIV style="margin-left: 40px;">
   *    1.0.0.0 to 2.0.0.0 <BR>
   *    1.3.2.5 to 2.0.0.0 <BR>
   *    2.4.0.2 to 3.0.0.0 <BR>
   * </DIV>
   */
  public void 
  incMassive() 
  {
    pIDs[0]++;
    pIDs[1] = 0;
    pIDs[2] = 0;
    pIDs[3] = 0;
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
    if((obj != null) && (obj instanceof VersionID)) {
      VersionID vid = (VersionID) obj;
      return (Arrays.equals(pIDs, vid.pIDs));
    }
    return false;
  }

  /**
   * Returns a hash code value for the object.
   */
  public int 
  hashCode() 
  {
    return toString().hashCode();
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    StringBuffer buf = new StringBuffer();

    int wk;
    for(wk=0; wk<4; wk++) {
      buf.append(pIDs[wk]);
      if(wk < 3) 
	buf.append(".");
    }

    return buf.toString();
  }

  /**
   * Convert from a string representation. 
   */ 
  public void 
  fromString
  (
   String str    
  ) 
  {
    if(str == null) 
      throw new IllegalArgumentException
	("The version string cannot be (null)!");

    if(str.length() == 0) 
      throw new IllegalArgumentException
	("The version string cannot be empty!");      

    String[] parts = str.split("\\.");
    if(parts.length != 4)
      throw new IllegalArgumentException
	("Found the wrong number (" + parts.length + ") of revision number compoents " + 
	 "in (" + str + "), should have been (4)!");

    int ids[] = new int[4];
    
    int wk;
    for(wk=0; wk<4; wk++) {
      if(parts[wk].length() == 0) 
	throw new IllegalArgumentException
	  ("Found a missing version number component in (" + str + ")!");

      int num = 0;
      try {
	num = Integer.parseInt(parts[wk]);
      }
      catch (NumberFormatException e) {
	throw new IllegalArgumentException
	  ("Illegal version number component (" + parts[wk] + ") found in (" + str + ")!");
      }

      if(num < 0) 
	throw new IllegalArgumentException
	  ("Negative version number component(" + num + ") found in (" + str + ")!");

      if((wk == 0) && (num < 1)) 
	throw new IllegalArgumentException
	  ("The first version number component (" + num + ") must be positive!");
      
      ids[wk] = num;
    }

    pIDs = ids;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this object with the specified object for order.
   * 
   * @param obj [<B>in</B>]
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
    
    if(!(obj instanceof VersionID))
      throw new IllegalArgumentException("The object to compare was NOT a VersionID!");

    return compareTo((VersionID) obj);
  }


  /**
   * Compares this <CODE>VersionID</CODE> with the given <CODE>VersionID</CODE> for order.
   * 
   * @param vid [<B>in</B>]
   *   The <CODE>VersionID</CODE> to be compared.
   */
  public int
  compareTo
  (
   VersionID vid 
  )
  {
    int wk;
    for(wk=0; wk<4; wk++)
      if(pIDs[wk] != vid.pIDs[wk])
 	return (pIDs[wk] - vid.pIDs[wk]);
    
    return (pIDs.length - vid.pIDs.length);
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
    return super.clone();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 309955827563014312L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The revision number components.
   */
  private int[] pIDs = { 1, 0, 0, 0 };
  
}



