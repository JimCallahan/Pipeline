package us.temerity.pipeline.plugin.TaskCollection.v2_4_28;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   T A S K   C O L L E C T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The basic publish and verify builders for v2.4.28 task setups.
 */
public 
class TaskCollection
  extends BaseBuilderCollection
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TaskCollection()
  {
    super("Task", new VersionID("2.4.28"), "Temerity", 
          "The basic publish and verify builders for v2.4.28 task setups.");
    
    LayoutGroup layout = new LayoutGroup(true);
    layout.addEntry("VerifyTask");
    layout.addEntry("RunVerify");
    layout.addSeparator();
    layout.addEntry("PublishTask");
    layout.addEntry("RunPublish");
    setLayout(layout);
    
    underDevelopment();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/


  /**
   * Returns a list of all the builders that the this collection has in it, followed
   * by the full classpath to the class file that can be used to instantiate that builder.
   * <p>
   * All Builder Collections needs to override this method to return the list of builders
   * that they provide.
   * 
   * @return
   *   A mapping of Builder names to the classpath for the Builder.  By default, 
   *   this returns an empty TreeMap.
   */
  @Override
  public TreeMap<String, String> 
  getBuildersProvided()
  {
    String pkg = "us.temerity.pipeline.plugin.TaskCollection.v2_4_28.";

    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("PublishTask", pkg + "PublishTaskBuilder");
    toReturn.put("RunPublish", pkg + "RunPublishBuilder");
    toReturn.put("VerifyTask", pkg + "VerifyTaskBuilder");
    toReturn.put("RunVerify", pkg + "RunVerifyBuilder");
    
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2241859362406456400L;
}
