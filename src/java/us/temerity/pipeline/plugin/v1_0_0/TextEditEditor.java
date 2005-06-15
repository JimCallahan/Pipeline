// $Id: TextEditEditor.java,v 1.1 2005/06/15 12:16:55 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T   E D I T   E D I T O R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The default Mac OS X text editor.
 */
public
class TextEditEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TextEditEditor()
  {
    super("TextEdit", new VersionID("1.0.0"), 
	  "The Mac OS X Text Editor.", 
	  "TextEdit");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Launch the editor program.
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
//   public SubProcessLight
//   launch
//   (
//    FileSeq fseq,      
//    Map<String, String> env,      
//    File dir        
//   ) 
//     throws PipelineException
//   {
//     ArrayList<String> args = new ArrayList<String>();
//     args.add("-e");
//     args.add("tell application \"" + getProgram() + "\"");

//     for(File file : fseq.getFiles()) {
//       String macpath = file.getPath().substring(1).replace("/",":");
//       args.add("-e");
//       args.add("open file \"" + macpath + "\"");
//     }

//     args.add("-e");
//     args.add("end tell");

//     SubProcessLight proc = new SubProcessLight(getName(), "osascript", args, env, dir);
//     proc.start();

//     return proc;
//   }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2899399504530920642L;

}


