package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
//import javax.swing.JPanel;

import us.temerity.pipeline.*;
//import us.temerity.pipeline.ui.JConfirmDialog;
//import us.temerity.pipeline.ui.JToolDialog;

/**
 * Special checkout for Pre-lighting, to grab the latest animation and models.
 * <p>
 * Will also hook up cloth sim nodes if they exist for this shot and change 
 * the action of the switch light node to load the caches.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 * @deprecated
 */
public class DylanatorTool extends BaseTool{

	/*-----------------------------------------------*/
	/*             STATIC INTERNAL VARS              */
	/*-----------------------------------------------*/
	private static final long serialVersionUID = 9012338413662512239L;

	/*-----------------------------------------------*/
	/*                  INTERNALS                    */
	/*-----------------------------------------------*/
	private static String switchPattern = ".*/production/.*/lgt/.*_switch.*";
	private static String animPattern = ".*/production/.*/anim/.*_anim";
	private static String cltPattern = ".*/production/.*/seq.*/syf/.*_clt";
	private static String hiresPattern = ".*/assets/(character|set|prop)/.*";
	private static String loresPattern = hiresPattern+"_lr";

	private static final String aApplyCache = "ApplyCache";

	public static final LinkPolicy REF = LinkPolicy.Reference;
	public static final LinkPolicy DEP = LinkPolicy.Dependency;
	public static final LinkRelationship LINKALL = LinkRelationship.All;
	public static final CheckOutMode over = CheckOutMode.OverwriteAll;
	public static final CheckOutMode keep = CheckOutMode.KeepModified;
	public static final CheckOutMethod froz = CheckOutMethod.AllFrozen;
	public static final CheckOutMethod frozU = CheckOutMethod.FrozenUpstream;
	public static final CheckOutMethod pFroz = CheckOutMethod.PreserveFrozen;

	private PrintWriter err;

	private TreeSet<String> pLoresSrcs;
	private TreeSet<String> pHiresSrcs;

	/**
	 * The current working area user|view|toolset.
	 */
	private String pUser;
	private String pView;
	private String pToolset;

	private PluginMgrClient plug;

	private String modRep = "ModelReplace";

