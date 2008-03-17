// $Id: DjvQtAction.java,v 1.1 2008/03/17 23:20:15 jim Exp $

package us.temerity.pipeline.plugin.DjvQtAction.v2_4_1;

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
 * QuickTime generation is performed using the official Apple QuickTime library on Mac OS X
 * and Windows platforms.  For this reason, this plugin does not support Unix systems which 
 * should use the DjvUnixQt action plugin instead. <P> 
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
 *   Quality <BR>
 *   <DIV style="margin-left: 40px;">
 *     The QuickTime video compression quality.
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
class DjvQtAction
  extends PythonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DjvQtAction() 
  {
    super("DjvQt", new VersionID("2.4.1"), "Temerity", 
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
      choices.add("Raw");               
      choices.add("JPEG");             
      choices.add("MJPEG-A");           
      choices.add("MJPEG-B");           
      choices.add("H263");              
      choices.add("H264");               
      choices.add("DVC-NTSC");          
      choices.add("DVC-PAL");            

      ActionParam param = 
        new EnumActionParam
        (aCodec,
         "The name of the QuickTime codec to use to encode the images.",
         "JPEG", choices);
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
      layout.addEntry(aQuality);  
      layout.addEntry(aFPS); 

      setSingleLayout(layout);  
    }

    removeSupport(OsType.Unix);
    addSupport(OsType.Windows);
    //addSupport(OsType.MacOS);   // Must be root or console owner to work! (sigh)
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
    Path target = 
      getPrimaryTargetPath(agenda, DjvActionUtils.getQtExtensions(), "QuickTime Movie");
    String targetSuffix = agenda.getPrimaryTarget().getFilePattern().getSuffix();

    /* source images */ 
    Path sourcePath = null;
    FileSeq sourceSeq = null;
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
      sourceSeq = fseq;

      NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
      sourcePath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());      

      Path path = new Path(sourcePath, DjvActionUtils.toDjvFileSeq(fseq));
      sourceImages = path.toOsString();
    }

    /* lookup codec name */ 
    String codec = getSingleStringParamValue(aCodec);
    if(codec == null) 
      throw new PipelineException
        ("Somehow there was no value for the " + aCodec + " parameter!"); 

    /* compression quality */ 
    String quality = getSingleStringParamValue(aQuality); 

    /* playback speed */ 
    String fps = getSingleStringParamValue(aFPS); 

    /* create temporary Python script to run "djv_convert" and copy the movie 
         (for some reason the QuickTime output has to be on the local machine!) */ 
    Path tmpPath = getTempPath(agenda);
    File script = createTemp(agenda, "py");
    try {      
      FileWriter out = new FileWriter(script);

      /* import modules */ 
      out.write("import shutil\n");

      /* include the "launch" method definition */ 
      out.write(getPythonLaunchHeader()); 

      /* run "djv_convert" */ 
      Path movie = new Path(tmpPath, target.getName()); 
      {
        String program = "djv_convert";
        if(PackageInfo.sOsType == OsType.Windows) 
          program = "djv_convert.exe";
          
        /* we need the full path so DJV will find its libraries and plugins */ 
        {
          ExecPath epath = new ExecPath(agenda.getEnvironment().get("PATH"));
          File pfile = epath.which(program); 
          if(pfile == null) 
            throw new PipelineException
              ("Unable to find binary (" + program + ") in any of the directories given " + 
               "by the PATH environmental variable!"); 
          Path path = new Path(pfile);
          program = path.toString();
        }

        out.write
          ("launch('" + program + "', " + 
           "['" + escPath(sourceImages) + "', " + "'" + escPath(movie) + "', " + 
           "'-io_save', 'QuickTime', 'codec', '" + codec  + "', " + 
           "'-io_save', 'QuickTime', 'quality', '" + quality + "', " + 
           "'-speed', '" + fps + "'])\n"); 
      }

      /* copy the temporary movie to its target location */ 
      out.write("shutil.copy('" + movie + "', '" + target + "')\n");
      
      out.write("\n" + 
                "print 'ALL DONE.'\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary Python script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    /* create the process to run the action */     
    return createPythonSubProcess(agenda, script, outFile, errFile); 
  }  

  
   
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1235801520490690375L;

  public static final String aImageSource = "ImageSource";
  public static final String aCodec       = "Codec";
  public static final String aQuality     = "Quality";
  public static final String aMin         = "Min";
  public static final String aLow         = "Low";
  public static final String aNormal      = "Normal";
  public static final String aHigh        = "High";
  public static final String aMax         = "Max";
  public static final String aLossless    = "Lossless";
  public static final String aFPS         = "FPS";
  
}
