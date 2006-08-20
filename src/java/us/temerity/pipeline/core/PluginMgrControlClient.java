// $Id: PluginMgrControlClient.java,v 1.5 2006/08/20 05:46:51 jim Exp $
  
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
    super();
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
   * @throws PipelineException
   *   If unable to install the plugin.
   */ 
  public void
  installPlugin
  (
   File classdir, 
   File pluginfile
  ) 
    throws PipelineException 
  {
    /* the canonical class directory */ 
    File cdir = null;
    try {
      cdir = classdir.getCanonicalFile();

      if(!cdir.isDirectory()) 
	throw new IOException();
    }
    catch(IOException ex) {
      throw new PipelineException 
	("The plugin directory (" + classdir + ") was not a valid directory!");
    }

    /* the canonical class file */ 
    File cfile = null;
    try {
      cfile = pluginfile.getCanonicalFile();

      if(!cfile.isFile()) 
	throw new IOException();
    }
    catch(IOException ex) {
      throw new PipelineException 
	("The plugin file (" + pluginfile + ") was not a valid file!");
    }

    /* the class file relative to the class directory */ 
    File rfile = null;
    {
      String fpath = cfile.getPath();
      String dpath = cdir.getPath();

      if(!fpath.startsWith(dpath)) 
	throw new PipelineException 
	  ("The plugin file (" + cfile + ") was not located under the " + 
	   "plugin directory (" + cdir + ")!");
      
      rfile = new File(fpath.substring(dpath.length()));
    }

    /* the Java package name and plugin revision number */ 
    String pkgName = null; 
    VersionID pkgID = null;
    try {
      File parent = rfile.getParentFile();
      pkgName = parent.getPath().substring(1).replace('/', '.'); 
      
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
      String parts[] = cfile.getName().split("\\.");
      if((parts.length == 2) && (parts[1].equals("class") || parts[1].equals("jar"))) {
	isJar = parts[1].equals("jar");
	cname = (pkgName + "." + parts[0]);
      }
      else {
	throw new PipelineException 
	  ("The plugin file (" + pluginfile + ") was not a Java class or JAR file!");
      }
    }
    
    /* load, instantiate and validate the plugin class or JAR file */ 
    {
      TreeMap<String,byte[]> contents = new TreeMap<String,byte[]>(); 
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
	    ("Unable to read the plugin JAR file (" + cfile + ")!");
	}
	
	if(!contents.containsKey(cname)) 
	  throw new PipelineException
	    ("The plugin JAR file (" + cfile + ") did not contain the required " + 
	     "plugin class (" + cname + ")!");
      }
      else {
	byte[] bytes = new byte[(int) cfile.length()];

	try {
	  FileInputStream in = new FileInputStream(cfile);
	  in.read(bytes);
	  in.close();
	}
	catch(IOException ex) {
	  throw new PipelineException
	    ("Unable to read the plugin class file (" + cfile + ")!");
	}

	contents.put(cname, bytes);
      }
      
      PluginInstallReq req = new PluginInstallReq(pluginfile, cname, pkgID, contents);
      
      Object obj = performTransaction(PluginRequest.Install, req);
      handleSimpleResponse(obj);    
    }
  }
}


