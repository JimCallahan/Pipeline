// $Id: VersionID.java,v 1.10 2004/05/21 18:07:30 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

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
 *   <I>Mega</I>.<I>Major</I>.<I>Minor</I>.<I>Micro</I> <BR>
 * </DIV> <BR> 
 * 
 * The component numbers are arranged left-to-right from most-to-least imporant.  The intial
 * revision number of a node is always (1.0.0.0).  The importance of subsequent revisions
 * can be determined by which component have been inremented as compared to the previous 
 * revision.
 */
public
class VersionID 
  implements Comparable, Cloneable, Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct an initial revision number (1.0.0.0).
   */ 
  public 
  VersionID() 
  {
    buildCache();
  }

  /**
   * Construct from revision number components. 
   * 
   * @param comps 
   *   The revision number components.
   */ 
  public 
  VersionID
  (
   int[] comps
  ) 
  {
    if(comps == null) 
      throw new IllegalArgumentException
	("The revision number components cannot be (null)!");

    if(comps.length != 4) 
      throw new IllegalArgumentException
	("There must be exactly (4) revision number components!");
      
    int wk;
    for(wk=0; wk<4; wk++) {
      if(comps[wk] < 0) 
	throw new IllegalArgumentException
	  ("Found a negative version number component (" + comps[wk] + ")!");

      if((wk == 0) && (comps[wk] < 1)) 
	throw new IllegalArgumentException
	  ("The first version number component (" + comps[wk] + ") must be positive!");
    }

    pIDs = comps.clone();

    buildCache();
  }

  /**
   * Construct from the given string representation. 
   * 
   * @param str 
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
   * Construct a new revision number by incrementing the specified level of the 
   * given revision number.
   * 
   * @param vid 
   *   The revision number to increment.
   * 
   * @param level  
   *   The revision number component level to increment.
   */ 
  public 
  VersionID 
  (
   VersionID vid, 
   Level level
  ) 
  {
    if(vid == null)
      throw new IllegalArgumentException
	("The revision number cannot be (null)!");

    pIDs = vid.pIDs.clone();

    int idx = level.ordinal();
    pIDs[idx]++;

    int wk;
    for(wk=idx+1; wk<4; wk++) 
      pIDs[wk] = 0;

    buildCache();
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

    buildCache();
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
      return ((pHashCode == vid.pHashCode) && 
	      Arrays.equals(pIDs, vid.pIDs));
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
   * Returns a string representation of the object. 
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
    
    if(!(obj instanceof VersionID))
      throw new IllegalArgumentException("The object to compare was NOT a VersionID!");

    return compareTo((VersionID) obj);
  }


  /**
   * Compares this <CODE>VersionID</CODE> with the given <CODE>VersionID</CODE> for order.
   * 
   * @param vid 
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
  {
    try {
      return super.clone();
    }
    catch(CloneNotSupportedException ex) {
      assert(false);
      return null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("RevisionNumber", toString());
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    String vstr = (String) decoder.decode("RevisionNumber");
    if(vstr == null) 
      throw new GlueException("The \"Revision\" was missing!");
    fromString(vstr);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P U B L I C   C L A S S E S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Identifiers for the level components of a revision number.
   */
  public
  enum Level
  {  
    /**
     * The first component level of the revision number. <P> 
     * 
     * Examples of incrementing this component level: 
     * <DIV style="margin-left: 40px;">
     *    1.0.0.0 to 2.0.0.0 <BR>
     *    1.3.2.5 to 2.0.0.0 <BR>
     *    2.4.0.2 to 3.0.0.0 <BR>
     * </DIV>
     */
    Mega,
    
    /**
     * The second component level of the revision number. <P> 
     * 
     * Examples of incrementing this component level: 
     * <DIV style="margin-left: 40px;">
     *    1.0.0.0 to 1.1.0.0 <BR>
     *    1.3.2.5 to 1.4.0.0 <BR>
     *    2.4.0.2 to 2.5.0.0 <BR>
     * </DIV>
     */
    Major,
    
    /**
     * The third component level of the revision number. <P> 
     * 
     * Examples of incrementing this component level: 
     * <DIV style="margin-left: 40px;">
     *    1.0.0.0 to 1.0.1.0 <BR>
     *    1.3.2.5 to 1.3.3.0 <BR>
     *    2.4.0.2 to 2.4.1.0 <BR>
     * </DIV>
     */
    Minor,
    
    /**
     * The fourth component level of the revision number. <P> 
     * 
     * Examples of incrementing this component level: 
     * <DIV style="margin-left: 40px;">
     *    1.0.0.0 to 1.0.0.1 <BR>
     *    1.3.2.5 to 1.3.2.6 <BR>
     *    2.4.0.2 to 2.4.0.3 <BR>
     * </DIV>
     */
    Micro;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convertion from the given string representation. 
   * 
   * @param str 
   *   The initial version ID encoded as a <CODE>String</CODE>.
   */ 
  private void 
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
	("Found the wrong number (" + parts.length + ") of revision number components " + 
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
	  ("Negative version number component (" + num + ") found in (" + str + ")!");

      if((wk == 0) && (num < 1)) 
	throw new IllegalArgumentException
	  ("The first version number component (" + num + ") must be positive!");
      
      ids[wk] = num;
    }

    pIDs = ids;

    buildCache();
  }

  /**
   * Compute the cached string representation and hash code for the file pattern.
   */
  private void
  buildCache() 
  {
    {
      StringBuffer buf = new StringBuffer();
      
      int wk;
      for(wk=0; wk<4; wk++) {
	buf.append(pIDs[wk]);
	if(wk < 3) 
	  buf.append(".");
      }
      
      pStringRep = buf.toString();
    }

    pHashCode = pStringRep.hashCode();
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
  

  /** 
   * The cached string representation.
   */
  private String  pStringRep;
 
  /** 
   * The cached hash code.
   */
  private int  pHashCode;
}



