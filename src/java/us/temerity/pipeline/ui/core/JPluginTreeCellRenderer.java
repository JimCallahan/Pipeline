// $Id: JPluginTreeCellRenderer.java,v 1.1 2005/06/28 18:05:22 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   T R E E   C E L L   R E N D E R E R                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JTree JTree} cells containing PluginTreeData instances.
 */ 
public
class JPluginTreeCellRenderer
  extends JLabel 
  implements TreeCellRenderer 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JPluginTreeCellRenderer() 
  {
    setOpaque(true);    
    setBackground(new Color(0.45f, 0.45f, 0.45f));
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
    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) value;
    PluginTreeData data = (PluginTreeData) tnode.getUserObject();

    setText(data.toString());

    if(isSelected) {
      setForeground(Color.yellow);
      setIcon((data.getVersionID() != null) ? sSelectedIcon : sSpacerIcon);
    }
    else {
      setForeground(Color.white);
      setIcon((data.getVersionID() != null) ? sNormalIcon : sSpacerIcon);
    }

    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4002556236355126376L;


  private static final Icon sSpacerIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png"));

  private static final Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static final Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));

}
