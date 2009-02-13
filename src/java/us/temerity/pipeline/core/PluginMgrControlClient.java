// $Id: PluginMgrControlClient.java,v 1.9 2009/02/13 04:54:00 jlee Exp $
  
package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.net.*; 
import java.io.*; 
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.jar.*; 

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   M G R   C O N T R O L   C L I E N T                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A control connection to the Pipeline plugin manager daemon. <P> 
 * 
 * This class handles network communication with the Pipeline plugin manager daemon 
 * <A HREF="../../../../man/plpluginmgr.html"><B>plpluginmgr</B><A>(1).  
 */
class PluginMgrControlClient
  extends BasePluginMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   **/
  public
  PluginMgrControlClient() 
  {
    super("PluginMgrControlClient");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I V E   P R I V I L E G E S                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the work groups and administrative privileges from the MasterMgr.
   * 
   * @param privs
   *   The privileges. 
   * 
   * @throws PipelineException
   *   If unable to update the privileges.
   */ 
  public synchronized void 
  updateAdminPrivileges
  (
   AdminPrivileges privs
  ) 
    throws PipelineException 
  {
    verifyConnection();

    MiscUpdateAdminPrivilegesReq req = privs.getUpdateRequest();
    Object obj = performTransaction(PluginRequest.UpdateAdminPrivileges, req); 
    handleSimpleResponse(obj);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P L U G I N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Install a new or updated plugin class or JAR file.
   * 
   * @param classdir
   *   The sole Java CLASSPATH directory used to load the class. 
   * 
   * @param pluginfile
   *   The plugin class or JAR file.
   * 
   * @param external
   *   Whether to ignore the Local Vendor check.
   *
   * @param rename
   *   Whether to ignore the Java class/package aliasing check.
   * 
   * @throws PipelineException
   *   If unable to install the plugin.
   */ 
  public void
  installPlugin
  (
   File classdir, 
   File pluginfile, 
   boolean external, 
   boolean rename, 
   boolean dryRun
  ) 
    throws PipelineException 
  {
    /* the canonical class directory */ 
    Path cdir = null;
    try {
      File dir = classdir.getCanonicalFile();
      if(!dir.isDirectory()) 
	throw new IOException();

      cdir = new Path(dir);
    }
    catch(IOException ex) {
      throw new PipelineException 
	("The plugin directory (" + classdir + ") was not a valid directory!");
    }

    /* the canonical class file */ 
    Path cpath = null;
    try {
      File file = pluginfile.getCanonicalFile();
      if(!file.isFile()) 
	throw new IOException();

      cpath = new Path(file);
    }
    catch(IOException ex) {
      throw new PipelineException 
	("The plugin file (" + pluginfile + ") was not a valid file!");
    }

    /* the class file relative to the class directory */ 
    Path rpath = null;
    {
      String fpath = cpath.toString();
      String dpath = cdir.toString(); 

      if(!fpath.startsWith(dpath)) 
	throw new PipelineException 
	  ("The plugin file (" + cpath + ") was not located under the " + 
	   "plugin directory (" + cdir + ")!");
      
      rpath = new Path(fpath.substring(dpath.length()));
    }

    /* the Java package name and plugin revision number */ 
    String pkgName = null; 
    VersionID pkgID = null;
    try {
      Path parent = rpath.getParentPath();
      pkgName = parent.toString().substring(1).replace('/', '.'); 
      
      String vstr = parent.getName();
      if(!vstr.startsWith("v")) 
	throw new IllegalArgumentException
	  ("The directory (" + vstr + ") did not match the pattern (v#_#_#)!");
      pkgID = new VersionID(vstr.substring(1).replace("_", "."));
    }
    catch(IllegalArgumentException ex) {
      throw new PipelineException
	("The plugin file (" + pluginfile + ") was not located under a directory who's " +
	 "name designates a legal plugin revision number:\n" + ex.getMessage());
    }

    /* the class name */ 
    String cname = null;
    boolean isJar = false;
    {
      String parts[] = cpath.getName().split("\\.");
      if((parts.length == 2) && (parts[1].equals("class") || parts[1].equals("jar"))) {
	isJar = parts[1].equals("jar");
	cname = (pkgName + "." + parts[0]);
      }
      else {
	throw new PipelineException 
	  ("The plugin file (" + pluginfile + ") was not a Java class or JAR file!");
      }
    }
    
    /* load the Java byte-code from the supplied class or JAR file */ 
    {
      TreeMap<String,byte[]> contents = new TreeMap<String,byte[]>(); 
      File cfile = cpath.toFile(); 
      if(isJar) {
	try {
	  JarInputStream in = new JarInputStream(new FileInputStream(cfile)); 
	  
	  byte buf[] = new byte[4096];
	  while(true) {
	    JarEntry entry = in.getNextJarEntry();
	    if(entry == null) 
	      break;
	    
	    if(!entry.isDirectory()) {
	      String path = entry.getName(); 
	      if(path.endsWith("class")) {
		String jcname = path.substring(0, path.length()-6).replace('/', '.'); 
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		while(true) {
		  int len = in.read(buf, 0, buf.length); 
		  if(len == -1) 
		    break;
		  out.write(buf, 0, len);
		}
		
		contents.put(jcname, out.toByteArray());
	      }
	    }
	  }

	  in.close();
	}
	catch(IOException ex) {
	  throw new PipelineException
	    ("Unable to read the plugin JAR file (" + cpath + ")!");
	}
	
	if(!contents.containsKey(cname)) 
	  throw new PipelineException
	    ("The plugin JAR file (" + cpath + ") did not contain the required " + 
	     "plugin class (" + cname + ")!");
      }
      else {
        int size = (int) cfile.length();
	byte[] bytes = new byte[size];

	try {
	  FileInputStream in = new FileInputStream(cfile);
	  in.read(bytes);
	  in.close();
	}
	catch(IOException ex) {
	  throw new PipelineException
	    ("Unable to read the plugin class file (" + cpath + ")!");
	}

	contents.put(cname, bytes);
      }
      
      PluginInstallReq req = 
        new PluginInstallReq(pluginfile, cname, pkgID, contents, external, rename, dryRun);
      
      Object obj = performTransaction(PluginRequest.Install, req);

      /* In addition to a SuccessRsp and FailureRsp, the response can be a PluginCountRsp.  
          A PluginCountRsp is a subclass of SuccessRsp since the installation was successful, 
          but contains extra information about the state of required plugins and unregistered 
          plugins.  This is used to inform the user that there might be plugins that need to 
          be installed. */
      if(obj instanceof PluginCountRsp) {
        PluginCountRsp rsp = (PluginCountRsp)obj;

        int requiredPluginsCount = rsp.getRequiredPluginCount();
        int unknownPluginsCount  = rsp.getUnknownPluginCount();

        if(requiredPluginsCount > 0)
          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Info, 
            requiredPluginsCount + " plugin" + (requiredPluginsCount > 1 ? "s " : " ") + 
            "need" + (requiredPluginsCount > 1 ? "" : "s") + " to be installed.  " + 
            "Please rerun plplugin with the --list-required option " + 
            "to get the full details.");

        if(unknownPluginsCount > 0)
          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Info, 
            unknownPluginsCount + " unregistered plugin" + 
            (unknownPluginsCount > 1 ? "s " : " ") + 
            (unknownPluginsCount > 1 ? "have" : "has") + 
            " been detected.  They have not been loaded.  " + 
            "Install the plugin" + 
            (unknownPluginsCount > 1 ? "s " : " ") + 
            " using plplugin --install.  " + 
            "Please rerun plplugin with the --list-required option " + 
            "to get the full details.");
      }
      else if(obj instanceof SuccessRsp) {
        LogMgr.getInstance().log
         (LogMgr.Kind.Ops, LogMgr.Level.Info, 
         "All required plugins are installed "); 
      }
      else {
        handleFailure(obj);
      }
    }
  }


  /**
   * List the required plugins that need to be installed or the detection of 
   * unregistered plugins.
   */
  public void
  listRequiredPlugins
  ()
    throws PipelineException
  {
    Object obj = performTransaction(PluginRequest.ListRequired, null);

    if(obj instanceof PluginListRequiredRsp) {
      PluginListRequiredRsp rsp = (PluginListRequiredRsp)obj;

      MappedSet<PluginType,PluginID> requiredPlugins = rsp.getRequiredPlugins();
      MappedSet<PluginType,PluginID> unknownPlugins  = rsp.getUnknownPlugins();

      int requiredPluginCount = 0;
      int unknownPluginCount  = 0;

      for(PluginType plgType : requiredPlugins.keySet()) {
        for(PluginID plgID : requiredPlugins.get(plgType)) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Info, 
            "   Name : " + plgID.getName() + "\n" + 
            "Version : " + plgID.getVersionID() + "\n" + 
            " Vendor : " + plgID.getVendor() + "\n" + 
            "   Type : " + plgType + "\n" + 
            " Status : Required plugin, needs to be installed.\n");

          requiredPluginCount++;
        }
      }

      for(PluginType plgType : unknownPlugins.keySet()) {
        for(PluginID plgID : unknownPlugins.get(plgType)) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Info, 
            "   Name : " + plgID.getName() + "\n" + 
            "Version : " + plgID.getVersionID() + "\n" + 
            " Vendor : " + plgID.getVendor() + "\n" + 
            "   Type : " + plgType + "\n" + 
            " Status : Unregistered plugin, not loaded.\n");

          unknownPluginCount++;
        }
      }

      if(unknownPluginCount > 0) {
        LogMgr.getInstance().log
         (LogMgr.Kind.Ops, LogMgr.Level.Info, 
         unknownPluginCount + " unregistered plugin" + 
         (unknownPluginCount > 1 ? "s " : " ") + "detected.");
      }

      if(requiredPluginCount > 0) {
        LogMgr.getInstance().log
         (LogMgr.Kind.Ops, LogMgr.Level.Info, 
         requiredPluginCount + " plugin" + (requiredPluginCount > 1 ? "s " : " ") + 
          "need" + (requiredPluginCount > 1 ? "" : "s") + " to be installed.");
      }
      
      if(requiredPluginCount == 0 && unknownPluginCount == 0) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Info, 
          "All required plugins are installed.");
      }
    }
    else {
      handleFailure(obj);
    }

    LogMgr.getInstance().flush();
  }
}


