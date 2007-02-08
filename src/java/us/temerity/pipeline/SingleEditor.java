// $Id: SingleEditor.java,v 1.13 2007/02/08 01:49:32 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;
   
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
    try {
      FrameRange range = fseq.getFrameRange(); 
      if((range != null) && (!range.isSingle()))
	throw new PipelineException
	  ("The " + getName() + " Editor can only edit a single file at a time!");
      
      return super.prep(author, fseq, env, dir);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to launch this Editor!\n" +
	 ex.getMessage());
    }    
  }

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
    FrameRange range = fseq.getFrameRange(); 
    if((range != null) && (!range.isSingle()))
      throw new PipelineException
	("The " + getName() + " Editor can only edit a single file at a time!");

    return super.launch(fseq, env, dir);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3940846614355631531L;

}


