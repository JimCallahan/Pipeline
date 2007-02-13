package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.File;
import java.util.*;

import us.temerity.pipeline.*;

/**
 * This tool builds the turntable network for all assets. <P>
 * 
 * The Tool is designed to be run on either a character, set, or prop that lives 
 * in an 'assets' directory.  It has paths for Lair hardcoded into it, so it will
 * need to be modified it is to be used with other projects. <P>
 * 
 * The class makes use of three pipeline nodes and build two others.
 * The nodes it uses are:
 * <ul>
 * <li> The asset node that was clicked on.  This will be the model that is rendered 
 * in the turntable setup.
 * <li> A Maya scene that contains the turntable camera and light rig.  Currently this is
 * hardcoded to <code>/projects/lr/assets/tt/setups/circ360</code>.
 * <li> A mel script that contains the render globals for rendering the turntables.  
 * Currently this is hardcoded to 
 * <code>/projects/lr/assets/tools/render/turntable-render</code>.</ul><br>
 * It builds the following two nodes.
 * <ul><li> A Maya scene that references in the asset node and the light rig node.
 * <li> An image node that uses the MayaMRayRender Action to render the generated
 * Maya scene, using the provided mel script as the render globals.</ul><br>
 * <p>
 * This tool is currently limited in terms of what it should be.  It should be extended
 * to all for turntables to be built for pretty much any node, in any project.  Selection Keys
 * always become an interesting issue.  There should be a way to make sure that the selection
 * keys are set correctly for all the built nodes.  Currently, they are hardcoded.  There 
 * should be a GUI that allows users to select the following.
 * <ul><li> A turntable setup from a list of all the setups availible in that project. 
 * (All projects will have to store their turntable setups in a similar location, something
 * like <code>/projects/<i>projectname</i>/assets/tt/setups/</code>).  Or, there could just
 * be a directory in globals that contains setups.  Or maybe a combination of both.
 * <li> Which rendering action should be used to render the images/
 * <li> The path to store the images and the tt scene. Should have a reasonable default
 * value.
 * <li> The name that the generated image and maya scene should have.
 * <li> A start, end, and step value for the frame range.  (currently hardcoded to 1-90x1).
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 * 
 */
public class BuildAssetTurntableTool extends BaseTool{

	/*-----------------------------------------------*/
	/*             STATIC INTERNAL VARS              */
	/*-----------------------------------------------*/
	private static final long serialVersionUID = 741681269437250780L;

	/*-----------------------------------------------*/
	/*                  INTERNALS                    */
	/*-----------------------------------------------*/
	private static final String assetPattern = ".*/assets/(character|set|prop)";

	/**
	 * These three are set from the node that the tool was run on.
	 */
	private String pUser;
	private String pView;
	private String pToolset;

	/**
	 * The folder where the maya scene will be put.  The images will be stored
	 * in a subdirectory called img under this path
	 */
	private String folder;
	/**
	 * The render globals mel for the turntable.
	 */
	private String ttMel;
	/**
	 * The setup scene for the turntable that contains the lights and camera.
	 */
	private String cameraName;
	private PluginMgrClient plug;

	private String assetName;
	
	/**
	 * Builds the turntable network for all assets
	 */
	public BuildAssetTurntableTool()
	{
		super("BuildAssetTurntable", new VersionID("1.0.0"), "SCEA",
		"Builds the turntable network for all assets.");

		underDevelopment();

		plug = PluginMgrClient.getInstance();
		
		cameraName = "/projects/lr/assets/tt/setups/circ360";
		ttMel = "/projects/lr/assets/tools/render/turntable-render";
		
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
		
		if(!nPath.toString().matches(assetPattern))
			throw new PipelineException("This tool only works on assets.");

		//folder = status.getName().substring(0,status.getName().length()-assetName.length());
		folder += "/tt/";
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();
		pToolset = status.getDetails().getWorkingVersion().getToolset();
		return "...Couldn't put Humpty together again...";
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
		int endFrame = 90;
		int byFrame = 1;

		/*
		 * Checks out the mel script and the setup scene.
		 */
		mclient.checkOut(pUser, pView, cameraName, null, 
			CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
		mclient.checkOut(pUser, pView, ttMel, null, 
				CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
		
		/*
		 * Uses a default editor.  An explicit Editor designation is probably
		 * better, since this is based on a per-user setting, so you could
		 * get an old value.
		 */
		BaseEditor editor = mclient.getEditorForSuffix(extension);

		/*build the ma node*/
		NodeMod turnMod = registerNode(mclient, folder+assetName+"_tt","ma", 
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
		
		NodeMod imgMod = registerSequence(mclient, folder+"img/"+ assetName+"_tt", pad, 
				extension, editor, startFrame, endFrame, byFrame);
		mclient.link(pUser, pView, imgMod.getName(), turnMod.getName(), LinkPolicy.Dependency,
				LinkRelationship.All, null);
		mclient.link(pUser, pView, imgMod.getName(), ttMel, LinkPolicy.Dependency,
				LinkRelationship.All, null);
		
		VersionID ver = plugs.get("Temerity", "MayaMRayRender").last();
		BaseAction imgAction = plug.newAction("MayaMRayRender", ver, "Temerity");
		imgAction.setSingleParamValue("MayaScene", turnMod.getName());
		imgAction.setSingleParamValue("PreExportMEL", ttMel);
		imgMod.setAction(imgAction);
		imgMod.setExecutionMethod(ExecutionMethod.Parallel);
		imgMod.setBatchSize(3);
		JobReqs jreqs = imgMod.getJobRequirements();
		jreqs.addLicenseKey("MentalRay");
		jreqs.addSelectionKey("MentalRay");
		jreqs.addSelectionKey("Lair");
		jreqs.addSelectionKey("Lighting");
		jreqs.addSelectionKey("LinuxOnly");
		//jreqs.setRampUp(2.5);
		jreqs.setMinDisk(1073741824L);
		jreqs.setMinMemory(1073741824L);
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
