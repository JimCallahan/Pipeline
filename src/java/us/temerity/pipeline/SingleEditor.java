// $Id: SingleEditor.java,v 1.2 2004/02/25 01:28:15 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S I N G L E   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of Pipline node editor plugins that can only edit single files.
 */
public
class SingleEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected
  SingleEditor() 
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
  SingleEditor
  (
   String name,  
   String desc,
   String program 
  ) 
  {
    super(name, desc, program);
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
    FrameRange range = fseq.getFrameRange(); 
    if((range != null) && (!range.isSingle()))
      throw new IllegalArgumentException
	("The " + getName() + " Editor can only edit a single file at a time!");

    return super.launch(fseq, env, dir);
  }
}


