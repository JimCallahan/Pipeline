// $Id: MRayRenderAction.java,v 1.1 2007/06/19 18:24:49 jim Exp $

package us.temerity.pipeline.plugin.MRayRenderAction.v2_0_10;

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
 *     The level of messages that mental ray will display during the render. 
 *   </DIV> <BR>
 * 
 *   Base Libraries<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     This allows you to load either the base mi libraries stored in MI_ROOT or the 
 *     one stored in the MAYA_LOCATION, or to load no base libraries at all. To make the 
 *     Maya and none options work, an empty rayrc file must be placed in the MI_ROOT 
 *     directory. If it is not, then the MI_ROOT base libraries will always be loaded. 
 *     Simply unsetting MI_ROOT does not work, unless the rayrc files are removed from all 
 *     the search paths that ray(1) uses when it is run. 
 *   </DIV> <BR>
 * 
 *   Include Path<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     A colon seperated list of directories which overrides the path used to resolve 
 *     "$include" statements in the MI scene file.
 *   </DIV> <BR>
 * 
 *   Library Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of directories that mental ray searches for shader 
 *     libraries containing shader code before the default library paths. 
 *   </DIV> <BR>
 * 
 *   Texture Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     A colon seperated list of directories that mental ray searches for texture files.
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
 *   </DIV> <BR>
 * 
 *   Include<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Each source node sequence which sets this to YES should contain a secondary sequence 
 *     that is an MI file containing include and link statements. If this parameter is set 
 *     to YES for a node which does not meet those specifications, an error will be thrown. 
 *     These MI files are the first to be included in the temp render MIs. This is designed 
 *     to work with the output of the MRayShaderInclude Action which creates the correct MI
 *     secondary sequence. Attempting to use this parameter with a node not generated by 
 *     MRayShaderInclude will result in an error.<BR>
 * 
 *     It is not necessary to use this parameter for include and link statements in
 *     a MI file that is a primary file sequence. In that case, simply use a low
 *     Order value to ensure that it gets loaded first. 
 *  </DIV> 
 * </DIV> <P>
 */
