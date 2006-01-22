// $Id: GelShaderAction.java,v 1.1 2006/01/22 21:00:58 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   G E L   S H A D E R   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Compiles Gelato or RenderMan Shading Language source files to generate Gelato 
 * shader byte-code.<P> 
 * 
 * Compiles the Gelato shader source file (.gsl) or RenderMan shader source file (.sl) which 
 * is the single member of the primary file sequence of one of the source nodes into a 
 * byte-code (.gso) shader which is the single member of the primary file sequence of this 
 * node.  RenderMan shaders (.sl) will be first translated Gelato Shading Language using
 * the RSL2GSL tool.<P> 
 * 
 * See the Gelato documentation for details about <B>gslc</B>(1) and the 
 * <A href="http://www.renderman.org/RMR/Utils/gelato/download/rsl2gsl_ReadMe.html">
 * RSL2GSL</A> documentation for information on shader translation.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Shader Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Shading Language source for the shader.<BR>
 *   </DIV> <P>
 * 
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
 * 
 *   Encrypt <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to encrypt the generated shader byte-code.
 *   </DIV> <P>
 * 
 * 
 *   Verbosity: <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies the level of verbosity of the gslc(1) shader compiler.
 *     <UL>
 *       <LI> Errors
 *       <LI> Warnings
 *       <LI> Details
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Debug DSO <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to print additional information about DSO loading.
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV>
 */
public
class GelShaderAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GelShaderAction() 
  {
    super("GelShader", new VersionID("2.0.0"), "Temerity",
	  "Compiles Gelato or RenderMan Shading Language source files to generate " + 
	  "Gelato shader byte-code.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ShaderSource",
	 "The source node which contains the Shading Language source for the shader.",
	 null);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Errors");
      choices.add("Warnings"); 
      choices.add("Details");

      ActionParam param = 
	new EnumActionParam
	("Verbosity", 
	 "Specifies the level of verbosity of the gslc(1) shader compiler.",
	 "Errors", choices);
      addSingleParam(param);
    }       

    {
      ActionParam param = 
	new BooleanActionParam
	("DebugDSO",
	 "Whether to print additional information about DSO loading.", 
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
      ActionParam param = 
	new BooleanActionParam
	("Encrypt",
	 "Whether to encrypt the generated shader byte-code.",
	 false);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("ExtraOptions",
	 "Additional command-line arguments.", 
	 null);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ShaderSource");
      layout.addSeparator();
      layout.addEntry("IncludePaths");
      layout.addEntry("DefinedSymbols");
      layout.addSeparator();
      layout.addEntry("Encrypt");
      layout.addSeparator();
      layout.addEntry("Verbosity");
      layout.addEntry("DebugDSO");
      layout.addEntry("ExtraOptions");

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
    File source = null; 
    boolean isRenderMan = false;
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
	if((suffix == null) || (!suffix.equals("sl") && !suffix.equals("gsl")) ||
	   (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain a single " + 
	     "Gelato Shading Language (.gsl) or RenderMan Shading Languge (.sl) " + 
	     "source code file.");

	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	source = new File(PackageInfo.sProdDir, 
			  snodeID.getWorkingParent() + "/" + fseq.getFile(0));

	isRenderMan = suffix.equals("sl");
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	FilePattern fpat = fseq.getFilePattern();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("gso") || (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a single " + 
	     "compiled Gelato shader (.gso) file.");

	target = fseq.getFile(0);

	if(!prefix.equals(fpat.getPrefix())) 
	  throw new PipelineException
	    ("The shader source file (" + source + ") and compiled shader file " + 
	     "(" + target + ") must have the same prefix.");
      }
    }

    ArrayList<String> args = new ArrayList<String>();
    {
      {
	EnumActionParam param = (EnumActionParam) getSingleParam("Verbosity"); 
	args.add("-verbosity");
	args.add(String.valueOf(param.getIndex()));
      }	  

      Boolean debug = (Boolean) getSingleParamValue("DebugDSO");
      if((debug != null) && debug) 
	args.add("-debugdso");
      
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

      Boolean encrypt = (Boolean) getSingleParamValue("Encrypt");
      if((encrypt != null) && encrypt) 
	args.add("-x");

      addExtraOptions(args);
    }

    SortedMap<String,String> env = agenda.getEnvironment();

    if(isRenderMan) {
      File script = createTemp(agenda, 0755, "bash");
      File gsl = createTemp(agenda, 0666, "gsl");
      try {      
	FileWriter out = new FileWriter(script);

	out.write
	  ("#!/bin/bash\n" +
	   "\n" + 
	   "cpp -DGELATO=1 " + source + " | " + 
	   "rslparse -gsl -indirect defaultIndirect -occlusion defaultOcclusion " + 
	   source + " > " + gsl + "\n");
	   
	String path = env.get("PATH");
	if(path == null) 
	  throw new PipelineException
	    ("Somehow no PATH was defined by the toolset!");

	ExecPath epath = new ExecPath(path);
	File rpath = epath.which("rslparse");
	if(rpath == null) 
	  throw new PipelineException
	    ("Unable to determine the path to the rslparse(1) executable!");
	
	out.write("gslc -I" + rpath.getParent()); 

	for(String arg : args) 
	  out.write(" " + arg);

	out.write(" -o " + target + " " + gsl + "\n");

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
	   script.getPath(),  new ArrayList<String>(), env, agenda.getWorkingDir(), 
	   outFile, errFile); 
      }
      catch(Exception ex) {
	throw new PipelineException
	  ("Unable to generate the SubProcess to perform this Action!\n" +
	   ex.getMessage());
      }
    }
    else {
      try {
	args.add("-o");
	args.add(target.toString());
	args.add(source.toString());
	
	return new SubProcessHeavy
	  (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	   "gslc", args, env, agenda.getWorkingDir(), 
	   outFile, errFile);
      }
      catch(Exception ex) {
	throw new PipelineException
	  ("Unable to generate the SubProcess to perform this Action!\n" +
	   ex.getMessage());
      }
    }
  }

  /**
   * Append any additional command-line arguments.
   */ 
  private void 
  addExtraOptions
  (
   ArrayList<String> args
  ) 
    throws PipelineException
  {
    String extra = (String) getSingleParamValue("ExtraOptions");
    if(extra == null) 
      return;

    String parts[] = extra.split("\\p{Space}");
    int wk;
    for(wk=0; wk<parts.length; wk++) {
      if(parts[wk].length() > 0) 
	args.add(parts[wk]);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7393136187111248327L;

}

