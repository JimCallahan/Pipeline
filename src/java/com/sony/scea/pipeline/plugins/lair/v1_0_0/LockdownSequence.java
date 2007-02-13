package com.sony.scea.pipeline.plugins.lair.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.NodeTreeComp.State;

public class LockdownSequence extends BaseTool
{
 
  public LockdownSequence()
  {
    super("LockdownSequence", new VersionID("1.0.0"), "SCEA",
      "Locks down a whole sequence");
    
    underDevelopment();
    
    pPhase = 1;
    
    System.err.println("Collect "+ pPhase);
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
  }
  
  public synchronized String collectPhaseInput() throws PipelineException 
  {
    switch (pPhase)
    {
      case 1:
	return collectFirstPhaseInput();
      case 2:
	return collectSecondPhaseInput();
      case 3:
	return collectFourthPhaseInput();
      case 4:
	return collectFourthPhaseInput();
	default:
	  throw new PipelineException("Bad phase");
    }
    
  }
  
  public synchronized String collectFirstPhaseInput() throws PipelineException {
    if(pPrimary==null)
      throw new PipelineException("Please select a node.");

    if(!pPrimary.matches(switchPattern))
      throw new PipelineException("This tool will only work on a switchLgt node!");
    
    if (pSelected.size() != 1)
      throw new PipelineException("Only select one node");

    NodeID nodeID = pSelected.get(pPrimary).getNodeID();
    pUser = nodeID.getAuthor();
    pView = nodeID.getView();
    
    Path switchPath = new Path(pPrimary);
    String temp = switchPath.getName();
    String buffer[] = temp.split("_");
    pSeqName = buffer[0];
    pTopLevelSeqDir = switchPath.getParentPath().getParentPath().getParentPath(); 
    return ": I don't like sand. It's coarse, rough, irritating and it gets everywhere";
  }

  public synchronized String collectSecondPhaseInput() throws PipelineException {
    return ": Checking Out Models";
  }

  public synchronized String collectThirdPhaseInput() throws PipelineException {
    return ": Checking Out Scenes";
  }
  
  public synchronized String collectFourthPhaseInput() throws PipelineException {
    return ": Syncing Models";
  }

