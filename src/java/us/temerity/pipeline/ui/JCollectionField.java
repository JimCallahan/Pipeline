// $Id: JCollectionField.java,v 1.1 2004/05/16 19:21:38 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   C O L L E C T I O N   F I E L D                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents a {@link Collection Collection}.
 */
public 
class JCollectionField
  extends JPanel
  implements MouseListener, ActionListener
{
  public 
  JCollectionField
  (
   Collection<String> values
  )
  {
    super();  
    setName("CollectionField");

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    addMouseListener(this);

    {
      add(Box.createHorizontalGlue());
      
      {
	JLabel label = new JLabel();
	pLabel = label;
	
	add(label);
      }
    
      add(Box.createHorizontalGlue());
      add(new JLabel(sIcon));
      add(Box.createRigidArea(new Dimension(8, 0)));
    }

    pPopup = new JPopupMenu(); 

    setValues(values);
    setSelectedIndex(0);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Set collection values. 
   */ 
  public void 
  setValues
  (
   Collection<String> values
  ) 
  {
    pValues = new ArrayList<String>(values);

    pPopup.removeAll();
    for(String v : pValues) {
      JMenuItem item = new JMenuItem(v);
      
      item.setActionCommand(v);
      item.addActionListener(this);

      pPopup.add(item);  
    }

    setSelectedIndex(0);
  }

  /**
   * Get the collection values.
   */ 
  public Collection<String>
  getValues() 
  {
    return pValues;
  }


  /**
   * Set the selected value.
   */ 
  public void
  setSelected
  (
   String v
  ) 
  {
    setSelectedIndex(pValues.indexOf(v));
  }

  /**
   * Get the selected value.
   * 
   * @return 
   *   The selected value or <CODE>null</CODE> if undefined.
   */ 
  public String
  getSelected() 
  {
    if(pSelectedIdx >= 0) 
      return pValues.get(pSelectedIdx);
    return null;
  }

  
  /**
   * Set the selected index.
   */ 
  public void 
  setSelectedIndex
  (
   int idx
  ) 
  {
    assert(idx < pValues.size());
    pSelectedIdx = idx;

    if(pSelectedIdx >= 0) 
      pLabel.setText(pValues.get(idx));
    else 
      pLabel.setText(null);
  }
 
  /**
   * Get the selected index.
   * 
   * @return 
   *   The index or <CODE>-1</CODE> if undefined.
   */ 
  public int
  getSelectedIndex() 
  {
    return pSelectedIdx;
  }
 

  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    setSelected(e.getActionCommand());
  }


  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) {} 

  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered(MouseEvent e) {}

  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited(MouseEvent e) {}

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  ) 
  {
    if((pValues.size() > 0) && (pSelectedIdx >= 0)) {
      Dimension size = e.getComponent().getSize();
      pPopup.setPopupSize(new Dimension(size.width, 23*pValues.size()));
      pPopup.show(e.getComponent(), 0, size.height);
    }
  } 

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3098195836855262214L;


  private static Icon sIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CollectionFieldIcon.png"));


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The popup menu.
   */ 
  private JPopupMenu  pPopup; 

  /**
   * The text label.
   */ 
  private JLabel  pLabel;


  /**
   * The underlying Collection.
   */ 
  private ArrayList<String>  pValues;

  /**
   * The selected index.
   */
  private int  pSelectedIdx;
}
