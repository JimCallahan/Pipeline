// $Id: JArchiverParamsDialog.java,v 1.2 2005/03/10 08:07:27 jim Exp $

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
/*   A R C H I V E R   P A R A M S   D I A L O G                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Used to select the Archiver plugin and its parameters to use when archiving nodes.
 */ 
public 
class JArchiverParamsDialog
  extends JBaseDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JArchiverParamsDialog
  (
   Dialog owner
  ) 
  {
    super(owner, "Archive", true);

    /* initialize fields */ 
    {
      pArchiverPlugins = PluginMgrClient.getInstance().getArchivers(); 
      pArchiverParamComponents = new TreeMap<String,Component>();
    }
    

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  
      
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	  
	{
	  JIdentifierField field = 
	    UIFactory.createTitledIdentifierField
	    (tpanel, "Volume Prefix:", sTSize, 
	     vpanel, "Archive", sVSize, 
	     "The prefix to prepend to the names of created archive volumes.");
	  pPrefixField = field;
	}
	  
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	
	{
	  JByteSizeField field = 
	    UIFactory.createTitledByteSizeField
	    (tpanel, "Minimum Size:", sTSize, 
	     vpanel, 1073741824L, sVSize, 
	     "The mimimum archive volume size (in bytes).");
	  pMinSizeField = field;
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");
	    
	  JCollectionField field = 
	    UIFactory.createTitledCollectionField
	    (tpanel, "Archiver:", sTSize, 
	     vpanel, values, sVSize, 
	     "The name of the Archiver plugin to use when archiving nodes.");
	  pArchiverField = field;
	    
	  field.setActionCommand("archiver-changed");
	  field.addActionListener(this);
	}
	  
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");
	    
	  JCollectionField field = 
	    UIFactory.createTitledCollectionField
	    (tpanel, "Version:", sTSize, 
	     vpanel, values, sVSize, 
	     "The revision number of the Archiver plugin.");
	  pArchiverVersionField = field;
	    
	  field.setActionCommand("archiver-changed");
	  field.addActionListener(this);
	}
	  
	body.add(comps[2]);
      }

      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	JDrawer drawer = new JDrawer("Archiver Parameters:", (JComponent) comps[2], true);
	drawer.setToolTipText(UIFactory.formatToolTip("Archiver plugin parameters."));
	pArchiverParamsDrawer = drawer;
	body.add(drawer);
      }

      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");
	
	spanel.setMinimumSize(new Dimension(sTSize+sVSize, 7));
	spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(sTSize+sVSize, 7));
	
	body.add(spanel);
      }

      JScrollPane scroll = new JScrollPane(body);
      {
	scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	
	Dimension size = new Dimension(sTSize+sVSize+52, 200);
	scroll.setMinimumSize(size);
	scroll.setPreferredSize(size);

	scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      }

      super.initUI("Archive Nodes:", true, scroll, "Archive", null, null, "Cancel");
      pack();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the prefix to prepend to the names of created archive volumes.
   */ 
  public String
  getPrefix()
  {
    return pPrefixField.getText();
  }

  /**
   * The The mimimum archive volume size (in bytes).
   */ 
  public Long
  getMinSize() 
  {
    return pMinSizeField.getValue();
  }

  /**
   * Get the archiver plugin instance to use to archive the nodes.
   */
  public BaseArchiver
  getArchiver() 
  {
    if(pArchiver != null) {
      for(ArchiverParam aparam : pArchiver.getParams()) {
	Component comp = pArchiverParamComponents.get(aparam.getName()); 
	Comparable value = null;
	if(aparam instanceof BooleanArchiverParam) {
	  JBooleanField field = (JBooleanField) comp;
	  value = field.getValue();
	}
	else if(aparam instanceof ByteSizeArchiverParam) {
	  JByteSizeField field = (JByteSizeField) comp;
	  value = field.getValue();	  
	}
	else if(aparam instanceof DirectoryArchiverParam) {
	  JPathField field = (JPathField) comp;
	  value = field.getText();
	}
	else if(aparam instanceof DoubleArchiverParam) {
	  JDoubleField field = (JDoubleField) comp;
	  value = field.getValue();
	}
	else if(aparam instanceof EnumArchiverParam) {
	  JCollectionField field = (JCollectionField) comp;
	  EnumArchiverParam eparam = (EnumArchiverParam) aparam;
	  value = eparam.getValueOfIndex(field.getSelectedIndex());
	}
	else if(aparam instanceof IntegerArchiverParam) {
	  JIntegerField field = (JIntegerField) comp;
	  value = field.getValue();
	}
	else if(aparam instanceof StringArchiverParam) {
	  JTextField field = (JTextField) comp;
	  value = field.getText();	  
	}
	else {
	  assert(false) : "Unknown archiver parameter type!";
	}

	pArchiver.setParamValue(aparam.getName(), value);
      }
    }

    return pArchiver;
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components.
   */
  public void 
  updateArchiver() 
  {
    try {
      PluginMgrClient mgr = PluginMgrClient.getInstance();
      mgr.update();

      pArchiverPlugins = mgr.getArchivers();
    }
    catch(PipelineException ex) {
      UIMaster.getInstance().showErrorDialog(ex);
    }

    pArchiverField.removeActionListener(this);
    {
      TreeSet<String> values = new TreeSet<String>();
      values.addAll(pArchiverPlugins.keySet());
      values.add("-");
      pArchiverField.setValues(values);

      if((pArchiver != null) && (values.contains(pArchiver.getName()))) 
	pArchiverField.setSelected(pArchiver.getName());
      else 
	pArchiverField.setSelected("-");
    }
    pArchiverField.addActionListener(this);

    doArchiverChanged(true);
  }

  /**
   * Update the UI components associated archiver parameters.
   */ 
  private void 
  updateArchiverParams() 
  {
    Component comps[] = UIFactory.createTitledPanels();
    JPanel tpanel = (JPanel) comps[0];
    JPanel vpanel = (JPanel) comps[1];
    
    boolean first = true;
    if((pArchiver != null) && pArchiver.hasParams()) {
      for(String pname : pArchiver.getLayout()) {
	if(pname == null) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	}
	else {
	  if(!first) 
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  ArchiverParam aparam = pArchiver.getParam(pname);
	  if(aparam != null) {
	    if(aparam instanceof BooleanArchiverParam) {
	      Boolean value = (Boolean) aparam.getValue();
	      JBooleanField field = 
		UIFactory.createTitledBooleanField 
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, sVSize, 
		 aparam.getDescription());
	      field.setValue(value);

	      pArchiverParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof ByteSizeArchiverParam) {
	      Long value = (Long) aparam.getValue();
	      JByteSizeField field = 
		UIFactory.createTitledByteSizeField 
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      pArchiverParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof DirectoryArchiverParam) {
	      String value = (String) aparam.getValue();
	      JPathField field = 
		UIFactory.createTitledPathField
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      // add file browser dialog button here...

	      pArchiverParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof DoubleArchiverParam) {
	      Double value = (Double) aparam.getValue();
	      JDoubleField field = 
		UIFactory.createTitledDoubleField 
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      pArchiverParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof EnumArchiverParam) {
	      EnumActionParam eparam = (EnumActionParam) aparam;
	      
	      JCollectionField field = 
		UIFactory.createTitledCollectionField
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, eparam.getValues(), sVSize, 
		 aparam.getDescription());
	      
	      field.setSelected((String) eparam.getValue());

	      pArchiverParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof IntegerArchiverParam) {
	      Integer value = (Integer) aparam.getValue();
	      JIntegerField field = 
		UIFactory.createTitledIntegerField 
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      pArchiverParamComponents.put(pname, field);
	    }
	    else if(aparam instanceof StringArchiverParam) {
	      String value = (String) aparam.getValue();
	      JTextField field = 
		UIFactory.createTitledEditableTextField 
		(tpanel, aparam.getNameUI() + ":", sTSize, 
		 vpanel, value, sVSize, 
		 aparam.getDescription());

	      pArchiverParamComponents.put(pname, field);	      
	    }
	    else {
	      assert(false) : "Unknown archiver parameter type!";
	    }
	  }
	}
	
	first = false;
      }
    }
    else {
      tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
      vpanel.add(Box.createHorizontalGlue());
    }

    UIFactory.addVerticalGlue(tpanel, vpanel);

    pArchiverParamsDrawer.setContents((JComponent) comps[2]);
  }

  /**
   * Update the archiver version field.
   */ 
  private void 
  updateArchiverVersionField()
  {
    pArchiverVersionField.removeActionListener(this);
    {
      if(pArchiver != null) {
	TreeSet<String> vstr = new TreeSet<String>();
	TreeSet<VersionID> vids = pArchiverPlugins.get(pArchiver.getName());
	for(VersionID vid : vids)
	  vstr.add("v" + vid.toString());
	pArchiverVersionField.setValues(vstr);
	
	pArchiverVersionField.setSelected("v" + pArchiver.getVersionID().toString());
	pArchiverVersionField.setEnabled(true);
      }
      else {
	TreeSet<String> vstr = new TreeSet<String>();
	vstr.add("-");
	pArchiverVersionField.setValues(vstr);
	pArchiverVersionField.setSelected("-");
	pArchiverVersionField.setEnabled(false);
      }
    }
    pArchiverVersionField.addActionListener(this);
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
    if(cmd.equals("archiver-changed")) 
      doArchiverChanged(false);
    else
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the appearance of the archiver fields after a change of value.
   * 
   * @param forceRebuild
   *   Whether the action should be rebuilt regardless of whether it has changed.
   */ 
  private void 
  doArchiverChanged
  (
   boolean forceRebuild
  ) 
  {
    BaseArchiver oarchiver = pArchiver;
    {
      String aname = pArchiverField.getSelected();
      if(aname.equals("-")) {
	pArchiver = null;
      }
      else {
	VersionID vid = null;
	boolean rebuild = false;
	if(forceRebuild || (oarchiver == null) || !oarchiver.getName().equals(aname)) 
	  rebuild = true;
	else {
	  String vstr = pArchiverVersionField.getSelected();
	  if(vstr.equals("-")) 
	    rebuild = true;
	  else {
	    vid = new VersionID(vstr.substring(1));
	    if(!vid.equals(oarchiver.getVersionID()))
	      rebuild = true;
	  }
	}

	if(rebuild) {
	  try {
	    pArchiver = PluginMgrClient.getInstance().newArchiver(aname, vid);
	    if((oarchiver != null) && oarchiver.getName().equals(pArchiver.getName()))
	      pArchiver.setParamValues(oarchiver);
	  }
	  catch(PipelineException ex) {
	    UIMaster.getInstance().showErrorDialog(ex);
	    pArchiver = null;	    
	  }
	}
      }

      updateArchiverVersionField();
      updateArchiverParams();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -236439332236952251L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 200;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current archiver instance.
   */ 
  private BaseArchiver  pArchiver; 

  /**
   * Cached names and version numbers of the loaded action plugins. 
   */
  private TreeMap<String,TreeSet<VersionID>>  pArchiverPlugins; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The prefix to prepend to the names of created archive volumes.
   */ 
  private JIdentifierField  pPrefixField; 

  /**
   * The The mimimum archive volume size (in bytes).
   */ 
  private JByteSizeField  pMinSizeField; 

  /**
   * The name of the archiver plugin. 
   */ 
  private JCollectionField  pArchiverField; 

  /**
   * The revision number of the archiver plugin.
   */ 
  private JCollectionField  pArchiverVersionField;

  /**
   * The drawer containing archver parameter components.
   */ 
  private JDrawer pArchiverParamsDrawer; 

  /**
   * The archiver parameter components indexed by parameter name.
   */ 
  private TreeMap<String,Component>  pArchiverParamComponents; 

}
