// $Id: NukeFrameCyclerEditor.java,v 1.2 2007/07/25 19:45:44 jim Exp $

package us.temerity.pipeline.plugin.NukeFrameCyclerEditor.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   F R A M E   C Y C L E R   E D I T O R                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays an image sequence using FrameCycler bundled with Nuke. <P> 
 * 
 * By default, this Editor launches the "Nuke4.6" binary.  This can be overridden by 
 * specifying an alternate binary with the NUKE_BINARY environmental variable in the 
 * Toolset used to run this Editor plugin. On Windows, the Nuke binary name should 
 * include the ".exe" extension.<P> 
 * 
 * By default, the "python" program is used by this Editor run feed the TCL required to 
 * launch FrameCycler into Nuke. An alternative program can be specified by setting 
 * PYTHON_BINARY in the Toolset environment to the name of the Python interpertor this 
 * Editor should use.  When naming an alternative Python interpretor under Windows, make 
 * sure to include the ".exe" extension in the program name.
 */
public
class NukeFrameCyclerEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeFrameCyclerEditor()
  {
    super("NukeFrameCycler", new VersionID("2.2.1"), "Temerity",
	  "Displays an image sequence using FrameCycler bundled with Nuke.", 
	  "Nuke4.6");

    addSupport(OsType.MacOS);
    //addSupport(OsType.Windows);
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
      FrameRange range = fseq.getFrameRange();
      FilePattern fpat = fseq.getFilePattern();
      if(fpat.getSuffix() == null) 
        throw new PipelineException
          ("The image sequence to be viewed (" + fseq + ") must have a image format suffix!");

      /* create a temporary Nuke script to load the frames */ 
      Path nukeScript = new Path(createTemp("nk"));
      try {
        FileWriter out = new FileWriter(nukeScript.toFile()); 

        out.write
          ("load framecycler_this.tcl\n" + 
           "IrToken\n" + 
           "exec $fc_path " + fpat + " "); 

        if(range != null) 
          out.write(range.getStart() + "-" + range.getEnd());

        out.write("\n"); 

        out.close();
      } 
      catch (IOException ex) {
        throw new PipelineException
          ("Unable to write temporary Nuke script (" + nukeScript + ") required by " + 
           "the " + getName() + " Editor!\n" + 
           ex.getMessage());
      }

      /* create a temporary Python script to run Nuke piping the script to STDIN */ 
      File script = createTemp("py");
      try {
        String nuke = NukeActionUtils.getNukeProgram(env); 

        FileWriter out = new FileWriter(script); 
        out.write
          ("import subprocess\n" + 
           "nuke = open('" + nukeScript + "', 'r')\n" + 
           "p = subprocess.Popen(['" + nuke + "', '-t'], stdin=nuke)\n" + 
           "p.communicate()\n");

        out.close();
      } 
      catch (IOException ex) {
        throw new PipelineException
          ("Unable to write temporary Python script (" + script + ") required by " + 
           "the " + getName() + " Editor!\n" + 
           ex.getMessage());
      }

      /* command line arguments */ 
      ArrayList<String> args = new ArrayList<String>();
      args.add(script.getPath());

      String python = PythonActionUtils.getPythonProgram(env);

      return new SubProcessLight(author, getName(), python, args, env, dir);
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

  private static final long serialVersionUID = -3479250668021781442L;

}


