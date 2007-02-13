package com.sony.scea.pipeline.plugins.lair.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;

public class LinkClothTool extends BaseTool
{
  public LinkClothTool()
  {
    super("LinkCloth", new VersionID("1.0.0"), "SCEA",
      "Links cloth to a bunch of nodes");
    
    underDevelopment();
    
    plug  = PluginMgrClient.getInstance();
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
  }
  
  public synchronized String collectPhaseInput() throws PipelineException 
  {
    for (String name : pSelected.keySet())
    {
      if (!name.matches(switchPattern))
	throw new PipelineException("Only select switch nodes.  You selected: " + name);
    }
    return ": One thing I've learned in life, is that my Princess will always be in another castle";
  }
  
  public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient) 
  throws PipelineException 
  { 
    for (String switchName : pSelected.keySet())
    {
      NodeStatus stat = pSelected.get(switchName);
      NodeID id = stat.getNodeID();
      pUser = id.getAuthor();
      pView = id.getView();
      NodeMod switchMod = stat.getDetails().getWorkingVersion();
      String pToolset = switchMod.getToolset();
      PluginSet plugs = mclient.getToolsetActionPlugins(pToolset);
      String animName = null;

      {
	TreeSet<String> switchSrcs = new TreeSet<String>(switchMod.getSourceNames());

	for(String src: switchSrcs){
	  if(src.matches(animPattern)){
	    animName = src;
	    continue;
	  }
	}
      }


      Path p = new Path(switchName);
      Path syfRoot = new Path(p.getParentPath().getParentPath(), "syf");
      ArrayList<String> syfDirs = getChildrenDirs(mclient, syfRoot.toString());
      boolean toCache = false;
      for(String dir: syfDirs){
	System.err.println(dir);
	Path dPath = new Path(syfRoot, dir);
	ArrayList<String> simDir = getChildrenNodes(mclient, dPath.toString());
	for(String pCache : simDir){
	  Path cPath = new Path(dPath, pCache);
	  if(cPath.toString().matches(cltPattern)){
	    System.err.println("\t"+cPath.toString());
	    try{
	      VersionID ver = mclient.getCheckedInVersion(cPath.toString(), null).getVersionID();
	      mclient.lock(pUser, pView, cPath.toString(), ver);
	      mclient.link(pUser, pView, switchMod.getName(), cPath.toString(), LinkPolicy.Dependency, LinkRelationship.All, null);
	    } catch (PipelineException e){
	      e.printStackTrace();
	      continue;
	    }
	    toCache = true;
	  }
	}
      }
      BaseAction oldAction = switchMod.getAction();
      String actionName =  "ModelReplace";
      if(toCache){
	actionName += "Syflex";
      }
      VersionID latestVer = plugs.get("SCEA", actionName).last();

      if((oldAction==null) || (!oldAction.getName().equals(actionName))
	  || (!oldAction.getVersionID().equals(latestVer)))
      {
	BaseAction action = plug.newAction(actionName, latestVer, "SCEA");
	action.setSingleParamValues(oldAction);
	action.setSingleParamValue("Source", animName);
	action.setSingleParamValue("Response", "Ignore");
	if(toCache)
	  action.setSingleParamValue(aApplyCache, true);
	switchMod.setAction(action);
	mclient.modifyProperties(pUser, pView, switchMod);
      } else {
	if(!oldAction.getSingleParamValue("Response").equals("Ignore")){
	  oldAction.setSingleParamValue("Response", "Ignore");
	  switchMod.setAction(oldAction);
	  mclient.modifyProperties(pUser, pView, switchMod);
	}//end if
      }//end else
    
    }
    return false;
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
  
  private String pUser;
  private String pView;
  private PluginMgrClient plug;
  
  private static String switchPattern = ".*/production/.*/lgt/.*_switch.*";
  private static String cltPattern = ".*/production/.*/seq.*/syf/.*_clt";
  private static String animPattern = ".*/production/.*/anim/.*_anim";
  private static final String aApplyCache = "ApplyCache";
  private static final long serialVersionUID = -8724014217826970525L;
  
}
