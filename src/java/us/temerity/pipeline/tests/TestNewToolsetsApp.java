// $Id: TestNewToolsetsApp.java,v 1.1 2004/05/21 21:17:51 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.toolset.*;

import java.io.*; 
import java.util.*; 
import java.util.logging.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   T O O L S E T S                                                        */
/*------------------------------------------------------------------------------------------*/

public 
class TestNewToolsetsApp
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
      TestNewToolsetsApp app = new TestNewToolsetsApp();
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
    throws PipelineException, GlueException
  {
    File dir = new File("data/toolsets");

    String files[] = dir.list();
    int wk;
    for(wk=0; wk<files.length; wk++) {
      File file = new File(dir, files[wk]);
      if(file.isFile()) {
	PackageMod mod = new PackageMod(file.getName());
	mod.init(file);
	printPackage(mod);
      }
    }
  }

  



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private void 
  printPackage
  (
   PackageCommon com
  ) 
    throws GlueException
  { 
    GlueEncoder ge = new GlueEncoderImpl("Package", com);
    System.out.print(ge.getText() + "\n");
  }
 
}
