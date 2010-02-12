// $Id: JavaApp.java,v 1.1 2009/07/06 10:25:26 jim Exp $

package us.temerity.pipeline.apps;

import us.temerity.pipeline.*;
import us.temerity.pipeline.parser.*;

import java.io.*; 
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import javax.tools.*; 

/*------------------------------------------------------------------------------------------*/
/*   J A V A   A P P                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the <A HREF="../../../../man/pljava.html"><B>pljava</B></A>(1)
 * tool. <P> 
 */
public
class JavaApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  JavaApp() 
  {
    super("pljava");
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the application with the given command-line arguments.
   * 
   * @param args 
   *   The command-line arguments.
   */ 
  public static void 
  main 
  (
   String[] args
  )
  {
    JavaApp app = new JavaApp();
    app.packageArguments(args);

    boolean success = false;
    try {
      JavaOptsParser parser = new JavaOptsParser(app.getPackagedArgsReader()); 
      parser.setApp(app);
      parser.CommandLine();

      success = true;
    }
    catch(ParseException ex) {
      app.handleParseException(ex);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
 	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
 	 ex.getMessage()); 
    } 
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
    }
    finally {
      LogMgr.getInstance().cleanup();
    }

    System.exit(success ? 0 : 1);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the program entry point main(String[]) method by loading classes from the given 
   * JAR file. <P> 
   * 
   * If there is a Class-Paths entry in the JAR's manifest, any directories or JARs
   * mentioned will also be added to the class loaders search paths.
   * 
   * @param jarPath
   *   The file system path to the JAR file.
   * 
   * @param mainClassName
   *   The fully qualified name of the class containing the entry point method or 
   *   <CODE>null</CODE> if it is to be read from the JAR's Main-Class manifest entry.
   */ 
  public void 
  loadFromJar
  (
   Path jarPath,
   String mainClassName
  ) 
    throws PipelineException
  {
    Set<Path> classPaths = new TreeSet<Path>();
    classPaths.add(jarPath);

    String mclass = mainClassName;
    {
      JarFile jar = null;
      try {
        jar = new JarFile(jarPath.toFile(), false, JarFile.OPEN_READ); 
        Manifest manifest = jar.getManifest();
        if(manifest != null) {
          Attributes attrs = manifest.getMainAttributes(); 

          if(mclass == null) 
            mclass = attrs.getValue(Attributes.Name.MAIN_CLASS);

          String jarClassPaths = attrs.getValue(Attributes.Name.CLASS_PATH); 
          if(jarClassPaths != null) {
            for(String comp : jarClassPaths.split("\\s")) {
              if(comp.length() > 0) {
                try {
                  classPaths.add(new Path(comp));
                }
                catch(Exception ex) {
                  throw new PipelineException
                    ("The path (" + comp + ") contained in the Class-Paths attribute of " + 
                     "the Manifest in JAR file (" + jarPath + ") was not a legal file " + 
                     "system path!"); 
                }
              }
            }
          }
        }
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to read the Manifest of the JAR file (" + jarPath + ")!"); 
      }
      finally {
        try {
          if(jar != null) 
            jar.close(); 
        }
        catch(IOException ex) {
        }
      }
    }

    if(mclass == null) 
      throw new PipelineException 
        ("No main class was specified either using the --main option or in the Manifest " + 
         "of the JAR file (" + jarPath + ")!"); 

    loadClasses(classPaths, mclass, false); 
  }    

  /**
   * Set the program entry point main(String[]) method by loading classes from the given 
   * set of class root directory paths. <P> 
   * 
   * @param classPaths
   *   The file system paths of directories containing classes to load.
   * 
   * @param mainClassName
   *   The fully qualified name of the class containing the entry point method.
   */ 
  public void 
  loadFromClassPaths
  (
   Set<Path> classPaths, 
   String mainClassName
  ) 
    throws PipelineException 
  {
    loadClasses(classPaths, mainClassName, true); 
  }

  /**
   * Compile the given single Java source file containing the entry point main(String[]) 
   * method and load the resulting class files to set this entry point.<P> 
   * 
   * The provided Java source file must have the same name as the class it specifies
   * which must contain the entry point method.  This class should also be in the root 
   * Java package so that its name its completely determined by the last component of 
   * the supplied Java source file name (minus ".java" suffix).
   */ 
  public void 
  compileAndLoad
  (
   Path sourcePath
  )
    throws PipelineException
  {
    String sourceName = sourcePath.getName();
    if(!sourceName.endsWith(".java")) 
      throw new PipelineException
        ("The file (" + sourcePath + ") does not appear to be a Java source file!"); 
    String mainClassName = sourceName.substring(0, sourceName.length()-5);

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if(compiler == null) 
      throw new PipelineException
        ("Unable to find a Java Compiler to use to compile the source file!"); 
    
    StandardJavaFileManager filemgr = compiler.getStandardFileManager(null, null, null);
    if(filemgr == null) 
      throw new PipelineException
        ("Unable to obtain the default Compiler File Manager!"); 

    Path classPath = 
      new Path(new Path(PackageInfo.sTempPath, "pljava-" + PackageInfo.sUser), 
               Long.toString(System.currentTimeMillis()));

    File classDir = classPath.toFile(); 
    if(!classDir.mkdirs()) 
      throw new PipelineException
        ("Unable to create the temporary directory (" + classDir + ") used to store " +
         "the compiled Java byte-code for the given Java source file!");

    try {
      LinkedList<File> dir = new LinkedList<File>();
      dir.add(classDir); 
      filemgr.setLocation(StandardLocation.CLASS_OUTPUT, dir);
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to set temporary directory (" + classDir + ") used to store " +
         "the compiled Java byte-code for the given Java source file!");
    }

    {
      LinkedList<File> sources = new LinkedList<File>();
      sources.add(sourcePath.toFile());

      JavaCompiler.CompilationTask task = 
        compiler.getTask(null, filemgr, null, null, null, 
                         filemgr.getJavaFileObjectsFromFiles(sources)); 
      task.call();
    }

    {
      File fs[] = classDir.listFiles();
      if(fs != null) {
        for(File file : fs)
          FileCleaner.add(file);
      }
    }
    
    Set<Path> classPaths = new TreeSet<Path>();
    classPaths.add(classPath);
    loadClasses(classPaths, mainClassName, true); 
  }    

  /**
   * Set the program entry point main(String[]) method by loading classes from the given 
   * set of class root directory and/or JAR file paths. <P> 
   * 
   * @param classPaths
   *   The file system paths of directories containing classes to load.
   * 
   * @param mainClassName
   *   The fully qualified name of the class containing the entry point method.
   * 
   * @param dirsOnly
   *   Whether only directory paths are allowed in <CODE>classPaths</CODE>.
   */ 
  private void 
  loadClasses
  (
   Set<Path> classPaths, 
   String mainClassName,
   boolean dirsOnly
  ) 
    throws PipelineException 
  {
    URL[] urls = new URL[classPaths.size()];
    {
      int wk = 0;
      for(Path path : classPaths) {
        File cfile = null;
        try {
          cfile = path.toFile().getCanonicalFile(); 
        }
        catch(IOException ex) {
          throw new PipelineException
            ("Unable to determine the canonical file system path specified by " + 
             "(" + path + ")!", ex); 
        }

        boolean isDir = cfile.isDirectory(); 
        if(dirsOnly && !isDir)
          throw new PipelineException
            ("The class path (" + path + ") does not specify a valid directory!"); 

        Path cpath = new Path(cfile); 
        String spec = ("file:///" + cpath + (isDir ? "/" : "")); 
        try {
          urls[wk] = new URL(spec); 
        }
        catch(MalformedURLException ex) {
          throw new PipelineException
            ("Unable to construct a valid URL (" + spec + ") from the given path " + 
             "(" + path + ")!", ex); 
        }

        wk++;
      }
    }

    try {
      ClassLoader loader = new URLClassLoader(urls); 
      Class cls = loader.loadClass(mainClassName);
      pMainMethod = cls.getMethod("main", (new String[0]).getClass()); 
    }
    catch(Exception ex) {
      StringBuilder buf = new StringBuilder(); 
      buf.append("Unable to load main class (" + mainClassName + ") from:\n"); 
      for(URL url : urls) 
        buf.append("  " + url + "\n");
      throw new PipelineException(buf.toString(), ex); 
    }
  }    

  /**
   * Invoke the program entry point main(String[]) method with the given arguments.
   */ 
  public void 
  runProgram
  (
   ArrayList<String> args
  ) 
    throws PipelineException
  {
    try {
      String[] argAry = args.toArray(new String[0]);
      Object[] arguments = { args.toArray(argAry) };
      pMainMethod.invoke(null, arguments); 
    }  
    catch(IllegalAccessException ex) {
      throw new PipelineException
        ("Unable to invoke: " + pMainMethod + "\n" + 
         "The method was inaccessible due to Java language access control."); 
    }
    catch(IllegalArgumentException ex) {
      throw new PipelineException
        ("Unable to invoke: " + pMainMethod + "\n" + 
         "Due to mismatch between the method type signature and the provided arguments."); 
    }
    catch(InvocationTargetException ex) {
      throw new PipelineException
        (Exceptions.getFullMessage
         ("Unable to invoke: " + pMainMethod + "\n" + 
          "Due to the method throwing an exception.", ex));  
    }
    catch(ExceptionInInitializerError ex) {
      throw new PipelineException
        ("Unable to invoke: " + pMainMethod + "\n" + 
         "Because the initialization provoked by this method failed."); 
    }
    catch(Exception ex) {
      throw new PipelineException
        (Exceptions.getFullMessage("Unable to invoke: " + pMainMethod, ex)); 
    }
  }    

  

  /*----------------------------------------------------------------------------------------*/
  /*   O P T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The implementation of the <CODE>--help</CODE> command-line option.
   */ 
  public void
  help()
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "USAGE:\n" +
       "  pljava [options] --jar=... [--main=...][-- args ...]\n" + 
       "  pljava [options] --classpath=... [--classpath=...] --main=... [-- args ...]\n" +
       "  pljava [options] --source=... [-- args ...]\n" +  
       "\n" + 
       "  pljava --help\n" +
       "  pljava --html-help\n" +
       "  pljava --version\n" + 
       "  pljava --release-date\n" + 
       "  pljava --copyright\n" + 
       "  pljava --license\n" + 
       "\n" + 
       "OPTIONS:\n" +
       "  [--log-file=...][--log-backups=...][--log=...]\n" +
       "\n" + 
       "\n" +  
       "Use \"pljava --html-help\" to browse the full documentation.\n");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Generate an explanitory message for the non-literal token.
   */ 
  protected String
  tokenExplain
  (
   int kind,
   boolean printLiteral
  ) 
  {
    switch(kind) {
    case JavaOptsParserConstants.EOF:
      return "EOF";

    case JavaOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case JavaOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case JavaOptsParserConstants.PATH_ARG:
      return "an file system path";

    case JavaOptsParserConstants.INTEGER:
      return "an integer";

    case JavaOptsParserConstants.STRING:
      return "a string";

    case JavaOptsParserConstants.JAVA_ARG: 
      return "a Java program argument";

    default: 
      if(printLiteral) 
	return JavaOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The entry point method for running the Java program.
   */ 
  private Method  pMainMethod; 

}

