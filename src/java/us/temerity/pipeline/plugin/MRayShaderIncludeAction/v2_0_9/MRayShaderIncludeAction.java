// $Id: MRayShaderIncludeAction.java,v 1.1 2007/06/17 15:34:43 jim Exp $

package us.temerity.pipeline.plugin.MRayShaderIncludeAction.v2_0_9;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   S H A D E R   I N C L U D E   A C T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a Mental Ray scene fragment which specifies the locations of a set of shader 
 * definition files and dynamic libraries. <P> 
 * 
 * Generates a Mental Ray scene fragment (.mi") which contains "$include" commands for 
 * each of the shader definition files (.mi) and dynamic libraries (.so) associated with 
 * the primary file sequence of one of the source nodes. <P> 
 * 
 * Also generates a tiny Maya Scene as a secondary file sequence which contains a 
 * ScriptNode that properly load the Mental Ray shaders into Maya.  This ScriptNode scene 
 * is usually included into downstream scenes which need to use the shaders via Maya's 
 * scene referencing. 
 */
public
class MRayShaderIncludeAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  MRayShaderIncludeAction() 
  {
    super("MRayShaderInclude", new VersionID("2.0.9"), "Temerity",
	  "Generates a Mental Ray scene fragment which specifies the locations of a set " + 
	  "of shader definition files and dynamic libraries.");     
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
    Path targetDefs = null;
    Path targetIncludes = null;
    TreeSet<Path> defs = new TreeSet<Path>();
    TreeSet<Path> libs = new TreeSet<Path>();
    {
      /* the generated MI shader includes file */ 
      {	
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || (suffix == null) || !suffix.equals("mi"))
	  throw new PipelineException
	    ("The MRayShaderInclude Action requires that the secondary target file " + 
	     "sequence be a single Mental Ray file (.mi)."); 

	targetIncludes = new Path(PackageInfo.sProdPath,
				  nodeID.getWorkingParent() + "/" + fseq.getPath(0));
      }

      /* the generated combined shader definition file */ 
      {
	SortedSet<FileSeq> fseqs = agenda.getSecondaryTargets();
	if(fseqs.size() != 1) 
	  throw new PipelineException
	    ("The MRayShaderInclude Action requires a secondary file sequence " + 
	     "which is a single Mental Ray shader definition file (.mi)."); 

	FileSeq fseq = fseqs.first();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || (suffix == null) || !suffix.equals("mi"))
	  throw new PipelineException
	    ("The MRayShaderInclude Action requires that the primary target file " + 
	     "sequence must be a single Mental Ray shader definition file (.mi)."); 
	  
	targetDefs = new Path(PackageInfo.sProdPath,
			      nodeID.getWorkingParent() + "/" + fseq.getPath(0));
      }
    }

    /* collect the set of shader definition files and DSO's */ 
    { 
      for(String sname : agenda.getSourceNames()) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || !(suffix.equals("mi") || suffix.equals("so")))
	throw new PipelineException
	  ("The MRayShaderInclude Action requires that the source node (" + sname + ") " + 
	   "must have either a single Mental Ray shader definition file (.mi) or a " + 
	   "compiled shader dynamic library (.so) file as its primary file sequence!");

	Path npath  = new Path(sname);	
	Path path = new Path(npath.getParentPath(), fseq.getPath(0));
	if(suffix.equals("mi"))
	  defs.add(path);
	else
	  libs.add(path); 
      }
    }

    File script = createTemp(agenda, 0644, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      /* write MI file */ 
      {
	out.write("cat > " + targetIncludes.toOsString() + " <<EOF\n");

	for(Path lpath : libs) 
	  out.write("link \"\\$WORKING" + lpath.toOsString() + "\"\n");

	out.write("\n");

	for(Path dpath : defs)
	  out.write("\\$include \"\\$WORKING" + dpath.toOsString() + "\"\n");

	out.write("EOF\n\n");
      }

      /* write combined shader definition file */ 
      {
	out.write("cat");
	for(Path dpath : defs) 
	  out.write(" $WORKING" + dpath);
	out.write(" > " + targetDefs.toOsString() + "\n\n");
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
      ArrayList<String> args = new ArrayList<String>();
      args.add(script.getPath());
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "bash", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = 8524206833958161080L;

}

