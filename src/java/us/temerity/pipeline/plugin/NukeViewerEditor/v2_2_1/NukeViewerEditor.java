// $Id: NukeViewerEditor.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.NukeViewerEditor.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   V I E W E R   E D I T O R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays an image sequence using a Nuke viewer. <P> 
 * 
 * By default, this Editor launches the "Nuke4.6" binary.  This can be overridden by 
 * specifying an alternate binary with the NUKE_BINARY environmental variable in the 
 * Toolset used to run this Editor plugin. On Windows, the Nuke binary name should 
 * include the ".exe" extension.
 */
public
class NukeViewerEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeViewerEditor()
  {
    super("NukeViewer", new VersionID("2.2.1"), "Temerity",
	  "Displays an image sequence using a Nuke viewer.", 
	  "Nuke4.6");

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a {@link SubProcessLight} instance which when executed will launch an editor
   * program to view the given file sequence as arguments. <P> 
   * 
   * The default implementation executes the editor program obtained with {@link #getProgram 
   * getProgram} method under the given environment.  Subclasses should override this method 
   * if more specialized behavior or different command line arguments are needed in order to 
   * launch the editor for the given file sequence. <P> 
   * 
   * @param author
   *   The name of the user owning the files. 
   * 
   * @param fseq  
   *   The file sequence to edit.
   * 
   * @param env  
   *   The environment under which the editor is run.  
   * 
   * @param dir  
   *   The working directory where the editor is run.
   *
   * @return 
   *   The controlling <CODE>SubProcessLight</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
   * 
   * @see SubProcessLight
   */  
  public SubProcessLight
  prep
  (
   String author, 
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    try {
      FilePattern fpat = fseq.getFilePattern();
      if(fpat.getSuffix() == null) 
        throw new PipelineException
          ("The image sequence to be viewed (" + fseq + ") must have a image format suffix!");

      /* command line arguments */ 
      ArrayList<String> args = new ArrayList<String>();
      args.add("-g");
      args.add("-v");
      args.add(NukeActionUtils.toNukeFilePattern(fpat));

      FrameRange range = fseq.getFrameRange();      
      if(range != null) 
        args.add(NukeActionUtils.toNukeFrameRange(range));
      
      String nuke = NukeActionUtils.getNukeProgram(env); 

      return new SubProcessLight(author, getName(), nuke, args, env, dir);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to launch this Editor!\n" +
	 ex.getMessage());
    }       
  }

  /** 
   * This implementation always throws a PipelineException, to insure that the {@link #prep
   * prep} method is used for this Editor instead of this deprecated method.
   * 
   * @param fseq  
   *   The file sequence to edit.
   * 
   * @param env  
   *   The environment under which the editor is run.  
   * 
   * @param dir  
   *   The working directory where the editor is run.
   *
   * @return 
   *   The controlling <CODE>SubProcessLight</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
   */  
  @SuppressWarnings("deprecation")
  @Deprecated
  public SubProcessLight
  launch
  (
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    throw new PipelineException
      ("This method should never be called since the prep() method does not return (null)!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5067755326994537128L;

}


