// $Id: JCheckOutDialog.java,v 1.2 2005/02/22 02:31:04 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K - O U T   D I A L O G                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the revision number and other options of the node to check-out.
 */ 
public 
class JCheckOutDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JCheckOutDialog() 
  {
    super("Check-Out Node", true);

    /* initialize fields */ 
    {
      pVersionIDs = new TreeMap<String,ArrayList<VersionID>>();

      pLatestVersionFields = new TreeMap<String,JTextField>();

      pVersionFields = new TreeMap<String,JCollectionField>();
      pModeFields    = new TreeMap<String,JCollectionField>();
      pMethodFields  = new TreeMap<String,JCollectionField>();
    }

    /* create dialog body components */ 
    {
      Box box = new Box(BoxLayout.Y_AXIS);
      pMainBox = box;

      {
	Box sbox = new Box(BoxLayout.Y_AXIS);
	pMasterBox = sbox;
	
	{
	  Component comps[] = UIFactory.createTitledPanels();
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	  
	  {
	    JCollectionField field = 
	      UIFactory.createTitledCollectionField
	      (tpanel, "Check-Out Mode:", sTSize+3, 
	       vpanel, CheckOutMode.titles(), sVSize, 
	       "The criteria used to determine whether working versions should be replaced.");
	    
	    field.setSelectedIndex(2);
	    
	    field.addActionListener(this);
	    field.setActionCommand("mode-changed");
	    
	    pMasterModeField = field;
	  }
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  {
	    JCollectionField field = 
	      UIFactory.createTitledCollectionField
	      (tpanel, "Check-Out Method:", sTSize+3, 
	       vpanel, CheckOutMethod.titles(), sVSize,
	       "The method for replacing working files.");
	    
	    field.setSelectedIndex(0);
	    
	    field.addActionListener(this);
	    field.setActionCommand("method-changed");
	    
	    pMasterMethodField = field;
	  }
	  
	  sbox.add(comps[2]);	  
	}
	
	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");
	  
	  spanel.setMinimumSize(new Dimension(sTSize+sVSize, 7));
	  spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
	  spanel.setPreferredSize(new Dimension(sTSize+sVSize, 7));
	  
	  sbox.add(spanel);
	}

	box.add(sbox);
      }

      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	pVersionBox = vbox;
	
	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");
	  
	  spanel.setMinimumSize(new Dimension(sTSize+sVSize, 7));
	  spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	  spanel.setPreferredSize(new Dimension(sTSize+sVSize, 7));
	  
	  vbox.add(spanel);
	}
	
	{
	  JScrollPane scroll = new JScrollPane(vbox);
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	  
	  scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
	  
	  box.add(scroll);
	}
      }
      
      super.initUI("Check-Out:", true, box, "Check-Out", null, null, "Cancel");
      pack();

      setSize(sTSize+sVSize+63, 500);
    }
  }

  



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the revision number of the nodes to check-out indexed by node name. <P> 
   * 
   * @return 
   *   The selected revision numbers.
   */
  public TreeMap<String,VersionID> 
  getVersionIDs()
  {
    TreeMap<String,VersionID> versions = new TreeMap<String,VersionID>();
    for(String name : pVersionFields.keySet()) {
      JCollectionField field = pVersionFields.get(name);
      versions.put(name, pVersionIDs.get(name).get(field.getSelectedIndex()));
    }
    return versions;
  }
    
  /**
   * Get the criteria used to determine whether nodes upstream of the root node of 
   * the check-out should also be checked-out indexed by node name. <P> 
   * 
   * @return 
   *   The selected modes.
   */ 
  public TreeMap<String,CheckOutMode>
  getModes()
  {
    TreeMap<String,CheckOutMode> modes = new TreeMap<String,CheckOutMode>();
    for(String name : pVersionFields.keySet()) {
      JCollectionField field = pModeFields.get(name);
      modes.put(name, CheckOutMode.values()[field.getSelectedIndex()]);
    }
    return modes;
  }

  /**
   * Get the method for creating working area files/links from the checked-in files 
   * indexed by node name. <P> 
   * 
   * @return 
   *   The selected methods.
   */ 
  public TreeMap<String,CheckOutMethod>
  getMethods()
  {
    TreeMap<String,CheckOutMethod> methods = new TreeMap<String,CheckOutMethod>();
    for(String name : pVersionFields.keySet()) {
      JCollectionField field = pMethodFields.get(name);
      methods.put(name, CheckOutMethod.values()[field.getSelectedIndex()]);
    }
    return methods;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the revision numbers of the node to be checked-out.
   * 
   * @param versions
   *   The revision numbers of the checked-in versions of the nodes indexed by fully
   *   resolved node name.
   */ 
  public void 
  updateVersions
  (
   TreeMap<String,ArrayList<VersionID>> versions
  )
  { 
    pVersionIDs.clear(); 
    pLatestVersionFields.clear(); 
    pVersionFields.clear(); 
    pModeFields.clear(); 
    pMethodFields.clear(); 

    pVersionBox.removeAll();

    if((versions == null) || (versions.isEmpty()))  {
      pMasterBox.setVisible(false);
      pConfirmButton.setEnabled(false);
    }
    else {
      for(String name : versions.keySet()) {
	ArrayList<VersionID> vids = new ArrayList<VersionID>(versions.get(name));
	Collections.reverse(vids);
	pVersionIDs.put(name, vids);
      
	{
	  Component comps[] = UIFactory.createTitledPanels();
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	
	  {
	    JTextField field = 
	      UIFactory.createTitledTextField
	      (tpanel, "Latest Version:", sTSize, 
	       vpanel, "v" +  vids.get(0), sVSize, 
	       "The revision number of the latest version.");

	    pLatestVersionFields.put(name, field);
	  }
	    
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	  
	  {
	    ArrayList<String> values = new ArrayList<String>();
	    for(VersionID vid : vids) 
	      values.add("v" + vid);
	    
	    JCollectionField field = 
	      UIFactory.createTitledCollectionField
	      (tpanel, "Check-Out Version:", sTSize, 
	       vpanel, values, this, sVSize, 
	       "The revision number of the version to check-out.");

	    field.setSelectedIndex(0);

	    pVersionFields.put(name, field);
	  }
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  {
	    JCollectionField field = 
	      UIFactory.createTitledCollectionField
	      (tpanel, "Check-Out Mode:", sTSize, 
	       vpanel, CheckOutMode.titles(), sVSize, 
	       "The criteria used to determine whether working versions should be replaced.");
	    
	    field.setSelectedIndex(2);
	    
	    pModeFields.put(name, field);
	  }

	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  {
	    JCollectionField field = 
	      UIFactory.createTitledCollectionField
	      (tpanel, "Check-Out Method:", sTSize, 
	       vpanel, CheckOutMethod.titles(), sVSize,
	       "The method for replacing working files.");

	    field.setSelectedIndex(0);

	    pMethodFields.put(name, field);
	  }

	  JDrawer drawer = new JDrawer(name + ":", (JComponent) comps[2], true);
	  pVersionBox.add(drawer);
	}
      }
      
      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");
	
	spanel.setMinimumSize(new Dimension(sTSize+sVSize, 7));
	spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(sTSize+sVSize, 7));
	
	pVersionBox.add(spanel);
      }

      boolean isSingle = (pVersionIDs.size() == 1);
      pHeaderLabel.setText("Check-Out:  " + (isSingle ? "Single Node" : "Multiple Nodes"));
      pMasterBox.setVisible(!isSingle);

      pConfirmButton.setEnabled(true);
    }

    pMainBox.revalidate();
    pMainBox.repaint();
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
    String cmd = e.getActionCommand();
    if(cmd.equals("mode-changed")) 
      doModeChanged();
    else if(cmd.equals("method-changed")) 
      doMethodChanged();
    else
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Change all check-out modes at once.
   */ 
  private void 
  doModeChanged()
  {
    int idx = pMasterModeField.getSelectedIndex();
    for(JCollectionField field : pModeFields.values()) 
      field.setSelectedIndex(idx);
  }

  /**
   * Change all check-out methods at once.
   */ 
  private void 
  doMethodChanged()
  {
    int idx = pMasterMethodField.getSelectedIndex();
    for(JCollectionField field : pMethodFields.values()) 
      field.setSelectedIndex(idx);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6486928125261792739L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 200;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision numbers of the currently checked-in versions of the node.
   */ 
  private TreeMap<String,ArrayList<VersionID>>  pVersionIDs;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The box containing all components.
   */ 
  private Box  pMainBox; 

  /**
   * The box containing the master mode/method components.
   */ 
  private Box  pMasterBox; 

  /**
   * The box containing the node version components.
   */ 
  private Box  pVersionBox; 

  /**
   * The latest revision number field.
   */ 
  private TreeMap<String,JTextField>  pLatestVersionFields;

  /**
   * The field for selecting the revision number to check-out.
   */ 
  private TreeMap<String,JCollectionField>  pVersionFields; 

  /**
   * The criteria used to determine whether nodes upstream of the root node of the check-out
   * should also be checked-out.
   */ 
  private JCollectionField                  pMasterModeField;
  private TreeMap<String,JCollectionField>  pModeFields;

  /**
   * The method for creating working area files/links from the checked-in files.
   */
  private JCollectionField                  pMasterMethodField;
  private TreeMap<String,JCollectionField>  pMethodFields;

}
