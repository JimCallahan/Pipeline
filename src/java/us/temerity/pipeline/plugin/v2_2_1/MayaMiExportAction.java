// $Id: MayaMiExportAction.java,v 1.4 2007/04/04 07:33:30 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*; 

import java.io.*;
import java.util.ArrayList;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   M I   E X P O R T   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports MentalRay entities from a Maya scene. <P> 
 * 
 * See the Maya documentation for the MEL command (Mayatomr) for details.
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Maya scene file. 
 *   </DIV> <BR>
 * 
 *   Output Format <BR>
 *   <DIV style="margin-left: 40px;">
 *     The format of the output MI file. 
 *   </DIV> <BR>
 * 
 *   <I> Render Global Options</I> <BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Export Exact Hierarchy 
 *     <DIV style="margin-left: 40px;"> 
 *       Tries to preserve the DAG hierarchy during processing. This produces additional 
 *       Mental Ray instgroup entities. There are certain unresolved material inheritance 
 *       issues in this mode, but it works well in the general case. Deeply nested DAG 
 *       hierarchies may be translated much faster compared to the standard Maya iterator 
 *       mode that always flattens the DAG. 
 *     </DIV><BR>
 * 
 *     Export Full DAG Path 
 *     <DIV style="margin-left: 40px;"> 
 *       Uses the full DAG path names instead of the shortest possible name for Mental Ray 
 *       scene entities. This is not required to generate a valid scene, but ensures 
 *       reproducible names even if DAG entity names are reused in Maya. On the other hand, 
 *       with deeply nested DAG hierarchy names, you may exceed the maximum supported name
 *       length in Mental Ray. 
 *     </DIV><BR>
 * 
 *     Export Textures First 
 *     <DIV style="margin-left: 40px;"> 
 *       Collects all file texture references in the scene first. This ensures that missing 
 *       texture files are reported early in the process, but may slow down scene processing
 *       depending on the number of file textures being used. It may also write out textures 
 *       references that are never used in the shading graph, because it doesn't perform a 
 *       complete scene graph traversal for performance reasons.
 *     </DIV><BR>
 * 
 *     Export Post Effects 
 *     <DIV style="margin-left: 40px;"> 
 *       Lets you export post effects. This will create lots of maya garbage in your camera 
 *       and shader scenes. Probably best to leave this off unless you specifically need this
 *       functionality.
 *     </DIV><BR>
 * 
 *     Export Assigned Only 
 *     <DIV style="margin-left: 40px;"> 
 *       This option ignores objects without materials during translation so that they are 
 *       not part of the final rendered scene. 
 *     </DIV><BR>
 * 
 *     Export Visible Only 
 *     <DIV style="margin-left: 40px;">
 *       This option ignores non-animated invisible scene entities during translation so 
 *       that they are not part of the final rendered scene. This option is on by default. 
 *     </DIV><BR>
 * 
 *     Optimize Anim Detection 
 *     <DIV style="margin-left: 40px;"> 
 *       When this option is turned on, the processing of non-animated geometry is 
 *       significantly optimized because Mental Ray for Maya detects animated nodes prior to 
 *       processing the scene. This is especially useful for scenes that contain many static 
 *       objects and only a few simply animated objects. 
 *     </DIV><BR>
 * 
 *     Use Default Light 
 *     <DIV style="margin-left: 40px;"> 
 *       Should the default light be turned on. By default this is off. 
 *     </DIV> 
 *   </DIV> <P>
 * 
 *   <I>Fragment Export</I> <BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Export Set 
 *     <DIV style="margin-left: 40px;">
 *       The name of the Maya Set used to select the DAG nodes to export from the Maya
 *       scene. If unset, then the entire scene will be exported. 
 *     </DIV><BR>
 * 
 *     Export Children 
 *     <DIV style="margin-left: 40px;"> 
 *       Whether to additionally export all child DAGs of the nodes selected for export. 
 *     </DIV><BR>
 * 
 *     Export Materials 
 *     <DIV style="margin-left: 40px;"> 
 *       Whether to additionally export all materials associated with the nodes selected 
 *       for export. 
 *     </DIV><BR>
 * 
 *     Export Connections
 *     <DIV style="margin-left: 40px;">
 *       Whether to additionally export shading nodes which are connected to (driving) the 
 *       nodes selected for export.
 *     </DIV>
 *   </DIV><P>
 * 
 *   <I>Exported Entities</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Links <BR>
 *     Includes <BR>
 *     Versions <BR>
 *     Textures <BR>
 *     Objects <BR>
 *     Groups <BR>
 *     Lights <BR>
 *     Cameras <BR>
 *     Materials <BR>
 *     Options <BR>
 *     Functions <BR>
 *     Function Decls <BR>
 *     Phenomena Decls <BR>
 *     User Data <BR>
 *     <BR>
 *     Object Instances <BR>
 *     Light Instances <BR>
 *     Group Instances <BR>
 *     Camera Instances <BR>
 *     Function Instances <BR>
 *     <BR>
 *     Render <BR>
 *     <BR>
 *     Custom Text <BR>
 *     Custom Shaders <BR>
 *     Custom Phenomena <BR>
 *   </DIV><P>
 * 
 *   <I>MEL Scripts</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Pre Export MEL <BR>
 *     <DIV style="margin-left: 40px;">
 *       The MEL script to evaluate before exporting MI files.
 *     </DIV> <BR>
 *   
 *     Post Export MEL <BR>
 *     <DIV style="margin-left: 40px;">
 *       The MEL script to evaluate after exporting MI files.
 *     </DIV> 
 *    </DIV> 
 * </DIV> <P> 
 */
