// $Id: ScriptAction.java,v 1.2 2005/07/26 04:58:30 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S C R I P T   A C T I O N                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Runs an executable script. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node who's primary file sequence is a single executable script.
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
    super("Script", new VersionID("2.0.0"), 
	  "Interprets an arbitrary script.");

    {
      ActionParam param = 
	new LinkActionParam
	("Script", 
	 "The node containing the script to execute.",
	 null);
      addSingleParam(param);
    }
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
    /* get the name of the executable script file */ 
    File script = null;
    {
      String sname = (String) getSingleParamValue("Script"); 
      if(sname == null) {
	throw new PipelineException
	  ("The Script parameter was not set!");
      }
      else {	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Script node (" + sname + ") was not one of the source nodes!");
	
	if(!fseq.isSingle()) 
	  throw new PipelineException
	    ("The Script Action requires that the source node specified by the Script " +
	     "parameter (" + sname + ") must have a single executable file as its " + 
	     "primary file sequence!");
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	script = new File(PackageInfo.sProdDir,
			  snodeID.getWorkingParent() + "/" + fseq.getFile(0));
      }
    }

    /* add the extra environmental variables */ 
    TreeMap<String,String> senv = new TreeMap<String,String>(agenda.getEnvironment());
    {
      senv.put("PIPELINE_JOB_ID", String.valueOf(agenda.getJobID()));

      NodeID nodeID = agenda.getNodeID();
      senv.put("PIPELINE_NODE_NAME", nodeID.getName());
      senv.put("PIPELINE_NODE_AUTHOR", nodeID.getAuthor());
      senv.put("PIPELINE_NODE_VIEW", nodeID.getView());

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

	    SortedSet<FileSeq> fseqs = agenda.getSecondarySources(sname);
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
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 script.getPath(), args, senv, agenda.getWorkingDir(), 
	 outFile, errFile);
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

  private static final long serialVersionUID = -1568686919875931487L;

}



