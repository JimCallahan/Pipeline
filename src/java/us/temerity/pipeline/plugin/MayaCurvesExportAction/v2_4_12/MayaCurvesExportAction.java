package us.temerity.pipeline.plugin.MayaCurvesExportAction.v2_4_12;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   C U R V E S   E X P O R T   A C T I O N                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Action that exports animation data as animation curves from a maya scene.
 * <p>
 * This action can act in one of two modes, depending on how its file sequences are 
 * configured.  If the primary file sequence is a Maya scene, then the action will select all
 * the objects in the set defined by the ExportSuffix parameter and export the animation from
 * those objects.  If the primary file sequence does not have a suffix, then the action will 
 * look at the secondary file sequences.  A selection set name will be built by combining the
 * Export Prefix (optional) + Prefix name of the secondary sequence + Export Suffix.  This
 * allows multiple curve files to exported from a single invocation of maya.
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The Maya scene containing the shaders to be exported.
 *   </DIV> <BR>
 *     
 *   Export Suffix<BR>
 *   <DIV style="margin-left: 40px;">
 *     The suffix of the set that is going to have its animation exported.  If this action is 
 *     operating the single file mode, then this is the entire name of the set.  Otherwise it
 *     will be combined with the prefix name of the secondary sequence and potentially the
 *     Export Prefix.  
 *   </DIV> <BR>
 *   
 *   Export Prefix<BR>
 *   <DIV style="margin-left: 40px;">
 *     An optional prefix for the set that is going to have its animation exported.  This
 *     parameter is only read in the case where multiple animation files are being exported.  
 *   </DIV> <BR>
 *     
 *   Bake Animation <BR>
 *   <DIV style="margin-left: 40px;">
 *     Should the animation be baked before it is exported.
 *   </DIV> <BR>
 *   
 *   Selected Time Range<BR>
 *   <DIV style="margin-left: 40px;">
 *     Should the animation only be baked over the time range of the selected objects before it
 *     is exported.  If this is set to (no), then it will be baked over a frame range for
 *     everything in the scene.
 *   </DIV> <BR>
 *   
 *   Alternative Bake MEL<BR>
 *   <DIV style="margin-left: 40px;">
 *     An alternative MEL snippet to do the animation baking.  This code will be
 *     inserted directly into the MEL script generated by this Action.  The variable
 *     $export exists in the script and contains the name of the set being used to
 *     export the animation.  The value of $export should not be changed.
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
 *     A MEL snippet that will get pasted into the MEL script right before the
 *     export happens.  Use this to modify what is selected to change what is
 *     getting exported.
 * </DIV>  
 */
