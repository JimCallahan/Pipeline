// $Id: ScanNetworkPanel.java,v 1.1 2009/06/11 05:35:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_6;

import java.awt.*;

import javax.swing.*;


public 
class ScanNetworkPanel
  extends JPanel
{
  public
  ScanNetworkPanel
  (
    String rootNode  
  )
  {
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    this.add(Box.createHorizontalGlue());
    Box vbox = new Box(BoxLayout.PAGE_AXIS);
    vbox.add(Box.createVerticalGlue());
    JTextArea area = new JTextArea(10, 6);
    area.setName("TextArea");
    area.setText
      ("Press next to begin scanning the template network rooted at:\n(" + rootNode + ")");
    area.setWrapStyleWord(true);
    area.setLineWrap(true);
    Dimension dim = new Dimension(450, 50);
    area.setMinimumSize(dim);
    area.setMaximumSize(dim);
    area.setPreferredSize(dim);
    vbox.add(area);
    vbox.add(Box.createVerticalGlue());
    this.add(vbox);
    this.add(Box.createHorizontalGlue());
  }
  
  private static final long serialVersionUID = -5821232884678790422L;
}
