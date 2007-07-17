// $Id: MayaMiShaderAction.java,v 1.6 2007/07/17 23:22:08 jesse Exp $

package us.temerity.pipeline.plugin.MayaMiShaderAction.v2_3_2;

import java.io.*;
import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.MayaActionUtils;

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
  extends MayaActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  MayaMiShaderAction()
  {
    super("MayaMiShader", new VersionID("2.3.2"), "Temerity",
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
    
    {
      ActionParam param = 
        new BooleanActionParam
        (aUseMRLightLinking, 
         "Should all maya shaders be converted to using the mental ray light " +
         "linking conventions.", 
         true); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new BooleanActionParam
        (aDisableLightLinker, 
         "Should Maya light-linking information be exported.  If UseMRLightLinking is" +
         "enabled, this should be disabled, otherwise unpredictable results may occur.", 
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
      
      {
	ActionParam param = 
	  new LinkActionParam
	  (aExportModMEL,
           "A MEL snippet to insert into the process right before exporting happens." +
           "The snippet will have access to the $newMats variable, which is an array of" +
           "all the shading groups that are going to be exported.  Modifications made " +
           "to this array will change which shaders are exported.", 
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
      layout.addEntry(aUseMRLightLinking);
      layout.addEntry(aDisableLightLinker);

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
        group.addEntry(aExportModMEL);
        
        layout.addSubGroup(group);
      }

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
    /* the source Maya scene */ 
    Path sourceScene = getMayaSceneSourcePath(aMayaScene, agenda);
    
    /* MEL script paths */ 
    Path preExportMEL  = getMelScriptSourcePath(aPreExportMEL, agenda);
    Path postExportMEL = getMelScriptSourcePath(aPostExportMEL, agenda);
    String melSnippet = null;
    try {
      melSnippet = getMelSnippet(agenda);
    }
    catch (IOException ex) {
      throw new PipelineException
	("Unable to read the export mod mel file for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

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
    
    boolean disableLightLinker = getSingleBooleanParamValue(aDisableLightLinker);
    boolean useMRLightLinking = getSingleBooleanParamValue(aUseMRLightLinking);


    /* create a temporary MEL script to export the MI files */ 
    Path ipath = new Path(getTempPath(agenda), "shaders.py");
    File exportMEL = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(exportMEL);
      
      out.write
        ("global proc addToArray(string $src[], string $tgt[])\n" + 
         "{\n" + 
         "  string $each;\n" + 
         "  for ($each in $src)\n" + 
         "    $tgt[size($tgt)] = $each;\n" + 
         "}\n" + 
         "\n" + 
         "global proc string[] getAllMRNodes()\n" + 
         "{\n" + 
         "  string $final[];\n" + 
         "  string $lib;\n" + 
         "  for ($lib in `mrFactory -q -all`)\n" + 
         "  {\n" + 
         "    addToArray(`mrFactory -q -s $lib`, $final)  ;\n" + 
         "  }\n" + 
         "  return $final;\n" + 
         "}\n\n" +
         "global proc breakConn(string $name)\n" + 
         "{\n" + 
         "  string $conns[] = `listConnections -p true -s true -d false $name`;\n" + 
         "  catch(`disconnectAttr $conns[0] $name`);\n" + 
         "}\n\n" +
         "global proc findMayaShaders(string $shader, string $found[])\n" + 
         "{\n" + 
         "  string $nodes[] = {\"volumeFog\", \"hairTubeShader\", \"oceanShader\", \"surfaceLuminance\", \"particleCloud\", \"lambert\", \"rampShader\", \"useBackground\", \"phong\", \"phongE\", \"blinn\", \"anisotropic\"}; \n" + 
         "\n" + 
         "  string $nodeType = `nodeType $shader`;\n" + 
         "  if (stringArrayCount($nodeType, $nodes) > 0)\n" + 
         "    $found[size($found)] = $shader;\n" + 
         "  string $conn[] = `listConnections -p false -s true -d false $shader`;\n" + 
         "  $conn = stringArrayRemoveDuplicates($conn);\n" + 
         "  string $con;\n" + 
         "  for ($con in $conn)\n" + 
         "    findMayaShaders($con, $found);\n" + 
         "}\n\n");

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
        
        if (disableLightLinker)
          out.write("setAttr \"mentalrayGlobals.exportLightLinker\" false;\n");
        else
          out.write("setAttr \"mentalrayGlobals.exportLightLinker\" true;\n");
        
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
           "  string $mrNodes[] = getAllMRNodes();\n");
        
        if (useMRLightLinking)
          out.write("  string $mayaShaderList[];\n" +
          	    "  $fid = `fopen \"" + ipath + "\" \"w\"`;\n" + 
          	    "  int $first = 1;\n\n");
        out.write
          ("  for($sg in $shadingGroups) {\n" +
           "    string $buffer[];\n" +
           "    tokenize($sg, \":\", $buffer);\n" +
           "    string $matBase = $buffer[(size($buffer)-1)];\n\n" +
           
           "    string $matName;\n" +
           "    if($materialNs == $finalNs) {\n" +
           "      breakConn($sg + \".miMaterialShader\");\n" + 
           "      breakConn($sg + \".miDisplacementShader\");\n" + 
           "      breakConn($sg + \".miShadowShader\");\n" + 
           "      breakConn($sg + \".miVolumeShader\");\n" + 
           "      breakConn($sg + \".miPhotonShader\");\n" + 
           "      breakConn($sg + \".miPhotonVolumeShader\");\n" + 
           "      breakConn($sg + \".miEnvironmentShader\");\n" + 
           "      breakConn($sg + \".miLightMapShader\");\n" + 
           "      breakConn($sg + \".miContourShader\");\n" + 
           "      breakConn($sg + \".miMaterialShader\");\n" + 
           "      // Maya stuff\n" + 
           "      breakConn($sg + \".surfaceShader\");\n" + 
           "      breakConn($sg + \".displacementShader\");\n" + 
           "      breakConn($sg + \".volumeShader\");\n" +
           "      $matName = $sg;\n" +
           "      $newMats[size($newMats)] = $matName;\n" +
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

           "    setAttr ($matName + \".miExportMrMaterial\") 0;\n");
        if (useMRLightLinking)
          out.write("    clear($mayaShaderList);\n\n");
      
	if(getSingleBooleanParamValue(aMaterialShader)) { 
	  out.write
            ("    if(`objExists $shadeName`) {\n" + 
             "      if (stringArrayCount(`nodeType $shadeName`, $mrNodes)) {\n" + 
             "        connectAttr -f ($shadeName + \".message\")\n" + 
             "                       ($matName + \".miMaterialShader\");\n" +
             "        setAttr ($matName + \".miExportShadingEngine\") 0;\n" +  
             "        print(\"connecting shader: \" + $shadeName + \"\\n\");\n");
	  if (useMRLightLinking)
             out.write
              ("        findMayaShaders($shadeName, $mayaShaderList);\n");
	  out.write
            ("      }\n");
	  out.write
	    ("      else { //This is a Maya Shader\n" + 
	     "        connectAttr -f ($shadeName + \".message\")\n" + 
	     "                       ($matName + \".surfaceShader\");\n" +
	     "        setAttr ($matName + \".miExportShadingEngine\") 1;\n" +
	     "        print(\"connecting shader: \" + $shadeName + \"\\n\");\n");
	  if (useMRLightLinking)
            out.write
               ("        findMayaShaders($shadeName, $mayaShaderList);\n" + 
	        "        $mayaShaderList[size($mayaShaderList)] = ($shadeName + \":shadow\");\n"); 
	  out.write
	    ("      }\n");
          out.write
            ("    }\n\n");
	}
	
	if(getSingleBooleanParamValue(aDisplacementShader)) {
	  out.write
            ("    string $dispName = $shaderNs + $matBase + \"_disp\";\n" + 
             "    if(`objExists $dispName`) {\n" +
             "      connectAttr -f ($dispName + \".message\")\n" +
             "                     ($matName + \".miDisplacementShader\");\n" +
             "    print(\"connecting shader: \" + $dispName + \"\\n\");\n");
	  if (useMRLightLinking)
	    out.write
	      ("      findMayaShaders($dispName, $mayaShaderList);\n");
          out.write("    }\n\n");
	}
	
	if(getSingleBooleanParamValue(aShadowShader)) {
	  out.write
            ("    string $shadowName = $shadeName + \"_shad\";\n" +
             "    if(`objExists $shadowName`) {\n" +
             "      connectAttr -f ($shadowName + \".message\")\n" +
             "                     ($matName + \".miShadowShader\");\n");
	  if (useMRLightLinking)
	    out.write
	      ("      findMayaShaders($shadowName, $mayaShaderList);\n");
          out.write("    }\n\n");
	}

	if(getSingleBooleanParamValue(aVolumeShader)) { 
	  out.write
            ("    string $volumeName = $shadeName + \"_vol\";\n" +
             "    if(`objExists $volumeName`) {\n" +
             "      connectAttr -f ($volumeName + \".message\")\n" +
             "                     ($matName + \".miVolumeShader\");\n");
	  if (useMRLightLinking)
	    out.write
	      ("      findMayaShaders($volumeName, $mayaShaderList);\n");
          out.write("    }\n\n");
	}

	if(getSingleBooleanParamValue(aPhotonShader)) { 
	  out.write
            ("    string $photonName = $shadeName + \"_pho\";\n" +
             "    if(`objExists $photonName`) {\n" +
             "      connectAttr -f ($photonName + \".message\")\n" +
             "                     ($matName + \".miPhotonShader\");\n");
	  if (useMRLightLinking)
	    out.write
	      ("      findMayaShaders($photonName, $mayaShaderList);\n");
          out.write("    }\n\n");
	}

	if(getSingleBooleanParamValue(aPhotonVolShader)) { 
	  out.write
            ("    string $photonVolName = $shadeName + \"_phov\";\n" +
             "    if(`objExists $photonVolName`) {\n" +
             "      connectAttr -f ($photonVolName + \".message\")\n" +
             "                     ($matName + \".miPhotonVolumeShader\");\n");
	  if (useMRLightLinking)
	    out.write
	      ("      findMayaShaders($photonVolName, $mayaShaderList);\n");
          out.write("    }\n\n");
	}

	if(getSingleBooleanParamValue(aEnvShader)) { 
	  out.write
            ("    string $envName = $shadeName + \"_env\";\n" +
             "    if(`objExists $envName`) {\n" +
             "      connectAttr -f ($envName + \".message\")\n" +
             "                     ($matName + \".miEnvironmentShader\");\n");
	  if (useMRLightLinking)
	    out.write
	      ("      findMayaShaders($envName, $mayaShaderList);\n");
          out.write("    }\n\n");
	}

	if(getSingleBooleanParamValue(aLightMapShader)) { 
	  out.write
            ("    string $lightMapName = $shadeName + \"_lmap\";\n" +
             "    if(`objExists $lightMapName`) {\n" +
             "      connectAttr -f ($lightMapName + \".message\")\n" +
             "                     ($matName + \".miLightMapShader\");\n");
	  if (useMRLightLinking)
	    out.write
	      ("      findMayaShaders($lightMapName, $mayaShaderList);\n");
          out.write("    }\n\n");
	}

	if(getSingleBooleanParamValue(aContourShader)) { 
	  out.write
            ("    string $contourName = $shadeName + \"_con\";\n" +
             "    if(`objExists $contourName`) {\n" +
             "      connectAttr -f ($contourName + \".message\")\n" +
             "                     ($matName + \".miContourSaditi.kapoor@gmail.comhader\");\n");
	  if (useMRLightLinking)
	    out.write
	      ("      findMayaShaders($contourName, $mayaShaderList);\n");
          out.write("    }\n\n");
	}
	
	if (useMRLightLinking)
	  out.write
	    ("    if (size($mayaShaderList) > 0) {\n" +
	     "      print(\"MAYA SHADER LIST:\");\n" +
	     "      print($mayaShaderList);\n" + 
	     "      string $obj;\n" + 
	     "      string $lights[];\n" + 
	     "      select -r $sg;\n" +
	     "      print(\"SHADING GROUP: \" + $sg + \"\\n\");\n" +
	     "      string $list[] = `ls -sl`;\n" +
	     "      print(\"LIST:\");\n" +
	     "      print($list);\n" +
	     "      for ($obj in $list)\n" + 
	     "      {\n" +
	     "        print(\"OBJECTS: \" + $obj + \"\\n\");\n" +
	     "        addToArray(`lightlink -q -o $obj -t true -shp false -sets false`, $lights) ;\n" +
	     "        print(\"   LIGHTS:\\n\");\n" +
	     "        print($lights);\n" +
	     "        print(\"   LIGHTS DONE:\\n\");\n" +
	     "        string $sets[] = `lightlink -q -o $obj -t false -shp false -sets true` ;\n" + 
	     "        string $set;\n" + 
	     "        for ($set in $sets)\n" + 
	     "        {\n" + 
	     "          print(\"SET: \" + $set + \"\\n\");\n" +
	     "          addToArray(`sets -q $set`, $lights);\n" +
	     "          print(\"   LIGHTS:\\n\");\n" +
	     "          print($lights);\n" +
	     "          print(\"   LIGHTS DONE:\\n\");\n" +
	     "        }\n" + 
	     "      }\n" + 
	     "      $lights = stringArrayRemoveDuplicates($lights);\n" +
	     "      print($lights);\n" +
	     "      int $size = size($lights);\n" + 
	     "      int $i;\n" + 
	     "      string $shader;\n" + 
	     "      for ($shader in $mayaShaderList)\n" + 
	     "      {\n" + 
	     "        if ($first) \n" + 
	     "        {\n" + 
	     "          fprint $fid (\"shaders = {\");\n" + 
	     "          $first = 0;\n" + 
	     "        }\n" + 
	     "        else\n" + 
	     "          fprint $fid \", \";\n" + 
	     "        fprint $fid (\"\'\" + $shader + \"\' : [\");\n" + 
	     "        for ($i = 0; $i < $size; $i++)\n" + 
	     "        {\n" + 
	     "          fprint $fid (\"\'\" + $lights[$i] + \"\'\");\n" + 
	     "          if ($i < ($size -1))\n" + 
	     "            fprint $fid \", \";\n" + 
	     "        }\n" + 
	     "        fprint $fid (\"]\");\n" + 
	     "      }\n" + 
	     "    }\n"); 

		  
	out.write
          ("  }\n\n");
	
	if (useMRLightLinking) 
	  out.write
	    ("  if ($first) \n" + 
	     "    {\n" + 
	     "    fprint $fid (\"shaders = {\");\n" + 
	     "    $first = 0;\n" + 
	     "  }\n" + 
	     "  fprint $fid (\"}\\n\");\n" + 
	     "  fclose $fid;");
	
	
	out.write
          ("  // EXPORT SELECTION\n" +
           "  select -r -ne $newMats;\n" +
           "  print(\"SELECTED:\\n\");\n" + 
           "  print($newMats);\n");
	if (useMRLightLinking)
	  out.write("  select -add `ls -type \"light\"`;\n" +
	  	    "  select -add $newMats;\n");
        out.write("}\n\n");
      }
      if (melSnippet != null) {
	out.write(melSnippet);
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
           "-fragmentIncomingShdrs -exportFilter 2089719 -xp \\\"3313333333\\\" "); 
       
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
      out.write("import re\n");
      
      /* export the MI files from Maya */ 
      out.write(createMayaPythonLauncher(sourceScene, exportMEL) + "\n");

      NodeID id = agenda.getNodeID();
      Path workingArea = 
	new Path(new Path(PackageInfo.sWorkPath, id.getAuthor()), id.getView());

      /* define the texture path fixing Python function */ 
      out.write
        ("prefixes = ['" + workingArea + "/', '$WORKING/', " + 
                     "'renderData/mentalray/']\n\n" +
         "locals = ['" + PackageInfo.getTempPath(OsType.Unix) + "', " + 
                   "'" + PackageInfo.getTempPath(OsType.MacOS) + "', " + 
                   "'" + PackageInfo.getTempPath(OsType.Windows) + "']\n" +
         "lightTypes = ['maya_pointlight', 'maya_spotlight', 'maya_ambientlight', " +
                       "'maya_directionallight', 'maya_arealight', 'maya_shapelight', " +
                       "'maya_volumelight', 'mib_light_spot', 'mib_light_point', " +
                       "'mib_light_infinite', 'physical_light', 'mib_blackbody', 'mib_cie_d', " +
                       "'mib_light_photometric']\n" +
         "mayaShaderTypes = ['maya_lambert', 'maya_shadow', 'maya_blinn', " +
                            "'maya_anisotropic', 'maya_fur', 'maya_phong', 'maya_phongE', " +
                            "'maya_rampshader', 'maya_w10fur', 'maya_hairtubeshader', 'maya_oceanshader', " +
                            "'maya_shadow', 'maya_usebackground', 'maya_fluidshader', 'maya_volumefog', " +
                            "'maya_particlecloud', 'maya_surfaceluminance']\n" + 
         "def fixTexturePath(line, target):\n" + 
         "  parts = line.split()\n" + 
         "  length = len(parts)\n" + 
         "  texture = parts[(length - 1)].strip('\\\"')\n" + 
         "  print ('Texture part: ' + texture) \n" + 
         "  for prefix in prefixes: \n" + 
         "    if texture.startswith(prefix): \n" + 
         "      fixed = texture[len(prefix):len(texture)] \n" + 
         "      print ('Renamed \"' + texture + '\"\\n  To \"' + fixed + '\"')\n" + 
         "      target.write(\" \".join(parts[0:(length-1)]) + ' \"' + fixed + '\"\\n')\n" + 
         "      noop = False \n" + 
         "      break \n" + 
         "  if noop: \n" + 
         "    isLocal = False \n" + 
         "    for prefix in locals: \n" + 
         "      if texture.startswith(prefix): \n" + 
         "        isLocal = True \n" + 
         "        break \n" + 
         "    if not isLocal: \n" + 
         "      raise SystemExit, ('ERROR: The file texture path \"' + texture + '\" was not valid!') \n" + 
         "  if noop: \n" + 
         "    target.write(line) \n" +
         "\n\n\n" +
         "def miShader(spath, tpath, ipath):\n" + 
         "  target = open(tpath, 'w')\n" + 
         "  try:\n" + 
         "    source = open(spath, 'rU')\n" +
         "    pat = re.compile('.*color texture.*')\n\n");
      
      if (useMRLightLinking)
	out.write
        ("    info = open(ipath, 'rU')\n" + 
         "    exec info\n" + 
         "    info.close()\n" + 
         "    \n" + 
         "    shadeList = list()\n" + 
         "    shadeShadow = list()\n" + 
         "    shadowLookup = dict()\n" + 
         "    for shader in shaders:\n" + 
         "      shadeList.append(shader)\n" + 
         "      shadow = shader + ':shadow'\n" + 
         "      shadeShadow.append(shadow)\n" + 
         "      shadowLookup[shadow] = shader\n" + 
         "      \n");
      out.write
        ("    mode = 'normal'\n" + 
         "    shaderCache = ''\n" + 
         "    shaderName = ''\n" + 
         "    try:\n" + 
         "      for line in source:\n" + 
         "        clean = line.lstrip()\n" + 
         "        if mode == 'normal':\n" + 
         "          if pat.match(line):\n" + 
         "            fixTexturePath(line, target)\n");
      if (useMRLightLinking)
	out.write
        ("          elif clean.startswith('shader'):\n" + 
         "            mode = 'shader'\n" + 
         "            shaderCache = line\n" + 
         "            parts = line.split()\n" + 
         "            shaderName = parts[1].strip('\"')\n");
      out.write
        ("          else:\n" + 
         "            target.write(line)\n");
      if (useMRLightLinking)
	out.write
        ("        elif mode == 'shader':\n" + 
         "          parts = line.split()\n" + 
         "          shaderType = parts[0].strip('\"')\n" + 
         "          if lightTypes.count(shaderType) > 0:\n" + 
         "            mode = 'skip'\n" + 
         "          elif mayaShaderTypes.count(shaderType) > 0:\n" + 
         "            mode = 'maya'\n" + 
         "            target.write(shaderCache)\n" + 
         "            target.write(line)\n" + 
         "          else:\n" + 
         "            mode = 'normal'\n" + 
         "            target.write(shaderCache)\n" + 
         "            target.write(line)\n" + 
         "        elif mode == 'skip':\n" + 
         "          if clean.startswith(')'):\n" + 
         "            mode = 'normal'\n" + 
         "        elif mode == 'maya':\n" + 
         "          if clean.startswith(')'):\n" + 
         "            target.write(line)\n" + 
         "            mode = 'normal'\n" + 
         "          elif clean.startswith('\"lightMode'):\n" + 
         "            target.write('    \"lightMode\" 0,\\n')\n" + 
         "            mode = 'lights'\n" + 
         "          elif clean.startswith('\"lightLink'):\n" + 
         "            mode = 'maya'\n" + 
         "          else:\n" + 
         "            target.write(line)\n" + 
         "        elif mode == 'lights':\n" + 
         "          if shadeList.count(shaderName) > 0:\n" + 
         "            lights = shaders[shaderName]\n" + 
         "          elif shadeShadow.count(shaderName) > 0:\n" + 
         "            print('Shadow stuff')\n" + 
         "            lights = shaders[shadowLookup[shaderName]]\n" + 
         "          else:\n" + 
         "            lights = list()\n" + 
         "          target.write('    \"lightList\" ' + str(lights).replace('\\'', '\"'))\n" + 
         "          if not clean.startswith(')'):\n" + 
         "            target.write(',')\n" + 
         "          target.write('\\n' + line)\n" + 
         "          mode = 'normal'\n");
      out.write
        ("    finally:\n" + 
         "      source.close()\n" + 
         "  finally:\n" + 
         "    target.close()\n\n\n");

      /* fix each MI file */ 
      for(Path path : targetSeq.getPaths()) {
        Path spath = new Path(getTempPath(agenda), path); 
        Path tpath = new Path(agenda.getTargetPath(), path); 
        out.write("miShader('" + spath + "', '" + tpath + "', '"+ ipath + "')\n\n");
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

  
  
  private String 
  getMelSnippet
  (
    ActionAgenda agenda  
  )
    throws PipelineException, IOException
  {
    String toReturn = null;
    Path melPath = getMelScriptSourcePath(aExportModMEL, agenda);
    if (melPath == null)
      return toReturn;
    File file = melPath.toFile();
    BufferedReader in = new BufferedReader(new FileReader(file));
    while(true) {
      String line = in.readLine();
      if(line == null) 
        break;
      if (toReturn == null)
	toReturn = line + "\n";
      else
	toReturn += line + "\n";
    }
    return toReturn;
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4971189759602455128L;

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
  public static final String aExportModMEL       = "ExportModMEL";
  
  public static final String aDisableLightLinker = "DisableLightLinker";
  public static final String aUseMRLightLinking  = "UseMRLightLinking";
  
	
}
