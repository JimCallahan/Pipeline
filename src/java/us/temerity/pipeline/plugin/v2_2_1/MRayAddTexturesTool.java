// $Id: MRayAddTexturesTool.java,v 1.3 2007/04/05 10:15:57 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   A D D   T E X T U R E S   T O O L                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Registers and links Mental Ray textures and source images to a texture grouping node. 
 */
public class 
MRayAddTexturesTool 
  extends BaseTool
{
  public 
  MRayAddTexturesTool()
  {
    super("MRayAddTextures", new VersionID("2.2.1"), "Temerity",
	  "Registers and links Mental Ray textures and source images to a texture " + 
          "grouping node.");

    pImageSuffixes = new ArrayList<String>();
    pImageSuffixes.add("bmp");
    pImageSuffixes.add("iff");
    pImageSuffixes.add("gif");
    pImageSuffixes.add("hdr");
    pImageSuffixes.add("jpg");
    pImageSuffixes.add("jpeg");
    pImageSuffixes.add("png");
    pImageSuffixes.add("ppm");
    pImageSuffixes.add("psd");
    pImageSuffixes.add("rgb");
    pImageSuffixes.add("sgi");
    pImageSuffixes.add("bw");
    pImageSuffixes.add("tga");
    pImageSuffixes.add("tif");
    pImageSuffixes.add("tiff");
      
    addPhase(new PhaseOne());
    addPhase(new PhaseTwo());
    addPhase(new PhaseThree());
    addPhase(new PhaseFour());

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
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
    
    /**
     * Specify the essential common node parameters. 
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    public String
    collectInput() 
      throws PipelineException 
    {
      /* create dialog components */
      JComponent body = null;
      JPathField textureGroupNameField;
      {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
        body = (JComponent) comps[2];

        textureGroupNameField = 
          UIFactory.createTitledPathField
          (tpanel, "Texture Group Node:", sTSize, 
           vpanel, new Path("/"), sVSize,
           "The node downstream of all generated MAP texture nodes used to group the " + 
           "textures for a particular model.");
      }

      /* validate node selections and initialize dialog components */ 
      if(pPrimary == null)
	throw new PipelineException
	  ("The primary selection must be the Texture Grouping Node!");
      textureGroupNameField.setText(pPrimary);

      /* query the user */
      JToolDialog diag = new JToolDialog("MRay Add Textures: Esentials", body, "Continue");
      diag.setVisible(true);
      if(!diag.wasConfirmed())
        return null;
      
      /* texture group node name */ 
      pTextureGroup = textureGroupNameField.getPath(); 

      return ": Validating Source Images...";
    }
    
    /**
     * Validate the selected source image nodes and target texture group node.
     * 
     * @param mclient
     *   The network connection to the plmaster(1) daemon.
     * 
     * @param qclient
     *   The network connection to the plqueuemgr(1) daemon.
     * 
     * @return    
     *   What to do next: Continue, Repeat or Finish?
     * 
     * @throws PipelineException
     *    If unable to sucessfully execute this phase of the tool.
     */
    public NextPhase
    execute
    (
     MasterMgrClient mclient,
     QueueMgrClient qclient
    ) 
      throws PipelineException
    { 
      pActiveToolsets = new TreeSet<String>();
      pActiveToolsets.addAll(mclient.getActiveToolsetNames());

      pImageVersions = new TreeMap<String,NodeMod>();
      pImageSeqs     = new TreeMap<String,FileSeq>();
        
      for(String name : pSelected.keySet()) {
        NodeStatus status = pSelected.get(name);
        
        if((pAuthor == null) || (pView == null)) {
          NodeID nodeID = status.getNodeID();
          pAuthor = nodeID.getAuthor();
          pView   = nodeID.getView();
        }

        NodeDetails details = status.getDetails();
        if(details != null) {
          NodeMod mod = details.getWorkingVersion();
          if(name.equals(pPrimary)) 
            pDefaultToolset = mod.getToolset();
          else 
            pImageSeqs.put(name, mod.getPrimarySequence()); 
        }
      }

      validateImageNodes(mclient);

      if(pDefaultToolset == null) 
        throw new PipelineException
          ("The selected Texture Grouping Node (" + pPrimary + ") must be checked-out in " + 
           "the current working area with valid status (not grey) to be used with the " + 
           getName() + " Tool.");

      if((pAuthor == null) || (pView == null)) 
        throw new PipelineException
          ("Unable to determine the current working area view from the selected nodes!");

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
     * Edit the list of source images to build into textures.  New source image nodes can 
     * also be registered by browsing for the images.
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    public String
    collectInput() 
      throws PipelineException 
    {
      pImagesModified = false;

      /* create dialog components */
      JScrollPane scroll = null;
      {
        JPanel vpanel = new JPanel();
        vpanel.setLayout(new BoxLayout(vpanel, BoxLayout.Y_AXIS));
          
        for(String name : pImageSeqs.keySet()) {
          FileSeq fseq = pImageSeqs.get(name);

          Box hbox = new Box(BoxLayout.X_AXIS);
          {
            {
              Path path = new Path(name);
              String text = path.getParent() + "/" + fseq;
              hbox.add(UIFactory.createTextField(text, sVSize, JLabel.LEFT));
            }
            
            hbox.add(Box.createRigidArea(new Dimension(3, 0)));
            
            {
              String text = (pImageVersions.containsKey(name)) ? "Use Existing" : "Register";
              JTextField field = UIFactory.createTextField(text, sSSize, JLabel.CENTER);
              field.setMaximumSize(new Dimension(sTSize, 19));
              hbox.add(field);
            }
            
            hbox.add(Box.createRigidArea(new Dimension(8, 0)));
            
            {
	      JButton btn = new JButton();
	      btn.setName("CloseButton");
	      
	      Dimension size = new Dimension(15, 19);
	      btn.setMinimumSize(size);
	      btn.setMaximumSize(size);
	      btn.setPreferredSize(size);
	      
	      btn.setActionCommand("remove:" + name);
	      btn.addActionListener(this);
	      
	      hbox.add(btn);
	    } 
	    
	    hbox.add(Box.createRigidArea(new Dimension(4, 0)));

            vpanel.add(hbox);  
          }

          vpanel.add(Box.createRigidArea(new Dimension(0, 3)));        
        }

        vpanel.add(UIFactory.createFiller(sTSize+sSSize+15));
        
        {
          scroll = new JScrollPane(vpanel);
          
          scroll.setHorizontalScrollBarPolicy
            (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
          scroll.setVerticalScrollBarPolicy
            (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
          
          int ht = Math.min(Math.max(pImageSeqs.size() * 22, 100), 500);
          Dimension size = new Dimension(sVSize+sSSize+77, ht);
          scroll.setMinimumSize(size);
          scroll.setPreferredSize(size);
          
          scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        }
      }

      /* query the user */
      {
        pDialog = new JToolDialog("MRay Add Textures: Source Images", scroll, 
                                  "Continue", "Browse", null);
        
        JButton btn = pDialog.getApplyButton();
        btn.setActionCommand("browse");
        btn.addActionListener(this); 

        pDialog.setVisible(true);     
   
        if(pImagesModified)
          return ": Validating Source Images...";
        if(!pDialog.wasConfirmed())
          return null;
        return ": Finding Textures...";
      }
    }

    /**
     * Validate that all of the selected nodes are legitimate image source nodes.
     * 
     * @param mclient
     *   The network connection to the plmaster(1) daemon.
     * 
     * @param qclient
     *   The network connection to the plqueuemgr(1) daemon.
     * 
     * @return 
     *   What to do next: Continue, Repeat or Finish?
     * 
     * @throws PipelineException
     *   If unable to sucessfully execute this phase of the tool.
     */
    public NextPhase
    execute
    (
     MasterMgrClient mclient,
     QueueMgrClient qclient
    ) 
      throws PipelineException
    {
      if(!pImagesModified) 
        return NextPhase.Continue; 

      validateImageNodes(mclient);

      return NextPhase.Repeat; 
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
      if(cmd.startsWith("remove:")) {
        String name = cmd.substring(7); 
        pImageVersions.remove(name);
        pImageSeqs.remove(name);
        pImagesModified = true;
      }
      else if(cmd.equals("browse")) {
        JFileSeqSelectDialog diag = new JFileSeqSelectDialog(pDialog);
        diag.updateHeader(pAuthor, pView);

        Path root = new Path(PackageInfo.sWorkPath, pAuthor + "/" + pView);
        diag.setRootDir(root.toFile());

        if(pLastBrowsePath == null) {
          if(pTextureGroup != null) 
            pLastBrowsePath = new Path(root, pTextureGroup.getParentPath()); 
          else 
            pLastBrowsePath = root;
        }

        diag.updateTarget(pLastBrowsePath.toFile());

        diag.setVisible(true);
        if(diag.wasConfirmed()) {
          Path dpath = diag.getDirectoryPath();
          if(dpath != null) {
            pLastBrowsePath = new Path(root, dpath);

            ArrayList<FileSeq> fseqs = null;
            {
              FileSeq fseq = diag.getSelectedFileSeq();
              if(fseq != null) {
                fseqs = new ArrayList<FileSeq>();
                fseqs.add(fseq);
              }
              else {
                fseqs = diag.getAllFileSeqs();
              }
            }

            for(FileSeq fseq : fseqs) {
              Path npath = new Path(dpath, fseq.getFilePattern().getPrefix());
              pImageSeqs.put(npath.toString(), fseq);
              pImagesModified = true;
            }
          }
        }
      }

      if(pImagesModified) 
        pDialog.setVisible(false); 
    }



    /*--------------------------------------------------------------------------------------*/
    /*   I N T E R N A L S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    /**
     * The tool dialog showing the list of source images.
     */ 
    private JToolDialog  pDialog; 

    /**
     * Whether new images where added or existing ones removed.
     */ 
    private boolean  pImagesModified; 

    /**
     * The path to the last directory browsed for source images.
     */ 
    private Path  pLastBrowsePath; 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   T H R E E                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class PhaseThree
    extends BaseTool.ToolPhase
  {
    public 
    PhaseThree() 
    {
      super();
    }
    
    /**
     * 
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    public String
    collectInput() 
      throws PipelineException 
    {
      /* create dialog components */
      JComponent body = null;
      JCollectionField toolsetField; 
      JCollectionField formatField;
      JCollectionField texelLayoutField;
      JCollectionField byteOrderField;
      {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
        body = (JComponent) comps[2];

        {
          toolsetField = 
            UIFactory.createTitledCollectionField
            (tpanel, "Toolset", sTSize, 
             vpanel, pActiveToolsets, sVSize2, 
             "The name of the Toolset to use when registering new image and texture nodes.");
          
          toolsetField.setSelected(pDefaultToolset);
        }

        UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
          
        {
          ArrayList<String> choices = new ArrayList<String>();
          choices.add("Pyramid");
          choices.add("Flat");
          
          formatField = 
            UIFactory.createTitledCollectionField
            (tpanel, "Texture Format", sTSize, 
             vpanel, choices, sVSize2, 
             "The format of texture data stored in the generated MAP files.");
          
          formatField.setSelected("Pyramid");
        }

        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        {
          ArrayList<String> choices = new ArrayList<String>();
          choices.add("Scanlines");
          choices.add("Tiles");
          
          texelLayoutField = 
            UIFactory.createTitledCollectionField
            (tpanel, "Texel Layout", sTSize, 
             vpanel, choices, sVSize2, 
             "How to organize output texel data in the generated texture MAP files.");
          
          texelLayoutField.setSelected("Scanlines");
        }
        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
        
        {
          ArrayList<String> choices = new ArrayList<String>();
          choices.add("Little-Endian");
          choices.add("Big-Endian");
          
          byteOrderField = 
            UIFactory.createTitledCollectionField
            (tpanel, "Texture Byte Order", sTSize, 
             vpanel, choices, sVSize2, 
             "The byte ordering of texel data in the output texture MAP files.");
          
          byteOrderField.setSelected("Little-Endian");
        }
      }

      /* query the user */
      JToolDialog diag = new JToolDialog("MRay Add Textures: Node Properties", body, 
                                         "Continue");
      diag.setVisible(true);
      if(!diag.wasConfirmed())
        return null;
      
      /* toolset name */ 
      pToolset = toolsetField.getSelected();
      
      /* MRayTexture parameters */ 
      pTextureFormat = formatField.getSelected(); 
      pTexelLayout   = texelLayoutField.getSelected(); 
      pByteOrder     = byteOrderField.getSelected(); 

      return ": Registering/Linking Nodes...";
    }
    
    /**
     * Validate the selected source image nodes. 
     * 
     * @param mclient
     *   The network connection to the plmaster(1) daemon.
     * 
     * @param qclient
     *   The network connection to the plqueuemgr(1) daemon.
     * 
     * @return    
     *   What to do next: Continue, Repeat or Finish?
     * 
     * @throws PipelineException
     *    If unable to sucessfully execute this phase of the tool.
     */
    public NextPhase
    execute
    (
     MasterMgrClient mclient,
     QueueMgrClient qclient
    ) 
      throws PipelineException
    { 
      PluginMgrClient pclient = PluginMgrClient.getInstance();

      /* find the newest version of the MRayTexture Action plugin in the selected toolset 
           which is at least as new as this Tool plugin */ 
      VersionID mrayTextureVID = null;
      {
        VersionID vid = null;
        PluginSet plugins = mclient.getToolsetActionPlugins(pToolset);
        Set<VersionID> vids = plugins.getVersions("Temerity", "MRayTexture");
        if(vids != null) {
          for(VersionID pvid : vids) {
            if(pvid.compareTo(getVersionID()) >= 0) {
              if((vid == null) || (pvid.compareTo(vid) > 0)) 
                vid = pvid;
            }
          }
        }
        
        if(vid == null) 
          throw new PipelineException
            ("No sufficiently new (v" + getVersionID() + " or later) version of the " + 
             "MRayTexture Action plugin was available in the selected Toolset " + 
             "(" + pToolset + ")!");

        mrayTextureVID = vid;
      }

      /* find the newest version of the ImfDisp Editor plugin in the selected toolset 
           which is at least as new as this Tool plugin */ 
      VersionID imfDispVID = null;
      {
        VersionID vid = null;
        PluginSet plugins = mclient.getToolsetEditorPlugins(pToolset);
        Set<VersionID> vids = plugins.getVersions("Temerity", "ImfDisp");
        if(vids != null) {
          for(VersionID pvid : vids) {
            if(pvid.compareTo(getVersionID()) >= 0) {
              if((vid == null) || (pvid.compareTo(vid) > 0)) 
                vid = pvid;
            }
          }
        }
        
        if(vid == null) 
          throw new PipelineException
            ("No sufficiently new (v" + getVersionID() + " or later) version of the " + 
             "MRayTexture Action plugin was available in the selected Toolset " + 
             "(" + pToolset + ")!");

        imfDispVID = vid;
      }

      /* get node path to the directory which will contain the texture nodes */ 
      Path mapPath = new Path(pTextureGroup.getParentPath(), "map");

      /* process the source images */ 
      StringBuilder buf = new StringBuilder();
      for(String iname : pImageSeqs.keySet()) {
        FileSeq ifseq = pImageSeqs.get(iname);
        
        /* lookup or register a new source image node */ 
        NodeMod imod = pImageVersions.get(iname);
        if(imod == null) {
          try {
            String suffix = ifseq.getFilePattern().getSuffix();
            BaseEditor editor = mclient.getEditorForSuffix(suffix);
            NodeMod mod = new NodeMod(iname, ifseq, null, pToolset, editor);
            mclient.register(pAuthor, pView, mod); 
          }
          catch(PipelineException ex) {
            buf.append
              ("---\n" +
               "Source Image Node: " + iname + "\n\n" +
               "Unable to register the image source node.  Skipping texture node " + 
               "generation and node linking for this image.\n\n" + 
               ex.getMessage() + "\n\n");
            continue;              
          }
          
          try {
            imod = mclient.getWorkingVersion(pAuthor, pView, iname);
          }
          catch(PipelineException ex) {
            buf.append
              ("---\n" +
               "Source Image Node: " + iname + "\n\n" +
               "Unable to retrieve the newly registered image source node.  Skipping " + 
               "texture node generation and node linking for this image!\n\n"); 
            continue;
          }
        }
        
        /* register a new texture node */
        String tname = null;
        NodeMod tmod = null;
        {
          Path ipath = new Path(iname);
          Path path = new Path(mapPath, ipath.getName()); 
          tname = path.toString();

          try {
            tmod = mclient.getWorkingVersion(pAuthor, pView, tname);
          }
          catch(PipelineException ex) {
          }

          if(tmod != null) {
            buf.append
              ("---\n" +
               "Texture Node: " + tname + "\n\n" +
               "Skipped registration of texture node since it already exists in the " + 
               "current working area!\n\n");
          }
          else {
            try {
              mclient.getAllCheckedInVersions(tname);
              buf.append
                ("---\n" +
                 "Texture Node: " + tname + "\n\n" +
                 "Unable to register the texture node because at least one checked-in " + 
                 "version of the node exists, but the node is not checked-out in the " + 
                 "current working area!\n\n"); 
              continue;              
            }
            catch(PipelineException ex) {
            }

            try {
              FilePattern ipat = ifseq.getFilePattern();
              FilePattern mpat = new FilePattern(ipat.getPrefix(), ipat.getPadding(), "map");
              FileSeq mfseq = new FileSeq(mpat, ifseq.getFrameRange());
              BaseEditor editor = pclient.newEditor("ImfDisp", imfDispVID, "Temerity");
              NodeMod mod = new NodeMod(tname, mfseq, null, pToolset, editor);
              mclient.register(pAuthor, pView, mod);

              BaseAction action = 
                pclient.newAction("MRayTexture", mrayTextureVID, "Temerity");
              action.setSingleParamValue("ImageSource", iname);
              action.setSingleParamValue("Format", pTextureFormat);
              action.setSingleParamValue("TexelLayout", pTexelLayout);
              action.setSingleParamValue("ByteOrder", pByteOrder);
              mod.setAction(action);
              mclient.modifyProperties(pAuthor, pView, mod);
            }
            catch(PipelineException ex) {
            buf.append
              ("---\n" +
               "Texture Node: " + tname + "\n\n" +
               "Unable to register the texture node.  Skipping linking this node to the " + 
               "source image and Texture Grouping nodes.\n\n" + 
               ex.getMessage() + "\n\n");
              continue;              
            }
            
            try {
              imod = mclient.getWorkingVersion(pAuthor, pView, iname);
            }
            catch(PipelineException ex) {
              buf.append
                ("---\n" +
                 "Texture Node: " + tname + "\n\n" +
                 "Unable to retrieve the newly registered texture node.  Skipping linking " + 
                 "this node to the source image and Texture Grouping nodes.\n\n");
              continue;
            }
          }
        }

        /* link the nodes */ 
        try {
          mclient.link(pAuthor, pView, tname, iname, 
                       LinkPolicy.Dependency, LinkRelationship.OneToOne, 0); 
          pRoots.remove(iname);
        }
        catch(PipelineException ex) {
          buf.append
            ("---\n" +
             "Source Image Node: " + iname + "\n" +
             "Texture Node: " + tname + "\n\n" +
             "Unable to link image source node to the texture node.\n\n" + 
             ex.getMessage() + "\n\n");
        }
         
        try {
          mclient.link(pAuthor, pView, pTextureGroup.toString(), tname, 
                       LinkPolicy.Dependency, LinkRelationship.All, null); 
          pRoots.remove(tname);
        }
        catch(PipelineException ex) {
          buf.append
            ("---\n" +
             "Texture Node: " + tname + "\n\n" +
             "Texture Grouping Node: " + pTextureGroup + "\n\n" +
             "Unable to link texture node to the texture grouping node.\n\n" + 
             ex.getMessage() + "\n\n");
        }
      }

      String msg = buf.toString();
      if(msg.length() > 0) 
        pWarnings = msg;

      return NextPhase.Finish; 
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   F O U R                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class PhaseFour
    extends BaseTool.ToolPhase
  {
    public 
    PhaseFour() 
    {
      super();
    }
    
    /**
     * Display any warning messages.
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    public String
    collectInput() 
      throws PipelineException 
    {
      if(pWarnings != null) {
        JErrorDialog diag = new JErrorDialog(JToolDialog.getRootFrame());
        diag.setMessage
          ("Warning: ",  
           "Some problems where encountered while performing node operations:\n\n" + 
           pWarnings);
        diag.setVisible(true);
      }
      
      return ": Done.";
    }
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Check the node status of each source image node and remove any nodes which are 
   * unsuitable.
   */ 
  private void 
  validateImageNodes
  (
   MasterMgrClient mclient
  ) 
  {
    /* find illegal nodes */ 
    TreeSet<String> dead = new TreeSet<String>();
    for(String name : pImageSeqs.keySet()) {
      
      /* strip out nodes which aren't one of the supported image formats */ 
      {
        FileSeq fseq = pImageSeqs.get(name);
        String suffix = fseq.getFilePattern().getSuffix();
        if(!pImageSuffixes.contains(suffix)) {
          dead.add(name);
          break;
        }
      }

      /* update working version */ 
      NodeMod mod = pImageVersions.get(name);
      if(mod == null) {
        try {
          mod = mclient.getWorkingVersion(pAuthor, pView, name);
          pImageVersions.put(name, mod);
        }
        catch(PipelineException ex) {
        }
      }
    }

    /* remove illegal nodes from consideration */ 
    for(String name : dead) {
      pImageVersions.remove(name);
      pImageSeqs.remove(name);
    }      
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2035583670674887039L;

  private static final int sSSize  = 120;
  private static final int sTSize  = 160;
  private static final int sVSize  = 480;
  private static final int sVSize2 = 200;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The supported source image filename suffixes.
   */
  private ArrayList<String>  pImageSuffixes; 

  /**
   * The names of the active, default and selected toolsets.
   */ 
  private TreeSet<String>  pActiveToolsets; 
  private String           pDefaultToolset;  
  private String           pToolset; 

  /** 
   * The current working area view.
   */ 
  private String pAuthor;
  private String pView;

  /**
   * Fully qualified name of the texture grouping node as a path
   * or <CODE>null</CODE> if none was selected.
   */
  private Path  pTextureGroup; 

  /**
   * Common MRayTexture Action parameters 
   */
  private String  pTextureFormat;
  private String  pTexelLayout;  
  private String  pByteOrder;    

  /**
   * The working versions of source image nodes indexed by the node names. <P> 
   * 
   * Unregistered nodes are NOT included in this table.
   */ 
  private TreeMap<String,NodeMod>  pImageVersions;

  /**
   * The source image file sequences indexed by the image node names. <P> 
   * 
   * Unregistered nodes ARE included in this table.
   */ 
  private TreeMap<String,FileSeq>  pImageSeqs;

  /**
   * Node registration and linking warning messages.
   */ 
  private String  pWarnings;
    
}
