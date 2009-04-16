// $Id: InternalBuilder.java,v 1.1 2009/04/16 17:57:25 jesse Exp $

package com.nathanlove.pipeline.plugin.InternalCollection.v1_0_0;

import java.lang.reflect.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;

import com.nathanlove.pipeline.plugin.BuilderInfo.*;


public 
class InternalBuilder
  extends BaseBuilder
{
  public
  InternalBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    this(mclient, qclient, builderInformation, new StudioDefs("hurray"));
  }
  
  public
  InternalBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    StudioDefs defs
  )
    throws PipelineException
  {
    super("Internal", "The Internal Builder", 
      mclient, qclient, builderInformation );
    
    pStudioDefs = defs;
    
    addCheckinWhenDoneParam();
    
    addSetupPass(new InfoPass());
    
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
      validateBuiltInParams();
      
      pLog.logAndFlush(Kind.Ops, Level.Info, "Hurray here I am in the Internal Builder");
      pLog.logAndFlush(Kind.Ops, Level.Info, "The project is " + pStudioDefs.getProjectName());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      BaseBuilderCollection collect = 
        pPlug.newBuilderCollection("ExternalCollection", new VersionID("1.0.0"), "NathanLove");
      Class argv[] = 
      {
        MasterMgrClient.class,
        QueueMgrClient.class,
        BuilderInformation.class,
        StudioDefs.class
      };
      Constructor c = collect.getBuilderConstructor("External", argv);
      Object args[] =
      {
        pClient,
        pQueue,
        getBuilderInformation(),
        pStudioDefs
      };
      try {
        BaseBuilder builder = (BaseBuilder) c.newInstance(args);
        addSubBuilder(builder);
      }
      catch (Exception ex) {
        String message = Exceptions.getFullMessage("Error creating the builder", ex);
        throw new PipelineException(message);
      }
      
    }
    
    private static final long serialVersionUID = -8900490049414099723L;
  }

  private static final long serialVersionUID = -4062492704933624581L;


  private StudioDefs pStudioDefs;
}
