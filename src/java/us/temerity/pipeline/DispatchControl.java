// $Id: DispatchControl.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   D I S P A T C H   C O N T R O L                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A named control for tuning the criteria that the dispatcher uses to assign a job to a 
 * given slot.
 */
public 
class DispatchControl
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
  DispatchControl()
  { 
    init();
  }
  
  /**
   * Construct a new Dispatcher control.
   * <p>
   * The criteria will be ordered in the Pipeline default ordering.
   * 
   * @param name
   *   The name of the control
   */
  public
  DispatchControl
  (
    String name
  )
  {
    super(name);
    init();
    
    pCriteria.addAll(DispatchCriteria.all());
  }
  
  /**
   * Construct a new Dispatcher control.
   * <p>
   * If any criteria are not included in the set, then they will be appended to the end of 
   * the set in the order they appear in the default Pipeline control.
   *
   * @param name
   *   The name of the control
   *
   * @param crits
   *   Cannot be <code>null</code> or contain a <code>null</code> entry.
   *   
   * @throws IllegalArgumentException
   *   If any of the above rules are broken.
   */
  public 
  DispatchControl
  (
    String name,
    Set<DispatchCriteria> crits
  )
  {
    super(name);
    init();
    
    setCriteria(crits);
  }
  
  /**
   * Copy constructor.
   */
  public 
  DispatchControl
  (
    DispatchControl control
  )
  {
    super(control.pName);
    init();
    
    setCriteria(control.pCriteria);
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the instance.
   */ 
  private void 
  init() 
  {
    pCriteria = new LinkedHashSet<DispatchCriteria>();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the Dispatch criteria.
   * <p>
   * If any criteria are not included in this list, then they will be appended to the end of 
   * the list in the order they appear in the default Pipeline control.
   * 
   * @param criteria
   *   Cannot be <code>null</code> or contain a <code>null</code> entry.
   *   
   * @throws IllegalArgumentException
   *   If any of the above rules are broken.
   */
  public void
  setCriteria
  (
    Set<DispatchCriteria> criteria
  )
  {
    if (criteria == null)
      throw new IllegalArgumentException("(criteria) cannot be null.");
    
    if (criteria.contains(null))
      throw new IllegalArgumentException("(criteria) cannot contain any (null) values.");
    
    pCriteria = new LinkedHashSet<DispatchCriteria>(criteria);
    
    for (DispatchCriteria crit : DispatchCriteria.values()) {
      if (!pCriteria.contains(crit))
        pCriteria.add(crit);
    }
  }
  
  /**
   * Get the criteria.
   */
  public Set<DispatchCriteria>
  getCriteria()
  {
    return Collections.unmodifiableSet(pCriteria);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C R I T E R I A                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the criteria that occupies the numbered position, with zero being the top criteria.
   */
  public DispatchCriteria
  getCriteria
  (
    int position  
  )
  {
    if (position >= pCriteria.size())
      throw new IllegalArgumentException
        ("There is no (" + position + ") entry in the Dispatch Control");
    return pCriteria.toArray(new DispatchCriteria[0])[position];
  }
  
  /**
   * Move the criteria up one slot in the control
   */
  public void
  moveUp
  (
    DispatchCriteria crit  
  )
  {
    TreeMap<Double, DispatchCriteria> rank = new TreeMap<Double, DispatchCriteria>();
    double i = 1d;
    for (DispatchCriteria c : pCriteria) {
      if (c == crit)
        rank.put(i - 1.5, c);
      else
        rank.put(i, c);
      i++;
    }
    pCriteria.clear();
    pCriteria.addAll(rank.values());
  }
  
  /**
   * Move the criteria down one slot in the control
   */
  public void
  moveDown
  (
    DispatchCriteria crit  
  )
  {
    TreeMap<Double, DispatchCriteria> rank = new TreeMap<Double, DispatchCriteria>();
    double i = 1d;
    for (DispatchCriteria c : pCriteria) {
      if (c == crit)
        rank.put(i + 1.5, c);
      else
        rank.put(i, c);
      i++;
    }
    pCriteria.clear();
    pCriteria.addAll(rank.values());
  }
  
  /**
   * Move the criteria to the top of the control
   */
  public void
  makeTop
  (
    DispatchCriteria crit  
  )
  {
    TreeMap<Double, DispatchCriteria> rank = new TreeMap<Double, DispatchCriteria>();
    double i = 1d;
    for (DispatchCriteria c : pCriteria) {
      if (c == crit)
        rank.put(0d, c);
      else
        rank.put(i, c);
      i++;
    }
    pCriteria.clear();
    pCriteria.addAll(rank.values());
  }
  
  /**
   * Move the criteria to the bottome of the control
   */
  public void
  makeBottom
  (
    DispatchCriteria crit  
  )
  {
    TreeMap<Double, DispatchCriteria> rank = new TreeMap<Double, DispatchCriteria>();
    double i = 1d;
    for (DispatchCriteria c : pCriteria) {
      if (c == crit)
        rank.put(Double.MAX_VALUE, c);
      else
        rank.put(i, c);
      i++;
    }
    pCriteria.clear();
    pCriteria.addAll(rank.values());
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
    
    if(!pCriteria.isEmpty()) 
      encoder.encode("Criteria", pCriteria);
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

    LinkedHashSet<DispatchCriteria> crits = 
      (LinkedHashSet<DispatchCriteria>) decoder.decode("Criteria"); 
    if(crits != null) 
      pCriteria = crits;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3528991030626249960L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The list of criteria that is used to determine how jobs are dispatched
   */ 
  private LinkedHashSet<DispatchCriteria>  pCriteria; 
}
