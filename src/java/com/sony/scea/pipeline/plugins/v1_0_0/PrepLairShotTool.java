package com.sony.scea.pipeline.plugins.v1_0_0;

import java.util.*;

import javax.swing.JPanel;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.JConfirmDialog;
import us.temerity.pipeline.ui.JToolDialog;

/**
 * Allows a user to check out the latest versions of the first level of source
 * nodes as well as the latest version of the target node.
 * <p>
 * Has some values hardcoded for lair, like the finalize mel scripts.  It probably shouldn't
 * do that.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class PrepLairShotTool extends BaseTool {

	/*-----------------------------------------------*/
	/* STATIC INTERNAL VARS */
	/*-----------------------------------------------*/
	private static final long serialVersionUID = -8354647132163337860L;

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

	public static final CheckOutMode over = CheckOutMode.OverwriteAll;

	public static final CheckOutMode keep = CheckOutMode.KeepModified;

	public static final CheckOutMethod froz = CheckOutMethod.AllFrozen;
	
	public static final CheckOutMethod frozU = CheckOutMethod.FrozenUpstream;

	public static final CheckOutMethod pFroz = CheckOutMethod.PreserveFrozen;

	/*-----------------------------------------------*/
	/* CONSTRUCTOR */
	/*-----------------------------------------------*/

	/**
	 * Allows a user to check out the latest versions of the first level of
	 * source nodes as well as the latest version of the target node.
	 */
	public PrepLairShotTool() {
		super("Prep Lair Shot", new VersionID("1.0.0"), "SCEA",
				"Checks out the latest repository version of the first level of source"
						+ " nodes as well as the target node.");
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

		if (pSelected.size() != 1)
			throw new PipelineException("Only one Target Node may be selected.");

		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();

		OverallNodeState state = status.getDetails().getOverallNodeState();
		JToolDialog tool = new JToolDialog("PrepLair", new JPanel(), "Continue");

		if (!state.equals(OverallNodeState.Identical)) {
			JConfirmDialog dialog = new JConfirmDialog(tool,
					"This node is not identical to the checked in node.\n"
							+ "Using this tool could be a bad idea.\n Do you want to continue?");
			dialog.setVisible(true);
			if (!dialog.wasConfirmed()) {
				return null;
			}// end if
			return ": Using unidentical target node";
		}// end if
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
		NodeStatus stat = pSelected.get(pPrimary);
		pSourceNames = stat.getSourceNames();
		for (String src : pSourceNames) {
			getLatest(pUser, pView, mclient, src, over, frozU);
		}// end for
		getLatest(pUser, pView, mclient, pPrimary, keep, pFroz);
		{
		  jcheckOut(mclient, pUser, pView, "/projects/lr/assets/tools/mel/finalize-character_lr", null, over, froz);
		  jcheckOut(mclient, pUser, pView, "/projects/lr/assets/tools/mel/finalize-set_lr", null, over, froz);
		  jcheckOut(mclient, pUser, pView, "/projects/lr/assets/tools/mel/finalize-prop_lr", null, over, froz);		   
		}
		return false;
	}// end executePhase(MasterMgrClient,QueueMgrClient)

	/**
	 * Checks out the latest version of the node using the CheckOutMode and
	 * CheckOutMethod passed in. This method is modified from the Globals class.
	 * 
	 * @param user
	 * @param view
	 * @param mclient
	 * @param name
	 * @param mode
	 * @param method
	 * @throws PipelineException
	 */
	public static void getLatest(String user, String view,
			MasterMgrClient mclient, String name, CheckOutMode mode,
			CheckOutMethod method) throws PipelineException {
		TreeSet<VersionID> versions = mclient.getCheckedInVersionIDs(name);
		VersionID latestID = versions.last();
		mclient.checkOut(user, view, name, latestID, mode, method);
	}// end
		// getLatest(String,String,MasterMgrClient,String,CheckOutMode,CheckOutMethod)
	  private void jcheckOut(MasterMgrClient mclient, String user, String view, String name,
	      VersionID id, CheckOutMode mode, CheckOutMethod method) throws PipelineException
	  {
	    if (id == null)
	      id = mclient.getCheckedInVersionIDs(name).last();
	    if (id == null)
	      throw new PipelineException("BAD BAD BAD");
	    mclient.checkOut(user, view, name, id, mode, method);
	  }

}// end class
