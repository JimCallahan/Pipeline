// $Id: JRestoreParamsDialog.java,v 1.1 2005/03/21 07:04:56 jim Exp $

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
/*   R E S T O R E   P A R A M S   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Used to set the Archiver plugin parameters to use when restoring nodes.
 */ 
public 
class JRestoreParamsDialog
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
  JRestoreParamsDialog
  (
   Dialog owner
  ) 
  {
    super(owner, "Restore", true);

    /* initialize fields */ 
    {
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
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");
	    
	  JTextField field = 
	    UIFactory.createTitledTextField
	    (tpanel, "Archiver:", sTSize, 
	     vpanel, null, sVSize, 
	     "The name of the Archiver plugin used to restore the versions.");
	  pArchiverField = field;
	}
	  
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	{
	  ArrayList<String> values = new ArrayList<String>();
	  values.add("-");
	    
	  JTextField field = 
	    UIFactory.createTitledTextField
	    (tpanel, "Version:", sTSize, 
	     vpanel, null, sVSize, 
	     "The revision number of the Archiver plugin.");
	  pArchiverVersionField = field;
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
	
	Dimension size = new Dimension(sTSize+sVSize+52, 300);
	scroll.setMinimumSize(size);
	scroll.setPreferredSize(size);

	scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      }

      super.initUI("Restore", true, scroll, "Restore", null, null, "Cancel");
      pack();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the archiver plugin instance to use to restore the nodes.
   */ 
  public void
  setArchive
  (
   ArchiveVolume volume
  ) 
  {
    pHeaderLabel.setText("Restore:  " + volume.getName());

    pArchiver = volume.getArchiver();

    pArchiverField.setText(pArchiver.getName());
    pArchiverVersionField.setText("v" + pArchiver.getVersionID());

    updateArchiverParams();
  }

  /**
   * Get the archiver plugin instance to use to restore the nodes.
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




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3254341717948112265L;
  
  private static final int sTSize = 180;
  private static final int sVSize = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current archiver instance.
   */ 
  private BaseArchiver  pArchiver; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the archiver plugin. 
   */ 
  private JTextField  pArchiverField; 

  /**
   * The revision number of the archiver plugin.
   */ 
  private JTextField  pArchiverVersionField;

  /**
   * The drawer containing archver parameter components.
   */ 
  private JDrawer pArchiverParamsDrawer; 

  /**
   * The archiver parameter components indexed by parameter name.
   */ 
  private TreeMap<String,Component>  pArchiverParamComponents; 

}
