// $Id: JobEditAsReq.java,v 1.1 2007/02/07 21:15:14 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   E D I T   A S   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to launch an Editor plugin to edit the given files as the specified user.
 */
public
class JobEditAsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param editor 
   *  The editor plugin instance use to edit the files.
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
   */
  public
  JobEditAsReq
  (
   BaseEditor editor, 
   String author, 
   FileSeq fseq,      
   Map<String,String> env,      
   File dir        
  )
  {
    super();

    if(editor == null) 
      throw new IllegalArgumentException
	("The editor plugin cannot be (null)!");
    pEditor = editor; 

    if(author == null) 
      throw new IllegalArgumentException
	("The file owner cannot be (null)!");
    pAuthor = author; 

    if(fseq == null) 
      throw new IllegalArgumentException
	("The file sequence cannot be (null)!");
    pFileSeq = fseq; 

    if(env == null) 
      throw new IllegalArgumentException
	("The environment cannot be (null)!");
    pEnv = env;  
    
    if(dir == null) 
      throw new IllegalArgumentException
	("The working directory cannot be (null)!");
    pDir = dir; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the editor plugin instance use to edit the files.
   */
  public BaseEditor
  getEditor()
  {
    return pEditor;
  }

  /**
   * Get the name of the user owning the files.
   */
  public String
  getAuthor()
  {
    return pAuthor;
  }

  /**
   * Get the file sequence to edit.
   */
  public FileSeq
  getFileSeq()
  {
    return pFileSeq;
  }

  /**
   * Get the environment under which the editor is run.  
   */
  public Map<String,String>
  getEnvironment() 
  {
    return pEnv;
  }

  /**
   * Get the working directory where the editor is run.
   */
  public File
  getWorkingDir()
  {
    return pDir;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E R I A L I Z A B L E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the serializable fields to the object stream. <P> 
   * 
   * This enables the node to convert a dynamically loaded action plugin instance into a 
   * generic staticly loaded BaseAction instance before serialization.
   */ 
  private void 
  writeObject
  (
   java.io.ObjectOutputStream out
  )
    throws IOException
  {
    out.writeObject(new BaseEditor(pEditor));
    out.writeObject(pAuthor); 
    out.writeObject(pFileSeq); 
    out.writeObject(pEnv); 
    out.writeObject(pDir); 
  }  

  /**
   * Read the serializable fields from the object stream. <P> 
   * 
   * This enables the node to dynamically instantiate an action plugin instance and copy
   * its parameters from the generic staticly loaded BaseAction instance in the object 
   * stream. 
   */ 
  private void 
  readObject
  (
    java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
    BaseEditor editor = (BaseEditor) in.readObject();
    try {
      PluginMgrClient client = PluginMgrClient.getInstance();
      pEditor = client.newEditor(editor.getName(), editor.getVersionID(), editor.getVendor());
    }
    catch(PipelineException ex) {
      throw new IOException(ex.getMessage());
    }

    pAuthor  = (String) in.readObject(); 
    pFileSeq = (FileSeq) in.readObject(); 
    pEnv     = (Map<String,String>) in.readObject(); 
    pDir     = (File) in.readObject(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5457425952042786725L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The editor plugin instance. 
   */ 
  private BaseEditor  pEditor;
  
  /**
   * The name of the user owning the files.
   */ 
  private String  pAuthor;
 
  /**
   * The file sequence to edit. 
   */ 
  private FileSeq  pFileSeq;
 
  /**
   * The environment under which the editor is run.  
   */ 
  private Map<String,String>  pEnv;
 
  /**
   * The working directory where the editor is run.
   */ 
  private File  pDir;
 
}
  
