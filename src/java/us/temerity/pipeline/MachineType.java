// $Id: MachineType.java,v 1.1 2008/07/22 21:36:07 jesse Exp $

package us.temerity.pipeline;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   M A C H I N E   T Y P E                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A Combination of OsType and MachineType that represents a specific sort of hardware.
 */
public 
class MachineType
  implements Comparable<MachineType>, Glueable, Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  protected 
  MachineType() 
  {}

  /**
   * Create a new Machine Type.
   * 
   * @param os
   *   The operating system.
   * 
   * @param arch
   *   The machine architecture.
   *   
   * @throws IllegalArgumentException
   *   If either value is <code>null</code> or the architecture is not valid for the 
   *   specified os.
   */
  public
  MachineType
  (
    OsType os,
    ArchType arch
  )
  {
    if (os == null)
      throw new IllegalArgumentException("Cannot have a (null) os value");
    if (arch == null)
      throw new IllegalArgumentException("Cannot have a (null) arch value");
    if (os.isValidArchType(arch))
      throw new IllegalArgumentException
        ("The ArchType (" + arch.toTitle() + ") is not valid for the OsType " +
         "(" + os.toTitle() + ")");
    
    pOsType = os;
    pArchType = arch;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the os type.
   */
  public final OsType 
  getOsType()
  {
    return pOsType;
  }

  
  /**
   * Get the architecture type.
   */
  public final ArchType 
  getArchType()
  {
    return pArchType;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  
  @Override
  public int 
  compareTo
  (
    MachineType that
  )
  {
    int compare = this.pOsType.compareTo(that.pOsType);
    if (compare == 0)
      compare = this.pArchType.compareTo(that.pArchType);
    return compare;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public void 
  fromGlue
  (
    GlueDecoder decoder
  )
    throws GlueException
  {
    OsType os = (OsType) decoder.decode("OsType"); 
    if(os == null) 
      throw new GlueException("The \"OsType\" was missing!");
    pOsType = os;
    
    ArchType arch = (ArchType) decoder.decode("ArchType"); 
    if(arch == null) 
      throw new GlueException("The \"ArchType\" was missing!");
    pArchType = arch;
  }

  @Override
  public void 
  toGlue
  (
    GlueEncoder encoder
  )
    throws GlueException
  {
    encoder.encode("OsType", pOsType);
    encoder.encode("ArchType", pArchType);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R I D E S                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public String 
  toString()
  {
    return pOsType.toTitle() + ":" + pArchType.toTitle();
  }
  
  @Override
  public boolean 
  equals
  (
    Object obj
  )
  {
    if (!(obj instanceof MachineType))
      return false;
    
    MachineType that = (MachineType) obj;
    
    return ((this.pOsType == that.pOsType) && (this.pArchType == that.pArchType ));
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets all the valid Machine Types.
   */
  public static TreeSet<MachineType>
  getAllMachineTypes()
  {
    TreeSet<MachineType> toReturn = new TreeSet<MachineType>();
    for (OsType os : OsType.all()) 
      for (ArchType arch : OsType.getValidArchTypes(os))
        toReturn.add(new MachineType(os, arch));
    return toReturn;
  }
  
  /**
   * Gets all the valid Machine Types for the given OS.
   * 
   * @param os
   *   The os type.
   */
  public static TreeSet<MachineType>
  getMachinesTypes
  (
    OsType os  
  )
  {
    TreeSet<MachineType> toReturn = new TreeSet<MachineType>();
    for (ArchType arch : OsType.getValidArchTypes(os))
      toReturn.add(new MachineType(os, arch));
    return toReturn;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8785129117203174640L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private OsType pOsType;
  private ArchType pArchType;
}
