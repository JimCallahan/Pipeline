// $Id: NodeAddAnnotationReq.java,v 1.1 2007/06/15 00:27:31 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   A D D   A N N O T A T I O N   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to add the given annotation to the set of current annotations for the given node.
 */
public
class NodeAddAnnotationReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param nname 
   *   The fully resolved node name.
   * 
   * @param aname 
   *   The name of the annotation. 
   * 
   * @param annot 
   *   The new node annotation to add.
   */
  public
  NodeAddAnnotationReq
  (
   String nname, 
   String aname, 
   BaseAnnotation annot 
  )
  { 
    if(nname == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pNodeName = nname;

    if(aname == null) 
      throw new IllegalArgumentException("The annotation name cannot be (null)!");
    pAnnotationName = aname;

    if(annot == null) 
      throw new IllegalArgumentException("The annotation cannot be (null)!");
    pAnnotation = annot;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node name.
   */
  public String
  getNodeName() 
  {
    return pNodeName;
  }

  /**
   * Gets the annotation name.
   */
  public String
  getAnnotationName() 
  {
    return pAnnotationName;
  }

  /**
   * Get the annotation plugin instance.
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
    out.writeObject(pNodeName); 
    out.writeObject(pAnnotationName); 
    out.writeObject(new BaseAnnotation(pAnnotation));
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
    pNodeName = (String) in.readObject();
    pAnnotationName = (String) in.readObject();

    BaseAnnotation annot = (BaseAnnotation) in.readObject();
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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 500101053058203883L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name.
   */ 
  private String  pNodeName;

  /**
   * The annotation name.
   */ 
  private String  pAnnotationName;

  /**
   * The annotation plugin instance.
   */ 
  private BaseAnnotation  pAnnotation; 

}
  
