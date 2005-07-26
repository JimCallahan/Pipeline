// $Id: TerminalEditor.java,v 1.2 2005/07/26 04:58:30 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E R M I N A L   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A UNIX terminal in the node's working directory.", 
 */
public
class TerminalEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TerminalEditor()
  {
    super("Terminal", new VersionID("2.0.0"),
	  "A UNIX terminal in the node's working directory.", 
	  "Terminal");
  }

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Launch the editor program (obtained with {@link #getProgram getProgram}) under the given 
   * environmant with all of the files which comprise the given file sequence as 
   * arguments. The environment <CODE>env</CODE> consists of a table of environmental 
   * variable name/value pairs.  Typically, this environment is corresponds to a Toolset. <P>
   * 
   * Subclasses should override this method if more specialized behavior or different 
   * command line arguments are needed in order to launch the editor for the given file 
   * sequence.
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
  launch
  (
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    ArrayList<String> args = new ArrayList<String>();
    SubProcessLight proc = null;
    if(PackageInfo.sOsType == OsType.Unix) {
      args.add("--working-directory=" + dir);
      
      proc = new SubProcessLight(getName(), "gnome-terminal", args, env, dir);
      proc.start();  
    }
    else if(PackageInfo.sOsType == OsType.MacOS) {
      args.add("-e");
      args.add("tell application \"Terminal\"");

      args.add("-e");
      StringBuffer buf = new StringBuffer();
      for(String key : env.keySet()) 
	buf.append("export " + key + "='" + env.get(key) + "'; ");
      buf.append("cd " + dir + "; clear");
      args.add("do script \"" + buf + "\"");

      args.add("-e");
      args.add("end tell");

      proc = new SubProcessLight(getName(), "osascript", args, env, dir);
      proc.start();  
    }
    else {
      throw new PipelineException
	("The Terminal Editor is not yet supported on the Windows operating system.");
    }
  
    return proc;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8966634638768863109L;

}


