// $Id: MatteTestStage.java,v 1.1 2008/02/07 10:11:19 jesse Exp $

package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0.stages;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;


public 
class MatteTestStage
  extends AfterFXTemplateStage
{

  public
  MatteTestStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String templateNode,
    List<String> plateFiles,
    String matteFile
  )
    throws PipelineException
  {
    super("MatteTest", 
          "Builds an AfterFX file that layers the matte over top of the plate",
          stageInformation, context, client, nodeName, templateNode);
     addRotoSource(matteFile);
     for (String plateFile : plateFiles)
       addPlateSource(plateFile);
  }
  
  private static final long serialVersionUID = -1430472765050981632L;
}
