// $Id: JOwnerViewDialog.java,v 1.6 2004/05/13 02:37:41 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   O W N E R   V I E W   D I A L O G                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog to change author and/or name of the current the working area view for a 
 * {@link JManagerPanel JManagerPanel}.
 */ 
public 
class JOwnerViewDialog
  extends JBaseDialog
  implements ListSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param author
   *   The current owner of the working area view.
   *
   * @param view
   *   The current name of the working area view.
   * 
   * @param table
   *   The table of valid working area view names indexed by author user name.
   */ 
  public 
  JOwnerViewDialog
  (
   String author, 
   String view, 
   TreeMap<String,TreeSet<String>> table
  )
  {
    super("Change Owner|View", true);

    if(author == null)
      throw new IllegalArgumentException("The author cannot be (null)!");

    if(view == null)
      throw new IllegalArgumentException("The view cannot be (null)!");

    if(table == null)
      throw new IllegalArgumentException("The table cannot be (null)!");
    pTable = table;

    if(!pTable.containsKey(author)) 
      throw new IllegalArgumentException
	("The author (" + author + ") was not listed in the table!");
      
    if(!pTable.containsKey(author)) 
      throw new IllegalArgumentException
	("The author (" + author + ") had no view (" + view + ") listed in the table!");


    /* create dialog body components */ 
    {
      Box body = new Box(BoxLayout.X_AXIS);
      {
	body.add(Box.createRigidArea(new Dimension(20, 0)));

	pAuthorList = addListComponents(body, "Owner:");

	body.add(Box.createRigidArea(new Dimension(20, 0)));

	pViewList = addListComponents(body, "View:");

	body.add(Box.createRigidArea(new Dimension(20, 0)));
      }

      super.initUI("Change Owner|View", true, body, "Confirm", null, null, "Cancel");
    }

      
    /* initialize lists */ 
    {
      {
	DefaultListModel model = (DefaultListModel) pAuthorList.getModel();
	for(String name : pTable.keySet())
	  model.addElement(name);
	pAuthorList.setSelectedValue(author, true);
      } 
      
      {
	DefaultListModel model = (DefaultListModel) pViewList.getModel();
	for(String name : pTable.get(author))
	  model.addElement(name);
	pViewList.setSelectedValue(view, true);
      } 
    }

    /* add selection listeners */ 
    pAuthorList.addListSelectionListener(this);
    pViewList.addListSelectionListener(this);
  }
     
  /**
   * Add the list panel components.
   * 
   * @param box
   *   The parent horizontal box.
   * 
   * @param title
   *   The title of the list.
   */ 
  private JList 
  addListComponents
  (
   Box box, 
   String title
  ) 
  {
    Box vbox = new Box(BoxLayout.Y_AXIS);	

    vbox.add(Box.createRigidArea(new Dimension(0, 20)));
    
    vbox.add(UIMaster.createPanelLabel(title));
    
    vbox.add(Box.createRigidArea(new Dimension(0, 4)));

    JList lst = null;
    {
      lst = new JList(new DefaultListModel());
      lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      lst.setCellRenderer(new JListCellRenderer());

      {
	JScrollPane scroll = new JScrollPane(lst);
	
	scroll.setMinimumSize(new Dimension(120, 120));
	scroll.setPreferredSize(new Dimension(200, 200));
	
	scroll.setHorizontalScrollBarPolicy
	  (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	
	vbox.add(scroll);
      }
    }

    vbox.add(Box.createRigidArea(new Dimension(0, 20)));

    box.add(vbox);

    return lst;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of user which owns the working area view.
   * 
   * @return 
   *   The author or <CODE>null</CODE> if none was selected.
   */ 
  public String
  getAuthor() 
  {
    return ((String) pAuthorList.getSelectedValue());
  }

  /** 
   * Get the name of the working area view.
   * 
   * @return 
   *   The view or <CODE>null</CODE> if none was selected.
   */
  public String
  getView()
  {
    return ((String) pViewList.getSelectedValue());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    String author = (String) pAuthorList.getSelectedValue();
    DefaultListModel model = (DefaultListModel) pViewList.getModel();

    if(pAuthorList == e.getSource()) {
      model.clear();

      if(author != null) {
	for(String name : pTable.get(author))
	  model.addElement(name);
      }
    }

    String view = (String) pViewList.getSelectedValue();
    pConfirmButton.setEnabled((author != null) && (view != null));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6049795946975300474L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The table of valid working area view names indexed by author user name.
   */ 
  private TreeMap<String,TreeSet<String>>  pTable;


  /**
   * The list of user names owning a working area view.
   */ 
  private JList  pAuthorList;

  /**
   * The list of working area views.
   */ 
  private JList  pViewList;

}
