// $Id: AirBakeAction.java,v 1.1 2007/06/17 15:34:37 jim Exp $

package us.temerity.pipeline.plugin.AirBakeAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A I R   B A K E   A C T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Bakes shading and lighting information into texture maps using BakeAIR. <P> 
 * 
 * See the <A href="http://www.sitexgraphics.com/html/air.html">AIR</A> documentation for 
 * <A href="http://www.sitexgraphics.com/air.pdf"><B>bakeair</B></A>(1) for details. <P>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Bake Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the RenderMan scene to bake.
 *   </DIV> <BR>
 * </DIV>
 */
public
class AirBakeAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AirBakeAction() 
  {
    super("AirBake", new VersionID("2.0.0"), "Temerity", 
	  "Bakes shading and lighting information into texture maps using BakeAIR.");

    {
      ActionParam param = 
	new LinkActionParam
	("BakeScene",
	 "The source node which contains the RenderMan scene to bake.", 
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

    /* sanity checks */ 
    File bakeRIB = null;
    {
      String sname = (String) getSingleParamValue("BakeScene"); 
      if(sname != null) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Bake Scene node (" + sname + ") was not one of the source " + 
	     "nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || !(suffix.equals("rib"))) 
	  throw new PipelineException
	    ("The AirBake Action requires that the source node specified by the Bake " +
	     "Scene parameter (" + sname + ") must have a single RIB file as " + 
	       "its primary file sequence!");
	
	NodeID snodeID = new NodeID(nodeID, sname);
	  bakeRIB = new File(PackageInfo.sProdDir,
			     snodeID.getWorkingParent() + "/" + fseq.getFile(0));
      }
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add(bakeRIB.getPath());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "bakeair", args,agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = 4903904387351403521L;

}

