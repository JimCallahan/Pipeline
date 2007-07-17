package us.temerity.pipeline.plugin.FBXToCurvesAction.v2_3_1;

import java.io.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.MayaActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   F B X   T O   C U R V E S   A C T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Converts animation in an FBX curve file into Maya scene contains a group of Animation
 * curves.
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The Maya scene containing the shaders to be exported.
 *   </DIV> <BR>
 *   
 *   FBX Scene<BR>
 *   <DIV style="margin-left: 40px;">
 *     The FBX scene containing the fbx animation file.
 *     
 *   Export Set<BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the Maya Set used to identify the DAG nodes whose animation 
 *     channels should be exported.  This should not include the namespace if the 
 *     Namespace parameter is being used.
 *     
 *   Namespace <BR>
 *   <DIV style="margin-left: 40px;">
 *     An optional namespace to apply to the model on import.
 *     
 *   Clean Up Namespace <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do we need to run a MEL script to clean up namespaces after the export.  This will 
 *     be ignored if the Namespace param is null.
 *   
 *   Linear Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The linear unit that the generated scene will use. 
 *   </DIV> <BR>
 * 
 *   Angular Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The angular unit that the generated scene will use. 
 *   </DIV> <BR>
 *   
 *   Time Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The unit of time and frame rate that the generated scene will use. 
 *   </DIV> <BR>
 *   
 *   Initial MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node containing the MEL script to evaluate just after scene creation
 *     and before importing any models.
 *   </DIV> <BR>
 * 
 *   Model MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node containing the MEL script to evaluate after importing all models,
 *     but before loading and applying any animation data.
 *   </DIV> <BR>
 * 
 *   Anim MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node containing the MEL script to evaluate after loading and applying all
 *     animation data, but before saving the generated Maya scene.
 *   </DIV> <BR>
 * 
 *   Final MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node containing the MEL script to evaluate after saving the generated 
 *     Maya scene.
 *   </DIV> <BR>
 *   
 *   New Scene MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     A MEL script to evaluate in the scene created by exporting the animation.
 *   </DIV>    
 * </DIV>  
 */
