package com.sony.scea.pipeline.plugins.v1_0_0;

import java.util.Set;
import java.util.TreeSet;

import us.temerity.pipeline.*;

/**
 * Allows a user to unlink all source nodes from the selected node.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class UnlinkAllTool extends BaseTool{

	/*-----------------------------------------------*/
	/* STATIC INTERNAL VARS */
	/*-----------------------------------------------*/
	private static final long serialVersionUID = -2938949929739389392L;
	
	/*-----------------------------------------------*/
	/* INTERNALS */
	/*-----------------------------------------------*/

	/**
	 * The current working area user|view.
	 */
	private String pUser;

	private String pView;

	/**
	 * Nodes which will be checked out as latest.
	 */
	private Set<String> pSourceNames;

	/*-----------------------------------------------*/
	/* CONSTRUCTOR */
	/*-----------------------------------------------*/

	/**
	 * Allows a user to check out the latest versions of the first level of
	 * source nodes as well as the latest version of the target node.
	 */
	public UnlinkAllTool() {
		super("Unlink All", new VersionID("1.0.0"), "SCEA",
				"Unlinks all sources of the selected node.");
		underDevelopment();

		addSupport(OsType.MacOS);
		addSupport(OsType.Windows);

		pSourceNames = new TreeSet<String>();
	}// end constructor

	/**
	 * Check that the user has properly selected a target node for this tool
	 * <P>
	 * 
	 * @return The phase progress message or <CODE>null</CODE> to abort early.
	 * @throws PipelineException
	 *         If unable to validate the given user input.
	 */
	public synchronized String collectPhaseInput() throws PipelineException {
		if (pPrimary == null)
			throw new PipelineException(
					"The primary selection must be the Target Node!");

		if (pSelected.size() < 1)
			throw new PipelineException("At least one node must be selected.");

		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();
		return ": No errors so far.";
	}// end collectPhaseInput()

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
			QueueMgrClient qclient) throws PipelineException {
		
		for(String node: pSelected.keySet()){
			NodeStatus stat = pSelected.get(node);
			pSourceNames = stat.getSourceNames();
			for(String source: pSourceNames){
				mclient.unlink(pUser, pView, node, source);
			}
		}
		return false;
	}// end executePhase(MasterMgrClient,QueueMgrClient)

}
