// $Id: PluginApp.java,v 1.22 2009/03/26 06:48:37 jlee Exp $

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
   * Display a count summary of all plugins.
   */
  public void
  pluginSummary
  (
    PluginMgrControlClient client
  )
  {
    DoubleMap<PluginType,PluginID,PluginStatus> pluginStatus = 
      client.getPluginStatus();

    int maxLength = 0;
    for(String plgType : PluginType.titles())
      maxLength = Math.max(plgType.length()+1, maxLength);

    String totalMsg = "TOTAL";

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Info,
       tbar(80) + "\n" + 
       title("PluginSummary"));

    TreeSet<String> vset = new TreeSet<String>();
    for(PluginType ptype : pluginStatus.keySet())
      for(PluginID pid : pluginStatus.keySet(ptype))
	vset.add(pid.getVendor());

    for(String vendor : vset) {

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info,
         bar(80) + "\n" + 
         " " + vendor + " Plugins\n" + 
         pad(maxLength) + "   required  loaded  unknown\n" +
         pad(maxLength) + "   -------------------------");

      int totalM = 0;
      int totalL = 0;
      int totalU = 0;
      for(PluginType ptype : PluginType.all()) {
	TreeMap<PluginID,PluginStatus> plugins = pluginStatus.get(ptype);

	if(plugins == null)
	  continue;

        int mcnt = 0;
	int lcnt = 0;
	int ucnt = 0;

	for(PluginID pid : plugins.keySet()) {
	  if(!pid.getVendor().equals(vendor))
	    continue;
	  
	  PluginStatus pstat = plugins.get(pid);

	  switch(pstat) {
	    case Missing:
	      {
		mcnt++;
	      }
	      break;
	    case Installed:
	    case UnderDevelopment:
	    case Permanent:
	      {
		lcnt++;
	      }
	      break;
	    case Unknown:
	      {
		ucnt++;
	      }
	      break;
	  }
	}

        if((mcnt > 0) || (lcnt > 0) || (ucnt > 0)) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Plg, LogMgr.Level.Info,
             lpad(ptype.toString(), maxLength) + " :  " + 
             lpad(Integer.toString(lcnt+mcnt), 5) + 
             lpad(Integer.toString(lcnt), 8) + 
             lpad(Integer.toString(ucnt), 9));
        }
          
        totalM += mcnt;
        totalL += lcnt;
        totalU += ucnt;
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info,
         "\n" + lpad("TOTAL", maxLength) + " :  " + 
         lpad(Integer.toString(totalM+totalL), 5) + 
         lpad(Integer.toString(totalL), 8) + 
         lpad(Integer.toString(totalU), 9) + "\n");

      if(totalM > 0) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Plg, LogMgr.Level.Info,
           pad(maxLength) + "   (" + (totalM) + " REQUIRED PLUGIN" + 
           (((totalM) > 1) ? "S" : "") + " MISSING!)\n"); 
      }
      else {
        LogMgr.getInstance().log
          (LogMgr.Kind.Plg, LogMgr.Level.Info,
           pad(maxLength) + "   (all plugins found)\n"); 
      }
    }

    LogMgr.getInstance().flush();
  }

  /**
   * List the installed plugins with optional: 
   *   plugin status, 
   *   plugin type, 
   *   plugin vendor, 
   *   plugin name, 
   *   plugin version id filtering.
   */ 
  public void 
  listPlugins
  (
   PluginMgrControlClient client, 
   TreeSet<PluginStatus>  pstatFilter, 
   TreeSet<PluginType>    ptypeFilter, 
   TreeSet<String>        vendorFilter, 
   TreeSet<String>        nameFilter, 
   TreeSet<VersionID>     versionFilter, 
   boolean                displayContents
  ) 
    throws PipelineException 
  {
    DoubleMap<PluginType,PluginID,PluginStatus> pluginStatus = 
      client.getPluginStatus();

    for(PluginType ptype : PluginType.all()) {
      if(!pluginStatus.containsKey(ptype))
	continue;

      if(pluginStatus.get(ptype).isEmpty())
	continue;

      if(!ptypeFilter.isEmpty() && !ptypeFilter.contains(ptype))
	continue;

      boolean displayPluginTypeBanner = true;

      for(PluginID pid : pluginStatus.keySet(ptype)) {
	String vendor = pid.getVendor();
	String name   = pid.getName();
	VersionID vid = pid.getVersionID();

	if(!vendorFilter.isEmpty() && !vendorFilter.contains(vendor))
	  continue;

	if(!nameFilter.isEmpty() && !nameFilter.contains(name))
	  continue;

	if(!versionFilter.isEmpty() && !versionFilter.contains(vid))
	  continue;

	PluginStatus pstat = pluginStatus.get(ptype, pid);

	if(!pstatFilter.isEmpty())
	  switch(pstat) {
	    case UnderDevelopment:
	      {
		if(!pstatFilter.contains(PluginStatus.UnderDevelopment) && 
		   !pstatFilter.contains(PluginStatus.Installed) && 
		   !pstatFilter.contains(PluginStatus.Required))
		  continue;
	      }
	      break;
	    case Permanent:
	      {
		if(!pstatFilter.contains(PluginStatus.Permanent) && 
		   !pstatFilter.contains(PluginStatus.Installed) && 
		   !pstatFilter.contains(PluginStatus.Required))
		  continue;
	      }
	      break;
	    case Missing:
	      {
		if(!pstatFilter.contains(PluginStatus.Missing) && 
		   !pstatFilter.contains(PluginStatus.Required))
		  continue;
	      }
	      break;
	    case Unknown:
	      {
		if(!pstatFilter.contains(PluginStatus.Unknown))
		  continue;
	      }
	      break;
	  }

	if(displayPluginTypeBanner) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Info,
	     tbar(80) + "\n" + 
	     title(ptype.toString()));

	  displayPluginTypeBanner = false;
	}

	StringBuilder buf = new StringBuilder();

	buf.append("Name        : " + name + "\n");
        buf.append("Version     : " + vid + "\n");
        buf.append("Vendor      : " + vendor + "\n");

	switch(pstat) {
	  case Installed:
	  case UnderDevelopment:
	  case Permanent:
	    {
	      BasePlugin plugin = newPlugin(client, ptype, pid);

	      if(plugin == null)
		throw new PipelineException
		  ("There is no plugin () of type ()");

	      buf.append("Supports    :");

	      for(OsType os : plugin.getSupports()) 
		buf.append(" " + os.toTitle());
	      buf.append("\n");

	      buf.append("Description : " + 
	                 wordWrap(plugin.getDescription(), 14, 80) + "\n");
	      buf.append("PluginType  : " + ptype + "\n");
	      buf.append("Status      : " + pstat + "\n");
	      buf.append("Class       : " + plugin.getClass().getName());

	      if(displayContents) {
		SortedMap<String,Long> resources = plugin.getResources();

		LogMgr.getInstance().log
		  (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
		   "Resources (" + resources + ")");

		if(resources != null && !resources.isEmpty()) {
		  buf.append("\n");
		  buf.append("Resources   :\n");

		  for(String path : resources.keySet()) {
		    long filesize = resources.get(path);

		    buf.append("\n");
		    buf.append(" ");
		    buf.append(path);
		    buf.append(" (");
		    buf.append(filesize);
		    buf.append(")");

		    /* The commented out section is probably too verbose, but it
		       served as a good test that resources worked.  For this case 
		       the argument to getResource was the absolute path to the 
		       resource not the relative path that most users would use. */
		    /*
		    URL resourceURL = plugin.getResource("/" + path);
		    long resourceFilesize = plugin.getResourceSize("/" + path);

		    buf.append("\n ");
		    buf.append(resourceURL);
		    buf.append(" (");
		    buf.append(resourceFilesize);
		    buf.append(")");

		    try {
		      buf.append("file exists (");
		      
		      URI resourceURI = resourceURL.toURI();
		      File resourceFile = new File(resourceURI);
		      
		      buf.append(resourceFile.exists());
		      buf.append(")");
		    }
		    catch(URISyntaxException ex) {
		      LogMgr.getInstance().log
			(LogMgr.Kind.Ops, LogMgr.Level.Warning, 
			 "Unable to instantiate a File object from " + 
			 "resource URL (" + resourceURL + ")!");
		    }
		    */
		  }
		}
	      }
	    }
	    break;
	  case Missing:
	    {
	      buf.append("PluginType  : " + ptype + "\n");
	      buf.append("Status      : Missing\n");
	    }
	    break;
	  case Unknown:
	    {
	      buf.append("PluginType  : " + ptype + "\n");
	      buf.append("Status      : Unknown\n");
	    }
	    break;
	}

	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Info,
           bar(80) + "\n\n" + buf.toString() + "\n");
      }
    }

    LogMgr.getInstance().flush();
  }

  /**
   * Retrieve a plugin based on type and plugin id.
   */
  private BasePlugin
  newPlugin
  (
   PluginMgrControlClient client, 
   PluginType ptype, 
   PluginID pid
  )
    throws PipelineException
  {
    String name = pid.getName();
    VersionID vid = pid.getVersionID();
    String vendor = pid.getVendor();

    switch(ptype) {
      case Editor:
        return client.newEditor(name, vid, vendor);

      case Action:
        return client.newAction(name, vid, vendor);

      case Comparator:
        return client.newComparator(name, vid, vendor);

      case Tool:
        return client.newTool(name, vid, vendor);

      case Annotation:
        return client.newAnnotation(name, vid, vendor);

      case Archiver:
        return client.newArchiver(name, vid, vendor);

      case MasterExt:
        return client.newMasterExt(name, vid, vendor);

      case QueueExt:
        return client.newQueueExt(name, vid, vendor);

      case KeyChooser:
        return client.newKeyChooser(name, vid, vendor);

      case BuilderCollection:
        return client.newBuilderCollection(name, vid, vendor);

      default:
	return null;
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
       "  plplugin [options] --list [--status=...] [--type=...] " + 
       "[--vendor=...] [--name=...] [--vsn=...] [--summary]\n" +  
       "  plplugin [options] --contents [--status=...] [--type=...] " + 
       "[--vendor=...] [--name=...] [--vsn=...] [--summary]\n" + 
       "  plplugin [options] --summary\n" + 
       "  plplugin [options] --install class-file1 [class-file2 ...]\n" + 
       "\n" + 
       "  plplugin --help\n" +
       "  plplugin --html-help\n" +
       "  plplugin --version\n" + 
       "  plplugin --release-date\n" + 
       "  plplugin --copyright\n" + 
       "  plplugin --license\n" + 
       "\n" + 
       "OPTIONS:\n" +
       "  [--log-file=...] [--log-backups=...] [--log=...]\n" +
       "  [--external] [--rename] [--dry-run]\n" + 
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


