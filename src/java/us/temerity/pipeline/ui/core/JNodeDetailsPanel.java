// $Id: JNodeDetailsPanel.java,v 1.9 2005/03/11 06:33:44 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

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

      pEditorPlugins      = PluginMgrClient.getInstance().getEditors();
      pEditorMenuLayout   = new PluginMenuLayout();
      pRefreshEditorMenus = true; 
      
      pActionPlugins = PluginMgrClient.getInstance().getActions();

      pActionParamComponents = new TreeMap<String,Component[]>();
      pActionParamGroupsOpen = new TreeMap<String,Boolean>();

      pLinkActionParamValues    = new ArrayList<String>();
      pLinkActionParamNodeNames = new ArrayList<String>();

      pSelectionKeyComponents = new TreeMap<String,Component[]>();
      pLicenseKeyComponents   = new TreeMap<String,Component[]>();
    }

    /* initialize the popup menus */ 
    {
      JMenuItem item;
      JMenu sub;
      
      pWorkingPopup   = new JPopupMenu();  
      pCheckedInPopup = new JPopupMenu(); 

      {
	item = new JMenuItem("Apply Changes");
	pApplyItem = item;
	item.setActionCommand("apply");
	item.addActionListener(this);
	pWorkingPopup.add(item);

	pWorkingPopup.addSeparator();
      }

      pEditItems     = new JMenuItem[2];
      pEditWithMenus = new JMenu[2];

      JPopupMenu menus[] = { pWorkingPopup, pCheckedInPopup };
      int wk;
      for(wk=0; wk<menus.length; wk++) {
	item = new JMenuItem((wk == 1) ? "View" : "Edit"); 
	pEditItems[wk] = item;
	item.setActionCommand("edit");
	item.addActionListener(this);
	menus[wk].add(item);
	
	pEditWithMenus[wk] = new JMenu((wk == 1) ? "View With" : "Edit With");
	menus[wk].add(pEditWithMenus[wk]);
      }
      
      {
	pWorkingPopup.addSeparator();
	
	item = new JMenuItem("Queue Jobs");
	pQueueJobsItem = item;
	item.setActionCommand("queue-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);

	item = new JMenuItem("Queue Jobs Special...");
	pQueueJobsSpecialItem = item;
	item.setActionCommand("queue-jobs-special");
	item.addActionListener(this);
	pWorkingPopup.add(item);
	
	item = new JMenuItem("Pause Jobs");
	pPauseJobsItem = item;
	item.setActionCommand("pause-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);
      
	item = new JMenuItem("Resume Jobs");
	pResumeJobsItem = item;
	item.setActionCommand("resume-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);
	
	item = new JMenuItem("Kill Jobs");
	pKillJobsItem = item;
	item.setActionCommand("kill-jobs");
	item.addActionListener(this);
	pWorkingPopup.add(item);

	pWorkingPopup.addSeparator();

	item = new JMenuItem("Remove Files");
	pRemoveFilesItem = item;
	item.setActionCommand("remove-files");
	item.addActionListener(this);
	pWorkingPopup.add(item);
      } 

      updateMenuToolTips();
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
	  JLabel label = new JLabel();
	  pHeaderIcon = label;
	  
	  label.addMouseListener(this); 

	  panel.add(label);	  
	}
	
	panel.add(Box.createRigidArea(new Dimension(3, 0)));

	{
	  JLabel label = new JLabel("X");
	  pHeaderLabel = label;
	  
	  label.setName("DialogHeaderLabel");	       

	  panel.add(label);	  
	}

	panel.add(Box.createHorizontalGlue());

	{
	  JLabel label = new JLabel(sFrozenIcon);
	  pFrozenLabel = label;
	  panel.add(label);	  
	}

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
	  
	  btn.setToolTipText(UIFactory.formatToolTip
			     ("Apply the changes to node properties."));

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
	  JTextField field = UIFactory.createTextField(null, 100, JLabel.LEFT);
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
	      pVersionStateField = UIFactory.createTitledTextField
		(tpanel, "Version State:", sTSize, 
		 vpanel, "-", sSSize, 
		 "The relationship between working and checked-in revision numbers.");
	    }

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	    /* revision number */ 
	    { 
	      {
		JLabel label = UIFactory.createFixedLabel
		  ("Revision Number:", sTSize, JLabel.RIGHT, 
		   "The revision number of the checked-in version upon which the working " + 
		   "the working version is based.");
		tpanel.add(label);
	      }

	      {
		Box hbox = new Box(BoxLayout.X_AXIS);

		{
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pBaseVersionField = field;

		  hbox.add(field);
		}

		hbox.add(Box.createRigidArea(new Dimension(8, 0)));
		
		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JCollectionField field = UIFactory.createCollectionField(values, sVSize);
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
	  drawer.setToolTipText(UIFactory.formatToolTip("Node revision information."));
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
	      pPropertyStateField = UIFactory.createTitledTextField
		(tpanel, "Property State:", sTSize, 
		 vpanel, "-", sSSize, 
		 "The relationship between the values of the node properties associated " + 
		 "with the working and checked-in versions of a node."); 
	    }

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	    /* toolset */ 
	    { 
	      {
		JLabel label = UIFactory.createFixedLabel
		  ("Toolset:", sTSize, JLabel.RIGHT, 
		   "The name of the shell environment used to run Editors and Actions " + 
		   "associated with the node.");
		pToolsetTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);

		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JCollectionField field = UIFactory.createCollectionField(values, sVSize);
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
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInToolsetField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	    /* editor */ 
	    { 
	      {
		JLabel label = UIFactory.createFixedLabel
		  ("Editor:", sTSize, JLabel.RIGHT, 
		   "The name of the Editor plugin used to edit/view the files associated " +
		   "with the node.");
		pEditorTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);

		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JCollectionField field = UIFactory.createCollectionField(values, sVSize);
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
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInEditorField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }
	  }
	  
	  JDrawer drawer = new JDrawer("Properties:", (JComponent) comps[2], true);
	  drawer.setToolTipText(UIFactory.formatToolTip
				("Node property related information."));
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
		JLabel label = UIFactory.createFixedLabel
		  ("Action:", sTSize, JLabel.RIGHT, 
		   "The name of the Action plugin used to regenerate the files associated " +
		   "with the node.");
		pActionTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JCollectionField field = UIFactory.createCollectionField(values, sVSize);
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
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInActionField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	    /* action version */ 
	    { 
	      {
		JLabel label = UIFactory.createFixedLabel
		  ("Version:", sTSize, JLabel.RIGHT, 
		   "The revision number of the Action plugin.");
		pActionVersionTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");

		  JCollectionField field = UIFactory.createCollectionField(values, sVSize);
		  pWorkingActionVersionField = field;

		  field.setActionCommand("action-changed");
		  field.addActionListener(this);

		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(20, 0)));

		{
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInActionVersionField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	    /* action enabled */ 
	    { 
	      {
		JLabel label = UIFactory.createFixedLabel
		  ("Enabled:", sTSize, JLabel.RIGHT, 
		   "Whether the Action plugin is currently enabled.");
		pActionEnabledTitle = label;
		tpanel.add(label);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  JBooleanField field = UIFactory.createBooleanField(sVSize);
		  pWorkingActionEnabledField = field;
		  
		  field.setValue(null);

		  field.setActionCommand("action-enabled-changed");
		  field.addActionListener(this);

		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(20, 0)));

		{
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInActionEnabledField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

	    UIFactory.addVerticalGlue(tpanel, vpanel);

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
		      JLabel label = UIFactory.createFixedLabel
			("Overflow Policy:", sTSize-7, JLabel.RIGHT, 
			 "The frame range overflow policy.");
		      pOverflowPolicyTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {	
			ArrayList<String> values = new ArrayList<String>();
			values.add("-");
	
			JCollectionField field = 
			  UIFactory.createCollectionField(values, sVSize);
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
			  UIFactory.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInOverflowPolicyField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }

		  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

		  /* execution method */ 
		  { 
		    {
		      JLabel label = UIFactory.createFixedLabel
			("Execution Method:", sTSize-7, JLabel.RIGHT, 
			 "The methodology for regenerating the files associated with nodes " +
			 "with enabled Action plugins.");
		      pExecutionMethodTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		
			ArrayList<String> values = new ArrayList<String>();
			values.add("-");

			JCollectionField field = 
			  UIFactory.createCollectionField(values, sVSize);
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
			  UIFactory.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInExecutionMethodField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }

		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

		  /* batch size */ 
		  { 
		    {
		      JLabel label = UIFactory.createFixedLabel
			("Batch Size:", sTSize-7, JLabel.RIGHT, 
			 "For parallel jobs, this is the maximum number of frames assigned " +
			 "to each job.  A value of (0) means to assign as many frames as " + 
			 "possible to each job.");
		      pBatchSizeTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		  
			JIntegerField field = 
			  UIFactory.createIntegerField(null, sVSize, JLabel.CENTER);
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
			  UIFactory.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInBatchSizeField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }

		  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

		  /* priority */ 
		  { 
		    {
		      JLabel label = UIFactory.createFixedLabel
			("Priority:", sTSize-7, JLabel.RIGHT, 
			 "The relative priority of jobs submitted for this node.");
		      pPriorityTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		  
			JIntegerField field = 
			  UIFactory.createIntegerField(null, sVSize, JLabel.CENTER);
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
			  UIFactory.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInPriorityField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }

		  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

		  /* ramp-up interval */ 
		  { 
		    {
		      JLabel label = UIFactory.createFixedLabel
			("Ramp Up Interval:", sTSize-7, JLabel.RIGHT, 
			 "The time interval (in seconds) to wait before scheduling " + 
			 "new jobs to the server running the job.");
		      pRampUpTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		  
			JIntegerField field = 
			  UIFactory.createIntegerField(null, sVSize, JLabel.CENTER);
			pWorkingRampUpField = field;

			field.setActionCommand("ramp-up-changed");
			field.addActionListener(this);

			hbox.add(field);
		      }

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JButton btn = new JButton();		 
			pSetRampUpButton = btn;
			btn.setName("SmallLeftArrowButton");

			Dimension size = new Dimension(12, 12);
			btn.setMinimumSize(size);
			btn.setMaximumSize(size);
			btn.setPreferredSize(size);

			btn.setActionCommand("set-ramp-up");
			btn.addActionListener(this);

			hbox.add(btn);
		      } 

		      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		      {
			JTextField field = 
			  UIFactory.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInRampUpField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }

		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

		  /* maximum load */ 
		  { 
		    {
		      JLabel label = UIFactory.createFixedLabel
			("Maximum Load:", sTSize-7, JLabel.RIGHT, 
			 "The maxmimum system load allowed on an eligable host.");
		      pMaxLoadTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		  
			JFloatField field = 
			  UIFactory.createFloatField(null, sVSize, JLabel.CENTER);
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
			  UIFactory.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInMaxLoadField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }

		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

		  /* minimum memory */ 
		  { 
		    {
		      JLabel label = UIFactory.createFixedLabel
			("Minimum Memory:", sTSize-7, JLabel.RIGHT, 
			 "The minimum amount of free memory required on an eligable host.");
		      pMinMemoryTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		  
			JByteSizeField field = 
			  UIFactory.createByteSizeField(null, sVSize, JLabel.CENTER);
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
			  UIFactory.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInMinMemoryField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }
		  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

		  /* minimum disk */ 
		  { 
		    {
		      JLabel label = UIFactory.createFixedLabel
			("Minimum Disk:", sTSize-7, JLabel.RIGHT, 
			 "The minimum amount of free temporary local disk space required " +
			 "on an eligable host.");
		      pMinDiskTitle = label;
		      tpanel.add(label);
		    }

		    {
		      Box hbox = new Box(BoxLayout.X_AXIS);

		      {		  
			JByteSizeField field = 
			  UIFactory.createByteSizeField(null, sVSize, JLabel.CENTER);
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
			  UIFactory.createTextField("-", sVSize, JLabel.CENTER);
			pCheckedInMinDiskField = field;

			hbox.add(field);
		      }

		      vpanel.add(hbox);
		    }
		  }
		}

		JDrawer drawer = 
		  new JDrawer("Job Requirements:", (JComponent) comps[2], true);
		drawer.setToolTipText(UIFactory.formatToolTip
		  ("The requirements that a server must meet in order to be eligable " +
		   "to run jobs associated with this node."));
		pJobReqsDrawer = drawer;
		dbox.add(drawer);
	      }

	      /* selection keys */ 
	      {
		Box box = new Box(BoxLayout.Y_AXIS);
		pSelectionKeysBox = box;

		JDrawer drawer = new JDrawer("Selection Keys:", box, false);
		drawer.setToolTipText(UIFactory.formatToolTip
		  ("The set of selection keys a server must have in order to be eligable " + 
		   "to run jobs associated with this node."));
		pSelectionDrawer = drawer;
		dbox.add(drawer);
	      }

	      /* license keys */ 
	      {
		Box box = new Box(BoxLayout.Y_AXIS);
		pLicenseKeysBox = box;

		JDrawer drawer = new JDrawer("License Keys:", box, false);
		drawer.setToolTipText(UIFactory.formatToolTip
		  ("The set of license keys which are required in order to run jobs " + 
		   "associated with this node."));
		pLicenseDrawer = drawer;
		dbox.add(drawer);
	      }

	      jrbox.add(dbox);
	    }

	    abox.add(jrbox);
	  }
	  
	  JDrawer drawer = new JDrawer("Regeneration Action:", abox, true);
	  drawer.setToolTipText(UIFactory.formatToolTip("Action plugin information."));
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

    updateNodeStatus(null, null, null);
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
    Component comps[] = UIFactory.createTitledPanels();

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
   * Get the title of this type of panel.
   */
  public String 
  getTypeName() 
  {
    return "Node Details";
  }
  

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
   * 
   * @param editorPlugins
   *   The names of versions of the loaded editor plugins.   
   * 
   * @param editorLayout
   *   The menu layout for editor plugins.
   */
  public synchronized void 
  updateNodeStatus
  (
   String author, 
   String view, 
   NodeStatus status, 
   TreeMap<String,TreeSet<VersionID>> editorPlugins, 
   PluginMenuLayout editorLayout 
  ) 
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);

    updateNodeStatus(status, editorPlugins, editorLayout);
  }

  /**
   * Update the UI components to reflect the given node status.
   * 
   * @param status
   *   The current node status.
   * 
   * @param editorPlugins
   *   The names of versions of the loaded editor plugins.   
   * 
   * @param editorLayout
   *   The menu layout for editor plugins.
   */
  public synchronized void 
  updateNodeStatus
  (
   NodeStatus status,
   TreeMap<String,TreeSet<VersionID>> editorPlugins, 
   PluginMenuLayout editorLayout   
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

    {
      PluginMgrClient plg = PluginMgrClient.getInstance();
      pActionPlugins = plg.getActions();

      if(editorPlugins != null) 
	pEditorPlugins = editorPlugins;
      else 
	pEditorPlugins = plg.getEditors();

      if(editorLayout != null) {
	pEditorMenuLayout = editorLayout; 
	pRefreshEditorMenus = true;
      }
      else {
	UIMaster master = UIMaster.getInstance(); 
	try {
	  pEditorMenuLayout = master.getMasterMgrClient().getEditorMenuLayout();
	  pRefreshEditorMenus = true;
	} 
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}      
      }
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
	      name = ("NeedsCheckOutMajor-" + details.getOverallQueueState());
	      break;
	      
	    case Minor:
	      name = ("NeedsCheckOut-" + details.getOverallQueueState());
	      break;
	      
	    case Micro:
	      name = ("NeedsCheckOutMicro-" + details.getOverallQueueState());
	    }
	  }
	  else {
	    name = (details.getOverallNodeState() + "-" + details.getOverallQueueState());
	  }

	  NodeMod mod = details.getWorkingVersion();
	  if((mod != null) && mod.isFrozen()) 
	    name = (name + "-Frozen-Normal");
	  else 
	    name = (name + "-Normal");
	}
		
	pHeaderLabel.setText(pStatus.toString());
	pNodeNameField.setText(pStatus.getName());
      }
      else {
	pHeaderLabel.setText(null);
	pNodeNameField.setText(null);
      }
      
      try {
	pHeaderIcon.setIcon(TextureMgr.getInstance().getIcon(name));
      }
      catch(IOException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Tex, LogMgr.Level.Severe,
	   "Internal Error:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	System.exit(1);
      } 
    } 

    /* frozen node? */
    {
      pIsFrozen = false;
      if((details != null) && (details.getWorkingVersion() != null))
	pIsFrozen = details.getWorkingVersion().isFrozen();

      pFrozenLabel.setVisible(pIsFrozen);
      pApplyButton.setVisible(!pIsFrozen);
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

	  pWorkingToolsetField.setEnabled(!pIsLocked && !pIsFrozen && (work != null));
	}
	pWorkingToolsetField.addActionListener(this);
	
	pSetToolsetButton.setEnabled
	  (!pIsLocked && !pIsFrozen && (work != null) && (latest != null));
	
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
	    editors.addAll(pEditorPlugins.keySet());
	  editors.add("-");
	  pWorkingEditorField.setValues(editors);
	  
	  if((work != null) && 
	     (work.getEditor() != null) && (editors.contains(work.getEditor())))
	    pWorkingEditorField.setSelected(work.getEditor());
	  else 
	    pWorkingEditorField.setSelected("-");
	  
	  pWorkingEditorField.setEnabled(!pIsLocked && !pIsFrozen && (work != null));
	}
	pWorkingEditorField.addActionListener(this);
	
	pSetEditorButton.setEnabled
	  (!pIsLocked && !pIsFrozen && (work != null) && (latest != null));
	
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
	  actions.addAll(pActionPlugins.keySet());
	actions.add("-");
	pWorkingActionField.setValues(actions);
	
	BaseAction waction = initWorkingAction();
	if((waction != null) && (actions.contains(waction.getName())))
	  pWorkingActionField.setSelected(waction.getName());
	else 
	  pWorkingActionField.setSelected("-");
	
	pWorkingActionField.setEnabled(!pIsLocked && !pIsFrozen && (work != null));
	
	updateActionVersionFields();
      }
      pWorkingActionField.addActionListener(this);

      pSetActionButton.setEnabled
	(!pIsLocked && !pIsFrozen && (work != null) && (latest != null));

      {
	BaseAction caction = getCheckedInAction();	
	if(caction != null) 
	  pCheckedInActionField.setText(caction.getName());
	else 
	  pCheckedInActionField.setText("-");
      }

      if((work != null) && (getWorkingAction() != null)) {
	pWorkingActionEnabledField.setValue(work.isActionEnabled()); 
	pWorkingActionEnabledField.setEnabled(!pIsLocked && !pIsFrozen);
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
    pApplyItem.setEnabled(false);
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

      if(action != null) 
	pCheckedInActionEnabledField.setText(vsn.isActionEnabled() ? "YES" : "no");
      else 
	pCheckedInActionEnabledField.setText("-");

      doActionChanged();    
    }

    /* job requirements panel */ 
    updateJobRequirements(false); 

    /* restore the enabled state of the apply button */ 
    pApplyButton.setEnabled(isEnabled);
    pApplyItem.setEnabled(isEnabled);
  }

  /**
   * Update the action versions fields.
   */ 
  private void 
  updateActionVersionFields()
  {
    pWorkingActionVersionField.removeActionListener(this);
    {
      BaseAction waction = getWorkingAction();
      if(waction != null) {
	TreeSet<String> vstr = new TreeSet<String>();
	TreeSet<VersionID> vids = pActionPlugins.get(waction.getName());
	for(VersionID vid : vids)
	  vstr.add("v" + vid.toString());
	pWorkingActionVersionField.setValues(vstr);
	
	pWorkingActionVersionField.setSelected("v" + waction.getVersionID().toString());
	pWorkingActionVersionField.setEnabled(!pIsLocked && !pIsFrozen);
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

    /* per-source params */ 
    if((action != null) && action.supportsSourceParams()) {
      UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

      pEditSourceParamsDialog = null;
      pViewSourceParamsDialog = null;
      
      pSourceParamComponents = new Component[4];
      
      {
	JLabel label = UIFactory.createFixedLabel
	  ("Source Parameters:", sTSize, JLabel.RIGHT, 
	   "The Action plugin parameters associated with each source node.");
	pSourceParamComponents[0] = label;
	
	tpanel.add(label);
      }
      
      { 
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	if((waction != null) && waction.supportsSourceParams()) {
	  JButton btn = new JButton((pIsLocked || pIsFrozen) ? "View..." : "Edit...");
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
	      new JSourceParamsDialog(!pIsLocked && !pIsFrozen, title, stitles, 
				      snames, waction);
	  }
	}
	else {
	  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
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
	  
	  btn.setEnabled(!pIsLocked && !pIsFrozen && 
			 (waction != null) && (caction != null) && 
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
	  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
	  pSourceParamComponents[3] = field;
	  
	  hbox.add(field);
	}
	
	vpanel.add(hbox);
	
	doSourceParamsChanged();
      }	
    }
    else {
      tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
      vpanel.add(Box.createHorizontalGlue());
    }
    pActionParamsBox.add(comps[2]);

    /* single valued parameters */ 
    if((action != null) && action.hasSingleParams()) {
      pLinkActionParamValues.clear();
      for(String sname : pStatus.getSourceNames()) 
	pLinkActionParamValues.add(pStatus.getSource(sname).toString());
      pLinkActionParamValues.add("-");
      
      pLinkActionParamNodeNames.clear();
      pLinkActionParamNodeNames.addAll(pStatus.getSourceNames());
      pLinkActionParamNodeNames.add(null);

      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	hbox.addComponentListener(this);

	{
	  JPanel spanel = new JPanel();
	  spanel.setName("Spacer");
	  
	  spanel.setMinimumSize(new Dimension(7, 0));
	  spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	  spanel.setPreferredSize(new Dimension(7, 0));
	  
	  hbox.add(spanel);
	}

	updateSingleActionParams(action, waction, caction, action.getSingleLayout(), hbox, 1);
	
	pActionParamsBox.add(hbox);
      }
    }

    pActionBox.revalidate();
    pActionBox.repaint();
  }

  /**
   * Recursively create drawers containing the working and checked-in single valued 
   * action parameters.
   */ 
  private void 
  updateSingleActionParams
  (
   BaseAction action, 
   BaseAction waction, 
   BaseAction caction, 
   LayoutGroup group, 
   Box sbox, 
   int level
  ) 
  {
    Box dbox = new Box(BoxLayout.Y_AXIS);    
    {
      Component comps[] = createCommonPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];

      for(String pname : group.getEntries()) {
	if(pname == null) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	}
	else {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  /* single valued parameter */ 
	  ActionParam param = action.getSingleParam(pname);
	  if(param != null) {
	    Component pcomps[] = new Component[4];

	    {
	      JLabel label = UIFactory.createFixedLabel
		(param.getNameUI() + ":", sTSize-7*level, JLabel.RIGHT, 
		 param.getDescription());

	      pcomps[0] = label;
	      
	      tpanel.add(label);
	    }

	    { 
	      Box hbox = new Box(BoxLayout.X_AXIS);

	      {
		ActionParam aparam = null;
		if(waction != null) 
		  aparam = waction.getSingleParam(param.getName());

		if(aparam != null) {
		  if(aparam instanceof BooleanActionParam) {
		    Boolean value = (Boolean) aparam.getValue();
		    JBooleanField field = 
		      UIFactory.createBooleanField(value, sVSize);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!pIsLocked && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof IntegerActionParam) {
		    Integer value = (Integer) aparam.getValue();
		    JIntegerField field = 
		      UIFactory.createIntegerField(value, sVSize, JLabel.CENTER);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!pIsLocked && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof DoubleActionParam) {
		    Double value = (Double) aparam.getValue();
		    JDoubleField field = 
		      UIFactory.createDoubleField(value, sVSize, JLabel.CENTER);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!pIsLocked && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof StringActionParam) {
		    String value = (String) aparam.getValue();
		    JTextField field = 
		      UIFactory.createEditableTextField(value, sVSize, JLabel.CENTER);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!pIsLocked && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof EnumActionParam) {
		    EnumActionParam eparam = (EnumActionParam) aparam;
		    JCollectionField field = 
		      UIFactory.createCollectionField(eparam.getValues(), sVSize);
		    pcomps[1] = field;

		    field.setSelected((String) eparam.getValue());

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!pIsLocked && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof LinkActionParam) {
		    JCollectionField field = 
		      UIFactory.createCollectionField(pLinkActionParamValues, sVSize);
		    pcomps[1] = field;

		    String source = (String) aparam.getValue();
		    int idx = pLinkActionParamNodeNames.indexOf(source);
		    if(idx != -1) 
		      field.setSelectedIndex(idx);
		    else 
		      field.setSelected("-");

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!pIsLocked && !pIsFrozen);

		    hbox.add(field);
		  }
		}
		else {
		  JLabel label = UIFactory.createLabel("-", sVSize, JLabel.CENTER);
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

		btn.setEnabled(!pIsLocked && !pIsFrozen && 
			       (waction != null) && (caction != null) && 
			       caction.getName().equals(waction.getName()));

		hbox.add(btn);
	      } 

	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	      {
		ActionParam aparam = null;
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
		    else if(aparam instanceof BooleanActionParam) {
		      Boolean value = (Boolean) aparam.getValue();
		      if(value != null) 
			text = (value ? "YES" : "no");
		      else 
			text = "-";
		    }
		    else {
		      Comparable value = aparam.getValue();
		      if(value != null)
			text = value.toString();
		    }
		  }

		  JTextField field = UIFactory.createTextField(text, sVSize, JLabel.CENTER);
		  pcomps[3] = field;

		  hbox.add(field);
		}
		else {
		  JLabel label = UIFactory.createLabel("-", sVSize, JLabel.CENTER);
		  label.setName("TextFieldLabel");

		  pcomps[3] = label;

		  hbox.add(label);
		}
	      }

	      vpanel.add(hbox);
	    }

	    pActionParamComponents.put(param.getName(), pcomps);
	  }
	  
	  /* parameter preset */ 
	  else if(action.getPresetChoices(pname) != null) {

	    java.util.List<String> choices = null;
	    if(waction != null) {
	      choices = new ArrayList<String>();
	      choices.add("-");
	      choices.addAll(waction.getPresetChoices(pname));
	    }

	    String tname = presetNameUI(pname);
	    if(choices != null) {
	      JCollectionField field = UIFactory.createTitledCollectionField
		(tpanel, tname + ":", sTSize-7*level,
		 vpanel, choices, sSSize, 
		 "Action plugin parameter presets.");
	      field.addActionListener(new PresetChoice(pname, field));
	    }
	    else {
	      UIFactory.createTitledTextField
		(tpanel, tname + ":", sTSize-7*level,
		 vpanel, "-", sSSize, 
		 "Action plugin parameter presets.");		 
	    }
	  }
	}
      }

      dbox.add(comps[2]);
    }
    
    if(!group.getSubGroups().isEmpty())  {
      Box hbox = new Box(BoxLayout.X_AXIS);
      hbox.addComponentListener(this);

      {
	JPanel spanel = new JPanel();
	spanel.setName("Spacer");
	
	spanel.setMinimumSize(new Dimension(7, 0));
	spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
	spanel.setPreferredSize(new Dimension(7, 0));
	
	hbox.add(spanel);
      }

      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	for(LayoutGroup sgroup : group.getSubGroups()) 
	  updateSingleActionParams(action, waction, caction, sgroup, vbox, level+1);

	hbox.add(vbox);
      }

      dbox.add(hbox);
    }

    {
      JDrawer drawer = new JDrawer(group.getNameUI() + ":", dbox, true);
      drawer.addActionListener(new UpdateParamGroupsOpen(group.getName(), drawer));
      drawer.setToolTipText(UIFactory.formatToolTip(group.getDescription()));
      sbox.add(drawer);
      
      Boolean isOpen = pActionParamGroupsOpen.get(group.getName());
      if(isOpen == null) {
	isOpen = group.isOpen();
	pActionParamGroupsOpen.put(group.getName(), isOpen);
      }
      drawer.setIsOpen(isOpen);
    }
  }
      
  /**
   * Converts compact preset names into a more human friendly form.
   */ 
  private String
  presetNameUI
  (
   String name
  )
  {
    StringBuffer buf = new StringBuffer();
    char c[] = name.toCharArray();
    int wk;
    buf.append(c[0]);
    for(wk=1; wk<(c.length-1); wk++) {
      if(Character.isUpperCase(c[wk]) && 
	 (Character.isLowerCase(c[wk-1]) ||
	  Character.isLowerCase(c[wk+1])))
	  buf.append(" ");

      buf.append(c[wk]);
    }
    buf.append(c[wk]);

    return (buf.toString());
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
	  
	  pWorkingOverflowPolicyField.setEnabled
	    (!pIsLocked && !pIsFrozen && (waction != null));
	}
	
	pSetOverflowPolicyButton.setEnabled
	  (!pIsLocked && !pIsFrozen && (waction != null) && (caction != null));
	
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
	  
	  pWorkingExecutionMethodField.setEnabled
	    (!pIsLocked && !pIsFrozen && (waction != null));
	}
	
	pSetExecutionMethodButton.setEnabled
	  (!pIsLocked && !pIsFrozen && (waction != null) && (caction != null));
	
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
	  
	  pWorkingPriorityField.setEnabled(!pIsLocked && !pIsFrozen && (wjreq != null));
	}

	pSetPriorityButton.setEnabled
	  (!pIsLocked && !pIsFrozen && (wjreq != null) && (cjreq != null));
	
	if(cjreq != null)
	  pCheckedInPriorityField.setText(String.valueOf(cjreq.getPriority()));
	else 
	  pCheckedInPriorityField.setText("-");

	doPriorityChanged();
      }

      /* ramp-up interval */ 
      { 
	if(refresh) {
	  pWorkingRampUpField.removeActionListener(this);
	  {
	    if(wjreq != null) 
	      pWorkingRampUpField.setValue(wjreq.getRampUp());
	    else 
	      pWorkingRampUpField.setValue(null);	
	  }
	  pWorkingRampUpField.addActionListener(this);
	  
	  pWorkingRampUpField.setEnabled(!pIsLocked && !pIsFrozen && (wjreq != null));
	}

	pSetRampUpButton.setEnabled
	  (!pIsLocked && !pIsFrozen && (wjreq != null) && (cjreq != null));
	
	if(cjreq != null)
	  pCheckedInRampUpField.setText(String.valueOf(cjreq.getRampUp()));
	else 
	  pCheckedInRampUpField.setText("-");

	doRampUpChanged();
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
	  
	  pWorkingMaxLoadField.setEnabled(!pIsLocked && !pIsFrozen && (wjreq != null));
	}

	pSetMaxLoadButton.setEnabled
	  (!pIsLocked && !pIsFrozen && (wjreq != null) && (cjreq != null));
	
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
	  
	  pWorkingMinMemoryField.setEnabled(!pIsLocked && !pIsFrozen && (wjreq != null));
	}

	pSetMinMemoryButton.setEnabled
	  (!pIsLocked && !pIsFrozen && (wjreq != null) && (cjreq != null));
	
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
	  
	  pWorkingMinDiskField.setEnabled(!pIsLocked && !pIsFrozen && (wjreq != null));
	}

	pSetMinDiskButton.setEnabled
	  (!pIsLocked && !pIsFrozen && (wjreq != null) && (cjreq != null));
	
	if(cjreq != null)
	  pCheckedInMinDiskField.setText
	    (JByteSizeField.longToString(cjreq.getMinDisk()));
	else 
	  pCheckedInMinDiskField.setText("-");

	doMinDiskChanged();
      }

      /* selection keys */ 
      {
	TreeMap<String,String> keys = new TreeMap<String,String>();
	UIMaster master = UIMaster.getInstance();
	try {
	  for(SelectionKey key : master.getQueueMgrClient().getSelectionKeys())
	    keys.put(key.getName(), key.getDescription());
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}

	pSelectionKeysBox.removeAll();
	pSelectionKeyComponents.clear();

	Component comps[] = createCommonPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
    
	if(keys.isEmpty()) {
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
	else {
	  boolean first = true; 
	  for(String kname : keys.keySet()) {
	    boolean hasWorkingKey = 
	      (wjreq != null) && wjreq.getSelectionKeys().contains(kname);
	    boolean hasCheckedInKey = 
	      (cjreq != null) && cjreq.getSelectionKeys().contains(kname);

	    if(!first) 
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    first = false;

	    Component pcomps[] = new Component[4];

	    {
	      JLabel label = UIFactory.createFixedLabel
		(kname + ":", sTSize-7, JLabel.RIGHT, keys.get(kname));
	      pcomps[0] = label;

	      tpanel.add(label);
	    }

	    { 
	      Box hbox = new Box(BoxLayout.X_AXIS);

	      {
		JBooleanField field = UIFactory.createBooleanField(sVSize);
		pcomps[1] = field;

		if(wjreq != null)
		  field.setValue(hasWorkingKey);
		else 
		  field.setValue(null);

		field.setActionCommand("selection-key-changed:" + kname);
		field.addActionListener(this);

		field.setEnabled(!pIsLocked && !pIsFrozen && (wjreq != null));

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

		btn.setEnabled(!pIsLocked && !pIsFrozen && 
			       (wjreq != null) && (cjreq != null));

		hbox.add(btn);
	      } 

	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	      {
		JTextField field = 
		  UIFactory.createTextField("-", sVSize, JLabel.CENTER);
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
	TreeMap<String,String> keys = new TreeMap<String,String>();
	UIMaster master = UIMaster.getInstance();
	try {
	  for(LicenseKey key : master.getQueueMgrClient().getLicenseKeys())
	    keys.put(key.getName(), key.getDescription());
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	}

	pLicenseKeysBox.removeAll();
	pLicenseKeyComponents.clear();

	Component comps[] = createCommonPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];

	if(keys.isEmpty()) {
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize-7, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
	else {
	  boolean first = true; 
	  for(String kname : keys.keySet()) {
	    boolean hasWorkingKey = 
	      (wjreq != null) && wjreq.getLicenseKeys().contains(kname);
	    boolean hasCheckedInKey = 
	      (cjreq != null) && cjreq.getLicenseKeys().contains(kname);
	    
	    if(!first) 
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    first = false;
	    
	    Component pcomps[] = new Component[4];
	    
	    {
	      JLabel label = UIFactory.createFixedLabel
		(kname + ":", sTSize-7, JLabel.RIGHT, keys.get(kname));
	      pcomps[0] = label;
	      
	      tpanel.add(label);
	    }
	    
	    { 
	      Box hbox = new Box(BoxLayout.X_AXIS);
	      
	      {
		JBooleanField field = UIFactory.createBooleanField(sVSize);
		pcomps[1] = field;
		
		if(wjreq != null)
		  field.setValue(hasWorkingKey);
		else 
		  field.setValue(null);
		
		field.setActionCommand("license-key-changed:" + kname);
		field.addActionListener(this);
		
		field.setEnabled(!pIsLocked && !pIsFrozen && (wjreq != null));
		
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
		
		btn.setEnabled(!pIsLocked && !pIsFrozen && 
			       (wjreq != null) && (cjreq != null));
		
		hbox.add(btn);
	      } 
	      
	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	      
	      {
		JTextField field = 
		  UIFactory.createTextField("-", sVSize, JLabel.CENTER);
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
      for(ActionParam param : action.getSingleParams()) 
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
	ActionParam aparam = null;
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
	ActionParam aparam = null;
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

  /**
   * Update the editor plugin menus.
   */ 
  private synchronized void 
  updateEditorMenus()
  {
    if(pRefreshEditorMenus) {
      int wk;
      for(wk=0; wk<pEditWithMenus.length; wk++) {
	pEditWithMenus[wk].removeAll();
	for(PluginMenuLayout pml : pEditorMenuLayout) 
	  pEditWithMenus[wk].add(buildPluginMenu(pml, "edit-with", pEditorPlugins));
      }
      
      pRefreshEditorMenus = false;
    }
  }
  
  /**
   * Recursively update a plugin menu.
   */ 
  private JMenuItem
  buildPluginMenu
  (
   PluginMenuLayout layout, 
   String prefix, 
   TreeMap<String,TreeSet<VersionID>> plugins
  ) 
  {
    JMenuItem item = null;
    if(layout.isMenuItem()) {
      item = new JMenuItem(layout.getTitle());
      item.setActionCommand(prefix + ":" + layout.getName() + ":" + layout.getVersionID());
      item.addActionListener(this);
   
      TreeSet<VersionID> vids = plugins.get(layout.getName());
      item.setEnabled((vids != null) && vids.contains(layout.getVersionID()));
    }
    else {
      JMenu sub = new JMenu(layout.getTitle()); 
      for(PluginMenuLayout pml : layout) 
	sub.add(buildPluginMenu(pml, prefix, plugins));
      item = sub;
    }

    return item;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the panel to reflect new user preferences.
   */ 
  public void 
  updateUserPrefs() 
  {
    updateMenuToolTips();
  }

  /**
   * Update the menu item tool tips.
   */ 
  private void 
  updateMenuToolTips() 
  {
    UserPrefs prefs = UserPrefs.getInstance();
       
    updateMenuToolTip
      (pApplyItem, prefs.getApplyChanges(),
       "Apply the changes to the working version.");

    int wk;
    for(wk=0; wk<pEditItems.length; wk++) {
      updateMenuToolTip
	(pEditItems[wk], prefs.getEdit(), 
	 "Edit primary file sequences of the current primary selection.");
    }

    updateMenuToolTip
      (pQueueJobsItem, prefs.getQueueJobs(), 
       "Submit jobs to the queue for the current primary selection.");
    updateMenuToolTip
      (pQueueJobsSpecialItem, prefs.getQueueJobsSpecial(), 
       "Submit jobs to the queue for the current primary selection with special job " + 
       "requirements.");
    updateMenuToolTip
      (pPauseJobsItem, prefs.getPauseJobs(), 
       "Pause all jobs associated with the selected nodes.");
    updateMenuToolTip
      (pResumeJobsItem, prefs.getResumeJobs(), 
       "Resume execution of all jobs associated with the selected nodes.");
    updateMenuToolTip
      (pKillJobsItem, prefs.getKillJobs(), 
       "Kill all jobs associated with the selected nodes.");

    updateMenuToolTip
      (pRemoveFilesItem, prefs.getRemoveFiles(), 
       "Remove all the primary/secondary files associated with the selected nodes.");
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
  mousePressed
  (
   MouseEvent e
  )
  {
    /* manager panel popups */ 
    if(pManagerPanel.handleManagerMouseEvent(e)) 
      return;

    /* local mouse events */ 
    if(e.getSource() == pHeaderIcon) {
      if(pStatus == null) 
	return; 

      NodeDetails details = pStatus.getDetails();
      if(details == null) 
	return;

      NodeMod work = details.getWorkingVersion();
      NodeVersion latest = details.getLatestVersion();
      if((work != null) && !work.isFrozen()) {
	updateEditorMenus();
	pWorkingPopup.show(e.getComponent(), e.getX(), e.getY());
      }
      else if(latest != null) {
	updateEditorMenus();
	pCheckedInPopup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }
  
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
    /* manager panel hotkeys */ 
    if(pManagerPanel.handleManagerKeyEvent(e)) 
      return;

    /* local hotkeys */ 
    UserPrefs prefs = UserPrefs.getInstance();
    if((prefs.getApplyChanges() != null) &&
       prefs.getApplyChanges().wasPressed(e) && 
       pApplyButton.isEnabled())
      doApply();

    else if((prefs.getEdit() != null) &&
	    prefs.getEdit().wasPressed(e))
      doEdit();
    
    else if((prefs.getQueueJobs() != null) &&
	    prefs.getQueueJobs().wasPressed(e))
      doQueueJobs();
    else if((prefs.getQueueJobsSpecial() != null) &&
	    prefs.getQueueJobsSpecial().wasPressed(e))
      doQueueJobsSpecial();
    else if((prefs.getPauseJobs() != null) &&
	    prefs.getPauseJobs().wasPressed(e))
	doPauseJobs();
    else if((prefs.getResumeJobs() != null) &&
	    prefs.getResumeJobs().wasPressed(e))
      doResumeJobs();
    else if((prefs.getKillJobs() != null) &&
	      prefs.getKillJobs().wasPressed(e))
      doKillJobs();
    
    else if((prefs.getRemoveFiles() != null) &&
	    prefs.getRemoveFiles().wasPressed(e))
      doRemoveFiles();

    else {
      switch(e.getKeyCode()) {
      case KeyEvent.VK_SHIFT:
      case KeyEvent.VK_ALT:
      case KeyEvent.VK_CONTROL:
	break;
      
      default:
	Toolkit.getDefaultToolkit().beep();
      }
    }
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
    else if(cmd.equals("set-ramp-up")) 
      doSetRampUp();
    else if(cmd.equals("ramp-up-changed")) 
      doRampUpChanged();
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

    else if(cmd.equals("edit"))
      doEdit();
    else if(cmd.startsWith("edit-with:"))
      doEditWith(cmd.substring(10)); 

    else if(cmd.equals("queue-jobs"))
      doQueueJobs();
    else if(cmd.equals("queue-jobs-special"))
      doQueueJobsSpecial();
    else if(cmd.equals("pause-jobs"))
      doPauseJobs();
    else if(cmd.equals("resume-jobs"))
      doResumeJobs();
    else if(cmd.equals("kill-jobs"))
      doKillJobs();

    else if(cmd.equals("remove-files"))
      doRemoveFiles();        
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
    if(pIsLocked || pIsFrozen) 
      return;

    if((pStatus != null) && (pStatus.getDetails() != null)) {
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
	      for(ActionParam aparam : waction.getSingleParams()) {
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
	    
	    /* ramp-up interval */ 
	    {
	      Integer interval = pWorkingRampUpField.getValue();
	      if(interval == null) 
		pWorkingRampUpField.setValue(jreq.getRampUp());
	      else 
		jreq.setRampUp(interval);
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
	  pApplyItem.setEnabled(false);
	  
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
    pApplyItem.setEnabled(true);

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
    pApplyItem.setEnabled(true);

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
    pApplyItem.setEnabled(true);

    BaseAction oaction = getWorkingAction();
    {
      String aname = pWorkingActionField.getSelected();
      if(aname.equals("-")) {
	setWorkingAction(null);

	pWorkingActionEnabledField.setValue(null);
	pWorkingActionEnabledField.setEnabled(false);

	pActionParamComponents.clear();
	pActionParamGroupsOpen.clear();
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
	    setWorkingAction(PluginMgrClient.getInstance().newAction(aname, vid));
	    
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
	  pActionParamGroupsOpen.clear();
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
    pApplyItem.setEnabled(true);

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

    ActionParam wparam = null;
    if(waction != null) 
      wparam = waction.getSingleParam(pname);
      
    ActionParam cparam = null;
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
    pApplyItem.setEnabled(true);
  
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
	  new JSourceParamsDialog(!pIsLocked && !pIsFrozen, title, stitles, snames, waction);
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
    pApplyItem.setEnabled(true);

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
    pApplyItem.setEnabled(true);
    
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
    pApplyItem.setEnabled(true);

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
      pWorkingBatchSizeField.setEnabled(!pIsLocked && !pIsFrozen);
      pSetBatchSizeButton.setEnabled
	(!pIsLocked && !pIsFrozen && (cmethod != null) && (cmethod.equals("Parallel")));
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
    pApplyItem.setEnabled(true);
    
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
    pApplyItem.setEnabled(true);
    
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
   * Set the working ramp-up interval field from the value of the checked-in field.
   */ 
  private void 
  doSetRampUp()
  { 
    pWorkingRampUpField.removeActionListener(this);
      pWorkingRampUpField.setText(pCheckedInRampUpField.getText());
    pWorkingRampUpField.addActionListener(this);

    doRampUpChanged();
  }

  /**
   * Update the appearance of the ramp-up interval field after a change of value.
   */ 
  private void 
  doRampUpChanged()
  {
    pApplyButton.setEnabled(true);
    pApplyItem.setEnabled(true);
    
    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String winterval = pWorkingRampUpField.getText();
      String cinterval = pCheckedInRampUpField.getText();      
      if(!cinterval.equals(winterval)) {
	color = Color.cyan;
      }
    }

    pRampUpTitle.setForeground(color);
    pWorkingRampUpField.setWarningColor(color);
    pCheckedInRampUpField.setForeground(color);
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
    pApplyItem.setEnabled(true);
    
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
    pApplyItem.setEnabled(true);
    
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
    pApplyItem.setEnabled(true);
    
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
      pApplyItem.setEnabled(true);
    
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
      pApplyItem.setEnabled(true);
    
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
   * Edit/View the current node with the editor specified by the node version.
   */ 
  private void 
  doEdit() 
  {
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();

	if(com != null) {
	  EditTask task = new EditTask(com);
	  task.start();
	}
      }
    }
  }

  /**
   * Edit/View the current node with the given editor.
   */ 
  private void 
  doEditWith
  (
   String editor
  ) 
  {
    String ename = null;
    VersionID evid = null;
    String parts[] = editor.split(":");
    switch(parts.length) {
    case 1:
      ename = editor;
      break;

    case 2:
      ename = parts[0];
      evid = new VersionID(parts[1]);
      break;

    default:
      assert(false);
    }

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	NodeCommon com = details.getWorkingVersion();
	if(com == null) 
	  com = details.getLatestVersion();

	if(com != null) {
	  EditTask task = new EditTask(com, ename, evid);
	  task.start();
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Queue jobs to the queue for the primary current node and all nodes upstream of it.
   */ 
  private void 
  doQueueJobs() 
  {
    if(pIsFrozen) 
      return;

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {	  
	QueueJobsTask task = new QueueJobsTask(pStatus.getName());
	task.start();
      }
    }
  }

  /**
   * Queue jobs to the queue for the primary current node and all nodes upstream of it
   * with special job requirements.
   */ 
  private void 
  doQueueJobsSpecial() 
  {
    if(pIsFrozen) 
      return;

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	JQueueJobsDialog diag = UIMaster.getInstance().showQueueJobsDialog();
	if(diag.wasConfirmed()) {
	  Integer batchSize = null;
	  if(diag.overrideBatchSize()) 
	    batchSize = diag.getBatchSize();
	  
	  Integer priority = null;
	  if(diag.overridePriority()) 
	    priority = diag.getPriority();
	  
	  Integer interval = null;
	  if(diag.overrideRampUp()) 
	    interval = diag.getRampUp();
	  
	  TreeSet<String> keys = null;
	  if(diag.overrideSelectionKeys()) 
	    keys = diag.getSelectionKeys();
	  
	  QueueJobsTask task = 
	    new QueueJobsTask(pStatus.getName(), batchSize, priority, interval, keys);
	  task.start();
	}
      }
    }
  }

  /**
   * Pause all waiting jobs associated with the current node.
   */ 
  private void 
  doPauseJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<Long> paused = new TreeSet<Long>();
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	Long[] jobIDs   = details.getJobIDs();
	QueueState[] qs = details.getQueueState();
	assert(jobIDs.length == qs.length);

	int wk;
	for(wk=0; wk<jobIDs.length; wk++) {
	  switch(qs[wk]) {
	  case Queued:
	    assert(jobIDs[wk] != null);
	    paused.add(jobIDs[wk]);
	  }
	}
      }
    }

    if(!paused.isEmpty()) {
      PauseJobsTask task = new PauseJobsTask(paused);
      task.start();
    }
  }

  /**
   * Resume execution of all paused jobs associated with the current node.
   */ 
  private void 
  doResumeJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<Long> resumed = new TreeSet<Long>();
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	Long[] jobIDs   = details.getJobIDs();
	QueueState[] qs = details.getQueueState();
	assert(jobIDs.length == qs.length);

	int wk;
	for(wk=0; wk<jobIDs.length; wk++) {
	  switch(qs[wk]) {
	  case Paused:
	    assert(jobIDs[wk] != null);
	    resumed.add(jobIDs[wk]);
	  }
	}
      }
    }

    if(!resumed.isEmpty()) {
      ResumeJobsTask task = new ResumeJobsTask(resumed);
      task.start();
    }
  }

  /**
   * Kill all jobs associated with the current node.
   */ 
  private void 
  doKillJobs() 
  {
    if(pIsFrozen) 
      return;

    TreeSet<Long> dead = new TreeSet<Long>();
    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	Long[] jobIDs   = details.getJobIDs();
	QueueState[] qs = details.getQueueState();
	assert(jobIDs.length == qs.length);

	int wk;
	for(wk=0; wk<jobIDs.length; wk++) {
	  switch(qs[wk]) {
	  case Queued:
	  case Paused:
	  case Running:
	    assert(jobIDs[wk] != null);
	    dead.add(jobIDs[wk]);
	  }
	}
      }
    }

    if(!dead.isEmpty()) {
      KillJobsTask task = new KillJobsTask(dead);
      task.start();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove all primary/secondary files associated with the current node.
   */ 
  private void 
  doRemoveFiles() 
  {
    if(pIsFrozen) 
      return;

    if(pStatus != null) {
      NodeDetails details = pStatus.getDetails();
      if(details != null) {
	RemoveFilesTask task = new RemoveFilesTask(pStatus.getName());
	task.start();
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Update the table of whether each parameter group is open when the drawer is opened
   * or closed.
   */ 
  private
  class UpdateParamGroupsOpen
    implements ActionListener
  {
    public 
    UpdateParamGroupsOpen
    (
     String name, 
     JDrawer drawer
    ) 
    {
      pName   = name;
      pDrawer = drawer;
    }

    /** 
     * Invoked when an action occurs. 
     */ 
    public void 
    actionPerformed
    (
     ActionEvent e
    ) 
    {
      pActionParamGroupsOpen.put(pName, pDrawer.isOpen());
    }
    
    private String   pName;
    private JDrawer  pDrawer;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Updates single valued parameter field when a preset choice is made.
   */ 
  private
  class PresetChoice
    implements ActionListener
  {
    public
    PresetChoice
    (
     String name, 
     JCollectionField field
    ) 
    {
      pName  = name;
      pField = field;
    }
    
    /** 
     * Invoked when an action occurs. 
     */ 
    public void 
    actionPerformed
    (
     ActionEvent e
    ) 
    {
      String choice = pField.getSelected();
      if(choice == null)
	return;

      if(pWorkingAction != null) {
	SortedMap<String,Comparable> values = pWorkingAction.getPresetValues(pName, choice);
	if(values != null) {
	  for(String pname : values.keySet()) {
	    Component comps[] = pActionParamComponents.get(pname);
	    ActionParam aparam = pWorkingAction.getSingleParam(pname);
	    if((aparam != null) && (comps != null)) {
	      Comparable value = values.get(pname);
	      
	      if(aparam instanceof BooleanActionParam) {
		JBooleanField field = (JBooleanField) comps[1];
		field.setValue((Boolean) value);
		doActionParamChanged(pname);
	      }
	      else if(aparam instanceof IntegerActionParam) {
		JIntegerField field = (JIntegerField) comps[1];
		field.setValue((Integer) value);
		doActionParamChanged(pname);
	      }
	      else if(aparam instanceof DoubleActionParam) {
		JDoubleField field = (JDoubleField) comps[1];
		field.setValue((Double) value);
		doActionParamChanged(pname);
	      }
	      else if(aparam instanceof StringActionParam) {
		JTextField field = (JTextField) comps[1];
		field.setText((String) value);
		doActionParamChanged(pname);
	      }
	      else if(aparam instanceof EnumActionParam) {
		JCollectionField field = (JCollectionField) comps[1];
		field.setSelected((String) value);
		doActionParamChanged(pname);
	      }
	    }
	  }
	}
      }
    }
    
    private String            pName;
    private JCollectionField  pField;
  }


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

  /** 
   * Edit/View the primary file sequence of the given node version.
   */ 
  private
  class EditTask
    extends UIMaster.EditTask
  {
    public 
    EditTask
    (
     NodeCommon com
    ) 
    {
      UIMaster.getInstance().super(com, pAuthor, pView);
      setName("JNodeDetailsPanel:EditTask");
    }

    public 
    EditTask
    (
     NodeCommon com, 
     String ename, 
     VersionID evid
    ) 
    {
      UIMaster.getInstance().super(com, ename, evid, pAuthor, pView);
      setName("JNodeDetailsPanel:EditTask");
    }
  }


  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Queue jobs to the queue for the given node.
   */ 
  private
  class QueueJobsTask
    extends UIMaster.QueueJobsTask
  {
    public 
    QueueJobsTask
    (
     String name
    ) 
    {
      this(name, null, null, null, null);
    }

    public 
    QueueJobsTask
    (
     String name, 
     Integer batchSize, 
     Integer priority, 
     Integer rampUp, 
     TreeSet<String> selectionKeys
    ) 
    {
      UIMaster.getInstance().super(name, pAuthor, pView, 
				   batchSize, priority, rampUp, selectionKeys);
      setName("JNodeDetailsPanel:QueueJobsTask");
    }

    protected void
    postOp() 
    {
      if(pGroupID > 0) {
	PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
	JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	if(viewer != null) 
	  viewer.updateRoots();
      } 
    }
  }

  /** 
   * Pause the given jobs.
   */ 
  private
  class PauseJobsTask
    extends UIMaster.PauseJobsTask
  {
    public 
    PauseJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super(jobIDs, pAuthor, pView);
      setName("JNodeDetailsPanel:PauseJobsTask");

      pJobIDs = jobIDs; 
    }

    protected void
    postOp() 
    {
      if(pGroupID > 0) {
	PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
	JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	if(viewer != null) 
	  viewer.updateRoots();
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  /** 
   * Resume execution of the the given paused jobs.
   */ 
  private
  class ResumeJobsTask
    extends UIMaster.ResumeJobsTask
  {
    public 
    ResumeJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super(jobIDs, pAuthor, pView);
      setName("JNodeDetailsPanel:ResumeJobsTask");

      pJobIDs = jobIDs; 
    }

    protected void
    postOp() 
    {
      if(pGroupID > 0) {
	PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
	JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	if(viewer != null) 
	  viewer.updateRoots();
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  /** 
   * Kill the given jobs.
   */ 
  private
  class KillJobsTask
    extends UIMaster.KillJobsTask
  {
    public 
    KillJobsTask
    (
     TreeSet<Long> jobIDs
    ) 
    {
      UIMaster.getInstance().super(jobIDs, pAuthor, pView);
      setName("JNodeDetailsPanel:KillJobsTask");

      pJobIDs = jobIDs; 
    }

    protected void
    postOp() 
    {
      if(pGroupID > 0) {
	PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
	JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	if(viewer != null) 
	  viewer.updateRoots();
      }
    }

    private TreeSet<Long>  pJobIDs; 
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Remove the working area files associated with the given nodes.
   */ 
  private
  class RemoveFilesTask
    extends UIMaster.RemoveFilesTask
  {
    public 
    RemoveFilesTask
    (
     String name
    ) 
    {
      UIMaster.getInstance().super(name, pAuthor, pView);
      setName("JNodeDetailsPanel:RemoveFilesTask");
    }
    
    protected void
    postOp() 
    {
      if(pGroupID > 0) {
	PanelGroup<JNodeViewerPanel> panels = UIMaster.getInstance().getNodeViewerPanels();
	JNodeViewerPanel viewer = panels.getPanel(pGroupID);
	if(viewer != null) 
	  viewer.updateRoots();
      }      
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2714804145579513176L;


  private static final int  sTSize = 180;
  private static final int  sVSize = 160;
  private static final int  sSSize = 343;


  private static Icon sFrozenIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("FrozenIcon.png"));


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
   * Cached names and version numbers of the loaded editor plugins. 
   */
  private TreeMap<String,TreeSet<VersionID>>  pEditorPlugins; 

  /**
   * The menu layout for editor plugins.
   */ 
  private PluginMenuLayout  pEditorMenuLayout;

  /**
   * Whether the Swing editor menus need to be rebuild from the menu layout.
   */ 
  private boolean pRefreshEditorMenus; 

  
  /**
   * Cached names and version numbers of the loaded action plugins. 
   */
  private TreeMap<String,TreeSet<VersionID>>  pActionPlugins; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The working file popup menu.
   */ 
  private JPopupMenu  pWorkingPopup; 
  
  /**
   * The working file popup menu items.
   */ 
  private JMenuItem  pApplyItem;
  private JMenuItem  pQueueJobsItem;
  private JMenuItem  pQueueJobsSpecialItem;
  private JMenuItem  pPauseJobsItem;
  private JMenuItem  pResumeJobsItem;
  private JMenuItem  pKillJobsItem;
  private JMenuItem  pRemoveFilesItem;  

  /**
   * The checked-in file popup menu.
   */ 
  private JPopupMenu  pCheckedInPopup; 

  /**
   * The edit with submenus.
   */ 
  private JMenuItem[]  pEditItems;
  private JMenu[]      pEditWithMenus; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The node name/state header.
   */ 
  private JLabel  pHeaderIcon;
  private JLabel  pHeaderLabel;
  
  /**
   * The fully resolved node name field.
   */ 
  private JTextField pNodeNameField;
  
  /**
   * An icon which indicates whether the working version is frozen.
   */
  private boolean  pIsFrozen; 
  private JLabel   pFrozenLabel;

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
   * Whether the drawers containing the single valued action parameter components are
   * open indexed by parameter group name.
   */ 
  private TreeMap<String,Boolean>  pActionParamGroupsOpen; 

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
   * The ramp-up interval title label.
   */ 
  private JLabel  pRampUpTitle;

  /**
   * The working ramp-up interval field.
   */ 
  private JIntegerField pWorkingRampUpField;

  /**
   * The set ramp-up interval button.
   */ 
  private JButton  pSetRampUpButton;

  /**
   * The checked-in ramp-up interval field.
   */ 
  private JTextField pCheckedInRampUpField;


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
