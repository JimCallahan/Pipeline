// $Id: ToolsetMod.java,v 1.1 2008/07/22 21:36:07 jesse Exp $

package us.temerity.pipeline.toolset2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;


/**
 * The modifiable version of a toolset.
 */
public 
class ToolsetMod
  extends ToolsetCommon
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
  ToolsetMod()
  {
    super();
    pGenerated = false;
  }
  
  /**
   * Create an empty toolset.
   * 
   * @param name
   *   The name of the toolset.
   *   
   * @param machineTypes
   *   The list of machine types that this toolset should support or <code>null</code> if the
   *   toolset should support all possible machine types. 
   */ 
  protected
  ToolsetMod
  (
   String name,
   TreeSet<MachineType> machineTypes
  )
  {
    super(name, machineTypes);
    
    pAuthor = PackageInfo.sUser;
    
    pGenerated = false;
  }
  
  /**
   * Create a toolset composed of the  given packages.
   * 
   * @param name
   *   The name of the toolset.
   * 
   * @param packages
   *   The list of the packages and their versions in order of evaluation.
   *   
   * @param machineTypes
   *   The list of machine types that this toolset should support or <code>null</code> if the
   *   toolset should support all possible machine types. 
   */ 
  protected
  ToolsetMod
  (
   String name, 
   ListMap<String, String> packages,
   TreeSet<MachineType> machineTypes
  ) 
  {
    super(name, packages, machineTypes);
    
    pAuthor = PackageInfo.sUser;
    
    pGenerated = false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G E N E R A T E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate the ToolsetMod from the included list of packages.
   * <p>
   * The list of packages must exactly match the the list of packages that the toolset is
   * defined as containing. 
   * 
   * @param packages
   *   The list of packages to use to generate this toolset.
   *    
   * @throws PipelineException
   *   If the list of packages passed in is not the same as the list of packages that the 
   *   toolset is defined as containing.
   */
  @SuppressWarnings("null")
  public void
  generate
  (
    ArrayList<PackageCommon> packages 
  )
    throws PipelineException
  {
    DoubleMap<MachineType, String, String> environment = 
      new DoubleMap<MachineType, String, String>();
    
    pConflicts    = new DoubleMap<MachineType, String, LinkedList<Integer>>();
    pAnyConflicts = new MappedSet<MachineType, Integer>();
    
    pHasModifiable = false;

    pSupportedMachineTypes = new TreeSet<MachineType>();
    
    ListMap<String, String> packageList = getPackages();
    
    if (packageList.size() != packages.size())
      throw new PipelineException
        ("Invalid number of packages contained in call to generate()");
    
    for (int i = 0; i < packageList.size(); i++) {
      PackageCommon pack = packages.get(i);
      String correctName = packageList.getKey(i);
      String correctID = packageList.getValue(i);
      
      String packageName = pack.getName();
      String packID = null;
      
      if(pack instanceof PackageVersion) 
        packID = ((PackageVersion) pack).getVersionID().toString();
      else {
        pHasModifiable = true;
        packID = ((PackageMod) pack).getAuthor();
      }
      if (!correctName.equals(packageName) || 
          !correctID.equals(packID) )
      throw new PipelineException
        ("The package (" + packageName + "), version (" + packID + ") was passed in as the " +
         "(" + i + ") package.  It does not match the expected package (" + correctName + ") " +
         "and version (" + correctID +")");
      
      pSupportedMachineTypes.addAll(pack.getSupportedMachineTypes());
    }
    
    /* Allow the user to specify a subset of supported MachineTypes to use.*/
    TreeSet<MachineType> selectedMachineTypes = getSelectedMachineTypes();
    TreeSet<MachineType> actualMachineTypes = new TreeSet<MachineType>();
    if (selectedMachineTypes != null) {
      for (MachineType type : selectedMachineTypes)
        if (pSupportedMachineTypes.contains(type))
          actualMachineTypes.add(type);
    }
    else
      actualMachineTypes = new TreeSet<MachineType>(pSupportedMachineTypes);

    for (MachineType machine : actualMachineTypes) {
      OsType os = machine.getOsType();
      ArchType arch = machine.getArchType();
      String pathSep = PackageInfo.getPathSep(os);  
        
      int idx = 0;
      for(PackageCommon com : packages) {
        for(PackageEntry entry : com.getEntries(os, arch)) {
          String key = entry.getName();
          
          String value = entry.getValue();
          if(environment.containsKey(machine, key)) {
            String prev = environment.get(machine, key);
            switch(entry.getMergePolicy()) {
            case Exclusive:
              {
                LinkedList<Integer> pkgs = pConflicts.get(machine, key);
                if(pkgs == null) {
                  pkgs = new LinkedList<Integer>();
                  pConflicts.put(machine, key, pkgs);
                }
                pkgs.add(idx);

                pAnyConflicts.put(machine, idx);
              }
              break;
              
            case Override:
              environment.put(machine, key, value);
              break;

            case Ignore:
              break;
              
            case AppendPath:
            case PrependPath:
              if(prev != null) {
                if(value != null) {
                  String first = null;
                  String second = null;
                  switch(entry.getMergePolicy()) {
                  case AppendPath:
                    first  = prev;
                    second = value;
                    break;
                    
                  case PrependPath:
                    first  = value;
                    second = prev;
                  }
                  
                  ArrayList<String> paths = new ArrayList<String>();
                  {
                    String dirs[] = first.split(pathSep);  
                    int wk;
                    for(wk=0; wk<dirs.length; wk++) 
                      if((dirs[wk].length() > 0) && !paths.contains(dirs[wk]))
                        paths.add(dirs[wk]);
                  }
                  
                  {
                    String dirs[] = second.split(pathSep);
                    int wk;
                    for(wk=0; wk<dirs.length; wk++) 
                      if((dirs[wk].length() > 0) && !paths.contains(dirs[wk]))
                        paths.add(dirs[wk]);
                  }
                  
                  String nvalue = null;
                  {
                    StringBuilder buf = new StringBuilder();
                    for(String path : paths) 
                    buf.append(path + pathSep);
                    String str = buf.toString();
                    
                    if(paths.size() > 1) 
                      nvalue = str.substring(0, str.length()-1);
                    else 
                      nvalue = str;
                  }
                  
                  environment.put(machine, key, nvalue);
                }
                else {
                  environment.put(machine, key, prev);
                }
              }
              else {
                environment.put(machine, key, value);
              }
            }
          }
          else {
            environment.put(machine, key, value);
          }
        }
        idx++;
      }
    }
    pEnvCache = new EnvCache(environment);
    pGenerated = true;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this toolset have any environmental variable package conflicts?
   */ 
  public boolean 
  hasConflicts() 
  {
    if (!pGenerated)
      throw new IllegalStateException
        ("The toolset must be generated before informational methods are called.");
    
    return (!pAnyConflicts.isEmpty());
  }
  
  /**
   * Does the given environmental variable have any package conflicts?
   * 
   * @param name
   *   The environmental variable name.
   */ 
  public boolean 
  isEnvConflicted
  (
   String name
  ) 
  {
    if (!pGenerated)
      throw new IllegalStateException
        ("The toolset must be generated before informational methods are called.");
    
    return pConflicts.containsKey(name);
  }

  /**
   * Get the names of the environmental variables which have package conflicts, 
   * indexed by machine type.
   */ 
  public MappedSet<MachineType, String>
  getConflictedEnvNames() 
  {
    if (!pGenerated)
      throw new IllegalStateException
        ("The toolset must be generated before informational methods are called.");
    
    return pConflicts.mappedKeySet();
  }

  /**
   * Does the package for the given index have any environmental variable conflicts
   * for any machine type?
   * 
   * @param idx
   *   The package index.
   */ 
  public boolean 
  isPackageConflicted
  (
   int idx
  ) 
  {
    if (!pGenerated)
      throw new IllegalStateException
        ("The toolset must be generated before informational methods are called.");
    
    for (MachineType machine : pAnyConflicts.keySet())
      if (pAnyConflicts.get(machine).contains(idx))
        return true;
    return false;
  }
  
  /**
   * Does the package for the given index have a conflict for the given environmental 
   * variable name?
   * 
   * @param idx
   *   The package index.
   * 
   * @param name
   *   The environmental variable name.
   */ 
  public boolean 
  isPackageEnvConflicted
  (
   int idx, 
   String name
  )
  {
    if (!pGenerated)
      throw new IllegalStateException
        ("The toolset must be generated before informational methods are called.");
    
    for (MachineType machine : pConflicts.keySet()) {
      TreeMap<String, LinkedList<Integer>> conflicts = pConflicts.get(machine);
      if (conflicts.containsKey(name) && conflicts.get(name).contains(idx))
        return true;
    }
    return false;
  }
  
  /**
   * Is the current toolset ready to be frozen?
   */ 
  public boolean 
  isFreezable() 
  {
    if (!pGenerated)
      throw new IllegalStateException
        ("The toolset must be generated before informational methods are called.");
    
    return (hasPackages() && !pHasModifiable && !hasConflicts());
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Was the toolset built with any modifiable packages.
   */ 
  public boolean 
  hasModifiablePackages()
  {
    if (!pGenerated)
      throw new IllegalStateException
        ("The toolset must be generated before informational methods are called.");    
    return pHasModifiable;
  }
  
  /**
   * Does the toolset contain a modifiable package with the given name.
   */ 
  public boolean 
  hasModifiablePackage
  (
   String name
  ) 
  {
    if (!pGenerated)
      throw new IllegalStateException
        ("The toolset must be generated before informational methods are called.");
    ListMap<String, String> packages = getPackages();
    String id = packages.get(name);
    if (id != null && !VersionID.isValidVersionID(id))
      return true;

    return false;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the user who created this package.
   */
  public String
  getAuthor()
  {
    return pAuthor;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Marks the toolset as being needed to be regenerated.
   */
  public void
  invalidate()
  {
    pGenerated = false;
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the cooked toolset environment specialized for a specific machine type.
   * 
   * @param machine
   *   The machine type.
   * 
   * @throws IllegalArgumentException
   *   If the specified machine type is not valid for this toolset.
   * 
   * @throws IllegalStateException
   *   If {@link #generate(ArrayList)} has not been called prior to this call.
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
    MachineType machine  
  )
  {
    if (!pGenerated)
      throw new IllegalStateException
        ("Unable to return an environment if the toolset mod has not been generated.");
    return pEnvCache.getEnvironment(machine);
  }
  
  /**
   * Get the cooked toolset environment specialized for a specific user and machine type..
   * 
   * @param author
   *   The user owning the generated environment.
   * 
   * @param machine
   *   The machine type.
   * 
   * @throws IllegalArgumentException
   *   If the specified machine type is not valid for this toolset.
   *   
   * @throws IllegalStateException
   *   If {@link #generate(ArrayList)} has not been called prior to this call.
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
    String author,
    MachineType machine  
  )
  {
    if (!pGenerated)
      throw new IllegalStateException
        ("Unable to return an environment if the toolset mod has not been generated.");
    return pEnvCache.getEnvironment(author, machine);
  }
  
  /**
   * Get the cooked toolset environment specialized for a specific user, working area, and 
   * machine type.
   * 
   * @param author
   *   The user owning the generated environment.
   *   
   * @param view
   *   The name of the user's working area view. 
   * 
   * @param machine
   *   The machine type.
   * 
   * @throws IllegalArgumentException
   *   If the specified machine type is not valid for this toolset.
   *   
   * @throws IllegalStateException
   *   If {@link #generate(ArrayList)} has not been called prior to this call.
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
    String author,
    String view,
    MachineType machine  
  )
  {
    if (!pGenerated)
      throw new IllegalStateException
        ("Unable to return an environment if the toolset mod has not been generated.");
    return pEnvCache.getEnvironment(author, view, machine);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public boolean 
  isFrozen()
  {
    return false;
  }
  
  @Override
  public Set<MachineType> 
  getActualMachineTypes()
  {
    if (!pGenerated)
      throw new IllegalStateException
        ("Unable to return the list of actual machine types if the toolset mod has not " +
         "been generated.");
    return pEnvCache.getSupportedMachineTypes();
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

  private static final long serialVersionUID = -6401664241617329079L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The state of the toolset.
   * <p>
   * If the toolset is not generated, then it is merely a list of packages and versions, without
   * actually containing a cooked environment.  Once the toolset has been cooked, this will be
   * true, indicating it can be used.  Calls to {@link #getEnvironment(MachineType)} will
   * result in exceptions being thrown unless the toolset has been cooked.
   */
  private boolean pGenerated;
  
  /**
   * A list of the supported machine types.
   */
  private TreeSet<MachineType> pSupportedMachineTypes;
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Was the toolset built with any modifiable packages.
   */ 
  private boolean  pHasModifiable;
  
  /**
   * The indices in order of evaluation of the packages which are in conflict indexed by 
   * the environmental variable name.
   */
  private DoubleMap<MachineType, String, LinkedList<Integer>>  pConflicts;
  
  /**
   * The indices of the packages for which there are one or more environmental 
   * variable conflicts.
   */
  private MappedSet<MachineType, Integer>  pAnyConflicts;
  
  /**
   * The environments contained in this toolset mod.
   */
  private EnvCache pEnvCache;

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The user who created this mod.
   * <p>
   * Used as a unique identifier for this toolset mod.
   */
  private String pAuthor;
}
