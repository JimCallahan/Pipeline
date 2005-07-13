// $Id: HfsMRayDSAction.java,v 1.1 2005/07/13 13:52:07 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   M R A Y   D S   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates an OTL containing Houdini dialog scripts for a set of MentalRay shaders. <P> 
 * 
 * Reads the shader parameters from MentalRay shader files (.mi) which are the 
 * single member of the primary file sequence of the source nodes to generate an Operator 
 * Type Library (.otl) file.  <P> 
 */
public
class HfsMRayDSAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsMRayDSAction() 
  {
    super("HfsMRayDS", new VersionID("1.0.0"), 
	  "Generates an OTL containing Houdini dialog scripts for a set of " + 
	  "MentalRay shaders."); 

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

    /* file sequence checks */ 
    TreeSet<File> sources = new TreeSet<File>();
    File target = null; 
    {
      for(String sname : agenda.getSourceNames()) {
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);

	File sourceFile = null;
	{
	  FileSeq fseq = agenda.getPrimarySource(sname);	  
	  FilePattern fpat = fseq.getFilePattern();
	  String suffix = fpat.getSuffix();
	  if((suffix == null) || !suffix.equals("mi") || (fseq.numFrames() != 1))
	    throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain a single " + 
	     "(.slo) file containing a MentalRay shader.");
	  
	  sources.add(new File(PackageInfo.sProdDir, 
			       snodeID.getWorkingParent() + "/" + fseq.getFile(0)));
	}
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	FilePattern fpat = fseq.getFilePattern();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("otl") || (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a single " + 
	     "Operator Type Library (.otl) file.");

	target = fseq.getFile(0);
      }
    }

    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      out.write("#!/bin/bash\n\n");

      for(File sourceFile : sources) 
	out.write("echo Compiling: " + sourceFile.getName() + "\n" + 
		  "mids -l " + target + " " + sourceFile + "\n");

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

  private static final long serialVersionUID = 7993458042073690313L;

}

