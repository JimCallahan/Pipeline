// $Id: IDisplayEditor.java,v 1.1 2006/05/07 21:33:59 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   I - D I S P L A Y   E D I T O R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The 3Delight image viewer. <P> 
 * 
 * See the <A href="http://www.3delight.com">3Delight</A> documentation for 
 * <A href="http://www.3delight.com/ZDoc/i-display.html"><B>i-display</B></A>(1) for 
 * details. <P> 
 */
public
class IDisplayEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  IDisplayEditor()
  {
    super("I-Display", new VersionID("2.0.9"), "Temerity",
	  "The 3Delight image viewer.", 
	  "i-display"); 
    
    addSupport(OsType.Windows); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5907024349857072792L;

}


