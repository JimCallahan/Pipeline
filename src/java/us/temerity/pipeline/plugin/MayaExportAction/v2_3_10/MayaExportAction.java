package us.temerity.pipeline.plugin.MayaExportAction.v2_3_10;

import java.io.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.MayaActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   E X P O R T   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/
/**
 * Action that exports selected objects from a maya scene.
 * <p>
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The Maya scene containing the shaders to be exported.
 *   </DIV> <BR>
 *     
 *   Export Set<BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the Maya Set used to identify the DAG nodes whose animation 
 *     channels should be exported. 
 *   </DIV> <BR>
 *     
 *   Clean Up Namespace <BR>
 *   <DIV style="margin-left: 40px;">
 *     Do we need to run a MEL script to clean up namespaces after the export.
 *   </DIV> <BR>
 *   
 *   Initial MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node containing the MEL script to evaluate just after the scene is 
 *     opened.
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
 *   
 *   Pre Export MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     A MEL snippet that will get pasted into the mel script right before the
 *     export happens.  Use this to modify what is selected to change what is
 *     getting exported.
 *   </DIV>   
 * </DIV>  
 */
public 
class MayaExportAction
  extends MayaActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaExportAction()
  {
    super("MayaExport", new VersionID("2.3.10"), "Temerity",
      "An Action to export selected objects from an animated scene.");
    
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
	new StringActionParam
	(aExportSet, 
	 "The name of the Maya Set or group used to identify the DAG nodes to be exported.", 
	 "SELECT"); 
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
    
    {
      ActionParam param = 
	new LinkActionParam
	(aPreExportMEL,
	 "A MEL snippet that will get pasted into the mel script right before the " +
	 "export happens.  Use this to modify what is selected to change what is " +
	 "getting exported.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
    	new BooleanActionParam
    	(aExportChannels,
    	 "Should channel information be exported.", 
    	 true); 
      addSingleParam(param);
    }
    
    {
        ActionParam param = 
      	new BooleanActionParam
      	(aExportConstraints,
      	 "Should constraint information be exported.", 
      	 true); 
        addSingleParam(param);
      }

    {
        ActionParam param = 
      	new BooleanActionParam
      	(aExportExpressions,
      	 "Should expression information be exported.", 
      	 true); 
        addSingleParam(param);
      }

    {
        ActionParam param = 
      	new BooleanActionParam
      	(aExportHistory,
      	 "Should history information be exported.", 
      	 true); 
        addSingleParam(param);
      }

    
    addInitalMELParam();
    addFinalMELParam();
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addEntry(aExportSet);
      layout.addEntry(aCleanUpNamespace);
      layout.addSeparator();
      layout.addEntry(aInitialMEL);
      layout.addEntry(aPreExportMEL);
      layout.addEntry(aFinalMEL);
      layout.addEntry(aNewSceneMEL);
      
      LayoutGroup group = 
        new LayoutGroup("ExportOptions", "Maya Options for file export", true);

      group.addEntry(aExportChannels);
      group.addEntry(aExportConstraints);
      group.addEntry(aExportExpressions);
      group.addEntry(aExportHistory);
      
      layout.addSubGroup(group);
      
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
    
    /* the mel script path*/
    Path newSceneMEL = getMelScriptSourcePath(aNewSceneMEL, agenda);
    
    int exportHistory     = getSingleBooleanParamValue(aExportHistory) ? 1 : 0;
    int exportConstraint  = getSingleBooleanParamValue(aExportConstraints) ? 1 : 0;
    int exportExpressions = getSingleBooleanParamValue(aExportExpressions) ? 1 : 0;
    int exportChannels    = getSingleBooleanParamValue(aExportChannels) ? 1 : 0;
    
    boolean cleanUp = getSingleBooleanParamValue(aCleanUpNamespace);
    boolean tempScene = false;
    if ( (newSceneMEL != null) || cleanUp )
      tempScene = true;
    
    Path newScene = null;
    if (tempScene)
      newScene = new Path(getTempPath(agenda), "tempMayaScene." + suffix); 
    
    String exportSet = getSingleStringParamValue(aExportSet);
    
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

    /* create a temporary MEL script used to export the curves*/ 
    File script = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(script);

      writeInitialMEL(agenda, out);
      
      out.write("string $export = \"" + exportSet + "\";\n");
      
      Path actualTarget = targetScene;
      if (tempScene)
	actualTarget = newScene;
      
      if (melSnippet != null) {
        out.write(melSnippet);
      }
      
      out.write("if (`objExists $export`) \n" +
      	        "{\n" +
      	        "  select -r $export;\n" +
      	        "  file -f " +
      		"  -type \"" + sceneType + "\" " +
      		"  -constructionHistory " +  exportHistory + " " +
      		"  -constraints " +  exportConstraint + " " +
      		"  -channels " +  exportChannels + " " +
      		"  -expressions " +  exportExpressions + " " +
      		"  -es \"" + actualTarget + "\";\n");
      writeFinalMEL(agenda, out);
      out.write("}\n" + 
                "else \n" +
                "{\n" + 
                "  file -f -new;\n" + 
                "  file -rename \"" + actualTarget + "\";\n" + 
                "  file -type \"" + sceneType + "\";\n" + 
                "  file -save;\n" + 
                "}\n\n");
      
      if (tempScene) {
	    out.write("// NEW SCENE SCRIPT\n" +
	              "file -open -f \"" + newScene + "\";\n");
	if (cleanUp)
	  out.write("string $ns;\n" + 
	  	    "for ($ns in `namespaceInfo -lon`) {\n" + 
	  	    "  if ($ns != \"UI\")\n" + 
	  	    "    catch(`namespace -f -mv $ns \":\"`);\n" + 
	            "}\n\n");
	if (newSceneMEL != null)
	  out.write("print \"New-Scene Script: " + newSceneMEL+ "\\n\";\n" +
	  	    "source \"" + newSceneMEL + "\";\n");
	out.write("file -rename \"" + targetScene + "\";\n" + 
		  "file -save;\n");
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
    return createMayaSubProcess(mayaSourceScene, script, true, agenda, outFile, errFile);
  }

  
  private String 
  getMelSnippet
  (
    ActionAgenda agenda  
  )
    throws PipelineException, IOException
  {
    String toReturn = null;
    Path melPath = getMelScriptSourcePath(aPreExportMEL, agenda);
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
  
  public static final String aExportSet          = "ExportSet";
  public static final String aNewSceneMEL        = "NewSceneMEL";
  public static final String aPreExportMEL       = "PreExportMEL";
  public static final String aCleanUpNamespace   = "CleanUpNamespace";
  
  public static final String aExportHistory      = "ExportHistory";
  public static final String aExportChannels     = "ExportChannels";
  public static final String aExportExpressions  = "ExportExpressions";
  public static final String aExportConstraints  = "ExportConstraints";

  private static final long serialVersionUID = 1647951829960892221L;
}
