// $Id: JNodeBrowserTreeCellRenderer.java,v 1.3 2004/05/29 06:38:06 jim Exp $

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
/*   N O D E   B R O W S E R   T R E E   C E L L   R E N D E R E R                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JTree JTree} cells in the 
 * {@link JNodeBrowserPanel JNodeBrowserPanel}.
 */ 
public
class JNodeBrowserTreeCellRenderer
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
  JNodeBrowserTreeCellRenderer
  (
   JNodeBrowserPanel browser
  ) 
  {
    pBrowser = browser;
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
    //super.getTreeCellRendererComponent(tree, value, 
    //isSelected, isExpanded, isLeaf, row, hasFocus);

    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) value;
    TreePath tpath = new TreePath(tnode.getPath());
    NodeTreeComp comp = (NodeTreeComp) tnode.getUserObject();

    setText(comp.getName());

    boolean selected = pBrowser.isSelected(tpath);

    switch(comp.getState()) {
    case Branch:
      setIcon(sSpacerIcon);
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
    case Branch:
      setForeground(Color.white);
      break;

    case OtherPending:
    case OtherWorking:
      setForeground(new Color(0.75f, 0.75f, 0.75f));
      break;
      
    default:
      setForeground(selected ? Color.yellow : Color.white);
      break;
    }
    
    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3097835577102260282L;


  private static Icon sSpacerIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png"));

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



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent node browser.
   */ 
  private JNodeBrowserPanel  pBrowser;

}