  public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient) 
  throws PipelineException 
  { 
    switch (pPhase)
    {
      case 1:
	return executeFirstPhase(mclient, qclient);
      case 2:
	return executeSecondPhase(mclient, qclient);
      case 3:
	return executeThirdPhase(mclient, qclient);
      case 4:
	return executeFourthPhase(mclient, qclient);
	default:
	  throw new PipelineException("Bad phase");
    }
  }
  
  public synchronized boolean executeFirstPhase(MasterMgrClient mclient, QueueMgrClient qclient) 
  throws PipelineException 
  { 
    pAssets = new TreeSet<String>();
    pToolset = mclient.getDefaultToolsetName();

    pShotList = getChildrenDirs(mclient, pTopLevelSeqDir.toString());
    for (String shot : pShotList)
    {
      Path animPath = new Path(new Path(new Path(pTopLevelSeqDir, shot), "anim"), pSeqName + "_" + shot + "_anim");
      NodeVersion ver = mclient.getCheckedInVersion(animPath.toString(), null);
      for (String source : ver.getSourceNames())
      {
	if (source.matches(loresPattern))
	{
	  source = source.replaceAll("_lr", "");
	  pAssets.add(source);
	}
      }
    }
    jcheckOut(mclient, pUser, pView, switchMel, null, CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
    pPhase++;
    return true;
  }
  
  public synchronized boolean executeSecondPhase(MasterMgrClient mclient, QueueMgrClient qclient) 
  throws PipelineException 
  { 
    for (String asset : pAssets)
    {
      String lrAsset = asset + "_lr";
      String rigName = null;
      {
	Path asp =  new Path(asset);
	String name = asp.getName();
	rigName = new Path(new Path(asp.getParentPath(), "rig"), name + "_rig").toString();
	if (!doesNodeExists(mclient, rigName))
	  rigName = null;
      }

      NodeVersion lrV = mclient.getCheckedInVersion(lrAsset, null);
      NodeVersion hiV = mclient.getCheckedInVersion(asset, null);
      if (rigName == null)
      {
	Set<String> sources = hiV.getSourceNames();
	for (String source : sources)
	{
	  if (source.contains("/rig/"))
	    rigName = source;
	  break;
	}
      }
      System.err.println(asset);
      System.err.println(rigName);
      
      LinkVersion lrLink = lrV.getSource(rigName);
      LinkVersion hrLinkVersion = hiV.getSource(rigName);
      VersionID lrVersion = null;
      if (lrLink != null)
	lrVersion = lrLink.getVersionID();
      VersionID hrVersion = hrLinkVersion.getVersionID();
      int compare = hrVersion.compareTo(lrVersion);
      if (compare < 0)
      {
	jcheckOut(mclient, pUser, pView, asset, null, CheckOutMode.OverwriteAll, CheckOutMethod.FrozenUpstream);
	jcheckOut(mclient, pUser, pView, lrAsset, null, CheckOutMode.OverwriteAll, CheckOutMethod.FrozenUpstream);
      }
      else
      {
	jcheckOut(mclient, pUser, pView, lrAsset, null, CheckOutMode.OverwriteAll, CheckOutMethod.FrozenUpstream);
	jcheckOut(mclient, pUser, pView, asset, null, CheckOutMode.OverwriteAll, CheckOutMethod.FrozenUpstream);
      }
    }

    pPhase++;
    return true;
  }

  public synchronized boolean executeThirdPhase(MasterMgrClient mclient, QueueMgrClient qclient) 
  throws PipelineException 
  { 
    pRoots.clear();
    for (String shot : pShotList)
    {
      String switchName = new Path(new Path(new Path(pTopLevelSeqDir, shot), "lgt"), pSeqName + "_" +  shot + "_switchLgt").toString();
      String animName = new Path(new Path(new Path(pTopLevelSeqDir, shot), "anim"), pSeqName + "_" + shot + "_anim").toString();
      jcheckOut(mclient, pUser, pView, animName, null, CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
      jcheckOut(mclient, pUser, pView, switchName, null, CheckOutMode.KeepModified, CheckOutMethod.PreserveFrozen);
      pRoots.add(switchName.toString());
    }
    pPhase ++;
    return true;
  }
  
  public synchronized boolean executeFourthPhase(MasterMgrClient mclient, QueueMgrClient qclient) 
  throws PipelineException 
  {
    for (String shot : pShotList)
    {
      String switchName = new Path(new Path(new Path(pTopLevelSeqDir, shot), "lgt"), pSeqName + "_" +  shot + "_switchLgt").toString();
      String animName = new Path(new Path(new Path(pTopLevelSeqDir, shot), "anim"), pSeqName + "_" + shot + "_anim").toString();
      NodeMod switchMod = mclient.getWorkingVersion(pUser, pView, switchName.toString());
      NodeMod animMod = mclient.getWorkingVersion(pUser, pView, animName.toString());

      if ( !switchMod.getToolset().equals(pToolset) )
      {
	switchMod.setToolset(pToolset);
	mclient.modifyProperties(pUser, pView, switchMod);
      }

      { //ModelSync
	TreeSet<String> pHiresSrcs = new TreeSet<String>();
	TreeSet<String> pLoresSrcs = new TreeSet<String>();
	for (String src : animMod.getSourceNames())
	  if (src.matches(loresPattern))
	    pLoresSrcs.add(src);
	for (String src : switchMod.getSourceNames())
	  if (src.matches(hiresPattern))
	    if(pLoresSrcs.contains(src+"_lr"))
	      pHiresSrcs.add(src);
	for(String lores: pLoresSrcs) 
	{
	  String hr = lores.replace("_lr","");
	  if(!pHiresSrcs.contains(hr))
	    pHiresSrcs.add(hr);
	}

	{
	  switchMod = mclient.getWorkingVersion(pUser, pView, switchName);
	  TreeSet<String> switchSrcs = new TreeSet<String>(switchMod.getSourceNames());
	  for(String src: pHiresSrcs){
	    if((src.matches(hiresPattern) &&(!switchSrcs.contains(src)))){
	      mclient.link(pUser, pView, switchName, src, LinkPolicy.Dependency, LinkRelationship.All, null);
	      switchSrcs.add(src);
	    }
	  }
	}
	{
	  switchMod = mclient.getWorkingVersion(pUser, pView, switchName);
	  TreeSet<String> switchSrcs = new TreeSet<String>(switchMod.getSourceNames());
	  for(String src: switchSrcs){
	    if((src.matches(hiresPattern) &&(!pHiresSrcs.contains(src)))){
	      mclient.unlink(pUser, pView, switchName, src);	
	    }
	  }
	}
      } //ModelSync

      {
	mclient.link(pUser, pView, switchName, switchMel, LinkPolicy.Dependency, LinkRelationship.All, null);
	BaseAction act = switchMod.getAction();
	VersionID id = act.getVersionID();
	if (act.getName().equals("ModelReplace") && !id.equals(new VersionID("1.2.0")))
	{
	  BaseAction newAct = PluginMgrClient.getInstance().newAction("ModelReplace", new VersionID("1.2.0"), "SCEA");
	  newAct.setSingleParamValues(act);
	  act = newAct;
	}
	act.setSingleParamValue("PostReplaceMEL", switchMel);
	switchMod.setAction(act);
	mclient.modifyProperties(pUser, pView, switchMod);
      }
    }
    return false;
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
  
  private ArrayList<String> getChildrenDirs(MasterMgrClient mclient, String start)
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
  
  private TreeSet<String> pAssets;
  private Path pTopLevelSeqDir;
  private String pSeqName;
  private ArrayList<String> pShotList;
  private String pUser;
  private String pView;
  private String pToolset;
  
  private static String switchPattern = ".*/production/.*/lgt/.*_switch.*";
  private static String hiresPattern = ".*/assets/(character|set|prop)/.*";
  private static String loresPattern = hiresPattern+"_lr";
  private static final long serialVersionUID = -2997206418050444495L;
  private static final String switchMel = "/projects/lr/assets/tools/mel/switchLightMel";
  private int pPhase;
  
  
}
