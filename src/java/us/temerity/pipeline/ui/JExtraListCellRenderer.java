// $Id: JExtraListCellRenderer.java,v 1.1 2004/06/03 09:30:32 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.toolset.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   E X T R A   L I S T   C E L L   R E N D E R E R                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for {@link JList JList} cell which provides an extra right justified
 * label for displaying additional information per cell.
 */ 
public
class JExtraListCellRenderer
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
  JExtraListCellRenderer() 
  {
    super();
    
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    
    pLabel = new JLabel();
    pLabel.setHorizontalAlignment(JLabel.LEFT);
    add(pLabel);
    
    add(Box.createRigidArea(new Dimension(8, 0)));
    add(Box.createHorizontalGlue());
    
    pExtraLabel = new JLabel();
    pExtraLabel.setHorizontalTextPosition(JLabel.LEFT);
    pExtraLabel.setIconTextGap(9);
    add(pExtraLabel);
    
    add(Box.createRigidArea(new Dimension(5, 0)));
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
    if(isSelected) {
      pLabel.setForeground(Color.yellow);
      pLabel.setIcon(sSelectedIcon);

      pExtraLabel.setForeground(Color.yellow);
    }
    else {
      pLabel.setForeground(Color.white);
      pLabel.setIcon(sNormalIcon); 

      pExtraLabel.setForeground(Color.white);
    }

    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6610744284537860590L;


  private static Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));


  protected static Icon sConflictIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ConflictIcon.png"));

  protected static Icon sConflictSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ConflictSelectedIcon.png"));


  protected static Icon sCheckIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CheckIcon.png"));

  protected static Icon sCheckSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CheckSelectedIcon.png"));


  protected static Icon sBlankIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("BlankIcon.png"));


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
