// $Id: JLockDialog.java,v 1.3 2006/09/25 12:11:44 jim Exp $

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
/*   L O C K   D I A L O G                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user for the revision numbers of the nodes to lock.
 */ 
public 
class JLockDialog
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
  JLockDialog
  (
   Frame owner
  ) 
  {
    super(owner, "Lock Node");

    /* initialize fields */ 
    {
      pVersionIDs    = new TreeMap<String,ArrayList<VersionID>>();
      pVersionFields = new TreeMap<String,JCollectionField>();
    }

    /* create dialog body components */ 
    {
      Box box = new Box(BoxLayout.Y_AXIS);
      pMainBox = box;

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
      
      super.initUI("Lock:", box, "Lock", null, null, "Cancel");
      pack();

      setSize(sTSize+sVSize+63, 500);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the revision number of the nodes to lock indexed by node name. <P> 
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
   * Update the revision numbers of the node to be locked.
   * 
   * @param base
   *   The revision number of the base checked-in version of the nodes indexed by fully
   *   resolved node name.
   * 
   * @param versions
   *   The revision numbers of the checked-in versions of the nodes indexed by fully
   *   resolved node name.
   * 
   * @param offline
   *   The revision nubers of all offline checked-in versions the nodes indexed by fully
   *   resolved node name.
   */ 
  public void 
  updateVersions
  (
   TreeMap<String,VersionID> base, 
   TreeMap<String,TreeSet<VersionID>> versions, 
   TreeMap<String,TreeSet<VersionID>> offline
  )
  {
    pVersionIDs.clear(); 
    pVersionFields.clear();

    pVersionBox.removeAll();

    if((versions == null) || (versions.isEmpty()))  {
      pConfirmButton.setEnabled(false);
    }
    else {
      for(String name : versions.keySet()) {
	ArrayList<VersionID> vids = new ArrayList<VersionID>(versions.get(name));
	Collections.reverse(vids);
	pVersionIDs.put(name, vids);

	VersionID bvid = base.get(name);
      
	{
	  Component comps[] = UIFactory.createTitledPanels();
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	
	  UIFactory.createTitledTextField
	    (tpanel, "Latest Version:", sTSize, 
	     vpanel, "v" +  vids.get(0), sVSize, 
	     "The revision number of the latest version.");
	    
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIFactory.createTitledTextField
	    (tpanel, "Base Version:", sTSize, 
	     vpanel, (bvid != null) ? ("v" +  bvid) : "-", sVSize, 
	     "The revision number of the current base checked-in version.");
	    
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
	      (tpanel, "Lock Version:", sTSize, 
	       vpanel, values, this, sVSize, 
	       "The revision number of the checked-in versions which is the target " + 
	       "of the lock.");
	    
	    {
	      ArrayList<VersionID> avids = new ArrayList<VersionID>(vids);
	      int idx = 0; 
	      if(bvid != null) 
		idx = avids.indexOf(bvid);
	      field.setSelectedIndex(idx);
	    }

	    pVersionFields.put(name, field);
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
      pHeaderLabel.setText("Lock " + (isSingle ? ":" : "Multiple Nodes:"));

      pConfirmButton.setEnabled(true);
    }

    pMainBox.revalidate();
    pMainBox.repaint();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2418733587288498497L;
  
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

}
