// $Id: TestAction.java,v 1.4 2004/07/24 18:14:11 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   A C T I O N                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A test action with at lease one of each type of parameter.
 */
public
class TestAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TestAction() 
  {
    super("Test", 
	  "A test action with at lease one of each type of parameter.");

    {
      BaseActionParam param = 
	new IntegerActionParam("SomeInteger", 
			       "An integer parameter.",
			       123);
      addSingleParam(param);
    }

    {
      BaseActionParam param = 
	new DoubleActionParam("SomeDouble", 
			      "An double parameter.",
			      123.456);
      addSingleParam(param);
    }

    {
      BaseActionParam param = 
	new StringActionParam("SomeString",
			      "A short string parameter.",
			      "test");
      addSingleParam(param);
    }

    {
      BaseActionParam param = 
	new TextActionParam("SomeText",
			    "An long string parameter.",
			    "Some testing text...");
      addSingleParam(param);
    }

    {
      BaseActionParam param = 
	new EnumActionParam("SomeEnum",
			    "An enum parameter.",
			    TestColor.Red, 
			    TestColor.all(),
			    TestColor.titles());
      addSingleParam(param);
    }
    
    {
      BaseActionParam param = 
	new LinkActionParam("SomeLink",
			    "An upstream link parameter.",
			    null);
      addSingleParam(param);
    }

    // add per-link parameters
  }


  /*----------------------------------------------------------------------------------------*/
  /*   B A S E   A C T I O N   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this action support per-source parameters?  
   */ 
  public boolean 
  supportsSourceParams()
  {
    return true;
  }
  
  /**
   * Get an initial set of action parameters associated with an upstream node. 
   */ 
  public TreeMap<String,BaseActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,BaseActionParam> params = new TreeMap<String,BaseActionParam>();
    
    {
      BaseActionParam param = 
	new IntegerActionParam("AnotherInteger", 
			       "An integer parameter.",
			       123);
      params.put(param.getName(), param);
    }

    {
      BaseActionParam param = 
	new DoubleActionParam("AnotherDouble", 
			      "An double parameter.",
			      123.456);
      params.put(param.getName(), param);
    }

    {
      BaseActionParam param = 
	new StringActionParam("AnotherString",
			      "A short string parameter.",
			      "test");
      params.put(param.getName(), param);
    }

    {
      BaseActionParam param = 
	new TextActionParam("AnotherText",
			    "An long string parameter.",
			    "Some testing text...");
      params.put(param.getName(), param);
    }

    {
      BaseActionParam param = 
	new EnumActionParam("AnotherEnum",
			    "An enum parameter.",
			    TestColor.Red, 
			    TestColor.all(),
			    TestColor.titles());
      params.put(param.getName(), param);
    }
    
    {
      BaseActionParam param = 
	new LinkActionParam("AnotherLink",
			    "An upstream link parameter.",
			    null);
      params.put(param.getName(), param);
    }

    return params;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcess SubProcess} instance which when executed will fulfill
   * the given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda.
   */
  public SubProcess
  prep
  (
   ActionAgenda agenda
  )
    throws PipelineException
  {
    throw new PipelineException("Not implemented yet...");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1201400081088512588L;

}



