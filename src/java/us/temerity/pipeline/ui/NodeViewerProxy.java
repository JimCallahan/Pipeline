// $Id: NodeViewerProxy.java,v 1.2 2009/12/18 19:55:34 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   V I E W E R   P R O X Y                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * An interface for managing a Node Viewer panel.
 */ 
public 
interface NodeViewerProxy
  extends PanelProxy
{
  /*----------------------------------------------------------------------------------------*/
  /*  C O N T E N T                                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the status of all currently displayed node networks indexed by the root node 
   * names of these networks. 
   */ 
  public Map<String,NodeStatus> 
  getNodeStatus() 
    throws PipelineException;
    

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the root nodes of the node networks currently displayed.
   */ 
  public Set<String> 
  getRoots()
    throws PipelineException;
    
  /**
   * Set the names of the root nodes to display and update the panel.
   * 
   * @param names
   *   The fully resolved names of the root nodes.
   */ 
  public void 
  setRoots
  (
   Set<String> names
  ) 
    throws PipelineException;

  /**
   * Clear the display of all nodes.
   */ 
  public void 
  clearRoots() 
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initiate an update from the server of the contents of all panels which share the 
   * same update channel as this panel.<P> 
   * 
   * @param lightweight
   *   Whether perform lightweight node status (true) or heavyweight node status (false). 
   */ 
  public void
  update
  (
   boolean lightweight
  ) 
    throws PipelineException;

  

  /*----------------------------------------------------------------------------------------*/
  /*  S E L E C T I O N                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the paths from the root node of each network to the currently selected nodes.<P> 
   * 
   * Note that the same node may be displayed multiple times but with different paths from 
   * the root node of the network.  The {@link NodePath} identifies this unique path for 
   * each node.
   */ 
  public Set<NodePath>
  getSelectedPaths() 
    throws PipelineException;

  /**
   * Get the names of the currently selected nodes.<P> 
   * 
   * Note that since this method only returns the names of the nodes and not the unique
   * {@link NodePath}, it cannot differentiate between different selection states for nodes
   * with the same names but different paths.  If any node with a given name is selected it
   * will be included in the names returned.
   */ 
  public Set<String> 
  getSelectedNames() 
    throws PipelineException;

  /**
   * Set the paths from the root node of each network to the nodes to select. <P> 
   * 
   * Note that the same node by be displayed multiple times but with different paths from 
   * the root node of the network. The {@link NodePath} identifies this unique path for 
   * each node.
   */ 
  public void 
  setSelectedPaths
  (
   Set<NodePath> paths
  ) 
    throws PipelineException;

  /**
   * Set the names of the nodes to select.<P> 
   * 
   * Note that since this method sets the names of the nodes to select and not the unique
   * {@link NodePath}, all nodes with the given names will be selected.
   */ 
  public void 
  setSelectedNames
  (
   Set<String> names
  ) 
    throws PipelineException;
    
  /**
   * Deselect all nodes.
   */ 
  public void 
  clearSelection() 
    throws PipelineException;
    


  /*----------------------------------------------------------------------------------------*/
  /*  C A M E R A                                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Move the camera to frame the given bounding box.
   */ 
  public void 
  frameBounds
  (
   BBox2d bbox
  ) 
    throws PipelineException;

  /**
   * Move the camera to frame the bounds of the currently selected nodes.
   */ 
  public void 
  frameSelection() 
    throws PipelineException;

  /**
   * Move the camera to frame all displayed nodes.
   */                  
  public void 
  frameAll() 
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current position of the camera used to display the nodes.
   */ 
  public Point2d
  getCameraPosition()
    throws PipelineException;

  /**
   * Set the current position of the camera used to display the nodes.
   */ 
  public void 
  setCameraPosition
  (
   Point2d pos
  )
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current zoom factor of the camera used to display the nodes.
   */ 
  public double
  getCameraZoom()
    throws PipelineException;

  /**
   * Set the current zoom factor of the camera used to display the nodes.
   */ 
  public void 
  setCameraZoom
  (
   double zoom
  )
    throws PipelineException;



  /*----------------------------------------------------------------------------------------*/
  /*  N O D E   D I S P L A Y                                                               */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the bounding box which contains the displayed nodes with the given paths. 
   */ 
  public BBox2d
  getNodeBounds
  (
   Set<NodePath> paths
  ) 
    throws PipelineException;

  /**
   * Get the bounding box which contains all displayed nodes.
   */ 
  public BBox2d
  getNodeBounds() 
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the horizontal/vertical spacing between nodes and redisplay.
   */ 
  public Vector2d
  getNodeSpacing() 
    throws PipelineException;
    
  /**
   * Set the horizontal/vertical spacing between nodes and redisplay.
   */ 
  public void 
  setNodeSpacing
  (
   Vector2d space
  )
    throws PipelineException;
    
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the display alignment of root nodes of each node network displayed.
   */ 
  public LayoutOrientation
  getOrientation() 
    throws PipelineException;

  /**
   * Set the display alignment of root nodes of each node network displayed.
   */ 
  public void 
  setOrientation
  (
   LayoutOrientation orient
  )
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Relayout the nodes collapsing all paths.
   */ 
  public void 
  collapseAll() 
    throws PipelineException;

  /**
   * Relayout the nodes expanding all paths.
   */ 
  public void 
  expandAll() 
    throws PipelineException;
    
  /**
   * Relayout the nodes expanding all paths to the given depth.
   */ 
  public void 
  expandToDepth
  (
   int depth
  ) 
    throws PipelineException;
    
  /**
   * Relayout the nodes expanding only the first occurance of a given node. 
   */ 
  public void 
  automaticExpand() 
    throws PipelineException;
    
  /**
   * Get whether the node identified by the given path is currently collapsed.
   */ 
  public boolean 
  getNodeCollapsed
  (
   NodePath path
  ) 
    throws PipelineException;
    
  /**
   * Set whether the node identified by the given path should be collapsed and relayout.
   */ 
  public void 
  setNodeCollapsed
  (
   NodePath path, 
   boolean collapse
  ) 
    throws PipelineException;
    

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get whether downstream node networks are being displayed. 
   */ 
  public boolean
  getShowDownstream() 
    throws PipelineException;
  
  /**
   * Set whether to display downstream node networks. 
   */ 
  public void 
  setShowDownstream
  (
   boolean show
  )
    throws PipelineException;

  
  /**
   * Get the criteria used to determine which downstream paths are displayed. 
   */ 
  public DownstreamMode
  getDownstreamMode() 
    throws PipelineException;
  
  /**
   * Set the criteria used to determine which downstream paths are displayed. 
   */ 
  public void 
  setDownstreamMode
  (
   DownstreamMode mode 
  )
    throws PipelineException;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get whether node detail hints should be displayed.
   */ 
  public boolean 
  getShowNodeDetailHints() 
    throws PipelineException;

  /**
   * Set whether node detail hints should be displayed.
   */ 
  public void
  setShowNodeDetailHints
  (
   boolean show
  ) 
    throws PipelineException;
    

  /**
   * Get whether toolset hints should be displayed.
   */ 
  public boolean 
  getShowToolsetHints() 
    throws PipelineException;

  /**
   * Set whether toolset hints should be displayed.
   */ 
  public void
  setShowToolsetHints
  (
   boolean show
  ) 
    throws PipelineException;

  
  /**
   * Get whether editor plugin hints should be displayed.
   */ 
  public boolean 
  getShowEditorHints() 
    throws PipelineException;

  /**
   * Set whether editor plugin hints should be displayed.
   */ 
  public void
  setShowEditorHints
  (
   boolean show
  ) 
    throws PipelineException;


  /**
   * Get whether user editing hints should be displayed.
   */ 
  public boolean 
  getShowEditingHints() 
    throws PipelineException;

  /**
   * Set whether user editing hints should be displayed.
   */ 
  public void
  setShowEditingHints
  (
   boolean show
  ) 
    throws PipelineException;


  /**
   * Get whether action plugin hints should be displayed.
   */ 
  public boolean 
  getShowActionHints() 
    throws PipelineException;

  /**
   * Set whether action plugin hints should be displayed.
   */ 
  public void
  setShowActionHints
  (
   boolean show
  ) 
    throws PipelineException;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  E D I T O R   P L U G I N S                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Edit/View the currently displayed node with the given name using the Editor specified
   * by the node version.
   */ 
  public void 
  edit
  (
   String name
  ) 
    throws PipelineException;
    
  /**
   * Edit/View the currently displayed node with the given name using the default Editor 
   * for file type.
   */ 
  public void 
  editWithDefault
  (
   String name
  ) 
    throws PipelineException;

  /**
   * Edit/View the currently displayed node with the given name using the default Editor 
   * for file type.
   * 
   * @param name
   *   The fully resolved name of the node.
   * 
   * @param ename
   *   The name of the Editor plugin.
   * 
   * @param evid
   *   The revision number of the Editor plugin.
   * 
   * @param evendor 
   *   The vendor of the Editor plugin.
   */ 
  public void 
  editWith
  (
   String name, 
   String ename, 
   VersionID evid, 
   String evendor
  ) 
    throws PipelineException;

}
