// $Id: NodeID.java,v 1.12 2006/09/29 03:03:21 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   I D                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A unique identifier of the working version of a node under a particular view owned by 
 * a particular user. 
 */
public
class NodeID
  extends Named
  implements Comparable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  NodeID()
  {}

  /** 
   * Construct a unique identifier for the given working version of a node under the 
   * given view owned by the given user. <P> 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   */
  public
  NodeID
  ( 
   String author, 
   String view, 
   String name
  ) 
  {
    super(name);

    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");
    pView = view;

    buildCache();
  }

  /** 
   * Construct a unique working version identifier with a different node name, but 
   * in the same view owned by the same user as the given identifier.
   * 
   * @param id
   *   The source of user and view information.
   * 
   * @param name 
   *   The fully resolved node name.
   */
  public
  NodeID
  ( 
   NodeID id, 
   String name
  ) 
  {
    super(name);

    pAuthor = id.getAuthor();    
    pView   = id.getView();

    buildCache();
  }

  /** 
   * Copy constructor.
   */
  public
  NodeID
  (
   NodeID id 
  ) 
  {
    super(id.getName());

    pAuthor = id.getAuthor();    
    pView   = id.getView();

    buildCache();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the name of user which owens the working version.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor;
  }

  /** 
   * Get the name of the working area view.
   */
  public String
  getView()
  {
    return pView;
  }

  
  /** 
   * Get the node path without the last node name component. 
   */ 
  public Path
  getParent() 
  {
    Path path = new Path(pStringRep);
    return path.getParentPath();
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the working version path relative to the root production directory.
   */
  public Path
  getWorkingPath() 
  {
    return (new Path("/working" + pStringRep));
  }

  /** 
   * Get parent directory of the working version path relative to the root 
   * production directory.
   */
  public Path
  getWorkingParent() 
  {
    Path path = new Path("/working" + pStringRep);
    return path.getParentPath();
  }

  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the checked-in version path relative to the root production directory.
   * 
   * @param vid 
   *   The revision number of the checked-in version.
   */
  public Path
  getCheckedInPath
  (
   VersionID vid
  ) 
  {
    if(vid == null) 
      throw new IllegalArgumentException("The revision number cannot be (null)!");

    return (new Path("/repository/" + pName + "/" + vid)); 
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
    if((obj != null) && (obj instanceof NodeID)) {
      NodeID id = (NodeID) obj;
      return ((pHashCode == id.pHashCode) && 
	      pStringRep.equals(id.pStringRep));
    }
    return false;
  }

  /**
   * Returns a hash code value for the object.
   */
  public int 
  hashCode() 
  {
    if(pStringRep == null)
      throw new IllegalStateException(); 
    return pHashCode;
  }

  /**
   * Returns a string representation of the object. <P> 
   * 
   * @return 
   *   The full path to the node under the root working directory.
   */
  public String
  toString() 
  {
    if(pStringRep == null)
      throw new IllegalStateException(); 
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
    
    if(!(obj instanceof NodeID))
      throw new IllegalArgumentException("The object to compare was NOT a NodeID!");

    return compareTo((NodeID) obj);
  }


  /**
   * Compares this <CODE>NodeID</CODE> with the given <CODE>NodeID</CODE> for order.
   * 
   * @param id 
   *   The <CODE>NodeID</CODE> to be compared.
   */
  public int
  compareTo
  (
   NodeID id
  )
  {
    return pStringRep.compareTo(id.pStringRep);
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
    return new NodeID(this);
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
    super.toGlue(encoder);

    encoder.encode("Author", pAuthor);
    encoder.encode("View", pView);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    String author = (String) decoder.decode("Author");
    if(author == null) 
      throw new GlueException("The \"Author\" was missing!");
    pAuthor = author;

    String view = (String) decoder.decode("View");
    if(view == null) 
      throw new GlueException("The \"View\" was missing!");
    pView = view;

    buildCache();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compute the cached string representation and hash code for the file pattern.
   */
  private void
  buildCache() 
  {
    pStringRep = ("/" + pAuthor + "/" + pView + pName);
    pHashCode  = pStringRep.hashCode();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5074009750552938470L;




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The name of user which owens the working version.
   */
  private String  pAuthor;

  /** 
   * The name of the working area view.
   */
  private String  pView;


  /** 
   * The cached string representation.
   */
  private String  pStringRep;
 
  /** 
   * The cached hash code.
   */
  private int  pHashCode;
 
}

