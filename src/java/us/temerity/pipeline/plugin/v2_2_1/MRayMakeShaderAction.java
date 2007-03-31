// $Id: MRayMakeShaderAction.java,v 1.1 2007/03/31 23:11:11 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

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
    super("MRayMakeShader", new VersionID("2.2.1"), "Temerity",
	  "Generates a C source file containing definitions and function stubs for a " + 
	  "Mental Ray shader based on a shader definition file.");
   
    {
      ActionParam param = 
	new LinkActionParam
	(aShaderDefinition,
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
	(aMode, 
	 "What types of functions stubs to create.", 
	 "Shader Only", choices);
      addSingleParam(param);
    }       

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aShaderDefinition);
      layout.addSeparator();
      layout.addEntry(aMode);

      setSingleLayout(layout);   
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
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
    /* source shader definition path */ 
    Path sourcePath = getPrimarySourcePath(aShaderDefinition, agenda, "mi", 
                                           "Mental Ray shader definition file"); 
    if(sourcePath == null) 
      throw new PipelineException("The ShaderDefinition node was not specified!");

    /* target stub shader path */ 
    Path targetPath = getPrimaryTargetPath(agenda, "c", "C source code file");

    /* generate the shader stub */ 
    {
      String program = "mkmishader";
      if(PackageInfo.sOsType == OsType.Windows) 
        program = "mkmishader.exe";

      ArrayList<String> args = new ArrayList<String>();
      
      if(getSingleEnumParamIndex(aMode) == 1)
	args.add("-i");

      args.add("-v");
      args.add(sourcePath.toOsString());

      /* create the process to run the action */ 
      return createSubProcess(agenda, program, args, agenda.getEnvironment(), 
                              agenda.getTargetPath().toFile(), outFile, errFile);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7965136307489712985L;

  public static final String aShaderDefinition = "ShaderDefinition";
  public static final String aMode             = "Mode";

}

