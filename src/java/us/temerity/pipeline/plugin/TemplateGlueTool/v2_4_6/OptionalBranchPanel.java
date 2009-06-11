// $Id: OptionalBranchPanel.java,v 1.1 2009/06/11 05:35:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_6;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_3.*;
import us.temerity.pipeline.ui.*;


public 
class OptionalBranchPanel
  extends JPanel
  implements ActionListener
{
  public
  OptionalBranchPanel
  (
    TemplateNetworkScan scan,
    TemplateGlueInformation oldSettings  
  )
  {
    super();
    
    pNextID = 0;
    pOrder = new LinkedList<Integer>();
    pEntries = new TreeMap<Integer, OptionalBranchEntry>();
    pAddEntries = new ArrayList<AddEntry>();
    
    pMissing = scan.getOptionalBranchValues().keySet();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    {
      pButtonBox = new Box(BoxLayout.LINE_AXIS);
      
      JButton add = UIFactory.createDialogButton("Add", "add", this, "Add an Optional Branch");
      pButtonBox.add(add);
      pButtonBox.add(Box.createHorizontalGlue());
      pBox.add(pButtonBox);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    {
      pHeaderBox = new Box(BoxLayout.LINE_AXIS);
      Dimension dim = new Dimension(150, 19);
      {
        JLabel label = UIFactory.createLabel("Optional Branch", dim.width, SwingConstants.LEFT);
        label.setMaximumSize(dim);
        pHeaderBox.add(label);
        pHeaderBox.add(Box.createHorizontalStrut(8));
      }
      
      pHeaderBox.add(Box.createHorizontalGlue());
      pBox.add(pHeaderBox);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    if (oldSettings != null) {
     ListMap<String, Boolean> branches = oldSettings.getOptionalBranches();
     
     for (Entry<String, Boolean> entry : branches.entrySet()) {
       createEntry(entry.getKey(), entry.getValue());
     }
    }
    createEntry(null, null);
    
    pBox.add(Box.createVerticalStrut(20));
    
    {
      pButtonBox2 = new Box(BoxLayout.LINE_AXIS);
      
      JButton add = 
        UIFactory.createDialogButton("Add All", "addall", this, "Add all the found optional branches");
      pButtonBox2.add(add);
      pButtonBox2.add(Box.createHorizontalGlue());
      pBox.add(pButtonBox2);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    for (String extra : pMissing) {
      AddEntry entry = new AddEntry(this, extra);
      pBox.add(entry);
      pBox.add(Box.createVerticalStrut(4));
      pAddEntries.add(entry);
    }
    
    pBox.add(UIFactory.createFiller(100));
    
    Dimension dim = new Dimension(700, 500);
    
    JScrollPane scroll = UIFactory.createScrollPane
     (pBox, 
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
      dim, null, null);
    
    this.add(scroll); 
  }
  
  public ListMap<String, Boolean>
  getOptionalBranches()
    throws PipelineException
  {
    ListMap<String, Boolean> toReturn = new ListMap<String, Boolean>();
    for (int i : pOrder) {
      OptionalBranchEntry entry = pEntries.get(i);
      String branch = entry.getOptionalBranch();
      if (branch!= null && !branch.equals("")) {
        if (toReturn.keySet().contains(branch))
          throw new PipelineException
            ("The Optional Branch (" + branch + ") appears more than once in the panel.  " +
             "Please correct this before continuing.");
        
        toReturn.put(branch, entry.getDefaultValue());
      }
    }
    return toReturn;
  }
  
  private TreeSet<String>
  collectBranchesModes()
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    for (int i : pOrder) {
      OptionalBranchEntry entry = pEntries.get(i);
      String mode = entry.getOptionalBranch();
      if (mode != null && !mode.equals("")) {
        toReturn.add(mode);
      }
    }
    return toReturn;
  }
  
  /**
   * @param rep
   * @param def
   * @param param
   */
  private void 
  createEntry
  (
    String range,
    Boolean defaultValue
  )
  {
    OptionalBranchEntry entry = 
       new OptionalBranchEntry(this, pNextID, range, defaultValue);
     pBox.add(entry);
     pBox.add(Box.createVerticalStrut(2));
     pEntries.put(pNextID, entry);
     pOrder.add(pNextID);
     pNextID++;
  }
  
  private void
  relayout()
  {
    pBox.removeAll();
    pBox.add(pButtonBox);
    pBox.add(Box.createVerticalStrut(4));
    pBox.add(pHeaderBox);
    pBox.add(Box.createVerticalStrut(4));
    for (int i : pOrder) {
      OptionalBranchEntry entry = pEntries.get(i);
      pBox.add(entry);
      pBox.add(Box.createVerticalStrut(2));
    }
    
    pBox.add(Box.createVerticalStrut(20));
    pBox.add(pButtonBox2);
    pBox.add(Box.createVerticalStrut(4));
    
    for (AddEntry entry : pAddEntries) {
      pBox.add(entry);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    pBox.add(UIFactory.createFiller(100));
    pBox.revalidate();
  }
  
  @Override
  public void 
  actionPerformed
  (
    ActionEvent e
  )
  {
    String command = e.getActionCommand();
    if (command.startsWith("remove-")) {
      int id = Integer.valueOf(command.replace("remove-", ""));
      int idx = pOrder.indexOf(id);
      pOrder.remove(idx);
      pEntries.remove(idx);
      relayout();
    }
    else if (command.startsWith("up-")) {
      int id = Integer.valueOf(command.replace("up-", ""));
      int idx = pOrder.indexOf(id);
      if (idx == 0)
        return;
      pOrder.remove(idx);
      pOrder.add(idx-1, id);
      relayout();
    }
    else if (command.startsWith("down-")) {
      int id = Integer.valueOf(command.replace("down-", ""));
      int idx = pOrder.indexOf(id);
      if (idx == (pOrder.size() - 1))
        return;
      pOrder.remove(idx);
      pOrder.add(idx+1, id);
      relayout();
    }
    else if (command.equals("add")) {
      createEntry(null, null);
      relayout();
    }
    else if (command.equals("addall")) {
      TreeSet<String> existing = collectBranchesModes();
      for (String entry : pMissing) {
        if (!existing.contains(entry)) {
          createEntry(entry, null);
        }
      }
      relayout();
    }
    else if (command.startsWith("add-")) {
      String name = command.replace("add-", "");
      TreeSet<String> existing = collectBranchesModes();
      if (!existing.contains(name)) 
        createEntry(name, null);
      relayout();
    }
  } 
  
  
  private class
  AddEntry
    extends Box
  {
    private 
    AddEntry
    (
      ActionListener parent,
      String range
    )
    {
      super(BoxLayout.LINE_AXIS);
      
      JParamNameField field = UIFactory.createParamNameField(range, 150, SwingConstants.LEFT);
      field.setMaximumSize(field.getPreferredSize());
      field.setEditable(false);
      this.add(field);
      this.add(Box.createHorizontalStrut(8));
      {
        JButton but = 
          UIFactory.createDialogButton("Add", "add-" + range, parent, "Add the missing replacement");
        this.add(but);
      }
      this.add(Box.createHorizontalGlue()); 
    }

    private static final long serialVersionUID = -2956919358583413464L;
  }
  
  private class
  OptionalBranchEntry
    extends Box
  {
    private
    OptionalBranchEntry
    (
      ActionListener parent,
      int id,
      String branch,
      Boolean defaultValue
    )
    {
      super(BoxLayout.LINE_AXIS);
      
      pOptionalBranch = UIFactory.createParamNameField(branch, 150, SwingConstants.LEFT);
      pOptionalBranch.setMaximumSize(pOptionalBranch.getPreferredSize());
      this.add(pOptionalBranch);
      this.add(Box.createHorizontalStrut(8));
      
      pDefaultValue = UIFactory.createBooleanField(defaultValue, 150);
      
      this.add(Box.createHorizontalStrut(4));
      {
        JButton but = 
          UIFactory.createDialogButton("Up", "up-" + id, parent, "Move the optional branch up.");
        this.add(but);
        this.add(Box.createHorizontalStrut(8));
      }
      {
        JButton but = 
          UIFactory.createDialogButton("Down", "down-" + id, parent, "Move the optional branch down.");
        this.add(but);
        this.add(Box.createHorizontalStrut(8));
      }
      {
        JButton but = 
          UIFactory.createDialogButton("Remove", "remove-" + id, parent, "Remove the optional branch.");
        this.add(but);
      }
      
      this.add(Box.createHorizontalGlue());
    }
    
    private String
    getOptionalBranch()
    {
      return pOptionalBranch.getText();
    }
    
    private Boolean
    getDefaultValue()
    {
      return pDefaultValue.getValue();
    }

    private static final long serialVersionUID = -1976361438613594824L;

    private JParamNameField pOptionalBranch;
    private JBooleanField pDefaultValue;
  }
  
  private static final long serialVersionUID = 4837345737569850278L;
  
  private int pNextID;
  private TreeMap<Integer, OptionalBranchEntry> pEntries;
  private LinkedList<Integer> pOrder;
  
  private Set<String> pMissing;
  private ArrayList<AddEntry> pAddEntries;
  
  private Box pBox;
  private Box pButtonBox;
  private Box pHeaderBox;
  private Box pButtonBox2;
}
