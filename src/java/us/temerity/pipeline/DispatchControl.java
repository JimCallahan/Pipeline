// $Id: DispatchControl.java,v 1.2 2009/09/29 20:44:41 jesse Exp $

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
    
    setCriteria(new LinkedHashSet<DispatchCriteria>(control.pCriteria));
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the instance.
   */ 
  private void 
  init() 
  {
    pCriteria = new LinkedList<DispatchCriteria>();
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
    
    pCriteria = new LinkedList<DispatchCriteria>(criteria);
    
    for (DispatchCriteria crit : DispatchCriteria.values()) {
      if (!pCriteria.contains(crit))
        pCriteria.add(crit);
    }
  }
  
  /**
   * Get the criteria.
   */
  public List<DispatchCriteria>
  getCriteria()
  {
    return Collections.unmodifiableList(pCriteria);
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
   * 
   * @param position
   *   The position in the list of criteria to find the criteria to move.
   */
  public void
  moveUp
  (
    int position  
  )
  {
    if (position < 0 || position >= pCriteria.size())
      throw new IllegalArgumentException
        ("The position (" + position +") is not a valid position in a dispatch control");
    if (position == 0)
      return;
    DispatchCriteria crit = pCriteria.remove(position);
    pCriteria.add(position-1, crit);
  }
  
  /**
   * Move the criteria down one slot in the control
   * 
   * @param position
   *   The position in the list of criteria to find the criteria to move.
   */
  public void
  moveDown
  (
    int position
  )
  {
    if (position < 0 || position >= pCriteria.size())
      throw new IllegalArgumentException
        ("The position (" + position +") is not a valid position in a dispatch control");
    if (position == (pCriteria.size() - 1))
      return;
    DispatchCriteria crit = pCriteria.remove(position);
    pCriteria.add(position+1, crit);
  }
  
  /**
   * Move the criteria to the top of the control
   * 
   * @param position
   *   The position in the list of criteria to find the criteria to move.
   */
  public void
  makeTop
  (
    int position
  )
  {
    if (position < 0 || position >= pCriteria.size())
      throw new IllegalArgumentException
        ("The position (" + position +") is not a valid position in a dispatch control");

    DispatchCriteria crit = pCriteria.remove(position);
    pCriteria.addFirst(crit);
  }
  
  /**
   * Move the criteria to the bottom of the control
   * 
   * @param position
   *   The position in the list of criteria to find the criteria to move.
   */
  public void
  makeBottom
  (
    int position 
  )
  {
    if (position < 0 || position >= pCriteria.size())
      throw new IllegalArgumentException
        ("The position (" + position +") is not a valid position in a dispatch control");

    DispatchCriteria crit = pCriteria.remove(position);
    pCriteria.addLast(crit);
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

    Object o = decoder.decode("Criteria");
    
    if (o != null) {
      if (o instanceof LinkedHashSet) {
        LinkedHashSet<DispatchCriteria> crits = 
          (LinkedHashSet<DispatchCriteria>) o;
        pCriteria.addAll(crits);
      }
      else
        pCriteria = (LinkedList<DispatchCriteria>) o;
    }
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
  private LinkedList<DispatchCriteria>  pCriteria; 
}
