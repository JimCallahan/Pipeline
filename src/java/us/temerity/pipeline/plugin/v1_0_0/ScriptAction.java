// $Id: ScriptAction.java,v 1.1 2004/09/08 18:31:28 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

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
 *   PIPELINE_NODE_NAME <BR>
 *   <DIV style="margin-left: 40px;">
 *     The fully resolved name of the target node. 
 *   </DIV> <BR>
 *
 *   PIPELINE_NODE_AUTHOR <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the user which owns the target node.
 *   </DIV> <BR>
 * 
 *   PIPELINE_NODE_VIEW <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the user's working area view which contains the target node.
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
 *   PIPELINE_NUM_SOURCES <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of upstream source nodes.
 *   </DIV> <BR>
 *
 *   PIPELINE_SOURCE_# <BR>
 *   <DIV style="margin-left: 40px;">
 *     The fully resolved node name of a source node, where (#) is replaced by the index 
 *     of the source. The index will be in the range [0,$PIPELINE_NUM_SOURCES).
 *   </DIV> <BR>
 *
 *   PIPELINE_PRIMARY_SOURCE_# <BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of files which make up the primary file sequence of a source
 *     node, where (#) is replaced by the index of the source. The index will be 
 *     in the range [0,$PIPELINE_NUM_SOURCES).
 *   </DIV> <BR>
 *
 *   PIPELINE_NUM_SECONDARY_SOURCES_# <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of secondary file sequences associated with each source node, where 
 *     (#) is replaced by the index of the source.
 *   </DIV> <BR>
 * 
 *   PIPELINE_SECONDARY_SOURCE_#_# <BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of files which make up the secondary file sequence of a 
 *     source node, where the first (#) is replaced by the index of the source and the 
 *     second (#) is replaced by the index of the secondary file sequence.  The first index 
 *     will be in the range [0,$PIPELINE_NUM_SOURCES).  The second index will be in the 
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
    super("Script", new VersionID("1.0.0"), 
	  "Interprets an arbitrary script.");

    {
      BaseActionParam param = 
	new StringActionParam("Interpreter", 
			      "The program which will interpret the script.", 
			      "bash");
      addSingleParam(param);
    }

    {
      BaseActionParam param = 
	new TextActionParam("ScriptText", 
			    "The script text to execute.",
			    "# put your script text here...");
      addSingleParam(param);
    }
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
	String path = agenda.getEnvironment().get("PATH");
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

    /* write a temporary executable script file */ 
    File script = createTemp(agenda, 0755, "script");
    try {      
      FileWriter out = new FileWriter(script);
	
      String header = ("#!" + interp + "\n\n");
      out.write(header);
      
      String body = (String) getSingleParamValue("ScriptText");
      out.write(body);
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* add the extra environmental variables */ 
    TreeMap<String,String> senv = new TreeMap<String,String>(agenda.getEnvironment());
    {
      senv.put("PIPELINE_JOB_ID", String.valueOf(agenda.getJobID()));

      NodeID nodeID = agenda.getNodeID();
      senv.put("PIPELINE_NODE_NAME", nodeID.getName());
      senv.put("PIPELINE_NODE_AUTHOR", nodeID.getAuthor());
      senv.put("PIPELINE_NODE_VIEW", nodeID.getAuthor());

      senv.put("PIPELINE_PRIMARY_TARGET", fileList(agenda.getPrimaryTarget()));

      {
	SortedSet<FileSeq> fseqs = agenda.getSecondaryTargets();
	senv.put("PIPELINE_NUM_SECONDARY_TARGETS", String.valueOf(fseqs.size()));

	int wk = 0;
	for(FileSeq fseq : fseqs) {
	  senv.put("PIPELINE_SECONDARY_TARGET_" + wk, fileList(fseq));
	  wk++;
	}
      }

      {
	Set<String> sources = agenda.getSourceNames();
	senv.put("PIPELINE_NUM_SOURCES", String.valueOf(sources.size()));
	{
	  int dk = 0;
	  for(String sname : sources) {
	    senv.put("PIPELINE_SOURCE_" + dk, sname);
	    senv.put("PIPELINE_PRIMARY_SOURCE_" + dk, 
		     fileList(agenda.getPrimarySource(sname)));

	    SortedSet<FileSeq> fseqs = agenda.getSecondarySource(sname);
	    if(!fseqs.isEmpty()) {
	      senv.put("PIPELINE_NUM_SECONDARY_SOURCES_" + dk, String.valueOf(fseqs.size()));

	      int wk = 0;
	      for(FileSeq fseq : fseqs) {
		senv.put("PIPELINE_SECONDARY_SOURCE_" + dk + "_" + wk, fileList(fseq));
		wk++;
	      }
	    }

	    dk++;
	  }
	}
      }
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      return new SubProcess(agenda.getNodeID().getAuthor(), 
			    getName() + "-" + agenda.getJobID(), 
			    script.getPath(), args, senv, agenda.getWorkingDir());
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



