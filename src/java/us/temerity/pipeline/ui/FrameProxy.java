// $Id: FrameProxy.java,v 1.1 2009/12/18 08:44:26 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.math.Vector2i;
import us.temerity.pipeline.math.Point2i;

/*------------------------------------------------------------------------------------------*/
/*   F R A M E   P R O X Y                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An interface for managing a top-level window of plui(1).
 */ 
public 
interface FrameProxy 
{
  /*----------------------------------------------------------------------------------------*/
  /*   W I N D O W S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the window. 
   */ 
  public String
  getWindowName()
    throws PipelineException;

  /** 
   * Set the name of the window. 
   */ 
  public void 
  setWindowName
  (
   String name
  )
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current size (in pixels) of the window.
   */ 
  public Vector2i
  getSize() 
    throws PipelineException;

  /**
   * Set the current size (in pixels) of the window.
   */ 
  public void 
  setSize
  (
   Vector2i size
  ) 
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current location (in pixels) of the window's top-left corner.
   */ 
  public Point2i
  getLocation() 
    throws PipelineException;

  /**
   * Set the current location (in pixels) of the window's top-left corner.
   */ 
  public void 
  setLocation
  (
   Point2i loc
  ) 
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether this window is the main plui(1) window. 
   */ 
  public boolean
  isMainWindow()
    throws PipelineException; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove all panels and close the window. <P> 
   * 
   * If this is the main plui(1) window, this is equivalient to calling 
   * {@link UIClient#quit UIClient.quit()}.
   */ 
  public void
  closeWindow()
    throws PipelineException;



  /*----------------------------------------------------------------------------------------*/
  /*   P A N E L S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the root of the component hierchy contained by the window.
   */ 
  public PanelComponentProxy
  getRootPanel()
    throws PipelineException;

}
