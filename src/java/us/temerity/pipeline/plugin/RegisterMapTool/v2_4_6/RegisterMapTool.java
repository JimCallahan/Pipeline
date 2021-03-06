// $Id: RegisterMapTool.java,v 1.2 2009/09/01 22:47:31 jim Exp $

package us.temerity.pipeline.plugin.RegisterMapTool.v2_4_6;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   R E G I S T E R   M A P   T O O L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Registers nodes associated with optimized Mental Ray memory mappable pyramid textures 
 * for all the selected image texture nodes.  If the primary node is is not a texture
 * file then it will be the texture grouping node and the newly created mip-maps will 
 * be linked to it. <P> 
 */
public class 
RegisterMapTool 
  extends BaseTool
{
  public 
  RegisterMapTool()
  {
    super("RegisterMap", new VersionID("2.4.6"), "Temerity",
	  "Registers nodes associated with optimized Mental Ray memory mappable " + 
	  "pyramid textures for all the selected image texture nodes.");

    pTextureFormats = new ArrayList<String>();
    pTextureFormats.add("bmp");
    pTextureFormats.add("iff");
    pTextureFormats.add("gif");
    pTextureFormats.add("hdr");
    pTextureFormats.add("jpg");
    pTextureFormats.add("jpeg");
    pTextureFormats.add("png");
    pTextureFormats.add("ppm");
    pTextureFormats.add("psd");
    pTextureFormats.add("rgb");
    pTextureFormats.add("sgi");
    pTextureFormats.add("bw");
    pTextureFormats.add("tga");
    pTextureFormats.add("tif");
    pTextureFormats.add("tiff");

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);    
  }



  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create and show graphical user interface components to collect information
   * from the user to use as input in the next phase of execution for the tool. <P>
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException
   *   If unable to validate the given user input.
   */
  public synchronized String 
  collectPhaseInput() 
    throws PipelineException
  {
    if(pSelected.size() == 0)
      return null;

    pOriginalNodes = new ArrayList<String>();
    pNewNodes = new ArrayList<String>();
    pToolsets = new ArrayList<String>();

    /* If the primary node is not a texture node, it will be the texture grouping node. */
    {
      NodeStatus status = pSelected.get(pPrimary);
      if(status.hasLightDetails()) {
	NodeMod mod = status.getLightDetails().getWorkingVersion();
	String suffix = mod.getPrimarySequence().getFilePattern().getSuffix();
	if(!pTextureFormats.contains(suffix)) {
	  pTextureGroupingNode = true;
	}
      }
    }

    for(String each : pSelected.keySet()) {
      NodeStatus status = pSelected.get(each);
      if(status.hasLightDetails()) {
        NodeMod mod = status.getLightDetails().getWorkingVersion();
        NodeID nodeID = status.getNodeID();
        String suffix = mod.getPrimarySequence().getFilePattern().getSuffix();
        if(pTextureFormats.contains(suffix)) {
          pOriginalNodes.add(each);
          pToolsets.add(mod.getToolset());
          pUser = nodeID.getAuthor();
          pView = nodeID.getView();
          Path p = new Path(each);
          String newName = p.getParentPath().getParent() + "/map/" + p.getName();
          pNewNodes.add(newName);
        }
      }
    }

    if(pOriginalNodes.size() > 0) {
      pNewPathField = new ArrayList<JTextField>();
      pNewNameField = new ArrayList<JTextField>();
      Box vbox = new Box(BoxLayout.Y_AXIS);
      
      {
	 Component comps[] = UIFactory.createTitledPanels();
	 JPanel tpanel = (JPanel) comps[0];
	 JPanel vpanel = (JPanel) comps[1];

	 if(pTextureGroupingNode) {
	   JTextField field = 
	     UIFactory.createTitledTextField
	     (tpanel, "Texture Grouping Node", sTSize, 
	      vpanel, pPrimary, sVSize2, "The texture grouping node.");

	   field.setEditable(false);
	 }
	 
	 pTexelValues = new ArrayList<String>();
	 pTexelValues.add("Scanlines");
	 pTexelValues.add("Tiles");
	 
	 pTexelTypeField = UIFactory.createTitledCollectionField(tpanel, 
	    "Texel Layout", sTSize, vpanel, pTexelValues, sVSize2, 
	    "The Texel Layout for the created textures");
	 pTexelTypeField.setSelected("Scanlines");
	 
	 UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	 
	 pByteOrderValues = new ArrayList<String>();
	 pByteOrderValues.add("Little-Endian");
	 pByteOrderValues.add("Big-Endian");
	 
	 pByteOrderField = UIFactory.createTitledCollectionField(tpanel, 
	    "Byte Order", sTSize, vpanel, pByteOrderValues, sVSize2, 
	    "The Byte Order for the created textures");
	 pByteOrderField.setSelected("Little-Endian");


	 JDrawer drawer = new JDrawer("Base Settings", (JComponent) comps[2], true);
	 vbox.add(drawer);
      }
      
      int i = 0;
      for(String orig : pOriginalNodes) {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	
	Path p = new Path(pNewNodes.get(i));

	{
	  JTextField field = 
	    UIFactory.createTitledTextField
	    (tpanel, "Texture Node Name", sTSize, 
	     vpanel, orig, sVSize2, "The texture node. ");

	  field.setEditable(false);
	}
	
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	{
	  JTextField field = 
	    UIFactory.createTitledEditableTextField
	    (tpanel, "Map Node Path", sTSize, 
	     vpanel, p.getParent(), sVSize2,
	     "The Path for the map node.");

	  pNewPathField.add(field);
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	{
	  JTextField field = 
	    UIFactory.createTitledEditableTextField
	    (tpanel, "Map Node Name", sTSize, 
	     vpanel, p.getName(), sVSize2,
	     "The Name (without extention) for the map node.");

	  pNewNameField.add(field);
	}

	JDrawer drawer = new JDrawer(orig, (JComponent) comps[2], true);
	vbox.add(drawer);
	i++;
      }
      
      {
	 JPanel spanel = new JPanel();
	 spanel.setName("Spacer");

	 spanel.setMinimumSize(new Dimension(sTSize+sVSize2, 7));
	 spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	 spanel.setPreferredSize(new Dimension(sTSize+sVSize2, 7));

	 vbox.add(spanel);
      }

      JScrollPane scroll = new JScrollPane(vbox);
      Dimension size = new Dimension(sTSize + sVSize2 + 52, 500);
      scroll.setMinimumSize(size);
      scroll.setPreferredSize(size);
      
      scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      JToolDialog diag = new JToolDialog("Build Lighting Scenes", scroll, "Confirm");
      
      diag.setVisible(true);
      
      if(diag.wasConfirmed()) 
	return ": Creating Map Nodes...";
    }

    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform one phase in the execution of the tool. <P> 
   * 
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @return 
   *   Whether to continue and collect user input for the next phase of the tool.
   * 
   * @throws PipelineException
   *   If unable to sucessfully execute this phase of the tool.
   */
  public synchronized boolean 
  executePhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    int i = 0;
    for (JTextField pathField : pNewPathField) {
      String oldName = pOriginalNodes.get(i);
      JTextField nameField = pNewNameField.get(i);
      String toolset = pToolsets.get(i);
      
      String newName = pathField.getText() + "/" + nameField.getText();
      NodeMod mod = registerNode(mclient, toolset, newName, "map", editorImfDisp());
      mclient.link(pUser, pView, newName, oldName, LinkPolicy.Dependency,
		   LinkRelationship.All, null);

      /* If the primary node is not a texture, the link the mip-map to the
         primary node. */
      if(pTextureGroupingNode) {
	mclient.link(pUser, pView, pPrimary, newName, LinkPolicy.Dependency,
		     LinkRelationship.All, null);
      }

      BaseAction act = actionMRayTexture();
      act.setSingleParamValue("ImageSource", oldName);
      act.setSingleParamValue("TexelLayout", pTexelTypeField.getSelected());
      act.setSingleParamValue("ByteOrder", pByteOrderField.getSelected());
      mod.setAction(act);
      mclient.modifyProperties(pUser, pView, mod);

      if(pRoots.contains(oldName)) {
	pRoots.remove(oldName);

	/* If a texture grouping node exists do not add the new mip-map node 
	   to pRoots. */
	if(!pTextureGroupingNode)
	  pRoots.add(newName);
      }

      i++;
    }

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private NodeMod 
  registerNode
  (
   MasterMgrClient mclient, 
   String toolset, 
   String name,
   String extention, 
   BaseEditor editor
  )
    throws PipelineException
  {
    File f = new File(name);
    FileSeq animSeq = new FileSeq(f.getName(), extention);
    NodeMod animNode = new NodeMod(name, animSeq, null, false, toolset, editor);
    mclient.register(pUser, pView, animNode);
    return animNode;
  }

  private BaseAction 
  actionMRayTexture() 
    throws PipelineException
  {
    PluginMgrClient plug = PluginMgrClient.getInstance();
    //return plug.newAction("MRayTexture", new VersionID("2.0.9"), "Temerity");
    return plug.newAction("MRayTexture", new VersionID("2.2.1"), "Temerity");
  }

  private BaseEditor 
  editorImfDisp() 
    throws PipelineException
  {
    PluginMgrClient plug = PluginMgrClient.getInstance();
    //return plug.newEditor("ImfDisp", new VersionID("2.0.9"), "Temerity");
    return plug.newEditor("ImfDisp", new VersionID("2.2.1"), "Temerity");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8552555872774855875L;

  private static final int sTSize = 150;
  private static final int sVSize2 = 400;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private ArrayList<String> pTextureFormats;
  private ArrayList<String> pTexelValues;
  private ArrayList<String> pByteOrderValues;
  private ArrayList<String> pOriginalNodes;
  private ArrayList<String> pNewNodes;
  private ArrayList<String> pToolsets;
  private ArrayList<JTextField> pNewPathField;
  private ArrayList<JTextField> pNewNameField;
  private JCollectionField pTexelTypeField;
  private JCollectionField pByteOrderField;

  private String pUser;
  private String pView;

  /**
   * Whether the primary node selected can be used as a texture grouping node.
   */
  private boolean  pTextureGroupingNode;

}
