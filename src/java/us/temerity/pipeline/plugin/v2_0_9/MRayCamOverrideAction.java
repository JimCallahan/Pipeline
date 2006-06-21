// $Id: MRayCamOverrideAction.java,v 1.1 2006/06/21 05:25:10 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   C A M   O V E R R I D E   A C T I O N                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Builds a list of MI commands to be used in a incremental camera statement. <P>
 * 
 * Simply takes the settings on the node and writes the camera statements. <P>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Override Resolution<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Print the override statements for heigh and width. 
 *   </DIV> <BR>
 * 
 *   Image Height <BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The Height of the image. 
 *   </DIV> <BR>
 * 
 *   Image Width <BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The Width of the image. 
 *   </DIV> <BR>
 * 
 *   Override Aspect<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Print the override statements for aspect ratio. 
 *   </DIV> <BR>
 * 
 *   Aspect Ratio<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The aspect ratio of the image. 
 *   </DIV> <BR>
 * 
 *   Override Aperture<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Print the override statements for aperture.
 *   </DIV> <BR>
 * 
 *   Aperture<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The camera aperture. 
 *   </DIV> <BR>
 * 
 *   Override Clipping<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Print the override statements for near and far clipping planes. 
 *   </DIV> <BR>
 * 
 *   Near Clipping<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The near clipping plane. 
 *   </DIV> <BR>
 * 
 *   Far Clipping<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The far clipping plane. 
 *   </DIV> <BR>
 * 
 *   Override Focal<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Print the override statements for focal length. 
 *   </DIV> <BR>
 * 
 *   Focal Length<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The focal length of the lens. 
 *   </DIV> <BR>
 * </DIV>
 * <P>
 */
