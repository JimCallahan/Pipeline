// $Id: ExternalBuilder.java,v 1.2 2009/10/27 05:30:56 jesse Exp $

package com.nathanlove.pipeline.plugin.ExternalCollection.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;

import com.nathanlove.pipeline.plugin.BuilderInfo.*;


public 
class ExternalBuilder
  extends BaseBuilder
{
  public
  ExternalBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    this(mclient, qclient, builderInformation, new StudioDefs("nothurray"));
  }
  
  public
  ExternalBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    StudioDefs defs
  )
    throws PipelineException
  {
    super("External", "The External Builder", 
      mclient, qclient, builderInformation );
    
    pStudioDefs = defs;
    
    addCheckinWhenDoneParam();
    
    addSetupPass(new InfoPass());
    addConstructPass(new BuildPass());
    
    AdvancedLayoutGroup layout = 
      new AdvancedLayoutGroup
      ("Builder Information", 
       "The pass where all the basic information about the asset is collected " +
       "from the user.", 
       "BuilderSettings", 
       true);
    layout.addEntry(1, aUtilContext);
    layout.addEntry(1, null);
    layout.addEntry(1, aCheckinWhenDone);
    layout.addEntry(1, aActionOnExistence);
    layout.addEntry(1, aReleaseOnError);
  
    PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
    setLayout(finalLayout);
  }
  
  private 
  class InfoPass
    extends SetupPass
  {
    public 
    InfoPass()
    {
      super("Info Pass", 
            "Info pass for the AssetBuilder");
      
    }
    
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      UtilContext save = pContext;
      validateBuiltInParams();
      setContext(save);
      
      pLog.logAndFlush(Kind.Ops, Level.Info, "Hurray here I am in the External Builder");
      pLog.logAndFlush(Kind.Ops, Level.Info, "The project is " + pStudioDefs.getProjectName());
    }

    private static final long serialVersionUID = 321191179261260073L;
  }
  
  private 
  class BuildPass
    extends ConstructPass
  {
    private
    BuildPass()
    {
      super("BuildPass", "Makes stuff.");
    }
    
    @Override
    public void buildPhase()
      throws PipelineException
    {
      String nodeName = "/tests/files/doesnotExist";
      
      addToQueueList(nodeName);
      addToCheckInList(nodeName);
    }
  }

  private static final long serialVersionUID = -6656333311497442069L;

  private StudioDefs pStudioDefs;

}
