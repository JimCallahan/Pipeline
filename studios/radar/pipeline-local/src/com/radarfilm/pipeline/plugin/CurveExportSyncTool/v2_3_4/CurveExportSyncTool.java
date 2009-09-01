package com.radarfilm.pipeline.plugin.CurveExportSyncTool.v2_3_4;

import java.util.*;

import us.temerity.pipeline.*;

public 
class CurveExportSyncTool 
  extends BaseTool 
{

	public CurveExportSyncTool()
	{
	    super("CurveExportSync", new VersionID("2.3.4"), "Radar",
		  "Adds new curve export nodes to an existing animation setup.");
	    
	    underDevelopment();
	    
	    addSupport(OsType.Windows);
	    addSupport(OsType.MacOS);
	}
	
	/*----------------------------------------------------------------------------------------*/
	/*  O P S                                                                                 */
	/*----------------------------------------------------------------------------------------*/

	/** a
	 * Create and show graphical user interface components to collect information from the 
	 * user to use as input in the next phase of execution for the tool. <P> 
	 * 
	 * @return 
	 *   The phase progress message or <CODE>null</CODE> to abort early.
	 * 
	 * @throws PipelineException 
	 *   If unable to validate the given user input.
	 */  
	public synchronized String
	collectPhaseInput() 
	throws PipelineException 
	{
		if(pSelected.size() != 1)
			throw new PipelineException("Please select only one node.");
		if (!pPrimary.matches(aAnimationPattern))
			throw new PipelineException("The selected Node (" + pPrimary + ") is not a Radar animation node.");
		return ": Creating new animation nodes...";
	}
	
	/**
	 * Perform one phase in the execution of the tool. <P> 
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
	public synchronized boolean
	executePhase
	(
	  MasterMgrClient mclient,
	  QueueMgrClient qclient
	) 
	throws PipelineException
	{
		NodeStatus status = pSelected.get(pPrimary);
		NodeID id = status.getNodeID();
		NodeMod mod = mclient.getWorkingVersion(id);
		Set<String> sources = mod.getSourceNames();
		TreeMap<String, String> charNames = new TreeMap<String, String>();
		for (String source : sources) {
			String fileName = new Path(source).getName();
			if (fileName.equals("renderCam")) {
				charNames.put("cam", source);
				continue;
			}
			
			String[] buffer = fileName.split("_");
			if (buffer.length < 2)
				throw new PipelineException("The source model (" + fileName + ") does not match the naming convention");
			charNames.put(buffer[0], source);
		}
		
		PluginMgrClient plug = PluginMgrClient.getInstance();
		String author = id.getAuthor();
		String view = id.getView();
		
		Path pendingPath = new Path(new Path(new Path(pPrimary).getParentPath().getParentPath(), "assets"), "pending");
		
		String curveTestName = new Path(pendingPath, "curvesTest").toString();
		NodeMod curveTestMod = mclient.getWorkingVersion(author, view, curveTestName);
		{
			boolean checkout = false;
			boolean local = false;
			boolean repository = false;
			TreeSet<String> working = mclient.getWorkingNames(author, view, curveTestName);
			if (working.size() > 0) {
				local = true;
			}
			TreeSet<String> checkedIn = mclient.getCheckedInNames(curveTestName);
			//Check it out 
			if (checkedIn.size() > 0) {
				checkout = true;
				repository = true;
			}
			if (checkout) {
				if (!local)
					mclient.checkOut(author, view, curveTestName, null, CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
				else if (repository) {
					VersionID localVersion = curveTestMod.getWorkingID();
					VersionID latestVersion = mclient.getCheckedInVersionIDs(curveTestName).last();
					if (!localVersion.equals(latestVersion))
						mclient.checkOut(author, view, curveTestName, null, CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
				}
			}
		}
		
		TreeMap<String, String> charNameToCurves = new TreeMap<String, String>();
		for (String charName : charNames.keySet()) {
			String animFile = new Path(pendingPath, charName + "Anim").toString();
			charNameToCurves.put(charName, animFile);
			boolean checkout = false;
			boolean register = false;
			boolean checkLink = false;
			boolean repository = false;
			NodeMod animMod = null;
			
			//Does a working version exist?
			TreeSet<String> working = mclient.getWorkingNames(author, view, animFile);
			if (working.size() > 0) {
				checkLink = true;
				checkout = true;
				animMod = mclient.getWorkingVersion(author, view, animFile);
			}
			//Does a checked-in version exist?
			TreeSet<String> checkedIn = mclient.getCheckedInNames(animFile);
			if (checkedIn.size() > 0) {
				checkLink = true;
				checkout = true;
				repository = true;
			}
			// Make a new node
			if (animMod == null && !repository){
				register = true;
				checkLink = true;
			}
			if (checkout) {
				if (animMod == null)
					mclient.checkOut(author, view, animFile, null, CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
				else if (repository) {
					VersionID localVersion = animMod.getWorkingID();
					VersionID latestVersion = mclient.getCheckedInVersionIDs(animFile).last();
					if (!localVersion.equals(latestVersion))
						mclient.checkOut(author, view, animFile, null, CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
				}
			}
			if (register) {
				BaseEditor mayaEditor = plug.newEditor("MayaProject", new VersionID("2.2.1"), "Temerity");
				animMod = new NodeMod(animFile, new FileSeq(new Path(animFile).getName(), "ma"), null, false, mod.getToolset(), mayaEditor );
				mclient.register(author, view, animMod);
			}
			if (checkLink) {
				animMod =  mclient.getWorkingVersion(author, view, animFile);
				Set<String> animSources = animMod.getSourceNames();
				if (animSources.size() > 1) {
					for (String aSource : animSources)
						mclient.unlink(author, view, animFile, aSource);
					mclient.link(author, view, animFile, pPrimary, LinkPolicy.Dependency);
				}
				else if (animSources.size() == 1) {
					String sourceName = new TreeSet<String>(animSources).first();
					if (!sourceName.equals(pPrimary)) {
						mclient.unlink(author, view, animFile, sourceName);
						mclient.link(author, view, animFile, pPrimary, LinkPolicy.Dependency);
					}
				}
				else
					mclient.link(author, view, animFile, pPrimary, LinkPolicy.Dependency);
			}
			BaseAction animAction = plug.newAction("MayaCurvesExport", new VersionID("2.3.4"), "Temerity");
			animAction.setSingleParamValue("MayaScene", pPrimary);
			animAction.setSingleParamValue("ExportSet", charName + ":SELECTION");
			animAction.setSingleParamValue("CleanUpNamespace", true);
			animAction.setSingleParamValue("BakeAnimation", true);
			animMod.setAction(animAction);
			mclient.modifyProperties(author, view, animMod);
		}
		
		Set<String> curveTestSources = curveTestMod.getSourceNames();
		TreeSet<String> curveTestSourceTest = new TreeSet<String>(curveTestMod.getSourceNames());
		BaseAction curveTestAction = curveTestMod.getAction();
		for (String charName : charNames.keySet()) {
			String rigName = charNames.get(charName);
			String curveName = charNameToCurves.get(charName);
			if (!curveTestSources.contains(rigName)) {
				mclient.link(author, view, curveTestName, rigName, LinkPolicy.Dependency);
				curveTestAction.initSourceParams(rigName);
				curveTestAction.setSourceParamValue(rigName, "SceneType", "Model");
				curveTestAction.setSourceParamValue(rigName, "BuildType", "Reference");
				curveTestAction.setSourceParamValue(rigName, "NameSpace", true);
				curveTestAction.setSourceParamValue(rigName, "PrefixName", charName);
			}
			if (!curveTestSources.contains(curveName)) {
				mclient.link(author, view, curveTestName, curveName, LinkPolicy.Dependency);
				curveTestAction.initSourceParams(curveName);
				curveTestAction.setSourceParamValue(curveName, "SceneType", "Animation");
				curveTestAction.setSourceParamValue(curveName, "BuildType", "Reference");
				curveTestAction.setSourceParamValue(curveName, "NameSpace", true);
				curveTestAction.setSourceParamValue(curveName, "PrefixName", charName);
			}
			curveTestSourceTest.remove(rigName);
			curveTestSourceTest.remove(curveName);
		}
		for (String oldSource : curveTestSourceTest) {
			String name = new Path(oldSource).getName();
			if (!name.equals("animRenderLight"))
				mclient.unlink(author, view, curveTestName, oldSource);
		}
		curveTestMod.setAction(curveTestAction);
		mclient.modifyProperties(author, view, curveTestMod);
		
		return false;
	}
	  
	private static final long serialVersionUID = 1097759261366603005L;
	
	private static final String aAnimationPattern = ".*/shotWork/.*/animationWork";
}
