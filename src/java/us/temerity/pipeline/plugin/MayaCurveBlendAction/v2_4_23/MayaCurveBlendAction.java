package us.temerity.pipeline.plugin.MayaCurveBlendAction.v2_4_23;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   C U R V E   B L E N D   A C T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Combine two or more maya scenes containing animation curves. <p>
 * 
 * The most likely source of these curves would be the MayaCurveExport Action, though they 
 * could come from any Action which exports Maya animCurve nodes. <p>
 * 
 * The Action works by copying keyframe information from each source curve onto a newly 
 * created curve.  If different scenes define curves with the same name, but different types
 * (like AnimCurveTL vs AnimCurveTA) the results will be unpredictable.  While tangency 
 * information is technically being preserved, this Action will work most correctly in 
 * situations where animation has been baked to a key on every frame. <p>
 * 
 * The Action can check for overlapping curves and throw an error if it detects two sources 
 * trying to write to the same frame.  If overlap checking is not turned on, then the source 
 * with the higher order (which will be written later) will end up setting the keyframe value 
 * at that time.  For the sake of predictable outcomes, there should only be one source 
 * defining keyframes for a given range of time.  This action can also merge two or more 
 * sources which define keyframes for the same range of times, but on different controls.
 *
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Allow Overlap <BR>
 *   <DIV style="margin-left: 40px;">
 *     Should two scenes be allowed to define a key frame for the same frame on the same 
 *     curve.
 *   </DIV> <BR>
 * </DIV> <P> 
 *   
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     The order to apply the curves in, with the lowest order being applied first.  This 
 *     means that if allow overlap is turned off, the scenes with a higher order will 
 *     potentially override keys in scenes with lower orders.  Multiple scenes can have the
 *     same order, but no guarantees are made about the order in how those scenes will be 
 *     applied.
 *   </DIV> <BR>
 * </DIV> <P>
 */
