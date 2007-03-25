// $Id: ScriptAction.java,v 1.4 2007/03/25 03:12:51 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

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
 *   </DIV> <BR>
 * 
 *   FullPaths <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to supply the values for the target/source file list environmental variables
 *     as fully resolved file name paths instead of the default simple file name.
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
 * sequences or node dependencies. <P> 
 * 
 * For new development, we would recommend using the {@link PythonAction Python} Action 
 * instead of this Action for the best multi-platform support.  Although you could provide
 * a Python script to this Action as its Script source node, passing job prep information
 * through the environment is less elegant than the dynamic module approach used by the 
 * Python Action plugin.
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
    super("Script", new VersionID("2.2.1"), "Temerity", 
	  "Interprets an arbitrary script.");

    {
      ActionParam param = 
	new LinkActionParam
	(aScript, 
	 "The node containing the script to execute.",
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(aFullPaths, 
	 "Whether to supply the values for the target/source file list environmental " + 
         "variables as fully resolved file name paths instead of the default simple file " + 
         "name.", 
	 false);
      addSingleParam(param);
    }

    /* layout */ 
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aScript);
      layout.addSeparator(); 
      layout.addEntry(aFullPaths);
    
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
    /* get the name of the executable script file */ 
    Path script = getPrimarySourcePath(aScript, agenda, "executable script");

    /* whether to supply full paths */ 
    boolean fullPaths = getSingleBooleanParamValue(aFullPaths); 

    /* add the extra environmental variables */ 
    TreeMap<String,String> senv = new TreeMap<String,String>(agenda.getEnvironment());
    {
      senv.put("PIPELINE_JOB_ID", String.valueOf(agenda.getJobID()));

      NodeID nodeID = agenda.getNodeID();
      senv.put("PIPELINE_NODE_NAME", nodeID.getName());
      senv.put("PIPELINE_NODE_AUTHOR", nodeID.getAuthor());
      senv.put("PIPELINE_NODE_VIEW", nodeID.getView());

      NodeID nid = fullPaths ? nodeID : null;
      senv.put("PIPELINE_PRIMARY_TARGET", fileList(nid, agenda.getPrimaryTarget()));

      {
	SortedSet<FileSeq> fseqs = agenda.getSecondaryTargets();
	senv.put("PIPELINE_NUM_SECONDARY_TARGETS", String.valueOf(fseqs.size()));

	int wk = 0;
	for(FileSeq fseq : fseqs) {
	  senv.put("PIPELINE_SECONDARY_TARGET_" + wk, fileList(nid, fseq));
	  wk++;
	}
      }

      {
	Set<String> sources = agenda.getSourceNames();
	senv.put("PIPELINE_NUM_SOURCES", String.valueOf(sources.size()));
	{
	  int dk = 0;
	  for(String sname : sources) {
            NodeID snid = fullPaths ? new NodeID(nodeID, sname) : null;

	    senv.put("PIPELINE_SOURCE_" + dk, sname);
	    senv.put("PIPELINE_PRIMARY_SOURCE_" + dk, 
		     fileList(snid, agenda.getPrimarySource(sname)));

	    SortedSet<FileSeq> fseqs = agenda.getSecondarySources(sname);
	    if(!fseqs.isEmpty()) {
	      senv.put("PIPELINE_NUM_SECONDARY_SOURCES_" + dk, String.valueOf(fseqs.size()));

	      int wk = 0;
	      for(FileSeq fseq : fseqs) {
		senv.put("PIPELINE_SECONDARY_SOURCE_" + dk + "_" + wk, fileList(snid, fseq));
		wk++;
	      }
	    }

	    dk++;
	  }
	}
      }
    }

    /* create the process to run the action */ 
    return createSubProcess(agenda, script.toString(), null, senv, outFile, errFile);
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
   NodeID nodeID, 
   FileSeq fseq 
  ) 
  {
    StringBuilder buf = new StringBuilder();

    for(Path path : fseq.getPaths()) {
      Path fpath = path;
      if(nodeID != null) 
        fpath = getWorkingNodeFilePath(nodeID, path);

      buf.append(fpath.toOsString() + ":");
    }

    return buf.substring(0, buf.length()-1);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3684239903264889986L;

  public static final String aScript    = "Script"; 
  public static final String aFullPaths = "FullPaths"; 

}



