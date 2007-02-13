package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JPanel;

import us.temerity.pipeline.*;
import us.temerity.pipeline.NodeTreeComp.State;
import us.temerity.pipeline.ui.JConfirmDialog;
import us.temerity.pipeline.ui.JToolDialog;

/**
 * This tool allows the user to build a compositing render tree for a selected 
 * lighting node.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class BuildShakePreCompTool extends BaseTool {

	private String lgtPattern = ".*/production/.*/lgt/.*_lgt";
	private String imgPattern = ".*/production/.*/img/.*";
	private String prePattern = ".*/production/.*/comp/.*_pre";
	private String pUser;
	private String pView;
	private String shotFolder;
	private String pToolset;
	private String compFolder;
	
	private PluginMgrClient plug;
	
	private String filePrefix;

	private final String CH = "ch";
	private final String CHOCC = "chOcc";
	private final String ENVOCC = "envOcc";
	private final String MV = "mv";
	private final String ZDOF = "z";
	private final String SKY = "sky";
	private final String SHDW = "shdw";
	private final String FG = "fg";
	private final String MG = "mg";
	private final String BG = "bg";

	/**
	 * List of nodes in the pass, indexed by the name of the pass
	 */
	private TreeMap<String, String> nameToNodeNameMap;
	private TreeMap<String, FrameRange> passRange;
	
	private String workingArea;
	private String preName;

	private static final long serialVersionUID = 1716285116636717207L;

	
	public BuildShakePreCompTool(){
		super("BuildShakePreComp", new VersionID("1.0.0"), "SCEA", "This tool creates the " +
		"pre-comp shake scripts based on the rendered images of the selected lighting node.");

		//err = null;
		nameToNodeNameMap = new TreeMap<String, String>();
		passRange = new TreeMap<String, FrameRange>();

		plug = PluginMgrClient.getInstance();
		//addSupport(OsType.MacOS);
		//addSupport(OsType.Windows);

		underDevelopment();
	}

	/**
	 * Check that the user has properly selected a target node for this tool <P>
	 * 
	 * @return 
	 *   The phase progress message or <CODE>null</CODE> to abort early.
	 * 
	 * @throws PipelineException
	 *   If unable to validate the given user input.
	 */
	public synchronized String collectPhaseInput() throws PipelineException
	{
		if(pPrimary==null)
			throw new PipelineException("Please select something!");

		if(pSelected.size()!=1)
			throw new PipelineException("Only one node can be selected!");

		if(!(pPrimary.matches(lgtPattern)||pPrimary.matches(prePattern)||pPrimary.matches(imgPattern) ) )
			throw new PipelineException("This tool only works on lgt nodes.");

		boolean offImage = false;;
		
		if (pPrimary.matches(imgPattern))
		  offImage = true;
		
		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();

		plug = PluginMgrClient.getInstance();

		Path nPath = new Path(pPrimary);
		String nodeName = nPath.getName();
		
		if (offImage)
		  shotFolder = nPath.getParentPath().getParentPath().getParent();
		else
		  shotFolder = nPath.getParentPath().getParent();
		workingArea = nodeID.getWorkingPath().toString().replaceAll(pPrimary,"");		
		compFolder = shotFolder+"/comp";
		
		if(pPrimary.matches(lgtPattern)){
			filePrefix = nodeName.replaceAll("seq","s");
			filePrefix = filePrefix.replaceAll("lgt", "");
			preName = compFolder+"/"+filePrefix+"pre";
		}else if (pPrimary.matches(prePattern)){
			//filePrefix = lgtNodeName.replaceAll("seq","s");
			filePrefix = nodeName;
			filePrefix = filePrefix.replaceAll("pre", "");
			preName = pPrimary;
		} else if (pPrimary.matches(imgPattern))
		{
		  String pieces[] =  nodeName.split("_");
		  filePrefix = nodeName.replaceAll(pieces[2], "");
		  preName = compFolder + "/" + filePrefix + "pre";
		}

		compFolder = shotFolder + "/comp";
		
		System.err.println("File prefix is "+filePrefix+" and the folder is "+ shotFolder);
		System.err.println("The working area is "+workingArea);
		System.err.println("Prename is "+ preName);

		return "...Joy comes in the morning.";
	}//end collectPhaseInput


	/**
	 * Perform execution of the tool.<P>
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
	public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
	throws PipelineException
	{
		pToolset = mclient.getDefaultToolsetName();		
		
		String movName = null;
		Integer startFrame = null;
		Integer endFrame = null;
		Integer byFrame = null;

		{
			/*--get location of all layers--*/
			String imgFolder = shotFolder + "/img";


			ArrayList<String> imgFolders = getChildrenDirs(mclient, imgFolder);
			for(String layer: imgFolders){
				System.err.println(layer);
				String layerNodeName = imgFolder+"/"+layer+"/"+filePrefix+layer;
				System.err.println("The whole path is "+layerNodeName);
				NodeMod lyrMod = null;

				if(layer.equals("test"))	
					continue;


				if(doesNodeExists(mclient, layerNodeName)){
					try{
						lyrMod = mclient.getWorkingVersion(pUser, pView, layerNodeName);
					} catch (PipelineException e){
					  	try {
					  	  System.err.println("\tTrying to Lock node");
					  	  TreeMap<VersionID, NodeVersion> versions = mclient.getAllCheckedInVersions(layerNodeName);
					  	mclient.lock(pUser, pView, layerNodeName, versions.lastKey());
						lyrMod = mclient.getWorkingVersion(pUser, pView, layerNodeName);
					  	} catch (PipelineException ex1)
					  	{
					  	  System.err.println("\tUnable to lock: skipping");
					  	  ex1.printStackTrace();
					  	  continue;
					  	}
					}

					//if(lyrMod.getSourceNames().contains(pPrimary)){
						FrameRange fRange = lyrMod.getPrimarySequence().getFrameRange();
						if(startFrame==null)
							startFrame = fRange.getStart();

						if((endFrame==null) || (endFrame<fRange.getEnd()))
							endFrame = fRange.getEnd();

						if(byFrame==null)
							byFrame = fRange.getBy();
						
						nameToNodeNameMap.put(layer,layerNodeName);
						System.err.println("\tAdded to Map");
						passRange.put(layer, fRange);
					//}
				}//node Exists	
			}
		}

		System.err.println(nameToNodeNameMap);
		
		if(nameToNodeNameMap.isEmpty())
			return false;

		{
			NodeMod preMod = null;
			boolean rebuild = false; 
			movName = filePrefix.substring(0,filePrefix.length()-1);
			
			if(doesNodeExists(mclient, preName)){
				try {
					preMod = mclient.getWorkingVersion(pUser, pView, preName);
				} catch (PipelineException e){
				  	jcheckOut(mclient, pUser, pView, preName, null, CheckOutMode.KeepModified, 
							CheckOutMethod.PreserveFrozen);
					preMod = mclient.getWorkingVersion(pUser, pView, preName);
				}
				
				// Got to do the linking before we check the status.
				System.err.println("Got the precomp script, ready to go.");
				
				for(String layerPath: nameToNodeNameMap.values()){
					System.err.println(layerPath);
					mclient.link(pUser, pView, preMod.getName(), layerPath, LinkPolicy.Reference, 
							LinkRelationship.All, null);
				}
				
				OverallNodeState state = mclient.status(pUser, pView, preName).getDetails().getOverallNodeState();
				System.err.println(state);
				
				JToolDialog tool = new JToolDialog("BuildShakePreComp", new JPanel(), "Continue");
				if (!state.equals(OverallNodeState.Identical)) {
					JConfirmDialog dialog = new JConfirmDialog(tool,
							"There is already a shake script. Do you want to rebuild the script too?");
					dialog.setVisible(true);
					rebuild = dialog.wasConfirmed();
				}// end if
			} else {
				preMod = createNode(compFolder, startFrame, endFrame, byFrame, mclient, qclient);

				for(String layerPath: nameToNodeNameMap.values()){
				  System.err.println(layerPath);
				  mclient.link(pUser, pView, preMod.getName(), layerPath, LinkPolicy.Reference, 
				    LinkRelationship.All, null);
				}
				rebuild = true;
			}
					
			
			if(rebuild) {
				if(preMod.isActionEnabled()){
					preMod.setActionEnabled(false);
					mclient.modifyProperties(pUser, pView, preMod);
				}
				String shkPrefix = compFolder + "/" + filePrefix;
				String movFolder = compFolder + "/mov/";
				String imgRender = compFolder + "/img/"+filePrefix+"comp";
				System.err.println("The shake file will be "+shkPrefix + "pre.shk" + " in " + compFolder);
				writeShakeScript(workingArea + compFolder, workingArea+movFolder, mclient, startFrame, 
						endFrame, byFrame, imgRender);
			}
			pRoots.remove(pPrimary);
			pRoots.add(preName);
		}

		return false;

	}//end executePhase


	private NodeMod createNode(String compFolder, int startFrame, int endFrame, int byFrame, MasterMgrClient mclient,
			QueueMgrClient qclient) 
	throws PipelineException {

		DoubleMap<String, String, TreeSet<VersionID>> plugs = mclient
		.getToolsetActionPlugins(pToolset);
		
		BaseEditor shkEditor = mclient.getEditorForSuffix("shk");

		NodeMod preMod = null;
		{
			preMod = registerNode(mclient, compFolder+"/"+filePrefix+"pre",	"shk", shkEditor);
			
			VersionID tVer = plugs.get("Temerity", "Touch").last();
			BaseAction tchAction = plug.newAction("Touch", tVer, "Temerity");		
			preMod.setAction(tchAction);
			mclient.modifyProperties(pUser, pView, preMod);
			QueueJobGroup jobGroup = mclient.submitJobs(pUser, pView, preMod.getName(), null);
			
			boolean next = false;
			while (!next)
			{
				next = queueAndWait(mclient, qclient, jobGroup);
			}
			preMod.setActionEnabled(false);
			mclient.modifyProperties(pUser, pView, preMod);
			pRoots.add(preMod.getName());
			pRoots.remove(pPrimary);
		}
		
		return preMod;

	}//end createNode


	/*
	 */
	private void writeShakeScript( String compFolder, String movFolder, MasterMgrClient mclient, 
			int startFrame, int endFrame, int byFrame, String outRender) 
	throws PipelineException {

		File script = null;
		File shkScript = null;
		String extension = "iff";
		Path wdir = null;

		try {  
			wdir = new Path(PackageInfo.sProdPath.toOsString());
			
			script = File.createTempFile("BuildShakePreComp-Copy.",".bash",PackageInfo.sTempPath.toFile());
			shkScript = File.createTempFile("BuildShakePreComp.",".shk",PackageInfo.sTempPath.toFile());
		
			FileCleaner.add(script);
			FileCleaner.add(shkScript);

			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(shkScript)));
			
			PrintWriter bashOut = new PrintWriter(new BufferedWriter(new FileWriter(script)));
			Path target = new Path(PackageInfo.sProdPath.toOsString() + compFolder + "/" + filePrefix);

			bashOut.println("cp "+ shkScript.getPath() + " " + target.toOsString()+"pre.shk");
