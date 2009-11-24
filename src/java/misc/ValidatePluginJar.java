// $Id: ValidatePluginJar.java,v 1.1 2009/11/24 07:32:03 jim Exp $

import java.net.*; 
import java.io.*; 
import java.lang.reflect.*;
import java.math.*;
import java.nio.*;
import java.nio.channels.*;
import java.security.*;
import java.util.*;
import java.util.jar.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;

/*------------------------------------------------------------------------------------------*/
/*   V A L I D A T E   P L U G I N   J A R                                                  */
/*------------------------------------------------------------------------------------------*/

public 
class ValidatePluginJar
{  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args  
  )
  {
    boolean success = false;
    try {
      if(args.length != 2) 
        usage(); 

      File prevRootDir = new File(args[0]);
      File jarFile     = new File(args[1]); 

      ValidatePluginJar app = new ValidatePluginJar(); 
      success = app.validatePlugin(prevRootDir, jarFile);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
    }
    catch(Exception ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
    }
    finally {
      LogMgr.getInstance().cleanup();
    }

    System.exit(success ? 0 : 1);
  }

  private static void 
  usage()
    throws PipelineException
  {
    throw new PipelineException
      ("usage: ValidatePluginJar prev-root-dir plugin-file\n" + 
       "\n" +
       "Checks the contents of a plugin JAR file (plugin-file) before installation\n" + 
       "against the corresponding plugin from a previous release of Pipeline located\n" + 
       "at (prev-root-dir).\n" + 
       "\n" + 
       "This utility should be run from the same working directory and given the same\n" + 
       "path to the JAR file as with plplugin(1). It will unpack and instantiate the\n" +
       "contained plugin before testing all of the class and non-class resource data\n" + 
       "files within the JAR.  The tests include checking for classes missing or added\n" + 
       "as well as checksum based tests for each non-class data resource file.\n" + 
       "\n" + 
       "If any differences are detected between the new and previously installed\n" + 
       "plugins or if there is an error while trying to validate them, this utility\n" + 
       "will exit with a non-zero exit code.  So it would be suitable to including\n" + 
       "into a build process prior to running plplugin(1) to prevent the installation\n" + 
       "of invalid plugins during an upgrade."); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new instance.
   **/
  public 
  ValidatePluginJar()
  {
    try {
      pDigest = MessageDigest.getInstance("MD5");
    }
    catch(NoSuchAlgorithmException ex) {
      throw new IllegalArgumentException("MD5 is not supported?!");
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   P L U G I N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Load a plugin from a JAR file, testing its contents against the same plugin from 
   * a previous version of Pipeline.
   * 
   * @param prevRoot
   *   The root install directory of the previous Pipeline install.
   * 
   * @param pluginJarFile
   *   The plugin JAR file.
   * 
   * @throws PipelineException
   *   If unable to validate the plugin.
   * 
   * @return 
   *   Whether the previous and new plugins contain the same classes and resource files.
   */ 
  public boolean
  validatePlugin
  (
   File prevRoot, 
   File pluginJarFile
  ) 
    throws PipelineException 
  {
    /* the canonical class directory */ 
    Path cdir = null;
    try {
      File classdir = new File("."); 
      File dir = classdir.getCanonicalFile();
      if(!dir.isDirectory()) 
	throw new IOException();

      cdir = new Path(dir);
    }
    catch(IOException ex) {
      throw new PipelineException 
	("The current directory was not a valid directory!");
    }

    /* the canonical class file */ 
    Path cpath = null;
    try {
      File file = pluginJarFile.getCanonicalFile();
      if(!file.isFile()) 
	throw new IOException();

      cpath = new Path(file);
    }
    catch(IOException ex) {
      throw new PipelineException 
	("The plugin file (" + pluginJarFile + ") was not a valid file!");
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
	("The plugin file (" + pluginJarFile + ") was not located under a directory who's " +
	 "name designates a legal plugin revision number:\n" + ex.getMessage());
    }

    /* the class name */ 
    String cname = null;
    {
      String parts[] = cpath.getName().split("\\.");
      if((parts.length == 2) && parts[1].equals("jar")) {
	cname = (pkgName + "." + parts[0]);
      }
      else {
	throw new PipelineException 
	  ("The plugin file (" + pluginJarFile + ") was not a JAR file!");
      }
    }
    
    /* take apart the plugin JAR file... */ 

    /* class bytecode indexed by Java class name */ 
    TreeMap<String,byte[]> contents = new TreeMap<String,byte[]>(); 

    /* the checksums of the non-class data indexed by the data filenames */ 
    TreeMap<String,byte[]> checksums = new TreeMap<String,byte[]>();
    {
      File cfile = cpath.toFile(); 
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
                
                checksums.put(path, checksum);
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
    

    /* instantiate the plugin */ 
    BasePlugin plg = loadPlugin(contents, cname);
    PluginID pid = plg.getPluginID();
    
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Info, 
       "-- VALIDATNG PLUGIN ------------------------------------------------\n" + 
       "        Name : " + pid.getName() + "\n" +
       "     Version : " + pid.getVersionID() + "\n" + 
       "      Vendor : " + pid.getVendor() + "\n" + 
       "  PluginType : " + plg.getPluginType() + "\n" + 
       "--------------------------------------------------------------------\n"); 
    
    /* compare what we found with the same plugin from the previous version of Pipeline */
    boolean identical = true;
    {
      File prevPluginDir = 
        new File(prevRoot, "plugins/" + pid.getVendor() + "/" + plg.getPluginType() + 
                 "/" + pid.getName() + "/" + pid.getVersionID()); 

      File prevPluginJarFile = new File(prevPluginDir, pid.getName() + ".jar");

      if(!prevPluginJarFile.isFile()) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe, 
           "This plugin does not exist in the previous version!\n" + 
           "  Location Checked: " + prevPluginJarFile); 
      }
      
      /* the names of the classes in the previously installed plugin JAR */ 
      TreeSet<String> prevClassNames = new TreeSet<String>();
      try {
        JarInputStream in = new JarInputStream(new FileInputStream(prevPluginJarFile)); 
        while(true) {
          JarEntry entry = in.getNextJarEntry();
          if(entry == null) 
            break;
          
          if(!entry.isDirectory()) {
            String path = entry.getName(); 
            if(path.endsWith("class")) {
              String jcname = path.substring(0, path.length()-6).replace('/', '.'); 
              prevClassNames.add(jcname); 
            }
            else {
              LogMgr.getInstance().logAndFlush
                (LogMgr.Kind.Ops, LogMgr.Level.Warning, 
                 "Somehow there was a non-class file (" + path + ") in the JAR file " + 
                 "(" + prevPluginJarFile + ")!"); 
            }
          }
        }
        
        in.close();
      }
      catch(IOException ex) {
        throw new PipelineException
        ("Unable to read the plugin JAR file (" + prevPluginJarFile + ")!");
      }
      
      /* compare the class files */ 
      {
        TreeSet<String> allClassNames = new TreeSet<String>();
        allClassNames.addAll(prevClassNames);
        allClassNames.addAll(contents.keySet());

        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info, 
           " RESULT           CLASS NAMES\n" + 
           "--------------   ---------------"); 
        for(String lname : allClassNames) {
          boolean inPrev = prevClassNames.contains(lname);
          boolean inNext = contents.containsKey(lname);

          String verdict = null;
          if(inNext) {
            if(inPrev) {
              verdict = "FOUND IN BOTH";
            }
            else {
              verdict = "ONLY IN NEW  "; 
              identical = false;
            }
          }
          else {
            if(inPrev) {
              verdict = "ONLY IN OLD  ";
              identical = false;
            }
            else 
              throw new IllegalStateException("Better not be possible!"); 
          }

          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Info, 
             verdict + "    " + lname); 
        }

        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info, 
           "--------------------------------------------------------------------\n"); 
      }

      /* the names and checksums of the previously installed non-class resource files */ 
      TreeMap<String,byte[]> prevChecksums = new TreeMap<String,byte[]>();
      {
        File resourceDir = new File(prevPluginDir, ".resources");
        if(resourceDir.isDirectory()) 
          findResources(prevChecksums, resourceDir, resourceDir);
      }

      /* compare resources */ 
      {
        TreeSet<String> allResourceNames = new TreeSet<String>();
        allResourceNames.addAll(prevChecksums.keySet());
        allResourceNames.addAll(checksums.keySet());

        if(allResourceNames.isEmpty()) {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Info, 
             "No Resources Found.");
        } 
        else {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Info, 
             " RESULT           RESOURCE NAMES\n" + 
             "--------------   ------------------"); 
        
          for(String rname : allResourceNames) {
            boolean inPrev = prevChecksums.containsKey(rname);
            boolean inNext = checksums.containsKey(rname);

            String verdict = null;
            if(inNext) {
              if(inPrev) {
                byte[] prev = prevChecksums.get(rname);
                byte[] next = checksums.get(rname);
                if(Arrays.equals(prev, next)) {
                  verdict = "IDENTICAL    ";
                }
                else {
                  verdict = "DIFFERENT    ";
                  identical = false;
                }
              }
              else {
                verdict = "ONLY IN NEW  ";
                identical = false;
              }
            }
            else {
              if(inPrev) {
                verdict = "ONLY IN OLD  ";
                identical = false;
              }
              else 
                throw new IllegalStateException("Better not be possible!"); 
            }
        
            LogMgr.getInstance().logAndFlush
              (LogMgr.Kind.Ops, LogMgr.Level.Info, 
               verdict + "    " + rname); 
          }
        }

        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info, 
           "--------------------------------------------------------------------"); 
      }
    }

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Info, 
       "\n" + (identical ? "Plugin Looks OK." : "SOMETHING IS WRONG!")); 

    return identical;
  }
    
  private void 
  findResources
  (
   TreeMap<String,byte[]> checksums, 
   File rootDir, 
   File current
  )
    throws PipelineException 
  {
    if(current.isDirectory()) {
      File files[] = current.listFiles(); 
      if(files != null) {
        for(File file : current.listFiles()) 
          findResources(checksums, rootDir, file);
      }
    }
    else if(current.isFile()) {
      try {
        String cstr = current.toString();
        String rname = cstr.substring(rootDir.toString().length()+1, cstr.length());
        checksums.put(rname, NativeFileSys.md5sum(new Path(current)));
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to compute the checksum for previously installed resource file " + 
           "(" + current + ")!"); 
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



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The message digest algorithm. 
   */ 
  private MessageDigest pDigest;
  
}
