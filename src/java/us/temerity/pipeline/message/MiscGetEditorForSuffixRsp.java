// $Id: MiscGetEditorForSuffixRsp.java,v 1.5 2005/09/07 21:11:16 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   S U F F I X   E D I T O R S   R S P                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscGetEditorForSuffixReq MiscGetEditorForSuffixReq} 
 * request.
 */
public
class MiscGetEditorForSuffixRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param editor 
   *   The editor plugin instance or <CODE>null</CODE> if undefined.
   */ 
  public
  MiscGetEditorForSuffixRsp
  (
   TaskTimer timer, 
   BaseEditor editor
  )
  { 
    super(timer);

    pEditor = editor;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getEditorForSuffix():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the editor plugin instance or <CODE>null</CODE> if undefined.
   */
  public BaseEditor
  getEditor() 
  {
    return pEditor;
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
    if(pEditor != null)
      out.writeObject(new BaseEditor(pEditor));
    else 
      out.writeObject((BaseEditor) null);
  }  

  /**
   * Read the serializable fields from the object stream. <P> 
   * 
   * This enables the node to dynamically instantiate an action plugin instance from 
   * the generic staticly loaded BaseAction instance in the object stream. 
   */ 
  private void 
  readObject
  (
    java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
    BaseEditor editor = (BaseEditor) in.readObject();
    if(editor != null) {
      try {
	PluginMgrClient client = PluginMgrClient.getInstance();
	pEditor = client.newEditor(editor.getName(), editor.getVersionID(), editor.getVendor());
      }
      catch(PipelineException ex) {
	throw new IOException(ex.getMessage());
      }
    }
    else {
      pEditor = null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1549480439983487721L;


  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The editor plugin instance or <CODE>null</CODE> if undefined.
   */ 
  private BaseEditor  pEditor;

}
  
