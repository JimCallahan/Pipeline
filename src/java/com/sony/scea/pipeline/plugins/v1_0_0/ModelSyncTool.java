package com.sony.scea.pipeline.plugins.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;

/**
 * Allows a user to sync the hi-res models with the lo-res models for an 
 * animation. If there are hi-res models that are not used in the animation
 * then they are removed. If lo-res models exist in that animation  but do 
 * not have hi-res versions hooked up to the lighting set up, then the correct
 * hi-res models are added.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class ModelSyncTool extends BaseTool
{

	/*-----------------------------------------------*/
	/*             STATIC INTERNAL VARS              */
	/*-----------------------------------------------*/
	private static final long serialVersionUID = -8207371946985983079L;

	/*-----------------------------------------------*/
	/*                  INTERNALS                    */
	/*-----------------------------------------------*/

	/**
	 * The current working area user|view.
	 */
	private String pUser;
	private String pView;

	/**
	 * The source parameter for the action on the target node.
	 */
	private TreeMap<String,String> pAnimSrcs;
	
	private String hiresPattern = ".*/assets/(character|set|prop)/.*";
	private String loresPattern = hiresPattern+"_lr";

	/**
	 * The action name that should be on the target node.
	 */
	private String actionName = "ModelReplace";



	public static final LinkPolicy REF = LinkPolicy.Reference;
	public static final LinkRelationship LINKALL = LinkRelationship.All;
	public static final CheckOutMethod frozU = CheckOutMethod.FrozenUpstream;
	public static final CheckOutMode over = CheckOutMode.OverwriteAll;

	/*-----------------------------------------------*/
	/*                 CONSTRUCTOR                   */
	/*-----------------------------------------------*/

	/**
	 * Allows a user to sync the hi-res models with the lo-res models for an 
	 * animation.
	 */
	public ModelSyncTool()
	{
		super("Model Sync Tool", new VersionID("1.0.0"), "SCEA",
				"Makes sure that the hi-res models for an animation are in sync"
				+ "with the lo-res models.");
		underDevelopment();

		addSupport(OsType.MacOS);
		addSupport(OsType.Windows);
		//pPhase = 1;

		//pSourceNames = new TreeSet<String>();

		pAnimSrcs = new TreeMap<String, String>();
	}//end constructor


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
		if ( pPrimary == null )
			throw new PipelineException("The primary selection must be the Target Node!");

		if ( pSelected.size() < 1 )
			throw new PipelineException("You must select at least one node.");

		NodeStatus status = pSelected.get(pPrimary);
		NodeID nodeID = status.getNodeID();
		pUser = nodeID.getAuthor();
		pView = nodeID.getView();

		for(String node: pSelected.keySet()){
			NodeStatus stat = pSelected.get(node);

			NodeMod mod = stat.getDetails().getWorkingVersion();
			if (mod.getAction()==null )
				throw new PipelineException("The target node does not have a Model Replace action!");
			
			if ( !mod.getAction().getName().startsWith(actionName) )
				throw new PipelineException("The target node does not have a Model Replace action!");

			LinkActionParam param = (LinkActionParam) mod.getAction().getSingleParam("Source");

			if (param==null)
				throw new PipelineException("There is no source parameter value!");

			/*if (params.size() != 1 )
				throw new PipelineException("Somehow " + stat.getName() +
						" has more than one source parameter!");*/
			String pAnimSrc = param.getStringValue();
			/*for (ActionParam param : params)
			{
				if(param.getValue()!=null)
					pAnimSrc = param.getValue().toString();
			}//end for
			 */
			if ( pAnimSrc == null )
				throw new PipelineException("There is no source parameter value!");

			pAnimSrcs.put(node, pAnimSrc);
		}

		return ": syncing models.";
	}//end collectPhaseInput()


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
		for(String switchName: pAnimSrcs.keySet()){
			

			TreeSet<String> pHiresSrcs = new TreeSet<String>();
			TreeSet<String> pLoresSrcs = new TreeSet<String>();

			System.err.println(switchName);
			NodeMod switchMod = mclient.getWorkingVersion(pUser, pView, switchName);			

			System.err.println(pAnimSrcs.get(switchName));
			NodeMod animMod = mclient.getWorkingVersion(pUser, pView, pAnimSrcs.get(switchName));
			Set<String> pAnimSrcNames = animMod.getSourceNames();

			for (String src : pAnimSrcNames)
			{
				if (src.matches(loresPattern))
					pLoresSrcs.add(src);
			}//end for

			for (String src : switchMod.getSourceNames())
			{
				if (src.matches(hiresPattern))
				{
					if(pLoresSrcs.contains(src+"_lr"))
						pHiresSrcs.add(src);		
				}//end if
			}//end for

			//add necessary hires models
			for(String lores: pLoresSrcs) {
				String hr = lores.replace("_lr","");
				if(!pHiresSrcs.contains(hr))
					pHiresSrcs.add(hr);
			}

			{
				switchMod = mclient.getWorkingVersion(pUser, pView, switchName);
				TreeSet<String> switchSrcs = new TreeSet<String>(switchMod.getSourceNames());
				//System.err.println("Final hiRes list:"+pHiresSrcs+"\n");
				//System.err.println("switch now has:\n\t" +switchSrcs +"\n");
				//System.err.println("Looking for things to add.");
				for(String src: pHiresSrcs){
					if((src.matches(hiresPattern) &&(!switchSrcs.contains(src)))){
						System.err.println("Linking: "+src);
						jcheckOut(mclient, pUser, pView, src, null, over, frozU);
						mclient.link(pUser, pView, switchName, src, REF, LINKALL, null);
						switchSrcs.add(src);
					}
					//System.err.println("src from hiRes list: "+src);
				}
			}

			{
				switchMod = mclient.getWorkingVersion(pUser, pView, switchName);
				TreeSet<String> switchSrcs = new TreeSet<String>(switchMod.getSourceNames());
				//System.err.println("switch now has:\n\t" + switchSrcs+"\n");
				for(String src: switchSrcs){
					if((src.matches(hiresPattern) &&(!pHiresSrcs.contains(src)))){
						System.err.println("Unlinking: "+ src);
						mclient.unlink(pUser, pView, switchName, src);	
					}
					//System.err.println("src from switch node list: "+src);
				}
			}
		}
		System.err.println("DONE");
		return false;
	}//end executePhase(MasterMgrClient,QueueMgrClient)
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
