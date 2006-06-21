// $Id: RegisterMapTool.java,v 1.1 2006/06/21 05:25:10 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

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
 * for all the selected image texture nodes. <P> 
 */
public class 
RegisterMapTool 
  extends BaseTool
{
  public 
  RegisterMapTool()
  {
    super("RegisterMap", new VersionID("2.0.9"), "Temerity",
	  "Registers nodes associated with optimized Mental Ray memory mappable " + 
	  "pyramid textures for all the selected image texture nodes.");

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
    
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
    if (pSelected.size() == 0)
      return null;

    pOriginalNodes = new ArrayList<String>();
    pNewNodes = new ArrayList<String>();
    pToolsets = new ArrayList<String>();

    for (String each : pSelected.keySet()) {
      NodeStatus status = pSelected.get(each);
      NodeDetails det = status.getDetails();
      NodeMod mod = det.getWorkingVersion();
      NodeID nodeID = status.getNodeID();
      String suffix = mod.getPrimarySequence().getFilePattern().getSuffix();
      if (pTextureFormats.contains(suffix)) {
	pOriginalNodes.add(each);
	pToolsets.add(mod.getToolset());
	pUser = nodeID.getAuthor();
	pView = nodeID.getView();
	Path p = new Path(each);
	String newName = p.getParentPath().getParent() + "/map/" + p.getName();
	pNewNodes.add(newName);
      }
    }

    if (pOriginalNodes.size() > 0) {
      pNewPathField = new ArrayList<JTextField>();
      pNewNameField = new ArrayList<JTextField>();
      Box vbox = new Box(BoxLayout.Y_AXIS);
      
      int i = 0;
      for (String orig : pOriginalNodes) {
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

	{
	  JTextField field = 
	    UIFactory.createTitledEditableTextField
	    (tpanel, "Map Node Path", sTSize, 
	     vpanel, p.getParent(), sVSize2,
	     "The Path for the map node.");

	  pNewPathField.add(field);
	}

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

      JScrollPane scroll = new JScrollPane(vbox);
      Dimension size = new Dimension(sTSize + sVSize2 + 52, 500);
      scroll.setMinimumSize(size);
      scroll.setPreferredSize(size);
      
      scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      JToolDialog diag = new JToolDialog("Build Lighting Scenes", scroll, "Confirm");
      
      diag.setVisible(true);
      
      if (diag.wasConfirmed()) 
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
      BaseAction act = actionMRayTexture();
      act.setSingleParamValue("ImageSource", oldName);
      mod.setAction(act);
      mclient.modifyProperties(pUser, pView, mod);
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
    NodeMod animNode = new NodeMod(name, animSeq, null, toolset, editor);
    mclient.register(pUser, pView, animNode);
    return animNode;
  }

  private BaseAction 
  actionMRayTexture() 
    throws PipelineException
  {
    PluginMgrClient plug = PluginMgrClient.getInstance();
    return plug.newAction("MRayTexture", new VersionID("2.0.9"), "Temerity");
  }

  private BaseEditor 
  editorImfDisp() 
    throws PipelineException
  {
    PluginMgrClient plug = PluginMgrClient.getInstance();
    return plug.newEditor("ImfDisp", new VersionID("2.0.9"), "Temerity");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7016387231644670393L;

  private static final int sTSize = 120;
  private static final int sVSize2 = 400;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private ArrayList<String> pTextureFormats;
  private ArrayList<String> pOriginalNodes;
  private ArrayList<String> pNewNodes;
  private ArrayList<String> pToolsets;
  private ArrayList<JTextField> pNewPathField;
  private ArrayList<JTextField> pNewNameField;

  private String pUser;
  private String pView;

}
