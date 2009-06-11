// $Id: ExternalsPanel.java,v 1.1 2009/06/11 05:35:08 jesse Exp $

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
class ExternalsPanel
  extends JPanel
  implements ActionListener
{
  public
  ExternalsPanel
  (
    JTemplateGlueDialog dialog,
    TemplateNetworkScan scan,
    TemplateGlueInformation oldSettings,
    Set<String> contexts
  )
  {
    pNextID = 0;
    pOrder = new LinkedList<Integer>();
    pEntries = new TreeMap<Integer, ExternalEntry>();
    pAddEntries = new ArrayList<AddEntry>();
    
    pAllContexts = contexts;
    
    pDialog = dialog;
    
    pMissing = scan.getExternals().keySet();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    {
      pButtonBox = new Box(BoxLayout.LINE_AXIS);
      
      JButton add = UIFactory.createDialogButton("Add", "add", this, "Add an external sequence.");
      pButtonBox.add(add);
      pButtonBox.add(Box.createHorizontalGlue());
      pBox.add(pButtonBox);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    {
      pHeaderBox = new Box(BoxLayout.LINE_AXIS);
      Dimension dim = new Dimension(150, 19);
      {
        JLabel label = UIFactory.createLabel("External Name", dim.width, SwingConstants.LEFT);
        label.setMaximumSize(dim);
        pHeaderBox.add(label);
        pHeaderBox.add(Box.createHorizontalStrut(8));
      }
      
      pHeaderBox.add(Box.createHorizontalGlue());
      pBox.add(pHeaderBox);
      pBox.add(Box.createVerticalStrut(4));
    }
    
    if (oldSettings != null) {
      MappedSet<String, String> externals = oldSettings.getExternals();
      for (Entry<String, TreeSet<String>> entry : externals.entrySet()) {
        String external = entry.getKey();
        createEntry(external, entry.getValue());
      }
     }
     createEntry(null, null);
     
     pBox.add(Box.createVerticalStrut(20));
     
     {
       pButtonBox2 = new Box(BoxLayout.LINE_AXIS);
       
       JButton add = 
         UIFactory.createDialogButton("Add All", "addall", this, "Add all the found external sequences");
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
  
  private void 
  createEntry
  (
    String external,
    TreeSet<String> contexts
  )
  {
    ExternalEntry entry = 
       new ExternalEntry(this, pNextID, external, contexts);
     pBox.add(entry);
     pBox.add(Box.createVerticalStrut(2));
     pEntries.put(pNextID, entry);
     pOrder.add(pNextID);
     pNextID++;
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
    else if (command.equals("add")) {
      createEntry(null, null);
      relayout();
    }
    else if (command.equals("addall")) {
      TreeSet<String> existing = collectExternals();
      for (String entry : pMissing) {
        if (!existing.contains(entry)) {
          createEntry(entry, null);
        }
      }
      relayout();
    }
    else if (command.startsWith("add-")) {
      String name = command.replace("add-", "");
      TreeSet<String> existing = collectExternals();
      if (!existing.contains(name)) 
        createEntry(name, null);
      relayout();
    }
  }
  
  public MappedSet<String, String>
  getExternals()
    throws PipelineException
  {
    MappedSet<String, String> toReturn = new MappedSet<String, String>();
    for (int i : pOrder) {
      ExternalEntry entry = pEntries.get(i);
      String external = entry.getExternal();
      if (external != null && !external.equals("")) {
        if (toReturn.keySet().contains(external))
          throw new PipelineException
            ("The external seq (" + external + ") appears more than once in the panel.  " +
             "Please correct this before continuing.");
        TreeSet<String> externalContexts = entry.getExternalContexts();
        toReturn.put(external, externalContexts);
      }
    }
    return toReturn;
  }
  
  private TreeSet<String>
  collectExternals() 
  {
    TreeSet<String> toReturn = new TreeSet<String>();
    for (int i : pOrder) {
      ExternalEntry entry = pEntries.get(i);
      String extenal = entry.getExternal();
      if (extenal != null && !extenal.equals("")) {
        toReturn.add(extenal);
      }
    }
    return toReturn;
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
      ExternalEntry entry = pEntries.get(i);
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
  
  private class
  ExternalEntry
    extends Box
    implements ActionListener
  {
    private
    ExternalEntry
    (
      ActionListener parent,
      int id,
      String external,
      TreeSet<String> contexts
    )
    {
      super(BoxLayout.LINE_AXIS);
      
      TreeSet<String> old = new TreeSet<String>();
      if (contexts != null)
        old.addAll(contexts);
    
      pContextFields = new TreeMap<Integer, ExternalContextEntry>();
      pReplaceOrder = new LinkedList<Integer>();

      pReplaceID = 0;
      pInsideBox = new Box(BoxLayout.PAGE_AXIS);
      
      Box hbox = new Box(BoxLayout.LINE_AXIS);
      
      pExternal = UIFactory.createParamNameField(external, 150, SwingConstants.LEFT);
      pExternal.setMaximumSize(pExternal.getPreferredSize());
      hbox.add(pExternal);
      hbox.add(Box.createHorizontalStrut(8));
      
      {
        JButton but = 
          UIFactory.createDialogButton("Add", "add", this, "Add another External Sequence.");
        hbox.add(but);
      }

      hbox.add(Box.createHorizontalStrut(4));
      
      {
        JButton but = 
          UIFactory.createDialogButton("Remove", "remove-" + id, parent, 
            "Remove the External Sequence");
        hbox.add(but);
      }
      hbox.add(Box.createHorizontalGlue());
      
      pInsideBox.add(hbox);
      
      pHeader = hbox;
      
      pInsideBox.add(Box.createVerticalStrut(2));
      
      {
        pReplaceHeader = new Box(BoxLayout.LINE_AXIS);
        pReplaceHeader.add(Box.createHorizontalStrut(75));
        JLabel l1 = UIFactory.createFixedLabel("External Context", 150, SwingConstants.LEFT);
        pReplaceHeader.add(l1);
        pReplaceHeader.add(Box.createHorizontalGlue());
      }

      pInsideBox.add(pReplaceHeader);
      pInsideBox.add(Box.createVerticalStrut(2)); 

      
      for (String context : old) {
       if (pAllContexts.contains(context)) {
        addExternalContext(context); 
       }
      }
      
      this.add(pInsideBox);
    }
    
    private void
    addExternalContext
    (
      String context
    )
    {
      if (!pAllContexts.isEmpty() ) {
        ExternalContextEntry entry =
          new ExternalContextEntry(this, pReplaceID, context);
        pInsideBox.add(entry);
        pInsideBox.add(Box.createVerticalStrut(2));
        pContextFields.put(pReplaceID, entry);
        pReplaceOrder.add(pReplaceID);
        pReplaceID++;
      }
    }
    
    public String
    getExternal()
    {
      return pExternal.getText();
    }
    
    public TreeSet<String>
    getExternalContexts()
    {
      TreeSet<String> toReturn = new TreeSet<String>();
      for (ExternalContextEntry entry : pContextFields.values()) {
        toReturn.add(entry.getContext());
      }
      return toReturn;
    }

    private void
    relayout()
    {
      pInsideBox.removeAll();
      pInsideBox.add(pHeader);
      pInsideBox.add(Box.createVerticalStrut(2));
      pInsideBox.add(pReplaceHeader);
      pInsideBox.add(Box.createVerticalStrut(2));
      for (int i : pReplaceOrder) {
        ExternalContextEntry entry = pContextFields.get(i);
        pInsideBox.add(entry);
        pInsideBox.add(Box.createVerticalStrut(2));
      }
      pInsideBox.revalidate();
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
        int idx = pReplaceOrder.indexOf(id);
        pReplaceOrder.remove(idx);
        pContextFields.remove(idx);
        relayout();
      }
      else if (command.equals("add")) {
        addExternalContext(null);
        relayout();
      }
    }
    
    private class
    ExternalContextEntry
      extends Box
    {
      private
      ExternalContextEntry
      (
        ActionListener parent,
        int id,
        String context
      )
      {
        super(BoxLayout.LINE_AXIS);
        
        this.add(Box.createHorizontalStrut(75));

        pContext = 
          UIFactory.createCollectionField(pAllContexts, pDialog, 150);
        pContext.setMaximumSize(pContext.getPreferredSize());
        if (context != null)
          pContext.setSelected(context);
        
        this.add(pContext);
        
        this.add(Box.createHorizontalStrut(4));
        
        {
          JButton but = 
            UIFactory.createDialogButton("Remove", "remove-" + id, parent, "remove the replacement");
          this.add(but);
        }
        this.add(Box.createHorizontalGlue());
      }

      public String
      getContext()
      {
        return pContext.getSelected();
      }
      
      private static final long serialVersionUID = -3678513077237928470L;

      
      private JCollectionField pContext;
    }
    
    private Box pInsideBox;
    
    private Box pHeader;
    private Box pReplaceHeader;

    private int pReplaceID;
    
    private JParamNameField pExternal;
    private LinkedList<Integer> pReplaceOrder;

    private TreeMap<Integer, ExternalContextEntry> pContextFields;
    
    private static final long serialVersionUID = 561167289784883755L;
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

    private static final long serialVersionUID = -8394336171067212249L;
  }

  private static final long serialVersionUID = -5590800937891453791L;
  
  
  private int pNextID;
  private TreeMap<Integer, ExternalEntry> pEntries;
  private LinkedList<Integer> pOrder;
  
  private Set<String> pMissing;
  private ArrayList<AddEntry> pAddEntries;
  
  private Set<String> pAllContexts;
  
  private JTemplateGlueDialog pDialog;
  private Box pBox;
  private Box pButtonBox;
  private Box pHeaderBox;
  private Box pButtonBox2;
}
