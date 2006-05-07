// $Id: JLayoutTreeCellRenderer.java,v 1.4 2006/05/07 21:30:14 jim Exp $

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
/*   L A Y O U T   T R E E   C E L L   R E N D E R E R                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JTree JTree} cells for 
 * {@link JBaseLayoutDialog JBaseLayoutDialog}.
 */ 
public
class JLayoutTreeCellRenderer
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
  JLayoutTreeCellRenderer() 
  {
    super();
    
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    
    pLabel = new JLabel();
    pLabel.setHorizontalAlignment(JLabel.LEFT);
    add(pLabel);
    
    add(Box.createRigidArea(new Dimension(32, 0)));
    
    pExtraLabel = new JLabel();
    pExtraLabel.setHorizontalTextPosition(JLabel.LEFT);
    add(pExtraLabel);
    
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
    JSaveLayoutDialog.TreeData data = (JSaveLayoutDialog.TreeData) tnode.getUserObject();

    pLabel.setText(data.toString());

    if(data.getName() != null) {
      if(isSelected) {
	pLabel.setForeground(Color.yellow);
	pLabel.setIcon(sSelectedIcon);
      }
      else {
	pLabel.setForeground(Color.white);
	pLabel.setIcon(sNormalIcon);
      }	

      if(data.getPath().equals(UIMaster.getInstance().getDefaultLayoutPath())) 
	pExtraLabel.setText("(default)");
      else 
	pExtraLabel.setText(null);
    }
    else {
      pLabel.setForeground(isSelected ? Color.yellow : Color.white);
      pLabel.setIcon(sSpacerIcon);
      pExtraLabel.setText(null);
    }

    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5085958176505086517L;


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
   * The main label.
   */
  protected JLabel  pLabel;

  /**
   * The extra information (right justified) label.
   */
  protected JLabel  pExtraLabel;

}
