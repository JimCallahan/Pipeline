package us.temerity.pipeline.builder.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import us.temerity.pipeline.laf.LookAndFeelLoader;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   T R E E   C E L L   R E N D E R E R                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JTree JTree} cells in the 
 * {@link JBuilderTopPanel}.
 */ 
public
class JBuilderTreeCellRenderer
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
  JBuilderTreeCellRenderer() 
  {
    setOpaque(true);    
    setBackground(new Color(0.45f, 0.45f, 0.45f));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

  public Component 
  getTreeCellRendererComponent
  (
   @SuppressWarnings("unused")
   JTree tree, 
   Object value, 
   boolean isSelected,
   @SuppressWarnings("unused")
   boolean isExpanded, 
   boolean isLeaf, 
   @SuppressWarnings("unused")
   int row, 
   @SuppressWarnings("unused")
   boolean hasFocus
  ) 
  { 
    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) value;
    BuilderTreeNodeInfo info = (BuilderTreeNodeInfo) tnode.getUserObject();
    boolean isActive = info.isActive();

    setText(info.getText());
    
    //boolean realLeaf = info.isLeaf();

    if(isLeaf) {
      if (isActive) {
	if (isSelected) {
	  setForeground(new Color(100, 255, 100));
	  setIcon(sActiveSelectedIcon);
	}
	else {
	  setForeground(Color.green);	  
	  setIcon(sActiveIcon);
	}
      }
      else {
	if(isSelected) {
	  setForeground(Color.yellow);
	  setIcon(sSelectedIcon);
	}
	else {
	  setForeground(Color.white);
	  setIcon(sNormalIcon);
	}	
      }
    }
    else {
      setForeground(Color.white);
      setIcon(sSpacerIcon);
    }

    return this;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7043081923099981338L;
  
  private static final Icon sSpacerIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png"));

  private static final Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static final Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));
  
  private static final Icon sActiveIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellActiveIcon.png"));
  
  private static final Icon sActiveSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellActiveSelectedIcon.png"));
}
