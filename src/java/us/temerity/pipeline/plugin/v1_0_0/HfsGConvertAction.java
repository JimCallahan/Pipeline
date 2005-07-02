// $Id: HfsGConvertAction.java,v 1.1 2005/07/02 20:45:38 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   G C O N V E R T   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Convert the geometry files which make up the primary file sequence of one of the source
 * nodes into the geometry format of the primary file sequence of this node. <P> 
 * 
 * See the <A href="http://www.sidefx.com">Houdini</A> documentation for details on the
 * usage and behavior of gconvert(1).<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Geometry Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the geometry to convert. <BR> 
 *   </DIV> 
 * </DIV> <P> 
 */
public
class HfsGConvertAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsGConvertAction() 
  {
    super("HfsGConvert", new VersionID("1.0.0"), 
	  "Converts images files from another format.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("GeometrySource",
	 "The source node containing the geometry to convert.",
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
    FileSeq fromSeq = null;
    FileSeq toSeq = null;
    {    
      {
	String sname = (String) getSingleParamValue("GeometrySource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Geometry Source was not set!");

	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Geometry Source (" + sname + ") was not one of the source " + 
	     "nodes!");

	String suffix = fseq.getFilePattern().getSuffix();
	if(suffix == null)
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") of the Geometry Source " + 
	     "(" + sname + ") does not have a filename suffix!");
	  
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	fromSeq = 
	  new FileSeq(PackageInfo.sProdDir.getPath() + snodeID.getWorkingParent(), fseq);
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(suffix == null)
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") does not have a " + 
	     "filename suffix!");
	
	toSeq = fseq;
      }
      
      if(fromSeq.numFrames() != toSeq.numFrames()) 
	throw new PipelineException 
	  ("The source file sequence (" + fromSeq + ") did not have the same number of " +
	   "frames as the target file sequence (" + toSeq + ")!");
    }
    
    /* create the wrapper shell script */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      out.write("#!/bin/bash\n\n");

      int wk;
      for(wk=0; wk<fromSeq.numFrames(); wk++) {
	File fromGeometry = fromSeq.getFile(wk);
	File toGeometry   = toSeq.getFile(wk);
	
	out.write("gconvert " + fromGeometry + " " + toGeometry + "\n");
      }	

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    try {
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 script.getPath(), new ArrayList<String>(), 
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

  private static final long serialVersionUID = 1459087361389708227L;

}

