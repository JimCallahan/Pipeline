// $Id: PackageCommon.java,v 1.1 2008/07/22 21:36:07 jesse Exp $

package us.temerity.pipeline.toolset2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   C O M M O N                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The superclass of <CODE>PackageVersion</CODE> and <CODE>PackageMode</CODE> which provides 
 * the common fields and methods needed by both classes. <P>
 */
public abstract 
class PackageCommon
  extends Named
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  protected 
  PackageCommon() 
  {
    pEntries = new TreeMap<String,PackageEntry>();
    pOSEntries = new DoubleMap<OsType, String, PackageEntry>();
    pArchEntries = new TripleMap<OsType, ArchType, String, PackageEntry>();
  }
  
  /**
   * Internal constructor used by subclasses to create an empty package.
   * 
   * @param name 
   *   The name of the package.
   */ 
  protected
  PackageCommon
  (
   String name 
  ) 
  {
    super(name);
    pEntries = new TreeMap<String,PackageEntry>();
    pOSEntries = new DoubleMap<OsType, String, PackageEntry>();
    pArchEntries = new TripleMap<OsType, ArchType, String, PackageEntry>();
  }
  
  /** 
   * Internal copy constructor used by both <CODE>PackageMod</CODE> and 
   * <CODE>PackageVersion</CODE> when constructing instances based off an instance of 
   * the other subclass.
   */
  protected 
  PackageCommon
  (
   PackageCommon com
  ) 
  {
    super(com.getName());
    
    pEntries = new TreeMap<String,PackageEntry>(com.pEntries);
    pOSEntries = new DoubleMap<OsType, String, PackageEntry>(com.pOSEntries);
    pArchEntries = new TripleMap<OsType, ArchType, String, PackageEntry>(com.pArchEntries);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether this package has no environmental variable definitions.
   */ 
  public boolean
  isEmpty()
  {
    return pEntries.isEmpty() && pOSEntries.isEmpty() && pArchEntries.isEmpty() ;
  }
  

  /** 
   * Whether the given package has the same name as this package.
   * 
   * @param com 
   *   The package
   */
  public boolean
  similarTo
  (
   PackageCommon com
  )
  {
    if(com != null) 
      return (pName.equals(com.getName()));

    return false;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the environmental variables defined for this package, including those
   * defined at os and arch levels.
   * 
   * @param os
   *   The OS value.
   *
   * @param arch
   *   The architecture value.
   *   
   * @throws IllegalArgumentException
   *   If a <code>null</code> value is passed in for either parameter.   
   */ 
  public Set<String>
  getEnvNames
  (
    OsType os,
    ArchType arch
  ) 
  {
    if (os == null)
      throw new IllegalArgumentException
      ("Invalid call to getEnvNames().  os cannot be (null).");
    if (arch == null)
      throw new IllegalArgumentException
        ("Invalid call to getEnvNames().  arch cannot be (null).");
    
    validateArguments(os, arch);
    
    TreeSet<String> collect = new TreeSet<String>(pEntries.keySet());
    if (pOSEntries.containsKey(os)) {
      collect.addAll(pOSEntries.get(os).keySet());
      if (pArchEntries.containsKey(os, arch)) 
        collect.addAll(pArchEntries.get(os, arch).keySet());
    }
      
    return Collections.unmodifiableSet(collect);
  }
  
  /**
   * Get the value of the environmental variable with the given name.
   * 
   * @param name
   *   The name of the environment variable
   * 
   * @param os
   *   An optional OS parameter.  If this is passed in, the value will be searched for at the 
   *   OS level.  Can be <code>null</code> if no OS value is desired.
   *
   * @param arch
   *   An optional architecture type parameter.  If this is passed in the value will be 
   *   searched for at OS level, as well as the arch level.  Can be <code>null</code> if no 
   *   Arch value is desired.
   * 
   * @return 
   *   The value or <CODE>null</CODE> if the variable is undefined or has no value.
   *   
   * @throws IllegalArgumentException
   *   If a non-null arch value is passed in with a null os value.
   */ 
  public String
  getEnvValue
  (
   String name,
   OsType os,
   ArchType arch
  ) 
  {
    if (os == null && arch != null)
      throw new IllegalArgumentException
        ("Invalid call to getEnvValue().  Passing a (null) os value and a " +
         "non-null arch value is not supported behavior.");
    
    if (os != null && arch != null)
      validateArguments(os, arch);
    
    PackageEntry e = pEntries.get(name);
    if (e == null && os != null) {
      e = pOSEntries.get(os, name);
      if (e == null && arch != null) 
        e = pArchEntries.get(os, arch, name);
    }
    if(e != null)
      return e.getValue();
    return null;
  }
  
  /**
   * Get the package combine policy for the environmental variable with the given name.
   *
   * @param name
   *   The name of the environment variable
   * 
   * @param os
   *   An optional OS parameter.  If this is passed in, the policy will be searched for at the 
   *   OS level.  Can be <code>null</code> if no OS search is desired.
   *
   * @param arch
   *   An optional architecture type parameter.  If this is passed in the policy will be 
   *   searched for at OS level, as well as the arch level.  Can be <code>null</code> if no 
   *   Arch search is desired.
   *
   * @return 
   *   The policy or <CODE>null</CODE> if the variable is undefined.
   *   
   * @throws IllegalArgumentException
   *   If a non-null arch value is passed in with a null os value.
   */ 
  public MergePolicy
  getMergePolicy
  (
    String name,
    OsType os,
    ArchType arch
  ) 
  {
    if (os == null && arch != null)
      throw new IllegalArgumentException
        ("Invalid call to getMergePolicy().  Passing a (null) os value and a " +
         "non-null arch value is not supported behavior.");
    
    if (os != null && arch != null)
      validateArguments(os, arch);
    
    PackageEntry e = pEntries.get(name);
    if (e == null && os != null) {
      e = pOSEntries.get(os, name);
      if (e == null && arch != null) 
        e = pArchEntries.get(os, arch, name);
    }
    if(e != null)
      return e.getMergePolicy();
    return null;
  }
  
  /**
   * Get the {@link PackageEntry PackageEntries} defined in this package, for the specified 
   * os and arch levels.
   * 
   * @param os
   *   The OS value.
   *
   * @param arch
   *   The architecture value.
   *   
   * @throws IllegalArgumentException
   *   If a <code>null</code> value is passed in for either parameter.   
   */ 
  public ArrayList<PackageEntry>
  getEntries
  (
    OsType os,
    ArchType arch
  ) 
  {
    if (os == null)
      throw new IllegalArgumentException
      ("Invalid call to getEntries().  os cannot be (null).");
    if (arch == null)
      throw new IllegalArgumentException
        ("Invalid call to getEntries().  arch cannot be (null).");
    
    validateArguments(os, arch);
    
    ArrayList<PackageEntry> collect = new ArrayList<PackageEntry>();
    
    collect.addAll(pEntries.values());
    if (pOSEntries.containsKey(os)) {
      collect.addAll(pOSEntries.get(os).values());
      if (pArchEntries.containsKey(os, arch)) 
        collect.addAll(pArchEntries.get(os, arch).values());
    }
    
    return collect;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the package environment.
   * 
   * @param os
   *   The OS value.
   *
   * @param arch
   *   The architecture value.
   *   
   * @throws IllegalArgumentException
   *   If a <code>null</code> value is passed in for either parameter.   
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
    OsType os,
    ArchType arch
  )
  {
    
    if (os == null)
      throw new IllegalArgumentException
        ("Invalid call to getEnvironment().  os cannot be (null).");
    if (arch == null)
      throw new IllegalArgumentException
        ("Invalid call to getEnvironment().  arch cannot be (null).");
    
    validateArguments(os, arch);
    
    TreeMap<String,String> env = new TreeMap<String,String>();
    for(String name : pEntries.keySet())
      env.put(name, pEntries.get(name).getValue());
    if (pOSEntries.containsKey(os)) {
      for (String name : pOSEntries.get(os).keySet())
        env.put(name, pOSEntries.get(os, name).getValue());
      if (pArchEntries.containsKey(os, arch)) {
       for (String name : pArchEntries.get(os, arch).keySet())
         env.put(name, pArchEntries.get(os, arch, name).getValue());
      }
    }
    return env;
  }

  /**
   * Get the package environment specific to the given user.
   * 
   * @param author
   *   The user owning the generated environment.
   *   
   * @param os
   *   The OS value.
   *
   * @param arch
   *   The architecture value.
   *   
   * @throws IllegalArgumentException
   *   If a <code>null</code> value is passed in for any parameter.   
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
   String author,
   OsType os,
   ArchType arch
  )
  {
    if(author == null) 
      throw new IllegalArgumentException
      ("Invalid call to getEnvironment().  author cannot be (null).");
    if (os == null)
      throw new IllegalArgumentException
        ("Invalid call to getEnvironment().  os cannot be (null).");
    if (arch == null)
      throw new IllegalArgumentException
        ("Invalid call to getEnvironment().  arch cannot be (null).");
    
    validateArguments(os, arch);


    TreeMap<String,String> env = getEnvironment(os, arch);

    switch(os) {
    case Unix:
    case MacOS:
      {
        env.put("USER", author);

        Path home = new Path(PackageInfo.sHomePath, author);
        env.put("HOME", home.toOsString(os)); 
      }
      break;

    case Windows:
      {
        env.put("USERNAME", author);

        Path profile = PackageInfo.getUserProfilePath(author);
        if(profile != null) 
          env.put("USERPROFILE", profile.toOsString(os));

        Path appdata = PackageInfo.getAppDataPath(author);
        if(appdata != null) 
          env.put("APPDATA", appdata.toOsString(os));
      }
    }

    env.put("PIPELINE_OSTYPE", os.toString());
    env.put("PIPELINE_ARCHTYPE", arch.toString());

    return env;
  }

  /**
   * Get the package environment specific to the given user and working area.
   * 
   * @param author
   *   The user owning the generated environment.
   * 
   * @param view 
   *   The name of the user's working area view.
   *   
   * @param os
   *   The OS value.
   *
   * @param arch
   *   The architecture value.
   *   
   * @throws IllegalArgumentException
   *   If a <code>null</code> value is passed in for any parameter.
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
   String author, 
   String view,
   OsType os,
   ArchType arch
  )
  {
    if(author == null) 
      throw new IllegalArgumentException
      ("Invalid call to getEnvironment().  author cannot be (null).");
    if(view== null) 
      throw new IllegalArgumentException
      ("Invalid call to getEnvironment().  view cannot be (null).");
    if (os == null)
      throw new IllegalArgumentException
        ("Invalid call to getEnvironment().  os cannot be (null).");
    if (arch== null)
      throw new IllegalArgumentException
        ("Invalid call to getEnvironment().  arch cannot be (null).");
    
    validateArguments(os, arch);

    TreeMap<String,String> env = getEnvironment(author, os, arch);

    Path working = new Path(PackageInfo.sWorkPath, author + "/" + view);
    env.put("WORKING", working.toOsString(os));

    return env;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a list of the {@link MachineType MachinesTypes} that this Package provides
   * information for.
   * 
   * If a package has High-Level variables, it is considered to support all MachineTypes
   * underneath that. So a Windows variable will enable the Windows:x86 and the Windows:x86_64
   * MachineTypes.
   */
  public abstract TreeSet<MachineType>
  getSupportedMachineTypes();
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure that the architecture is valid for the specified operating system.
   * 
   * @param os
   *   The os type
   * 
   * @param arch
   *   The arch type
   *   
   * @throws IllegalArgumentException
   *   If the architecture is not valid with the specified operating system.
   */
  protected void
  validateArguments
  (
    OsType os,
    ArchType arch
  )
  {
    if (!os.isValidArchType(arch))
      throw new IllegalArgumentException
        ("The ArchType (" + arch.toTitle() + ") is not valid for the OsType " +
         "(" + os.toTitle() + ")");
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R I D E S                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  @Override
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof PackageCommon)) {
      PackageCommon com = (PackageCommon) obj;
      return pEntries.equals(com.pEntries) &&
             pOSEntries.equals(com.pOSEntries) &&
             pArchEntries.equals(com.pArchEntries);
    }
    return false;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    if(!pEntries.isEmpty()) 
      encoder.encode("Entries", pEntries);
    if (!pOSEntries.isEmpty())
      encoder.encode("OSEntries", pOSEntries);
    if (!pArchEntries.isEmpty())
      encoder.encode("ArchEntries", pArchEntries);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeMap<String,PackageEntry> entries = 
      (TreeMap<String,PackageEntry>) decoder.decode("Entries"); 
    if(entries != null) 
      pEntries = entries;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -6375245806244433411L;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of environmental variable entries located at the package level.
   */
  protected TreeMap<String, PackageEntry>  pEntries;
  
  /**
   * The table of environmental variable entries located at the operating system level.
   */
  protected DoubleMap<OsType, String, PackageEntry> pOSEntries;
  
  /**
   * The table of environmental variable entries located at the architecture level.
   */
  protected TripleMap<OsType, ArchType, String, PackageEntry> pArchEntries;
  
}
