// $Id: JBooleanListDialog.java,v 1.1 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   L I S T   D I A L O G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog that displays a list of boolean fields.
 */
public 
class JBooleanListDialog
  extends JFullDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param parent
   *   The parent dialog.
   *   
   * @param title
   *   The title of the dialog
   *   
   * @param query
   *   The query that should be displayed 
   */ 
  public 
  JBooleanListDialog
  (
    Frame parent,
    String title,
    String query
  )
  {
    super(parent, title);
    
    pBooleanFields = new TreeMap<String, JBooleanField>();
    
    Component comps[] = UIFactory.createTitledPanels();
    
    pTPanel = (JPanel) comps[0];
    pVPanel = (JPanel) comps[1];
    
    pTopBox = Box.createVerticalBox();
    pTopBox.add(comps[2]);
    pTopBox.add(UIFactory.createFiller(20));
    
    Dimension size = new Dimension(350, 325);
    
    JScrollPane pane = UIFactory.createScrollPane
      (pTopBox, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
       JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, size, size, null);
    
    super.initUI(query, pane, "Add", null, null, "Cancel");
    
    this.setMaximumSize(getPreferredSize());
  }
 
  
 
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the list of keys that will have boolean fields displayed.<p>
   * 
   * This will clear all previous entries.
   */
  public void
  setFields
  (
    Collection<String> keys
  )
  {
    pTPanel.removeAll();
    pVPanel.removeAll();
    pBooleanFields.clear();
    
    for (String key : keys) {
      JBooleanField field = 
        UIFactory.createTitledBooleanField(pTPanel, key, sTSize, pVPanel, sVSize);
      UIFactory.addVerticalSpacer(pTPanel, pVPanel, 3);
      pBooleanFields.put(key, field);
      pTopBox.revalidate();
    }
  }
  
  /**
   * Get the names of the boolean fields that are set to <code>true</code>.
   */
  public TreeSet<String>
  getSelected()
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    
    for (Entry<String, JBooleanField> entry : pBooleanFields.entrySet()) {
      if (entry.getValue().getValue())
        toReturn.add(entry.getKey());
    }
    return toReturn;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5539652852892730266L;

  private static final int sTSize  = 100;
  private static final int sVSize  = 50;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private JPanel pTPanel;
  private JPanel pVPanel;
  
  private Box pTopBox;
  private TreeMap<String, JBooleanField> pBooleanFields;
  
}
