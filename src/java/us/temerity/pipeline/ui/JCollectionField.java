// $Id: JCollectionField.java,v 1.9 2005/01/09 16:01:11 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   C O L L E C T I O N   F I E L D                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents a {@link Collection Collection}.
 */
public 
class JCollectionField
  extends JPanel
  implements MouseListener, PopupMenuListener, ActionListener, ListSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JCollectionField
  (
   Collection<String> values
  )
  {
    this(values, null);
  }

  /**
   * Construct a new field.
   */ 
  public 
  JCollectionField
  (
   Collection<String> values, 
   JDialog parent
  )
  {
    super();  
    setName("CollectionField");

    setAlignmentY(0.5f);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    addMouseListener(this);

    {
      add(Box.createRigidArea(new Dimension(16, 0)));

      {
	JValueField field = new JValueField(this);
	pTextField = field;

	field.setName("CollectionValueTextField");
	field.setHorizontalAlignment(JLabel.CENTER);
	field.setEditable(false);
	field.setHighlighter(null);
	field.addMouseListener(this);

	add(field);
      }
    
      {
	JLabel label = new JLabel(sEnabledIcon);
	pIconLabel = label;
	add(label);
      }

      add(Box.createRigidArea(new Dimension(4, 0)));
    }

    pPopup = new JPopupMenu(); 
    pPopup.addPopupMenuListener(this);

    {
      if(parent != null) 
	pDialog = new JDialog(parent);
      else 
	pDialog = new JDialog();
    
      pDialog.setUndecorated(true);
      pDialog.setResizable(false);
      pDialog.setAlwaysOnTop(true);

      pDialog.addWindowListener(new CloseDialogListener());

      {
	JPanel panel = new JPanel();
	panel.setName("ItemListPanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));   

	{
	  JList lst = new JList(new DefaultListModel());
	  pItemList = lst;
	  
	  lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	  lst.setCellRenderer(new JListCellRenderer());

	  lst.addListSelectionListener(this);

	  {
	    JScrollPane scroll = new JScrollPane(lst);
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroll.setVerticalScrollBarPolicy
	      (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	  
	    panel.add(scroll);
	  }
	}

	pDialog.setContentPane(panel);
      }
    }

    setValues(values);
    setSelectedIndex(0);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Set the collection values. 
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
    return Collections.unmodifiableCollection(pValues);
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
      pTextField.setText(pValues.get(idx));
    else 
      pTextField.setText(null);

    pTextField.fireActionPerformed2();
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
  /*   E V E N T S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets whether or not this component is enabled.
   */ 
  public void 
  setEnabled
  (
   boolean enabled
  )
  {
    if(enabled && !isEnabled()) {
      addMouseListener(this);
      pTextField.addMouseListener(this);
      pIconLabel.setIcon(sEnabledIcon);
    }
    else if(!enabled && isEnabled()) {
      removeMouseListener(this);
      pTextField.removeMouseListener(this);
      pIconLabel.setIcon(sDisabledIcon);
    }

    super.setEnabled(enabled);
  }


  /**
   * Adds the specified action listener to receive action events from this collection field.
   */ 
  public void
  addActionListener
  (
   ActionListener l
  )
  {
    pTextField.addActionListener(l);
  }

  /**
   * Removes the specified action listener so that it no longer receives action events
   * from this collection field.
   */ 
  public void 	
  removeActionListener
  (
   ActionListener l
  )
  {
    pTextField.removeActionListener(l);
  }
          
  /**
   * Sets the command string used for action events.
   */ 
  public void 	
  setActionCommand
  (
   String command
  )
  {
    pTextField.setActionCommand(command);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J C O M P O N E N T   O V E R R I D E S                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets the foreground color of this component.
   */ 
  public void 
  setForeground
  (
   Color fg
  )
  {
    pFieldForegroundColor = fg;
    if(pTextField != null) 
      pTextField.setForeground(fg);
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
    pTextField.setForeground(pFieldForegroundColor);
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
      pTextField.setForeground(Color.yellow);
      Dimension size = getSize();

      if(pValues.size() < sItemLimit) {
	pPopup.setPopupSize(new Dimension(size.width, 23*pValues.size() + 10));
	pPopup.show(this, 0, size.height);
      }
      else {
	pItemList.removeListSelectionListener(this);
	{
	  DefaultListModel model = (DefaultListModel) pItemList.getModel();
	  model.clear();
	  
	  for(String value : pValues) 
	    model.addElement(value);	  
	  
	  pItemList.setSelectedIndex(getSelectedIndex());
	}
	pItemList.addListSelectionListener(this);

	{
	  Point pos = new Point(0, size.height);
	  SwingUtilities.convertPointToScreen(pos, this);

	  Dimension dsize = new Dimension(size.width, 23*sItemLimit + 14);
	  pDialog.setSize(dsize);

	  Rectangle bounds = pDialog.getGraphicsConfiguration().getBounds();

	  if(pos.x < bounds.x) 
	    pos.x += bounds.x - pos.x;
	  else if((pos.x + dsize.width) > (bounds.x + bounds.width)) 
	    pos.x += (bounds.x + bounds.width) - (pos.x + dsize.width);
	  
	  if((pos.y + dsize.height) > (bounds.y + bounds.height)) 
	    pos.y += (bounds.y + bounds.height) - (pos.y + dsize.height);

	  pDialog.setLocation(pos);
	}

	pDialog.setVisible(true);
      }
    }
  } 

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}
  

  /*-- POPUP MENU LISTNER METHODS ----------------------------------------------------------*/

  /**
   * This method is called when the popup menu is canceled. 
   */ 
  public void 
  popupMenuCanceled
  (
   PopupMenuEvent e
  )
  { 
    pTextField.setForeground(pFieldForegroundColor);
  }
   
  /**
   * This method is called before the popup menu becomes invisible. 
   */ 
  public void
  popupMenuWillBecomeInvisible(PopupMenuEvent e) {} 
  
  /**
   * This method is called before the popup menu becomes visible. 
   */ 
  public void 	
  popupMenuWillBecomeVisible(PopupMenuEvent e) {} 



  /*-- LIST SELECTION LISTENER METHODS -----------------------------------------------------*/

  /**
   * Called whenever the value of the selection changes.
   */ 
  public void 	
  valueChanged
  (
   ListSelectionEvent e
  )
  {
    if(e.getValueIsAdjusting()) 
      return;

    int idx = pItemList.getSelectedIndex();
    if(idx != -1) {
      setSelectedIndex(idx);
      pDialog.setVisible(false);
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  public
  class JValueField
    extends JTextField
  {
    public 
    JValueField
    (
     JCollectionField parent
    ) 
    {
      pParent = parent;
    }    

    public JCollectionField
    getCollectionField() 
    {
      return pParent;
    }

    public void 
    fireActionPerformed2()
    {
      fireActionPerformed();
    }

    private static final long serialVersionUID = -5299266041553415611L;

    private JCollectionField  pParent;
  }

  public 
  class CloseDialogListener
    extends WindowAdapter
  {
    public 
    CloseDialogListener() 
    {}

    public void 
    windowDeactivated
    (
     WindowEvent e
    ) 
    {
      pDialog.setVisible(false);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final int sItemLimit = 10;

  private static final long serialVersionUID = -3098195836855262214L;


  private static Icon sEnabledIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CollectionFieldIcon.png"));

  private static Icon sDisabledIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CollectionFieldIconDisabled.png"));


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The popup menu.
   */ 
  private JPopupMenu  pPopup; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The item selection dialog used for long lists.
   */ 
  private JDialog  pDialog;

  /** 
   * The item selection list.
   */ 
  private JList  pItemList;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The value text field.
   */ 
  private JValueField  pTextField;

  /**
   * The icon.
   */ 
  private JLabel  pIconLabel; 

  /**
   * The foreground color of the text field.
   */ 
  private Color  pFieldForegroundColor;


  /**
   * The underlying Collection.
   */ 
  private ArrayList<String>  pValues;

  /**
   * The selected index.
   */
  private int  pSelectedIdx;
}
