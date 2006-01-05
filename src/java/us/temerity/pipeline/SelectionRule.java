// $Id: SelectionRule.java,v 1.1 2006/01/05 16:54:43 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   R U L E                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A selection schedule rule which is always active. <P> 
 * 
 * Can be used as a default rule, but is also the base class and common interface for more 
 * specialized selection rules.
 */
public 
class SelectionRule
  implements Cloneable, Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new selection schedule rule.
   */ 
  public
  SelectionRule()
  {}

  /**
   * Copy constructor. 
   */ 
  public 
  SelectionRule
  (
   SelectionRule rule
  )
  {
    pGroup = rule.pGroup;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the selection group which is activated by this rule.
   * 
   * @return
   *   The selection group or <CODE>null</CODE> if undefined.
   */ 
  public String
  getGroup() 
  {
    return pGroup;
  }

  /**
   * Set the name of the selection group which is activated by this rule or 
   * <CODE>null</CODE> to clear.
   */ 
  public void
  setGroup
  (
   String name
  ) 
  {
    pGroup = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the rule is active during the given point in time.
   */ 
  public boolean
  isActive
  (
   Date date
  )
  {
    return true;
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
    return new SelectionRule(this);
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
    encoder.encode("Group", pGroup);  
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    pGroup = (String) decoder.decode("Group"); 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -9118929846011805641L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the selection group which is activated by this rule.
   */ 
  protected String  pGroup; 
}
