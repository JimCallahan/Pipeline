// $Id: UserBalanceGroup.java,v 1.4 2009/12/11 18:56:32 jesse Exp $

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
    
    pDefaultValue = 0;
    pDefaultMaxShare = 0;
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
    
    pDefaultValue = group.pDefaultValue;
    pDefaultMaxShare = group.pDefaultMaxShare;
    pUserValues.putAll(group.getUserValues());
    pUserMaxShares.putAll(group.getUserMaxShare());
    pGroupValues.putAll(group.getGroupValues());
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
    pUserValues = new TreeMap<String, Integer>();
    pGroupValues = new TreeMap<String, Integer>();
    pGroupMaxShares = new TreeMap<String, Double>();
    pUserMaxShares = new TreeMap<String, Double>();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default value assigned to all users not listed in the map.
   */
  public int
  getDefaultValue()
  {
    return pDefaultValue;
  }
  
  /**
   * Set the default value that will be assigned to all users not listed in the map.
   * 
   * @throws IllegalArgumentException
   *   If the default value is less than zero.
   */
  public void
  setDefaultValue
  (
    int defaultValue
  )
  {
    if (!isValidValue(defaultValue))
      throw new IllegalArgumentException
        ("It is not valid to have the groups's default queue share be less than zero.");
    pDefaultValue = defaultValue;
  }
  
  /**
   * Check if an Integer value is a valid default share value.

   * @return
   *   <code>False</code> if defaultValue is <code>null</code> or less than zero.  
   *   Otherwise <code>True</code>. 
   */
  public static boolean
  isValidValue
  (
    Integer defaultValue  
  )
  {
    if (defaultValue == null || defaultValue < 0)
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
   * Get the map of users to their un-normalized share of the queue.
   */
  public Map<String, Integer>
  getUserValues()
  {
    return Collections.unmodifiableMap(pUserValues);
  }
  
  /**
   * Set the map of users and their un-normalized share of the queue.
   * 
   * @param userValues
   *   The map which will replace the current mapping of values or <code>null</code> to clear 
   *   all the existing mappings.
   *   
   * @throws IllegalArgumentException
   *   If a value less than zero is specified for a user's queue share.
   */
  public void
  setUserValues
  (
    Map<String, Integer> userValues  
  )
  {
    pUserValues.clear();
    if (userValues != null) {
      for (Integer i : userValues.values())
        if (!isValidValue(i))
          throw new IllegalArgumentException
            ("It is not valid to have a user's queue share be less than zero.");
      pUserValues.putAll(userValues);
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the map of groups to their un-normalized share of the queue.
   */
  public Map<String, Integer>
  getGroupValues()
  {
    return Collections.unmodifiableMap(pGroupValues);
  }
  
  /**
   * Set the map of groups and their un-normalized share of the queue.
   * 
   * @param groupValues
   *   The map which will replace the current mapping of values or <code>null</code> to clear 
   *   all the existing mappings.
   *   
   * @throws IllegalArgumentException
   *   If a value less than zero is specified for a group's queue share.
   */
  public void
  setGroupValues
  (
    Map<String, Integer> groupValues  
  )
  {
    pGroupValues.clear();
    if (groupValues != null) {
      for (Integer i : groupValues.values())
        if (!isValidValue(i))
          throw new IllegalArgumentException
            ("It is not valid to have a group's queue share be less than zero.");
      pGroupValues.putAll(groupValues);
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the map of users and the maximum percentage (represented as a Double between 0 and 1)
   * of the queue they should have.<p>
   * 
   * These values are added to the user values to calculate the final max percentage that a
   * user can have of the balance group.
   * 
   * @param userShares
   *   The map which will replace the current mapping of maximum or <code>null</code> to clear 
   *   all the existing mappings.
   *   
   * @throws IllegalArgumentException
   *   If a value less than zero or more than one is specified for a user's queue share.
   */
  public void
  setUserMaxShares
  (
    Map<String, Double> userShares  
  )
  {
    pUserMaxShares.clear();
    if (userShares != null) {
      for (Double i : userShares.values())
        if (!isValidMaxShare(i))
          throw new IllegalArgumentException
            ("It is not valid for a user's max share of the queue to be greater than 1 or " +
             "less than 0.");
      pUserMaxShares.putAll(userShares);
    }
  }
  
  /**
   * Get the map of users and the maximum percentage (represented as a Double between 0 and 1)
   * of the queue they should have.
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
   *   The map which will replace the current mapping of maximum or <code>null</code> to clear 
   *   all the existing mappings.
   *   
   * @throws IllegalArgumentException
   *   If a value less than zero or more than one is specified for a group's queue share.
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
            ("It is not valid for a group's max share of the queue to be greater than 1 or " +
             "less than 0.");
      pGroupMaxShares.putAll(groupShares);
    }
  }

  /**
   * Get the map of groups and the maximum percentage (represented as a Double between 0 
   * and 1) of the queue they should have.
   */
  public Map<String, Double>
  getGroupMaxShare()
  {
    return Collections.unmodifiableMap(pGroupMaxShares);
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get a map of the users to the actual percentage of the queue that they are entitled to
   * under the current set of user and group values.
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
  getNormalizedUserValues
  (
    WorkGroups wgroups
  )
  {
    DoubleOpMap<String> toReturn = new DoubleOpMap<String>();  
    double total = 0d;
    for (String group : wgroups.getGroups()) {
      if (pGroupValues.containsKey(group)) {
        TreeSet<String> users = wgroups.getUsersInGroup(group);
        double value = pGroupValues.get(group);
        if (value > 0d) {
          for (String user : users) {
            toReturn.apply(user, value);
            total += value;
          }
        }
      }
    }
    
    for (String user : wgroups.getUsers()) {
      Integer value = pUserValues.get(user);
      if (value == null && toReturn.get(user) == null)
        value = pDefaultValue;
      
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
  getFinalMaxShares
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
    
    encoder.encode("DefaultValue", pDefaultValue);
    
    encoder.encode("DefaultMaxShare", pDefaultMaxShare);
    
    if (!pUserValues.isEmpty())
      encoder.encode("UserValues", pUserValues);
    
    if (!pGroupValues.isEmpty())
      encoder.encode("GroupValues", pGroupValues);
    
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
    
    pDefaultValue = (Integer) decoder.decode("DefaultValue");
    
    pDefaultMaxShare = (Double) decoder.decode("DefaultMaxShare");
    
    {
      TreeMap<String, Integer> temp = 
        (TreeMap<String, Integer>) decoder.decode("UserValues");
      if (temp != null)
        pUserValues.putAll(temp);
    }
    
    {
      TreeMap<String, Integer> temp = 
        (TreeMap<String, Integer>) decoder.decode("GroupValues");
      if (temp != null)
        pGroupValues.putAll(temp);
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
  private int pDefaultValue;
  
  /**
   * Default max share assigned to all the users not listed in the map.
   */
  private double pDefaultMaxShare;
  
  /**
   * Mapping of user names to their un-normalized queue share.
   */
  private TreeMap<String, Integer> pUserValues;
  
  /**
   * Mapping of group names to their un-normalized queue share.
   */
  private TreeMap<String, Integer> pGroupValues;
  
  /**
   * Mapping of user names to the maximum share they should get of the queue.
   */
  private TreeMap<String, Double> pUserMaxShares;
  
  /**
   * Mapping of group names to the maximum share they should get of the queue.
   */
  private TreeMap<String, Double> pGroupMaxShares;
}
