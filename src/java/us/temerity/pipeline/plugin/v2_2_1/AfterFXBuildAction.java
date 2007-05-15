package us.temerity.pipeline.plugin.v2_2_1;
import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.plugin.CommonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   A F T E R   F X   B U I L D   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a new After Effects scene from component image sources. <P> 
 **/
public 
class AfterFXBuildAction
  extends CommonActionUtils
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
    
    {
      ActionParam param = 
	new DoubleActionParam
	(aCompFrameRate,
	 "The frame rate for the comp.",
	 30.);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	(aCompPixelRatio,
	 "The pixel aspect ratio for the comp.",
	 1.);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new IntegerActionParam
	(aCompHeight,
	 "The height in pixels of the comp.",
	 720);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new IntegerActionParam
	(aCompWidth,
	 "The width in pixels of the comp.",
	 486);
      addSingleParam(param);
    }
    
    /* 
     * Support for OsX can be added when After Effects CS3 launches, making it possible
     * to run AfterFX as an AppleScript app.  I have not been able to get the Mac version
     * of 7.0 to run scripts from the command line.
     */
    //addSupport(OsType.MacOS);
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
      ActionParam param = 
	new DoubleActionParam
	(aFrameRate, 
	 "The Frame rate to interpret this footage at.",
	 30.);
      params.put(param.getName(), param);
    }
    
    {
      ActionParam param = 
	new IntegerActionParam
	(aLevel, 
	 "Which level of the comp the source should appear at.  " +
	 "The higher the number, the closer to the top of the composition it will be layered.",
	 0);
      params.put(param.getName(), param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("ADD");
      choices.add("ALPHA_ADD");
      choices.add("CLASSIC_COLOR_BURN");
      choices.add("CLASSIC_COLOR_DODGE");
      choices.add("CLASSIC_DIFFERENCE");
      choices.add("COLOR");
      choices.add("COLOR_BURN");
      choices.add("COLOR_DODGE");
      choices.add("DANCING_DISSOLVE");
      choices.add("DARKEN");
      choices.add("DIFFERENCE");
      choices.add("DISSOLVE");
      choices.add("EXCLUSION");
      choices.add("HARD_LIGHT");
      choices.add("HUE");
      choices.add("LIGHTEN");
      choices.add("LINEAR_BURN");
      choices.add("LINEAR_DODGE");
      choices.add("LINEAR_LIGHT");
      choices.add("LUMINESCENT_PREMUL");
      choices.add("LUMINOSITY");
      choices.add("MULTIPLY");
      choices.add("NORMAL");
      choices.add("OVERLAY");
      choices.add("PIN_LIGHT");
      choices.add("SATURATION");
      choices.add("SCREEN");
      choices.add("SILHOUETTE_ALPHA");
      choices.add("SILHOUETTE_LUMA");
      choices.add("SOFT_LIGHT");
      choices.add("STENCIL_ALPHA");
      choices.add("STENCIL_LUMA");
      choices.add("VIVID_LIGHT");
      
      ActionParam param = 
	new EnumActionParam
	(aBlendMode, 
	 "What blend mode should this layer be set to.",
	 "NORMAL",
	 choices);
      params.put(param.getName(), param);
    }
    
    {
      ActionParam param = 
	new IntegerActionParam
	(aOrder, 
	 "Where in time order the source should appear.  " +
	 "Sources with the same order will start at the same time.",
	 100);
      params.put(param.getName(), param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	(aPixelRatio, 
	 "The Pixel Aspect ratio to interpret this footage at.",
	 1.);
      params.put(param.getName(), param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	(aPixelRatio, 
	 "The Pixel Aspect ratio to interpret this footage at.",
	 1.);
      params.put(param.getName(), param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>(3);
      choices.add(aIgnore);
      choices.add(aStraight);
      choices.add(aPreMultipled);
      
      ActionParam param = 
	new EnumActionParam
	(aAlphaMode, 
	 "How should the footages alpha be interpreted.",
	 aPreMultipled,
	 choices);
      params.put(param.getName(), param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>(3);
      choices.add(aBlack);
      choices.add(aWhite);
      
      ActionParam param = 
	new EnumActionParam
	(aPreMultiplyColor, 
	 "What color should the alpha be premultiplied by.",
	 aBlack,
	 choices);
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
    
    Path targetPath = getPrimaryTargetPath(agenda, "aep", "The Target After Effects Project");
    
    String compName = getSingleStringParamValue(aCompName);
    Double compFrameRate = getSingleDoubleParamValue(aCompFrameRate, new Range<Double>(1., 99.));
    Double compPixelRatio = getSingleDoubleParamValue(aCompPixelRatio, new Range<Double>(.01, 100.));
    Integer compHeight = getSingleIntegerParamValue(aCompHeight, new Range<Integer>(4, 30000));
    Integer compWidth = getSingleIntegerParamValue(aCompWidth, new Range<Integer>(4, 30000));
    double offset= 1/compFrameRate;
        
    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, "jsx");
    try {      
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
      
      out.println("app.exitAfterLaunchAndEval = true;");
      out.println("app.beginSuppressDialogs();");
      out.println("app.project.close(CloseOptions.DO_NOT_SAVE_CHANGES);");
      out.println("app.newProject();");
      
      out.println("");
      
      MappedLinkedList<Integer, String> orderedList = new MappedLinkedList<Integer, String>();
      MappedLinkedList<Integer, Double> orderLength = new MappedLinkedList<Integer, Double>();
      for (String sourceName : agenda.getSourceNames()) {
	Integer order = (Integer) getSourceParamValue(sourceName, aOrder);
	orderedList.put(order, sourceName);
	FrameRange range = agenda.getPrimarySource(sourceName).getFrameRange();
	Double frameRate = (Double) getSourceParamValue(sourceName, aFrameRate);
	if (range.isSingle())
	  orderLength.put(order, 1/frameRate);
	else {
	  double length = (range.getEnd() - range.getStart() + 1)/frameRate;
	  orderLength.put(order, length);
	}
      }
      
      double totalLength = offset;
      for (int order : orderedList.keySet()) {
	double length = (new TreeSet<Double>(orderLength.get(order))).last();
	totalLength += length;
      }
      totalLength = 20.;
      
      out.println("var comp = app.project.items.addComp(\"" + compName + "\", " + 
	          compHeight + ", " + compWidth + ", " + compPixelRatio + ", " + 
	          totalLength + ", " + compFrameRate + ");");
      out.println("var layers = comp.layers;");
      
      out.println("");

      
      double startTime = 0;
      for (int order : orderedList.keySet()) {
	double length = (new TreeSet<Double>(orderLength.get(order))).last();

	MappedLinkedList<Integer, String> levelList = new MappedLinkedList<Integer, String>();
	for (String sourceName : orderedList.get(order)) {
	  Integer level = (Integer) getSourceParamValue(sourceName, aLevel);
	  levelList.put(level, sourceName);
	}
	
	for (int level : levelList.keySet()) {
	  for (String sourceName : levelList.get(level)) {
	    FileSeq sSeq = agenda.getPrimarySource(sourceName);
	    boolean sequence = sSeq.hasFrameNumbers();
	    Path spath = getWorkingNodeFilePath(agenda, sourceName, sSeq);

	    Double frameRate = (Double) getSourceParamValue(sourceName, aFrameRate);
	    Double pixelAspect = (Double) getSourceParamValue(sourceName, aPixelRatio);
	    String alphaMode = (String) getSourceParamValue(sourceName, aAlphaMode);
	    String preMultColor = (String) getSourceParamValue(sourceName, aPreMultiplyColor);
	    String blendMode = (String) getSourceParamValue(sourceName, aBlendMode);

	    out.println("{");
	    out.println("  var f = new File(\"" + spath.toOsString().replaceAll("\\\\", "\\\\\\\\") + "\");");
	    out.println("  var importOptions = new ImportOptions (f);");
	    out.println("  importOptions.importAs = ImportAsType.FOOTAGE;");
	    if (sequence)
	      out.println("  importOptions.sequence = true;");
	    else
	      out.println("  importOptions.sequence = false;");
	    out.println("  var footage = app.project.importFile (importOptions);");
	    out.println("  if (!footage.mainSource.isStill)");
	    out.println("    footage.mainSource.conformFrameRate = " + frameRate + ";");
	    out.println("  footage.pixelAspect = " + pixelAspect + ";");
	    out.println("  footage.mainSource.alphaMode = AlphaMode." + alphaMode + ";");
	    if (preMultColor.equals(aWhite))
	      out.println("  footage.mainSource.premulColor = new Array(1,1,1);");
	    else
	      out.println("  footage.mainSource.premulColor = new Array(0,0,0);");
	    
	    out.println("  var avlayer = layers.add(footage);");
	    out.println("  avlayer.startTime = " + startTime + ";");
	    out.println("  avlayer.moveToBeginning();");
	    out.println("  avlayer.blendingMode = BlendingMode." + blendMode + ";");
	    out.println("}");
	  }
	}
	startTime += length;// + offset;
      }
      
      //out.println("{");
      out.println("var f = new File(\"" + targetPath.toOsString().replaceAll("\\\\", "\\\\\\\\") + "\");");
      out.println("app.project.save(f);");
      out.println("app.project.close(CloseOptions.DO_NOT_SAVE_CHANGES);");
      out.println("app.newProject();");
      out.println("app.quit();");
      //out.println("}");
      
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -435600151478672924L;
  
  // Single Params
  public static final String aCompName = "CompName";
  public static final String aCompFrameRate = "CompFrameRate";
  public static final String aCompPixelRatio = "CompPixelRatio";
  public static final String aCompHeight = "CompHeight";
  public static final String aCompWidth = "CompWidth";
  
  // Source Params
  public static final String aFrameRate = "FrameRate";
  public static final String aPixelRatio = "PixelRatio";
  public static final String aAlphaMode = "AlphaMode";
  public static final String aPreMultiplyColor = "PreMultiplyColor";
  public static final String aLevel = "Level";
  public static final String aOrder = "Order";
  public static final String aBlendMode = "BlendMode";
  
  // Options for AlphaMode
  public static final String aIgnore = "IGNORE";
  public static final String aStraight = "STRAIGHT";
  public static final String aPreMultipled = "PREMULTIPLIED";
  
  // Options for PreMultiplyColor
  public static final String aWhite =  "White";
  public static final String aBlack = "Black";
}
