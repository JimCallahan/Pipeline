package com.sony.scea.pipeline.plugins.v1_0_0;


import java.io.File;
import java.util.TreeMap;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.NodeTreeComp.State;

/*------------------------------------------------------------------------------------------*/
/*   D I S A B L E   A C T I O N   T O O L                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Disables the actions on all of the selected nodes.
 */
public class 
MakeMatteRenderTool 
  extends BaseTool
{
	
public 
  MakeMatteRenderTool()
  {
    super("MakeMatteRender", new VersionID("1.0.0"), "SCEA",  "Creates a Maya node by combining camera animation and matte painting");
    
    underDevelopment();
    plug = PluginMgrClient.getInstance();
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }
  
  public NodeMod registerNode(MasterMgrClient mclient, String name, 
			String extension,BaseEditor editor) throws PipelineException
	{
		File f = new File(name);
		FileSeq fSeq = new FileSeq(f.getName(), extension);
		NodeMod animNode = new NodeMod(name, fSeq, null, pToolset, editor);
		mclient.register(pUser, pView, animNode);
		return animNode;
	}//end registerNode
  
  public boolean doesNodeExists(MasterMgrClient mclient, String name) throws PipelineException
	{
		TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
		comps.put(name, false);
		NodeTreeComp treeComps = mclient.updatePaths(pUser, pView, comps);
		State state = getState(treeComps, name);
		if ( state == null || state.equals(State.Branch) )
			return false;
		return true;
	}
  
private static State getState(NodeTreeComp treeComps, String scene)
	{
		State toReturn = null;
		Path p = new Path(scene);
		NodeTreeComp dest = null;
		for (String s : p.getComponents())
		{
			if ( dest == null )
				dest = treeComps.get(s);
			else
				dest = dest.get(s);

			if ( dest == null )
				break;
		}
		if ( dest != null )
			toReturn = dest.getState();
		return toReturn;
	}
  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Create and show graphical user interface components to collect information from the 
   * user to use as input in the next phase of execution for the tool. <P> 
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
    if(pSelected.size() != 1)
      return null;
    
    NodeStatus status = pSelected.get(pPrimary);
	NodeID nodeID = status.getNodeID();
    pUser = nodeID.getAuthor();
	pView = nodeID.getView();
	
    return ": Creating Maya File...";
  }

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
	/*
    for(String name : pSelected.keySet()) {
      NodeStatus status = pSelected.get(name);
      NodeID nodeID = status.getNodeID();
      NodeMod mod = mclient.getWorkingVersion(nodeID);
      mod.setActionEnabled(false);
      mclient.modifyProperties(nodeID.getAuthor(), nodeID.getView(), mod);
    }
    */
	  //NodeID skelNodeID;
      String srcAnimName = "";
      NodeID animNodeID = null;
      String animNodeName = "";
      //String Dir = "";
      String animFileName = "";
  
	  for(String name : pSelected.keySet())
	  {
		  //status of current node iterated...
	      NodeStatus status = pSelected.get(name);
	      //NodeID nodeID = status.getNodeID();
	      
	      //NodeMod mod = mclient.getWorkingVersion(nodeID);
	      NodeMod mod = status.getDetails().getWorkingVersion();
	      
	      
	      //mod.setActionEnabled(false);
	      //check to see if this is the anim node or the skeleton node...
	      FileSeq fseq = mod.getPrimarySequence();
	      String suffix = fseq.getFilePattern().getSuffix();
	      
	      if(suffix.equals("ma") || suffix.equals("mb"))
	      {
	    	  animNodeID = status.getNodeID();
	    	  //animFileName = fseq.get.getFile(0).toString();
	    	  
	    	  if (name.matches(animPattern))
	    	  {
	    		  srcAnimName = name;
	    		  pToolset = mod.getToolset();
	    	  }
	    	  else
	    	  {
	    		  throw new PipelineException ("input is not an animation file!");
	    	  }
	      }
	      else
	      {
	    	  throw new PipelineException ("source is not a maya file(s)!");
	      }
	      //mclient.modifyProperties(nodeID.getAuthor(), nodeID.getView(), mod);
	  }
      
      
      //get animNodeName minus the extension...
	  //String outputName = Dir + "/mb/" + animFileName.replace(".fbx", "");
	  if (animNodeID == null)
	  {
		  throw new PipelineException ("something went wrong it's probably your fault!");
	  }
	  //String camNodeName = animNodeID.getParent() + "/cam/" + animFileName.replace("_anim", "_cam");
	  String camNodeName = srcAnimName.replace("/anim", "/anim/cam");
	  camNodeName = camNodeName.replace("_anim", "_cam");
	  
	  String matteNodeName = "/projects/lr/assets/mattes";
	  String matteFile = srcAnimName.replace("projects/lr/production/lair/", "");
	  matteFile = matteFile.replace("/anim","");
	  matteFile = matteFile.replace("_anim", "_mattes");
	  matteNodeName += matteFile;
	  String rootName = srcAnimName.replace("/anim","/lgt");
	  rootName = rootName.replace("_anim", "_matte");
	  int startFrame = 0;
	  int endFrame = 30;
	  //use the image sequence file to figure out proper frame ranges...
	  String testImgSeq = srcAnimName.replace("/anim", "/img/test");
	  testImgSeq = testImgSeq.replace("_anim", "");
	  //System.err.println(testImgSeq);
	  if ( doesNodeExists(mclient, testImgSeq) )
	  {
		  //System.err.println("i have a test image sequence...");
		  NodeVersion testMod = mclient.getCheckedInVersion(testImgSeq, null);
		  //jcheckOut(mclient, pUser, pView, testImgSeq, null, CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
		  FileSeq testSeq = testMod.getPrimarySequence();
		  
		  startFrame = testSeq.getFrameRange().getStart();
		  endFrame = testSeq.getFrameRange().getEnd();
	  }

	  //System.err.println(startFrame);
	  //System.err.println(endFrame);
	  
	  DoubleMap<String, String, TreeSet<VersionID>> plugs = mclient.getToolsetActionPlugins(pToolset);
	  //System.err.println(camNodeName);
	  
	  //throw new PipelineException (outputName);
      /*build the cam ma node*/
	  //NodeMod matteMod = registerNode(mclient, camNodeName,"ma", mclient.getEditorForSuffix("ma"));
	  	//jcheckOut(mclient, pUser, pView, matteNodeName, null, CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	  /*
	  	try
	    {
	  		jcheckOut(mclient, pUser, pView, matteNodeName, null, CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	    } catch ( PipelineException ex )
	    {
	      throw new PipelineException("this shot does not have a matte file - exiting\n" + ex.getMessage());
	    }
	    */
	  if ( doesNodeExists(mclient, matteNodeName) )
	  {
		  jcheckOut(mclient, pUser, pView, matteNodeName, null, CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
	  }
	  else
	  {
		  throw new PipelineException("this shot does not have a matte file - exiting\n");
	  }
	  
		NodeMod camMod = registerNode(mclient, camNodeName,"ma", mclient.getEditorForSuffix("ma"));
		
		mclient.link(pUser, pView, camMod.getName(), srcAnimName, LinkPolicy.Reference,
			LinkRelationship.All, null);
		//mclient.link(pUser, pView, outMod.getName(), animNodeName, LinkPolicy.Reference,
		//		LinkRelationship.All, null);
	
		
		//apply action...
		VersionID ttVer = plugs.get("SCEA", "CameraExport").last();
		
		BaseAction action = plug.newAction("CameraExport", ttVer, "SCEA");
		action.setSingleParamValue("MayaSource", srcAnimName);
		//action.setSingleParamValue("FBXAnim", animNodeName);
		camMod.setAction(action);
		mclient.modifyProperties(pUser, pView, camMod);
		
		NodeMod rootMod = registerNode(mclient, rootName, "ma", mclient.getEditorForSuffix("ma"));
		mclient.link(pUser, pView, rootMod.getName(), camNodeName, LinkPolicy.Reference, LinkRelationship.All, null);
		mclient.link(pUser, pView, rootMod.getName(), matteNodeName, LinkPolicy.Reference, LinkRelationship.All, null);
		
		ttVer = plugs.get("Temerity", "MayaBuild").last();
		BaseAction rootAction = plug.newAction("MayaBuild", ttVer, "Temerity");
		//action.setSingleParamValue("MayaSource", srcAnimName);
		rootAction.setSingleParamValue("TimeUnits", "NTSC (30 fps)");
		rootAction.setSingleParamValue("StartFrame", startFrame);
		rootAction.setSingleParamValue("EndFrame", endFrame);
		
		rootAction.initSourceParams(camNodeName);
		rootAction.setSourceParamValue(camNodeName, "PrefixName", "cam");
		rootAction.setSourceParamValue(camNodeName, "BuildType", "Reference");
		rootAction.setSourceParamValue(camNodeName, "NameSpace", true);
		
		rootAction.initSourceParams(matteNodeName);
		rootAction.setSourceParamValue(matteNodeName, "PrefixName", "cam");
		rootAction.setSourceParamValue(matteNodeName, "BuildType", "Reference");
		rootAction.setSourceParamValue(matteNodeName, "NameSpace", true);
		
		//action.setSingleParamValue("FBXAnim", animNodeName);
		rootMod.setAction(rootAction);
		mclient.modifyProperties(pUser, pView, rootMod);
		
		//run the job...
		rootMod.setActionEnabled(true);
		mclient.submitJobs(pUser, pView, rootMod.getName(), null);
		rootMod.setActionEnabled(false);
   
    return false;
  }
  
  private void jcheckOut(MasterMgrClient mclient, String user, String view, String name,
      VersionID id, CheckOutMode mode, CheckOutMethod method) throws PipelineException
  {
    if (id == null)
      id = mclient.getCheckedInVersionIDs(name).last();
    if (id == null)
      throw new PipelineException("BAD BAD BAD");
    mclient.checkOut(user, view, name, id, mode, method);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/


  private String pUser;
  private String pView;
  private String pToolset;
  private PluginMgrClient plug;
  private static final long serialVersionUID = -7182736440382341785L;
  private final String animPattern = "/projects/.*/production/.*/anim/.*_anim";
}
