package us.temerity.pipeline.ui;

import java.awt.Dimension;

import javax.swing.*;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   C O N T E X T   F I E L D                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 *  A field made up of three collection fields allowing selection of an angular unit, a linear
 *  unit and a time unit to be used when building Maya scenes.
 */
public 
class JMayaContextField
  extends JPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JMayaContextField()
    throws PipelineException
  {
    this(null);
  }

  
  /**
   * Construct a new field.
   */ 
  public 
  JMayaContextField
  (
    JDialog parent
  ) 
    throws PipelineException 
  {
    super();
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.setAlignmentY(0.5f);
    
    pAngularUnitField = new JCollectionField(MayaContext.getAngularUnits(), parent);
    
    pLinearUnitField = new JCollectionField(MayaContext.getLinearUnits(), parent);
    
    pTimeUnitField = new JCollectionField(MayaContext.getTimeUnits(), parent);
    
    MayaContext context = new MayaContext();
    pAngularUnitField.setSelected(context.getAngularUnit());
    pLinearUnitField.setSelected(context.getLinearUnit());
    pTimeUnitField.setSelected(context.getTimeUnit());
    
    this.add(pAngularUnitField);
    this.add(Box.createRigidArea(new Dimension(0, 3)));
    this.add(pLinearUnitField);
    this.add(Box.createRigidArea(new Dimension(0, 3)));
    this.add(pTimeUnitField);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Set the MayaContext value.
   */ 
  public void 
  setValue
  (
    MayaContext value
  ) 
  {
    pAngularUnitField.setSelected(value.getAngularUnit());
    pLinearUnitField.setSelected(value.getLinearUnit());
    pTimeUnitField.setSelected(value.getTimeUnit());
  }
  
  /**
   * Get the MayaContext value.
   */ 
  public MayaContext
  getValue()
    throws PipelineException
  {
    return new MayaContext(pAngularUnitField.getSelected(), 
      			   pLinearUnitField.getSelected(), 
      			   pTimeUnitField.getSelected());
  }
  
  /**
   * Gets the Angular Unit Field.
   * 
   * @return
   * 	The {@link JCollectionField} that set the Angular Unit value.
   */
  public JCollectionField getAngularUnitField()
  {
    return pAngularUnitField;
  }

  /**
   * Gets the Linear Unit Field.
   * 
   * @return
   * 	The {@link JCollectionField} that set the Linear Unit value.
   */
  public JCollectionField getLinearUnitField()
  {
    return pLinearUnitField;
  }

  /**
   * Gets the Time Unit Field.
   * 
   * @return
   * 	The {@link JCollectionField} that set the Time Unit value.
   */
  public JCollectionField getTimeUnitField()
  {
    return pTimeUnitField;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public void setMaximumSize(Dimension maximumSize)
  {
    super.setMaximumSize(maximumSize);
    Dimension childD = new Dimension(maximumSize.width, 19);
    pAngularUnitField.setMaximumSize(childD);
    pLinearUnitField.setMaximumSize(childD);
    pTimeUnitField.setMaximumSize(childD);
  }

  @Override
  public void setMinimumSize(Dimension minimumSize)
  {
    super.setMinimumSize(minimumSize);
    Dimension childD = new Dimension(minimumSize.width, 19);
    pAngularUnitField.setMinimumSize(childD);
    pLinearUnitField.setMinimumSize(childD);
    pTimeUnitField.setMinimumSize(childD);
  }

  @Override
  public void setPreferredSize(Dimension preferredSize)
  {
    super.setPreferredSize(preferredSize);
    Dimension childD = new Dimension(preferredSize.width, 19);
    pAngularUnitField.setPreferredSize(childD);
    pLinearUnitField.setPreferredSize(childD);
    pTimeUnitField.setPreferredSize(childD);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 163371774971001572L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The three collection fields that contain the field's data.
   */
  private JCollectionField pAngularUnitField;
  private JCollectionField pLinearUnitField;
  private JCollectionField pTimeUnitField;
}
