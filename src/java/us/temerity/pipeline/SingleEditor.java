// $Id: SingleEditor.java,v 1.14 2007/03/29 19:31:51 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;
   
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S I N G L E   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of Pipline node editor plugins that can only edit single files.
 * 
 * @deprecated
 *   This class does not implement the new {@link BaseEditor#prep prep} method and exists 
 *   solely to support existing Editor plugins already derrived from this class. 
 */
@Deprecated
public
class SingleEditor
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
  SingleEditor() 
  {
    super();
  }

  /** 
   * Construct with a name and description. 
   * 
   * @param name 
   *   The short name of the editor.  
   * 
   * @param vid
   *   The action plugin revision number. 
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   * @param program 
   *   A name of the editor executable. 
   */ 
  protected
  SingleEditor
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
   * Launch the editor program (obtained with {@link #getName getName}) under the given 
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
   *   The controlling <CODE>SubProcess</CODE> instance. 
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
    if(!fseq.isSingle())
      throw new PipelineException
        ("The " + getName() + " Editor can only edit a single file at a time!");

    return super.launch(fseq, env, dir);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3940846614355631531L;

}


