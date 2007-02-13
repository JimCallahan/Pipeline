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
MakeFBXMayaTool 
  extends BaseTool
{

public 
  MakeFBXMayaTool()
  {
    super("MakeFBXMaya", new VersionID("1.0.0"), "SCEA",  "Creates a Maya node by combining a Maya Skeleton File and an FBX anim");
    
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
    if(pSelected.size() != 2)
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
      String skelNodeName = "";
      //NodeID animNodeID;
      String animNodeName = "";
      //String Dir = "";
      //String animFileName = "file";
      
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
	    	  //skelNodeID = status.getNodeID();
	    	  //skelNodeName = fseq.getPath(0).toString();
	    	  skelNodeName = name;
	    	  pToolset = mod.getToolset();
	      }
	      else if(suffix.equals("fbx"))
	      {
	    	  //animNodeID = status.getNodeID();
	    	  //animFileName = fseq.getPath(0).toString();
	    	  animNodeName = name;
	    	  //Dir = new Path(name).getParent().toString();
	      }
	      else
	      {
	    	  throw new PipelineException ("invalid input file(s)!");
	      }
	      //mclient.modifyProperties(nodeID.getAuthor(), nodeID.getView(), mod);
	  }
	  
      //get animNodeName minus the extension...
	  //String outputName = Dir + "/mb/" + animFileName.replace(".fbx", "");
	  String outputName = animNodeName + "_make";
	  DoubleMap<String, String, TreeSet<VersionID>> plugs = mclient.getToolsetActionPlugins(pToolset);
	  //System.err.println(outputName);
	  
	  //throw new PipelineException (outputName);
      /*build the mb node*/
	  
	  NodeMod outMod;
	  if (!doesNodeExists(mclient, outputName))
	  {
		  outMod = registerNode(mclient, outputName,"mb", mclient.getEditorForSuffix("mb"));
	  }
	  else
	  {
		  outMod = mclient.getWorkingVersion(pUser, pView, outputName);
	  }
	 
		mclient.link(pUser, pView, outMod.getName(), skelNodeName, LinkPolicy.Reference,
			LinkRelationship.All, null);
		mclient.link(pUser, pView, outMod.getName(), animNodeName, LinkPolicy.Dependency,
				LinkRelationship.All, null);
		
		//apply action...
		VersionID ttVer = plugs.get("SCEA", "FBXImport").last();
		
		BaseAction action = plug.newAction("FBXImport", ttVer, "SCEA");
		action.setSingleParamValue("MayaSkeleton", skelNodeName);
		action.setSingleParamValue("FBXAnim", animNodeName);
		outMod.setAction(action);
		mclient.modifyProperties(pUser, pView, outMod);
		
//		register the anim ma file...
		String exportedAnimName = animNodeName + "_export";
		NodeMod animMod;
		  if (!doesNodeExists(mclient, exportedAnimName))
		  {
			  animMod = registerNode(mclient, exportedAnimName,"ma", mclient.getEditorForSuffix("ma"));
		  }
		  else
		  { 
			  animMod = mclient.getWorkingVersion(pUser, pView, exportedAnimName);
		  }
		  
		mclient.link(pUser, pView, exportedAnimName, outputName, LinkPolicy.Dependency,
					LinkRelationship.All, null);
		
		BaseAction expAction = plug.newAction("ExportAnimRef", ttVer, "SCEA");
		expAction.setSingleParamValue("MayaScene", outputName);
		animMod.setAction(expAction);
		mclient.modifyProperties(pUser, pView, animMod);
		
//register the master shot node...
		String masterShotName = animNodeName + "_master";
		NodeMod masterMod;
		if (!doesNodeExists(mclient, masterShotName))
		  {
			  masterMod = registerNode(mclient, masterShotName,"mb", mclient.getEditorForSuffix("mb"));
		  }
		  else
		  {
			  masterMod = mclient.getWorkingVersion(pUser, pView, masterShotName);
		  }
		
		mclient.link(pUser, pView, masterShotName, exportedAnimName, LinkPolicy.Reference,
				LinkRelationship.All, null);
		mclient.link(pUser, pView, masterShotName, skelNodeName, LinkPolicy.Reference,
				LinkRelationship.All, null);
						
		BaseAction masterAction = plug.newAction("ConnectReferencedAnim", ttVer, "SCEA");
		masterAction.setSingleParamValue("Skeleton", skelNodeName);
		masterAction.setSingleParamValue("Anim", exportedAnimName);
		masterMod.setAction(masterAction);
		mclient.modifyProperties(pUser, pView, masterMod);
		
		//run the job...
		masterMod.setActionEnabled(true);
		mclient.submitJobs(pUser, pView, masterMod.getName(), null);
		masterMod.setActionEnabled(false);

    return false;
  }

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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/


  private String pUser;
  private String pView;
  private String pToolset;
  private PluginMgrClient plug;
  private static final long serialVersionUID = -1910518111724074593L;
}
