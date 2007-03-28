// $Id: MayaMiShaderAction.java,v 1.1 2007/03/28 20:05:06 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

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
 *   Output Format <BR>
 *   <DIV style="margin-left: 40px;">
 *     The format of the output MI file. 
 *   </DIV> <BR>
 * 
 *   Material Namespace <BR>
 *   <DIV style="margin-left: 40px;">
 *     The prefix used to identify the shading groups which will be used by this action 
 *     to export shaders.
 *   </DIV> <BR>
 * 
 *   Shader Namespace <BR>
 *   <DIV style="margin-left: 40px;">
 *     The prefix that will be used to identify the shaders that should be connected to 
 *     the shading groups selected with the Material Namespace.
 *   </DIV> <BR>
 * 
 *   Render Pass Suffix <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the render pass used as the suffix of shader to connect to the shading
 *     groups selected with the Material Namespace.
 *   </DIV> <BR>
 * 
 *   Final Namespace <BR>
 *   <DIV style="margin-left: 40px;">
 *     The prefix that will be included in all the exported shaders.
 *   </DIV> <BR>
 * 
 *   Fix Texture Paths <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to fix the texture paths written by Maya so that they will be compatible 
 *     with the MRayRender Action.
 *   </DIV> <BR>
 * 
 *   <I>Shader Export</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Materal Shader <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether the materal shader should be exported.
 *     </DIV> <BR>
 *   
 *     Displacement Shader <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether the displacement shader should be exported.
 *     </DIV> <BR>
 *   
 *     Shadow Shader <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether the shadow shader should be exported.
 *     </DIV> <BR>
 *   
 *     Volume Shader <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether the volume shader should be exported.
 *     </DIV> <BR>
 *   
 *     Photon Shader <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether the photon shader should be exported.
 *     </DIV> <BR>
 *   
 *     Photon Vol Shader <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether the photon volume shader should be exported.
 *     </DIV> <BR>
 *   
 *     Env Shader <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether the environment shader should be exported.
 *     </DIV> <BR>
 *   
 *     Light Map Shader <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether the light map shader should be exported.
 *     </DIV> <BR>
 *   
 *     Contour Shader <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether the contour shader should be exported.
 *     </DIV> 
 *   </DIV> <BR>
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
  extends MayaAction
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  MayaMiShaderAction()
  {
    super("MayaMiShader", new VersionID("2.2.1"), "Temerity",
	  "Exports shaders for the correct pass based on the name of the source.");

    {
      ActionParam param = 
	new LinkActionParam
	(aMayaScene, 
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
	(aOutputFormat, 
	 "The format of the output MI file.",
	 "ASCII", choices);
      addSingleParam(param);
    } 
    
    {
      ActionParam param = 
	new StringActionParam
	(aMaterialNamespace, 
	 "The prefix used to identify the shading groups which will be used by this " + 
         "action to export shaders.", 
	 "final"); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aShaderNamespace,
	 "The prefix that will be used to identify the shaders that should be connected " + 
         "to the shading groups selected with the Material Namespace.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new StringActionParam
	(aRenderPassSuffix,
	 "The name of the render pass used as the suffix of shader to connect to the " + 
         "shading groups selected with the Material Namespace.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new StringActionParam
	(aFinalNamespace,
	 "The prefix that will be included in all the exported shaders.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new BooleanActionParam
        (aFixTexturePaths, 
         "Whether to fix the texture paths written by Maya so that they will be " + 
         "compatible with the MRayRender Action.", 
         true); 
      addSingleParam(param);
    }

    /* shader export */ 
    {
      {
        ActionParam param = 
          new BooleanActionParam
          (aMaterialShader,
           "Whether the material shader should be exported.", 
           true); 
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aDisplacementShader,
           "Whether the displacement shader should be exported.", 
           true); 
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aShadowShader,
           "Whether the shadow shader should be exported.", 
           true); 
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aVolumeShader,
           "Whether the volume shader should be exported.", 
           true); 
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aPhotonShader,
           "Whether the photon shader should be exported.", 
           true); 
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aPhotonVolShader,
           "Whether the photon volume shader should be exported.", 
           true); 
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aEnvShader,
           "Whether the environment shader should be exported.", 
           true); 
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aLightMapShader,
           "Whether the light map shader should be exported.", 
           true); 
        addSingleParam(param);
      }

      {
        ActionParam param = 
          new BooleanActionParam
          (aContourShader,
           "Whether the contour shader should be exported.", 
           true); 
        addSingleParam(param);
      }
    }

    /* MEL scripts */
    {
      {
	ActionParam param = 
	  new LinkActionParam
	  (aPreExportMEL,
           "The MEL script to evaluate before exporting MI files.", 
	   null);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new LinkActionParam
	  (aPostExportMEL,
           "The MEL script to evaluate after exporting MI files.", 
	   null);
	addSingleParam(param);
      }
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addEntry(aOutputFormat);
      layout.addSeparator();      
      layout.addEntry(aMaterialNamespace);
      layout.addEntry(aShaderNamespace);
      layout.addEntry(aRenderPassSuffix);
      layout.addEntry(aFinalNamespace);
      layout.addSeparator();      
      layout.addEntry(aFixTexturePaths); 

      {
	LayoutGroup group = 
          new LayoutGroup
          ("Shader Export", 
           "Parameters for controlling which types of shaders should be exported.",
           true);

	group.addEntry(aMaterialShader);
	group.addEntry(aDisplacementShader);
	group.addEntry(aShadowShader);
	group.addEntry(aVolumeShader);
	group.addEntry(aPhotonShader);
	group.addEntry(aPhotonVolShader);
	group.addEntry(aEnvShader);
	group.addEntry(aLightMapShader);
	group.addEntry(aContourShader);

	layout.addSubGroup(group);
      }

      {
        LayoutGroup group = 
          new LayoutGroup
          ("MEL Scripts",
           "MEL scripts run at various stages of the exporting process.", 
           true);
        
        group.addEntry(aPreExportMEL);
        group.addEntry(aPostExportMEL);
        
        layout.addSubGroup(group);
      }

      setSingleLayout(layout);
    }
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);     

    underDevelopment();
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
    /* the source Maya scene */ 
    Path sourceScene = getMayaSceneSourcePath(aMayaScene, agenda);
    
    /* MEL script paths */ 
    Path preExportMEL  = getMelScriptSourcePath(aPreExportMEL, agenda);
    Path postExportMEL = getMelScriptSourcePath(aPostExportMEL, agenda);

    /* the MI files to export */ 
    FileSeq targetSeq = null;
    {
      FileSeq fseq = agenda.getPrimaryTarget();
      String suffix = fseq.getFilePattern().getSuffix();
      if((suffix == null) || !suffix.equals("mi"))	   
        throw new PipelineException
          ("The primary file sequence (" + fseq + ") must contain one or more Mental " + 
           "Ray Input (.mi) files!");

      targetSeq = fseq;
    }

    /* shader naming constants */
    String materialNs = getSingleStringParamValue(aMaterialNamespace);
    if(materialNs == null)
      materialNs = "";

    String shaderNs = getSingleStringParamValue(aShaderNamespace);
    if(shaderNs == null)
      shaderNs = "";

    String passSuffix = getSingleStringParamValue(aRenderPassSuffix);
    if(passSuffix == null)
       passSuffix = "";

    String finalNs = getSingleStringParamValue(aFinalNamespace); 
    if(finalNs == null)
      finalNs = "";


    /* create a temporary MEL script to export the MI files */ 
    File exportMEL = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(exportMEL); 

      out.write
        ("if(!`pluginInfo -q -l \"Mayatomr\"`)\n" + 
         "  loadPlugin \"Mayatomr\";\n" + 
         "\n" + 
         "miCreateDefaultNodes();\n" + 
         "miCreateOtherOptionsNodesForURG();\n\n");
      
      if(preExportMEL != null) 
	out.write("// PRE-EXPORT SCRIPT\n" + 
                  "print \"Pre-Export Script: " + preExportMEL + "\\n\";\n" +
                  "source \"" + preExportMEL + "\";\n\n");

      /* set the Maya and Mental Ray render globals */ 
      {
        out.write("// RENDER GLOBALS\n");

        if(targetSeq.hasFrameNumbers()) {
          /* you need this to actually force maya to export a sequence if the globals in the 
             scene are still set to a single frame which happens to the be default in Maya */
          out.write("setAttr \"defaultRenderGlobals.animation\" true;\n");
          
          /* ensure that the naming formats are all correct */ 
          out.write("setAttr \"defaultRenderGlobals.outFormatControl\" false;\n" +
                    "setAttr \"defaultRenderGlobals.periodInExt\" true;\n" +
                    "setAttr \"defaultRenderGlobals.putFrameBeforeExt\" true;\n");
          
          /* set the output frame range to match the target MI file sequence */ 
          FrameRange range = targetSeq.getFrameRange();
          out.write
            ("setAttr \"defaultRenderGlobals.startFrame\" " + range.getStart() + ";\n" + 
             "setAttr \"defaultRenderGlobals.endFrame\" " + range.getEnd() + ";\n" + 
             "setAttr \"defaultRenderGlobals.byFrameStep\" " + range.getBy() + ";\n");
        }

        out.write
          ("setAttr \"mentalrayGlobals.exportExactHierarchy\" false;\n" +
           "setAttr \"mentalrayGlobals.exportFullDagpath\" false;\n" +
           "setAttr \"mentalrayGlobals.exportTexturesFirst\" false;\n" +
           "setAttr \"mentalrayGlobals.exportPostEffects\" false;\n" +
           "setAttr \"defaultRenderGlobals.enableDefaultLight\" false;\n\n");
      }

      /* connect the shaders for the current render pass up to the material shading groups */ 
      {
        out.write
          ("// REWIRE THE SHADERS\n" +
           "{\n" +
           "  string $materialNs = \"" + materialNs + "\";\n" +
           "  string $finalNs = \"" + finalNs + "\";\n" +
           "  string $shaderNs = \"" + shaderNs + "\";\n\n" +

           "  if(!`namespace -exists $finalNs`)\n" +
           "    namespace -add $finalNs;\n\n" +

           "  if($shaderNs != \"\")\n" +
           "    $shaderNs += \":\";\n\n" +

           "  string $passSuffix = \"" + passSuffix + "\";\n\n" +

           "  string $shadingGroups[] = \n" + 
           "    `ls -type \"shadingEngine\" ($materialNs + \":*\")`;\n\n" +
           
           "  string $sg;\n" +
           "  string $newMats[];\n" +

           "  for($sg in $shadingGroups) {\n" +
           "    string $buffer[];\n" +
           "    tokenize($sg, \":\", $buffer);\n" +
           "    string $matBase = $buffer[(size($buffer)-1)];\n\n" +
           
           "    string $matName;\n" +
           "    if($materialNs == $finalNs) {\n" +
           "      string $conns[] = `listConnections -p true -s true -d false " + 
                                      "($sg + \".miMaterialShader\")`;\n" +
           "      catch(`disconnectAttr $conns[0] ($sg + \".miMaterialShader\")`);\n\n" +
           
           "      $conns = `listConnections -p true -s true -d false " +
                             "($sg + \".miDisplacementShader\")`;\n" +
           "      catch(`disconnectAttr $conns[0] ($sg + \".miDisplacementShader\")`);\n\n" +
           
           "      $conns = `listConnections -p true -s true -d false " +
                             "($sg + \".miShadowShader\")`;\n" +
           "      catch(`disconnectAttr $conns[0] ($sg + \".miShadowShader\")`);\n\n" +
           
           "      $conns = `listConnections -p true -s true -d false " +
                             "($sg + \".miVolumeShader\")`;\n" +
           "      catch(`disconnectAttr $conns[0] ($sg + \".miVolumeShader\")`);\n\n" +

           "      $conns = `listConnections -p true -s true -d false " +
                             "($sg + \".miPhotonShader\")`;\n" +
           "      catch(`disconnectAttr $conns[0] ($sg + \".miPhotonShader\")`);\n\n" +
           
           "      $conns = `listConnections -p true -s true -d false " +
                             "($sg + \".miPhotonVolumeShader\")`;\n" +
           "      catch(`disconnectAttr $conns[0] ($sg + \".miPhotonVolumeShader\")`);\n\n" +
           
           "      $conns = `listConnections -p true -s true -d false " +
                             "($sg + \".miEnvironmentShader\")`;\n" +
           "      catch(`disconnectAttr $conns[0] ($sg + \".miEnvironmentShader\")`);\n\n" +
           
           "      $conns = `listConnections -p true -s true -d false " +
                             "($sg + \".miLightMapShader\")`;\n" +
           "      catch(`disconnectAttr $conns[0] ($sg + \".miLightMapShader\")`);\n\n" +
           
           "      $conns = `listConnections -p true -s true -d false " +
                             "($sg + \".miContourShader\")`;\n" +
           "      catch(`disconnectAttr $conns[0] ($sg + \".miContourShader\")`);\n\n" +

           "      $matName = $sg;\n" +
           "    }\n" + 
           "    else {\n" +
           "      if($finalNs != \"\") {\n" +
           "        namespace -set $finalNs;\n" +
           "        $matName = $finalNs + \":\" + $matBase;\n" +
           "      }\n" + 
           "      else {\n" +
           "        $matName = $matBase;\n" +
           "      }\n\n" +
           
           "      string $newMat = `createNode -name $matBase shadingEngine`;\n\n" +

           "      if($newMat != $matName)\n" +
           "        error(\"Cannot create the temporary shading engine.  There is " + 
           "probably a misnamed shading group or shader.  The problem is that " + 
           "\" + $newMat + \" does not match \" + $matName);\n\n" +

           "      $newMats[size($newMats)] = $newMat;\n" +
           "      if($finalNs != \"\")\n" +
           "        namespace -set \":\";\n" +
           "    }\n\n" +

           "    string $shadeName = $shaderNs + $matBase + \"_\" + $passSuffix;\n" +
           "    print(\"SHADER: \" + $shadeName + \"\\n\");\n\n" + 

           "    setAttr ($matName + \".miExportMrMaterial\") 1;\n" + 
           "    setAttr ($matName + \".miExportShadingEngine\") 0;\n\n");
      
	if(getSingleBooleanParamValue(aMaterialShader)) 
	  out.write
            ("    if(`objExists $shadeName`) {\n" + 
             "      connectAttr -f ($shadeName + \".message\")\n" + 
             "                     ($matName + \".miMaterialShader\");\n" +
             "    }\n\n"); 
	
	if(getSingleBooleanParamValue(aDisplacementShader)) 
	  out.write
            ("    string $dispName = $shaderNs + $matBase + \"_disp\";\n" + 
             "    if(`objExists $dispName`) {\n" +
             "      connectAttr -f ($dispName + \".message\")\n" +
             "                     ($matName + \".miDisplacementShader\");\n" +
             "    }\n\n");
	
	if(getSingleBooleanParamValue(aShadowShader))
	  out.write
            ("    string $shadowName = $shadeName + \"_shad\";\n" +
             "    if(`objExists $shadowName`) {\n" +
             "      connectAttr -f ($shadowName + \".message\")\n" +
             "                     ($matName + \".miShadowShader\");\n" +
             "    }\n\n");

	if(getSingleBooleanParamValue(aVolumeShader)) 
	  out.write
            ("    string $volumeName = $shadeName + \"_vol\";\n" +
             "    if(`objExists $volumeName`) {\n" +
             "      connectAttr -f ($volumeName + \".message\")\n" +
             "                     ($matName + \".miVolumeShader\");\n" +
             "    }\n\n");

	if(getSingleBooleanParamValue(aPhotonShader)) 
	  out.write
            ("    string $photonName = $shadeName + \"_pho\";\n" +
             "    if(`objExists $photonName`) {\n" +
             "      connectAttr -f ($photonName + \".message\")\n" +
             "                     ($matName + \".miPhotonShader\");\n" +
             "    }\n\n");

	if(getSingleBooleanParamValue(aPhotonVolShader)) 
	  out.write
            ("    string $photonVolName = $shadeName + \"_phov\";\n" +
             "    if(`objExists $photonVolName`) {\n" +
             "      connectAttr -f ($photonVolName + \".message\")\n" +
             "                     ($matName + \".miPhotonVolumeShader\");\n" +
             "    }\n\n");

	if(getSingleBooleanParamValue(aEnvShader)) 
	  out.write
            ("    string $envName = $shadeName + \"_env\";\n" +
             "    if(`objExists $envName`) {\n" +
             "      connectAttr -f ($envName + \".message\")\n" +
             "                     ($matName + \".miEnvironmentShader\");\n" +
             "    }\n\n");

	if(getSingleBooleanParamValue(aLightMapShader)) 
	  out.write
            ("    string $lightMapName = $shadeName + \"_lmap\";\n" +
             "    if(`objExists $lightMapName`) {\n" +
             "      connectAttr -f ($lightMapName + \".message\")\n" +
             "                     ($matName + \".miLightMapShader\");\n" +
             "    }\n\n");

	if(getSingleBooleanParamValue(aContourShader)) 
	  out.write
            ("    string $contourName = $shadeName + \"_con\";\n" +
             "    if(`objExists $contourName`) {\n" +
             "      connectAttr -f ($contourName + \".message\")\n" +
             "                     ($matName + \".miContourShader\");\n" +
             "    }\n\n");

	out.write
          ("  }\n\n" +
           "  // EXPORT SELECTION\n" +
           "  select -r -ne $newMats;\n" + 
           "}\n\n");
      }
      
      /* export the MI files */ 
      {
        out.write
          ("// EXPORT MI FILES\n" + 
           "string $command = \"Mayatomr -miStream -tabstop 2 ");
      
        FilePattern fpat = targetSeq.getFilePattern();
        if(targetSeq.hasFrameNumbers()) 
          out.write("-perframe 2 -padframe " + fpat.getPadding() + " ");
      
        out.write
          ("-active -fragmentExport -fragmentChildDag -fragmentMaterials " +
           "-fragmentIncomingShdrs -exportFilter 6291191 -xp \\\"3323333333\\\" "); 
       
        Path tpath = new Path(getTempPath(agenda), 
                              fpat.getPrefix() + "." + fpat.getSuffix());   
        out.write
          ("-file \\\"" + tpath + "\\\"\";\n" +
           "evalEcho($command);\n\n");
      }

      if(postExportMEL != null)
	out.write("// POST-EXPORT SCRIPT\n" + 
                  "print \"Post-Export Script: " + postExportMEL + "\\n\";\n" +
                  "source \"" + postExportMEL + "\";\n");

      out.close();      
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MEL script file (" + exportMEL + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create a temporary Python script file to export MI files from Maya and 
         post-process the generated files */ 
    File script = createTemp(agenda, "py"); 
    try {
      FileWriter out = new FileWriter(script);

      /* include the "launch" method definition */ 
      out.write(getPythonLaunchHeader()); 
      
      /* export the MI files from Maya */ 
      out.write(createMayaPythonLauncher(sourceScene, exportMEL) + "\n");

      /* define the texture path fixing Python function */ 
      out.write
        ("prefixes = ['/base/prod/working/jim/default/', '$WORKING/', " + 
                     "'renderData/mentalray/']\n\n" +
         "locals = ['" + PackageInfo.getTempPath(OsType.Unix) + "', " + 
                   "'" + PackageInfo.getTempPath(OsType.MacOS) + "', " + 
                   "'" + PackageInfo.getTempPath(OsType.Windows) + "']\n\n" + 
         "def fixTexturePaths(spath, tpath):\n" + 
         "  print\n" + 
         "  print ('Fixing Texture Paths in: ' + tpath)\n" + 
         "  source = open(spath, 'rU')\n" + 
         "  target = open(tpath, 'w')\n" + 
         "  try:\n" + 
         "    for line in source:\n" + 
         "      noop = True\n" + 
         "      if line.startswith('color texture'):\n" + 
         "        parts = line.split()\n" + 
         "        texture = parts[3].strip('\"')\n" + 
         "        for prefix in prefixes:\n" + 
         "          if texture.startswith(prefix):\n" + 
         "            fixed = texture[len(prefix):len(texture)]\n" + 
         "            print ('Renamed \"' + texture + '\"\\n  To \"' + fixed + '\"')\n" + 
         "            target.write('color texture ' + parts[2] + ' \"' + fixed + '\"\\n')\n" +
         "            noop = False\n" + 
         "            break\n" + 
         "        if noop:\n" + 
         "          isLocal = False\n" + 
         "          for prefix in locals:\n" + 
         "            if texture.startswith(prefix):\n" + 
         "              isLocal = True\n" + 
         "              break\n" + 
         "          if not isLocal:\n" + 
         "            raise SystemExit, ('ERROR: The file texture path \"' + texture + '\" " +
                                         "was not valid!')\n" + 
         "      if noop:\n" + 
         "        target.write(line)\n" + 
         "  finally:\n" + 
         "    source.close()\n" + 
         "    target.close()\n\n");

      /* fix each MI file */ 
      for(Path path : targetSeq.getPaths()) {
        Path spath = new Path(getTempPath(agenda), path); 
        Path tpath = new Path(agenda.getTargetPath(), path); 

        out.write("fixTexturePaths('" + spath + "', '" + tpath + "')\n\n");
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

  private static final long serialVersionUID = -7595539365398372233L;

  public static final String aMayaScene          = "MayaScene";
  public static final String aOutputFormat       = "OutputFormat";
  public static final String aMaterialNamespace  = "MaterialNamespace";
  public static final String aShaderNamespace    = "ShaderNamespace";
  public static final String aRenderPassSuffix   = "RenderPassSuffix";
  public static final String aFinalNamespace     = "FinalNamespace";
  public static final String aFixTexturePaths    = "FixTexturePaths"; 
  public static final String aMaterialShader     = "MaterialShader";
  public static final String aDisplacementShader = "DisplacementShader";
  public static final String aShadowShader       = "ShadowShader";
  public static final String aVolumeShader       = "VolumeShader";
  public static final String aPhotonShader       = "PhotonShader";
  public static final String aPhotonVolShader    = "PhotonVolShader";
  public static final String aEnvShader          = "EnvShader";
  public static final String aLightMapShader     = "LightMapShader";
  public static final String aContourShader      = "ContourShader";
  public static final String aPostExportMEL      = "PostExportMEL";
  public static final String aPreExportMEL       = "PreExportMEL";
	
}
