// $Id: PackageVersion.java,v 1.1 2008/07/22 21:36:07 jesse Exp $

package us.temerity.pipeline.toolset2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   V E R S I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A read-only version of a package. <P> 
 */
public 
class PackageVersion
  extends PackageCommon
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  PackageVersion() 
  {
    super();
  }
  
  /**
   * Construct a read-only package version from a modifiable package.
   * 
   * @param author
   *   The name of the user creating the package.
   * 
   * @param mod
   *   The modifiable package.
   * 
   * @param vid 
   *   The revision number of the new read-only package.
   * 
   * @param desc
   *   The package description.
   */ 
  public
  PackageVersion
  (
    String author, 
    PackageMod mod, 
    VersionID vid,
    String desc
  ) 
  {
    super(mod);
    
    if(vid == null) 
      throw new IllegalArgumentException("The revision number cannot be (null)!");
    pVersionID = vid;

    if(desc == null) 
      throw new IllegalArgumentException("The package description cannot be (null)!");
    pMessage = new SimpleLogMessage(author, desc);
    
    pSupportedMachineTypes = new TreeSet<MachineType>(mod.getSupportedMachineTypes());
  }
  
  /** 
   * Copy constructor. 
   * 
   * @param vsn 
   *   The <CODE>PackageVersion</CODE> to copy.
   */ 
  public 
  PackageVersion
  (
    PackageVersion vsn
  ) 
  {
    super(vsn);

    pVersionID = vsn.getVersionID();
    pMessage   = new SimpleLogMessage(vsn.pMessage);
    
    pSupportedMachineTypes = new TreeSet<MachineType>(vsn.getSupportedMachineTypes());
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Whether the given package has the same name and revision number as this package.
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
    if((com != null) && (com instanceof PackageVersion)) {
      PackageVersion vsn = (PackageVersion) com;
      return (super.similarTo(com) && 
              pVersionID.equals(vsn.getVersionID()));
    }

    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the revision number of this version of the package.
   */ 
  public VersionID
  getVersionID()
  {
    return pVersionID;
  }

  /**
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the 
   * version was created.
   */ 
  public long
  getTimeStamp() 
  {
    return pMessage.getTimeStamp();
  }

  /**
   * Get the name of the user who created the version.
   */ 
  public String
  getAuthor() 
  {
    return pMessage.getAuthor();
  }

  /**
   * Get the package description.
   */ 
  public String
  getDescription() 
  {
    return pMessage.getMessage();
  }
  
  @Override
  public TreeSet<MachineType>
  getSupportedMachineTypes()
  {
    return new TreeSet<MachineType>(pSupportedMachineTypes);
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

    encoder.encode("VersionID", pVersionID);
    encoder.encode("Message", pMessage);
    encoder.encode("SupportedMachineTypes", pSupportedMachineTypes);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void 
  fromGlue
  (
    GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    VersionID vid = (VersionID) decoder.decode("VersionID");
    if(vid == null) 
      throw new GlueException("The \"VersionID\" was missing!");
    pVersionID = vid;

    SimpleLogMessage msg = (SimpleLogMessage) decoder.decode("Message");
    if(msg == null) 
      throw new GlueException("The \"Message\" was missing!");
    pMessage = msg;
    
    TreeSet<MachineType> smt = (TreeSet<MachineType>) decoder.decode("SupportedMachineTypes");
    if (smt == null)
      throw new GlueException("The \"Supported Machine Types\" were missing!");
    pSupportedMachineTypes = smt;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1239795136113028579L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision number of this version. 
   */ 
  private VersionID  pVersionID;       

  /**
   * The descriptive message given at the time this version of the package was created. <P> 
   * 
   * The timestamp and author of the message are also the timestamp and author of the 
   * package version. <P> 
   */
  private SimpleLogMessage  pMessage;
  
  /**
   * The list of machine types that this package provides variables for.
   */
  private TreeSet<MachineType> pSupportedMachineTypes;
}
