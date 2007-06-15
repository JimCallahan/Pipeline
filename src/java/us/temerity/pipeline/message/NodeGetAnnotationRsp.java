// $Id: NodeGetAnnotationRsp.java,v 1.1 2007/06/15 00:27:31 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   A N N O T A T I O N   R S P                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetAnnotationReq NodeGetAnnotationReq} request.
 */
public
class NodeGetAnnotationRsp
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
   * @param annot 
   *   The annotation instance or <CODE>null</CODE> if none exists.
   */
  public
  NodeGetAnnotationRsp
  (
   TaskTimer timer, 
   BaseAnnotation annot 
  )
  { 
    super(timer);

    pAnnotation = annot;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getAnnotation(): " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the annotation plugin instance or <CODE>null</CODE> if none exists.
   */ 
  public BaseAnnotation
  getAnnotation()
  {
    return pAnnotation;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E R I A L I Z A B L E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the serializable fields to the object stream. <P> 
   * 
   * This enables the class to convert a dynamically loaded annotation plugin instance into a 
   * generic staticly loaded BaseAnnotation instance before serialization.
   */ 
  private void 
  writeObject
  (
   java.io.ObjectOutputStream out
  )
    throws IOException
  {
    BaseAnnotation annot = null; 
    if(pAnnotation != null) 
      annot = new BaseAnnotation(pAnnotation);
    out.writeObject(annot);
  }
  
  /**
   * Read the serializable fields from the object stream. <P> 
   * 
   * This enables the class to dynamically instantiate an annotation plugin instance and copy
   * its parameters from the generic staticly loaded BaseAnnotation instance in the object 
   * stream. 
   */ 
  private void 
  readObject
  (
   java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
    BaseAnnotation annot = (BaseAnnotation) in.readObject();
    if(annot != null) {
      try {
        PluginMgrClient client = PluginMgrClient.getInstance();
        pAnnotation = client.newAnnotation(annot.getName(), 
                                           annot.getVersionID(), 
                                           annot.getVendor());
        pAnnotation.setParamValues(annot);
      }
      catch(PipelineException ex) {
        throw new IOException(ex.getMessage());
      }
    }
    else {
      pAnnotation = null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7517366956551318456L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The annotation plugin instance.
   */ 
  private BaseAnnotation  pAnnotation; 

}
  
