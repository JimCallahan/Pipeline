// $Id: SwitchAction.java,v 1.1 2004/09/22 05:41:54 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

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
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  SwitchAction() 
  {
    super("Switch", new VersionID("1.0.0"), 
	  "Copies the files associated with a selected source node.");
    
    {
      BaseActionParam param = 
	new LinkActionParam
	("Source", 
	 "The source node which contains the files to copy.", 
	 null);
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
    /* sanity checks */ 
    {
      /* the primary file sequences */ 
      {
	FileSeq target = agenda.getPrimaryTarget();
	for(String sname : agenda.getSourceNames()) {
	  FileSeq source = agenda.getPrimarySource(sname);

	  if(target.numFrames() != source.numFrames()) 
	    throw new PipelineException 
	      ("The primary file sequence (" + source + ") of source node (" + sname + ")" + 
	       "does not contain the same number of frames (" + target.numFrames() + ") " +
	       "as the primary file sequence (" + target + ") of the target node " + 
	       "(" + agenda.getNodeID().getName() + ")!");
	}
      }
      
      /* the secondary file sequences */ 
      {
	int num = agenda.getSecondaryTargets().size();
	for(String sname : agenda.getSourceNames()) {
	  if(num != agenda.getSecondarySources(sname).size()) 
	    throw new PipelineException 
	      ("The source node (" + sname + ") does not have the same number of secondary" +
	       "file sequences as the target node (" + agenda.getNodeID().getName() + ")!");
	}
      }
    }

    /* create a temporary script file */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      out.write("#!/bin/bash\n\n");

      /* the primary file sequences */ 
      {
	FileSeq target = agenda.getPrimaryTarget();
	for(String sname : agenda.getSourceNames()) {
	  NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	  File sdir = new File(PackageInfo.sProdDir, snodeID.getWorkingParent().toString());
	  
	  FileSeq source = agenda.getPrimarySource(sname);
	  int wk;
	  for(wk=0; wk<target.numFrames(); wk++) {
	    out.write("cp --remove-destination " + 
		      sdir + "/" + source.getFile(wk) + " " +
		      target.getFile(wk) + "\n");
	  }
	}
      }

      /* the secondary file sequences */ 
      {
	ArrayList<FileSeq> targets = 
	  new ArrayList<FileSeq>(agenda.getSecondaryTargets());

	for(String sname : agenda.getSourceNames()) {
	  NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	  File sdir = new File(PackageInfo.sProdDir, snodeID.getWorkingParent().toString());

	  ArrayList<FileSeq> sources = 
	    new ArrayList<FileSeq>(agenda.getSecondarySources(sname));

	  int sk;
	  for(sk=0; sk<targets.size(); sk++) {
	    FileSeq target = targets.get(sk);
	    FileSeq source = sources.get(sk);

	    int wk;
	    for(wk=0; wk<target.numFrames(); wk++) {
	      out.write("cp --remove-destination " + 
			sdir + "/" + source.getFile(wk) + " " +
			target.getFile(wk) + "\n");
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
    try {
      return new SubProcess(agenda.getNodeID().getAuthor(), 
			    getName() + "-" + agenda.getJobID(), 
			    script.getPath(), new ArrayList<String>(), 
			    agenda.getEnvironment(), agenda.getWorkingDir());
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3533025088968410027L;

}