public
class MayaMiExportAction
  extends MayaActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaMiExportAction() 
  {
    super("MayaMiExport", new VersionID("2.2.1"), "Temerity",
	  "Exports MentalRay geometry and other scene data from a Maya scene.");

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

    /* fragment export */ 
    {
      {
	ActionParam param = 
	  new StringActionParam
	  (sExportSet,
	   "The name of the Maya Set used to select the DAG nodes to export from the " +
	   "Maya scene. If unset, then the entire scene will be exported.", 
	   null);
	addSingleParam(param);
      }
    
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportChildren,
	   "Whether to additionally export all child DAGs of the nodes selected for export.",
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportMaterials,
	   "Whether to additionally export all materials associated with the nodes " +
	   "selected for export.",
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportConnections,
	   "Whether to additionally export shading nodes which are connected to (driving) " + 
	   "the nodes selected for export.",
	   true);
	addSingleParam(param);
      }
    }

    /* exported entities */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aLinks, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aIncludes, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aVersions, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aTextures, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aObjects, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aGroups, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aLights, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aCameras, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aMaterials, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aOptions, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aFunctions, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aFunctionDecls, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aPhenomenaDecls, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aUserData, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aObjectInstances, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aLightInstances, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aGroupInstances, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aCameraInstances, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aFunctionInstances, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aRender, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aCustomText, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aCustomShaders, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aCustomPhenomena, 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("All");
	choices.add("None");
	choices.add(sCamerasPreset);
	choices.add(sGeoAllPreset);
	choices.add(sGeoDefPreset);
	choices.add(sGeoInstPreset);
	choices.add(sLightsPreset);
	choices.add(aShadePreset);
	choices.add(sOptionsPreset);

	addPreset("EntityPresets", choices);

	buildPresets();
      }
    }

    {
      /* Render Global Options */
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportExactHierarchy,
	   "Tries to preserve the DAG hierarchy during processing. This produces " + 
	   "additional Mental Ray instgroup entities. There are certain unresolved " + 
	   "material inheritance issues in this mode, but it works well in the general " + 
	   "case. Deeply nested DAG hierarchies may be translated much faster compared " + 
	   "to the standard Maya iterator mode that always flattens the DAG.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportFullDagPath,
	   "Uses the full DAG path names instead of the shortest possible name for mental " + 
           "ray scene entities. This is not required to generate a valid scene, but " + 
           "ensures reproducible names even if DAG entity names are reused in Maya. On " + 
           "the other hand, with deeply nested DAG hierarchy names, you may exceed " + 
	   "the maximum supported name length in Mental Ray.", 
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportTexturesFirst,
	   "Collects all file texture references in the scene first. This ensures that " + 
           "missing texture files are reported early in the process, but may slow down " + 
           "scene processing depending on the number of file textures being used. " + 
	   "It may also write out textures references that are never used in the shading " + 
           "graph, because it doesn't perform a complete scene graph traversal for " + 
	   "performance reasons.", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportPostEffects,
	   "Lets you export post effects. This will create the stupid glow buffer.", 
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportAssignedOnly,
	   "This option ignores objects without materials during " + 
	   "translation so that they are not part of the final " + 
	   "rendered scene.", true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aExportVisibleOnly,
	   "This option ignores non-animated invisible scene entities during translation " + 
	   "so that they are not part of the final rendered scene.",
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aOptimizeAnimDetection,
	   "When this option is turned on, the processing of non-animated geometry is " + 
           "significantly optimized because Mental Ray for Maya detects animated nodes " + 
           "prior to processing the scene. This is especially useful for scenes that " + 
           "contain many static objects and only a few simply animated objects.", 
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aUseDefaultLight,
	   "Turns on or off the default light in the scene.  Off by default.",
	   false);
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

    buildLayout();

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

    /* name of the export set */ 
    String exportSet = getSingleStringParamValue(sExportSet); 

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

    /* create a temporary MEL script to export the MI files */ 
    File script = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(script); 

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
        
        boolean hierarchy  = getSingleBooleanParamValue(aExportExactHierarchy);  
        boolean dagPath    = getSingleBooleanParamValue(aExportFullDagPath);
        boolean texFirst   = getSingleBooleanParamValue(aExportTexturesFirst);
        boolean postFx     = getSingleBooleanParamValue(aExportPostEffects);
        boolean assigned   = getSingleBooleanParamValue(aExportAssignedOnly); 
        boolean visible    = getSingleBooleanParamValue(aExportVisibleOnly); 
        boolean optAnim    = getSingleBooleanParamValue(aOptimizeAnimDetection);
        boolean defaultLt  = getSingleBooleanParamValue(aUseDefaultLight);
        
        out.write
          ("setAttr \"mentalrayGlobals.exportExactHierarchy\" " + hierarchy + ";\n" + 
           "setAttr \"mentalrayGlobals.exportFullDagpath\" " + dagPath + ";\n" + 
           "setAttr \"mentalrayGlobals.exportTexturesFirst\" " + texFirst + ";\n" + 
           "setAttr \"mentalrayGlobals.exportPostEffects\" " + postFx + ";\n" + 
           "setAttr \"mentalrayGlobals.exportAssignedOnly\" " + assigned + ";\n" + 
           "setAttr \"mentalrayGlobals.exportVisibleOnly\" " + visible + ";\n" + 
           "setAttr \"mentalrayGlobals.optimizeAnimateDetection\" " + optAnim + ";\n" + 
           "setAttr \"defaultRenderGlobals.enableDefaultLight\" " + defaultLt + ";\n\n"); 
      }
	
      /* make sure only cameras in the export set are renderable */ 
      if((exportSet != null) && 
	 getSingleBooleanParamValue(aCameras) || 
         getSingleBooleanParamValue(aCameraInstances)) {

	out.write
          ("// CAMERA RENDERABLE SETTINGS\n" +
           "string $cameras[] = `ls -type camera`;\n" +
           "string $cam;\n" +
           "for($cam in $cameras) {\n" +
           "  setAttr ($cam + \".renderable\") 0;\n" +
           "}\n" +
           "select -r \"" + exportSet + "\";\n" +
           "$cameras = `ls -sl -type camera`;\n" +
           "for($cam in $cameras) {\n" +
           "  setAttr ($cam + \".renderable\") 1;\n" +
           "}\n\n"); 
      }
	
      /* select all objects in the export set */ 
      if(exportSet != null)
	out.write("// EXPORT SELECTION\n" +
                  "select -r \"" + exportSet + "\";\n\n");
	
      /* export the MI files */ 
      {
        out.write
          ("// EXPORT MI FILES\n" + 
           "string $command = \"Mayatomr -miStream -tabstop 2 ");
	
        FilePattern fpat = targetSeq.getFilePattern();
        if(targetSeq.hasFrameNumbers())
          out.write("-perframe 2 -padframe " + fpat.getPadding() + " ");

        if(exportSet != null) {
          out.write("-active -fragmentExport ");

          if(getSingleBooleanParamValue(aExportChildren))
            out.write("-fragmentChildDag ");
	
          if(getSingleBooleanParamValue(aExportMaterials)) 
	    out.write("-fragmentMaterials ");

          if(getSingleBooleanParamValue(aExportConnections))
	    out.write("-fragmentIncomingShdrs ");
	}

        {
          long filter = 0;
	
          if(!getSingleBooleanParamValue(aLinks))
	    filter += 1;
	
          if(!getSingleBooleanParamValue(aIncludes))
	    filter += 2;
	
          if(!getSingleBooleanParamValue(aVersions))
	    filter += 4;
	
          if(!getSingleBooleanParamValue(aTextures))
	    filter += 8;
	
          if(!getSingleBooleanParamValue(aObjects))
	    filter += 16;
	
          if(!getSingleBooleanParamValue(aGroups))
	    filter += 32;
	
          if(!getSingleBooleanParamValue(aLights))
	    filter += 64;
	
          if(!getSingleBooleanParamValue(aCameras))
	    filter += 128;
	
          if(!getSingleBooleanParamValue(aMaterials))
	    filter += 256;
	
          if(!getSingleBooleanParamValue(aOptions))
	    filter += 512;
	
          if(!getSingleBooleanParamValue(aFunctions))
	    filter += 1024;
	
          if(!getSingleBooleanParamValue(aFunctionDecls))
	    filter += 2048;
	
          if(!getSingleBooleanParamValue(aPhenomenaDecls))
	    filter += 4096;
	
          if(!getSingleBooleanParamValue(aUserData))
	    filter += 8192;
	
          if(!getSingleBooleanParamValue(aObjectInstances))
	    filter += 16384;
	
          if(!getSingleBooleanParamValue(aGroupInstances))
            filter += 32768;
	
          if(!getSingleBooleanParamValue(aLightInstances))
            filter += 65536;
	
          if(!getSingleBooleanParamValue(aCameraInstances))
	    filter += 131072;
	
          if(!getSingleBooleanParamValue(aFunctionInstances))
	    filter += 262144;
	
          if(!getSingleBooleanParamValue(aRender))
	    filter += 524288;
	
          if(!getSingleBooleanParamValue(aCustomText))
	    filter += 1048576;
	
          if(!getSingleBooleanParamValue(aCustomShaders))
	    filter += 2097152;
	
          if(!getSingleBooleanParamValue(aCustomPhenomena))
	    filter += 4194304;
	  
          if(filter > 0)
            out.write("-exportFilter " + filter + " ");
        }

        Path tpath = new Path(agenda.getTargetPath(), 
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
	("Unable to write the temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    /* create the process to run the action */
    return createMayaSubProcess(sourceScene, script, true, agenda, outFile, errFile);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private void 
  buildLayout()
  {
    LayoutGroup layout = new LayoutGroup(true);
    layout.addEntry(aMayaScene);
    layout.addEntry(aOutputFormat);

    {
      LayoutGroup group = 
        new LayoutGroup
	("Render Global Settings",
	 "Parameters in the render globals which control how objects are " + 
	 "translated to the mi format.",
	 true);

      group.addEntry(aExportExactHierarchy);
      group.addEntry(aExportFullDagPath);
      group.addEntry(aExportTexturesFirst);
      group.addEntry(aExportPostEffects);
      group.addEntry(aExportAssignedOnly);
      group.addEntry(aExportVisibleOnly);
      group.addEntry(aOptimizeAnimDetection);
      group.addEntry(aUseDefaultLight);

      layout.addSubGroup(group);
    }

    {
      LayoutGroup group = 
        new LayoutGroup
	("Fragment Export",
	 "Parameters for specifying which subset of the Maya scene to export.",
	 false);

      group.addEntry(sExportSet);
      group.addSeparator();
      group.addEntry(aExportChildren);
      group.addEntry(aExportMaterials);
      group.addEntry(aExportConnections);

      layout.addSubGroup(group);
    }

    {
      LayoutGroup group = 
        new LayoutGroup
	("Exported Entities",
	 "Fine grained control over the types of Mental Ray entities exported.",
	 false);

      group.addEntry("EntityPresets");
      group.addSeparator();
      group.addEntry(aLinks);
      group.addEntry(aIncludes);
      group.addEntry(aVersions);
      group.addEntry(aTextures);
      group.addEntry(aObjects);
      group.addEntry(aGroups);
      group.addEntry(aLights);
      group.addEntry(aCameras);
      group.addEntry(aMaterials);
      group.addEntry(aOptions);
      group.addEntry(aFunctions);
      group.addEntry(aFunctionDecls);
      group.addEntry(aPhenomenaDecls);
      group.addEntry(aUserData);
      group.addSeparator();
      group.addEntry(aObjectInstances);
      group.addEntry(aLightInstances);
      group.addEntry(aGroupInstances);
      group.addEntry(aCameraInstances);
      group.addEntry(aFunctionInstances);
      group.addSeparator();
      group.addEntry(aRender);
      group.addSeparator();
      group.addEntry(aCustomText);
      group.addEntry(aCustomShaders);
      group.addEntry(aCustomPhenomena);

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

  private void 
  buildPresets()
  {
    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put(aLinks, true);
      values.put(aIncludes, true);
      values.put(aVersions, true);
      values.put(aTextures, true);
      values.put(aObjects, true);
      values.put(aGroups, true);
      values.put(aLights, true);
      values.put(aCameras, true);
      values.put(aMaterials, true);
      values.put(aOptions, true);
      values.put(aFunctions, true);
      values.put(aFunctionDecls, true);
      values.put(aPhenomenaDecls, true);
      values.put(aUserData, true);
      values.put(aObjectInstances, true);
      values.put(aLightInstances, true);
      values.put(aGroupInstances, true);
      values.put(aCameraInstances, true);
      values.put(aFunctionInstances, true);
      values.put(aRender, true);
      values.put(aCustomText, true);
      values.put(aCustomShaders, true);
      values.put(aCustomPhenomena, true);

      addPresetValues("EntityPresets", "All", values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put(aLinks, false);
      values.put(aIncludes, false);
      values.put(aVersions, false);
      values.put(aTextures, false);
      values.put(aObjects, false);
      values.put(aGroups, false);
      values.put(aLights, true);
      values.put(aCameras, false);
      values.put(aMaterials, false);
      values.put(aOptions, false);
      values.put(aFunctions, false);
      values.put(aFunctionDecls, false);
      values.put(aPhenomenaDecls, false);
      values.put(aUserData, false);
      values.put(aObjectInstances, false);
      values.put(aLightInstances, true);
      values.put(aGroupInstances, false);
      values.put(aCameraInstances, false);
      values.put(aFunctionInstances, false);
      values.put(aRender, false);
      values.put(aCustomText, false);
      values.put(aCustomShaders, true);
      values.put(aCustomPhenomena, false);

      addPresetValues("EntityPresets", sLightsPreset, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put(aLinks, false);
      values.put(aIncludes, false);
      values.put(aVersions, false);
      values.put(aTextures, false);
      values.put(aObjects, false);
      values.put(aGroups, false);
      values.put(aLights, false);
      values.put(aCameras, true);
      values.put(aMaterials, false);
      values.put(aOptions, false);
      values.put(aFunctions, false);
      values.put(aFunctionDecls, false);
      values.put(aPhenomenaDecls, false);
      values.put(aUserData, false);
      values.put(aObjectInstances, false);
      values.put(aLightInstances, false);
      values.put(aGroupInstances, false);
      values.put(aCameraInstances, true);
      values.put(aFunctionInstances, false);
      values.put(aRender, false);
      values.put(aCustomText, false);
      values.put(aCustomShaders, false);
      values.put(aCustomPhenomena, false);

      addPresetValues("EntityPresets", sCamerasPreset, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put(aLinks, false);
      values.put(aIncludes, false);
      values.put(aVersions, false);
      values.put(aTextures, false);
      values.put(aObjects, false);
      values.put(aGroups, false);
      values.put(aLights, false);
      values.put(aCameras, false);
      values.put(aMaterials, false);
      values.put(aOptions, true);
      values.put(aFunctions, false);
      values.put(aFunctionDecls, false);
      values.put(aPhenomenaDecls, false);
      values.put(aUserData, false);
      values.put(aObjectInstances, false);
      values.put(aLightInstances, false);
      values.put(aGroupInstances, false);
      values.put(aCameraInstances, false);
      values.put(aFunctionInstances, false);
      values.put(aRender, false);
      values.put(aCustomText, false);
      values.put(aCustomShaders, false);
      values.put(aCustomPhenomena, false);

      addPresetValues("EntityPresets", sOptionsPreset, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put(aLinks, false);
      values.put(aIncludes, false);
      values.put(aVersions, false);
      values.put(aTextures, false);
      values.put(aObjects, true);
      values.put(aGroups, false);
      values.put(aLights, false);
      values.put(aCameras, false);
      values.put(aMaterials, false);
      values.put(aOptions, false);
      values.put(aFunctions, false);
      values.put(aFunctionDecls, false);
      values.put(aPhenomenaDecls, false);
      values.put(aUserData, false);
      values.put(aObjectInstances, true);
      values.put(aLightInstances, false);
      values.put(aGroupInstances, false);
      values.put(aCameraInstances, false);
      values.put(aFunctionInstances, false);
      values.put(aRender, false);
      values.put(aCustomText, false);
      values.put(aCustomShaders, false);
      values.put(aCustomPhenomena, false);
      values.put(aExportMaterials, false);

      addPresetValues("EntityPresets", sGeoAllPreset, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put(aLinks, false);
      values.put(aIncludes, false);
      values.put(aVersions, false);
      values.put(aTextures, false);
      values.put(aObjects, true);
      values.put(aGroups, false);
      values.put(aLights, false);
      values.put(aCameras, false);
      values.put(aMaterials, false);
      values.put(aOptions, false);
      values.put(aFunctions, false);
      values.put(aFunctionDecls, false);
      values.put(aPhenomenaDecls, false);
      values.put(aUserData, false);
      values.put(aObjectInstances, false);
      values.put(aLightInstances, false);
      values.put(aGroupInstances, false);
      values.put(aCameraInstances, false);
      values.put(aFunctionInstances, false);
      values.put(aRender, false);
      values.put(aCustomText, false);
      values.put(aCustomShaders, false);
      values.put(aCustomPhenomena, false);
      values.put(aExportMaterials, false);

      addPresetValues("EntityPresets", sGeoDefPreset, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put(aLinks, false);
      values.put(aIncludes, false);
      values.put(aVersions, false);
      values.put(aTextures, false);
      values.put(aObjects, true);
      values.put(aGroups, false);
      values.put(aLights, false);
      values.put(aCameras, false);
      values.put(aMaterials, false);
      values.put(aOptions, false);
      values.put(aFunctions, false);
      values.put(aFunctionDecls, false);
      values.put(aPhenomenaDecls, false);
      values.put(aUserData, false);
      values.put(aObjectInstances, true);
      values.put(aLightInstances, false);
      values.put(aGroupInstances, false);
      values.put(aCameraInstances, false);
      values.put(aFunctionInstances, false);
      values.put(aRender, false);
      values.put(aCustomText, false);
      values.put(aCustomShaders, false);
      values.put(aCustomPhenomena, false);

      addPresetValues("EntityPresets", sGeoInstPreset, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put(aLinks, false);
      values.put(aIncludes, false);
      values.put(aVersions, false);
      values.put(aTextures, false);
      values.put(aObjects, false);
      values.put(aGroups, false);
      values.put(aLights, false);
      values.put(aCameras, false);
      values.put(aMaterials, true);
      values.put(aOptions, false);
      values.put(aFunctions, false);
      values.put(aFunctionDecls, false);
      values.put(aPhenomenaDecls, false);
      values.put(aUserData, false);
      values.put(aObjectInstances, false);
      values.put(aLightInstances, false);
      values.put(aGroupInstances, false);
      values.put(aCameraInstances, false);
      values.put(aFunctionInstances, false);
      values.put(aRender, false);
      values.put(aCustomText, false);
      values.put(aCustomShaders, true);
      values.put(aCustomPhenomena, true);
      values.put(aExportMaterials, true);

      addPresetValues("EntityPresets", aShadePreset, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put(aLinks, false);
      values.put(aIncludes, false);
      values.put(aVersions, false);
      values.put(aTextures, false);
      values.put(aObjects, false);
      values.put(aGroups, false);
      values.put(aLights, false);
      values.put(aCameras, false);
      values.put(aMaterials, false);
      values.put(aOptions, false);
      values.put(aFunctions, false);
      values.put(aFunctionDecls, false);
      values.put(aPhenomenaDecls, false);
      values.put(aUserData, false);
      values.put(aObjectInstances, false);
      values.put(aLightInstances, false);
      values.put(aGroupInstances, false);
      values.put(aCameraInstances, false);
      values.put(aFunctionInstances, false);
      values.put(aRender, false);
      values.put(aCustomText, false);
      values.put(aCustomShaders, false);
      values.put(aCustomPhenomena, false);

      addPresetValues("EntityPresets", "None", values);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4692676603954543446L;
  
  public static final String aMayaScene             = "MayaScene";
  public static final String aOutputFormat          = "OutputFormat";
  public static final String sExportSet             = "ExportSet";
  public static final String aExportChildren        = "ExportChildren";
  public static final String aExportMaterials       = "ExportMaterials";
  public static final String aExportConnections     = "ExportConnections";
  public static final String aLinks                 = "Links";
  public static final String aIncludes              = "Includes";
  public static final String aVersions              = "Versions";
  public static final String aTextures              = "Textures";
  public static final String aObjects               = "Objects";
  public static final String aGroups                = "Groups";
  public static final String aLights                = "Lights";
  public static final String aCameras               = "Cameras";
  public static final String aMaterials             = "Materials";
  public static final String aOptions               = "Options";
  public static final String aFunctions             = "Functions";
  public static final String aFunctionDecls         = "FunctionDecls";
  public static final String aPhenomenaDecls        = "PhenomenaDecls";
  public static final String aUserData              = "UserData";
  public static final String aObjectInstances       = "ObjectInstances";
  public static final String aLightInstances        = "LightInstances";
  public static final String aGroupInstances        = "GroupInstances";
  public static final String aCameraInstances       = "CameraInstances";
  public static final String aFunctionInstances     = "FunctionInstances";
  public static final String aRender                = "Render";
  public static final String aCustomText            = "CustomText";
  public static final String aCustomShaders         = "CustomShaders";
  public static final String aCustomPhenomena       = "CustomPhenomena";
  public static final String aExportExactHierarchy  = "ExportExactHierarchy";
  public static final String aExportFullDagPath     = "ExportFullDagPath";
  public static final String aExportTexturesFirst   = "ExportTexturesFirst";
  public static final String aExportPostEffects     = "ExportPostEffects";
  public static final String aExportAssignedOnly    = "ExportAssignedOnly";
  public static final String aExportVisibleOnly     = "ExportVisibleOnly";
  public static final String aOptimizeAnimDetection = "OptimizeAnimDetection";
  public static final String aUseDefaultLight       = "UseDefaultLight";
  public static final String aPostExportMEL         = "PostExportMEL";
  public static final String aPreExportMEL          = "PreExportMEL";

  private static final String aShadePreset   = "Mental Ray Shaders and Material";
  private static final String sGeoInstPreset = "Geometry Instances";
  private static final String sGeoDefPreset  = 
    "Geometry Definition (Stub Materials/No Instances)";
  private static final String sGeoAllPreset  = 
    "Geometry (Including Instances/Stub Materials)";
  private static final String sOptionsPreset = "Options (Render Globals)";
  private static final String sCamerasPreset = "Camera Declarations and Instances";
  private static final String sLightsPreset  = "Mental Ray Lights";

}
