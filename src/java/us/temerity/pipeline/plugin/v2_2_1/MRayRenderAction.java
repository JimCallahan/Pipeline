// $Id: MRayRenderAction.java,v 1.1 2007/03/18 02:43:56 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.GlueException;
import us.temerity.pipeline.glue.io.GlueEncoderImpl;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   R E N D E R   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The Mental Ray renderer.<P>
 * 
 * This action pulls together multiple MI files, parses them for pertinent
 * information, builds temporary render MI files from the parsed information,
 * and then renders images from the temporary render MI files.
 * 
 * This action calls the outside program MiPrepRender, which comes as part of
 * the default pipeline install. MiPrepRender is the program that is actually
 * responsible for doing all the parsing and temporary scene file building. The
 * MRayRender Action merely exports a GLUE file containing all the information
 * that MiPrepRender needs to work and then calls ray for all the resulting
 * files that MiPrepRender generates.
 * 
 * All of the MI file (.mi) dependencies of the target image which set the Order
 * per-source sequence parameter will be processed. The frame range rendered
 * will be limited by frame numbers of the target images. The Action invokes a
 * new instance of 'ray' for each MI file. This is due to how mental ray keeps
 * library information loaded when rendering multiple MIs. Since each MI file
 * may potentially have a different set of library files and includes, a
 * separate invocation of 'ray' is necessary to ensure that each one runs in the
 * correct environment.<P>
 * 
 * See the Mental Ray documentation for details about <B>ray</B>(1).<P>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Verbosity <BR>
 *   <DIV style="margin-left: 40px;">
 *     The verbosity of render progress, warning and error messages. <BR>
 *   </DIV> <BR>
 * 
 *   Command <BR>
 *   <DIV style="margin-left: 40px;"> 
 *     What command do you want this action to run. Currently this only supports one 
 *     command which will combine all the files and run a render. 
 *   </DIV> <BR>
 * 
 *   Camera MI Files<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     This is a Link Parameter used to flag a particular MI file or sequence as being the 
 *     only one that contains cameras that should have render statements created. This is 
 *     useful if you have multiple MI files containing cameras, but only wish to render the 
 *     cameras from one of them. If only one MI file or sequence has cameras in it, then you
 *     do not need to set this parameter. 
 *   </DIV> <BR>
 * 
 *   Framebuffer Type<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     This determines the output buffer type of the images that are rendered by this 
 *     action. This is inserted into the camera in the same incremental override that 
 *     inserts the filename and image type.
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
 *     "$include" statements in the MI scene file.  References to Toolset environmental 
 *     variables ('${varname}') will be evaluated.
 *   </DIV> <BR>
 * 
 *   Library Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A semicolon seperated list of directories that mental ray searches for shader 
 *     libraries containing shader code before the default library paths. References to 
 *     Toolset environmental variables ('${varname}') will be evaluated.
 *   </DIV> <BR>
 * 
 *   Texture Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A semicolon seperated list of directories that mental ray searches for texture files.
 *     References to Toolset environmental variables ('${varname}') will be evaluated.
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV><P> 
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
 */
