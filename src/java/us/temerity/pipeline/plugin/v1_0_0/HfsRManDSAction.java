// $Id: HfsRManDSAction.java,v 1.3 2005/12/04 05:51:38 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   R M A N   D S   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates an OTL containing Houdini dialog scripts for a set of compiled RenderMan 
 * shaders. <P> 
 * 
 * Reads the shader parameters from compiled RenderMan shader files (.slo) which are the 
 * single member of the primary file sequence of the source nodes to generate an Operator 
 * Type Library (.otl) file.  If a source node contains an icon (.icon) file as a secondary 
 * sequence it will be used as the icon used for the RenderMan shader in Houdini. <P> 
 */
public
class HfsRManDSAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsRManDSAction() 
  {
    super("HfsRManDS", new VersionID("1.0.0"), "Temerity", 
	  "Generates an OTL containing Houdini dialog scripts for a set of compiled " +
	  "RenderMan shaders.");
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
    TreeMap<File,File> sources = new TreeMap<File,File>();
    File target = null; 
    {
      for(String sname : agenda.getSourceNames()) {
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);

	File sourceFile = null;
	{
	  FileSeq fseq = agenda.getPrimarySource(sname);	  
	  FilePattern fpat = fseq.getFilePattern();
	  String suffix = fpat.getSuffix();
	  if((suffix == null) || !suffix.equals("slo") || (fseq.numFrames() != 1))
	    throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain a single " + 
	     "(.slo) file containing a compiled RenderMan shader.");
	  
	  sourceFile = new File(PackageInfo.sProdDir, 
				snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}

	File iconFile = null;
	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  String suffix = fpat.getSuffix();
	  if((suffix != null) && suffix.equals(".icon") && (fseq.numFrames() == 1)) {
	    iconFile = new File(PackageInfo.sProdDir, 
				snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	    break;
	  }
	}
	
	sources.put(sourceFile, iconFile);
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

      for(File sourceFile : sources.keySet()) {
	out.write("echo Reading: " + sourceFile.getName() + "\n" + 
		  "rmands");

	File iconFile = sources.get(sourceFile);
	if(iconFile != null) 
	  out.write(" -C " + iconFile);

	out.write(" -l " + target + " " + sourceFile + "\n");
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

  private static final long serialVersionUID = -8497873247600958892L;

}

