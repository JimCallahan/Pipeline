// $Id: JNodeDetailsPanel.java,v 1.64 2009/09/01 10:59:39 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.net.URI;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.text.*; 

/*------------------------------------------------------------------------------------------*/
/*   N O D E   D E T A I L S   P A N E L                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A viewer/editor of node properties. <P> 
 * 
 * The node properties displayed include: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Whether the file sequences associated with the node are intermediate. <BR>
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
  extends JBaseNodeDetailPanel
  implements ComponentListener, DocumentListener
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
      pActionParamGroupsOpen = new TreeMap<String,Boolean>();

      pDocToParamName = new ListMap<Document,String>();

      pSelectionKeyComponents = new TreeMap<String,Component[]>();
      pLicenseKeyComponents   = new TreeMap<String,Component[]>();
      pHardwareKeyComponents  = new TreeMap<String,Component[]>();

      pAnnotations       = new TreeMap<String,BaseAnnotation[]>();
      pAnnotationsPanels = new TreeMap<String,JAnnotationPanel>(); 
      pDocToAnnotName    = new ListMap<Document,String>();

      /* separate the sources for working and checked-in nodes */
      {
	pWorkingLinkActionParamValues    = new ArrayList<String>();
	pWorkingLinkActionParamNodeNames = new ArrayList<String>();

	pCheckedInLinkActionParamValues    = new ArrayList<String>();
	pCheckedInLinkActionParamNodeNames = new ArrayList<String>();

	pWorkingSources   = new TreeMap<String,NodeCommon>();
	pCheckedInSources = new DoubleMap<String,VersionID,NodeCommon>();
      }
    }

    /* initialize the popup menus */ 
    {
      initBasicMenus(true, true); 
      
      pSelectEditorPopup = new JPopupMenu();
      pSelectActionPopup = new JPopupMenu();

      updateMenuToolTips();
    }

    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

      /* header */ 
      {
        pApplyToolTipText   = "Apply the changes to node properties."; 
        pUnApplyToolTipText = "There are no unsaved changes to Apply at this time."; 

	JPanel panel = initHeader(true); 
	add(panel);
      }

      add(Box.createRigidArea(new Dimension(0, 4)));

      /* full node name */ 
      {
        initNameField(this);
        pNodeNameField.setFocusable(true);     
        pNodeNameField.addKeyListener(this);   
        pNodeNameField.addMouseListener(this); 
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
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  JLabel label = UIFactory.createFixedLabel
		    ("Property State:", sTSize, JLabel.RIGHT, 
		     "The relationship between the values of the node properties " + 
		     "associated with the working and checked-in versions of a node.");
		  hbox.add(label);
		}
		tpanel.add(hbox);
	      }

	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  JTextField field = UIFactory.createTextField("-", sSSize, JLabel.CENTER);
		  pPropertyStateField = field;

		  hbox.add(field);
		}
		vpanel.add(hbox);
	      }
	    }

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	    /* intermediate files */ 
	    { 
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  JLabel label = UIFactory.createFixedLabel
		    ("Intermediate Files:", sTSize, JLabel.RIGHT, 
		     "Whether the file sequences managed by this node are intermediate " + 
                     "(temporary) in nature and therefore should never be saved/restored " + 
                     "along with repository versions.");
		  pIntermediateTitle = label;
		  hbox.add(label);
		}
		tpanel.add(hbox);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);

		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add("-");
		  
		  JBooleanField field = UIFactory.createBooleanField(sVSize);
		  pWorkingIntermediateField = field;
		  
		  field.setActionCommand("intermediate-changed");
		  field.addActionListener(this);

		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JButton btn = new JButton();		 
		  pSetIntermediateButton = btn;
		  btn.setName("SmallLeftArrowButton");
		  
		  Dimension size = new Dimension(12, 12);
		  btn.setMinimumSize(size);
		  btn.setMaximumSize(size);
		  btn.setPreferredSize(size);
	    
		  btn.setActionCommand("set-intermediate");
		  btn.addActionListener(this);
		  
		  hbox.add(btn);
		} 

		hbox.add(Box.createRigidArea(new Dimension(4, 0)));

		{
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInIntermediateField = field;

		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	    /* toolset */ 
	    { 
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  JLabel label = UIFactory.createFixedLabel
		    ("Toolset:", sTSize, JLabel.RIGHT, 
		     "The name of the shell environment used to run Editors and Actions " + 
		     "associated with the node.");
		  pToolsetTitle = label;
		  hbox.add(label);
		}
		tpanel.add(hbox);
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

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	    /* editor */ 
	    { 
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  {
		    JButton btn = new JButton();
		    pEditorHelpButton = btn;
		    btn.setName("HelpButton");

		    Dimension size = new Dimension(19, 19);
		    btn.setMinimumSize(size);
		    btn.setMaximumSize(size);
		    btn.setPreferredSize(size);

		    btn.setActionCommand("show-editor-help");
		    btn.addActionListener(this);

		    hbox.add(btn);
		  }

		  hbox.add(Box.createRigidArea(new Dimension(4, 0)));
		  hbox.add(Box.createHorizontalGlue());

		  String text        = "Editor:";
		  String tooltipText = "The name of the Editor plugin used to " + 
				       "edit/view the files associated " +
		                       "with the node.";

		  JLabel label = new JLabel(text);
		  {
		    Font font = label.getFont();
		    FontMetrics fontmetrics = label.getFontMetrics(font);
		    Graphics graphics = label.getGraphics();

		    Rectangle2D rect = fontmetrics.getStringBounds(text, graphics);

		    int width = (int)rect.getWidth();

		    Dimension size = new Dimension(width, 19);
		    label.setMinimumSize(size);
		    label.setMaximumSize(size);
		    label.setPreferredSize(size);

		    label.setHorizontalAlignment(JLabel.RIGHT);
		    label.setToolTipText(UIFactory.formatToolTip(tooltipText));
		  }

		  pEditorTitle = label;
		  hbox.add(label);
		}

		tpanel.add(hbox);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);

		{
		  JPluginSelectionField field = 
		    UIMaster.getInstance().createEditorSelectionField(pGroupID, sVSize);
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
	  
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    /* editor version */ 
	    { 
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  JLabel label = UIFactory.createFixedLabel
		    ("Version:", sTSize, JLabel.RIGHT, 
		     "The revision number of the Editor plugin.");
		  pEditorVersionTitle = label;
		  hbox.add(label);
		}
		tpanel.add(hbox);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pWorkingEditorVersionField = field;
		  
		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(20, 0)));
		
		{
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInEditorVersionField = field;
		  
		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
	    /* editor vendor */ 
	    { 
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  JLabel label = UIFactory.createFixedLabel
		    ("Vendor:", sTSize, JLabel.RIGHT, 
		     "The name of the vendor of the Editor plugin.");
		  pEditorVendorTitle = label;
		  hbox.add(label);
		}
		tpanel.add(hbox);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pWorkingEditorVendorField = field;
		  
		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(20, 0)));
		
		{
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInEditorVendorField = field;
		  
		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

 	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
 	    /* editor operating system sypport */ 
 	    { 
 	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  JLabel label = UIFactory.createFixedLabel
		    ("OS Support:", sTSize, JLabel.RIGHT, 
 		     "The operating system types supported by the Editor plugin.");
		  pEditorOsSupportTitle = label;
		  hbox.add(label);
		}
 		tpanel.add(hbox);
 	      }
	      
 	      {
 		Box hbox = new Box(BoxLayout.X_AXIS);
		
 		{
 		  JOsSupportField field = UIFactory.createOsSupportField(sVSize);
 		  pWorkingEditorOsSupportField = field;
		  
 		  hbox.add(field);
 		}
		
 		hbox.add(Box.createRigidArea(new Dimension(20, 0)));
		
 		{
 		  JOsSupportField field = UIFactory.createOsSupportField(sVSize);
 		  pCheckedInEditorOsSupportField = field;
		  
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
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  {
		    JButton btn = new JButton();
		    pActionHelpButton = btn;
		    btn.setName("HelpButton");

		    Dimension size = new Dimension(19, 19);
		    btn.setMinimumSize(size);
		    btn.setMaximumSize(size);
		    btn.setPreferredSize(size);

		    btn.setActionCommand("show-action-help");
		    btn.addActionListener(this);

		    hbox.add(btn);
		  }

		  hbox.add(Box.createRigidArea(new Dimension(4, 0)));
		  hbox.add(Box.createHorizontalGlue());

		  String text        = "Action:";
		  String tooltipText = "The name of the Action plugin used to " + 
		                       "regenerate the files associated with the node.";

		  JLabel label = new JLabel(text);
		  {
		    Font font = label.getFont();
		    FontMetrics fontmetrics = label.getFontMetrics(font);
		    Graphics graphics = label.getGraphics();

		    Rectangle2D rect = fontmetrics.getStringBounds(text, graphics);

		    int width = (int)rect.getWidth();

		    Dimension size = new Dimension(width, 19);
		    label.setMinimumSize(size);
		    label.setMaximumSize(size);
		    label.setPreferredSize(size);

		    label.setHorizontalAlignment(JLabel.RIGHT);
		    label.setToolTipText(UIFactory.formatToolTip(tooltipText));
		  }

		  pActionTitle = label;
		  hbox.add(label);
		}

		tpanel.add(hbox);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  JPluginSelectionField field = 
		    UIMaster.getInstance().createActionSelectionField(pGroupID, sVSize);
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
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  JLabel label = UIFactory.createFixedLabel
		    ("Version:", sTSize, JLabel.RIGHT, 
		     "The revision number of the Action plugin.");
		  pActionVersionTitle = label;
		  hbox.add(label);
		}
		tpanel.add(hbox);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pWorkingActionVersionField = field;

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
	    
	    /* action vendor */ 
	    { 
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  JLabel label = UIFactory.createFixedLabel
		    ("Vendor:", sTSize, JLabel.RIGHT, 
		     "The name of the vendor of the Action plugin.");
		  pActionVendorTitle = label;
		  hbox.add(label);
		}

		tpanel.add(hbox);
	      }
	      
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		
		{
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pWorkingActionVendorField = field;
		  
		  hbox.add(field);
		}
		
		hbox.add(Box.createRigidArea(new Dimension(20, 0)));
		
		{
		  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
		  pCheckedInActionVendorField = field;
		  
		  hbox.add(field);
		}
		
		vpanel.add(hbox);
	      }
	    }

 	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	    
 	    /* action operating system sypport */ 
 	    { 
 	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  JLabel label = UIFactory.createFixedLabel
		    ("OS Support:", sTSize, JLabel.RIGHT, 
 		     "The operating system types supported by the Action plugin.");
		  pActionOsSupportTitle = label;
		  hbox.add(label);
		}

		tpanel.add(hbox);
 	      }
	      
 	      {
 		Box hbox = new Box(BoxLayout.X_AXIS);
		
 		{
 		  JOsSupportField field = UIFactory.createOsSupportField(sVSize);
 		  pWorkingActionOsSupportField = field;
		  
 		  hbox.add(field);
 		}
		
 		hbox.add(Box.createRigidArea(new Dimension(20, 0)));
		
 		{
 		  JOsSupportField field = UIFactory.createOsSupportField(sVSize);
 		  pCheckedInActionOsSupportField = field;
		  
 		  hbox.add(field);
 		}
		
 		vpanel.add(hbox);
 	      }
 	    }

	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	    /* action enabled */ 
	    { 
	      {
		Box hbox = new Box(BoxLayout.X_AXIS);
		{
		  JLabel label = UIFactory.createFixedLabel
		    ("Enabled:", sTSize, JLabel.RIGHT, 
		     "Whether the Action plugin is currently enabled.");
		  pActionEnabledTitle = label;
		  hbox.add(label);
		}

		tpanel.add(hbox);
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
            jrbox.add(UIFactory.createSidebar());
	
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
	      
	      /* hardware keys */ 
	      {
		Box box = new Box(BoxLayout.Y_AXIS);
		pHardwareKeysBox = box;

		JDrawer drawer = new JDrawer("Hardware Keys:", box, false);
		drawer.setToolTipText(UIFactory.formatToolTip
		  ("The set of hardware keys a server must have in order to be eligable " + 
		   "to run jobs associated with this node."));
		pHardwareDrawer = drawer;
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
	  Box abox = new Box(BoxLayout.X_AXIS);

          abox.addComponentListener(this);
          abox.add(UIFactory.createSidebar());
          
          {
            Box avbox = new Box(BoxLayout.Y_AXIS);
            pAnnotationsBox = avbox;

            abox.add(avbox);
          }

	  JDrawer drawer = new JDrawer("Version Annotations:", abox, false);
	  drawer.setToolTipText(UIFactory.formatToolTip
            ("Annotation plugins associated with each node version.")); 
	  pAnnotationsDrawer = drawer;
	  vbox.add(drawer);
	}

        vbox.add(UIFactory.createFiller(sTSize+sVSize+30));
	vbox.add(Box.createVerticalGlue());

	{
	  JScrollPane scroll = UIFactory.createVertScrollPane(vbox);
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

    updateNodeStatus(null, null, null, null, null, null);
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
  @Override
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
  @Override
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
    /*
     * If we were trying to build a non-zero channel panel and failed, then the cache has
     * not been cleared by JManagerPanel and we need to do it ourselves here.
     */
    else if (groupID > 0)
      UIMaster.getInstance().getUICache(0).invalidateCaches();

    master.updateOpsBar();
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  @Override
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
   * Are the contents of the panel read-only. <P> 
   */ 
  @Override
  public boolean
  isLocked() 
  {
    return (super.isLocked() && !pPrivilegeDetails.isNodeManaged(pAuthor));
  }
  
  /**
   * Set the author and view.
   */ 
  @Override
  public synchronized void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    super.setAuthorView(author, view);    

    updatePanels();
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
    if((pStatus != null) && pStatus.hasLightDetails())
      return pStatus.getLightDetails().getWorkingVersion();
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

  /**
   * Initialize the temporary editor with the editor of the working version 
   * of the current node.
   * 
   * @return 
   *   The working editor or <CODE>null</CODE> if none exists.
   */ 
  private BaseEditor
  initWorkingEditor() 
  {
    pWorkingEditor = null;

    NodeMod mod = getWorkingVersion();
    if(mod != null) 
      pWorkingEditor = mod.getEditor();

    return pWorkingEditor;
  }

  /**
   * Get the temporary editor of the working version of the current node.
   * 
   * @return 
   *   The working editor or <CODE>null</CODE> if none exists.
   */ 
  private BaseEditor
  getWorkingEditor() 
  {
    return pWorkingEditor;
  }

  /**
   * Set the temporary editor of the working version of the current node.
   * 
   * @param editor
   *   The working editor.
   */ 
  private void
  setWorkingEditor
  (
   BaseEditor editor
  ) 
  {
    pWorkingEditor = editor;
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current node have any checked-in versions?
   */ 
  private boolean 
  hasCheckedIn() 
  {
    return ((pStatus != null) && pStatus.hasLightDetails() && 
	    (pStatus.getLightDetails().getLatestVersion() != null));
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
    if((pStatus != null) && pStatus.hasLightDetails()) {
      NodeDetailsLight details = pStatus.getLightDetails();
      if(details.getLatestVersion() != null) {
	ArrayList<VersionID> vids = details.getVersionIDs();
	Collections.reverse(vids);
	VersionID vid = vids.get(pCheckedInVersionField.getSelectedIndex());
	
	vsn = pCheckedInVersions.get(vid);
	if(vsn == null) {
	  UIMaster master = UIMaster.getInstance();
	  MasterMgrClient client = master.acquireMasterMgrClient();
	  try {
	    vsn = client.getCheckedInVersion(pStatus.getName(), vid);
	    pCheckedInVersions.put(vid, vsn);

	    /* cache the sources for a checked-in version */
	    for(LinkVersion link : vsn.getSources()) {
	      String sname = link.getName();
	      VersionID svid = link.getVersionID();
	      NodeVersion node = client.getCheckedInVersion(sname, svid);
	      pCheckedInSources.put(sname, svid, node);
	    }
	  }
	  catch(PipelineException ex) {
	    master.showErrorDialog(ex);
	    return null;
	  }
	  finally {
	    master.releaseMasterMgrClient(client);
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
   * Update all panels which share the current update channel.
   */ 
  @Override
  public void 
  updatePanels() 
  {
    if (pGroupID != 0) {
      PanelUpdater pu = new PanelUpdater(this);
      pu.execute();
    }
  }

  /**
   * Apply the updated information to this panel.
   * 
   * @param author
   *   Owner of the current working area.
   * 
   * @param view
   *   Name of the current working area view.
   * 
   * @param status
   *   The current status for the node being displayed. 
   * 
   * @param licenseKeys
   *   The current license keys.
   * 
   * @param selectionKeys
   *   The current selection keys.
   */
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view,
   NodeStatus status, 
   ArrayList<LicenseKey> licenseKeys, 
   ArrayList<SelectionKey> selectionKeys,
   ArrayList<HardwareKey> hardwareKeys,
   TreeMap<String,NodeCommon> workingSources, 
   DoubleMap<String,VersionID,NodeCommon> checkedInSources
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);    

    updateNodeStatus(status, licenseKeys, selectionKeys, hardwareKeys,
                     workingSources, checkedInSources);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components to reflect the given node status.
   * 
   * @param status
   *   The current node status.
   * 
   * @param licenseKeys
   *   The current license keys.
   * 
   * @param selectionKeys
   *   The current selection keys.
   * 
   * @param workingSources 
   *   The source versions of the working version.
   * 
   * @param checkedInSources
   *   The source versions for select checked-in versions.
   */
  protected synchronized void 
  updateNodeStatus
  (
   NodeStatus status, 
   ArrayList<LicenseKey> licenseKeys, 
   ArrayList<SelectionKey> selectionKeys,
   ArrayList<HardwareKey> hardwareKeys, 
   TreeMap<String,NodeCommon> workingSources, 
   DoubleMap<String,VersionID,NodeCommon> checkedInSources
  ) 
  {
    super.updateNodeStatus(status, false);

    pLicenseKeys   = licenseKeys; 
    pSelectionKeys = selectionKeys;
    pHardwareKeys  = hardwareKeys;

    if(workingSources != null) 
      pWorkingSources = workingSources;
    else
      pWorkingSources.clear(); 
    
    if(checkedInSources != null) 
      pCheckedInSources = checkedInSources;
    else
      pCheckedInSources.clear(); 

    NodeDetailsLight details = null;
    if(pStatus != null) 
      details = pStatus.getLightDetails();

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
      
      /* intermediate */ 
      {
	pWorkingIntermediateField.removeActionListener(this);
	{
	  if(work != null) 
	    pWorkingIntermediateField.setValue(work.isIntermediate());
	  else 
	    pWorkingIntermediateField.setValue(null);

	  pWorkingIntermediateField.setEnabled(!isLocked() && !pIsFrozen && (work != null));
	}
	pWorkingIntermediateField.addActionListener(this);
	
	pSetIntermediateButton.setEnabled
	  (!isLocked() && !pIsFrozen && (work != null) && (latest != null));
	
	{
	  if(latest != null) 
	    pCheckedInIntermediateField.setText(latest.isIntermediate() ? "YES" : "no");
	  else 
	    pCheckedInIntermediateField.setText("-");

	  pCheckedInIntermediateField.setEnabled(latest != null);
	}

	updateIntermediateColors();
      }

      /* toolset */ 
      {
	pWorkingToolsetField.removeActionListener(this);
	{
	  TreeSet<String> toolsets = new TreeSet<String>();
	  if(work != null) {
	    UIMaster master = UIMaster.getInstance();
	    UICache cache = master.getUICache(pGroupID);
	    try {
	      toolsets.addAll(cache.getCachedActiveToolsetNames());
	      if((work.getToolset() != null) && !toolsets.contains(work.getToolset()))
		toolsets.add(work.getToolset());
	    }
	    catch(PipelineException ex) {
	    }
	  }
	  
	  if(toolsets.isEmpty())
	    toolsets.add("-");
	  
	  LinkedList<String> vlist = new LinkedList<String>(toolsets);
	  Collections.reverse(vlist);	 
	  pWorkingToolsetField.setValues(vlist);

	  if((work != null) && (work.getToolset() != null)) 
	    pWorkingToolsetField.setSelected(work.getToolset());
	  else 
	    pWorkingToolsetField.setSelected("-");

	  pWorkingToolsetField.setEnabled(!isLocked() && !pIsFrozen && (work != null));
	}
	pWorkingToolsetField.addActionListener(this);
	
	pSetToolsetButton.setEnabled
	  (!isLocked() && !pIsFrozen && (work != null) && (latest != null));
	
	{
	  if(latest != null) 
	    pCheckedInToolsetField.setText(latest.getToolset());
	  else 
	    pCheckedInToolsetField.setText("-");

	  pCheckedInToolsetField.setEnabled(latest != null);
	}

	updateToolsetColors();
      }

      /* editor */ 
      { 
	pWorkingEditorField.removeActionListener(this);
	{
	  BaseEditor weditor = initWorkingEditor();
	  if(weditor != null)
	    pWorkingEditorField.setPlugin(weditor);
	  else
	    pWorkingEditorField.setPlugin(null);

	  updateEditorFields();
	  
	  pWorkingEditorField.setEnabled(!isLocked() && !pIsFrozen && (work != null));
	}
	pWorkingEditorField.addActionListener(this);
	
	pSetEditorButton.setEnabled
	  (!isLocked() && !pIsFrozen && (work != null) && (latest != null));
	
	{
	  BaseEditor editor = null;
	  if(latest != null) 
	    editor = latest.getEditor();

	  if(editor != null) {
	    pCheckedInEditorField.setText(editor.getName());
	    pCheckedInEditorVersionField.setText("v" + editor.getVersionID());
	    pCheckedInEditorVendorField.setText(editor.getVendor());
	    pCheckedInEditorOsSupportField.setSupports(editor.getSupports());
	  }
	  else {
	    pCheckedInEditorField.setText("-");
	    pCheckedInEditorVersionField.setText("-");
	    pCheckedInEditorVendorField.setText("-");	
	    pCheckedInEditorOsSupportField.setSupports(null);
	  }
	  
	  pCheckedInEditorField.setEnabled(latest != null);
	  pCheckedInEditorVersionField.setEnabled(latest != null);
	  pCheckedInEditorVendorField.setEnabled(latest != null);
	}

	updateEditorColors();
      }
    }
    
    /* actions panel */ 
    {
      pWorkingActionField.removeActionListener(this);
      { 
	BaseAction waction = initWorkingAction();
	if(waction != null) 
	  pWorkingActionField.setPlugin(waction);
	else
	  pWorkingActionField.setPlugin(null);
	
	pWorkingActionField.setEnabled(!isLocked() && !pIsFrozen && (work != null));
      }
      pWorkingActionField.addActionListener(this);

      pSetActionButton.setEnabled
	(!isLocked() && !pIsFrozen && (work != null) && (latest != null));

      {
	BaseAction caction = null;
	if(latest != null) 
	  caction = latest.getAction();

	if(caction != null) {
	  pCheckedInActionField.setText(caction.getName());
	  pCheckedInActionVersionField.setText("v" + caction.getVersionID());
	  pCheckedInActionVendorField.setText(caction.getVendor());
	  pCheckedInActionOsSupportField.setSupports(caction.getSupports());
	}
	else {
	  pCheckedInActionField.setText("-");
	  pCheckedInActionVersionField.setText("-");
	  pCheckedInActionVendorField.setText("-");
	  pCheckedInActionOsSupportField.setSupports(null);
	}
      }

      pWorkingActionEnabledField.removeActionListener(this);
      if((work != null) && (getWorkingAction() != null)) {
	pWorkingActionEnabledField.setValue(work.isActionEnabled()); 
	pWorkingActionEnabledField.setEnabled(!isLocked() && !pIsFrozen);
      }
      else {
	pWorkingActionEnabledField.setValue(null);
	pWorkingActionEnabledField.setEnabled(false);
      }
      pWorkingActionEnabledField.addActionListener(this);

      {
	if((latest != null) && (latest.getAction() != null)) 
	  pCheckedInActionEnabledField.setText(latest.isActionEnabled() ? "YES" : "no");
	else 
	  pCheckedInActionEnabledField.setText("-");
      }

      pActionParamComponents.clear();

      updateActionFields();
      updateActionParams(false);
      updateActionColors();
    }

    /* job requirements panel */ 
    updateJobRequirements(false, true);

    /* annotations panels */ 
    {
      TreeSet<String> anames = new TreeSet<String>();
      
      TreeMap<String,BaseAnnotation> wannots = null;
      {
        NodeMod mod = getWorkingVersion();
        if(mod != null) {
          wannots = mod.getAnnotations();
          anames.addAll(wannots.keySet());
        }
      }
        
      TreeMap<String,BaseAnnotation> cannots = null;
      {
        if(latest != null) {
          cannots = latest.getAnnotations(); 
          anames.addAll(cannots.keySet());
        }
      }
      
      pAnnotations.clear();
      for(String aname : anames) {
        BaseAnnotation annots[] = new BaseAnnotation[2];

        if(wannots != null) 
          annots[0] = wannots.get(aname);

        if(cannots != null) 
          annots[1] = cannots.get(aname);

        pAnnotations.put(aname, annots); 
      }

      rebuildAnnotationPanels();      
    }

    /* update help buttons */
    {
      pEditorHelpButton.setEnabled(false);
      pActionHelpButton.setEnabled(false);

      NodeCommon node = getWorkingVersion();

      if(node == null)
	node = getCheckedInVersion();

      if(node != null) {
	BaseEditor editor = node.getEditor();
	BaseAction action = node.getAction();

	if(editor != null && hasHelp(editor))
	  pEditorHelpButton.setEnabled(true);

	if(action != null && hasHelp(action))
	  pActionHelpButton.setEnabled(true);
      }
    }
  }

  /**
   * Set the temporary working and checked-in annotation tables from the values currently
   * in the annotation panel UI components.
   */ 
  private void
  extractAnnotationsFromPanels() 
  {
    TreeSet<String> anames = new TreeSet<String>();

    TreeMap<String,BaseAnnotation> wannots = new TreeMap<String,BaseAnnotation>();
    for(String aname : pAnnotationsPanels.keySet()) {
      JAnnotationPanel apanel = pAnnotationsPanels.get(aname);
      BaseAnnotation wannot = apanel.getWorkingAnnotation(); 
      if(wannot != null) {
        wannots.put(aname, wannot);
        anames.add(aname);
      }
    }

    TreeMap<String,BaseAnnotation> cannots = new TreeMap<String,BaseAnnotation>();
    for(String aname : pAnnotations.keySet()) {
      BaseAnnotation annots[] = pAnnotations.get(aname);
      if(annots[1] != null) {
        cannots.put(aname, annots[1]);
        anames.add(aname);
      }
    }

    pAnnotations.clear();
    for(String aname : anames) {
      BaseAnnotation annots[] = new BaseAnnotation[2];
      annots[0] = wannots.get(aname);
      annots[1] = cannots.get(aname);
      pAnnotations.put(aname, annots); 
    }
  }

  /**
   * Rebuild the annotation panels from the temporary working and checked-in annotation
   * tables.
   */ 
  private void 
  rebuildAnnotationPanels() 
  {
    pAnnotationsBox.removeAll();
    pAnnotationsPanels.clear(); 
    
    if(!pAnnotations.isEmpty()) {    
      String toolset = null;
      {
        NodeDetailsLight details = null;
        if(pStatus != null) 
          details = pStatus.getLightDetails();
        
        NodeMod work = null; 
        if(details != null) 
          work = details.getWorkingVersion();
      }

      for(String aname: pAnnotations.keySet()) {
        BaseAnnotation annots[] = pAnnotations.get(aname);
        JAnnotationPanel panel = new JAnnotationPanel(this, toolset, aname, annots);
        pAnnotationsBox.add(panel);        
        pAnnotationsPanels.put(aname, panel);
      }
    }
    
    pAnnotationsBox.revalidate(); 
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

    /* properties panel */ 
    {
      /* intermediate */ 
      {
        pCheckedInIntermediateField.setText(vsn.isIntermediate() ? "YES" : "no");
        updateIntermediateColors();
      }

      /* toolset */ 
      {
	if(vsn.getToolset() != null)
	  pCheckedInToolsetField.setText(vsn.getToolset());
	else 
	  pCheckedInToolsetField.setText("-");

	updateToolsetColors();
      }

      /* editor */ 
      {
	BaseEditor editor = vsn.getEditor();
	if(editor != null) {
	  pCheckedInEditorField.setText(editor.getName());
	  pCheckedInEditorVersionField.setText("v" + editor.getVersionID());
	  pCheckedInEditorVendorField.setText(editor.getVendor());
	  pCheckedInEditorOsSupportField.setSupports(editor.getSupports());
	}
	else {
	  pCheckedInEditorField.setText("-");
	  pCheckedInEditorVersionField.setText("-");
	  pCheckedInEditorVendorField.setText("-");
	  pCheckedInEditorOsSupportField.setSupports(null);
	}

	updateEditorColors();
      }
    }
    
    /* actions panel */ 
    {
      BaseAction action = vsn.getAction();	
      if(action != null) {
	pCheckedInActionField.setText(action.getName());
	pCheckedInActionVersionField.setText("v" + action.getVersionID());
	pCheckedInActionVendorField.setText(action.getVendor());
	pCheckedInActionOsSupportField.setSupports(action.getSupports());
      }
      else {
	pCheckedInActionField.setText("-");
	pCheckedInActionVersionField.setText("-");
	pCheckedInActionVendorField.setText("-");
	pCheckedInActionOsSupportField.setSupports(null);
      }

      if(action != null) 
	pCheckedInActionEnabledField.setText(vsn.isActionEnabled() ? "YES" : "no");
      else 
	pCheckedInActionEnabledField.setText("-");

      pActionParamComponents.clear();

      updateActionParams(false);
      updateActionColors();
    }

    /* job requirements panel */ 
    updateJobRequirements(false, false); 

    /* annotations */ 
    {
      TreeSet<String> anames = new TreeSet<String>();

      TreeMap<String,BaseAnnotation> wannots = new TreeMap<String,BaseAnnotation>();
      for(String aname : pAnnotationsPanels.keySet()) {
        JAnnotationPanel panel = pAnnotationsPanels.get(aname);
        BaseAnnotation annot = panel.getWorkingAnnotation();
        if(annot != null) {
          wannots.put(aname, annot); 
          anames.add(aname); 
        }
      }
        
      TreeMap<String,BaseAnnotation> cannots = vsn.getAnnotations(); 
      anames.addAll(cannots.keySet());
      
      pAnnotations.clear();
      for(String aname : anames) {
        BaseAnnotation annots[] = new BaseAnnotation[2];

        if(wannots != null) 
          annots[0] = wannots.get(aname);

        if(cannots != null) 
          annots[1] = cannots.get(aname);

        pAnnotations.put(aname, annots); 
      }

      rebuildAnnotationPanels();      
    }

    /* Update help buttons if there is no working version of the node. */
    {
      if(getWorkingVersion() == null) {
	pEditorHelpButton.setEnabled(false);
	pActionHelpButton.setEnabled(false);

	NodeCommon node = getCheckedInVersion();

	BaseEditor editor = node.getEditor();
	BaseAction action = node.getAction();

	if(editor != null && hasHelp(editor))
	  pEditorHelpButton.setEnabled(true);

	if(action != null && hasHelp(action))
	  pActionHelpButton.setEnabled(true);
      }
    }
  }

  /**
   * Update the appearance of the intermediate fields after a change of value.
   */ 
  private void 
  updateIntermediateColors() 
  {
    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String wtset = pWorkingIntermediateField.getValue() ? "YES" : "no";
      String ctset = pCheckedInIntermediateField.getText();
      if(!ctset.equals(wtset))
	color = Color.cyan;
    }

    pIntermediateTitle.setForeground(color);
    pWorkingIntermediateField.setForeground(color);
    pCheckedInIntermediateField.setForeground(color);
  }

  /**
   * Update the appearance of the toolset fields after a change of value.
   */ 
  private void 
  updateToolsetColors() 
  {
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

    if(hasWorking()) {
      UIMaster master = UIMaster.getInstance();
      String toolset = pWorkingToolsetField.getSelected();
      master.updateActionPluginField(pGroupID, toolset, pWorkingActionField);
      master.updateEditorPluginField(pGroupID, toolset, pWorkingEditorField);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the editor version and vendor fields. 
   */
  private void 
  updateEditorFields()
  {
    if(pWorkingEditorField.getPluginName() != null) {
      pWorkingEditorVersionField.setText("v" + pWorkingEditorField.getPluginVersionID());
      pWorkingEditorVendorField.setText(pWorkingEditorField.getPluginVendor());
      pWorkingEditorOsSupportField.setSupports(pWorkingEditorField.getPluginSupports());
    }
    else {
      pWorkingEditorVersionField.setText("-");
      pWorkingEditorVendorField.setText("-");
      pWorkingEditorOsSupportField.setSupports(null);
    }
  }

  /**
   * Update the appearance of the editor fields after a change of value.
   */ 
  private void 
  updateEditorColors() 
  {
    Color color = Color.white;
    if(hasWorking() && hasCheckedIn()) {
      String weditor = pWorkingEditorField.getPluginName();
      String wvsn    = pWorkingEditorVersionField.getText();
      String wvend   = pWorkingEditorVendorField.getText();

      String ceditor = pCheckedInEditorField.getText();
      String cvsn    = pCheckedInEditorVersionField.getText();
      String cvend   = pCheckedInEditorVendorField.getText();

      if(!ceditor.equals(weditor) || !cvsn.equals(wvsn) || !cvend.equals(wvend))
	color = Color.cyan;
    }

    pEditorTitle.setForeground(color);
    pWorkingEditorField.setForeground(color);
    pCheckedInEditorField.setForeground(color);

    pEditorVersionTitle.setForeground(color);
    pWorkingEditorVersionField.setForeground(color);
    pCheckedInEditorVersionField.setForeground(color);

    pEditorVendorTitle.setForeground(color);
    pWorkingEditorVendorField.setForeground(color);
    pCheckedInEditorVendorField.setForeground(color);

    pEditorOsSupportTitle.setForeground(color);
    pWorkingEditorOsSupportField.setForeground(color);
    pCheckedInEditorOsSupportField.setForeground(color);    
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the action version and vendor fields.
   */ 
  private void 
  updateActionFields()
  {
    BaseAction waction = getWorkingAction();
    if(waction != null) {
      pWorkingActionVersionField.setText("v" + waction.getVersionID());
      pWorkingActionVendorField.setText(waction.getVendor());
      pWorkingActionOsSupportField.setSupports(waction.getSupports());      
    }
    else {
      pWorkingActionVersionField.setText("-");
      pWorkingActionVendorField.setText("-");
      pWorkingActionOsSupportField.setSupports(null);
    }
  }

  /**
   * Update the UI components associated with the working and checked-in actions.
   */ 
  private void 
  updateActionParams
  (
   boolean modified
  ) 
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
	   "The Action plugin parameters associated with each source node file sequence.");
	pSourceParamComponents[0] = label;
	
	tpanel.add(label);
      }
      
      { 
	Box hbox = new Box(BoxLayout.X_AXIS);
	
	if((waction != null) && waction.supportsSourceParams()) {
	  JButton btn = new JButton((isLocked() || pIsFrozen) ? "View..." : "Edit...");
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
	    
	    ArrayList<String> snames  = new ArrayList<String>();
	    ArrayList<String> stitles = new ArrayList<String>();
	    ArrayList<FileSeq> sfseqs = new ArrayList<FileSeq>();
	    
	    for(String sname : pWorkingSources.keySet()) {
	      NodeCommon lcom = pWorkingSources.get(sname);

	      FileSeq primary = lcom.getPrimarySequence();
	      String stitle = primary.toString();

	      snames.add(sname);
	      stitles.add(stitle);
	      sfseqs.add(null);

	      for(FileSeq fseq : lcom.getSecondarySequences()) {
		snames.add(sname);
		stitles.add(stitle);
		sfseqs.add(fseq);
	      }
	    }
	    
	    pEditSourceParamsDialog = 
	      new JSourceParamsDialog
	        (getTopFrame(), !isLocked() && !pIsFrozen, title, snames, stitles, 
		 sfseqs, waction);
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
	  
	  btn.setEnabled(!isLocked() && !pIsFrozen && 
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

	    ArrayList<String> snames  = new ArrayList<String>();
	    ArrayList<String> stitles = new ArrayList<String>();
	    ArrayList<FileSeq> sfseqs = new ArrayList<FileSeq>();

	    for(LinkVersion link : vsn.getSources()) {
	      String sname = link.getName();
	      VersionID vid = link.getVersionID();

	      NodeCommon lcom = pCheckedInSources.get(sname, vid);

	      FileSeq primary = lcom.getPrimarySequence();
	      String stitle = primary.toString();

	      snames.add(sname);
	      stitles.add(stitle);
	      sfseqs.add(null);

	      for(FileSeq fseq : lcom.getSecondarySequences()) {
		snames.add(sname);
		stitles.add(stitle);
		sfseqs.add(fseq);
	      }
	    }
	    
	    pViewSourceParamsDialog = 
	      new JSourceParamsDialog
	        (getTopFrame(), false, title, snames, stitles, sfseqs, caction);
	  }
	}
	else {
	  JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
	  pSourceParamComponents[3] = field;
	  
	  hbox.add(field);
	}
	
	vpanel.add(hbox);
	
	doSourceParamsChanged(modified);
      }	
    }
    else {
      tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
      vpanel.add(Box.createHorizontalGlue());
    }
    pActionParamsBox.add(comps[2]);

    /* single valued parameters */ 
    if((action != null) && action.hasSingleParams()) {
      if(waction != null) {
	pWorkingLinkActionParamValues.clear();
	pWorkingLinkActionParamValues.add("-");

	pWorkingLinkActionParamNodeNames.clear();
	pWorkingLinkActionParamNodeNames.add(null);

	for(String sname : pWorkingSources.keySet()) {
	  NodeCommon node = pWorkingSources.get(sname);

	  pWorkingLinkActionParamValues.add(node.toString());
	  pWorkingLinkActionParamNodeNames.add(sname);
	}
      }

      if(caction != null) {
	pCheckedInLinkActionParamValues.clear();
	pCheckedInLinkActionParamValues.add("-");

	pCheckedInLinkActionParamNodeNames.clear();
	pCheckedInLinkActionParamNodeNames.add(null);

	NodeVersion vsn = getCheckedInVersion();

	for(String sname : vsn.getSourceNames()) {
	  LinkVersion link = vsn.getSource(sname);
	  VersionID vid = link.getVersionID();
	  NodeCommon node = pCheckedInSources.get(sname, vid);

	  pCheckedInLinkActionParamValues.add(node.toString());
	  pCheckedInLinkActionParamNodeNames.add(sname);
	}
      }

      {
	Box hbox = new Box(BoxLayout.X_AXIS);

	hbox.addComponentListener(this);
        hbox.add(UIFactory.createSidebar());
      
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

      int entries = 0;
      for(String pname : group.getEntries()) {
	if(pname == null) {
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	}
	else {
	  entries++;
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

		    field.setEnabled(!isLocked() && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof IntegerActionParam) {
		    Integer value = (Integer) aparam.getValue();
		    JIntegerField field = 
		      UIFactory.createIntegerField(value, sVSize, JLabel.CENTER);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!isLocked() && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof ByteSizeActionParam) {
		    Long value = (Long) aparam.getValue();
		    JByteSizeField field = 
		      UIFactory.createByteSizeField(value, sVSize, JLabel.CENTER);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!isLocked() && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof DoubleActionParam) {
		    Double value = (Double) aparam.getValue();
		    JDoubleField field = 
		      UIFactory.createDoubleField(value, sVSize, JLabel.CENTER);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!isLocked() && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof Color3dActionParam) {
		    Color3d value = (Color3d) aparam.getValue();
		    JColorField field = 
		      UIFactory.createColorField(getTopFrame(), value, sVSize);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!isLocked() && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof Tuple2iActionParam) {
		    Tuple2i value = (Tuple2i) aparam.getValue();
		    JTuple2iField field = UIFactory.createTuple2iField(value, sVSize);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!isLocked() && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof Tuple3iActionParam) {
		    Tuple3i value = (Tuple3i) aparam.getValue();
		    JTuple3iField field = UIFactory.createTuple3iField(value, sVSize);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!isLocked() && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof Tuple2dActionParam) {
		    Tuple2d value = (Tuple2d) aparam.getValue();
		    JTuple2dField field = UIFactory.createTuple2dField(value, sVSize);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!isLocked() && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof Tuple3dActionParam) {
		    Tuple3d value = (Tuple3d) aparam.getValue();
		    JTuple3dField field = UIFactory.createTuple3dField(value, sVSize);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!isLocked() && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof Tuple4dActionParam) {
		    Tuple4d value = (Tuple4d) aparam.getValue();
		    JTuple4dField field = UIFactory.createTuple4dField(value, sVSize);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!isLocked() && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof TextAreaActionParam) {
                    TextAreaActionParam bparam = (TextAreaActionParam) aparam; 
		    String value = (String) aparam.getValue();
                    int rows = bparam.getRows(); 
                    JTextArea area = UIFactory.createEditableTextArea(value, rows);
		    pcomps[1] = area;

                    int height = 19*rows + 3*(rows-1);
                    Dimension size = new Dimension(sVSize, height);
                    area.setMinimumSize(size);
                    area.setMaximumSize(new Dimension(Integer.MAX_VALUE, height)); 
                    area.setPreferredSize(size);
                    
                    Document doc = area.getDocument(); 
                    doc.addDocumentListener(this);
                    pDocToParamName.put(doc, pname);

                    area.setEnabled(!isLocked() && !pIsFrozen);

		    hbox.add(area);

                    /* pad below the title */ 
                    tpanel.add(Box.createRigidArea(new Dimension(0, height-19)));
		  }
		  else if(aparam instanceof StringActionParam) {
		    String value = (String) aparam.getValue();
		    JTextField field = 
		      UIFactory.createEditableTextField(value, sVSize, JLabel.CENTER);
		    pcomps[1] = field;

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!isLocked() && !pIsFrozen);

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

		    field.setEnabled(!isLocked() && !pIsFrozen);

		    hbox.add(field);
		  }
		  else if(aparam instanceof LinkActionParam) {
		    JCollectionField field = 
		      UIFactory.createCollectionField(pWorkingLinkActionParamValues, sVSize);
		    pcomps[1] = field;

		    String source = (String) aparam.getValue();
		    int idx = pWorkingLinkActionParamNodeNames.indexOf(source);
		    if(idx != -1) 
		      field.setSelectedIndex(idx);
		    else 
		      field.setSelected("-");

		    field.addActionListener(this);
		    field.setActionCommand("action-param-changed:" + aparam.getName());

		    field.setEnabled(!isLocked() && !pIsFrozen);

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

		btn.setEnabled(!isLocked() && !pIsFrozen && 
			       (waction != null) && (caction != null) && 
			       caction.getName().equals(waction.getName()));

		/* disable the button if LinkActionParam and the value does
		   not exist in both working and checked-in lists. */
		{
		  if(btn.isEnabled()) {
		    ActionParam aparam = caction.getSingleParam(param.getName());

		    if(aparam != null && aparam instanceof LinkActionParam) {
		      String source = (String) aparam.getValue();
		      int idx = pWorkingLinkActionParamNodeNames.indexOf(source);
		      if(idx == -1)
			btn.setEnabled(false);
		    }
		  }
		}

		hbox.add(btn);
	      } 

	      hbox.add(Box.createRigidArea(new Dimension(4, 0)));

	      {
		ActionParam aparam = null;
		if((caction != null) && 
		   ((waction == null) || caction.getName().equals(waction.getName())))
		  aparam = caction.getSingleParam(param.getName());

		if(aparam != null) {
                  if(aparam instanceof Color3dActionParam) {
		    Color3d value = (Color3d) aparam.getValue();
		    JColorField field = 
		      UIFactory.createColorField(getTopFrame(), value, sVSize);
		    pcomps[3] = field;

		    field.setEnabled(false); 

		    hbox.add(field);
		  }
		  else if(aparam instanceof Tuple2iActionParam) {
		    Tuple2i value = (Tuple2i) aparam.getValue();
		    JTuple2iField field = new JTuple2iField(); 
                    field.setValue(value);
		    pcomps[3] = field;

                    Dimension size = new Dimension(sVSize, 19);
                    field.setMinimumSize(size);
                    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
                    field.setPreferredSize(size);

		    field.setEnabled(false); 

		    hbox.add(field);
		  }
		  else if(aparam instanceof Tuple3iActionParam) {
		    Tuple3i value = (Tuple3i) aparam.getValue();
		    JTuple3iField field = new JTuple3iField(); 
                    field.setValue(value);
		    pcomps[3] = field;

                    Dimension size = new Dimension(sVSize, 19);
                    field.setMinimumSize(size);
                    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
                    field.setPreferredSize(size);

		    field.setEnabled(false); 

		    hbox.add(field);
		  }
		  else if(aparam instanceof Tuple2dActionParam) {
		    Tuple2d value = (Tuple2d) aparam.getValue();
		    JTuple2dField field = new JTuple2dField(); 
                    field.setValue(value);
		    pcomps[3] = field;

                    Dimension size = new Dimension(sVSize, 19);
                    field.setMinimumSize(size);
                    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
                    field.setPreferredSize(size);

		    field.setEnabled(false); 

		    hbox.add(field);
		  }
		  else if(aparam instanceof Tuple3dActionParam) {
		    Tuple3d value = (Tuple3d) aparam.getValue();
		    JTuple3dField field = new JTuple3dField(); 
                    field.setValue(value);
		    pcomps[3] = field;

                    Dimension size = new Dimension(sVSize, 19);
                    field.setMinimumSize(size);
                    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
                    field.setPreferredSize(size);

		    field.setEnabled(false); 

		    hbox.add(field);
		  }
		  else if(aparam instanceof Tuple4dActionParam) {
		    Tuple4d value = (Tuple4d) aparam.getValue();
		    JTuple4dField field = new JTuple4dField(); 
                    field.setValue(value);
		    pcomps[3] = field;

                    Dimension size = new Dimension(sVSize, 19);
                    field.setMinimumSize(size);
                    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
                    field.setPreferredSize(size);

		    field.setEnabled(false); 

		    hbox.add(field);
		  }
		  else if(aparam instanceof TextAreaActionParam) {
                    TextAreaActionParam bparam = (TextAreaActionParam) aparam; 
                    String value = (String) aparam.getValue();
                    int rows = bparam.getRows();
                    JTextArea area = UIFactory.createTextArea(value, rows);
		    pcomps[3] = area;
                    
                    int height = 19*rows + 3*(rows-1);
                    Dimension size = new Dimension(sVSize, height);
                    area.setMinimumSize(size);
                    area.setMaximumSize(new Dimension(Integer.MAX_VALUE, height)); 
                    area.setPreferredSize(size);

		    area.setEnabled(false); 

		    hbox.add(area);
                  }
                  else {
                    String text = "-";
                    {
                      if(aparam instanceof LinkActionParam) {
                        String source = (String) aparam.getValue();
                        int idx = pCheckedInLinkActionParamNodeNames.indexOf(source);
                        if(idx != -1) 
                          text = pCheckedInLinkActionParamValues.get(idx);
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
	} // End of else clause that is run if the param name is not null
      }
      if (entries > 0)
        dbox.add(comps[2]);
    }
    
    if(!group.getSubGroups().isEmpty())  {
      Box hbox = new Box(BoxLayout.X_AXIS);

      hbox.addComponentListener(this);
      hbox.add(UIFactory.createSidebar()); 

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
    StringBuilder buf = new StringBuilder();
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
   */ 
  private void 
  updateJobRequirements
  (
   boolean modified, 
   boolean initialize
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
	if(modified || initialize) {
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
	    (!isLocked() && !pIsFrozen && (waction != null));
	}
	
	pSetOverflowPolicyButton.setEnabled
	  (!isLocked() && !pIsFrozen && (waction != null) && (caction != null));
	
	if(caction != null)
	  pCheckedInOverflowPolicyField.setText(vsn.getOverflowPolicy().toTitle());
	else 
	  pCheckedInOverflowPolicyField.setText("-");

	doOverflowPolicyChanged(modified);
      }

      /* execution method */ 
      {
	if(modified || initialize) {
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
	    (!isLocked() && !pIsFrozen && (waction != null));
	}
	
	pSetExecutionMethodButton.setEnabled
	  (!isLocked() && !pIsFrozen && (waction != null) && (caction != null));
	
	if(caction != null)
	  pCheckedInExecutionMethodField.setText(vsn.getExecutionMethod().toTitle());
	else 
	  pCheckedInExecutionMethodField.setText("-");

	doExecutionMethodChanged(modified);
      }

      /* batch size */ 
      { 
	if(modified || initialize) {
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

	doBatchSizeChanged(modified);
      }

      /* priority */ 
      { 
	if(modified || initialize) {
	  pWorkingPriorityField.removeActionListener(this);
	  {
	    if(wjreq != null) 
	      pWorkingPriorityField.setValue(wjreq.getPriority());
	    else 
	      pWorkingPriorityField.setValue(null);	
	  }
	  pWorkingPriorityField.addActionListener(this);
	  
	  pWorkingPriorityField.setEnabled(!isLocked() && !pIsFrozen && (wjreq != null));
	}

	pSetPriorityButton.setEnabled
	  (!isLocked() && !pIsFrozen && (wjreq != null) && (cjreq != null));
	
	if(cjreq != null)
	  pCheckedInPriorityField.setText(String.valueOf(cjreq.getPriority()));
	else 
	  pCheckedInPriorityField.setText("-");

	doPriorityChanged(modified);
      }

      /* ramp-up interval */ 
      { 
	if(modified || initialize) {
	  pWorkingRampUpField.removeActionListener(this);
	  {
	    if(wjreq != null) 
	      pWorkingRampUpField.setValue(wjreq.getRampUp());
	    else 
	      pWorkingRampUpField.setValue(null);	
	  }
	  pWorkingRampUpField.addActionListener(this);
	  
	  pWorkingRampUpField.setEnabled(!isLocked() && !pIsFrozen && (wjreq != null));
	}

	pSetRampUpButton.setEnabled
	  (!isLocked() && !pIsFrozen && (wjreq != null) && (cjreq != null));
	
	if(cjreq != null)
	  pCheckedInRampUpField.setText(String.valueOf(cjreq.getRampUp()));
	else 
	  pCheckedInRampUpField.setText("-");

	doRampUpChanged(modified);
      }

      /* maximum load */ 
      { 
	if(modified || initialize) {
	  pWorkingMaxLoadField.removeActionListener(this);
	  {
	    if(wjreq != null) 
	      pWorkingMaxLoadField.setValue(wjreq.getMaxLoad());
	    else 
	      pWorkingMaxLoadField.setValue(null);	
	  }
	  pWorkingMaxLoadField.addActionListener(this);
	  
	  pWorkingMaxLoadField.setEnabled(!isLocked() && !pIsFrozen && (wjreq != null));
	}

	pSetMaxLoadButton.setEnabled
	  (!isLocked() && !pIsFrozen && (wjreq != null) && (cjreq != null));
	
	if(cjreq != null)
	  pCheckedInMaxLoadField.setText(String.valueOf(cjreq.getMaxLoad()));
	else 
	  pCheckedInMaxLoadField.setText("-");

	doMaxLoadChanged(modified);
      }

      /* minimum memory */ 
      { 
	if(modified || initialize) {
	  pWorkingMinMemoryField.removeActionListener(this);
	  {
	    if(wjreq != null) 
	      pWorkingMinMemoryField.setValue(wjreq.getMinMemory());
	    else 
	      pWorkingMinMemoryField.setValue(null);	
	  }
	  pWorkingMinMemoryField.addActionListener(this);
	  
	  pWorkingMinMemoryField.setEnabled(!isLocked() && !pIsFrozen && (wjreq != null));
	}

	pSetMinMemoryButton.setEnabled
	  (!isLocked() && !pIsFrozen && (wjreq != null) && (cjreq != null));
	
	if(cjreq != null)
	  pCheckedInMinMemoryField.setText
	    (ByteSize.longToString(cjreq.getMinMemory()));
	else 
	  pCheckedInMinMemoryField.setText("-");

	doMinMemoryChanged(modified);
      }

      /* minimum disk */ 
      { 
	if(modified || initialize) {
	  pWorkingMinDiskField.removeActionListener(this);
	  {
	    if(wjreq != null) 
	      pWorkingMinDiskField.setValue(wjreq.getMinDisk());
	    else 
	      pWorkingMinDiskField.setValue(null);	
	  }
	  pWorkingMinDiskField.addActionListener(this);
	  
	  pWorkingMinDiskField.setEnabled(!isLocked() && !pIsFrozen && (wjreq != null));
	}

	pSetMinDiskButton.setEnabled
	  (!isLocked() && !pIsFrozen && (wjreq != null) && (cjreq != null));
	
	if(cjreq != null)
	  pCheckedInMinDiskField.setText
	    (ByteSize.longToString(cjreq.getMinDisk()));
	else 
	  pCheckedInMinDiskField.setText("-");

	doMinDiskChanged(modified);
      }

      /* selection keys */ 
      {
	TreeMap<String,String> keys = new TreeMap<String,String>();
	if(pSelectionKeys != null) {
	  for(SelectionKey key : pSelectionKeys)
	    keys.put(key.getName(), key.getDescription());
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

		field.setEnabled(!isLocked() && !pIsFrozen && (wjreq != null));

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

		btn.setEnabled(!isLocked() && !pIsFrozen && 
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

	    doSelectionKeyChanged(kname, modified);
	  }
	}

	pSelectionKeysBox.add(comps[2]);
      }
      
      /* hardware keys */ 
      {
	TreeMap<String,String> keys = new TreeMap<String,String>();
	if(pHardwareKeys != null) {
	  for(HardwareKey key : pHardwareKeys)
	    keys.put(key.getName(), key.getDescription());
	}

	pHardwareKeysBox.removeAll();
	pHardwareKeyComponents.clear();

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
	      (wjreq != null) && wjreq.getHardwareKeys().contains(kname);
	    boolean hasCheckedInKey = 
	      (cjreq != null) && cjreq.getHardwareKeys().contains(kname);

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

		field.setActionCommand("hardware-key-changed:" + kname);
		field.addActionListener(this);

		field.setEnabled(!isLocked() && !pIsFrozen && (wjreq != null));

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

		btn.setActionCommand("set-hardware-key:" + kname);
		btn.addActionListener(this);

		btn.setEnabled(!isLocked() && !pIsFrozen && 
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

	    pHardwareKeyComponents.put(kname, pcomps);

	    doHardwareKeyChanged(kname, modified);
	  }
	}

	pHardwareKeysBox.add(comps[2]);
      }


      /* license keys */ 
      {
	TreeMap<String,String> keys = new TreeMap<String,String>();
	if(pLicenseKeys != null) {
	  for(LicenseKey key : pLicenseKeys)
	    keys.put(key.getName(), key.getDescription());
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
		
		field.setEnabled(!isLocked() && !pIsFrozen && (wjreq != null));
		
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
		
		btn.setEnabled(!isLocked() && !pIsFrozen && 
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
	    
	    doLicenseKeyChanged(kname, modified);
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
	    waction.getVersionID().equals(caction.getVersionID()) && 
	    waction.getVendor().equals(caction.getVendor()))))
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

    pActionVendorTitle.setForeground(fg);
    pWorkingActionVendorField.setForeground(fg);
    pCheckedInActionVendorField.setForeground(fg);

    pActionOsSupportTitle.setForeground(fg);
    pWorkingActionOsSupportField.setForeground(fg);
    pCheckedInActionOsSupportField.setForeground(fg);  

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
	  else if(aparam instanceof ByteSizeActionParam) {
	    JByteSizeField field = (JByteSizeField) pcomps[1];
	    if(field.getValue() != null) 
	      wtext = field.getValue().toString();
	  }
	  else if(aparam instanceof DoubleActionParam) {
	    JDoubleField field = (JDoubleField) pcomps[1];
	    if(field.getValue() != null) 
	      wtext = field.getValue().toString();
	  }
	  else if(aparam instanceof Color3dActionParam) {
	    JColorField field = (JColorField) pcomps[1];
	    if(field.getValue() != null) 
	      wtext = field.getValue().toString();
	  }
	  else if(aparam instanceof Tuple2iActionParam) {
	    JTuple2iField field = (JTuple2iField) pcomps[1];
	    if(field.getValue() != null) 
	      wtext = field.getValue().toString();  
	  }
	  else if(aparam instanceof Tuple3iActionParam) {
	    JTuple3iField field = (JTuple3iField) pcomps[1];
	    if(field.getValue() != null) 
	      wtext = field.getValue().toString();  
	  }
	  else if(aparam instanceof Tuple2dActionParam) {
	    JTuple2dField field = (JTuple2dField) pcomps[1];
	    if(field.getValue() != null) 
	      wtext = field.getValue().toString();  
	  }
	  else if(aparam instanceof Tuple3dActionParam) {
	    JTuple3dField field = (JTuple3dField) pcomps[1];
	    if(field.getValue() != null) 
	      wtext = field.getValue().toString();  
	  }
	  else if(aparam instanceof Tuple4dActionParam) {
	    JTuple4dField field = (JTuple4dField) pcomps[1];
	    if(field.getValue() != null) 
	      wtext = field.getValue().toString();  
	  }
	  else if(aparam instanceof TextAreaActionParam) {
	    JTextArea area = (JTextArea) pcomps[1];
	    wtext = area.getText();
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
	    wtext = pWorkingLinkActionParamNodeNames.get(field.getSelectedIndex());
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
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);
  
    encoder.encode("VersionDrawerOpen",     pVersionDrawer.isOpen());
    encoder.encode("PropertyDrawerOpen",    pPropertyDrawer.isOpen());
    encoder.encode("ActionDrawerOpen",      pActionDrawer.isOpen());
    encoder.encode("JobReqsDrawerOpen",     pJobReqsDrawer.isOpen());
    encoder.encode("SelectionDrawerOpen",   pSelectionDrawer.isOpen());
    encoder.encode("HardwareDrawerOpen",    pHardwareDrawer.isOpen());
    encoder.encode("LicenseDrawerOpen",     pLicenseDrawer.isOpen());
    encoder.encode("AnnotationsDrawerOpen", pAnnotationsDrawer.isOpen());
  }

  @Override
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
      Boolean open = (Boolean) decoder.decode("HardwareDrawerOpen");
      if(open != null) 
	pHardwareDrawer.setIsOpen(open);
    }

    {
      Boolean open = (Boolean) decoder.decode("LicenseDrawerOpen");
      if(open != null) 
	pLicenseDrawer.setIsOpen(open);
    }

    {
      Boolean open = (Boolean) decoder.decode("AnnotationsDrawerOpen");
      if(open != null) 
	pAnnotationsDrawer.setIsOpen(open);
    }

    super.fromGlue(decoder);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
  @Override
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    super.actionPerformed(e); 

    String cmd = e.getActionCommand();
    if(cmd.equals("update-version")) 
      updateVersion();
    else if(cmd.equals("set-intermediate")) 
      doSetIntermediate();
    else if(cmd.equals("intermediate-changed")) 
      doIntermediateChanged(true);
    else if(cmd.equals("set-toolset")) 
      doSetToolset();
    else if(cmd.equals("toolset-changed")) 
      doToolsetChanged(true);
    else if(cmd.equals("set-editor")) 
      doSetEditor();
    else if(cmd.equals("editor-changed")) 
      doEditorChanged(true);

    else if(cmd.equals("set-action")) 
      doSetAction();
    else if(cmd.equals("action-changed")) 
      doActionChanged(true);
    else if(cmd.equals("action-enabled-changed")) 
      doActionEnabledChanged(true);
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
      doOverflowPolicyChanged(true);
    else if(cmd.equals("set-execution-method")) 
      doSetExecutionMethod();
    else if(cmd.equals("execution-method-changed")) 
      doExecutionMethodChanged(true);
    else if(cmd.equals("set-batch-size")) 
      doSetBatchSize();
    else if(cmd.equals("batch-size-changed")) 
      doBatchSizeChanged(true);
    else if(cmd.equals("set-priority")) 
      doSetPriority();
    else if(cmd.equals("priority-changed")) 
      doPriorityChanged(true);
    else if(cmd.equals("set-ramp-up")) 
      doSetRampUp();
    else if(cmd.equals("ramp-up-changed")) 
      doRampUpChanged(true);
    else if(cmd.equals("set-maximum-load")) 
      doSetMaxLoad();
    else if(cmd.equals("maximum-load-changed")) 
      doMaxLoadChanged(true);
    else if(cmd.equals("set-minimum-memory")) 
      doSetMinMemory();
    else if(cmd.equals("minimum-memory-changed")) 
      doMinMemoryChanged(true);
    else if(cmd.equals("set-minimum-disk")) 
      doSetMinDisk();
    else if(cmd.equals("minimum-disk-changed")) 
      doMinDiskChanged(true);
    else if(cmd.startsWith("selection-key-changed:")) 
      doSelectionKeyChanged(cmd.substring(22), true);
    else if(cmd.startsWith("set-selection-key:")) 
      doSetSelectionKey(cmd.substring(18));
    else if(cmd.startsWith("hardware-key-changed:")) 
      doHardwareKeyChanged(cmd.substring(21), true);
    else if(cmd.startsWith("set-hardware-key:")) 
      doSetHardwareKey(cmd.substring(17));
    else if(cmd.startsWith("license-key-changed:")) 
      doLicenseKeyChanged(cmd.substring(20), true);
    else if(cmd.startsWith("set-license-key:")) 
      doSetLicenseKey(cmd.substring(16));

    else if(cmd.equals("add-annotation")) 
      doAddAnnotation();
    else if(cmd.startsWith("set-annotation:")) 
      doSetAnnotation(cmd.substring(15));
    else if(cmd.startsWith("annotation-changed:")) 
      doAnnotationChanged(cmd.substring(19), true);
    else if(cmd.startsWith("remove-annotation:"))
      doRemoveAnnotation(cmd.substring(18));
    else if(cmd.startsWith("rename-annotation:"))
      doRenameAnnotation(cmd.substring(18));
    else if(cmd.startsWith("set-annot-param:")) 
      doSetAnnotationParam(cmd.substring(16));
    else if(cmd.startsWith("annot-param-changed:")) 
      doAnnotationParamChanged(cmd.substring(20));

    else if(cmd.equals("show-editor-help"))
      doShowEditorHelp();
    else if(cmd.equals("show-action-help"))
      doShowActionHelp();
  }


  /*-- DOCUMENT LISTENER METHODS -----------------------------------------------------------*/

  public void 
  changedUpdate
  (
    DocumentEvent e
  )
  {}

  public void 
  insertUpdate
  (
    DocumentEvent e
  )
  {
    Document doc = e.getDocument();
    
    String aname = pDocToAnnotName.get(doc);
    if(aname != null) {
      JAnnotationPanel panel = pAnnotationsPanels.get(aname);
      if(panel != null) 
        panel.doAnnotationParamChanged(doc);
    }
    else {
      doActionParamChanged(doc);
    }
  }

  public void 
  removeUpdate
  (
    DocumentEvent e
  )
  { 
    Document doc = e.getDocument();
    
    String aname = pDocToAnnotName.get(doc);
    if(aname != null) {
      JAnnotationPanel panel = pAnnotationsPanels.get(aname);
      if(panel != null) 
        panel.doAnnotationParamChanged(doc);
    }
    else {
      doActionParamChanged(doc);
    }
  } 



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Modify the working version of the node based on the current settings if the 
   * UI components.
   */ 
  @Override
  public void 
  doApply()
  {
    super.doApply();

    if(isLocked() || pIsFrozen) 
      return;

    if((pStatus != null) && pStatus.hasLightDetails()) {
      NodeMod work = pStatus.getLightDetails().getWorkingVersion();
      if(work != null) {
	try { 
	  NodeMod mod = new NodeMod(work);
	  mod.removeAllSources();

	  /* properties panel */ 
	  {
            Boolean isIntermediate = pWorkingIntermediateField.getValue();
            if(isIntermediate != null) 
              mod.setIntermediate(isIntermediate);

	    String toolset = pWorkingToolsetField.getSelected();
	    if((toolset != null) && !toolset.equals("-"))
	      mod.setToolset(toolset);
	    
	    if(pWorkingEditorField.getPluginName() == null) {
	      mod.setEditor(null);
	    }
	    else {
	      try {
		PluginMgrClient pclient = PluginMgrClient.getInstance();
		mod.setEditor(pclient.newEditor(pWorkingEditorField.getPluginName(), 
						pWorkingEditorField.getPluginVersionID(),
						pWorkingEditorField.getPluginVendor()));
	      }
	      catch(PipelineException ex) {
		mod.setEditor(null);
	      }
	    }
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
		else if(aparam instanceof ByteSizeActionParam) {   
		  JByteSizeField field = (JByteSizeField) pcomps[1];
		  value = field.getValue();
		}
		else if(aparam instanceof DoubleActionParam) { 
		  JDoubleField field = (JDoubleField) pcomps[1];
		  value = field.getValue();
		}
		else if(aparam instanceof Color3dActionParam) { 
		  JColorField field = (JColorField) pcomps[1];
		  value = field.getValue();
		}
		else if(aparam instanceof Tuple2iActionParam) { 
		  JTuple2iField field = (JTuple2iField) pcomps[1];
		  value = field.getValue();
		}
		else if(aparam instanceof Tuple3iActionParam) { 
		  JTuple3iField field = (JTuple3iField) pcomps[1];
		  value = field.getValue();
		}
		else if(aparam instanceof Tuple2dActionParam) { 
		  JTuple2dField field = (JTuple2dField) pcomps[1];
		  value = field.getValue();
		}
		else if(aparam instanceof Tuple3dActionParam) { 
		  JTuple3dField field = (JTuple3dField) pcomps[1];
		  value = field.getValue();
		}
		else if(aparam instanceof Tuple4dActionParam) { 
		  JTuple4dField field = (JTuple4dField) pcomps[1];
		  value = field.getValue();
		}
                else if(aparam instanceof TextAreaActionParam) {
                  JTextArea area = (JTextArea) pcomps[1];
                  value = area.getText();
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
		  value = pWorkingLinkActionParamNodeNames.get(field.getSelectedIndex());
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
		
		/* batch size (Parallel) */ 
		if(idx == 2) {
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
	    
	    /* hardware keys */ 
	    {
	      jreq.removeAllHardwareKeys();

	      for(String kname : pHardwareKeyComponents.keySet()) {
		Component pcomps[] = pHardwareKeyComponents.get(kname);
		JBooleanField field = (JBooleanField) pcomps[1];
		Boolean value = field.getValue();
		if((value != null) && value) 
		  jreq.addHardwareKey(kname);
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

          {
            mod.removeAnnotations();
            for(String aname : pAnnotationsPanels.keySet()) {
              JAnnotationPanel panel = pAnnotationsPanels.get(aname);
              BaseAnnotation wannot = panel.getWorkingAnnotation();
              if(wannot != null) 
                mod.addAnnotation(aname, wannot);
            }
          }

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
   * Set the working intermediate field from the value of the checked-in field.
   */ 
  private void 
  doSetIntermediate()
  { 
    pWorkingIntermediateField.removeActionListener(this);
    {
      boolean isIntermediate = false;
      String str = pCheckedInIntermediateField.getText();
      if((str != null) && str.equals("YES"))
        isIntermediate = true;
        
      pWorkingIntermediateField.setValue(isIntermediate); 
    }
    pWorkingIntermediateField.addActionListener(this);
  
    doIntermediateChanged(true);
  }

  /**
   * Update the appearance of the intermediate field after a change of value.
   */ 
  private void 
  doIntermediateChanged
  (
   boolean modified
  ) 
  {
    if(modified) 
      unsavedChange("Intermediate Files"); 

    updateIntermediateColors();
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
	  
	  LinkedList<String> vlist = new LinkedList<String>(values);
	  Collections.reverse(vlist);	  
	  pWorkingToolsetField.setValues(vlist);
	}
	
	pWorkingToolsetField.setSelected(toolset);
      }
    }
    pWorkingToolsetField.addActionListener(this);
  
    doToolsetChanged(true);
  }

  /**
   * Update the appearance of the toolset field after a change of value.
   */ 
  private void 
  doToolsetChanged
  (
   boolean modified
  ) 
  {
    if(modified) 
      unsavedChange("Toolset"); 

    updateToolsetColors();
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
      NodeVersion vsn = getCheckedInVersion();
      pWorkingEditorField.setPlugin(vsn.getEditor());
    }
    pWorkingEditorField.addActionListener(this);

    doEditorChanged(true);
  }

  /**
   * Update the appearance of the editor field after a change of value.
   */ 
  private void 
  doEditorChanged
  (
   boolean modified
  )  
  {
    if(modified) 
      unsavedChange("Editor"); 

    updateEditorFields();
    updateEditorColors();

    /* update working editor */
    BaseEditor oeditor = getWorkingEditor();
    {
      String ename = pWorkingEditorField.getPluginName();

      if(ename == null) {
	setWorkingEditor(null);
      }
      else {
	VersionID vid = pWorkingEditorField.getPluginVersionID();
	String vendor = pWorkingEditorField.getPluginVendor();

	if((oeditor == null) || !oeditor.getName().equals(ename) ||
	   (vid == null) || !vid.equals(oeditor.getVersionID()) ||
	   (vendor == null) || !vendor.equals(oeditor.getVendor())) {

	  try {
	    setWorkingEditor(PluginMgrClient.getInstance().newEditor(ename, vid, vendor));
	  }
	  catch(PipelineException ex) {
	    UIMaster.getInstance().showErrorDialog(ex);
	    setWorkingEditor(null);
	  }
	}
      }
    }

    /* update help button */
    {
      pEditorHelpButton.setEnabled(false);

      BaseEditor editor = getWorkingEditor();

      if(editor != null && hasHelp(editor))
	pEditorHelpButton.setEnabled(true);
    }
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
      
      if(action != null) {
	pWorkingActionField.setPlugin(action);

	pWorkingActionEnabledField.setValue(vsn.isActionEnabled());
	pWorkingActionEnabledField.setEnabled(true);	  
      }
      else {
	pWorkingActionField.setPlugin(null);
	
	pWorkingActionEnabledField.setValue(null);
	pWorkingActionEnabledField.setEnabled(false);
      }
      setWorkingAction(action);
    }
    pWorkingActionField.addActionListener(this);

    pActionParamComponents.clear();
    pDocToParamName.clear();

    updateActionFields();
    updateActionParams(true);
    updateActionColors();

    updateJobRequirements((oaction == null) && (getWorkingAction() != null), false);
  }

  /**
   * Update the appearance of the action fields after a change of value.
   */ 
  private void 
  doActionChanged
  (
   boolean modified
  ) 
  {
    if(modified)
      unsavedChange("Action");

    BaseAction oaction = getWorkingAction();
    {
      String aname = pWorkingActionField.getPluginName();
      if(aname == null) {
	setWorkingAction(null);

	pWorkingActionEnabledField.setValue(null);
	pWorkingActionEnabledField.setEnabled(false);

	pActionParamComponents.clear();
	pActionParamGroupsOpen.clear();
        pDocToParamName.clear();
      }
      else {
	VersionID vid = pWorkingActionField.getPluginVersionID();
	String vendor = pWorkingActionField.getPluginVendor();
	if((oaction == null) || !oaction.getName().equals(aname) ||
	   (vid == null) || !vid.equals(oaction.getVersionID()) ||
	   (vendor == null) || !vendor.equals(oaction.getVendor())) {
	  try {
	    setWorkingAction(PluginMgrClient.getInstance().newAction(aname, vid, vendor));
	    
	    BaseAction waction = getWorkingAction();
	    if(oaction != null) {
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
  	    pWorkingActionField.setPlugin(null);
	    pWorkingActionField.addActionListener(this);
	  }

	  pActionParamComponents.clear();
	  pActionParamGroupsOpen.clear();
          pDocToParamName.clear();
	}
      }

      updateActionFields();
      updateActionParams(modified);
      updateActionColors();
    }

    updateJobRequirements((oaction == null) && (getWorkingAction() != null), false);

    /* update the help button */
    {
      pActionHelpButton.setEnabled(false);
      
      BaseAction action = getWorkingAction();

      if(action != null && hasHelp(action))
	pActionHelpButton.setEnabled(true);
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the appearance of the action enabled fields after a change of value.
   */ 
  private void 
  doActionEnabledChanged
  (
   boolean modified
  ) 
  {
    if(modified) 
      unsavedChange("Action Enabled");

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
	else if(wparam instanceof ByteSizeActionParam) {
	  JByteSizeField field = (JByteSizeField) pcomps[1];
	  field.setValue((Long) value);
	}
	else if(wparam instanceof DoubleActionParam) {
	  JDoubleField field = (JDoubleField) pcomps[1];
	  field.setValue((Double) value);
	}
	else if(wparam instanceof Color3dActionParam) {
	  JColorField field = (JColorField) pcomps[1];
	  field.setValue((Color3d) value);
	}
	else if(wparam instanceof Tuple2iActionParam) {
	  JTuple2iField field = (JTuple2iField) pcomps[1];
	  field.setValue((Tuple2i) value);
	}
	else if(wparam instanceof Tuple3iActionParam) {
	  JTuple3iField field = (JTuple3iField) pcomps[1];
	  field.setValue((Tuple3i) value);
	}
	else if(wparam instanceof Tuple2dActionParam) {
	  JTuple2dField field = (JTuple2dField) pcomps[1];
	  field.setValue((Tuple2d) value);
	}
	else if(wparam instanceof Tuple3dActionParam) {
	  JTuple3dField field = (JTuple3dField) pcomps[1];
	  field.setValue((Tuple3d) value);
	}
	else if(wparam instanceof Tuple4dActionParam) {
	  JTuple4dField field = (JTuple4dField) pcomps[1];
	  field.setValue((Tuple4d) value);
	}
        else if(wparam instanceof TextAreaActionParam) {
          JTextArea area = (JTextArea) pcomps[1];
	  if(value != null) 
	    area.setText(value.toString());
	  else 
	    area.setText(null);
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
	  
	  int idx = pWorkingLinkActionParamNodeNames.indexOf(value);
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
    unsavedChange("Action Parameter: " + pname);
    updateActionParamColor(pname, null);
  }

  /**
   * Update the appearance of the action parameter fields after a change of parameter value.
   */ 
  private void 
  doActionParamChanged
  (
   Document doc
  ) 
  {
    String pname = pDocToParamName.get(doc);
    unsavedChange("Action Parameter: " + pname);
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
      doSourceParamsChanged(true);
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
	
	ArrayList<String> snames  = new ArrayList<String>();
	ArrayList<String> stitles = new ArrayList<String>();
	ArrayList<FileSeq> sfseqs = new ArrayList<FileSeq>();
	
	for(String sname : pStatus.getSourceNames()) {
	  NodeMod lmod = pStatus.getSource(sname).getLightDetails().getWorkingVersion();
	  
	  FileSeq primary = lmod.getPrimarySequence();
	  String stitle = primary.toString();
	  
	  snames.add(sname);
	  stitles.add(stitle);
	  sfseqs.add(null);
	  
	  for(FileSeq fseq : lmod.getSecondarySequences()) {
	    snames.add(sname);
	    stitles.add(stitle);
	    sfseqs.add(fseq);
	  }
	}
	
	pEditSourceParamsDialog = 
	  new JSourceParamsDialog
	    (getTopFrame(), !isLocked() && !pIsFrozen, title, snames, stitles, 
	     sfseqs, waction);
      }

      doSourceParamsChanged(true);
    }
  }

  /**
   * Update the appearance of the edit/view source params button after a change of value.
   */ 
  private void 
  doSourceParamsChanged
  (
   boolean modified
  ) 
  {
    if(modified)
      unsavedChange("Action Source Parameters");

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

    doOverflowPolicyChanged(true);
  }

  /**
   * Update the appearance of the overflow policy field after a change of value.
   */ 
  private void 
  doOverflowPolicyChanged
  (
   boolean modified
  ) 
  {
    if(modified) 
      unsavedChange("Overflow Policy");
    
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

    doExecutionMethodChanged(true);
  }

  /**
   * Update the appearance of the execution method field after a change of value.
   */ 
  private void 
  doExecutionMethodChanged
  (
   boolean modified
  ) 
  {
    if(modified)
      unsavedChange("Execution Method"); 

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
       (pWorkingExecutionMethodField.getSelectedIndex() < 2)) {
      pWorkingBatchSizeField.setValue(null);
      pWorkingBatchSizeField.setEnabled(false);
      pSetBatchSizeButton.setEnabled(false);
    }
    else {      
      if(pWorkingBatchSizeField.getValue() == null) 
	pWorkingBatchSizeField.setValue(0);
      pWorkingBatchSizeField.setEnabled(!isLocked() && !pIsFrozen);
      pSetBatchSizeButton.setEnabled
	(!isLocked() && !pIsFrozen && (cmethod != null) && (cmethod.equals("Parallel")));
    }

    doBatchSizeChanged(modified);
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

    doBatchSizeChanged(true);
  }

  /**
   * Update the appearance of the batch size field after a change of value.
   */ 
  private void 
  doBatchSizeChanged
  (
   boolean modified
  ) 
  {
    if(modified)
      unsavedChange("Batch Size"); 
    
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

    doPriorityChanged(true);
  }

  /**
   * Update the appearance of the priority field after a change of value.
   */ 
  private void 
  doPriorityChanged
  (
   boolean modified
  ) 
  {
    if(modified)
      unsavedChange("Priority"); 
    
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

    doRampUpChanged(true);
  }

  /**
   * Update the appearance of the ramp-up interval field after a change of value.
   */ 
  private void 
  doRampUpChanged
  (
   boolean modified
  ) 
  {
    if(modified)
      unsavedChange("Ramp Up Interval"); 
    
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

    doMaxLoadChanged(true);
  }

  /**
   * Update the appearance of the maximum load field after a change of value.
   */ 
  private void 
  doMaxLoadChanged
  (
   boolean modified
  ) 
  {
    if(modified)
      unsavedChange("Maximum Load"); 
    
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

    doMinMemoryChanged(true);
  }

  /**
   * Update the appearance of the minimum memory field after a change of value.
   */ 
  private void 
  doMinMemoryChanged
  (
   boolean modified
  ) 
  {
    if(modified)
      unsavedChange("Minimum Memory"); 
    
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

    doMinDiskChanged(true);
  }

  /**
   * Update the appearance of the minimum disk field after a change of value.
   */ 
  private void 
  doMinDiskChanged
  (
   boolean modified
  ) 
  {
    if(modified)
      unsavedChange("Minimum Disk"); 
    
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

      doSelectionKeyChanged(kname, true);
    }
  }

  /**
   * Update the appearance of the selection key field with the given name after a 
   * change of value.
   */ 
  private void 
  doSelectionKeyChanged
  (
   String kname,
   boolean modified
  ) 
  {
    Component pcomps[] = pSelectionKeyComponents.get(kname);
    if(pcomps != null) {
      if(modified)
        unsavedChange("Selection Key: " + kname); 
    
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
   * Set the hardware key field with the given name from the value of the checked-in field.
   */ 
  private void 
  doSetHardwareKey
  (
   String kname
  ) 
  { 
    Component pcomps[] = pHardwareKeyComponents.get(kname);
    if(pcomps != null) {
      JBooleanField wfield = (JBooleanField) pcomps[1];

      String ckey = ((JTextField) pcomps[3]).getText();
      if(ckey.equals("YES"))
	wfield.setValue(true);
      else if(ckey.equals("no"))
	wfield.setValue(false);

      doHardwareKeyChanged(kname, true);
    }
  }

  /**
   * Update the appearance of the hardware key field with the given name after a 
   * change of value.
   */ 
  private void 
  doHardwareKeyChanged
  (
   String kname,
   boolean modified
  ) 
  {
    Component pcomps[] = pHardwareKeyComponents.get(kname);
    if(pcomps != null) {
      if(modified)
        unsavedChange("Hardware Key: " + kname); 
    
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

      doLicenseKeyChanged(kname, true);
    }
  }

  /**
   * Update the appearance of the license key field with the given name after a 
   * change of value.
   */ 
  private void 
  doLicenseKeyChanged
  (
   String kname,
   boolean modified
  ) 
  {
    Component pcomps[] = pLicenseKeyComponents.get(kname);
    if(pcomps != null) {
      if(modified)
        unsavedChange("License Key: " + kname); 
    
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


  /*--------------------------------------------------------------------------------------*/
  
  /**
   * Add a new annotation to this node version.
   */ 
  private void 
  doAddAnnotation() 
  { 
    JNewIdentifierDialog diag = 
      new JNewIdentifierDialog(getTopFrame(), "New Annotation", "New Annotation Name:", 
                               null, "Add");
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      String aname = diag.getName();
      if((aname != null) && (aname.length() > 0)) {
        JAnnotationPanel panel = pAnnotationsPanels.get(aname);
        if(panel != null) {
          BaseAnnotation wannot = panel.getWorkingAnnotation();
          if(wannot != null) {
            UIMaster.getInstance().showErrorDialog
              ("Error:", 
               "The new annotation name (" + aname + ") is already being used by an " +
               "existing working version annotation!");
            return;
          }
        }

        extractAnnotationsFromPanels();
        BaseAnnotation annots[] = pAnnotations.get(aname);
        if(annots == null) {
          annots = new BaseAnnotation[2];
          pAnnotations.put(aname, annots);
        }          
        
        rebuildAnnotationPanels();

        unsavedChange("Annotation Added: " + aname);
      }
    }
  }

  /**
   * Set the working annotation field from the value of the checked-in field.
   */ 
  private void 
  doSetAnnotation
  (
   String aname
  ) 
  { 
    pAnnotationsPanels.get(aname).doSetAnnotation();
  }

  /**
   * Update the appearance of the annotation field after a change of value.
   */ 
  private void 
  doAnnotationChanged
  (
   String aname,   
   boolean modified
  )  
  {
    pAnnotationsPanels.get(aname).doAnnotationChanged(modified);
  }

  /**
   * Remove the given working annotation.
   */ 
  public void 
  doRemoveAnnotation
  (
   String aname
  ) 
  { 
    extractAnnotationsFromPanels();

    BaseAnnotation annots[] = pAnnotations.get(aname);
    if(annots != null) {
      if(annots[1] == null) 
        pAnnotations.remove(aname);
      else 
        annots[0] = null;

      unsavedChange("Annotation Removed: " + aname);
    }          

    rebuildAnnotationPanels();
  }
  
  /**
   * Rename the given annotation panel.
   */ 
  public void 
  doRenameAnnotation
  (
   String aname
  ) 
  {
    JAnnotationPanel panel = pAnnotationsPanels.get(aname);
    if(panel != null) {
      BaseAnnotation wannot = panel.getWorkingAnnotation();
      if(wannot != null) {
        JNewIdentifierDialog diag = 
          new JNewIdentifierDialog
            (getTopFrame(), "Rename Annotation", "New Annotation Name:", aname, "Rename");

        diag.setVisible(true);
        if(diag.wasConfirmed()) {
          String nname = diag.getName();
          if((nname != null) && (nname.length() > 0) && !nname.equals(aname)) {

            extractAnnotationsFromPanels();

            BaseAnnotation annots[] = pAnnotations.get(nname);
            if(annots == null) {
              annots = new BaseAnnotation[2];
              pAnnotations.put(nname, annots);
            }
            annots[0] = wannot;

            pAnnotations.remove(aname);
             
            rebuildAnnotationPanels();
            
            unsavedChange("Annotation Renamed from: " + aname + " to " + nname);
          }
        }
      }
    }
  }

  /**
   * Set the working annotation parameter field from the value of the checked-in 
   * annotation parameter.
   */ 
  public void 
  doSetAnnotationParam
  (
   String args
  ) 
  {
    String parts[] = args.split(":");
    if((parts.length == 2) && (parts[0].length() > 0) && (parts[1].length() > 0)) {
      String aname = parts[0];
      String pname = parts[1];

      JAnnotationPanel panel = pAnnotationsPanels.get(aname);
      if(panel != null) 
        panel.doSetAnnotationParam(pname); 
    }
  }

  /**
   * Notify the panel that an annotation parameter has changed value.
   */ 
  public void 
  doAnnotationParamChanged
  (
   String args
  ) 
  {
    String parts[] = args.split(":");
    if((parts.length == 2) && (parts[0].length() > 0) && (parts[1].length() > 0)) {
      String aname = parts[0];
      String pname = parts[1];

      JAnnotationPanel panel = pAnnotationsPanels.get(aname);
      if(panel != null) 
        panel.doAnnotationParamChanged(pname); 
    }
  }
  
  /**
   * Notify the panel that an annotation parameter has changed value.
   */ 
  public void 
  doAnnotationParamChanged
  (
   Document doc
  ) 
  {
    String aname = pDocToAnnotName.get(doc);
    JAnnotationPanel panel = pAnnotationsPanels.get(aname);
    if(panel != null) 
      panel.doAnnotationParamChanged(doc); 
  }


  /*----------------------------------------------------------------------------------------*/

  private void
  doShowEditorHelp()
  {
    if(!pEditorHelpButton.isEnabled())
      return;

    BaseEditor editor = null;
    {
      if(hasWorking()) {
	editor = getWorkingEditor();
      }
      else {
	NodeVersion vsn = getCheckedInVersion();

	if(vsn != null)
	  editor = vsn.getEditor();
      }
    }

    if(editor != null)
      showPluginHelp(editor);
  }

  private void
  doShowActionHelp()
  {
    if(!pActionHelpButton.isEnabled())
      return;

    BaseAction action = null;
    {
      if(hasWorking()) {
	action = getWorkingAction();
      }
      else {
	NodeVersion vsn = getCheckedInVersion();

	if(vsn != null)
	  action = vsn.getAction();
      }
    }

    if(action != null)
      showPluginHelp(action);
  }

  private void
  showPluginHelp
  (
   BasePlugin plugin
  )
  {
    String cname = plugin.getClass().getName();
    Path path = new Path(PackageInfo.sInstPath, 
                         "share/docs/javadoc/" + 
			 cname.replace(".", "/") + 
			 ".html");

    File file = path.toFile();

    try {
      if(!file.exists())
	throw new PipelineException
	  ("Path (" + path + ") does not exist!");

      {
	URI uri = file.toURI();
	Desktop.getDesktop().browse(uri);
	return;
      }
    }
    catch(Exception ex) {
      UIMaster.getInstance().showErrorDialog(ex);
    }
  }

  private boolean
  hasHelp
  (
   BasePlugin plugin
  )
  {
    if(plugin != null) {
      String cname = plugin.getClass().getName();
      Path path = new Path(PackageInfo.sInstPath, 
                           "share/docs/javadoc/" + cname.replace(".", "/") + ".html");
      return path.toFile().exists();
    }

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A component representing a pair of working and checked-in node annotation plugin 
   * instances which share a common name.
   */ 
  private 
  class JAnnotationPanel
    extends JPanel
  {
    /**
     * Construct a new annotation panel.
     */ 
    public 
    JAnnotationPanel
    (
     JNodeDetailsPanel parent, 
     String toolset, 
     String aname,
     BaseAnnotation annots[]
    ) 
    {
      super();
     
      /* initialize fields */ 
      {
	pAnnotName = aname; 
        pParent = parent; 

        pToolsetName = toolset;

        pWorkingAnnotation   = annots[0];
        pCheckedInAnnotation = annots[1];

        pParamComponents     = new TreeMap<String,Component[]>();
        pDocToAnnotParamName = new ListMap<Document, String>();
      }

      initUI();
    }



    /*--------------------------------------------------------------------------------------*/
    /*   A C C E S S O R S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    /**
     * The name of the annotation plugin instance.
     */ 
    @Override
    public String
    getName() 
    {
      return pAnnotName;
    }

    /**
     * Get the updated working plugin instance based on the values in the UI fields.
     */
    public BaseAnnotation
    getWorkingAnnotation() 
    {
      if(pWorkingAnnotation != null) {
        for(AnnotationParam aparam : pWorkingAnnotation.getParams()) {
          if(!pWorkingAnnotation.isParamConstant(aparam.getName())) {
            Component comps[] = pParamComponents.get(aparam.getName()); 
            if(comps != null) {
              Comparable value = null;
              if(aparam instanceof BooleanAnnotationParam) {
                JBooleanField field = (JBooleanField) comps[1];
                value = field.getValue();
              }
              else if(aparam instanceof DoubleAnnotationParam) {
                JDoubleField field = (JDoubleField) comps[1];
                value = field.getValue();
              }
              else if(aparam instanceof EnumAnnotationParam) {
                JCollectionField field = (JCollectionField) comps[1];
                EnumAnnotationParam eparam = (EnumAnnotationParam) aparam;
                value = eparam.getValueOfIndex(field.getSelectedIndex());
              }
              else if(aparam instanceof IntegerAnnotationParam) {
                JIntegerField field = (JIntegerField) comps[1];
                value = field.getValue();
              }
              else if(aparam instanceof TextAreaAnnotationParam) {
                JTextArea field = (JTextArea) comps[1];
                value = field.getText();	  
              }
              else if(aparam instanceof StringAnnotationParam) {
                JTextField field = (JTextField) comps[1];  
                value = field.getText();	  
              }
              else if(aparam instanceof ParamNameAnnotationParam) {
                JTextField field = (JTextField) comps[1];
                value = field.getText();          
              }
              else if(aparam instanceof PathAnnotationParam) {
                JPathField field = (JPathField) comps[1];
                value = field.getPath();	  
              }
              else if(aparam instanceof ToolsetAnnotationParam) {
                JCollectionField field = (JCollectionField) comps[1];
                String toolset = field.getSelected();
                if(toolset.equals("-") || (toolset.length() == 0))
                  value = null;
                else 
                  value = toolset;
              }
              else if(aparam instanceof WorkGroupAnnotationParam) {
                JCollectionField field = (JCollectionField) comps[1];
                String ugname = field.getSelected(); 
                if(ugname.equals("-") || (ugname.length() == 0))
                  value = null;
                else if(ugname.startsWith("[") && ugname.endsWith("]"))
                  value = ugname.substring(1, ugname.length()-1);
                else 
                  value = ugname;
              }
              else if(aparam instanceof BuilderIDAnnotationParam) {
                JBuilderIDSelectionField field = (JBuilderIDSelectionField) comps[1];
                value = field.getBuilderID();
              }
              else {
                assert(false) : "Unknown annotation parameter type!";
              }
            
              pWorkingAnnotation.setParamValue(aparam.getName(), value);
            }
          }
        }
      }
      
      return pWorkingAnnotation;
    }


   
    /*--------------------------------------------------------------------------------------*/
    /*   U S E R   I N T E R F A C E                                                        */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Initialize the common user interface components.
     */ 
    private void 
    initUI()
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      Box vbox = new Box(BoxLayout.Y_AXIS);
      {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];

        /* edit buttons */ 
        {
          tpanel.add(Box.createRigidArea(new Dimension(0, 19)));

          Box hbox = new Box(BoxLayout.X_AXIS);

          {
            JButton btn = new JButton("Rename...");
            pRenameButton = btn;
            btn.setName("ValuePanelButton");
            btn.setRolloverEnabled(false);
            btn.setFocusable(false);

            Dimension size = new Dimension(sVSize/2-2, 19);
            btn.setMinimumSize(size);
            btn.setPreferredSize(size);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));

            btn.setActionCommand("rename-annotation:" + pAnnotName);
            btn.addActionListener(pParent);

            hbox.add(btn);
          }

          hbox.add(Box.createRigidArea(new Dimension(4, 0)));

          {
            JButton btn = new JButton("Remove...");
            pRemoveButton = btn;
            btn.setName("ValuePanelButton");
            btn.setRolloverEnabled(false);
            btn.setFocusable(false);

            Dimension size = new Dimension(sVSize/2-2, 19);
            btn.setMinimumSize(size);
            btn.setPreferredSize(size);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));

            btn.setActionCommand("remove-annotation:" + pAnnotName);
            btn.addActionListener(pParent);

            hbox.add(btn);
          }

          vpanel.add(hbox);

          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
        }

        /* annotation */ 
        {
          {
            JLabel label = UIFactory.createFixedLabel
              ("Annotation:", sTSize-7, JLabel.RIGHT, 
               "The name of the Annotation plugin.");
            pAnnotationTitle = label;
            tpanel.add(label);
          }

          {
            Box hbox = new Box(BoxLayout.X_AXIS);

            {
              JPluginSelectionField field = 
                UIFactory.createPluginSelectionField
                  (new PluginMenuLayout(),
                   new TripleMap<String,String,VersionID,TreeSet<OsType>>(), sVSize);
              pWorkingAnnotationField = field;

              field.setActionCommand("annotation-changed:" + pAnnotName);
              field.addActionListener(pParent);

              hbox.add(field);
            }

            hbox.add(Box.createRigidArea(new Dimension(4, 0)));

            {
              JButton btn = new JButton();		 
              pSetAnnotationButton = btn;
              btn.setName("SmallLeftArrowButton");

              Dimension size = new Dimension(12, 12);
              btn.setMinimumSize(size);
              btn.setMaximumSize(size);
              btn.setPreferredSize(size);

              btn.setActionCommand("set-annotation:" + pAnnotName);
              btn.addActionListener(pParent);

              hbox.add(btn);
            } 

            hbox.add(Box.createRigidArea(new Dimension(4, 0)));

            {
              JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
              pCheckedInAnnotationField = field;

              hbox.add(field);
            }

            vpanel.add(hbox);
          }
        }

        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        /* annotation version */ 
        { 
          {
            JLabel label = UIFactory.createFixedLabel
              ("Version:", sTSize-7, JLabel.RIGHT, 
               "The revision number of the Annotation plugin.");
            pAnnotationVersionTitle = label;
            tpanel.add(label);
          }

          {
            Box hbox = new Box(BoxLayout.X_AXIS);

            {
              JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
              pWorkingAnnotationVersionField = field;

              hbox.add(field);
            }

            hbox.add(Box.createRigidArea(new Dimension(20, 0)));

            {
              JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
              pCheckedInAnnotationVersionField = field;

              hbox.add(field);
            }

            vpanel.add(hbox);
          }
        }

        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        /* annotation vendor */ 
        { 
          {
            JLabel label = UIFactory.createFixedLabel
              ("Vendor:", sTSize-7, JLabel.RIGHT, 
               "The name of the vendor of the Annotation plugin.");
            pAnnotationVendorTitle = label;
            tpanel.add(label);
          }

          {
            Box hbox = new Box(BoxLayout.X_AXIS);

            {
              JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
              pWorkingAnnotationVendorField = field;

              hbox.add(field);
            }

            hbox.add(Box.createRigidArea(new Dimension(20, 0)));

            {
              JTextField field = UIFactory.createTextField("-", sVSize, JLabel.CENTER);
              pCheckedInAnnotationVendorField = field;

              hbox.add(field);
            }

            vpanel.add(hbox);
          }
        }
        
        vbox.add(comps[2]);
      }

      {
        Box hbox = new Box(BoxLayout.X_AXIS);

        hbox.addComponentListener(pParent);
        hbox.add(UIFactory.createSidebar());
        
        {
          JDrawer drawer = 
            new JDrawer("Annotation Parameters:", null, true);
          drawer.setToolTipText(UIFactory.formatToolTip("Annotation plugin parameters."));
          pParamsDrawer = drawer;

          hbox.add(drawer);
        }  
        
        vbox.add(hbox);
      }

      {
        JDrawer drawer = new JDrawer("Annotation: " + pAnnotName, vbox, true);
        drawer.setToolTipText(UIFactory.formatToolTip("Node Annotation."));
        add(drawer);
      }

      updateAnnotation();
    }

    /**
     * Update the UI components.
     */
    private void 
    updateAnnotation() 
    {
      pWorkingAnnotationField.removeActionListener(pParent);
      {
        UIMaster master = UIMaster.getInstance();
        master.updateAnnotationPluginField(pGroupID, pToolsetName, pWorkingAnnotationField); 

        pWorkingAnnotationField.setPlugin(pWorkingAnnotation);

        updateAnnotationFields();
      }
      pWorkingAnnotationField.addActionListener(pParent);

      pSetAnnotationButton.setEnabled
        (!isLocked() && !pIsFrozen && 
         (pWorkingAnnotation != null) && (pCheckedInAnnotation != null));
	
      {
        BaseAnnotation annot = pCheckedInAnnotation;
        if(annot != null) {
          pCheckedInAnnotationField.setText(annot.getName());
          pCheckedInAnnotationVersionField.setText("v" + annot.getVersionID());
          pCheckedInAnnotationVendorField.setText(annot.getVendor());
        }
        else {
          pCheckedInAnnotationField.setText("-");
          pCheckedInAnnotationVersionField.setText("-");
          pCheckedInAnnotationVendorField.setText("-");	
        }
	
        pCheckedInAnnotationField.setEnabled(annot != null);
        pCheckedInAnnotationVersionField.setEnabled(annot != null);
        pCheckedInAnnotationVendorField.setEnabled(annot != null);
      }
      
      updateAnnotationParams();
      updateAnnotationColors();
    }

    /**
     * Update the annotation plugin version and vendor fields.
     */ 
    public void 
    updateAnnotationFields()
    {
      if(pWorkingAnnotationField.getPluginName() != null) {
        pWorkingAnnotationVersionField.setText
          ("v" + pWorkingAnnotationField.getPluginVersionID());
        pWorkingAnnotationVendorField.setText(pWorkingAnnotationField.getPluginVendor());
      }
      else {
        pWorkingAnnotationVersionField.setText("-");
        pWorkingAnnotationVendorField.setText("-");
      }
    }
    
    /**
     * Update the UI components associated annotation parameters.
     */ 
    private void 
    updateAnnotationParams() 
    {
      /* the annotations */ 
      BaseAnnotation wannot = pWorkingAnnotation;
      BaseAnnotation cannot = pCheckedInAnnotation;

      BaseAnnotation annot = null;
      if(wannot != null) 
        annot = wannot;
      else if(cannot != null) 
        annot = cannot;

      /* lookup common server info... */ 
      TreeSet<String> toolsets = null; 
      Set<String> workUsers  = null;
      Set<String> workGroups = null;
      if((wannot != null) || (cannot != null)) {
        boolean needsToolsets = false;
        boolean needsWorkGroups = false;

        if(wannot != null) {
          for(AnnotationParam aparam : wannot.getParams()) {
            if(aparam instanceof ToolsetAnnotationParam) 
              needsToolsets = true;
            else if(aparam instanceof WorkGroupAnnotationParam) 
              needsWorkGroups = true;
          }
        }

        if(cannot != null) {
          for(AnnotationParam aparam : cannot.getParams()) {
            if(aparam instanceof ToolsetAnnotationParam) 
              needsToolsets = true;
            else if(aparam instanceof WorkGroupAnnotationParam) 
              needsWorkGroups = true;
          }
        }
        
        UIMaster master = UIMaster.getInstance();
        UICache cache = master.getUICache(pGroupID);
        if(needsToolsets) {
          toolsets = new TreeSet<String>();
          toolsets.add("-");
          try {
            toolsets.addAll(cache.getCachedActiveToolsetNames());
          }
          catch(PipelineException ex) {
          }
        }

        if(needsWorkGroups) {
          try {
            WorkGroups wgroups = cache.getCachedWorkGroups();
            workGroups = wgroups.getGroups();
            workUsers  = wgroups.getUsers();
          }
          catch(PipelineException ex) {
            workGroups = new TreeSet<String>(); 
            workUsers  = new TreeSet<String>(); 
          }
        }
      }

      pRenameButton.setEnabled(wannot != null);
      pRemoveButton.setEnabled(wannot != null);

      pParamComponents.clear();
      pDocToAnnotParamName.clear();
      
      boolean first = true;
      if((annot != null) && annot.hasParams()) {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];

        for(String pname : annot.getLayout()) {
          if(pname == null) {
            UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
          }
          else {
            if(!first) 
              UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
            
            AnnotationParam param = annot.getParam(pname);
            if(param != null) {
              String cname = (pAnnotName + ":" + pname);
              Component pcomps[] = new Component[4];              
              pParamComponents.put(pname, pcomps);
              
              {
                JLabel label = UIFactory.createFixedLabel
                  (param.getNameUI() + ":", sTSize-14, JLabel.RIGHT, 
                   param.getDescription());
             
                pcomps[0] = label;
           
                tpanel.add(label);

                {
                  AnnotationParam aparam = null;
                  if(annot != null) 
                    aparam = annot.getParam(pname);
                  
                  if((aparam != null) && (aparam instanceof TextAreaAnnotationParam)) { 
                    TextAreaAnnotationParam bparam = (TextAreaAnnotationParam) aparam; 
                    int rows = bparam.getRows(); 
                    int height = 19*rows + 3*(rows-1);
                    tpanel.add(Box.createRigidArea(new Dimension(0, height-19)));
                  }
                }
              }
              
              { 
                Box hbox = new Box(BoxLayout.X_AXIS);

                {
                  AnnotationParam aparam = null;
                  if(wannot != null) 
                    aparam = wannot.getParam(pname);
                  
                  if(aparam != null) {  
                    boolean paramEnabled = 
                      (!isLocked() && !pIsFrozen && !wannot.isParamConstant(pname)); 
                    
                    if(aparam instanceof BooleanAnnotationParam) {
                      Boolean value = (Boolean) aparam.getValue();
                      JBooleanField field = 
                        UIFactory.createBooleanField(value, sVSize);
                      pcomps[1] = field;
                    
                      field.setActionCommand("annot-param-changed:" + cname);
                      field.addActionListener(pParent);
                      
                      field.setEnabled(paramEnabled);
                      
                      hbox.add(field);
                    }
                    else if(aparam instanceof DoubleAnnotationParam) {
                      Double value = (Double) aparam.getValue();

                      JDoubleField field = 
                        UIFactory.createDoubleField(value, sVSize, JLabel.CENTER);
                      pcomps[1] = field;
                    
                      field.setActionCommand("annot-param-changed:" + cname); 
                      field.addActionListener(pParent);
                      
                      field.setEnabled(paramEnabled);
                      
                      hbox.add(field);
                    }
                    else if(aparam instanceof EnumAnnotationParam) {
                      EnumAnnotationParam eparam = (EnumAnnotationParam) aparam;
                      
                      JCollectionField field = 
                        UIFactory.createCollectionField(eparam.getValues(), sVSize); 
                      pcomps[1] = field;

                      field.setSelected((String) eparam.getValue());
                      
                      field.setActionCommand("annot-param-changed:" + cname); 
                      field.addActionListener(pParent);
                      
                      field.setEnabled(paramEnabled);
                      
                      hbox.add(field);
                    }
                    else if(aparam instanceof IntegerAnnotationParam) {
                      Integer value = (Integer) aparam.getValue();
                      JIntegerField field = 
                        UIFactory.createIntegerField(value, sVSize, JLabel.CENTER);
                      pcomps[1] = field;
                    
                      field.setActionCommand("annot-param-changed:" + cname); 
                      field.addActionListener(pParent);
                      
                      field.setEnabled(paramEnabled);
                      
                      hbox.add(field);
                    }
                    else if(aparam instanceof TextAreaAnnotationParam) {
                      TextAreaAnnotationParam bparam = (TextAreaAnnotationParam) aparam; 
                      String value = (String) aparam.getValue(); 
                      int rows = bparam.getRows(); 
                      JTextArea area = UIFactory.createEditableTextArea(value, rows);
                      pcomps[1] = area;

                      int height = 19*rows + 3*(rows-1);
                      Dimension size = new Dimension(sVSize, height);
                      area.setMinimumSize(size);
                      area.setMaximumSize(new Dimension(Integer.MAX_VALUE, height)); 
                      area.setPreferredSize(size);

                      Document doc = area.getDocument();
                      doc.addDocumentListener(pParent);
                      pDocToAnnotParamName.put(doc, pname);
                      pDocToAnnotName.put(doc, pAnnotName);
                      
                      area.setEnabled(paramEnabled); 
                      	   
                      hbox.add(area);
                    }
                    else if(aparam instanceof StringAnnotationParam) {
                      String value = (String) aparam.getValue();
                      JTextField field = 
                        UIFactory.createEditableTextField(value, sVSize, JLabel.CENTER);
                      pcomps[1] = field;

                      field.setActionCommand("annot-param-changed:" + cname);
                      field.addActionListener(pParent);
                      
                      field.setEnabled(paramEnabled); 
                      	
                      hbox.add(field);         
                    }
                    else if(aparam instanceof ParamNameAnnotationParam) {
                      String value = (String) aparam.getValue();
                      JTextField field = 
                        UIFactory.createParamNameField(value, sVSize, JLabel.CENTER);
                      pcomps[1] = field;

                      field.setActionCommand("annot-param-changed:" + cname);
                      field.addActionListener(pParent);
                      
                      field.setEnabled(paramEnabled); 
                        
                      hbox.add(field);         
                    }
                    else if(aparam instanceof PathAnnotationParam) {
                      Path value = (Path) aparam.getValue();
                       JPathField field = 
                         UIFactory.createPathField(value, sVSize, JLabel.CENTER);
                       pcomps[1] = field;

                      field.setActionCommand("annot-param-changed:" + cname);
                      field.addActionListener(pParent);
                      
                      field.setEnabled(paramEnabled); 
                      	
                      hbox.add(field);         
                    }
                    else if(aparam instanceof ToolsetAnnotationParam) {
                      String value = (String) aparam.getValue();
                      
                      TreeSet<String> values = new TreeSet<String>(toolsets);
                      if((value != null) && !values.contains(value))
                        values.add(value); 

                      JCollectionField field = 
                        UIFactory.createCollectionField(values, sVSize); 
                      pcomps[1] = field;

                      if(value != null) 
                        field.setSelected(value);
                      else 
                        field.setSelected("-");
                      
                      field.setActionCommand("annot-param-changed:" + cname);
                      field.addActionListener(pParent);
                      
                      field.setEnabled(paramEnabled); 
                      	
                      hbox.add(field);         
                    }
                    else if(aparam instanceof WorkGroupAnnotationParam) {
                      WorkGroupAnnotationParam wparam = (WorkGroupAnnotationParam) aparam;
                      String value = (String) aparam.getValue();
                      
                      TreeSet<String> values = new TreeSet<String>();
                      values.add("-");
                      if(wparam.allowsGroups()) {
                        for(String gname : workGroups) 
                          values.add("[" + gname + "]"); 
                      }
                      if(wparam.allowsUsers()) 
                        values.addAll(workUsers);
                        
                      JCollectionField field = 
                        UIFactory.createCollectionField(values, sVSize); 
                      pcomps[1] = field;
                       
                      if(value == null) 
                        field.setSelected("-");
                      else {                  
                        if(wparam.allowsGroups() && workGroups.contains(value))
                          field.setSelected("[" + value + "]");
                        else if(wparam.allowsUsers() && workUsers.contains(value))
                          field.setSelected(value);
                        else 
                          field.setSelected("-");
                      }
                      
                      field.setActionCommand("annot-param-changed:" + cname);
                      field.addActionListener(pParent);
                      
                      field.setEnabled(paramEnabled); 
                      	
                      hbox.add(field);         
                    }
                    else if(aparam instanceof BuilderIDAnnotationParam) {
                      BuilderID value = (BuilderID) aparam.getValue();

                      JBuilderIDSelectionField field = 
                        UIMaster.getInstance().createBuilderIDSelectionField(pGroupID, sVSize);
                      pcomps[1] = field;
                      
                      field.setEnabled(paramEnabled); 
                      	
                      hbox.add(field);         
                    }
                    else {
                      assert(false) : 
                        ("Unknown annotation parameter type (" + pname + ")!");
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
                  
                  btn.setActionCommand("set-annot-param:" + cname);
                  btn.addActionListener(pParent);
                  
                  btn.setEnabled(!isLocked() && !pIsFrozen && 
                                 (wannot != null) && (cannot != null) && 
                                 cannot.getName().equals(wannot.getName()));
                  
                  hbox.add(btn);
                } 
                
                hbox.add(Box.createRigidArea(new Dimension(4, 0)));
                
                {
                  AnnotationParam aparam = null;
                  if((cannot != null) && 
                     ((wannot == null) || cannot.getName().equals(wannot.getName())))
                    aparam = cannot.getParam(pname);
                  
                  if(aparam != null) {
                    if(aparam instanceof TextAreaAnnotationParam) {
                      TextAreaAnnotationParam bparam = (TextAreaAnnotationParam) aparam; 
                      String value = (String) aparam.getValue(); 
                      int rows = bparam.getRows(); 
                      JTextArea area = UIFactory.createTextArea(value, rows);
                      pcomps[3] = area;

                      int height = 19*rows + 3*(rows-1);
                      Dimension size = new Dimension(sVSize, height);
                      area.setMinimumSize(size);
                      area.setMaximumSize(new Dimension(Integer.MAX_VALUE, height)); 
                      area.setPreferredSize(size);

                      area.setEnabled(false); 

                      hbox.add(area);
                    }
                    else if(aparam instanceof BuilderIDAnnotationParam) {
                      BuilderID value = (BuilderID) aparam.getValue();

                      JBuilderIDSelectionField field = 
                        UIMaster.getInstance().createBuilderIDSelectionField(pGroupID, sVSize);
                      pcomps[3] = field;
                      
                      field.setEnabled(false); 
                      	
                      hbox.add(field);         
                    }
                    else {
                      String text = "-";
                      {
                        if(aparam instanceof BooleanAnnotationParam) {
                          Boolean value = (Boolean) aparam.getValue();
                          if(value != null) 
                            text = (value ? "YES" : "no");
                          else 
                            text = "-";
                        }
                        else if(aparam instanceof WorkGroupAnnotationParam) {
                          WorkGroupAnnotationParam gparam = (WorkGroupAnnotationParam) aparam;
                          String value = (String) gparam.getValue();

                          text = "-";
                          if(value != null) {
                            if(gparam.allowsGroups() && workGroups.contains(value))
                              text = ("[" + value + "]");
                            else if(gparam.allowsUsers() && workUsers.contains(value))
                              text = value;
                          }                          
                        }
                        else {
                          Comparable value = aparam.getValue();
                          if(value != null)
                            text = value.toString();
                        }
                      }
                    
                      JTextField field = 
                        UIFactory.createTextField(text, sVSize, JLabel.CENTER);
                      pcomps[3] = field;
                      
                      hbox.add(field);
                    }
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

              /* optional builder ID slave fields */ 
              if((pcomps[1] instanceof JBuilderIDSelectionField) ||
                 (pcomps[3] instanceof JBuilderIDSelectionField)) {
                 
                JBuilderIDSelectionField wfield = null;
                if(pcomps[1] instanceof JBuilderIDSelectionField)
                  wfield = (JBuilderIDSelectionField) pcomps[1];
                
                JBuilderIDSelectionField cfield = null;
                if(pcomps[3] instanceof JBuilderIDSelectionField)
                  cfield = (JBuilderIDSelectionField) pcomps[3];
                
                UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

                /* builder version */ 
                {
                  {
                    JLabel label = 
                      UIFactory.createFixedLabel
                      ("Version:", sTSize-14, JLabel.RIGHT, 
                       "The revision number of the builder collection."); 
                    tpanel.add(label); 
                  }
                  
                  {
                    Box hbox = new Box(BoxLayout.X_AXIS);

                    if(wfield != null) 
                      hbox.add(wfield.createVersionField(sVSize));
                    else {
                      JLabel label = UIFactory.createLabel("-", sVSize, JLabel.CENTER);
                      label.setName("TextFieldLabel");
                      hbox.add(label); 
                    }
                      
                    hbox.add(Box.createRigidArea(new Dimension(20, 0)));

                    if(cfield != null) 
                      hbox.add(cfield.createVersionField(sVSize));
                    else {
                      JLabel label = UIFactory.createLabel("-", sVSize, JLabel.CENTER);
                      label.setName("TextFieldLabel");
                      hbox.add(label); 
                    }

                    vpanel.add(hbox); 
                  }
                }

                UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

                /* builder vendor */ 
                {
                  {
                    JLabel label = 
                      UIFactory.createFixedLabel
                      ("Vendor:", sTSize-14, JLabel.RIGHT, 
                       "The vendor of the builder collection."); 
                    tpanel.add(label); 
                  }
                    
                  {
                    Box hbox = new Box(BoxLayout.X_AXIS);

                    if(wfield != null) 
                      hbox.add(wfield.createVendorField(sVSize));
                    else {
                      JLabel label = UIFactory.createLabel("-", sVSize, JLabel.CENTER);
                      label.setName("TextFieldLabel");
                      hbox.add(label); 
                    }

                    hbox.add(Box.createRigidArea(new Dimension(20, 0)));
                    
                    if(cfield != null) 
                      hbox.add(cfield.createVendorField(sVSize));
                    else {
                      JLabel label = UIFactory.createLabel("-", sVSize, JLabel.CENTER);
                      label.setName("TextFieldLabel");
                      hbox.add(label); 
                    }
                    
                    vpanel.add(hbox); 
                  }
                }

                UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

                /* builder OS support */ 
                {
                  {
                    JLabel label = 
                      UIFactory.createFixedLabel
                      ("OS Support:", sVSize, JLabel.RIGHT, 
                       "The operating systems supported by the builer collection."); 
                    tpanel.add(label); 
                  }
                  
                  {
                    Box hbox = new Box(BoxLayout.X_AXIS);

                    if(wfield != null) 
                      hbox.add(wfield.createOsSupportField(sVSize));
                    else {
                      JLabel label = UIFactory.createLabel("-", sVSize, JLabel.CENTER);
                      label.setName("TextFieldLabel");
                      hbox.add(label); 
                    }

                    hbox.add(Box.createRigidArea(new Dimension(20, 0)));
                    
                    if(cfield != null) 
                      hbox.add(cfield.createOsSupportField(sVSize));
                    else {
                      JLabel label = UIFactory.createLabel("-", sVSize, JLabel.CENTER);
                      label.setName("TextFieldLabel");
                      hbox.add(label); 
                    }

                    vpanel.add(hbox); 
                  }
                }
                
                UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
                
                /* builder OS support */ 
                {
                  {
                    JLabel label = 
                      UIFactory.createFixedLabel
                      ("Builder Name:", sVSize, JLabel.RIGHT, 
                       "The name of the selected builder within the builder collection."); 
                    tpanel.add(label); 
                  }
                  
                  {
                    Box hbox = new Box(BoxLayout.X_AXIS);

                    if(wfield != null) 
                      hbox.add(wfield.createBuilderNameField(sVSize));
                    else {
                      JLabel label = UIFactory.createLabel("-", sVSize, JLabel.CENTER);
                      label.setName("TextFieldLabel");
                      hbox.add(label); 
                    }

                    hbox.add(Box.createRigidArea(new Dimension(20, 0)));

                    if(cfield != null) 
                      hbox.add(cfield.createBuilderNameField(sVSize));
                    else {
                      JLabel label = UIFactory.createLabel("-", sVSize, JLabel.CENTER);
                      label.setName("TextFieldLabel");
                      hbox.add(label); 
                    }

                    vpanel.add(hbox); 
                  }
                }              

                /* set the value for all fields */ 
                if(wfield != null) {
                  AnnotationParam wparam = null;
                  if(wannot != null) 
                    wparam = wannot.getParam(pname);
                  
                  if(wparam != null) 
                    wfield.setBuilderID((BuilderID) wparam.getValue()); 

                  wfield.setActionCommand("annot-param-changed:" + cname);
                  wfield.addActionListener(pParent);
                }

                if(cfield != null) {
                  AnnotationParam cparam = null;
                  if((cannot != null) && 
                     ((wannot == null) || cannot.getName().equals(wannot.getName())))
                    cparam = cannot.getParam(pname);
                  
                  if(cparam != null) 
                    cfield.setBuilderID((BuilderID) cparam.getValue()); 
                }
              }
            }
          }
            
          first = false;
        }
        
        UIFactory.addVerticalGlue(tpanel, vpanel);
        
        pParamsDrawer.setContents((JComponent) comps[2]);
      }
      else {
        pParamsDrawer.setContents(null);
      }
    }

    /**
     * Update the appearance of the annotation fields after a change of value.
     */ 
    private void 
    updateAnnotationColors()
    {
      BaseAnnotation wannot = pWorkingAnnotation;
      BaseAnnotation cannot = pCheckedInAnnotation;
      
      BaseAnnotation annot = null;
      if(wannot != null) 
        annot = wannot;
      else if (cannot != null) 
        annot = cannot;

      Color color = Color.white;
      if((wannot != null) && (cannot != null)) {  
        if(!(((wannot == null) && (cannot == null)) ||
             ((wannot != null) && (cannot != null) && 
              wannot.getName().equals(cannot.getName()) && 
              wannot.getVersionID().equals(cannot.getVersionID()) && 
              wannot.getVendor().equals(cannot.getVendor()))))
          color = Color.cyan;
        else 
          color = null;
      }
      
      pAnnotationTitle.setForeground(color);
      pWorkingAnnotationField.setForeground(color);
      pCheckedInAnnotationField.setForeground(color);
      
      pAnnotationVersionTitle.setForeground(color);
      pWorkingAnnotationVersionField.setForeground(color);
      pCheckedInAnnotationVersionField.setForeground(color);
      
      pAnnotationVendorTitle.setForeground(color);
      pWorkingAnnotationVendorField.setForeground(color);
      pCheckedInAnnotationVendorField.setForeground(color);

      if(annot != null) {
        for(AnnotationParam aparam : annot.getParams()) 
          updateAnnotParamColor(aparam.getName(), color);
      }
    }
  
    /**
     * Update the color of the UI components associated with an action parameter.
     */ 
    private void 
    updateAnnotParamColor
    (
     String pname,
     Color color
    ) 
    {
      Component pcomps[] = pParamComponents.get(pname);
      if(pcomps == null)
        return;

      Color fg = color;
      if(fg == null) {
        String wtext = null;
        {
          AnnotationParam aparam = null;
          if(pWorkingAnnotation != null) 
            aparam = pWorkingAnnotation.getParam(pname);
          
          if(aparam != null) {
            if(aparam instanceof BooleanAnnotationParam) {
              JBooleanField field = (JBooleanField) pcomps[1];
              if(field.getValue() != null) 
                wtext = field.getValue().toString();
            }
            else if(aparam instanceof DoubleAnnotationParam) {
              JDoubleField field = (JDoubleField) pcomps[1];
              if(field.getValue() != null) 
                wtext = field.getValue().toString();
            }
            else if(aparam instanceof EnumAnnotationParam) {
              JCollectionField field = (JCollectionField) pcomps[1];
              wtext = field.getSelected();
            }
            else if(aparam instanceof IntegerAnnotationParam) {  
              JIntegerField field = (JIntegerField) pcomps[1];
              if(field.getValue() != null) 
                wtext = field.getValue().toString();
            }
            else if(aparam instanceof TextAreaAnnotationParam) {
              JTextArea area = (JTextArea) pcomps[1];
              wtext = area.getText();
            }
            else if(aparam instanceof StringAnnotationParam) {
              JTextField field = (JTextField) pcomps[1];
              wtext = field.getText();
            }
            else if(aparam instanceof ParamNameAnnotationParam) {
              JTextField field = (JTextField) pcomps[1];
              wtext = field.getText();
            }
            else if(aparam instanceof PathAnnotationParam) {
              JPathField field = (JPathField) pcomps[1];
              Path path = field.getPath();
              if(path != null) 
                wtext = path.toString();
            }
            else if(aparam instanceof ToolsetAnnotationParam) {
              JCollectionField field = (JCollectionField) pcomps[1];
              wtext = field.getSelected();
            }
            else if(aparam instanceof WorkGroupAnnotationParam) {
              JCollectionField field = (JCollectionField) pcomps[1];
              String ugname = field.getSelected(); 
              if(ugname != null) {
                if(ugname.equals("-") || (ugname.length() == 0))
                  wtext = null;
                else if(ugname.startsWith("[") && ugname.endsWith("]"))
                  wtext = ugname.substring(1, ugname.length()-1);
                else 
                  wtext = ugname;
              }
            }
            else if(aparam instanceof BuilderIDAnnotationParam) {
              JBuilderIDSelectionField field = (JBuilderIDSelectionField) pcomps[1];
              BuilderID builderID = field.getBuilderID();
              if(builderID != null) 
                wtext = builderID.toString();
            }
          }
        }

        String ctext = null;
        {
          AnnotationParam aparam = null;
          if((pCheckedInAnnotation != null) && 
             ((pWorkingAnnotation == null) || 
              pCheckedInAnnotation.getName().equals(pWorkingAnnotation.getName())))
            aparam = pCheckedInAnnotation.getParam(pname);

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
    
    

    /*--------------------------------------------------------------------------------------*/
    /*   A C T I O N S                                                                      */
    /*--------------------------------------------------------------------------------------*/

    /**
     * Set the working annotation field from the value of the checked-in field.
     */ 
    public void 
    doSetAnnotation() 
    { 
      BaseAnnotation cannot = getCheckedInVersion().getAnnotation(pAnnotName);

      try {
        PluginMgrClient pclient = PluginMgrClient.getInstance();
        pWorkingAnnotation = pclient.newAnnotation(cannot.getName(), 
                                                   cannot.getVersionID(), 
                                                   cannot.getVendor()); 
        pWorkingAnnotation.setParamValues(cannot);
        unsavedChange("Annotation Plugin: " + pAnnotName); 
      }
      catch(PipelineException ex) {
        UIMaster.getInstance().showErrorDialog(ex);
        pWorkingAnnotation = null;	    
      }

      updateAnnotation(); 
    }

    /**
     * Update the appearance of the annotation field after a change of value.
     */ 
    public void 
    doAnnotationChanged
    (
     boolean modified
    )  
    {
      if(modified) 
        unsavedChange("Annotation Plugin: " + pAnnotName); 

      BaseAnnotation oannot = getWorkingAnnotation(); 

      String aname = pWorkingAnnotationField.getPluginName();
      if(aname == null) {
        pWorkingAnnotation = null;
      }
      else {
        VersionID avid = pWorkingAnnotationField.getPluginVersionID();
        String avendor = pWorkingAnnotationField.getPluginVendor();
        
        if((oannot == null) || 
           !oannot.getName().equals(aname) ||
           !oannot.getVersionID().equals(avid) ||
           !oannot.getVendor().equals(avendor)) {
          try {
            PluginMgrClient pclient = PluginMgrClient.getInstance();
            pWorkingAnnotation = pclient.newAnnotation(aname, avid, avendor);
            if(oannot != null)
              pWorkingAnnotation.setParamValues(oannot);
            unsavedChange("Annotation Plugin: " + aname);
          }
          catch(PipelineException ex) {
            UIMaster.getInstance().showErrorDialog(ex);
            pWorkingAnnotation = null;	    
          }
        }
      }
      
      updateAnnotationFields();
      updateAnnotationParams();

      updateAnnotationColors();
    }

    /**
     * Remove the given working annotation.
     */ 
    public void 
    doRemoveAnnotation() 
    {
      pWorkingAnnotation = null;
      updateAnnotation();
    }

    
    /*--------------------------------------------------------------------------------------*/

    /**
     * Set the working annotation parameter field from the value of the checked-in 
     * annotation parameter.
     */ 
    public void 
    doSetAnnotationParam
    (
     String pname
    ) 
    {
      BaseAnnotation wannot = pWorkingAnnotation;
      BaseAnnotation cannot = pCheckedInAnnotation;

      AnnotationParam wparam = null;
      if(wannot != null) 
        wparam = wannot.getParam(pname);
      
      AnnotationParam cparam = null;
      if(cannot != null) 
        cparam = cannot.getParam(pname);
      
      if((wparam != null) && (cparam != null) && wannot.getName().equals(cannot.getName())) {
        Component pcomps[] = pParamComponents.get(pname);
        if(pcomps != null) {
          Comparable value = cparam.getValue();
          if(wparam instanceof BooleanAnnotationParam) {
            JBooleanField field = (JBooleanField) pcomps[1];
            field.setValue((Boolean) value);
          }
          else if(wparam instanceof DoubleAnnotationParam) {
            JDoubleField field = (JDoubleField) pcomps[1];
            field.setValue((Double) value);
          }
          else if(wparam instanceof EnumAnnotationParam) {
            JCollectionField field = (JCollectionField) pcomps[1];
            field.setSelected(value.toString()); 
          }
          else if(wparam instanceof IntegerAnnotationParam) {
            JIntegerField field = (JIntegerField) pcomps[1];
            field.setValue((Integer) value);
          }
          else if(wparam instanceof TextAreaAnnotationParam) {
            JTextArea field = (JTextArea) pcomps[1]; 
            if(value != null) 
              field.setText(value.toString());
            else 
              field.setText(null);
          }
          else if(wparam instanceof StringAnnotationParam) {
            JTextField field = (JTextField) pcomps[1];
            if(value != null) 
              field.setText(value.toString());
            else 
              field.setText(null);
          }
          else if(wparam instanceof ParamNameAnnotationParam) {
            JParamNameField field = (JParamNameField) pcomps[1];
            if(value != null) 
              field.setText(value.toString());
            else 
              field.setText(null);
          }
          else if(wparam instanceof PathAnnotationParam) {
            JPathField field = (JPathField) pcomps[1];
            field.setPath((Path) value);	  
          }
          else if(wparam instanceof ToolsetAnnotationParam) {
            JCollectionField field = (JCollectionField) pcomps[1];
            if(value != null) 
              field.setSelected((String) value);
            else 
              field.setSelected("-");
          }
          else if(wparam instanceof WorkGroupAnnotationParam) {
            JCollectionField field = (JCollectionField) pcomps[1];
            if(value == null) 
              field.setSelected("-");
            else {        
              Set<String> workUsers  = null;
              Set<String> workGroups = null;
              {
                UIMaster master = UIMaster.getInstance();
                UICache cache = master.getUICache(pGroupID);
                try {
                  WorkGroups wgroups = cache.getCachedWorkGroups();
                  workGroups = wgroups.getGroups();
                  workUsers  = wgroups.getUsers();
                }
                catch(PipelineException ex) {
                  workGroups = new TreeSet<String>(); 
                  workUsers  = new TreeSet<String>(); 
                }
              }
          
              WorkGroupAnnotationParam wgparam = (WorkGroupAnnotationParam) wparam;
              if(wgparam.allowsGroups() && workGroups.contains(value))
                field.setSelected("[" + value + "]");
              else if(wgparam.allowsUsers() && workUsers.contains(value))
                field.setSelected((String) value);
              else 
                field.setSelected("-");
            }
          }
          else if(wparam instanceof BuilderIDAnnotationParam) {
            JBuilderIDSelectionField field = (JBuilderIDSelectionField) pcomps[1];
            field.setBuilderID((BuilderID) value);
          }
          else {
            assert(false) : "Unknown annotation parameter type!";
          }

          doAnnotationParamChanged(pname);
        }
      }
    }

    
    /*--------------------------------------------------------------------------------------*/

  
    /**
     * Notify the panel that an annotation parameter has changed value.
     */ 
    public void 
    doAnnotationParamChanged
    (
     String pname
    ) 
    {
      unsavedChange("Annotation Parameter: " + pAnnotName + " (" + pname + ")"); 
      updateAnnotParamColor(pname, null);
    }
    
    /**
     * Notify the panel that an annotation parameter has changed value.
     */ 
    public void 
    doAnnotationParamChanged
    (
     Document doc
    ) 
    {
      String pname = pDocToAnnotParamName.get(doc);
      unsavedChange("Annotation Parameter: " + pAnnotName + " (" + pname + ")"); 
      updateAnnotParamColor(pname, null);
    }

    


    /*--------------------------------------------------------------------------------------*/
    /*   I N T E R N A L S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    private static final long serialVersionUID = 94363755980501272L;

    private JNodeDetailsPanel  pParent; 

    private String pToolsetName; 

    private String          pAnnotName; 
    private BaseAnnotation  pWorkingAnnotation; 
    private BaseAnnotation  pCheckedInAnnotation; 

    private JButton pRenameButton;
    private JButton pRemoveButton;
     
    private JLabel                 pAnnotationTitle;     
    private JPluginSelectionField  pWorkingAnnotationField;                         
    private JButton                pSetAnnotationButton; 
    private JTextField             pCheckedInAnnotationField;

    private JLabel      pAnnotationVersionTitle;
    private JTextField  pWorkingAnnotationVersionField;
    private JTextField  pCheckedInAnnotationVersionField;

    private JLabel      pAnnotationVendorTitle;
    private JTextField  pWorkingAnnotationVendorField;
    private JTextField  pCheckedInAnnotationVendorField;

    private JDrawer                      pParamsDrawer; 
    private TreeMap<String,Component[]>  pParamComponents; 
    private ListMap<Document, String>    pDocToAnnotParamName;
  }
  

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
	      else if(aparam instanceof ByteSizeActionParam) {
		JByteSizeField field = (JByteSizeField) comps[1];
		field.setValue((Long) value);
		doActionParamChanged(pname);
	      }
	      else if(aparam instanceof DoubleActionParam) {
		JDoubleField field = (JDoubleField) comps[1];
		field.setValue((Double) value);
		doActionParamChanged(pname);
	      }
	      else if(aparam instanceof Color3dActionParam) {
		JColorField field = (JColorField) comps[1];
		field.setValue((Color3d) value);
		doActionParamChanged(pname);
	      }
	      else if(aparam instanceof Tuple2iActionParam) {
		JTuple2iField field = (JTuple2iField) comps[1];
		field.setValue((Tuple2i) value);
		doActionParamChanged(pname);
	      }
	      else if(aparam instanceof Tuple3iActionParam) {
		JTuple3iField field = (JTuple3iField) comps[1];
		field.setValue((Tuple3i) value);
		doActionParamChanged(pname);
	      }
	      else if(aparam instanceof Tuple2dActionParam) {
		JTuple2dField field = (JTuple2dField) comps[1];
		field.setValue((Tuple2d) value);
		doActionParamChanged(pname);
	      }
	      else if(aparam instanceof Tuple3dActionParam) {
		JTuple3dField field = (JTuple3dField) comps[1];
		field.setValue((Tuple3d) value);
		doActionParamChanged(pname);
	      }
	      else if(aparam instanceof Tuple4dActionParam) {
		JTuple4dField field = (JTuple4dField) comps[1];
		field.setValue((Tuple4d) value);
		doActionParamChanged(pname);
	      }
              else if(aparam instanceof TextAreaActionParam) {
                JTextArea area = (JTextArea) comps[1];
                area.setText((String) value);
		doActionParamChanged(area.getDocument());
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

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Modifying Node...")) {
        MasterMgrClient client = master.acquireMasterMgrClient();
	try {
	  client.modifyProperties(pAuthor, pView, pNodeMod);
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseMasterMgrClient(client);
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private NodeMod  pNodeMod;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2714804145579513176L;

  private static final int  sTSize = 180;
  private static final int  sVSize = 160;
  private static final int  sSSize = 343;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Cached checked-in versions associated with the current node.
   */ 
  private TreeMap<VersionID,NodeVersion>  pCheckedInVersions; 

  /**
   * Cached source versions of the working version.
   */
  private TreeMap<String,NodeCommon>  pWorkingSources;

  /**
   * Cached sources for each checked-in version selected.
   */
  private DoubleMap<String,VersionID,NodeCommon>  pCheckedInSources;

  /**
   * The current license keys.
   */
  private ArrayList<LicenseKey>  pLicenseKeys; 

  /**
   * The current selection keys.
   */
  private ArrayList<SelectionKey>  pSelectionKeys; 
  
  /**
   * The current hardware keys.
   */
  private ArrayList<HardwareKey>  pHardwareKeys; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Plugin selection popup menus.
   */ 
  private JPopupMenu   pSelectEditorPopup; 
  private JPopupMenu   pSelectActionPopup; 


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
   * The intermediate title label.
   */ 
  private JLabel  pIntermediateTitle;

  /**
   * The working intermediate field.
   */ 
  private JBooleanField pWorkingIntermediateField;

  /**
   * The set intermediate button.
   */ 
  private JButton  pSetIntermediateButton;

  /**
   * The checked-in intermediate field.
   */ 
  private JTextField pCheckedInIntermediateField;


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
   * The editor help button.
   */
  private JButton  pEditorHelpButton;

  /**
   * The editor title label.
   */ 
  private JLabel  pEditorTitle;

  /**
   * The working editor name field.
   */ 
  private JPluginSelectionField pWorkingEditorField;

  /**
   * The temporary working editor.
   */ 
  private BaseEditor  pWorkingEditor;

  /**
   * The set editor button.
   */ 
  private JButton  pSetEditorButton;

  /**
   * The checked-in editor field.
   */ 
  private JTextField pCheckedInEditorField;


  /**
   * The editor version title label.
   */ 
  private JLabel  pEditorVersionTitle;

  /**
   * The working editor revision number field.
   */ 
  private JTextField pWorkingEditorVersionField;

  /**
   * The checked-in editor version field.
   */ 
  private JTextField pCheckedInEditorVersionField;


  /**
   * The editor vendor title label.
   */ 
  private JLabel  pEditorVendorTitle;

  /**
   * The working editor vendor field.
   */ 
  private JTextField pWorkingEditorVendorField;

  /**
   * The checked-in editor vendor field.
   */ 
  private JTextField pCheckedInEditorVendorField;

  
  /**
   * The editor operating system support title label.
   */ 
  private JLabel  pEditorOsSupportTitle;
  
  /**
   * The working editor operating system support field.
   */ 
  private JOsSupportField pWorkingEditorOsSupportField;

  /**
   * The checked-in editor operating system support field.
   */ 
  private JOsSupportField pCheckedInEditorOsSupportField;


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
   * The action help button.
   */
  private JButton  pActionHelpButton;

  /**
   * The action title label.
   */ 
  private JLabel  pActionTitle;

  /**
   * The working action field.
   */ 
  private JPluginSelectionField pWorkingActionField;

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
  private JTextField pWorkingActionVersionField;

  /**
   * The checked-in action version field.
   */ 
  private JTextField pCheckedInActionVersionField;


  /**
   * The action vendor title label.
   */ 
  private JLabel  pActionVendorTitle;

  /**
   * The working action vendor field.
   */ 
  private JTextField pWorkingActionVendorField;

  /**
   * The checked-in action vendor field.
   */ 
  private JTextField pCheckedInActionVendorField;


  /**
   * The action operating system support title label.
   */ 
  private JLabel  pActionOsSupportTitle;
  
  /**
   * The working action operating system support field.
   */ 
  private JOsSupportField pWorkingActionOsSupportField;

  /**
   * The checked-in action operating system support field.
   */ 
  private JOsSupportField pCheckedInActionOsSupportField;



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
   * The action parameter names indexed by the TextArea documents editing the parameter.
   */ 
  private ListMap<Document,String> pDocToParamName;

  /**
   * The JCollectionField values and corresponding fully resolved names of the 
   * upstream nodes used by LinkActionParam fields.
   */ 
  private ArrayList<String>  pWorkingLinkActionParamValues;
  private ArrayList<String>  pWorkingLinkActionParamNodeNames;
  private ArrayList<String>  pCheckedInLinkActionParamValues;
  private ArrayList<String>  pCheckedInLinkActionParamNodeNames;

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
   * The drawer containing the license key components.
   */ 
  private JDrawer  pLicenseDrawer;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The hardware keys container.
   */ 
  private Box  pHardwareKeysBox;

  /**
   * The title, working and checked-in hardware key components indexed by 
   * hardware key name.
   */ 
  private TreeMap<String,Component[]>  pHardwareKeyComponents;
  
  /**
   * The drawer containing the hardware key components.
   */ 
  private JDrawer  pHardwareDrawer;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The annotations container. 
   */ 
  private Box  pAnnotationsBox;

  /**
   * Temporary table of pairs of working and checked-in annotations indexed by 
   * their common annotations name. 
   */ 
  private TreeMap<String,BaseAnnotation[]>  pAnnotations;

  /**
   * The panels containing information about the pair of working and checked-in 
   * annotations indexed by thier common annotations name. 
   */ 
  private TreeMap<String,JAnnotationPanel>  pAnnotationsPanels;

  /**
   * The drawer containing all annotations components. 
   */ 
  private JDrawer  pAnnotationsDrawer;

  /**
   * The annotation names indexed by the TextArea annotation parameter documents.
   */ 
  private ListMap<Document,String> pDocToAnnotName;


}
