// $Id: BaseGenStateTest.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   G E N   S T A T E   T E S T                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Abstract base class for all classes which generate Java source code for Pipeline state 
 * test classes.
 */ 
public 
class BaseGenStateTest
  extends BaseCodeGen
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new Java source code generator.
   * 
   * @param title
   *   The string used to construct the title banner comment in the source code.
   * 
   * @param className
   *   The literal name of the Java class to create.
   * 
   * @param superClassName
   *   The literal name of the superclass of the Java class to create.
   * 
   * @param resultClassName
   *   The literal name of the Java class to return from the generated 
   *   <CODE>computeState</CODE> method. 
   */ 
  public
  BaseGenStateTest
  (
   String title,
   String className, 
   String superClassName, 
   String resultClassName
  ) 
    throws ParseException 
  {
    pTitle = title; 
    pClassName = className; 
    pSuperClassName = superClassName; 
    pResultClassName = resultClassName; 
    
    pRootGenerator = new RootTestGenerator();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   C O D E   G E N E R A T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate the Java source code for the test class and write it to file.
   */ 
  public final void 
  generateClassFile() 
    throws GenerateException 
  {
    /* */ 
    String body = null;
    try {
      body = pRootGenerator.generate(true, true, 1);
    }
    catch(GenerateException ex) {
      throw new GenerateException
        ("While attempting to generate the body of " + pClassName + ".computeState():\n" + 
         "After having processed states: " + ex.getMessage());
    }

    /* generate the source code string */ 
    String sourceCode = 
      ("// $Id: BaseGenStateTest.java,v 1.1 2009/04/07 06:01:20 jim Exp $\n" +
       "\n" + 
       "package us.temerity.pipeline.core;\n" + 
       "\n" + 
       "import us.temerity.pipeline.*;\n" + 
       "\n" + 
       "import java.io.*;\n" + 
       "\n" + 
       genHeader(pTitle) + 
       "/**\n" + 
       " * .\n" + 
       " */\n" +
       "public\n" + 
       "class " + pClassName + "\n" + 
       "  extends " + pSuperClassName + "\n" + 
       "{\n" + 
       genHeader("CONSTRUCTOR", 1) +
       "\n" + 
       "  /**\n" +
       "   * Construct an instance of the test.\n" + 
       "   */\n" + 
       "  public\n" + 
       "  " + pClassName + "\n" + 
       "  (\n" + 
       "    VersionState versionState,\n" + 
       "    NodeMod work,\n" + 
       "    NodeVersion base,\n" + 
       "    NodeVersion latest\n" + 
       "  )\n" + 
       "  {\n" + 
       "    super(versionState, work, base, latest);\n" + 
       "  }\n" + 
       "\n" +
       "\n" + 
       "\n" + 
       genHeader("STATE TESTS", 1) +
       "\n" + 
       "  /**\n" + 
       "   * Perform the tests needed to compute the PropertyState.\n" + 
       "   */\n" + 
       "  public " + pResultClassName + "\n" + 
       "  computeState()\n" + 
       body + "\n" + 
       "}\n");
      
    /* write the file */ 
    File file = new File("core/" + pClassName + ".java"); 
    try {
      FileWriter out = new FileWriter(file);
      out.write(sourceCode);
      out.close();
    }
    catch(IOException ex) {
      throw new GenerateException
        ("Unable to write the source code for class (" + pClassName + ") to disk!", ex); 
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
   
  /** 
   * The string used to construct the title banner comment in the source code.
   */ 
  protected final String pTitle; 

  /**
   * The literal name of the Java class to create.
   */ 
  protected final String pClassName; 

  /**
   * The literal name of the superclass of the Java class to create.
   */ 
  protected final String pSuperClassName; 

  /**
   * The literal name of the Java class to return from the generated 
   * <CODE>computeState</CODE> method. 
   */ 
  protected final String pResultClassName; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Root of the test generator tree. 
   */ 
  protected BaseGenerator pRootGenerator; 

}



