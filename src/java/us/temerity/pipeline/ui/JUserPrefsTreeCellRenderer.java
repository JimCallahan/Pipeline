// $Id: JUserPrefsTreeCellRenderer.java,v 1.1 2004/05/13 02:37:41 jim Exp $

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
/*   U S E R   P R E F S   T R E E   C E L L   R E N D E R E R                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JTree JTree} cells in the 
 * {@link UserPrefsDialog UserPrefsDialog}.
 */ 
public
class JUserPrefsTreeCellRenderer
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
  JUserPrefsTreeCellRenderer() 
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
    String text = (String) tnode.getUserObject();

    setText(text);

    if(isLeaf) {
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
      setForeground(Color.white);
      setIcon(sSpacerIcon);
    }

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = -5085958176505086517L;


  private static Icon sSpacerIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png"));

  private static Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));

}
