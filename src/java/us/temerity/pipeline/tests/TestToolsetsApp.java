// $Id: TestToolsetsApp.java,v 1.1 2004/02/23 23:50:35 jim Exp $

import us.temerity.pipeline.*;

import java.io.*; 
import java.util.*; 
import java.util.logging.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   T O O L S E T S                                                        */
/*------------------------------------------------------------------------------------------*/

public 
class TestToolsetsApp
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

    try {
      TestToolsetsApp app = new TestToolsetsApp();
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
    throws PipelineException
  {
    printToolset("sdev040211");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private void 
  printToolset
  (
   String name
  ) 
    throws PipelineException
  {
    System.out.print("Toolset: " + name + "\n");

    TreeMap<String,String> env = Toolsets.lookup(name);
    for(String key : env.keySet()) {
      String value = env.get(key);
      System.out.print("  " + key + " = " + value + "\n");    
    }

    System.out.print("\n");
  }
 
}
