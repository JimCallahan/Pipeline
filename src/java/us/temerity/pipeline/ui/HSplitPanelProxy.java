// $Id: HSplitPanelProxy.java,v 1.1 2009/12/18 08:44:26 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.PipelineException;

/*------------------------------------------------------------------------------------------*/
/*   H S P L I T   P A N E L   P R O X Y                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A component of the top level top-level windows of plui(1) which manages its two child 
 * components which split the total panel area horizontally separated by divider.<P> 
 */ 
public 
interface HSplitPanelProxy
  extends PanelComponentProxy
{
  /**
   * Get the panel component one level lower in the hierarchy displayed to the left the 
   * divider.
   */ 
  public PanelComponentProxy
  getLeftChild()
    throws PipelineException;

  /**
   * Get the panel component one level lower in the hierarchy displayed to the right the 
   * divider.
   */ 
  public PanelComponentProxy
  getRightChild()
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of pixels from the left of the tabbed panel to the divider.
   */ 
  public int
  getDividerLocation()
    throws PipelineException;

  /**
   * Set the number of pixels from the left of the tabbed panel to the divider.<P> 
   * 
   * A value of -1 will reset the divider to its preferred location.  A value of (0) will 
   * give the entire panel area to the right component.  A value equal to or greater than
   * the horizontal size of the panel will give entire panel area to the left component.  
   */ 
  public int
  setDividerLocation
  (
   int pixels
  )
    throws PipelineException;

}
