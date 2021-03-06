// $Id: BaseAppleScriptEditor.java,v 1.9 2008/03/10 05:55:48 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   A P P L E   S C R I P T   E D I T O R                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * A convenient super class for writing Pipeline Editor plugins which communicate with
 * Mac OS X applications using Apple Script. <P> 
 * 
 * An Apple Script is generate on the fly which directs the application to open each 
 * of the files contained in the file sequence passed to the {@link #launch launch} method.
 * If the application is not already running it will be started. <P> 
 * 
 * The generated script has the following format: <P> 
 * 
 * <DIV style="margin-left: 40px;"><CODE>
 * tell application "</CODE><I>program</I><CODE>"
 *   open file "</CODE><I>path-to-file</I><CODE>"
 *   ...
 * end tell
 * </CODE></DIV><P> 
 * 
 * See the <A href="http://developer.apple.com/documentation/AppleScript AppleScript">Apple 
 * Script Documentation</A> for details.
 */
public
class BaseAppleScriptEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  protected
  BaseAppleScriptEditor() 
  {
    super();
  }

  /** 
   * Construct with the given name, version and description. 
   * 
   * @param name 
   *   The short name of the editor.
   * 
   * @param vid
   *   The editor plugin revision number. 
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the editor.
   *
   * @param program 
   *   A name of the editor executable.
   */ 
  protected
  BaseAppleScriptEditor
  (
   String name, 
   VersionID vid,
   String vendor, 
   String desc, 
   String program
  ) 
  {
    super(name, vid, vendor, desc, program);
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
    if(PackageInfo.sOsType != OsType.MacOS) 
      return null; 

    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-e");
      args.add("tell application \"" + getProgram() + "\"");
      
      for(File file : fseq.getFiles()) {
        String macpath = file.getPath().substring(1).replace("/",":");
        args.add("-e");
        args.add("open file \"" + macpath + "\"");
      }
      
      args.add("-e");
      args.add("end tell");
      
      return new SubProcessLight(author, getName(), "osascript", args, env, dir);
    }
    catch(Exception ex) {
      throw new PipelineException
        ("Unable to generate the SubProcess to launch this Editor!\n" +
         ex.getMessage());
    }    
  }

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
   * @deprecated
   *   Unlike the {@link #prep prep} method, the convention is for this method to also execute
   *   the generated SubProcessLight instance.  New subclasses should implement the {@link 
   *   #prep prep} method instead to allow the caller a chance to execute the process as 
   *   another user.  Namely, as the owner of the files being edited.  The owner of the files
   *   is passes as an additional argument to {@link #prep prep} called (author) which must
   *   be passed on as a constructor argument of the generated SubProcessLight instance.
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
    ArrayList<String> args = new ArrayList<String>();
    args.add("-e");
    args.add("tell application \"" + getProgram() + "\"");

    for(File file : fseq.getFiles()) {
      String macpath = file.getPath().substring(1).replace("/",":");
      args.add("-e");
      args.add("open file \"" + macpath + "\"");
    }

    args.add("-e");
    args.add("end tell");

    SubProcessLight proc = new SubProcessLight(getName(), "osascript", args, env, dir);
    proc.start();

    return proc;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6091948675278742105L;

}



