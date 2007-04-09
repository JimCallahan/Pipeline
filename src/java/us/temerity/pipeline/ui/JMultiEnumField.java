/**
 * 
 */
package us.temerity.pipeline.ui;

import java.awt.Dimension;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.PipelineException;

/*------------------------------------------------------------------------------------------*/
/*   M U L T I   E N U M   F I E L D                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents a {@link MultiEnumParam}.
 * <P>
 * This field displays itself as a list of boolean parameters, organized with an optional
 * layout or alphabetically if no layout is specified.
 */
public class JMultiEnumField
  extends JPanel
{
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs a new field.
   * 
   * @param initialValues
   * 	A set of values that will initially be set to true.
   * 
   * @param values
   * 	A set of all the values in the parameter. 
   * 
   * @param layout
   * 	A layout for all the parameters.  If this is <code>null</code>, then all
   *    the values will be arranged in alphabetical order.  Otherwise, this structure
   *    needs to contain a string value for each value the parameter could have in the 
   *    order that they are supposed to be displayed.  It can also contain <code>null</code> 
   *    values. Each <code>null</code> value will be displayed as a vertical space between
   *    values.
   */
  public 
  JMultiEnumField
  (
    Set<String> initialValues,
    Set<String> values,
    ArrayList<String> layout
  ) 
  throws PipelineException
  {
    super();
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.setAlignmentY(0.5f);
    
    if (values == null)
      throw new IllegalArgumentException
        ("Cannot create a JMultiEnumField with a (null) values parameter");
    
    if (initialValues == null)
      initialValues = new TreeSet<String>();

    if(layout == null) {
      pLayout = new ArrayList<String>(values);
      Collections.sort(pLayout);
    }
    else
      pLayout = new ArrayList<String>(layout);
    
    validateLayout(values, pLayout);
    
    pBooleanFields = new TreeMap<String, JBooleanField>();
   
    int num = pLayout.size();
    for(int i = 0; i < num; i++) {
      String value = pLayout.get(i);
      if (value != null) {
	JBooleanField field = new JBooleanField();
	if(initialValues.contains(value))
	  field.setValue(true);
	else
	  field.setValue(false);
	pBooleanFields.put(value, field);
	this.add(field);
      if(i != (num - 1))
	this.add(Box.createRigidArea(new Dimension(0, 3)));
      } else
	this.add(Box.createRigidArea(new Dimension(0, 12)));
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
    Set<String> values
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
  
  /**
   * Returns the layout used for this field.
   */
  public ArrayList<String>
  getFieldLayout()
  {
    return pLayout;
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
  /*   V A L I D A T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A method to confirm that the inputted layout contains adequate entries for all
   * possible params and no extraneous ones.
   */
  protected void 
  validateLayout
  (
    Set<String> values,
    ArrayList<String> layout
  )
    throws IllegalArgumentException 
  {
    TreeSet<String> remaining = new TreeSet<String>(values);
    for (String position : layout)
    {
      if (position == null)
	continue;
      if (!values.contains(position))
	throw new IllegalArgumentException
	  ("Invalid Layout Entry ("+ position +") in a JMultiEnumParamField" );
      if (!remaining.contains(position))
	throw new IllegalArgumentException
	  ("Invalid Layout Entry ("+ position +") in a JMultiEnumParamField.  " +
	   "This entry appears to have existed twice in the layout."); 
      remaining.remove(position);
    }
    if (remaining.size() != 0)
      throw new IllegalArgumentException
        ("Missing Layout Entries in a JMultiEnumParamField.  " +
         "There are not entries for all the param's values.");
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
  
  /**
   * The layout for the fields.
   */
  private ArrayList<String> pLayout;

}
