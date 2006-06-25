// $Id: BaseEditor.java,v 1.15 2006/06/25 23:29:27 jim Exp $

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
   * Get the name of the catagory of this plugin.
   */ 
  public String 
  getPluginCatagory() 
  {
    return "Editor";
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



