// $Id: QueueEditDispatchControlsReq.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.message;

import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   E D I T   D I S P A T C H   C O N T R O L S   R E Q                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to change the criteria order for the specified dispatch controls. <P> 
 */
public 
class QueueEditDispatchControlsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param controls
   *   The dispatch controls to modify.
   */
  public
  QueueEditDispatchControlsReq
  (
    Collection<DispatchControl> controls
  )
  { 
    super();

    if(controls == null) 
      throw new IllegalArgumentException
	("The dispatch controls cannot be (null)!");
    pControls = controls;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the dispatch controls to modify.
   */
  public Collection<DispatchControl> 
  getDispatchControls()
  {
    return pControls; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1044840010541894682L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The dispatch controls to modify.
   */ 
  private Collection<DispatchControl>  pControls; 
}