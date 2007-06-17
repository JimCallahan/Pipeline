// $Id: RealFlowSimAction.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.RealFlowSimAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E A L  F L O W  S I M   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Runs a RealFlow simulation generating per-frame particle and mesh data files.
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Project <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the RealFlow project to simulate. <BR> 
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class RealFlowSimAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  RealFlowSimAction() 
  {
    super("RealFlowSim", new VersionID("2.0.0"), "Temerity",
	  "Runs a RealFlow simulation generating per-frame particle and mesh data files.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("Project",
	 "The source RealFlow project to simulate.", 
	 null);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("Project");

      setSingleLayout(layout);
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
    NodeID nodeID = agenda.getNodeID();

    /* sanity checks */ 
    File scene = null;
    int startFrame = 0;
    int endFrame = 0;
    {
      {
 	FileSeq fseq = agenda.getPrimaryTarget();
 	if(!fseq.hasFrameNumbers()) 
 	  throw new PipelineException
 	    ("The RealFlowSim Action requires that the output particle files have frame numbers.");
	
	FrameRange range = fseq.getFrameRange();
	startFrame = range.getStart();
	endFrame   = range.getEnd();
	if(range.getBy() != 1) 
	  throw new PipelineException
	    ("The RealFlowSim Action requires a frame step increment of (1)!");

 	String suffix = fseq.getFilePattern().getSuffix();
 	if((suffix == null) || !suffix.equals("bin"))	   
 	  throw new PipelineException
 	    ("The RealFlowSim Action requires that the primary target file sequence contains " + 
 	     "RealFlow particle files (.bin)!");
      }

      {
	String pname = (String) getSingleParamValue("Project"); 
	if(pname != null) {
	  FileSeq fseq = agenda.getPrimarySource(pname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Project node (" + pname + ") was not one of the source " + 
	       "nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("flw"))) 
	    throw new PipelineException
	      ("The RealFlowSim Action requires that the source node specified by the " +
	       "Project parameter (" + pname + ") must have a single RealFlow project file " + 
	       "(.flw) as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, pname);
	  scene = new File(PackageInfo.sProdDir,
			   snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
	else {
	  throw new PipelineException
	    ("The RealFlowSim Action requires the Project parameter to be set!");
	}
      }      
    }


    // TEMPORARY WORKAROUND...
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      
      File dir = new File(PackageInfo.sProdDir, nodeID.getWorkingParent().toString());
      
      out.write
	("#!/bin/bash\n\n" + 
	 "rm -f " + dir + "/Circle???????.bin\n" +
	 "rm -f " + dir + "/../meshes/Mesh???????.bin\n" + 
	 "realflow -nogui -range " + startFrame + " " + endFrame + " " + scene + "\n");
	 
      for(File file : agenda.getPrimaryTarget().getFiles())
	out.write("touch " + file + "\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    // TEMPORARY WORKAROUND...

    
    /* create the process to run the action */ 
    try {
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 script.getPath(),  new ArrayList<String>(), 
	 agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
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

  private static final long serialVersionUID = -7433712133406323213L;

}

