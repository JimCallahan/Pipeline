// $Id: VSplitPanelProxy.java,v 1.1 2009/12/18 08:44:26 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.PipelineException;

/*------------------------------------------------------------------------------------------*/
/*   V S P L I T   P A N E L   P R O X Y                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A component of the top level top-level windows of plui(1) which manages its two child 
 * components which split the total panel area vertically separated by divider.<P> 
 */ 
public 
interface VSplitPanelProxy
  extends PanelComponentProxy
{
  /**
   * Get the panel component one level lower in the hierarchy displayed above the divider.
   */ 
  public PanelComponentProxy
  getTopChild()
    throws PipelineException;

  /**
   * Get the panel component one level lower in the hierarchy displayed below the divider.
   */ 
  public PanelComponentProxy
  getBottomChild()
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of pixels from the top of the tabbed panel to the divider.
   */ 
  public int
  getDividerLocation()
    throws PipelineException;

  /**
   * Set the number of pixels from the top of the tabbed panel to the divider.<P> 
   * 
   * A value of -1 will reset the divider to its preferred location.  A value of (0) will 
   * give the entire panel area to the bottom component.  A value equal to or greater than
   * the vertical size of the panel will give entire panel area to the top component.  
   */ 
  public int
  setDividerLocation
  (
   int pixels
  )
    throws PipelineException;

}
