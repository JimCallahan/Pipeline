// $Id: SelectionGroup.java,v 1.3 2006/01/05 16:54:43 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   G R O U P                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A named collection of selection biases.
 */
public
class SelectionGroup
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  SelectionGroup()
  { 
    init();
  }

  /**
   * Construct a new selection group.
   * 
   * @param name
   *   The name of the group.
   */ 
  public
  SelectionGroup
  (
   String name
  ) 
  {
    super(name);
    init();
  }

  /**
   * Construct a new selection group with the same selection key biases as the given group.
   * 
   * @param name
   *   The name of the new group.
   * 
   * @param group
   *   Copy selection key biases from this group.
   */ 
  public
  SelectionGroup
  (
   String name, 
   SelectionGroup group
  ) 
  {
    super(name);
    init();

    for(String key : group.getKeys()) 
      addBias(key, group.getBias(key));
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the instance.
   */ 
  private void 
  init() 
  {
    pSelectionBiases = new TreeMap<String,Integer>();
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the selection keys which are members of the group.
   */
  public synchronized Set<String>
  getKeys()
  {
    return Collections.unmodifiableSet(pSelectionBiases.keySet());
  }

  /**
   * Get the bias for the given selection key.
   * 
   * @param key
   *   The name of the selection key.
   * 
   * @return 
   *   The bias for the given key or <CODE>null</CODE> if the key is not defined.
   */ 
  public synchronized Integer
  getBias
  (
   String key
  ) 
  {
    return pSelectionBiases.get(key);
  }
  
  /**
   * Add (or replace) the bias for the given selection key.
   * 
   * @param key
   *   The name of the selection key.
   * 
   * @param bias 
   *   The selection bias for the key. 
   */ 
  public synchronized void 
  addBias
  (
   String key, 
   Integer bias
  ) 
  {
    if(bias == null) 
      throw new IllegalArgumentException
	("The selection bias cannot be (null)!");
    pSelectionBiases.put(key, bias);
  }
  
  /** 
   * Remove the selection key and bias for the named key.
   *
   * @param key 
   *    The name of the selection key to remove.
   */
  public synchronized void
  removeBias
  (
   String key
  ) 
  {
    pSelectionBiases.remove(key);
  }
  
  /** 
   * Remove all selection keys.
    */
  public synchronized void
  removeAllBiases() 
  {
    pSelectionBiases.clear();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B   S E L E C T I O N                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the selection score for the given job requirements. <P> 
   * 
   * This method will return <CODE>null</CODE> if the selection group does not contain 
   * all of the selection keys required.
   * 
   * @param jreqs
   *   The requirements that this host must meet in order to be eligable to run the job. 
   * 
   * @param keys 
   *   The names of the valid selection keys.
   * 
   * @return 
   *   The combined selection bias or <CODE>null</CODE> if the host fails the requirements.
   */ 
  public synchronized Integer
  computeSelectionScore
  (
   JobReqs jreqs, 
   TreeSet<String> keys
  )
  {
    int total = 0;
    for(String key : jreqs.getSelectionKeys()) {
      if(keys.contains(key)) {
	Integer bias = pSelectionBiases.get(key);
	if(bias == null) 
	  return null;
      
	total += bias; 
      }
    }

    return total;
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
    
    if(!pSelectionBiases.isEmpty()) 
      encoder.encode("SelectionBiases", pSelectionBiases);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeMap<String,Integer> biases = 
      (TreeMap<String,Integer>) decoder.decode("SelectionBiases"); 
    if(biases != null) 
      pSelectionBiases = biases;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3083288349921145769L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The selection key biases of the host indexed by selection key name.
   */ 
  private TreeMap<String,Integer>  pSelectionBiases; 

}
