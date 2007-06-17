// $Id: PxShaderAction.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.PxShaderAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P X   S H A D E R   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Compiles Shading Language source files to generate a Pixie shader. <P>
 * 
 * Compiles the shader source file (.sl) which is the single member of the primary file 
 * sequence of one of the source nodes into a byte-code (.sdr) shader which is the single member 
 * of the primary file sequence of this node. <P> 
 * 
 * See the <A href="http://pixie.sourceforge.net/">Pixie</A> documentation for details 
 * about <B>sdrc</B>(1). <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Shader Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Shading Language source for the shader.<BR>
 *   </DIV> <BR>
 * 
 *   Verbose <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to print verbose messages.
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
class PxShaderAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  PxShaderAction() 
  {
    super("PxShader", new VersionID("2.0.0"), "Temerity",
	  "Compiles Shading Language source files to generate a Pixie shader.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ShaderSource",
	 "The source node which contains the Shading Language source for the shader.",
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	("Verbose",
	 "Whether to print verbose messages.",
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
      layout.addEntry("ShaderSource");
      layout.addSeparator();
      layout.addEntry("Verbose");
      layout.addEntry("IncludePaths");
      layout.addEntry("DefinedSymbols");

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

    /* file sequence checks */ 
    File source = null; 
    File sourceDir = null; 
    File target = null; 
    String prefix = null;
    {
      {    
	String sname = (String) getSingleParamValue("ShaderSource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Shader Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Shader Source (" + sname + ") was not one of the source nodes!");

	FilePattern fpat = fseq.getFilePattern();
	prefix = fpat.getPrefix();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("sl") || (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain a single " + 
	     "Shading Language (.sl) source code file.");

	source = fseq.getFile(0);

	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	sourceDir = new File(PackageInfo.sProdDir, snodeID.getWorkingParent().toString());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	FilePattern fpat = fseq.getFilePattern();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("sdr") || (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a single " + 
	     "compiled Pixie shader (.sdr) file.");

	target = new File(PackageInfo.sProdDir, 
			  nodeID.getWorkingParent() + "/" + fseq.getFile(0));

	if(!prefix.equals(fpat.getPrefix())) 
	  throw new PipelineException
	    ("The shader source file (" + source + ") and compiled shader file " + 
	     "(" + target + ") must have the same prefix.");
      }
    }

    ArrayList<String> args = new ArrayList<String>();

    Boolean verbose = (Boolean) getSingleParamValue("Verbose");
    if((verbose != null) && verbose) 
      args.add("-ri");

    {
      String value = (String) getSingleParamValue("IncludePaths");
      if(value != null) {
	String[] paths = value.split(":");
	int wk;
	for(wk=0; wk<paths.length; wk++) {
	  if((paths[wk] != null) && (paths[wk].length() > 0))
	    args.add("-I" + paths[wk]);
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
	    args.add("-D" + symbols[wk]);
	}
      }
    }

    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      String cmdopts = null;
      {
	StringBuilder buf = new StringBuilder();
	buf.append("sdrc");
	for(String arg : args) 
	  buf.append(" " + arg);
	cmdopts = buf.toString();
      }
      
      out.write("#!/bin/bash\n\n" +
		"cd " + sourceDir + "\n" + 
		"sdrc");

      for(String arg : args) 
	out.write(" " + arg);

      out.write(" -o " + target + " " + source + "\n");       

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

  private static final long serialVersionUID = 587441020410233133L;

}

