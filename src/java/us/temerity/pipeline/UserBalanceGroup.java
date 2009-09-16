// $Id: UserBalanceGroup.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.Map.*;

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
  }
  
  /**
   * Copy constructor.
   */
  public 
  UserBalanceGroup
  (
    UserBalanceGroup group
  )
  {
    super(group.pName);
    init();
    
    pDefaultValue = group.pDefaultValue;
    pUserValues.putAll(group.getUserValues());
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the instance.
   */ 
  private void 
  init() 
  {
    pUserValues = new TreeMap<String, Integer>();
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
    if (defaultValue < 0)
      throw new IllegalArgumentException
        ("It is not value to have the groups's default queue share be less than zero.");
    pDefaultValue = defaultValue;
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
        if (i < 0)
          throw new IllegalArgumentException
            ("It is not value to have a user's queue share be less than zero.");
      pUserValues.putAll(userValues);
    }
  }
  
  /**
   * Get a map of the users to the actual percentage of the queue that they are entitled to 
   * under the current set of user values.
   * 
   * @param users
   *   The list of users that will be used to calculate the normalized values.  This list does 
   *   not need to coincide with all the users in this group.  Users who are in the group, but 
   *   not in the list will have their values ignored; users who are in the list but not in 
   *   the group will have the default value assigned to them.
   */
  public TreeMap<String, Double>
  getNormalizedUserValues
  (
    TreeSet<String> users  
  )
  {
    TreeMap<String, Double> toReturn = new TreeMap<String, Double>();  
    
    TreeMap<String, Integer> shares = new TreeMap<String, Integer>();
    double total = 0d;
    for (String user : users) {
      Integer share = pUserValues.get(user);
      if (share == null)
        share = pDefaultValue;
      total+= share;
      shares.put(user, share);
    }
    
    for (Entry<String, Integer> entry : shares.entrySet()) {
      if (total > 0d)
        toReturn.put(entry.getKey(), entry.getValue() / total);
      else
        toReturn.put(entry.getKey(), 0d);
    }
    return toReturn;
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
   * Mapping of user names to their un-normalized queue share.
   */
  private TreeMap<String, Integer> pUserValues;
}
