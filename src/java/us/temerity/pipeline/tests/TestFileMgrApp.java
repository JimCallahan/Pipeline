// $Id: TestFileMgrApp.java,v 1.1 2004/03/12 13:49:09 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*; 
import java.util.*; 
import java.util.logging.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   F I L E   M G R                                                        */
/*------------------------------------------------------------------------------------------*/

public 
class TestFileMgrApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args  /* IN: command line arguments */
  )
  {
    Logs.init();
    //Logs.sub.setLevel(Level.FINEST);

    try {
      TestFileMgrApp app = new TestFileMgrApp();
      app.run();
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } 
 
    System.exit(0);
  }  


  public void 
  run() 
  {


  }

 
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/



}
