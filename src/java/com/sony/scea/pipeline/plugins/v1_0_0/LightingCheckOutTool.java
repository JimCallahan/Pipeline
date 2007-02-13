package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import us.temerity.pipeline.*;

/**
 * Special checkout for Lighting. 
 * <p>
 * This ensures that the lighter is working with the latest animation and hi-res models.
 * Updates all the lighting nodes to the latest default toolset as well.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class LightingCheckOutTool extends BaseTool{

	/*-----------------------------------------------*/
	/*             STATIC INTERNAL VARS              */
	/*-----------------------------------------------*/
	private static final long serialVersionUID = 6443317277818678959L;
	
	/*-----------------------------------------------*/
	/*                  INTERNALS                    */
	/*-----------------------------------------------*/
	private static String switchPattern = ".*/production/.*/lgt/.*_switch.*";
//	private static String animPattern = ".*/production/.*/anim/.*_anim";
	private static String lgtPattern = ".*/production/.*/lgt/.*_lgt";
//	private String basicLights = "/projects/lr/assets/lights/testRigs/basicLights";

	public static final CheckOutMode over = CheckOutMode.OverwriteAll;
	public static final CheckOutMode keep = CheckOutMode.KeepModified;
	public static final CheckOutMethod froz = CheckOutMethod.AllFrozen;
	public static final CheckOutMethod frozU = CheckOutMethod.FrozenUpstream;
	public static final CheckOutMethod pFroz = CheckOutMethod.PreserveFrozen;


	/**
	 * The current working area user|view|toolset.
	 */
	private String pUser;
	private String pView;
	private String pToolset;

//	private PluginMgrClient plug;

	public LightingCheckOutTool(){

		super("LightingCheckOut", new VersionID("1.0.0"), "SCEA",
				"Special checkout for Lighting. This ensures that " +
				"the lighter is working with the latest animation and " +
		"hi-res models.");

		/*pLoresSrcs = new TreeSet<String>();
		pHiresSrcs = new TreeSet<String>();
		err=null;*/
		
//		plug = PluginMgrClient.getInstance();

		underDevelopment();
		addSupport(OsType.MacOS);
		addSupport(OsType.Windows);
	}//end constructor

	/**
	 * Check that the user has properly selected a target node for this tool
	 * <P>
	 * 
	 * @return The phase progress message or <CODE>null</CODE> to abort early.
	 * @throws PipelineException
	 *         If unable to validate the given user input.
	 */
	public synchronized String collectPhaseInput() throws PipelineException {
		if((pPrimary==null)||(pSelected.size()!=1))
			throw new PipelineException("Please selected one node only.");

		if(!pPrimary.matches(lgtPattern))
			throw new PipelineException("This tool will only work on a lgt node!");

		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();

		return "...Oh Happy Day!";
	}

	/**
	 * Perform execution of the tool.
	 * <P>
	 * 
	 * @param mclient
	 *        The network connection to the plmaster(1) daemon.
	 * @param qclient
	 *        The network connection to the plqueuemgr(1) daemon.
	 * @return Whether to continue and collect user input for the next phase of
	 *         the tool.
	 * @throws PipelineException
	 *         If unable to sucessfully execute this phase of the tool.
	 */
	public synchronized boolean executePhase(MasterMgrClient mclient, 
			QueueMgrClient qclient) throws PipelineException 
	{
//		DoubleMap<String, String, TreeSet<VersionID>> plugs = null;
		NodeStatus status = pSelected.get(pPrimary); 

		String switchName = null;

		{
			/*-check out the switch and anim nodes-*/
			OverallNodeState state = status.getDetails().getOverallNodeState();
			if(!state.equals(OverallNodeState.Modified))
				jcheckOut(mclient,pUser, pView, pPrimary, null, keep, pFroz);
			
			NodeMod lgtMod = mclient.getWorkingVersion(pUser,pView,pPrimary);
			
			pToolset = mclient.getDefaultToolsetName();
//			plugs = mclient.getToolsetActionPlugins(pToolset);
			
			Set<String> lgtSrcs = lgtMod.getSourceNames();
			for(String src: lgtSrcs){
				if(src.matches(switchPattern)){
					switchName = src;
					break;
				}
			}//end for
			System.err.println("switch node: "+switchName);
			if(switchName==null)
				throw new PipelineException("This lgt node does not have an attached switch node");
			
			/*-if the frame range is set to 30fps, cool, else set it to 30fps-*/
			
			{
				lgtMod = mclient.getWorkingVersion(pUser,pView,pPrimary);
				if(!lgtMod.getToolset().equals(pToolset)){
					lgtMod.setToolset(pToolset);
					mclient.modifyProperties(pUser, pView, lgtMod);
				}
//				BaseAction action = lgtMod.getAction();
//				if((action==null)||(!action.getName().equals("MayaBuild"))){
//					VersionID ttVer = plugs.get("Temerity", "MayaBuild").last();
//					action = plug.newAction("MayaBuild", ttVer, "Temerity");
//					action.initSourceParams(switchName);
//					action.setSourceParamValue(switchName, "PrefixName", "switch");
//				}
//				
//				String param = (String) action.getSingleParamValue("TimeUnits");
//				if(!param.equals("NTSC (30 fps)")){
//					action.setSingleParamValue("TimeUnits", "NTSC (30 fps)");
//				} 
//				
//				lgtMod.setAction(action);
//				lgtMod.setActionEnabled(false);
//				
//				JobReqs jreqs = lgtMod.getJobRequirements();
//				if(!jreqs.getSelectionKeys().contains("LinuxOnly"))
//					jreqs.addSelectionKey("LinuxOnly");
//				lgtMod.setJobRequirements(jreqs);
//				mclient.modifyProperties(pUser, pView, lgtMod);
			}	
				
		}


		System.err.println("Check-out");
		TreeMap<String, TreeSet<Long>> resp = jcheckOut(mclient,pUser, pView, switchName, null, over, froz);
		if(resp!=null){
			System.err.println(resp);
			jcheckOut(mclient,pUser, pView, switchName, null, keep, pFroz);
			/*{			
				NodeMod switchMod = mclient.getWorkingVersion(pUser, pView, switchName);
				TreeSet<String> switchSrcs = new TreeSet<String>(switchMod.getSourceNames());
				String animName = null;
				for(String src: switchSrcs){
					if(src.matches(animPattern)){
						animName = src;
						jcheckOut(mclient,pUser, pView, src, null, keep, pFroz);
						break;
					}//end if
				}//end for
				System.err.println("anim node: "+animName);
				if(animName==null)
					throw new PipelineException("This switch node does not have an associated anim node");
			}*/
		}

		return false;
	}//end executePhase	

	  private TreeMap<String, TreeSet<Long>> jcheckOut(MasterMgrClient mclient, String user, String view, String name,
	      VersionID id, CheckOutMode mode, CheckOutMethod method) throws PipelineException
	  {
	    if (id == null)
	      id = mclient.getCheckedInVersionIDs(name).last();
	    if (id == null)
	      throw new PipelineException("BAD BAD BAD");
	    return mclient.checkOut(user, view, name, id, mode, method);
	  }
	

}//end class
