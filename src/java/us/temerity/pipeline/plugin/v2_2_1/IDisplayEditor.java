// $Id: IDisplayEditor.java,v 1.1 2007/04/05 10:02:58 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

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
    super("I-Display", new VersionID("2.2.1"), "Temerity",
	  "The 3Delight image viewer.", 
	  "i-display"); 
    
    addSupport(OsType.MacOS); 
    addSupport(OsType.Windows); 

    underDevelopment();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1244874045028933206L;

}


