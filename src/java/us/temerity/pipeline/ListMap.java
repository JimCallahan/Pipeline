package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L I S T   M A P                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 *  A {@link Map} that preserves its entries in a user specified order.
 *  <p>
 *  By default, the ordering is based upon the order than things are added to
 *  the class.  There are also methods with support inserting entries at specific
 *  points in the Map.
 *  <p>
 *  This implementation is inherently slower than something like a {@link TreeMap}. 
 *  The very nature of the keySet in this implementation requires an O(n) cost search
 *  everytime something is inserted or a pValue is requested as opposed to the O(log(n)) 
 *  provided by {@link TreeMap}.  Therefore this class should only be used where the 
 *  ordering qualities it provides are necessary.
 *  <p>
 *  One other advantage of this class is that unlike the {@link TreeMap} it does not depend
 *  upon the pKey pValue implementing {@link Comparable}.
 *  
 *  @see Map
 *  @see TreeMap
 */
public 
class ListMap<K, V>
  extends AbstractMap<K,V>
  implements Map<K, V>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  ListMap()
  {
    pMap = new LinkedList<MapEntry<K,V>>();
  }
  
  public
  ListMap
  (
    Map<? extends K, ? extends V> m
  )
  {
    pMap = new LinkedList<MapEntry<K,V>>();
    putAll(m);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R F A C E   M E T H O D S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Removes all mappings from this Map.
   * 
   * @see java.util.Map#clear()
   */
  public void 
  clear()
  {
    modCount++;
    pMap.clear();
  }

  /**
   * Returns a set view of the mappings contained in this map.  The set's
   * iterator returns the mappings in ascending key order.  Each element in
   * the returned set is a <tt>Map.Entry</tt>.  The set is backed by this
   * map, so changes to this map are reflected in the set, and vice-versa.
   * The set supports element removal, which removes the corresponding
   * mapping from the ListMap, through the <tt>Iterator.remove</tt>,
   * <tt>Set.remove</tt>, and <tt>clear</tt> operations.  It does not support 
   * the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a set view of the mappings contained in this map.
   */
  public Set<java.util.Map.Entry<K, V>> 
  entrySet()
  {
    if (entrySet == null) {
      entrySet = new AbstractSet<Map.Entry<K,V>>() {
	public Iterator<Map.Entry<K,V>> iterator() {
	  return new EntryIterator() ;
	}

	@SuppressWarnings("unchecked")
	public boolean contains(Object o) {
	  if (!(o instanceof Map.Entry))
	    return false;
	  Map.Entry<K,V> entry = (Map.Entry<K,V>) o;
	  V value = entry.getValue();
	  Entry<K,V> p = getEntry(entry.getKey());
	  return p != null && (p.getValue().equals(value));
	}

	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
	  if (!(o instanceof Map.Entry))
	    return false;
	  Entry<K,V> entry = (Entry<K,V>) o;
	  V value = entry.getValue();
	  MapEntry<K,V> p = getEntry(entry.getKey());
	  if (p != null && (p.getValue().equals(value))) {
	    pMap.remove(p);
	    return true;
	  }
	  return false;
	}

	public int size() {
	  return ListMap.this.size();
	}

	public void clear() {
	  ListMap.this.clear();
	}
      };
    }
    return entrySet;
  }

  /**
   * Associates the specified value with the specified key in this map
   * (optional operation).  If the map previously contained a mapping for
   * this key, the old value is removed.  Note that this changes the order
   * of the Map.  The old value will be removed from whereever it existed
   * and the new value will be added at the end.  If a value needs to be
   * replaced, use the <code>replace</code> method.<p>
   *
   * @param key key with which the specified value is to be associated.
   * @param value value to be associated with the specified key.
   * 
   * @return previous value associated with specified key, or <tt>null</tt>
   *	       if there was no mapping for key.  (A <tt>null</tt> return can
   *	       also indicate that the map previously associated <tt>null</tt>
   *	       with the specified key, if the implementation supports
   *	       <tt>null</tt> values.)
   * 
   * @throws IllegalArgumentException if some aspect of this key or value *
   *            prevents it from being stored in this map.
   */
  public V 
  put
  (
    K key, 
    V value
  )
  {
    MapEntry<K, V> entry = new MapEntry<K, V>(key, value);
    
    V oldEntry = remove(key);
    modCount++;
    pMap.add(entry);
    return oldEntry;
  }
  
  /**
   * Removes the mapping for this key from this map if present (optional
   * operation). <p>
   *
   * This implementation iterates over <tt>entrySet()</tt> searching for an
   * entry with the specified key.  If such an entry is found, its value is
   * obtained with its <tt>getValue</tt> operation, the entry is removed
   * from the Collection (and the backing map) with the iterator's
   * <tt>remove</tt> operation, and the saved value is returned.  If the
   * iteration terminates without finding such an entry, <tt>null</tt> is
   * returned.  Note that this implementation requires linear time in the
   * size of the map; many implementations will override this method.<p>
   *
   * Note that this implementation throws an
   * <tt>UnsupportedOperationException</tt> if the <tt>entrySet</tt> iterator
   * does not support the <tt>remove</tt> method and this map contains a
   * mapping for the specified key.
   *
   * @param key key whose mapping is to be removed from the map.
   * @return previous value associated with specified key, or <tt>null</tt>
   *	       if there was no entry for key.  (A <tt>null</tt> return can
   *	       also indicate that the map previously associated <tt>null</tt>
   *	       with the specified key, if the implementation supports
   *	       <tt>null</tt> values.)
   */
  public V 
  remove
  (
    Object key
  )
  {
    modCount++;
    return super.remove(key);
  }

  /**
   * Returns the number of key-value mappings in this map.
   *  
   * @see java.util.Map#size()
   */
  public int 
  size()
  {
    return pMap.size();
  }

  /*----------------------------------------------------------------------------------------*/
  /*   P R I V A T E   M E T H O D S                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  @SuppressWarnings("unchecked")
  private MapEntry<K, V>
  getEntry
  (
    Object key
  )
  {
    K k = (K) key;
    for(MapEntry<K, V> entry : pMap) {
      if (k.equals(key))
	return entry;
    }
    return null;
  }

  private LinkedList<MapEntry<K,V>> pMap;
  
  private int modCount = 0;
  
  /**
   * This field is initialized to contain an instance of the entry set
   * view the first time this view is requested.  The view is stateless,
   * so there's no reason to create more than one.
   */
  private transient volatile Set<Map.Entry<K,V>> entrySet = null;
  
  private abstract 
  class PrivateEntryIterator<T> 
    implements Iterator<T> 
  {

    PrivateEntryIterator() 
    {
      intIterator = pMap.iterator();
    }

    public boolean 
    hasNext() 
    {
      return intIterator.hasNext();
    }

    final Entry<K,V> 
    nextEntry() 
    {
      if(modCount != expectedModCount)
	throw new ConcurrentModificationException();
      return intIterator.next();
    }

    public void 
    remove() 
    {
      if(modCount != expectedModCount)
	throw new ConcurrentModificationException();
      intIterator.remove();
    }
    
    private int expectedModCount = ListMap.this.modCount;
    private Iterator<MapEntry<K,V>> intIterator;
  }
  
  private 
  class EntryIterator 
    extends PrivateEntryIterator<Map.Entry<K,V>> 
  {
    public Map.Entry<K,V> 
    next() 
    {
      return nextEntry();
    }
  }
  
