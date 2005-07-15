// $Id: JPluginMenuLayoutTreeCellRenderer.java,v 1.5 2005/07/15 02:16:46 jim Exp $

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
/*   P L U G I N   M E N U   L A Y O U T   T R E E   C E L L   R E N D E R E R              */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JTree JTree} cells containing 
 * {@link PluginMenuLayout PluginMenuLayout} data. 
 */ 
public
class JPluginMenuLayoutTreeCellRenderer
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
  JPluginMenuLayoutTreeCellRenderer
  (
   JBaseToolsetPluginsPanel parent
  ) 
  {
    super();
    
    pParent = parent;

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    
    pLabel = new JLabel();
    pLabel.setHorizontalAlignment(JLabel.LEFT);
    add(pLabel);
    
    add(Box.createRigidArea(new Dimension(60, 0)));
    add(Box.createHorizontalGlue());
    
    pPluginLabel = new JLabel();
    pPluginLabel.setHorizontalTextPosition(JLabel.RIGHT);
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
    PluginMenuLayout pml = (PluginMenuLayout) tnode.getUserObject();

    pLabel.setText(pml.getTitle()); 

    Color itemColor = Color.white;
    if(pml.isMenuItem()) {
      pLabel.setIcon(isSelected ? sSelectedIcon : sNormalIcon);
      pPluginLabel.setText(pml.getName() + " (v" + pml.getVersionID() + ")");      
      
      if(!pParent.isPluginSupported(pml.getName(), pml.getVersionID()))
	itemColor = new Color(0.8f, 0.8f, 0.8f);
    }
    else {
      pLabel.setIcon(sSpacerIcon);
      pPluginLabel.setText(null);
    }

    pPluginLabel.setForeground(itemColor);

    if(tnode == pParent.getDragMenuLayoutNode())
      pLabel.setForeground(Color.cyan);
    else 
      pLabel.setForeground(isSelected ? Color.yellow : itemColor);

    int depth = tnode.getPath().length;
    Dimension size = new Dimension(500-23*depth, 23);
    setMinimumSize(size);
    setPreferredSize(size); 
    setMaximumSize(size); 

    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3166571185807451489L;


  private static final Icon sSpacerIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png"));

  private static final Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static final Icon sSelectedIcon = 
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

  /**
   * The parent panel.
   */ 
  private JBaseToolsetPluginsPanel  pParent;

}
