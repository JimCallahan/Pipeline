// $Id: VersionID.java,v 1.1 2004/02/28 19:59:47 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   V E R S I O N   I D                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A unique identifier for a specific revision of a Pipeline node. <P> 
 * 
 * Revision numbers are composed of dot seperated lists of positive integer values and
 * must contain at least one value.  Examples of valid version IDs include: "1.2", "3.1.5" 
 * and "4".
 */
public
class VersionID 
  implements Comparable, Cloneable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
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
   * Copy constructor 
   */ 
  public
  VersionID
  (
   VersionID vid  
  ) 
  {
    pIDs = new int[vid.pIDs.length];
    
    int wk;
    for(wk=0; wk<vid.pIDs.length; wk++)
      pIDs[wk] = vid.pIDs[wk];
  }

    
 
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the complete set of version level numbers.
   */ 
  public int[]
  getVersionNumbers() 
  {
    return pIDs;
  }
  

      
  /*----------------------------------------------------------------------------------------*/
  /*   R E L A T I V E   I N C R E M E N T I N G                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Increment the most minor level of version. <P> 
   * 
   * For example: "1.5.2" -> "1.5.3" 
   */ 
  public void
  incMinor() 
  {
    pIDs[pIDs.length-1]++;
  }


  /**
   * Extend the levels of version one sublevel. <P> 
   * 
   * For example: "1.5.2" -> "1.5.2.1" 
   */ 
  public void
  addMinor() 
  {
    int[] ids = new int[pIDs.length+1];

    int wk;
    for(wk=0; wk<pIDs.length; wk++)
      ids[wk] = pIDs[wk];
    ids[wk] = 1;

    pIDs = ids;
  }
  

  /**
   * Increment the lowest major level of version. <P> 
   * 
   * For example: "1.5.2" -> "1.6" 
   */ 
  public void
  incMajor()
  {
    if(pIDs.length < 2) {
      incMinor();
      return;
    }
      
    int[] ids = new int[pIDs.length-1];
    
    int wk;
    for(wk=0; wk<ids.length; wk++)
      ids[wk] = pIDs[wk];
    pIDs = ids;

    pIDs[pIDs.length-1]++;
  }

     
  /*----------------------------------------------------------------------------------------*/
  /*   A B S O L U T E   I N C R E M E N T I N G                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Increment the fourth level of version numbers. 
   */
  public void 
  trivial() 
  {
    int[] ids = new int[4];

    int wk;
    for(wk=0; (wk<pIDs.length) && (wk<4); wk++)
      ids[wk] = pIDs[wk];

    for(; wk<3; wk++)
      ids[wk] = 1;

    ids[3]++;

    pIDs = ids;
  }

  /**
   * Increment the third level of version numbers. 
   */
  public void 
  minor() 
  {
    int[] ids = new int[3];

    int wk;
    for(wk=0; (wk<pIDs.length) && (wk<3); wk++)
      ids[wk] = pIDs[wk];

    for(; wk<2; wk++)
      ids[wk] = 1;

    ids[2]++;

    pIDs = ids;
  }

  /**
   * Increment the second level of version numbers. 
   */
  public void 
  major() 
  {
    int[] ids = new int[2];

    int wk;
    for(wk=0; (wk<pIDs.length) && (wk<2); wk++)
      ids[wk] = pIDs[wk];

    for(; wk<1; wk++)
      ids[wk] = 1;

    ids[1]++;

    pIDs = ids;
  }

  /**
   * Increment the first level of version numbers. 
   */
  public void 
  massive() 
  {
    int[] ids = new int[1];

    int wk;
    for(wk=0; (wk<pIDs.length) && (wk<1); wk++)
      ids[wk] = pIDs[wk];

    ids[0]++;

    pIDs = ids;
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
      return (Arrays.equals(pIDs, vid.getVersionNumbers()));
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
    for(wk=0; wk<pIDs.length; wk++) {
      buf.append(pIDs[wk]);
      if(wk != (pIDs.length-1)) 
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
    if(parts.length == 0)
      throw new IllegalArgumentException
	("No version numbers found in (" + str + ")!");

    int ids[] = new int[parts.length];
    
    int wk;
    for(wk=0; wk<parts.length; wk++) {
      if(parts[wk].length() == 0) 
	throw new IllegalArgumentException
	  ("Found a missing version number component in (" + str + ")!");

      int num = 0;
      try {
	num = Integer.parseInt(parts[wk]);
      }
      catch (NumberFormatException e) {
	throw new IllegalArgumentException
	  ("Illegal version number (" + parts[wk] + ") found in (" + str + ")!");
      }

      if(num < 1) 
	throw new IllegalArgumentException
	  ("Non-positive version number (" + num + ") found in (" + str + ")!");

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
    for(wk=0; wk<pIDs.length && wk<vid.pIDs.length; wk++)
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
   * The version level numbers. 
   */
  private int[] pIDs;  
  
}



