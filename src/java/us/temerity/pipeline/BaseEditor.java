// $Id: BaseEditor.java,v 1.18 2007/02/07 21:08:53 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder; 

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
 * be reinitialized when the action is stored to disk or when it is sent over the network. <P>
 * 
 * While new plugin subclass versions are being modified and tested the 
 * {@link #underDevelopment underDevelopment} method should be called in the subclasses
 * constructor to enable the plugin to be dynamically reloaded.  
 */
public
class BaseEditor
  extends BasePlugin
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */ 
  public
  BaseEditor() 
  {
    super();
  }

  /** 
   * Construct with the given name, version, vendor and description. 
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
  BaseEditor
  (
   String name, 
   VersionID vid,
   String vendor, 
   String desc, 
   String program
  ) 
  {
    super(name, vid, vendor, desc);
    
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
    super(editor.pName, editor.pVersionID, editor.pVendor, editor.pDescription);

    setSupports(editor.getSupports());

    pProgram = editor.pProgram;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the Editor subclass implements the {@link #prep prep} method. <P> 
   * 
   * The default implementation returns <CODE>false</CODE>.  Subclasses which implement the
   * newer {@link #prep prep} method which replaces the deprecated {@link #launch launch} 
   * method should override this method to return <CODE>true</CODE>.
   */ 
  public boolean
  hasPrepMethod() 
  {
    return false;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets the name of the editor executable. <P> 
   * 
   * Automatically appends ".exe" to the program name when running under Windows.
   */
  public String
  getProgram() 
  {
    if(PackageInfo.sOsType == OsType.Windows) 
      return (pProgram + ".exe");
    return pProgram;
  }

   
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get which general type of plugin this is. 
   */ 
  public PluginType
  getPluginType()
  {
    return PluginType.Editor;
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a {@link SubProcessLight} instance which when executed will launch an editor
   * program to view the given file sequence as arguments. <P> 
   * 
   * The default implementation returns <CODE>null</CODE> so that subclasses implemented 
   * prior to the addition of this method can be be determined by checking the return 
   * value.  Code which launches Editor plugins should first try to generate a 
   * {@link SubProcessLight} instance using this method.  If this returns <CODE>null</CODE>
   * then the {@link #launch launch} method should be then called to support older 
   * implementations. <P> 
   * 
   * For default functionality similar to the {@link #launch launch} method subclasses should
   * be derrived from the {@link SimpleEditor} class instead of {@link BaseEditor}.  This 
   * class implements a {@link #prep prep} method which is identical to functionality of the
   * {@link #launch launch} of this class except for not executing the process and adding 
   * support for substituting user ownership.  See the explanation of why {@link #launch 
   * launch} was deprecated for details.
   * 
   * @para author
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
    return null;
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
    String dpath = dir.toString();
    ArrayList<String> args = new ArrayList<String>();
    for(File file : fseq.getFiles()) {
      String fpath = file.getPath();
      if(fpath.startsWith(dpath)) 
	args.add(fpath.substring(dpath.length()+1));
      else 
	args.add(fpath); 
    }

    SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, env, dir);
    proc.start();
    return proc;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   M I S C   F I L E   U T I L S                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Insures that the given working directory containing the files to be edited exists.
   */
  public void 
  makeWorkingDirs
  (
   File dir
  ) 
    throws PipelineException
  {
    if(dir.exists()) {
      if(!dir.isDirectory()) 
	throw new PipelineException
	  ("Unable to create the working area directory (" + dir + ") because a file with " + 
	   "the same name already exists!"); 
      return;
    }

    try {
      if(!dir.mkdirs()) 
	throw new PipelineException
	  ("Unable to create the working area directory (" + dir + ") containing the file " + 
	   "to be edited!");
    }
    catch(SecurityException ex) {
      throw new PipelineException
	("You do not have the necessary permission to create the working area directory " + 
	 "(" + dir + ") containing the file to be edited!");
    }
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
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    BaseEditor clone = (BaseEditor) super.clone();
    clone.pProgram = pProgram;
    
    return clone;
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



