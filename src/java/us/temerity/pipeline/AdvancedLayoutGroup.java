
package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   A D V A N C E D   L A Y O U T   G R O U P                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Hierarchical grouping of parameters and presets associated with plugins which determine 
 * the layout of UI components broken down by columns.
 * <P>
 * When you initialize this class for the first time, it initialize the first column of the 
 * layout.  All subsequent actions require you to provide a column number to specify what
 * column is being modified.
 */
public 
class AdvancedLayoutGroup
  extends Described
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
  AdvancedLayoutGroup() 
  {
    pIsOpen      = new TreeMap<Integer, Boolean>();
    pEntries     = new TreeMap<Integer, LinkedList<String>>();
    pSubGroups   = new TreeMap<Integer, LinkedList<LayoutGroup>>();
    pColumnNames = new TreeMap<Integer, String>();
  }

  /**
   * Construct a new default layout group. <P> 
   * 
   * @param colName
   * 	The name of the first column to be added. 
   * @param isOpen
   *   Whether the group is initially open.
   */
  public 
  AdvancedLayoutGroup
  (
    String colName,
    boolean isOpen
  ) 
  {
    this("Root", "", colName, isOpen); 
  }

  /**
   * Construct a new layout group, initializing the first column.
   * 
   * @param name
   *   The name of the group.
   * @param desc 
   *   A short description of the layout group.
   * @param colName
   * 	The name of the first column to be added. 
   * @param isOpen
   *   Whether the group is initially open.
   */ 
  public 
  AdvancedLayoutGroup
  (
   String name,   
   String desc,
   String colName,
   boolean isOpen
  ) 
  {
    super(name, desc);
    
    pIsOpen = new TreeMap<Integer, Boolean>();
    pEntries   = new TreeMap<Integer, LinkedList<String>>();
    pSubGroups = new TreeMap<Integer, LinkedList<LayoutGroup>>();
    pColumnNames = new TreeMap<Integer, String>();
    
    pIsOpen.put(1, isOpen);
    pEntries.put(1, new LinkedList<String>());
    pSubGroups.put(1, new LinkedList<LayoutGroup>());
    pColumnNames.put(1, colName);
  }

  /**
   * Construct a new layout group which is a copy of the given group but with a 
   * new name and description.
   * 
   * @param name
   *   The name of the group.
   * 
   * @param desc 
   *   A short description of the layout group.
   * 
   * @param group
   *   The layout group to copy.
   */ 
  public 
  AdvancedLayoutGroup
  (
   String name,   
   String desc, 
   AdvancedLayoutGroup group
  ) 
  {
    super(name, desc);

    pIsOpen      = new TreeMap<Integer, Boolean>(group.pIsOpen);
    pEntries     = new TreeMap<Integer, LinkedList<String>>(group.pEntries);
    pSubGroups   = new TreeMap<Integer, LinkedList<LayoutGroup>>(group.pSubGroups);
    pColumnNames = new TreeMap<Integer, String>(group.pColumnNames); 
  }
  
  /**
   * Construct a new layout group which is a copy of the given group.

   * @param group
   *   The layout group to copy.
   */ 
  public 
  AdvancedLayoutGroup
  (
   AdvancedLayoutGroup group
  ) 
  {
    super(group.getName(), group.getDescription());

    pIsOpen      = new TreeMap<Integer, Boolean>(group.pIsOpen);
    pEntries     = new TreeMap<Integer, LinkedList<String>>(group.pEntries);
    pSubGroups   = new TreeMap<Integer, LinkedList<LayoutGroup>>(group.pSubGroups);
    pColumnNames = new TreeMap<Integer, String>(group.pColumnNames); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets a modified form of the name of the group with spaces inserted between 
   * each word. <P> 
   * 
   * This name is used in the UI as the title of grouping components.
   * 
   * @see #getName
   */ 
  public String
  getNameUI()
  {
    StringBuilder buf = new StringBuilder();
    char c[] = getName().toCharArray();
    int wk;
    buf.append(c[0]);
    for(wk=1; wk<(c.length-1); wk++) {
      if(Character.isUpperCase(c[wk]) && 
	 (Character.isLowerCase(c[wk-1]) ||
	  Character.isLowerCase(c[wk+1])))
	  buf.append(" ");

      buf.append(c[wk]);
    }
    buf.append(c[wk]);

    return (buf.toString());
  }
  
  /**
   * Gets a modified form of the name of the column with spaces inserted between 
   * each word. <P> 
   * 
   * This name is used in the UI as the title of column.
   * 
   * @see #getName
   */ 
  public String
  getColumnNameUI(Integer col)
  {
    validateColumn(col);
    StringBuilder buf = new StringBuilder();
    char c[] = pColumnNames.get(col).toCharArray();
    int wk;
    buf.append(c[0]);
    for(wk=1; wk<(c.length-1); wk++) {
      if(Character.isUpperCase(c[wk]) && 
	 (Character.isLowerCase(c[wk-1]) ||
	  Character.isLowerCase(c[wk+1])))
	  buf.append(" ");

      buf.append(c[wk]);
    }
    buf.append(c[wk]);

    return (buf.toString());
  }

  /**
   * Adds a new column to the layout. <P>
   * 
   * This will create a new column that comes right after the last created column.
   * This method must be called before any attempts are made to write to a column
   * (except the first column, which is created by default).  There is no lazy
   * creation that happens in the getter/setter methods.
   * 
   * @param colName
   * 	The name of the column.
   * @param isOpen
   * 	Whether the group is initially open.
   * @return
   * 	An Integer that represents the column, needed for the getter and setter methods.
   */
  public Integer addColumn
  (
    String colName,
    boolean isOpen
  )
  {
    Integer newColumn = pIsOpen.lastKey() + 1;
    pColumnNames.put(newColumn, colName);
    pIsOpen.put(newColumn, isOpen);
    pEntries.put(newColumn, new LinkedList<String>());
    pSubGroups.put(newColumn, new LinkedList<LayoutGroup>());
    
    return newColumn;
  }
  
  /**
   * @return a map of all the entries in all the columns
   */
  public SortedMap<Integer, LinkedList<String>> getAllEntries()
  {
    return Collections.unmodifiableSortedMap(pEntries);
  }

  /**
   * @return a map of all the subgroups in all the columns
   */
  public SortedMap<Integer, LinkedList<LayoutGroup>> getAllSubGroups()
  {
    return Collections.unmodifiableSortedMap(pSubGroups);
  }

  /**
   * @return a map of all the isOpen parameters for all the columns
   */
  public SortedMap<Integer, Boolean> getAllIsOpen()
  {
    return Collections.unmodifiableSortedMap(pIsOpen);
  }

  /**
   * @return a map of the column names for all the columns
   */
  public SortedMap<Integer, String> getAllColumnNames()
  {
    return Collections.unmodifiableSortedMap(pColumnNames);
  }
  
  /**
   * @return a map of all the columns
   */
  public SortedSet<Integer> getAllColumns()
  {
    return Collections.unmodifiableSortedSet(new TreeSet<Integer>(pIsOpen.keySet()));
  }

  /**
   * @return the number of columns that have been initialized.
   */
  public int getNumberOfColumns()
  {
    return pIsOpen.size();
  }
  
  public boolean hasEntries()
  {
    Integer count = 0;
    for (LinkedList<String> params : pEntries.values())
    {
      count += params.size();
      if(count > 0)
	return true;
    }
    for (LinkedList<LayoutGroup> group: pSubGroups.values())
    {
      for (LayoutGroup layout : group)
      {
	count += layout.getEntries().size();
	if(count > 0)
	  return true;
      }
    }
    return false;
  }
  
  /**
   * Whether the group is initially open. <P> 
   * 
   * If the group is open, the UI components will be created in a state which exposes the 
   * parameter/preset fields.  Otherwise the group title will be shown but the paramter/preset
   * fields will be initially hidden.
   * 
   * @param col
   * 	Which column is being checked.
   */ 
  public boolean
  isOpen
  (
    Integer col
  ) 
  {
    validateColumn(col);
    return pIsOpen.get(col);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the parameters and/or presets in the order they should be 
   * displayed in the user interface. <P> 
   * 
   * The return list may contain <CODE>null</CODE> values to indicate the positions of 
   * parameter separators.
   * 
   * @param col
   * 	Which column is being queried.
   */ 
  public List<String>
  getEntries
  (
    Integer col
  ) 
  {
    validateColumn(col);
    return Collections.unmodifiableList(pEntries.get(col));
  }

  /**
   * Add the given parameter/preset name to the group. <P> 
   * 
   * @param col
   * 	The column to hold the parameter.
   * @param name
   *   The name of the parameter.
   */ 
  public void 
  addEntry
  (
    Integer col, 
    String name
  ) 
  {
    validateColumn(col);
    pEntries.get(col).add(name);
  }
  
  /**
   * Removes an entry from the layout.
   * <p>
   * This also performs some basic cleanup.  If the entry is sandwiched
   * between two <code>null</code> values, one of the two <code>null</code>
   * values will be removed, so that an extra gap is not created due to the 
   * removal of this entry.
   * @param name
   * 	The name of the entry to be removed.
   */
  public void
  removeEntry
  (
    String name
  )
  {
    if(name == null)
      throw new IllegalArgumentException("Cannot remove a null value from a layout.");
    for(LinkedList<String> list : pEntries.values()) {
      if(cleanList(name, list))
	return;
    }
    for(LinkedList<LayoutGroup> groups : pSubGroups.values()) {
      for(LayoutGroup group : groups) {
	if(cleanGroup(name, group))
	  return;
      }
    }
  }
  
  private boolean
  cleanGroup
  (
    String name,
    LayoutGroup group
  )
  {
    if(cleanList(name, group.getEntries()))
      return true;
    for(LayoutGroup sub : group.getSubGroups())
      if(cleanGroup(name, sub))
	return true;
    return false;
  }
  
  private boolean 
  cleanList
  (
    String name,
    List<String> list
  )
  {
    int i = 0;
    int max = list.size() - 1;
    for(String entry : list) {
      if(name.equals(entry)) {
	if(i != 0 && i != max) {
	  String prev = list.get(i - 1);
	  String next = list.get(i + 1);
	  if(prev == null && next == null)
	    list.remove(i + 1);
	  list.remove(i);
	}
	else if(i == 0) {
	 String next = list.get(i + 1);
	 if(next == null)
	   list.remove(i + 1);
	 list.remove(i);
	}
	else if(i == max) {
	  String prev = list.get(i - 1);
	  list.remove(i);
	  if(prev == null)
	    list.remove(i - 1);
	}
	return true;
      }
      i++;
    }
    return false;
  }
  
  /**
   * Adds the given parameter/preset name to the beginning of the group. <P> 

   * @param col
   * 	The column to hold the parameter.
   * @param name
   *   The name of the parameter.
   */ 
  public void 
  prependEntry
  (
    Integer col,
    String name
  ) 
  {
    validateColumn(col);
    pEntries.get(col).addFirst(name);
  }

  /**
   * Add a parameter/preset separator to the group. <P>
   * 
   * @param col
   * 	The column to hold the separator.
   */ 
  public void 
  addSeparator
  (
    Integer col
  ) 
  {
    validateColumn(col);
    pEntries.get(col).add(null);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the parameter subgroups.
   * 
   * @param col
   * 	Which column is being queried.
   */ 
  public List<LayoutGroup>
  getSubGroups
  (
    Integer col
  ) 
  {
    validateColumn(col);
    return Collections.unmodifiableList(pSubGroups.get(col));
  }

  /**
   * Add the given parameter subgroup to the group.
   * 
   * @param col
   * 	The column to hold the parameter.
   * @param group
   *   The parameter subgroup.
   */ 
  public void 
  addSubGroup
  (
    Integer col,
    LayoutGroup group
  ) 
  {
    validateColumn(col);
    pSubGroups.get(col).add(group);
  }

  /**
   * Add the given parameter subgroup to the beginning of the group.
   * 
   * @param col
   * 	The column to hold the parameter.
   * @param group
   *   The parameter subgroup.
   */ 
  public void 
  prependSubGroup
  (
    Integer col, 
    LayoutGroup group
  ) 
  {
    validateColumn(col);
    pSubGroups.get(col).addFirst(group);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Utility method to generate an exception when access to a non-existant column
   * is attempted.
   * 
   * @param col
   * 	Which column was the access attempted for.
   * @return
   * 	
   */
  private void validateColumn(Integer col)
  {
    if (pIsOpen.get(col) == null)
      throw new IllegalArgumentException
        ("There is no Layout associated with column ("+col+")" +
         " in the AdvancedLayoutGroup ("+getName()+")");
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
    super.toGlue(encoder);

    if(!pEntries.isEmpty()) 
      encoder.encode("Entries", pEntries);

    if(!pSubGroups.isEmpty())     
      encoder.encode("SubGroups", pSubGroups);
    
    if (!pColumnNames.isEmpty())
      encoder.encode("ColumnNames", pColumnNames);
    
    if (!pIsOpen.isEmpty())
      encoder.encode("IsOpen", pIsOpen);
  }

  @SuppressWarnings("unchecked")
  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);
    
    TreeMap<Integer, LinkedList<String>> names = (TreeMap<Integer, LinkedList<String>>) decoder.decode("Entries");
    if(names != null) 
      pEntries = names;

    TreeMap<Integer, LinkedList<LayoutGroup>> groups = (TreeMap<Integer, LinkedList<LayoutGroup>>) decoder.decode("SubGroups");
    if(groups != null) 
      pSubGroups = groups;
    
    TreeMap<Integer, String> columnNames = (TreeMap<Integer, String>) decoder.decode("ColumnNames");
    if(columnNames != null)
      pColumnNames = columnNames;
    
    TreeMap<Integer, Boolean> isOpen = (TreeMap<Integer, Boolean>) decoder.decode("IsOpen");
    if(isOpen != null)
      pIsOpen = isOpen;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -848297433224906986L;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the member parameters, broken down by pass.
   */ 
  private TreeMap<Integer, LinkedList<String>>  pEntries; 

  /**
   * The layout subgroups, broken down by pass.
   */ 
  private TreeMap<Integer, LinkedList<LayoutGroup>>  pSubGroups; 

  /**
   * Whether the group is initially open, broken down by pass.
   */ 
  private TreeMap<Integer, Boolean> pIsOpen;
  
  /**
   * The name of each column
   */
  private TreeMap<Integer, String> pColumnNames;
}
