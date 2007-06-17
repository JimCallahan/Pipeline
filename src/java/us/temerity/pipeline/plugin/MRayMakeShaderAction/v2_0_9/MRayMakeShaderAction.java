// $Id: MRayMakeShaderAction.java,v 1.1 2007/06/17 15:34:42 jim Exp $

package us.temerity.pipeline.plugin.MRayMakeShaderAction.v2_0_9;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   M A K E   S H A D E R   A C T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a C source file containing definitions and function stubs for a Mental Ray 
 * shader based on a shader definition file. <P> 
 * 
 * Uses mkmishader(1) to generate C source file (.c) containing definitions and function 
 * stubs from a Mental Ray shader definition file (.mi). 
 * 
 * See the Mental Ray documentation for details about <B>mkmishader(1)</B>(1). <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Shader Definition <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the definition for the shader.<BR>
 *   </DIV> <BR>
 * 
 *   Mode <BR>
 *   <DIV style="margin-left: 40px;">
 *     What types of functions stubs to create.
 *     <UL>
 *       <LI> Shader Only - Create the base shader function only.
 *       <LI> Init, Shader & Exit - Also create init and exit shaders.
 *     </UL>
 *   </DIV> <BR>
 * </DIV>
 */
public 
class MRayMakeShaderAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  MRayMakeShaderAction() 
  {
    super("MRayMakeShader", new VersionID("2.0.9"), "Temerity",
	  "Generates a C source file containing definitions and function stubs for a " + 
	  "Mental Ray shader based on a shader definition file.");
   
    {
      ActionParam param = 
	new LinkActionParam
	("ShaderDefinition",
	 "The source node which contains the definition for the shader.", 
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Shader Only");
      choices.add("Init, Shader & Exit");

      ActionParam param = 
	new EnumActionParam
	("Mode", 
	 "What types of functions stubs to create.", 
	 "Shader Only", choices);
      addSingleParam(param);
    }       

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ShaderDefinition");
      layout.addSeparator();
      layout.addEntry("Mode");

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
    Path source = null; 
    Path target = null; 
    {
      String prefix = null;
      {    
	String sname = (String) getSingleParamValue("ShaderDefinition"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Shader Definition was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Shader Definition (" + sname + ") was not one of the " + 
	     "source nodes!");

	FilePattern fpat = fseq.getFilePattern();
	prefix = fpat.getPrefix();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("mi") || (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain a single " + 
	     "Mental Ray shader definition (.mi) source code file.");

	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	source = new Path(PackageInfo.sProdPath, 
			  snodeID.getWorkingParent() + "/" + fseq.getPath(0));
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	FilePattern fpat = fseq.getFilePattern();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("c") || (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a single " + 
	     "shader C source (.c) file.");

	target = fseq.getPath(0);

	if(!prefix.equals(fpat.getPrefix())) 
	  throw new PipelineException
	    ("The shader source file (" + source + ") and shader C source file " + 
	     "(" + target + ") must have the same prefix!");
      }
    }

    ArrayList<String> args = new ArrayList<String>();

    {
      EnumActionParam param = (EnumActionParam) getSingleParam("Mode");
      switch(param.getIndex()) {
      case 0:
	break;
	
      case 1:
	args.add("-i");
	break;

      default:
	throw new PipelineException
	  ("Illegal Generate value!");
      }
    }
    
    try {
      args.add("-v");
      args.add(source.toOsString());
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "mkmishader", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -1967016229365629084L;

}

