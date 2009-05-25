// $Id: SleepAction.java,v 1.1 2009/05/25 01:19:42 jesse Exp $

package us.temerity.pipeline.plugin.SleepAction.v2_4_6;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   S L E E P   A C T I O N                                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates an empty file for all of the primary and secondary file sequences associated with 
 * the target node and then sleeps for 15 seconds.
 */
public
class SleepAction
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  

  public
  SleepAction() 
  {
    super("Sleep", new VersionID("2.4.6"), "Temerity",
          "Creates an empty file for all of the primary and secondary file sequences " + 
          "associated with the target node and then sleeps.");

    addSupport(OsType.MacOS);
    
    {
      ArrayList<String> values = new ArrayList<String>();
      Collections.addAll(values, "Batch", "Render");
      ActionParam param =
        new EnumActionParam("ActionType", "useless param", "Batch", values);
      addSingleParam(param);
    }
    
    underDevelopment();
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
      File script = createTemp(agenda, 0755, "bash");
      try {      
        BufferedWriter out = new BufferedWriter(new FileWriter(script));
        
        out.write("touch ");
        
        ArrayList<String> args = new ArrayList<String>();
        for(File file : agenda.getPrimaryTarget().getFiles()) 
          out.write(file.toString() + " ");
        
        for(FileSeq fseq : agenda.getSecondaryTargets()) {
          for(File file : fseq.getFiles())
            out.write(file.toString() + " ");
        }
        out.write("\n");
        out.write("sleep 15");
        
        out.close();

      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to write temporary script file (" + script + ") for Job " + 
           "(" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
      }
      
      String program = "bash";
      ArrayList<String> args = new ArrayList<String>();
      args.add(script.getPath());

      return createSubProcess(agenda, program, args, null, outFile, errFile); 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6898457155232340145L;
}

