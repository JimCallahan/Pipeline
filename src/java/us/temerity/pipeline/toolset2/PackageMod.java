// $Id: PackageMod.java,v 1.1 2008/07/22 21:36:07 jesse Exp $

package us.temerity.pipeline.toolset2;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   M O D                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A modifiable version of a toolset package. <P> 
 */
public 
class PackageMod
  extends PackageCommon
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public 
  PackageMod() 
  {
    super();
  }
  
  /**
   * Construct an empty modifiable package.
   * 
   * @param name 
   *   The name of the package
   */ 
  public
  PackageMod
  (
   String name
  ) 
  {
    super(name);
    pAuthor = PackageInfo.sUser;
  }
  
  /** 
   * Construct a new modifiable version based another package.
   * 
   * @param com 
   *   The package to copy.
   */ 
  public 
  PackageMod
  (
   PackageCommon com 
  ) 
  {
    super(com);
    pAuthor = PackageInfo.sUser;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Whether the given package has the same name as this package.
   * 
   * @param com 
   *   The package
   */
  @Override
  public boolean
  similarTo
  (
   PackageCommon com
  )
  {
    if((com != null) && (com instanceof PackageMod)) 
      return super.similarTo(com);

    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user who created this package.
   */
  public String
  getAuthor()
  {
    return pAuthor;
  }
  
  /**
   * Create an environmental variable entry with the given name. <P> 
   * 
   * The value of the created environmental variable will be <CODE>null</CODE> and the 
   * policy will be the default policy for the given variable name (see 
   * {@link MergePolicy#getDefaultPolicy getDefaultPolicy} for details).
   * 
   * @param name
   *   The name of the environmental variable.
   *
   * @param os
   *   The OS value for the variable or <code>null</code> if this is a package level variable.
   * 
   * @param arch
   *   The Arch value for the variable or <code>null</code> if this is an OS level variable.
   *   
   * @throws IllegalArgumentException
   *   If a non-null arch value is passed in with a null os value.
   *   
   * @throws PipelineException
   *   If an attempt is made to add a variable which is already defined at a conflicting level.
   */ 
  public void 
  createEntry
  (
   String name,
   OsType os,
   ArchType arch
  ) 
    throws PipelineException
  {
    setEntry(name, null, MergePolicy.getDefaultPolicy(name), os, arch);
  }
  
  /**
   * Set the value and policy of the environmental variable entry with the given name. <P>
   * <p>
   * If no entry exists for the given name, a new entry will be created.
   * <p>
   * Packages cannot declare the same variable more than once in any hierarchy traversal.  So 
   * if you declare "Path" as a top level variable, you cannot have an OS specific "Path" 
   * variable.  However, if you declare a Unix "Path" variable, you could still have a Windows 
   * x86 and Windows x86_64 "Path" variable, since they could never end up in the same final 
   * result.
   * 
   * @param name
   *   The name of the environmental variable.
   * 
   * @param value
   *   The value of the environmental variable.
   * 
   * @param policy
   *   The package combine policy for this entry.
   *   
   * @param os
   *   The OS value for the variable or <code>null</code> if this is a package level variable.
   *
   * @param arch
   *   The Arch value for the variable or <code>null</code> if this is an OS level variable.
   *   
   * @throws IllegalArgumentException
   *   If a non-null arch value is passed in with a null os value.
   *   
   * @throws PipelineException
   *   If an attempt is made to add a variable which is already defined at a conflicting level.
   */ 
  public void 
  setEntry
  (
   String name, 
   String value, 
   MergePolicy policy,
   OsType os,
   ArchType arch
  ) 
    throws PipelineException
  {
    if (os == null && arch != null)
      throw new IllegalArgumentException
        ("Invalid call to setEntry().  Passing a (null) os value and a " +
         "non-null arch value is not supported behavior.");
    searchForConflict(name, os, arch);
    if (os == null && arch == null) 
      pEntries.put(name, new PackageEntry(name, value, policy));
    else if (arch == null)
      pOSEntries.put(os, name, new PackageEntry(name, value, policy));
    else {
      validateArguments(os, arch);
      pArchEntries.put(os, arch, name, new PackageEntry(name, value, policy));
    }
  }
  
  /**
   * Set the value of an existing environmental variable entry with the given name.
   * 
   * @param name
   *   The name of the environmental variable.
   * 
   * @param value
   *   The value of the environmental variable.
   *   
   * @param os
   *   The OS value for the variable or <code>null</code> if this is a package level variable.
   *
   * @param arch
   *   The Arch value for the variable or <code>null</code> if this is an OS level variable.
   *   
   * @throws IllegalArgumentException
   *   If a non-null arch value is passed in with a null os value or if an attempt is made to
   *   set the value of a non-existent variable.
   */ 
  @SuppressWarnings("null")
  public void 
  setValue
  (
   String name, 
   String value,
   OsType os,
   ArchType arch
  ) 
  {
    if (os == null && arch != null)
      throw new IllegalArgumentException
        ("Invalid call to setEntry().  Passing a (null) os value and a " +
         "non-null arch value is not supported behavior.");
    
    if (os == null && arch == null) {
      PackageEntry e = pEntries.get(name);
      if(e == null) 
        throw new IllegalArgumentException
          ("No environmental variable entry exist with the name (" + name + ") at the " +
           "package level!");

      pEntries.put(name, new PackageEntry(name, value, e.getMergePolicy()));
    }
    else if (arch == null) {
      PackageEntry e = pOSEntries.get(os, name);
      if(e == null) 
        throw new IllegalArgumentException
          ("No environmental variable entry exist with the name (" + name + ") at the " +
           "(" + os.toTitle() + ") OS level!");
      pOSEntries.put(os, name, new PackageEntry(name, value, e.getMergePolicy()));
    }
    else {
      validateArguments(os, arch);
      PackageEntry e = pArchEntries.get(os, arch, name);
      if(e == null) 
        throw new IllegalArgumentException
          ("No environmental variable entry exist with the name (" + name + ") at the " +
           "(" + arch.toTitle() + ") Arch level for the (" + os.toTitle() + ") OS!");
      pArchEntries.put(os, arch, name, new PackageEntry(name, value, e.getMergePolicy()));
    }
  }

  /**
   * Set the policy of an existing environmental variable entry with the given name.
   * 
   * @param name
   *   The name of the environmental variable.
   * 
   * @param policy
   *   The new package combine policy for this entry.
   *   
   * @param os
   *   The OS value for the variable or <code>null</code> if this is a package level variable.
   *
   * @param arch
   *   The Arch value for the variable or <code>null</code> if this is an OS level variable.
   *   
   * @throws IllegalArgumentException
   *   If a non-null arch value is passed in with a null os value or if an attempt is made to
   *   set the policy of a non-existent variable.
   */ 
  @SuppressWarnings("null")
  public void 
  setMergePolicy
  (
   String name, 
   MergePolicy policy,
   OsType os,
   ArchType arch
  ) 
  {
    if (os == null && arch != null)
      throw new IllegalArgumentException
        ("Invalid call to setEntry().  Passing a (null) os value and a " +
         "non-null arch value is not supported behavior.");
    
    if (os == null && arch == null) {
      PackageEntry e = pEntries.get(name);
      if(e == null) 
        throw new IllegalArgumentException
          ("No environmental variable entry exist with the name (" + name + ") at the " +
           "package level!");

      pEntries.put(name, new PackageEntry(name, e.getValue(), policy));
    }
    else if (arch == null) {
      PackageEntry e = pOSEntries.get(os, name);
      if(e == null) 
        throw new IllegalArgumentException
          ("No environmental variable entry exist with the name (" + name + ") at the " +
           "(" + os.toTitle() + ") OS level!");
      pOSEntries.put(os, name, new PackageEntry(name, e.getValue(), policy));
    }
    else {
      validateArguments(os, arch);
      PackageEntry e = pArchEntries.get(os, arch, name);
      if(e == null) 
        throw new IllegalArgumentException
          ("No environmental variable entry exist with the name (" + name + ") at the " +
           "(" + arch.toTitle() + ") Arch level for the (" + os.toTitle() + ") OS!");
      pArchEntries.put(os, arch, name, new PackageEntry(name, e.getValue(), policy));
    }  }


  /**
   * Remove the environmental variable entry with the given name.
   * 
   * @param name
   *   The name of the environmental variable.
   *   
   * @param os
   *   The OS value for the variable or <code>null</code> if this is a package level variable.
   *
   * @param arch
   *   The Arch value for the variable or <code>null</code> if this is an OS level variable.
   *   
   * @throws IllegalArgumentException
   *   If a non-null arch value is passed in with a null os value.
   */ 
  public void 
  removeEntry
  (
   String name,
   OsType os,
   ArchType arch
  ) 
  {
    if (os == null && arch != null)
      throw new IllegalArgumentException
        ("Invalid call to setEntry().  Passing a (null) os value and a " +
         "non-null arch value is not supported behavior.");
    
    if (os == null && arch == null) 
      pEntries.remove(name);
    else if (arch == null)
      pOSEntries.remove(os, name);
    else {
      validateArguments(os, arch);
      pArchEntries.remove(os, arch, name);
    }
  }


  /**
   * Remove all of the environmental variable entries at all levels.
   */ 
  public void 
  removeAllEntries()
  {
    pEntries.clear();
    pOSEntries.clear();
    pArchEntries.clear();
  }
  
  /**
   * Remove all of the environmental variable entries at the specified level.
   * 
   * @param os
   *   The OS value for the variable or <code>null</code> if this is a package level remove.
   *
   * @param arch
   *   The Arch value for the variable or <code>null</code> if this is an OS level remove.
   *   
   * @throws IllegalArgumentException
   *   If a non-null arch value is passed in with a null os value.
   */ 
  public void 
  removeAllEntries
  (
    OsType os,
    ArchType arch
  )
  {
    if (os == null && arch != null)
      throw new IllegalArgumentException
        ("Invalid call to setEntry().  Passing a (null) os value and a " +
         "non-null arch value is not supported behavior.");
    
    if (os == null && arch == null) 
      pEntries.clear();
    else if (arch == null)
      if (pOSEntries.get(os) != null)
        pOSEntries.get(os).clear();
    else {
      validateArguments(os, arch);
      if (pArchEntries.get(os, arch) != null)
        pArchEntries.get(os, arch).clear();
    }
  }
  
 
  @Override
  public TreeSet<MachineType>
  getSupportedMachineTypes()
  {
    TreeSet<MachineType> toReturn = new TreeSet<MachineType>();
    if (!pEntries.isEmpty())
      return MachineType.getAllMachineTypes();
    for (OsType os : pOSEntries.keySet()) {
      toReturn.addAll(MachineType.getMachinesTypes(os));
    }
    for (OsType os : pArchEntries.keySet()) {
      for (ArchType arch : pArchEntries.keySet(os))
        toReturn.add(new MachineType(os, arch));
    }
    return toReturn;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Evaluate the given <B>bash</B>(1) shell script and use the results to set the 
   * environmental variable entries of the package. <P> 
   * 
   * The policies of the created variables will be set to the default policy for each 
   * variable name (see {@link MergePolicy#getDefaultPolicy getDefaultPolicy} for 
   * details).
   * 
   * @param script
   *   The <B>bash</B>(1) shell script to evaluate.
   * 
   * @param os
   *   The OS value for the variables or <code>null</code> if these are package level 
   *   variables.
   *
   * @param arch
   *   The Arch value for the variables or <code>null</code> if these are OS level variables.
   *   
   * @throws IllegalArgumentException
   *   If a non-null arch value is passed in with a null os value.
   * 
   * @throws PipelineException
   *   If unable to evaluate the shell script.
   */ 
  public void 
  loadShellScript
  (
   File script,
   OsType os,
   ArchType arch
  ) 
    throws PipelineException
  {
    if (os == null && arch != null)
      throw new IllegalArgumentException
        ("Invalid call to setEntry().  Passing a (null) os value and a " +
         "non-null arch value is not supported behavior.");
    
    if (os == null && arch == null)
      validateArguments(os, arch);

    if(PackageInfo.sOsType == OsType.Windows) 
      throw new PipelineException
        ("The load script operation is not supported on Windows.");

    File sfile = null;
    try {
      sfile = script.getCanonicalFile();
    }
    catch(IOException ex) {
      throw new PipelineException(ex);
    }

    /* evaluate the shell script and collect the output */ 
    String output[] = null;
    {
      String envbin = null;
      {
        File efile = new File("/usr/bin/env");
        if(!efile.isFile()) 
          efile = new File("/bin/env");
        if(!efile.isFile())
           throw new PipelineException
             ("Could not determine the location of the (env) program!"); 
        envbin = efile.getPath();
      }
      
      ArrayList<String> args = new ArrayList<String>();
      args.add("--noprofile");
      args.add("-c");
      args.add("if source " + sfile + " > /dev/null 2>&1; " +
               "then " + envbin + "; else exit 1; fi");
      
      TreeMap<String,String> env = new TreeMap<String,String>();

      SubProcessLight proc = 
        new SubProcessLight("EvalPackage", "/bin/bash", args, env, sfile.getParentFile());
      proc.start();
    
      try {
        proc.join();
      }
      catch(InterruptedException ex) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Sub, LogMgr.Level.Severe,
           ex.getMessage());
      }
    
      if(!proc.wasSuccessful()) 
        throw new PipelineException
          ("Unable to evaluate the shell script (" + script + "):\n\n" + 
           proc.getStdErr());

      output = proc.getStdOut().split("\\n");
    }
    
    /* parse the environmental variable output */ 
    {
      int wk;
      for(wk=0; wk<output.length; wk++) {
        int idx = output[wk].indexOf('=');
        if(idx == -1) 
          throw new PipelineException 
            ("Unable to parse line (" + wk + ") of the shell output (" + output[wk] + ")!");

        String name = output[wk].substring(0, idx);
        if(!name.equals("HOME") && 
           !name.equals("PWD") && 
           !name.equals("SHLVL") && 
           !name.equals("USER") && 
           !name.equals("WORKING") && 
           !name.equals("PIPELINE_OSTYPE") && 
           !name.equals("_")) {

          String value = null;
          if(idx < (output[wk].length()-1)) 
            value = output[wk].substring(idx+1);

          setEntry(name, value, MergePolicy.getDefaultPolicy(name), os, arch);
        }
      }
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Search for a conflict adding a new variable and throw a PipelineException if one is
   * found.
   * <p>
   * Packages cannot declare the same variable more than once in any hierarchy traversal. So
   * if you declare "Path" as a top level variable, you cannot have an OS specific "Path"
   * variable. However, if you declare a Unix "Path" variable, you could still have a Windows
   * x86 and Windows x86_64 "Path" variable, since they could never end up in the same final
   * result.
   */
  @SuppressWarnings("null")
  private void
  searchForConflict
  (
    String variableName,
    OsType os,
    ArchType arch 
  )
    throws PipelineException
  {
    /* Adding or modifying this at the package level */
    if (os == null && arch == null) {
      for (OsType os2 : pOSEntries.keySet()) {
        if (pOSEntries.get(os2).keySet().contains(variableName))
          throw new PipelineException
            ("Unable to add the variable (" + variableName + ") at the package level since " +
             "it was already found at the (" + os2.toTitle() + ") OS level.");
        if (pArchEntries.get(os2) != null) {
          for (ArchType arch2 : pArchEntries.get(os2).keySet()) {
            if (pArchEntries.get(os2, arch2).keySet().contains(variableName))
              throw new PipelineException
                ("Unable to add the variable (" + variableName + ") at the package level " +
                 "since it was already found at the (" + arch2 + ") Arch level in the " +
                "(" + os2.toTitle() + ") OS.");
          }
        }
      }
    }
    /* Adding or modifying this at the os level */
    else if (arch == null) {
     if (pEntries.keySet().contains(variableName))
       throw new PipelineException
         ("Unable to add the variable (" + variableName + ") at the (" + os.toTitle() + ") " +
       	  "OS level since it was already found at the package level.");
     if (pArchEntries.get(os) != null) {
       for (ArchType arch2 : pArchEntries.get(os).keySet()) {
         if (pArchEntries.get(os, arch2).keySet().contains(variableName))
           throw new PipelineException
             ("Unable to add the variable (" + variableName + ") at the " +
              "(" + os.toTitle() + ") OS level since it was already found at the " +
              "(" + arch2 + ") Arch level for this OS.");
       }
     }
    }
    /* Adding or modifying this at the arch level */
    else {
      if (pEntries.keySet().contains(variableName))
        throw new PipelineException
          ("Unable to add the variable (" + variableName + ") at the " +
           "(" + arch.toTitle() + ") Arch level for the (" + os.toTitle() + ") OS since it " +
           "was already found at the package level.");
      if (pOSEntries.get(os) != null) {
        if (pOSEntries.get(os).keySet().contains(variableName))
          throw new PipelineException
            ("Unable to add the variable (" + variableName + ") at the " +
             "(" + arch.toTitle() + ") Arch level for the (" + os.toTitle() + ") OS since " +
             "it was already found at the OS level.");

      }
    }
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
    
    encoder.encode("Author", pAuthor);
  } 
  
  @Override
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);
    
    String author = (String) decoder.decode("Author");
    if(author == null) 
      throw new GlueException("The \"Author\" was missing or (null)!");
    pAuthor = author;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7195678882204109780L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The user who created this mod.
   * <p>
   * Used as a unique identifier for this package mod.
   */
  private String pAuthor;
}
