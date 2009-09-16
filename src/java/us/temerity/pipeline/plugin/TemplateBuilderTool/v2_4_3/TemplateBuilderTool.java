// $Id: TemplateBuilderTool.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateBuilderTool.v2_4_3;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_3.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   B U I L D E R   T O O L                                              */
/*------------------------------------------------------------------------------------------*/

public 
class TemplateBuilderTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public 
  TemplateBuilderTool()
  {
    super("TemplateBuilder", new VersionID("2.4.3"), "Temerity", 
          "Launches the template builder on a template node.");
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
  }

  @Override
  public synchronized String 
  collectPhaseInput()
    throws PipelineException
  {
    if (pSelected.size() != 1 || pPrimary == null)
      throw new PipelineException("Please only select one node to run the template builder.");
    
    return ": Launching the Template Builder";
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
    
    
    BuilderInformation info = 
      new BuilderInformation(true, false, false, false, new MultiMap<String, String>());
    TemplateInfoBuilder builder = 
      new TemplateInfoBuilder(mclient, qclient, info, pPrimary, getAuthor(), getView());
    
    Level opLevel = LogMgr.getInstance().getLevel(Kind.Ops);
    switch(opLevel) {
    case Info:
    case Warning:
    case Severe:
      LogMgr.getInstance().setLevel(Kind.Ops, Level.Fine);
      break;
    }
    
    builder.run();
    
    
    
    return false;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7615333727009770310L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  
}
