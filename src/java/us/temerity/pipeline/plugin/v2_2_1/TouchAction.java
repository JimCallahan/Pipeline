// $Id: TouchAction.java,v 1.2 2007/03/21 22:14:04 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T O U C H   A C T I O N                                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates an empty file for all of the primary and secondary file sequences associated with 
 * the target node.
 */
public
class TouchAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TouchAction() 
  {
    super("Touch", new VersionID("2.2.1"), "Temerity",
          "Creates an empty file for all of the primary and secondary file sequences " + 
          "associated with the target node.");

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
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

  private static final long serialVersionUID = 5705090883810897380L;

}

