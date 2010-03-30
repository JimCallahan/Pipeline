// $Id: FileExtractSiteVersionReq.java,v 1.1 2009/03/25 22:02:24 jim Exp $

package us.temerity.pipeline.message.file;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   E X T R A C T   S I T E   V E R S I O N   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a JAR archive containing both files and metadata associated with a checked-in
 * version of a node suitable for transfer to a remote site.
 * 
 * @see MasterMgr
 */
public
class FileExtractSiteVersionReq
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
   * @param vsn
   *   The extracted node version with all modifications applied to include in the 
   *   JAR archive.
   * 
   * @param stamp
   *   The timestamp of when this node was extracted.
   * 
   * @param creator
   *   The name of the user who extracted the node.
   * 
   * @param tarPath
   *   The name of the TAR archive to create.
   */
  public
  FileExtractSiteVersionReq
  (
   String name, 
   TreeSet<String> referenceNames, 
   String localSiteName, 
   TreeSet<FileSeq> replaceSeqs, 
   TreeMap<String,String> replacements,
   NodeVersion vsn, 
   long stamp, 
   String creator, 
   Path tarPath
  )
  { 
    super();

    if(name == null) 
      throw new IllegalArgumentException
	("The fully resolved node name cannot be (null)!");
    pName = name;
    
    pReferenceNames = referenceNames;

    if(localSiteName == null) 
      throw new IllegalArgumentException
	("The local site name cannot be (null)!");
    pLocalSiteName = localSiteName;
    
    pReplaceSeqs = replaceSeqs;
    pReplacements = replacements;
    
    if(vsn == null) 
      throw new IllegalArgumentException
	("The extracted node version cannot be (null)!");
    pNodeVersion = vsn;

    pStamp = stamp;

    if(creator == null) 
      throw new IllegalArgumentException
	("The user extracting the node cannot be (null)!");
    pCreator = creator;

    if(tarPath == null) 
      throw new IllegalArgumentException
	("The path for the TAR archive cannot be (null)!");
    pTarPath = tarPath;
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
   * Gets the extracted node version with all modifications applied to include in the 
   * TAR archive.
   */
  public NodeVersion
  getNodeVersion() 
  {
    return pNodeVersion;
  }

  /**
   * Gets the timestamp of when this node was extracted.
   */
  public long
  getStamp() 
  {
    return pStamp;
  }

  /**
   * Gets the name of the user who extracted the node.
   */
  public String
  getCreator() 
  {
    return pCreator;
  }

  /**
   * Gets the name of the TAR archive to create.
   */
  public Path
  getTarPath() 
  {
    return pTarPath;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3430992938938342172L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node name of the node to extract.
   */
  public String  pName;

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
   * Gets the extracted node version with all modifications applied to include in the 
   * TAR archive.
   */
  public NodeVersion pNodeVersion;

  /**
   * Gets the timestamp of when this node was extracted.
   */
  public long pStamp;

  /**
   * Gets the name of the user who extracted the node.
   */
  public String pCreator;

  /**
   * Gets the name of the TAR archive to create.
   */
  public Path  pTarPath;

}
  
