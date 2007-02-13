/**
 * 
 */
package us.temerity.pipeline.ui;

import java.awt.Dimension;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.MultiEnumParam;
import us.temerity.pipeline.PipelineException;

/*------------------------------------------------------------------------------------------*/
/*   M U L T I   E N U M   F I E L D                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents a {@link MultiEnumParam}.
 */
public class JMultiEnumField
  extends JPanel
{
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs a new field.
   * @throws PipelineException 
   */
  public 
  JMultiEnumField
  (
    Collection<String> values,
    Collection<String> initialValues
  ) 
  throws PipelineException
  {
    super();
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.setAlignmentY(0.5f);
    
    if (values == null)
      throw new PipelineException
      	("Cannot create a JMultiEnumField with a (null) values parameter");
    
    if (initialValues == null)
      initialValues = new TreeSet<String>();
    
    pBooleanFields = new TreeMap<String, JBooleanField>();
   
    ArrayList<String> list = new ArrayList<String>(values);
    Collections.sort(list);
    int num = values.size();
    for(int i = 0; i < num; i++) {
      String value = list.get(i);
      JBooleanField field = new JBooleanField();
      if(initialValues.contains(value))
	field.setValue(true);
      else
	field.setValue(false);
      pBooleanFields.put(value, field);
      this.add(field);
      if(i != (num - 1))
	this.add(Box.createRigidArea(new Dimension(0, 3)));
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the values.
   */ 
  public void 
  setSelectedValues
  (
    Collection<String> values
  ) 
  {
    for(String fieldName : pBooleanFields.keySet()) {
      JBooleanField field = pBooleanFields.get(fieldName);
      if(values.contains(fieldName)) {
	field.setValue(true);
      }
      else {
	field.setValue(false);
      }
    }
  }
  
  /**
   * Sets the value.
   */ 
  public void 
  setSelectedValue
  (
    String key,
    Boolean value
  ) 
  {
    if (pBooleanFields.keySet().contains(key))
      pBooleanFields.get(key).setValue(value);
  }
  
  /**
   * Gets the value of the given key
   * @param key
   * @return <code>null</code> if the key is not a value in the param.
   */
  public Boolean
  getSelectedValue
  (
    String key
  )
  {
    if (pBooleanFields.keySet().contains(key))
      return pBooleanFields.get(key).getValue();
    return null;
  }
  
  /**
   * Gets all the keys that are set to true
   * 
   * @return
   */
  public Collection<String>
  getSelectedValues()
  {
    TreeSet<String> set = new TreeSet<String>();
    for (String key : pBooleanFields.keySet())
    {
      if (pBooleanFields.get(key).getValue())
	set.add(key);
    }
    return set;
  }
  
  public Collection<String>
  getValues()
  {
    return Collections.unmodifiableCollection(pBooleanFields.keySet());
  }

  /**
   * Gets all the {@link JBooleanField}s that make up the field.
   * 
   * @return
   */
  public TreeMap<String, JBooleanField>
  getFields()
  {
    return pBooleanFields;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public void setMaximumSize(Dimension maximumSize)
  {
    super.setMaximumSize(maximumSize);
    Dimension childD = new Dimension(maximumSize.width, 19);
    for (JBooleanField field : pBooleanFields.values())
      field.setMaximumSize(childD);
  }

  @Override
  public void setMinimumSize(Dimension minimumSize)
  {
    super.setMinimumSize(minimumSize);
    Dimension childD = new Dimension(minimumSize.width, 19);
    for (JBooleanField field : pBooleanFields.values())
      field.setMinimumSize(childD);
  }

  @Override
  public void setPreferredSize(Dimension preferredSize)
  {
    super.setPreferredSize(preferredSize);
    Dimension childD = new Dimension(preferredSize.width, 19);
    for (JBooleanField field : pBooleanFields.values())
      field.setPreferredSize(childD);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -6171587576211699143L;
 

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The TreeMap that contains the boolean fields for this 
   */
  private TreeMap<String, JBooleanField> pBooleanFields;
}
