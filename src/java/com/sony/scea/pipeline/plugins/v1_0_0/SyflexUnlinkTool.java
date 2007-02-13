package com.sony.scea.pipeline.plugins.v1_0_0;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import us.temerity.pipeline.*;

/**
 * Unlinks the animation node from a syflex sim tree. <p>
 * never actually used.  Was made unnecessary by changes to {@link BuildSyflexTreeTool}.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class SyflexUnlinkTool extends BaseTool {

	private static final long serialVersionUID = -6845705158577866216L;

	private String pUser;
	private String pView;

	private String cacheScene;
	private String animScene;

	private static String animPattern = ".*/production/.*/anim/.*_anim";	
	private static String cachePattern = ".*/production/.*/syf/seq.*_cache";
	private static String syfPattern = ".*/production/.*/syf/.*/seq.*_syf_.*";


	public SyflexUnlinkTool()
	{
		super("SyflexUnlink", new VersionID("1.0.0"), "SCEA",
		"Unlinks the animation node from a syflex sim tree.");

		underDevelopment();
		addSupport(OsType.MacOS);
		addSupport(OsType.Windows);

	}

	public synchronized String collectPhaseInput() throws PipelineException
	{
		if ( pPrimary == null )
			throw new PipelineException("You need to have a node selected");

		if ( pSelected.size() != 2 )
			throw new PipelineException("Must have two nodes selected.");

		for (String name : pSelected.keySet())
		{
			if ( name.matches(cachePattern) )
				cacheScene = name;

			if ( name.matches(animPattern) )
				animScene = name;
		}

		if ( cacheScene == null || animScene == null )
			throw new PipelineException("You did not select a cache "
					+ "and an animation node");

		NodeStatus stat = pSelected.get(pPrimary);
		NodeID id = stat.getNodeID();
		pUser = id.getAuthor();
		pView = id.getView();

		return " : the magic of pipeline.";
	}

	public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
	throws PipelineException
	{
		Path p = new Path(cacheScene);
		TreeSet<String> syfDirs = new TreeSet<String>(getChildrenDirs(mclient, p.getParent()));

		for(String dir: syfDirs){
			Path dirPath = new Path(p.getParentPath(), dir);
			System.err.println(dirPath);
			TreeSet<String> syfNodes = new TreeSet<String>(getChildrenNodes(mclient, dirPath.toString()));
			
			if(syfNodes.isEmpty())
				continue;

			for(String syfChild: syfNodes){
				Path syfPath = new Path(dirPath, syfChild);
				System.err.println("\t"+syfPath);
				if(!syfPath.toString().matches(syfPattern))
					continue;

				NodeMod syfMod = mclient.getWorkingVersion(pUser, pView, syfPath.toString());
				if(syfMod.getSourceNames().contains(animScene))
					mclient.unlink(pUser, pView, syfMod.getName(), animScene);
				System.err.println("\tMoving on.");
			}

		}
		return false;
	}


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
			treeComps = treeComps.get(comp);
		}
		for (String s : treeComps.keySet())
		{
			toReturn.add(s);
		}
		return toReturn;
	}

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
}
