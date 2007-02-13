package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.File;
import java.util.*;

import us.temerity.pipeline.*;

/**
 * Another variation of the {@link BuildAssetTurntableTool} tool which should
 * be combined into that tool.
 * <p>
 * This one creates 4K still frames from 4 different camera angles.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 * 
 */
public class RenderAssetStillsTool extends BaseTool{

	/*-----------------------------------------------*/
	/*             STATIC INTERNAL VARS              */
	/*-----------------------------------------------*/

	private static final long serialVersionUID = 1843415326766768166L;

	/*-----------------------------------------------*/
	/*                  INTERNALS                    */
	/*-----------------------------------------------*/
	private static final String assetPattern = ".*/assets/(character|set|prop)";

	private String pUser;
	private String pView;
	private String pToolset;

	private String folder;
	private String stillMel;
	private String cameraName;
	private PluginMgrClient plug;

	private String assetName;
	
	/**
	 * Builds the turntable network for all assets
	 */
	public RenderAssetStillsTool()
	{
		super("Render Asset Stills", new VersionID("1.0.0"), "SCEA",
		"Builds the network to render 4K stills of an assets.");

		underDevelopment();

		plug = PluginMgrClient.getInstance();
		
		cameraName = "/projects/lr/assets/tt/setups/still4K";
		stillMel = "/projects/lr/assets/tools/render/render-still4K";
		
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
		if ( pPrimary == null )
			throw new PipelineException("The primary selection must be the Target Node!");

		if ( pSelected.size() != 1 )
			throw new PipelineException("Only one Target Node may be selected.");
		
		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		
		Path nPath = new Path(pPrimary);
		folder = nPath.getParent();
		assetName = nPath.getName();
		nPath = nPath.getParentPath().getParentPath();
		
		if(!nPath.toOsString().matches(assetPattern))
			throw new PipelineException("This tool only works on assets.");

		//folder = status.getName().substring(0,status.getName().length()-assetName.length());
		folder += "/tt/";
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();
		pToolset = status.getDetails().getWorkingVersion().getToolset();
		return "...All the king's horses and all the king's men...";
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
	@Override
	public synchronized boolean executePhase(MasterMgrClient mclient, 
		QueueMgrClient qclient) throws PipelineException 
	{
		DoubleMap<String, String, TreeSet<VersionID>> plugs = mclient
		.getToolsetActionPlugins(pToolset);
		String extension = "iff";
		int pad = 4;
		int startFrame = 1;
		int endFrame = 5;
		int byFrame = 1;

		
		mclient.checkOut(pUser, pView, cameraName, null, 
			CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
		mclient.checkOut(pUser, pView, stillMel, null, 
				CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);		
		BaseEditor editor = mclient.getEditorForSuffix(extension);

		/*build the ma node*/
		NodeMod turnMod = registerNode(mclient, folder+assetName+"_stills","ma", 
			mclient.getEditorForSuffix("ma"));
		mclient.link(pUser, pView, turnMod.getName(), pPrimary, LinkPolicy.Reference,
			LinkRelationship.All, null);
		mclient.link(pUser, pView, turnMod.getName(), cameraName, LinkPolicy.Reference,
				LinkRelationship.All, null);
		
		VersionID ttVer = plugs.get("Temerity", "MayaBuild").last();
		BaseAction action = plug.newAction("MayaBuild", ttVer, "Temerity");
		action.setSingleParamValue("TimeUnits", "NTSC (30 fps)");
		action.setSingleParamValue("StartFrame", startFrame);
		action.setSingleParamValue("EndFrame", endFrame);
		action.initSourceParams(pPrimary);
		action.setSourceParamValue(pPrimary,"BuildType", "Reference");
		action.setSourceParamValue(pPrimary,"NameSpace", true);
		action.setSourceParamValue(pPrimary,"PrefixName", "asset");
		
		action.initSourceParams(cameraName);
		action.setSourceParamValue(cameraName,"BuildType", "Reference");
		action.setSourceParamValue(cameraName,"NameSpace", true);
		action.setSourceParamValue(cameraName,"PrefixName", "circ360");
		turnMod.setAction(action);
		mclient.modifyProperties(pUser, pView, turnMod);
		turnMod.setActionEnabled(true);
		mclient.submitJobs(pUser, pView, turnMod.getName(), null);
		turnMod.setActionEnabled(false);
		
		NodeMod imgMod = registerSequence(mclient, folder+"img/"+ assetName+"_stills", pad, 
				extension, editor, startFrame, endFrame, byFrame);
		mclient.link(pUser, pView, imgMod.getName(), turnMod.getName(), LinkPolicy.Dependency,
				LinkRelationship.All, null);
		mclient.link(pUser, pView, imgMod.getName(), stillMel, LinkPolicy.Dependency,
				LinkRelationship.All, null);
		
		VersionID ver = plugs.get("Temerity", "MayaMRayRender").last();
		BaseAction imgAction = plug.newAction("MayaMRayRender", ver, "Temerity");
		imgAction.setSingleParamValue("MayaScene", turnMod.getName());
		imgAction.setSingleParamValue("PreExportMEL", stillMel);
		imgMod.setAction(imgAction);
		imgMod.setExecutionMethod(ExecutionMethod.Parallel);
		imgMod.setBatchSize(1);
		JobReqs jreqs = imgMod.getJobRequirements();
		jreqs.addLicenseKey("MentalRay");
		jreqs.addSelectionKey("MentalRay");
		jreqs.addSelectionKey("Lair");
		jreqs.addSelectionKey("Lighting");
		jreqs.addSelectionKey("LinuxOnly");
		//jreqs.setRampUp(2.5);
		jreqs.setMinDisk(1073741824L);
		jreqs.setMinMemory(3221225472L);
		imgMod.setJobRequirements(jreqs);
		mclient.modifyProperties(pUser, pView, imgMod);		
		
		if(pRoots.contains(pPrimary)){
			pRoots.remove(pPrimary);
			pRoots.add(imgMod.getName());
		}
		
		return false;
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

	public NodeMod registerSequence(MasterMgrClient mclient, String name, int pad, 
			String extention, BaseEditor editor, int startF, int endf, int byF) 
	throws PipelineException
	{
		Path p = new Path(name);
		FilePattern pat = new FilePattern(p.getName(), pad, extention);
		FrameRange range = new FrameRange(startF, endf, byF);
		FileSeq animSeq = new FileSeq(pat, range);
		NodeMod animNode = new NodeMod(name, animSeq, null, pToolset, editor);
		mclient.register(pUser, pView, animNode);
		return animNode;
	}//end registerSequence
	
}
