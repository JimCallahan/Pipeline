// $Id: SwitchAction.java,v 1.4 2007/04/12 15:40:25 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   S W I T C H   A C T I O N                                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * Replaces the primary and secondary files of the target node with copies of the primary 
 * and secondary files of one of its source nodes. <P> 
 * 
 * Each source node must have the same numbers of primary and secondary files as the 
 * target node. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Source <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node which contains the files to copy.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class SwitchAction
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  SwitchAction() 
  {
    super("Switch", new VersionID("2.2.1"), "Temerity",
	  "Copies the files associated with a selected source node.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aSource, 
	 "The source node which contains the files to copy.", 
	 null);
      addSingleParam(param);
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
    NodeID nodeID = agenda.getNodeID();

    /* sanity checks */ 
    {
      FileSeq target = agenda.getPrimaryTarget();

      {
	String sname = (String) getSingleParamValue(aSource);
	if(sname == null) 
	  throw new PipelineException
	    ("The Source node was not set!");
	
	if(!agenda.getSourceNames().contains(sname))
	  throw new PipelineException
	    ("The Source node (" + sname + ") is not linked to the target node " + 
	     "(" + target + ")!");
      }	

      /* the primary file sequences */ 
      {
	for(String sname : agenda.getSourceNames()) {
	  FileSeq source = agenda.getPrimarySource(sname);

	  if(target.numFrames() != source.numFrames()) 
	    throw new PipelineException 
	      ("The primary file sequence (" + source + ") of source node (" + sname + ")" + 
	       "does not contain the same number of frames (" + target.numFrames() + ") " +
	       "as the primary file sequence (" + target + ") of the target node " + 
	       "(" + nodeID.getName() + ")!");
	}
      }
      
      /* the secondary file sequences */ 
      {
	int num = agenda.getSecondaryTargets().size();
	for(String sname : agenda.getSourceNames()) {
	  if(num != agenda.getSecondarySources(sname).size()) 
	    throw new PipelineException 
	      ("The source node (" + sname + ") does not have the same number of secondary" +
	       "file sequences as the target node (" + nodeID.getName() + ")!");
	}
      }
    }

    /* create a temporary script file */   
    File script = createTempScript(agenda); 
    try {      
      FileWriter out = new FileWriter(script);

      String sname = (String) getSingleParamValue(aSource);
      NodeID snodeID = new NodeID(nodeID, sname);

      String cpOpts = "--remove-destination";
      if(PackageInfo.sOsType == OsType.MacOS)
	cpOpts = "-f";

      /* the primary file sequences */ 
      {
        FileSeq target = agenda.getPrimaryTarget();
	FileSeq source = agenda.getPrimarySource(sname);

	int wk;
	for(wk=0; wk<target.numFrames(); wk++) {
          Path spath = getWorkingNodeFilePath(snodeID, source.getPath(wk));

          if(PackageInfo.sOsType == OsType.Windows) {
            Path tpath = getWorkingNodeFilePath(nodeID, target.getPath(wk));

            out.write("copy /y \"" + spath.toOsString() + "\" " + 
                      "\"" + tpath.toOsString() + "\"\n");
          }
          else {
            out.write("cp " + cpOpts + " " + spath.toOsString() + " " + 
                      target.getPath(wk).toOsString() + "\n");
          }
	}
      }

      /* the secondary file sequences */ 
      {
	ArrayList<FileSeq> targets = 
	  new ArrayList<FileSeq>(agenda.getSecondaryTargets());
	ArrayList<FileSeq> sources = 
	  new ArrayList<FileSeq>(agenda.getSecondarySources(sname));
	
	int sk;
	for(sk=0; sk<targets.size(); sk++) {
	  FileSeq target = targets.get(sk);
	  FileSeq source = sources.get(sk);
	  
	  int wk;
	  for(wk=0; wk<target.numFrames(); wk++) {
            Path spath = getWorkingNodeFilePath(snodeID, source.getPath(wk));

            if(PackageInfo.sOsType == OsType.Windows) {
              Path tpath = getWorkingNodeFilePath(nodeID, target.getPath(wk));

              out.write("copy /y \"" + spath.toOsString() + "\" " + 
                        "\"" + tpath.toOsString() + "\"\n");
            }
            else {
              out.write("cp " + cpOpts + " " + spath.toOsString() + " " + 
                        target.getPath(wk).toOsString() + "\n");
            }
	  }
	}
      }
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    return createScriptSubProcess(agenda, script, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 10121604292032299L;

  public static final String aSource = "Source"; 

}