public class 
MRayRenderAction 
  extends BaseAction
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MRayRenderAction() 
  {
    super("MRayRender", new VersionID("2.0.10"), "Temerity", 
	  "The Mental Ray renderer.");

    {
      ArrayList<String> verbose = new ArrayList<String>();
      verbose.add("No Messages");
      verbose.add("Fatal Messages Only");
      verbose.add("Error Messages");
      verbose.add("Warning Messages");
      verbose.add("Info Messages");
      verbose.add("Progress Messages");
      verbose.add("Details Messages");

      ActionParam param = 
	new EnumActionParam
	("RenderVerbosity",
	 "The verbosity of render progress, warning and error messages.",
	 "Warning Messages", verbose);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("IncludePath",
	 "A colon seperated list of directories which overrides the path used to " +
	 "resolve $include statements in the MI scene file.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("LibraryPath",
	 "A colon seperated list of directories that mental ray searches for shader " +
	 "libraries containing shader code before the default library paths.",
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("TexturePath",
	 "A colon seperated list of directories that mental ray searches for texture " +
	 "files.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(extraOptsParam,
	 "Additional command-line arguments.", null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("RenderPrep");

      ActionParam param = 
	new EnumActionParam
	(commandParam,
	 "What command do you want to be run", 
	 "RenderPrep", 
	 choices);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("MI_ROOT");
      choices.add("MAYA_LOCATION");
      choices.add("NONE");

      ActionParam param = 
	new EnumActionParam
	(baseLibParam,
	 "Which base libraries do you wish to load for this render", "MI_ROOT",
	 choices);
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
	(bufferParam, 
	 "The output data format.",
	 "+rgba", choices);
      addSingleParam(param);      
    }

    {
      ActionParam param = 
	new LinkActionParam
	(cameraParam,
	 "The MI file containing the camera information. Only needs to be set if you " + 
	 "have cameras in multiple MI's but only wish to use this particular one " +
	 "in render statements.", 
	 null);
      addSingleParam(param);   
    }
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(commandParam);
      layout.addEntry(cameraParam);
      layout.addEntry(bufferParam);
      layout.addSeparator();
      layout.addEntry("RenderVerbosity");
      layout.addSeparator();
      layout.addEntry(baseLibParam);
      layout.addEntry("IncludePath");
      layout.addEntry("LibraryPath");
      layout.addEntry("TexturePath");
      layout.addSeparator();
      layout.addEntry(extraOptsParam);

      setSingleLayout(layout);
    }
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
	 "Process the MI file in this order.", 
	 100);
      params.put(param.getName(), param);
    }
    {
      ActionParam param = 
	new BooleanActionParam
	("Include",
	 "Is this a special Include node.", 
	 false);
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
      TreeMap<Integer, LinkedList<Path>> sourceMIs = new TreeMap<Integer, LinkedList<Path>>();
      for (String sname : agenda.getSourceNames()) {
	ActionInfo info = agenda.getSourceActionInfo(sname);
	if (info != null) {
	  String actionName = info.getName();
	  if ((actionName == null) || !actionName.equals("MRayShaderInclude")) {
	    if (hasSourceParams(sname)) {
	      FileSeq fseq = agenda.getPrimarySource(sname);
	      Integer order = (Integer) getSourceParamValue(sname, "Order");
	      addSourceMIs(nodeID, sname, fseq, order, sourceMIs);
	    }
	    
	    for (FileSeq fseq : agenda.getSecondarySources(sname)) {
	      FilePattern fpat = fseq.getFilePattern();
	      if (hasSecondarySourceParams(sname, fpat)) {
		Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, "Order");
		addSourceMIs(nodeID, sname, fseq, order, sourceMIs);
	      }
	    }
	  }
	}
      }
      
      for (LinkedList<Path> mis : sourceMIs.values())
	sources.addAll(mis);
      
      if (sources.isEmpty())
	throw new PipelineException
	  ("No source MI files where specified using the per-source Order parameter!");

      target = agenda.getPrimaryTarget();
    }

    /* Setup for the parser */
    File glueFile = createTemp(agenda, 0644, "glue");

    ArrayList<String> scenes = null;
    {
      try {
	scenes = new ArrayList<String>();
	for (Path p : target.getPaths()) {
	  File scene = createTemp(agenda, 0666, "mi");
	  scenes.add(scene.getPath());
	}

	TreeMap<String, Object> toGlue = new TreeMap<String, Object>();
	BaseAction act = new BaseAction(this);
	toGlue.put("file", scenes);
	toGlue.put("agenda", agenda);
	toGlue.put("action", act);

	GlueEncoderImpl encode = new GlueEncoderImpl("agenda", toGlue);
	FileWriter out = new FileWriter(glueFile);
	out.write(encode.getText());
	out.close();
      } 
      catch (GlueException e) {
	throw new PipelineException
	  ("Error converting the agenda to Glue for Job " + 
	   "(" + agenda.getJobID() + ")!\n" + e.getMessage());
      } 
      catch (IOException e) {
	throw new PipelineException
	  ("Unable to write the temporary GLUE script file (" + glueFile.getPath() + ") " + 
	   "for Job " + "(" + agenda.getJobID() + ")!\n" +
	   e.getMessage());
      }
    }

    File script = createTemp(agenda, 0666, "bash");

    try {
      FileWriter out = new FileWriter(script);
      
      SortedMap<String, String> env = agenda.getEnvironment();
      
      String pipelineUtilPath = env.get("PIPELINE_UTILS_LIB");
      if (pipelineUtilPath == null)
	throw new PipelineException
	  ("The PIPELINE_UTILS_LIB is not defined in the current environment.  This " + 
	   "makes it impossible to determine the location of the needed jar files to " + 
	   "perform this Action");

      pipelineUtilPath = pipelineUtilPath.replace(" ", "");

      String glueJar = pipelineUtilPath + "/glue.jar";
      String prepJar = pipelineUtilPath + "/miRenderPrep.jar";
      
      String pipelinePath = env.get("PIPELINE_HOME");
      if (pipelineUtilPath == null)
	throw new PipelineException
	  ("The PIPELINE_HOME is not defined in the current environment.  This makes it " +
	   "impossible to determine the location of the needed jar files to perform this " + 
	   "Action");

      pipelineUtilPath = pipelineUtilPath.replace(" ", "");

      String apiJar = pipelinePath + "/lib/api.jar";
      String classPath = apiJar + ":" + glueJar + ":" + prepJar;

      out.write("java -cp " + classPath
		+ " us.temerity.pipeline.utils.MiRenderPrep \"" + glueFile.getPath()
		+ "\"\n");

      ArrayList<String> args = new ArrayList<String>();

      {
	EnumActionParam param = (EnumActionParam) getSingleParam("RenderVerbosity");
	if (param != null) {
	  int level = param.getIndex();
	  if (level == -1)
	    throw new PipelineException("The Render Verbosity was illegal!");
	    
	  args.add("-verbose");
	  args.add(String.valueOf(level));
	}
      }

      EnumActionParam rootEnumParam = (EnumActionParam) getSingleParam(baseLibParam);
      String rootParam = null;
      switch (rootEnumParam.getIndex()) {
      case MI_ROOT_VALUE:
	rootParam = env.get("MI_ROOT");
	break;
      case MAYA_LOCATION_VALUE:
	rootParam = env.get("MAYA_LOCATION") + "/mentalray";
	break;
      }

      {
	String path = (String) getSingleParamValue("IncludePath");
	if ((path != null) && (path.length() == 0))
	  path = null;

	String ipath = null;

	if (rootParam != null) {
	  if (path != null)
	    ipath = (rootParam + "/include:" + path);
	  else
	    ipath = (rootParam + "/include");
	}
	else if (path != null) {
	  ipath = path;
	}

	if (ipath != null) {
	  args.add("-I");
	  args.add(ipath);
	}
      }

      {
	String path = (String) getSingleParamValue("LibraryPath");
	if ((path != null) && (path.length() == 0))
	  path = null;

	String lpath = null;

	if (rootParam != null) {
	  if (path != null)
	    lpath = (rootParam + "/lib:" + path);
	  else
	    lpath = (rootParam + "/lib");
	}
	else if (path != null) {
	  lpath = path;
	}

	if (lpath != null) {
	  args.add("-L");
	  args.add(lpath);
	}
      }

      {
	String path = (String) getSingleParamValue("TexturePath");
	if ((path != null) && (path.length() > 0)) {
	  args.add("-T");
	  args.add(path);
	}
      }

      StringBuilder buf = new StringBuilder();
      for (String arg : args) 
	buf.append(arg + " ");

      String extraOpts = (String) getSingleParamValue(extraOptsParam);
      if (extraOpts != null)
	buf.append(extraOpts + " ");

      for (String scene : scenes)
	out.write("ray " + buf.toString() + " " + scene + "\n");

      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write the temporary script file (" + script.getPath() + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add(script.getPath());
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "bash", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
    } 
    catch (Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }


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
   TreeMap<Integer,LinkedList<Path>> sourceMIs
  )
    throws PipelineException
  {
    String suffix = fseq.getFilePattern().getSuffix();
    if ((suffix == null) || !suffix.equals("mi"))
      throw new PipelineException
	("The file sequence (" + fseq + ") associated with source node (" + sname + ") " +
	 "must have contain MI files!");

    NodeID snodeID = new NodeID(nodeID, sname);
    for (Path path : fseq.getPaths()) {
      Path source = new Path(PackageInfo.sProdPath, 
			     snodeID.getWorkingParent() + "/" + path);

      LinkedList<Path> mis = sourceMIs.get(order);
      if (mis == null) {
	mis = new LinkedList<Path>();
	sourceMIs.put(order, mis);
      }
      
      mis.add(source);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2862127392931902720L;

  private static final String commandParam   = "Command";
  private static final String bufferParam    = "FramebufferType";
  private static final String cameraParam    = "CameraMIFiles";
  private static final String baseLibParam   = "BaseLibraries";
  private static final String extraOptsParam = "ExtraOptions";

  private static final int MI_ROOT_VALUE       = 0;
  private static final int MAYA_LOCATION_VALUE = 1;

}
