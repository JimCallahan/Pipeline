// $Id: MRayShaderAction.java,v 1.1 2007/06/17 15:34:43 jim Exp $

package us.temerity.pipeline.plugin.MRayShaderAction.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   S H A D E R   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Compiles C source files to generate a Mental Ray shader DSO. <P>
 * 
 * Compiles the shader source file (.c) which is the single member of the primary file 
 * sequence of one of the source nodes into native dynamic library (.so, .dll) shader which 
 * is the single member of the primary file sequence of this node. <P> 
 * 
 * Note that the behavior and results of this Action are highly hardware and operating system
 * specific.  Shaders are compiled using the native tools on the machine where the action 
 * is run and the shader DSO produced will only be compatible with operating system used 
 * to compile the shader.  Typically, a single shader C source file will be compiled into
 * DSO's by seperate nodes for each OS supported at a studio.  You should use Selection Keys
 * to limit each of the nodes using this Action to a single hardware/operating system 
 * configuration to avoid architecture incompatibility problems. <P>
 * 
 * See the Mental Ray documentation for details on shader compilation and linking. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Shader Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the C source for the shader.<BR>
 *   </DIV> <BR>
 * 
 *   Architecture <BR>
 *   <DIV style="margin-left: 40px;">
 *     CPU architecture of target render hosts.
 *     <UL>
 *       <LI> 32-Bit
 *       <LI> 64-Bit
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Debug <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to compile with debugging flags and options.
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
 *   </DIV>
 * </DIV> <P> 
 * 
 * By default, the "python" program is used by this action to run the compilation commands.  
 * An alternative program can be specified by setting PYTHON_BINARY in the Toolset 
 * environment to the name of the Python interpertor this Action should use.  When naming an 
 * alternative Python interpretor under Windows, make sure to include the ".exe" extension 
 * in the program name.
 */
