// $Id: PythonApp.java,v 1.3 2008/02/14 20:26:29 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.bootstrap.*;

import org.python.core.*; 
import org.python.util.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   P Y T H O N   A P P                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the <A HREF="../../../../man/plpython.html"><B>plpython</B></A>(1)
 * tool. <P> 
 */
public
class PythonApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  PythonApp() 
  {
    super("plpython");
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   R U N                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the application with the given command-line arguments.
   * 
   * @param args 
   *   The command-line arguments.
   */ 
  public
  void 
  run
  (
   String[] args
  )
  {
    packageArguments(args);

    boolean success = false;
    try {
      PythonOptsParser parser = 
	new PythonOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      parser.CommandLine();

      success = true;
    }
    catch(ParseException ex) {
      handleParseException(ex);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage()); 
    }    
    catch (PyException pye) {
      if(Py.matchException(pye, Py.SystemExit)) {
        PyObject value = pye.value;
        if(value instanceof PyInstance) {
          PyObject pycode = value.__findattr__("code");
          if(pycode instanceof PyInteger) {
            PyInteger code = (PyInteger) pycode;
            System.exit(code.getValue());
          }
          else if(pycode instanceof PyNone) {
            System.exit(0);
          } 
        }
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         Exceptions.getFullMessage(pye));      
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
   * Read Python source code from a file.
   */ 
  public void 
  executeScript
  (
   Path script, 
   ArrayList<String> args
  ) 
    throws PipelineException 
  {
    PythonInterpreter python = new PythonInterpreter(); 
    initPytonArgs(python, script.getName(), args);
    python.execfile(script.toOsString());
  }

  /**
   * Read Python source code from STDIN.
   */ 
  public void 
  readScriptFromStdIn
  (  
   ArrayList<String> args
  ) 
    throws PipelineException 
  {
    InteractiveInterpreter python = new InteractiveInterpreter();
    initPytonArgs(python, "-", args);

    try {
      LineNumberReader reader = new LineNumberReader(new InputStreamReader(System.in)); 
      try {
        while(true) {
          String line = reader.readLine();
          if(line == null) 
            break;
          
          python.runsource(line);
        }
      }
      catch(IOException ex) {
        throw new PipelineException
          ("While reading Python code from STDIN (line " + reader.getLineNumber() + "): " + 
           ex.getMessage());
      }
      finally {
        reader.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to read Python code from STDIN:\n\n" + 
         ex.getMessage());
    }
  }
  
  /**
   * Start an interactive Python session.
   */ 
  public void 
  interactive() 
    throws PipelineException 
  {
    InteractiveConsole python = new InteractiveConsole();
    initPytonArgs(python, "", new ArrayList<String>());    
    python.interact("Pipeline " + PackageInfo.sVersion + "\n" + 
                     python.getDefaultBanner()); 
  }

  /**
   * Inialize the Python programs command line arguments.
   */ 
  private void 
  initPytonArgs
  (
   PythonInterpreter python, 
   String program, 
   ArrayList<String> args
  ) 
  {
    python.exec("import sys");

    StringBuilder buf = new StringBuilder();
    for(String arg : args) 
      buf.append(", \"" + arg.replaceAll("\"", "\\\\\"") + "\"");
 
    python.exec("sys.argv = ['" + program + "'" + buf + "]");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The implementation of the <CODE>--copyright</CODE> command-line option.
   */ 
  public void
  copyright()
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       PackageInfo.sCopyright + "\n\n" +
       PySystemState.copyright);
  }

  /**
   * The implementation of the <CODE>--help</CODE> command-line option.
   */ 
  public void
  help()
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "USAGE:\n" +
       "  plpython [options] [--file=... | --stdin]\n" +  
       "\n" + 
       "  plpython --help\n" +
       "  plpython --html-help\n" +
       "  plpython --version\n" + 
       "  plpython --release-date\n" + 
       "  plpython --copyright\n" + 
       "  plpython --license\n" + 
       "\n" + 
       "OPTIONS:\n" +
       "  [--log-file=...][--log-backups=...][--log=...]\n" +
       "\n" + 
       "\n" +  
       "Use \"plpython --html-help\" to browse the full documentation.\n");
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
    case PythonOptsParserConstants.EOF:
      return "EOF";

    case PythonOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case PythonOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case PythonOptsParserConstants.PATH_ARG:
      return "an file system path";

    case PythonOptsParserConstants.INTEGER:
      return "an integer";

    case PythonOptsParserConstants.PYTHON_ARG: 
      return "python program argument";

    default: 
      if(printLiteral) 
	return PythonOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }
}

