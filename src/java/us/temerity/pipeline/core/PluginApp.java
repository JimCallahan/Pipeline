// $Id: PluginApp.java,v 1.11 2007/06/15 00:27:31 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
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

    try {
      NativeFileSys.umask(022);
    }
    catch(IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 getFullMessage(ex));
      System.exit(1);
    }
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
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage()); 
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 getFullMessage(ex));
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
      TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getEditors();
      if(!versions.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   tbar(80) + "\n" + 
	   "  E D I T O R S");
	
	for(String vendor : versions.keySet()) {
	  for(String name : versions.get(vendor).keySet()) {
	    for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	      BaseEditor plg = client.newEditor(name, vid, vendor);
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Info,
		 bar(80) + "\n\n" + plg + "\n");
	    }
	  }
	}
      }
    }
    
    {
      TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getActions();
      if(!versions.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   tbar(80) + "\n" + 
	   "  A C T I O N S");
	
	for(String vendor : versions.keySet()) {
	  for(String name : versions.get(vendor).keySet()) {
	    for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	      BaseAction plg = client.newAction(name, vid, vendor);
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Info,
		 bar(80) + "\n\n" + plg + "\n");
	    }
	  }
	}
      }
    }
    
    {
      TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getComparators();
      if(!versions.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   tbar(80) + "\n" + 
	   "  C O M P A R A T O R S");
	
	for(String vendor : versions.keySet()) {
	  for(String name : versions.get(vendor).keySet()) {
	    for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	      BaseComparator plg = client.newComparator(name, vid, vendor);
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Info,
		 bar(80) + "\n\n" + plg + "\n");
	    }
	  }
	}
      }
    }
    
    {
      TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getTools();
      if(!versions.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   tbar(80) + "\n" + 
	   "  T O O L S"); 
	
	for(String vendor : versions.keySet()) {
	  for(String name : versions.get(vendor).keySet()) {
	    for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	      BaseTool plg = client.newTool(name, vid, vendor);
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Info,
		 bar(80) + "\n\n" + plg + "\n");
	    }
	  }
	}
      }
    }
    
    {
      TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getAnnotations();
      if(!versions.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   tbar(80) + "\n" + 
	   "  A N N O T A T I O N S"); 
	
	for(String vendor : versions.keySet()) {
	  for(String name : versions.get(vendor).keySet()) {
	    for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	      BaseAnnotation plg = client.newAnnotation(name, vid, vendor);
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Info,
		 bar(80) + "\n\n" + plg + "\n");
	    }
	  }
	}
      }
    }
    
    {
      TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getArchivers();
      if(!versions.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   tbar(80) + "\n" + 
	   "  A R C H I V E R S");
	
	for(String vendor : versions.keySet()) {
	  for(String name : versions.get(vendor).keySet()) {
	    for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	      BaseArchiver plg = client.newArchiver(name, vid, vendor);
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Info,
		 bar(80) + "\n\n" + plg + "\n");
	    }
	  }
	}
      }
    }

    {
      TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getMasterExts();
      if(!versions.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   tbar(80) + "\n" + 
	   "  M A S T E R   E X T S");
	
	for(String vendor : versions.keySet()) {
	  for(String name : versions.get(vendor).keySet()) {
	    for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	      BaseMasterExt plg = client.newMasterExt(name, vid, vendor);
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Info,
		 bar(80) + "\n\n" + plg + "\n");
	    }
	  }
	}
      }
    }

    {
      TripleMap<String,String,VersionID,TreeSet<OsType>> versions = client.getQueueExts();
      if(!versions.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
	   tbar(80) + "\n" + 
	   "  Q U E U E   E X T S");
	
	for(String vendor : versions.keySet()) {
	  for(String name : versions.get(vendor).keySet()) {
	    for(VersionID vid : versions.get(vendor).get(name).keySet()) {
	      BaseQueueExt plg = client.newQueueExt(name, vid, vendor);
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Info,
		 bar(80) + "\n\n" + plg + "\n");
	    }
	  }
	}
      }
    }

    LogMgr.getInstance().flush();
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

    case PluginOptsParserConstants.PLUGIN_FILE:
      return "a plugin Java class or JAR file";

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


