// $Id: AfterFXTemplateStage.java,v 1.1 2008/02/07 10:11:19 jesse Exp $

package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;


public abstract
class AfterFXTemplateStage
  extends AfterFXSceneStage
{
  AfterFXTemplateStage
  (
    String name,
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String templateNode
  )
    throws PipelineException
  {
    super(name, desc, stageInformation, context, client, 
          nodeName, new PluginContext("AfterFXTemplate", "TheO"));
    addLink(new LinkMod(templateNode, LinkPolicy.Dependency));
    addSingleParamValue(aTemplateName, templateNode);
  }
  
  protected void
  addPlateSource
  (
    String sourceName
  )
    throws PipelineException
  {
    addLink(new LinkMod(sourceName, LinkPolicy.Reference));
    addSourceParamValue(sourceName, aSourceType, aPlate);
    addSource(sourceName);
  }
  
  protected void
  addRotoSource
  (
    String sourceName
  )
    throws PipelineException
  {
    addLink(new LinkMod(sourceName, LinkPolicy.Reference));
    addSourceParamValue(sourceName, aSourceType, aRoto);
    addSource(sourceName);
  }
  
  public static final String aTemplateName = "TemplateName";
  public static final String aSourceType = "SourceType";
  public static final String aPlate = "Plate";
  public static final String aRoto = "Roto";
  private static final long serialVersionUID = -1666133452752068246L;

}
