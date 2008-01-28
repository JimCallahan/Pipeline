package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M U L T I   M A P                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A recursive data-structure that can contain a leaf value at each level.
 * <p>
 * There are two different ways that values can be put into this data structure. These are
 * controlled by the variable leafOnly which can be passed into any of the putValue methods.
 * If this variable is set to <code>true</code> then values can only be stored in leaf
 * nodes. Any attempt to put a value in to the data structure in a way that would violate that
 * requirement or an attempt to add a value to a branch of the structure that already violates
 * that structure will result in an error being thrown.
 * <p>
 * For example, if a value is added to the keys <code>first,second,third</code> and then a
 * value is added to the keys <code>first,second,third,fourth</code> with the
 * <code>leafOnly</code> flag turned on, an exception will be thrown when the putValue
 * methods finds the value at <code>third</code>.
 * <p>
 * This is built on top of the {@link ListMap} data structure, so it provides an ordered
 * mapping.
 * <p>
 * @param <K> The key type.
 * @param <V> The value type.
 */
public 
class MultiMap<K, V>
  extends ListMap<K, MultiMap<K, V>>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct an empty map.
   */ 
  public 
  MultiMap()
  {
    super();
    pLeafValue = null;
  }
  
  public MultiMap
  (
    V value
  )
  {
    super();
    pLeafValue = value;
  }
  
  public MultiMap
  (
    MultiMap<K, V> mmap
  )
  {
    super();
    putAll(mmap);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Sets the leaf value of the current MultiMap node.
   * <p>
   * @param leafValue The new value.
   * @return The old value, if any, of the MultiMap node.
   */
  public V
  setLeafValue
  (
    V leafValue
  )
  {
    V toReturn = pLeafValue;
    pLeafValue = leafValue;
    return toReturn;
  }
  
  /**
   * Gets the leaf value of the current MultiMap node.
   */
  public V
  getLeafValue()
  {
    return pLeafValue;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  public V 
  putValue
  (
    K key,
    V value,
    boolean leafOnly
  ) 
  {
    return putValue(listFromObject(key), value, leafOnly);
  }
  
  public V 
  putValue
  (
    K key,
    V value
  )
  {
   return putValue(key, value, false); 
  }
  
  @SuppressWarnings("null")
  public V
  putValue
  (
    List<K> keys,
    V value,
    boolean leafOnly
  )
  {
    validateKeys(keys);
    MultiMap<K, V> old = this;
    MultiMap<K, V> current = null;
    for (K key : keys) {
      current = old.get(key);
      if (current == null) {
	current = new MultiMap<K, V>();
	old.put(key, current);
      }
      if (old.hasLeafValue() && leafOnly)
	throw new IllegalArgumentException
	  ("Attempting to add an entry on a branch that already has a value with " +
	   "the leafOnly flag turned on.");
      old = current;
    }
    if (!current.isEmpty() && leafOnly) 
      throw new IllegalArgumentException
        ("Attempting to add an entry to a non-leaf node with the leafOnly flag turned on.");
    return current.setLeafValue(value);
  }
  
  public V
  putValue
  (
    List<K> keys,
    V value
  )
  {
    return putValue(keys, value, false);
  }
  
  /**
   * Inserts all the of key/value mappings from the given map into this map.
   * 
   * @param tmap
   *   The map to insert.
   */ 
  public void
  putAll
  (
   MultiMap<K,V> tmap
  )  
  {
    pLeafValue = tmap.pLeafValue;
    
    for (K key : tmap.keySet()) {
      MultiMap<K, V> entry = tmap.get(key);
      LinkedList<K> keys = listFromObject(key);
      putAllHelper(entry, keys);
    }
  }
  
  private void
  putAllHelper
  (
    MultiMap<K,V> source,
    LinkedList<K> keys 
  )
  {
    putValue(keys, source.pLeafValue);
    for (K key : source.keySet()) {
      MultiMap<K, V> entry = source.get(key);
      LinkedList<K> newKeys = new LinkedList<K>(keys);
      newKeys.add(key);
      putAllHelper(entry, newKeys);
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  public V
  getValue
  (
    List<K> keys
  )
  {
    return getMap(keys).getLeafValue();
  }
  
  public V 
  getValue
  (
    K key
  ) 
  {
    return getValue(listFromObject(key));
  }

  /*----------------------------------------------------------------------------------------*/
  
  public MultiMap<K, V>
  getMap
  (
    List<K> keys
  )
  {
    validateKeys(keys);
    MultiMap<K, V> old = this;
    MultiMap<K, V> current = null;
    for (K key : keys) {
      current = old.get(key);
      if (current == null)
	return null;
      old = current;
    }
    return current;
  }
  
  public MultiMap<K, V>
  getMap
  (
    K key
  ) 
  {
    return getMap(listFromObject(key));
  }
  
  /*----------------------------------------------------------------------------------------*/

  @SuppressWarnings("null")
  public V
  removeValue
  (
    List<K> keys
  )
  {
    validateKeys(keys);
    MultiMap<K, V> old = this;
    MultiMap<K, V> current = null;
    for (K key : keys) {
      current = old.get(key);
      if (current == null)
	return null;
      old = current;
    }
    V toReturn = current.getLeafValue();
    current.setLeafValue(null);
    pruneTree(keys);
    return toReturn;
  }
  
  public V
  removeValue
  (
    K key
  )
  {
    return removeValue(listFromObject(key));  
  }
  
  /*----------------------------------------------------------------------------------------*/

  public boolean
  isLeaf()
  {
   if (pLeafValue != null && size() == 0)
     return true;
   return false;
  }
  
  public boolean
  isBranch()
  {
   if (size() > 0)
     return true;
   return false;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  public boolean
  hasLeafValue()
  {
    if (pLeafValue == null)
      return false;
    return true;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P R I V A T E   M E T H O D S                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  private void
  validateKeys
  (
    List<K> keys
  )
  {
    if (keys == null || keys.isEmpty())
      throw new IllegalArgumentException
      ("The list of keys passed to methods in multiMap must be instantiated " +
       "and contain at least one key.");
    
    for (K key : keys) {
      if (key == null)
	throw new IllegalArgumentException("MultiMap does not support null keys");   
    }
   
  }
  
  private void
  pruneTree
  (
    List<K> keys
  )
  {
    LinkedList<K> newKeys = new LinkedList<K>(keys);
    K key = newKeys.poll();
    MultiMap<K,V> entry = get(key);
    if (newKeys.size() > 0) 
      entry.pruneTree(newKeys);
    if (!entry.hasLeafValue() && entry.isEmpty())
      remove(key);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   D E B U G                                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  public void
  printMapValues()
  {
    System.out.println();
    System.out.println("Printing MultiMap");
    String start = "root";
    if (hasLeafValue())
      System.out.println(getLeafValue() + "\t" + start);
    for (K key : keySet()) {
      MultiMap<K, V> entry = get(key);
      entry.printHelper(start + "," + key.toString());
    }
    System.out.println("Done Printing MultiMap");
    System.out.println();
  }
  
  private void
  printHelper
  (
    String start
  )
  {
    if (hasLeafValue())
      System.out.println(getLeafValue() + "\t" + start);
    for (K key : keySet()) {
      MultiMap<K, V> entry = get(key);
      entry.printHelper(start + "," + key.toString());
    }
  }

  public void
  printMap()
  {
    System.out.println();
    System.out.println("Printing MultiMap");
    String start = "root";
    System.out.println(getLeafValue() + "\t" + start);
    for (K key : keySet()) {
      MultiMap<K, V> entry = get(key);
      entry.printHelper2(start + "," + key.toString());
    }
    System.out.println("Done Printing MultiMap");
    System.out.println();
  }

  private void
  printHelper2
  (
    String start
  )
  {
    System.out.println(getLeafValue() + "\t" + start);
    for (K key : keySet()) {
      MultiMap<K, V> entry = get(key);
      entry.printHelper2(start + "," + key.toString());
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * 
   */
  public ArrayList<MultiMapEntry<K, V>> 
  entries()
  {
    ArrayList<MultiMapEntry<K, V>> toReturn = new ArrayList<MultiMapEntry<K,V>>();
    if (hasLeafValue()) {
      toReturn.add(new MultiMapEntry<K, V>(null, getLeafValue()));
    }
    for (K key : keySet() ) {
      LinkedList<K> keys = new LinkedList<K>();
      keys.add(key);
      get(key).buildEntries(toReturn, keys);
    }
    return toReturn;
  }
  
  private void
  buildEntries
  (
    ArrayList<MultiMapEntry<K, V>> store,
    LinkedList<K> keys
  )
  {
    if (hasLeafValue()) {
      store.add(new MultiMapEntry<K, V>(keys, getLeafValue()));
    }
    for (K key : keySet() ) {
      LinkedList<K> newKeys = new LinkedList<K>(keys);
      newKeys.add(key);
      get(key).buildEntries(store, newKeys);
    }
  }
  
  public ListMappedArrayList<K, MultiMapNamedEntry<K, V>>
  namedEntries()
  {
    ListMappedArrayList<K, MultiMapNamedEntry<K, V>> toReturn = 
      new ListMappedArrayList<K, MultiMapNamedEntry<K,V>>();
    for (K key : keySet() ) {
      LinkedList<K> keys = new LinkedList<K>();
      keys.add(key);
      get(key).buildNamedEntries(toReturn, keys);
    }
    return toReturn;
  }
  
  private void
  buildNamedEntries
  (
    ListMappedArrayList<K, MultiMapNamedEntry<K, V>> store,
    LinkedList<K> keys
  )
  {
    if (hasLeafValue()) {
      MultiMapNamedEntry<K, V> entry = new MultiMapNamedEntry<K, V>(keys, getLeafValue());
      store.put(entry.getName(), entry);
    }
    for (K key : keySet() ) {
      LinkedList<K> newKeys = new LinkedList<K>(keys);
      newKeys.add(key);
      get(key).buildNamedEntries(store, newKeys);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   U T I L I T Y   M E T H O D S                                          */
  /*----------------------------------------------------------------------------------------*/
  
  public static <E> LinkedList<E>
  listFromObject
  (
    E key
  )
  {
    LinkedList<E> list = new LinkedList<E>();
    list.add(key);
    return list;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3711029684706854838L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private V pLeafValue;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   C L A S S E S                                                          */
  /*----------------------------------------------------------------------------------------*/

  public static
  class MultiMapEntry<K, V>
  {
   
    public MultiMapEntry()
    {
      pValue = null;
      pKeys = null;
    }
    
    public MultiMapEntry
    (
      List<K> keys,
      V value
    )
    {
      pValue = value;
      if (keys == null)
	pKeys = null;
      else
	pKeys = new LinkedList<K>(keys);
    }
    
    public V
    getValue()
    {
      return pValue; 
    }
    
    public List<K>
    getKeys()
    {
      return Collections.unmodifiableList(pKeys);
    }
    
    private V pValue;
    private LinkedList<K> pKeys;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  public static
  class MultiMapNamedEntry<K, V>
  {
   
    public MultiMapNamedEntry()
    {
      pValue = null;
      pName = null;
      pKeys = new LinkedList<K>();
    }
    
    public MultiMapNamedEntry
    (
      List<K> keys,
      V value
    )
    {
      pValue = value; 
      if (keys == null)
	pKeys = null;
      else
	pKeys = new LinkedList<K>(keys);
      if (pKeys == null | pKeys.isEmpty())
	pName = null;
      else
	pName = pKeys.poll();
    }
    
    public MultiMapNamedEntry
    (
      List<K> keys,
      V value,
      K name
    )
    {
      pValue = value; 
      if (keys == null)
	pKeys = null;
      else
	pKeys = new LinkedList<K>(keys);
      pName = name;
    }
    
    public K
    getName()
    {
      return pName;
    }
    
    public V
    getValue()
    {
      return pValue; 
    }
    
    public List<K>
    getKeys()
    {
      return Collections.unmodifiableList(pKeys);
    }
    
    public boolean
    hasKeys()
    {
      if (pKeys == null || pKeys.isEmpty())
	return false;
      return true;
    }
    
    private K pName;
    private V pValue;
    private LinkedList<K> pKeys;
  }
}
