// $Id: JSaveLayoutTreeCellRenderer.java,v 1.1 2004/05/11 19:17:03 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   T R E E   C E L L   R E N D E R E R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JTree JTree} cells for 
 * {@link JSaveLayoutDialog JSaveLayoutDialog}.
 */ 
public
class JSaveLayoutTreeCellRenderer
  extends DefaultTreeCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JSaveLayoutTreeCellRenderer() 
  {
    super();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generates the component which renders the cell.. 
   */ 
  public Component 
  getTreeCellRendererComponent
  (
   JTree tree, 
   Object value, 
   boolean isSelected, 
   boolean isExpanded, 
   boolean isLeaf, 
   int row, 
   boolean hasFocus
  ) 
  { 
    super.getTreeCellRendererComponent(tree, value, 
				       isSelected, isExpanded, isLeaf, row, hasFocus);
    
    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) value;
    JSaveLayoutDialog.TreeData data = (JSaveLayoutDialog.TreeData) tnode.getUserObject();

    if(data.getName() != null) {
      if(isSelected) {
	setForeground(Color.yellow);
	setIcon(sSelectedIcon);
      }
      else {
	setForeground(Color.white);
	setIcon(sNormalIcon);
      }	
    }
    else {
      setForeground(isSelected ? Color.yellow : Color.white);
      setIcon(sSpacerIcon);
    }

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5085958176505086517L;


  private static Icon sSpacerIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png"));

  private static Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));

}
