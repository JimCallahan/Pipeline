// $Id: HfsVEXAction.java,v 1.2 2005/09/07 19:17:08 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   V E X   A C T I O N                                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates an OTL by compiling a set of VEX based Houdini OPs/Shaders. <P>
 * 
 * Compiles a set of Houdini VEX operator and/or shader source files (.vfl) which are the 
 * single member of the primary file sequence of the source nodes to generate an Operator 
 * Type Library (.otl) file.  If a source node contains an icon (.icon) file as a secondary 
 * sequence it will be used as the icon used for the operator in Houdini. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Encrypt <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to encrypt the VEX included in the OTL.
 *   </DIV> <BR>
 * 
 *   Include Paths <BR>
 *   <DIV style="margin-left: 40px;">
 *     Colon seperated list of additional directories to search for included files.
 *   </DIV> <BR>
 * 
 *   Defined Symbols <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whitespace seperated preprocessor symbol definitions.
 *   </DIV> <BR>
 * </DIV>
 */
public
class HfsVEXAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsVEXAction() 
  {
    super("HfsVEX", new VersionID("1.0.0"), "Temerity", 
	  "Generates an OTL by compiling a set of VEX based Houdini OPs/Shaders.");
   
    {
      ActionParam param = 
	new BooleanActionParam
	("Encrypt", 
	 "Whether to encrypt the VEX included in the OTL.", 
	 false);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new StringActionParam
	("IncludePaths",
	 "Colon seperated list of additional directories to search for included files.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("DefinedSymbols",
	 "Whitespace seperated preprocessor symbol definitions.", 
	 null);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("Encrypt");
      layout.addEntry("IncludePaths");
      layout.addEntry("DefinedSymbols");

      setSingleLayout(layout);   
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
	  if((suffix == null) || !suffix.equals("vfl") || (fseq.numFrames() != 1))
	    throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain a single " + 
	     "(.vfl) file containing VEX source code.");
	  
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

      String options = null;
      {
	StringBuffer buf = new StringBuffer();
	{
	  Boolean encrypt = (Boolean) getSingleParamValue("Encrypt");
	  if((encrypt != null) && encrypt) 
	    buf.append(" -c"); 
	}
	
	{
	  String value = (String) getSingleParamValue("IncludePaths");
	  if(value != null) {
	    String[] paths = value.split(":");
	    int wk;
	    for(wk=0; wk<paths.length; wk++) {
	      if((paths[wk] != null) && (paths[wk].length() > 0))
		buf.append(" -I" + paths[wk]);
	    }
	  }
	}
	
	{
	  String value = (String) getSingleParamValue("DefinedSymbols");
	  if(value != null) {
	    String[] symbols = value.split("\\p{Space}");
	    int wk;
	    for(wk=0; wk<symbols.length; wk++) {
	      if((symbols[wk] != null) && (symbols[wk].length() > 0))
		buf.append(" -D" + symbols[wk]);
	    }
	  }
	}
	
	options = buf.toString();
      }

      for(File sourceFile : sources.keySet()) {
	out.write("echo Compiling: " + sourceFile.getName() + "\n" + 
		  "vcc" + options);

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

  private static final long serialVersionUID = -6536153771955987722L;

}

