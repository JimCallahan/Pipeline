// $Id: JNodeDetailsPanel.java,v 1.16 2004/09/21 23:51:43 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.core.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   D E T A I L S   P A N E L                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A viewer/editor of node properties. <P> 
 * 
 * The node properties displayed include: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   The toolset environment under which editors and actions are run. <BR>
 *   The name of the editor plugin used to edit the data files associated with the node. <BR>
 *   The regeneration action and its single and per-dependency parameters. <BR>
 *   The job requirements. <BR>
 *   The IgnoreOverflow and IsSerial flags. <BR>
 *   The job batch size. <BR> 
 * </DIV> <BR> 
 * 
 */ 
public  
class JNodeDetailsPanel
  extends JTopLevelPanel
  implements MouseListener, KeyListener, ComponentListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JNodeDetailsPanel()
  {
    super();

    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JNodeDetailsPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  private void 
  initUI()
  {
    /* initialize fields */ 
    {
      pCheckedInVersions = new TreeMap<VersionID,NodeVersion>();

      pActionParamComponents = new TreeMap<String,Component[]>();

      pLinkActionParamValues    = new ArrayList<String>();
      pLinkActionParamNodeNames = new ArrayList<String>();

      pSelectionKeyComponents = new TreeMap<String,Component[]>();
      pLicenseKeyComponents   = new TreeMap<String,Component[]>();
    }

    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

      /* header */ 
      {
	JPanel panel = new JPanel();	

	panel.setName("DialogHeader");	
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

	{
	  JLabel label = new JLabel("X");
	  pHeaderLabel = label;
	  
	  label.setName("DialogHeaderLabel");	       

	  panel.add(label);	  
	}

	panel.add(Box.createHorizontalGlue());

	{
	  JButton btn = new JButton();		
	  pApplyButton = btn;
	  btn.setName("ApplyHeaderButton");
		  
	  Dimension size = new Dimension(19, 19);
	  btn.setMinimumSize(size);
	  btn.setMaximumSize(size);
	  btn.setPreferredSize(size);
	  
	  btn.setActionCommand("apply");
	  btn.addActionListener(this);
	  
	  panel.add(btn);
	} 
      
	add(panel);
      }

      add(Box.createRigidArea(new Dimension(0, 4)));

      /* full node name */ 
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	{
	  JTextField field = UIMaster.createTextField(null, 100, JLabel.LEFT);
	  pNodeNameField = field;
	  
	  field.setFocusable(true);
	  field.addKeyListener(this);
	  field.addMouseListener(this); 

	  hbox.add(field);
	}

	hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	add(hbox);
      }
	
      add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	/* versions panel */ 
	{
	  Component comps[] = createCommonPanels();
	  {
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    /* version state */ 
	    {
	      pVersionStateField = 
		UIMaster.createTitledTextField(tpanel, "Version State:", sTSize, 
					       vpanel, "-", sSSize);
	    }

	    UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	    /* revision number */ 
	    { 
	      tpanel.add(UIMaster.createFixedLabel("Revision Number:", sTSize, JLabel.RIGHT));

	      {
		Box hbox = new Box(BoxLayout.X_AXIS);

		{
		  JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
		  pBaseVersionField = field;

		  hbox.add(field);
		}

		hbox.add(Box.createRigidArea(new Dimension(8, 0)));
		
		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JCollectionField field = UIMaster.createCollectionField(values, sVSize);
		  pCheckedInVersionField = field;

		  field.addActionListener(this);
		  field.setActionCommand("update-version");

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }
	  }
	
	  JDrawer drawer = new JDrawer("Versions:", (JComponent) comps[2], true);
	  pVersionDrawer = drawer;
	  vbox.add(drawer);
	}

	/* properties panel */ 
	{
	  Component comps[] = createCommonPanels();
	  {
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    
	    /* property state */ 
	    {
	      pPropertyStateField = 
		UIMaster.createTitledTextField(tpanel, "Property State:", sTSize, 
					       vpanel, "-", sSSize);
	    }

	    UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	    /* toolset */ 
	    { 
	      {
		JLabel label = UIMaster.createFixedLabel("Toolset:", sTSize, JLabel.RIGHT);
		pToolsetTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);

		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JCollectionField field = UIMaster.createCollectionField(values, sVSize);
		  pWorkingToolsetField = field;
		  
		  field.setActionCommand("toolset-changed");
		  field.addActionListener(this);

		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JButton btn = new JButton();		 
		  pSetToolsetButton = btn;
		  btn.setName("SmallLeftArrowButton");
		  
		  Dimension size = new Dimension(12, 12);
		  btn.setMinimumSize(size);
		  btn.setMaximumSize(size);
		  btn.setPreferredSize(size);
	    
		  btn.setActionCommand("set-toolset");
		  btn.addActionListener(this);
		  
		  hbox.add(btn);
		} 

		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInToolsetField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* editor */ 
	    { 
	      {
		JLabel label = UIMaster.createFixedLabel("Editor:", sTSize, JLabel.RIGHT);
		pEditorTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);

		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JCollectionField field = UIMaster.createCollectionField(values, sVSize);
		  pWorkingEditorField = field;
		  
		  field.setActionCommand("editor-changed");
		  field.addActionListener(this);

		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JButton btn = new JButton();		 
		  pSetEditorButton = btn;
		  btn.setName("SmallLeftArrowButton");
		  
		  Dimension size = new Dimension(12, 12);
		  btn.setMinimumSize(size);
		  btn.setMaximumSize(size);
		  btn.setPreferredSize(size);
	    
		  btn.setActionCommand("set-editor");
		  btn.addActionListener(this);
		  
		  hbox.add(btn);
		} 

		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInEditorField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }
	  }
	  
	  JDrawer drawer = new JDrawer("Properties:", (JComponent) comps[2], true);
	  pPropertyDrawer = drawer;
	  vbox.add(drawer);
	}
	
	/* actions panel */ 
	{
	  Box abox = new Box(BoxLayout.Y_AXIS);
	  pActionBox = abox;

	  {
	    Component comps[] = createCommonPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    tpanel.setName("TopTitlePanel");
	    JPanel vpanel = (JPanel) comps[1];
	    vpanel.setName("TopValuePanel");

	    /* action */ 
	    { 
	      {
		JLabel label = UIMaster.createFixedLabel("Action:", sTSize, JLabel.RIGHT);
		pActionTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JCollectionField field = UIMaster.createCollectionField(values, sVSize);
		  pWorkingActionField = field;
		
		  field.setActionCommand("action-changed");
		  field.addActionListener(this);

		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JButton btn = new JButton();		 
		  pSetActionButton = btn;
		  btn.setName("SmallLeftArrowButton");
		  
		  Dimension size = new Dimension(12, 12);
		  btn.setMinimumSize(size);
		  btn.setMaximumSize(size);
		  btn.setPreferredSize(size);
	    
		  btn.setActionCommand("set-action");
		  btn.addActionListener(this);
		  
		  hbox.add(btn);
		} 

		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInActionField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* action version */ 
	    { 
	      {
		JLabel label = 
		  UIMaster.createFixedLabel("Version:", sTSize, JLabel.RIGHT);
		pActionVersionTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");

		  JCollectionField field = UIMaster.createCollectionField(values, sVSize);
		  pWorkingActionVersionField = field;

		  field.setActionCommand("action-changed");
		  field.addActionListener(this);

		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(20, 0)));

		{
		  JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInActionVersionField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

	    UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	    /* action enabled */ 
	    { 
	      {
		JLabel label = 
		  UIMaster.createFixedLabel("Enabled:", sTSize, JLabel.RIGHT);
		pActionEnabledTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  JBooleanField field = UIMaster.createBooleanField(sVSize);
		  pWorkingActionEnabledField = field;
		  
		  field.setValue(null);

		  field.setActionCommand("action-enabled-changed");
		  field.addActionListener(this);

		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(20, 0)));

		{
		  JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInActionEnabledField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

	    UIMaster.addVerticalGlue(tpanel, vpanel);

	    abox.add(comps[2]);
	  }	  

	  {
	    Box apbox = new Box(BoxLayout.Y_AXIS);
	    pActionParamsBox = apbox;

	    abox.add(apbox);
	  }

	  {
	    Box jrbox = new Box(BoxLayout.X_AXIS);
	    pJobReqsBox = jrbox;

	    jrbox.addComponentListener(this);

	    {
	      JPanel spanel = new JPanel();
	      spanel.setName("Spacer");

	      spanel.setMinimumSize(new Dimension(7, 0));
	      spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	      spanel.setPreferredSize(new Dimension(7, 0));

	      jrbox.add(spanel);
	    }
	
	    { 
	      Box dbox = new Box(BoxLayout.Y_AXIS);

	      /* job requirements */ 
	      {
		Component comps[] = createCommonPanels();
		{
		  JPanel tpanel = (JPanel) comps[0];
		  JPanel vpanel = (JPanel) comps[1];

		  /* overflow policy */ 
		  { 
		    {
		      JLabel label = 
			UIMaster.createFixedLabel("Overflow Policy:", sTSize-7, JLabel.RIGHT);
		      pOverflowPolicyTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {	
			ArrayList<String> values = new ArrayList<String>();
			values.add("-");
	
			JCollectionField field = 
			  UIMaster.createCollectionField(values, sVSize);
			pWorkingOverflowPolicyField = field;

			field.setActionCommand("overflow-policy-changed");
			field.addActionListener(this);

			hbox.add(field);
		      }

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JButton btn = new JButton();		 
			pSetOverflowPolicyButton = btn;
			btn.setName("SmallLeftArrowButton");

			Dimension size = new Dimension(12, 12);
			btn.setMinimumSize(size);
			btn.setMaximumSize(size);
			btn.setPreferredSize(size);

			btn.setActionCommand("set-overflow-policy");
			btn.addActionListener(this);

			hbox.add(btn);
		      } 

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JTextField field = 
			  UIMaster.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInOverflowPolicyField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }

		  UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

		  /* execution method */ 
		  { 
		    {
		      JLabel label = 
			UIMaster.createFixedLabel("Execution Method:", 
						  sTSize-7, JLabel.RIGHT);
		      pExecutionMethodTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		
			ArrayList<String> values = new ArrayList<String>();
			values.add("-");

			JCollectionField field = 
			  UIMaster.createCollectionField(values, sVSize);
			pWorkingExecutionMethodField = field;

			field.setActionCommand("execution-method-changed");
			field.addActionListener(this);

			hbox.add(field);
		      }

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JButton btn = new JButton();		 
			pSetExecutionMethodButton = btn;
			btn.setName("SmallLeftArrowButton");

			Dimension size = new Dimension(12, 12);
			btn.setMinimumSize(size);
			btn.setMaximumSize(size);
			btn.setPreferredSize(size);

			btn.setActionCommand("set-execution-method");
			btn.addActionListener(this);

			hbox.add(btn);
		      } 

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JTextField field = 
			  UIMaster.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInExecutionMethodField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }

		  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

		  /* batch size */ 
		  { 
		    {
		      JLabel label = 
			UIMaster.createFixedLabel("Batch Size:", sTSize-7, JLabel.RIGHT);
		      pBatchSizeTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		  
			JIntegerField field = 
			  UIMaster.createIntegerField(null, sVSize, JLabel.CENTER);
			pWorkingBatchSizeField = field;

			field.setActionCommand("batch-size-changed");
			field.addActionListener(this);

			hbox.add(field);
		      }

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JButton btn = new JButton();		 
			pSetBatchSizeButton = btn;
			btn.setName("SmallLeftArrowButton");

			Dimension size = new Dimension(12, 12);
			btn.setMinimumSize(size);
			btn.setMaximumSize(size);
			btn.setPreferredSize(size);

			btn.setActionCommand("set-batch-size");
			btn.addActionListener(this);

			hbox.add(btn);
		      } 

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JTextField field = 
			  UIMaster.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInBatchSizeField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }

		  UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

		  /* priority */ 
		  { 
		    {
		      JLabel label = 
			UIMaster.createFixedLabel("Priority:", sTSize-7, JLabel.RIGHT);
		      pPriorityTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		  
			JIntegerField field = 
			  UIMaster.createIntegerField(null, sVSize, JLabel.CENTER);
			pWorkingPriorityField = field;

			field.setActionCommand("priority-changed");
			field.addActionListener(this);

			hbox.add(field);
		      }

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JButton btn = new JButton();		 
			pSetPriorityButton = btn;
			btn.setName("SmallLeftArrowButton");

			Dimension size = new Dimension(12, 12);
			btn.setMinimumSize(size);
			btn.setMaximumSize(size);
			btn.setPreferredSize(size);

			btn.setActionCommand("set-priority");
			btn.addActionListener(this);

			hbox.add(btn);
		      } 

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JTextField field = 
			  UIMaster.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInPriorityField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }

		  UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

		  /* maximum load */ 
		  { 
		    {
		      JLabel label = 
			UIMaster.createFixedLabel("Maximum Load:", sTSize-7, JLabel.RIGHT);
		      pMaxLoadTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		  
			JFloatField field = 
			  UIMaster.createFloatField(null, sVSize, JLabel.CENTER);
			pWorkingMaxLoadField = field;

			field.setActionCommand("maximum-load-changed");
			field.addActionListener(this);

			hbox.add(field);
		      }

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JButton btn = new JButton();		 
			pSetMaxLoadButton = btn;
			btn.setName("SmallLeftArrowButton");

			Dimension size = new Dimension(12, 12);
			btn.setMinimumSize(size);
			btn.setMaximumSize(size);
			btn.setPreferredSize(size);

			btn.setActionCommand("set-maximum-load");
			btn.addActionListener(this);

			hbox.add(btn);
		      } 

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JTextField field = 
			  UIMaster.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInMaxLoadField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }

		  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

		  /* minimum memory */ 
		  { 
		    {
		      JLabel label = 
			UIMaster.createFixedLabel("Minimum Memory:", sTSize-7, JLabel.RIGHT);
		      pMinMemoryTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		  
			JByteSizeField field = 
			  UIMaster.createByteSizeField(null, sVSize, JLabel.CENTER);
			pWorkingMinMemoryField = field;

			field.setActionCommand("minimum-memory-changed");
			field.addActionListener(this);

			hbox.add(field);
		      }

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JButton btn = new JButton();		 
			pSetMinMemoryButton = btn;
			btn.setName("SmallLeftArrowButton");

			Dimension size = new Dimension(12, 12);
			btn.setMinimumSize(size);
			btn.setMaximumSize(size);
			btn.setPreferredSize(size);

			btn.setActionCommand("set-minimum-memory");
			btn.addActionListener(this);

			hbox.add(btn);
		      } 

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JTextField field = 
			  UIMaster.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInMinMemoryField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }
		  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

		  /* minimum disk */ 
		  { 
		    {
		      JLabel label = 
			UIMaster.createFixedLabel("Minimum Disk:", sTSize-7, JLabel.RIGHT);
		      pMinDiskTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		  
			JByteSizeField field = 
			  UIMaster.createByteSizeField(null, sVSize, JLabel.CENTER);
			pWorkingMinDiskField = field;

			field.setActionCommand("minimum-disk-changed");
			field.addActionListener(this);

			hbox.add(field);
		      }

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JButton btn = new JButton();		 
			pSetMinDiskButton = btn;
			btn.setName("SmallLeftArrowButton");

			Dimension size = new Dimension(12, 12);
			btn.setMinimumSize(size);
			btn.setMaximumSize(size);
			btn.setPreferredSize(size);

			btn.setActionCommand("set-minimum-disk");
			btn.addActionListener(this);

			hbox.add(btn);
		      } 

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JTextField field = 
			  UIMaster.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInMinDiskField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }
		}

		JDrawer drawer = 
		  new JDrawer("Job Requirements:", (JComponent) comps[2], true);
		pJobReqsDrawer = drawer;
		dbox.add(drawer);
	      }

	      /* selection keys */ 
	      {
		Box box = new Box(BoxLayout.Y_AXIS);
		pSelectionKeysBox = box;

		JDrawer drawer = new JDrawer("Selection Keys:", box, false);
		pSelectionDrawer = drawer;
		dbox.add(drawer);
	      }

	      /* license keys */ 
	      {
		Box box = new Box(BoxLayout.Y_AXIS);
		pLicenseKeysBox = box;

		JDrawer drawer = new JDrawer("License Keys:", box, false);
		pLicenseDrawer = drawer;
		dbox.add(drawer);
	      }

	      jrbox.add(dbox);
	    }

	    abox.add(jrbox);
	  }
	  
	  JDrawer drawer = new JDrawer("Regeneration Action:", abox, true);
	  pActionDrawer = drawer;
	  vbox.add(drawer);
	}
	
	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");
	  
	  spanel.setMinimumSize(new Dimension(sTSize+sSSize+30, 7));
	  spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	  spanel.setPreferredSize(new Dimension(sTSize+sSSize+30, 7));
	  
	  vbox.add(spanel);
	}

	vbox.add(Box.createVerticalGlue());

	{
	  JScrollPane scroll = new JScrollPane(vbox);
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	  scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

	  add(scroll);
	}
      }

      Dimension size = new Dimension(sTSize+sSSize+58, 120);
      setMinimumSize(size);
      setPreferredSize(size);

      setFocusable(true);
      addKeyListener(this);
      addMouseListener(this); 
    }

    updateNodeStatus(null);
  }

  /**
   * Create the title/value panels.
   * 
   * @return 
   *   The title panel, value panel and containing box.
   */   
  private Component[]
  createCommonPanels()
  {
    Component comps[] = UIMaster.createTitledPanels();

    {
      JPanel panel = (JPanel) comps[0];
      panel.setFocusable(true);
      panel.addKeyListener(this);
      panel.addMouseListener(this); 
    }

    return comps;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the group ID. <P> 
   * 
   * Group ID values must be in the range: [1-9]
   * 
   * @param groupID
   *   The new group ID or (0) for no group assignment.
   */ 
  public void
  setGroupID
  (
   int groupID
  )
  {
    UIMaster master = UIMaster.getInstance();

    PanelGroup<JNodeDetailsPanel> panels = master.getNodeDetailsPanels();

    if(pGroupID > 0)
      panels.releaseGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && panels.isGroupUnused(groupID)) {
      panels.assignGroup(this, groupID);
      pGroupID = groupID;
    }
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  public boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    PanelGroup<JNodeDetailsPanel> panels = UIMaster.getInstance().getNodeDetailsPanels();
    return panels.isGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the name of the currently displayed node.
   * 
   * @return 
   *   The fully resolved node name or <CODE>null</CODE> if undefined.
   */ 
  public synchronized String 
  getNodeName() 
  {
    if(pStatus != null) 
      return pStatus.getName();
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current node have a working version?
   */ 
  private boolean 
  hasWorking() 
  {
    return (getWorkingVersion() != null);
  }

  /**
   * Get the working version of the current node.
   * 
   * @return 
   *   The working version or <CODE>null</CODE> if none exists.
   */ 
  private NodeMod
  getWorkingVersion() 
  {
    if((pStatus != null) && (pStatus.getDetails() != null))
      return pStatus.getDetails().getWorkingVersion();
    return null;
  }


  /**
   * Initialize the temporary regeneration action with the action of the working version 
   * of the current node.
   * 
   * @return 
   *   The working action or <CODE>null</CODE> if none exists.
   */ 
  private BaseAction
  initWorkingAction() 
  {
    pWorkingAction = null;

    NodeMod mod = getWorkingVersion();
    if(mod != null) 
      pWorkingAction = mod.getAction();

    return pWorkingAction;
  }

  /**
   * Get the temporary regeneration action of the working version of the current node.
   * 
   * @return 
   *   The working action or <CODE>null</CODE> if none exists.
   */ 
  private BaseAction
  getWorkingAction() 
  {
    return pWorkingAction;
  }

  /**
   * Set the temporary regeneration action of the working version of the current node.
   * 
   * @param action
   *   The working action.
   */ 
  private void
  setWorkingAction
  (
   BaseAction action
  ) 
  {
    pWorkingAction = action;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current node have any checked-in versions?
   */ 
  private boolean 
  hasCheckedIn() 
  {
    return ((pStatus != null) && (pStatus.getDetails() != null) && 
	    (pStatus.getDetails().getLatestVersion() != null));
  }

  /**
   * Get the selected checked-in version of the current node.
   * 
   * @return 
   *   The checked-in version or <CODE>null</CODE> if none exists.
   */ 
  private NodeVersion
  getCheckedInVersion() 
  {
    NodeVersion vsn = null;
    if((pStatus != null) && (pStatus.getDetails() != null)) {
      NodeDetails details = pStatus.getDetails();
      if(pStatus.getDetails().getLatestVersion() != null) {
	ArrayList<VersionID> vids = details.getVersionIDs();
	Collections.reverse(vids);
	VersionID vid = vids.get(pCheckedInVersionField.getSelectedIndex());
	
	vsn = pCheckedInVersions.get(vid);
	if(vsn == null) {
	  UIMaster master = UIMaster.getInstance();
	  try {
	    MasterMgrClient client = master.getMasterMgrClient();
	    vsn = client.getCheckedInVersion(pStatus.getName(), vid);
	    pCheckedInVersions.put(vid, vsn);
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	    return null;
	  }
	}
	assert(vsn != null);
      }
    }

    return vsn;
  }

  /**
   * Get the regeneration action of the selected checked-in version of the current node.
   * 
   * @return 
   *   The checked-in action or <CODE>null</CODE> if none exists.
   */ 
  private BaseAction
  getCheckedInAction() 
  {
    NodeVersion vsn = getCheckedInVersion();
    if(vsn != null) 
      return vsn.getAction();
    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the UI components to reflect the given node status.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param status
   *   The current node status.
   */
  public synchronized void 
  updateNodeStatus
  (
   String author, 
   String view, 
   NodeStatus status
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateNodeStatus(status);
  }

  /**
   * Update the UI components to reflect the given node status.
   * 
   * @param status
   *   The current node status.
   */
  public synchronized void 
  updateNodeStatus
  (
   NodeStatus status
  ) 
  {
    pStatus = status;

    NodeDetails details = null;
    if(pStatus != null) 
      details = pStatus.getDetails();

    NodeMod work = null;
    NodeVersion base = null;
    NodeVersion latest = null;
    if(details != null) {
      work = details.getWorkingVersion();
      base = details.getBaseVersion();
      latest = details.getLatestVersion();
    }

    {
      pCheckedInVersions.clear();

      if(base != null) 
	pCheckedInVersions.put(base.getVersionID(), base);

      if(latest != null) 
	pCheckedInVersions.put(latest.getVersionID(), latest);
    }

    /* header */ 
    {
      String name = "Blank-Normal";
      if(pStatus != null) {
	if(details != null) {
	  if(details.getOverallNodeState() == OverallNodeState.NeedsCheckOut) {
	    VersionID wvid = details.getWorkingVersion().getWorkingID();
	    VersionID lvid = details.getLatestVersion().getVersionID();
	    switch(wvid.compareLevel(lvid)) {
	    case Major:
	      name = ("NeedsCheckOutMajor-" + details.getOverallQueueState() + "-Normal");
	      break;
	      
	    case Minor:
	      name = ("NeedsCheckOut-" + details.getOverallQueueState() + "-Normal");
	      break;
	      
	    case Micro:
	      name = ("NeedsCheckOutMicro-" + details.getOverallQueueState() + "-Normal");
	    }
	  }
	  else {
	    name = (details.getOverallNodeState() + "-" + 
		    details.getOverallQueueState() + "-Normal");
	  }
	}
		
	pHeaderLabel.setText(pStatus.toString());
	pNodeNameField.setText(pStatus.getName());
      }
      else {
	pHeaderLabel.setText(null);
	pNodeNameField.setText(null);
      }
      
      try {
	pHeaderLabel.setIcon(TextureMgr.getInstance().getIcon(name));
      }
      catch(IOException ex) {
	Logs.tex.severe("Internal Error:\n" + 
			"  " + ex.getMessage());
	Logs.flush();
	System.exit(1);
      } 
    }   

    /* versions panel */ 
    {
      if(details != null) 
	pVersionStateField.setText(details.getVersionState().toTitle());
      else 
	pVersionStateField.setText("-");

      /* revision number */ 
      {
	if(base != null) 
	  pBaseVersionField.setText("v" + base.getVersionID().toString());
	else 
	  pBaseVersionField.setText("-");
	
	{
	  ArrayList<String> values = new ArrayList<String>();
	  if(details != null) {
	    for(VersionID vid : details.getVersionIDs()) 
	      values.add("v" + vid.toString());
	  }
	  Collections.reverse(values);
	  
	  if(values.isEmpty()) 
	    values.add("-");

	  pCheckedInVersionField.removeActionListener(this);
 	    pCheckedInVersionField.setValues(values);
	    pCheckedInVersionField.setSelectedIndex(0);
	  pCheckedInVersionField.addActionListener(this);

	  pCheckedInVersionField.setEnabled(latest != null);
	}
      }
    }

    /* properties panel */ 
    {
      if(details != null) 
	pPropertyStateField.setText(details.getPropertyState().toTitle());
      else 
	pPropertyStateField.setText("-");
      
      /* toolset */ 
      {
	pWorkingToolsetField.removeActionListener(this);
	{
	  TreeSet<String> toolsets = new TreeSet<String>();
	  if(work != null) {
	    UIMaster master = UIMaster.getInstance();
	    try {
	      toolsets.addAll(master.getMasterMgrClient().getActiveToolsetNames());
	      if((work.getToolset() != null) && !toolsets.contains(work.getToolset()))
		toolsets.add(work.getToolset());
	    }
	    catch(PipelineException ex) {
	    }
	  }
	  
	  if(toolsets.isEmpty())
	    toolsets.add("-");
	  
	  pWorkingToolsetField.setValues(toolsets);

	  if((work != null) && (work.getToolset() != null)) 
	    pWorkingToolsetField.setSelected(work.getToolset());
	  else 
	    pWorkingToolsetField.setSelected("-");

	  pWorkingToolsetField.setEnabled(!pIsLocked && (work != null));
	}
	pWorkingToolsetField.addActionListener(this);
	
	pSetToolsetButton.setEnabled(!pIsLocked && (work != null) && (latest != null));
	
	{
	  if(latest != null)
	    pCheckedInToolsetField.setText(latest.getToolset());
	  else 
	    pCheckedInToolsetField.setText("-");

	  pCheckedInToolsetField.setEnabled(latest != null);
	}

	doToolsetChanged();
      }

      /* editor */ 
      { 
	pWorkingEditorField.removeActionListener(this);
	{
	  TreeSet<String> editors = new TreeSet<String>();
	  if(work != null) 
	    editors.addAll(PluginMgr.getInstance().getEditors().keySet());
	  editors.add("-");
	  pWorkingEditorField.setValues(editors);
	  
	  if((work != null) && 
	     (work.getEditor() != null) && (editors.contains(work.getEditor())))
	    pWorkingEditorField.setSelected(work.getEditor());
	  else 
	    pWorkingEditorField.setSelected("-");
	  
	  pWorkingEditorField.setEnabled(!pIsLocked && (work != null));
	}
	pWorkingEditorField.addActionListener(this);
	
	pSetEditorButton.setEnabled(!pIsLocked && (work != null) && (latest != null));
	
	{
	  if((latest != null) && (latest.getEditor() != null))
	    pCheckedInEditorField.setText(latest.getEditor());
	  else 
	    pCheckedInEditorField.setText("-");
	  
	  pCheckedInEditorField.setEnabled(latest != null);
	}

	doEditorChanged();
      }
    }
    
    /* actions panel */ 
    {
      pWorkingActionField.removeActionListener(this);
      {
	TreeSet<String> actions = new TreeSet<String>();
	if(work != null) 
	  actions.addAll(PluginMgr.getInstance().getActions().keySet());
	actions.add("-");
	pWorkingActionField.setValues(actions);
	
	BaseAction waction = initWorkingAction();
	if((waction != null) && (actions.contains(waction.getName())))
	  pWorkingActionField.setSelected(waction.getName());
	else 
	  pWorkingActionField.setSelected("-");
	
	pWorkingActionField.setEnabled(!pIsLocked && (work != null));
	
	updateActionVersionFields();
      }
      pWorkingActionField.addActionListener(this);

      pSetActionButton.setEnabled(!pIsLocked && (work != null) && (latest != null));

      {
	BaseAction caction = getCheckedInAction();	
	if(caction != null) 
	  pCheckedInActionField.setText(caction.getName());
	else 
	  pCheckedInActionField.setText("-");
      }

      if((work != null) && (getWorkingAction() != null)) {
	pWorkingActionEnabledField.setValue(work.isActionEnabled()); 
	pWorkingActionEnabledField.setEnabled(true);
      }
      else {
	pWorkingActionEnabledField.setValue(null);
	pWorkingActionEnabledField.setEnabled(false);
      }

      {
	NodeVersion cvsn = getCheckedInVersion(); 
	if((cvsn != null) && (cvsn.getAction() != null)) 
	  pCheckedInActionEnabledField.setText(cvsn.isActionEnabled() ? "YES" : "no");
	else 
	  pCheckedInActionEnabledField.setText("-");
      }

      pActionParamComponents.clear();
      doActionChanged();
    }

    /* job requirements panel */ 
    updateJobRequirements(true);

    pApplyButton.setEnabled(false);
  }

  /**
   * Update checked-in version related values of all fields.
   */ 
  private void 
  updateVersion() 
  {
    /* lookup the selected checked-in version */ 
    NodeVersion vsn = getCheckedInVersion();
    assert(vsn != null);

    /* save whether the apply button is already enabled? */ 
    boolean isEnabled = pApplyButton.isEnabled();

    /* properties panel */ 
    {
      /* toolset */ 
      {
	if(vsn.getToolset() != null)
	  pCheckedInToolsetField.setText(vsn.getToolset());
	else 
	  pCheckedInToolsetField.setText("-");

	doToolsetChanged();
      }

      /* editor */ 
      {
	if(vsn.getEditor() != null)
	  pCheckedInEditorField.setText(vsn.getEditor());
	else 
	  pCheckedInEditorField.setText("-");

	doEditorChanged();
      }
    }
    
    /* actions panel */ 
    {
      BaseAction action = vsn.getAction();	
      if(action != null) 
	pCheckedInActionField.setText(action.getName());
      else 
	pCheckedInActionField.setText("-");

      doActionChanged();    
    }

    /* job requirements panel */ 
    updateJobRequirements(false); 

    /* restore the enabled state of the apply button */ 
    pApplyButton.setEnabled(isEnabled);
  }

  /**
   * Update the action versions fields.
   */ 
  private void 
  updateActionVersionFields()
  {
    pWorkingActionVersionField.removeActionListener(this);
    {
      TreeMap<String,TreeSet<VersionID>> plgs = PluginMgr.getInstance().getActions();

      BaseAction waction = getWorkingAction();
      if(waction != null) {
	TreeSet<String> vstr = new TreeSet<String>();
	TreeSet<VersionID> vids = plgs.get(waction.getName());
	for(VersionID vid : vids)
	  vstr.add("v" + vid.toString());
	pWorkingActionVersionField.setValues(vstr);
	
	pWorkingActionVersionField.setSelected("v" + waction.getVersionID().toString());
	pWorkingActionVersionField.setEnabled(true);
      }
      else {
	TreeSet<String> vstr = new TreeSet<String>();
	vstr.add("-");
	pWorkingActionVersionField.setValues(vstr);
	pWorkingActionVersionField.setSelected("-");
	pWorkingActionVersionField.setEnabled(false);
      }
    }
    pWorkingActionVersionField.addActionListener(this);

    BaseAction caction = getCheckedInAction();	
    if(caction != null) 
      pCheckedInActionVersionField.setText("v" + caction.getVersionID());
    else 
      pCheckedInActionVersionField.setText("-");    
  }

  /**
   * Update the UI components associated with the working and checked-in actions.
   */ 
  private void 
  updateActionParams()
  {
    pActionParamsBox.removeAll();

    BaseAction waction = getWorkingAction();
    BaseAction caction = getCheckedInAction();

    BaseAction action = null;
    if(waction != null) 
      action = waction;
    else if(caction != null) 
      action = caction;

    Component comps[] = createCommonPanels();
    JPanel tpanel = (JPanel) comps[0];
    tpanel.setName("BottomTitlePanel");
    JPanel vpanel = (JPanel) comps[1];
    vpanel.setName("BottomValuePanel");

    if((action == null) || (!action.hasSingleParams() && !action.supportsSourceParams())) {
      tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
      vpanel.add(Box.createHorizontalGlue());
    }
    else {
      {
	pLinkActionParamValues.clear();
	for(String sname : pStatus.getSourceNames()) 
	  pLinkActionParamValues.add(pStatus.getSource(sname).toString());
	pLinkActionParamValues.add("-");
	
	pLinkActionParamNodeNames.clear();
	pLinkActionParamNodeNames.addAll(pStatus.getSourceNames());
	pLinkActionParamNodeNames.add(null);
      }

      UIMaster.addVerticalSpacer(tpanel, vpanel, 9);

      for(String pname : action.getSingleLayout()) {
	if(pname == null) {
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	}
	else {
	  BaseActionParam param = action.getSingleParam(pname);

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  Component pcomps[] = new Component[4];
	  
	  {
	    JLabel label = 
	      UIMaster.createFixedLabel(param.getNameUI() + ":", sTSize, JLabel.RIGHT);
	    pcomps[0] = label;
	    
	    tpanel.add(label);
	  }
	  
	  { 
	    Box hbox = new Box(BoxLayout.X_AXIS);
	    
	    {
	      BaseActionParam aparam = null;
	      if(waction != null) 
		aparam = waction.getSingleParam(param.getName());
	      
	      if(aparam != null) {
		if(aparam instanceof BooleanActionParam) {
		  Boolean value = (Boolean) aparam.getValue();
		  JBooleanField field = 
		    UIMaster.createBooleanField(value, sVSize);
		  pcomps[1] = field;
		  
		  field.addActionListener(this);
		  field.setActionCommand("action-param-changed:" + aparam.getName());
		  
		  field.setEnabled(!pIsLocked);
		  
		  hbox.add(field);
		}
		else if(aparam instanceof IntegerActionParam) {
		  Integer value = (Integer) aparam.getValue();
		  JIntegerField field = 
		    UIMaster.createIntegerField(value, sVSize, JLabel.CENTER);
		  pcomps[1] = field;
		  
		  field.addActionListener(this);
		  field.setActionCommand("action-param-changed:" + aparam.getName());
		  
		  field.setEnabled(!pIsLocked);
		  
		  hbox.add(field);
		}
		else if(aparam instanceof DoubleActionParam) {
		  Double value = (Double) aparam.getValue();
		  JDoubleField field = 
		    UIMaster.createDoubleField(value, sVSize, JLabel.CENTER);
		  pcomps[1] = field;
		  
		  field.addActionListener(this);
		  field.setActionCommand("action-param-changed:" + aparam.getName());
		  
		  field.setEnabled(!pIsLocked);
		  
		  hbox.add(field);
		}
		else if(aparam instanceof StringActionParam) {
		  String value = (String) aparam.getValue();
		  JTextField field = 
		    UIMaster.createEditableTextField(value, sVSize, JLabel.CENTER);
		  pcomps[1] = field;
		  
		  field.addActionListener(this);
		  field.setActionCommand("action-param-changed:" + aparam.getName());
		  
		  field.setEnabled(!pIsLocked);
		  
		  hbox.add(field);
		}
		else if(aparam instanceof EnumActionParam) {
		  EnumActionParam eparam = (EnumActionParam) aparam;
		  JCollectionField field = 
		    UIMaster.createCollectionField(eparam.getValues(), sVSize);
		  pcomps[1] = field;
		  
		  field.setSelected((String) eparam.getValue());
		  
		  field.addActionListener(this);
		  field.setActionCommand("action-param-changed:" + aparam.getName());
		  
		  field.setEnabled(!pIsLocked);
		  
		  hbox.add(field);
		}
		else if(aparam instanceof LinkActionParam) {
		  JCollectionField field = 
		    UIMaster.createCollectionField(pLinkActionParamValues, sVSize);
		  pcomps[1] = field;
		  
		  String source = (String) aparam.getValue();
		  int idx = pLinkActionParamNodeNames.indexOf(source);
		  if(idx != -1) 
		    field.setSelectedIndex(idx);
		  else 
		    field.setSelected("-");
		  
		  field.addActionListener(this);
		  field.setActionCommand("action-param-changed:" + aparam.getName());
		  
		  field.setEnabled(!pIsLocked);
		  
		  hbox.add(field);
		}
	      }
	      else {
		JLabel label = UIMaster.createLabel("-", sVSize, JLabel.CENTER);
		label.setName("TextFieldLabel");
		
		pcomps[1] = label;
		
		hbox.add(label);
	      }
	    }

	    hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	    {
	      JButton btn = new JButton();		 
	      pcomps[2] = btn;
	      btn.setName("SmallLeftArrowButton");
	      
	      Dimension size = new Dimension(12, 12);
	      btn.setMinimumSize(size);
	      btn.setMaximumSize(size);
	      btn.setPreferredSize(size);
	      
	      btn.addActionListener(this);
	      btn.setActionCommand("set-action-param:" + param.getName());
	      
	      btn.setEnabled(!pIsLocked && (waction != null) && (caction != null) && 
			     caction.getName().equals(waction.getName()));
	      
	      hbox.add(btn);
	    } 
	    
	    hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	    
	    {
	      BaseActionParam aparam = null;
	      if((caction != null) && 
		 ((waction == null) || caction.getName().equals(waction.getName())))
		aparam = caction.getSingleParam(param.getName());
	      
	      if(aparam != null) {
		String text = "-";
		{
		  if(aparam instanceof LinkActionParam) {
		    String source = (String) aparam.getValue();
		    int idx = pLinkActionParamNodeNames.indexOf(source);
		    if(idx != -1) 
		      text = pLinkActionParamValues.get(idx);
		  }
		  else {
		    Comparable value = aparam.getValue();
		    if(value != null)
		      text = value.toString();
		  }
		}
		
		JTextField field = UIMaster.createTextField(text, sVSize, JLabel.CENTER);
		pcomps[3] = field;
		
		hbox.add(field);
	      }
	      else {
		JLabel label = UIMaster.createLabel("-", sVSize, JLabel.CENTER);
		label.setName("TextFieldLabel");
		
		pcomps[3] = label;
		
		hbox.add(label);
	      }
	    }
	    
	    vpanel.add(hbox);
	  }
	  
	  pActionParamComponents.put(param.getName(), pcomps);
	}
      }
      
      /* per-source params */ 
      if(action.supportsSourceParams()) {
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	pEditSourceParamsDialog = null;
	pViewSourceParamsDialog = null;

	pSourceParamComponents = new Component[4];

	{
	  JLabel label = 
	    UIMaster.createFixedLabel("Source Parameters:", sTSize, JLabel.RIGHT);
	   pSourceParamComponents[0] = label;
	  
	  tpanel.add(label);
	}
	
	{ 
	  Box hbox = new Box(BoxLayout.X_AXIS);

	  if((waction != null) && waction.supportsSourceParams()) {
	    JButton btn = new JButton(pIsLocked ? "View..." : "Edit...");
	    pSourceParamComponents[1] = btn;
		
	    btn.setName("ValuePanelButton");
	    btn.setRolloverEnabled(false);
	    btn.setFocusable(false);
	    
	    Dimension size = new Dimension(sVSize, 19);
	    btn.setMinimumSize(size);
	    btn.setPreferredSize(size);
	    btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	    
	    btn.addActionListener(this);
	    btn.setActionCommand("edit-source-params");
	    
	    hbox.add(btn);

	    {
	      String title = pStatus.toString();

	      ArrayList<String> snames = new ArrayList<String>(pStatus.getSourceNames()); 
	      
	      ArrayList<String> stitles = new ArrayList<String>();
	      for(String sname : snames) 
		stitles.add(pStatus.getSource(sname).toString());

	      pEditSourceParamsDialog = 
		new JSourceParamsDialog(!pIsLocked, title, stitles, snames, waction);
	    }
	  }
	  else {
	    JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
	    pSourceParamComponents[1] = field;
	    
	    hbox.add(field);
	  }

	  hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	  {
	    JButton btn = new JButton();		 
	    pSourceParamComponents[2] = btn;
	    btn.setName("SmallLeftArrowButton");
	    
	    Dimension size = new Dimension(12, 12);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);
	    
	    btn.addActionListener(this);
	    btn.setActionCommand("set-source-params");
	    
	    btn.setEnabled(!pIsLocked && (waction != null) && (caction != null) && 
			   caction.getName().equals(waction.getName()));
	    
	    hbox.add(btn);
	  } 
	  
	  hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	  if((caction != null) && caction.supportsSourceParams()) {
	    JButton btn = new JButton("View...");
	    pSourceParamComponents[3] = btn;
		
	    btn.setName("ValuePanelButton");
	    btn.setRolloverEnabled(false);
	    btn.setFocusable(false);
	    
	    Dimension size = new Dimension(sVSize, 19);
	    btn.setMinimumSize(size);
	    btn.setPreferredSize(size);
	    btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	    
	    btn.addActionListener(this);
	    btn.setActionCommand("view-source-params");
	    
	    hbox.add(btn);

	    {
	      NodeVersion vsn = getCheckedInVersion();
	      String title = (pStatus.toString() + " (v" + vsn.getVersionID() + ")");

	      ArrayList<String> snames = new ArrayList<String>(pStatus.getSourceNames()); 
	      
	      ArrayList<String> stitles = new ArrayList<String>();
	      for(String sname : snames) 
		stitles.add(pStatus.getSource(sname).toString());

	      pViewSourceParamsDialog = 
		new JSourceParamsDialog(false, title, stitles, snames, caction);
	    }
	  }
	  else {
	    JTextField field = UIMaster.createTextField("-", sVSize, JLabel.CENTER);
	    pSourceParamComponents[3] = field;
	    
	    hbox.add(field);
	  }

	  vpanel.add(hbox);

	  doSourceParamsChanged();
	}	
      }
    }
    pActionParamsBox.add(comps[2]);

    pActionBox.revalidate();
    pActionBox.repaint();
  }


  /**
   * Update the UI components associated with the working and checked-in job requirements.
   * 
   * @param refresh
   *   Whether to reset the values of existing working components.
   */ 
  private void 
  updateJobRequirements
  (
   boolean refresh
  )
  {
    BaseAction waction = getWorkingAction();
    BaseAction caction = getCheckedInAction();

    BaseAction action = null;
    if(waction != null) 
      action = waction;
    else if(caction != null) 
      action = caction;

    /* job requirements */ 
    if(action != null) {
      NodeMod work = getWorkingVersion();
      JobReqs wjreq = null;
      if(work != null)
	wjreq = work.getJobRequirements();
      if((wjreq == null) && (waction != null)) 
	wjreq = JobReqs.defaultJobReqs();

      NodeVersion vsn = getCheckedInVersion();
      JobReqs cjreq = null;
      if((vsn != null) && (caction != null))
	cjreq = vsn.getJobRequirements();

      /* overflow policy */ 
      {
	if(refresh) {
	  pWorkingOverflowPolicyField.removeActionListener(this);
	  if(waction != null) {
	    OverflowPolicy policy = work.getOverflowPolicy();
	    if(policy == null) 
	      policy = OverflowPolicy.Abort;

	    pWorkingOverflowPolicyField.setValues(OverflowPolicy.titles());
	    pWorkingOverflowPolicyField.setSelectedIndex(policy.ordinal());
	  }
	  else {
	    ArrayList<String> values = new ArrayList<String>();
	    values.add("-");
	    pWorkingOverflowPolicyField.setValues(values);
	    pWorkingOverflowPolicyField.setSelected("-");
	  }
	  pWorkingOverflowPolicyField.addActionListener(this);
	  
	  pWorkingOverflowPolicyField.setEnabled(!pIsLocked && (waction != null));
	}
	
	pSetOverflowPolicyButton.setEnabled
	  (!pIsLocked && (waction != null) && (caction != null));
	
	if(caction != null)
	  pCheckedInOverflowPolicyField.setText(vsn.getOverflowPolicy().toTitle());
	else 
	  pCheckedInOverflowPolicyField.setText("-");

	doOverflowPolicyChanged();
      }

      /* execution method */ 
      {
	if(refresh) {
	  pWorkingExecutionMethodField.removeActionListener(this);
	  if(waction != null) {
	    ExecutionMethod method = work.getExecutionMethod();
	    if(method == null) 
	      method = ExecutionMethod.Serial;

	    pWorkingExecutionMethodField.setValues(ExecutionMethod.titles());
	    pWorkingExecutionMethodField.setSelectedIndex(method.ordinal());
	  }
	  else {
	    ArrayList<String> values = new ArrayList<String>();
	    values.add("-");
	    pWorkingExecutionMethodField.setValues(values);
	    pWorkingExecutionMethodField.setSelected("-");
	  }
	  pWorkingExecutionMethodField.addActionListener(this);
	  
	  pWorkingExecutionMethodField.setEnabled(!pIsLocked && (waction != null));
	}
	
	pSetExecutionMethodButton.setEnabled
	  (!pIsLocked && (waction != null) && (caction != null));
	
	if(caction != null)
	  pCheckedInExecutionMethodField.setText(vsn.getExecutionMethod().toTitle());
	else 
	  pCheckedInExecutionMethodField.setText("-");

	doExecutionMethodChanged();
      }

      /* batch size */ 
      { 
	if(refresh) {
	  pWorkingBatchSizeField.removeActionListener(this);
	  {
	    if(waction != null) 
	      pWorkingBatchSizeField.setValue(work.getBatchSize());
	    else 
	      pWorkingBatchSizeField.setValue(null);	
	  }
	  pWorkingBatchSizeField.addActionListener(this);
	}

	if((caction != null) && (vsn.getBatchSize() != null))
	  pCheckedInBatchSizeField.setText(vsn.getBatchSize().toString());
	else 
	  pCheckedInBatchSizeField.setText("-");

	doBatchSizeChanged();
      }

      /* priority */ 
      { 
	if(refresh) {
	  pWorkingPriorityField.removeActionListener(this);
	  {
	    if(wjreq != null) 
	      pWorkingPriorityField.setValue(wjreq.getPriority());
	    else 
	      pWorkingPriorityField.setValue(null);	
	  }
	  pWorkingPriorityField.addActionListener(this);
	  
	  pWorkingPriorityField.setEnabled(!pIsLocked && (wjreq != null));
	}

	pSetPriorityButton.setEnabled
	  (!pIsLocked && (wjreq != null) && (cjreq != null));
	
	if(cjreq != null)
	  pCheckedInPriorityField.setText(String.valueOf(cjreq.getPriority()));
	else 
	  pCheckedInPriorityField.setText("-");

	doPriorityChanged();
      }

      /* maximum load */ 
      { 
	if(refresh) {
	  pWorkingMaxLoadField.removeActionListener(this);
	  {
	    if(wjreq != null) 
	      pWorkingMaxLoadField.setValue(wjreq.getMaxLoad());
	    else 
	      pWorkingMaxLoadField.setValue(null);	
	  }
	  pWorkingMaxLoadField.addActionListener(this);
	  
	  pWorkingMaxLoadField.setEnabled(!pIsLocked && (wjreq != null));
	}

	pSetMaxLoadButton.setEnabled
	  (!pIsLocked && (wjreq != null) && (cjreq != null));
	
	if(cjreq != null)
	  pCheckedInMaxLoadField.setText(String.valueOf(cjreq.getMaxLoad()));
	else 
	  pCheckedInMaxLoadField.setText("-");

	doMaxLoadChanged();
      }

      /* minimum memory */ 
      { 
	if(refresh) {
	  pWorkingMinMemoryField.removeActionListener(this);
	  {
	    if(wjreq != null) 
	      pWorkingMinMemoryField.setValue(wjreq.getMinMemory());
	    else 
	      pWorkingMinMemoryField.setValue(null);	
	  }
	  pWorkingMinMemoryField.addActionListener(this);
	  
	  pWorkingMinMemoryField.setEnabled(!pIsLocked && (wjreq != null));
	}

	pSetMinMemoryButton.setEnabled
	  (!pIsLocked && (wjreq != null) && (cjreq != null));
	
	if(cjreq != null)
	  pCheckedInMinMemoryField.setText
	    (JByteSizeField.longToString(cjreq.getMinMemory()));
	else 
	  pCheckedInMinMemoryField.setText("-");

	doMinMemoryChanged();
      }

      /* minimum disk */ 
      { 
	if(refresh) {
	  pWorkingMinDiskField.removeActionListener(this);
	  {
	    if(wjreq != null) 
	      pWorkingMinDiskField.setValue(wjreq.getMinDisk());
	    else 
	      pWorkingMinDiskField.setValue(null);	
	  }
	  pWorkingMinDiskField.addActionListener(this);
	  
	  pWorkingMinDiskField.setEnabled(!pIsLocked && (wjreq != null));
	}

	pSetMinDiskButton.setEnabled
	  (!pIsLocked && (wjreq != null) && (cjreq != null));
	
	if(cjreq != null)
	  pCheckedInMinDiskField.setText
	    (JByteSizeField.longToString(cjreq.getMinDisk()));
	else 
	  pCheckedInMinDiskField.setText("-");

	doMinDiskChanged();
      }

      /* selection keys */ 
      {
	TreeSet<String> previous = new TreeSet<String>();
	if(!refresh) {
	  for(String kname : pSelectionKeyComponents.keySet()) {
	    Component pcomps[] = pSelectionKeyComponents.get(kname);
	    JBooleanField field = (JBooleanField) pcomps[1];
	    Boolean value = field.getValue();
	    if((value != null) && value) 
	      previous.add(kname);
	  }
	}

	TreeSet<String> knames = new TreeSet<String>();
	if(refresh) {
	  knames.clear();
	  UIMaster master = UIMaster.getInstance();
	  try {
	    knames.addAll(master.getQueueMgrClient().getSelectionKeyNames());
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
	}
	else {
	  knames.addAll(pSelectionKeyComponents.keySet());
	}

	pSelectionKeysBox.removeAll();
	pSelectionKeyComponents.clear();

	Component comps[] = createCommonPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
    
	if(knames.isEmpty()) {
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
	else {
	  boolean first = true; 
	  for(String kname : knames) {
	    boolean hasWorkingKey = false;
	    if(refresh) 
	      hasWorkingKey = (wjreq != null) && wjreq.getSelectionKeys().contains(kname);
	    else 
	      hasWorkingKey = previous.contains(kname);

	    boolean hasCheckedInKey = 
	      (cjreq != null) && cjreq.getSelectionKeys().contains(kname);

	    if(!first) 
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	    first = false;

	    Component pcomps[] = new Component[4];

	    {
	      JLabel label = 
		UIMaster.createFixedLabel(kname + ":", sTSize-7, JLabel.RIGHT);
	      pcomps[0] = label;

	      tpanel.add(label);
	    }

	    { 
	      Box hbox = new Box(BoxLayout.X_AXIS);

	      {
		JBooleanField field = UIMaster.createBooleanField(sVSize);
		pcomps[1] = field;

		if(wjreq != null)
		  field.setValue(hasWorkingKey);
		else 
		  field.setValue(null);

		field.setActionCommand("selection-key-changed:" + kname);
		field.addActionListener(this);

		field.setEnabled(!pIsLocked && (wjreq != null));

		hbox.add(field);
	      }

	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	      {
		JButton btn = new JButton();		 
		pcomps[2] = btn;
		btn.setName("SmallLeftArrowButton");

		Dimension size = new Dimension(12, 12);
		btn.setMinimumSize(size);
		btn.setMaximumSize(size);
		btn.setPreferredSize(size);

		btn.setActionCommand("set-selection-key:" + kname);
		btn.addActionListener(this);

		btn.setEnabled(!pIsLocked && (wjreq != null) && (cjreq != null));

		hbox.add(btn);
	      } 

	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	      {
		JTextField field = 
		  UIMaster.createTextField("-", sVSize, JLabel.CENTER);
		pcomps[3] = field;

		if(cjreq != null)
		  field.setText(hasCheckedInKey ? "YES" : "no");

		hbox.add(field);
	      }

	      vpanel.add(hbox);
	    }

	    pSelectionKeyComponents.put(kname, pcomps);

	    doSelectionKeyChanged(kname);
	  }
	}

	pSelectionKeysBox.add(comps[2]);
      }

      /* license keys */ 
      {
	TreeSet<String> previous = new TreeSet<String>();
	if(!refresh) {
	  for(String kname : pLicenseKeyComponents.keySet()) {
	    Component pcomps[] = pLicenseKeyComponents.get(kname);
	    JBooleanField field = (JBooleanField) pcomps[1];
	    Boolean value = field.getValue();
	    if((value != null) && value) 
	      previous.add(kname);
	  }
	}

	TreeSet<String> knames = new TreeSet<String>();
	if(refresh) {
	  knames.clear();
	  UIMaster master = UIMaster.getInstance();
	  try {
	    knames.addAll(master.getQueueMgrClient().getLicenseKeyNames());
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	  }
	}
	else {
	  knames.addAll(pLicenseKeyComponents.keySet());
	}

	pLicenseKeysBox.removeAll();
	pLicenseKeyComponents.clear();

	Component comps[] = createCommonPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];

	if(knames.isEmpty()) {
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
	else {
	  boolean first = true; 
	  for(String kname : knames) {
	    boolean hasWorkingKey = false;
	    if(refresh) 
	      hasWorkingKey = (wjreq != null) && wjreq.getLicenseKeys().contains(kname);
	    else 
	      hasWorkingKey = previous.contains(kname);
	    
	    boolean hasCheckedInKey = 
	      (cjreq != null) && cjreq.getLicenseKeys().contains(kname);
	    
	    if(!first) 
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	    first = false;
	    
	    Component pcomps[] = new Component[4];
	    
	    {
	      JLabel label = 
		UIMaster.createFixedLabel(kname + ":", sTSize-7, JLabel.RIGHT);
	      pcomps[0] = label;
	      
	      tpanel.add(label);
	    }
	    
	    { 
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      {
		JBooleanField field = UIMaster.createBooleanField(sVSize);
		pcomps[1] = field;
		
		if(wjreq != null)
		  field.setValue(hasWorkingKey);
		else 
		  field.setValue(null);
		
		field.setActionCommand("license-key-changed:" + kname);
		field.addActionListener(this);
		
		field.setEnabled(!pIsLocked && (wjreq != null));
		
		hbox.add(field);
	      }
	      
	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	      
	      {
		JButton btn = new JButton();		 
		pcomps[2] = btn;
		btn.setName("SmallLeftArrowButton");
		
		Dimension size = new Dimension(12, 12);
		btn.setMinimumSize(size);
		btn.setMaximumSize(size);
		btn.setPreferredSize(size);
		
		btn.setActionCommand("set-license-key:" + kname);
		btn.addActionListener(this);
		
		btn.setEnabled(!pIsLocked && (wjreq != null) && (cjreq != null));
		
		hbox.add(btn);
	      } 
	      
	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	      
	      {
		JTextField field = 
		  UIMaster.createTextField("-", sVSize, JLabel.CENTER);
		pcomps[3] = field;
		
		if(cjreq != null)
		  field.setText(hasCheckedInKey ? "YES" : "no");
		
		hbox.add(field);
	      }
	      
	      vpanel.add(hbox);
	    }
	    
	    pLicenseKeyComponents.put(kname, pcomps);
	    
	    doLicenseKeyChanged(kname);
	  }
	}

	pLicenseKeysBox.add(comps[2]);
      }

      pJobReqsBox.setVisible(true);
    }
    else {
      pJobReqsBox.setVisible(false);
    }

    pActionBox.revalidate();
    pActionBox.repaint();
  }

  /**
   * Update the color of the UI components associated with working and checked-in action.
   */ 
  private void 
  updateActionColors()
  {
    BaseAction waction = getWorkingAction();
    BaseAction caction = getCheckedInAction();

    BaseAction action = null;
    if(waction != null) 
      action = waction;
    else if(caction != null) 
      action = caction;

    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      if(!(((waction == null) && (caction == null)) ||
	   ((waction != null) && (caction != null) && 
	    waction.getName().equals(caction.getName()) && 
	    waction.getVersionID().equals(caction.getVersionID()))))
	color = Color.cyan;
      else 
	color = null;
    }
    
    Color fg = color;
    if(fg == null) 
      fg = Color.white;

    pActionTitle.setForeground(fg);
    pWorkingActionField.setForeground(fg);
    pCheckedInActionField.setForeground(fg);

    pActionVersionTitle.setForeground(fg);
    pWorkingActionVersionField.setForeground(fg);
    pCheckedInActionVersionField.setForeground(fg);

    updateActionEnabledColors();

    if(action != null) {
      for(BaseActionParam param : action.getSingleParams()) 
	updateActionParamColor(param.getName(), color);
    }
  }

  /**
   * Update the color of the action enabled UI components.
   */ 
  private void 
  updateActionEnabledColors()
  {
    Color color = Color.white;
    if(hasWorking() && hasCheckedIn() && 
       !pWorkingActionEnabledField.getText().equals(pCheckedInActionEnabledField.getText()))
      color = Color.cyan;

    pActionEnabledTitle.setForeground(color);
    pWorkingActionEnabledField.setForeground(color);
    pCheckedInActionEnabledField.setForeground(color);
  }

  /**
   * Update the color of the UI components associated with an action parameter.
   */ 
  private void
  updateActionParamColor
  (
   String pname, 
   Color color
  ) 
  {
    Component pcomps[] = pActionParamComponents.get(pname);
    if(pcomps == null)
      return;

    Color fg = color;
    if(fg == null) {
      BaseAction waction = getWorkingAction();
      BaseAction caction = getCheckedInAction();

      String wtext = null;
      {
	BaseActionParam aparam = null;
	if(waction != null) 
	  aparam = waction.getSingleParam(pname);
      
	if(aparam != null) {
	  if(aparam instanceof BooleanActionParam) {
	    JBooleanField field = (JBooleanField) pcomps[1];
	    if(field.getValue() != null) 
	      wtext = field.getValue().toString();
	  }
	  else if(aparam instanceof IntegerActionParam) {
	    JIntegerField field = (JIntegerField) pcomps[1];
	    if(field.getValue() != null) 
	      wtext = field.getValue().toString();
	  }
	  else if(aparam instanceof DoubleActionParam) {
	    JDoubleField field = (JDoubleField) pcomps[1];
	    if(field.getValue() != null) 
	      wtext = field.getValue().toString();
	  }
	  else if(aparam instanceof StringActionParam) {
	    JTextField field = (JTextField) pcomps[1];
	    wtext = field.getText();
	  }
	  else if(aparam instanceof EnumActionParam) {
	    JCollectionField field = (JCollectionField) pcomps[1];
	    wtext = field.getSelected();
	  }
	  else if(aparam instanceof LinkActionParam) {
	    JCollectionField field = (JCollectionField) pcomps[1];
	    wtext = pLinkActionParamNodeNames.get(field.getSelectedIndex());
	  }
	}
      }
      
      String ctext = null;
      {
	BaseActionParam aparam = null;
	if((caction != null) && 
	   ((waction == null) || caction.getName().equals(waction.getName())))
	  aparam = caction.getSingleParam(pname);
	
	if((aparam != null) && (aparam.getValue() != null))
	  ctext = aparam.getValue().toString();
      }

      if(((wtext == null) && (ctext == null)) ||
	 ((wtext != null) && wtext.equals(ctext)))
	fg = Color.white;
      else
	fg = Color.cyan;
    }
    
    {
      pcomps[0].setForeground(fg);
      
      if(pcomps[1] instanceof JIntegerField) 
	((JIntegerField) pcomps[1]).setWarningColor(fg);
      else if(pcomps[1] instanceof JDoubleField) 
	((JDoubleField) pcomps[1]).setWarningColor(fg);
      else 
	pcomps[1].setForeground(fg);
      
      pcomps[3].setForeground(fg);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);
  
    encoder.encode("VersionDrawerOpen",   pVersionDrawer.isOpen());
    encoder.encode("PropertyDrawerOpen",  pPropertyDrawer.isOpen());
    encoder.encode("ActionDrawerOpen",    pActionDrawer.isOpen());
    encoder.encode("JobReqsDrawerOpen",   pJobReqsDrawer.isOpen());
    encoder.encode("SelectionDrawerOpen", pSelectionDrawer.isOpen());
    encoder.encode("LicenseDrawerOpen",   pLicenseDrawer.isOpen());
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    {
      Boolean open = (Boolean) decoder.decode("VersionDrawerOpen");
      if(open != null) 
	pVersionDrawer.setIsOpen(open);
    }

    {
      Boolean open = (Boolean) decoder.decode("PropertyDrawerOpen");
      if(open != null) 
	pPropertyDrawer.setIsOpen(open);
    }

    {
      Boolean open = (Boolean) decoder.decode("ActionDrawerOpen");
      if(open != null) 
	pActionDrawer.setIsOpen(open);
    }

    {
      Boolean open = (Boolean) decoder.decode("JobReqsDrawerOpen");
      if(open != null) 
	pJobReqsDrawer.setIsOpen(open);
    }

    {
      Boolean open = (Boolean) decoder.decode("SelectionDrawerOpen");
      if(open != null) 
	pSelectionDrawer.setIsOpen(open);
    }

    {
      Boolean open = (Boolean) decoder.decode("LicenseDrawerOpen");
      if(open != null) 
	pLicenseDrawer.setIsOpen(open);
    }

    super.fromGlue(decoder);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
  mouseEntered
  (
   MouseEvent e
  ) 
  {
    requestFocusInWindow();
  }

  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited
  (
   MouseEvent e
  ) 
  {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
  }

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed(MouseEvent e) {}
  
  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}



  /*-- KEY LISTENER METHODS ----------------------------------------------------------------*/

  /**
   * invoked when a key has been pressed.
   */   
  public void 
  keyPressed
  (
   KeyEvent e
  )
  {
    UserPrefs prefs = UserPrefs.getInstance();

    if((prefs.getNodeDetailsApplyChanges() != null) &&
       prefs.getNodeDetailsApplyChanges().wasPressed(e) && 
       pApplyButton.isEnabled())
      doApply();
  }

  /**
   * Invoked when a key has been released.
   */ 
  public void 	
  keyReleased(KeyEvent e) {}

  /**
   * Invoked when a key has been typed.
   */ 
  public void 	
  keyTyped(KeyEvent e) {} 



  /*-- COMPONENT LISTENER METHODS ----------------------------------------------------------*/

  /**
   * Invoked when the component has been made invisible.
   */ 
  public void 	
  componentHidden(ComponentEvent e) {} 

  /**
   * Invoked when the component's position changes.
   */ 
  public void 
  componentMoved(ComponentEvent e) {} 

  /**
   * Invoked when the component's size changes.
   */ 
  public void 
  componentResized
  (
   ComponentEvent e
  )
  {
    Box box = (Box) e.getComponent();
    
    Dimension size = box.getComponent(1).getSize();

    JPanel spacer = (JPanel) box.getComponent(0);
    spacer.setMaximumSize(new Dimension(7, size.height));
    spacer.revalidate();
    spacer.repaint();
  }
  
  /**
   * Invoked when the component has been made visible.
   */
  public void 
  componentShown(ComponentEvent e) {}



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
    if(cmd.equals("apply")) 
      doApply();
    else if(cmd.equals("update-version")) 
      updateVersion();
    else if(cmd.equals("set-toolset")) 
      doSetToolset();
    else if(cmd.equals("toolset-changed")) 
      doToolsetChanged();
    else if(cmd.equals("set-editor")) 
      doSetEditor();
    else if(cmd.equals("editor-changed")) 
      doEditorChanged();
    else if(cmd.equals("set-action")) 
      doSetAction();
    else if(cmd.equals("action-changed")) 
      doActionChanged();
    else if(cmd.equals("action-enabled-changed")) 
      doActionEnabledChanged();
    else if(cmd.startsWith("set-action-param:")) 
      doSetActionParam(cmd.substring(17));
    else if(cmd.startsWith("action-param-changed:")) 
      doActionParamChanged(cmd.substring(21));
    else if(cmd.equals("edit-source-params")) 
      doEditSourceParams();
    else if(cmd.equals("view-source-params")) 
      doViewSourceParams();
    else if(cmd.equals("set-source-params")) 
      doSetSourceParams();
    else if(cmd.equals("set-overflow-policy")) 
      doSetOverflowPolicy();
    else if(cmd.equals("overflow-policy-changed")) 
      doOverflowPolicyChanged();
    else if(cmd.equals("set-execution-method")) 
      doSetExecutionMethod();
    else if(cmd.equals("execution-method-changed")) 
      doExecutionMethodChanged();
    else if(cmd.equals("set-batch-size")) 
      doSetBatchSize();
    else if(cmd.equals("batch-size-changed")) 
      doBatchSizeChanged();
    else if(cmd.equals("set-priority")) 
      doSetPriority();
    else if(cmd.equals("priority-changed")) 
      doPriorityChanged();
    else if(cmd.equals("set-maximum-load")) 
      doSetMaxLoad();
    else if(cmd.equals("maximum-load-changed")) 
      doMaxLoadChanged();
    else if(cmd.equals("set-minimum-memory")) 
      doSetMinMemory();
    else if(cmd.equals("minimum-memory-changed")) 
      doMinMemoryChanged();
    else if(cmd.equals("set-minimum-disk")) 
      doSetMinDisk();
    else if(cmd.equals("minimum-disk-changed")) 
      doMinDiskChanged();
    else if(cmd.startsWith("selection-key-changed:")) 
      doSelectionKeyChanged(cmd.substring(22));
    else if(cmd.startsWith("set-selection-key:")) 
      doSetSelectionKey(cmd.substring(18));
    else if(cmd.startsWith("license-key-changed:")) 
      doLicenseKeyChanged(cmd.substring(20));
    else if(cmd.startsWith("set-license-key:")) 
      doSetLicenseKey(cmd.substring(16));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Modify the working version of the node based on the current settings if the 
   * UI components.
   */ 
  private void 
  doApply()
  {
    if(!pIsLocked && (pStatus != null) && (pStatus.getDetails() != null)) {
      NodeMod work = pStatus.getDetails().getWorkingVersion();
      if(work != null) {
	try { 
	  NodeMod mod = new NodeMod(work);
	  mod.removeAllSources();

	  /* properties panel */ 
	  {
	    String toolset = pWorkingToolsetField.getSelected();
	    if((toolset != null) && !toolset.equals("-"))
	      mod.setToolset(toolset);
	    
	    String editor = pWorkingEditorField.getSelected();
	    if((editor != null) && !editor.equals("-"))
	      mod.setEditor(editor);
	    else 
	      mod.setEditor(null);	
	  }
	
	  /* action panel */ 
	  {
	    BaseAction waction = getWorkingAction();
	    if(waction != null) {

	      /* single valued parameters */ 
	      for(BaseActionParam aparam : waction.getSingleParams()) {
		Component pcomps[] = pActionParamComponents.get(aparam.getName());
		Comparable value = null;
		if(aparam instanceof BooleanActionParam) {   
		  JBooleanField field = (JBooleanField) pcomps[1];
		  value = field.getValue();
		}
		else if(aparam instanceof IntegerActionParam) {   
		  JIntegerField field = (JIntegerField) pcomps[1];
		  value = field.getValue();
		}
		else if(aparam instanceof DoubleActionParam) { 
		  JDoubleField field = (JDoubleField) pcomps[1];
		  value = field.getValue();
		}
		else if(aparam instanceof StringActionParam) {
		  JTextField field = (JTextField) pcomps[1];
		  value = field.getText();
		}
		else if(aparam instanceof EnumActionParam) {
		  JCollectionField field = (JCollectionField) pcomps[1];
		  EnumActionParam eparam = (EnumActionParam) aparam;
		  value = eparam.getValueOfIndex(field.getSelectedIndex());
		}
		else if(aparam instanceof LinkActionParam) {
		  JCollectionField field = (JCollectionField) pcomps[1];
		  value = pLinkActionParamNodeNames.get(field.getSelectedIndex());
		}
		
		waction.setSingleParamValue(aparam.getName(), value);
	      }

	      mod.setAction(waction);

	      /* action enabled */ 
	      {
		Boolean enabled = pWorkingActionEnabledField.getValue();
		if(enabled != null) 
		  mod.setActionEnabled(enabled);
	      }

	      /* overflow policy */ 
	      {
		int idx = pWorkingOverflowPolicyField.getSelectedIndex();
		mod.setOverflowPolicy(OverflowPolicy.values()[idx]);
	      } 
	      
	      /* execution method */ 
	      {
		int idx = pWorkingExecutionMethodField.getSelectedIndex();
		mod.setExecutionMethod(ExecutionMethod.values()[idx]);
		
		/* batch size */ 
		if(idx == 1) {
		  Integer size = pWorkingBatchSizeField.getValue();
		  if((size == null) || (size < 0)) {
		    pWorkingBatchSizeField.setValue(0);
		    size = 0;
		  }
		  
		  mod.setBatchSize(size);
		}
	      }
	    }
	    else {
	      mod.setAction(null);
	    }	  

	    setWorkingAction(null);
	  }

	  /* job requirements */ 
	  JobReqs jreq = mod.getJobRequirements();
	  if(jreq != null) {
	    
	    /* priority */ 
	    {
	      Integer priority = pWorkingPriorityField.getValue();
	      if(priority == null) 
		pWorkingPriorityField.setValue(jreq.getPriority());
	      else 
		jreq.setPriority(priority);
	    }
	    
	    /* maximum load */ 
	    {
	      Float load = pWorkingMaxLoadField.getValue();
	      if((load == null) || (load <= 0.0) || (load > 20.0))
		pWorkingMaxLoadField.setValue(jreq.getMaxLoad());
	      else 	      
		jreq.setMaxLoad(load);
	    }
	    
	    /* minimum memory */ 
	    {
	      Long memory = pWorkingMinMemoryField.getValue();
	      if(memory == null) 
		pWorkingMinMemoryField.setValue(jreq.getMinMemory());
	      else
		jreq.setMinMemory(memory);
	    }
	    
	    /* minimum disk */ 
	    {
	      Long disk = pWorkingMinDiskField.getValue();
	      if(disk == null) 
		pWorkingMinDiskField.setValue(jreq.getMinDisk());
	      else
		jreq.setMinDisk(disk);
	    }

	    /* selection keys */ 
	    {
	      jreq.removeAllSelectionKeys();

	      for(String kname : pSelectionKeyComponents.keySet()) {
		Component pcomps[] = pSelectionKeyComponents.get(kname);
		JBooleanField field = (JBooleanField) pcomps[1];
		Boolean value = field.getValue();
		if((value != null) && value) 
		  jreq.addSelectionKey(kname);
	      }
	    }

	    /* license keys */ 
	    {
	      jreq.removeAllLicenseKeys();

	      for(String kname : pLicenseKeyComponents.keySet()) {
		Component pcomps[] = pLicenseKeyComponents.get(kname);
		JBooleanField field = (JBooleanField) pcomps[1];
		Boolean value = field.getValue();
		if((value != null) && value) 
		  jreq.addLicenseKey(kname);
	      }
	    }

	    mod.setJobRequirements(jreq);
	  }	

	  pApplyButton.setEnabled(false);
	  
	  ModifyTask task = new ModifyTask(mod);
	  task.start();
	}
	catch(PipelineException ex) {
	  UIMaster.getInstance().showErrorDialog(ex);
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working toolset field from the value of the checked-in field.
   */ 
  private void 
  doSetToolset()
  { 
    pWorkingToolsetField.removeActionListener(this);
    {
      String toolset = pCheckedInToolsetField.getText();
      if(!toolset.equals("-")) {
	if(!pWorkingToolsetField.getValues().contains(toolset)) {
	  TreeSet<String> values = new TreeSet<String>(pWorkingToolsetField.getValues());
	  values.add(toolset);
	  pWorkingToolsetField.setValues(values);
	}
	
	pWorkingToolsetField.setSelected(toolset);
      }
    }
    pWorkingToolsetField.addActionListener(this);
  
    doToolsetChanged();
  }

  /**
   * Update the appearance of the toolset field after a change of value.
   */ 
  private void 
  doToolsetChanged() 
  {
    pApplyButton.setEnabled(true);

    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String wtset = pWorkingToolsetField.getSelected();
      String ctset = pCheckedInToolsetField.getText();
      if(!ctset.equals(wtset))
	color = Color.cyan;
    }

    pToolsetTitle.setForeground(color);
    pWorkingToolsetField.setForeground(color);
    pCheckedInToolsetField.setForeground(color);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working editor field from the value of the checked-in field.
   */ 
  private void 
  doSetEditor()
  { 
    pWorkingEditorField.removeActionListener(this);
    {
      String editor = pCheckedInEditorField.getText();
      if(pWorkingEditorField.getValues().contains(editor))
	pWorkingEditorField.setSelected(editor);
      else 
	pWorkingEditorField.setSelected("-");
    }
    pWorkingEditorField.addActionListener(this);

    doEditorChanged();
  }

  /**
   * Update the appearance of the editor field after a change of value.
   */ 
  private void 
  doEditorChanged() 
  {
    pApplyButton.setEnabled(true);

    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String weditor = pWorkingEditorField.getSelected();
      String ceditor = pCheckedInEditorField.getText();
      if(!ceditor.equals(weditor))
	color = Color.cyan;
    }

    pEditorTitle.setForeground(color);
    pWorkingEditorField.setForeground(color);
    pCheckedInEditorField.setForeground(color);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working action and parameter fields from the value of the checked-in action.
   */ 
  private void 
  doSetAction()
  { 
    BaseAction oaction = getWorkingAction();

    pWorkingActionField.removeActionListener(this);
    {
      NodeVersion vsn = getCheckedInVersion();
      BaseAction action = null;
      if(vsn != null) 
	action = vsn.getAction();
      
      if((action != null) && 
	 pWorkingActionField.getValues().contains(action.getName())) {
	pWorkingActionField.setSelected(action.getName());
	
	pWorkingActionEnabledField.setValue(vsn.isActionEnabled());
	pWorkingActionEnabledField.setEnabled(true);	  
      }
      else {
	pWorkingActionField.setSelected("-");
	
	pWorkingActionEnabledField.setValue(null);
	pWorkingActionEnabledField.setEnabled(false);
      }
      setWorkingAction(action);
    }
    pWorkingActionField.addActionListener(this);

    updateActionVersionFields();

    pActionParamComponents.clear();
    updateActionParams();
    updateActionColors();

    updateJobRequirements((oaction == null) && (getWorkingAction() != null));
  }

  /**
   * Update the appearance of the action fields after a change of value.
   */ 
  private void 
  doActionChanged()
  {
    pApplyButton.setEnabled(true);

    BaseAction oaction = getWorkingAction();
    {
      String aname = pWorkingActionField.getSelected();
      if(aname.equals("-")) {
	setWorkingAction(null);

	pWorkingActionEnabledField.setValue(null);
	pWorkingActionEnabledField.setEnabled(false);

	pActionParamComponents.clear();
      }
      else {
	VersionID vid = null;
	boolean rebuild = false;
	if((oaction == null) || !oaction.getName().equals(aname)) 
	  rebuild = true;
	else {
	  String vstr = pWorkingActionVersionField.getSelected();
	  if(vstr.equals("-")) 
	    rebuild = true;
	  else {
	    vid = new VersionID(vstr.substring(1));
	    if(!vid.equals(oaction.getVersionID()))
	      rebuild = true;
	  }
	}

	if(rebuild) {
	  try {
	    setWorkingAction(PluginMgr.getInstance().newAction(aname, vid));
	    
	    BaseAction waction = getWorkingAction();
	    if((oaction != null) && oaction.getName().equals(waction.getName())) {
	      waction.setSingleParamValues(oaction);
	      waction.setSourceParamValues(oaction);
	    }

	    if(pWorkingActionEnabledField.getValue() == null) 
	      pWorkingActionEnabledField.setValue(true);
	    pWorkingActionEnabledField.setEnabled(true);	  
	  }
	  catch(PipelineException ex) {
	    UIMaster.getInstance().showErrorDialog(ex);
	    
	    setWorkingAction(null);
	    
	    pWorkingActionEnabledField.setValue(null);
	    pWorkingActionEnabledField.setEnabled(false);
	    
	    pWorkingActionField.removeActionListener(this);
  	    pWorkingActionField.setSelected("-");
	    pWorkingActionField.addActionListener(this);
	  }

	  pActionParamComponents.clear();
	}
      }

      updateActionVersionFields();
      updateActionParams();
      updateActionColors();
    }

    updateJobRequirements((oaction == null) && (getWorkingAction() != null));
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the appearance of the action enabled fields after a change of value.
   */ 
  private void 
  doActionEnabledChanged() 
  {
    pApplyButton.setEnabled(true);
    updateActionEnabledColors();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working action parameter field from the value of the checked-in action parameter.
   */ 
  private void 
  doSetActionParam
  (
   String pname   
  ) 
  { 
    BaseAction waction = getWorkingAction();
    BaseAction caction = getCheckedInAction();

    BaseActionParam wparam = null;
    if(waction != null) 
      wparam = waction.getSingleParam(pname);
      
    BaseActionParam cparam = null;
    if(caction != null) 
      cparam = caction.getSingleParam(pname);
      
    if((wparam != null) && (cparam != null) && waction.getName().equals(caction.getName())) {
      Component pcomps[] = pActionParamComponents.get(pname);
      if(pcomps != null) {
	Comparable value = cparam.getValue();
	if(wparam instanceof BooleanActionParam) {
	  JBooleanField field = (JBooleanField) pcomps[1];
	  field.setValue((Boolean) value);
	}
	else if(wparam instanceof IntegerActionParam) {
	  JIntegerField field = (JIntegerField) pcomps[1];
	  field.setValue((Integer) value);
	}
	else if(wparam instanceof DoubleActionParam) {
	  JDoubleField field = (JDoubleField) pcomps[1];
	  field.setValue((Double) value);
	}
	else if(wparam instanceof StringActionParam) {
	  JTextField field = (JTextField) pcomps[1];
	  if(value != null) 
	    field.setText(value.toString());
	  else 
	    field.setText(null);
	}
	else if(wparam instanceof EnumActionParam) {
	  JCollectionField field = (JCollectionField) pcomps[1];
	  field.setSelected(value.toString());
	}
	else if(wparam instanceof LinkActionParam) {
	  JCollectionField field = (JCollectionField) pcomps[1];
	  
	  int idx = pLinkActionParamNodeNames.indexOf(value);
	  if(idx != -1) 
	    field.setSelectedIndex(idx);
	}

	doActionParamChanged(pname);
      }
    }
  }

  /**
   * Update the appearance of the action parameter fields after a change of parameter value.
   */ 
  private void 
  doActionParamChanged
  (
   String pname
  ) 
  {
    pApplyButton.setEnabled(true);
  
    updateActionParamColor(pname, null);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Show a dialog for editing the working per-source parameters.
   */ 
  private void 
  doEditSourceParams() 
  {
    pEditSourceParamsDialog.setVisible(true);

    if(pEditSourceParamsDialog.wasConfirmed()) {
      pEditSourceParamsDialog.updateParams(pWorkingAction);
      doSourceParamsChanged();
    }
  }

  /**
   * Show a dialog for viewing the checked-in per-source parameters.
   */ 
  private void 
  doViewSourceParams() 
  {
    pViewSourceParamsDialog.setVisible(true);
  }  

  /**
   * Set the working per-source parameters from the checked-in per-source parameters.
   */ 
  private void 
  doSetSourceParams() 
  {
    BaseAction waction = getWorkingAction();
    BaseAction caction = getCheckedInAction();
    if((waction != null) && (caction != null)) {
      waction.removeAllSourceParams();
      waction.setSourceParamValues(caction);
      
      {
	String title = pStatus.toString();
	
	ArrayList<String> snames = new ArrayList<String>(pStatus.getSourceNames()); 
	
	ArrayList<String> stitles = new ArrayList<String>();
	for(String sname : snames) 
	  stitles.add(pStatus.getSource(sname).toString());
	
	pEditSourceParamsDialog = 
	  new JSourceParamsDialog(!pIsLocked, title, stitles, snames, waction);
      }

      doSourceParamsChanged();
    }
  }

  /**
   * Update the appearance of the edit/view source params button after a change of value.
   */ 
  private void 
  doSourceParamsChanged()
  {
    pApplyButton.setEnabled(true);

    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      BaseAction waction = getWorkingAction();
      BaseAction caction = getCheckedInAction();
      if(((waction != null) && (caction == null)) ||
	 ((caction != null) && (waction == null))) {
	color = Color.cyan;
      }
      else if((waction != null) && (caction != null)) {
	if(!waction.equalSourceParams(caction)) 
	  color = Color.cyan;
      }
    }

    int wk;
    for(wk=0; wk<pSourceParamComponents.length; wk++)
      pSourceParamComponents[wk].setForeground(color);
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working overflow policy field from the value of the checked-in field.
   */ 
  private void 
  doSetOverflowPolicy()
  { 
    pWorkingOverflowPolicyField.removeActionListener(this);
      pWorkingOverflowPolicyField.setSelected(pCheckedInOverflowPolicyField.getText());
    pWorkingOverflowPolicyField.addActionListener(this);

    doOverflowPolicyChanged();
  }

  /**
   * Update the appearance of the overflow policy field after a change of value.
   */ 
  private void 
  doOverflowPolicyChanged()
  {
    pApplyButton.setEnabled(true);
    
    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String wpolicy = pWorkingOverflowPolicyField.getSelected();
      String cpolicy = pCheckedInOverflowPolicyField.getText();      
      if(!cpolicy.equals(wpolicy))
	color = Color.cyan;
    }

    pOverflowPolicyTitle.setForeground(color);
    pWorkingOverflowPolicyField.setForeground(color);
    pCheckedInOverflowPolicyField.setForeground(color);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working execution method field from the value of the checked-in field.
   */ 
  private void 
  doSetExecutionMethod()
  { 
    pWorkingExecutionMethodField.removeActionListener(this);
      pWorkingExecutionMethodField.setSelected(pCheckedInExecutionMethodField.getText());
    pWorkingExecutionMethodField.addActionListener(this);

    doExecutionMethodChanged();
  }

  /**
   * Update the appearance of the execution method field after a change of value.
   */ 
  private void 
  doExecutionMethodChanged()
  {
    pApplyButton.setEnabled(true);

    String cmethod = null;
    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String wmethod = pWorkingExecutionMethodField.getSelected();
      cmethod = pCheckedInExecutionMethodField.getText();      
      if(!cmethod.equals(wmethod))
	color = Color.cyan;
    }

    pExecutionMethodTitle.setForeground(color);
    pWorkingExecutionMethodField.setForeground(color);
    pCheckedInExecutionMethodField.setForeground(color);

    if((getWorkingAction() == null) || 
       (pWorkingExecutionMethodField.getSelectedIndex() == 0)) {
      pWorkingBatchSizeField.setValue(null);
      pWorkingBatchSizeField.setEnabled(false);
      pSetBatchSizeButton.setEnabled(false);
    }
    else {      
      if(pWorkingBatchSizeField.getValue() == null) 
	pWorkingBatchSizeField.setValue(0);
      pWorkingBatchSizeField.setEnabled(!pIsLocked);
      pSetBatchSizeButton.setEnabled
	(!pIsLocked && (cmethod != null) && (cmethod.equals("Parallel")));
    }

    doBatchSizeChanged();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working batch size field from the value of the checked-in field.
   */ 
  private void 
  doSetBatchSize()
  { 
    pWorkingBatchSizeField.removeActionListener(this);
      pWorkingBatchSizeField.setText(pCheckedInBatchSizeField.getText());
    pWorkingBatchSizeField.addActionListener(this);

    doBatchSizeChanged();
  }

  /**
   * Update the appearance of the batch size field after a change of value.
   */ 
  private void 
  doBatchSizeChanged()
  {
    pApplyButton.setEnabled(true);
    
    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String wmethod = pWorkingExecutionMethodField.getSelected();
      String cmethod = pCheckedInExecutionMethodField.getText();     
 
      String wsize = pWorkingBatchSizeField.getText();
      String csize = pCheckedInBatchSizeField.getText();      
      if(!cmethod.equals(wmethod) || !csize.equals(wsize))
	color = Color.cyan;
    }

    pBatchSizeTitle.setForeground(color);
    pWorkingBatchSizeField.setWarningColor(color);
    pCheckedInBatchSizeField.setForeground(color);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working priority field from the value of the checked-in field.
   */ 
  private void 
  doSetPriority()
  { 
    pWorkingPriorityField.removeActionListener(this);
      pWorkingPriorityField.setText(pCheckedInPriorityField.getText());
    pWorkingPriorityField.addActionListener(this);

    doPriorityChanged();
  }

  /**
   * Update the appearance of the priority field after a change of value.
   */ 
  private void 
  doPriorityChanged()
  {
    pApplyButton.setEnabled(true);
    
    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String wpriority = pWorkingPriorityField.getText();
      String cpriority = pCheckedInPriorityField.getText();      
      if(!cpriority.equals(wpriority)) {
	color = Color.cyan;
      }
    }

    pPriorityTitle.setForeground(color);
    pWorkingPriorityField.setWarningColor(color);
    pCheckedInPriorityField.setForeground(color);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working maximum load field from the value of the checked-in field.
   */ 
  private void 
  doSetMaxLoad()
  { 
    pWorkingMaxLoadField.removeActionListener(this);
      pWorkingMaxLoadField.setText(pCheckedInMaxLoadField.getText());
    pWorkingMaxLoadField.addActionListener(this);

    doMaxLoadChanged();
  }

  /**
   * Update the appearance of the maximum load field after a change of value.
   */ 
  private void 
  doMaxLoadChanged()
  {
    pApplyButton.setEnabled(true);
    
    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String wload = pWorkingMaxLoadField.getText();
      String cload = pCheckedInMaxLoadField.getText();      
      if(!cload.equals(wload))
	color = Color.cyan;
    }

    pMaxLoadTitle.setForeground(color);
    pWorkingMaxLoadField.setWarningColor(color);
    pCheckedInMaxLoadField.setForeground(color);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working minimum memory field from the value of the checked-in field.
   */ 
  private void 
  doSetMinMemory()
  { 
    pWorkingMinMemoryField.removeActionListener(this);
      pWorkingMinMemoryField.setText(pCheckedInMinMemoryField.getText());
    pWorkingMinMemoryField.addActionListener(this);

    doMinMemoryChanged();
  }

  /**
   * Update the appearance of the minimum memory field after a change of value.
   */ 
  private void 
  doMinMemoryChanged()
  {
    pApplyButton.setEnabled(true);
    
    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String wmem = pWorkingMinMemoryField.getText();
      String cmem = pCheckedInMinMemoryField.getText();      
      if(!cmem.equals(wmem))
	color = Color.cyan;
    }

    pMinMemoryTitle.setForeground(color);
    pWorkingMinMemoryField.setWarningColor(color);
    pCheckedInMinMemoryField.setForeground(color);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the working minimum disk field from the value of the checked-in field.
   */ 
  private void 
  doSetMinDisk()
  { 
    pWorkingMinDiskField.removeActionListener(this);
      pWorkingMinDiskField.setText(pCheckedInMinDiskField.getText());
    pWorkingMinDiskField.addActionListener(this);

    doMinDiskChanged();
  }

  /**
   * Update the appearance of the minimum disk field after a change of value.
   */ 
  private void 
  doMinDiskChanged()
  {
    pApplyButton.setEnabled(true);
    
    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String wdisk = pWorkingMinDiskField.getText();
      String cdisk = pCheckedInMinDiskField.getText();      
      if(!cdisk.equals(wdisk))
	color = Color.cyan;
    }

    pMinDiskTitle.setForeground(color);
    pWorkingMinDiskField.setWarningColor(color);
    pCheckedInMinDiskField.setForeground(color);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the selection key field with the given name from the value of the checked-in field.
   */ 
  private void 
  doSetSelectionKey
  (
   String kname
  ) 
  { 
    Component pcomps[] = pSelectionKeyComponents.get(kname);
    if(pcomps != null) {
      JBooleanField wfield = (JBooleanField) pcomps[1];

      String ckey = ((JTextField) pcomps[3]).getText();
      if(ckey.equals("YES"))
	wfield.setValue(true);
      else if(ckey.equals("no"))
	wfield.setValue(false);

      doSelectionKeyChanged(kname);
    }
  }

  /**
   * Update the appearance of the selection key field with the given name after a 
   * change of value.
   */ 
  private void 
  doSelectionKeyChanged
  (
   String kname
  ) 
  {
    Component pcomps[] = pSelectionKeyComponents.get(kname);
    if(pcomps != null) {
      pApplyButton.setEnabled(true);
    
      Color color = Color.white;
      if(hasWorking() && hasCheckedIn()) {
	String wkey = ((JBooleanField) pcomps[1]).getText();
	String ckey = ((JTextField) pcomps[3]).getText();
	if(!ckey.equals(wkey))
	  color = Color.cyan;
      }

      pcomps[0].setForeground(color);
      pcomps[1].setForeground(color);
      pcomps[3].setForeground(color);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the license key field with the given name from the value of the checked-in field.
   */ 
  private void 
  doSetLicenseKey
  (
   String kname
  ) 
  { 
    Component pcomps[] = pLicenseKeyComponents.get(kname);
    if(pcomps != null) {
      JBooleanField wfield = (JBooleanField) pcomps[1];

      String ckey = ((JTextField) pcomps[3]).getText();
      if(ckey.equals("YES"))
	wfield.setValue(true);
      else if(ckey.equals("no"))
	wfield.setValue(false);

      doLicenseKeyChanged(kname);
    }
  }

  /**
   * Update the appearance of the license key field with the given name after a 
   * change of value.
   */ 
  private void 
  doLicenseKeyChanged
  (
   String kname
  ) 
  {
    Component pcomps[] = pLicenseKeyComponents.get(kname);
    if(pcomps != null) {
      pApplyButton.setEnabled(true);
    
      Color color = Color.white;
      if(hasWorking() && hasCheckedIn()) {
	String wkey = ((JBooleanField) pcomps[1]).getText();
	String ckey = ((JTextField) pcomps[3]).getText();
	if(!ckey.equals(wkey))
	  color = Color.cyan;
      }

      pcomps[0].setForeground(color);
      pcomps[1].setForeground(color);
      pcomps[3].setForeground(color);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Modify the properties of the given working version of a node.
   */ 
  private
  class ModifyTask
    extends Thread
  {
    public 
    ModifyTask
    (
     NodeMod mod
    ) 
    {
      super("JNodeDetailsPanel:ModifyTask");

      pNodeMod = mod;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp("Modifying Node...")) {
	try {
	  master.getMasterMgrClient().modifyProperties(pAuthor, pView, pNodeMod);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}

	if(pGroupID > 0) {
	  PanelGroup<JNodeViewerPanel> panels = master.getNodeViewerPanels();
	  JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	  if(viewer != null) 
	    viewer.updateRoots();
	}
      }
    }

    private NodeMod  pNodeMod;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2714804145579513176L;


  private static final int  sTSize = 120;
  private static final int  sVSize = 150;
  private static final int  sSSize = 323;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current node status.
   */ 
  private NodeStatus  pStatus;

  /**
   * Cached checked-in versions associated with the current node.
   */ 
  private TreeMap<VersionID,NodeVersion>  pCheckedInVersions; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node name/state header.
   */ 
  private JLabel pHeaderLabel;
  
  /**
   * The fully resolved node name field.
   */ 
  private JTextField pNodeNameField;
  
  /**
   * The button used to apply changes to the working version of the node.
   */ 
  private JButton  pApplyButton;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The version state field.
   */ 
  private JTextField pVersionStateField;

  /**
   * The base revision number field.
   */ 
  private JTextField pBaseVersionField;

  /**
   * The checked-in revision numbers field.
   */ 
  private JCollectionField pCheckedInVersionField;

  /**
   * The drawer containing the version components.
   */ 
  private JDrawer  pVersionDrawer;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The property state field.
   */ 
  private JTextField pPropertyStateField;


  /**
   * The toolset title label.
   */ 
  private JLabel  pToolsetTitle;

  /**
   * The working toolset field.
   */ 
  private JCollectionField pWorkingToolsetField;

  /**
   * The set toolset button.
   */ 
  private JButton  pSetToolsetButton;

  /**
   * The checked-in toolset field.
   */ 
  private JTextField pCheckedInToolsetField;


  /**
   * The editor title label.
   */ 
  private JLabel  pEditorTitle;

  /**
   * The working editor field.
   */ 
  private JCollectionField pWorkingEditorField;

  /**
   * The set editor button.
   */ 
  private JButton  pSetEditorButton;

  /**
   * The checked-in editor field.
   */ 
  private JTextField pCheckedInEditorField;

  /**
   * The drawer containing the property components.
   */ 
  private JDrawer  pPropertyDrawer;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The top-level container of the actions drawer.
   */ 
  private Box  pActionBox;


  /**
   * The action title label.
   */ 
  private JLabel  pActionTitle;

  /**
   * The working action field.
   */ 
  private JCollectionField pWorkingActionField;

  /**
   * The set action button.
   */ 
  private JButton  pSetActionButton;

  /**
   * The checked-in action field.
   */ 
  private JTextField pCheckedInActionField;


  /**
   * The action version title label.
   */ 
  private JLabel  pActionVersionTitle;

  /**
   * The working action version field.
   */ 
  private JCollectionField pWorkingActionVersionField;

  /**
   * The checked-in action version field.
   */ 
  private JTextField pCheckedInActionVersionField;



  /**
   * The action enabled title label.
   */ 
  private JLabel  pActionEnabledTitle;

  /**
   * The working action enabled field.
   */ 
  private JBooleanField pWorkingActionEnabledField;

  /**
   * The checked-in action enabled field.
   */ 
  private JTextField pCheckedInActionEnabledField;


  /**
   * The action parameters container.
   */ 
  private Box  pActionParamsBox;

  /**
   * The temporary working regeneration action.
   */ 
  private BaseAction  pWorkingAction;

  /**
   * The title, working and checked-in action parameter components indexed by 
   * action parameter name.
   */ 
  private TreeMap<String,Component[]>  pActionParamComponents;

  /**
   * The JCollectionField values and corresponding fully resolved names of the 
   * upstream nodes used by LinkActionParam fields.
   */ 
  private ArrayList<String>  pLinkActionParamValues;
  private ArrayList<String>  pLinkActionParamNodeNames;

  /**
   * The UI compontents related to per-source action parameters.
   */ 
  private Component pSourceParamComponents[]; 


  /**
   * The dialog used to edit/view working per-source parameters.
   */ 
  private JSourceParamsDialog  pEditSourceParamsDialog;

  /**
   * The dialog used to view checked-in per-source parameters.
   */ 
  private JSourceParamsDialog  pViewSourceParamsDialog;

  /**
   * The drawer containing the action components.
   */ 
  private JDrawer  pActionDrawer;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The job requirements container.
   */ 
  private Box  pJobReqsBox;


  /**
   * The overflow policy title label.
   */ 
  private JLabel  pOverflowPolicyTitle;

  /**
   * The working overflow policy field.
   */ 
  private JCollectionField pWorkingOverflowPolicyField;

  /**
   * The set overflow policy button.
   */ 
  private JButton  pSetOverflowPolicyButton;

  /**
   * The checked-in overflow policy field.
   */ 
  private JTextField pCheckedInOverflowPolicyField;


  /**
   * The execution method title label.
   */ 
  private JLabel  pExecutionMethodTitle;

  /**
   * The working execution method field.
   */ 
  private JCollectionField pWorkingExecutionMethodField;

  /**
   * The set execution method button.
   */ 
  private JButton  pSetExecutionMethodButton;

  /**
   * The checked-in execution method field.
   */ 
  private JTextField pCheckedInExecutionMethodField;


  /**
   * The batch size title label.
   */ 
  private JLabel  pBatchSizeTitle;

  /**
   * The working batch size field.
   */ 
  private JIntegerField pWorkingBatchSizeField;

  /**
   * The set batch size button.
   */ 
  private JButton  pSetBatchSizeButton;

  /**
   * The checked-in batch size field.
   */ 
  private JTextField pCheckedInBatchSizeField;


  /**
   * The priority title label.
   */ 
  private JLabel  pPriorityTitle;

  /**
   * The working priority field.
   */ 
  private JIntegerField pWorkingPriorityField;

  /**
   * The set priority button.
   */ 
  private JButton  pSetPriorityButton;

  /**
   * The checked-in priority field.
   */ 
  private JTextField pCheckedInPriorityField;


  /**
   * The maximum load title label.
   */ 
  private JLabel  pMaxLoadTitle;

  /**
   * The working maximum load field.
   */ 
  private JFloatField pWorkingMaxLoadField;

  /**
   * The set maximum load button.
   */ 
  private JButton  pSetMaxLoadButton;

  /**
   * The checked-in maximum load field.
   */ 
  private JTextField pCheckedInMaxLoadField;


  /**
   * The minimum load title label.
   */ 
  private JLabel  pMinMemoryTitle;

  /**
   * The working minimum load field.
   */ 
  private JByteSizeField pWorkingMinMemoryField;

  /**
   * The set minimum load button.
   */ 
  private JButton  pSetMinMemoryButton;

  /**
   * The checked-in minimum load field.
   */ 
  private JTextField pCheckedInMinMemoryField;


  /**
   * The minimum load title label.
   */ 
  private JLabel  pMinDiskTitle;

  /**
   * The working minimum load field.
   */ 
  private JByteSizeField pWorkingMinDiskField;

  /**
   * The set minimum load button.
   */ 
  private JButton  pSetMinDiskButton;

  /**
   * The checked-in minimum load field.
   */ 
  private JTextField pCheckedInMinDiskField;

  /**
   * The drawer containing the job requirements components.
   */ 
  private JDrawer  pJobReqsDrawer;



  /*----------------------------------------------------------------------------------------*/

  /**
   * The selection keys container.
   */ 
  private Box  pSelectionKeysBox;

  /**
   * The title, working and checked-in selection key components indexed by 
   * selection key name.
   */ 
  private TreeMap<String,Component[]>  pSelectionKeyComponents;
  
  /**
   * The drawer containing the selection key components.
   */ 
  private JDrawer  pSelectionDrawer;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The license keys container.
   */ 
  private Box  pLicenseKeysBox;

  /**
   * The title, working and checked-in license key components indexed by 
   * license key name.
   */ 
  private TreeMap<String,Component[]>  pLicenseKeyComponents;
  
  /**
   * The drawer containing the licence key components.
   */ 
  private JDrawer  pLicenseDrawer;

}
