// $Id: MayaResolutionAction.java,v 1.2 2008/03/20 21:08:39 jim Exp $

package us.temerity.pipeline.plugin.MayaResolutionAction.v2_4_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.*;

import java.io.*;
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E S O L U T I O N   A C T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a MEL script which sets the render resolution.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the source node containing the image who's size will determine the 
 *     render resolution.  If specified, the ImageWidth and ImageHeight parameters will 
 *     be ignored.
 *   </DIV> <BR>
 *   <P>  
 * 
 *   Image Width <BR>
 *   <DIV style="margin-left: 40px;">
 *     The horizontal resolution of the output image in pixels. <BR>
 *   </DIV> <BR>
 *  
 *   Image Height <BR>
 *   <DIV style="margin-left: 40px;">
 *     The vertical resolution of the output image in pixels. <BR>
 *   </DIV> <BR>
 * 
 *   Pixel Aspect Ratio <BR>
 *   <DIV style="margin-left: 40px;">
 *     Ratio of pixel height to pixel width. <BR>
 *   </DIV> 
 * </DIV> <P> 
 * 
 * This action uses the Imagemagick identify(1) utility to analyze the source image. <P> 
 * 
 * The "python" program is used by this action to run identify(1), and construct the target
 * MEL script based on the results of identify(1).  An alternative program can be specified 
 * by setting PYTHON_BINARY in the Toolset environment to the name of the Python interpertor 
 * this Action should use.  When naming an alternative Python interpretor under Windows, 
 * make sure to include the ".exe" extension in the program name. <P> 
 */
