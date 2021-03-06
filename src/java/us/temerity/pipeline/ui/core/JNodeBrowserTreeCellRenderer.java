// $Id: JNodeBrowserTreeCellRenderer.java,v 1.4 2009/10/06 17:45:54 jlee Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   B R O W S E R   T R E E   C E L L   R E N D E R E R                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JTree JTree} cells in the 
 * {@link JNodeBrowserPanel JNodeBrowserPanel}.
 */ 
public
class JNodeBrowserTreeCellRenderer
  extends DefaultTreeCellRenderer
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
    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) value;
    TreePath tpath = new TreePath(tnode.getPath());
    NodeTreeComp comp = (NodeTreeComp) tnode.getUserObject();
    if(comp != null) {
      boolean selected = pBrowser.isSelected(tpath);
      int idx = comp.getState().ordinal();

      String suffix = "";
      {
        UserPrefs prefs = UserPrefs.getInstance();

        if(prefs.getDisplayPrimarySuffix() && 
           comp.getPrimarySuffix() != null)
          suffix = "." + comp.getPrimarySuffix();
      }

      setText(comp.getName() + suffix); 
      setIcon(selected ? sSelectedIcons[idx] : sIcons[idx]);
      
      Color normal = comp.isHidden() ? new Color(1.0f, 0.5f, 0.0f): Color.white;

      switch(comp.getState()) {
      case Branch:
	setForeground(normal);
	break;
	
      case WorkingOtherCheckedInNone:
	setForeground(new Color(0.75f, 0.75f, 0.75f));
	break;
	
      default:
	setForeground(selected ? Color.yellow : normal);
	break;
      }
    }
    else {
      setText("(hidden)");
      setIcon(null);
      setForeground(new Color(0.75f, 0.75f, 0.75f));
    }
    
    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3097835577102260282L;

  private static final Icon sIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon1.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon2.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon3.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon4.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon5.png"))
  };

  private static final Icon sSelectedIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeCellSpacerIcon.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon1Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon2Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon3Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon4Selected.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon5Selected.png"))
  };


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent node browser.
   */ 
  private JNodeBrowserPanel  pBrowser;

}
