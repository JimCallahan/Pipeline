// $Id: PanelProxy.java,v 1.1 2009/12/18 08:44:26 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.PipelineException;

/*------------------------------------------------------------------------------------------*/
/*   P A N E L   P R O X Y                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An common interface for managing individual node, queue and job related panels within a 
 * plui(1) window.
 */ 
public 
interface PanelProxy
  extends PanelComponentProxy
{
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the type of this panel.
   */ 
  public PanelType
  getType()
    throws PipelineException;
    
  /**
   * Get the Manager Panel which containing this panel. <P> 
   * 
   * A convenience method which is equivalent to {@link #getParent getParent()} with a 
   * typecast to {@link ManagerPanelProxy}.
   */ 
  public ManagerPanelProxy
  getManager()
    throws PipelineException;

}
