// $Id: BaseEditor.java,v 1.2 2004/02/25 01:26:34 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * The superclass of all Pipeline node editor plugins. <P>
 * 
 */
public
class BaseEditor
  extends Described
  implements Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  protected
  BaseEditor() 
  {
    super();
  }

  /** 
   * Construct with a name and description. 
   * 
   * @param name [<B>in</B>]
   *   The short name of the editor.
   * 
   * @param desc [<B>in</B>]
   *   A short description used in tooltips.
   *
   * @param program [<B>in</B>]
   *   A name of the editor executable. 
   */ 
  protected
  BaseEditor
  (
   String name,  
   String desc, 
   String program
  ) 
  {
    super(name, desc);
    
    if(program == null)
      throw new IllegalArgumentException("The program cannot be (null)!");
    pProgram = program;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets the name of the editor executable.
   */
  public String
  getProgram() 
  {
    return pProgram;
  }

   

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Launch the editor program (obtained with {@link #getName getName}) under the given 
   * environmant with all of the files which comprise the given file sequence as 
   * arguments. The environment <CODE>env</CODE> consists of a table of environmental 
   * variable name/value pairs.  Typically, this environment is corresponds to a Toolset. <P>
   * 
   * Subclasses should override this method if more specialized behavior or different 
   * command line arguments are needed in order to launch the editor for the given file 
   * sequence.
   * 
   * @param fseq [<B>in</B>] 
   *   The file sequence to edit.
   * 
   * @param env [<B>in</B>] 
   *   The environment under which the editor is run.  
   * 
   * @param dir [<B>in</B>] 
   *   The working directory where the editor is run.
   *
   * @return 
   *   The controlling <CODE>SubProcess</CODE> instance. 
   * 
   * @see SubProcess
   */  
  public SubProcess
  launch
  (
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
  {
    ArrayList<String> args = new ArrayList<String>();
    for(File file : fseq.getFiles()) 
      args.add(file.getPath());

    SubProcess proc = new SubProcess(getName(), getProgram(), args, env, dir);
    proc.start();
    return proc;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   *  The name of the editor executable.
   */     
  private String  pProgram;
  
}



