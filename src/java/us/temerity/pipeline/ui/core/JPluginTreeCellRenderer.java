// $Id: JPluginTreeCellRenderer.java,v 1.2 2006/05/07 21:30:14 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   T R E E   C E L L   R E N D E R E R                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JTree JTree} cells containing PluginTreeData instances.
 */ 
public
class JPluginTreeCellRenderer
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
  JPluginTreeCellRenderer() 
  {
    super();

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    pLabel = new JLabel();
    pLabel.setHorizontalAlignment(JLabel.LEFT);
    add(pLabel);

    add(Box.createRigidArea(new Dimension(20, 0)));
    add(Box.createHorizontalGlue());
    
    pIconLabels = new JLabel[3];
    for(OsType os : OsType.all()) {
      JLabel label = new JLabel();
      pIconLabels[os.ordinal()] = label;
      label.setToolTipText(UIFactory.formatToolTip
			   ("The " + os.toTitle() + " operating system."));  
    }
    
    add(pIconLabels[OsType.Unix.ordinal()]);
    
    add(Box.createRigidArea(new Dimension(6, 0)));
    
    add(pIconLabels[OsType.MacOS.ordinal()]);
    
    add(Box.createRigidArea(new Dimension(6, 0)));
    
    add(pIconLabels[OsType.Windows.ordinal()]);
    
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
    PluginTreeData data = (PluginTreeData) tnode.getUserObject();
    
    pLabel.setText(data.toString());
    if(isSelected) {
      pLabel.setForeground(Color.yellow);
      pLabel.setIcon((data.getVersionID() != null) ? sSelectedIcon : sSpacerIcon);
    }
    else {
      pLabel.setForeground(Color.white);
      pLabel.setIcon((data.getVersionID() != null) ? sNormalIcon : sSpacerIcon);
    }

    SortedSet<OsType> supports = data.getSupports();
    if(supports != null) {
      for(OsType os : OsType.all()) {
	int idx = os.ordinal();

	Icon icon = sUnsupportedIcons[idx];
	if(supports.contains(os)) 
	  icon = isSelected ? sSelectedIcons[idx] : sSupportedIcons[idx];
	pIconLabels[os.ordinal()].setIcon(icon);
      }
    }
    else {
      for(OsType os : OsType.all()) 
	pIconLabels[os.ordinal()].setIcon(null);
    }

    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4002556236355126376L;


  private static final Icon sSpacerIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png"));

  private static final Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static final Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));


  private static final Icon sSupportedIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("UnixSupportedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WindowsSupportedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("MacOSSupportedIcon.png"))
  };

  private static final Icon sUnsupportedIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("UnixUnsupportedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WindowsUnsupportedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("MacOSUnsupportedIcon.png"))
  };

  private static final Icon sSelectedIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("UnixSelectedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WindowsSelectedIcon.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("MacOSSelectedIcon.png"))
  };


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The text label. 
   */ 
  private JLabel  pLabel;

  /**
   * The OS icon labels. 
   */ 
  private JLabel  pIconLabels[];

}