public class 
MRayRenderAction 
  extends MRayAction
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MRayRenderAction() 
  {
    super("MRayRender", new VersionID("2.2.1"), "Temerity", 
	  "The Mental Ray renderer.");

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
	 "resolve $include statements in the MI scene file.  References to Toolset " + 
         "environmental variables ('${varname}') will be evaluated.",
	 "${MI_ROOT}/include");
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aLibraryPath,
	 "A semicolon seperated list of directories that mental ray searches for shader " +
	 "libraries containing shader code before the default library paths.  References " + 
         "to Toolset environmental variables ('${varname}') will be evaluated.", 
	 "${MI_ROOT}/lib");
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aTexturePath,
	 "A semicolon seperated list of directories that mental ray searches for texture " +
	 "files. References to Toolset environmental variables ('${varname}') will be " +
         "evaluated.", 
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
        values.put(aIncludePath, "${MI_ROOT}/include");
        values.put(aLibraryPath, "${MI_ROOT}/lib"); 
	
        addPresetValues(aBaseLibraries, "Mental Ray", values);
      }
      
      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aIncludePath, "${MAYA_LOCATION}/mentalray/include");
        values.put(aLibraryPath, "${MAYA_LOCATION}/mentalray/lib"); 
	
        addPresetValues(aBaseLibraries, "Maya", values);
      }
    }

    {
      ActionParam param = 
	new StringActionParam
	(aExtraOptions,
	 "Additional command-line arguments.", null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("RenderPrep");

      ActionParam param = 
	new EnumActionParam
	(aCommand,
	 "What command do you want to be run", 
	 "RenderPrep", choices);
      addSingleParam(param);
    }

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
	 "The output data format.",
	 "+rgba", choices);
      addSingleParam(param);      
    }

    {
      ActionParam param = 
	new LinkActionParam
	(aCameraMIFiles,
	 "The MI file containing the camera information. Only needs to be set if you " + 
	 "have cameras in multiple MI's but only wish to use this particular one " +
	 "in render statements.", 
	 null);
      addSingleParam(param);   
    }
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aCommand);
      layout.addEntry(aCameraMIFiles);
      layout.addEntry(aFramebufferType);
      layout.addSeparator();
      layout.addEntry(aRenderVerbosity);
      layout.addSeparator();
      layout.addEntry(aBaseLibraries);
      layout.addEntry(aIncludePath);
      layout.addEntry(aLibraryPath);
      layout.addEntry(aTexturePath);
      layout.addSeparator();
      layout.addEntry(aExtraOptions);

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
	(aOrder,
	 "Process the MI file in this order.", 
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
    NodeID nodeID = agenda.getNodeID();

    /* sanity checks */
    LinkedList<Path> sources = new LinkedList<Path>();
    FileSeq target = null;
    {
      MappedLinkedList<Integer,Path> sourceMIs = new MappedLinkedList<Integer,Path>(); 
      for(String sname : agenda.getSourceNames()) {
	ActionInfo info = agenda.getSourceActionInfo(sname);
	if(info != null) {
	  String actionName = info.getName();
	  if((actionName == null) || !actionName.equals("MRayShaderInclude")) {
	    if(hasSourceParams(sname)) {
	      FileSeq fseq = agenda.getPrimarySource(sname);
	      Integer order = (Integer) getSourceParamValue(sname, aOrder);
	      addSourceMIs(nodeID, sname, fseq, order, sourceMIs);
	    }
	    
	    for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	      FilePattern fpat = fseq.getFilePattern();
	      if(hasSecondarySourceParams(sname, fpat)) {
		Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, aOrder);
		addSourceMIs(nodeID, sname, fseq, order, sourceMIs);
	      }
	    }
	  }
	}
      }
      
      for (LinkedList<Path> mis : sourceMIs.values())
	sources.addAll(mis);
      
      if(sources.isEmpty())
	throw new PipelineException
	  ("No source MI files where specified using the per-source Order parameter!");

      target = agenda.getPrimaryTarget();
    }

    /* create temporary stub MI files to be populated by MiRenderPrep */ 
    ArrayList<String> scenes = new ArrayList<String>();
    for(Path path : target.getPaths()) 
      scenes.add(createTemp(agenda, "mi").getPath());

    /* create the GLUE input file for MiRenderPrep */
    File glueFile = createTemp(agenda, "glue");
    try {
      TreeMap<String, Object> table = new TreeMap<String, Object>();
      BaseAction act = new BaseAction(this);
      table.put("file", scenes);
      table.put("agenda", agenda);
      table.put("action", act);
      
      GlueEncoderImpl encode = new GlueEncoderImpl("MiRenderPrepInput", table);
      FileWriter out = new FileWriter(glueFile);
      out.write(encode.getText());
      out.close();
    } 
    catch(Exception ex) {
      throw new PipelineException
        ("Unable to write the temporary GLUE input file (" + glueFile + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" + 
         ex.getMessage());
    }

    /* create a temporary Python script file to run MiRenderPrep 
         followed by per-frame MentalRay renders */
    File script = createTempScript(agenda); 
    try {
      FileWriter out = new FileWriter(script);
      
      SortedMap<String, String> env = agenda.getEnvironment();
      
      /* include the "launch" method definition */ 
      out.write(getPythonLaunchHeader()); 

      /* parse/prep the MI files with MiRenderPrep */ 
      {
        String classPath = null;
        {
          Path path = new Path(PackageInfo.sInstPath, "lib/api.jar");
          String utilClassPath = env.get("PIPELINE_UTIL_CLASSPATH");
          if(utilClassPath != null) 
            classPath = (utilClassPath + File.pathSeparator + path.toOsString());
          else 
            classPath = path.toOsString();
        }
        
        out.write
          ("launch('java', ['-cp', '" + classPath + "', " + 
           "'us.temerity.pipeline.utils.v2_0_14.MiRenderPrep', 'glueFile'])\n"); 
      }
      
      /* construct to common MentalRay command-line arguments */  
      String common = null;
      {
        StringBuilder buf = new StringBuilder();

        String ray = getMRayProgram(env);
        
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

      for(String scene : scenes) 
        out.write(common + ", '" + scene + "'])\n");

      out.write("\n" + 
                "print 'ALL DONE.'\n");

      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write the temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, script, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A helper method for generating source MI filenames.
   */
  private void 
  addSourceMIs
  (
   NodeID nodeID, 
   String sname, 
   FileSeq fseq, 
   Integer order,
   MappedLinkedList<Integer,Path> sourceMIs 
  )
    throws PipelineException
  {
    String suffix = fseq.getFilePattern().getSuffix();
    if ((suffix == null) || !suffix.equals("mi"))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " +
	 "must have contain MI files!");

    NodeID snodeID = new NodeID(nodeID, sname);
    for (Path path : fseq.getPaths()) 
      sourceMIs.put(order, getWorkingNodeFilePath(snodeID, path));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4429227939230954922L;

  private static final String aRenderVerbosity = "RenderVerbosity";
  private static final String aIncludePath     = "IncludePath";
  private static final String aLibraryPath     = "LibraryPath";
  private static final String aTexturePath     = "TexturePath";
  private static final String aCommand         = "Command";
  private static final String aFramebufferType = "FramebufferType";
  private static final String aCameraMIFiles   = "CameraMIFiles";
  private static final String aBaseLibraries   = "BaseLibraries";
  private static final String aExtraOptions    = "ExtraOptions";
  private static final String aOrder           = "Order"; 

}
