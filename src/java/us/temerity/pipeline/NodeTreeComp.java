// $Id: NodeTreeComp.java,v 1.1 2004/05/02 12:15:34 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   T R E E   C O M P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */
public
class NodeTreeComp
  extends NodeTreeCommon
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct the root path component.
   */ 
  public 
  NodeTreeComp() 
  {
    super();
  }  

  /**
   * Construct a new node path component based on the given node path component entry.
   * 
   * @param entry
   *   The node path component entry.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */ 
  public 
  NodeTreeComp
  (
   NodeTreeEntry entry, 
   String author, 
   String view 
  ) 
  {
    super(entry.getName());
    
    if(entry.isLeaf()) {
      pIsLeaf = true;

      pIsCheckedIn = entry.isCheckedIn();
      pIsWorking   = entry.hasWorking();
      pIsLocal     = entry.hasWorking(author, view);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Does there exist at least one working node version corresponding to this component?
   */ 
  public boolean
  isWorking() 
  {
    assert(pIsLeaf);
    return pIsWorking;
  }
  
  /**
   * Does there exist a working node version within the local view?
   */ 
  public boolean
  isLocal()
  {
    assert(pIsLeaf);
    return pIsLocal;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4350033841849318790L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does there exist at least one working node version corresponding to this component?
   */   
  private boolean pIsWorking;

  /**
   * Does there exist a working node version within the local view?
   */    
  private boolean pIsLocal;

}
