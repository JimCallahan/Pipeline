package com.radar.pipeline.builder.maya2mr.v2_3_2;


import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.QueueMgrClient;
import us.temerity.pipeline.builder.BuilderInformation;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.AssetBuilder;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.DefaultBuilderAnswers;

/*------------------------------------------------------------------------------------------*/
/*   R A D A R   A S S E T   B U I L D E R                                                  */
/*------------------------------------------------------------------------------------------*/


public 
class RadarAssetBuilder
  extends AssetBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public 
  RadarAssetBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    super
      (mclient,
       qclient,
       new DefaultBuilderAnswers(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)), 
       new RadarAssetNames(mclient, qclient),
       new RadarProjectNames(mclient, qclient),
       info);
  }
  private static final long serialVersionUID = -7603820481994992856L;
}
