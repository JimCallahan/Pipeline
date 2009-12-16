// $Id: UserBalanceGroup.java,v 1.6 2009/12/16 04:13:33 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.BaseOpMap.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   U S E R   B A L A N C E   G R O U P                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A named queue control used to divide a group of machines among users and/or workgroups. 
 */
public 
class UserBalanceGroup
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
  UserBalanceGroup()
  {
    init();
  }
  
  /**
   * Construct a new User Balance Group.
   * <p>
   * The default value will be 0.
   * 
   * @param name
   *   The name of the group
   */
  public
  UserBalanceGroup
  (
    String name
  )
  {
    super(name);
    init();
    
    pDefaultBias = 0;
    pDefaultMaxShare = 1;
  }
  
  /**
   * Construct a new balance group with the same user values as the given group.
   * 
   * @param name
   *   The name of the new group.
   * 
   * @param group
   *   Copy user values from this group.
   */ 
  public 
  UserBalanceGroup
  (
    String name,
    UserBalanceGroup group
  )
  {
    super(name);
    init();
    
    pDefaultBias = group.pDefaultBias;
    pDefaultMaxShare = group.pDefaultMaxShare;
    pUserBiases.putAll(group.getUserBiases());
    pUserMaxShares.putAll(group.getUserMaxShare());
    pGroupBiases.putAll(group.getGroupBiases());
    pGroupMaxShares.putAll(group.getGroupMaxShare());
  }
  
  /**
   * Copy Constructor
   * 
   * @param group
   *   Balance Group to copy.
   */
  public
  UserBalanceGroup
  (
    UserBalanceGroup group  
  )
  {
    this(group.getName(), group);
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the instance.
   */ 
  private void 
  init() 
  {
    pUserBiases = new TreeMap<String, Integer>();
    pGroupBiases = new TreeMap<String, Integer>();
    pGroupMaxShares = new TreeMap<String, Double>();
    pUserMaxShares = new TreeMap<String, Double>();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default bias assigned to all users who do not have a user or group override.
   */
  public int
  getDefaultBias()
  {
    return pDefaultBias;
  }
  
  /**
   * Set the default bias that will be assigned to all users who do not have a 
   * user or group override.
   * 
   * @throws IllegalArgumentException
   *   If the default value is less than zero.
   */
  public void
  setDefaultBias
  (
    int defaultBias
  )
  {
    if (!isValidBias(defaultBias))
      throw new IllegalArgumentException
        ("It is not valid to have the balance groups's default bias be less than zero.");
    pDefaultBias = defaultBias;
  }
  
  /**
   * Check if an Integer value is a valid bias.

   * @return
   *   <code>False</code> if defaultBias is <code>null</code> or less than zero.  
   *   Otherwise <code>True</code>. 
   */
  public static boolean
  isValidBias
  (
    Integer defaultBias
  )
  {
    if (defaultBias == null || defaultBias < 0)
      return false;
    return true;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default max share value assigned to all users not listed in the map.
   */
  public double
  getDefaultMaxShare()
  {
    return pDefaultMaxShare;
  }
  
  /**
   * Set the default max share value that will be assigned to all users not listed in the map.
   * 
   * @throws IllegalArgumentException
   *   If the max share value is not between zero and one.
   */
  public void
  setDefaultMaxShare
  (
    double maxShare
  )
  {
    if (!isValidMaxShare(maxShare))
      throw new IllegalArgumentException
        ("It is not valid to have the groups's default max share be less than zero or " +
         "greater than one.");
    pDefaultMaxShare = maxShare;
  }
  
  /**
   * Check if a Double value is a valid default max share.

   * @return
   *   <code>False</code> if maxShare is <code>null</code> or less than zero or greater than 
   *   one.  Otherwise <code>True</code>. 
   */
  public static boolean
  isValidMaxShare
  (
    Double maxShare  
  )
  {
    if (maxShare == null || (maxShare < 0d || maxShare > 1d))
      return false;
    return true;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the map of users to their un-normalized biases.
   */
  public Map<String, Integer>
  getUserBiases()
  {
    return Collections.unmodifiableMap(pUserBiases);
  }
  
  /**
   * Set the map of users and their un-normalized biases.
   * 
   * @param userBiases
   *   The map which will replace the current mapping of biases or <code>null</code> to clear 
   *   all the existing mappings.
   *   
   * @throws IllegalArgumentException
   *   If a value less than zero is specified for a user's bias.
   */
  public void
  setUserBiases
  (
    Map<String, Integer> userBiases  
  )
  {
    pUserBiases.clear();
    if (userBiases != null) {
      for (Integer i : userBiases.values())
        if (!isValidBias(i))
          throw new IllegalArgumentException
            ("It is not valid to have a user's bias be less than zero.");
      pUserBiases.putAll(userBiases);
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the map of groups to their un-normalized bias.
   */
  public Map<String, Integer>
  getGroupBiases()
  {
    return Collections.unmodifiableMap(pGroupBiases);
  }
  
  /**
   * Set the map of groups and their un-normalized biases.
   * 
   * @param groupBiases
   *   The map which will replace the current mapping of biases or <code>null</code> to clear 
   *   all the existing mappings.
   *   
   * @throws IllegalArgumentException
   *   If a value less than zero is specified for a group's bias.
   */
  public void
  setGroupBiases
  (
    Map<String, Integer> groupBiases  
  )
  {
    pGroupBiases.clear();
    if (groupBiases != null) {
      for (Integer i : groupBiases.values())
        if (!isValidBias(i))
          throw new IllegalArgumentException
            ("It is not valid to have a group's bias be less than zero.");
      pGroupBiases.putAll(groupBiases);
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the map of users and the maximum percentage (represented as a Double between 0 and 1)
   * of the balance group they should have.<p>
   * 
   * These values are added to the group max shares to calculate the final max percentage 
   * that a user can have of the balance group.
   * 
   * @param userMaxShares
   *   The map which will replace the current mapping of max shares or <code>null</code> to 
   *   clear all the existing mappings.
   *   
   * @throws IllegalArgumentException
   *   If a value less than zero or more than one is specified for a user's max share.
   */
  public void
  setUserMaxShares
  (
    Map<String, Double> userMaxShares  
  )
  {
    pUserMaxShares.clear();
    if (userMaxShares != null) {
      for (Double i : userMaxShares.values())
        if (!isValidMaxShare(i))
          throw new IllegalArgumentException
            ("It is not valid for a user's max share of the balance group to be greater " +
             "than 1 or less than 0.");
      pUserMaxShares.putAll(userMaxShares);
    }
  }
  
  /**
   * Get the map of users and the maximum percentage (represented as a Double between 0 and 1)
   * of the balance group they should have.
   */
  public Map<String, Double>
  getUserMaxShare()
  {
    return Collections.unmodifiableMap(pUserMaxShares);
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the map of group and the maximum percentage (represented as a Double between 0 and 1)
   * of the balance group they should have. <p>
   * 
   * These values are added to the user values to calculate the final max percentage that a
   * user can have of the balance group.
   * 
   * @param groupShares
   *   The map which will replace the current mapping of max group shares or 
   *   <code>null</code> to clear all the existing mappings.
   *   
   * @throws IllegalArgumentException
   *   If a value less than zero or more than one is specified for a group's max shares.
   */
  public void
  setGroupMaxShares
  (
    Map<String, Double> groupShares  
  )
  {
    pGroupMaxShares.clear();
    if (groupShares != null) {
      for (Double i : groupShares.values())
        if (!isValidMaxShare(i))
          throw new IllegalArgumentException
            ("It is not valid for a group's max share of the balance group to be greater " +
             "than 1 or less than 0.");
      pGroupMaxShares.putAll(groupShares);
    }
  }

  /**
   * Get the map of groups and the maximum percentage (represented as a Double between 0 
   * and 1) of the balance group they should have.
   */
  public Map<String, Double>
  getGroupMaxShare()
  {
    return Collections.unmodifiableMap(pGroupMaxShares);
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a map of the users to the actual percentage of the balance group that they are 
   * entitled to under the current set of user and group biases.
   * <p>
   * The list of users and groups in the WorkGroups structure is considered definitive in
   * terms of calculating shares of queue. Users who are in the user balance group, but not in
   * the workgroup will have their values ignored; users who are in the workgroup but not in
   * the user balance group will have the default value assigned to them. Groups which are not
   * in the user balance group will have a value of zero assigned to them.
   * 
   * @param wgroups
   *   The list of users and groups that will be used to calculate the normalized values.
   * 
   * @return 
   *   A map of users to the share of the queue they should get under the current rules.
   */
  public DoubleOpMap<String>
  getCalculatedFairShares
  (
    WorkGroups wgroups
  )
  {
    DoubleOpMap<String> toReturn = new DoubleOpMap<String>();  
    double total = 0d;
    for (String group : wgroups.getGroups()) {
      if (pGroupBiases.containsKey(group)) {
        TreeSet<String> users = wgroups.getUsersInGroup(group);
        double value = pGroupBiases.get(group);
        if (value > 0d) {
          for (String user : users) {
            toReturn.apply(user, value);
            total += value;
          }
        }
      }
    }
    
    for (String user : wgroups.getUsers()) {
      Integer value = pUserBiases.get(user);
      if (value == null && toReturn.get(user) == null)
        value = pDefaultBias;
      
      if (value != null) {
        total += value;
        toReturn.apply(user, (double) value);
      }
    }
    
    TreeSet<String> users = new TreeSet<String>(toReturn.keySet());
    if (total > 0d) {
      for (String user : users)
        toReturn.apply(user, total, Op.Divide);
    }
    else {
      for (String user : users)
        toReturn.put(user, 0d);
    }
    
    return toReturn;
  }
  
  public DoubleOpMap<String>
  getCalculatedMaxShares
  (
    WorkGroups wgroups
  )
  {
    DoubleOpMap<String> toReturn = new DoubleOpMap<String>();
    for (String group : wgroups.getGroups()) {
      if (pGroupMaxShares.containsKey(group)) {
        TreeSet<String> users = wgroups.getUsersInGroup(group);
        double value = pGroupMaxShares.get(group);
        if (value >= 0d) {
          for (String user : users) {
            toReturn.apply(user, value);
          }
        }
      }
    }
    
    for (String user : wgroups.getUsers()) {
      Double value = pUserMaxShares.get(user);
      if (value == null && toReturn.get(user) == null)
        value = pDefaultMaxShare;
      
      if (value != null)
        toReturn.apply(user, (double) value);
      toReturn.apply(user, 1d, Op.Min);
      toReturn.apply(user, 0d, Op.Max);
    }
    
    return toReturn;
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
    
    encoder.encode("DefaultBias", pDefaultBias);
    
    encoder.encode("DefaultMaxShare", pDefaultMaxShare);
    
    if (!pUserBiases.isEmpty())
      encoder.encode("UserBiases", pUserBiases);
    
    if (!pGroupBiases.isEmpty())
      encoder.encode("GroupBiases", pGroupBiases);
    
    if (!pUserMaxShares.isEmpty())
      encoder.encode("UserMaxShares", pUserMaxShares);
    
    if (!pGroupMaxShares.isEmpty())
      encoder.encode("GroupMaxShares", pGroupMaxShares);
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
    
    {
      Object o = decoder.decode("DefaultBias");
      if (o == null)
        throw new GlueException("A UserBalanceGroup must contain a DefaultBias value");
      pDefaultBias = (Integer) o;
    }
    
    {
      Object o = decoder.decode("DefaultMaxShare");
      if (o == null)
        throw new GlueException("A UserBalanceGroup must contain a DefaultMaxShare value");
      pDefaultMaxShare = (Double) o;
    }
    
    {
      TreeMap<String, Integer> temp = 
        (TreeMap<String, Integer>) decoder.decode("UserBiases");
      if (temp != null)
        pUserBiases.putAll(temp);
    }
    
    {
      TreeMap<String, Integer> temp = 
        (TreeMap<String, Integer>) decoder.decode("GroupBiases");
      if (temp != null)
        pGroupBiases.putAll(temp);
    }
    
    {
      TreeMap<String, Double> temp = 
        (TreeMap<String, Double>) decoder.decode("UserMaxShares");
      if (temp != null)
        pUserMaxShares.putAll(temp);
    }
    
    {
      TreeMap<String, Double> temp = 
        (TreeMap<String, Double>) decoder.decode("GroupMaxShares");
      if (temp != null)
        pGroupMaxShares.putAll(temp);
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6905282685574499246L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Default value assigned to all users not listed in the map.
   */
  private int pDefaultBias;
  
  /**
   * Default max share assigned to all the users not listed in the map.
   */
  private double pDefaultMaxShare;
  
  /**
   * Mapping of user names to their un-normalized queue share.
   */
  private TreeMap<String, Integer> pUserBiases;
  
  /**
   * Mapping of group names to their un-normalized queue share.
   */
  private TreeMap<String, Integer> pGroupBiases;
  
  /**
   * Mapping of user names to the maximum share they should get of the queue.
   */
  private TreeMap<String, Double> pUserMaxShares;
  
  /**
   * Mapping of group names to the maximum share they should get of the queue.
   */
  private TreeMap<String, Double> pGroupMaxShares;
}
