// $Id: NukeQtAction.java,v 1.1 2008/01/26 23:31:57 jim Exp $

package us.temerity.pipeline.plugin.NukeQtAction.v2_4_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.*;

import java.io.*;
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   N U K E   Q T   A C T I O N                                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates a QuickTime movie from a sequence of images using Nuke. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images used to create the movie. 
 *   </DIV> <BR>
 * 
 *   FPS <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of image frames per second.
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
    super("NukeQt", new VersionID("2.4.2"), "Temerity", 
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
	new DoubleActionParam
	(aFPS,
	 "The number of image frames per second.",
	 null);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageSource);   
      layout.addEntry(aFPS); 

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
    ArrayList<String> suffixes = new ArrayList<String>();
    suffixes.add("qt");
    suffixes.add("mov");
    Path targetPath = getPrimaryTargetPath(agenda, suffixes, "QuickTime movie file (.mov)");

    /* source images */ 
    Path sourcePath = null;
    FileSeq sourceSeq = null;
    {
      String sname = getSingleStringParamValue(aImageSource); 
      if(sname == null) 
        throw new PipelineException
          ("The Image Source was not set!");
      
      FileSeq fseq = agenda.getPrimarySource(sname);
      if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the " + aImageSource + " (" + sname + ") was not one of the " + 
             "source nodes!");
      
      sourceSeq = fseq;
      
      NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
      sourcePath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
    }

    /* create a temporary Nuke script */ 
    Path script = new Path(createTemp(agenda, "nk"));
    try {    
      FileWriter out = new FileWriter(script.toFile()); 

      /* read the source images in */
      FilePattern fpat = sourceSeq.getFilePattern(); 
      FrameRange range = sourceSeq.getFrameRange(); 
      out.write("Read {\n" + 
                " inputs 0\n" + 
                " file " + sourcePath + "/" + toNukeFilePattern(fpat) + "\n" +
                " first " + range.getStart() + "\n" + 
                " last " + range.getEnd() + "\n" + 
                " name READ\n" + 
                "}\n");

      /* write the quicktime out */
      Double fps = (Double) getSingleParamValue(aFPS);
      out.write("Write {\n" + 
                " file " + targetPath + "\n" +
                " file_type mov\n" + 
                " codec \"Motion JPEG A\"\n" + 
                " fps " + fps + "\n" +
                " name WRITE\n" + 
                "}\n");

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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 5731109898295689139L;

  public static final String aImageSource = "ImageSource";
  public static final String aFPS         = "FPS";
  
}