public 
class MayaResolutionAction
  extends PythonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaResolutionAction() 
  {
    super("MayaResolution", new VersionID("2.4.2"), "Temerity", 
	  "Generates a MEL script which sets the render resolution based on the size " + 
          "of an existing image."); 
    
    {
      ActionParam param = 
	new LinkActionParam
	(aImageSource,
	 "The name of the source node containing the image who's size will determine the " + 
         "render resolution. If specified, the ImageWidth and ImageHeight parameters will " +
         "be ignored.", 
	 null);
      addSingleParam(param);
    } 
    
    {
      ActionParam param = 
        new IntegerActionParam
        (aImageWidth,
         "The horizontal resolution of the output image in pixels.", 
         640);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new IntegerActionParam
        (aImageHeight,
         "The vertical resolution of the output image in pixels.", 
         480);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
        new DoubleActionParam
        (aPixelAspectRatio,
         "Ratio of pixel height to pixel width.", 
         1.0);
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

      addPreset(aImageResolution, choices);

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       320);
        values.put(aImageHeight,      240);
        values.put(aPixelAspectRatio, 1.0);
	
        addPresetValues(aImageResolution, "320x240", values);
      }
      
      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       640);
        values.put(aImageHeight,      480);
        values.put(aPixelAspectRatio, 1.0);
	
        addPresetValues(aImageResolution, "640x480", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       1024);
        values.put(aImageHeight,      1024);
        values.put(aPixelAspectRatio, 1.0);
	
        addPresetValues(aImageResolution, "1k Square", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       2048);
        values.put(aImageHeight,      2048);
        values.put(aPixelAspectRatio, 1.0);
	
        addPresetValues(aImageResolution, "2k Square", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       3072);
        values.put(aImageHeight,      3072);
        values.put(aPixelAspectRatio, 1.0);
	
        addPresetValues(aImageResolution, "3k Square", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       4096);
        values.put(aImageHeight,      4096);
        values.put(aPixelAspectRatio, 1.0);
	
        addPresetValues(aImageResolution, "4k Square", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       720);
        values.put(aImageHeight,      576);
        values.put(aPixelAspectRatio, 1.066);
	
        addPresetValues(aImageResolution, "CCIR PAL/Quantel PAL", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       720);
        values.put(aImageHeight,      486);
        values.put(aPixelAspectRatio, 0.900);
	
        addPresetValues(aImageResolution, "CCIR 601/Quantel NTSC", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       1024);
        values.put(aImageHeight,      768);
        values.put(aPixelAspectRatio, 1.0);
	
        addPresetValues(aImageResolution, "Full 1024", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       1280);
        values.put(aImageHeight,      1024);
        values.put(aPixelAspectRatio, 1.066);
	
        addPresetValues(aImageResolution, "Full 1280/Screen", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       1280);
        values.put(aImageHeight,      720);
        values.put(aPixelAspectRatio, 1.0);
	
        addPresetValues(aImageResolution, "HD 720", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       1920);
        values.put(aImageHeight,      1080);
        values.put(aPixelAspectRatio, 1.0);
	
        addPresetValues(aImageResolution, "HD 1080", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       646);
        values.put(aImageHeight,      485);
        values.put(aPixelAspectRatio, 1.001);
	
        addPresetValues(aImageResolution, "NTSC 4d", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       768);
        values.put(aImageHeight,      576);
        values.put(aPixelAspectRatio, 1.0);
	
        addPresetValues(aImageResolution, "PAL 768", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       780);
        values.put(aImageHeight,      576);
        values.put(aPixelAspectRatio, 0.984);
	
        addPresetValues(aImageResolution, "PAL 780", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       512);
        values.put(aImageHeight,      486);
        values.put(aPixelAspectRatio, 1.265);
	
        addPresetValues(aImageResolution, "Targa 486", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       512);
        values.put(aImageHeight,      482);
        values.put(aPixelAspectRatio, 1.255);
	
        addPresetValues(aImageResolution, "Target NTSC", values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aImageSource,      null); 
        values.put(aImageWidth,       512);
        values.put(aImageHeight,      576);
        values.put(aPixelAspectRatio, 1.500);
	
        addPresetValues(aImageResolution, "Targa PAL", values);
      }
    }

    /* parameter layout */ 
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageResolution); 
      layout.addEntry(aImageSource);  
      layout.addEntry(aImageWidth);
      layout.addEntry(aImageHeight);
      layout.addEntry(aPixelAspectRatio);  

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
    /* target MEL script */ 
    Path targetPath = getPrimaryTargetPath(agenda, "mel", "MEL script");

    /* source image */ 
    Path sourcePath = null;
    {
      String sname = getSingleStringParamValue(aImageSource); 
      if(sname != null) {
        FileSeq fseq = agenda.getPrimarySource(sname);
        if(fseq == null) 
          throw new PipelineException
            ("Somehow the " + aImageSource + " (" + sname + ") was not one of the " + 
             "source nodes!");
        
        NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
        sourcePath = new Path(PackageInfo.sProdPath, 
                              new Path(snodeID.getWorkingParent(), fseq.getPath(0)));
      }
    }

    /* resolution */ 
    int width    = getSingleIntegerParamValue(aImageWidth,  new Range(1, null)); 
    int height   = getSingleIntegerParamValue(aImageHeight, new Range(1, null)); 
    double ratio = getSingleDoubleParamValue(aPixelAspectRatio, new Range(0.0, null, false));
    
    /* use an existing image to determine the resolution */ 
    if(sourcePath != null) {

      /* create a temporary Python script */   
      File script = createTemp(agenda, "py"); 
      try {
        FileWriter out = new FileWriter(script); 

        /* include the "launch" method definition */ 
        out.write(PythonActionUtils.getPythonLaunchHeader()); 

        /* collect output resolution information with identify(1) */ 
        out.write 
          ("args1 = ['identify', '-format', '%w %h', '" + sourcePath + "']\n" + 
           "print('RUNNING: ' + ' '.join(args1))\n" +
           "p1 = subprocess.Popen(args1, stdout=subprocess.PIPE)\n" +
           "result1 = p1.wait()\n" + 
           "if result1 != 0:\n" +
           "  sys.exit('Unable to identify: " + sourcePath + "\\n  " + 
           "Exit Code = ' + str(result1) + '\\n')\n" + 
           "str1 = p1.stdout.read()\n" + 
           "outputX = str1.split()[0]\n" + 
           "outputY = str1.split()[1]\n" + 
           "ratio = (float(outputX) / float(outputY)) * float(" + ratio + ")\n");

        /* generate the target MEL script */ 
        out.write
          ("out = open('" + targetPath + "', 'w', 1024)\n" + 
           "try:\n" + 
           "  out.write('// IMAGE RESOLUTION\\n')\n" + 
           "  out.write('setAttr \"defaultResolution.aspectLock\" 0;\\n')\n" + 
           "  out.write('setAttr \"defaultResolution.width\" ' + outputX + ';\\n')\n" + 
           "  out.write('setAttr \"defaultResolution.height\" ' + outputY + ';\\n')\n" + 
           "  out.write('setAttr \"defaultResolution.deviceAspectRatio\" " +
           "' + str(ratio) + ';\\n')\n" + 
           "finally:\n" + 
           "    out.close()\n\n"); 

        out.write("print 'ALL DONE.'\n");

        out.close();
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to write temporary Python script file (" + script + ") for Job " + 
           "(" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
      }

      return createPythonSubProcess(agenda, script, outFile, errFile);
    }
    
    /* specify the resolution directly */ 
    else {
      /* create a temporary file which will be copied to the target */ 
      File temp = createTemp(agenda, "mel");
      try {      
        FileWriter out = new FileWriter(temp);
        
      	double deviceRatio = (((double) width) / ((double) height)) * ratio;
        
	out.write
	  ("// IMAGE RESOLUTION\n" + 
	   "setAttr \"defaultResolution.aspectLock\" 0;\n" + 
	   "setAttr \"defaultResolution.width\" " + width + ";\n" + 
	   "setAttr \"defaultResolution.height\" " + height + ";\n" + 
	   "setAttr \"defaultResolution.deviceAspectRatio\" " + deviceRatio + ";\n\n");
      
        out.close();
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to write the target MEL script file (" + temp + ") for Job " + 
           "(" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
      }
      
      /* create the process to run the action */ 
      return createTempCopySubProcess(agenda, temp, targetPath, outFile, errFile);
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -375691338398432953L;

  public static final String aImageResolution  = "ImageResolution";
  public static final String aImageSource      = "ImageSource";
  public static final String aImageWidth       = "ImageWidth";
  public static final String aImageHeight      = "ImageHeight";
  public static final String aPixelAspectRatio = "PixelAspectRatio";
     
}
