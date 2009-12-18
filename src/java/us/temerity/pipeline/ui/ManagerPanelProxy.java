// $Id: ManagerPanelProxy.java,v 1.2 2009/12/18 19:54:56 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.PipelineException;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E R   P A N E L   P R O X Y                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A component of the top level top-level windows of plui(1) which is used to control layout
 * and the leaf level node, queue and job panel types.
 */ 
public 
interface ManagerPanelProxy
  extends PanelComponentProxy
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N T E N T S                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the single leaf-level node, queue or job panel managed by this panel.
   */ 
  public PanelProxy
  getContents()
    throws PipelineException;

  /**
   * Change the type of the leaf-level node, queue or job panel managed by this panel.
   * 
   * @param ptype
   *   The panel type.
   * 
   * @return
   *   The proxy for the new panel.
   */ 
  public PanelProxy
  setContents
  (
   PanelType ptype
  ) 
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/
  /*   L A Y O U T                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Splits the current panel by inserting a new VSplit Panel as the parent of both the 
   * the current Manager Panel (below) and a newly created Manager Panel (above). <P> 
   * 
   * @return 
   *   The newly created Manager Panel initially containing an Empty Panel.
   */ 
  public ManagerPanelProxy
  addAbove()
    throws PipelineException;

  /**
   * Splits the current panel by inserting a new VSplit Panel as the parent of both the 
   * the current Manager Panel (above) and a newly created Manager Panel (below). <P> 
   * 
   * @return 
   *   The newly created Manager Panel initially containing an Empty Panel.
   */ 
  public ManagerPanelProxy
  addBelow()
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Splits the current panel by inserting a new HSplit Panel as the parent of both the 
   * the current Manager Panel (right) and a newly created Manager Panel (left). <P> 
   * 
   * @return 
   *   The newly created Manager Panel initially containing an Empty Panel.
   */ 
  public ManagerPanelProxy
  addLeft()
    throws PipelineException;

  /**
   * Splits the current panel by inserting a new HSplit Panel as the parent of both the 
   * the current Manager Panel (left) and a newly created Manager Panel (right). <P> 
   * 
   * @return 
   *   The newly created Manager Panel initially containing an Empty Panel.
   */ 
  public ManagerPanelProxy
  addRight()
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Inserts a new Tabbed Panel as the parent of the current Manager Panel (first tab). <P>
   * 
   * If the parent of this Managr Panel is already a Tabbed Panel, then a new tab is added
   * containing a new created Manager Panel instead.  The newly created Manager Panel will 
   * initially contain an Empty Panel.<P> 
   * 
   * @return 
   *   The Manager Panel which is the contents of the newly created tab.
   */ 
  public ManagerPanelProxy
  addTab() 
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Removes the current Manager Panel (and its contents) from its parent panel.<P> 
   * 
   * The consequences of this depend on the type of parent panel.  If the parent is a VSplit 
   * or HSplit Panel this operation results in the parent split panel being removed as well
   * and replaced with the contents of the other panel sibling of this Manager Panel.  If the
   * parent panel is a Tabbed Panel, then the tab containing this Manager Panel will be
   * removed. If this Manager Panel is the only panel in a top-level window, then the 
   * operation will be ignored.
   * 
   * @return 
   *   The parent panel from which this Manager Panel was removed or <CODE>null</CODE> if 
   *   this Manager Panel was not removed.
   */ 
  public PanelComponentProxy
  closePanel() 
    throws PipelineException;

  

  /*----------------------------------------------------------------------------------------*/
  /*   W O R K I N G    A R E A S                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the ownwer of the working area being displayed by this Manager Panel.
   */ 
  public void 
  getAuthor() 
    throws PipelineException;

  /**
   * Get name of the working area being displayed by this Manager Panel.
   */ 
  public void 
  getView() 
    throws PipelineException;

  /**
   * Change the working area of this Manager Panel and update.
   * 
   * @param author
   *   The owner of the working area.
   * 
   * @param view
   *   The name of the working area.
   */ 
  public void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
    throws PipelineException;

  

  /*----------------------------------------------------------------------------------------*/
  /*   U P D A T E                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the identifier of the update channel [0,9] used by the panel.
   */ 
  public void
  setUpdateChannel
  (
   int channel
  )
    throws PipelineException;

  /**
   * Get the identifier of the update channel [0,9] used by the panel.
   */ 
  public int
  getUpdateChannel()
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initiate an update from the server of the contents of all panels which share the 
   * same update channel as this panel.
   */ 
  public void
  update()
    throws PipelineException;

    
}
