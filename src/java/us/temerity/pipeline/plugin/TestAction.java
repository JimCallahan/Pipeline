// $Id: TestAction.java,v 1.2 2004/06/22 19:36:44 jim Exp $

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
   * Construct a {@link SubProcess SubProcess} instance which when executed will 
   * regenerate the given file sequences for the target node. <P>
   * 
   * @param jobID  
   *   A unique job identifier.
   * 
   * @param name  
   *   The fully resolved name of the target node. 
   * 
   * @param author  
   *   The name of the user which submitted the job.
   * 
   * @param primaryTarget  
   *   The primary file sequence to generate.
   *
   * @param secondaryTargets  
   *   The secondary file sequences to generate.
   *
   * @param primarySources  
   *   A table of primary file sequences associated with each dependency.
   *
   * @param secondarySources  
   *   The table of secondary file sequences associated with each dependency.
   *
   * @param env  
   *   The environment under which the action is run.  
   * 
   * @param dir  
   *   The working directory where the action is run.
   * 
   * @return 
   *   The SubProcess which will regenerate the target file sequences.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   file sequence arguments.
   */
  public SubProcess
  prep
  (
   int jobID,                
   String name,              
   String author,            
   FileSeq primaryTarget,    
   ArrayList<FileSeq> secondaryTargets,
   Map<String,FileSeq> primarySources,    
   Map<String,ArrayList> secondarySources,   // should be: Map<String,ArrayList<FileSeq>>  
   Map<String,String> env, 
   File dir                 
  )
    throws PipelineException
  {
    throw new PipelineException("Not implemented yet...");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 4585135812320237957L;

}