public 
class MayaCurveBlendAction
  extends MayaActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public
  MayaCurveBlendAction() 
  {
    super("MayaCurveBlend", new VersionID("2.4.23"), "Temerity",
          "Combines multiple maya curve scenes in to a single scene made up of all the " +
          "animation.");
    
    addInitalMELParam();
    addAnimMELParam();
    addFinalMELParam();
    
    {
      ActionParam param = 
        new BooleanActionParam
        (aAllowOverlap,
         "Should the action allow two source files to set key frame value for the same node.",
         false);
      addSingleParam(param);
    }
    
    addUnitsParams();
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aAllowOverlap);
      layout.addSeparator();
      addUnitsParamsToLayout(layout);
      layout.addSeparator();
      layout.addEntry(aInitialMEL);
      layout.addEntry(aAnimMEL);
      layout.addEntry(aFinalMEL);
      
      setSingleLayout(layout);
    }
    {
      LinkedList<String> layout = new LinkedList<String>();
      layout.add(aOrder);

      setSourceLayout(layout);
    }
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   B A S E   A C T I O N   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this action support per-source parameters?  
   */ 
  @Override
  public boolean 
  supportsSourceParams()
  {
    return true;
  }
  
  /**
   * Get an initial set of action parameters associated with an upstream node. 
   */ 
  @Override
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();

    {
      ActionParam param = 
        new IntegerActionParam
        (aOrder,
         "The order in which the curves should be merged.", 
         null);
      params.put(param.getName(), param);
    }
    
    return params;
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
    /* the target Maya scene */
    Path targetScene = getMayaSceneTargetPath(agenda);
    String sceneType = getMayaSceneType(agenda);

    MappedSet<Integer, Path> sourceScenes = new MappedSet<Integer, Path>();
    for (String sourceName : agenda.getSourceNames()) {
      if (hasSourceParams(sourceName)) {
        int order = getSourceIntegerParamValue(sourceName, aOrder);
        
        FileSeq sSeq = agenda.getPrimarySource(sourceName);
        String suffix = sSeq.getFilePattern().getSuffix();
        if (suffix == null || !(suffix.equals("ma") || suffix.equals("mb")))
          throw new PipelineException
            ("Each source specified with an order parameter must be a maya scene file.");
        Path filePath = getWorkingNodeFilePath(agenda, sourceName, sSeq);

        sourceScenes.put(order, filePath);
      }
    }
    
    boolean allowOverlap = getSingleBooleanParamValue(aAllowOverlap);
    
    /* create a temporary MEL and python script file */
    File melScript = createTemp(agenda, "mel");
    File pythonScript = createTemp(agenda, "py");
    try {      
      FileWriter out = new FileWriter(melScript);
      
      /* rename the current scene as the output scene */ 
      out.write("// SCENE SETUP\n" + 
                "file -rename \"" + targetScene + "\";\n" + 
                "file -type \"" + sceneType + "\";\n\n");
      
      out.write(genUnitsMEL());
      
      writeInitialMEL(agenda, out);
      
      int i = 0;
      for (Entry<Integer, TreeSet<Path>> entry : sourceScenes.entrySet()) {
        for (Path p : entry.getValue()) {
          String namespace = "curves" + pad(i);
          out.write(
            "file -reference -namespace \"" + namespace + "\" \"" + p.toString() + "\";\n");
          i++;
        }
      }

      out.write
        ("python(\"source = open('" + new Path(pythonScript).toString() + "', 'r')\");\n" + 
         "python(\"exec source\");\n\n" +
         "python(\"source.close()\");\n\n");
      
      writeAnimMEL(agenda, out);
      
      out.write("string $refFiles[] = `file -q -r`;");
      out.write("for ($file in $refFiles){");
      out.write("   file -rr -f $file;");
      out.write("}\n");
      
      /* save the file */ 
      out.write("// SAVE\n" + 
                "print \"Saving Generated Scene: " + targetScene + "\\n\";\n" + 
                "file -save;\n\n");
      
      writeFinalMEL(agenda, out);

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary MEL script file (" + melScript + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    
    
    try {      
      FileWriter out = new FileWriter(pythonScript);
      
      out.write(
        "import maya.cmds as cmds\n" + 
        "import maya.OpenMaya as om\n" + 
        "import maya.OpenMayaAnim as oma\n" + 
        "\n" + 
        "def stripNamespace(name):\n" + 
        "  all = list()\n" + 
        "  split1 = name.split('|')\n" + 
        "  for piece in split1:\n" + 
        "    split2 = piece.split(':')\n" + 
        "    all.append(split2[len(split2) - 1])\n" + 
        "  return '|'.join(all)\n" + 
        "\n" + 
        "def createAnimCurve(name, type):\n" + 
        "  cmds.createNode(type, n=name)\n" + 
        "  cmds.select(name, r=True)\n" + 
        "  sel = om.MSelectionList()\n" + 
        "  om.MGlobal.getActiveSelectionList(sel)\n" + 
        "  object = om.MObject()\n" + 
        "  sel.getDependNode(0, object)\n" + 
        "  return oma.MFnAnimCurve(object)\n" + 
        "  \n" + 
        "def getAnimCurve(name):\n" + 
        "  cmds.select(name, r=True)\n" + 
        "  sel = om.MSelectionList()\n" + 
        "  om.MGlobal.getActiveSelectionList(sel)\n" + 
        "  object = om.MObject()\n" + 
        "  sel.getDependNode(0, object)\n" + 
        "  return oma.MFnAnimCurve(object)\n" + 
        "\n");
      out.write(
        "def arrayName(name, attr, num):\n" + 
        "  return name + '.' + attr + '[' + str(num) + ']'\n\n");
      out.write(
        "\n" + 
        "\n" + 
        "allowOverwrite = " + (allowOverlap ? "True" : "False") + "\n" + 
        "\n" + 
        "scriptUtil = om.MScriptUtil()\n" + 
        "\n" + 
        "namespaces = cmds.namespaceInfo(lon=True)\n" + 
        "namespaces.remove('UI')\n" + 
        "namespaces.remove('shared')\n" + 
        "for namespace in namespaces:\n" + 
        "  animNodes = cmds.ls(namespace + ':*', type='animCurve')\n" + 
        "  for animNode in animNodes:\n" + 
        "    nodeType = cmds.nodeType(animNode)\n" + 
        "    finalNode = stripNamespace(animNode)\n" + 
        "    \n" + 
        "    newCurve = None\n" + 
        "    if not cmds.objExists(finalNode):\n" + 
        "      newCurve = createAnimCurve(finalNode, nodeType)\n" + 
        "    else:\n" + 
        "      newCurve = getAnimCurve(finalNode)\n" + 
        "    sourceCurve = getAnimCurve(animNode)\n" + 
        "    for index in range(0, sourceCurve.numKeys()):\n" + 
        "      time = sourceCurve.time(index)\n" + 
        "      value = sourceCurve.value(index)\n" + 
        "      inTan = sourceCurve.inTangentType(index)\n" + 
        "      outTan = sourceCurve.outTangentType(index)\n" + 
        "      if not allowOverwrite:\n" + 
        "        pointer = scriptUtil.asUintPtr()\n" + 
        "        if newCurve.find(time, pointer):\n" + 
        "          raise Exception('The Node (' + finalNode + ') at frame (' + " +
        "str(time.asUnits(om.MTime.uiUnit())) + ') has more than one curve specified for " +
        "it and allow Overwrite is set to false')\n" + 
        "      newCurve.addKey(time, value, inTan, outTan)\n");
      
      out.write(
        "node = cmds.createNode('transform', name='curveInfo')\n" + 
        "cmds.select(node, r=True)\n" + 
        "cmds.addAttr(dataType='string', multi=True, longName='curveName', " +
          "shortName='cn')\n" + 
        "cmds.addAttr(dataType='string', multi=True, longName='attrName', shortName='an')\n" + 
        "\n" +
        "\n" + 
        "newInfo = dict()\n" + 
        "curveInfos = cmds.ls('*:curveInfo')\n" + 
        "for curveInfo in curveInfos:\n" + 
        "  size = cmds.getAttr(curveInfo + '.curveName', size=True);\n" + 
        "  for i in range(0, size):\n" + 
        "    cName = cmds.getAttr(arrayName(curveInfo, 'curveName', i))\n" + 
        "    aName = cmds.getAttr(arrayName(curveInfo, 'attrName', i))\n" + 
        "    newInfo[aName] = cName\n" + 
        "\n" + 
        "num = 0\n" + 
        "for aName in newInfo.keys():\n" + 
        "  cmds.setAttr(arrayName(node, 'attrName', num), aName, type='string')\n" + 
        "  cmds.setAttr(arrayName(node, 'curveName', num), newInfo[aName], type='string')\n" + 
        "  num = num + 1\n"); 
      
      out.close();

    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary python script file (" + pythonScript + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    File launchScript = createTemp(agenda, "py");
    try {      
      FileWriter out = new FileWriter(launchScript);
      out.write(getMayaPythonLaunchHeader()); 

      out.write(createMayaPythonLauncher(null, melScript));

      out.close();
    }
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to write temporary python script file (" + launchScript + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
      
    return createPythonSubProcess(agenda, launchScript, outFile, errFile); 
  }
  
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a four digit padding string for a reference number.
   */ 
  private String 
  pad
  (
   int num
  )
    throws PipelineException
  {
    String parse = String.valueOf(num);
    int length = parse.length();
    if (length > 4)
      throw new PipelineException("Cannot create references numbers over 9999");
    for (int i = length; i < 4; i++) 
      parse = "0" + parse;
    return parse;
  }
  
  private static final String 
  getMayaPythonLaunchHeader() 
  {
    return 
      ("import subprocess;\n" +
       "import sys;\n\n" +
       "def launch(program, args):\n" +
       "  a = [program] + args\n" +
       "  print('RUNNING: ' + ' '.join(a))\n" +
       "  sys.stdout.flush()\n" + 
       "  result = subprocess.call(a)\n" +
       "  if result != 0 and result != -1073741819:\n" +
       "    sys.exit('  FAILED: Exit Code = ' + str(result));\n\n");
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -5899220413928271652L;

  public static final String aOrder = "Order";
  public static final String aAllowOverlap = "AllowOverlap";
}
