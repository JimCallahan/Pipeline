package us.temerity.pipeline.plugin.AfterFXBuildAction.v2_2_1;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.plugin.AfterFXActionUtils;
import us.temerity.pipeline.plugin.CommonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   A F T E R   F X   B U I L D   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a new After Effects scene from component image sources. <P> 
 **/
public 
class AfterFXBuildAction
  extends AfterFXActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AfterFXBuildAction()
  {
    super("AfterFXBuild", new VersionID("2.2.1"), "Temerity",
	  "Generates a new After Effects scene from component image sources.");

    {
      ActionParam param = 
  	new StringActionParam
  	(aCompName,
  	 "The name of the After Effects Comp to create.",
  	 "Comp1");
      addSingleParam(param);
    }
    
    addCompFrameRateParam();
    addCompHeightParam();
    addCompWidthParam();
    addCompPixelRatioParam();

    /* 
     * Support for OsX can be added when After Effects CS3 launches, making it possible
     * to run AfterFX as an AppleScript app.  I have not been able to get the Mac version
     * of 7.0 to run scripts from the command line.
     */
    //addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
    removeSupport(OsType.Unix);
    
    LayoutGroup layout = new LayoutGroup(true);
    layout.addEntry(aCompName);
    layout.addEntry(null);
    layout.addEntry(aCompHeight);
    layout.addEntry(aCompWidth);
    layout.addEntry(aCompPixelRatio);
    layout.addEntry(null);
    layout.addEntry(aFrameRate);
    
    LinkedList<String> sourceLayout = new LinkedList<String>();
    sourceLayout.add(aLayer);
    sourceLayout.add(aPass);
    sourceLayout.add(aOrder);
    sourceLayout.add(aBlendMode);
    sourceLayout.add(aFrameRate);
    sourceLayout.add(aPixelRatio);
    sourceLayout.add(aAlphaMode);
    sourceLayout.add(aPreMultColor);
    
    setSourceLayout(sourceLayout);
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
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aAdd);
      choices.add(aAlphaAdd);
      choices.add(aClassicColorBurn);
      choices.add(aClassicColorDodge);
      choices.add(aClassicDifference);
      choices.add(aColor);
      choices.add(aColorBurn);
      choices.add(aColorDodge);
      choices.add(aDancingDissolve);
      choices.add(aDarken);
      choices.add(aDifference);
      choices.add(aDissolve);
      choices.add(aExclusion);
      choices.add(aHardLight);
      choices.add(aHue);
      choices.add(aLighten);
      choices.add(aLinearBurn);
      choices.add(aLinearDodge);
      choices.add(aLinearLight);
      choices.add(aLuminescentPremul);
      choices.add(aLuminosity);
      choices.add(aMultiply);
      choices.add(aNormal);
      choices.add(aOverlay);
      choices.add(aPinLight);
      choices.add(aSaturation);
      choices.add(aScreen);
      choices.add(aSilhouetteAlpha);
      choices.add(aSilhouetteLuma);
      choices.add(aSoftLight);
      choices.add(aStencilAlpha);
      choices.add(aStencilLuma);
      choices.add(aVividLight);
      
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

    addSourceFrameRateParam(params);
    
    addSourcePixelRatioParam(params);
    
    addSourceAlphaModeParam(params);
    addSourcePreMultColorParam(params);
    
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
    ArrayList<String> extensions = new ArrayList<String>();
    // This probably needs to be extended
    {
      extensions.add("jpeg");
      extensions.add("jpg");
      extensions.add("sgi");
      extensions.add("tiff");
      extensions.add("tiff");
      extensions.add("rgb");
      extensions.add("iff");
      extensions.add("png");
      extensions.add("psd");
      extensions.add("tga");
    }
    
    Path targetPath = getAfterFXSceneTargetPath(agenda);
    
    String compName = getSingleStringParamValue(aCompName);
    Double compFrameRate = getSingleDoubleParamValue(aCompFrameRate, new Range<Double>(1., 99.));
    Double compPixelRatio = getSingleDoubleParamValue(aCompPixelRatio, new Range<Double>(.01, 100.));
    Integer compHeight = getSingleIntegerParamValue(aCompHeight, new Range<Integer>(4, 30000));
    Integer compWidth = getSingleIntegerParamValue(aCompWidth, new Range<Integer>(4, 30000));
    double offset= 1/compFrameRate;
        
    /* create a temporary JSX script file */ 
    File script = createTemp(agenda, "jsx");
    try {      
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
      
      out.write("app.exitAfterLaunchAndEval = true;\n" + 
      		"app.beginSuppressDialogs();\n" + 
      		"app.project.close(CloseOptions.DO_NOT_SAVE_CHANGES);\n" +
      		"app.newProject();\n\n");
      
      MappedLinkedList<Integer, String> passList = new MappedLinkedList<Integer, String>();
      for (String sourceName : agenda.getSourceNames()) {
	Integer pass = (Integer) getSourceParamValue(sourceName, aPass);
	passList.put(pass, sourceName);
      }
      
      LinkedList<String> zeroPasses = passList.remove(0);
      
      MappedLinkedList<Integer, Double> orderLength = new MappedLinkedList<Integer, Double>();
      for (String sourceName : agenda.getSourceNames()) {
	Integer order = (Integer) getSourceParamValue(sourceName, aOrder);
	FrameRange range = agenda.getPrimarySource(sourceName).getFrameRange();
	Double frameRate = (Double) getSourceParamValue(sourceName, aFrameRate);
	if (range.isSingle())
	  orderLength.put(order, 1/frameRate);
	else {
	  double length = (range.getEnd() - range.getStart() + 1)/frameRate;
	  orderLength.put(order, length);
	}
      }

      TreeMap<Integer, Double> orderStart = new TreeMap<Integer, Double>();
      
      double totalLength = offset;
      for (int order : orderLength.keySet()) {
	orderStart.put(order, totalLength);
	double length = (new TreeSet<Double>(orderLength.get(order))).last();
	totalLength += length;
      }
      
      out.write("var comp = app.project.items.addComp(\"" + compName + "\", " + 
	          compWidth + ", " + compHeight + ", " + compPixelRatio + ", " + 
	          totalLength + ", " + compFrameRate + ");\n" +
      		"var layers = comp.layers;\n\n");
      
      if (zeroPasses != null) {
	MappedLinkedList<Integer, String> layerList = new MappedLinkedList<Integer, String>();
	for (String sourceName : zeroPasses) {
	  Integer layer = (Integer) getSourceParamValue(sourceName, aLayer);
	  layerList.put(layer, sourceName);
	}
	for (int layer : layerList.keySet()) {
	  for (String sourceName : layerList.get(layer)) {
	    writeSourceToScript(agenda, out, orderStart, sourceName, "layers", false);
	  }
	}
      }
      
      for (Integer pass : passList.keySet())
      {
	String passCompName = "Pass" + pass;
	LinkedList<String> layers = passList.get(pass);

	MappedLinkedList<Integer, String> layerList = new MappedLinkedList<Integer, String>();
	for (String sourceName : layers) {
	  Integer layer = (Integer) getSourceParamValue(sourceName, aLayer);
	  layerList.put(layer, sourceName);
	}
	
	out.write("{\n" +
	          "var passComp = app.project.items.addComp(\"" + passCompName + "\", " + 
	          compWidth + ", " + compHeight + ", " + compPixelRatio + ", " + 
	          totalLength + ", " + compFrameRate + ");\n" +
      		  "var passLayers = passComp.layers;\n\n");

	boolean first = true;
	String compBlendMode = null;
	for (int layer : layerList.keySet()) {
	  for (String sourceName : layerList.get(layer)) {
	    String blendMode = 
	      writeSourceToScript(agenda, out, orderStart, sourceName, "passLayers", first);
	    if (first) {
              compBlendMode = getBlendMode(blendMode);
              first = false;
	    }
	  }
	}
        out.write("  var avlayer = layers.add(passComp);\n" +
		  "  avlayer.startTime = " + offset + ";\n" + 
		  "  avlayer.moveToBeginning();\n" +
		  "  avlayer.blendingMode = BlendingMode." + compBlendMode + ";\n" + 
      		  "}\n");
      }
      
      out.write("var f = new File(\"" + CommonActionUtils.escPath(targetPath.toOsString()) + "\");\n" +
      		"app.project.save(f);\n" +
      		"app.quit();\n");
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary JSX script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-m");
      args.add("-r");
      args.add(script.getAbsolutePath());
      
      return createSubProcess(agenda, "AfterFX.exe", args, outFile, errFile);
    }
  }

  private String 
  writeSourceToScript
  (
    ActionAgenda agenda, 
    PrintWriter out,
    TreeMap<Integer, Double> orderStart, 
    String sourceName,
    String layerName,
    boolean first 
  )
    throws PipelineException
  {
    FileSeq sSeq = agenda.getPrimarySource(sourceName);
    boolean sequence = sSeq.hasFrameNumbers();
    Path spath = getWorkingNodeFilePath(agenda, sourceName, sSeq);

    Double frameRate = (Double) getSourceParamValue(sourceName, aFrameRate);
    Double pixelAspect = (Double) getSourceParamValue(sourceName, aPixelRatio);
    String alphaMode = ((String) getSourceParamValue(sourceName, aAlphaMode)).toUpperCase();
    String preMultColor = (String) getSourceParamValue(sourceName, aPreMultColor);
    String blendMode = (String) getSourceParamValue(sourceName, aBlendMode);

    int order = (Integer) getSourceParamValue(sourceName, aOrder);
    double startTime = orderStart.get(order);
    
    

    out.write("{\n" +
              "  var f = new File(\"" + CommonActionUtils.escPath(spath.toOsString()) + "\");\n" +
              "  var importOptions = new ImportOptions (f);\n" + 
              "  importOptions.importAs = ImportAsType.FOOTAGE;\n");

    if (sequence)
      out.println("  importOptions.sequence = true;");
    else
      out.println("  importOptions.sequence = false;");
    out.write("  var footage = app.project.importFile (importOptions);\n" +
              "  if (!footage.mainSource.isStill)\n" +
              "    footage.mainSource.conformFrameRate = " + frameRate + ";\n" +
              "  footage.pixelAspect = " + pixelAspect + ";\n" +
              "  footage.mainSource.alphaMode = AlphaMode." + alphaMode + ";\n");
    if (preMultColor.equals(aWhite))
      out.println("  footage.mainSource.premulColor = new Array(1,1,1);");
    else
      out.println("  footage.mainSource.premulColor = new Array(0,0,0);");

    if (!first)
      out.write("  var avlayer = " + layerName + ".add(footage);\n" +
    	"  avlayer.startTime = " + startTime + ";\n" + 
    	"  avlayer.moveToBeginning();\n" +
    	"  avlayer.blendingMode = BlendingMode." + getBlendMode(blendMode) + ";\n" + 
            "}\n");
    else {
      out.write("  var avlayer = passLayers.add(footage);\n" +
    	"  avlayer.startTime = " + startTime + ";\n" + 
    	"  avlayer.moveToBeginning();\n" +
    	"  avlayer.blendingMode = BlendingMode.NORMAL;\n" + 
        "}\n");
    }
    return blendMode;
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  
  public static String
  getBlendMode
  (
    String paramName
  )
  {
    if (paramName.equals(aAdd))
      return "ADD";
    else if (paramName.equals(aAlphaAdd))
    	return "ALPHA_ADD";
    else if (paramName.equals(aClassicColorBurn))
  	return "CLASSIC_COLOR_BURN";
    else if (paramName.equals(aClassicColorDodge))
	return "CLASSIC_COLOR_DODGE";
    else if (paramName.equals(aClassicDifference))
	return "CLASSIC_DIFFERENCE";
    else if (paramName.equals(aColor))
	return "COLOR";
    else if (paramName.equals(aColorBurn))
	return "COLOR_BURN";
    else if (paramName.equals(aColorDodge))
	return "COLOR_DODGE";
    else if (paramName.equals(aDancingDissolve))
	return "DANCING_DISSOLVE";
    else if (paramName.equals(aDarken))
	return "DARKEN";
    else if (paramName.equals(aDifference))
	return "DIFFERENCE";
    else if (paramName.equals(aDissolve))
	return "DISSOLVE";
    else if (paramName.equals(aExclusion))
	return "EXCLUSION";
    else if (paramName.equals(aHardLight))
	return "HARD_LIGHT";
    else if (paramName.equals(aHue))
	return "HUE";
    else if (paramName.equals(aLighten))
	return "LIGHTEN";
    else if (paramName.equals(aLinearBurn))
	return "LINEAR_BURN";
    else if (paramName.equals(aLinearDodge))
	return "LINEAR_DODGE";
    else if (paramName.equals(aLinearLight))
	return "LINEAR_LIGHT";
    else if (paramName.equals(aLuminescentPremul))
	return "LUMINESCENT_PREMUL";
    else if (paramName.equals(aLuminosity))
	return "LUMINOSITY";
    else if (paramName.equals(aMultiply))
	return "MULTIPLY";
    else if (paramName.equals(aNormal))
	return "NORMAL";
    else if (paramName.equals(aOverlay))
	return "OVERLAY";
    else if (paramName.equals(aPinLight))
	return "PIN_LIGHT";
    else if (paramName.equals(aSaturation))
	return "SATURATION";
  else if (paramName.equals(aScreen))
	return "SCREEN";
  else if (paramName.equals(aSilhouetteAlpha))
	return "SILHOUETTE_ALPHA";
  else if (paramName.equals(aSilhouetteLuma))
	return "SILHOUETTE_LUMA";
  else if (paramName.equals(aSoftLight))
	return "SOFT_LIGHT";
  else if (paramName.equals(aStencilAlpha))
	return "STENCIL_ALPHA";
  else if (paramName.equals(aStencilLuma))
	return "STENCIL_LUMA";
  else if (paramName.equals(aVividLight))
    	return "VIVID_LIGHT";
  else
    return null;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aCompName = "CompName";
  
  private static final long serialVersionUID = -435600151478672924L;
}
