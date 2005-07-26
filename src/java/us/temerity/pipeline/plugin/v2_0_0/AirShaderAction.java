// $Id: AirShaderAction.java,v 1.2 2005/07/26 04:58:30 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A I R   S H A D E R   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Compiles RenderMan Shading Language source files into AIR shaders. <P>
 * 
 * Compiles the shader source file (.sl) which is the single member of the primary file 
 * sequence of one of the source nodes into a byte-code or object-code (.sdl) shader which 
 * is the single member of the primary file sequence of this node. <P> 
 * 
 * See the <A href="http://www.sitexgraphics.com/html/air.html">AIR</A> documentation for 
 * <A href="http://www.sitexgraphics.com/air.pdf"><B>shaded</B></A>(1) for details. <P>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Shader Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Shading Language source for the shader.<BR>
 *   </DIV> <BR>
 * 
 *   Generate <BR> 
 *   <DIV style="margin-left: 40px;">
 *     The type of compiled shader to generate: <BR>
 *     <UL>
 *       <LI>Byte-Code - Generates platform independent byte-code.
 *       <LI>Object-Code - Generates optimized plaform specific object-code.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Optimization <BR> 
 *   <DIV style="margin-left: 40px;">
 *     The level of compiler optimization: <BR>
 *     <UL>
 *       <LI>None - No optimization.
 *       <LI>Runtime - Optimize each instance of shader at runtime.
 *     </UL>
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
class AirShaderAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AirShaderAction() 
  {
    super("AirShader", new VersionID("2.0.0"), 
	  "Compiles RenderMan Shading Language source files into AIR shaders.");
    
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
      choices.add("Byte-Code");
      choices.add("Object-Code");

      ActionParam param = 
	new EnumActionParam
	("Generate", 
	 "The type of compiled shader to generate.",
	 "Byte-Code", choices);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new BooleanActionParam
	("StaticOptimize", 
	 "Whether to perform static compile-time optimizations.",
	 true);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new BooleanActionParam
	("RuntimeOptimize", 
	 "Whether to perform runtime per-instance optimizations.",
	 true);
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
      layout.addEntry("Generate");
      layout.addEntry("StaticOptimize");
      layout.addEntry("RuntimeOptimize");
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
    File source = null; 
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
	if((suffix == null) || !(suffix.equals("sl") || suffix.equals("vsl")) || 
	   (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain a single " + 
	     "Shading Language (.sl|.vsl) source code file.");

	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	source = new File(PackageInfo.sProdDir, 
			  snodeID.getWorkingParent() + "/" + fseq.getFile(0));
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	FilePattern fpat = fseq.getFilePattern();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("slb") || (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a single " + 
	     "compiled shader (.slb) file.");

	target = fseq.getFile(0);

	if(!prefix.equals(fpat.getPrefix())) 
	  throw new PipelineException
	    ("The shader source file (" + source + ") and compiled shader file " + 
	     "(" + target + ") must have the same prefix.");
      }
    }

    ArrayList<String> args = new ArrayList<String>();

    {
      EnumActionParam param = (EnumActionParam) getSingleParam("Generate");
      switch(param.getIndex()) {
      case 0:
	break;
	
      case 1:
	{
	  boolean found = false;
	  for(FileSeq fseq : agenda.getSecondaryTargets()) {
	    FilePattern fpat = fseq.getFilePattern();
	    String suffix = fpat.getSuffix();
	    if(fpat.getPrefix().equals(prefix) && (suffix != null) && suffix.equals("sso")) {
	      found = true; 
	      break;
	    }
	  }

	  if(!found)
	    throw new PipelineException
	      ("Native object-code shaders must also have a secondary file sequence " + 
	       "containing (" + prefix + ".sso)!");

	  args.add("-dso");
	}
	break;

      default:
	throw new PipelineException
	  ("Illegal Generate value!");
      }
    }
    
    Boolean sopt = (Boolean) getSingleParamValue("StaticOptimize");
    if((sopt == null) || !sopt) 
      args.add("-noopt");

    Boolean ropt = (Boolean) getSingleParamValue("RuntimeOptimize");
    if((ropt == null) || !ropt) 
      args.add("-norto");

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

    try {
      args.add("-o");
      args.add(target.toString());
      args.add(source.toString());
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "shaded", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -7265551405599713462L;

}

