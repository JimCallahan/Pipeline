// $Id: JFancyTree.java,v 1.1 2004/05/11 19:17:03 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   L A Y E R E D   T R E E                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link JTree JTree} which properly draws the horizontal/vertical grouping lines.
 */ 
public 
class JFancyTree
  extends JTree
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new tree component.
   * 
   * @param model
   *   The tree model.
   */
  public 
  JFancyTree
  (
   TreeModel model
  ) 
  {
    super(model);
      
    setShowsRootHandles(true);
    setRootVisible(false);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   O V E R R I D E S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Performs any custom painting.
   */
  protected void 
  paintComponent
  (
   Graphics graphics
  )
  {
    super.paintComponent(graphics);

    Graphics2D gfx = (Graphics2D) graphics.create(); 
    gfx.setColor(new Color(0.6f, 0.6f, 0.6f));

    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) getModel().getRoot();
    Enumeration e = tnode.children();
    if(e != null) {
      while(e.hasMoreElements()) {
	DefaultMutableTreeNode child = (DefaultMutableTreeNode) e.nextElement(); 
	paintLines(gfx, child, null);
      }
    }
  }
   
  private Integer  
  paintLines
  (
   Graphics2D gfx, 
   DefaultMutableTreeNode tnode, 
   Integer px
  ) 
  {
    TreePath tpath = new TreePath(tnode.getPath()); 
    if(!isVisible(tpath)) 
      return null;

    Rectangle r = getPathBounds(tpath);

    int y = r.y + (r.height / 2);
    if(px != null) {
      gfx.drawLine(px, y, r.x-5, y);
    }

    Integer my = null;
    int npx = r.x + 5;
    Enumeration e = tnode.children();
    if(e != null) {
      while(e.hasMoreElements()) {
	DefaultMutableTreeNode child = (DefaultMutableTreeNode) e.nextElement(); 
	Integer cy = paintLines(gfx, child, npx);
	if(cy != null) {
	  if(my == null) 
	    my = cy;
	  else 
	    my = Math.max(my, cy);
	}
      }
    }

    if(my != null) 
      gfx.drawLine(npx, r.y+(r.height / 2)+9, npx, my);

    return y;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -6795929634751231730L;

  
}
