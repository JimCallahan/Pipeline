// $Id: MayaMiExportAction.java,v 1.1 2007/06/19 18:24:50 jim Exp $

package us.temerity.pipeline.plugin.MayaMiExportAction.v2_0_10;

import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.*;

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
 *     The source node which contains the Maya scene file. <BR> 
 *   </DIV> <BR>
 * 
 *   Output Format <BR>
 *   <DIV style="margin-left: 40px;">
 *     The format of the output MI file. <BR>
 *   </DIV> 
 * 
 *   <I> Render Global Options</I> <BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Export Exact Hierarchy 
 *     <DIV style="margin-left: 40px;"> 
 *       Tries to preserve the DAG hierarchy during processing. This produces additional 
 *       mental ray instgroup entities. There are certain unresolved material inheritance 
 *       issues in this mode, but it works well in the general case. Deeply nested DAG 
 *       hierarchies may be translated much faster compared to the standard Maya iterator 
 *       mode that always flattens the DAG. 
 *     </DIV><BR>
 * 
 *     Export Full DAG Path 
 *     <DIV style="margin-left: 40px;"> 
 *       Uses the full DAG path names instead of the shortest possible name for mental ray 
 *       scene entities. This is not required to generate a valid scene, but ensures 
 *       reproducible names even if DAG entity names are reused in Maya. On the other hand, 
 *       with deeply nested DAG hierarchy names, you may exceed the maximum supported name
 *       length in mental ray. 
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
 *       significantly optimized because mental ray for Maya detects animated nodes prior to 
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
public
class MayaMiExportAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaMiExportAction() 
  {
    super("MayaMiExport", new VersionID("2.0.10"), "Temerity",
	  "Exports MentalRay geometry and other scene data from a Maya scene.");

    {
      ActionParam param = 
	new LinkActionParam
	("MayaScene",
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
	("OutputFormat", 
	 "The format of the output MI file.",
	 "ASCII", choices);
      addSingleParam(param);
    } 

    /* fragment export */ 
    {
      {
	ActionParam param = 
	  new StringActionParam
	  ("ExportSet", 
	   "The name of the Maya Set used to select the DAG nodes to export from the " +
	   "Maya scene. If unset, then the entire scene will be exported.", 
	   null);
	addSingleParam(param);
      }
    
      {
	ActionParam param = 
	  new BooleanActionParam
	  ("ExportChildren",
	   "Whether to additionally export all child DAGs of the nodes selected for export.",
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("ExportMaterials",
	   "Whether to additionally export all materials associated with the nodes " +
	   "selected for export.",
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("ExportConnections",
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
	  ("Links", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Includes", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Versions", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Textures", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Objects", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Groups", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Lights", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Cameras", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Materials", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Options", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Functions", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("FunctionDecls", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("PhenomenaDecls", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("UserData", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("ObjectInstances", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("LightInstances", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("GroupInstances", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("CameraInstances", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("FunctionInstances", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("Render", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("CustomText", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("CustomShaders", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  ("CustomPhenomena", 
	   "", 
	   true);
	addSingleParam(param);
      }

      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("All");
	choices.add("None");
	choices.add(preset_CAMERAS);
	choices.add(preset_GEOALL);
	choices.add(preset_GEODEC);
	choices.add(preset_GEOINST);
	choices.add(preset_MRLIGHTS);
	choices.add(preset_MRSHADE);
	choices.add(preset_OPTIONS);

	addPreset("EntityPresets", choices);

	buildPresets();
      }
    }

    {
      /* Render Global Options */
      {
	ActionParam param = 
	  new BooleanActionParam
	  (PARAM_exactHier,
	   "Tries to preserve the DAG hierarchy during processing. This produces " + 
	   "additional mental ray instgroup entities. There are certain unresolved " + 
	   "material inheritance issues in this mode, but it works well in the general " + 
	   "case. Deeply nested DAG hierarchies may be translated much faster compared " + 
	   "to the standard Maya iterator mode that always flattens the DAG.",
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (PARAM_fullDag,
	   "Uses the full DAG path names instead of the shortest " + 
	   "possible name for mental ray scene entities. " + 
	   "This is not required to generate a valid scene, " + 
	   "but ensures reproducible names even if DAG entity " + 
	   "names are reused in Maya. On the other hand, with "	+ 
	   "deeply nested DAG hierarchy names, you may exceed " + 
	   "the maximum supported name length in mental ray", 
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (PARAM_textures,
	   "Collects all file texture references in the scene first. " + 
	   "This ensures that missing texture files are reported " + 
	   "early in the process, but may slow down scene processing " + 
	   "depending on the number of file textures being used. " + 
	   "It may also write out textures references that are " + 
	   "never used in the shading graph, because it doesn't " + 
	   "perform a complete scene graph traversal for " + 
	   "performance reasons.", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (PARAM_post,
	   "Lets you export post effects. This will create the stupid glow buffer.", 
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (PARAM_assigned,
	   "This option ignores objects without materials during " + 
	   "translation so that they are not part of the final " + 
	   "rendered scene.", true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (PARAM_visible,
	   "This option ignores non-animated invisible scene entities during translation " + 
	   "so that they are not part of the final rendered scene.",
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (PARAM_optimize,
	   "When this option is turned on, the processing of " + 
	   "non-animated geometry is significantly optimized because " +  
	   "mental ray for Maya detects animated nodes prior to " + 
	   "processing the scene. This is especially useful for " + 
	   "scenes that contain many static objects and only a " + 
	   "few simply animated objects.", 
	   false);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (PARAM_defaultLight,
	   "Turns on or off the default light in the scene.  Off by default.",
	   false);
	addSingleParam(param);
      }

    }

    /* MEL scripts */
    {
      {
	ActionParam param = new LinkActionParam(PARAM_preExport,
						"The pre-export MEL script.", null);
	addSingleParam(param);
      }

      {
	ActionParam param = new LinkActionParam(PARAM_postExport,
						"The post-export MEL script.", null);
	addSingleParam(param);
      }
    }

    buildLayout();

    addSupport(OsType.MacOS);
    // addSupport(OsType.Windows); // SHOULD WORK, BUT UNTESTED
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
    FileSeq target = null;
    Path scene = null;
    String exportSet = null;
    Path preExport = null;
    Path postExport = null;
    {
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("mi"))	   
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") must contain one or more Mental " + 
	     "Ray Input (.mi) files!");
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
	      ("The MayaMiExport Action requires that the source node specified by the " +
	       "Maya Scene parameter (" + sname + ") must have a single Maya scene file " + 
	       "as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  scene = new Path(PackageInfo.sProdPath,
			   snodeID.getWorkingParent() + "/" + fseq.getPath(0));
	}
	else {
	  throw new PipelineException
	    ("The MayaMiExport Action requires the Maya Scene parameter to be set!");
	}
      }      

      {
	String str = (String) getSingleParamValue("ExportSet"); 
	if((str != null) && (str.length() > 0)) 
	  exportSet = str;
      }

      {
	String sname = (String) getSingleParamValue(PARAM_preExport);
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Pre Export MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The MayaMiExport Action requires that the source node specified by the " +
	       "Pre Export MEL parameter (" + sname + ") must have a single MEL script " + 
	       "as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  preExport = new Path(PackageInfo.sProdPath,
			       snodeID.getWorkingParent() + "/" + fseq.getPath(0));
	}
      }

      {
	String sname = (String) getSingleParamValue(PARAM_postExport);
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Post Export MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The MayaMiExport Action requires that the source node specified by the " +
	       "Post Export MEL parameter (" + sname + ") must have a single MEL script " + 
	       "as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  postExport = new Path(PackageInfo.sProdPath,
				snodeID.getWorkingParent() + "/" + fseq.getPath(0));
	}
      }
    }

    /* create a temporary MEL script */ 
    File script = createTemp(agenda, 0644, "mel");
    try {
      PrintWriter out = new PrintWriter(new FileWriter(script));

      if(PackageInfo.sOsType == OsType.Windows) {
	out.println("if(!`pluginInfo -q -l \"Mayatomr.mll\"`)");
	out.println("loadPlugin \"Mayatomr.mll\";");
      } 
      else {
	out.println("if(!`pluginInfo -q -l \"Mayatomr.so\"`)");
	out.println("loadPlugin \"Mayatomr.so\";");
      }
      
      out.println("miCreateDefaultNodes();");
      out.println("miCreateOtherOptionsNodesForURG();");
      
      if(preExport != null) 
	out.write("source \"" + preExport + "\";\n\n");
      
      FilePattern fpat = target.getFilePattern();
      FrameRange range = target.getFrameRange();
      if(target.hasFrameNumbers()) {
	
	// You need this to actually force maya to export a sequence
	// if the globals in the scene are still set to a single frame
	// which happens to the be default in Maya.
	out.write("setAttr \"defaultRenderGlobals.animation\" 1;\n");

	// This should ensure the naming formats are all correct.
	out.write("setAttr \"defaultRenderGlobals.outFormatControl\" 0;\n");
	out.write("setAttr \"defaultRenderGlobals.periodInExt\" 1;\n");
	out.write("setAttr \"defaultRenderGlobals.putFrameBeforeExt\" 1;");
	  
	out.write
	  ("setAttr \"defaultRenderGlobals.startFrame\" " + range.getStart() + ";\n" + 
	   "setAttr \"defaultRenderGlobals.endFrame\" " + range.getEnd() + ";\n" + 
	   "setAttr \"defaultRenderGlobals.byFrameStep\" " + range.getBy() + ";\n\n");
      }

      // sets all the render global values in the mel script
      {
	boolean tf = (Boolean) getSingleParamValue(PARAM_exactHier);
	out.print("setAttr \"mentalrayGlobals.exportExactHierarchy\" ");
	if(tf)
	  out.println(1 + ";");
	else
	  out.println(0 + ";");
      }
	
      {
	boolean tf = (Boolean) getSingleParamValue(PARAM_fullDag);
	out.print("setAttr \"mentalrayGlobals.exportFullDagpath\" ");
	if(tf)
	  out.println(1 + ";");
	else
	  out.println(0 + ";");
      }

      {
	boolean tf = (Boolean) getSingleParamValue(PARAM_textures);
	out.print("setAttr \"mentalrayGlobals.exportTexturesFirst\" ");
	if(tf)
	  out.println(1 + ";");
	else
	  out.println(0 + ";");
      }
	
      {
	boolean tf = (Boolean) getSingleParamValue(PARAM_post);
	out.print("setAttr \"mentalrayGlobals.exportPostEffects\" ");
	if(tf)
	  out.println(1 + ";");
	else
	  out.println(0 + ";");
      }
	
      {
	boolean tf = (Boolean) getSingleParamValue(PARAM_assigned);
	out.print("setAttr \"mentalrayGlobals.exportAssignedOnly\" ");
	if(tf)
	  out.println(1 + ";");
	else
	  out.println(0 + ";");
      }
	
      {
	boolean tf = (Boolean) getSingleParamValue(PARAM_visible);
	out.print("setAttr \"mentalrayGlobals.exportVisibleOnly\" ");
	if(tf)
	  out.println(1 + ";");
	else
	  out.println(0 + ";");
      }
	
      {
	boolean tf = (Boolean) getSingleParamValue(PARAM_optimize);
	out.print("setAttr \"mentalrayGlobals.optimizeAnimateDetection\" ");
	if(tf)
	  out.println(1 + ";");
	else
	  out.println(0 + ";");
      }

      {
	boolean tf = (Boolean) getSingleParamValue(PARAM_defaultLight);
	out.print("setAttr \"defaultRenderGlobals.enableDefaultLight\" ");
	if(tf)
	  out.println(1 + ";");
	else
	  out.println(0 + ";");
      }
	
      if((exportSet != null) && 
	 ((Boolean) getSingleParamValue("Cameras") || 
	  (Boolean) getSingleParamValue("CameraInstances"))) {
	out.println("string $cameras[] = `ls -type camera`;");
	out.println("string $cam;");
	out.println("for ($cam in $cameras)");
	out.println("{");
	out.println("	setAttr ($cam + \".renderable\") 0;");
	out.println("}");
	out.println("select -r CAMERA;");
	out.println("$cameras = `ls -sl -type camera`;");
	out.println("for ($cam in $cameras)");
	out.println("{");
	out.println("	setAttr ($cam + \".renderable\") 1;");
	out.println("}");
      }
	
      if(exportSet != null)
	out.write("select -r \"" + exportSet + "\";\n\n");
	
      out.write("string $command = \"Mayatomr -miStream -tabstop 2 ");
	
      if(target.hasFrameNumbers())
	out.write("-perframe 2 -padframe " + fpat.getPadding() + " ");

      if(exportSet != null) {
	out.write("-active -fragmentExport ");

	{
	  Boolean tf = (Boolean) getSingleParamValue("ExportChildren"); 
	  if((tf != null) && tf) 
	    out.write("-fragmentChildDag ");
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("ExportMaterials"); 
	  if((tf != null) && tf) 
	    out.write("-fragmentMaterials ");
	}

	{
	  Boolean tf = (Boolean) getSingleParamValue("ExportConnections"); 
	  if((tf != null) && tf) 
	    out.write("-fragmentIncomingShdrs ");
	}
      }

      {
	int filter = 0;
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("Links"); 
	  if((tf == null) || !tf) 
	    filter += 1;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("Includes"); 
	  if((tf == null) || !tf) 
	    filter += 2;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("Versions"); 
	  if((tf == null) || !tf) 
	    filter += 4;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("Textures"); 
	  if((tf == null) || !tf) 
	    filter += 8;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("Objects"); 
	  if((tf == null) || !tf) 
	    filter += 16;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("Groups"); 
	  if((tf == null) || !tf) 
	    filter += 32;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("Lights"); 
	  if((tf == null) || !tf) 
	    filter += 64;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("Cameras"); 
	  if((tf == null) || !tf) 
	    filter += 128;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("Materials"); 
	  if((tf == null) || !tf) 
	    filter += 256;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("Options"); 
	  if((tf == null) || !tf) 
	    filter += 512;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("Functions"); 
	  if((tf == null) || !tf) 
	    filter += 1024;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("FunctionDecls"); 
	  if((tf == null) || !tf) 
	    filter += 2048;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("PhenomenaDecls"); 
	  if((tf == null) || !tf) 
	    filter += 4096;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("UserData"); 
	  if((tf == null) || !tf) 
	    filter += 8192;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("ObjectInstances"); 
	  if((tf == null) || !tf) 
	    filter += 16384;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("GroupInstances");
	  if((tf == null) || !tf)
	    filter += 32768;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("LightInstances");
	  if((tf == null) || !tf)
	    filter += 65536;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("CameraInstances"); 
	  if((tf == null) || !tf) 
	    filter += 131072;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("FunctionInstances"); 
	  if((tf == null) || !tf) 
	    filter += 262144;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("Render"); 
	  if((tf == null) || !tf) 
	    filter += 524288;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("CustomText"); 
	  if((tf == null) || !tf) 
	    filter += 1048576;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("CustomShaders"); 
	  if((tf == null) || !tf) 
	    filter += 2097152;
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue("CustomPhenomena"); 
	  if((tf == null) || !tf) 
	    filter += 4194304;
	}
	  
	if(filter > 0)
	  out.write("-exportFilter " + filter + " ");
      }

      out.write("-file \\\"" + fpat.getPrefix() + "." + fpat.getSuffix() + "\\\"\";\n\n");
      out.println("evalEcho($command);");

      if(postExport != null)
	out.write("source \"" + postExport + "\";\n");
	
      out.close();
    }
    catch(IOException ex) {
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
      if(PackageInfo.sOsType == OsType.Windows) 
	program = (program + ".exe");

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 program, args, agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private void 
  buildLayout()
  {
    LayoutGroup layout = new LayoutGroup(true);
    layout.addEntry("MayaScene");
    layout.addEntry("OutputFormat");

    {
      LayoutGroup group = new LayoutGroup
	("Render Global Settings",
	 "Parameters in the render globals which control how objects are " + 
	 "translated to the mi format.",
	 true);
      group.addEntry(PARAM_exactHier);
      group.addEntry(PARAM_fullDag);
      group.addEntry(PARAM_textures);
      group.addEntry(PARAM_post);
      group.addEntry(PARAM_assigned);
      group.addEntry(PARAM_visible);
      group.addEntry(PARAM_optimize);
      group.addEntry(PARAM_defaultLight);

      layout.addSubGroup(group);
    }

    {
      LayoutGroup group = new LayoutGroup
	("Fragment Export",
	 "Parameters for specifying which subset of the Maya scene to export.",
	 false);
      group.addEntry("ExportSet");
      group.addSeparator();
      group.addEntry("ExportChildren");
      group.addEntry("ExportMaterials");
      group.addEntry("ExportConnections");

      layout.addSubGroup(group);
    }

    {
      LayoutGroup group = new LayoutGroup
	("Exported Entities",
	 "Fine grained control over the types of Mental Ray entities exported.",
	 false);
      group.addEntry("EntityPresets");
      group.addSeparator();
      group.addEntry("Links");
      group.addEntry("Includes");
      group.addEntry("Versions");
      group.addEntry("Textures");
      group.addEntry("Objects");
      group.addEntry("Groups");
      group.addEntry("Lights");
      group.addEntry("Cameras");
      group.addEntry("Materials");
      group.addEntry("Options");
      group.addEntry("Functions");
      group.addEntry("FunctionDecls");
      group.addEntry("PhenomenaDecls");
      group.addEntry("UserData");
      group.addSeparator();
      group.addEntry("ObjectInstances");
      group.addEntry("LightInstances");
      group.addEntry("GroupInstances");
      group.addEntry("CameraInstances");
      group.addEntry("FunctionInstances");
      group.addSeparator();
      group.addEntry("Render");
      group.addSeparator();
      group.addEntry("CustomText");
      group.addEntry("CustomShaders");
      group.addEntry("CustomPhenomena");

      layout.addSubGroup(group);
    }

    {
      LayoutGroup group = new LayoutGroup
	("MEL Scripts",
	 "MEL scripts run at various stages of the exporting process.", 
	 true);
      group.addEntry(PARAM_preExport);
      group.addEntry(PARAM_postExport);

      layout.addSubGroup(group);
    }

    setSingleLayout(layout);
  }

  private void 
  buildPresets()
  {
    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put("Links", true);
      values.put("Includes", true);
      values.put("Versions", true);
      values.put("Textures", true);
      values.put("Objects", true);
      values.put("Groups", true);
      values.put("Lights", true);
      values.put("Cameras", true);
      values.put("Materials", true);
      values.put("Options", true);
      values.put("Functions", true);
      values.put("FunctionDecls", true);
      values.put("PhenomenaDecls", true);
      values.put("UserData", true);
      values.put("ObjectInstances", true);
      values.put("LightInstances", true);
      values.put("GroupInstances", true);
      values.put("CameraInstances", true);
      values.put("FunctionInstances", true);
      values.put("Render", true);
      values.put("CustomText", true);
      values.put("CustomShaders", true);
      values.put("CustomPhenomena", true);

      addPresetValues("EntityPresets", "All", values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put("Links", false);
      values.put("Includes", false);
      values.put("Versions", false);
      values.put("Textures", false);
      values.put("Objects", false);
      values.put("Groups", false);
      values.put("Lights", true);
      values.put("Cameras", false);
      values.put("Materials", false);
      values.put("Options", false);
      values.put("Functions", false);
      values.put("FunctionDecls", false);
      values.put("PhenomenaDecls", false);
      values.put("UserData", false);
      values.put("ObjectInstances", false);
      values.put("LightInstances", true);
      values.put("GroupInstances", false);
      values.put("CameraInstances", false);
      values.put("FunctionInstances", false);
      values.put("Render", false);
      values.put("CustomText", false);
      values.put("CustomShaders", true);
      values.put("CustomPhenomena", false);

      addPresetValues("EntityPresets", preset_MRLIGHTS, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put("Links", false);
      values.put("Includes", false);
      values.put("Versions", false);
      values.put("Textures", false);
      values.put("Objects", false);
      values.put("Groups", false);
      values.put("Lights", false);
      values.put("Cameras", true);
      values.put("Materials", false);
      values.put("Options", false);
      values.put("Functions", false);
      values.put("FunctionDecls", false);
      values.put("PhenomenaDecls", false);
      values.put("UserData", false);
      values.put("ObjectInstances", false);
      values.put("LightInstances", false);
      values.put("GroupInstances", false);
      values.put("CameraInstances", true);
      values.put("FunctionInstances", false);
      values.put("Render", false);
      values.put("CustomText", false);
      values.put("CustomShaders", false);
      values.put("CustomPhenomena", false);

      addPresetValues("EntityPresets", preset_CAMERAS, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put("Links", false);
      values.put("Includes", false);
      values.put("Versions", false);
      values.put("Textures", false);
      values.put("Objects", false);
      values.put("Groups", false);
      values.put("Lights", false);
      values.put("Cameras", false);
      values.put("Materials", false);
      values.put("Options", true);
      values.put("Functions", false);
      values.put("FunctionDecls", false);
      values.put("PhenomenaDecls", false);
      values.put("UserData", false);
      values.put("ObjectInstances", false);
      values.put("LightInstances", false);
      values.put("GroupInstances", false);
      values.put("CameraInstances", false);
      values.put("FunctionInstances", false);
      values.put("Render", false);
      values.put("CustomText", false);
      values.put("CustomShaders", false);
      values.put("CustomPhenomena", false);

      addPresetValues("EntityPresets", preset_OPTIONS, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put("Links", false);
      values.put("Includes", false);
      values.put("Versions", false);
      values.put("Textures", false);
      values.put("Objects", true);
      values.put("Groups", false);
      values.put("Lights", false);
      values.put("Cameras", false);
      values.put("Materials", false);
      values.put("Options", false);
      values.put("Functions", false);
      values.put("FunctionDecls", false);
      values.put("PhenomenaDecls", false);
      values.put("UserData", false);
      values.put("ObjectInstances", true);
      values.put("LightInstances", false);
      values.put("GroupInstances", false);
      values.put("CameraInstances", false);
      values.put("FunctionInstances", false);
      values.put("Render", false);
      values.put("CustomText", false);
      values.put("CustomShaders", false);
      values.put("CustomPhenomena", false);
      values.put("ExportMaterials", false);

      addPresetValues("EntityPresets", preset_GEOALL, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put("Links", false);
      values.put("Includes", false);
      values.put("Versions", false);
      values.put("Textures", false);
      values.put("Objects", true);
      values.put("Groups", false);
      values.put("Lights", false);
      values.put("Cameras", false);
      values.put("Materials", false);
      values.put("Options", false);
      values.put("Functions", false);
      values.put("FunctionDecls", false);
      values.put("PhenomenaDecls", false);
      values.put("UserData", false);
      values.put("ObjectInstances", false);
      values.put("LightInstances", false);
      values.put("GroupInstances", false);
      values.put("CameraInstances", false);
      values.put("FunctionInstances", false);
      values.put("Render", false);
      values.put("CustomText", false);
      values.put("CustomShaders", false);
      values.put("CustomPhenomena", false);
      values.put("ExportMaterials", false);

      addPresetValues("EntityPresets", preset_GEODEC, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put("Links", false);
      values.put("Includes", false);
      values.put("Versions", false);
      values.put("Textures", false);
      values.put("Objects", true);
      values.put("Groups", false);
      values.put("Lights", false);
      values.put("Cameras", false);
      values.put("Materials", false);
      values.put("Options", false);
      values.put("Functions", false);
      values.put("FunctionDecls", false);
      values.put("PhenomenaDecls", false);
      values.put("UserData", false);
      values.put("ObjectInstances", true);
      values.put("LightInstances", false);
      values.put("GroupInstances", false);
      values.put("CameraInstances", false);
      values.put("FunctionInstances", false);
      values.put("Render", false);
      values.put("CustomText", false);
      values.put("CustomShaders", false);
      values.put("CustomPhenomena", false);

      addPresetValues("EntityPresets", preset_GEOINST, values);
    }

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put("Links", false);
      values.put("Includes", false);
      values.put("Versions", false);
      values.put("Textures", false);
      values.put("Objects", false);
      values.put("Groups", false);
      values.put("Lights", false);
      values.put("Cameras", false);
      values.put("Materials", true);
      values.put("Options", false);
      values.put("Functions", false);
      values.put("FunctionDecls", false);
      values.put("PhenomenaDecls", false);
      values.put("UserData", false);
      values.put("ObjectInstances", false);
      values.put("LightInstances", false);
      values.put("GroupInstances", false);
      values.put("CameraInstances", false);
      values.put("FunctionInstances", false);
      values.put("Render", false);
      values.put("CustomText", false);
      values.put("CustomShaders", true);
      values.put("CustomPhenomena", true);
      values.put("ExportMaterials", true);

      addPresetValues("EntityPresets", preset_MRSHADE, values);
    }

    // ... more possible?

    {
      TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
      values.put("Links", false);
      values.put("Includes", false);
      values.put("Versions", false);
      values.put("Textures", false);
      values.put("Objects", false);
      values.put("Groups", false);
      values.put("Lights", false);
      values.put("Cameras", false);
      values.put("Materials", false);
      values.put("Options", false);
      values.put("Functions", false);
      values.put("FunctionDecls", false);
      values.put("PhenomenaDecls", false);
      values.put("UserData", false);
      values.put("ObjectInstances", false);
      values.put("LightInstances", false);
      values.put("GroupInstances", false);
      values.put("CameraInstances", false);
      values.put("FunctionInstances", false);
      values.put("Render", false);
      values.put("CustomText", false);
      values.put("CustomShaders", false);
      values.put("CustomPhenomena", false);

      addPresetValues("EntityPresets", "None", values);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final String PARAM_exactHier = "ExportExactHierarchy";
  private static final String PARAM_fullDag = "ExportFullDagPath";
  private static final String PARAM_textures = "ExportTexturesFirst";
  private static final String PARAM_post = "ExportPostEffects";
  private static final String PARAM_assigned = "ExportAssignedOnly";
  private static final String PARAM_visible = "ExportVisibleOnly";
  private static final String PARAM_optimize = "OptimizeAnimDetection";
  private static final String PARAM_defaultLight = "UseDefaultLight";
  private static final String PARAM_postExport = "PostExportMEL";
  private static final String PARAM_preExport = "PreExportMEL";
  private static final String preset_MRSHADE = "Mental Ray Shaders and Material";
  private static final String preset_GEOINST = "Geometry Instances";
  private static final String preset_GEODEC = 
    "Geometry Definition (Stub Materials/No Instances)";
  private static final String preset_GEOALL = 
    "Geometry (Including Instances/Stub Materials)";
  private static final String preset_OPTIONS = "Options (Render Globals)";
  private static final String preset_CAMERAS = "Camera Declarations and Instances";
  private static final String preset_MRLIGHTS = "Mental Ray Lights";



  /*----------------------------------------------------------------------------------------*/
  /* S T A T I C I N T E R N A L S */
  /*----------------------------------------------------------------------------------------*/

   private static final long serialVersionUID = -3212649057235728812L;

}
