// $Id: PluginMgrControlClient.java,v 1.1 2005/01/15 02:56:32 jim Exp $
  
package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.net.*; 
import java.io.*; 
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   M G R   C O N T R O L   C L I E N T                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A control connection to the Pipeline plugin manager daemon. <P> 
 * 
 * This class handles network communication with the Pipeline plugin manager daemon 
 * <A HREF="../../../../man/plpluginmgr.html"><B>plpluginmgr</B><A>(1).  
 */
public
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
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Install a new or updated plugin class.
   * 
   * @param classdir
   *   The sole Java CLASSPATH directory used to load the class. 
   * 
   * @param classfile
   *   The plugin class file.
   * 
   * @throws PipelineException
   *   If unable to install the plugin.
   */ 
  public void
  installPlugin
  (
   File classdir, 
   File classfile
  ) 
    throws PipelineException 
  {
    if(!PackageInfo.sUser.equals(PackageInfo.sPipelineUser)) 
      throw new PipelineException
	("Only the (" + PackageInfo.sPipelineUser + ") may install plugins!");

    /* the canonical class directory */ 
    File cdir = null;
    try {
      cdir = classdir.getCanonicalFile();

      if(!cdir.isDirectory()) 
	throw new IOException();
    }
    catch(IOException ex) {
      throw new PipelineException 
	("The class directory (" + classdir + ") was not a valid directory!");
    }

    /* the canonical class file */ 
    File cfile = null;
    try {
      cfile = classfile.getCanonicalFile();

      if(!cfile.isFile()) 
	throw new IOException();
    }
    catch(IOException ex) {
      throw new PipelineException 
	("The class file (" + classfile + ") was not a valid file!");
    }

    /* the class file relative to the class directory */ 
    File rfile = null;
    {
      String fpath = cfile.getPath();
      String dpath = cdir.getPath();

      if(!fpath.startsWith(dpath)) 
	throw new PipelineException 
	  ("The class file (" + cfile + ") was not located under the class directory " + 
	   "(" + cdir + ")!");
      
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
	("The class file (" + classfile + ") was not located under a directory who's " +
	 "name designates a legal plugin revision number:\n" + ex.getMessage());
    }

    /* the class name */ 
    String cname = null;
    {
      String parts[] = cfile.getName().split("\\.");
      if((parts.length == 2) && parts[1].equals("class")) 
	cname = (pkgName + "." + parts[0]);
      else 
	throw new PipelineException 
	  ("The class file (" + classfile + ") was not a Java class file!");
    }
    
    /* load the raw bytes */ 
    byte[] bytes = new byte[(int) cfile.length()];
    try {
      FileInputStream in = new FileInputStream(cfile);
      in.read(bytes);
	in.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to read the class file (" + cfile + ")!");
    }
    
    PluginInstallReq req = new PluginInstallReq(classfile, cname, pkgID, bytes);
    
    Object obj = performTransaction(PluginRequest.Install, req);
    handleSimpleResponse(obj);    
  }
}


