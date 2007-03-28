// $Id: MRayRenderAction.java,v 1.2 2007/03/28 20:03:50 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   R E N D E R   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders a collection of MI files using Mental Ray Standalone.<P> 
 * 
 * All of the MI file dependencies of the target image which set the Order per-source 
 * sequence parameter will be processed. The frame range rendered will be limited by frame 
 * numbers of the target images. The Action invokes a new instance of the Mental Ray renderer
 * for each MI file. This is due to how Mental Ray keeps library information loaded when 
 * rendering multiple MIs. Since each MI file may potentially have a different set of library 
 * files and includes, a separate invocation of 'ray' is necessary to ensure that each one 
 * runs in the correct environment.<P>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Framebuffer Type<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The data format of the target rendered images.
 *   </DIV> <BR>
 * 
 *   Keep Temp Files<BR>
 *   <DIV style="margin-left: 40px;">
 *     Should the Action preserve the temporary MI files it generates for rendering. 
 *   </DIV> <BR>
 *   
 *   Render Verbosity<BR>
 *   <DIV style="margin-left: 40px;">
 *     The verbosity of render progress, warning and error messages.
 *   </DIV> <BR>
 *   
 *   Include Path<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     A semicolon seperated list of directories which overrides the path used to resolve 
 *     "$include" statements in the MI scene file.  Toolset environmental variable 
 *     substitutions are enabled (see {@link ActionAgenda#evaluate evaluate}).
 *   </DIV> <BR>
 * 
 *   Library Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A semicolon seperated list of directories that mental ray searches for shader 
 *     libraries containing shader code before the default library paths.  Toolset 
 *     environmental variable substitutions are enabled (see {@link ActionAgenda#evaluate 
 *     evaluate}).
 *   </DIV> <BR>
 * 
 *   Texture Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A semicolon seperated list of directories that mental ray searches for texture files.  
 *     Toolset environmental variable substitutions are enabled (see {@link 
 *     ActionAgenda#evaluate evaluate}).
 *   </DIV> <BR>
 * 
 *   Extra Opts<BR>
 *   <DIV style="margin-left: 40px;">
 *      Additional command-line arguments.
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node sequence which sets this parameter should contain MI files. This 
 *     parameter determines the order in which the input MI files are processed. If this 
 *     parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P>
 * 
 * If the environmental variable MRAY_BINARY is defined, its value will be used as the 
 * name of the renderer executable instead of the "ray" (Unix/MacOS) or "ray345.exe" 
 * (Windows).  On Windows, the renderer name should include the ".exe" extension. <P> 
 * 
 * By default, the "python" program is used by this action when running on Windows to 
 * process the MI files.  An alternative program can be specified by setting PYTHON_BINARY 
 * in the Toolset environment to the name of the Python interpertor this Action should use.  
 * When naming an alternative Python interpretor under Windows, make sure to include the
 * ".exe" extension in the program name.
 */ 
public class MRayRenderAction
  extends MayaAction
{
  /*---------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                               */
  /*---------------------------------------------------------------------------------------*/

  public 
  MRayRenderAction() 
  {
    super("MRayRender", new VersionID("2.2.1"), "Temerity",
          "");

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("+rgba");
      choices.add("+rgba_16");
      choices.add("+rgba_fp");
      choices.add("+rgb");
      choices.add("+rgb_16");
      choices.add("+rgb_fp");
      choices.add("+rgbe");
      choices.add("+a");
      choices.add("+a_16");
      choices.add("+a_fp");
      choices.add("+s");
      choices.add("+s_16");
      choices.add("+s_fp");
      choices.add("+z");
      choices.add("+n");
      choices.add("+m");
      choices.add("+tag");
      choices.add("+vta");
      choices.add("+vts");
      choices.add("+bit");
      choices.add("+coverage");
      choices.add("-rgba");
      choices.add("-rgba_16");
      choices.add("-rgba_fp");
      choices.add("-rgb");
      choices.add("-rgb_16");
      choices.add("-rgb_fp");
      choices.add("-rgbe");
      choices.add("-a");
      choices.add("-a_16");
      choices.add("-a_fp");
      choices.add("-s");
      choices.add("-s_16");
      choices.add("-s_fp");
      choices.add("-z");
      choices.add("-n");
      choices.add("-m");
      choices.add("-tag");
      choices.add("-vta");
      choices.add("-vts");
      choices.add("-bit");
      choices.add("-coverage");

      ActionParam param = 
	new EnumActionParam
	(aFramebufferType, 
	 "The data format of the target rendered images.", 
	 "+rgba", choices);
      addSingleParam(param);      
    }

    {
      ActionParam param =
	new BooleanActionParam
	(aKeepTempFiles, 
	 "Should the Action preserve the temporary MI files it generates for rendering.", 
	 false);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("No Messages");
      choices.add("Fatal Messages Only");
      choices.add("Error Messages");
      choices.add("Warning Messages");
      choices.add("Info Messages");
      choices.add("Progress Messages");
      choices.add("Details Messages");

      ActionParam param = 
	new EnumActionParam
	(aRenderVerbosity,
	 "The verbosity of render progress, warning and error messages.",
	 "Warning Messages", choices);
      addSingleParam(param);
    }

    { 
      ActionParam param = 
	new StringActionParam
	(aIncludePath,
	 "A semicolon seperated list of directories which overrides the path used to " +
	 "resolve $include statements in the MI scene file.  Toolset environmental " + 
         "variable substitutions are enabled.", 
         "${MI_ROOT+}/include"); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aLibraryPath,
	 "A semicolon seperated list of directories that mental ray searches for shader " +
	 "libraries containing shader code before the default library paths.  Toolset " + 
         "environmental variable substitutions are enabled.", 
         "${MI_ROOT+}/lib"); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aTexturePath,
	 "A semicolon seperated list of directories that mental ray searches for texture " +
	 "files.  Toolset environmental variable substitutions are enabled.", 
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Mental Ray");
      choices.add("Maya");
      
      addPreset(aBaseLibraries, choices);

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aIncludePath, "${MI_ROOT+}/include");
        values.put(aLibraryPath, "${MI_ROOT+}/lib"); 
	
        addPresetValues(aBaseLibraries, "Mental Ray", values);
      }
      
      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aIncludePath, "${MAYA_LOCATION+}/mentalray/include");
        values.put(aLibraryPath, "${MAYA_LOCATION+}/mentalray/lib"); 
	
        addPresetValues(aBaseLibraries, "Maya", values);
      }
    }

    addExtraOptionsParam(); 
  
    {    
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aFramebufferType); 
      layout.addSeparator();
      layout.addEntry(aKeepTempFiles);
      layout.addEntry(aRenderVerbosity);
      layout.addSeparator();
      layout.addEntry(aBaseLibraries);
      layout.addEntry(aIncludePath);
      layout.addEntry(aLibraryPath);
      layout.addEntry(aTexturePath);
      layout.addSeparator();
      addExtraOptionsParamToLayout(layout); 

      setSingleLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    underDevelopment();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   B A S E   A C T I O N   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does this action support per-source parameters?
   */
  public boolean 
  supportsSourceParams()
  {
    return true;
  }

  /**
   * Get an initial set of action parameters associated with an upstream node.
   */
  public TreeMap<String, ActionParam> 
  getInitialSourceParams()
  {
    TreeMap<String, ActionParam> params = new TreeMap<String, ActionParam>();

    {
      ActionParam param = 
	new IntegerActionParam
	("Order",
	 "Each source node sequence which sets this parameter should contain MI files. " + 
         "This parameter determines the order in which the input MI files are processed. " + 
         "If this parameter is not set for a source node file sequence, it will be ignored.",
	 100);
      params.put(param.getName(), param);
    }

    return params;
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
    /* the paths to the target images and temporary MI files */ 
    ArrayList<Path> targetPaths = new ArrayList<Path>();
    ArrayList<Path> miPaths = new ArrayList<Path>();
    {
      FileSeq fseq = agenda.getPrimaryTarget();
      String suffix = fseq.getFilePattern().getSuffix();

      ArrayList<String> exts = new ArrayList<String>();
      exts.add("bmp");
      exts.add("gif");
      exts.add("jpg");
      exts.add("rgb");
      exts.add("rla");
      exts.add("sgi");
      exts.add("tga");
      exts.add("tif");
      exts.add("iff");
      exts.add("eps");
      exts.add("exr");
      exts.add("hdr");
      
      if(!exts.contains(suffix))
        throw new PipelineException
          ("The " + getName() + " Action does not support target images with a suffix " + 
           "of (" + suffix + ").");

      if(!fseq.hasFrameNumbers())
        throw new PipelineException
          ("The " + getName() + " Action requires that the output images have frame " + 
           "numbers.");

      targetPaths.addAll(fseq.getPaths());

      {
        FilePattern fpat = fseq.getFilePattern();
        FilePattern mpat = new FilePattern(fpat.getPrefix(), fpat.getPadding(), "mi");
        FrameRange range = fseq.getFrameRange();
        
        FileSeq miSeq = new FileSeq(getTempPath(agenda).toString(), new FileSeq(mpat, range));
        miPaths.addAll(miSeq.getPaths);
      }
    }


    /* create temporary MI files ready to render */  
    {
      int idx = 0;
      for(Path targetPath : targetPaths) {
        Path miPath = miPaths.get(idx);

        try {
          FileWriter out = new FileWriter(miPath.toFile());

          /* load the common shaders */ 
          out.write
            ("link \"base.so\"\n" +
             "$include \"base.mi\"\n" +
             "link \"physics.so\"\n" +
             "$include \"physics.mi\"\n" +
             "link \"contour.so\"\n" +
             "$include \"contour.mi\"\n" +
             "link \"subsurface.so\"\n" +
             "$include \"subsurface.mi\"\n" +
             "link \"paint.so\"\n" +
             "$include \"paint.mi\"\n\n");
          
          




















          


        } 
        catch(IOException ex) {
          throw new PipelineException
            ("Unable to write the temporary MI file (" + miPath + ") for Job " + 
             "(" + agenda.getJobID() + ")!\n" +
             ex.getMessage());
        }

        idx++;
      }
    }

    /* create a temporary Python script file to run the per-frame MentalRay renders */
    File script = createTemp(agenda, "py"); 
    try {
      FileWriter out = new FileWriter(script);
      
      /* include the "launch" method definition */ 
      out.write(getPythonLaunchHeader()); 

      /* construct to common MentalRay command-line arguments */  
      String common = null;
      {
        StringBuilder buf = new StringBuilder();

        String ray = MRayAction.getMRayProgram(agenda);

        buf.append
          ("launch('" + ray + "', " + 
           "['-verbose', '" + getSingleEnumParamIndex(aRenderVerbosity) + "'");
        
        String inc = getSingleStringParamValue(aIncludePath);
        if(inc != null)
          buf.append(", '-I', '" + agenda.evaluate(inc) + "'");

        String lib = getSingleStringParamValue(aLibraryPath);
        if(lib != null)
          buf.append(", '-L', '" + agenda.evaluate(lib) + "'");
        
        String tex = getSingleStringParamValue(aTexturePath);
        if(tex != null)
          buf.append(", '-T', '" + agenda.evaluate(tex) + "'");

        for(String extra : getExtraOptionsArgs()) 
          buf.append(", '" + extra + "'");

        common = buf.toString();
      }

      /* render the MI files and cleanup */ 
      {
        for(Path miPath : miPaths) 
          out.write(common + ", '" + miPath + "'])\n");
        out.write("\n");

        if(getSingleBooleanParamValue(aKeepTempFiles)) {
          for(Path miPath : miPaths) 
            out.write("os.remove('" + miPath + "')\n");
        }
      }

      out.write("\n" + 
                "print 'ALL DONE.'\n");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write the temporary Python script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, script, outFile, errFile); 
  }   


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 

  public static final String aFramebufferType = "FramebufferType";
  public static final String aKeepTempFiles   = "KeepTempFiles";
  public static final String aRenderVerbosity = "RenderVerbosity";
  public static final String aBaseLibraries   = "BaseLibraries"; 
  public static final String aLibraryPath     = "LibraryPath";
  public static final String aIncludePath     = "IncludePath";
  public static final String aTexturePath     = "TexturePath";

}
