// $Id: LinkVersion.java,v 1.7 2005/01/03 00:05:31 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   V E R S I O N                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about the relationship between a checked-in version of a node 
 * (<CODE>NodVersion</CODE>) and the upstream checked-in node to which it is linked. <P> 
 */ 
public
class LinkVersion
  extends LinkCommon
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  LinkVersion() 
  {
    super();
  }

  /**
   * Construct from a working node link.
   * 
   * @param link 
   *   The working node link.
   * 
   * @param vid 
   *   The the revision number of the upstream node version upon which the downstream 
   *   node depends.
   */ 
  public 
  LinkVersion
  (
   LinkMod link, 
   VersionID vid 
  ) 
  {
    super(link);

    if(vid == null) 
      throw new IllegalArgumentException
	("The node version ID cannot be (null)!");
    pVersionID = vid;
  }

  /**
   * Copy constructor. 
   */ 
  public 
  LinkVersion
  (
   LinkVersion link   
  ) 
  {
    super(link);

    pVersionID = link.getVersionID();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the revision number of the specific <CODE>NodeVersion</CODE> of the upstream node 
   * upon which the downstream node depends.
   */ 
  public VersionID
  getVersionID()
  {
    return pVersionID;
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
    if(obj != null) {
      if(obj instanceof LinkVersion) {
	LinkVersion vsn = (LinkVersion) obj;
	if(super.equals(obj)) 
	  return pVersionID.equals(vsn.pVersionID);
	return false; 
      }
      else if(obj instanceof LinkMod) {
	return super.equals(obj);
      }
    }
    return false;
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

    encoder.encode("VersionID", pVersionID);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    {
      VersionID vid = (VersionID) decoder.decode("VersionID");
      if(vid == null) 
	throw new GlueException("The \"VersionID\" was missing!");
      pVersionID = vid;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6715275129958262248L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision number of the specific <CODE>NodeVersion</CODE> of the upstream node 
   * upon which the downstream node depends.
   */ 
  private VersionID  pVersionID;  

}



