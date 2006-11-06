// $Id: WorkGroups.java,v 1.4 2006/11/06 00:58:33 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   W O R K   G R O U P S                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Arbitrary non-exclusive groupings of Pipeline users. <P> 
 * 
 * Work groups define the scope of the Node Manager and Queue Manager privileges. A user 
 * with one of these manager privileges is allowed to perform operations on all nodes or jobs 
 * owned by any user which is a member of one or more of their work groups.  For instance, 
 * a Queue Manager would be allowed to Preept jobs of other users in their work group. <P> 
 * 
 * Users can belong to more than one work group.   
 * 
 * @see Privileges
 */
public
class WorkGroups
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new work group.
   */ 
  public 
  WorkGroups() 
  {
    pUserIDs    = new TreeMap<String,Integer>();
    pUserNames  = new TreeMap<Integer,String>();
    pNextUserID = 1; 
 
    pGroupIDs    = new TreeMap<String,Integer>();
    pGroupNames  = new TreeMap<Integer,String>();
    pNextGroupID = 1; 

    pUserGroups = new TreeMap<Integer,TreeSet<Integer>>();
    pGroupUsers = new TreeMap<Integer,TreeSet<Integer>>();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether a user with the given name exists.
   * 
   * @param uname
   *   The unique name of the user.
   */ 
  public boolean
  isUser
  (
   String uname
  ) 
  {
    return pUserIDs.containsKey(uname);
  }

  /**
   * Get the names of all users. 
   */ 
  public Set<String> 
  getUsers()
  {
    return Collections.unmodifiableSet(pUserIDs.keySet());
  }

  /**
   * Add a new user.
   * 
   * @param uname
   *   The unique name of the user.
   */ 
  public void 
  addUser
  (
   String uname
  )
  {
    if(pUserIDs.containsKey(uname)) 
      return;
    
    pUserIDs.put(uname, pNextUserID);
    pUserNames.put(pNextUserID, uname);
    pNextUserID++;

    renumber();
  }

  /**
   * Remove an existing user. 
   * 
   * @param uname
   *   The unique name of the user.
   */ 
  public void 
  removeUser
  ( 
   String uname
  )
  {
    Integer uid = pUserIDs.remove(uname);
    if(uid == null) 
      return;

    pUserNames.remove(uid);

    TreeSet<Integer> gids = pUserGroups.remove(uid);
    if(gids != null) {
      for(Integer gid : gids) {
	int sign = (gid > 0) ? 1 : -1; 

	TreeSet<Integer> uids = pGroupUsers.get(sign * gid);
	uids.remove(sign * uid);

	if(uids.isEmpty()) 
	  pGroupUsers.remove(sign * gid); 
      }
    }
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether a group with the given name exists.
   * 
   * @param gname
   *   The unique name of the group.
   */ 
  public boolean
  isGroup
  (
   String gname
  ) 
  {
    return pGroupIDs.containsKey(gname);
  }

  /**
   * Get the names of all groups. 
   */ 
  public Set<String> 
  getGroups()
  {
    return Collections.unmodifiableSet(pGroupIDs.keySet());
  }

  /**
   * Add a new group.
   * 
   * @param gname
   *   The unique name of the group.
   */ 
  public void 
  addGroup
  (
   String gname
  )
  {
    if(pGroupIDs.containsKey(gname)) 
      return;
    
    pGroupIDs.put(gname, pNextGroupID);
    pGroupNames.put(pNextGroupID, gname);
    pNextGroupID++;

    renumber();
  }

  /**
   * Remove an existing group. 
   * 
   * @param gname
   *   The unique name of the group.
   */ 
  public void 
  removeGroup
  ( 
   String gname 
  )
  {
    Integer gid = pGroupIDs.remove(gname);
    if(gid == null) 
      return;

    pGroupNames.remove(gid);

    TreeSet<Integer> uids = pGroupUsers.remove(gid);
    if(uids != null) {
      for(Integer uid : uids) {
	int sign = (uid > 0) ? 1 : -1; 

	TreeSet<Integer> gids = pUserGroups.get(sign * uid);
	gids.remove(sign * gid);

	if(gids.isEmpty()) 
	  pUserGroups.remove(sign * uid); 
      }
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether a user is a member or manager of a particular group.
   * 
   * @param uname
   *   The unique name of the user.
   * 
   * @param gname
   *   The unique name of the group.
   * 
   * @return 
   *   Returns <CODE>false</CODE> if member, <CODE>true</CODE> if manager or 
   *   <CODE>null</CODE> not a member or manager of the group.
   */
  public Boolean 
  isMemberOrManager
  (
   String uname, 
   String gname
  )
  {
    Integer uid = pUserIDs.get(uname);
    if(uid == null) 
      return null; 

    Integer gid = pGroupIDs.get(gname);
    if(gid == null) 
      return null; 
      
    TreeSet<Integer> gids = pUserGroups.get(uid);
    if(gids != null) {
      if(gids.contains(gid)) 
	return false;
      else if(gids.contains(-gid))
	return true;
    }
    
    return null;	
  }

  /**
   * Change whether a user is a member or manager of a particular group.
   * 
   * @param uname
   *   The unique name of the user.
   * 
   * @param gname
   *   The unique name of the group.
   * 
   * @param isMemberOrManager
   *   Set to <CODE>false</CODE> if member, <CODE>true</CODE> if manager or 
   *   <CODE>null</CODE> not a member or manager of the group.
   */ 
  public void 
  setMemberOrManager
  ( 
   String uname, 
   String gname, 
   Boolean isMemberOrManager
  )
  {
    Integer uid = pUserIDs.get(uname);
    if(uid == null) 
      return;

    Integer gid = pGroupIDs.get(gname);
    if(gid == null) 
      return;
    
    TreeSet<Integer> gids = pUserGroups.get(uid);
    TreeSet<Integer> uids = pGroupUsers.get(gid);
    if(isMemberOrManager != null) {
      if(gids == null) {
	gids = new TreeSet<Integer>(); 
	pUserGroups.put(uid, gids);
      }
      
      if(isMemberOrManager) {
	gids.add(-gid);
	gids.remove(gid);
      }
      else {
	gids.add(gid);
	gids.remove(-gid);
      }
      
      if(uids == null) {
	uids = new TreeSet<Integer>();
	pGroupUsers.put(gid, uids);
      }

      if(isMemberOrManager) {
	uids.add(-uid);
	uids.remove(uid);
      }
      else {
	uids.add(uid);
	uids.remove(-uid);
      }
    }
    else {
      if(gids != null) {
	gids.remove(gid); 
	gids.remove(-gid); 

	if(gids.isEmpty()) 
	  pUserGroups.remove(uid);
      }
      
      if(uids != null) {
	uids.remove(uid); 
	uids.remove(-uid); 

	if(uids.isEmpty()) 
	  pGroupUsers.remove(gid);
      }      
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether a user is a manager in one or more group in which another user is 
   * a member (or manager). <P>
   * 
   * If the manager and member user names are idential, this method will return
   * <CODE>true</CODE> regardless if the user belongs to any groups.
   * 
   * @param manager
   *   The unique name of the manager user. 
   * 
   * @param member
   *   The unique name of the member user. 
   */ 
  public boolean
  isManagedUser
  (
   String manager,
   String member
  ) 
  {
    if(manager.equals(member)) 
      return true;

    Integer uid1 = pUserIDs.get(manager);
    Integer uid2 = pUserIDs.get(member);
    if((uid1 != null) && (uid2 != null)) {
      TreeSet<Integer> gids = pUserGroups.get(uid1);
      if(gids != null) {
	for(Integer gid : gids) {
	  if(gid < 0) {
	    TreeSet<Integer> uids2 = pGroupUsers.get(-gid);
	    if(uids2.contains(uid2) || uids2.contains(-uid2))
	      return true; 
	  }
	}
      }
    }

    return false; 
  }

  /**
   * Get the names of the users which are members (or managers) of at least one group 
   * for which the given user is a manager. <P> 
   * 
   * @param manager
   *   The unique name of the user. 
   * 
   * @return 
   *   The managed user names (does not include the manager user).
   */ 
  public Set<String> 
  getManagedUsers
  (
   String manager
  ) 
  {
    TreeSet<String> users = new TreeSet<String>();

    Integer uid = pUserIDs.get(manager);
    if(uid != null) {
      TreeSet<Integer> suids = new TreeSet<Integer>(); 
      TreeSet<Integer> gids = pUserGroups.get(uid);
      if(gids != null) {
	for(Integer gid : gids) {
	  if(gid < 0) {
	    for(Integer suid : pGroupUsers.get(-gid))
	      suids.add(Math.abs(suid));
	  }
	}
      }
      
      for(Integer suid : suids) {
	if(!suid.equals(uid)) 
	  users.add(pUserNames.get(suid));
      }
    }
    
    return users;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    renumber();

    if(!pUserIDs.isEmpty()) 
      encoder.encode("UserIDs", pUserIDs);

    if(!pGroupIDs.isEmpty()) 
      encoder.encode("GroupIDs", pGroupIDs);

    if(!pUserGroups.isEmpty()) 
      encoder.encode("UserGroups", pUserGroups);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    {
      TreeMap<String,Integer> ids = (TreeMap<String,Integer>) decoder.decode("UserIDs"); 
      if(ids != null) {
	pUserIDs = ids;

	for(String uname : pUserIDs.keySet())
	  pUserNames.put(pUserIDs.get(uname), uname);

	pNextUserID = pUserNames.lastKey() + 1;
      }
    }

    {
      TreeMap<String,Integer> ids = (TreeMap<String,Integer>) decoder.decode("GroupIDs"); 
      if(ids != null) {
	pGroupIDs = ids;

	for(String gname : pGroupIDs.keySet())
	  pGroupNames.put(pGroupIDs.get(gname), gname);

	pNextGroupID = pGroupNames.lastKey() + 1;
      }
    }

    {
      TreeMap<Integer,TreeSet<Integer>> ugs = 
	(TreeMap<Integer,TreeSet<Integer>>) decoder.decode("UserGroups");
      if(ugs != null) {
	pUserGroups = ugs;

	for(Integer uid : pUserGroups.keySet()) {
	  for(Integer gid : pUserGroups.get(uid)) {
	    int sign = (gid > 0) ? 1 : -1; 

	    TreeSet<Integer> uids = pGroupUsers.get(sign * gid); 
	    if(uids == null) {
	      uids = new TreeSet<Integer>();
	      pGroupUsers.put(sign * gid, uids);
	    }

	    uids.add(sign * uid);
	  }
	}
      }
    }    

    renumber();
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Renumber user and group IDs so that the IDs are contiguous starting with 1.
   */ 
  private void 
  renumber() 
  {
    /* skip this unless Integer precision is about to be exceeded */ 
    if((pNextUserID < Integer.MAX_VALUE) && (pNextGroupID < Integer.MAX_VALUE))
      return;

    TreeMap<Integer,Integer> uidMap = new TreeMap<Integer,Integer>(); 
    {
      pNextUserID = 1; 
      pUserNames.clear();
      for(String uname : pUserIDs.keySet()) {
	uidMap.put(pUserIDs.get(uname), pNextUserID);
	pUserIDs.put(uname, pNextUserID);
	pUserNames.put(pNextUserID, uname);
	pNextUserID++;
      }
    }

    TreeMap<Integer,Integer> gidMap = new TreeMap<Integer,Integer>(); 
    {
      pNextGroupID = 1; 
      pGroupNames.clear();
      for(String gname : pGroupIDs.keySet()) {
	gidMap.put(pGroupIDs.get(gname), pNextGroupID);
	pGroupIDs.put(gname, pNextGroupID);
	pGroupNames.put(pNextGroupID, gname);
	pNextGroupID++;
      }
    }

    {
      TreeMap<Integer,TreeSet<Integer>> oldUGs = pUserGroups; 
      pUserGroups = new TreeMap<Integer,TreeSet<Integer>>();
      for(Integer ouid : oldUGs.keySet()) {
	Integer uid = uidMap.get(ouid);

	TreeSet<Integer> ogids = oldUGs.get(ouid);

	TreeSet<Integer> gids = new TreeSet<Integer>();
	pUserGroups.put(uid, gids);

	for(Integer ogid : ogids) 
	  gids.add(gidMap.get(ogid) * ((ogid > 0) ? 1 : -1));
      }
    }

    {
      TreeMap<Integer,TreeSet<Integer>> oldGUs = pGroupUsers; 
      pGroupUsers = new TreeMap<Integer,TreeSet<Integer>>();
      for(Integer ogid : oldGUs.keySet()) {
	Integer gid = gidMap.get(ogid);

	TreeSet<Integer> ouids = oldGUs.get(ogid);

	TreeSet<Integer> uids = new TreeSet<Integer>();
	pGroupUsers.put(gid, uids);

	for(Integer ouid : ouids) 
	  uids.add(uidMap.get(ouid) * ((ouid > 0) ? 1 : -1));
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3015318589674607126L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The IDs of defined users indexed by user name.
   */
  private TreeMap<String,Integer>  pUserIDs;  

  /**
   * The names of users indexed by user ID. <P> 
   */ 
  private TreeMap<Integer,String>  pUserNames; 

  /**
   * The ID to assign the next new user added.
   */ 
  private int pNextUserID;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The IDs of defined groups indexed by group name.
   */
  private TreeMap<String,Integer>  pGroupIDs; 

  /**
   * The names of groups indexed by group ID. <P> 
   */ 
  private TreeMap<Integer,String>  pGroupNames; 

  /**
   * The ID to assign the next new group added.
   */ 
  private int pNextGroupID;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The IDs of groups of which a user is a member (group ID > 0) or manager (group ID < 0) 
   * indexed by user ID. <P> 
   */ 
  private TreeMap<Integer,TreeSet<Integer>>  pUserGroups; 

  /**
   * The IDs of users which are members (user ID > 0) or managers (user ID < 0) of a group
   * indexed by group ID. <P> 
   */ 
  private TreeMap<Integer,TreeSet<Integer>>  pGroupUsers; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Note that the IDs for users and groups above are arbitrary and have no correspondence 
   * to the Unix UIDs or GIDs.  
   */ 
}
