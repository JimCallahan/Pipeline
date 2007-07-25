/**
 * 
 */
package us.temerity.pipeline.plugin.ShaveCacheCopyAction.v2_0_9;

import java.io.*;
import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   S H A V E   C A C H E   C O P Y   A C T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Copies a Shave and a Haircut cache from one place to another. <P> 
 * 
 */
public class ShaveCacheCopyAction
  extends BaseAction
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public ShaveCacheCopyAction()
  {
    super("ShaveCacheCopy", new VersionID("2.0.9"), 
          "Temerity", "Copy a Shave and a Haircut cache to another location.");
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
    
    {
      FileSeq fseq = agenda.getPrimaryTarget();
      if (!fseq.hasFrameNumbers())
	throw new PipelineException(
	"Cache files must have frame numbers");
      FilePattern pat = fseq.getFilePattern();
      String suffix = pat.getSuffix();
      if (!suffix.equals("stat"))
	throw new PipelineException("Cache files must have the (stat) suffix");
      int padding = fseq.getFilePattern().getPadding();
      if (padding != 4)
	throw new PipelineException
	  ("Cache files must have a padding of 4");
      
      String tempShaveName = pat.getPrefix();
      if (!tempShaveName.startsWith("shaveStatFile_"))
	throw new PipelineException
	  ("The Cache files do not have the proper start to their name.  " +
	   "All cache files must start with (shaveStatFile_)");
    }
    
    String sname = null;
    {
	TreeSet<String> sources =  new TreeSet<String>(agenda.getSourceNames());

	if(sources.size() != 1)
	  throw new PipelineException
	  ("Only one source is allowed.");
	sname = sources.first();
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if (!fseq.hasFrameNumbers())
	  throw new PipelineException(
	    "Source cache files must have frame numbers");
	FilePattern pat = fseq.getFilePattern();
	String suffix = pat.getSuffix();
	if (!suffix.equals("stat"))
	  throw new PipelineException("Source cache files must have the (stat) suffix");
	int padding = fseq.getFilePattern().getPadding();
	if (padding != 4)
	  throw new PipelineException
	  ("Source cache files must have a padding of 4");
	Path p = new Path(nodeID.getName());

	String tempShaveName = pat.getPrefix();
	if (!tempShaveName.startsWith("shaveStatFile_"))
	  throw new PipelineException
	  ("The Source cache files do not have the proper start to their name.  " +
	  "All cache files must start with (shaveStatFile_)");
    }
    
    /* create a temporary script file */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
	FileWriter out = new FileWriter(script);

	out.write("#!/bin/bash\n\n");

	String cpOpts = "--remove-destination";
	if(PackageInfo.sOsType == OsType.MacOS)
	  cpOpts = "-f";

	/* the primary file sequences */ 
	{
	  FileSeq target = agenda.getPrimaryTarget();
	  NodeID snodeID = new NodeID(agenda.getNodeID(), sname);

	  Path spath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
	  FileSeq source = agenda.getPrimarySource(sname);
	  int wk;
	  for(wk=0; wk<target.numFrames(); wk++) {
	    out.write("cp " + cpOpts + " " +
	      spath + "/" + source.getFile(wk) + " " +
	      target.getFile(wk) + "\n");
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
  
  private static final long serialVersionUID = -2523646943158940759L;
}
