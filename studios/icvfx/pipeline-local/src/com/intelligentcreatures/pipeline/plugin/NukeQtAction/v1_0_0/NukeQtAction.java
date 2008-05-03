package com.intelligentcreatures.pipeline.plugin.NukeQtAction.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.*;

import java.io.*;
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   N U K E   Q T   A C T I O N                                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates a QuickTime movie using Nuke from either a sequence of images or by evaluating 
 * a given Nuke script which generates the source images. <P> 
 * 
 * An optional audio soundtrack can also be specified for the generated movie.  When reading
 * from a Nuke script, this action can be used in conjunction with the NukeSubstComp action 
 * or other dynamic Nuke script generating actions to avoid the need of pre-rendering source 
 * images. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images used to create the movie. 
 *   </DIV> <BR>
 * 
 *   Generate Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Nuke script responsible for generating the source
 *     images for the QuickTime movie. 
 *   </DIV> <BR>
 * 
 *   Reformat Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the optional Nuke script fragment to be appended 
 *     to the Nuke Read node created to load the source images before generating the
 *     QuickTime movie.
 *   </DIV> <BR>
 *   <P> 
 * 
 * 
 *   Codec <BR> 
 *   <DIV style="margin-left: 40px;">
 *     The name of the QuickTime codec to use to encode the images.
 *   </DIV>
 * 
 *   Fast Start <BR> 
 *   <DIV style="margin-left: 40px;">
 *     Whether to flatten the QuickTime movie so it can be played while still downloading.
 *   </DIV>
 * 
 *   Quality <BR>
 *   <DIV style="margin-left: 40px;">
 *     The QuickTime video compression quality.
 *   </DIV>
 *
 *   Keyframe Rate <BR>
 *   <DIV style="margin-left: 40px;">
 *     The minimum frequency of keyframes by the encoding codec.  Set to (0) to allow codec
 *     to choose rate automatically.
 *   </DIV>
 *   <P> 
 * 
 * 
 *   FPS <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of image frames per second.
 *   </DIV>
 *   <P> 
 *   
 * 
 *   Audio Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The optional source node which contains the audio soundtrack for the movie. 
 *   </DIV> <BR>
 * 
 *   Audio Offset <BR>
 *   <DIV style="margin-left: 40px;">
 *     The offset of the source audio soundtrack to the source images.
 *   </DIV>
 * 
 *   Audio Units <BR>
 *   <DIV style="margin-left: 40px;">
 *     The time units to use when interpreting the AudioOffset parameter.
 *   </DIV>
 * </DIV> <P> 
 * 
 * By default, this Action executes the "Nuke4.6" binary.  This can be overridden by 
 * specifying an alternate binary with the NUKE_BINARY environmental variable in the 
 * Toolset used to run this Action plugin. On Windows, the Nuke binary name should 
 * include the ".exe" extension.<P> 
 */
