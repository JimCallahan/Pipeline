// $Id: NukeFrameCyclerEditor.java,v 1.2 2008/03/17 22:59:43 jim Exp $

package us.temerity.pipeline.plugin.NukeFrameCyclerEditor.v2_4_21;

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
 * This will only work with Nuke 5.2 or later, due to its use of the new Nuke python API. <p>
 * 
 * By default, this Editor launches the "Nuke5.2" binary.  This can be overridden by 
 * specifying an alternate binary with the NUKE_BINARY environmental variable in the 
 * Toolset used to run this Editor plugin. On Windows, the Nuke binary name should 
 * include the ".exe" extension.<P> 
 * 
 * By default, the "python" program is used by this Editor to feed the py required to 
 * launch FrameCycler into Nuke. An alternative program can be specified by setting 
 * PYTHON_BINARY in the Toolset environment to the name of the Python interpreter this 
 * Editor should use.  When naming an alternative Python interpreter under Windows, make 
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
    super("NukeFrameCycler", new VersionID("2.4.21"), "Temerity",
	  "Displays an image sequence using FrameCycler bundled with Nuke.", 
	  "Nuke5.2");
    
    underDevelopment();

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
  @Override
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


      Path script = new Path(createTemp("py"));
        try {
          FileWriter out = new FileWriter(script.toFile()); 
          
          String fileName = null;
          String rangeString = null;
          
          if(fseq.isSingle()) {
            fileName = fseq.getPath(0).toString(); 
          }
          else {
            fileName = fpat.getPrefix() + ".#";
            String suffix = fpat.getSuffix();
            if(suffix != null) 
              fileName += "." + suffix;

            rangeString = range.getStart() + "-" + range.getEnd();
          }
          
          out.write
            ("import subprocess\n" + 
             "import nuke\n" + 
             "import nukescripts\n" + 
             "import platform\n" + 
             "import sys\n" + 
             "import os.path\n" + 
             "import re\n" + 
             "import thread\n" + 
             "\n" + 
             "\n" + 
             "path = nukescripts.__file__\n" + 
             "path = path.replace('__init__.pyc','framecycler_this.py')\n" + 
             "execfile(path)\n" + 
             "\n" + 
             "args = []\n" + 
             "\n" + 
             "filename = \"" + fileName + "\"\n" + 
             "\n" + 
             "fc_path = os.path.normpath(fc_path)\n" + 
             "if nuke.env['WIN32']:\n" + 
             "   args.append( \"\\\"\" + fc_path + \"\\\"\" )\n" + 
             "   args.append( \"\\\"\" + filename + \"\\\"\" )\n" + 
             "else:\n" + 
             "   args.append( fc_path )\n" + 
             "   args.append(filename)\n" + 
             "\n");
          if (rangeString != null) {
            out.write
            ("range = \"\"\n" + 
             "args.append(range)\n");
          }
          out.write
            ("\n" + 
             "\n" + 
             "nuke.IrToken()\n" + 
             "os.spawnv(os.P_NOWAITO, fc_path, args)\n" + 
             "\n"); 
          
          out.close();
        } 
        catch (IOException ex) {
          throw new PipelineException
            ("Unable to write temporary Python script (" + script + ") required by " + 
             "the " + getName() + " Editor!\n" + 
             ex.getMessage());
        }

      /* create a temporary Python script to run Nuke piping the script to STDIN */ 
      File pythonScript = createTemp("py");
      try {
        String nuke = NukeActionUtils.getNukeProgram(env); 

        FileWriter out = new FileWriter(pythonScript); 
        out.write
          ("import subprocess\n" + 
           "nuke = open('" + script + "', 'r')\n" + 
           "p = subprocess.Popen(['" + nuke + "', '-ti'], stdin=nuke)\n" + 
           "p.communicate()\n");

        out.close();
      } 
      catch (IOException ex) {
        throw new PipelineException
          ("Unable to write temporary Python script (" + pythonScript + ") required by " + 
           "the " + getName() + " Editor!\n" + 
           ex.getMessage());
      }

      /* command line arguments */ 
      ArrayList<String> args = new ArrayList<String>();
      args.add(pythonScript.getPath());

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
  @Override
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

  private static final long serialVersionUID = -2375851910088141856L;
}


