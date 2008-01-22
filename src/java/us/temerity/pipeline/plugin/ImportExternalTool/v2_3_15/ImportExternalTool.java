// $Id: ImportExternalTool.java,v 1.1 2008/01/22 16:58:55 jim Exp $

package us.temerity.pipeline.plugin.ImportExternalTool.v2_3_15;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.VersionID.Level;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   I M P O R T   E X T E R N A L   T O O L                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Finds a sequence located somewhere outside the working area, creates a node that represents
 * those files (using symlinks to the original source to save space and time), checks that
 * node in, and then checks it back out frozen.
 * <p>
 * Due to the nature of symlinks and the necessity for the server to be able to resolve these
 * links, this tool currently only works on Linux. It would be possible to make it work on
 * some other operating system, but it would require information about server name mapping to
 * be available to the tool in some form. It would also require the symlinks to be made on the
 * server side, in such a way that they might not actually be visible from the artist's
 * workstation, but would be visible from the file manager, which is what matter for the
 * check-in.
 * <p>
 * The following phases are run.
 * <ul>
 * <li> Collect Phase 1: Allow the user to select the file(s) to be added and then input
 *      registration information for that node and a check-in message.
 * <li> Execute Phase 1: Go ahead and register the node, make the symlinks, do a status
 *      update.  If the status update succeeds, check-in the node and then check it out
 *      frozen.  Otherwise, remove the node and throw an error message.
 */
