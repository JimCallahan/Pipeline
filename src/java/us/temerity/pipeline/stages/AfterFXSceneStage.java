// $Id: AfterFXSceneStage.java,v 1.2 2008/02/10 05:34:51 jim Exp $

package us.temerity.pipeline.stages;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;


public 
class AfterFXSceneStage
  extends StandardStage
  implements FinalizableStage
{
  protected 
  AfterFXSceneStage
  (
    String name,
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    PluginContext action
  )
    throws PipelineException
  {
    super(name, desc, stageInformation, context, client,
      nodeName, "aep", null, action);
    pSources = new TreeSet<String>();
  }
  
  protected void
  addSource
  (
    String source
  )
  {
    if (source != null)
      pSources.add(source);
  }
  
  /**
   * Finishes off the work of the stage after it has been queued.
   * <p>
   * Disables the action on the node and flips all the image sources to Associations.
   * 
   */
  public void 
  finalizeStage() 
    throws PipelineException
  {
    disableAction();
    for (String source : pSources)
      pClient.link(getAuthor(), getView(), pRegisteredNodeName, source, LinkPolicy.Association);
  }
  
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aAfterFXScene;
  }

  static final long serialVersionUID = -7039790058064568645L;

  private TreeSet<String> pSources; 
}
