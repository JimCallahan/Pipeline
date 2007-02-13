package com.sony.scea.pipeline.plugins.lair.v1_0_0;

import java.io.File;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.NodeTreeComp.State;

public class BuildShaveRenderTreeTool extends BaseTool
{
  private static final long serialVersionUID = 1054502111323873789L;
  public BuildShaveRenderTreeTool()
  {
    super("BuildShaveRenderTree", new VersionID("1.0.0"), "SCEA",
      "Builds a shave render tree.");
    
    characterList = new TreeSet<String>();
    plug = PluginMgrClient.getInstance();

    underDevelopment();
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }
  
  public synchronized String collectPhaseInput() throws PipelineException
  {
    if ( pPrimary == null )
      throw new PipelineException("You need to have a node selected");
    if ( pSelected.size() != 1 )
      throw new PipelineException("You can only select one node.");
    
    if (!pPrimary.matches(switchPattern))
      throw new PipelineException("You've got to select a switch light node.  thank you very much");
    
    NodeStatus stat = pSelected.get(pPrimary);
    NodeID id = stat.getNodeID();
    pUser = id.getAuthor();
    pView = id.getView();
    
    String shotName = new Path(pPrimary).getName().replaceAll("_switchLgt", "");
    switchHairName = pPrimary.replaceAll("_switchLgt", "_switchHair");
    lgtHairName = pPrimary.replaceAll("_switchLgt", "_hair");
    imgName = new Path(new Path(new Path(new Path(pPrimary).getParentPath().getParentPath(), "img"), "test"), shotName).toString();
    
    System.err.println(imgName);
    String imgShotName = shotName.replaceAll("seq", "s") + "_hair";
    hairImageName = new Path(new Path(new Path(imgName).getParentPath().getParentPath(), "hair"), imgShotName).toString();
    
    NodeCommon mod = stat.getDetails().getWorkingVersion();
    if (mod == null)
      mod = stat.getDetails().getLatestVersion();
    for (String source : mod.getSourceNames())
    {
      Path p = new Path(source);
      String name = p.getName();
      if (hairChars.contains(name))
	characterList.add(source);
    }
    
    return ": I like Frenchmen very much, because even when they insult you they do it so nicely. -Josephine Baker";
  }
  
