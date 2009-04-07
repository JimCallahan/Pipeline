// $Id: BaseGenerator.java,v 1.1 2009/04/07 06:01:20 jim Exp $

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   G E N E R A T O R                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Base class for Java conditional code generators.<P> 
 * 
 * For each cell read from the state spreadsheet flat text file, one of these generators is 
 * created.  They are assembled into a tree structure which merges shared states and when
 * completed mirrors the structure of the conditional tests which will be generated.
 */ 
public abstract
class BaseGenerator
  extends BaseCodeGen
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new code generator. 
   * 
   * @param key
   *   The value of the spreadsheet cell this represents.
   * 
   * @param policy
   *   The policy which determines legal values for this type of cell. 
   */ 
  public
  BaseGenerator
  (
   Comparable key, 
   CellPolicy policy
  ) 
    throws ParseException
  {
    if(key == null) 
      throw new ParseException
        ("The cell value cannot be (null)!");
    pCellKey = key;

    if(policy == null) 
      throw new ParseException
        ("The key generator cannot be (null)!"); 
    pCellPolicy = policy;

    pChildren = new TreeMap<Comparable,BaseGenerator>(); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the value of the spreadsheet cell associated with this generator.
   */
  public Comparable
  getCellKey() 
  {
    return pCellKey;
  }

  /**
   * Get the policy which determines legal values for this type of cell. 
   */
  public CellPolicy
  getCellPolicy() 
  {
    return pCellPolicy;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T E   T R E E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the cell values of all child generators.
   */ 
  public Set<Comparable> 
  getChildKeys() 
  {
    return Collections.unmodifiableSet(pChildren.keySet()); 
  }
   
  /**
   * Whether a child generator for the given cell value.
   * 
   * @param key
   *   The value of the spreadsheet cell associated with the child generator.
   */ 
  public boolean 
  childExists
  (
   Comparable key
  ) 
  {
    return pChildren.containsKey(key);
  }
  
  /**
   * Get a child generator based on it cell value.
   * 
   * @param key
   *   The value of the spreadsheet cell associated with the child generator.
   * 
   * @return
   *   The child generator or <CODE>null</CODE> of none exists for that cell key.
   */ 
  public BaseGenerator
  getChild
  (
   Comparable key
  )
    throws ParseException
  {
    return pChildren.get(key);
  }
  
  /**
   * Get all child generators.
   * 
   * @return
   *   The child generator or <CODE>null</CODE> of none exists for that cell key.
   */ 
  public Collection<BaseGenerator> 
  getChildren() 
  {
    return Collections.unmodifiableCollection(pChildren.values());
  }
  
  /**
   * Validate whether the given column index from the spreadsheet matches those of the 
   * existing children and throw an exception if not.
   */
  public void 
  validateChildColumn
  (
   int col
  ) 
    throws ParseException 
  {
    if((pChildColumn != null) && (pChildColumn != col)) 
      throw new ParseException
        ("Attempting to add a child from column (" + col + "), yet existing children are " + 
         "from column (" + pChildColumn + ")!"); 
  }

  /**
   * Add a new child generator.<P> 
   * 
   * Attempting to add a child generator which replaced an existing child generator for
   * the same cell value will generate a runtime exception.
   * 
   * @param col
   *   The column index from the spreadsheet of the child being added.
   * 
   * @param gen
   *   The child generator. 
   */ 
  public void
  addChild
  (
   int col, 
   BaseGenerator gen
  ) 
    throws ParseException
  {
    validateChildColumn(col); 
    pChildColumn = col;

    if(gen == null) 
      throw new ParseException("The generator cannot be (null)!");

    Comparable key = gen.getCellKey();
    if(childExists(key))
       throw new ParseException
         ("Attempting to overwrite an existing child generator for the cell value " + 
          "(" + key + ")!"); 

    pChildren.put(key, gen); 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   C O D E   G E N E R A T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Recursively generate the Java source code to represent the current conditional case
   * and all subcases.
   * 
   * @param first
   *   Whether this is the first child generator processed by its parent generator.
   * 
   * @param last
   *   Whether this is the last child generator processed by its parent generator.
   * 
   * @param level 
   *   The indentation level for this block of generated Java source code.
   * 
   * @return 
   *   The generated Java source code.
   * 
   * @throws GenerateException
   *   If any potential problems are detected with the states being processed while
   *   generating the source code.
   */
  public String
  generate
  (
   boolean first, 
   boolean last, 
   int level 
  ) 
    throws GenerateException    
  {
    StringBuilder buf = new StringBuilder();

    buf.append(openScope(first, level)); 
  
    LinkedList<Comparable> missingKeys = null;
    if(!pChildren.isEmpty()) {
      BaseGenerator firstChild = pChildren.get(pChildren.firstKey());

      LinkedList<Comparable> ordered = null;
      try {
        ordered = firstChild.getCellPolicy().validateAndOrder(getChildKeys());
      }
      catch(GenerateException ex) {
        throw new GenerateException
          (toString() + "\n" + ex.getMessage()); 
      }

      Comparable firstKey = ordered.getFirst();
      Comparable lastKey  = ordered.getLast();
      for(Comparable key : ordered) {
        BaseGenerator child = pChildren.get(key);
        try {
          buf.append(child.generate(key.equals(firstKey), key.equals(lastKey), level+1));
        }
        catch(GenerateException ex) {
          throw new GenerateException
            (toString() + " -> " + ex.getMessage()); 
        }
      }
    }

    buf.append(closeScope(last, level)); 

    return buf.toString();
  }

  /**
   * Generate the code which begins the current conditional scope. 
   * 
   * @param first
   *   Whether this is the first child generator processed by its parent generator.
   * 
   * @param level 
   *   The indentation level for this block of generated Java source code.
   */ 
  public abstract String
  openScope
  (
   boolean first, 
   int level 
  ); 
    
  /**
   * Generate the code which ends the current conditional scope. 
   * 
   * @param last
   *   Whether this is the last child generator processed by its parent generator.
   * 
   * @param level 
   *   The indentation level for this block of generated Java source code.
   */ 
  public abstract String
  closeScope
  (
   boolean last, 
   int level 
  ); 
    
  /**
   * Generate code for the conditional expression being evaluated in the current scope.
   */ 
  public abstract String
  conditional();
    
  

  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    return "[" + pCellKey.toString() + "]"; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The string value of the spreadsheet cell associated with this generator used as a 
   * key for uniquely looking up test generators.
   */ 
  private Comparable pCellKey; 

  /**
   * The key generator for this type.
   */ 
  private CellPolicy pCellPolicy;

  /**
   * The column index from the spreadsheet of the child generators.
   */ 
  private Integer pChildColumn; 

  /**
   * The table of child generators indexed by cell key.
   */ 
  private TreeMap<Comparable,BaseGenerator> pChildren;

}



