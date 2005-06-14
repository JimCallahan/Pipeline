// $Id: JPackageTreeCellRenderer.java,v 1.2 2005/06/14 13:38:33 jim Exp $

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
/*   P A C K A G E   T R E E   C E L L   R E N D E R E R                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for cells for the All Packages tree in the JManagetToolsetsDialog. 
 */ 
public
class JPackageTreeCellRenderer
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
  JPackageTreeCellRenderer() 
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
    PackageTreeData data = (PackageTreeData) tnode.getUserObject();

    setText(data.toString());

    if(data.isPackage()) 
      setIcon(isSelected ? sSelectedIcon : sNormalIcon);
    else 
      setIcon(sSpacerIcon);

    setForeground(isSelected ? Color.yellow : Color.white);

    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5581210060414280203L;


  private static final Icon sSpacerIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png"));

  private static final Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static final Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));

}
