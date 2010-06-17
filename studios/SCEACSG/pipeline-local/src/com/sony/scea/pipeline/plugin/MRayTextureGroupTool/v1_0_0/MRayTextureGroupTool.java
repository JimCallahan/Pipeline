package com.sony.scea.pipeline.plugin.MRayTextureGroupTool.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

public 
class MRayTextureGroupTool
  extends CommonToolUtils
{
  public 
  MRayTextureGroupTool()
  {
    super("MRayTextureGroup", new VersionID("1.0.0"), "SCEA",
          "Update the secondary sequences on a MRayTextureGroupAction to match its " +
          "sources.");
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
    
    underDevelopment();
  }
  
  @Override
  public synchronized String
  collectPhaseInput() 
    throws PipelineException 
  {
    if(pSelected.size() != 1)
      throw new PipelineException("Must have one and only one node selected.");
    
    if (pPrimary == null)
      throw new PipelineException("This tool must be run on a node.");
    
    return " : Rocking the Casbah since 2006...";
  }
  
  @Override
  public synchronized boolean 
  executePhase
  (
    MasterMgrClient mclient, 
    QueueMgrClient qclient
  )
    throws PipelineException
  {
    NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), pPrimary);
    
    BaseAction act = mod.getAction();
    if (act == null || 
        !act.getPluginID().equals(new PluginID("MRayTextureGroup", 
                                               new VersionID("1.0.0"), "SCEA")))
      throw new PipelineException
        ("This tool can only be run on nodes using the MRayTextureGroup action.");
    
    mod.removeAllSecondarySequences();
    
    for (String source : mod.getSourceNames()) {
      String prefix = new Path(source).getName();
      FileSeq sseq = new FileSeq(prefix, "map");
      mod.addSecondarySequence(sseq);
    }
    
    mclient.modifyProperties(getAuthor(), getView(), mod);
    
    
    return false;
  }
  
  private static final long serialVersionUID = 3092239166645632809L;
}
