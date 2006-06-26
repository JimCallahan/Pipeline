// $Id: MayaMiShaderAction.java,v 1.1 2006/06/26 21:18:23 jim Exp $

package us.temerity.pipeline.plugin.v2_0_13;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.GlueException;
import us.temerity.pipeline.glue.io.GlueEncoderImpl;

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
    super("MayaMiShader", new VersionID("2.0.13"), "Temerity",
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
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("TextureFix");
      choices.add("None");
      
      ActionParam param = 
	new EnumActionParam
	(PARAM_command,
	 "The command passed to the post process.  If this is set to TextureFix, " +
	 "it will fix the texture paths to work with MRayRender Action.",
	 "TextureFix", 
	 choices);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(PARAM_namespace, 
	 "The prefix that will be used to identify the shading groups that will have " + 
	 "shaders connected to them for exporting.",
	 "final"); 
      addSingleParam(param);
    }


    {
      ActionParam param = 
	new StringActionParam
	(PARAM_shadespace,
	 "The prefix that will be used to identify the shaders that need to be connected " + 
	 "to the shading groups.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new StringActionParam
	(PARAM_finalspace,
	 "The prefix that will be included in all the exported shaders.  " + 
	 "If this has no value it was use the base namespace.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new BooleanActionParam
	(PARAM_material,
	 "Should the material shader be exported?", 
	 true); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(PARAM_displacement,
	 "Should the displacement shader be exported?", 
	 true); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(PARAM_shadow,
	 "Should the shadow shader be exported?", 
	 true); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(PARAM_volume,
	 "Should the volume shader be exported?", 
	 true); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(PARAM_photon,
	 "Should the photon shader be exported?", 
	 true); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(PARAM_photonV,
	 "Should the photon volume shader be exported?", 
	 true); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(PARAM_environment,
	 "Should the environment shader be exported?", 
	 true); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(PARAM_lightmap,
	 "Should the light map shader be exported?", 
	 true); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(PARAM_contour,
	 "Should the contour shader be exported?", 
	 true); 
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
      layout.addEntry(PARAM_shadespace);
      layout.addEntry(PARAM_finalspace);
      layout.addEntry(PARAM_command);
      layout.addEntry(PARAM_format);

      {
	LayoutGroup sub = new LayoutGroup(true);
	sub.addEntry(PARAM_material);
	sub.addEntry(PARAM_displacement);
	sub.addEntry(PARAM_shadow);
	sub.addEntry(PARAM_volume);
	sub.addEntry(PARAM_photon);
	sub.addEntry(PARAM_photonV);
	sub.addEntry(PARAM_environment);
	sub.addEntry(PARAM_lightmap);
	sub.addEntry(PARAM_contour);
	layout.addSubGroup(sub);
      }

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
    String shadeSpace = null;
    String finalname = null;
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
	    ("The MayaMiShader Action requires the Maya Scene parameter to be set!");
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
	      ("The MayaMiShader Action requires that the source node specified " +
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

	finalname = (String) getSingleParamValue(PARAM_finalspace);
	if (finalname == null)
	  finalname = "";

	shadeSpace = (String) getSingleParamValue(PARAM_shadespace);
	if (shadeSpace == null)
	  shadeSpace = "";
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
	out.println("    string $finalname = \"" + finalname + "\";");
	out.println("    string $shadespace = \"" + shadeSpace + "\";");
	out.println("    if (!`namespace -exists $finalname`)");
	out.println("        namespace -add $finalname;");

	out.println("    string $pass = \"" + passName + "\";");
	out.println("    string $shadingGroups[] = "
		    + "`ls -type \"shadingEngine\" ($namespace + \":*\")`;");
	out.println("    string $sg;");
	out.println("    string $newMats[];");

	out.println("    for ($sg in $shadingGroups)");
	out.println("    {");
	out.println("        string $matName;");
	out.println("        string $shaderSpace;");
	out.println("        string $matBase;");
	
	out.println("        string $buffer[];");
	out.println("        tokenize($sg, \":\", $buffer);");

	out.println("        string $matBase = $buffer[(size($buffer)- 1)];");
	out.println("        if ($namespace == $finalname)");
	out.println("        {");
	out.println("            string $conns[] = "
		    + "`listConnections -p true -s true -d false "
		    + "($sg + \".miMaterialShader\")`;");
	out.println("            catch(`disconnectAttr $conns[0] "
		    + "($sg + \".miMaterialShader\")`);");
	out.println("            $conns = "
		    + "`listConnections -p true -s true -d false "
		    + "($sg + \".miDisplacementShader\")`;");
	out.println("            catch(`disconnectAttr $conns[0] "
		    + "($sg + \".miDisplacementShader\")`);");
	out.println("            $conns = "
		    + "`listConnections -p true -s true -d false "
		    + "($sg + \".miShadowShader\")`;");
	out.println("            catch(`disconnectAttr $conns[0] "
		    + "($sg + \".miShadowShader\")`);");
	out.println("            $conns = "
		    + "`listConnections -p true -s true -d false "
		    + "($sg + \".miVolumeShader\")`;");
	out.println("            catch(`disconnectAttr $conns[0] "
		    + "($sg + \".miVolumeShader\")`);");
	out.println("            $conns = "
		    + "`listConnections -p true -s true -d false "
		    + "($sg + \".miPhotonShader\")`;");
	out.println("            catch(`disconnectAttr $conns[0] "
		    + "($sg + \".miPhotonShader\")`);");
	out.println("            $conns = "
		    + "`listConnections -p true -s true -d false "
		    + "($sg + \".miPhotonVolumeShader\")`;");
	out.println("            catch(`disconnectAttr $conns[0] "
		    + "($sg + \".miPhotonVolumeShader\")`);");
	out.println("            $conns = "
		    + "`listConnections -p true -s true -d false "
		    + "($sg + \".miEnvironmentShader\")`;");
	out.println("            catch(`disconnectAttr $conns[0] "
		    + "($sg + \".miEnvironmentShader\")`);");
	out.println("            $conns = "
		    + "`listConnections -p true -s true -d false "
		    + "($sg + \".miLightMapShader\")`;");
	out.println("            catch(`disconnectAttr $conns[0] "
		    + "($sg + \".miLightMapShader\")`);");
	out.println("            $conns = "
		    + "`listConnections -p true -s true -d false "
		    + "($sg + \".miContourShader\")`;");
	out.println("            catch(`disconnectAttr $conns[0] "
		    + "($sg + \".miContourShader\")`);");
	out.println("            $matName = $sg;");
	out.println("        }else");
	out.println("        {");
	out.println("             if ($finalname != \"\")");
	out.println("             {");
	out.println("                  namespace -set $finalname;");
	out.println("                  $matName = "
		    + "$finalname + \":\" + $matBase;");
	out.println("             } else");
	out.println("             {");
	out.println("                  $matName = $matBase;");
	out.println("             }");
	out.println("             string $newMat = "
		    + "`createNode -name $matBase shadingEngine`;");
	out.println("");
	out.println("            if ($newMat != $matName)");
	out.println("                error( (\"Cannot create the temporary shading "
		    + "engine.  There is probably a misnamed shading group or shader.\\\n "
		    + "The exact problem is that \" + $newMat + \" does not match \" +"
		    + "$matName  ) );");
	out.println("            $newMats[size($newMats)] = $newMat;");
	out.println("            if ($finalname != \"\")");
	out.println("                  namespace -set \":\";");
	out.println("        }");
	out.println("");
	out.println("        if ($shadespace != \"\")");
	out.println("            $shadespace += \":\";");
	out.println("");
	out.println("        string $shadeName = "
		    + "$shadespace + $matBase + \"_\" + $pass;");
	out.println("        print($shadeName + \"\\n\");");
	out.println("        setAttr ($matName + \".miExportMrMaterial\") 1;");
	out.println("        setAttr ($matName + \".miExportShadingEngine\") 0;");

	if ((Boolean) getSingleParamValue(PARAM_material)) {
	  out.println("        if (`objExists $shadeName`)");
	  out.println("        {");
	  out.println("        connectAttr -f ($shadeName + \".outValue\") "
		      + "($matName + \".miMaterialShader\");");
	  out.println("        }");
	}
	
	if ((Boolean) getSingleParamValue(PARAM_displacement)) {
	  out.println("        string $dispName = "
		      + "$shadespace + $matBase + \"_disp\";");
	  out.println("        if (`objExists $dispName`)");
	  out.println("        {");
	  out.println("            connectAttr -f ($dispName + \".outValue\")"
		      + " ($matName + \".miDisplacementShader\");");
	  out.println("        }");
	}
	
	if ((Boolean) getSingleParamValue(PARAM_shadow)) {
	  out.println("        string $shadowName = $shadeName + \"_shad\";");
	  out.println("        if (`objExists $shadowName`)");
	  out.println("        {");
	  out.println("            connectAttr -f ($shadowName + \".outValue\")"
		      + " ($matName + \".miShadowShader\");");
	  out.println("        }");
	}

	if ((Boolean) getSingleParamValue(PARAM_volume)) {
	  out.println("        string $volumeName = $shadeName + \"_vol\";");
	  out.println("        if (`objExists $volumeName`)");
	  out.println("        {");
	  out.println("            connectAttr -f ($volumeName + \".outValue\")"
		      + " ($matName + \".miVolumeShader\");");
	  out.println("        }");
	}

	if ((Boolean) getSingleParamValue(PARAM_photon)) {
	  out.println("        string $photonName = $shadeName + \"_pho\";");
	  out.println("        if (`objExists $photonName`)");
	  out.println("        {");
	  out.println("            connectAttr -f ($photonName + \".outValue\") "
		      + "($matName + \".miPhotonShader\");");
	  out.println("        }");
	}
	
	if ((Boolean) getSingleParamValue(PARAM_photonV)) {
	  out.println("        string $photonVName = $shadeName + \"_phov\";");
	  out.println("        if (`objExists $photonVName`)");
	  out.println("        {");
	  out.println("            connectAttr -f ($photonVName + \".outValue\") "
		      + "($matName + \".miPhotonVolumeShader\");");
	  out.println("        }");
	}

	if ((Boolean) getSingleParamValue(PARAM_environment)) {
	  out.println("        string $envName = $shadeName + \"_env\";");
	  out.println("        if (`objExists $envName`)");
	  out.println("        {");
	  out.println("            connectAttr -f ($envName + \".outValue\") "
		      + "($matName + \".miEnvironmentShader\");");
	  out.println("        }");
	}
	
	if ((Boolean) getSingleParamValue(PARAM_lightmap)) {
	  out.println("        string $lMapName = $shadeName + \"_lmap\";");
	  out.println("        if (`objExists $lMapName`)");
	  out.println("        {");
	  out.println("            connectAttr -f ($lMapName + \".outValue\") "
		      + "($matName + \".miLightMapShader\");");
	  out.println("        }");
	}

	if ((Boolean) getSingleParamValue(PARAM_contour)) {
	  out.println("        string $contourName = $shadeName + \"_con\";");
	  out.println("        if (`objExists $contourName`)");
	  out.println("        {");
	  out.println("            connectAttr -f ($contourName + \".outValue\") "
		      + "($matName + \".miContourShader\");");
	  out.println("        }");
	}

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


    String command = (String) getSingleParamValue(PARAM_command);
    
    Map<String, String> env = agenda.getEnvironment();
    
    String mayaCommand = "";
    String program = "maya";
    if (PackageInfo.sOsType == OsType.Windows)
      program = (program + ".exe");
    mayaCommand += program + " ";
    mayaCommand += ("-batch ");
    mayaCommand += ("-script ");
    mayaCommand += ("\"" + script.getPath() + "\" ");
    mayaCommand += ("-file ");
    mayaCommand += ("\"" + scene.toOsString() + "\" ");
    
    String javaCommand = null;
    String cpCommand = null;
    if (command.equals("TextureFix")) {
      File glueFile = createTemp(agenda, 0644, "glue");
      ArrayList<String> scenes = null;
      {
	try {
	  scenes = new ArrayList<String>();
	  for (Path p : target.getPaths()) {
	    File tempscene = createTemp(agenda, 0666, "mi");
	    scenes.add(tempscene.getPath());
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
	    ("Error converting the agenda to Glue for Job "
	     + "(" + agenda.getJobID() + ")!\n" + e.getMessage());
	} 
	catch (IOException e) {
	  throw new PipelineException
	    ("Unable to write the temporary GLUE script file (" + glueFile.getPath() + ") " + 
	     "for Job " + "(" + agenda.getJobID() + ")!\n" + e.getMessage());
	}
      }
      
      String pipelineUtilPath = env.get("PIPELINE_UTILS_LIB");
      if (pipelineUtilPath == null)
	throw new PipelineException
	  ("The PIPELINE_UTILS_LIB is not defined in the current environment.  " + 
	   "This makes it impossible to determine the location of the needed " +
	   "jar files to perform this Action");
      pipelineUtilPath = pipelineUtilPath.replace(" ", "");

      String prepJar = pipelineUtilPath + "/miRenderPrep.jar";
      
      String pipelinePath = env.get("PIPELINE_HOME");
      if (pipelineUtilPath == null)
	throw new PipelineException
	  ("The PIPELINE_HOME is not defined in the current environment.  This makes it " + 
	   "impossible to determine the location of the needed jar files to perform " + 
	   "this Action");
      pipelineUtilPath = pipelineUtilPath.replace(" ", "");
      
      String apiJar = pipelinePath + "/lib/api.jar";
      String classPath = apiJar + ":" + prepJar;
      
      javaCommand = 
	("java -cp " + classPath + " us.temerity.pipeline.utils.MiRenderPrep " + 
	 "\"" + glueFile.getPath() + "\"");

      cpCommand = "";
      int frame = 0;
      for (String s : scenes) {
	Path sPath = new Path(PackageInfo.sProdPath, 
			      nodeID.getWorkingParent() + "/" + target.getPath(frame));
	String cp = "cp -f " + s + " " + sPath.toOsString() + ";";
	cpCommand += cp;
      }
    }
    
    File bashScript = createTemp(agenda, 0755, "bash");
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(bashScript)));
      out.println(mayaCommand);
      
      if (javaCommand != null) {
	out.println(javaCommand);
	out.println(cpCommand);
      }
      out.close();
      
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write the temporary bash script file (" + bashScript + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n"
	 + ex.getMessage());
    }
    
    Map<String, String> nenv = env;
    String midefs = env.get("PIPELINE_MI_SHADER_PATH");
    if (midefs != null) {
      nenv = new TreeMap<String, String>(env);
      Path dpath = new Path(new Path(agenda.getWorkingDir()), midefs);
      nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
    }

    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add(bashScript.getPath());
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "bash", args, nenv, agenda.getWorkingDir(), outFile,
	 errFile);
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

  private static final String PARAM_scene = "MayaScene";
  private static final String PARAM_format = "OutputFormat";
  private static final String PARAM_postExport = "PostExportMEL";
  private static final String PARAM_preExport = "PreExportMEL";
  private static final String PARAM_namespace = "MaterialNamespace";
  private static final String PARAM_shadespace = "ShaderNamespace";
  private static final String PARAM_finalspace = "FinalNamespace";
  private static final String PARAM_command = "Command";
  private static final String PARAM_material = "MaterialShader";
  private static final String PARAM_displacement = "DisplacementShader";
  private static final String PARAM_shadow = "ShadowShader";
  private static final String PARAM_volume = "VolumeShader";
  private static final String PARAM_photon = "PhotonShader";
  private static final String PARAM_photonV = "PhotonVolShader";
  private static final String PARAM_environment = "EnvShader";
  private static final String PARAM_lightmap = "LightMapShader";
  private static final String PARAM_contour = "ContourShader";
	
  private static final long serialVersionUID = -2148518649519060833L;

}
