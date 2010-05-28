// $Id: TemplateUIFactory.java,v 1.2 2009/12/18 07:19:18 jim Exp $

package us.temerity.pipeline.plugin.TemplateGenParamManifestTool.v2_4_27;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import us.temerity.pipeline.ui.*;


public 
class TemplateUIFactory
{
  /**
   * Create a new button for use in a panel. <P> 
   * 
   * @param text
   *   The button text.
   * 
   * @param actionCommand
   *    The command to pass to the ActionListener or <CODE>null</CODE> to ignore.
   * 
   * @param actionListener
   *    The listener which will handle the the event of the button being pressed or 
   *    <CODE>null</CODE> to ignore.
   * 
   * @param tooltip
   *   The tooltip text or <CODE>null</CODE> to ignore.
   */ 
  public static JButton
  createPanelButton
  (
   String text, 
   String actionCommand, 
   ActionListener actionListener, 
   String tooltip
  )
  {
    JButton btn = new JButton(text);
    btn.setName("ValuePanelButton"); 
    btn.setHorizontalTextPosition(SwingConstants.LEFT);
  
    Dimension size = btn.getPreferredSize();
    size.setSize(size.width, 25);
    btn.setPreferredSize(size);
    btn.setMinimumSize(size); 
    btn.setMaximumSize(size); 
        
    if((actionCommand != null) && (actionListener != null)) {
      btn.setActionCommand(actionCommand);
      btn.addActionListener(actionListener);
    }
  
    if(tooltip != null) 
      btn.setToolTipText(UIFactory.formatToolTip(tooltip)); 
  
    return btn;
  }

  /**
   * @param parent
   * @param actionCommand
   */
  public static JButton 
  createRemoveButton
  (
    ActionListener parent,
    String actionCommand
  )
  {
    JButton but = new JButton();
    but.addActionListener(parent);
    but.setActionCommand(actionCommand);
    but.setName("CloseButton");
    Dimension size = new Dimension(15, 19);
    but.setMinimumSize(size);
    but.setMaximumSize(size);
    but.setPreferredSize(size);
    return but;
  }
  
  public static JButton
  createUpButton
  (
    ActionListener parent,
    String actionCommand
  )
  {
    JButton but = new JButton();
    but.addActionListener(parent);
    but.setActionCommand(actionCommand);
    but.setName("UpArrowButton");
    Dimension size = new Dimension(16, 19);
    but.setMinimumSize(size);
    but.setMaximumSize(size);
    but.setPreferredSize(size);  
    return but;
  }
  
  public static JButton
  createDownButton
  (
    ActionListener parent,
    String actionCommand
  )
  {
    JButton but = new JButton();
    but.addActionListener(parent);
    but.setActionCommand(actionCommand);
    but.setName("DownArrowButton");
    Dimension size = new Dimension(16, 19);
    but.setMinimumSize(size);
    but.setMaximumSize(size);
    but.setPreferredSize(size);  
    return but;
  }
  
  public static Component
  createHorizontalIndent()
  {
    return Box.createHorizontalStrut(4);
  }
  
  public static Component
  createHorizontalSpacer()
  {
    return Box.createHorizontalStrut(8);
  }
  
  public static Component
  createSecondLevelIndent()
  {
    return Box.createHorizontalStrut(75);
  }
  
  public static Component
  createButtonSpacer()
  {
    return Box.createHorizontalStrut(6);
  }
  
  public static Component
  createVerticalGap()
  {
    return Box.createVerticalStrut(3);
  }
  
  public static Component
  createLargeVerticalGap()
  {
    return Box.createVerticalStrut(12);
  }
  
  public static Box
  createTitleBox
  (
    String title  
  )
  {
    Box toReturn = createHorizontalBox();
    Component label = UIFactory.createPanelLabel(title);
    toReturn.add(label);
    toReturn.add(Box.createHorizontalGlue());
    return toReturn;
  }
  
  public static Box
  createHorizontalBox()
  {
    Box toReturn = new Box(BoxLayout.LINE_AXIS);
    toReturn.add(createHorizontalIndent());
    return toReturn;
  }
  
  public static class
  AddEntry
    extends Box
  {
    protected
    AddEntry
    (
      ActionListener parent,
      String name,
      String type
    )
    {
      super(BoxLayout.LINE_AXIS);
      
      this.add(TemplateUIFactory.createHorizontalIndent());
      
      JParamNameField field = UIFactory.createParamNameField(name, 150, SwingConstants.LEFT);
      field.setMaximumSize(field.getPreferredSize());
      field.setEditable(false);
      field.setName("TextField");
      this.add(field);
      this.add(TemplateUIFactory.createHorizontalSpacer());
      {
        JButton but = 
          TemplateUIFactory.createPanelButton("Add", "add-" + name, parent, "Add the missing " + type);
        this.add(but);
      }
      this.add(Box.createHorizontalGlue()); 
    }
    
    private static final long serialVersionUID = -3561925475151159308L;
  }
}
