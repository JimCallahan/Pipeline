// $Id: TestNativeApp.java,v 1.1 2004/08/28 19:43:13 jim Exp $

import us.temerity.pipeline.*;

import java.io.*; 
import java.math.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   V E R S I O N   I D                                                    */
/*------------------------------------------------------------------------------------------*/

class TestNativeApp
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
    try {
      TestNativeApp app = new TestNativeApp();
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
    String name = "DNotify";
    String lname = System.mapLibraryName(name);

    System.out.print("Map: " + name + " -> " + lname + "\n");

    System.getProperties().list(new PrintStream(System.out));
  }


}
