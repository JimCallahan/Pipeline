package us.temerity.pipeline.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.PackageInfo;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.BaseUtil;
import us.temerity.pipeline.builder.UtilContext;

/*------------------------------------------------------------------------------------------*/
/*   U T I L   C O N T E X T   F I E L D                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 *  A field made up of three collection fields allowing selection of a user, a working
 *  area that belongs to the user, and a toolset.
 */
public 
class JUtilContextField
  extends JPanel
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   * 
   * @throws PipelineException 
   */ 
  public 
  JUtilContextField()
    throws PipelineException
  {
    this(null);
  }
  
  /**
   * Construct a new field.
   * 
   * @throws PipelineException 
   */ 
  public 
  JUtilContextField
  (
    JDialog parent
  ) 
    throws PipelineException 
  {
    super();
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.setAlignmentY(0.5f);
    
    pAuthorViewMapping = BaseUtil.getWorkingAreas();
    
    pAuthorField = new JCollectionField(pAuthorViewMapping.keySet(), parent);
    
    String author = PackageInfo.sUser;
    pAuthorField.setSelected(author);
    
    TreeSet<String> views = pAuthorViewMapping.get(author);
    
    pViewField = new JCollectionField(views, parent);
    pViewField.setSelected("default");
    
    pToolsetField = new JCollectionField(BaseUtil.getActiveToolsets(), parent);
    pToolsetField.setSelected(BaseUtil.getDefaultToolset());
    
    pAuthorField.addActionListener(this);
    
    this.add(pAuthorField);
    this.add(Box.createRigidArea(new Dimension(0, 3)));
    this.add(pViewField);
    this.add(Box.createRigidArea(new Dimension(0, 3)));
    this.add(pToolsetField);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Set the UtilContext value.
   */ 
  public void 
  setValue
  (
   UtilContext value
  ) 
  {
    pAuthorField.setSelected(value.getAuthor());
    pViewField.setSelected(value.getView());
    pToolsetField.setSelected(value.getToolset());
  }
  
  /**
   * Get the UtilContext value.
   */ 
  public UtilContext
  getValue() 
  {
    return new UtilContext(pAuthorField.getSelected(), 
      			   pViewField.getSelected(), 
      			   pToolsetField.getSelected());
  }
  
  /**
   * Gets the Author Field.
   * 
   * @return
   * 	The {@link JCollectionField} that set the Author value.
   */
  public JCollectionField getAuthorField()
  {
    return pAuthorField;
  }

  /**
   * Gets the View Field.
   * 
   * @return
   * 	The {@link JCollectionField} that set the View value.
   */
  public JCollectionField getViewField()
  {
    return pViewField;
  }

  /**
   * Gets the Toolset Field.
   * 
   * @return
   * 	The {@link JCollectionField} that set the Toolset value.
   */
  public JCollectionField getToolsetField()
  {
    return pToolsetField;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public void setMaximumSize(Dimension maximumSize)
  {
    super.setMaximumSize(maximumSize);
    Dimension childD = new Dimension(maximumSize.width, 19);
    pAuthorField.setMaximumSize(childD);
    pViewField.setMaximumSize(childD);
    pToolsetField.setMaximumSize(childD);
  }

  @Override
  public void setMinimumSize(Dimension minimumSize)
  {
    super.setMinimumSize(minimumSize);
    Dimension childD = new Dimension(minimumSize.width, 19);
    pAuthorField.setMinimumSize(childD);
    pViewField.setMinimumSize(childD);
    pToolsetField.setMinimumSize(childD);
  }

  @Override
  public void setPreferredSize(Dimension preferredSize)
  {
    super.setPreferredSize(preferredSize);
    Dimension childD = new Dimension(preferredSize.width, 19);
    pAuthorField.setPreferredSize(childD);
    pViewField.setPreferredSize(childD);
    pToolsetField.setPreferredSize(childD);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void actionPerformed(ActionEvent ev)
  {
    String author = pAuthorField.getSelected();
    String view = pViewField.getSelected();
    TreeSet<String> views = pAuthorViewMapping.get(author);
    pViewField.setValues(views);
    if (view != null)
    {
      if(views.contains(view))
	pViewField.setSelected(view);
      else if (views.contains("default"))
        pViewField.setSelected("default");
    }
    else if (views.contains("default"))
      pViewField.setSelected("default");
    else
      pViewField.setSelected(views.first());
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1972186414183886967L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The three collection fields that contain the field's data.
   */
  private JCollectionField pAuthorField;
  private JCollectionField pViewField;
  private JCollectionField pToolsetField;
  
  private TreeMap<String,TreeSet<String>> pAuthorViewMapping;
  
}
