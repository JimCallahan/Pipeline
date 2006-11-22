// $Id: TerminalEditor.java,v 1.1 2006/11/22 18:16:02 jim Exp $

package us.temerity.pipeline.plugin.v2_0_10;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E R M I N A L   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A UNIX terminal in the node's working directory with the current toolset environment.<P> 
 * 
 * When run on a Unix system, this Editor will attempt to use konsole(1), gnome-terminal(1) or
 * xterm(1) if found in the Toolset's PATH in that order.<P> 
 * 
 * On Mac OS X systems, the default Terminal program will be used via AppleScript.
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
    super("Terminal", new VersionID("2.0.10"), "Temerity",
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
      String path = env.get("PATH");
      if(path == null) 
	throw new PipelineException("No PATH was defined in the toolset!");
      ExecPath exec = new ExecPath(path);

      File envFile = exec.which("env");
      if(envFile == null) 
	throw new PipelineException("Unable to find the env(1) program!");
      
      File bashFile = exec.which("bash");
      if(bashFile == null) 
	throw new PipelineException("Unable to find the bash(1) program!");

      String terminal = null;
      if(exec.which("konsole") != null) {
	terminal = "konsole";
	args.add("-e");
      }
      else if(exec.which("gnome-terminal") != null) {
	terminal = "gnome-terminal";
	args.add("-x");
      }
      else if(exec.which("xterm") != null) {
	terminal = "xterm";
	args.add("-e");
      }
      else {
	throw new PipelineException
	  ("Unable to find any suitable X11 terminal program in the PATH!.\n\n" + 
	   "Search included:\n" + 
	   "  konsole\n" + 
	   "  gnome-terminal\n" + 
	   "  xterm");
      }

      args.add(envFile.toString());
      args.add("-i");
      args.add(bashFile.toString());
      args.add("--init-file");
      args.add(script.toString()); 
      
      proc = new SubProcessLight(getName(), terminal, args, env, dir);
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

  private static final long serialVersionUID = 5238943216946784840L;

}


