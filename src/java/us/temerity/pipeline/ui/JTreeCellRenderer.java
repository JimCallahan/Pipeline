// $Id: JTreeCellRenderer.java,v 1.1 2004/05/04 17:50:47 jim Exp $

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
 * The renderer used for the {@link JTree JTree} cells.
 */ 
public
class JTreeCellRenderer
  extends DefaultTreeCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JTreeCellRenderer() 
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
   boolean selected, 
   boolean expanded, 
   boolean leaf, 
   int row, 
   boolean hasFocus
  ) 
  { 
    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    
    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) value;
    NodeTreeComp comp = (NodeTreeComp) tnode.getUserObject();

    switch(comp.getState()) {
    case Branch:
      setIcon(null);
      break;

    case Pending:
      setIcon(sTreePendingIcon);
      break;

    case OtherPending:
      setIcon(sTreeOtherPendingIcon);
      break;

    case CheckedIn:
      setIcon(sTreeCheckedInIcon);
      break;

    case Working:
      setIcon(sTreeWorkingIcon);
      break;

    case OtherWorking:
      setIcon(sTreeOtherWorkingIcon);
    }

    switch(comp.getState()) {
    case OtherPending:
    case OtherWorking:
      setForeground(new Color(0.75f, 0.75f, 0.75f));
      break;
      
    default:
      setForeground(Color.white);
      break;
    }
    
    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 584004318062788314L;


  private static Icon sTreePendingIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreePendingIcon.png"));

  private static Icon sTreeOtherPendingIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeOtherPendingIcon.png"));

  private static Icon sTreeCheckedInIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCheckedInIcon.png"));

  private static Icon sTreeWorkingIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeWorkingIcon.png"));

  private static Icon sTreeOtherWorkingIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeOtherWorkingIcon.png"));



}
