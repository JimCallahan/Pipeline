package com.sony.scea.pipeline.plugins.v1_0_0;

import java.awt.Component;
import java.awt.Dimension;
import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.NodeTreeComp.State;
import us.temerity.pipeline.ui.*;

/**
 * This tool allows the user to build a compositing render tree for a selected 
 * lighting node.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class BuildShakeTreeTool extends BaseTool {

	private static final long serialVersionUID = -8236576786944271492L;

	private String prePattern = ".*/production/.*/comp/.*_pre";
	private String pUser;
	private String pView;
	private String compFolder;
	private String pToolset;
	private PluginMgrClient plug;

	private String filePrefix;

	private String extension;
	private Integer startFrame;
	private Integer endFrame;
	private Integer byFrame;
	private Integer pad;

	private String workingArea;


	public BuildShakeTreeTool(){
		super("BuildShakeTree", new VersionID("1.0.0"), "SCEA", "This tool creates the " +
		"pre-comp shake scripts based on the rendered images of the selected lighting node.");


		plug = PluginMgrClient.getInstance();

		addSupport(OsType.MacOS);
		//addSupport(OsType.Windows);

		extension = null;
		startFrame = null;
		endFrame = null;
		byFrame = null;
		pad = null;
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

		if(!pPrimary.matches(prePattern))
			throw new PipelineException("This tool only works on pre-comp shake nodes.");


		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();

		plug = PluginMgrClient.getInstance();

		Path nPath = new Path(pPrimary);
		String lgtNodeName = nPath.getName();

		filePrefix = lgtNodeName.replaceAll("pre","");


		compFolder = nPath.getParent();
		workingArea = nodeID.getWorkingPath().toString().replaceAll(pPrimary,"");		

		System.err.println("File prefix is "+filePrefix+" and the folder is "+ compFolder);
		System.err.println("The working area is "+workingArea);
		
//		String imgFolder = nPath.getParentPath().getParent() + "/img";
//		ArrayList<String> imgFolders = getChildrenDirs(mclient, imgFolder);
//		for(String layer: imgFolders){
//			System.err.println(layer);
//			String lyrLoc = imgFolder+"/"+layer+"/"+filePrefix+layer;
//			System.err.println("The whole path is "+lyrLoc);
//			NodeMod lyrMod = null;
//
//			if(layer.equals("test"))	
//				continue;
//
//
//			if(doesNodeExists(mclient, lyrLoc)){
//				try{
//					lyrMod = mclient.getWorkingVersion(pUser, pView, lyrLoc);
//				} catch (PipelineException e){
//					mclient.checkOut(pUser, pView, lyrLoc, null, 
//							CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
//					lyrMod = mclient.getWorkingVersion(pUser, pView, lyrLoc);
//				}
//
//				if(lyrMod.getSourceNames().contains(pPrimary)){
//					FrameRange fRange = lyrMod.getPrimarySequence().getFrameRange();
//					if(startFrame==null)
//						startFrame = fRange.getStart();
//
//					if((endFrame==null) || (endFrame<fRange.getEnd()))
//						endFrame = fRange.getEnd();
//
//					if(byFrame==null)
//						byFrame = fRange.getBy();
//				}
//			}//node Exists	
//		}
		
		
		/* DO GUI DRAWING STUFF*/
		JScrollPane scroll = null;
		JIntegerField startField;
		JIntegerField endField;
		JIntegerField byField;
		JIntegerField padField;
		JTextField extField;
		{
			int twidth = 180;
			int vwidth = 80;
			Box hbox = new Box(BoxLayout.X_AXIS);
			Component comps[] = UIFactory.createTitledPanels();
			JPanel tpanel = (JPanel) comps[0];
			JPanel vpanel = (JPanel) comps[1];
			JDrawer drawer = new JDrawer("Frame Info:" , (JComponent) comps[2], true);
			hbox.add(drawer);
			{
				extField = UIFactory.createTitledTextField(tpanel, 
						"By Frame", twidth, vpanel, "iff", vwidth);
				startField = UIFactory.createTitledIntegerField(tpanel, 
						"Start Frame", twidth, vpanel, 1, vwidth);
				endField = UIFactory.createTitledIntegerField(tpanel, 
						"End Frame", twidth, vpanel, 2, vwidth);
				byField = UIFactory.createTitledIntegerField(tpanel, 
						"By Frame", twidth, vpanel, 1, vwidth);
				padField = UIFactory.createTitledIntegerField(tpanel, 
						"Frame Padding", twidth, vpanel, 4, vwidth);
			}

			{
				JPanel spanel = new JPanel();
				spanel.setName("Spacer");

				spanel.setMinimumSize(new Dimension(twidth + vwidth, 7));
				spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
				spanel.setPreferredSize(new Dimension(twidth + vwidth, 7));

				hbox.add(spanel);
			}

			{
				scroll = new JScrollPane(hbox);
				scroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scroll
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

				Dimension size = new Dimension(twidth + vwidth, 140);
				scroll.setMinimumSize(size);
				scroll.setPreferredSize(size);

				scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
			}
		}

		/* query the user */
		{
			JToolDialog diag = new JToolDialog("What is the frame range?", scroll,
			"Confirm");      
			diag.setVisible(true);
			diag.setResizable(false);

			if(diag.wasConfirmed()){
				extension = (extField.getText()==null)? "iff" : extField.getText();
				startFrame = (startField.getValue()==null)? 1: startField.getValue();
				endFrame = (endField.getValue()==null)? 2 : endField.getValue();
				byFrame = (byField.getValue()==null)? 1 : byField.getValue();
				pad = (padField.getValue()==null)? 4 : padField.getValue();

			} else {
				return null;
			}
		}

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
		//String shkPrefix = compFolder + "/" + filePrefix;
		String imgRender = compFolder + "/img/"+filePrefix+"comp";
		createNodes(mclient, qclient);
		writeMovScript(mclient, imgRender);
		return false;
	}//end executePhase


	private void createNodes(MasterMgrClient mclient, QueueMgrClient qclient) 
	throws PipelineException 
	{

		DoubleMap<String, String, TreeSet<VersionID>> plugs = mclient
		.getToolsetActionPlugins(pToolset);

		BaseEditor shkEditor = mclient.getEditorForSuffix("shk");
		BaseEditor movEditor = mclient.getEditorForSuffix("mov");

		NodeMod compMod = null;
		{
			String compName = compFolder+ "/" + filePrefix+"comp";
			mclient.clone(pUser, pView, pPrimary, compName, false, true, true); 
			compMod = mclient.getWorkingVersion(pUser, pView, compName);	
			compMod.setAction(null);
		}


		NodeMod imgMod = null;
		{			
			imgMod = registerSequence(mclient, compFolder+"/img/"+filePrefix+"comp",pad,
					extension, shkEditor, startFrame, endFrame, byFrame);
			VersionID shkVer = plugs.get("Temerity", "ShakeComp").last();

			BaseAction shkAction = plug.newAction("ShakeComp", shkVer, "Temerity");
			mclient.link(pUser, pView, imgMod.getName(), compMod.getName(),
					LinkPolicy.Dependency, LinkRelationship.All, null);

			shkAction.setSingleParamValue("ShakeScript", compMod.getName());
			imgMod.setAction(shkAction);

			JobReqs jreqs = imgMod.getJobRequirements();
			jreqs.addSelectionKey("LinuxOnly");
			jreqs.addSelectionKey("Shake");
			imgMod.setJobRequirements(jreqs);
			imgMod.setActionEnabled(true);
			imgMod.setExecutionMethod(ExecutionMethod.Parallel);
			imgMod.setBatchSize(3);

			mclient.modifyProperties(pUser, pView, imgMod);
		}

		NodeMod movShkMod = null;
		{			
			movShkMod = registerNode(mclient, compFolder+"/" + filePrefix+"mov","shk", shkEditor);
			mclient.link(pUser, pView, movShkMod.getName(), imgMod.getName(),
					LinkPolicy.Reference, LinkRelationship.All, null);
		}

		String movName = filePrefix.substring(0,filePrefix.length()-1);
		NodeMod movMod = registerNode(mclient, compFolder+"/mov/"+movName, "mov", movEditor);
		{
			mclient.link(pUser, pView, movMod.getName(), movShkMod.getName(),
					LinkPolicy.Dependency, LinkRelationship.All, null);

			VersionID shkVer = plugs.get("Temerity", "ShakeComp").last();
			BaseAction shkAction = plug.newAction("ShakeComp", shkVer, "Temerity");
			shkAction.setSingleParamValue("ShakeScript", movShkMod.getName());
			movMod.setAction(shkAction);			

			JobReqs jreqs = movMod.getJobRequirements();
			jreqs.addSelectionKey("MacOnly");
			movMod.setJobRequirements(jreqs);

			mclient.modifyProperties(pUser, pView, movMod);
			pRoots.add(movMod.getName());
			pRoots.remove(pPrimary);
		}
	}//end createNodes


	/*
	 * 
	 */
	private void writeMovScript(MasterMgrClient mclient, String srcRender) 
	throws PipelineException 
	{
		File script = null;
		File shkScript = null;
		Path wdir = null;


		try {      
			script = File.createTempFile("BuildShakePreComp-Mov.",".bash",PackageInfo.sTempPath.toFile());
			shkScript = File.createTempFile("BuildShakePreComp-Mov.",".shk",PackageInfo.sTempPath.toFile());
			FileCleaner.add(script);
			FileCleaner.add(shkScript);

			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(shkScript)));	

			PrintWriter bashOut = new PrintWriter(new BufferedWriter(new FileWriter(script)));

			Path target = new Path(PackageInfo.sProdPath.toOsString()+workingArea+compFolder+"/" + filePrefix);
			wdir = new Path(PackageInfo.sProdPath.toOsString());

			System.err.println(wdir.toOsString());
			bashOut.println("cp "+ shkScript.getPath() + " " + target.toOsString() + "mov.shk");
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
				String movName = filePrefix.substring(0,filePrefix.length()-1);

				/*--Input nodes--*/
				out.println("// Input nodes");
				out.println("");
				String lyr = filePrefix+"img";
				out.println(lyr+" = SFileIn(\"$WORKING"+srcRender+"."
						+startFrame+"-"+endFrame+"x"+byFrame+"#."+extension+"\", ");
				out.println("    \"Auto\", 0, 0, \"v1.1\", \"0\", \"\");");
				out.println("IRetime("+lyr+", "+(startFrame-1)+", "+startFrame+", "+(endFrame+1)+
						", \"Freeze\", \"Freeze\");");
				
				out.println("");

				out.println("// Processing nodes");
				out.println("");
				out.println(movName +" = FileOut("+lyr+", \"$WORKING"+compFolder +"/mov/"+movName+".mov\"," +
						" \"Auto\", \"Shake 3 QT 1\", \"Apple Animation\", 0, \"" + 
						"100W@c6000WDcsuHpoe10pFnKA6W230PPMMRH#0LgfI5ALV1EaKGn3DG322egJHLPHI0O1b4T0meWYHEK" +
						"DxWGB9XYIOa1vm6NmIa2EZIBDHEoL7U8IfLR1Hqe5M4XMnMiGIqnB8NngK4pYITMG2Ph243ObcNAYT9Me" +
						"EOb9jiJB1KN47MNapAv5aOMLH07e5C5bYp9PnqltvgQ4gTW853C4b10U00GiX32\", 0, 44100, 16);");

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
							"Did not correctly edit the reference due to a maya error.!\n\n"
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


}