	public DylanatorTool(){

		super("Dylanator", new VersionID("1.0.0"), "SCEA",
				"Special checkout for the switchLgt node. This ensures that " +
				"the lighter is working with the latest animation and " +
		"hi-res models.");

		pLoresSrcs = new TreeSet<String>();
		pHiresSrcs = new TreeSet<String>();
		err=null;
		plug = PluginMgrClient.getInstance();
		
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
		if(pPrimary==null)
			throw new PipelineException("Please select a node.");

		if(!pPrimary.matches(switchPattern))
			throw new PipelineException("This tool will only work on a switchLgt node!");

		File errFile;
		try {
			errFile = File.createTempFile("Dylanator.", ".err",PackageInfo.sTempPath.toFile());
			err = new PrintWriter(errFile);
			FileCleaner.add(errFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();

//		OverallNodeState state = status.getDetails().getOverallNodeState();
//		JToolDialog tool = new JToolDialog("Dylanator", new JPanel(), "Continue");
//		if (!state.equals(OverallNodeState.Identical)) {
//		JConfirmDialog dialog = new JConfirmDialog(tool,
//		"This node is not identical to the checked in node.\n"
//		+ "Using this tool could be a bad idea.\n Do you want to continue?");
//		dialog.setVisible(true);
//		if (!dialog.wasConfirmed()) {
//		return null;
//		}// end if
//		}// end if

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
	public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient) 
	throws PipelineException 
	{ 
		DoubleMap<String, String, TreeSet<VersionID>> plugs = null;

		pToolset = mclient.getDefaultToolsetName();
		plugs = mclient.getToolsetActionPlugins(pToolset);

		mclient.checkOut(pUser, pView, "/projects/lr/assets/tools/mel/finalize-character", null, over, froz);
		mclient.checkOut(pUser, pView, "/projects/lr/assets/tools/mel/finalize-set", null, over, froz);
		mclient.checkOut(pUser, pView, "/projects/lr/assets/tools/mel/finalize-prop", null, over, froz);

		for(String switchName: pSelected.keySet()){
			dylanate(mclient, switchName, plugs);
		}


		err.close();

		return false;
	}//end executePhase


	private boolean dylanate(MasterMgrClient mclient, String switchName, 
			DoubleMap<String, String, TreeSet<VersionID>> plugs) throws PipelineException
	{
		err.println("\nProcessing: "+switchName);
		String animName = null;

		{			
			NodeMod switchMod = null;
			try{ 
				switchMod = mclient.getWorkingVersion(pUser, pView, switchName);
			} catch(PipelineException e){
				mclient.checkOut(pUser, pView, switchName, null, keep, pFroz);
				switchMod = mclient.getWorkingVersion(pUser, pView, switchName);
			}

			TreeSet<String> switchSrcs = new TreeSet<String>(switchMod.getSourceNames());

			for(String src: switchSrcs){
				if(src.matches(animPattern)){
					animName = src;
					err.println("Found anim node: "+src);
					mclient.checkOut(pUser, pView, src, null, over, frozU);
					continue;
				}//end if

				mclient.checkOut(pUser, pView, src, null, over, frozU);

				LinkMod lMod = switchMod.getSource(src);
				LinkPolicy rel = lMod.getPolicy();
				System.err.println(src+": "+rel);
				if(!rel.equals(REF)){
					System.err.println("umm");
					lMod.setPolicy(REF);
					switchMod.setSource(lMod);
					mclient.modifyProperties(pUser, pView, switchMod);
				}
			}//end for

			err.println("anim node: "+animName);

			mclient.modifyProperties(pUser, pView, switchMod);

			if(animName==null)
				throw new PipelineException("This switch node does not have an associated anim node");
		}
		err.println("Checked out the anim and switch nodes");
		
		String actionName = null;
		VersionID ttVer = null;
		boolean toCache = false;
		
		/*change the action setting on the Switch node*/
		{
			NodeMod switchMod = mclient.getWorkingVersion(pUser, pView, switchName);

			Path p = new Path(switchName);
			Path syfRoot = new Path(p.getParentPath().getParentPath(), "syf");
			System.err.println("Going to look for caches in: "+syfRoot);
			ArrayList<String> syfDirs = getChildrenDirs(mclient, syfRoot.toString());

			for(String dir: syfDirs){
				System.err.println(dir);
				Path dPath = new Path(syfRoot, dir);
				ArrayList<String> simDir = getChildrenNodes(mclient, dPath.toString());
				for(String pCache : simDir){
					Path cPath = new Path(dPath, pCache);
					if(cPath.toString().matches(cltPattern)){
						System.err.println("\t"+cPath.toString());
						try{ 
							mclient.checkOut(pUser, pView, cPath.toString(), null, over, froz);
							mclient.lock(pUser, pView, cPath.toString(), null);
							mclient.link(pUser, pView, switchMod.getName(), cPath.toString(), DEP, LINKALL, null);
						} catch (PipelineException e){
							e.printStackTrace();
						}
						toCache = true;
					}
				}
			}
			
			{
				BaseAction action = switchMod.getAction();
				if(!switchMod.getToolset().equals(pToolset)){
					switchMod.setToolset(pToolset);
					mclient.modifyProperties(pUser, pView, switchMod);
				}
				
				if(!toCache){
					actionName = modRep;
				} else {
					actionName = modRep+"Syflex";
				}
				ttVer = plugs.get("SCEA", actionName).last();
				
				if((action==null) || (!action.getName().equals(actionName))
						|| (!action.getVersionID().equals(ttVer))){
					System.err.println("Action name is incorrect - the switch node "+ switchName
							+" doesn't have a " + actionName + "Action");

					action = plug.newAction(actionName, ttVer, "SCEA");
					action.setSingleParamValue("Source", animName);
					action.setSingleParamValue("Response", "Ignore");
					if(toCache)
						action.setSingleParamValue(aApplyCache, true);
					switchMod.setAction(action);
					mclient.modifyProperties(pUser, pView, switchMod);
				} else {
					if(!action.getSingleParamValue("Response").equals("Ignore")){
//						action = plug.newAction(actionName, ttVer, "SCEA");
//						action.setSingleParamValue("Source", animName);
						action.setSingleParamValue("Response", "Ignore");
//						if(toCache)
//						action.setSingleParamValue(aApplyCache, true);
						switchMod.setAction(action);
						mclient.modifyProperties(pUser, pView, switchMod);
					}//end if
				}//end else

			}
		}


		{
			/*-check out and lock the animation sources-*/
			NodeMod animMod = mclient.getWorkingVersion(pUser, pView, animName);
			Set<String> animSrcs = animMod.getSourceNames();
			for(String src: animSrcs){		
				if (src.matches(loresPattern)){
					pLoresSrcs.add(src);
					err.println("Adding lores src "+src);
				}
				mclient.checkOut(pUser, pView, src, null, keep, pFroz);
			}//end for
			err.println("lores:" + pLoresSrcs);
		}

		/*-sync the animation assets with the switch assets.*/
		// also remove unnecessary hires models
		{
			NodeMod switchMod = mclient.getWorkingVersion(pUser, pView, switchName);
			for (String src : switchMod.getSourceNames())
			{
				if (src.matches(hiresPattern))
				{
					err.println("Found hires source "+src);
					if(pLoresSrcs.contains(src+"_lr")){
						pHiresSrcs.add(src);
						err.println("The hires source matched the anim node.");
					} else {
						err.println("Gotta remove "+src);
					}//end else
				}//end if
			}//end for
		}

		//add necessary hires models
		for(String lores: pLoresSrcs) {
			String hr = lores.replace("_lr","");
			err.println("Looking at lores source "+lores+" which matches "+hr);
			if(!pHiresSrcs.contains(hr)){
				err.println("Gotta add "+hr+" to "+switchName);
				pHiresSrcs.add(hr);
			}
		}

		{
			NodeMod switchMod = mclient.getWorkingVersion(pUser, pView, switchName);
			TreeSet<String> switchSrcs = new TreeSet<String>(switchMod.getSourceNames());
			err.println("Final hiRes list:"+pHiresSrcs+"\n");
			err.println("switch now has:\n\t" +switchSrcs +"\n");
			err.println("Looking for things to add.");
			for(String src: pHiresSrcs){
				if((src.matches(hiresPattern) &&(!switchSrcs.contains(src)))){
					err.print("Linking ");
					mclient.checkOut(pUser, pView, src, null, over, frozU);
					mclient.link(pUser, pView, pPrimary, src, REF, LINKALL, null);
					switchSrcs.add(src);
				}
				err.println("src from hiRes list: "+src);
			}
		}

		{
			NodeMod switchMod = mclient.getWorkingVersion(pUser, pView, switchName);
			TreeSet<String> switchSrcs = new TreeSet<String>(switchMod.getSourceNames());
			err.println("switch now has:\n\t" + switchSrcs+"\n");
			for(String src: switchSrcs){
				if((src.matches(hiresPattern) &&(!pHiresSrcs.contains(src)))){
					err.print("Unlinking ");
					mclient.unlink(pUser, pView, pPrimary, src);	
				}
				err.println("src from switch node list: "+src);
			}
		}

		/*queue the switch node*/
		mclient.submitJobs(pUser, pView, switchName, null);

		return false;
	}

	/**
	 * Returns all the directories that are located directly underneath a given path.
	 * From Globals
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


	/**
	 * Returns all the paths that are located directly underneath a given path.
	 * From Globals
	 * @param w
	 * 	Wrapper class.
	 * @param start
	 * 	The path to start the search underneath
	 * @return				
	 * @throws PipelineException
	 */
	public ArrayList<String> getChildrenNodes(MasterMgrClient mclient, String start)
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
			if(treeComps!=null)
				treeComps = treeComps.get(comp);
		}
		for (String s : treeComps.keySet())
		{
			toReturn.add(s);
		}
		return toReturn;
	}


}//end class
