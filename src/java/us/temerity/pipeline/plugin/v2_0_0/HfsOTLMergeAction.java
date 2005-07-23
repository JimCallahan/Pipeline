// $Id: HfsOTLMergeAction.java,v 1.1 2005/07/23 21:57:58 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   O T L   M E R G E   A C T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates an OTL containing all operator definitions from a set of source OTLs. <P> 
 * 
 * Each Operator Type Library (.otl) which is the single member of the primary file sequence 
 * of one of the source nodes will be included in the generated OTL file. <P> 
 * 
 * See the <A href="http://www.sidefx.com">Houdini</A> documentation for details on OTLs
 * and their usage with Houdini. <P> 
 * 
 * This action has no parameters. 
 */
public
class HfsOTLMergeAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsOTLMergeAction() 
  {
    super("HfsOTLMerge", new VersionID("2.0.0"), 
	  "Generates an OTL containing all operator definitions from a set of source OTLs.");
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
	  if((suffix == null) || !suffix.equals("otl") || (fseq.numFrames() != 1))
	    throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain a single " + 
	     "Operator Type Library (.otl) file.");
	  
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
	out.write("echo Adding OPs from: " + sourceFile.getName() + "\n" + 
		  "hotl -M " + sourceFile + " " + target + "\n");
      
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

  //  private static final long serialVersionUID = 

}

