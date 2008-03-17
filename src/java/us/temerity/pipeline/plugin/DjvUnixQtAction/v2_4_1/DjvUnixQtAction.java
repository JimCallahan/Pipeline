// $Id: DjvUnixQtAction.java,v 1.1 2008/03/17 22:55:57 jim Exp $

package us.temerity.pipeline.plugin.DjvUnixQtAction.v2_4_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.*;

import java.io.*;
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   D J V   U N I X   Q T   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates a QuickTime movie from a sequence of images using 
 * <A HREF="http://djv.sourceforge.net/index.html">DJV Imaging</A>.<P> 
 * 
 * QuickTime generation is performed using the <A HREf="http://ffmpeg.mplayerhq.hu">FFmpeg<A>
 * and <A HREF="http://libquicktime.sourceforge.net">libquicktime</A?> which have a different
 * set of capabilities than Apple's official QuickTime library available on Mac OS X and 
 * Windows systems.  Use the DjvQt Action on those systems. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images used to create the movie. 
 *   </DIV> <BR>
 * 
 *   Codec <BR> 
 *   <DIV style="margin-left: 40px;">
 *     The name of the QuickTime codec to use to encode the images.
 *   </DIV>
 * 
 *   FPS <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of image frames per second.
 *   </DIV>
 * </DIV> <P> 
 * 
 * See the documentation for <A HREF="http://djv.sourceforge.net/djv_convert.html">
 * djv_convert</A> and the <A HREF="http://djv.sourceforge.net/image_io.html#quicktime">
 * QuickTime</A> plugin for details on the underlying capabilities of DJV Imaging used by 
 * this action.
 */
public 
class DjvUnixQtAction
  extends DjvActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DjvUnixQtAction() 
  {
    super("DjvUnixQt", new VersionID("2.4.1"), "Temerity", 
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
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("raw");                  
      choices.add("rawalpha");            
      choices.add("v308");                 
      choices.add("v408");                 
      choices.add("v410");                 
      choices.add("yuv2");                 
      choices.add("yuv4");                 
      choices.add("yv12");                 
      choices.add("2vuy");                 
      choices.add("v210");                 
      choices.add("ffmpeg_mpg4");          
      choices.add("ffmpeg_msmpeg4v3");     
      choices.add("ffmpeg_h263");          
      choices.add("ffmpeg_h263p");         
      choices.add("ffmpeg_mjpg");          
      choices.add("ffmpeg_dv_ntsc");       
      choices.add("ffmpeg_dv_pal");        
      choices.add("ffmpeg_dv50_pal");      
      choices.add("ffmpeg_dv50_ntsc");     
      choices.add("png");                  
      choices.add("pngalpha");            
      choices.add("rtjpeg");               
      choices.add("jpeg");                
      choices.add("mjpa");                 
      choices.add("ffmpeg_dv_avi");        
      choices.add("ffmpeg_ffvhuff");       

      ActionParam param = 
        new EnumActionParam
        (aCodec,
         "The name of the QuickTime codec to use to encode the images.",
         "jpeg", choices);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("1");
      choices.add("3");
      choices.add("6");
      choices.add("12");
      choices.add("15");
      choices.add("16");
      choices.add("18");
      choices.add("23.98");
      choices.add("24");
      choices.add("25");
      choices.add("29.97");
      choices.add("30");
      choices.add("50");
      choices.add("59.94");
      choices.add("60");
      choices.add("120");

      ActionParam param = 
        new EnumActionParam
	(aFPS,
	 "The number of image frames per second.",
         "24", choices); 
      addSingleParam(param);
    } 

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageSource);  
      layout.addEntry(aCodec);  
      layout.addEntry(aFPS); 

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
    Path targetPath = getPrimaryTargetPath(agenda, getQtExtensions(), "QuickTime Movie");

    /* source images */ 
    String sourceImages = null;
    {
      String sname = getSingleStringParamValue(aImageSource); 
      if(sname == null) 
        throw new PipelineException
          ("The " + aImageSource + " was not specified!"); 
        
      FileSeq fseq = agenda.getPrimarySource(sname);
      if(fseq == null) 
        throw new PipelineException
          ("Somehow the " + aImageSource + " (" + sname + ") was not one of the " + 
           "source nodes!");
      
      NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
      Path spath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());      
      Path path = new Path(spath, toDjvFileSeq(fseq));

      sourceImages = path.toOsString();
    }

    /* lookup codec name */ 
    String codec = getSingleStringParamValue(aCodec);
    if(codec == null) 
      throw new PipelineException
        ("Somehow there was no value for the " + aCodec + " parameter!"); 

    /* playback speed */ 
    String fps = getSingleStringParamValue(aFPS); 

    /* create the process to run the action */ 
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add(sourceImages); 
      args.add(targetPath.toOsString()); 
      args.add("-io_save");
      args.add("QuickTime");
      args.add("codec");
      args.add(codec);
      args.add("-speed"); 
      args.add(fps);

      return createSubProcess(agenda, "djv_convert", args, outFile, errFile);
    }
  }  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -7626133505376912609L;

  public static final String aImageSource = "ImageSource";
  public static final String aCodec       = "Codec";
  public static final String aFPS         = "FPS";
  
}
