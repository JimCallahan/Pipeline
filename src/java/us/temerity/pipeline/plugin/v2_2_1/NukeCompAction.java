// $Id: NukeCompAction.java,v 1.3 2007/05/16 13:12:53 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   C O M P   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Executes a Nuke script evaluating one or more Write nodes to generate composited images.<P>
 * 
 * All Write nodes in the Nuke script for file sequences associated with the target node 
 * will be executed.  Any Write nodes not associated with target file sequences will be
 * ignored.  
 * 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Nuke Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Nuke script to execute.
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * By default, this Action executes the "Nuke4.6" binary.  This can be overridden by 
 * specifying an alternate binary with the NUKE_BINARY environmental variable in the 
 * Toolset used to run this Action plugin. On Windows, the Nuke binary name should 
 * include the ".exe" extension.<P> 
 *
 * All Read/Write nodes should have absolute file paths relative to the root working 
 * directory which start with the string "WORKING" in order to support portability of 
 * Nuke scripts between artists and operation sytems.  To enable the "WORKING" prefix to 
 * be expanded to the value of the WORKING environmental variable in these file paths, 
 * a "init.tcl" script as been provided with Pipeline in the "app-extra/nuke" directory 
 * where Pipeline is installed at your site.  You must either copy this script into the 
 * "plugin/user" directory of your Nuke installation or add Pipeline's "app-extra/nuke" 
 * directory to the NUKE_PATH defined in the Toolset using this plugin.
 */
public
class NukeCompAction
  extends NukeActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeCompAction() 
  {
    super("NukeComp", new VersionID("2.2.1"), "Temerity",
	  "Executes a Nuke script evaluating one or more Write nodes to generate " + 
          "composited images.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aNukeScript,
	 "The source Nuke script node.", 
	 null);
      addSingleParam(param);
    } 

    addExtraOptionsParam(); 

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aNukeScript);     
      layout.addSeparator();
      addExtraOptionsParamToLayout(layout); 

      setSingleLayout(layout);  
    }

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
    /* the Nuke script to evaluate */
    Path sourceScript = null;
    {
      ArrayList<String> suffixes = new ArrayList<String>();
      suffixes.add("nk");
      suffixes.add("nuke");

      sourceScript = getPrimarySourcePath(aNukeScript, agenda, suffixes, "Nuke script");
      if(sourceScript == null) 
        throw new PipelineException("The NukeScript node was not specified!");
    }

    /* all target file sequences in Nuke notation */ 
    TreeSet<String> targetNukePatterns = new TreeSet<String>();
    for(FileSeq fseq : agenda.getTargetSequences()) 
      targetNukePatterns.add(toNukeFilePattern(fseq.getFilePattern()));

    /* create a temporary Nuke script based on the source script which is modified to: 
       + insure that all file paths are prefixed with "WORKING" (see above) 
       + replace file path for Write nodes which match the target file sequences with 
           local paths to insure that the target files are generated properly (Windows) 
       + disable all Write nodes which don't match any target file sequence         */ 
    File script = createTemp(agenda, "nk");
    try {
      BufferedReader in = new BufferedReader(new FileReader(sourceScript.toFile())); 
      FileWriter out = new FileWriter(script); 

      int lnum = 1;
      try { 
        boolean isRead     = false;
        boolean isWrite    = false;
        boolean isDisabled = false;
        while(true) {
          String line = in.readLine();
          if(line == null) 
            break;

          boolean wasWritten = false;
          if(line.startsWith("Read {"))
            isRead = true;
          else if(line.startsWith("Write {"))
            isWrite = true;
          else if(line.startsWith("}")) {
            if(isDisabled) 
              out.write(" disable true\n");
            isRead     = false;
            isWrite    = false;
            isDisabled = false;            
          }
          else if((isRead || isWrite) && line.startsWith(" file ")) {
            String file = line.substring(6);
            if(!file.startsWith("WORKING/")) 
              throw new PipelineException
                ("Non-portable file path (" + file + ") detected in Nuke script " + 
                 "(" + sourceScript + ")!  All Read/Write node file paths should be " + 
                 "prefixed with \"WORKING\" in order to insure portability between " + 
                 "different artists and operating systems.  You must fix any non-portable " +
                 "file paths before execution of this Nuke script will be successful."); 
            
            if(isWrite) {
              Path path = new Path(file.substring(7));
              String npat = path.getName();
              if(targetNukePatterns.contains(npat)) {
                out.write(" file ./" + npat + "\n"); 
                wasWritten = true;
              }
              else {
                isDisabled = true;
              }
            }
          }

          if(!wasWritten) 
            out.write(line + "\n");
        }
      }
      finally {
        in.close();
        out.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary Nuke script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
      
    /* create the process to run the action */ 
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-nx"); 
      args.addAll(getExtraOptionsArgs());
      args.add(script.toString()); 
      
      FrameRange range = agenda.getPrimaryTarget().getFrameRange();
      if(range != null) {
        if(range.isSingle()) 
          args.add(Integer.toString(range.getStart()));
        else if(range.getBy() == 1) 
          args.add(range.getStart() + "," + range.getEnd());
        else 
          args.add(range.getStart() + "," + range.getEnd() + "," + range.getBy());
      }

      return createSubProcess(agenda, getNukeProgram(agenda), args, agenda.getEnvironment(), 
                              agenda.getTargetPath().toFile(), outFile, errFile);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7751545281132044742L;

  public static final String aNukeScript = "NukeScript";

}

