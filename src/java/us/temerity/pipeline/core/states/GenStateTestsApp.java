// $Id: GenStateTestsApp.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.awt.*; 
import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G E N   S T A T E   T E S T S   A P P                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates the Java source code for the node and queue state testing classes.
 */ 
public 
class GenStateTestsApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GenStateTestsApp() 
  {}


 
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The top-level function.
   */ 
  public static void 
  main
  (
   String[] args 
  )
  {
    try {
      if(args.length != 1) 
        throw new IllegalArgumentException("You must provide the Java source directory!"); 

      GenStateTestsApp app = new GenStateTestsApp();
      app.generateClasses(new File(args[0]));
    }
    catch(ParseException ex) {
      System.err.print(ex.getMessage()); 
    }
    catch(GenerateException ex) {
      System.err.print(ex.getMessage()); 
    }
    catch(Exception ex) {
      ex.printStackTrace(); 
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   G E N E R A T E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate the Java source code.
   * 
   * @param sourceDir
   *   The directory containing the Pipeline Java source code.
   */ 
  public void 
  generateClasses
  (
   File sourceDir
  ) 
    throws ParseException, GenerateException 
  {
    GenPropertyStateTest propTest = new GenPropertyStateTest();
    {
      File file = new File(sourceDir, "core/states/data/PropertyStates.txt"); 
      propTest.parseSpreadsheet(file); 
      propTest.generateClassFile();
    }

    // ... 

  }

}


