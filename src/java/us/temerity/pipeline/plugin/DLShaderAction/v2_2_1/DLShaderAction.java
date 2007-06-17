// $Id: DLShaderAction.java,v 1.1 2007/06/17 15:34:39 jim Exp $

package us.temerity.pipeline.plugin.DLShaderAction.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D L   S H A D E R   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Compiles RenderMan Shading Language source files into 3Delight shaders. 
 * 
 * Compiles the shader source file (.sl) which is the single member of the primary file 
 * sequence of one of the source nodes into a byte-code or object-code (.sdl) shader which 
 * is the single member of the primary file sequence of this node. <P> 
 * 
 * See the <A href="http://www.3delight.com">3Delight</A> documentation for
 * <A href="http://www.3delight.com/ZDoc/3delight_11.html"><B>shaderdl</B></A>(1) for 
 * details. <P> 
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
 *       <LI>Basic - Only the most basic optimizations are performed.
 *       <LI>Default - All well tested optimizations.
 *       <LI>Agressive - Most agressive optimizations included some experimental optimizations
 *           which may not always produce faster results.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Include Paths <BR>
 *   <DIV style="margin-left: 40px;">
 *     A semicolon seperated list of additional directories to search for included files. 
 *     Toolset environmental variable substitutions are enabled (see {@link 
 *     ActionAgenda#evaluate evaluate}).
 *   </DIV> <BR>
 * 
 *   Defined Symbols <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whitespace seperated preprocessor symbol definitions.  Toolset environmental variable 
 *     substitutions are enabled (see {@link ActionAgenda#evaluate evaluate}).
 *   </DIV> <BR>
 *   
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV>
 */
public
class DLShaderAction
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DLShaderAction() 
  {
    super("DLShader", new VersionID("2.2.1"), "Temerity", 
	  "Compiles RenderMan Shading Language source files into 3Delight shaders.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aShaderSource,
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
	(aGenerate, 
	 "The type of compiled shader to generate.",
	 "Byte-Code", choices);
      addSingleParam(param);
    } 

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("None");
      choices.add("Basic");
      choices.add("Default");
      choices.add("Agressive");

      ActionParam param = 
	new EnumActionParam
	(aOptimization, 
	 "The level of shader optimization.",
	 "Default", choices);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new StringActionParam
	(aIncludePaths,
	 "A semicolon seperated list of additional directories to search for included " + 
         "files.  Toolset environmental variable substitutions are enabled.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aDefinedSymbols,
	 "Whitespace seperated preprocessor symbol definitions.  Toolset environmental " +
         "variable substitutions are enabled.", 
	 null);
      addSingleParam(param);
    }

    addExtraOptionsParam(); 

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aShaderSource);
      layout.addSeparator();
      layout.addEntry(aGenerate);
      layout.addEntry(aOptimization);
      layout.addSeparator();
      layout.addEntry(aIncludePaths);
      layout.addEntry(aDefinedSymbols);
      layout.addSeparator();
      addExtraOptionsParamToLayout(layout); 

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
    Path sourcePath = getPrimarySourcePath(aShaderSource, agenda, 
                                           "sl", "Shading Language (.sl) source code file");
    if(sourcePath == null) 
      throw new PipelineException("The ShaderSource node was not specified!");

    /* target compiled shader path */ 
    Path targetPath = getPrimaryTargetPath(agenda, "sdl", "compiled shader file"); 
    
    /* validate shader name */
    String prefix = null;
    {
      prefix = agenda.getPrimaryTarget().getFilePattern().getPrefix();
      FileSeq fseq = agenda.getPrimarySource(getSingleStringParamValue(aShaderSource));
      if(!prefix.equals(fseq.getFilePattern().getPrefix()))
        throw new PipelineException
          ("The shader source file (" + sourcePath + ") and compiled shader file " + 
           "(" + targetPath + ") must have the same prefix.");
    }

    /* get additional include directories */ 
    ArrayList<Path> includes = new ArrayList<Path>();
    {
      String incs = getSingleStringParamValue(aIncludePaths);
      if(incs != null) {
        String parts[] = agenda.evaluate(incs).split(";"); 
        int wk;
        for(wk=0; wk<parts.length; wk++) {
          if((parts[wk] != null) && (parts[wk].length() > 0))
            includes.add(new Path(parts[wk]));
        }
      }
    }

    /* get preprocessor defines */ 
    ArrayList<String> defines = new ArrayList<String>();
    {
      String defs = getSingleStringParamValue(aDefinedSymbols);
      if(defs != null) {
        String parts[] = agenda.evaluate(defs).split("\\p{Space}");
        int wk;
        for(wk=0; wk<parts.length; wk++) {
          if((parts[wk] != null) && (parts[wk].length() > 0))
            defines.add(parts[wk]);
        }
      }
    }
    
    /* build command-line arguments */ 
    ArrayList<String> args = new ArrayList<String>();
    {
      switch(getSingleEnumParamIndex(aGenerate)) {
      case 0:
	args.add("--int");
	break;
	
      case 1:
	args.add("--dso");
	args.add("--dont-keep-c++-file");
	break;

      default:
	throw new PipelineException
	  ("Illegal Generate value!");
      }

      switch(getSingleEnumParamIndex(aOptimization)) {
      case 0:
	args.add("-O0");
	break;
	
      case 1:
	args.add("-O1");
	break;

      case 2:
	args.add("-O2");
	break;

      case 3:
	args.add("-O3");
	break;

      default:
	throw new PipelineException
	  ("Illegal Optimization level!");
      }
  
      for(String def : defines) 
        args.add("-D" + def);

      for(Path inc : includes) 
        args.add("-I" + inc); 

      args.addAll(getExtraOptionsArgs());

      args.add("-o");
      args.add(targetPath.toOsString());
      args.add(sourcePath.toOsString());
    }

    /* shader compiler */ 
    String program = "shaderdl";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "shaderdl.exe";

    /* create the process to run the action */ 
    return createSubProcess(agenda, program, args, null, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1658882618600481817L;

  public static final String aShaderSource   = "ShaderSource";
  public static final String aGenerate       = "Generate";
  public static final String aOptimization   = "Optimization";
  public static final String aIncludePaths   = "IncludePaths";
  public static final String aDefinedSymbols = "DefinedSymbols";
}

