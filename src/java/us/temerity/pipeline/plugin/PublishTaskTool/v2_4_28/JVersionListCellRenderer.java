package us.temerity.pipeline.plugin.PublishTaskTool.v2_4_28;

import java.awt.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   V E R S I O N   L I S T   C E L L   R E N D E R E R                                    */
/*------------------------------------------------------------------------------------------*/

public 
class JVersionListCellRenderer
  extends JPanel
  implements ListCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  JVersionListCellRenderer
  (
    PublishTaskTool tool  
  )
  {
    super();
    
    pTool = tool;
    
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    
    {
      Box hbox = new Box(BoxLayout.LINE_AXIS);
      
      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
      {
        JLabel label = new JLabel("v");
        label.setToolTipText(UIFactory.formatToolTip
          ("The revision number of the checked-in version."));
        
        hbox.add(label);
        
        pVersionLabel = label;
      }
      hbox.add(Box.createHorizontalGlue());
      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
      {
        JLabel label = new JLabel("user");
        label.setToolTipText(UIFactory.formatToolTip
          ("The name of the user owning the working version of the node that was " + 
           "checked-in, optionally followed by the name of the user who actually " + 
           "performed check-in (in parentheses) if different from the owner of " +
           "the node.  The owner can be different than the user performing the " +
           "check-in when its performed by a Node Manager in another user's " +
           "working area."));
        
        hbox.add(label);
        
        pUserLabel = label;
      }
      hbox.add(Box.createHorizontalGlue());
      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
      {
        JLabel label = new JLabel("date");
        label.setToolTipText(UIFactory.formatToolTip
          ("When the version was checked-in."));
        
        hbox.add(label);
        
        pDateLabel = label;
      }
      
      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
      add(hbox);
    }
    add(Box.createRigidArea(new Dimension(0, 3)));
    {
      Box hbox = new Box(BoxLayout.LINE_AXIS);
      
      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
      
      {
        JTextArea area = new JTextArea("message", 0, 39);
        
        area.setName("HistoryTextArea");
        
        area.setLineWrap(true);
        area.setWrapStyleWord(false);
        area.setEditable(false);
        
        area.setFocusable(true);
        
        pMessageArea = area;
        
        hbox.add(area);
      }
      
      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
      hbox.add(Box.createHorizontalGlue());
      
      add(hbox);
    }
    add(Box.createRigidArea(new Dimension(0, 3)));
    {
      Box hbox = new Box(BoxLayout.X_AXIS);
      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
      
      hbox.add(Box.createHorizontalGlue());
      {
        JLabel label = new JLabel("Published!");
        label.setToolTipText(UIFactory.formatToolTip
          ("Has this version been published already?."));

        hbox.add(label);

        pPublishStateLabel = label;
      }
      
      hbox.add(Box.createRigidArea(new Dimension(4, 0)));
      add(hbox);
    }

    {
      JPanel spanel = new JPanel();
      spanel.setName("Spacer");
      
      spanel.setMinimumSize(new Dimension(450, 7));
      spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
      spanel.setPreferredSize(new Dimension(4, 7));
      
      add(spanel);
    }
    add(Box.createRigidArea(new Dimension(0, 20)));
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public Component 
  getListCellRendererComponent
  (
    JList list,
    Object value,
    int index,
    boolean isSelected,
    boolean cellHasFocus
  )
  {
    NodeVersion ver = (NodeVersion) value;
    
    if (isSelected) {
      pVersionLabel.setForeground(Color.YELLOW);
      pDateLabel.setForeground(Color.YELLOW);
      pUserLabel.setForeground(Color.YELLOW);
      pPublishStateLabel.setForeground(Color.YELLOW);
      pMessageArea.setForeground(Color.YELLOW);
    }
    else {
      pVersionLabel.setForeground(Color.WHITE);
      pDateLabel.setForeground(Color.WHITE);
      pUserLabel.setForeground(Color.WHITE);
      pPublishStateLabel.setForeground(Color.WHITE);
      pMessageArea.setForeground(Color.WHITE);
    }
    
    pVersionLabel.setText("v" + ver.getVersionID().toString());
    
    {
      String author = ver.getAuthor();
      String impostor = ver.getImpostor();
      
      if (impostor == null)
        pUserLabel.setText(author);
      else
        pUserLabel.setText(author + " (" + impostor + ")");
    }
    
    pDateLabel.setText(TimeStamps.format(ver.getTimeStamp()));
    
    if (pTool.getPublishedVersions().contains(ver.getVersionID()))
      pPublishStateLabel.setText("Already Published");
    else
      pPublishStateLabel.setText("No Record of Publishing");
    
    pMessageArea.setText(ver.getMessage());
    pMessageArea.setRows(pMessageArea.getLineCount());
    
    this.validate();
    
    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5137881899258747334L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private PublishTaskTool pTool;
  
  private JLabel pVersionLabel;
  private JLabel pDateLabel;
  private JLabel pUserLabel;
  private JLabel pPublishStateLabel;
  
  private JTextArea pMessageArea;
}