public 
class MayaCurvesExportAction
  extends MayaActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaCurvesExportAction()
  {
    super("MayaCurvesExport", new VersionID("2.4.12"), "Temerity",
      "An Action to export animation curves from an animated scene.");
    
    addMayaSceneParam();
    
    {
      ActionParam param = 
	new StringActionParam
	(aExportPrefix, 
	 "The prefix that will be prepended to all export sets.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new StringActionParam
        (aExportSuffix, 
         "The suffix that will be appended to all export sets.", 
         "SELECT"); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aBakeAnimation,
	 "Should the animation be baked before it is exported.", 
	 false); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aSelectedTimeRange,
	 "Should the animation only be baked over the time range of the selected objects " + 
	 "before it is exported.  If this is set to (no), then it will be baked over a " + 
	 "frame range for everything in the scene.", 
	 false); 
      addSingleParam(param);
    }

    
    {
      ActionParam param = 
	new LinkActionParam
	(aAlternativeBakeMEL,
	 "An alternative MEL snippet to do the animation baking.  This code will be " +
	 "inserted directly into the MEL script generated by this Action.  The variable " +
	 "$export exists in the script and contains the name of the set being used to " +
	 "export the animation.  The value of $export should not be changed.", 
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
    
    addInitalMELParam();
    addFinalMELParam();
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addEntry(aExportSuffix);
      layout.addEntry(aExportPrefix);
      layout.addEntry(aCleanUpNamespace);
      layout.addSeparator();
      layout.addEntry(aBakeAnimation);
      layout.addEntry(aSelectedTimeRange);
      layout.addEntry(aAlternativeBakeMEL);
      layout.addSeparator();
      layout.addEntry(aInitialMEL);
      layout.addEntry(aPreExportMEL);
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
  @Override
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
    Path mayaSourceScene = getMayaSceneSourcePath(aMayaScene, agenda);
    if (mayaSourceScene == null)
      throw new PipelineException
        ("There was no value given for the Maya Scene parameter, which is required " +
         "for this action.");
    
    /* the mel script path*/
    Path newSceneMEL = getMelScriptSourcePath(aNewSceneMEL, agenda);
    Path bakeMEL = getMelScriptSourcePath(aAlternativeBakeMEL, agenda);
    
    boolean bakeAnim = getSingleBooleanParamValue(aBakeAnimation);
    boolean selectedTime = getSingleBooleanParamValue(aSelectedTimeRange);
    
    boolean cleanUp = getSingleBooleanParamValue(aCleanUpNamespace);
    
    String exportSuffix = getSingleStringParamValue(aExportSuffix, false);
    
    pToExport = new TreeMap<String, Path>();
    pToExportSceneType = new TreeMap<String, String>();
    pLocalScenes = new TreeMap<String, Path>();
    pSuffix = new TreeMap<String, String>();

    /* Two cases here. */
    boolean secs = false;
    String priSuffix = agenda.getPrimaryTarget().getFilePattern().getSuffix();
    if (priSuffix == null) {
      secs = true;
      String exportPrefix = getSingleStringParamValue(aExportPrefix);

      for (FileSeq sec : agenda.getSecondaryTargets() ) {
        Path p = new Path(agenda.getTargetPath(), sec.getPath(0));
        String secSuffix = sec.getFilePattern().getSuffix();
        String sceneType = null;
        if(secSuffix == null) 
          throw new PipelineException
            ("Cannot determine the Maya scene file type without a filename suffix for " +
             "secondary sequence (" + sec +")!");

        if(secSuffix.equals("ma"))
          sceneType = "mayaAscii";
        else if(secSuffix.equals("mb"))
          sceneType = "mayaBinary"; 
        else 
          throw new PipelineException
            ("Unknown Maya filename suffix (" + secSuffix + ") found!");
        
        String exportSet = sec.getFilePattern().getPrefix() + exportSuffix;
        if (exportPrefix != null)
          exportSet = exportPrefix + exportSet;
        
        pToExport.put(exportSet, p);
        pToExportSceneType.put(exportSet, sceneType);
        pSuffix.put(exportSet, secSuffix);
      }
    }
    else if (priSuffix.equals("ma") || priSuffix.equals("mb")) {
      Path targetScene = getMayaSceneTargetPath(agenda);
      String sceneType = getMayaSceneType(agenda);
      
      pToExport.put(exportSuffix, targetScene);
      pToExportSceneType.put(exportSuffix, sceneType);
      pSuffix.put(exportSuffix, priSuffix);
    }
    else
      throw new PipelineException
        ("The primary file sequence must either have a (null) suffix, in the multiple file " +
         "case, or an (ma) or (mb) suffix in the single file case.");
    
    
    File python = createTemp(agenda, "py");
    try {
      FileWriter out = new FileWriter(python);
      
      out.write
        ("import maya.cmds as cmd\n");
      
      out.write
        ("def stripNamespace(name):\n" + 
         "  all = list()\n" + 
         "  split1 = name.split('|')\n" + 
         "  for piece in split1:\n" + 
         "    split2 = piece.split(':')\n" + 
         "    all.append(split2[len(split2) - 1])\n" + 
         "  return '|'.join(all)\n\n");
      
      out.write
        ("def arrayName(name, attr, num):\n" + 
         "  return name + '.' + attr + '[' + str(num) + ']'\n\n");
      
      out.write
        ("node = cmd.createNode('transform', name='curveInfo')\n" + 
         "cmd.select(node, r=True)\n" + 
         "cmd.addAttr(dataType='string', multi=True, longName='curveName', shortName='cn')\n" + 
         "cmd.addAttr(dataType='string', multi=True, longName='attrName', shortName='an')\n" + 
         "\n" + 
         "filename = cmd.file(q=True, sn=True)\n" + 
         "input = open(filename, 'rU')\n" + 
         "num = 0\n" + 
         "for line in input:\n" + 
         "  line = line.lstrip()\n" + 
         "  line = line.rstrip()\n" + 
         "\n" + 
         "  if line.startswith(\"connectAttr\"):\n" + 
         "    parts = line.split()\n" + 
         "    attr = parts[2]\n" + 
         "    curve = parts[1]\n" + 
         "    attr = attr.strip(';\"')\n" + 
         "    curve = curve.strip(';\"')\n" + 
         "    attr = stripNamespace(attr)\n" + 
         "    curve = stripNamespace(curve)\n" + 
         "    cmd.setAttr(arrayName(node, \"attrName\", num), attr, type=\"string\")\n" + 
         "    cmd.setAttr(arrayName(node, \"curveName\", num), curve, type=\"string\")\n" + 
         "    num = num + 1\n\n");
      
      out.close();
    }
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write temporary python script file (" + python + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    /* create a temporary MEL script used to export the curves */ 
    File script = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(script);

      writeInitialMEL(agenda, out);
      
      if (bakeAnim) {
        if (bakeMEL == null) {
          out.write("select -cl;\n");
          for (String set : pToExport.keySet())
            out.write("select -r " + set +";\n");
          if (selectedTime)
            out.write
            ("//Baking over selected frame range\n" +
             "selectKey -time \":\" `ls -sl`;\n" + 
              "float $first = `findKeyframe -w \"first\"`;\n" + 
            "float $last = `findKeyframe -w \"last\"`;\n");
          else
            out.write
            ("//Baking over whole frame range\n" +
              "selectKey -time \":\" `ls`;\n" + 
              "float $first = `findKeyframe -w \"first\"`;\n" + 
            "float $last = `findKeyframe -w \"last\"`;\n");
          out.write
          ("if ($first != $last)\n" + 
           "{\n" +
           "  select -cl;\n");
          for (String set : pToExport.keySet())
            out.write("  select -r " + set +";\n");
          out.write
          ("  if (size(`ls -sl`) > 0)\n" +
           "    bakeResults -t ($first + \":\" + $last) -simulation true `ls -sl`;\n" +
          "}\n\n");
        }
	else {
	  String melSnippet = null;
	  try {
	    melSnippet = getMelSnippet(agenda, aAlternativeBakeMEL);
	  }
	  catch (IOException ex) {
	    throw new PipelineException
	    ("Unable to read the export mod mel file for Job " + 
	     "(" + agenda.getJobID() + ")!\n" +
	     ex.getMessage());
	  }
	  if (melSnippet != null) {
	    out.write(melSnippet);
	  }
	}
      }
        
      for (Entry<String, String> entry : pToExportSceneType.entrySet()) {
        String exportSet = entry.getKey();
        String sceneType = entry.getValue();
        String suffix = pSuffix.get(exportSet);
        
        File tempFile = createTemp(agenda, suffix);
        Path newScene = new Path(tempFile);
        pLocalScenes.put(exportSet, newScene);
        
        out.write("{\n");

        out.write("string $export = \"" + exportSet + "\";\n");

      
        out.write("select -r $export;\n");
      
        Path actualTarget = newScene;
      
        out.write
          ("if (catch(`file -f " +
            "-type \"" + sceneType + "\" " +
            "-eas \"" + actualTarget + "\"`))\n" +
            "{\n" + 
            "  file -new;\n" + 
            "  file -rename \"" + actualTarget + "\";\n" + 
            "  file -type \"" + sceneType + "\";\n" + 
            "  file -save;\n" +
          "}\n"); 

      
        writeFinalMEL(agenda, out);
        out.write("}\n");
      }
      
      for (Entry<String, Path> entry : pToExport.entrySet()) {
        String exportSet = entry.getKey();
        Path newScene = pLocalScenes.get(exportSet);
        Path targetScene = entry.getValue();
        
        out.write("{\n");
	out.write("// NEW SCENE SCRIPT\n" +
	          "file -open -f \"" + newScene + "\";\n");
	out.write
	  ("python(\"source = open('" + escPath(escPath(python.toString())) + "', 'rU')\");\n" + 
	   "python(\"exec source\");\n\n");
	if (cleanUp)
	  out.write
	    ("{\n" + 
	     "  string $ns;\n" + 
	     "  string $spaces[] = `namespaceInfo -lon`;\n" + 
	     "  while(size($spaces) > 2) {\n" + 
	     "    for ($ns in $spaces) {\n" + 
	     "      if ($ns != \"UI\") {\n" + 
	     "        if ($ns != \"shared\") {\n" + 
	     "          print(\"clearing namespace: \" + $ns + \"\\n\");\n" + 
	     "          catch(`namespace -f -mv $ns \":\"`);\n" + 
	     "          namespace -rm $ns;\n" + 
	     "        }\n" + 
	     "      }\n" + 
	     "    }\n" + 
	     "     $spaces = `namespaceInfo -lon`; \n" + 
	     "  }\n" + 
	    "}\n\n");
	if (newSceneMEL != null)
	  out.write("print \"New-Scene Script: " + newSceneMEL+ "\\n\";\n" +
	  	    "source \"" + newSceneMEL + "\";\n");
	out.write("file -rename \"" + targetScene + "\";\n" + 
		  "file -save;\n");
	out.write("}\n");
      }

      if (secs) {
        Path targetScene = getPrimaryTargetPath(agenda, new TreeSet<String>(), "empty file");
        out.write(
          "{\n" + 
          "  int $fid = `fopen \"" + targetScene +"\"`;\n" + 
          "  fclose $fid;\n" + 
          "}\n");

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

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6179776373413518227L;
  
  public static final String aExportPrefix       = "ExportPrefix";
  public static final String aExportSuffix       = "ExportSuffix";
  public static final String aNewSceneMEL        = "NewSceneMEL";
  public static final String aPreExportMEL       = "PreExportMEL";
  public static final String aCleanUpNamespace   = "CleanUpNamespace";
  public static final String aBakeAnimation      = "BakeAnimation";
  public static final String aSelectedTimeRange  = "SelectedTimeRange";
  public static final String aAlternativeBakeMEL = "AlternativeBakeMEL";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private TreeMap<String, Path> pToExport;
  private TreeMap<String, String> pToExportSceneType;
  private TreeMap<String, Path> pLocalScenes;
  private TreeMap<String, String> pSuffix;
}