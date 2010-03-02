// $Id: NodeExtractSiteVersionReq.java,v 1.1 2009/03/25 22:02:24 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   E X T R A C T   S I T E   V E R S I O N   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a JAR archive containing both files and metadata associated with a checked-in
 * version of a node suitable for transfer to a remote site.
 * 
 * @see MasterMgr
 */
public
class NodeExtractSiteVersionReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param name
   *   The fully resolved node name of the node to extract.
   * 
   * @param vid
   *   The revision number of the node version to extract. 
   * 
   * @param referenceNames
   *   The fully resolved names of the source nodes to include as Reference links or
   *   <CODE>null</CODE> if no links should be included.
   * 
   * @param localSiteName
   *   Name for the local site which will be used to modify extracted node names.
   * 
   * @param replaceSeqs
   *   The primary and secondary file sequences associated with the node to which all 
   *   string replacements should be applied or <CODE>null</CODE> to skip all file contents 
   *   replacements.
   * 
   * @param replacements
   *   The table of additional string replacements to perform on the files associated
   *   with the node version being extracted or <CODE>null</CODE> if there are no
   *   additional replacements. 
   * 
   * @parma dir
   *   The directory in which to place the JAR archive created.
   *
   * @param compress
   *   Whether to compress the files in the generated JAR archive.
   */
  public
  NodeExtractSiteVersionReq
  (
   String name, 
   VersionID vid, 
   TreeSet<String> referenceNames, 
   String localSiteName, 
   TreeSet<FileSeq> replaceSeqs, 
   TreeMap<String,String> replacements,
   Path dir, 
   boolean compress
  )
  { 
    super();

    if(name == null) 
      throw new IllegalArgumentException
	("The fully resolved node name cannot be (null)!");
    pName = name;

    if(vid == null) 
      throw new IllegalArgumentException
	("The revision number cannot be (null)!");
    pVersionID = vid;
    
    pReferenceNames = referenceNames;

    if(localSiteName == null) 
      throw new IllegalArgumentException
	("The local site name cannot be (null)!");
    pLocalSiteName = localSiteName;
    
    pReplaceSeqs = replaceSeqs;
    pReplacements = replacements;
    
    if(dir == null) 
      throw new IllegalArgumentException
	("The output directory cannot be (null)!");
    pDir = dir;

    pCompress = compress;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node name of the node to extract.
   */
  public String
  getName() 
  {
    return pName;
  }

  /**
   * Gets the revision number of the node version to extract. 
   */
  public VersionID
  getVersionID() 
  {
    return pVersionID;
  }

  /**
   * Gets the fully resolved names of the source nodes to include as Reference links or
   * <CODE>null</CODE> if no links should be included.
   */
  public TreeSet<String>
  getReferenceNames() 
  {
    return pReferenceNames;
  }

  /**
   * Gets for the local site which will be used to modify extracted node names.
   */
  public String
  getLocalSiteName() 
  {
    return pLocalSiteName;
  }

  /**
   * Gets the primary and secondary file sequences associated with the node to which all 
   * string replacements should be applied or <CODE>null</CODE> to skip all file contents 
   * replacements.
   */
  public TreeSet<FileSeq>
  getReplaceSeqs() 
  {
    return pReplaceSeqs; 
  }

  /**
   * Gets the table of additional string replacements to perform on the files associated
   * with the node version being extracted or <CODE>null</CODE> if there are no
   * additional replacements.
   */
  public TreeMap<String,String> 
  getReplacements() 
  {
    return pReplacements;
  }

  /**
   * Gets the directory in which to place the JAR archive created.
   */
  public Path
  getDir() 
  {
    return pDir;
  }

  /**
   * Whether to compress the files in the generated JAR archive.
   */ 
  public boolean 
  getCompress() 
  {
    return pCompress;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5175523751616713784L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node name of the node to extract.
   */
  public String  pName;

  /**
   * Gets the revision number of the node version to extract. 
   */
  public VersionID  pVersionID;

  /**
   * Gets the fully resolved names of the source nodes to include as Reference links or
   * <CODE>null</CODE> if no links should be included.
   */
  public TreeSet<String>  pReferenceNames;

  /**
   * Gets for the local site which will be used to modify extracted node names.
   */
  public String  pLocalSiteName;

  /**
   * Gets the primary and secondary file sequences associated with the node to which all 
   * string replacements should be applied or <CODE>null</CODE> to skip all file contents 
   * replacements.
   */
  public TreeSet<FileSeq>  pReplaceSeqs;

  /**
   * Gets the table of additional string replacements to perform on the files associated
   * with the node version being extracted or <CODE>null</CODE> if there are no
   * additional replacements.
   */
  public TreeMap<String,String>  pReplacements;

  /**
   * Gets the directory in which to place the JAR archive created.
   */
  public Path  pDir;

  /**
   * Whether to compress the files in the generated JAR archive.
   */ 
  public boolean pCompress; 

}
  