public class 
MRayCamOverrideAction 
  extends BaseAction
{
  public 
  MRayCamOverrideAction()
  {
    super("MRayCamOverride", new VersionID("2.0.9"), "Temerity",
	  "Builds a list of MI statements that can override camera settings as " + 
	  "an increment.");

    {
      ActionParam param = 
	new IntegerActionParam
	(heightParam,
	 "The Height of the Image to be rendered", 
	 486);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new IntegerActionParam
	(widthParam,
	 "The Width of the Image to be rendered", 
	 720);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	(aspectParam,
	 "The aspect ratio of the Image to be rendered", 
	 1.333);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	(apertureParam, 
	 "The aperture for the camera", 
	 1.41732);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	(nearParam,
	 "The near clipping plane for the camera", 
	 0.01);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	(farParam,
	 "The far clipping plane for the camera", 
	 1000.);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	(focalParam, 
	 "The focal length for the camera",
	 1.37795);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new BooleanActionParam
	(rezBoolParam,
	 "Should the resolution be overriden", 
	 true);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aspectBoolParam,
	 "Should the aspect ration be overriden", 
	 true);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new BooleanActionParam
	(apertureBoolParam,
	 "Should the aperture be overriden", 
	 true);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new BooleanActionParam
	(clipBoolParam,
	 "Should the clipping planes be overriden", 
	 true);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(focalBoolParam,
	 "Should the focal length be overriden", 
	 true);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("320x240");
      choices.add("640x480");
      choices.add("1k Square");
      choices.add("2k Square");
      choices.add("3k Square");
      choices.add("4k Square");
      choices.add("CCIR PAL/Quantel PAL");
      choices.add("CCIR 601/Quantel NTSC");
      choices.add("Full 1024");
      choices.add("Full 1280/Screen");
      choices.add("HD 720");
      choices.add("HD 1080");
      choices.add("NTSC 4d");
      choices.add("PAL 768");
      choices.add("PAL 780");
      choices.add("Targa 486");
      choices.add("Target NTSC");
      choices.add("Targa PAL");

      addPreset(imageRezPreset, choices);

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 320);
	values.put(heightParam, 240);
	values.put(aspectParam, 1.333);
	values.put(apertureParam, 1.25984);

	addPresetValues(imageRezPreset, "320x240", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 640);
	values.put(heightParam, 480);
	values.put(aspectParam, 1.333);
	values.put(apertureParam, 1.25984);

	addPresetValues(imageRezPreset, "640x480", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 1024);
	values.put(heightParam, 1024);
	values.put(aspectParam, 1.0);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "1k Square", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 2048);
	values.put(heightParam, 2048);
	values.put(aspectParam, 1.0);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "2k Square", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 3072);
	values.put(heightParam, 3072);
	values.put(aspectParam, 1.0);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "3k Square", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 4096);
	values.put(heightParam, 4096);
	values.put(aspectParam, 1.0);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "4k Square", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 720);
	values.put(heightParam, 576);
	values.put(aspectParam, 1.333);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "CCIR PAL/Quantel PAL", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 720);
	values.put(heightParam, 486);
	values.put(aspectParam, 1.333);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "CCIR 601/Quantel NTSC", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 1024);
	values.put(heightParam, 768);
	values.put(aspectParam, 1.333);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "Full 1024", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 1280);
	values.put(heightParam, 1024);
	values.put(aspectParam, 1.333);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "Full 1280/Screen", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 1280);
	values.put(heightParam, 720);
	values.put(aspectParam, 1.777);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "HD 720", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 1920);
	values.put(heightParam, 1080);
	values.put(aspectParam, 1.777);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "HD 1080", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 646);
	values.put(heightParam, 485);
	values.put(aspectParam, 1.333);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "NTSC 4d", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 768);
	values.put(heightParam, 576);
	values.put(aspectParam, 1.333);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "PAL 768", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 780);
	values.put(heightParam, 576);
	values.put(aspectParam, 1.333);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "PAL 780", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 512);
	values.put(heightParam, 486);
	values.put(aspectParam, 1.333);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "Targa 486", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 512);
	values.put(heightParam, 482);
	values.put(aspectParam, 1.333);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "Target NTSC", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(widthParam, 512);
	values.put(heightParam, 576);
	values.put(aspectParam, 1.333);
	values.put(apertureParam, 1.41732);

	addPresetValues(imageRezPreset, "Targa PAL", values);
      }
    }

    {
      LayoutGroup layout = new LayoutGroup(true);

      layout.addEntry(imageRezPreset);
      layout.addEntry(rezBoolParam);
      layout.addEntry(widthParam);
      layout.addEntry(heightParam);
      layout.addSeparator();
      layout.addEntry(aspectBoolParam);
      layout.addEntry(aspectParam);
      layout.addSeparator();
      layout.addEntry(apertureBoolParam);
      layout.addEntry(apertureParam);
      layout.addSeparator();
      layout.addEntry(focalBoolParam);
      layout.addEntry(focalParam);
      layout.addSeparator();
      layout.addEntry(clipBoolParam);
      layout.addEntry(nearParam);
      layout.addEntry(farParam);

      setSingleLayout(layout);
    }
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
    {
      Set<String> sources = agenda.getSourceNames();
      if (sources.size() > 0)
	throw new PipelineException
	  ("The MRayBuildCameraOverride Action does not use any source nodes");
    }

    {
      FileSeq fseq = agenda.getPrimaryTarget();
      String suffix = fseq.getFilePattern().getSuffix();
      if ((suffix == null) || !suffix.equals("mi") || !fseq.isSingle())
	throw new PipelineException
	  ("The primary file sequence (" + fseq + ") must contain exactly one Mental " + 
	   "Ray Input (.mi) file!");
      target = fseq;
      scene = new Path(PackageInfo.sProdPath, 
		       nodeID.getWorkingParent() + "/" + target.getPath(0));
    }

    /* create MI file */ 
    File document = createTemp(agenda, 0644, "mi");
    try {
      PrintWriter out = new PrintWriter(new FileWriter(document));

      if ((Boolean) getSingleParamValue(rezBoolParam)) {
	int width = (Integer) getSingleParamValue(widthParam);
	int height = (Integer) getSingleParamValue(heightParam);
	out.println("resolution " + width + " " + height);
      }

      if ((Boolean) getSingleParamValue(aspectBoolParam)) {
	double aspect = (Double) getSingleParamValue(aspectParam);
	out.println("aspect " + aspect);
      }

      if ((Boolean) getSingleParamValue(apertureBoolParam)) {
	double aperture = (Double) getSingleParamValue(apertureParam);
	out.println("aperture " + aperture);
      }

      if ((Boolean) getSingleParamValue(clipBoolParam)) {
	double near = (Double) getSingleParamValue(nearParam);
	double far = (Double) getSingleParamValue(farParam);
	out.println("clip " + near + " " + far);
      }

      if ((Boolean) getSingleParamValue(focalBoolParam)) {
	double focal = (Double) getSingleParamValue(focalParam);
	out.println("focal " + focal);
      }

      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MI scene file (" + scene + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add(document.getPath());
      args.add(scene.toOsString());

      try {
	return new SubProcessHeavy
	  (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	   "cp", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
	   outFile, errFile);
      } 
      catch (Exception ex) {
	throw new PipelineException
	  ("Unable to generate the SubProcess to perform this Action!\n" +
	   ex.getMessage());
      }
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private final static String heightParam = "ImageHeight";
  private final static String widthParam = "ImageWidth";
  private final static String aspectParam = "AspectRatio";
  private final static String apertureParam = "Aperture";
  private final static String nearParam = "NearClipping";
  private final static String farParam = "FarClipping";
  private final static String focalParam = "FocalLength";

  private final static String rezBoolParam = "OverrideResolution";
  private final static String aspectBoolParam = "OverrideAspect";
  private final static String apertureBoolParam = "OverrideAperture";
  private final static String clipBoolParam = "OverrideClipping";
  private final static String focalBoolParam = "OverrideFocal";

  private final static String imageRezPreset = "ImageResolution";

  private static final long serialVersionUID = -6885774243792243629L;

}
