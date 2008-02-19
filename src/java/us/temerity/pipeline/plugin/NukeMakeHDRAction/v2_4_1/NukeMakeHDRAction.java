// $Id: NukeMakeHDRAction.java,v 1.2 2008/02/19 02:13:01 jim Exp $

package us.temerity.pipeline.plugin.NukeMakeHDRAction.v2_4_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.*;

import java.io.*;
import java.util.*; 
import java.util.regex.*; 

/*------------------------------------------------------------------------------------------*/
/*   N U K E   M A K E   H D R   A C T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Converts a series of varying exposure raw digital images into a single high dynamica 
 * range (HDR) environment map.<P> 
 * 
 * The raw input images are supplied as source nodes.  Each of these source node's primary 
 * image sequence contains the range of exposures in CR2 format for one of (3) views.  Each 
 * view is assumed to be shot with an 180 degree angular fisheye lense.  The views should be
 * oriented at 120 degree intervals in a horizontal plane.  For best results, the number of 
 * exposure images and exposure settings should be indentical for of the (3) views.  All 
 * images should have been shot with the same aperature and other camera settings and vary 
 * only in exposure time (shutter speed).<P> 
 * 
 * For a given view, the raw images are combined using a guassian-like filter which weights 
 * the contribution of a pixel from each exposure based on its intensity.  Pixels closest to 
 * a value of (0.5) will receive higher weights, while pixels closer to (0.0) or (1.0) will 
 * receive lower weights.  The idea being that the accuracy of the pixel value from the 
 * input image is best within the middle range of intensities and therefore its contribution 
 * to the output should be higher then from underexposed or overexposed images. <P> 
 * 
 * Before computing pixel weights, the pixel intensity from the input images are graded
 * based on the given Black/White Point parameters.  Since CCD values typically are never 
 * (0.0) even when no photons are hitting the recepter and pixel values close to (1.0) can
 * also be innaccurate.  There is a separate Lowest Black Point used for the longest 
 * exposure image.  This gives finer control over the black point for the image with the 
 * most low intensity pixel values.<P> 
 * 
 * This action dynamically creates and executes a complex Nuke script based on its 
 * parameters and raw input images.  This Nuke script can generate one or more HDR images
 * depending on the names of the primary and secondary images sequences associated with 
 * the target node.  The names of these target images are automatically determined based on 
 * the prefix of the node running this action as follows:
 * 
 * <I>prefix</I>.hdr <BR>
 * <DIV style="margin-left: 40px;">
 *   The primary output combined HDR image.  The format of this image will either be a 
 *   latitude longitude cylindrical environment map (LatLon Mode) or a unwrapped 
 *   cube face environment map in the standard cross layout (EnvCross Mode).
 * </DIV> <BR> 
 * 
 * <I>prefix</I>_latlon.hdr <BR>
 * <DIV style="margin-left: 40px;">
 *   An optional secondary LatLon format combined HDR image.  
 * </DIV> <BR> 
 * 
 * <I>prefix</I>_cross.hdr <BR>
 * <DIV style="margin-left: 40px;">
 *   An optional secondary EnvCross format combined HDR image.  
 * </DIV> <BR> 
 * 
 * The generated Nuke script can also write HDR format diagnostic images for each of the 
 * input raw exposure image sequences based on their node name prefix as follows: 
 * 
 * <I>input-prefix</I>_combo.hdr <BR>
 * <DIV style="margin-left: 40px;">
 *   An optional secondary LatLon format HDR image containing the final combined exposure
 *   image for one of the input raw exposure views.  This image will be combined with the
 *   corresponding images from the other views to produce the final output HDR image.
 * </DIV> <BR> 
 * 
 * <I>input-prefix</I>_wts.hdr <BR>
 * <DIV style="margin-left: 40px;">
 *   An optional secondary LatLon format HDR image containing the combined pixel coverage 
 *   weights from each of the raw input exposure images for a view.  White pixels indicate 
 *   good coverage, meaning that there are sufficient pixel values within the middle range  
 *   in one or more exposures.  Black pixels indicate bad coverage, meaning that none of the 
 *   input exposure images contained middle range values for the pixel.  In other words,
 *   that the pixel was either underexposed or overexposed in all images.  If you see any 
 *   black in this image, it means that there is not a sufficiently large range of exposures 
 *   in the input images to fully represent the range of intensities in the scene.  Darker 
 *   areas in this image are also signs of poor coverage and therefore more error.
 * </DIV> <BR> 
 * 
 * <I>input-prefix</I>_nofix.hdr <BR>
 * <DIV style="margin-left: 40px;">
 *   An optional secondary LatLon format HDR image containing the uncorrected combined 
 *   exposure image for one of the input raw exposure views.  This image may contain black
 *   pixels where there is no coverage from the input raw exposure images.  This is most 
 *   likely to be caused by a lack of sufficienly short exposure images to capture a very 
 *   bright source of light in the image.<P> 
 * </DIV> <BR> 
 * 
 * <I>input-prefix</I>_fix.hdr <BR>
 * <DIV style="margin-left: 40px;">
 *   An optional secondary LatLon format HDR image containing high intensity pixels which 
 *   will fill in holes due to no coverage areas in the "nofix" image.  The intensity of 
 *   these pixels is generated based on the duration of the Missing Exposure parameter. 
 * </DIV> <BR> 
 * 
 * To generate any or all of these images, you simply need to add a secondary sequence with
 * the correct name to the node using this action.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Output Format<BR>
 *   <DIV style="margin-left: 40px;">
 *     The format for the primary output HDR image: 
 *     <DIV style="margin-left: 40px;">
 *       LatLon - Generates a latitute longitude cylindrical environment map. <BR>
 *       EnvCross - Generates an unwrapped cube face environment map in the standard cross 
 *       layout.
 *     </DIV> 
 *   </DIV> <BR>
 * 
 *   Output Size<BR>
 *   <DIV style="margin-left: 40px;">
 *     The resolution of the output HDR image data across a 90 degree field of view.  For 
 *     LatLon images, the full output resolution will be (Size*4 x Size*2) since these images 
 *     cover 360 degrees horizontally and 180 degrees vertically.  For EnvCross images, each 
 *     of the individual cube faces 90 covers degrees making the full output resolution 
 *     (Size*3 x Size*4).
 *   </DIV> <BR> 
 *   <P> 
 * 
 *   Exposure Times<BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which provides a plain text file containing exposure times (in 
 *     fractions of a second) for each of the raw input images.  The exposures should be 
 *     listed one per line and in the same order as the exposure images in each input image 
 *     sequence.  If specified, there must be exactly the same number of images in each of 
 *     the (3) view input image sequences and each sequence must be in the same exposure 
 *     duration order.  If not specified, the action will use dcraw(1) to extract exposure 
 *     times from each input image individually in which case the number and order of 
 *     exposure images is arbitrary.
 *   </DIV> <BR>
 * 
 *   Missing Exposure<BR>
 *   <DIV style="margin-left: 40px;">
 *     The exposure time (in fractions of a second) to give to pixels with zero coverage 
 *     from the input raw images.  The pixel values for these missing pixels will be 
 *     computed by scaling a white (1.0) value by Missing Exposure.  The logic being that 
 *     the missing pixels are overexposed in even the shorted exposure time images.
 *   </DIV> <BR>
 * 
 *   Black Point<BR>
 *   <DIV style="margin-left: 40px;">
 *     The minimum valid pixel value allowed in the raw exposure images. This value will be 
 *     considered as black (0.0) and any values below this in the input images will be 
 *     clamped to (0.0).
 *   </DIV> <BR>
 * 
 *   White Point<BR>
 *   <DIV style="margin-left: 40px;">
 *     The maximum valid pixel value allowed in the raw exposure images.  This value will be
 *     considered as white (1.0) and any values above this in the input images will be 
 *     clamped to (1.0).
 *   </DIV> 
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     This parameter determines the order in which the input raw exposure images are 
 *     combined in the final LatLon format HDR output image.  Input images are composited 
 *     from left-to-right (increasing longitude) based on increasing Order.  If this 
 *     parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 * 
 * By default, this Action executes the "Nuke4.6" binary.  This can be overridden by 
 * specifying an alternate binary with the NUKE_BINARY environmental variable in the 
 * Toolset used to run this Action plugin. On Windows, the Nuke binary name should 
 * include the ".exe" extension.<P> 
 * 
 * If no Exposure Times file is specified, this action will use the dcraw(1) program 
 * (http://www.cybercom.net/~dcoffin/dcraw) to extract exposure information from the CR2 
 * image header. 
 */
