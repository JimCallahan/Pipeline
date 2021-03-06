// $Id: MayaMiShaderAction.java,v 1.1 2007/06/19 18:24:50 jim Exp $

package us.temerity.pipeline.plugin.MayaMiShaderAction.v2_0_10;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   M I   S H A D E R   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports shaders for the correct pass based on the name of the source. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Maya scene file. <BR> 
 *   </DIV> <BR>
 * 
 *   Material Namespace <BR>
 *   <DIV style="margin-left: 40px;"> <BR>
 *   </DIV> 
 * 
 *   Output Format <BR>
 *   <DIV style="margin-left: 40px;">
 *     The format of the output MI file. <BR>
 *   </DIV> 
 * 
 *   <I>MEL Scripts</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Pre Export MEL <BR>
 *     <DIV style="margin-left: 40px;">
 *       The source node which contains the MEL script to evaluate before exporting begins. 
 *     </DIV> <BR>
 *   
 *     Post Export MEL <BR>
 *     <DIV style="margin-left: 40px;">
 *       The source node which contains the MEL script to evaluate after exporting ends. <BR>
 *     </DIV> 
 *    </DIV> 
 * </DIV> <P> 
 */
public class 
MayaMiShaderAction 
  extends BaseAction
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  MayaMiShaderAction()
  {
    super("MayaMiShader", new VersionID("2.0.10"), "Temerity",
	  "Exports shaders for the correct pass based on the name of the source.");

    {
      ActionParam param = 
	new LinkActionParam
	(PARAM_scene, 
	 "The source Maya scene node.", 
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("ASCII");
      choices.add("Binary");

      ActionParam param = 
	new EnumActionParam
	(PARAM_format, 
	 "The format of the output MI file.",
	 "ASCII", choices);
      addSingleParam(param);
    } 
    
    {
      ActionParam param = 
	new StringActionParam
	(PARAM_namespace, 
	 "The prefix that will be used to identify the shading groups that have " + 
	 "shaders connected to them for exporting.",
	 "final"); 
      addSingleParam(param);
    }


    /* MEL scripts */
    {
      {
	ActionParam param = 
	  new LinkActionParam
	  (PARAM_preExport,
	   "The pre-export MEL script.", 
	   null);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new LinkActionParam
	  (PARAM_postExport,
	   "The post-export MEL script.", 
	   null);
	addSingleParam(param);
      }
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(PARAM_scene);
      layout.addEntry(PARAM_namespace);
      layout.addEntry(PARAM_format);

      {
	LayoutGroup sub = new LayoutGroup(false);
	sub.addEntry(PARAM_preExport);
	sub.addEntry(PARAM_postExport);
	layout.addSubGroup(sub);
      }

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

    FileSeq target = null;
    Path scene = null;
    Path preExport = null;
    Path postExport = null;
    String passName = null;
    String nameSpace = null;
    {
      FileSeq fseq = agenda.getPrimaryTarget();
      String suffix = fseq.getFilePattern().getSuffix();
      if ((suffix == null) || !suffix.equals("mi"))
	throw new PipelineException
	  ("The primary file sequence (" + fseq + ") must contain one or more Mental " + 
	   "Ray Input (.mi) files!");
      target = fseq;
      passName = nodeID.getParent().getName();
    }

    /* sanity checks */
    {
      TreeSet<String> sources = new TreeSet<String>(agenda.getSourceNames());

      {
	String sname = (String) getSingleParamValue("MayaScene");
	if (sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if (fseq == null)
	    throw new PipelineException
	      ("Somehow the Maya Scene node (" + sname + ") was not one of the source " + 
	       "nodes!");

	  String suffix = fseq.getFilePattern().getSuffix();
	  if (!fseq.isSingle() || (suffix == null) || 
	      !(suffix.equals("ma") || suffix.equals("mb")))
	    throw new PipelineException
	      ("The MayaMiShader Action requires that the source node " + 
	       "specified by the Maya Scene parameter (" + sname + 
	       ") must have a single Maya scene file as its " + 
	       "primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  scene = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent() + "/"
			   + fseq.getPath(0));
	  sources.remove(sname);
	} 
	else {
	  throw new PipelineException
	    ("The MayaMiExport Action requires the Maya Scene parameter to be set!");
	}
      }

      {
	String sname = (String) getSingleParamValue(PARAM_preExport);
	if (sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if (fseq == null)
	    throw new PipelineException
	      ("Somehow the Pre Export MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if (!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel")))
	    throw new PipelineException
	      ("The MayaMiExport Action requires that the source node specified " +
	       "by the Pre Export MEL parameter (" + sname + ") must " +
	       "have a single MEL script as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  preExport = new Path(PackageInfo.sProdPath, 
			       snodeID.getWorkingParent() + "/" + fseq.getPath(0));
	  sources.remove(sname);
	}
      }

      {
	String sname = (String) getSingleParamValue(PARAM_postExport);
	if (sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if (fseq == null)
	    throw new PipelineException
	      ("Somehow the Post Export MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");

	  String suffix = fseq.getFilePattern().getSuffix();
	  if (!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel")))
	    throw new PipelineException
	      ("The MayaMiExport Action requires that the source node specified by the " +
	       "Post Export MEL parameter (" + sname + ") must have a single MEL script " +
	       "as its primary file sequence!");
	  
	  NodeID snodeID = new NodeID(nodeID, sname);
	  postExport = new Path(PackageInfo.sProdPath, 
				snodeID.getWorkingParent() + "/" + fseq.getPath(0));
	  sources.remove(sname);
	}
      }
      
      {
	nameSpace = (String) getSingleParamValue(PARAM_namespace);
	if (nameSpace == null)
	  nameSpace = "";
	else
	  nameSpace += ":";
      }
    } 

    /* end sanity checks. begin insanity */
    File script = createTemp(agenda, 0644, "mel");
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
      
      out.println("if (!`pluginInfo -q -l \"Mayatomr\"`)");
      out.println("{");
      out.println("     loadPlugin \"Mayatomr\";");
      out.println("     miCreateDefaultNodes();");
      out.println("     miCreateOtherOptionsNodesForURG();");
      out.println("}");
      out.println();
      
      if (preExport != null) {
	out.println("source \"" + preExport + "\";");
	out.println();
      }

      FilePattern fpat = target.getFilePattern();
      FrameRange range = target.getFrameRange();

      if (target.hasFrameNumbers()) {
	out.println("// Handling frame numbering and naming");
	out.println("setAttr \"defaultRenderGlobals.animation\" 1;");
	
	// This should ensure the naming formats are all correct. haha.
	out.println("setAttr \"defaultRenderGlobals.outFormatControl\" 0;");
	out.println("setAttr \"defaultRenderGlobals.periodInExt\" 1;");
	out.println("setAttr \"defaultRenderGlobals.putFrameBeforeExt\" 1;");
	
	out.println("setAttr \"defaultRenderGlobals.startFrame\" " + 
		    range.getStart() + ";");
	out.println("setAttr \"defaultRenderGlobals.endFrame\" " + 
		    range.getEnd() + ";");
	out.println("setAttr \"defaultRenderGlobals.byFrameStep\" " + 
		    range.getBy() + ";");
	out.println();
      }

      { // render global flags
	out.println("// Setting the render global flags");
	out.println("setAttr \"mentalrayGlobals.exportExactHierarchy\" 0;");
	out.println("setAttr \"mentalrayGlobals.exportFullDagpath\" 0;");
	out.println("setAttr \"mentalrayGlobals.exportTexturesFirst\" 0;");
	out.println("setAttr \"mentalrayGlobals.exportPostEffects\" 0;");
	out.println("setAttr \"defaultRenderGlobals.enableDefaultLight\" 0;");
	out.println();
      }

      {
	out.println("{");
	out.println("    string $namespace = \"" + nameSpace + "\";");
	out.println("    string $pass = \"" + passName + "\";");
	out.println("    string $shadingGroups[] = "
		    + "`ls -type \"shadingEngine\" ($namespace + \"*\")`;");
	out.println("    string $sg;");
	out.println("    string $newMats[];");
	out.println("    for ($sg in $shadingGroups)");
	out.println("    {");
	out.println("        string $buffer[];");
	out.println("        tokenize($sg, \":\", $buffer);");
	out.println("        string $matName = $buffer[(size($buffer) - 1)];");
	out.println("        string $newMat = `createNode -name $matName shadingEngine`;");
	out.println("        if ($newMat != $matName)");
	out.println("            error( (\"Cannot create the temporary shading engine. " + 
		    "There is probably a misnamed shading group or shader.\\\n " +
		    "The exact problem is that \" + $newMat + \" does not match \" +" +
		    "$matName  ) );");
	out.println("        $newMats[size($newMats)] = $newMat;");
	out.println("");
	out.println("");
	out.println("        string $shadeName = $matName + \"_\" + $pass;");
	out.println("        print($shadeName + \"\\n\");");
	out.println("        setAttr ($matName + \".miExportMrMaterial\") 1;");
	out.println("        setAttr ($matName + \".miExportShadingEngine\") 0;");
	out.println("        connectAttr -f ($shadeName + \".outValue\") " +
		                           "($matName + \".miMaterialShader\");");
	out.println("    }");
	out.println("select -r -ne $newMats;");
	out.println("}");
      }
      
      out.print("string $command = \"Mayatomr -miStream -tabstop 2 ");
      
      if (target.hasFrameNumbers()) 
	out.print("-perframe 2 -padframe " + fpat.getPadding() + " ");
      
      out.print("-active -fragmentExport -fragmentChildDag " +
		"-fragmentMaterials -fragmentIncomingShdrs ");
      
      out.print("-exportFilter 6291191 ");
      out.print("-xp \\\"3323333333\\\" ");
      
      out.println("-file \\\"" + fpat.getPrefix() + "." + fpat.getSuffix() + "\\\"\";");
      out.println("evalEcho($command);");
      out.println();

      if (postExport != null)
	out.println("source \"" + postExport + "\";");

      out.close();
      
    }
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-batch");
      args.add("-script");
      args.add(script.getPath());
      args.add("-file");
      args.add(scene.toOsString());
      
      String program = "maya";
      if (PackageInfo.sOsType == OsType.Windows)
	program = (program + ".exe");
      
      Map<String, String> env = agenda.getEnvironment();
      Map<String, String> nenv = env;
      String midefs = env.get("PIPELINE_MI_SHADER_PATH");
      if (midefs != null) {
	nenv = new TreeMap<String, String>(env);
	Path dpath = new Path(new Path(agenda.getWorkingDir()), midefs);
	nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
      }

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 program, args, nenv, agenda.getWorkingDir(), 
	 outFile, errFile);
    } 
    catch (Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final String PARAM_scene      = "MayaScene";
  private static final String PARAM_format     = "OutputFormat";
  private static final String PARAM_postExport = "PostExportMEL";
  private static final String PARAM_preExport  = "PreExportMEL";
  private static final String PARAM_namespace  = "MaterialNamespace";
	
  private static final long serialVersionUID = 1116177103366360723L;

}
