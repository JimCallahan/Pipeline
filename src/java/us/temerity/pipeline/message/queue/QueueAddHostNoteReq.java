// $Id: QueueGetByNameReq.java,v 1.1 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline.message.queue;

import us.temerity.pipeline.SimpleLogMessage;
import us.temerity.pipeline.message.PrivilegedReq;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   A D D   H O S T   N O T E   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Set the note for the given host.   
 */
public 
class QueueAddHostNoteReq
  extends PrivilegedReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param hname                                                                              
   *   The fully resolved name of the host.                                                    
   *                                                                                           
   * @param note                                                                               
   *   The server note or <CODE>null</CODE> to clear. 
   */
  public
  QueueAddHostNoteReq
  (
   String hname, 
   SimpleLogMessage note
  )
  {
    if(hname == null) 
      throw new IllegalArgumentException("The host name cannot be (null)!");
    pHostName = hname; 

    pHostNote = note; 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the host. 
   */
  public String
  getHostName()
  {
    return pHostName;
  }
  
  /**                                                                                             
   * Get the host note or <CODE>null</CODE> to clear. 
   */                                                                                             
  public SimpleLogMessage                                                                         
  getHostNote()                                                                                   
  {                                                                                               
    return pHostNote;                                                                             
  }                         

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -4979891214485540619L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the host. 
   */
  private String pHostName;
                                                                                           
  /**                                                                                             
   * The host note or <CODE>null</CODE> to clear.                                           
   */                                                                                             
  private SimpleLogMessage  pHostNote;      

}
