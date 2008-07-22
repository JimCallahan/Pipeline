// $Id: ToolsetCommon.java,v 1.1 2008/07/22 21:36:07 jesse Exp $

package us.temerity.pipeline.toolset2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T    C O M M O N                                                           */
/*------------------------------------------------------------------------------------------*/

/**
 * A named shell environment constructed by evaluating a set of toolset packages.
 */
public abstract
class ToolsetCommon
  extends Named
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
  ToolsetCommon()
  {}
  
  /**
   * Internal constructor used by subclasses to create an empty toolset.
   * 
   * @param name
   *   The name of the toolset.
   *   
   * @param machineTypes
   *   The list of machine types that this toolset should support or <code>null</code> if the
   *   toolset should support all possible machine types. 
   */ 
  protected
  ToolsetCommon
  (
   String name,
   Set<MachineType> machineTypes
  ) 
  {
    this(name, new ListMap<String, String>(), machineTypes);
  }
  
  /**
   * Internal constructor used by subclasses to create a toolset composed of the 
   * given packages.
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
  ToolsetCommon
  (
   String name, 
   ListMap<String, String> packages,
   Set<MachineType> machineTypes
  ) 
  {
    super(name);
    if (packages == null)
      throw new IllegalArgumentException("Packages should not be null");
    
    if (machineTypes != null)
      pSelectedMachineTypes = new TreeSet<MachineType>(machineTypes);
    
    pPackages = new ListMap<String, String>(pPackages);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A B S T R A C T                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the current toolset already frozen?
   */ 
  public abstract boolean 
  isFrozen();

  /**
   * Get the list of machine types that this toolset contains an environment for.
   */
  public abstract Set<MachineType>
  getActualMachineTypes();

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Does this toolset have any packages?
   */
  public boolean 
  hasPackages() 
  {
    return (!pPackages.isEmpty());
  }
  
  /** 
   * Get the number of packages.
   */ 
  public int
  getNumPackages() 
  {
    return (pPackages.size());
  }
  
  /**
   * Get the list of packages in the toolset.
   */ 
  public ListMap<String, String>
  getPackages() 
  {
    return new ListMap<String, String>(pPackages);
  }

  /**
   * Get the name of the package with the given index.
   */ 
  public String
  getPackageName
  (
   int idx
  ) 
  {
    return (pPackages.getKey(idx));
  }
  
  /**
   * Get revision of the package with the given index. <P> 
   * 
   * @return
   *   The revision, either a version number or a user's name.
   */ 
  public String
  getPackageVersionID
  (
   int idx
  ) 
  {
    return (pPackages.getValue(idx));
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The list of machine types that this toolset is defined as supporting, which may not
   * actually be the list of machine types that the toolset is capable of supporting.
   * <p>
   * This list can contain machine types which the toolset is not capable of supporting and
   * may also be missing machine types which the toolset is capable of supporting.  This list
   * should not be consider a definitive list of what the toolset actually supports, rather it
   * is the complete list of what the toolset is allowed to support.
   * <p>
   * If this is set to <code>null</code>, then the toolset will support all allowable machine
   * types.
   */
  public TreeSet<MachineType>
  getSelectedMachineTypes()
  {
    if (pSelectedMachineTypes == null)
      return null;
    return new TreeSet<MachineType>(pSelectedMachineTypes);
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

    encoder.encode("Packages", pPackages);
    encoder.encode("SelectedMachineTypes", pSelectedMachineTypes);
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

    ListMap<String, String> packages = 
      (ListMap<String, String>) decoder.decode("Packages");
    if(packages == null) 
      throw new GlueException("The \"Packages\" was missing or (null)!");
    pPackages = packages;

    TreeSet<MachineType> selected = 
      (TreeSet<MachineType>) decoder.decode("SelectedMachineTypes");
    pSelectedMachineTypes = selected;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3653921881899229002L;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  
  /**
   * The names and versions of the packages which make up the toolset in the order 
   * they are added into the environment.
   * 
   * The revision is either the version number, if the package is frozen or the name of the
   * user who created the working version.
   */
  private ListMap<String, String> pPackages;
  
  /**
   * A list of the selected machine types.
   */
  private TreeSet<MachineType> pSelectedMachineTypes;
}
