// $Id: TestAction.java,v 1.2 2007/07/31 14:58:40 jim Exp $

package us.temerity.pipeline.plugin.TestAction.v1_2_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.math.*;

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
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TestAction() 
  {
    super("Test",  new VersionID("1.2.0"), "Temerity", 
	  "A test action with at lease one of each type of parameter.");

    {
      ActionParam param = 
	new DoubleActionParam("SomeDouble", 
			      "An double parameter.",
			      123.456);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam("SomeBoolean", 
			       "An boolean parameter.",
			       false);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam("SomeString",
			      "A short string parameter.",
			      "test");
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam("ExtraString",
			      "An extra short string parameter.",
			      "extra");
      addSingleParam(param);
    }

    {
      ArrayList<String> colors = new ArrayList<String>();
      colors.add("Red");
      colors.add("Yellow");
      colors.add("Green");
      colors.add("Cyan");
      colors.add("Blue");
      colors.add("Purple");

      ActionParam param = 
	new EnumActionParam("SomeEnum",
			    "An enum parameter.",
			    "Purple", colors);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new Color3dActionParam("MyColor3d",
                               "A color parameter.", 
                               new Color3d(1.0, 0.7, 0.1)); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new Tuple2iActionParam("MyTuple2i",
                               "A tuple parameter with (2) Integer components.", 
                               new Tuple2i(2, 3)); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new Tuple3iActionParam("MyTuple3i",
                               "A tuple parameter with (3) Integer components.", 
                               new Tuple3i(3, 4, 5)); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new Tuple2dActionParam("MyTuple2d",
                               "A tuple parameter with (2) Double components.", 
                               new Tuple2d(2.3, 3.4)); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new Tuple3dActionParam("MyTuple3d",
                               "A tuple parameter with (3) Double components.", 
                               new Tuple3d(2.3, 3.4, 5.6)); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new Tuple4dActionParam("MyTuple4d",
                               "A tuple parameter with (4) Double components.", 
                               new Tuple4d(4.5, 5.6, 6.7, 7.8)); 
      addSingleParam(param);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
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
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    return TestParams.getInitialSourceParams();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will 
   * fulfill the given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda or a general failure of the prep method code.
   */
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    
    /* create the process to run the action */ 
    if(PackageInfo.sOsType == OsType.Windows) { 
      File script = createTemp(agenda, ".bat"); 
      try {
        FileWriter out = new FileWriter(script);
        
        Path wpath = agenda.getTargetPath(); 
        
        for(Path target : agenda.getPrimaryTarget().getPaths()) {
          Path path = new Path(wpath, target);
          out.write("@echo off > " + path.toOsString() + "\n"); 
        }
        
        for(FileSeq fseq : agenda.getSecondaryTargets()) {
          for(Path target : fseq.getPaths()) {
            Path path = new Path(wpath, target);
            out.write("@echo off > " + path.toOsString() + "\n"); 
          }
        }
        
        out.close();
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to write temporary BAT file (" + script + ") for Job " + 
           "(" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
      }
      
      return createScriptSubProcess(agenda, script, outFile, errFile);
    }
    else {
      ArrayList<String> args = new ArrayList<String>();
      for(File file : agenda.getPrimaryTarget().getFiles()) 
        args.add(file.toString());
      
      for(FileSeq fseq : agenda.getSecondaryTargets()) {
        for(File file : fseq.getFiles())
          args.add(file.toString());
      }
      
      return createSubProcess(agenda, "touch", args, outFile, errFile); 
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1201400081088512590L; 

}



