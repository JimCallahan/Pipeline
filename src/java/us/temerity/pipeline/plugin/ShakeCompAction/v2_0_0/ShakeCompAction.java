// $Id: ShakeCompAction.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.ShakeCompAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S H A K E   C O M P   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Executes a Shake script evaluating all FileOut nodes. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Shake Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Shake script to execute.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class ShakeCompAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ShakeCompAction() 
  {
    super("ShakeComp", new VersionID("2.0.0"), "Temerity",
	  "Executes a Shake script evaluating all FileOut nodes.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ShakeScript",
	 "The source Shake script node.", 
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
    NodeID nodeID = agenda.getNodeID();

    /* sanity check */ 
    File script = null; 
    FrameRange range = null;
    {
      /* generate the filename of the Maya scene to load */
      {
	String sname = (String) getSingleParamValue("ShakeScript"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Shake Script node was not specified!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Shake Script node (" + sname + ") was not one of the source nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || !(suffix.equals("shk")))
	  throw new PipelineException
	    ("The Shake Comp Action requires that the source node specified by the Shake " +
	     "Script parameter (" + sname + ") must have a single Shake script file (.shk) " + 
	     "as its primary file sequence!");
	
	NodeID snodeID = new NodeID(nodeID, sname);
	script = new File(PackageInfo.sProdDir,
			  snodeID.getWorkingParent() + "/" + fseq.getFile(0));
      }

      /* the target frame range */ 
      range = agenda.getPrimaryTarget().getFrameRange();
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-exec");
      args.add(script.getPath());
      
      if(range != null) {
	args.add("-t");
	args.add(range.toString()); 
      }

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "shake", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -4142485590414159271L;

}
