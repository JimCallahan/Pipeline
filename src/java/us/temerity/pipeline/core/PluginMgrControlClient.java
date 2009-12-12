// $Id: PluginMgrControlClient.java,v 1.20 2009/12/12 23:12:50 jim Exp $
  
package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.net.*; 
import java.io.*; 
import java.lang.reflect.*;
import java.math.*;
import java.nio.*;
import java.nio.channels.*;
import java.security.*;
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
    this(false);
  }

  /** 
   * Construct the sole instance.
   * 
   * @param forceLongTransactions
   *   Whether to treat all uses of {@link performTransaction} like 
   *   {@link performLongTransaction} with an infinite request timeout and a 60-second 
   *   response retry interval with infinite retries.
   **/
  public
  PluginMgrControlClient
  ( 
   boolean forceLongTransactions 
  ) 
  {
    super(forceLongTransactions, "PluginMgrControl");

    try {
      pDigest = MessageDigest.getInstance("MD5");
    }
    catch(NoSuchAlgorithmException ex) {
      throw new IllegalArgumentException("MD5 is not supported?!");
    }
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
  /*   A D M I N I S T R A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a set of database backup files. <P> 
   * 
   * The backup will not be perfomed until any currently running database operations have 
   * completed.  Once the databsae backup has begun, all new database operations will blocked
   * until the backup is complete.  The this reason, the backup should be performed during 
   * non-peak hours. <P> 
   * 
   * The database backup files will be automatically named: <P> 
   * <DIV style="margin-left: 40px;">
   *   plpluginmgr-db.<I>YYMMDD</I>.<I>HHMMSS</I>.tgz<P>
   * </DIV>
   * 
   * Where <I>YYMMDD</I>.<I>HHMMSS</I> is the year, month, day, hour, minute and second of 
   * the backup.  The backup file is a <B>gzip</B>(1) compressed <B>tar</B>(1) archive of
   * the {@link Glueable GLUE} format files which make of the persistent storage of the
   * Pipeline database. <P> 
   * 
   * Only privileged users may create a database backup. <P> 
   * 
   * @param dir
   *   The full path to the directory to store backup files.  This path is will be 
   *   interpreted as local to the machine running the plpluginmgr daemon.
   * 
   * @param dateString
   *   The time of the backup encoded as a string.
   * 
   * @throws PipelineException 
   *   If unable to perform the backup.
   */ 
  public synchronized void
  backupDatabase
  (
   Path dir, 
   String dateString
  ) 
    throws PipelineException
  {
    verifyConnection();
    
    PluginBackupDatabaseReq req = new PluginBackupDatabaseReq(dir, dateString); 

    Object obj = performLongTransaction(PluginRequest.BackupDatabase, req, 15000, 60000);  
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

    {
      long filesize = pluginfile.length();

      if(filesize > sMaxFileSize) {
	throw new PipelineException
	  ("The plugin (" + pluginfile + ") " + 
	   "file size greater than the max file size " + 
	   "(" + filesize + " > " + sMaxFileSize + ")!");
      }
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
      TreeMap<String,Long> resources = new TreeMap<String,Long>();
      TreeMap<String,byte[]> checksums = new TreeMap<String,byte[]>();
      TreeMap<String,Long> chunkTable = new TreeMap<String,Long>();

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

	      LogMgr.getInstance().log
		(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
		 "Path (" + path + ")");

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
	      else {
		try {
		  MessageDigest digest = (MessageDigest) pDigest.clone();
		  
		  long filesize = 0L;

		  while(true) {
		    int len = in.read(buf, 0, buf.length);
		    if(len == -1)
		      break;
		    filesize += len;
		    digest.update(buf, 0, len);
		  }

		  byte[] checksum = digest.digest();

		  resources.put(path, filesize);
		  checksums.put(path, checksum);

		  chunkTable.put(path, 0L);

		  BigInteger number = new BigInteger(1, checksum);

		  LogMgr.getInstance().log
		    (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
		     "Resource (" + path + ") file size (" + filesize + ") " + 
		     "checksum (" + number.toString(16) + ")");
		}
		catch(CloneNotSupportedException ex) {
		  throw new PipelineException
		    ("Unable to clone the MessageDigest!");
		}
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

      /* Load the plugin after checking for the serialVersionUID field.  The plugin 
         is used get the PluginID.  The PluginID is used to request the checksum from 
	 the PluginMgrServer. */
      BasePlugin plg = loadPlugin(contents, cname);

      TreeMap<String,byte[]> installedChecksums = new TreeMap<String,byte[]>();
      {
	PluginChecksumReq req 
	  = new PluginChecksumReq(plg.getPluginType(), plg.getPluginID());

	Object obj = performTransaction(PluginRequest.Checksum, req);

	if(obj instanceof PluginChecksumRsp) {
	  PluginChecksumRsp rsp = (PluginChecksumRsp) obj;

	  SortedMap<String,byte[]> checksumsFromServer = rsp.getChecksums();

	  if(checksumsFromServer != null)
	    installedChecksums.putAll(rsp.getChecksums());
	}
	else {
	  /* A failure rsp was received, proceed as if an empty checksum table 
	     was received and install all resources. */
	}
      }

      /* If there are no resources in the jar file and no resources on the server, 
         then we can safely do a normal plugin install.  No resources need to be 
	 installed and no resources on the server side need to be removed. */
      if(checksums.isEmpty() && installedChecksums.isEmpty()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	   "Installing the plugin (" + cname + ") normally since " + 
	   "it did not have resources and is not installing resources.");

	PluginInstallReq req = 
	  new PluginInstallReq(pluginfile, cname, pkgID, 
	                       contents, 
			       external, rename, dryRun);

	Object obj = performTransaction(PluginRequest.Install, req);

	if(obj instanceof PluginCountRsp) {
	  displayPluginCountRsp(obj);
	}
	else if(obj instanceof SuccessRsp) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Info, 
             "All required plugins are installed ");
	}
	else {
	  handleFailure(obj);
	}

	return;
      }

      if(!installedChecksums.isEmpty()) {
	for(String path : installedChecksums.keySet()) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	     "Installed resource (" + path + ")");

	  if(checksums.containsKey(path)) {
	    byte[] checksum1 = installedChecksums.get(path);
	    byte[] checksum2 = checksums.get(path);

	    if(Arrays.equals(checksum1, checksum2)) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
		 "The resource (" + path + ") does not need to be updated.");

	      resources.remove(path);
	    }
	  }
	}
      }

      LogMgr.getInstance().log
	(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	 "Performing a resource install of the plugin (" + cname + ").");

      PluginResourceInstallReq req = 
	new PluginResourceInstallReq(pluginfile, cname, pkgID, 
                                     contents, 
                                     resources, checksums, 
                                     external, rename, dryRun);

      Object obj = performTransaction(PluginRequest.ResourceInstall, req);

      if(obj instanceof PluginResourceInstallRsp) {
	/* The PluginMgrServer has sent back a resource install session ID.  
	   Start the sending of resource chunks. */
	PluginResourceInstallRsp rsp = (PluginResourceInstallRsp) obj;

	long sessionID = rsp.getSessionID();

	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	   "Session ID (" + sessionID + ")");

	installResourceChunks(sessionID, cpath, resources, checksums);
      }
      else if(obj instanceof PluginCountRsp) {
	displayPluginCountRsp(obj);
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

  private BasePlugin
  loadPlugin
  (
   TreeMap<String,byte[]> contents, 
   String cname
  )
    throws PipelineException
  {
    ClassLoader loader = new PluginClassLoader(contents, null);

    Class cls = null;
    BasePlugin plg = null;

    try {
      cls = loader.loadClass(cname);

      if(!BasePlugin.class.isAssignableFrom(cls)) 
	throw new PipelineException
	  ("The loaded class (" + cname + ") was not a Pipeline plugin!");

      /* Check the plugin defines the serialVersionUID that Serializable recommends.  
         Using Java reflection we can access private field by using getDeclaredField from 
	 a class object.  If the field is missing then a NosuchFieldException is thrown.  
	 Through my testing I was able to find one plugin that I failed to use serialver 
	 to obtain the serialVersionUID, however the class was still loaded because 
	 ObjectStreamClass provides a serialVersionUID for classes that fail to declare 
         one. This code is also in PluginMgr, but having it on the client side makes it 
         more efficient. */
      Field serialVersionUID = cls.getDeclaredField("serialVersionUID");
	
      plg = (BasePlugin) cls.newInstance();
    }
    catch(ClassNotFoundException ex) {
      throw new PipelineException
	("Unable to find plugin class (" + cname + "):\n" +
	 ex.getMessage());
    }
    catch(LinkageError ex) {
      throw new PipelineException
	("Unable to link plugin class (" + cname + "):\n" + 
	 ex.getMessage());
    }
    catch(NoSuchFieldException ex) {
      throw new PipelineException
	("The plugin class (" + cname + ") does not define a serialVersionUID field!  " + 
	 "Please run serialver to obtain a serialVersionUID.");
    }
    catch(InstantiationException ex) {
      throw new PipelineException
	("Unable to intantiate plugin class (" + cls.getName() + "):\n" +
	 ex.getMessage());
    }
    catch(IllegalAccessException ex) {
      throw new PipelineException
	("Unable to access plugin class (" + cls.getName() + "):\n" +
	 ex.getMessage());
    }
    catch(Exception ex) {
      throw new PipelineException
	("Exception thrown by constructor of plugin class (" + cls.getName() + "):\n" + 
	 ex.getMessage());
    }

    if(plg == null)
      throw new PipelineException
	("The plugin (" + cname + ") is null!");

    return plg;
  }

  private void
  installResourceChunks
  (
   long sessionID, 
   Path cpath, 
   TreeMap<String,Long> resources, 
   TreeMap<String,byte[]> checksums
  )
    throws PipelineException
  {
    try {
      File cfile = cpath.toFile();

      JarInputStream in = new JarInputStream(new FileInputStream(cfile)); 

      int numBytes;

      byte[] buf   = new byte[4096];
      byte[] bytes = new byte[4096*2 + sChunkSize];

      while(true) {
	JarEntry entry = in.getNextJarEntry();
	if(entry == null) 
	  break;

	if(entry.isDirectory())
	  continue;
	
	String path = entry.getName();

	if(path.endsWith("class"))
	  continue;

	if(!resources.containsKey(path))
	  continue;

	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Info, 
	   "Path (" + path + ")");

	long filesize  = resources.get(path);
	long bytesRead = 0L;

	numBytes = 0;

	while(true) {
	  int len = in.read(buf, 0, buf.length);
	  if(len == -1)
	    break;

	  bytesRead += len;
	  System.arraycopy(buf, 0, bytes, numBytes, len);
	  numBytes += len;

	  if((numBytes > sChunkSize) || (bytesRead == filesize)) {
	    PluginResourceChunkInstallReq req = 
	      new PluginResourceChunkInstallReq
		(sessionID, path, bytes, numBytes, bytesRead - numBytes);

	    Object obj = performTransaction
	      (PluginRequest.ResourceChunkInstall, req);
	    
	    int percentTransferred = (int)(bytesRead / (double)filesize * 100);

	    LogMgr.getInstance().log
	      (LogMgr.Kind.Plg, LogMgr.Level.Info, 
	       percentTransferred + "% (" + path + ")");

	    if(obj instanceof PluginCountRsp) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
		 "Recevied a PluginCountRsp.");

	      displayPluginCountRsp(obj);
	    }
	    else if(obj instanceof SuccessRsp) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Ops, LogMgr.Level.Info, 
                 "All required plugins are installed ");
	    }
	    else if(obj instanceof FailureRsp) {
	      handleFailure(obj);
	    }

	    numBytes = 0;
	  }
	}
      }

      in.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to read the plugin JAR file (" + cpath + ")!");
    }
  }

  private void
  displayPluginCountRsp
  (
   Object obj
  )
  {
    PluginCountRsp rsp = (PluginCountRsp)obj;

    int requiredPluginsCount = rsp.getRequiredPluginCount();
    int unknownPluginsCount  = rsp.getUnknownPluginCount();

    if(requiredPluginsCount > 0) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Warning, 
         requiredPluginsCount + " plugin" + (requiredPluginsCount > 1 ? "s " : " ") + 
         "still need" + (requiredPluginsCount > 1 ? "" : "s") + " to be installed.  " + 
         "Please rerun plplugin with the \"list --status=miss option\" " + 
          "to get the full details.");
    }

    if(unknownPluginsCount > 0) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Warning, 
         unknownPluginsCount + " unregistered plugin" + 
         (unknownPluginsCount > 1 ? "s " : " ") + 
         (unknownPluginsCount > 1 ? "have " : "has ") + 
         "been detected.  They have not been loaded.  " + 
         "Install the plugin" + 
	 (unknownPluginsCount > 1 ? "s " : " ") + 
         "using plplugin \"install\".  " + 
         "Please rerun plplugin with the \"list --status=unknown option\" " + 
         "to get the full details.");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The message digest algorithm. 
   */ 
  private MessageDigest pDigest;

  /**
   * The resource file chunk size, currently set at 512k.
   */
  private final static int  sChunkSize = 1024*512;

  /**
   * The maximum Jar file allowed.  Currently set at 128mb.
   */
  private final static long  sMaxFileSize = 1024 * 1024 * 128;

}

