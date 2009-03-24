// $Id: UICache.java,v 1.1 2009/03/24 01:21:21 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.*;

import us.temerity.pipeline.*;

/**
 * A cache containing commonly accessed data fields to improve UI responsiveness.
 * <p>
 * Caches active toolsets, default toolset, and user/group privileges.
 */
public 
class UICache
{

  /**
   * Get the cached names of the active Unix toolsets.
   * <P>
   * Each time the active toolset names are obtained from the server they are cached. If the
   * cache has not been invalidated since the last communication with the server, this method
   * returns the last cached value instead. If the cache has been invalidated, this method
   * behaves identically to {@link MasterMgrClient#getActiveToolsetNames()
   * getActiveToolsetNames}. This means that the name returned by this method is not
   * guaranteed to be up-to-date but is much faster.
   * <P>
   * 
   * This method is provided mostly to support UI components which depend on the default
   * toolset name, but don't need a more up-to-date value than the last status update. Unless
   * speed is a critical factor, its better to use the normal non-caching method to determine
   * the active toolset names.
   * 
   * @throws PipelineException
   *  If unable to determine the toolset names.
   */ 
  public synchronized TreeSet<String>
  getCachedActiveToolsetNames() 
    throws PipelineException
  {    
    if(pActiveToolsetNames == null) {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        pActiveToolsetNames = client.getActiveToolsetNames();
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
    }
    return pActiveToolsetNames;
  }
  

  /**
   * Manually invalidate the active toolset names cache.
   */ 
  public synchronized void
  invalidateCachedActiveToolsetNames()
  {
    pActiveToolsetNames = null;
  }

  /**
   * Get the cached name of the default Unix toolset.
   * <P>
   * 
   * Each time the default toolset name is obtained from the server it is cached. If the cache
   * has not been invalidated since the last communication with the server, this method
   * returns the last cached value instead. If the cache has been invalidated, this method
   * behaves identically to {@link MasterMgrClient#getDefaultToolsetName()
   * getDefaultToolsetName}. This means that the name returned by this method is not
   * guaranteed to be up-to-date but is much faster.
   * <P>
   * 
   * This method is provided mostly to support UI components which depend on the default
   * toolset name, but don't need a more up-to-date value than the last status update. Unless
   * speed is a critical factor, its better to use the normal non-caching method to determine
   * the default toolset name.
   * 
   * @throws PipelineException
   *  If unable to determine the default toolset name.
   */ 
  public synchronized String
  getCachedDefaultToolsetName() 
    throws PipelineException
  {    
    if(pDefaultToolsetName == null) {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        pDefaultToolsetName = client.getDefaultToolsetName();
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
    }
    return pDefaultToolsetName;
  }

  /**
   * Manually invalidate the default toolset name cache.
   */ 
  public synchronized void 
  invalidateCachedDefaultToolsetName()
  {
    pDefaultToolsetName = null;
  }

  /**
   * Get the cached work groups used to determine the scope of administrative privileges.
   * <p>
   * Each time the work groups are obtained from the server they are cached. If the cache has
   * not been invalidated since the last communication with the server, this method returns
   * the last cached value instead. If the cache has been invalidated, this method behaves
   * identically to {@link MasterMgrClient#getWorkGroups() getWorkGroups}. This means that the
   * groups returned by this method are not guaranteed to be up-to-date but are much faster.
   * <P>
   * This method is provided mostly to support UI components which depend on the work groups,
   * but don't need a more up-to-date value than the last status update. Unless speed is a
   * critical factor, its better to use the normal non-caching method to determine the work
   * groups.
   * 
   * @return 
   *   The work groups.
   * 
   * @throws PipelineException
   *   If unable to get the work groups.
   */ 
  public synchronized WorkGroups
  getCachedWorkGroups()
    throws PipelineException  
  {
    if(pWorkGroups == null)  {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        pWorkGroups = client.getWorkGroups();
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
    }
    return pWorkGroups;
  }

  /**
   * Manually invalidate the work groups cache.
   */ 
  public synchronized void 
  invalidateCachedWorkGroups()
  {
    pWorkGroups = null;
  }
  
  /**
   * Get the cached privileges granted to the current user with respect to all other users.
   * <P>
   * Each time the privileges are obtained from the server they are cached. If the cache has
   * not been invalidated since the last communication with the server, this method returns
   * the last cached value instead. If the cache has been invalidated, this method behaves
   * identically to {@link MasterMgrClient#getPrivilegeDetails() getPrivilegeDetails}. This
   * means that the privileges returned by this method are not guaranteed to be up-to-date and
   * operations relying on these privileges may still fail due to a change in privileges since
   * the cache was last updated.
   * <P>
   * @return 
   *   The privileges of the current user.
   * 
   * @throws PipelineException
   *  If unable to determine the privileges.
   */
  public synchronized PrivilegeDetails
  getCachedPrivilegeDetails() 
    throws PipelineException 
  {
    if(pPrivilegeDetails == null) {
      UIMaster master = UIMaster.getInstance();
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        pPrivilegeDetails = client.getPrivilegeDetails();
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
    }
    return pPrivilegeDetails;
  }

  /**
   * Manually invalidate the privilege details cache.
   */ 
  public synchronized void 
  invalidateCachedPrivilegeDetails()
  {
    pPrivilegeDetails = null;
  }
  
  /**
   * Shortcut method for invalidating all the caches.
   * 
   * @see #invalidateCachedActiveToolsetNames()
   * @see #invalidateCachedDefaultToolsetName()
   * @see #invalidateCachedPrivilegeDetails()
   * @see #invalidateCachedWorkGroups()
   */
  public synchronized void
  invalidateCaches()
  {
    pWorkGroups = null;
    pPrivilegeDetails = null;
    pDefaultToolsetName = null;
    pActiveToolsetNames = null;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached work groups used to determine the scope of administrative privileges or 
   * <CODE>null</CODE> if an operation which modifies the work groups has been called since
   * the cache was last updated.
   */ 
  private WorkGroups  pWorkGroups; 

  /**
   * The cached details of the administrative privileges granted to the current user or 
   * <CODE>null</CODE> if an operation which modifies the privileges has been called since
   * the cache was last updated.
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 

  /**
   * The cached name of the default toolset or <CODE>null</CODE> if an operation which 
   * modifies the default toolset name has been the cache was last updated.
   */ 
  private String  pDefaultToolsetName; 
  
  /**
   * The cached name of the active toolsets or <CODE>null</CODE> if an operation which 
   * modifies the active toolset names has been the cache was last updated.
   */ 
  private TreeSet<String> pActiveToolsetNames;
}
