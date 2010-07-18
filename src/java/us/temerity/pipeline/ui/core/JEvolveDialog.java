// $Id: JEvolveDialog.java,v 1.6 2009/07/13 17:26:02 jlee Exp $

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
/*   E V O L V E   D I A L O G                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the revision number of the checked-in version to use as the new basis
 * for the working version. 
 */ 
public 
class JEvolveDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   */ 
  public 
  JEvolveDialog
  (
   Frame owner
  ) 
  {
    super(owner, "Evolve Version");

    /* initialize fields */ 
    {
      pVersionIDs = new TreeMap<String,ArrayList<VersionID>>();
      
      pVersionFields = new TreeMap<String,JCollectionField>();

      pCheckedInMessages = new HashMap<JCollectionField,ArrayList<String>>();
    }

    /* create dialog body components */ 
    {
      Box box = new Box(BoxLayout.Y_AXIS);
      pMainBox = box;

      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	pVersionBox = vbox;
	
	vbox.add(UIFactory.createFiller(sTSize+sVSize));

	{
	  JScrollPane scroll = UIFactory.createVertScrollPane(vbox);
	  box.add(scroll);
	}
      }

      super.initUI("X", box, "Evolve", null, null, "Cancel");
      pack();
      
      setSize(sTSize+sVSize+63, 500);
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the revision number of the nodes to evolve indexed by node name. <P> 
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
    


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the header label and the revision numbers.
   * 
   * @param header
   *   The header label.
   * 
   * @param currentID
   *   The revision number of the current checked-in version upon which the working 
   *   version is based.
   * 
   * @param vids
   *   The revision numbers of the checked-in versions of the node.
   * 
   * @param offline
   *   The revision numbers of the offline checked-in versions.
   */ 
  public void 
  updateNameVersions
  (
   TreeMap<String,VersionID> currentIDs, 
   MappedSet<String,VersionID> versions, 
   MappedSet<String,VersionID> offline, 
   DoubleMap<String,VersionID,LogMessage> checkedInMessages
  )
  { 
    pCheckedInMessages.clear();
    pVersionIDs.clear();
    pVersionFields.clear(); 
    
    pVersionBox.removeAll();

    if((versions == null) || (versions.isEmpty()))  {
      pConfirmButton.setEnabled(false);
    }
    else {
      for(String name :versions.keySet()) {
	ArrayList<VersionID> vids = new ArrayList<VersionID>(versions.get(name));
	Collections.reverse(vids);
	pVersionIDs.put(name, vids);

	{
	  Component comps[] = UIFactory.createTitledPanels();
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];

	  UIFactory.createTitledTextField
	    (tpanel, "Current Version:", sTSize, 
	     vpanel, "v" + currentIDs.get(name), sVSize);

	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	  {
	    ArrayList<String> values = new ArrayList<String>();
	    for(VersionID vid : vids) {
	      String extra = "";
	      {
		TreeSet<VersionID> ovids = offline.get(name);
		if((ovids != null) && ovids.contains(vid))
		  extra = " - Offline";
	      }

	      values.add("v" + vid + extra);
	    }

	    JCollectionField field = 
	      UIFactory.createTitledCollectionField
	      (tpanel, "Evolve to Version:", sTSize, 
	       vpanel, values, this, sVSize, 
	       "The revision number of the version to evolve.");

	    ArrayList<String> messages = new ArrayList<String>();
	    {
	      TreeMap<VersionID,LogMessage> logHistory = checkedInMessages.get(name);

	      if(logHistory != null) {
		for(VersionID vid : vids) {
		  LogMessage log = logHistory.get(vid);

		  if(log != null)
		    messages.add(log.getMessage());
		  else
		    messages.add("There is no log message for (" + vid + ")");
		}
	      }
	    }

	    field.setSelectedIndex(0);
	    field.setToolTipText(UIFactory.formatToolTip(messages.get(0), 4));
	    field.addActionListener(this);
	    field.setActionCommand("version-changed");

	    pVersionFields.put(name, field);
	    pCheckedInMessages.put(field, messages);
	  }

	  /* The 3rd parameter to the JDrawer constructor is the preferred width, 
	     which is set to the width of the title + value panels. */
	  JDrawer drawer = new JDrawer(name + ":", (JComponent) comps[2], 
	                               sTSize+3 + sVSize, true);
	  pVersionBox.add(drawer);
	}
      }

      pVersionBox.add(UIFactory.createFiller(sTSize+sVSize));

      boolean isSingle = (pVersionIDs.size() == 1);
      pHeaderLabel.setText("Evolve Version " + (isSingle ? ":" : "Multiple Nodes:"));

      pConfirmButton.setEnabled(true);
    }
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
    if(cmd.equals("version-changed")) {
      if(e.getSource() instanceof JCollectionField)
	doVersionChanged((JCollectionField) e.getSource());
    }
    else
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Change the checked-in log message tool tip.
   *
   * @param field
   *   The source object of the event.
   */
  private void
  doVersionChanged
  (
   JCollectionField field
  )
  {
    ArrayList<String> messages = pCheckedInMessages.get(field);
    if(messages != null) {
      int idx = field.getSelectedIndex();

      if(idx > -1 && idx < messages.size())
	field.setToolTipText(UIFactory.formatToolTip(messages.get(idx), 4));
      else
	field.setToolTipText(UIFactory.formatToolTip("There is no log message."));
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7261599346057727439L;
  
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
   * The box containing the node version components.
   */ 
  private Box  pVersionBox;

  /**
   * The field for selecting the revision number to check-out.
   */ 
  private TreeMap<String,JCollectionField>  pVersionFields;

  /**
   * The checked-in log message history for each revision number.
   */
  private Map<JCollectionField,ArrayList<String>>  pCheckedInMessages;

}
