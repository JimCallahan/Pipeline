// $Id: MRayTextureUnpackAction.java,v 1.2 2007/03/28 20:05:13 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   T E X T U R E   U N P A C K   A C T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Extracts the pre-filtered level source images from an optimized Mental Ray memory 
 * mappable pyramid texture.<P> 
 * 
 * Unpacks the single texture MAP file which is the primary file sequence of one of the 
 * source nodes into a sequence of pre-filtered level images which are the primary file 
 * sequence of this node.  The first extracted image will be full resolution and each
 * successively image in the sequence half as large.  The number of levels extracted is 
 * dependent on the length of the target file sequence.  A Mental Ray texture can contain 
 * at most 20 levels.  For example, a texture with 8 levels could be extracted into a
 * sequence of target images: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   level.0000.tif (256x256)
 *   level.0001.tif (128x128)
 *   level.0002.tif (64x64)
 *   level.0003.tif (32x32)
 *   level.0004.tif (16x16)
 *   level.0005.tif (8x8)
 *   level.0006.tif (4x4)
 *   level.0007.tif (2x2)
 * </DIV> <BR>
 * 
 * Note that the frame number of the target image is used to determine the pre-filtered level
 * texture to extract (level 0 is the full resolution texture).  For this reason, using 
 * frame ranges which don't start at 0 will produce different results.  This action is the 
 * symetric opposite of MRayTexturePack and can also be used to extract the level images 
 * from a MAP file created with MRayTexture or directly with "imf_copy" through other 
 * means.<P> 
 * 
 * Note that the MAP files read by this Action are hardware specific so care needs
 * to be taken to insure that the hosts which generate the MAP files are of the same 
 * hardware type as the hosts which will use these files.  The best way of insuring this
 * is through the use of hardware specific Selection Keys. <P>
 * 
 * See the Mental Ray documentation for for details about <B>imf_copy</B>(1) and memory
 * mapped texture files. <P>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Texture Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the single Mental Ray memory mappable pyramid texture.
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * By default, the "python" program is used by this action when running on Windows to run
 * the "imf_copy" commands.  An alternative program can be specified by setting PYTHON_BINARY 
 * in the Toolset environment to the name of the Python interpertor this Action should use.  
 * When naming an alternative Python interpretor under Windows, make sure to include the 
 * ".exe" extension in the program name.
 */
public
class MRayTextureUnpackAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MRayTextureUnpackAction() 
  {
    super("MRayTextureUnpack", new VersionID("2.2.1"), "Temerity", 
	  "Extracts the pre-filtered level source images from an optimized Mental Ray " + 
          "memory mappable pyramid texture."); 
    
    {
      ActionParam param = 
	new LinkActionParam
	(aTextureSource,
	 "The source node which contains the single Mental Ray memory mappable " + 
         "pyramid texture.",
	 null);
      addSingleParam(param);
    }

    addExtraOptionsParam(); 

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aTextureSource);
      layout.addSeparator();  
      addExtraOptionsParamToLayout(layout);

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
    /* source texture path */ 
    Path sourcePath = 
      getPrimarySourcePath(aTextureSource, agenda, "map", 
                           "Mental Ray memory mappable pyramid texture");

    /* target pre-filtered level image paths */ 
    ArrayList<Path> targetPaths = getPrimaryTargetPaths(agenda, "pre-filtered image levels"); 

    /* target file sequence */ 
    FileSeq targetSeq = agenda.getPrimaryTarget(); 
    if(!targetSeq.hasFrameNumbers()) 
      throw new PipelineException
        ("The target file sequence (" + targetSeq + ") must have frame numbers!");         
    if(targetSeq.getFrameRange().getEnd() > 20) 
      throw new PipelineException
        ("The largest pre-filtered image level which can be extracted is (20)!"); 
    
    /* texture conversion program */ 
    String program = "imf_copy";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "imf_copy.exe";
    
    /* run directly for single frame cases */ 
    if(targetSeq.numFrames() == 1) {
      ArrayList<String> args = new ArrayList<String>();    
      args.add("-x"); 
      args.add(String.valueOf(targetSeq.getFrameRange().getStart()));
      args.add(targetPaths.get(0).toOsString());

      return createSubProcess(agenda, program, args, outFile, errFile);
    }
    
    /* for multiple frames, build a Python script */ 
    else { 
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
            Path tpath = targetPaths.get(idx);
            out.write("launch('" + program + "', ['-x', '" + idx + "', " + 
                      "'" + sourcePath + "', '" + tpath + "'])\n");
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
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5762901764057742718L;

  public static final String aTextureSource = "TextureSource";

}