  public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
  throws PipelineException
  {
    String toolset = mclient.getDefaultToolsetName();
    int start = 1;
    int end = 10;
    if(doesNodeExists(mclient, imgName))
    {
      NodeVersion ver = mclient.getCheckedInVersion(imgName, null);
      FrameRange fr = ver.getPrimarySequence().getFrameRange();
      start = fr.getStart();
      end = fr.getEnd();
    }
    jcheckOut(mclient, pUser, pView, hairRenderMel, null, CheckOutMode.OverwriteAll, CheckOutMethod.FrozenUpstream);
    jcheckOut(mclient, pUser, pView, switchHairMel, null, CheckOutMode.OverwriteAll, CheckOutMethod.FrozenUpstream);
    {
      jcheckOut(mclient, pUser, pView, pPrimary, null, CheckOutMode.OverwriteAll, CheckOutMethod.FrozenUpstream);
      mclient.clone(pUser, pView, pPrimary, switchHairName);
      
      for (String charName : characterList)
      {
	String charHair = charName + "_hair";
	mclient.unlink(pUser, pView, switchHairName, charName);
	jcheckOut(mclient, pUser, pView, charHair, null, CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
	mclient.link(pUser, pView, switchHairName, charHair, LinkPolicy.Dependency, LinkRelationship.All, null);
      }
      
      NodeMod switchHairMod = mclient.getWorkingVersion(pUser, pView, switchHairName);
      if (switchHairMod.getSourceNames().contains(switchMel))
	mclient.unlink(pUser, pView, switchHairName, switchMel);
      mclient.link(pUser, pView, switchHairName, switchHairMel, LinkPolicy.Dependency, LinkRelationship.All, null);

      BaseAction act = switchHairMod.getAction(); 
      BaseAction newAct = plug.newAction("ModelReplaceCache", null, "SCEA");
      newAct.setSingleParamValues(act);
      newAct.setSingleParamValue("TargetSuffix", "hair");
      newAct.setSingleParamValue("PostReplaceMEL", switchHairMel);
      switchHairMod.setAction(newAct);
      mclient.modifyProperties(pUser, pView, switchHairMod);
    }
    BaseEditor mayaEdit = plug.newEditor("MayaProject", null, "Temerity");
    {
      NodeMod lgtMod =  registerNode(mclient, lgtHairName, "ma", toolset, mayaEdit);
      mclient.link(pUser, pView, lgtHairName, switchHairName, LinkPolicy.Dependency, LinkRelationship.All, null);
      BaseAction lgtAct = plug.newAction("MayaBuild", null, "Temerity");
      lgtAct.initSourceParams(switchHairName);
      lgtAct.setSourceParamValue(switchHairName, "PrefixName", "switch");
      lgtAct.setSourceParamValue(switchHairName, "BuildType", "Reference");
      lgtAct.setSourceParamValue(switchHairName, "NameSpace", true);
      lgtAct.setSingleParamValue("TimeUnits", "NTSC (30 fps)");
      lgtAct.setSingleParamValue("StartFrame", start);
      lgtAct.setSingleParamValue("EndFrame", end);
      lgtMod.setAction(lgtAct);
      JobReqs req = lgtMod.getJobRequirements();
      req.addSelectionKey("Lair");
      req.addSelectionKey("LinuxOnly");
      lgtMod.setJobRequirements(req);
      mclient.modifyProperties(pUser, pView, lgtMod);
    }
    BaseEditor fcheckEdit = plug.newEditor("FCheck", null, "Temerity");
    {
      NodeMod imgMod = registerSequence(mclient, hairImageName, 4, "iff", toolset, fcheckEdit, start, end, 1);
      mclient.link(pUser, pView, hairImageName, lgtHairName, LinkPolicy.Dependency, LinkRelationship.All, null);
      mclient.link(pUser, pView, hairImageName, hairRenderMel, LinkPolicy.Dependency, LinkRelationship.All, null);
      BaseAction imgAct = plug.newAction("MayaRender", null, "Temerity");
      imgAct.setSingleParamValue("Renderer", "Software");
      imgAct.setSingleParamValue("Processors", 0);
      imgAct.setSingleParamValue("MayaScene", lgtHairName);
      imgAct.setSingleParamValue("PreRenderMEL", hairRenderMel);

      imgMod.setAction(imgAct);
      mclient.modifyProperties(pUser, pView, imgMod);

      JobReqs req = imgMod.getJobRequirements();
      req.addSelectionKey("Lair");
      req.addSelectionKey("Lighting");
      req.addSelectionKey("LinuxOnly");
      req.setMinDisk(536870912L);
      req.setMinMemory(3221225472L);
      imgMod.setJobRequirements(req);
      mclient.modifyProperties(pUser, pView, imgMod);
    }
    return false;
  }
  
  private NodeMod registerNode(MasterMgrClient client, String name, String extention,
      String toolset, BaseEditor editor) throws PipelineException
  {
    File f = new File(name);
    FileSeq animSeq = new FileSeq(f.getName(), extention);
    NodeMod animNode = new NodeMod(name, animSeq, null, toolset, editor);
    client.register(pUser, pView, animNode);
    return animNode;
  }
  
  public NodeMod registerSequence(MasterMgrClient mclient, String name, int pad, 
      String extention, String toolset, BaseEditor editor, int startF, int endf, int byF) 
  throws PipelineException
  {
    Path p = new Path(name);
    FilePattern pat = new FilePattern(p.getName(), pad, extention);
    FrameRange range = new FrameRange(startF, endf, byF);
    FileSeq animSeq = new FileSeq(pat, range);
    NodeMod animNode = new NodeMod(name, animSeq, null, toolset, editor);
    mclient.register(pUser, pView, animNode);
    return animNode;
  }//end registerSequenc

  public boolean doesNodeExists(MasterMgrClient mclient, String name)
    throws PipelineException
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

  private String switchHairName;
  private String lgtHairName;
  private String hairImageName;
  private String imgName;
  private String pUser;
  private String pView;
  
  private TreeSet<String> characterList;
  
  private PluginMgrClient plug;
  
  private final static String switchPattern = "/projects/.*/production/.*/lgt/.*_switchLgt";
  
  private final static String switchMel= "/projects/lr/assets/tools/mel/switchLightMel";
  private final static String switchHairMel= "/projects/lr/assets/tools/mel/switchHairMel";
  private final static String hairRenderMel = "/projects/lr/assets/tools/render/render-hair";
  private static TreeSet<String> hairChars = new TreeSet<String>();
  {
    hairChars.add("attakai2");
    hairChars.add("guardian2");
    hairChars.add("hybridRohn");
    hairChars.add("jevon");
    hairChars.add("kobakai2");
    hairChars.add("loden");
    hairChars.add("nakedRohn");
    hairChars.add("prophet");
    hairChars.add("renkai2");
    hairChars.add("rohn");
    hairChars.add("talan");
  }
  
  private static TreeSet<String> hairSimChars = new TreeSet<String>();
  {
    hairSimChars.add("attakai2");
    hairSimChars.add("kobakai2");
  }
}
