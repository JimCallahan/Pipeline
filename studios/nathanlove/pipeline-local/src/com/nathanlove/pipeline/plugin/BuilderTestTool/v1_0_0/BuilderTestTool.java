// $Id: BaseBuilder.java,v 1.33 2007/11/01 19:08:53 jesse Exp $

package com.nathanlove.pipeline.plugin.BuilderTestTool.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.plugin.*;


public 
class BuilderTestTool
  extends CommonToolUtils
{
  public 
  BuilderTestTool()
  {
    super("BuilderTest", new VersionID("1.0.0"), "NathanLove",
          "Tests the new builder logging code.");
    
    
    underDevelopment();
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }
  
  @Override
  public synchronized String 
  collectPhaseInput()
    throws PipelineException
  {
    return "DO IT!!!!!!!!!!!!!!";
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
    
    PluginMgrClient pclient = PluginMgrClient.getInstance();

    BaseBuilderCollection collection = 
      pclient.newBuilderCollection("BaseBuilders", new VersionID("1.0.0"), "NathanLove");

    BuilderInformation info = 
      new BuilderInformation("Astor", true, false, true, true, new MultiMap<String, String>());

    LogMgr log = LogMgr.getInstance("Astor");
    log.setLevel(Kind.Ops, Level.Finest);
    log.setLevel(Kind.Bld, Level.Finest);
    
    MasterMgrClient newMclient = new MasterMgrClient();
    QueueMgrClient newQclient = new QueueMgrClient();
    
    BaseBuilder builder = 
      collection.instantiateBuilder("Asset", newMclient, newQclient, info);
    builder.run();
    
    return false;
  }

  
  private static final long serialVersionUID = 848425461327593328L;

}