public 
class NukeMakeHDRAction
  extends NukeActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeMakeHDRAction() 
  {
    super("NukeMakeHDR", new VersionID("2.4.2"), "Temerity", 
	  "Converts a series of varying exposure raw digital images into a single " + 
          "high dynamica range (HDR) environment map."); 
  
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aLatLon); 
      choices.add(aEnvCross);

      ActionParam param = 
	new EnumActionParam
	(aOutputFormat,
	 "The format for the primary output HDR image. LatLon - Generates a latitute " + 
         "longitude cylindrical environment map.  EnvCross - Generates a unwrapped cube " + 
         "face environment map in the standard cross layout.", 
         aLatLon, choices);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new IntegerActionParam
	(aOutputSize,
	 "The resolution of the output HDR image data across a 90 degree field of view. " + 
         "For LatLon images, the full output resolution will be (Size*4 x Size*2) since " + 
         "these images cover 360 degrees horizontally and 180 degrees vertically.  For " + 
         "EnvCross images, each of the individual cube faces 90 covers degrees making the " + 
         "full output resolution (Size*3 x Size*4).", 
         256); 
      addSingleParam(param);
    } 
 

    {
      ActionParam param = 
	new LinkActionParam
	(aExposureTimes,
	 "The source node which provides a plain text file containing exposure times " + 
         "(in fractions of a second) for each of the raw input images.  The exposures " + 
         "should be listed one per line and in the same order as the exposure images in " + 
         "each input image sequence.  If specified, there must be exactly the same number " + 
         "of images in each of the (3) view input image sequences and each sequence must " + 
         "be in the same exposure duration order.  If not specified, the action will use " + 
         "dcraw(1) to extract exposure times from each input image individually in which " +
         "case the number and order of exposure images is arbitrary.", 
         null); 
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new DoubleActionParam
	(aMissingExposure,
	 "The exposure time (in fractions of a second) to give to pixels with zero " + 
         "coverage from the input raw images.  The pixel values for these missing " + 
         "pixels will be computed by scaling a white (1.0) value by Missing Exposure.  " + 
         "The logic being that the missing pixels are overexposed in even the shorted " +
         "exposure time images.", 
         0.0); 
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new DoubleActionParam
	(aBlackPoint,
	 "The minimum valid pixel value allowed in the raw exposure images. This value " + 
         "will be considered as black (0.0) and any values below this in the input images " +
         "will be clamped to (0.0).", 
         0.1); 
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new DoubleActionParam
	(aWhitePoint,
	 "The maximum valid pixel value allowed in the raw exposure images.  This value " + 
         "will be considered as white (1.0) and any values above this in the input images " + 
         "will be clamped to (1.0).", 
         0.9); 
      addSingleParam(param);
    } 
 
 
    /* parameter layout */ 
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aOutputFormat); 
      layout.addEntry(aOutputSize);
      layout.addSeparator();  
      layout.addEntry(aExposureTimes);
      layout.addEntry(aMissingExposure); 
      layout.addEntry(aBlackPoint);
      layout.addEntry(aWhitePoint);

      setSingleLayout(layout);  
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

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
	new IntegerActionParam
	(aOrder, 
	 "This parameter determines the order in which the input raw exposure images " + 
         "are combined in the final LatLon format HDR output image.  Input images are " + 
         "composited from left-to-right (increasing longitude) based on increasing Order. " + 
         "If this parameter is not set for a source node file sequence, it will be ignored.", 
	 100);
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
    /* output format */ 
    String outputFormat = getSingleStringParamValue(aOutputFormat, false);

    /* target output HDR image */ 
    Path targetPath = getPrimaryTargetPath(agenda, "hdr", "Final HDR Image");

    /* secondary (optional) HDR image paths */ 
    Path latlonPath = getDiagnosticPath(agenda, agenda.getPrimaryTarget(), "latlon"); 
    Path crossPath  = getDiagnosticPath(agenda, agenda.getPrimaryTarget(), "cross"); 
    
    /* the raw exposure image sequence paths and prefixes */ 
    ArrayList<FileSeq> rawSeqs = new ArrayList<FileSeq>(); 
    TreeMap<FileSeq,Path> rawPaths = new TreeMap<FileSeq,Path>();
    {
      MappedLinkedList<Integer,Path> paths = new MappedLinkedList<Integer,Path>(); 
      MappedLinkedList<Integer,FileSeq> seqs = new MappedLinkedList<Integer,FileSeq>();
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, aOrder);
	  addRawPaths(agenda, sname, fseq, order, paths, seqs);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, aOrder);
            addRawPaths(agenda, sname, fseq, order, paths, seqs);
	  }
	}
      }

      if(paths.isEmpty() || seqs.isEmpty()) 
	throw new PipelineException
	  ("No raw exposure images where specified using the per-source Order " +
           "parameter!"); 

      for(LinkedList<FileSeq> s : seqs.values()) 
        rawSeqs.addAll(s);

      int cnt = 0;
      for(LinkedList<Path> ps : paths.values()) {
        for(Path p : ps) { 
          rawPaths.put(rawSeqs.get(cnt), p); 
          cnt++;
        }
      }
    }

    /* secondary (optional) diagnostic HDR image paths indexed by raw exposure prefix */ 
    TreeMap<FileSeq,Path> comboPaths = new TreeMap<FileSeq,Path>(); 
    TreeMap<FileSeq,Path> wtsPaths = new TreeMap<FileSeq,Path>(); 
    TreeMap<FileSeq,Path> nofixPaths = new TreeMap<FileSeq,Path>(); 
    TreeMap<FileSeq,Path> fixPaths = new TreeMap<FileSeq,Path>(); 
    for(FileSeq fseq : rawSeqs) {
      addDiagnosticPath(agenda, fseq, "combo", comboPaths); 
      addDiagnosticPath(agenda, fseq, "wts", wtsPaths); 
      addDiagnosticPath(agenda, fseq, "nofix", nofixPaths);
      addDiagnosticPath(agenda, fseq, "fix", fixPaths); 
    }

    /* get exposure times */ 
    Range drange = new Range<Double>(0.0, null);
    double missingExposure = getSingleDoubleParamValue(aMissingExposure, drange); 
    TreeMap<FileSeq,ArrayList<Double>> exposures = new TreeMap<FileSeq,ArrayList<Double>>();
    {
      Path expPath = getPrimarySourcePath(aExposureTimes, agenda, "txt", "Exposure Times");
      if(expPath != null) {
        ArrayList<Double> times = new ArrayList<Double>(); 
        try {
          BufferedReader in = new BufferedReader(new FileReader(expPath.toFile()));
          while(true) {
            String line = in.readLine();
            if(line == null) 
              break;

            Matcher m = sExposurePat.matcher(line);
            if(m.matches()) { 
              String time = m.group(1);
              try {
                times.add(new Double(time));
              }
              catch(NumberFormatException ex) {
                throw new PipelineException
                  ("Bad exposure time (" + line + ") in file (" +  expPath + ")!");
              }
            }
          }

          in.close();
        }
        catch (IOException ex2) {
          throw new PipelineException
            ("Unable to read exposure times file (" + expPath + ") for Job " + 
             "(" + agenda.getJobID() + ")!\n" +
             ex2.getMessage());
        }

        for(FileSeq fseq : rawSeqs) {
          if(times.size() != fseq.numFrames()) 
            throw new PipelineException
              ("The number of exposure times (" + times.size() + ") found in file " + 
               "(" + expPath + ") is different than the number of input raw exposure " + 
               "images in source file sequence (" + fseq + ")!"); 
          exposures.put(fseq, times);
        }
      }
      else {
        for(FileSeq fseq : rawSeqs) {
          Path rpath = rawPaths.get(fseq);

          ArrayList<Double> times = new ArrayList<Double>(); 
          {
            ArrayList<String> args = new ArrayList<String>(); 
            args.add("-i"); 
            args.add("-v");
            for(Path path : fseq.getPaths()) {
              Path fullpath = new Path(rpath, path);
              args.add(fullpath.toOsString());
            }
            
            SubProcessLight proc = 
              new SubProcessLight(agenda.getJobID() + "-Exposures", "dcraw", args, 
                                  agenda.getEnvironment(), PackageInfo.sTempPath.toFile());
            try {
              proc.start();
              proc.join();
              if(!proc.wasSuccessful()) {
                throw new PipelineException
                  ("Failed to run dcraw(1) to extract exposure information from raw " + 
                   "image headers for the source images (" + fseq + "):\n\n" + 
                   proc.getStdOut() + "\n\n" + 
                   proc.getStdErr());
              }
              
              try {
                BufferedReader in = new BufferedReader(new StringReader(proc.getStdOut()));
                while(true) {
                  String line = in.readLine();
                  if(line == null) 
                    break;
                  
                  Matcher m = sShutterPat.matcher(line);
                  if(m.matches()) { 
                    String time = m.group(1);
                    if((time == null) || (time.length() == 0)) 
                      throw new PipelineException
                        ("Bad exposure time (" + line + ") in source images(" + fseq + ")!"); 
                    try {
                      times.add(new Double(time));
                    }
                    catch(NumberFormatException ex) {
                      throw new PipelineException
                        ("Bad exposure time (" + line + ") in source images(" + fseq + ")!");
                    }
                  }
                }
                
                in.close(); 
              }
              catch(IOException ex) {
                throw new PipelineException 
                  ("Failed to read exposure information from raw image headers " + 
                   "for the source images (" + fseq + "):\n\n" + 
                   proc.getStdOut() + "\n\n" + 
                   proc.getStdErr());
              }
            }
            catch(InterruptedException ex) {
              throw new PipelineException(ex);
            }
          }
          
          if(times.size() != fseq.numFrames()) 
            throw new PipelineException
              ("Somehow the number of exposure times (" + times.size() + ") extracted " + 
               "from the input raw exposure images did not match the number of images " + 
               "in source file sequence (" + fseq + ")!"); 
          exposures.put(fseq, times);
        }
      }

      for(FileSeq fseq : rawSeqs) {
        if(!exposures.containsKey(fseq))
          throw new PipelineException
            ("No exposure times found for input raw exposure images in source file " +
             "sequence (" + fseq + ")!"); 
      }

      // DEBUG 
      System.out.print("Exposure Times:\n"); 
      for(FileSeq fseq : exposures.keySet()) {  
        System.out.print("  " + fseq + ":\n"); 
        ArrayList<Double> times = exposures.get(fseq);
        int wk;
        for(wk=0; wk<fseq.numFrames(); wk++) 
          System.out.print("    " + fseq.getPath(wk) + " = " + times.get(wk) + "\n"); 
      }
      // DEBUG 
    }

    /* black/white points */ 
    double blackPoint = getSingleDoubleParamValue(aBlackPoint, drange); 
    double whitePoint = getSingleDoubleParamValue(aWhitePoint, drange); 

    /* compute common image resolutions */ 
    int size     = getSingleIntegerParamValue(aOutputSize, new Range<Integer>(1, null));
    int dsize    = size * 2; 
    float qwidth = ((float) dsize) * (2.0f/3.0f);
    int hwidth   = Math.round(qwidth * 2.0f); 
    int fwidth   = hwidth * 2;

    /* Nuke format lines for the common image resolutions */ 
    String sformat = ("\"" + dsize + " " + dsize + " 0 0 " + dsize + " " + dsize + " 1 \"");
    String hformat = ("\"" + hwidth + " " + dsize + " 0 0 " + hwidth + " " + dsize + " 1 \"");
    String fformat = ("\"" + fwidth + " " + dsize + " 0 0 " + fwidth + " " + dsize + " 1 \"");

    /* create a temporary Nuke script */ 
    Path script = new Path(createTemp(agenda, "nk"));
    try {    
      FileWriter out = new FileWriter(script.toFile()); 

      /* process exposures from each view */ 
      for(FileSeq fseq : rawSeqs) {
        String vtag = "__" + fseq.getFilePattern().getPrefix();

        /* compute weights for each raw image and scale them based on exposure time */ 
        int frame; 
        for(frame=0; frame<fseq.numFrames(); frame++) {
          String tag = (vtag + frame);
          Path path = fseq.getPath(frame); 
          Double exposure = exposures.get(fseq).get(frame);

          /* read the image */ 
          Path rpath = new Path(rawPaths.get(fseq), path);
          out.write
            ("Read {\n" +
             " inputs 0\n" +
             " file " + rpath.toOsString() + "\n" +
             " name Read" + tag + "\n" +
             "}\n");

          /* convert to latlon format */ 
          out.write
            ("SphericalTransform {\n" +
             " input \"Light Probe\"\n" +
             " full_format " + fformat + "\n" +
             " proxy_format " + fformat + "\n" + 
             " name LatLon" + tag + "\n" +
             "}\n" +
             "Crop {\n" +
             " box {0 0 " + hwidth + " " + dsize + "}\n" +
             " reformat true\n" +
             " crop false\n" +
             " name Crop" + tag + "\n" +
             "}\n" +
             "set SaveCrop" + tag + " [stack 0]\n");
             
          /* scale by (fraction of second) exposure time */  
          out.write
            ("Multiply {\n" +
             " value " + exposure + "\n" +
             " name Intensity" + tag + "\n" +
             "}\n" +
             "set SaveIntensity" + tag + " [stack 0]\n");
          
          /* apply color grade (black/white point) and convert to HSV color space */  
          out.write
            ("push $SaveCrop" + tag + "\n" +
             "Grade {\n" +
             " blackpoint " + ((frame == 0) ? 0.0 : blackPoint) + "\n" +
             " whitepoint " + whitePoint + "\n" +
             " name Grade" + tag + "\n" +
             "}\n" +
             "Colorspace {\n" +
             " colorspace_out HSV\n" +
             " name Colorspace" + tag + "\n" +
             "}\n");
          
          /* compute pixel weights based on value channel */ 
          out.write
            ("Expression {\n" +
             " temp_name0 inp\n" +
             " temp_expr0 b\n" +
             " temp_name1 blend\n" +
             " temp_expr1 0.5\n" +
             " temp_name2 result\n" +
             " temp_expr2 \"smoothstep(0, blend, inp)*(1-step(blend, inp)) + " + 
                           "smoothstep(0, blend, 1-inp)*step(1-blend, inp)\"\n" +
             " expr0 result\n" +
             " expr1 result\n" +
             " expr2 result\n" +
             " expr3 0\n" +
             " name WeightsA1\n" +
             "}\n" +
             "set SaveWeights" + tag + " [stack 0]\n");
        }

        /* sum the weights for all raw images for a view */ 
        {
          out.write("push 0\n"); 
          for(frame=0; frame<fseq.numFrames(); frame++) 
            out.write("push $SaveWeights" + vtag + frame + "\n"); 
          out.write
            ("Merge2 {\n" +
             " inputs " + fseq.numFrames() + "\n" +
             " operation plus\n" +
             " name CombineWts" + vtag + "\n" +
             "}\n" +
             "set SaveCombineWts" + vtag + " [stack 0]\n"); 
          
          /* write them out if a target secondary sequences exists */ 
          Path path = wtsPaths.get(fseq);
          if(path != null) {
            out.write
              ("Write {\n" +
               " file " + path.toOsString() + "\n" +
               " file_type hdr\n" +
               " name WriteWts" + vtag + "\n" +
               "}\n");
          }
        }

        /* compute normalized weights for each raw image and scale by these weights */ 
        for(frame=0; frame<fseq.numFrames(); frame++) {
          String tag = (vtag + frame);
          out.write
            ("push $SaveWeights" + tag + "\n" +
             "push $SaveCombineWts" + vtag + "\n" +
             "Merge2 {\n" +
             " inputs 2\n" +
             " operation divide\n" +
             " name Normal" + tag + "\n" +
             "}\n" +
             "set SaveNormal" + tag + " [stack 0]\n" +
             "push $SaveIntensity" + tag + "\n" +
             "Merge2 {\n" +
             " inputs 2\n" +
             " operation multiply\n" +
             " name NormalizedIntensity" + tag + "\n" +
             "}\n" +
             "set SaveNormalizedExposure" + tag + " [stack 0]\n"); 
        }

        /* combine the weighted raw images */ 
        {
          out.write("push 0\n"); 
          for(frame=0; frame<fseq.numFrames(); frame++) 
            out.write("push $SaveNormalizedExposure" + vtag + frame + "\n"); 
          out.write
            ("Merge2 {\n" +
             " inputs " + fseq.numFrames() + "\n" +
             " operation plus\n" +
             " name NoFix" + vtag + "\n" +
             "}\n" + 
             "set SaveNoFix" + vtag + " [stack 0]\n");

          /* write them out if a target secondary sequences exists */ 
          Path path = nofixPaths.get(fseq);
          if(path != null) {
            out.write
              ("Write {\n" +
               " file " + path.toOsString() + "\n" +
               " file_type hdr\n" +
               " name WriteNoFix" + vtag + "\n" +
               "}\n");
          }
        }
          
        /* combine normalized weights */ 
        {
          out.write("push 0\n"); 
          for(frame=0; frame<fseq.numFrames(); frame++) 
            out.write("push $SaveNormal" + vtag + frame + "\n"); 
          out.write
            ("Merge2 {\n" +
             " inputs " + fseq.numFrames() + "\n" +
             " operation plus\n" +
             " name CombineNormal" + vtag + "\n" +
             "}\n" + 
             "set SaveCombineNormal" + vtag + " [stack 0]\n" + 
             "Invert {\n" + 
             " name Invert" + vtag + "\n" + 
             "}\n" + 
             "Multiply {\n" + 
             " value " + missingExposure + "\n" + 
             " name Fix" + vtag + "\n" + 
             "}\n" + 
             "set SaveFix" + vtag + " [stack 0]\n"); 

          /* write them out if a target secondary sequences exists */ 
          Path path = fixPaths.get(fseq);
          if(path != null) {
            out.write
              ("Write {\n" + 
               " file " + path.toOsString() + "\n" + 
               " file_type hdr\n" + 
               " name WriteFix" + vtag + "\n" + 
               "}\n"); 
          }
        }

        /* generate final combined HDR image for a view */ 
        {
          out.write
            ("push 0\n" + 
             "push $SaveFix" + vtag + "\n" + 
             "push $SaveNoFix" + vtag + "\n" + 
             "Merge2 {\n" + 
             " inputs 2\n" + 
             " operation plus\n" + 
             " name FinalCombined" + vtag + "\n" + 
             "}\n" + 
             "set SaveFinalCombined" + vtag + " [stack 0]\n"); 

          /* write them out if a target secondary sequences exists */ 
          Path path = comboPaths.get(fseq);
          if(path != null) {
            out.write
              ("Write {\n" + 
               " file " + path.toOsString() + "\n" + 
               " file_type hdr\n" + 
               " name WriteCombo" + vtag + "\n" + 
               "}\n"); 
          }
        }
      }

      /* create a mask to trim pixels outside the 180 degree field of view */ 
      out.write
        ("Constant {\n" + 
         " inputs 0\n" + 
         " channels rgb\n" + 
         " full_format " + sformat + "\n" +
         " proxy_format " + sformat + "\n" +
         " name ConstantBlackSquare\n" + 
         "}\n" + 
         "Radial {\n" + 
         " cliptype format\n" + 
         " area {0 0 " + dsize + " " + dsize + "}\n" + 
         " softness 0.2\n" + 
         " name RadialMask\n" + 
         "}\n" + 
         "SphericalTransform {\n" + 
         " input \"Light Probe\"\n" + 
         " full_format " + fformat + "\n" + 
         " proxy_format " + fformat + "\n" + 
         " name LatLonMask\n" + 
         "}\n" + 
         "set SaveLatLonMask [stack 0]\n"); 

      /* position the masks at 120 degree intervals along the longitude */ 
      {
        float pos;
        int wk; 
        for(wk=0, pos=-qwidth; wk<5; wk++, pos+=qwidth) {
          out.write
            ("push $SaveLatLonMask\n" + 
             "Position {\n" + 
             " translate {" + pos + " 0}\n" + 
             " name PositionMask" + wk + "\n" + 
             "}\n" + 
             "set SavePositionMask" + wk + " [stack 0]\n");
        }
      }

      /* combine all masks */ 
      out.write
        ("push 0\n" + 
         "push $SavePositionMask4\n" + 
         "push $SavePositionMask3\n" + 
         "push $SavePositionMask2\n" + 
         "push $SavePositionMask1\n" + 
         "push $SavePositionMask0\n" + 
         "Merge2 {\n" + 
         " inputs 5\n" + 
         " operation plus\n" + 
         " name CombineMasks\n" + 
         "}\n" + 
         "set SaveCombineMasks [stack 0]\n");

      /* normalize the masks */ 
      {
        int wk; 
        for(wk=0; wk<5; wk++) {
          out.write
            ("push 0\n" + 
             "push $SavePositionMask" + wk + "\n" + 
             "push $SaveCombineMasks\n" + 
             "Merge2 {\n" + 
             " inputs 2\n" + 
             " operation divide\n" + 
             " name NormalMask" + wk + "\n" + 
             "}\n" + 
             "set SaveNormalMask" + wk + " [stack 0]\n");
        }
      }
      
      /* position and mask the combined view HDR images */ 
      {
        ArrayList<FileSeq> inputs = new ArrayList<FileSeq>(); 
        inputs.add(rawSeqs.get(2));
        inputs.add(rawSeqs.get(0));
        inputs.add(rawSeqs.get(1));
        inputs.add(rawSeqs.get(2));
        inputs.add(rawSeqs.get(0));
        
        float pos;
        int wk; 
        for(wk=0, pos=-qwidth; wk<5; wk++, pos+=qwidth) {
          FileSeq fseq = inputs.get(wk);
          String vtag = "__" + fseq.getFilePattern().getPrefix();

          out.write
            ("push 0\n" + 
             "push $SaveFinalCombined" + vtag + "\n" + 
             "Position {\n" + 
             " translate {" + pos + " 0}\n" + 
             " name PositionCombo" + wk + "\n" + 
             "}\n" + 
             "Reformat {\n" + 
             " full_format " + fformat + "\n" + 
             " proxy_format " + fformat + "\n" + 
             " resize none\n" + 
             " center false\n" + 
             " name ReformatCombo" + wk + "\n" + 
             "}\n" + 
             "push $SaveNormalMask" + wk + "\n" + 
             "Merge2 {\n" + 
             " inputs 2\n" + 
             " operation multiply\n" + 
             " name NormalizedCombo" + wk + "\n" + 
             "}\n" + 
             "set SaveNormalizedCombo" + wk + " [stack 0]\n");
        }
      }

      /* combine the views into a final LatLon format HDR image */ 
      {
        out.write
          ("push 0\n" + 
           "push $SaveNormalizedCombo4\n" + 
           "push $SaveNormalizedCombo3\n" + 
           "push $SaveNormalizedCombo2\n" + 
           "push $SaveNormalizedCombo1\n" + 
           "push $SaveNormalizedCombo0\n" + 
           "Merge2 {\n" + 
           " inputs 5\n" + 
           " operation plus\n" + 
           " name CombineCombos\n" + 
           "}\n" + 
           "Crop {\n" + 
           " box {0 0 " + (size*4) + " " + (size*2) + "}\n" + 
           " reformat true\n" + 
           " crop false\n" + 
           " name FinalLatLon\n" + 
           "}\n" + 
           "set SaveFinalLatLon [stack 0]\n");
        
        /* write it out.. */ 
        Path path = null;
        if(outputFormat.equals(aLatLon)) 
          path = targetPath;
        else if(latlonPath != null)
          path = latlonPath; 
        if(path != null) 
          out.write
            ("push 0\n" + 
             "push $SaveFinalLatLon\n" + 
             "Write {\n" + 
             " file " + path.toOsString() + "\n" + 
             " file_type hdr\n" + 
             " name WriteLatLon\n" + 
             "}\n");
      }
      
      /* optionally generate the cubed faced environment cross image */ 
      {
        Path path = null;
        if(outputFormat.equals(aEnvCross)) 
          path = targetPath;
        else if(crossPath != null)
          path = crossPath; 
        if(path != null) {
          ArrayList<String> faces = new ArrayList<String>(); 
          faces.add("NegX");
          faces.add("PosX");
          faces.add("NegY");
          faces.add("PosY");
          faces.add("NegZ");
          faces.add("PosZ");

          ArrayList<String> orients = new ArrayList<String>(); 
          orients.add("out_ry 270");
          orients.add("out_ry 90");
          orients.add("out_rx -90");
          orients.add("out_rx 90");
          orients.add("out_rx -180");
          orients.add(null);

          ArrayList<Tuple2i> deltas = new ArrayList<Tuple2i>();
          deltas.add(new Tuple2i(size*2, size*2));
          deltas.add(new Tuple2i(0, size*2));
          deltas.add(new Tuple2i(size, size));
          deltas.add(new Tuple2i(size, size*3));
          deltas.add(new Tuple2i(size, 0));
          deltas.add(new Tuple2i(size, size*2));

          String format = 
            ("\"" + size + " " + size + " 0 0 " + size + " " + size + " 1 \"");
    
          int wk; 
          for(wk=0; wk<6; wk++) {
            String face = faces.get(wk);
            String orient = orients.get(wk);
            Tuple2i delta = deltas.get(wk);
            out.write
              ("push 0\n" + 
               "push $SaveFinalLatLon\n" + 
               "SphericalTransform {\n" + 
               " input \"Lat Long map\"\n" + 
               " output Cube\n" +
               ((orient != null) ? (" " + orient + "\n") : "") + 
               " full_format " + format + "\n" + 
               " proxy_format " + format + "\n" + 
               " name " + face + "\n" + 
               "}\n" + 
               "BlackOutside {\n" + 
               " name BlackOutside" + face + "\n" + 
               "}\n" + 
               "Position {\n" + 
               " translate {" + delta.x() + " " + delta.y() + "}\n" + 
               " name Position" + face + "\n" + 
               "}\n" + 
               "set SavePosition" + face + " [stack 0]\n");
          }
          
          String cformat = 
            ("\""+(size*3)+" "+(size*4)+" 0 0 "+(size*3)+" "+(size*4)+" 1 \"");

          out.write
            ("push 0\n" + 
             "push $SavePositionNegX\n" + 
             "push $SavePositionPosX\n" + 
             "push $SavePositionNegY\n" + 
             "push $SavePositionPosY\n" + 
             "push $SavePositionNegZ\n" + 
             "push $SavePositionPosZ\n" + 
             "Constant {\n" + 
             " inputs 0\n" + 
             " channels rgb\n" + 
             " full_format " + cformat + "\n" + 
             " proxy_format " + cformat + "\n" + 
             " name EnvCrossBg\n" + 
             "}\n" + 
             "Merge2 {\n" + 
             " inputs 7\n" + 
             " name EnvCross\n" + 
             "}\n" + 
             "Write {\n" + 
             " file " + path.toOsString() + "\n" + 
             " file_type hdr\n" + 
             " name WriteEnvCross\n" + 
             "}\n");
        }
      }

      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to write temporary Nuke script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    /* create the process to run the action */ 
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-nx"); 
      args.add(script.toString()); 
      args.add("1"); 

      return createSubProcess(agenda, getNukeProgram(agenda), args, agenda.getEnvironment(), 
                              agenda.getTargetPath().toFile(), outFile, errFile);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A helper method for generating raw exposure paths and names.
   */ 
  private void 
  addRawPaths
  (
   ActionAgenda agenda, 
   String sname, 
   FileSeq fseq, 
   Integer order, 
   MappedLinkedList<Integer,Path> paths,
   MappedLinkedList<Integer,FileSeq> seqs
  )
    throws PipelineException 
  {
    if(order == null) 
      return;
    
    FilePattern fpat = fseq.getFilePattern();
    String suffix = fpat.getSuffix();
    if(!fseq.hasFrameNumbers() || (suffix == null)) 
      throw new PipelineException
        ("The " + getName() + " Action requires that the file sequence (" + fseq + ") of " + 
         "the source node (" + sname + ") selected for evaluation must be a sequence of " + 
         "raw exposure images!"); 

    NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
    paths.put(order, new Path(PackageInfo.sProdPath, snodeID.getWorkingParent()));
    seqs.put(order, fseq); 
  }

  /**
   * A helper method for generating diagnositc image paths.
   */ 
  private Path
  getDiagnosticPath
  (
   ActionAgenda agenda, 
   FileSeq fseq,    
   String comp
  )
  { 
    FileSeq sfseq = new FileSeq(fseq.getFilePattern().getPrefix() + "_" + comp, "hdr"); 
    if(!agenda.getSecondaryTargets().contains(sfseq)) 
      return null;
    
    Path dpath = new Path(agenda.getNodeID().getWorkingParent(), sfseq.getPath(0)); 
    return (new Path(PackageInfo.sProdPath, dpath)); 
  }
  
  /**
   * A helper method for generating and storing diagnositc image paths for each view.
   */ 
  private void 
  addDiagnosticPath
  (
   ActionAgenda agenda, 
   FileSeq fseq, 
   String comp, 
   TreeMap<FileSeq,Path> paths
  )
  { 
    Path dpath = getDiagnosticPath(agenda, fseq, comp); 
    if(dpath != null) 
      paths.put(fseq, dpath); 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static Pattern sExposurePat = Pattern.compile("[ \\t]*([0-9]+.[0-9]+)[ \\t]*"); 
  private static Pattern sShutterPat  = Pattern.compile("Shutter: 1/([0-9]+.[0-9]+) sec"); 

  private static final long serialVersionUID = -3393308960040001410L;

  public static final String aOutputFormat    = "OutputFormat";
  public static final String aLatLon          = "LatLon";
  public static final String aEnvCross        = "EnvCross";
  public static final String aOutputSize      = "OutputSize";
  public static final String aExposureTimes   = "ExposureTimes";
  public static final String aMissingExposure = "MissingExposure";
  public static final String aBlackPoint      = "BlackPoint";
  public static final String aWhitePoint      = "WhitePoint";
  public static final String aOrder           = "Order"; 

}
