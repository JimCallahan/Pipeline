// $Id: LicenseKey.java,v 1.2 2004/07/24 18:14:33 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L I C E N S E   K E Y                                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * A symbolic key which represents a limited number of floating software licenses.
 * 
 * @see JobReqs
 */
public
class LicenseKey
  extends Described
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public
  LicenseKey() 
  {
    super();
  }

  /** 
   * Construct a new license key.
   * 
   * @param name 
   *   The name of the license key.
   * 
   * @param desc 
   *   A short description of the license key.
   * 
   * @param total
   *   The total number of licenses.
   */ 
  public
  LicenseKey
  (
   String name,  
   String desc, 
   int total
  ) 
  {
    super(name, desc);
    setTotal(total);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the total number of licenses.
   */ 
  public int 
  getTotal() 
  {
    return pTotal;
  }

  /**
   * Set the total number of licenses.
   */ 
  public void
  setTotal
  (
   int total
  ) 
  {
    if(total < 0) 
      throw new IllegalArgumentException
	("The total number of licenses cannot be negative!");
    pTotal = total;
  }


  /**
   * Get the available number of licenses.
   */ 
  public int 
  getAvailable() 
  {
    return Math.max((pTotal - pUsed), 0);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Attempt to aquire a license.
   * 
   * @return 
   *   Whether a license was available.
   */ 
  public boolean
  aquire() 
  {
    if((pUsed - pTotal) > 0) {
      pUsed++;
      return true;
    }

    return false;
  }

  /**
   * Release a previously aquired license.
   */ 
  public void
  release()
  {
    if(pUsed > 0) 
      pUsed--;
  }

  
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);
    
    encoder.encode("Description", pDescription);    
    encoder.encode("Total", pTotal);    
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    String desc = (String) decoder.decode("Description"); 
    if(desc == null) 
      throw new GlueException("The \"Description\" was missing!");
    pDescription = desc;

    Integer total = (Integer) decoder.decode("Total"); 
    if(total == null) 
      throw new GlueException("The \"Total\" was missing!");
    pTotal = total;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7616282979518347032L;


  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The total number of licenses.
   */ 
  private int  pTotal; 

  /**
   * The number of licenses currently in use.
   */ 
  private int  pUsed; 

}



