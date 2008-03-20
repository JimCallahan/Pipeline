// $Id: MayaResolutionAction.java,v 1.3 2008/03/20 21:08:39 jim Exp $

package us.temerity.pipeline.plugin.MayaResolutionAction.v2_4_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.*;

import java.io.*;
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E S O L U T I O N   A C T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a MEL script which sets the render resolution based on the size of an existing
 * image. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the source node containing the image who's size will determine the 
 *     render resolution. 
 *   </DIV> <BR>
 *   <P>  
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
    super("MayaResolution", new VersionID("2.4.1"), "Temerity", 
	  "Generates a MEL script which sets the render resolution based on the size " + 
          "of an existing image."); 
    
    {
      ActionParam param = 
	new LinkActionParam
	(aImageSource,
	 "The name of the source node containing the image who's size will determine the " + 
         "render resolution.", 
	 null);
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

    /* parameter layout */ 
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageSource);  
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
      if(sname == null) 
        throw new PipelineException
          ("The " + aImageSource + "was not set!");

      FileSeq fseq = agenda.getPrimarySource(sname);
      if(fseq == null) 
        throw new PipelineException
          ("Somehow the " + aImageSource + " (" + sname + ") was not one of the " + 
           "source nodes!");
      
      NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
      sourcePath = new Path(PackageInfo.sProdPath, 
                            new Path(snodeID.getWorkingParent(), fseq.getPath(0)));
    }

    /* pixel aspect ratio */ 
    double aspectRatio = getSingleDoubleParamValue(aPixelAspectRatio, 
                                                   new Range(0.0, null, false));
    
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
         "ratio = (float(outputX) / float(outputY)) * float(" + aspectRatio + ")\n");

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

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 3652453999853479950L;

  public static final String aImageSource      = "ImageSource";
  public static final String aPixelAspectRatio = "PixelAspectRatio";
     
}
