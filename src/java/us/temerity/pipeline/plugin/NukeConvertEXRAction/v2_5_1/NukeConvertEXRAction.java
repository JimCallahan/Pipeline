package us.temerity.pipeline.plugin.NukeConvertEXRAction.v2_5_1;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   C O N V E R T   E X R   A C T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Convert tiled exr files into scanline (zip 1 compressed) exrs, for improved read speed in 
 * Nuke.
 */
public 
class NukeConvertEXRAction
  extends NukeActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeConvertEXRAction() 
  {
    super("NukeConvertEXR", new VersionID("2.5.1"), "Temerity",
          "Convert tiled exr files into scanline (zip 1 compressed) exrs, for improved " +
          "read speed in Nuke.");
    
    {
      ActionParam param = 
        new LinkActionParam
        (aImageSequence,
         "The source image sequence.", 
         null);
      addSingleParam(param);
    }
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

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
    Path targetPath = 
      getPrimaryTargetPaths(agenda, "exr", "exr image sequence").get(0);
    
    FileSeq targetSeq = agenda.getPrimaryTarget();
    
    Path srcPath = 
      getPrimarySourcePaths(aImageSequence, agenda, "exr", "exr image sequence").get(0);
    
    String srcName = getSingleStringParamValue(aImageSequence);
    
    FileSeq srcSeq = agenda.getPrimarySource(srcName);
    
    
    if  (srcSeq.hasFrameNumbers() && targetSeq.hasFrameNumbers() &&
        (!targetSeq.getFrameRange().equals(srcSeq.getFrameRange())))
      throw new PipelineException
        ("The source sequence and the target sequence do not have the same frame range");
    
    /* create a temporary Nuke script */ 
    Path script = new Path(createTemp(agenda, "nk"));
    try {    
      FileWriter out = new FileWriter(script.toFile()); 

      {
        /* read the source images in */
        FilePattern fpat = srcSeq.getFilePattern(); 
        FrameRange range = srcSeq.getFrameRange(); 
        out.write("Read {\n" + 
                  " inputs 0\n" + 
                  " file " + srcPath.getParent() + "/" + toNukeFilePattern(fpat) + "\n" +
                  " first " + range.getStart() + "\n" + 
                  " last " + range.getEnd() + "\n" + 
                  " name Read1\n" + 
                  "}\n");
      }
      {
        /* write the new images out */
        FilePattern fpat = targetSeq.getFilePattern(); 
        out.write("Write {\n" + 
        	  " file " + targetPath.getParent() + "/" + toNukeFilePattern(fpat) + "\n" + 
        	  " file_type exr\n" + 
        	  " datatype \"32 bit float\"\n" + 
        	  " name Write1\n" + 
        	  "}");
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
      args.add(toNukeFrameRange(srcSeq.getFrameRange()));

      return createSubProcess(agenda, getNukeProgram(agenda), args, agenda.getEnvironment(), 
                              agenda.getTargetPath().toFile(), outFile, errFile);
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8465357022558672092L;

  public static final String aImageSequence = "ImageSequence";
}
