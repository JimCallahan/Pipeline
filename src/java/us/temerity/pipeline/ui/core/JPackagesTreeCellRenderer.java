// $Id: JPackagesTreeCellRenderer.java,v 1.1 2005/01/03 06:56:24 jim Exp $

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
/*   P A C K A G E S  T R E E   C E L L   R E N D E R E R                                   */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JTree JTree} cells for the packages tree of the 
 * {@link JManageToolsetsDialog JManageToolsetsDialog}.
 */ 
public
class JPackagesTreeCellRenderer
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
  JPackagesTreeCellRenderer() 
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
    JManageToolsetsDialog.TreeData data = 
      (JManageToolsetsDialog.TreeData) tnode.getUserObject();

    setText(data.toString());

    if(isSelected) {
      setForeground(Color.yellow);
      setIcon((data.getName() != null) ? sSelectedIcon : sSpacerIcon);
    }
    else {
      setForeground(Color.white);
      setIcon((data.getName() != null) ? sNormalIcon : sSpacerIcon);
    }

    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3967969909601034040L;


  private static Icon sSpacerIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png"));

  private static Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));

}
