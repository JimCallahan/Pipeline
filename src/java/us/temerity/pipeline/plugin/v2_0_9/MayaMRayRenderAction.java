// $Id: MayaMRayRenderAction.java,v 1.2 2006/10/07 12:55:16 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   M R A Y   R E N D E R   A C T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports MentalRay geometry and other scene data from a Maya scene and renders it. <P> 
 * 
 * (more here...)
 * 
 */ 
public class 
MayaMRayRenderAction
  extends BaseAction
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  MayaMRayRenderAction() 
  {
    super("MayaMRayRender", new VersionID("2.0.9"), "Temerity",
	  "Exports MentalRay geometry and other scene data from a Maya scene renders it.");

    {
      ActionParam param =
	new LinkActionParam("MayaScene", "The source Maya scene node.", null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aPreExportMEL,
	 "A MEL script to be sourced before the mi export.", 
	 null);
      addSingleParam(param);
    }
  
    {
      ActionParam param = 
	new LinkActionParam
	(aPostExportMEL,
	 "A MEL script to be sourced after the mi export.", 
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param =
	new StringActionParam
	(aExtraOptions, 
	 "Additional command-line arguments.", 
	 null);
      addSingleParam(param);
    }
    
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
    FileSeq target = null;
    Path scene = null;
    Path preExport = null;
    Path postExport = null;
    
    {
      FileSeq fseq = agenda.getPrimaryTarget();
      String suffix = fseq.getFilePattern().getSuffix();
      {
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
	
	if ( !exts.contains(suffix) )
	  throw new PipelineException
	    ("The MRayExportRender Action does not support target "
	     + "images with a suffix of (" + suffix + ").");

	if ( !fseq.hasFrameNumbers() )
	  throw new PipelineException
	    ("The MRayExportRender Action requires that the output "
	     + "images have frame numbers.");
      }    
      scene = new Path(PackageInfo.sProdPath, nodeID.getWorkingParent() + "/");
      target = fseq;
    }

    {
      String sname = (String) getSingleParamValue("MayaScene"); 
      if(sname != null) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Maya Scene node (" + sname + ") was not one of the source " + 
	     "nodes!");
	  
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || !(suffix.equals("ma") || suffix.equals("mb"))) 
	  throw new PipelineException
	    ("The MRayExportRender Action requires that the source node specified by the " +
	     "Maya Scene parameter (" + sname + ") must have a single Maya scene file " + 
	     "as its primary file sequence!");

	NodeID snodeID = new NodeID(nodeID, sname);
	scene = new Path(PackageInfo.sProdPath,
			 snodeID.getWorkingParent() + "/" + fseq.getPath(0));
      }
      else {
	throw new PipelineException
	  ("The MRayExportRender Action requires the Maya Scene parameter to be set!");
      }
    }
    
    {
      String sname = (String) getSingleParamValue(aPreExportMEL); 
      if(sname != null) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Pre Export MEL node (" + sname + ") was not one of the " + 
	     "source nodes!");
	  
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	  throw new PipelineException
	    ("The MRayExportRender Action requires that the source node specified by the " +
	     "Pre Export MEL parameter (" + sname + ") must have a single MEL script " + 
	     "as its primary file sequence!");

	NodeID snodeID = new NodeID(nodeID, sname);
	preExport = new Path(PackageInfo.sProdPath,
			     snodeID.getWorkingParent() + "/" + fseq.getPath(0));
      }
    }
    
    {
      String sname = (String) getSingleParamValue(aPostExportMEL); 
      if(sname != null) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Post Export MEL node (" + sname + ") was not one of the " + 
	     "source nodes!");
	  
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	  throw new PipelineException
	    ("The MRayExportRender Action requires that the source node specified by the " +
	     "Post Export MEL parameter (" + sname + ") must have a single MEL script " + 
	     "as its primary file sequence!");

	NodeID snodeID = new NodeID(nodeID, sname);
	postExport = new Path(PackageInfo.sProdPath,
			      snodeID.getWorkingParent() + "/" + fseq.getPath(0));
      }
    }

    File melScript = createTemp(agenda, 0644, "mel");
    try {      
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(melScript)));
      if(preExport != null) 
	out.println("source \"" + preExport + "\";");

      out.println("{");
      out.println("	miCreateImageFormats();");
      out.println("	global string $miImgFormat[];");
      out.println("	global int $miImgExtNum[];");
      out.println("	string $temp = \""+ target.getFilePattern().getSuffix() +"\";");
      out.println("	int $num = 0;");
      out.println("	string $format;");
      out.println("	for ($format in $miImgFormat)");
      out.println("	{");
      out.println("		if ($format == $temp)");
      out.println("			break;");
      out.println("		$num++;");      
      out.println("	}");
      out.println("	setAttr defaultRenderGlobals.imageFormat $miImgExtNum[$num];");
      out.println("	setAttr defaultRenderGlobals.imfkey -type \"string\" $miImgFormat[$num];");
      out.println("}");
      
      FilePattern fpat = target.getFilePattern();
      FrameRange range = target.getFrameRange();
      out.println("setAttr defaultRenderGlobals.an true ;");
      out.println("setAttr defaultRenderGlobals.extensionPadding "+ fpat.getPadding() +" ;");
      out.println("setAttr defaultRenderGlobals.pff 1 ;");
      out.println("setAttr mentalrayGlobals.passAlphaThrough 1;");
      out.println("setAttr -type \"string\" " + "defaultRenderGlobals.ifp \"" + fpat.getPrefix()
		  + "\";");

      out.println("setAttr defaultRenderGlobals.startFrame " + range.getStart() + ";");
      out.println("setAttr defaultRenderGlobals.endFrame " + range.getEnd() + ";");
      out.println("setAttr defaultRenderGlobals.byFrameStep " + range.getBy() + ";");
      
      Path tempPath = getTempPath(agenda);
      tempPath = new Path(tempPath, fpat.getPrefix() + ".mi");
      
      out.println("mentalrayBatchExportProcedure( \"" + tempPath.toOsString()
		  + "\",\" -perframe 2 -padframe 4 -binary -xp \\\"1111333333\\\"\");");
      
      if(postExport != null) 
	out.println("source \"" + postExport + "\";");
      
      out.close();
      
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write the temporary script file (" + melScript.getPath() + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    String mayaCommand = "";
    String program = "maya";
    if(PackageInfo.sOsType == OsType.Windows)
      program = (program + ".exe");
    mayaCommand += program + " ";
    mayaCommand += ("-batch ");
    mayaCommand += ("-script ");
    mayaCommand += ("\"" + melScript.getPath() + "\" ");
    mayaCommand += ("-file ");
    mayaCommand += ("\"" + scene.toOsString() + "\" ");
    
    File miScript = createTemp(agenda, 0755, "mi3");
    {
      try {
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(miScript)));
	out.println("link \"base.so\"");
	out.println("$include \"base.mi\"");
	out.println("link \"physics.so\"");
	out.println("$include \"physics.mi\"");
	out.println("link \"contour.so\"");
	out.println("$include \"contour.mi\"");
	out.println("link \"subsurface.so\"");
	out.println("$include \"subsurface.mi\"");
	out.println("link \"paint.so\"");
	out.println("$include \"paint.mi\"");
	out.close();
      } catch(IOException ex) {
	throw new PipelineException
	  ("Unable to write the temporary script file ("
	   + miScript.getPath() + ") for Job " + "(" + agenda.getJobID() + ")!\n"
	   + ex.getMessage());
      }
    }


    Map<String, String> env = agenda.getEnvironment();
    File bashScript = createTemp(agenda, 0755, "bash");
    
    {
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
      String rootParam = env.get("MI_ROOT");
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

      StringBuffer buf = new StringBuffer();
      for (String arg : args) 
	buf.append(arg + " ");

      String extraOpts = (String) getSingleParamValue(aExtraOptions);
      if (extraOpts != null)
	buf.append(extraOpts + " ");

      
      try {
	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(bashScript)));
	out.println("#!/bin/bash");
	out.println(mayaCommand);
	out.println("");
	out.println("for name in `find " + getTempPath(agenda).toOsString()
		    + " -name \"*.mi\"`");
	out.println("do");
	out.println("     ray " + buf.toString()+ " " + miScript.getPath() + " $name");
	out.println("done");
	
	out.close();

      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to write the temporary script file ("
	   + bashScript.getPath() + ") for Job " + "(" + agenda.getJobID() + ")!\n"
	   + ex.getMessage());
      }
    }

    Map<String, String> nenv = env;
    String midefs = env.get("PIPELINE_MI_SHADER_PATH");
    if(midefs != null) {
      nenv = new TreeMap<String, String>(env);
      Path dpath = new Path(new Path(agenda.getWorkingDir()), midefs);
      nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
    }

    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add(bashScript.getPath());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "bash", args, nenv, agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -6255592986035207881L;


  private static final String aPreExportMEL  = "PreExportMEL";
  private static final String aPostExportMEL = "PostExportMEL";
  private static final String aExtraOptions  = "ExtraOptions";
    
}
