// $Id: TestToolsetsApp.java,v 1.3 2004/03/07 02:47:29 jim Exp $

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

    assert(!Toolsets.exists("bogus"));

    try {
      Toolsets.lookup("bogus");
    }
    catch (PipelineException ex) {
      System.out.print(ex + "\n");
    }
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

    SortedMap<String,String> env = Toolsets.lookup(name);
    for(String key : env.keySet()) {
      String value = env.get(key);
      System.out.print("  " + key + " = " + value + "\n");    
    }

    System.out.print("\n");
  }
 
}