public 
class NukeQtAction
  extends NukeActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeQtAction() 
  {
    super("NukeQt", new VersionID("1.0.0"), "ICVFX", 
	  "Generates a smaller version of a single image from a sequence of images.");

    {
      ActionParam param = 
	new LinkActionParam
	(aImageSource,
	 "The source which contains the images used to create the movie.", 
	 null);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new LinkActionParam
	(aGenerateScript,
	 "The source node which contains the Nuke script responsible for generating the " + 
         "source images for the QuickTime movie.", 
	 null);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new LinkActionParam
	(aReformatScript,
	 "The source node which contains the optional Nuke script fragment to be appended " + 
         "to the Nuke Read node created to load the source images before generating the " +
         "QuickTime movie.", 
	 null);
      addSingleParam(param);
    } 

    {
      pCodecs = new TreeMap<String,String>();
      pCodecs.put("Animation", "rle");
      pCodecs.put("Apple Intermediate", "icod");
      pCodecs.put("Apple Pixlet Video", "pxlt");
      pCodecs.put("Avid 1:1x", "AV1x");
      pCodecs.put("Avid DNxHD", "AVdn");
      pCodecs.put("Avid DV", "AVdv");
      pCodecs.put("Avid DV100", "AVd1");
      pCodecs.put("Avid Meridien Compressed", "AVDJ");
      pCodecs.put("Avid Meridien Uncompressed", "AVUI");
      pCodecs.put("Avid Packed", "AVup");
      pCodecs.put("BMP", "WRLE");
      pCodecs.put("Cinepak", "cvid");
      pCodecs.put("Component Video", "yuv2");
      pCodecs.put("DV - PAL", "dvcp");
      pCodecs.put("DV/DVCPRO - NTSC", "dvc");
      pCodecs.put("DVCPRO - PAL", "dvpp");
      pCodecs.put("DVCPRO HD 1080i50", "dvh5");
      pCodecs.put("DVCPRO HD 1080i60", "dvh6");
      pCodecs.put("DVCPRO HD 720p50", "dvhq");
      pCodecs.put("DVCPRO HD 720p60", "dvhp");
      pCodecs.put("DVCPRO50 - NTSC", "dv5n");
      pCodecs.put("DVCPRO50 - PAL", "dv5p");
      pCodecs.put("Graphics", "smc");
      pCodecs.put("H.261", "h261");
      pCodecs.put("H.263", "h263");
      pCodecs.put("H.264", "avc1");
      pCodecs.put("JPEG 2000", "mjp2");
      pCodecs.put("Motion JPEG A", "mjpa"); 
      pCodecs.put("Motion JPEG B", "mjpb");
      pCodecs.put("MPEG IMX 525/60 (30 Mb/s)", "mx3n");
      pCodecs.put("MPEG IMX 525/60 (40 Mb/s)", "mx4n");
      pCodecs.put("MPEG IMX 525/60 (50 Mb/s)", "mx5n");
      pCodecs.put("MPEG IMX 625/50 (30 Mb/s)", "mx3p");
      pCodecs.put("MPEG IMX 625/50 (40 Mb/s)", "mx4p");
      pCodecs.put("MPEG IMX 625/50 (50 Mb/s)", "mx5p");
      pCodecs.put("MPEG-4 Video", "mp4v");
      pCodecs.put("None", "raw");
      pCodecs.put("Photo - JPEG", "jpeg");
      pCodecs.put("Planar RGB", "8BPS");
      pCodecs.put("PNG", "png");
      pCodecs.put("Sorenson Video", "SVQ1");
      pCodecs.put("Sorenson Video 3", "SVQ3");
      pCodecs.put("TGA", "tga");
      pCodecs.put("TIFF", "tiff");
      pCodecs.put("Uncompressed 10-bit 4:2:2", "v210");
      pCodecs.put("Uncompressed 8-bit 4:2:2", "2vuy");
      pCodecs.put("VC H.263", "h263");
      pCodecs.put("Video", "rpza");
    
      ActionParam param = 
        new EnumActionParam
        (aCodec,
         "The name of the QuickTime codec to use to encode the images.",
         "Motion JPEG A", new ArrayList<String>(pCodecs.keySet()));
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(aFastStart,
	 "Whether to flatten the QuickTime movie so it can be played while still " + 
         "downloading.", 
         true); 
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aMin); 
      choices.add(aLow); 
      choices.add(aNormal); 
      choices.add(aHigh); 
      choices.add(aMax); 
      choices.add(aLossless); 
      
      ActionParam param = 
	new EnumActionParam
	(aQuality,
	 "The QuickTime video compression quality.", 
	 aNormal, choices);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	(aKeyframeRate, 
	 "The minimum frequency of keyframes by the encoding codec.  Set to (0) to allow " + 
         "codec to choose rate automatically.", 
	 1);
      addSingleParam(param);
    }


    {
      ActionParam param = 
	new DoubleActionParam
	(aFPS,
	 "The number of image frames per second.",
	 24.0);
      addSingleParam(param);
    }


    {
      ActionParam param = 
	new LinkActionParam
	(aAudioSource,
	 "The optional source node which contains the audio soundtrack for the movie.", 
	 null);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new DoubleActionParam
	(aAudioOffset,
	 "The offset of the source audio soundtrack to the source images.", 
	 0.0);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aSeconds); 
      choices.add(aFrames); 

      ActionParam param = 
	new EnumActionParam
	(aAudioUnits,
	 "The time units to use when interpreting the AudioOffset parameter.", 
	 aFrames, choices);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageSource);  
      layout.addEntry(aGenerateScript);   
      layout.addEntry(aReformatScript);    
      layout.addSeparator(); 
      layout.addEntry(aCodec);  
      layout.addEntry(aFastStart); 
      layout.addEntry(aQuality);  
      layout.addEntry(aKeyframeRate); 
      layout.addEntry(aFPS);    
      layout.addSeparator(); 
      layout.addEntry(aAudioSource); 
      layout.addEntry(aAudioOffset); 
      layout.addEntry(aAudioUnits); 

      setSingleLayout(layout);  
    }

    removeSupport(OsType.Unix);
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
    /* target quicktime movie */
    Path targetPath = null;
    {
      ArrayList<String> suffixes = new ArrayList<String>();
      suffixes.add("qt");
      suffixes.add("mov");

      targetPath = getPrimaryTargetPath(agenda, suffixes, "QuickTime Movie");
    }

    /* source images */ 
    Path sourcePath = null;
    FileSeq sourceSeq = null;
    {
      String sname = getSingleStringParamValue(aImageSource); 
      if(sname != null) {
        FileSeq fseq = agenda.getPrimarySource(sname);
        if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the " + aImageSource + " (" + sname + ") was not one of the " + 
             "source nodes!");
        
        sourceSeq = fseq;
        
        NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
        sourcePath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
      }
    }
    
    /* the source Nuke scripts */
    Path generateScript = null;
    Path reformatScript = null;
    {
      ArrayList<String> suffixes = new ArrayList<String>();
      suffixes.add("nk");
      suffixes.add("nuke");

      generateScript = getPrimarySourcePath(aGenerateScript, agenda, suffixes, "Nuke script");
      reformatScript = getPrimarySourcePath(aReformatScript, agenda, suffixes, "Nuke script");
    }

    /* sanity check... */ 
    if((sourcePath == null) && (generateScript == null)) 
      throw new PipelineException
        ("At least one of " + aImageSource + " or " + aGenerateScript + " paramaters must" + 
         "be specified!"); 

    if((sourcePath != null) && (generateScript != null)) 
      throw new PipelineException
        ("Only one of " + aImageSource + " or " + aGenerateScript + " paramaters can be " + 
         "specified!"); 
  
    /* what version of Nuke are we running? */ 
    double nukeVersion = getNukeProgramVersion(agenda);

    /* lookup codec name */ 
    String codec = null;
    {
      String title = getSingleStringParamValue(aCodec);
      if(title == null) 
        throw new PipelineException
          ("Somehow there was no value for the " + aCodec + " parameter!"); 

      if(nukeVersion < 4.8) {
        codec = ("\"" + title + "\"");
      }
      else {
        codec = pCodecs.get(title); 
        if(codec == null)
          throw new PipelineException
            ("Unknown " + aCodec + " (" + title + ") specified!");
      }
    }

    /* create a temporary Nuke script */ 
    Path script = new Path(createTemp(agenda, "nk"));
    try {    
      FileWriter out = new FileWriter(script.toFile()); 

      /* read the source images in */
      if(sourcePath != null) {
        FilePattern fpat = sourceSeq.getFilePattern(); 
        FrameRange range = sourceSeq.getFrameRange(); 
        out.write("Read {\n" + 
                  " inputs 0\n" + 
                  " file " + sourcePath + "/" + toNukeFilePattern(fpat) + "\n" +
                  " first " + range.getStart() + "\n" + 
                  " last " + range.getEnd() + "\n" + 
                  " name READ\n" + 
                  "}\n");
      }

      /* append the images source script */
      if(generateScript != null) 
        catScript(out, generateScript); 

      /* append the optional reformat script */ 
      if(reformatScript != null) 
        catScript(out, reformatScript); 

      /* write the quicktime out */
      {
        out.write("Write {\n" + 
                  " file " + targetPath + "\n" +
                  " file_type mov\n" + 
                  " codec " + codec + "\n"); 
        
        boolean flatten = getSingleBooleanParamValue(aFastStart);
        if(!flatten)
          out.write(" Flatten false\n"); 
        
        double fps = getSingleDoubleParamValue(aFPS); 
        out.write(" fps " + fps + "\n"); 
        
        String quality = getSingleStringParamValue(aQuality); 
        if(!quality.equals(aNormal))
          out.write(" quality " + quality + "\n"); 
       
        int keyframe = getSingleIntegerParamValue(aKeyframeRate);
        if(keyframe != 1) 
          out.write(" keyframerate " + keyframe + "\n"); 
 
        Path soundPath = getPrimarySourcePath(aAudioSource, agenda, "Audio Soundtrack");
        if(soundPath != null) {
          out.write(" audiofile " + soundPath + "\n"); 

          double offset = getSingleDoubleParamValue(aAudioOffset); 
          if(nukeVersion >= 4.8) {
            out.write(" audio_offset " + offset); 
            String units = getSingleStringParamValue(aAudioUnits); 
            if(!units.equals(aSeconds)) 
              out.write(" units " + units + "\n"); 
          }
        }

        out.write(" name WRITE\n" + 
                  "}\n");
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
      args.add(toNukeFrameRange(sourceSeq.getFrameRange()));

      return createSubProcess(agenda, getNukeProgram(agenda), args, agenda.getEnvironment(), 
                              agenda.getTargetPath().toFile(), outFile, errFile);
    }
  }  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Concatenate the given Nuke script.
   */ 
  private void 
  catScript
  (
   FileWriter out,
   Path script
  ) 
    throws PipelineException
  {
    try {
      BufferedReader in = new BufferedReader(new FileReader(script.toFile())); 
      try {
        while(true) {
          String line = in.readLine();
          if(line == null) 
            break;
          
          out.write(line + "\n"); 
        }
        
        out.write("\n\n"); 
      }
      finally {
        in.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to read the input Nuke script file (" + script + ")!\n" +
         ex.getMessage());
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  private static final long serialVersionUID = -8536905407833471576L;
  public static final String aImageSource    = "ImageSource";
  public static final String aGenerateScript = "GenerateScript";
  public static final String aReformatScript = "ReformatScript";
  public static final String aPresets        = "Presets";
  public static final String aCodec          = "Codec";
  public static final String aFastStart      = "FastStart";
  public static final String aQuality        = "Quality";
  public static final String aMin            = "Min";
  public static final String aLow            = "Low";
  public static final String aNormal         = "Normal";
  public static final String aHigh           = "High";
  public static final String aMax            = "Max";
  public static final String aLossless       = "Lossless";
  public static final String aKeyframeRate   = "KeyframeRate";
  public static final String aFPS            = "FPS";
  public static final String aAudioSource    = "AudioSource";
  public static final String aAudioOffset    = "AudioOffset";
  public static final String aAudioUnits     = "AudioUnits";
  public static final String aSeconds        = "Seconds"; 
  public static final String aFrames         = "Frames";
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A table of QuickTime codec 4-character codes indexed by a more human readable name.
   * Before Nuke-4.8, the file format contained the human readable names but now cotains
   * the 4-character codec names.  This table allows us to present the human readable 
   * names to the users while doing the Nuke version specific right thing underneath.
   */ 
  private TreeMap<String,String>  pCodecs; 

}
