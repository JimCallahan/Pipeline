// $Id: ScriptAction.java,v 1.3 2004/03/22 03:11:08 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S C R I P T   A C T I O N                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Executes a script using a arbitrary interpreter program. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Interpreter <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the program which will interpret the script text.  The fully resolved
 *     path to this program is placed on the first line of the generated temporary script
 *     immediately after "!#".  Any program which can be used in this manner can be used 
 *     for as the value for this parameter. <BR>
 *   </DIV> <BR>
 *   
 *   ScriptText <BR>
 *   <DIV style="margin-left: 40px;">
 *     The text to be interpreted. <BR>
 *   </DIV> 
 * </DIV> <P> 
 * 
 * In order to aid script writers in writing more general scripts, this action also adds
 * a set of environmental variables to the environment under which the script is executed.
 * These extra environmental variables correspond to the arguments passed to the 
 * {@link #prep prep} method.  The environmental variables are: <P> 
 *
 * <DIV style="margin-left: 40px;">
 *   PIPELINE_JOB_ID <BR>
 *   <DIV style="margin-left: 40px;">
 *     The unique job identifier of the job running this action.
 *   </DIV> <BR>
 *
 *   PIPELINE_NODE <BR>
 *   <DIV style="margin-left: 40px;">
 *     The fully resolved name of the target node. 
 *   </DIV> <BR>
 *
 *   PIPELINE_AUTHOR <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the user which submitted the job.
 *   </DIV> <BR>
 *
 *   PIPELINE_PRIMARY_TARGET <BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of files which make up the primary file sequence to generate. 
 *   </DIV> <BR>
 *
 *   PIPELINE_NUM_SECONDARY_TARGETS <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of secondary file sequences to generate.
 *   </DIV> <BR>
 *
 *   PIPELINE_SECONDARY_TARGET_# <BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of files which make up a secondary file sequences to generate, 
 *     where (#) is replaced by the index of the of the secondary file sequence.  The index 
 *     will be in the range [0,$PIPELINE_NUM_SECONDARY_TARGETS). 
 *   </DIV> <BR>
 *
 *   PIPELINE_NUM_DEPENDENCIES <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of dependencies of the target node.
 *   </DIV> <BR>
 *
 *   PIPELINE_DEPEND_# <BR>
 *   <DIV style="margin-left: 40px;">
 *     The fully resolved node name of a dependency, where (#) is replaced by the index 
 *     of the dependency. The index will be in the range [0,$PIPELINE_NUM_DEPENDENCIES).
 *   </DIV> <BR>
 *
 *   PIPELINE_PRIMARY_SOURCE_# <BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of files which make up the primary file sequence of a node 
 *     dependency, where (#) is replaced by the index of the dependency. The index will be 
 *     in the range [0,$PIPELINE_NUM_DEPENDENCIES).
 *   </DIV> <BR>
 *
 *   PIPELINE_NUM_SECONDARY_SOURCES_# <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of secondary file sequences associated with each node dependency, where 
 *     (#) is replaced by the index of the dependency.
 *   </DIV> <BR>
 * 
 *   PIPELINE_SECONDARY_SOURCE_#_# <BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of files which make up the secondary file sequence of a 
 *     dependency, where the first (#) is replaced by the index of the dependency and the 
 *     second (#) is replaced by the index of the secondary file sequence.  The first index 
 *     will be in the range [0,$PIPELINE_NUM_DEPENDENCIES).  The second index will be in the 
 *     range [0,$PIPELINE_NUM_SECONDARY_SOURCES_#). 
 *   </DIV> 
 * </DIV> <P> 
 * 
 * Note that some of these variables may not be defined if there are no secondary file
 * sequences or node dependencies.
 */
public
class ScriptAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ScriptAction() 
  {
    super("Script", 
	  "Interprets an arbitrary script.");

    {
      ActionParam param = 
	new ActionParam("Interpreter", 
			"The program which will interpret the script.", 
			"bash");
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new ActionParam("ScriptText", 
			"The script text to execute.",
			"# put your script text here...");
      addSingleParam(param);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcess SubProcess} instance which when executed will 
   * regenerate the given file sequences for the target node. <P>
   * 
   * @param jobID [<B>in</B>] 
   *   A unique job identifier.
   * 
   * @param name [<B>in</B>] 
   *   The fully resolved name of the target node. 
   * 
   * @param author [<B>in</B>] 
   *   The name of the user which submitted the job.
   * 
   * @param primaryTarget [<B>in</B>] 
   *   The primary file sequence to generate.
   *
   * @param secondaryTargets [<B>in</B>] 
   *   The secondary file sequences to generate.
   *
   * @param primarySources [<B>in</B>] 
   *   A table of primary file sequences associated with each dependency.
   *
   * @param secondarySources [<B>in</B>] 
   *   The table of secondary file sequences associated with each dependency.
   *
   * @param env [<B>in</B>] 
   *   The environment under which the action is run.  
   * 
   * @param dir [<B>in</B>] 
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
    /* resolve the absolute path to the interpreter program */ 
    String interp = null;
    {
      String prog = (String) getSingleParamValue("Interpreter");
    
      File ifile = new File(prog);
      if(ifile.isAbsolute()) {
	if(!ifile.exists()) 
	  throw new PipelineException
	    ("The interpreter program (" + prog + ") does not exist!");
      }
      else {
	String path = (String) env.get("PATH");
	if(path == null) 
	  throw new PipelineException
	    ("The interpreter program (" + prog + ") was not absolute and no PATH was " +
	     "provided in the environment!");
	
	ExecPath exec = new ExecPath(path);
	File absolute = exec.which(ifile.getPath());
	if(absolute == null) {
	  StringBuffer buf = new StringBuffer();
	  buf.append("The interpreter program (" + prog + ") was not absolute and could " +
		     "not be found using the PATH from the given environment!\n\n" +
		     "The directories searched: \n");
	  
	  for(File edir : exec.getDirectories()) 
	    buf.append("  " + edir + "\n");
	  
	  throw new PipelineException(buf.toString());
	}
	
	ifile = absolute;
      }

      interp = ifile.getPath();
    }

    /* create a temporary executable script file */ 
    File script = null;
    try {
      /* create temporary directory */ 
      File sdir = new File(getTempDir(), "pipeline");
      sdir.mkdir();
      
      /* generate script filename */ 
      File node = new File(name);
      script = File.createTempFile("ScriptAction-" + jobID, ".script", sdir);

      /* write script contents */ 
      {
	FileWriter out = new FileWriter(script);
	
	String header = ("#!" + interp + "\n\n");
	out.write(header);
      
	String body = (String) getSingleParamValue("ScriptText");
	out.write(body);
	
	out.close();
      }

      /* schedule deletion of the temporary script upon exit of the calling program */ 
      cleanupLater(script);
      
      /* make script executable */ 
      chmod(0777, script);
    }
    catch (Exception ex) {
      throw new PipelineException
	("Unable to create temporary script file (" + script.getPath() + ") for Job (" + 
	 jobID + ")!\n" +
	 ex.getMessage());
    }

    /* add the extra environmental variables */ 
    TreeMap<String,String> senv = new TreeMap<String,String>(env);
    {
      senv.put("PIPELINE_JOB_ID", String.valueOf(jobID));
      senv.put("PIPELINE_NODE", name);
      senv.put("PIPELINE_AUTHOR", author);
      senv.put("PIPELINE_PRIMARY_TARGET", fileList(primaryTarget));

      senv.put("PIPELINE_NUM_SECONDARY_TARGETS", String.valueOf(secondaryTargets.size()));
      {
	int wk = 0;
	for(FileSeq fseq : secondaryTargets) {
	  senv.put("PIPELINE_SECONDARY_TARGET_" + wk, fileList(fseq));
	  wk++;
	}
      }

      senv.put("PIPELINE_NUM_DEPENDENCIES", String.valueOf(primarySources.keySet().size()));
      {
	int dk = 0;
	for(String depend : primarySources.keySet()) {
	  senv.put("PIPELINE_DEPEND_" + dk, depend);
	  senv.put("PIPELINE_PRIMARY_SOURCE_" + dk, fileList(primarySources.get(depend)));

	  ArrayList fseqs = secondarySources.get(depend);
	  if(fseqs != null) {
	    senv.put("PIPELINE_NUM_SECONDARY_SOURCES_" + dk, String.valueOf(fseqs.size()));

	    int wk = 0;
	    for(Object obj : fseqs) {
	      FileSeq fseq = (FileSeq) obj;
	      senv.put("PIPELINE_SECONDARY_SOURCE_" + dk + "_" + wk, fileList(fseq));
	      wk++;
	    }
	  }

	  dk++;
	}
      }
    }

    /* create the process to run the action */ 
    try {
      return new SubProcess(author, getName() + "-" + jobID, 
			    script.getPath(), new ArrayList<String>(), senv, dir);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Generate a colon seperated list of files which make up the given file sequence.
   */
  private String 
  fileList
  (
   FileSeq fseq 
  ) 
  {
    StringBuffer buf = new StringBuffer();
    for(File file : fseq.getFiles()) 
      buf.append(file + ":");
    return buf.substring(0, buf.length()-1);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4585135812320237957L;

}



