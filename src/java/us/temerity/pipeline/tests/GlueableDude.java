// $Id: GlueableDude.java,v 1.2 2004/03/23 20:41:25 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   Test class used by TestGlueApp                                                         */
/*------------------------------------------------------------------------------------------*/

public class GlueableDude 
  implements Glueable 
{ 
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public GlueableDude()
  {
    myName = "Nobody";
    myAge  = 0;
  }

  public GlueableDude(String name, int age) 
  {
    myName = name;
    myAge  = age;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void toGlue(GlueEncoder ge) 
    throws GlueException
  {
    ge.encode("Name", myName);
    ge.encode("Age", myAge);
  }

  public void fromGlue(GlueDecoder gd) 
    throws GlueException
  {
    myName = (String)  gd.decode("Name");
    myAge  = (Integer) gd.decode("Age");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private String myName;
  private int    myAge;

}
