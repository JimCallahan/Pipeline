// $Id: NodeGetAnnotationsRsp.java,v 1.4 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   A N N O T A T I O N S   R S P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetAnnotationsReq NodeGetAnnotationsReq} request.
 */
public
class NodeGetAnnotationsRsp
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
   * @param annots
   *   The annotations for the node indexed by annotation name (may be empty) or 
   *   <CODE>null</CODE>.
   */
  public
  NodeGetAnnotationsRsp
  (
   TaskTimer timer, 
   TreeMap<String,BaseAnnotation> annots
  )
  { 
    super(timer);

    pAnnotations = new TreeMap<String,BaseAnnotation>(); 
    if(annots != null)
      pAnnotations.putAll(annots);

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getAnnotation():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get annotations for the node indexed by annotation name (may be empty).
   */ 
  public TreeMap<String,BaseAnnotation> 
  getAnnotations()
  {
    return pAnnotations;
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
    TreeMap<String,BaseAnnotation> annots = new TreeMap<String,BaseAnnotation>(); 

    for(String name : pAnnotations.keySet()) {
      BaseAnnotation annot = pAnnotations.get(name);
      if(annot != null) 
        annots.put(name, new BaseAnnotation(annot));
    }

    out.writeObject(annots);
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
    pAnnotations = new TreeMap<String,BaseAnnotation>(); 

    TreeMap<String,BaseAnnotation> annots = (TreeMap<String,BaseAnnotation>) in.readObject();
    for(String name : annots.keySet()) {
      BaseAnnotation annot = annots.get(name);
      if(annot != null) {
        try {
          PluginMgrClient client = PluginMgrClient.getInstance();
          BaseAnnotation nannot = client.newAnnotation(annot.getName(), 
                                                       annot.getVersionID(), 
                                                       annot.getVendor());
          nannot.setParamValues(annot);
          pAnnotations.put(name, nannot);
        }
        catch(PipelineException ex) {
          throw new IOException(ex.getMessage());
        }
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1987032555492456193L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The annotations for the node indexed by annotation name (may be empty).
   */ 
  private TreeMap<String,BaseAnnotation> pAnnotations; 

}
  
