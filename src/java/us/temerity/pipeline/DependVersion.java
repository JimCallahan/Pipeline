// $Id: DependVersion.java,v 1.2 2004/03/07 02:38:26 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   D E P E N D   V E R S I O N                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about the dependency relationship between a checked-in version of a node 
 * (<CODE>NodeVersion</CODE>) and the upstream checked-in version of the node upon which 
 * it depends. <P> 
 *  
 * @see NodeVersion 
 */ 
public
class DependVersion
  extends DependCommon
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  DependVersion() 
  {
    super();
  }

  /**
   * Construct from a working version dependency.
   * 
   * @param dep [<B>in</B>]
   *   The working version dependency.
   * 
   * @param vid [<B>in</B>]
   *   The the revision number of the upstream node version upon which the downstream 
   *   node depends.
   */ 
  public 
  DependVersion
  (
   DependMod dep, 
   VersionID vid 
  ) 
  {
    super(dep);

    if(vid == null) 
      throw new IllegalArgumentException
	("The node version ID cannot be (null)!");
    pVersionID = new VersionID(vid);
  }

  /**
   * Copy constructor. 
   */ 
  public 
  DependVersion
  (
   DependVersion dep   
  ) 
  {
    super(dep);

    pVersionID = dep.getVersionID();
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

  private static final long serialVersionUID = -444830622027398832L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision number of the specific <CODE>NodeVersion</CODE> of the upstream node 
   * upon which the downstream node depends.
   */ 
  private VersionID  pVersionID;  

}