//			bashOut.println("cp "+ shkScript.getPath() + " " + target.toOsString()+"comp.shk");

			bashOut.close();

			{
				out.println("// Shake v4.10.0606 - (c) Apple Computer, Inc. 1998-2006.  All Rights Reserved.");
				out.println("// Apple, the Apple logo and Shake are trademarks of Apple " +
				"Computer, Inc., registered in the U.S. and other countries.");
				out.println("\n");
			}

			{
				/*--Movie settings--*/
				out.println("SetTimeRange(\""+startFrame+"-"+endFrame+"\");");
				out.println("SetFieldRendering(0);");
				out.println("SetFps(30);");
				out.println("SetMotionBlur(1, 1, 0);");
				out.println("SetQuality(1);");
				out.println("SetUseProxy(\"Base\");");
				out.println("SetProxyFilter(\"default\");");
				out.println("SetPixelScale(1, 1);");
				out.println("SetUseProxyOnMissing(1);");
				out.println("SetDefaultWidth(1280);");
				out.println("SetDefaultHeight(720);");
				out.println("SetDefaultBytes(2);");
				out.println("SetDefaultAspect(1);");
				out.println("SetDefaultViewerAspect(1);");
				out.println("SetMacroCheck(1);");
				out.println("SetTimecodeMode(\"30 FPS\");");
				out.println("");
				out.println("DefineProxyPath(\"No_Precomputed_Proxy\", " +
				"1, 1, -1, \"Auto\", -1, 0, 0, \"\",1);");
				out.println("DefineProxyPath(\"No_Precomputed_Proxy\", " +
				"0.5, 1, 1, \"Auto\", 0, 0, 1, \"\");");
				out.println("DefineProxyPath(\"No_Precomputed_Proxy\", " +
				"0.25, 1, 1, \"Auto\", 0, 0, 2, \"\");");
				out.println("DefineProxyPath(\"No_Precomputed_Proxy\", " +
				"0.1, 1, 1, \"Auto\", 0, 0, 3, \"\");");
				out.println("SetAudio(\"100W@E0000qFdsuHW962Dl9BOW0mWa06w7mCJ000000000008\");");
				out.println("");
			}

			{
				/*--Input nodes--*/
				out.println("// Input nodes");
				out.println("");

				for(String key: nameToNodeNameMap.keySet()){
					if(key.equals(SHDW))
						out.println("Color1 = Color(1280, 720, 2, 0, red, red, 1, 0);");

					FrameRange fRange = passRange.get(key);

					String lyr = filePrefix+key;
					out.println(lyr+" = SFileIn(\"$WORKING"+nameToNodeNameMap.get(key)+"."
							+fRange.getStart()+"-"+fRange.getEnd()+"x"+fRange.getBy()+"#."+extension+"\", ");
					out.println("    \"Auto\", 0, 0, \"v1.1\", \"0\", \"\");");

					out.println("IRetime("+lyr+", "+startFrame+", "+startFrame+", "+endFrame
							+", \"Freeze\", \"Freeze\");");

					System.err.println(lyr+" = SFileIn(\"$WORKING"+nameToNodeNameMap.get(key)+"."
							+fRange.getStart()+"-"+fRange.getEnd()+"x"+fRange.getBy()+"#."+extension+"\", ");
					System.err.println("    \"Auto\", 0, 0, \"v1.1\", \"0\", \"\");");
				}
				out.println("");
			}


			{
				/*--PreComp--*/
				String resize = "Resize";
				Set<String> passes = nameToNodeNameMap.keySet();
				String env = null;
				String ch = null;
				String all = null;

				out.println("// Processing nodes");
				out.println("");
				for(String pass: passes){
					String lyr = filePrefix+pass;
					out.println(pass+resize+" = Resize("+ lyr +", 1280, 720, \"sinc\", 1);");
				}

				if(passes.contains(CH))
					ch = CH+resize;

				if((ch!=null) && passes.contains(CHOCC)) {
					
					out.println("IMult1_chOcc = IMult("+ch+", " + CHOCC+resize + ", 1, 100, 0);");
					ch = "IMult1_chOcc";
				}
				all = ch;

				if(passes.contains(ZDOF)) 
					out.println("Reorder1_Z = Reorder(" + ZDOF+resize + ", \"rgbar\");");

				if(passes.contains(FG) && (passes.contains(MG))
						&& (passes.contains(BG))){
					out.println("fg_over_mg = Over("+ FG+resize +","+ MG+resize +", 1, 0, 0);");
					out.println("fg_comp_over_bg = Over(fg_over_mg, "+ BG+resize +", 1, 0, 0);");
					env = "fg_comp_over_bg";
				} else if(passes.contains(FG) && (passes.contains(MG))){
					out.println("fg_over_mg = Over("+ FG+resize +","+ MG+resize +", 1, 0, 0);");
					env = "fg_over_mg";
				} else if(passes.contains(FG) && (passes.contains(BG))){
					out.println("fg_over_bg = Over("+ FG+resize +","+ BG+resize +", 1, 0, 0);");
					env = "fg_over_bg";
				} else if(passes.contains(MG) && (passes.contains(BG))){
					out.println("mg_over_bg = Over("+ MG+resize +","+ BG+resize +", 1, 0, 0);");
					env = "mg_over_bg";
				} else if(passes.contains(BG)){
					env = BG+resize;
				} else if(passes.contains(MG)){
					env = MG+resize;
				} else if(passes.contains(FG)){
					env = FG+resize;
				}


				if(env!=null){
					if(passes.contains(ENVOCC)){
						out.println("IMult2_env_occ = IMult("+env+", "+ ENVOCC+resize+", 1, 100, 0);");
						env = "IMult2_env_occ";
					}

					if(passes.contains(SKY)){
						out.println("env_over_sky = Over("+env+","+ SKY+resize+", 1, 0, 0);");
						env = "env_over_sky";
					}

					if(passes.contains(SHDW)){
						out.println("shadowPlate_Inside_shdw_Alpha = Inside(Color1, "+ SHDW+resize+", 1);");
						out.println("shadow_over_env = Over(shadowPlate_Inside_shdw_Alpha, "+env+", 1, 0, 0);");
						env = "shadow_over_env";
					}

					if(ch!=null) {
						out.println("character_over_env = Over("+ch+", "+env+", 1, 0, 0);");
						all = "character_over_env";
					} else {
						all = env;
					}
					if(passes.contains(MV)){
						out.println("RSMB3Vectors1 = RSMB3Vectors("+all+", " + MV+resize + ", ");
						out.println("    0.5, 1, 64, 1, 1, \"rfxksv7e66d24\");");
						all = "RSMB3Vectors1";
					}
				}

				out.println("FileOut = FileOut("+all+", \"$WORKING"+outRender+".#.iff\", ");				
				out.println("    \"Auto\");");
				out.println("");
				out.println("");
			}

			out.close();
		}catch(IOException ex){
			throw new PipelineException
			("Unable to write the target Shake script file (" + script + ")!\n" +
					ex.getMessage());
		}

		/* create the process to run the action */ 
		try {
			ArrayList<String> args = new ArrayList<String>();
			args.add(script.getPath());

			TreeMap<String, String> env =
				mclient.getToolsetEnvironment(pUser, pView, pToolset, PackageInfo.sOsType);
			SubProcessLight proc =
				new SubProcessLight("BuildShakePreComp","bash",args,env,wdir.toFile());
			try {
				proc.start();
				proc.join();
				if(!proc.wasSuccessful()) {
					throw new PipelineException(
							"Did not correctly edit write the script due to an error.!\n\n"
							+ proc.getStdOut() + "\n\n" + proc.getStdErr());
				}//end if
			}//end try
			catch(InterruptedException ex) {
				throw new PipelineException(ex);
			}//end catch
		} catch(Exception ex) {
			throw new PipelineException
			("Unable to generate the SubProcess to perform this Action!\n" +
					ex.getMessage());
		}//end catch

	}//end createShkFile


	
	/**
	 * Returns all the directories that are located directly underneath a given path.
	 * 
	 * @param w
	 * 	Wrapper class.
	 * @param start
	 * 	The path to start the search underneath
	 * @return
	 * @throws PipelineException
	 */
	public ArrayList<String> getChildrenDirs(MasterMgrClient mclient, String start)
	throws PipelineException
	{
		ArrayList<String> toReturn = new ArrayList<String>();
		TreeMap<String, Boolean> comps = new TreeMap<String, Boolean>();
		comps.put(start, false);
		NodeTreeComp treeComps = mclient.updatePaths(pUser, pView, comps);
		Path p = new Path(start);
		ArrayList<String> parts = p.getComponents();
		for (String comp : parts)
		{
			if ( treeComps == null )
				break;
			treeComps = treeComps.get(comp);
		}
		if ( treeComps != null )
		{
			for (String s : treeComps.keySet())
			{
				NodeTreeComp comp = treeComps.get(s);
				if ( comp.getState() == NodeTreeComp.State.Branch )
					toReturn.add(s);
			}
		}
		return toReturn;
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



	private boolean queueAndWait(MasterMgrClient mclient, QueueMgrClient qclient, QueueJobGroup jobGroup)
	throws PipelineException
	{
		try {
			Thread.sleep(5000);
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}

		boolean done = false;
		boolean error = false;

		while(!done) {
			TreeSet<Long> stuff = new TreeSet<Long>();
			stuff.add(jobGroup.getGroupID());
			TreeMap<Long, JobStatus> statuses = qclient.getJobStatus(stuff);
			for(JobStatus status : statuses.values()) {
				System.err.println("JobID: " + status.getJobID());
				JobState state = status.getState();
				System.err.println("State: " + state);
				if(state.equals(JobState.Failed) || state.equals(JobState.Aborted)) {
					error = true;
					break;
				}
				if(!state.equals(JobState.Finished)) {
					done = false;
					break;
				}
				done = true;
			}
			System.err.println("Value of Done: " + done);
			System.err.println("Value of Error: " + done);
			if(error)
				throw new PipelineException("The job for the (" + jobGroup.getNodeID().getName()
						+ ") did not complete correctly.");
			if(!done)
				break;
		}
		if(done) {
			System.err.println("Running completed code");
		}
		System.err.println();
		return done;
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

}
