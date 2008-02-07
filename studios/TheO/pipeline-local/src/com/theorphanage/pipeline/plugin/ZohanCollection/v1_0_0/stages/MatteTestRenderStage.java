// $Id: MatteTestRenderStage.java,v 1.1 2008/02/07 10:11:19 jesse Exp $

package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0.stages;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;


public 
class MatteTestRenderStage
  extends AfterFXRenderImgStage
{
  public 
  MatteTestRenderStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    FrameRange range,
    String sourceScene,
    List<String> sequences
  )
    throws PipelineException
  {
    super("MatteTestRender", "Renders the matte test scene",
         stageInformation, context, client,
         nodeName, range, 4, "jpg", sourceScene, "MATTE QC");
    for (String sequence : sequences) {
      addLink(new LinkMod(sequence, LinkPolicy.Dependency, LinkRelationship.OneToOne, 0));
    }
  }
  private static final long serialVersionUID = -1003202979941490619L;
}
