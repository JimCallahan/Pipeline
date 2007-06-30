// $Id: ExtReqs.java,v 1.2 2007/06/30 23:14:03 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   E X T   R E Q S                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The requirements on internal resources for a server extension operation.
 */
public 
class ExtReqs
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new set of master extension operation requirements with nothing required.
   */
  public
  ExtReqs() 
  {}

  /** 
   * Construct a new set of master extension operation requirements.
   * 
   * @param needsAnnotations
   *   Whether the operation being extended requires access to Node Annotations.
   * 
   * @param needsWorkGroups
   *   Whether the operation being extended requires access to Node WorkGroups.
   */
  public
  ExtReqs
  (
   boolean needsAnnotations, 
   boolean needsWorkGroups
  ) 
  {
    pNeedsAnnotation = needsAnnotations;
    pNeedsWorkGroups = needsWorkGroups;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the operation being extended requires access to Node Annotations.<P> 
   * 
   * If this is <CODE>true</CODE>, then the server will add any existing Annotation plugin 
   * instances to to those available using the {@link BaseExt#getAnnotation 
   * BaseExt.getAnnotation} method for the nodes involved in the operation. 
   */ 
  public boolean
  needsAnnotations() 
  {
    return pNeedsAnnotation;
  }
  
  /**
   * Whether the operation being extended requires access to Node WorkGroups.
   * 
   * If this is <CODE>true</CODE>, then a table describing the work group memberships of 
   * the user performing the operation will be available using the 
   * {@link BaseExt#getWorkGroups BaseExt.getWorkGroups} method.
   */ 
  public boolean
  needsWorkGroups() 
  {
    return pNeedsWorkGroups;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the operation being extended requires access to Node Annotations.
   */ 
  private boolean pNeedsAnnotation; 
  
  /**
   * Whether the operation being extended requires access to Node WorkGroups.
   */
  private boolean pNeedsWorkGroups;

}