public
class MRayShaderAction
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  MRayShaderAction() 
  {
    super("MRayShader", new VersionID("2.2.1"), "Temerity",
	  "Compiles C source files to generate a Mental Ray shader DSO."); 
    
    {
      ActionParam param = 
	new LinkActionParam
	(aShaderSource,
	 "The source node which contains the C source for the shader.",
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("32-Bit");
      choices.add("64-Bit"); 

      ActionParam param = 
	new EnumActionParam
	(aArchitecture, 
	 "CPU architecture of target render hosts.", 
	 "32-Bit", choices);
      addSingleParam(param);
    } 
       
    {
      ActionParam param = 
	new BooleanActionParam
	(aDebug,
	 "Whether to compile with debugging flags and options.", 
	 false);
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
      layout.addEntry(aArchitecture);
      layout.addEntry(aDebug);
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
    Path sourcePath = getPrimarySourcePath(aShaderSource, agenda, "c", "C source code file");
    if(sourcePath == null) 
      throw new PipelineException("The ShaderSource node was not specified!");

    /* target DSO path */ 
    Path targetPath = null;
    if(PackageInfo.sOsType == OsType.Windows) 
      targetPath = getPrimaryTargetPath(agenda, "dll", "compiled shader DSO file"); 
    else 
      targetPath = getPrimaryTargetPath(agenda, "so", "compiled shader DSO file"); 

    /* validate shader name */
    String prefix = null;
    {
      prefix = agenda.getPrimaryTarget().getFilePattern().getPrefix();
      FileSeq fseq = agenda.getPrimarySource(getSingleStringParamValue(aShaderSource));
      if(!prefix.equals(fseq.getFilePattern().getPrefix()))
        throw new PipelineException
          ("The shader C source file (" + sourcePath + ") and compiled shader DSO " + 
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
    
    /* the path to the Mental Ray SDK include directory */ 
    Path devkit = null;
    {
      String miRoot = agenda.getEnvironment().get("MI_ROOT");
      if(miRoot == null) 
        throw new PipelineException
          ("Cannot location the Mental Ray SDK without a MI_ROOT environmental variable " + 
           "defined in the current Toolset!");

      devkit = new Path(new Path(miRoot), "devkit");
    }

    /* temporary paths */ 
    Path tempPath = getTempPath(agenda);
    Path sourceTemp = null;
    Path objectTemp = null;
    {
      sourceTemp = new Path(tempPath, "shader.c"); 
      cleanupLater(sourceTemp.toFile());

      String objExt = (PackageInfo.sOsType == OsType.Windows) ? "obj" : "o";
        objectTemp = new Path(tempPath, "shader." + objExt); 
      cleanupLater(objectTemp.toFile());
    }

    /* create a temporary Python script to compile and link the shader */ 
    File script = createTemp(agenda, "py"); 
    try {
      FileWriter out = new FileWriter(script);

      /* import modules */ 
      out.write("import os\n" + 
                "import shutil\n");

      /* include the "launch" method definition */ 
      out.write(getPythonLaunchHeader()); 

      /* copy shader source file to temporary directory */ 
      out.write("shutil.copy('" + sourcePath + "', '" + sourceTemp + "')\n\n");

      /* compilation is different for each OS */ 
      if(PackageInfo.sOsType == OsType.Unix) { 
        String arch = null;
        switch(getSingleEnumParamIndex(aArchitecture)) {
        case 0:
          arch = "-m32";
          break;
          
        case 1:
          arch = "-m64";
          break;
          
        default:
          throw new PipelineException("Illegal Architecture value!");
        }
          
        /* compile to object code */ 
        out.write("launch('gcc', ['" + arch + "', '-c', '-fPIC', '-Bsymbolic'"); 
          
        if(getSingleBooleanParamValue(aDebug)) 
          out.write(", '-g', '-DDEBUG'"); 
        else 
          out.write(", '-O3'"); 
        
        for(String def : defines) 
          out.write(", '-D" + def + "'");
        
        for(Path inc : includes) 
          out.write(", '-I" + inc + "'");
        out.write(", '-I" + devkit + "'");
        
        out.write(", '" + sourceTemp + "'])\n\n");
          
        /* link the object code into a DSO */ 
        out.write("launch('gcc', ['" + arch + "'");
          
        if(getSingleBooleanParamValue(aDebug)) 
          out.write(", '-g'"); 
        
        out.write(", '-o', '" + targetPath + "', '-shared', '-rdynamic', " + 
                  "'" + objectTemp + "'])\n\n"); 
      }
      else if(PackageInfo.sOsType == OsType.MacOS) {
        if(getSingleEnumParamIndex(aArchitecture) != 0) 
          throw new PipelineException
            ("Only the 32-bit architecture is supported on Mac OS X at this time!");

        /* compile to object code */ 
        out.write("launch('gcc', ['-c', '-fPIC', '-dynamic', '-fno-common'");
          
        if(getSingleBooleanParamValue(aDebug)) 
          out.write(", '-g', '-DDEBUG'"); 
        else 
          out.write(", '-O3'"); 
        
        for(String def : defines) 
          out.write(", '-D" + def + "'");
        
        for(Path inc : includes) 
          out.write(", '-I" + inc + "'");
        out.write(", '-I" + devkit + "'");
        
        out.write(", '" + sourceTemp + "'])\n\n");
          
        /* link the object code into a DSO */ 
        out.write("launch('libtool', " + 
                  "['-flat_namespace', '-undefined', 'suppress', '-dynamic'"); 
        
        if(getSingleBooleanParamValue(aDebug)) 
          out.write(", '-g'"); 
        
        out.write(", '-o', '" + targetPath + "', '" + objectTemp + "'])\n\n"); 
      }
      else if(PackageInfo.sOsType == OsType.Windows) {
        /* compile to object code */ 
        out.write("launch('cl.exe', ['/I', '" + escPath(devkit) + "'");

        for(Path inc : includes) 
          out.write(", '/I', '" + escPath(inc) + "'");
        
        if(getSingleEnumParamIndex(aArchitecture) == 1) 
          out.write(", '/Zp8', '/EHsc'"); 
        
        if(!getSingleBooleanParamValue(aDebug)) 
          out.write(", '/O2'");

        out.write(", '/c', '/MD', '/nologo', '/W3', '-DWIN_NT'"); 
        
        if(getSingleEnumParamIndex(aArchitecture) == 1) 
          out.write(", '-DBIT64'"); 
        
        for(String def : defines) 
          out.write(", '/D" + escPath(def) + "'");
         
        out.write(", '" + escPath(sourceTemp) + "'])\n\n");

        /* link the object code into a DLL */ 
        String targetName = targetPath.getName();
        out.write
          ("launch('link.exe', ['/libpath:" + escPath(devkit) + "', 'shader.lib', " +
           "'/nologo', '/dll', '/nodefaultlib:LIBC.LIB', '/opt:noref', '/incremental:no', " + 
           "'/out:" + targetName + "', '" + escPath(objectTemp) + "'])\n\n");

        /* add manifest to the DLL */ 
        out.write
          ("launch('mt.exe', ['/nologo', '-manifest', '" + targetName + ".manifest', " + 
           "'-outputresource:" + targetName + ";2'])\n\n");

        /* move DLL to target location */ 
        out.write("shutil.copy('" + targetName + "', '" + targetPath + "')\n\n");

        /* cleanup */
        {
          Path targetTemp = new Path(tempPath, targetName);
          cleanupLater(targetTemp.toFile());
          
          Path manifest = new Path(tempPath, targetName + ".manifest");
          cleanupLater(manifest.toFile());
          
          Path expTemp = new Path(tempPath, prefix + ".exp");
          cleanupLater(expTemp.toFile());
          
          Path libTemp = new Path(tempPath, prefix + ".lib");
          cleanupLater(libTemp.toFile());
        }
      }

      out.write("print 'ALL DONE.'\n");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to write the temporary Python script file (" + script + ") for Job " + 
           "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    /* create the process to run the action */ 
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add(script.toString());
      
      return createSubProcess(agenda, getPythonProgram(agenda), args, null, tempPath.toFile(),
                              outFile, errFile);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1407560999833266134L;

  public static final String aShaderSource   = "ShaderSource";
  public static final String aKeepTempFiles  = "KeepTempFiles";
  public static final String aArchitecture   = "Architecture"; 
  public static final String aDebug          = "Debug";
  public static final String aIncludePaths   = "IncludePaths";
  public static final String aDefinedSymbols = "DefinedSymbols";

}




