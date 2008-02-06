package us.temerity.pipeline.builder;

import java.util.LinkedList;

/**
 * Represents the name of the utility.
 * <p>
 * It is defined as a hierarchical list of names, starting with its top-level parent and
 * descending down to the name of the current utility.
 */
public 
class PrefixedName
{
  /**
   * Builds a new name from a list of existing names, while appending a new name. 
   */
  public
  PrefixedName
  (
    LinkedList<String> prefixes,
    String name
  )
  {
    if (prefixes == null)
      pPrefixes = new LinkedList<String>();
    else
      pPrefixes = new LinkedList<String>(prefixes);
    if (name != null)
      pPrefixes.add(name);
  }
  
  /**
   * Creates a name with no prefixes.
   */
  public PrefixedName
  (
    String name
  )
  {
    pPrefixes = new LinkedList<String>();
    pPrefixes.add(name);
  }
  
  /**
   * Builds a new name from an existing {@link PrefixedName}, while appending a new name. 
   */
  public PrefixedName
  (
    PrefixedName prefixName,
    String name
  )
  {
    if (prefixName.pPrefixes == null)
      pPrefixes = new LinkedList<String>();
    else
      pPrefixes = new LinkedList<String>(prefixName.pPrefixes);
    if (name != null)
      pPrefixes.add(name);
  }

  /**
   * Copy Constructor.
   */
  public PrefixedName
  (
    PrefixedName prefixName
  )
  {
    if (prefixName.pPrefixes == null)
      pPrefixes = new LinkedList<String>();
    else
      pPrefixes = new LinkedList<String>(prefixName.pPrefixes);
  }
  
  @Override
  public String 
  toString()
  {
    StringBuilder toReturn = new StringBuilder();
    for (String each : pPrefixes) {
      if (toReturn.length() > 0)
        toReturn.append(" - ");
      toReturn.append(each);
    }
    return toReturn.toString();
  }
  
  @Override
  public boolean 
  equals
  (
    Object that
  )
  {
    if (!(that instanceof PrefixedName)) 
      return false;
    PrefixedName that1 = (PrefixedName) that;
    if (that1.toString().equals(this.toString()))
      return true;
    return false;
  }

  private LinkedList<String> pPrefixes;
}