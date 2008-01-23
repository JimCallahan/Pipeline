// $Id: AfterFXBuildAction.java,v 1.1 2008/01/23 16:25:58 jim Exp $

package us.temerity.pipeline.plugin.AfterFXBuildAction.v2_4_1;

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
 * 
 * The ADOBE_CS_VERSION environmental variable from the Toolset will be used to 
 * specify the version of the Adobe Create Suite used to launch After Effects.  For
 * example, setting ADOBE_CS_VERSION="3" will cause "Adobe Photoshop CS3" to be used. 
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
    super("AfterFXBuild", new VersionID("2.4.1"), "Temerity",
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

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aCompName);
      layout.addSeparator();
      layout.addEntry(aCompWidth);
      layout.addEntry(aCompHeight);     
      layout.addEntry(aCompPixelRatio);
      layout.addSeparator();
      layout.addEntry(aCompFrameRate);
      
      setSingleLayout(layout);  
    }
    
    {
      LinkedList<String> layout = new LinkedList<String>();
      layout.add(aLayer);
      layout.add(aPass);
      layout.add(aOrder);
      layout.add(aBlendMode);
      layout.add(aFrameRate);
      layout.add(aPixelRatio);
      layout.add(aAlphaMode);
      layout.add(aPreMultColor);
      
      setSourceLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
    removeSupport(OsType.Unix);
    
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

    Double compFrameRate = 
      getSingleDoubleParamValue(aCompFrameRate, new Range<Double>(1., 99.));

    Double compPixelRatio = 
      getSingleDoubleParamValue(aCompPixelRatio, new Range<Double>(.01, 100.));

    Integer compHeight = 
      getSingleIntegerParamValue(aCompHeight, new Range<Integer>(4, 30000));

    Integer compWidth = 
      getSingleIntegerParamValue(aCompWidth, new Range<Integer>(4, 30000));

    double offset= 1/compFrameRate;
        
    /* create a temporary JSX script file */ 
    File script = createTemp(agenda, "jsx");
    try {      
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
      
      out.write
        ("app.exitAfterLaunchAndEval = true;\n" + 
         "app.beginSuppressDialogs();\n" + 
         "app.project.close(CloseOptions.DO_NOT_SAVE_CHANGES);\n" +
         "app.newProject();\n\n");
      
      MappedLinkedList<Integer, String> layerList = new MappedLinkedList<Integer, String>();
      for (String sourceName : agenda.getSourceNames()) {
	Integer layer = (Integer) getSourceParamValue(sourceName, aLayer);
	layerList.put(layer, sourceName);
      }
      
      LinkedList<String> zeroLayers = layerList.remove(0);
      
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
      
      out.write
        ("var comp = app.project.items.addComp(\"" + compName + "\", " + 
         compWidth + ", " + compHeight + ", " + compPixelRatio + ", " + 
         totalLength + ", " + compFrameRate + ");\n" +
         "var passes = comp.layers;\n\n");
      
      if (zeroLayers != null) {
	MappedLinkedList<Integer, String> passList = new MappedLinkedList<Integer, String>();
	for (String sourceName : zeroLayers) {
	  Integer pass = (Integer) getSourceParamValue(sourceName, aPass);
	  passList.put(pass, sourceName);
	}
	for (int pass : passList.keySet()) {
	  for (String sourceName : passList.get(pass)) {
	    writeSourceToScript(agenda, out, orderStart, sourceName, "passes", false);
	  }
	}
      }
      
      for (Integer layer : layerList.keySet())
      {
	String layerCompName = "Layer" + layer;
	LinkedList<String> passes = layerList.get(layer);

	MappedLinkedList<Integer, String> passList = new MappedLinkedList<Integer, String>();
	for (String sourceName : passes) {
	  Integer pass = (Integer) getSourceParamValue(sourceName, aPass);
	  passList.put(pass, sourceName);
	}
	
	out.write
          ("{\n" +
           "  var layerComp = app.project.items.addComp(\"" + layerCompName + "\", " + 
           compWidth + ", " + compHeight + ", " + compPixelRatio + ", " + 
           totalLength + ", " + compFrameRate + ");\n" +
           "  var layerPasses = layerComp.layers;\n\n");

	boolean first = true;
	String compBlendMode = null;
	for (int pass : passList.keySet()) {
	  for (String sourceName : passList.get(pass)) {
	    String blendMode = 
	      writeSourceToScript(agenda, out, orderStart, sourceName, "layerPasses", first);

	    if (first) {
              compBlendMode = blendModeStr(blendMode);
              first = false;
	    }
	  }
	}
        out.write
          ("  var avpass = passes.add(layerComp);\n" +
           "  avpass.startTime = " + offset + ";\n" + 
           "  avpass.moveToBeginning();\n" +
           "  avpass.blendingMode = BlendingMode." + compBlendMode + ";\n" + 
           "}\n");
      }
      
      out.write
        ("var f = new " + 
         "File(\"" + CommonActionUtils.escPath(targetPath.toOsString()) + "\");\n" +
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

    if(PackageInfo.sOsType == OsType.MacOS) { 
      String vsn = agenda.getEnvironment().get("ADOBE_CS_VERSION");

      int csv = -1; 
      try {
        if(vsn == null)
          throw new PipelineException
            ("The ADOBE_CS_VERSION was not defined!"); 
        csv = Integer.valueOf(vsn);
      }
      catch(NumberFormatException ex) {
        throw new PipelineException
          ("The ADOBE_CS_VERSION given (" + vsn + ") was not a number!"); 
      }

      if(csv < 3)
        throw new PipelineException
          ("The Mac OS X is only supported for Adobe After Effects CS3 and above!");         

      ArrayList<String> args = new ArrayList<String>();
      args.add("-e");
      args.add("tell application \"Adobe After Effects CS" + csv + "\"");
      
      String macpath = script.getAbsolutePath().substring(1).replace("/",":");
      args.add("-e");
      args.add("DoScriptFile \"" + macpath + "\"");
      
      args.add("-e");
      args.add("end tell");
      
      return createSubProcess(agenda, "osascript", args, outFile, errFile);
    }
    else {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-m");
      args.add("-r");
      args.add(script.getAbsolutePath());
      
      return createSubProcess(agenda, "AfterFX.exe", args, outFile, errFile);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Spit out the code to read a source sequence into AE and optionally offset it in 
   * time based on the Order parameter.
   */
  private String 
  writeSourceToScript
  (
    ActionAgenda agenda, 
    PrintWriter out,
    TreeMap<Integer, Double> orderStart, 
    String sourceName,
    String passName,
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

    out.write
      ("{\n" +
       "  var f = new File(\"" + CommonActionUtils.escPath(spath.toOsString()) + "\");\n" +
       "  var importOptions = new ImportOptions (f);\n" + 
       "  importOptions.importAs = ImportAsType.FOOTAGE;\n");

    if (sequence)
      out.write("  importOptions.sequence = true;\n");
    else
      out.write("  importOptions.sequence = false;\n");

    out.write
      ("  var footage = app.project.importFile (importOptions);\n" +
       "  if (!footage.mainSource.isStill)\n" +
       "    footage.mainSource.conformFrameRate = " + frameRate + ";\n" +
       "  footage.pixelAspect = " + pixelAspect + ";\n" +
       "  footage.mainSource.alphaMode = AlphaMode." + alphaMode + ";\n");

    if (preMultColor.equals(aWhite))
      out.write("  footage.mainSource.premulColor = new Array(1,1,1);\n");
    else
      out.write("  footage.mainSource.premulColor = new Array(0,0,0);\n");

    if (!first) {
      out.write
        ("  var avpass = " + passName + ".add(footage);\n" +
         "  avpass.startTime = " + startTime + ";\n" + 
         "  avpass.moveToBeginning();\n" +
         "  avpass.blendingMode = BlendingMode." + blendModeStr(blendMode) + ";\n" + 
         "}\n");
    }
    else {
      out.write
        ("  var avpass = layerPasses.add(footage);\n" +
         "  avpass.startTime = " + startTime + ";\n" + 
         "  avpass.moveToBeginning();\n" +
         "  avpass.blendingMode = BlendingMode.NORMAL;\n" + 
         "}\n");
    }
    return blendMode;
  }
 
  /** 
   * Generates an AE BlendingMode value string.
   */
  public String
  blendModeStr
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
  
  private static final long serialVersionUID = -1229359062677006976L;

  public static final String aCompName = "CompName";  

}

