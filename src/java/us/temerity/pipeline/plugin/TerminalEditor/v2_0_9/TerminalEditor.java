// $Id: TerminalEditor.java,v 1.1 2007/06/17 15:34:46 jim Exp $

package us.temerity.pipeline.plugin.TerminalEditor.v2_0_9;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E R M I N A L   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A UNIX terminal in the node's working directory with the current toolset environment.", 
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
    super("Terminal", new VersionID("2.0.9"), "Temerity",
	  "A UNIX terminal in the node's working directory with the current " + 
	  "toolset environment.", 
	  "Terminal");

    addSupport(OsType.MacOS);
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
    /* create temporary shell script */ 
    File script = null;
    try {
      Path spath = new Path(PackageInfo.sTempPath, "pipeline");
      File sdir = spath.toFile();
      sdir.mkdir();
      
      script = File.createTempFile("TerminalEditor-", ".bash", sdir);
      FileCleaner.add(script);
      
      FileWriter out = new FileWriter(script);
      
      for(String key : env.keySet()) 
	out.write("export " + key + "='" + env.get(key) + "'\n");	  
      
      out.write("export PS1=\"\\n\\u@\\h \\w\\n$ \"\n");
      out.write("cd " + dir + "\n");
      out.write("clear");
      
      out.close();
    }
    catch (IOException ex) {
      throw new PipelineException
	("Unable to create the temporary script (" + script + ") for the Terminal " + 
	 "editor plugin!");
    }
    
    ArrayList<String> args = new ArrayList<String>();
    SubProcessLight proc = null;
    if(PackageInfo.sOsType == OsType.Unix) {      
      args.add("-x");
      args.add("/bin/env");
      args.add("-i");
      args.add("/bin/bash");
      args.add("--init-file");
      args.add(script.toString()); 
      proc = new SubProcessLight(getName(), "gnome-terminal", args, env, dir);
    }
    else if(PackageInfo.sOsType == OsType.MacOS) {
      args.add("-e");
      args.add("tell application \"Terminal\"");

      args.add("-e");
      args.add("do script \"/usr/bin/env -i /bin/bash --init-file " + script + "\""); 

      args.add("-e");
      args.add("end tell");

      proc = new SubProcessLight(getName(), "osascript", args, env, dir);
    }
    else {
      throw new PipelineException
	("The Terminal Editor is not yet supported on the Windows operating system.");
    }
  
    proc.start();  
    return proc;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7086503859149410654L;

}


