// $Id: PluginApp.java,v 1.5 2005/01/15 02:46:46 jim Exp $

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
      Logs.ops.severe(ex.getMessage()); 
    }
    catch(Exception ex) {
      Logs.ops.severe(getFullMessage(ex));
    }
    finally {
      Logs.cleanup();
    }

    System.exit(success ? 0 : 1);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * List the installed plugins.
   */ 
  public void 
  listPlugins
  (
   PluginMgrControlClient client
  ) 
    throws PipelineException 
  {
    {
      TreeMap<String,TreeSet<VersionID>> versions = client.getEditors();
      if(!versions.isEmpty()) {
	Logs.ops.info(tbar(80) + "\n" + 
		      "  E D I T O R S");
	
	for(String name : versions.keySet()) {
	  for(VersionID vid : versions.get(name)) {
	      BaseEditor plg = client.newEditor(name, vid);
	      Logs.ops.info(bar(80) + "\n\n" + plg + "\n");
	  }
	}
      }
    }
    
    {
      TreeMap<String,TreeSet<VersionID>> versions = client.getActions();
      if(!versions.isEmpty()) {
	Logs.ops.info(tbar(80) + "\n" + 
		      "  A C T I O N S");
	
	for(String name : versions.keySet()) {
	  for(VersionID vid : versions.get(name)) {
	      BaseAction plg = client.newAction(name, vid);
	      Logs.ops.info(bar(80) + "\n\n" + plg + "\n");
	  }
	}
      }
    }
    
    {
      TreeMap<String,TreeSet<VersionID>> versions = client.getComparators();
      if(!versions.isEmpty()) {
	Logs.ops.info(tbar(80) + "\n" + 
		      "  C O M P A R A T O R S");
	
	for(String name : versions.keySet()) {
	  for(VersionID vid : versions.get(name)) {
	      BaseComparator plg = client.newComparator(name, vid);
	      Logs.ops.info(bar(80) + "\n\n" + plg + "\n");
	  }
	}
      }
    }
    
    {
      TreeMap<String,TreeSet<VersionID>> versions = client.getTools();
      if(!versions.isEmpty()) {
	Logs.ops.info(tbar(80) + "\n" + 
		      "  T O O L S"); 
	
	for(String name : versions.keySet()) {
	  for(VersionID vid : versions.get(name)) {
	      BaseTool plg = client.newTool(name, vid);
	      Logs.ops.info(bar(80) + "\n\n" + plg + "\n");
	  }
	}
      }
    }
    
    {
      TreeMap<String,TreeSet<VersionID>> versions = client.getArchivers();
      if(!versions.isEmpty()) {
	Logs.ops.info(tbar(80) + "\n" + 
		      "  A R C H I V E R S");
	
	for(String name : versions.keySet()) {
	  for(VersionID vid : versions.get(name)) {
	      BaseArchiver plg = client.newArchiver(name, vid);
	      Logs.ops.info(bar(80) + "\n\n" + plg + "\n");
	  }
	}
      }
    }

    Logs.flush();
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
      "  plplugin [options] --install class-file1 [class-file2 ..]\n" + 
      "\n" + 
      "  plplugin --help\n" +
      "  plplugin --html-help\n" +
      "  plplugin --version\n" + 
      "  plplugin --release-date\n" + 
      "  plplugin --copyright\n" + 
      "  plplugin --license\n" + 
      "\n" + 
      "OPTIONS:\n" +
      "  [--log-file=...][--log-backups=...][--log=...]\n" +
      "\n" + 
      "\n" +  
      "Use \"plplugin --html-help\" to browse the full documentation.\n");
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
    case PluginOptsParserConstants.EOF:
      return "EOF";

    case PluginOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case PluginOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case PluginOptsParserConstants.CLASS_FILE:
      return "a Java class file";

    case PluginOptsParserConstants.PATH_ARG:
      return "an file system path";

    case PluginOptsParserConstants.INTEGER:
      return "an integer";

    default: 
      if(printLiteral) 
	return PluginOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }

}