//  private 
//  class KeyIterator 
//    extends PrivateEntryIterator<K> 
//  {
//    public K 
//    next() 
//    {
//      return nextEntry().getKey();
//    }
//  }
//
//  private 
//  class ValueIterator 
//    extends PrivateEntryIterator<V> 
//  {
//    public V 
//    next() 
//    {
//      return nextEntry().getValue();
//    }
//  }
  
  @SuppressWarnings("hiding")
  class MapEntry<K,V> 
    implements Entry<K,V> {

    public 
    MapEntry
    (
      K key, 
      V value
    ) 
    {
      pKey   = key;
      pValue = value;
    }

    public 
    MapEntry
    (
      Entry<K,V> e
    ) 
    {
      pKey   = e.getKey();
      pValue = e.getValue();
    }

    /** 
     * Returns the key corresponding to this entry.
     * 
     * @see Map.Entry#getKey()
     */
    public K 
    getKey()
    {
      return pKey;
    }

    /** 
     * Returns the value corresponding to this entry.  If the mapping
     * has been removed from the backing pMap (by the iterator's
     * <tt>remove</tt> operation), the results of this call are undefined.
     * 
     * @see Map.Entry#getValue()
     */
    public V 
    getValue()
    {
      return pValue;
    }

    /**
     * Replaces the value corresponding to this entry with the specified
     * pValue (optional operation).  (Writes through to the pMap.)  The
     * behavior of this call is undefined if the mapping has already been
     * removed from the pMap (by the iterator's <tt>remove</tt> operation).
     * 
     * @see Map.Entry#setValue(Object)
     */
    public V 
    setValue
    (
      V value
    )
    {
      V old = this.pValue;
      this.pValue = value;
      return old;
    }
    
    
    /**
     * Compares the specified object with this entry for equality.
     * Returns <tt>true</tt> if the given object is also a pMap entry and
     * the two entries represent the same mapping.  More formally, two
     * entries <tt>e1</tt> and <tt>e2</tt> represent the same mapping
     * if<pre>
     *     (e1.getKey()==null ?
     *      e2.getKey()==null : e1.getKey().equals(e2.getKey()))  &&
     *     (e1.getValue()==null ?
     *      e2.getValue()==null : e1.getValue().equals(e2.getValue()))
     * </pre>
     * This ensures that the <tt>equals</tt> method works properly across
     * different implementations of the <tt>Map.Entry</tt> interface.
     *
     * @param o object to be compared for equality with this pMap entry.
     * @return <tt>true</tt> if the specified object is equal to this pMap
     *         entry.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean 
    equals
    (
      Object obj
    )
    {
      if (!(obj instanceof MapEntry))
	return false;
      MapEntry e = (MapEntry) obj;
      return checkEquality(pKey, e.getKey()) && checkEquality(pValue, e.getValue());
    }
    
    /**
     * Implementation of the requirement for equality for the Entry class
     */
    private boolean 
    checkEquality
    (
      Object o1, 
      Object o2
    )
    {
      return (o1 == null ? o2 == null : o1.equals(o2));
    }

    /**
     * String equivlent of the class
     */
    @Override
    public String 
    toString()
    {
      return pKey.toString() + "=" + pValue.toString();
    }

    @Override
    public int 
    hashCode()
    {
      int keyHash = pKey.hashCode();
      int valueHash = pValue.hashCode();
      return keyHash ^ valueHash;
    }

    private K pKey;
    private V pValue;
  }
}
