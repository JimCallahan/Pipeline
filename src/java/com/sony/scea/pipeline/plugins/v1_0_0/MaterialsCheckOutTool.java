package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.*;
import java.util.*;

import javax.swing.JPanel;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.JConfirmDialog;
import us.temerity.pipeline.ui.JToolDialog;

/**
 * Special checkout for Materials. This ensures that the materials artist is working 
 * with the latest textures, models and rig
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class MaterialsCheckOutTool extends BaseTool{

	/*-----------------------------------------------*/
	/*             STATIC INTERNAL VARS              */
	/*-----------------------------------------------*/	
	private static final long serialVersionUID = -399041721436476824L;
	
	/*-----------------------------------------------*/
	/*                  INTERNALS                    */
	/*-----------------------------------------------*/
	private String assetPattern = ".*/assets/(character|set|prop)";
	private String texPattern = assetPattern + "/.*/textures/.*_tex";
	private String matPattern = assetPattern + "/.*/material/.*_mat";
	//private String loresPattern = hiresPattern+"_lr";

	public static final LinkPolicy REF = LinkPolicy.Reference;
	public static final LinkRelationship LINKALL = LinkRelationship.All;
	public static final CheckOutMode over = CheckOutMode.OverwriteAll;
	public static final CheckOutMode keep = CheckOutMode.KeepModified;
	public static final CheckOutMethod froz = CheckOutMethod.AllFrozen;
	public static final CheckOutMethod modi = CheckOutMethod.Modifiable;
	public static final CheckOutMethod frozU = CheckOutMethod.FrozenUpstream;
	public static final CheckOutMethod pFroz = CheckOutMethod.PreserveFrozen;

	private PrintWriter err;

	/**
	 * The current working area user|view|toolset.
	 */
	private String pUser;
	private String pView;

	public MaterialsCheckOutTool(){

		super("MaterialsCheckOut", new VersionID("1.0.0"), "SCEA",
				"Special checkout for Materials work. This ensures that " +
				"the materials artist is working with the latest animation and " +
		"hi-res models.");

		err=null;

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

		//folder = nPath.getParent();
		//assetName = nPath.getName();
		Path nPath = new Path(pPrimary);
		nPath = nPath.getParentPath().getParentPath();
		
		

		if(!nPath.toString().matches(assetPattern))
			throw new PipelineException("This tool only works on assets."+nPath.toString() + " " + nPath.toOsString());
	
		//if(!pPrimary.matches(matPattern))
		//throw new PipelineException("This tool will only work on a lgt node!");

		File errFile;
		try {
			errFile = File.createTempFile("MaterialsCheckOut", ".err",
					PackageInfo.sTempPath.toFile());
			err = new PrintWriter(errFile);
			FileCleaner.add(errFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();




		OverallNodeState state = status.getDetails().getOverallNodeState();
		JToolDialog tool = new JToolDialog("MaterialsCheckOut", new JPanel(), "Continue");
		if (!state.equals(OverallNodeState.Identical)) {
			JConfirmDialog dialog = new JConfirmDialog(tool,
					"This node is different from the checked in node. Do you want to continue with this check out?");
			dialog.setVisible(true);
			if (!dialog.wasConfirmed()) {
				return null;
			}// end if
		}// end if

		return "...Sing Hallelujah!";
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
		boolean newCheckOut = false;
		String matName = null;
		String texName = null;

		{
			/*find out if it is a fresh checkout*/
			JToolDialog tool = new JToolDialog("MaterialsCheckOut", new JPanel(), "Continue");
			JConfirmDialog dialog = new JConfirmDialog(tool,
					"Do you want a completely fresh checkout");
			dialog.setVisible(true);
			newCheckOut = dialog.wasConfirmed();
		}


		{
			/*-check out the asset and mat nodes-*/
			jcheckOut(mclient, pUser, pView, pPrimary, null, keep, pFroz);
			NodeMod assetMod = mclient.getWorkingVersion(pUser, pView, pPrimary);
			{	
				JobReqs jreqs = assetMod.getJobRequirements();
				jreqs.addSelectionKey("LinuxOnly");
				assetMod.setJobRequirements(jreqs);
				mclient.modifyProperties(pUser, pView, assetMod);
			}

			Set<String> assetSrcs = assetMod.getSourceNames();
			err.println("The asset sources are: ");      		

			for(String src: assetSrcs){
				err.println(src);

				if(src.matches(matPattern)){
					matName = src;
					err.println("Found mat node:\n\t "+src);
					if(newCheckOut){
						err.println("Clean mat checkout");
						jcheckOut(mclient, pUser,pView, src,null, over, frozU);
					} else {

						OverallNodeState state = 
							mclient.status(pUser,pView,src).getDetails().getOverallNodeState();

						if (!state.equals(OverallNodeState.Modified)){
							err.println("Mat node has not been modified");
							jcheckOut(mclient, pUser,pView, src,null, keep, pFroz);
						}

					}
				} else if(src.matches(matPattern+"Exp")){
					err.println("Matexp is:\n\t"+src);
					jcheckOut(mclient, pUser, pView, src, null, keep, modi);
					{	
						NodeMod expMod = mclient.getWorkingVersion(pUser, pView, src);
						JobReqs jreqs = expMod.getJobRequirements();
						jreqs.addSelectionKey("LinuxOnly");
						expMod.setJobRequirements(jreqs);
						mclient.modifyProperties(pUser, pView, expMod);
					}
				} else {
					jcheckOut(mclient, pUser, pView, src, null, over, froz);
				}
			}//end for
			err.println("mat node: "+matName);
			if(matName==null)
				throw new PipelineException("This asset node does not have an " +
				"attached mat node");
		}


		{
			/*find the texture node and check out so it can be changed. If new checkout, fresh textures*/
			NodeMod matMod = mclient.getWorkingVersion(pUser, pView, matName);
			TreeSet<String> matSrcs = new TreeSet<String>(matMod.getSourceNames());
			for(String src: matSrcs){
				if(src.matches(texPattern)){
					texName = src;
					err.println("Found tex node:\n\t "+src);
					if(newCheckOut){
						err.println("Clean");
						jcheckOut(mclient, pUser, pView, src, null, over, frozU);
					} else {
						err.println("Old stuff");
						jcheckOut(mclient, pUser, pView, src, null, keep, pFroz);
						jcheckOut(mclient, pUser, pView, src, null, keep, modi);
					}
					continue;
				}//end if
				jcheckOut(mclient, pUser, pView, src, null, over, froz);
			}//end for
			err.println("tex node: "+texName);
			if(texName==null)
				throw new PipelineException("This asset node does not have an associated " +
				"texture node");

		}

		err.println("Checked out the asset, mat and texture nodes");

		{
			/*check out finalise scripts*/
			jcheckOut(mclient, pUser, pView, "/projects/lr/assets/tools/mel/finalize-character", null, over, froz);
			jcheckOut(mclient, pUser, pView, "/projects/lr/assets/tools/mel/finalize-set", null, over, froz);
			jcheckOut(mclient, pUser, pView, "/projects/lr/assets/tools/mel/finalize-prop", null, over, froz);		   
		}

		err.close();

		return false;
			}//end executePhase
	
	  private void jcheckOut(MasterMgrClient mclient, String user, String view, String name,
	      VersionID id, CheckOutMode mode, CheckOutMethod method) throws PipelineException
	  {
	    if (id == null)
	      id = mclient.getCheckedInVersionIDs(name).last();
	    if (id == null)
	      throw new PipelineException("BAD BAD BAD");
	    mclient.checkOut(user, view, name, id, mode, method);
	  }
}//end class