public 
class FBXToCurvesAction 
  extends MayaActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public
  FBXToCurvesAction()
  {
    super("FBXToCurves", new VersionID("2.3.1"), "Temerity",
	  "An Action to take an FBX file containing animation and apply it to a Maya model.");
    
    addUnitsParams();
    
    {
      ActionParam param = 
	new LinkActionParam
	(aMayaScene, 
	 "The Maya scene containing the target for the animation.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aFBXScene, 
	 "The FBX scene containing the animation to be imported.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new StringActionParam
	(aExportSet, 
	 "The name of the Maya Set used to identify the DAG nodes " +
	 "whose animation channels should be exported.  This should not include the " +
	 "namespace if the Namespace parameter is being used.", 
	 "SELECT"); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new StringActionParam
	(aNamespace,
	 "The namespace that is on the FBX file that needs to be matched on the Maya scene.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aCleanUpNamespace,
	 "Do we need to run a MEL script to clean up namespaces after the export.  " +
	 "This will be ignored if the Namespace param is null.", 
	 false); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aNewSceneMEL,
	 "A MEL script to evaluate in the scene created by exporting the animation.", 
	 null); 
      addSingleParam(param);
    }
    
    addInitalMELParam();
    addAnimMELParam();
    addModelMELParam();
    addFinalMELParam();
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addEntry(aFBXScene);
      layout.addEntry(aExportSet);
      layout.addEntry(aNamespace);
      layout.addEntry(aCleanUpNamespace);
      layout.addSeparator();
      addUnitsParamsToLayout(layout); 
      layout.addSeparator();
      layout.addEntry(aInitialMEL);
      layout.addEntry(aModelMEL);
      layout.addEntry(aAnimMEL);
      layout.addEntry(aFinalMEL);
      layout.addEntry(aNewSceneMEL);
      
      setSingleLayout(layout);
    }
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
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
    /* the target Maya scene containing the exported shaders */
    Path targetScene = getMayaSceneTargetPath(agenda);
    String sceneType = getMayaSceneType(agenda);
    String suffix = agenda.getPrimaryTarget().getFilePattern().getSuffix();
    
    /* the source Maya scene */ 
    Path mayaSourceScene = getMayaSceneSourcePath(aMayaScene, agenda);
    Path fbxSourceScene = getFBXSceneSourcePath(aFBXScene, agenda);
    
    /* the mel script path*/
    Path newSceneMEL = getMelScriptSourcePath(aNewSceneMEL, agenda);
    
    String namespace = getSingleStringParamValue(aNamespace);
    boolean cleanUp = getSingleBooleanParamValue(aCleanUpNamespace);
    if (namespace == null)
      cleanUp = false;
    
    boolean tempScene = false;
    if ( (newSceneMEL != null) || cleanUp )
      tempScene = true;
    
    Path newScene = null;
    if (tempScene)
      newScene = new Path(getTempPath(agenda), "tempMayaScene." + suffix); 
    
    String exportSet = getSingleStringParamValue(aExportSet);
    
    /* create a temporary MEL script used to export the curves*/ 
    File script = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(script);
      
      /* load the fbx plugin */ 
      out.write("if (!`pluginInfo -q -l fbxmaya`)\n" +
      		"  loadPlugin \"fbxmaya\";\n\n");
      
      
      out.write(genUnitsMEL());
      
      writeInitialMEL(agenda, out);
      
      out.write("file -import " +
		"-type \"" + sceneType + "\" " );
      if (namespace != null)
	out.write("-namespace \"" + namespace + "\" ");
      out.write("-options \"v=0\"" +
		" \"" + mayaSourceScene + "\";\n\n");
      
      writeModelMEL(agenda, out);
      
      /*set FBX options and disable prompt*/
      out.write("eval FBXImportShowUI -v false;\n");
      out.write("eval FBXImportMode -v exmerge;\n");
      
      /*import in fbx animation...*/
      out.write("eval \"FBXImport -f \\\"" + fbxSourceScene + "\\\"\";\n\n"); 
      
      writeAnimMEL(agenda, out);
      
      if (namespace != null)
	exportSet = namespace + ":" + exportSet;
      
      out.write("select -r " + exportSet + ";\n");
      
      Path actualTarget = targetScene;
      if (tempScene)
	actualTarget = newScene;
      
      out.write("file -f " +
      		"-type \"" + sceneType + "\" " +
      		"-eas \"" + actualTarget + "\";\n\n");
      
      writeFinalMEL(agenda, out);

      if (tempScene) {
	out.write("// NEW SCENE SCRIPT\n" +
	          "file -open -f \"" + newScene + "\";\n");
	if (cleanUp)
	  out.write("namespace -f -mv \"" + namespace + "\" \":\";\n");
	if (newSceneMEL != null)
	  out.write("print \"New-Scene Script: " + newSceneMEL+ "\\n\";\n" +
	  	    "source \"" + newSceneMEL + "\";\n");
	out.write("file -rename \"" + targetScene + "\";\n" + 
		  "file -save\n;");
      }
      
      out.write("print \"ALL DONE.\\n\";\n");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    /* create the process to run the action */
    return createMayaSubProcess(null, script, true, agenda, outFile, errFile);

  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T Y   M E T H O D S                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  private Path 
  getFBXSceneSourcePath
  (
    String scene, 
    ActionAgenda agenda
  ) 
    throws PipelineException
  {
    return getPrimarySourcePath(scene, agenda, "fbx", "FBX anim file");
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aFBXScene         = "FBXScene";
  public static final String aExportSet        = "ExportSet";
  public static final String aNewSceneMEL      = "NewSceneMEL";
  public static final String aNamespace        = "Namespace";
  public static final String aCleanUpNamespace = "CleanUpNamespace"; 
  
  private static final long serialVersionUID = -2850329095954021895L;
}
