// $Id: JUnpackBundleDialog.java,v 1.1 2007/10/23 02:29:59 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   U N P A C K   B U N D L E   D I A L O G                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Unpack the contents of a node bundle (JAR archive) into the current working area.
 */ 
public 
class JUnpackBundleDialog
  extends JFullDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param channel
   *   The index of the update channel.
   * 
   * @param owner
   *   The parent frame.
   */ 
  public 
  JUnpackBundleDialog
  (
   int channel, 
   Frame owner,
   String author, 
   String view
  ) 
  {
    super(owner, "Unpack Bundle");

    /* init fields */ 
    {
      pChannel = channel;

      pAuthor = author; 
      pView   = view;
      
      pToolsetFields      = new TreeMap<String,JCollectionField>();
      pSelectionKeyFields = new TreeMap<String,JCollectionField>();
      pLicenseKeyFields   = new TreeMap<String,JCollectionField>();
    }

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();

      body.setName("MainDialogPanel");
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

      {
	Box vbox = new Box(BoxLayout.Y_AXIS);
	
	vbox.add(UIFactory.createPanelLabel("Bundle Details:"));
	
	vbox.add(Box.createRigidArea(new Dimension(0, 4)));
	
	{
	  JPanel tvp = new JPanel();
	  tvp.setName("TitleValuePanel");
	  tvp.setLayout(new BoxLayout(tvp, BoxLayout.Y_AXIS));

          {
            Component comps[] = UIFactory.createTitledPanels();
            JPanel tpanel = (JPanel) comps[0];
            JPanel vpanel = (JPanel) comps[1];

            {
              JPathField field = 
                UIFactory.createTitledPathField
                  (tpanel, "Node Bundle File:", sTSize, 
                   vpanel, (Path) null, sVSize, 
                   "The path to the node bundle file to unpack.");
              pBundleField = field;

              field.setActionCommand("bundle-changed");
              field.addActionListener(this);
            }

            UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

            pCreatedOnField = 
              UIFactory.createTitledTextField
                (tpanel, "Created On:", sTSize, 
                 vpanel, null, sVSize, 
                 "When the node bundle was created.");

            UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

            pCreatedByField =
              UIFactory.createTitledTextField
                (tpanel, "Created By:", sTSize, 
                 vpanel, null, sVSize, 
                 "Who created the node bundle and where it was made.");

            UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

            pPipelineVersionField =
              UIFactory.createTitledTextField
                (tpanel, "Pipeline Version:", sTSize, 
                 vpanel, null, sVSize, 
                 "The version of Pipeline running at the site where the node bundle " +
                 "was created.");

            tvp.add(comps[2]);
          }

          vbox.add(tvp);
        }

        body.add(vbox);
      }

      body.add(Box.createRigidArea(new Dimension(0, 20)));

      {
	Box hbox = new Box(BoxLayout.X_AXIS);
        
        {
          Box vbox = new Box(BoxLayout.Y_AXIS);
	
          vbox.add(UIFactory.createPanelLabel("Bundled Nodes:")); 
          
          vbox.add(Box.createRigidArea(new Dimension(0, 4)));
          
          {
            JPanel panel = new JPanel();
            panel.setName("ValuePanel");
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            
            pNodeBox = new Box(BoxLayout.Y_AXIS);
            panel.add(pNodeBox);

            panel.add(Box.createVerticalGlue());

            JScrollPane scroll = 
              UIFactory.createVertScrollPane(panel, sNSize+52, 250);
            vbox.add(scroll);
          }
          
          hbox.add(vbox);
        }

        hbox.add(Box.createRigidArea(new Dimension(20, 0)));
        
        {
          Box vbox = new Box(BoxLayout.Y_AXIS);
          {
            Dimension size = new Dimension(sTSize+sVSize+52, 250);
            vbox.setMinimumSize(size);
            vbox.setMaximumSize(new Dimension(sTSize+sVSize+52, Integer.MAX_VALUE));
            vbox.setPreferredSize(size);
          }

          vbox.add(UIFactory.createPanelLabel("Unpack Options:")); 
          
          vbox.add(Box.createRigidArea(new Dimension(0, 4)));
          
          {
            Box sbox = new Box(BoxLayout.Y_AXIS);

            {
              Component comps[] = UIFactory.createTitledPanels();
              JPanel tpanel = (JPanel) comps[0];
              JPanel vpanel = (JPanel) comps[1];
              
              pReleaseOnErrorField = 
                UIFactory.createTitledBooleanField
                  (tpanel, "Release On Error:", sTSize, 
                   vpanel, sVSize, 
                   "Whether to release all newly registered and/or modified nodes from " + 
                   "the working area if an error occurs in unpacking the node bundle.");
              pReleaseOnErrorField.setValue(true);
              
              UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
              
              pActionOnExistField = 
                UIFactory.createTitledCollectionField
                  (tpanel, "Action On Existence:", sTSize, 
                   vpanel, ActionOnExistence.titles(), 
                   this, sVSize, 
                   "What steps to take when encountering previously existing local " + 
                   "versions of nodes being unpacked.");
              
              sbox.add(comps[2]);
            }
            
            /* toolset remap */ 
            {
              pToolsetBox = new Box(BoxLayout.Y_AXIS);
              
              JDrawer drawer = new JDrawer("Convert Toolsets:", pToolsetBox, false);
              sbox.add(drawer);
            }
            
            /* selection key remap */ 
            {
              pSelectionKeyBox = new Box(BoxLayout.Y_AXIS);
              
              JDrawer drawer = 
                new JDrawer("Convert Selection Keys:", pSelectionKeyBox, false);
              sbox.add(drawer);
            }
            
            /* license key remap */ 
            {
              pLicenseKeyBox = new Box(BoxLayout.Y_AXIS);
              
              JDrawer drawer = new JDrawer("Convert License Keys:", pLicenseKeyBox, false);
              sbox.add(drawer);
            }
            
            sbox.add(UIFactory.createFiller(sTSize+sVSize+30));
            sbox.add(Box.createVerticalGlue());

            JScrollPane scroll = 
              UIFactory.createVertScrollPane(sbox, sTSize+sVSize+52, 250);
            vbox.add(scroll);
          }
          
          hbox.add(vbox);
        }

        body.add(hbox);
      }

      String extra[][] = {
        null,
	{ "Browse",  "browse" }
      };

      super.initUI("X", body, "Unpack", null, extra, "Cancel");

      pack();
    }  

    {
      pFileSelectDialog = new JFileSelectDialog(this, "Node Bundle", "Select Node Bundle:", 
                                                "Node Bundle File:", 100, "Select"); 
      pFileSelectDialog.setRootDir(new File("/"));
    }

    updateBundle(null, null);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract file system path to the node bundle.
   */ 
  public Path
  getBundlePath() 
  {
    return pBundleField.getPath();
  }

  /**
   * Get the node bundle metadata.
   */ 
  public NodeBundle
  getBundle() 
  {
    return pNodeBundle;
  }

  /**
   * Get whether to release all newly registered and/or modified nodes from the working area
   * if an error occurs in unpacking the node bundle.
   */ 
  public boolean
  getReleaseOnError()
  {
    return pReleaseOnErrorField.getValue(); 
  }

  /**
   * Get what steps to take when encountering previously existing local versions of nodes
   * being unpacked.
   */ 
  public ActionOnExistence
  getActionOnExistence()
  {
    return ActionOnExistence.valueOf(ActionOnExistence.class,
                                     pActionOnExistField.getSelected());     
  }

  /**
   * Get the table mapping the names of toolsets associated with the nodes in the node bundle
   * to toolsets at the local site.
   */ 
  public TreeMap<String,String> 
  getToolsetRemap()
  {
    TreeMap<String,String> remap = new TreeMap<String,String>();

    for(String tname : pToolsetFields.keySet()) {
      String value = pToolsetFields.get(tname).getSelected();
      if((value != null) && !value.equals("-")) 
        remap.put(tname, value);
    }

    return remap;
  }
  
  /**
   * Get the table mapping the names of selection keys associated with the nodes in the node 
   * bundle to selection keys at the local site.
   */ 
  public TreeMap<String,String> 
  getSelectionKeyRemap()
  {
    TreeMap<String,String> remap = new TreeMap<String,String>();

    for(String kname : pSelectionKeyFields.keySet())  {
      String value = pSelectionKeyFields.get(kname).getSelected();
      if((value != null) && !value.equals("-")) 
        remap.put(kname, value);
    }

    return remap;
  }
  
  /**
   * Get the table mapping the names of license keys associated with the nodes in the node 
   * bundle to license keys at the local site.
   */ 
  public TreeMap<String,String> 
  getLicenseKeyRemap()
  {
    TreeMap<String,String> remap = new TreeMap<String,String>();

    for(String kname : pLicenseKeyFields.keySet()) {
      String value = pLicenseKeyFields.get(kname).getSelected();
      if((value != null) && !value.equals("-")) 
        remap.put(kname, value);
    }

    return remap;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components for the current node bundle.
   */ 
  public void 
  updateBundle
  (
   Path bundlePath, 
   NodeBundle bundle
  ) 
  {
    /* header */
    if(bundlePath != null) 
      pHeaderLabel.setText("Unpack Bundle:  " + bundlePath.getName()); 
    else 
      pHeaderLabel.setText("Unpack Bundle:"); 

    /* bundle info */
    pNodeBundle = bundle;
    if(pNodeBundle != null) {
      pCreatedOnField.setText(TimeStamps.format(pNodeBundle.getCreatedOn()));
      pCreatedByField.setText(pNodeBundle.getCreatedBy() + " @ " + 
                              pNodeBundle.getCustomer());
      pPipelineVersionField.setText(pNodeBundle.getPipelineVersion());
    }
    else {
      pCreatedOnField.setText(null);
      pCreatedByField.setText(null);
      pPipelineVersionField.setText(null);
    }

    /* bundled nodes */
    {
      pNodeBox.removeAll();

      if(pNodeBundle != null) {
        for(NodeMod mod : pNodeBundle.getWorkingVersions()) {
          pNodeBox.add(UIFactory.createTextField(mod.getName(), sNSize, JLabel.LEFT));
          pNodeBox.add(Box.createRigidArea(new Dimension(0, 3)));
        }
      }
      
      pNodeBox.revalidate();
      pNodeBox.repaint();
    }

    /* lookup all used selection and license keys in the bundle */
    TreeSet<String> tsets = new TreeSet<String>();
    TreeSet<String> skeys = new TreeSet<String>();
    TreeSet<String> lkeys = new TreeSet<String>();
    if(pNodeBundle != null) {
      tsets.addAll(pNodeBundle.getAllToolsetNames());
      for(NodeMod mod : pNodeBundle.getWorkingVersions()) {
        JobReqs jreqs = mod.getJobRequirements();
        if(jreqs != null) {
          skeys.addAll(jreqs.getSelectionKeys());
          lkeys.addAll(jreqs.getLicenseKeys());
        }
      }
    }

    /* lookup current local toolset, selection key and license key names */
    TreeSet<String> toolsets = null;
    String defaultToolset = null;
    TreeSet<String> selectionKeys = null;
    TreeSet<String> licenseKeys = null;
    try {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient mclient = master.getMasterMgrClient(pChannel);
      toolsets = mclient.getActiveToolsetNames();
      defaultToolset = mclient.getDefaultToolsetName();
      
      QueueMgrClient qclient = master.getQueueMgrClient(pChannel);
      selectionKeys = qclient.getSelectionKeyNames();
      licenseKeys = qclient.getLicenseKeyNames();
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
      return;
    }

    /* rebuild the toolsets drawer */
    {
      TreeMap<String,String> oldRemap = getToolsetRemap();

      pToolsetBox.removeAll();
      pToolsetFields.clear();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];

      if(!tsets.isEmpty()) {
        ArrayList<String> names = new ArrayList<String>();
        names.add("-");
        names.addAll(toolsets);

        for(String tname : tsets) {
          JCollectionField field = 
            UIFactory.createTitledCollectionField
              (tpanel, tname + ":", sTSize, 
               vpanel, names, this, sVSize, 
               "Remap the bundled toolset (" + tname + ") to this local toolset.");
          
          String old = oldRemap.get(tname);
          if((old != null) && names.contains(old))
            field.setSelected(old);

          pToolsetFields.put(tname, field);
          
          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
        }
      }
      else {
        tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
        vpanel.add(Box.createHorizontalGlue());
      }

      pToolsetBox.add(comps[2]);
      pToolsetBox.revalidate();
      pToolsetBox.repaint();
    }

    /* rebuild the selection keys drawer */
    {
      TreeMap<String,String> oldRemap = getSelectionKeyRemap();

      pSelectionKeyBox.removeAll();
      pSelectionKeyFields.clear();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];

      if(!skeys.isEmpty()) {
        ArrayList<String> names = new ArrayList<String>();
        names.add("-");
        names.addAll(selectionKeys);

        for(String kname : skeys) {
          JCollectionField field = 
            UIFactory.createTitledCollectionField
              (tpanel, kname + ":", sTSize, 
               vpanel, names, this, sVSize, 
               "Remap the bundled selection key (" + kname + ") to this " +
               "local selection key.");
          
          String old = oldRemap.get(kname);
          if((old != null) && names.contains(old))
            field.setSelected(old);

          pSelectionKeyFields.put(kname, field);
          
          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
        }
      }
      else {
        tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
        vpanel.add(Box.createHorizontalGlue());
      }

      pSelectionKeyBox.add(comps[2]);
      pSelectionKeyBox.revalidate();
      pSelectionKeyBox.repaint();
    }

    /* rebuild the license keys drawer */
    {
      TreeMap<String,String> oldRemap = getLicenseKeyRemap();

      pLicenseKeyBox.removeAll();
      pLicenseKeyFields.clear();

      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];

      if(!lkeys.isEmpty()) {
        ArrayList<String> names = new ArrayList<String>();
        names.add("-");
        names.addAll(licenseKeys);

        for(String kname : lkeys) {
          JCollectionField field = 
            UIFactory.createTitledCollectionField
              (tpanel, kname + ":", sTSize, 
               vpanel, names, this, sVSize, 
               "Remap the bundled license key (" + kname + ") to this " +
               "local license key.");
          
          String old = oldRemap.get(kname);
          if((old != null) && names.contains(old))
            field.setSelected(old);

          pLicenseKeyFields.put(kname, field);
          
          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
        }
      }
      else {
        tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
        vpanel.add(Box.createHorizontalGlue());
      }

      pLicenseKeyBox.add(comps[2]);
      pLicenseKeyBox.revalidate();
      pLicenseKeyBox.repaint();
    }

    /* whether the bundle is valid */ 
    pConfirmButton.setEnabled((bundlePath != null) && (pNodeBundle != null));
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
    if(cmd.equals("bundle-changed"))
      doBundleChanged();  
    else if(cmd.equals("browse"))
      doBrowse();  
    else 
      super.actionPerformed(e);
  }


  /*----------------------------------------------------------------------------------------*/
  /*  A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply changes and close. 
   */ 
  public void 
  doConfirm()
  {

    // ...

    super.doConfirm();
  }

  /** 
   * The bundle file name has been modified.
   */ 
  private void
  doBundleChanged()
  {
    Path bundlePath = pBundleField.getPath();
    if(bundlePath != null) {
      ExtractBundleTask task = new ExtractBundleTask(bundlePath);
      task.start();	
    }
  }

  /** 
   * Browse for a new bundle file.
   */ 
  private void
  doBrowse()
  {
    Path bundlePath = pBundleField.getPath();
    if(bundlePath == null)
      bundlePath = new Path(PackageInfo.sWorkPath, pAuthor + "/" + pView);
    pFileSelectDialog.updateTargetFile(bundlePath.toFile());

    pFileSelectDialog.setVisible(true);
    if(pFileSelectDialog.wasConfirmed()) 
      pBundleField.setPath(new Path(pFileSelectDialog.getSelectedFile()));
    doBundleChanged();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the node metadata from the node bundle. 
   */ 
  private
  class ExtractBundleTask
   extends Thread
  {
    public 
    ExtractBundleTask
    (
     Path bundlePath
    ) 
    {
      super("JUnpackBundleDialog:ExtractBundleTask");

      pPath = bundlePath;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pChannel, "Extracting Bundle Metadata: " + pPath)) {
 	try {
 	  MasterMgrClient client = master.getMasterMgrClient(pChannel);
 	  NodeBundle bundle = client.extractBundle(pPath); 
       
          UpdateBundleTask task = new UpdateBundleTask(pPath, bundle);
          SwingUtilities.invokeLater(task);
 	}
 	catch(PipelineException ex) {
 	  master.showErrorDialog(ex);
 	  return;
 	}
 	finally {
 	  master.endPanelOp(pChannel, "Done.");
 	}
      }
    }

    private Path  pPath;
  }

  /** 
   * Update the dialog components.
   */ 
  private
  class UpdateBundleTask
   extends Thread
  {
    public 
    UpdateBundleTask
    (
     Path bundlePath, 
     NodeBundle bundle
    ) 
    {
      super("JUnpackBundleDialog:UpdateBundleTask");

      pPath = bundlePath;
      pBundle = bundle;
    }

    public void 
    run() 
    {
      updateBundle(pPath, pBundle);
    }

    private Path pPath;
    private NodeBundle pBundle;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 55089737091074959L;
  
  private static final int sTSize = 180;
  private static final int sVSize = 240;
  private static final int sNSize = 480;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The index of the update channel.
   */ 
  private int  pChannel; 

  /**
   * Target working area.
   */ 
  private String pAuthor;
  private String pView;

  /**
   * Target working area.
   */ 
  private NodeBundle  pNodeBundle; 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Bundle information.
   */ 
  private JTextField  pCreatedOnField;
  private JTextField  pCreatedByField;
  private JTextField  pPipelineVersionField;

  /**
   * Bundled node components.
   */ 
  private Box pNodeBox;

  /**
   * Unpacking options.
   */ 
  private JPathField        pBundleField;
  private JBooleanField     pReleaseOnErrorField;
  private JCollectionField  pActionOnExistField;

  /**
   * Toolset remap components.
   */ 
  private Box                               pToolsetBox;
  private TreeMap<String,JCollectionField>  pToolsetFields;

  /**
   * Selection key remap components.
   */ 
  private Box                               pSelectionKeyBox;
  private TreeMap<String,JCollectionField>  pSelectionKeyFields;

  /**
   * License key remap components.
   */ 
  private Box                               pLicenseKeyBox;
  private TreeMap<String,JCollectionField>  pLicenseKeyFields;

  /**
   * The node bundle file selection dialog.
   */ 
  private JFileSelectDialog  pFileSelectDialog;

}
