// $Id: PanelComponentProxy.java,v 1.1 2009/12/18 08:44:26 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.math.Vector2i;

import java.util.List; 

/*------------------------------------------------------------------------------------------*/
/*   P A N E L   C O M P O N E N T   P R O X Y                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A common interface for managing the various types of panels as well as the tabbed and 
 * split panes which make up the contents of a top level top-level window of plui(1).
 */ 
public 
interface PanelComponentProxy
{
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the panel component one level higher in the hierarchy which contains this
   * component.<P>
   * 
   * @return 
   *   The component or <CODE>null</CODE> if this is the root component of the panel.
   */ 
  public PanelComponentProxy
  getParent()
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of panel components one level lower in the hierarchy contained by this
   * component.<P>
   */ 
  public int
  numChildren()
    throws PipelineException;

  /**
   * Get the panel component one level lower in the hierarchy contained by this
   * component by index.<P>
   * 
   * @param idx
   *   The index of the child to return.
   * 
   * @return 
   *   The child component. 
   */ 
  public List<PanelComponentProxy>
  getChild
  (
   int idx
  )
    throws PipelineException;
  
  /**
   * Get all of the panel components one level lower in the hierarchy contained by this
   * component.<P>
   * 
   * @return 
   *   The child components. 
   */ 
  public List<PanelComponentProxy>
  getChildren()
    throws PipelineException;


  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current size of the panel.
   */ 
  public Vector2i
  getSize() 
    throws PipelineException;

}
