// $Id: TemplateGlueCollection.java,v 1.1 2009/01/05 17:22:38 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueCollection.v2_4_5;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   G L U E   C O L L E C T I O N                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Collection that exists for running template builders using the plbuilder command-line tool.
 */
public 
class TemplateGlueCollection
  extends BaseBuilderCollection
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateGlueCollection()
  {
    super("TemplateGlue", new VersionID("2.4.5"), "Temerity", 
          "Collection that exists for running template builders using the plbuilder " +
          "command-line tool.");
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
    
    LayoutGroup layout = new LayoutGroup(true);
    layout.addEntry("TemplateGlue");
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
    String pkg = "us.temerity.pipeline.plugin.TemplateGlueCollection.v2_4_5.";

    TreeMap<String, String> toReturn = new TreeMap<String, String>();
    toReturn.put("TemplateGlue", pkg + "TemplateGlueBuilder");
    
    return toReturn;
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3277930984681824309L;
}
