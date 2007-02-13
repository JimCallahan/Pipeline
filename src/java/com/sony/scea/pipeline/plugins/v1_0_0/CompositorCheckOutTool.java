package com.sony.scea.pipeline.plugins.v1_0_0;

import java.util.*;
import us.temerity.pipeline.*;
import us.temerity.pipeline.NodeTreeComp.State;

/**
 * Special checkout for Lighting. This ensures that the compositor is working 
 * with the latest renders
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class CompositorCheckOutTool extends BaseTool {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 442462285068063197L;
	
	private static String prePattern = ".*/production/.*/comp/.*_pre";
	private String pUser;
	private String pView;
	
	public CompositorCheckOutTool(){

		super("CompositorCheckOut", new VersionID("1.0.0"), "SCEA",
				"Special checkout for Compositor. This ensures that " +
				"the compostor is working with the latest renders.");

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

		if(!pPrimary.matches(prePattern))
			throw new PipelineException("This tool will only work on a lgt node!");

		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();

		return "...The hills are alive with the sound of music!";
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
		jcheckOut(mclient,pUser, pView, pPrimary, null, CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
		String compName = pPrimary.replaceAll("_pre", "_comp");
		NodeMod preMod = mclient.getWorkingVersion(pUser, pView, pPrimary);
		TreeSet<String> srcs = new TreeSet<String>(preMod.getSourceNames());
		
		NodeMod compMod = null;
		
		if(doesNodeExists(mclient, compName)){
			try {
				compMod = mclient.getWorkingVersion(pUser, pView, compName);
			} catch (PipelineException e){
				jcheckOut(mclient,pUser, pView, compName, null, CheckOutMode.KeepModified, 
						CheckOutMethod.PreserveFrozen);
				compMod = mclient.getWorkingVersion(pUser, pView, compName);
			}
		}
		
		for(String src: srcs){
			System.err.println(src);
			if(compMod!=null)
				mclient.link(pUser, pView, compMod.getName(), src, LinkPolicy.Reference, 
						LinkRelationship.All, null);
			mclient.lock(pUser, pView, src, null);
		}
		
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
