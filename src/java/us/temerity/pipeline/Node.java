// $Id: Node.java,v 1.1 2004/02/28 20:05:47 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E                                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The complete set of information related to a Pipeline node.
 */
public
class Node        
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a complete runtime representation of the node. <P> 
   *
   * Loads all Glue files related to the node including working versions, checked-in versions,
   * downstream connections for checked-in versions and change comments.
   * 
   * @param name [<B>in</B>]
   *   The fully resolved node name.
   * 
   * @throws GlueException
   *   If unable to access or parse any of the component Glue files.
   */
  public 
  Node
  (
   String name
  ) 
    throws GlueException
  {
    super(name);

    {
      pMods = new TreeMap<String,NodeMod>();

      // ... 
    }

    {
      pVersions = new TreeMap<VersionID,NodeVersion>();

      // ... 
    }

    {
      pComments = new TreeMap<VersionID,TreeMap<Date,LogMessage>>();

      // ... 
    }

    {
      pTargets  = new TreeMap<VersionID,TreeMap<String,VersionID>>();

      // ... 
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  
  

  

  /*----------------------------------------------------------------------------------------*/
  /*   I / O   H E L P E R S                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Save the current state of the given working version disk under the name of the 
   * given user. <P> 
   * 
   * If a working verion already exists on disk for <CODE>author</CODE>, a back-up copy 
   * of this file is made before replacing it with this version. 
   * 
   * @param mod [<B>in</B>]
   *   The working version to save to disk.
   * 
   * @param author [<B>in</B>]
   *   The of the user which owns the working version.
   * 
   * @throws GlueException
   *   If unable to save the Glue format file.
   */ 
  private void 
  saveMod
  ( 
   NodeMod mod,
   String author
  )
    throws GlueException    
  {
    

  }

  /** 
   * Save a new checked-in version of the node to disk. <P> 
   * 
   * If the version is successfully saved, the file on disk is made read-only to prevent
   * any accidental future modification of the checked-in version.  Once a version is saved
   * it should never be modified for any reason.
   * 
   * @param author [<B>in</B>]
   *   The of the user which owns the working version.
   * 
   * @throws GlueException
   *   If unable to save the Glue format file or if a checked-in version already exists
   *   on disk.
   */
  private void 
  saveNewVersion
  (
   NodeVersion vsn
  ) 
    throws GlueException 
  {

    
  }

  /** 
   * Save a new change comment associated with a checked-in version of the node. <P> 
   * 
   * @param comment [<B>in</B>]
   *   The change comment to save to disk.
   * 
   * @param vid [<B>in</B>]
   *   The revision number of the checked-in version the comment is associated with.
   * 
   * @throws GlueException
   *   If unable to save the Glue format file.
   */ 
  private void 
  saveNewComment
  (
   LogMessage comment, 
   VersionID vid
  ) 
    throws GlueException 
  {
    

  }

  /**
   * Save the current downstream node connection information associated with a checked-in
   * version with the given revision number to disk. <P> 
   *
   * If a downstream node connection information file already exists on disk for 
   * <CODE>vid</CODE>, a back-up copy of this file is made before replacing it.
   * 
   * @param vid [<B>in</B>]
   *   The revision number of the checked-in version the connections are associated with.
   * 
   * @throws GlueException
   *   If unable to save the Glue format file.
   */
  private void 
  saveVersionTargets
  (
   VersionID vid
   ) 
    throws GlueException
  {

  }
   



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The working versions of the node indexed by user name.
   */ 
  private TreeMap<String,NodeMod>  pMods;
 

  /**
   * The checked-in versions of the node indexed by revision number.
   */ 
  private TreeMap<VersionID,NodeVersion>  pVersions;
 
  /**
   * The change comments associated with the checked-in versions indexed by revision number
   * and timestamp of the comment.
   */ 
  private TreeMap<VersionID,TreeMap<Date,LogMessage>>  pComments;
 
  /**
   * The downstream connections for each checked-in version of the node.
   */ 
  private TreeMap<VersionID,TreeMap<String,VersionID>>  pTargets;
 
}

