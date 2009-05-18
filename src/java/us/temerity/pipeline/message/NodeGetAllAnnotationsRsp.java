// $Id: NodeGetAllAnnotationsRsp.java,v 1.1 2009/05/18 06:31:49 jesse Exp $

package us.temerity.pipeline.message;

import java.io.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   A L L   A N N O T A T I O N S   R S P                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a 
 * {@link NodeGetAllBothAnnotationsReq NodeGetAllBothAnnotationsReq} request.
 */
public
class NodeGetAllAnnotationsRsp
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
  NodeGetAllAnnotationsRsp
  (
    TaskTimer timer, 
    DoubleMap<NodeID, String, BaseAnnotation> annots
  )
  { 
    super(timer);

    pAnnotations = new DoubleMap<NodeID, String, BaseAnnotation>(); 
    if(annots != null)
      pAnnotations.putAll(annots);

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getBothAnnotation(TreeSet<NodeID>): " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get annotations for the node indexed by node id and annotation name (may be empty).
   */ 
  public DoubleMap<NodeID, String, BaseAnnotation> 
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
    ObjectOutputStream out
  )
    throws IOException
  {
    DoubleMap<NodeID, String,BaseAnnotation> annots = 
      new DoubleMap<NodeID, String, BaseAnnotation>(); 

    
    for(NodeID nodeID : pAnnotations.keySet()) {
      for (String name : pAnnotations.keySet(nodeID)) {
      BaseAnnotation annot = pAnnotations.get(nodeID, name);
      if(annot != null) 
        annots.put(nodeID, name, new BaseAnnotation(annot));
      }
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
  @SuppressWarnings("unchecked")
  private void 
  readObject
  (
    ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
    pAnnotations = new DoubleMap<NodeID, String, BaseAnnotation>(); 

    DoubleMap<NodeID, String, BaseAnnotation> annots = 
      (DoubleMap<NodeID, String, BaseAnnotation>) in.readObject();
    for (NodeID nodeID : annots.keySet()) {
      for(String name : annots.keySet(nodeID)) {
        BaseAnnotation annot = annots.get(nodeID, name);
        if(annot != null) {
          try {
            PluginMgrClient client = PluginMgrClient.getInstance();
            BaseAnnotation nannot = client.newAnnotation(annot.getName(), 
                                                         annot.getVersionID(), 
                                                         annot.getVendor());
            nannot.setParamValues(annot);
            pAnnotations.put(nodeID, name, nannot);
          }

          catch(PipelineException ex) {
            throw new IOException(ex.getMessage());
          }
        }
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4398799784861986692L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The annotations for the node indexed by node id and annotation name (may be empty).
   */ 
  private DoubleMap<NodeID, String, BaseAnnotation> pAnnotations; 

}
  
