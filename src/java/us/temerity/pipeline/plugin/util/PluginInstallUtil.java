// $Id: PluginInstallUtil.java,v 1.4 2009/06/03 01:45:52 jlee Exp $

package us.temerity.pipeline.plugin.util;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.PluginMetadata;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   I N S T A L L   U T I L                                                  */
/*------------------------------------------------------------------------------------------*/

class PluginInstallUtil
{
  public static void
  main
  (
   String[] args
  )
  {
    if(args.length != 3)
      throw new IllegalArgumentException
	("java us.temerity.pipeline.plugin.util.PluginInstallUtil " + 
	 "(path to plugin-all file) " + 
	 "(path to plugins) " + 
	 "(path to required plugins)");

    PluginInstallUtil util = new PluginInstallUtil(args[0], args[1], args[2]);

    try {
      util.run();
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());

      throw new RuntimeException(ex.getMessage());
    }
  }

  /**
   * Constructor
   *
   * @param pluginAllPath
   *   The path to the plugin-all file that is generated during the 
   *   Makefile target plugin/plugin-all
   *
   * @param pluginsInstallPath
   *   The path to the DEST/root-install-directory where plugins are installed.
   *
   * @param requiredPluginsPath
   *   The path to where to write the required plugins GLUE file for Temerity 
   *   plugins.
   */
  public
  PluginInstallUtil
  (
   String pluginAllPath, 
   String pluginsInstallPath, 
   String requiredPluginsPath
  )
  {
    try {
      pDigest = MessageDigest.getInstance("MD5");
    }
    catch(NoSuchAlgorithmException ex) {
      throw new IllegalArgumentException("MD5 is not supported?!");
    }

    pPluginAllPath = new Path(pluginAllPath);

    if(!pPluginAllPath.toFile().exists())
      throw new IllegalArgumentException("The plugin-all file does not exist!");

    pPluginsInstallPath = new Path(pluginsInstallPath);

    if(!pPluginsInstallPath.toFile().exists())
      throw new IllegalArgumentException("The plugins install directory does not exist!");

    pRequiredPluginsPath = new Path(requiredPluginsPath);

    if(!pRequiredPluginsPath.toFile().exists())
      throw new IllegalArgumentException("The required plugins directory does not exist!");
  }

  public void
  run()
    throws PipelineException
  {
    MappedSet<PluginType,PluginID> pset = new MappedSet<PluginType,PluginID>();
    ArrayList<String> pluginList = new ArrayList<String>();

    try {
      BufferedReader in = new BufferedReader(new FileReader(pPluginAllPath.toFile()));

      while(true) {
	String line = in.readLine();
	if(line == null)
	  break;

	String cpath = line.trim();

	pluginList.add(cpath);
      }
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to read the plugin-all file (" + pPluginAllPath + ")!");
    }
    
    for(String cpath : pluginList) {
      boolean isJar = cpath.endsWith(".jar");
      String cname = cpath.substring(0, cpath.length() - (isJar ? 4 : 6)).replace('/', '.');
	
      TreeMap<String,byte[]> contents  = new TreeMap<String,byte[]>();
      TreeMap<String,Long> resources = new TreeMap<String,Long>();
      TreeMap<String,byte[]> checksums = new TreeMap<String,byte[]>();

      TreeMap<String,byte[]> resourceBytes = new TreeMap<String,byte[]>();

      File cfile = new Path(cpath).toFile();

      if(isJar) {
	try {
	  JarInputStream jarIn = new JarInputStream(new FileInputStream(cfile));

	  byte buf[] = new byte[4096];
	  while(true) {
	    JarEntry entry = jarIn.getNextJarEntry();
	    if(entry == null) 
	      break;

	    if(entry.isDirectory())
	      continue;

	    String path = entry.getName(); 

	    if(path.endsWith("class")) {
	      String jcname = path.substring(0, path.length()-6).replace('/', '.'); 
		
	      ByteArrayOutputStream out = new ByteArrayOutputStream();
		
	      while(true) {
		int len = jarIn.read(buf, 0, buf.length); 
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
		  
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		while(true) {
		  int len = jarIn.read(buf, 0, buf.length);
		  if(len == -1)
		    break;
		    
		  filesize += len;
		  digest.update(buf, 0, len);
		  out.write(buf, 0, len);
		}

		byte[] checksum = digest.digest();

		resources.put(path, filesize);
		checksums.put(path, checksum);

		resourceBytes.put(path, out.toByteArray());
	      }
	      catch(CloneNotSupportedException ex) {
		throw new PipelineException
		  ("Unable to clone the MessageDigest!");
	      }
	    }
	  }
	  
	  jarIn.close();
	}
	catch(IOException ex) {
	  ex.printStackTrace();
	  throw new PipelineException
	    ("Unable to read the plugin JAR file (" + cfile + ")!");
	}

	if(!contents.containsKey(cname)) {
	  throw new PipelineException
	    ("The plugin JAR file (" + cfile + ") did not contain the required " + 
	     "plugin class (" + cname + ")!");
	}
      }
      else {
	byte[] bytes = new byte[(int) cfile.length()];

	try {
	  FileInputStream fileIn = new FileInputStream(cfile);
	  fileIn.read(bytes);
	  fileIn.close();
	}
	catch(IOException ex) {
	  throw new PipelineException
	    ("Unable to read the plugin class file (" + cfile + ")!");
	}

	contents.put(cname, bytes);
      }

      BasePlugin plg = loadPlugin(cname, contents);

      PluginType ptype = plg.getPluginType();
      PluginID pid = plg.getPluginID();
      
      String vendor = pid.getVendor();
      String name = pid.getName();
      VersionID vid = pid.getVersionID();

      if(!vendor.equals("Temerity")) {
	throw new PipelineException
	  ("The vendor the plugin (" + cname + ") " + 
	   "is not Temerity (" + vendor + ")!");
      }

      pset.put(ptype, pid);

      Path installPath = 
	new Path(pPluginsInstallPath, (vendor + "/" + ptype + "/" + name + "/" + vid));

      writePlugin(installPath, cname, plg, contents);
      writePluginMetadata(installPath, cname, plg, resources, checksums, resourceBytes);
    }

    writeRequiredPlugins(pset);
  }

  /**
   * Load a BasePlugin given a table of bytes.
   *
   * @param cname
   *   The name of the Java class for the plugin.
   *
   * @param contents
   *   The table of Java class bytes indexed by class name.
   */
  private BasePlugin
  loadPlugin
  (
   String cname, 
   TreeMap<String,byte[]> contents 
  )
    throws PipelineException
  {
    ClassLoader loader = new PluginClassLoader(contents, null);
    
    BasePlugin plg = null;

    try {
      Class cls = loader.loadClass(cname);

      if(!BasePlugin.class.isAssignableFrom(cls)) 
	throw new PipelineException
	  ("The loaded class (" + cname + ") was not a Pipeline plugin!");

      plg = (BasePlugin) cls.newInstance();
    }
    catch(InstantiationException ex) {
      throw new PipelineException
	("Unable to intantiate plugin class (" + cname + "):\n" +
	 ex.getMessage());
    }
    catch(IllegalAccessException ex) {
      throw new PipelineException
	("Unable to access plugin class (" + cname + "):\n" +
	 ex.getMessage());
    }
    catch(LinkageError ex) {
      throw new PipelineException
	("Unable to link plugin class (" + cname + "):\n" + 
	 ex.getMessage());
    }
    catch(ClassNotFoundException ex) {
      throw new PipelineException
	("Unable to find plugin class (" + cname + "):\n" +
	 ex.getMessage());
    }
    catch(Exception ex) {
      throw new PipelineException
	("Exception thrown by constructor of plugin class (" + cname + "):\n" + 
	 ex.getMessage());
    }

    if(plg == null)
      throw new PipelineException("Unable to instantiate class (" + cname + ")!");

    return plg;
  }

  /**
   * Write the plugin in the plugins install directory.  Depending on the
   * plugin it will be installed in the normal directory or the extra directory.
   *
   * @param installPath
   *   The plugins install directory or the plugin-extra directory.
   *
   * @param cname
   *   The Java class name of the Plugin
   *
   * @param plg
   *   The main BasePlugin of the plugin.
   *
   * @param contents
   *   The table of Java class bytes indexed by Java class name.
   *
   */
  private void
  writePlugin
  (
   Path installPath, 
   String cname, 
   BasePlugin plg, 
   TreeMap<String,byte[]> contents
  )
    throws PipelineException
  {
    PluginType ptype = plg.getPluginType();
    PluginID pid = plg.getPluginID();

    String vendor = pid.getVendor();
    String name = pid.getName();
    VersionID vid = pid.getVersionID();

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info, 
       vendor + "/" + 
       ptype + "/" + 
       name + "/" + 
       vid);

    {
      File installDir = installPath.toFile();

      if(!installDir.exists())
	if(!installDir.mkdirs())
	  throw new PipelineException("Unable to mkdir (" + installDir + ")!");
    }

    if(contents.size() > 1) {
      Path jarPath = new Path(installPath, name + ".jar");

      try {
	JarOutputStream out = new JarOutputStream(new FileOutputStream(jarPath.toFile()));

	for(String key : contents.keySet()) {
	  String ename = (key.replace(".", "/") + ".class");
	  byte bs[] = contents.get(key);

	  out.putNextEntry(new ZipEntry(ename));
	  out.write(bs, 0, bs.length);
	  out.closeEntry();
	}

	out.close();
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to write the Jar file for (" + ptype + ") (" + pid + ")!");
      }
    }
    else {
      Path classPath = new Path(installPath, name + ".class");

      try {
	FileOutputStream out = new FileOutputStream(classPath.toFile());
	out.write(contents.get(cname)); 
	out.close();
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to write the class file for (" + ptype + ") (" + pid + ")!");
      }
    }
  }

  /**
   * With the plugins resources feature and the new plugin install directory structure, 
   * we need to save some data about the installed plugins.  The information is stored in a 
   * GLUE file.  Currently the contents are the Java class name, the filesizes of the resources 
   * and the checksums of the resources.
   *
   * @param installPath
   *   The plugins or plugins-extra directory.
   *
   * @param cname
   *   The Java class name of the plugin.
   *
   * @param resources
   *   The table of resource file sizes indexed by jar path.
   *
   * @param checksums
   *   The table of resource checksums indexed by jar path.
   *
   * @param resourceBytes
   *   The table of resource bytes indexed by jar path.
   *   NOTE: This assumes that the resources included in Temerity plugins 
   *   are small in file size so they can be held in memory.  End user 
   *   plugin installs with resources chunk the files to ensure that 
   *   OutOfMemoryErrors do not occur.
   *
   */
  private void
  writePluginMetadata
  (
   Path installPath, 
   String cname, 
   BasePlugin plg, 
   TreeMap<String,Long> resources, 
   TreeMap<String,byte[]> checksums, 
   TreeMap<String,byte[]> resourceBytes
  )
    throws PipelineException
  {
    PluginType ptype = plg.getPluginType();
    PluginID pid = plg.getPluginID();

    String vendor = pid.getVendor();
    String name = pid.getName();
    VersionID vid = pid.getVersionID();

    if(!resources.isEmpty()) {
      Path resourcesPath = new Path(installPath, ".resources");
      {
	File resourcesDir = resourcesPath.toFile();

	if(!resourcesDir.exists()) {
	  if(!resourcesDir.mkdirs())
	    throw new PipelineException
	      ("Unable to mkdir (" + resourcesDir + ")!");
	}
      }

      for(String path : resources.keySet()) {
	Path rpath = new Path(resourcesPath, path);
	{
	  File rdir = rpath.getParentPath().toFile();

	  if(!rdir.exists()) {
	    if(!rdir.mkdirs())
	      throw new PipelineException
		("Unable to mkdir (" + rdir + ")!");
	  }
	}

	try {
	  FileOutputStream out = new FileOutputStream(rpath.toFile());
	  out.write(resourceBytes.get(path)); 
	  out.close();
	}
	catch(IOException ex) {
	  throw new PipelineException("Unable to write file (" + rpath + ")!");
	}
      }
    }

    PluginMetadata metadata = new PluginMetadata(cname, resources, checksums);

    Path metadataPath = new Path(installPath, ".metadata");
    try {
      GlueEncoderImpl.encodeFile("PluginMetadata", metadata, metadataPath.toFile());
    }
    catch(GlueException ex) {
      throw new PipelineException(ex);
    }
  }

  /**
   * Write the required plugins GLUE file for Temerity plugins.
   *
   * @param pset
   *   The MappedSet of PluginID indexed by PluginType.
   */
  private void
  writeRequiredPlugins
  (
   MappedSet<PluginType,PluginID> pset
  )
    throws PipelineException
  {
    try {
      Path requiredPluginsGLUE = new Path(pRequiredPluginsPath, "Temerity");

      GlueEncoderImpl.encodeFile
	("RequiredPlugins", pset, requiredPluginsGLUE.toFile());
    }
    catch(GlueException ex) {
      throw new PipelineException(ex);
    }
  }

  /**
   * The MessageDigest used to generate the MD5 checksums of resources.
   */
  private MessageDigest  pDigest;

  /**
   * Path to the plugin-all file.
   */
  private Path  pPluginAllPath;

  /**
   * Path to the plugins install directory.
   */
  private Path  pPluginsInstallPath;

  /**
   * Path to save the required plugins GLUE file for Temerity plugins.
   */
  private Path  pRequiredPluginsPath;

}

