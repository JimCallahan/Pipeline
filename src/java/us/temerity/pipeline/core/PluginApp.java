// $Id: PluginApp.java,v 1.1 2004/09/08 19:22:36 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.util.logging.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   A P P                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the <A HREF="../../../../man/plplugin.html"><B>plplugin</B></A>(1)
 * plugin installation tool. <P> 
 */
public
class PluginApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  PluginApp() 
  {
    super("plplugin");

    NativeFileSys.umask(022);
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
      PluginOptsParser parser = 
	new PluginOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      parser.CommandLine();

      success = true;
    }
    catch(ParseException ex) {
      handleParseException(ex);
    }
    catch(PipelineException ex) {
      Logs.plg.severe(ex.getMessage()); 
    }
    catch(Exception ex) {
      Logs.plg.severe(getFullMessage(ex));
    }
    finally {
      Logs.cleanup();
    }

    System.exit(success ? 0 : 1);
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
    Logs.ops.info(
      "USAGE:\n" +
      "  plplugin [options] --list\n" +  
      "  plplugin [options] --inspect class-file1 [class-file2 ..]\n" + 
      "  plplugin [options] [--force] --install class-file1 [class-file2 ..]\n" + 
      "\n" + 
      "  plplugin --help\n" +
      "  plplugin --html-help\n" +
      "  plplugin --version\n" + 
      "  plplugin --release-date\n" + 
      "  plplugin --copyright\n" + 
      "  plplugin --license\n" + 
      "\n" + 
      "OPTIONS:\n" +
      "  [--log=...]\n" +
      "\n" + 
      "\n" +  
      "Use \"plplugin --html-help\" to browse the full documentation.\n");
  }
}


