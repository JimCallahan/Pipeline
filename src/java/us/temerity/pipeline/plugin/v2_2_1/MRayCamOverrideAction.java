// $Id: MRayCamOverrideAction.java,v 1.1 2007/03/26 23:33:18 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   C A M   O V E R R I D E   A C T I O N                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Builds a list of MI statements that can override camera settings as an increment.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Override Resolution<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Whether the resolution should be overriden.
 *   </DIV> <BR>
 * 
 *   Image Width <BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The width of the image to be rendered.
 *   </DIV> <BR>
 * 
 *   Image Height <BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The height of the image to be rendered.
 *   </DIV> <BR>
 * 
 *   Override Aspect<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Whether the aspect ratio should be overriden.
 *   </DIV> <BR>
 * 
 *   Aspect Ratio<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The aspect ratio of the image to be rendered.
 *   </DIV> <BR>
 * 
 *   Override Aperture<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Whether the camera aperture should be overriden.
 *   </DIV> <BR>
 * 
 *   Aperture<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The aperture of the render camera.
 *   </DIV> <BR>
 * 
 *   Override Clipping<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Whether the clipping planes should be overriden.
 *   </DIV> <BR>
 * 
 *   Near Clipping<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The near clipping plane for the render camera.
 *   </DIV> <BR>
 * 
 *   Far Clipping<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The far clipping plane for the render camera.
 *   </DIV> <BR>
 * 
 *   Override Focal<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     Whether the focal length should be overriden.
 *   </DIV> <BR>
 * 
 *   Focal Length<BR>
 *   <DIV style="margin-left: 40px;"> 
 *     The focal length for the render camera.
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
    super("MRayCamOverride", new VersionID("2.2.1"), "Temerity",
	  "Builds a list of MI statements that can override camera settings as " + 
	  "an increment.");

    {
      ActionParam param = 
	new BooleanActionParam
	(aOverrideResolution,
	 "Whether the resolution should be overriden.", 
	 true);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new IntegerActionParam
	(aImageWidth,
	 "The width of the image to be rendered.", 
	 720);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new IntegerActionParam
	(aImageHeight,
	 "The height of the image to be rendered.", 
	 486);
      addSingleParam(param);
    }
    

    {
      ActionParam param = 
	new BooleanActionParam
	(aOverrideAspect,
	 "Whether the aspect ratio should be overriden.", 
	 true);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	(aAspectRatio,
	 "The aspect ratio of the image to be rendered.", 
	 1.333);
      addSingleParam(param);
    }

    
    {
      ActionParam param = 
	new BooleanActionParam
	(aOverrideAperture,
	 "Whether the camera aperture should be overriden.", 
	 true);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	(aAperture, 
	 "The aperture of the render camera.", 
	 1.41732);
      addSingleParam(param);
    }
    

    {
      ActionParam param = 
	new BooleanActionParam
	(aOverrideClipping,
	 "Whether the clipping planes should be overriden.", 
	 true);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	(aNearClipping,
	 "The near clipping plane for the render camera.", 
	 0.01);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	(aFarClipping,
	 "The far clipping plane for the render camera.", 
	 1000.);
      addSingleParam(param);
    }


    {
      ActionParam param = 
	new BooleanActionParam
	(aOverrideFocal,
	 "Whether the focal length should be overriden.", 
	 true);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	(aFocalLength, 
	 "The focal length for the render camera.",
	 1.37795);
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

      addPreset(sImageResolutionPreset, choices);

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 320);
	values.put(aImageHeight, 240);
	values.put(aAspectRatio, 1.333);
	values.put(aAperture, 1.25984);

	addPresetValues(sImageResolutionPreset, "320x240", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 640);
	values.put(aImageHeight, 480);
	values.put(aAspectRatio, 1.333);
	values.put(aAperture, 1.25984);

	addPresetValues(sImageResolutionPreset, "640x480", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 1024);
	values.put(aImageHeight, 1024);
	values.put(aAspectRatio, 1.0);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "1k Square", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 2048);
	values.put(aImageHeight, 2048);
	values.put(aAspectRatio, 1.0);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "2k Square", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 3072);
	values.put(aImageHeight, 3072);
	values.put(aAspectRatio, 1.0);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "3k Square", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 4096);
	values.put(aImageHeight, 4096);
	values.put(aAspectRatio, 1.0);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "4k Square", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 720);
	values.put(aImageHeight, 576);
	values.put(aAspectRatio, 1.333);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "CCIR PAL/Quantel PAL", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 720);
	values.put(aImageHeight, 486);
	values.put(aAspectRatio, 1.333);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "CCIR 601/Quantel NTSC", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 1024);
	values.put(aImageHeight, 768);
	values.put(aAspectRatio, 1.333);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "Full 1024", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 1280);
	values.put(aImageHeight, 1024);
	values.put(aAspectRatio, 1.333);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "Full 1280/Screen", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 1280);
	values.put(aImageHeight, 720);
	values.put(aAspectRatio, 1.777);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "HD 720", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 1920);
	values.put(aImageHeight, 1080);
	values.put(aAspectRatio, 1.777);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "HD 1080", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 646);
	values.put(aImageHeight, 485);
	values.put(aAspectRatio, 1.333);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "NTSC 4d", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 768);
	values.put(aImageHeight, 576);
	values.put(aAspectRatio, 1.333);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "PAL 768", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 780);
	values.put(aImageHeight, 576);
	values.put(aAspectRatio, 1.333);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "PAL 780", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 512);
	values.put(aImageHeight, 486);
	values.put(aAspectRatio, 1.333);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "Targa 486", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 512);
	values.put(aImageHeight, 482);
	values.put(aAspectRatio, 1.333);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "Target NTSC", values);
      }

      {
	TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
	values.put(aImageWidth, 512);
	values.put(aImageHeight, 576);
	values.put(aAspectRatio, 1.333);
	values.put(aAperture, 1.41732);

	addPresetValues(sImageResolutionPreset, "Targa PAL", values);
      }
    }

    {
      LayoutGroup layout = new LayoutGroup(true);

      layout.addEntry(sImageResolutionPreset);
      layout.addEntry(aOverrideResolution);
      layout.addEntry(aImageWidth);
      layout.addEntry(aImageHeight);
      layout.addSeparator();
      layout.addEntry(aOverrideAspect);
      layout.addEntry(aAspectRatio);
      layout.addSeparator();
      layout.addEntry(aOverrideAperture);
      layout.addEntry(aAperture);
      layout.addSeparator();
      layout.addEntry(aOverrideFocal);
      layout.addEntry(aFocalLength);
      layout.addSeparator();
      layout.addEntry(aOverrideClipping);
      layout.addEntry(aNearClipping);
      layout.addEntry(aFarClipping);

      setSingleLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows); 
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
    /* target MEL script */ 
    Path target = getPrimaryTargetPath(agenda, "mi", "Mental Ray Input (.mi) files");

    /* create a temporary file which will be copied to the target */ 
    File temp = createTemp(agenda, "mi");
    try {      
      FileWriter out = new FileWriter(temp);

      if(getSingleBooleanParamValue(aOverrideResolution)) {
	int width    = getSingleIntegerParamValue(aImageWidth, 1); 
	int height   = getSingleIntegerParamValue(aImageHeight, 1); 
	out.write("resolution " + width + " " + height + "\n");
      }

      if(getSingleBooleanParamValue(aOverrideAspect)) {
        double aspect = getSingleDoubleParamValue(aAspectRatio, 0.0);
	out.write("aspect " + aspect + "\n");
      }

      if(getSingleBooleanParamValue(aOverrideAperture)) {
	double aperture = getSingleDoubleParamValue(aAperture, 0.0);
	out.write("aperture " + aperture + "\n");
      }

      if(getSingleBooleanParamValue(aOverrideClipping)) {
	double near = getSingleDoubleParamValue(aNearClipping, 0.0);
	double far  = getSingleDoubleParamValue(aFarClipping, 0.0);
	out.write("clip " + near + " " + far + "\n");
      }

      if(getSingleBooleanParamValue(aOverrideFocal)) {
	double focal = getSingleDoubleParamValue(aFocalLength, 0.0);
	out.write("focal " + focal + "\n");
      }

      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MI file (" + temp + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    /* create the process to run the action */ 
    return createTempCopySubProcess(agenda, temp, target, outFile, errFile);    
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3830473738863473925L;

  public final static String aOverrideResolution = "OverrideResolution";
  public final static String aImageWidth         = "ImageWidth";
  public final static String aImageHeight        = "ImageHeight";
  public final static String aOverrideAspect     = "OverrideAspect";  
  public final static String aAspectRatio        = "AspectRatio";
  public final static String aOverrideAperture   = "OverrideAperture";
  public final static String aAperture           = "Aperture";
  public final static String aOverrideClipping   = "OverrideClipping";
  public final static String aNearClipping       = "NearClipping";
  public final static String aFarClipping        = "FarClipping";
  public final static String aOverrideFocal      = "OverrideFocal";
  public final static String aFocalLength        = "FocalLength";

  private final static String sImageResolutionPreset = "ImageResolution";

}
