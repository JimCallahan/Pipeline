// $Id: LinkVersion.java,v 1.1 2004/03/13 17:20:13 jim Exp $

package us.temerity.pipeline;

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
   * @param link [<B>in</B>]
   *   The working node link.
   * 
   * @param vid [<B>in</B>]
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
    pVersionID = new VersionID(vid);
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
    return new VersionID(pVersionID);
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



