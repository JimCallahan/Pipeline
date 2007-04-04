// $Id: DebugTextureAction.java,v 1.2 2007/04/04 07:33:30 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 
import us.temerity.pipeline.math.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D E B U G   T E X T U R E   A C T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Uses Shake to create a series of mip-map level images which can be used to create a 
 * texture useful in debugging filtering issues.<P> 
 * 
 * The target primary file sequence must be a sequence of image files.  The first image 
 * will generated at full resolution (see Image Size parameter below) and each successive
 * image in the sequence will be have half as large.  The first frame in the target sequence
 * should have a frame number of zero.<P> 
 * 
 * Each of the images generated are of a uniform but unique fully saturated color such that 
 * images are evenly distributed across the range of hues and each image is nearly 
 * complementary to its neighbors.  All colors selected for the generated images have full 
 * value and alpha.<P>
 * 
 * Due to the way this action uses the target file sequence to determine the size and color
 * of the images generated, it must be run with the Serial Execution Method to insure correct
 * results. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Size <BR>
 *   <DIV style="margin-left: 40px;">
 *     The image resolution of the largest mip-map level in the sequence. 
 *   </DIV> <BR>
 *   <BR>
 * </DIV>
 */
public
class DebugTextureAction
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DebugTextureAction() 
  {
    super("DebugTexture", new VersionID("2.2.1"), "Temerity", 
	  "Uses Shake to create a series of mip-map level images which can be used to " + 
          "create a texture useful in debugging filtering issues."); 
    
    {
      ActionParam param = 
	new IntegerActionParam
	(aImageSize, 
	 "The image resolution of the largest mip-map level in the sequence.",
	 512);
      addSingleParam(param);
    }

    addSupport(OsType.MacOS);
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
    /* target pre-filtered level image paths */ 
    ArrayList<Path> targetPaths = getPrimaryTargetPaths(agenda, "mip-map image levels"); 

    /* target file sequence */ 
    FileSeq targetSeq = agenda.getPrimaryTarget(); 
    if(!targetSeq.hasFrameNumbers()) 
      throw new PipelineException
        ("The target file sequence (" + targetSeq + ") must have frame numbers!");         
    if(targetSeq.numFrames() > 20) 
      throw new PipelineException
          ("The target file sequence (" + targetSeq + ") contained more than " + 
           "the maximum (20) number of pre-filtered image levels!");
    
    /* image resolution */ 
    int size = getSingleIntegerParamValue(aImageSize, 1); 

    /* create a temporary Python script */ 
    File script = createTemp(agenda, "py"); 
    try {
      FileWriter out = new FileWriter(script);

      /* include the "launch" method definition */ 
      out.write(getPythonLaunchHeader()); 
      
      /* convert the frames */ 
      {
        int frames[] = targetSeq.getFrameRange().getFrameNumbers();
        int idx; 
        for(idx=0; idx<frames.length; idx++) {
          int res = Math.max(1, (int) Math.floor(size / Math.pow(2.0, (double) idx)));

          Color3d color = new Color3d();
          {
            double inc = 360.0 / ((double) targetSeq.numFrames());
            double step = (Math.floor(180.0 / inc) + 1.0) * inc;
            double whole = (((double) idx) * step) / 360.0;
            double frac = whole - Math.floor(whole);

            color.fromHSV(new Tuple3d(frac * 360.0, 1.0, 1.0));
          }
          
          Path tpath = targetPaths.get(idx);

          out.write
            ("launch('shake', ['-color', '" + res + "', '" + res + "', '1', " + 
             "'" + color.r() + "', '" + color.g() + "', '" + color.b() + "', '1', " + 
             "'-fo', '" + tpath + "'])\n");
        }
      }

      out.write("\n" + 
                "print 'ALL DONE.'\n");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to write the temporary Python script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, script, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4260450982246960553L;

  public static final String aImageSize = "ImageSize";

}

