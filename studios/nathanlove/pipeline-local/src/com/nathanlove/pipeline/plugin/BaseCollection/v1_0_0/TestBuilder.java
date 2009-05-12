// $Id: TestBuilder.java,v 1.1 2009/05/12 03:29:58 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;


public 
class TestBuilder
  extends BaseBuilder
{
  public
  TestBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    super("Test", "Builder to test some features", mclient, qclient, builderInformation);

    
    addCheckinWhenDoneParam();
    
    {
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
   
      addSetupPass(new InformationPass());
      
      PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
      setLayout(finalLayout);
    }
  }
  
  private 
  class InformationPass
    extends SetupPass
  {
    public 
    InformationPass()
    {
      super("Information Pass", 
            "Information pass for the AssetBuilder");
    }
    
    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      addSubBuilder("BaseBuilders", null, "NathanLove", "Asset");
      addSubBuilder("TemplateGlue", null, null, "TemplateGlueBuilder");
    }

    private static final long serialVersionUID = 1397957757586237420L;
  }

  private static final long serialVersionUID = 8281142632941467767L;
}
