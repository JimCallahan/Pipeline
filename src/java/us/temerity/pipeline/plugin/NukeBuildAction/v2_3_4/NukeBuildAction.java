package us.temerity.pipeline.plugin.NukeBuildAction.v2_3_4;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   B U I L D   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a new Nuke comp from component image sources.
 * 
 *  TODO:  this doesn't handle secondary file sequences, which seems like an important case.
 *  Per jesse - AfterFXBuildAction doesn't either, so this is o.k. for now.<P> 
 **/
public 
class NukeBuildAction
extends CompositeActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  NukeBuildAction()
  {
    super("NukeBuild", new VersionID("2.3.4"), "Temerity",
    "Generates a new Nuke comp from component image sources.");

    addCompFrameRateParam();
    addCompHeightParam();
    addCompWidthParam();
    addCompPixelRatioParam();

    addSingleParam(new BooleanActionParam(aCompDoMerges,
      		   "Whether or not to build Merge nodes.",
      		   true)); 
    /* 
     * TODO:  what is supported?
     */
    //addSupport(OsType.MacOS);
    //addSupport(OsType.Windows);

    LayoutGroup layout = new LayoutGroup(true);
    layout.addEntry(aCompDoMerges);
    layout.addEntry(aCompHeight);
    layout.addEntry(aCompWidth);
    layout.addEntry(aCompPixelRatio);
    layout.addEntry(aCompFrameRate);
    setSingleLayout(layout);

    LinkedList<String> sourceLayout = new LinkedList<String>();
    sourceLayout.add(aLayer);
    sourceLayout.add(aPass);
    sourceLayout.add(aOrder);
    sourceLayout.add(aBlendMode);
    sourceLayout.add(aAlphaMode);
    setSourceLayout(sourceLayout);

    underDevelopment();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   B A S E   A C T I O N   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does this action support per-source parameters?  
   */ 
  public boolean 
  supportsSourceParams()
  {
    return true;
  }

  /**
   * Get an initial set of action parameters associated with an upstream node. 
   */ 
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();

    {
      /* NOTE:  these are in the order in which they appear in the Nuke interface */
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aNormal);	
      choices.add(aDisjointOver);
      choices.add(aConjointOver);
      choices.add(aMatte);
      choices.add(aCopy);
      choices.add(aUnder);
      choices.add(aIn);
      choices.add(aOut);
      choices.add(aMask);
      choices.add(aStencilAlpha);
      choices.add(aXor);
      choices.add(aAtop);
      choices.add(aScreen);
      choices.add(aAdd);
      choices.add(aAverage);
      choices.add(aMultiply);
      choices.add(aDivide);
      choices.add(aOverlay);
      choices.add(aMin);
      choices.add(aMax);
      choices.add(aColorDodge);
      choices.add(aColorBurn);
      choices.add(aHardLight);
      choices.add(aSoftLight);
      choices.add(aMinus);
      choices.add(aFrom);
      choices.add(aDifference);
      choices.add(aExclusion);

      ActionParam param = 
	new EnumActionParam
	(aBlendMode, 
	  "What blend mode should this layer be set to.",
	  aNormal,
	  choices);
      params.put(param.getName(), param);
    }

    addSourceLayerParam(params);
    addSourcePassParam(params);
    addSourceOrderParam(params);

    /* These are in AfterFXBuildAction, but are not required by Nuke.
     * The functionality could be duplicated by building additonal nodes, but it's 
     * questionable whether this is of any real use.  For now, assuming it's better 
     * to have a simpler interface.
     * 
     *addSourceFrameRateParam(params);
     *addSourcePixelRatioParam(params);
     */

    addSourceAlphaModeParam(params);

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
   * 
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

    /* create a temporary TCL script file to feed to Nuke*/ 
    File nukeScript = createTemp(agenda, "tcl");
    try {      
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(nukeScript)));

      Double compFrameRate = getSingleDoubleParamValue(aCompFrameRate, new Range<Double>(1., 9999.));
      out.write("knob root.fps "+compFrameRate+"\n");

      /* Calculate the offsets for each Order
       */
      TreeMap<Integer, Double> orderStart = new TreeMap<Integer, Double>();

      MappedLinkedList<Integer, Double> orderLength = new MappedLinkedList<Integer, Double>();
      for (String sourceName : agenda.getSourceNames()) {
	Integer order = (Integer) getSourceParamValue(sourceName, aOrder);
	FrameRange range = agenda.getPrimarySource(sourceName).getFrameRange();
	if (range.isSingle())
	  orderLength.put(order, 1.0);
	else {
	  double length = (range.getEnd() - range.getStart() + 1);
	  orderLength.put(order, length);
	}
      }
      double totalLength = 0;
      for (int order : orderLength.keySet()) {
	orderStart.put(order, totalLength);
	double length = (new TreeSet<Double>(orderLength.get(order))).last();
	totalLength += length;
      }
      out.write("# ORDERLENGTH = " + orderLength.toString() + "\n");
      out.write("# ORDERSTART = " + orderStart.toString() + "\n");


      /* Generate the commands to build the nuke node graph.  There is probably a more
       * elegant way of doing this, involving a recursive depth-first traversal or 
       * something - but for now it's just a bunch of nested loops.
       */
      
      Boolean compDoMerges = getSingleBooleanParamValue(aCompDoMerges);

      // Iterate over the passes
      MappedLinkedList<Integer, String> passList = new MappedLinkedList<Integer, String>();
      for (String sourceName : agenda.getSourceNames()) {
	Integer pass = (Integer) getSourceParamValue(sourceName, aPass);
	passList.put(pass, sourceName);
      }
      boolean firstPass = true;
      String lastBlendMode = null;
      for (Integer pass : passList.keySet()) {
	String passCompName = "Pass" + pass;

	out.write("# PASS = " + passCompName + "\n");

	// Iterate over the layers in this pass
	MappedLinkedList<Integer, String> layerList = new MappedLinkedList<Integer, String>();
	LinkedList<String> layers = passList.get(pass);
	for (String sourceName : layers) {
	  Integer layer = (Integer) getSourceParamValue(sourceName, aLayer);
	  layerList.put(layer, sourceName);
	}	
	boolean firstLayer = true;
	for (int layer : layerList.keySet()) {
	  out.write("# LAYER = " + layer + "\n");

	  // Iterate over the sources in this layer
	  boolean firstSource = true;
	  for (String sourceName : layerList.get(layer)) {
	    lastBlendMode = (String) getSourceParamValue(sourceName, aBlendMode);
	    writeSourceToScript(agenda, out, orderStart, sourceName);
	    out.write("set thisSourceRoot [selected_node]\n");

	    // If there were previous sources, connect this source to them
	    if (compDoMerges && firstSource == false) {
	      blendNodes(agenda, out, "$thisSourceRoot", "$lastSourceRoot", lastBlendMode, "sourceMerge");
	      out.write("set lastSourceRoot [selected_node]\n");
	    } else {
	      out.write("set lastSourceRoot $thisSourceRoot\n");
	      firstSource = false;
	    }
	  }

	  // If there were previous layers, connect this layer to them
	  if (compDoMerges && firstLayer == false) {
	    blendNodes(agenda, out, "$lastSourceRoot", "$lastLayerRoot", lastBlendMode, "layerMerge"+layer);
	    out.write("set lastLayerRoot [selected_node]\n");
	  } else {
	    out.write("set lastLayerRoot $lastSourceRoot\n");
	    firstLayer = false;
	  }
	}

	// If there were previous passes, connect this pass to them
	if (compDoMerges && firstPass == false) {
	  blendNodes(agenda, out, "$lastLayerRoot", "$lastPassRoot", lastBlendMode, "passMerge"+pass);
	  out.write("set lastPassRoot [selected_node]\n");
	} else {
	  out.write("set lastPassRoot $lastLayerRoot\n");
	  firstPass = false;
	}
      }

      // Save the Nuke script
      Path targetPath = getPrimaryTargetPath(agenda, "nk", "Nuke comp file");
      out.write("script_save "+targetPath+"\n");

      // Close the temp TCL script
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
      ("Unable to write temporary TCL script file (" + nukeScript + ") for Job " + 
	"(" + agenda.getJobID() + ")!\n" +
	ex.getMessage());
    }

    /* Create a temporary Python script to run Nuke piping the script to STDIN 
     * NOTE:  we're passing a "-d :0" argument to Nuke, which defines the X Display.
     * This shouldn't be necessary in Terminal Mode, but for some reason, Nuke spits
     * out a 'can't open DISPLAY ""' error if we don't.
     * 
     * TODO:  turns out that the Nuke "save_script" function is trying to open the
     * X display, even in terminal mode.  This causes this action to barf on machines
     * with no X servers (um, like anything in a renderfarm).  Need to figure out why
     * the heck Nuke is doing this, and make it stop.
     */ 
    Map<String, String> env = agenda.getEnvironment();
    File pyScript = createTemp(agenda, "py");
    try {
      FileWriter out = new FileWriter(pyScript); 

      String nukeApp = NukeActionUtils.getNukeProgram(env); 
      out.write
      ("import subprocess\n" + 
	"nukeScript = open('" + nukeScript + "', 'r')\n" + 
	"p = subprocess.Popen(['" + nukeApp + "', '-t', '-d :0'], stdin=nukeScript)\n" + 
      "p.communicate()\n");
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
      ("Unable to write temporary Python script file (" + pyScript + ") for Job " + 
	"(" + agenda.getJobID() + ")!\n" +
	ex.getMessage());
    }

    {
      ArrayList<String> args = new ArrayList<String>();
      String python = PythonActionUtils.getPythonProgram(env);
      args.add(pyScript.getPath());

      return createSubProcess(agenda, python, args, outFile, errFile);
    }
  }

  /* blendNodes - create a Merge2 node in Nuke to composite the given layers,
   * with the given blendMode as the operation.  
   */
  private void 
  blendNodes
  (
    ActionAgenda agenda, 
    PrintWriter out,
    String fgSource,
    String bgSource,
    String blendMode,
    String name
  )
  throws PipelineException
  {

    String nukeBlendMode = getNukeBlendMode(blendMode);
    if (name == null)
      name = "merge"+fgSource;
    out.write("Merge2 -New name "+name+" operation "+nukeBlendMode+"\n");
    out.write("input "+name+" 0 "+bgSource+"\n");
    out.write("input "+name+" 1 "+fgSource+"\n");
  }

  /* writeSourceToScript - spit out the code to read a source sequence into Nuke,
   * and optionally offset it in time based on the Order parameter.
   */
  private void
  writeSourceToScript
  (
    ActionAgenda agenda, 
    PrintWriter out,
    TreeMap<Integer, Double> orderStart, 
    String sourceName
  )
  throws PipelineException
  {
    FileSeq sSeq = agenda.getPrimarySource(sourceName);

    String alphaMode = (String) getSourceParamValue(sourceName, aAlphaMode);

    int order = (Integer) getSourceParamValue(sourceName, aOrder);
    double offset = orderStart.get(order);

    FilePattern fpat = sSeq.getFilePattern();
    FrameRange range = sSeq.getFrameRange();      
    Path readPath = new Path("WORKING" + (new Path(sourceName)).getParent() + "/" + 
      NukeActionUtils.toNukeFilePattern(fpat).toString());

    String cmd = "Read -New file " + readPath.toOsString();
    if (range != null)
      cmd += " first " + range.getStart() +
      	     " last " + range.getEnd();
    cmd += "\n";
    out.write(cmd);

    if (offset != 0)
      out.write("TimeOffset -New time_offset "+offset+"\n");

    if (alphaMode.equals(aPreMultipled))
      out.write("Unpremult -New \n");

    out.write("Grade -New \n");

    if (alphaMode.equals(aPreMultipled))
      out.write("Premult -New \n");
  }

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /* getNukeBlendMode() - returns a string that Nuke understands as a value for the "operation"
   * knob of a "Merge2" node.
   */
  private static String
  getNukeBlendMode
  (
    String paramName
  )
  {
    if (paramName.equals(aAdd))
      return "plus";
    else if (paramName.equals(aAtop))
      return "atop";
    else if (paramName.equals(aAverage))
      return "average";
    else if (paramName.equals(aColorBurn))
      return "color-burn";
    else if (paramName.equals(aColorDodge))
      return "color-dodge";
    else if (paramName.equals(aConjointOver))
      return "conjoint-over";
    else if (paramName.equals(aCopy))
      return "copy";
    else if (paramName.equals(aDifference))
      return "difference";
    else if (paramName.equals(aDisjointOver))
      return "disjoint-over";
    else if (paramName.equals(aDivide))
      return "divide";
    else if (paramName.equals(aExclusion))
      return "exclusion";
    else if (paramName.equals(aFrom))
      return "from";
    else if (paramName.equals(aHardLight))
      return "hard-light";
    else if (paramName.equals(aIn))
      return "in";
    else if (paramName.equals(aMask))
      return "mask";
    else if (paramName.equals(aMatte))
      return "matte";
    else if (paramName.equals(aMax))
      return "max";
    else if (paramName.equals(aMin))
      return "min";
    else if (paramName.equals(aMinus))
      return "minus";
    else if (paramName.equals(aMultiply))
      return "multiply";
    else if (paramName.equals(aNormal))
      return "over";
    else if (paramName.equals(aOverlay))
      return "overlay";
    else if (paramName.equals(aOut))
      return "out";
    else if (paramName.equals(aScreen))
      return "screen";
    else if (paramName.equals(aSoftLight))
      return "soft-light";
    else if (paramName.equals(aStencilAlpha))
      return "stencil";
    else if (paramName.equals(aUnder))
      return "under";
    else if (paramName.equals(aXor))
      return "xor";
    else
      return null;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /* TODO:  these need to be migrated upward into CompositeActionUtils 
   * Also, the functions "normal", "add", and "stencilAlpha" need to be standardized.
   * Nuke calls them "over", "plus" and "stencil", respectively.  (I'm not 100% sure
   * about stencil, actually.)
   */
  public static final String aAtop = "Atop";
  public static final String aAverage = "Average";
  public static final String aConjointOver = "Conjoint-Over";
  public static final String aCopy = "Copy";
  public static final String aDisjointOver = "Disjoint-Over";
  public static final String aDivide = "Divide";
  public static final String aFrom = "From";
  public static final String aIn = "In";
  public static final String aMask = "Mask";
  public static final String aMatte = "Matte";
  public static final String aMax = "Max";
  public static final String aMin = "Min";
  public static final String aMinus = "Minus";
  public static final String aOut = "Out";
  public static final String aUnder = "Under";
  public static final String aXor = "Xor";  

  public static final String aCompDoMerges = "CompDoMerges";

  private static final long serialVersionUID = 1622629414069115214L;

}
