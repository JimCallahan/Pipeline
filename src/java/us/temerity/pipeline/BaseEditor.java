// $Id: BaseEditor.java,v 1.8 2004/11/11 00:41:19 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * The superclass of all Pipeline editor plugins. <P>
 * 
 * New kinds of editors can be written by subclassing this class.  Due to the way plugins
 * are loaded and communicated between applications, any fields added to a subclass will
 * be reinitialized when the action is stored to disk or when it is sent over the network.
 */
public
class BaseEditor
  extends BasePlugin
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
   * Construct with the given name, version and description. 
   * 
   * @param name 
   *   The short name of the editor.
   * 
   * @param vid
   *   The editor plugin revision number. 
   * 
   * @param desc 
   *   A short description of the editor.
   *
   * @param program 
   *   A name of the editor executable.
   */ 
  protected
  BaseEditor
  (
   String name, 
   VersionID vid,
   String desc, 
   String program
  ) 
  {
    super(name, vid, desc);
    
    if(program == null)
      throw new IllegalArgumentException("The program cannot be (null)!");
    pProgram = program;
  }

  /**
   * Copy constructor. <P> 
   * 
   * Used internally to create a generic instances of plugin subclasses.  This constructor
   * should not be used in end user code! <P> 
   */ 
  public 
  BaseEditor
  (
   BaseEditor editor
  ) 
  {
    super(editor.pName, editor.pVersionID, editor.pDescription);

    pProgram = editor.pProgram;
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
    for(File file : fseq.getFiles()) 
      args.add(file.getPath());

    SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, env, dir);
    proc.start();
    return proc;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof BaseEditor)) {
      BaseEditor editor = (BaseEditor) obj;
      return (super.equals(obj) && 
	      pProgram.equals(editor.pProgram));
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5038577659347876974L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   *  The name of the editor executable.
   */     
  private String  pProgram;
  
}



