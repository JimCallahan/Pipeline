// $Id: JToolsetTreeCellRenderer.java,v 1.1 2005/06/13 16:05:01 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.*;
import us.temerity.pipeline.toolset.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T   T R E E   C E L L   R E N D E R E R                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for cells for the All Toolsets tree in the JManagetToolsetsDialog. 
 */ 
public
class JToolsetTreeCellRenderer
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
  JToolsetTreeCellRenderer
  (
   JManageToolsetsDialog dialog
  ) 
  {
    super();
    
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    {    
      pLabel = new JLabel();
      pLabel.setHorizontalAlignment(JLabel.LEFT);
      pLabel.setMinimumSize(new Dimension(80, 16));
      
      add(pLabel);
    }
    
    add(Box.createRigidArea(new Dimension(16, 0)));

    {
      pExtraLabel = new JLabel();

      pExtraLabel.setHorizontalAlignment(JLabel.RIGHT);
      pExtraLabel.setHorizontalTextPosition(JLabel.LEFT);
      pExtraLabel.setIconTextGap(9);

      add(pExtraLabel);
    }

    pDialog = dialog; 
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
    ToolsetTreeData data = (ToolsetTreeData) tnode.getUserObject();
    Toolset toolset = pDialog.lookupToolset(data.getName(), data.getOsType());
      
    Icon icon = sSpacerIcon;
    String extraLabel = null;
    Icon extraIcon = null;

    if(toolset != null) {
      icon = (isSelected ? sSelectedIcon : sNormalIcon);
      if(!toolset.isFrozen()) {
	extraLabel = "(working)";
	
	extraIcon = (isSelected ? sCheckSelectedIcon : sCheckIcon);
	if(toolset.hasConflicts() || !toolset.hasPackages())
	  extraIcon = (isSelected ? sConflictSelectedIcon : sConflictIcon);
      }
    }

    Color color = (isSelected ? Color.yellow : Color.white);

    pLabel.setIcon(icon); 
    pLabel.setText(data.toString());
    pLabel.setForeground(color);

    pExtraLabel.setIcon(extraIcon);
    pExtraLabel.setText(extraLabel);
    pExtraLabel.setForeground(color);

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManageToolsetsDialog  pDialog; 

  /**
   * The main label.
   */
  private JLabel  pLabel;

  /**
   * The extra information (right justified) label.
   */
  private JLabel  pExtraLabel;



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1039524329611860227L;


  private static Icon sSpacerIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png"));


  private static Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));


  private static Icon sConflictIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ConflictIcon.png"));

  private static Icon sConflictSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ConflictSelectedIcon.png"));


  private static Icon sCheckIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CheckIcon.png"));

  private static Icon sCheckSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CheckSelectedIcon.png"));
  
}
