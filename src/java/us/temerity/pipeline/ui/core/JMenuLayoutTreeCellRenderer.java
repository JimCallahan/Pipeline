// $Id: JMenuLayoutTreeCellRenderer.java,v 1.1 2005/01/05 09:44:31 jim Exp $

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
/*   M E N U   L A Y O U T   T R E E   C E L L   R E N D E R E R                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JTree JTree} cells for the menu layout tree of the 
 * {@link JBaseManagePluginsDialog JBaseManagePluginsDialog}.
 */ 
public
class JMenuLayoutTreeCellRenderer
  extends JPanel 
  implements TreeCellRenderer 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JMenuLayoutTreeCellRenderer() 
  {
    super();
    
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    
    pLabel = new JLabel();
    pLabel.setHorizontalAlignment(JLabel.LEFT);
    add(pLabel);
    
    add(Box.createRigidArea(new Dimension(32, 0)));
    
    pPluginLabel = new JLabel();
    pPluginLabel.setHorizontalTextPosition(JLabel.LEFT);
    add(pPluginLabel);
    
    add(Box.createRigidArea(new Dimension(5, 0)));
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
    JBaseManagePluginsDialog.MenuLayoutData data = 
      (JBaseManagePluginsDialog.MenuLayoutData) tnode.getUserObject();

    pLabel.setText(data.toString());

    if(data.isItem()) {
      if(isSelected) {
	pLabel.setForeground(Color.yellow);
	pLabel.setIcon(sSelectedIcon);
      }
      else {
	pLabel.setForeground(Color.white);
	pLabel.setIcon(sNormalIcon);
      }	
      
      if(data.getName() != null) 
	pPluginLabel.setText(data.getName() + " (v" + data.getVersionID() + ")");      
      else 
	pPluginLabel.setText("(none)");
    }
    else {
      pLabel.setForeground(isSelected ? Color.yellow : Color.white);
      pLabel.setIcon(sSpacerIcon);
      pPluginLabel.setText(null);
    }

    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2573743079809572059L;


  private static Icon sSpacerIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png"));

  private static Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The menu label.
   */
  private JLabel  pLabel;

  /**
   * The plugin version label.
   */
  private JLabel  pPluginLabel;

}
