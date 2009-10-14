// $Id: TemplateBuilderTool.java,v 1.1 2009/10/14 18:11:43 jesse Exp $

package us.temerity.pipeline.plugin.TemplateBuilderTool.v2_4_12;

import java.lang.reflect.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.*;
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
    super("TemplateBuilder", new VersionID("2.4.12"), "Temerity", 
          "Launches the template builder on a template node.");
    
    underDevelopment();
    
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
  
  @SuppressWarnings({ "incomplete-switch", "unchecked" })
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
    
    BaseBuilderCollection collection = 
      PluginMgrClient.getInstance().newBuilderCollection
        ("Template", new VersionID("2.4.12"), "Temerity");
    
    Class[] arguments = {MasterMgrClient.class, QueueMgrClient.class, 
                         BuilderInformation.class, String.class, String.class, String.class};
    
    Constructor construct = collection.getBuilderConstructor("TemplateInfoBuilder", arguments);
    
    BaseBuilder builder;
    try {
      builder = (BaseBuilder) construct.newInstance
        (mclient, qclient, info, pPrimary, getAuthor(), getView());
    }
    catch (Exception ex) {
      String message = Exceptions.getFullMessage
        ("Could not create TemplateInfoBuilder constructor", ex);
      throw new PipelineException(message);
    }
    
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

  private static final long serialVersionUID = -1240521156104955998L;
}