public 
class ImportExternalTool
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  ImportExternalTool()
  {
    super("ImportExternal", new VersionID("2.3.15"), "Temerity",
          "Tool for quickly adding a file outside of the working area into Pipeline.");
    
    underDevelopment();
    
    addPhase(new PhaseOne());
    addPhase(new PhaseTwo());
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   O N E                                                                    */
  /*----------------------------------------------------------------------------------------*/
  private 
  class PhaseOne
    extends BaseTool.ToolPhase
  {
    public 
    PhaseOne() 
    {
      super();
    }
    
    @Override
    public String
    collectInput() 
      throws PipelineException 
    {
      pDialog = new JToolDialog("Import External Tool", null, "Continue");
      
      JFileSeqSelectDialog dialog = new JFileSeqSelectDialog(pDialog);
      dialog.updateTargetDir(new File("/"));
      dialog.setVisible(true);
      if(!dialog.wasConfirmed())
        return null;
      pSourcePath = dialog.getDirectoryPath();
      if(pSourcePath == null)
        return null;
      pSourceSeq = dialog.getSelectedFileSeq();
      if (pSourceSeq == null)
        return null;
      
      return ": Gathering Pipeline Information.";
    }
    
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      pToolsets = mclient.getActiveToolsetNames();
      pDefaultToolset = mclient.getDefaultToolsetName();
      
      pPlugins = new TreeMap<String, TripleMap<String,String,VersionID,TreeSet<OsType>>>();
      pLayouts = new TreeMap<String, PluginMenuLayout>();
      
      TripleMap<String,String,VersionID,TreeSet<OsType>> all = 
        PluginMgrClient.getInstance().getEditors();
      
      for (String toolset : pToolsets) {
        DoubleMap<String,String,TreeSet<VersionID>> index = 
          mclient.getToolsetEditorPlugins(toolset);
        TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = 
          new TripleMap<String,String,VersionID,TreeSet<OsType>>();
        for(String vendor : index.keySet()) {
          for(String name : index.keySet(vendor)) {
            for(VersionID vid : index.get(vendor, name)) {
              plugins.put(vendor, name, vid, all.get(vendor, name, vid));
            }
          }
        }
        pPlugins.put(toolset, plugins);
        
        PluginMenuLayout layout = mclient.getEditorMenuLayout(toolset);
        pLayouts.put(toolset, layout);
      }
      
      pDefaultEditors = mclient.getDefaultSuffixEditors();
      
      return NextPhase.Continue;
    }
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   T W O                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class PhaseTwo
    extends BaseTool.ToolPhase
    implements ActionListener
  {
    public 
    PhaseTwo() 
    {
      super();
    }
    
    /**
     * Allow the user to select the file(s) to be added and then input registration
     * information for that node and a check-in message.
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    @Override
    public String
    collectInput() 
      throws PipelineException 
    {
      pHasFrameNums = pSourceSeq.hasFrameNumbers();
      
      Path sourcePath = new Path(pSourcePath, pSourceSeq.getFilePattern().getPrefix());
      
      /* Make an equivalent of the register dialog. */
      {
        Box body = null;
        {
          Component comps[] = UIFactory.createTitledPanels();
          JPanel tpanel = (JPanel) comps[0];
          JPanel vpanel = (JPanel) comps[1];
          body = (Box) comps[2];
          
          String sName = sourcePath.toString() + "/" + pSourceSeq.toString();
          
          JTextField source = 
            UIFactory.createTitledTextField(tpanel, "Source Prefix:", sTSize, 
                                            vpanel, sName, sVSize);
          source.setEditable(false);
          
          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
          
          pPrefixField =
            UIFactory.createTitledPathField(tpanel, "Target Prefix:", sTSize, 
                                            vpanel, new Path("/"), sVSize);
          
          UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
          
          {
            tpanel.add(UIFactory.createFixedLabel("Frame Range:", sTSize, JLabel.RIGHT));

            {
              Box hbox = new Box(BoxLayout.X_AXIS);

              {
                JIntegerField field = 
                  UIFactory.createIntegerField(null, sVSize1, JLabel.CENTER);
                pStartFrameField = field;

                hbox.add(field);
              }
              
              hbox.add(Box.createHorizontalGlue());
              hbox.add(Box.createRigidArea(new Dimension(8, 0)));

              {
                JLabel label = new JLabel("to");
                pToLabel = label;

                label.setName("DisableLabel");

                hbox.add(label);
              }

              hbox.add(Box.createRigidArea(new Dimension(8, 0)));
              hbox.add(Box.createHorizontalGlue());

              {
                JIntegerField field = 
                  UIFactory.createIntegerField(null, sVSize1, JLabel.CENTER);
                pEndFrameField = field;

                hbox.add(field);
              }

              hbox.add(Box.createHorizontalGlue());
              hbox.add(Box.createRigidArea(new Dimension(8, 0)));

              {
                JLabel label = new JLabel("by");
                pByLabel = label;

                label.setName("DisableLabel");

                hbox.add(label);
              }

              hbox.add(Box.createRigidArea(new Dimension(8, 0)));
              hbox.add(Box.createHorizontalGlue());

              {
                JIntegerField field = 
                  UIFactory.createIntegerField(null, sVSize1, JLabel.CENTER);
                pByFrameField = field;

                hbox.add(field);
              }
              
              Dimension size = new Dimension(sVSize+1, 19);
              hbox.setMinimumSize(size);
              hbox.setMaximumSize(size);
              hbox.setPreferredSize(size);

              vpanel.add(hbox);
            }
          }
          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
          
          pFramePaddingField = 
            UIFactory.createTitledIntegerField(tpanel, "Frame Padding:", sTSize, 
                                              vpanel, pSourceSeq.getFilePattern().getPadding(), sVSize);

          UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

          {
            JAlphaNumField field = 
              UIFactory.createTitledAlphaNumField(tpanel, "Filename Suffix:", sTSize, 
                                                 vpanel, null, sVSize);
            pSuffixField = field;
            
            field.addActionListener(this);
            field.setActionCommand("update-editor");
          }

          UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

          {
            JCollectionField field = 
              UIFactory.createTitledCollectionField(tpanel, "Toolset:", sTSize, 
                                                   vpanel, pToolsets, pDialog, sVSize, null);
            if (pDefaultToolset != null)
              field.setSelected(pDefaultToolset);
            pToolsetField = field;

            field.setActionCommand("toolset-changed");
            field.addActionListener(this);          
          }
          
          UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
          
          {
            JPluginSelectionField field = 
              UIFactory.createTitledPluginSelectionField
              (tpanel, "Editor:", sTSize, 
               vpanel, new PluginMenuLayout(), 
               new TripleMap<String,String,VersionID,TreeSet<OsType>>(), sVSize, 
               "The Editor plugin used to edit/view the files associated with the node.");
            pEditorField = field;

            field.setActionCommand("editor-changed");
            field.addActionListener(this);
          }

          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

          {
            pEditorVersionField = 
              UIFactory.createTitledTextField
              (tpanel, "Version:", sTSize, 
               vpanel, "-", sVSize, 
               "The revision number of the Editor plugin.");
          }

          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

          {
            pEditorVendorField = 
              UIFactory.createTitledTextField
              (tpanel, "Vendor:", sTSize, 
               vpanel, "-", sVSize, 
               "The name of the vendor of the Editor plugin.");
          }
          
          doToolsetChanged();
          pSuffixField.setText(pSourceSeq.getFilePattern().getSuffix());
          doUpdateEditor();

          UIFactory.addVerticalGlue(tpanel, vpanel);
        }
        
        pDialog = new JToolDialog("Import External Tool", body, "Continue");
      }
      
      if (!pSourceSeq.hasFrameNumbers()) {
        pStartFrameField.setEditable(false);
        pEndFrameField.setEditable(false);
        pByFrameField.setEditable(false);
      }
      else if (pSourceSeq.isSingle()) {
        FrameRange range = pSourceSeq.getFrameRange();
        pStartFrameField.setEditable(true);
        pEndFrameField.setEditable(false);
        pByFrameField.setEditable(false);
        pStartFrameField.setValue(range.getStart());
      }
      else {
        FrameRange range = pSourceSeq.getFrameRange();
        pStartFrameField.setEditable(true);
        pEndFrameField.setEditable(false);
        pByFrameField.setEditable(true);
        pStartFrameField.setValue(range.getStart());
        pEndFrameField.setValue(range.getEnd());
        pByFrameField.setValue(range.getBy());
        pStartFrameField.addActionListener(this);
        pStartFrameField.setActionCommand("update-frame-fields");
        pByFrameField.addActionListener(this);
        pByFrameField.setActionCommand("update-frame-fields");
      }

      pDialog.setVisible(true);
      
      if (!pDialog.wasConfirmed())
        return null;

      return ": Importing the external file(s) now.";
    }
    
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      doUpdateFrameFields();
      
      Integer start = pStartFrameField.getValue();
      Integer end = pEndFrameField.getValue();
      Integer by = pByFrameField.getValue();
      Integer padding = pFramePaddingField.getValue();
      if (padding == null)
        padding = 0;
      
      Path targetPath = pPrefixField.getPath();
      String targetPrefix = targetPath.getName();
      String suffix = pSuffixField.getText();
      if((suffix != null) && (suffix.length() == 0)) 
        suffix = null;
      
      FileSeq targetSeq = null;
      if (pSourceSeq.isSingle()) {
        if (!pSourceSeq.hasFrameNumbers()) 
          targetSeq = new FileSeq(targetPrefix, suffix);
        else 
          targetSeq = new FileSeq(new FilePattern(targetPrefix, padding, suffix), 
                                  new FrameRange(start));
      }
      else 
        targetSeq = new FileSeq(new FilePattern(targetPrefix, padding, suffix), 
                                new FrameRange(start, end, by));
      
      BaseEditor editor = null;
      {
        String name = pEditorField.getPluginName();
        if (name != null) {
         String vendor = pEditorField.getPluginVendor();
         VersionID ver = pEditorField.getPluginVersionID();
         editor = PluginMgrClient.getInstance().newEditor(name, ver, vendor);
        }
      }
      
      NodeMod mod = new NodeMod(targetPath.toString(), targetSeq, null, 
                                pToolsetField.getSelected(), editor);
      mclient.register(getAuthor(), getView(), mod);
      
      NodeID id = new NodeID(getAuthor(), getView(), targetPath.toString());
      
      ArrayList<String> args = new ArrayList<String>();
      try
      {
        File f = File.createTempFile("ImportExternalTool-SymLink.", ".bash", 
          PackageInfo.sTempPath.toFile());
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        Path targetParent = new Path(new Path(new Path(PackageInfo.sWorkPath, getAuthor()), getView()), 
                                                       targetPath.getParentPath());
        out.write("mkdir -p " + targetParent.toString() + "\n");
        if (targetSeq.hasFrameNumbers()) {
          for (int i = 0; i < targetSeq.getFrameRange().numFrames(); i++) {
            Path targetFinal = new Path(targetParent, targetSeq.getPath(i));
            Path sourceFinal = new Path(pSourcePath, pSourceSeq.getPath(i));
            out.write("ln -s " + sourceFinal + " " + targetFinal + "\n");
          }
        } 
        else {
          Path targetFinal = new Path(targetParent, targetSeq.getPath(0));
          Path sourceFinal = new Path(pSourcePath, pSourceSeq.getPath(0));
          out.write("ln -s " + sourceFinal + " " + targetFinal + "\n");
        }
        
        out.close();
        
        args.add(f.getAbsolutePath());
        
      } catch(IOException ex) {
        mclient.release(id, true);
        throw new PipelineException
          ("Unable to create the temporary bash script used to create" + 
           "the symbolic links!");
    }
      
      TreeMap<String,String> env = 
        mclient.getToolsetEnvironment
        (getAuthor(), getView(), pDefaultToolset, PackageInfo.sOsType);
     
      SubProcessLight proc = 
        new SubProcessLight("ImportExternalTool-SymLink", "bash", args, env, PackageInfo.sTempPath.toFile());
      try {
        proc.start();
        proc.join();
        if(!proc.wasSuccessful()) {
          mclient.release(id, true);
          throw new PipelineException
          ("Failed to run the bash script\n\n" +
            proc.getStdOut() + "\n\n" + 
            proc.getStdErr());
        }
      }
      catch(InterruptedException ex) {
        mclient.release(id, true);
        throw new PipelineException(ex);
      }
      NodeStatus status = mclient.status(id);
      NodeDetails details = status.getDetails();
      if (details.getOverallNodeState() == OverallNodeState.Missing) {
        mclient.release(id, true);
        throw new PipelineException
          ("After the symbolic links were made, the server was unable to see the files.  " +
           "This is probably due to the selection of files which are only visible " +
           "on the local file system, not the network file system.  " +
           "Check and see if the path (" + pSourcePath + ") is accessible from the " +
           "Pipeline server machine.");
      }
      
      mclient.checkIn
        (id, "Created by the Import External tool from the file sequence " +
             "(" + pSourceSeq.toString() + ") in the directory " +
             "(" + pSourcePath.toString() +")", Level.Minor);
      
      mclient.checkOut(id, null, CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
      
      return NextPhase.Finish;
    }
    
    
    
    /*--------------------------------------------------------------------------------------*/
    /*   L I S T E N E R S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    /*-- ACTION LISTENER METHODS -----------------------------------------------------------*/

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
//      if(cmd.equals("browse")) 
//        doBrowse();
      if(cmd.equals("update-frame-fields")) 
        doUpdateFrameFields();
      else if(cmd.equals("update-editor")) 
        doUpdateEditor();
      else if(cmd.equals("toolset-changed")) 
        doToolsetChanged();
      else if(cmd.equals("editor-changed")) 
        doEditorChanged();
    }
    
    
    
    /*--------------------------------------------------------------------------------------*/
    /*   A C T I O N S                                                                      */
    /*--------------------------------------------------------------------------------------*/
    
    /**
     * Update the enabled state of the frame related fields.
     */ 
    private void 
    doUpdateFrameFields()
    {
      if (pSourceSeq.hasFrameNumbers()) {
       if (!pSourceSeq.isSingle()) {
         Integer start = pStartFrameField.getValue();
         Integer by = pByFrameField.getValue();

         FrameRange frameRange = pSourceSeq.getFrameRange();
         if (start == null)
           start = frameRange.getStart();
         if (by == null)
           by = frameRange.getBy();

         Integer totalFrames = frameRange.numFrames();
         Integer end = start + ( (totalFrames - 1) * by);
         pStartFrameField.removeActionListener(this);
         pByFrameField.removeActionListener(this);
         pStartFrameField.setValue(start);
         pEndFrameField.setValue(end);
         pByFrameField.setValue(by);
         pStartFrameField.addActionListener(this);
         pByFrameField.addActionListener(this);

       }
      }
    }
    
    /**
     * Update the editor based on the current filename suffix.
     */ 
    private void 
    doUpdateEditor()
    {
      BaseEditor editor = null;
      String suffix = pSuffixField.getText();
      if((suffix != null) && (suffix.length() > 0)) {
        for (SuffixEditor suf : pDefaultEditors) {
          if (suffix.equals(suf.getSuffix())) {
            editor = suf.getEditor();
            break;
          }
        }
      }
      pEditorField.setPlugin(editor);
    }

    /**
     * Update the editor version field when the editor plugin changes.
     */ 
    private void 
    doEditorChanged() 
    {
      if(pEditorField.getPluginName() != null) {
        pEditorVersionField.setText("v" + pEditorField.getPluginVersionID());
        pEditorVendorField.setText(pEditorField.getPluginVendor());
      }
      else {
        pEditorVersionField.setText("-");
        pEditorVendorField.setText("-");
      }
    }

    /**
     * Update the editor plugins available in the current toolset.
     */ 
    private void 
    doToolsetChanged()
    {
      String toolset = pToolsetField.getSelected();
  
      pEditorField.updatePlugins(pLayouts.get(toolset), pPlugins.get(toolset));
    }
    

    /*--------------------------------------------------------------------------------------*/
    /*   I N T E R N A L S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    /**
     * The filename prefix.
     */ 
    private JPathField  pPrefixField;
    
    /**
     * The start frame.
     */ 
    private JIntegerField  pStartFrameField;

    /**
     * The "to" label.
     */ 
    private JLabel pToLabel;
    
    /**
     * The end frame.
     */ 
    private JIntegerField  pEndFrameField;

    /**
     * The "by" label.
     */ 
    private JLabel pByLabel;
    
    /**
     * The by frame.
     */ 
    private JIntegerField  pByFrameField;
    
    /**
     * The frame number padding.
     */ 
    private JIntegerField  pFramePaddingField;

    /**
     * The filename suffix.
     */ 
    private JAlphaNumField  pSuffixField;
    
    /**
     * The toolset name.
     */ 
    private JCollectionField  pToolsetField;
    
    /**
     * The editor plugin.
     */ 
    private JPluginSelectionField pEditorField;

    /**
     * The editor revision number. 
     */ 
    private JTextField pEditorVersionField;

    /**
     * The editor vendor name. 
     */ 
    private JTextField pEditorVendorField;
    
    private JButton pBrowseButton;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private FileSeq pSourceSeq;
  
  private boolean pHasFrameNums;
  
  private Integer pSeqLength;

  private JToolDialog pDialog;
  
  private Path pSourcePath;
  
  private TreeSet<String> pToolsets;
  private String pDefaultToolset;
  /**
   * Map is TreeSet,PluginName,Vendor,VersionID
   */
  private TreeMap<String,
            TripleMap<String,String,VersionID,TreeSet<OsType>>> pPlugins;
  
  private TreeMap<String,PluginMenuLayout> pLayouts;
  
  private TreeSet<SuffixEditor> pDefaultEditors;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4256918169396528762L;
  private static final int sTSize  = 150;
  
  private static final int sVSize  = 300;
  private static final int sVSize1 = 80;
}
