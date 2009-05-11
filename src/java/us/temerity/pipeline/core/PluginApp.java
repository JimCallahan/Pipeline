// $Id: PluginApp.java,v 1.23 2009/05/11 17:46:56 jlee Exp $

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

	String pluginPath = vendor + "/" + ptype + "/" + name + "/" + vid;

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
		  ("There is no plugin (" + pluginPath + ") of type (" + pstat + ")");

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
   * Extract the resources for the plugin given vendor/type/name/version.  
   * Not all options need to be specified, as long as a single plugin matches.
   */
  public void
  extractPluginResources
  (
   PluginMgrControlClient client, 
   TreeSet<String>     vendorFilter, 
   TreeSet<PluginType> ptypeFilter, 
   TreeSet<String>     nameFilter, 
   TreeSet<VersionID>  versionFilter, 
   TreeSet<String> extractPaths, 
   File extractDir
  )
    throws PipelineException
  {
    Path extractPath = null;
    try {
      File dir = extractDir.getCanonicalFile();

      if(!dir.isDirectory()) {
	if(!dir.mkdirs())
	  throw new IOException();
      }

      extractPath = new Path(dir);
    }
    catch(IOException ex) {
      throw new PipelineException
	("The directory (" + extractDir + ") was not a valid directory!");
    }

    DoubleMap<PluginType,PluginID,PluginStatus> pluginStatus = 
      client.getPluginStatus();

    DoubleMap<PluginType,PluginID,PluginStatus> pluginTable = 
      new DoubleMap<PluginType,PluginID,PluginStatus>();

    int pcnt = 0;

    for(PluginType ptype : pluginStatus.keySet()) {
      if(!ptypeFilter.isEmpty() && !ptypeFilter.contains(ptype))
	continue;

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

	pluginTable.put(ptype, pid, pstat);
	pcnt++;
      }
    }

    /*
    for(PluginType ptype : pluginTable.keySet()) {
      for(PluginID pid : pluginTable.keySet(ptype)) {
	String vendor = pid.getVendor();
	String name   = pid.getName();
	VersionID vid = pid.getVersionID();

	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Info, 
	   vendor + "/" + ptype + "/" + name + "/" + vid);
      }
    }
    */

    if(pcnt == 0) {
      throw new PipelineException
	("There is no plugin that matches the options provided.  " + 
	 "Please rerun extact with more details about the plugin.");
    }
    else if(pcnt > 1) {
      throw new PipelineException
	("There are (" + pcnt + ") plugins that match the options provided.  " + 
	 "Please rerun extract with more details about the plugin.");
    }

    for(PluginType ptype : pluginTable.keySet()) {
      for(PluginID pid : pluginTable.keySet(ptype)) {
	String vendor = pid.getVendor();
	String name   = pid.getName();
	VersionID vid = pid.getVersionID();

	String pluginPath = vendor + "/" + ptype + "/" + name + "/" + vid;

	PluginStatus pstat = pluginTable.get(ptype, pid);

	switch(pstat) {
	  case Installed:
	  case UnderDevelopment:
	  case Permanent:
	    {
	      BasePlugin plugin = newPlugin(client, ptype, pid);

	      if(plugin == null)
		throw new PipelineException
		  ("There is no plugin (" + pluginPath + ") of type (" + pstat + ")");

	      SortedMap<String,Long> resources = plugin.getResources();

	      if(resources == null || resources.isEmpty()) {
		throw new PipelineException
		  ("There are no resources for plugin (" + pluginPath + ")");
	      }
	      
	      TreeSet<String> rset = new TreeSet<String>();

	      if(extractPaths.isEmpty()) {
		for(String path : resources.keySet()) {
		  rset.add(path);
		}
	      }
	      else {
		TreeSet<String> extractPathsCopy = new TreeSet<String>(extractPaths);

		for(String path : extractPaths) {
		  if(resources.containsKey(path)) {
		    rset.add(path);

		    extractPathsCopy.remove(path);
		  }
		}

		if(!extractPathsCopy.isEmpty()) {
		  for(String expath : extractPathsCopy) {
		    for(String path : resources.keySet()) {
		      if(path.startsWith(expath)) {
			rset.add(path);
		      }
		    }
		  }
		}
	      }

	      for(String path : rset) {
		long filesize = resources.get(path);
		Path rpath = new Path(extractPath, path);

		try {
		  writeResource(plugin, path, filesize, rpath);
		}
		catch(PipelineException ex) {
		  throw ex;
		}
	      }
	    }
	    break;
	  default:
	    {
	      throw new PipelineException
		("Plugin (" + pluginPath + ") is not installed!");
	    }
	}
      }
    }
  }

  /**
   * Write the resource to the specified directory.
   */
  private void
  writeResource
  (
   BasePlugin plugin, 
   String path, 
   long filesize, 
   Path rpath
  )
    throws PipelineException
  {
    File rfile = rpath.toFile();
    File parent = rfile.getParentFile();

    if(!parent.exists()) {
      if(!parent.mkdirs()) {
	throw new PipelineException("Unable to mkdirs (" + parent + ")!");
      }
    }

    URL url = plugin.getResource("/" + path);

    long bytesRead = 0L;
    
    try {
      InputStream in = url.openStream();
      FileOutputStream out = new FileOutputStream(rfile);

      byte[] buf = new byte[4096];

      while(true) {
	int len = in.read(buf, 0, buf.length);
	if(len == -1)
	  break;
	bytesRead += len;
	out.write(buf, 0, len);
      }

      out.close();
      in.close();
    }
    catch(IOException ex) {
      throw new PipelineException(ex);
    }

    if(bytesRead != filesize) {
      if(rfile.exists()) {
	rfile.delete();
      }

      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info, 
	 "Error with resource (" + path + ")!");
    }
    else {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Info, 
	 "Wrote resource (" + path + ").");
    }
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
       "  plplugin [options] list [plugin filtering options]\n" + 
       "  plplugin [options] contents [plugin filtering options]\n" + 
       "  plplugin [options] summary\n" + 
       "  plplugin [options] install " + 
       "[--external] [--rename] [--dry-run] plugin-file1 [plugin-file2 ...]\n" + 
       "  plplugin [options] extract " + 
       "[--type=] [--vendor=] [--name=] [--version=] [--dir=] [resource-path ...]\n" +
       "\n" + 
       "  plplugin --help\n" +
       "  plplugin --html-help\n" +
       "  plplugin --version\n" + 
       "  plplugin --release-date\n" + 
       "  plplugin --copyright\n" + 
       "  plplugin --license\n" + 
       "\n" + 
       "PLUGIN FILTERING OPTIONS:\n" + 
       "  [--status=...] [--type=...] [--vendor=...] [--name=...] [--version=...]\n" + 
       "\n" + 
       "OPTIONS:\n" +
       "  [--log-file=...] [--log-backups=...] [--log=...]\n" +
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


