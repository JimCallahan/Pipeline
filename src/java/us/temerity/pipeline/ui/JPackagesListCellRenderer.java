// $Id: JPackagesListCellRenderer.java,v 1.1 2004/05/29 06:38:43 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.toolset.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E S   L I S T   C E L L   R E N D E R E R                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for the {@link JList JList} cells.
 */ 
public
class JPackagesListCellRenderer
  extends JPanel 
  implements ListCellRenderer 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JPackagesListCellRenderer() 
  {
    super();
    
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    
    pPackageLabel = new JLabel();
    pPackageLabel.setHorizontalAlignment(JLabel.LEFT);
    add(pPackageLabel);
    
    add(Box.createRigidArea(new Dimension(8, 0)));
    add(Box.createHorizontalGlue());
    
    pVersionLabel = new JLabel();
    add(pVersionLabel);
    
    add(Box.createRigidArea(new Dimension(8, 0)));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generates the component to be displayed for JList cells.
   */ 
  public Component 
  getListCellRendererComponent
  (
   JList list,
   Object value,
   int index,
   boolean isSelected,
   boolean cellHasFocus
  )
  {
    PackageVersion pkg = (PackageVersion) value;

    pPackageLabel.setText(pkg.getName());
    pVersionLabel.setText("(v" + pkg.getVersionID().toString() + ")");

    if(isSelected) {
      pPackageLabel.setForeground(Color.yellow);
      pPackageLabel.setIcon(sSelectedIcon);
    }
    else {
      pPackageLabel.setForeground(Color.white);
      pPackageLabel.setIcon(sNormalIcon); 
    }

    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5009178842357406649L;


  private static Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The package label.
   */
  private JLabel  pPackageLabel;

  /**
   * The version label.
   */
  private JLabel  pVersionLabel;
  
}
