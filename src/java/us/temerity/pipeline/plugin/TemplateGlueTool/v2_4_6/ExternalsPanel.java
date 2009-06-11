// $Id: ExternalsPanel.java,v 1.2 2009/06/11 19:41:22 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_6;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_3.*;
import us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_6.TemplateUIFactory.*;
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
    pHasMissing = !pMissing.isEmpty();
    
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    
    pBox = new Box(BoxLayout.PAGE_AXIS);
    
    pTitleBox = TemplateUIFactory.createTitleBox("Externals:");
    
    {
      pButtonBox = TemplateUIFactory.createHorizontalBox();
      
      JButton add = TemplateUIFactory.createPanelButton
        ("Add External", "add", this, "Add an external sequence.");
      pButtonBox.add(add);
      pButtonBox.add(Box.createHorizontalGlue());
    }
    
    {
      pHeaderBox = TemplateUIFactory.createHorizontalBox();
      int width = 150;
      {
        JLabel label = 
          UIFactory.createFixedLabel("External Name:", width, SwingConstants.LEFT);
        pHeaderBox.add(label);
      }
      pHeaderBox.add(Box.createHorizontalGlue());
    }
    
    if (oldSettings != null) {
      MappedSet<String, String> externals = oldSettings.getExternals();
      for (Entry<String, TreeSet<String>> entry : externals.entrySet()) {
        String external = entry.getKey();
        createEntry(external, entry.getValue());
      }
     }
     
     {
       pButtonBox2 = TemplateUIFactory.createHorizontalBox();
       
       pButtonBox2.add(UIFactory.createFixedLabel
         ("Found in scan: ", 150, SwingConstants.LEFT));
       
       pHeaderBox.add(TemplateUIFactory.createHorizontalSpacer());

       JButton add = 
         TemplateUIFactory.createPanelButton("Add All", "addall", this, "Add all the found contexts");
       pButtonBox2.add(add);
       pButtonBox2.add(Box.createHorizontalGlue());
     }
     
     for (String extra : pMissing) {
       AddEntry entry = new AddEntry(this, extra, "External Seq");
       pAddEntries.add(entry);
     }
     
     
     Dimension dim = new Dimension(700, 500);
     
     JScrollPane scroll = UIFactory.createScrollPane
      (pBox, 
       ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, 
       ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
       dim, null, null);
     
     this.add(scroll);
     
     relayout();
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
      pEntries.remove(id);
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
    pBox.add(pTitleBox);
    pBox.add(TemplateUIFactory.createLargeVerticalGap());
    pBox.add(pHeaderBox);
    pBox.add(TemplateUIFactory.createVerticalGap());
    
    for (int i : pOrder) {
      ExternalEntry entry = pEntries.get(i);
      pBox.add(entry);
      pBox.add(TemplateUIFactory.createLargeVerticalGap());
    }
    
    pBox.add(TemplateUIFactory.createVerticalGap());
    pBox.add(pButtonBox);
    pBox.add(TemplateUIFactory.createVerticalGap());
    
    if (pHasMissing) {
      pBox.add(Box.createVerticalStrut(20));
      pBox.add(UIFactory.createPanelBreak());
      pBox.add(Box.createVerticalStrut(20));
      pBox.add(pButtonBox2);
      pBox.add(TemplateUIFactory.createVerticalGap());

      for (AddEntry entry : pAddEntries) {
        pBox.add(entry);
        pBox.add(TemplateUIFactory.createVerticalGap());
      }
    }
    
    pBox.add(TemplateUIFactory.createLargeVerticalGap());
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
      
      {
        Box hbox = TemplateUIFactory.createHorizontalBox();

        pExternal = UIFactory.createParamNameField(external, 150, SwingConstants.LEFT);
        pExternal.setMaximumSize(pExternal.getPreferredSize());
        hbox.add(pExternal);
        hbox.add(TemplateUIFactory.createHorizontalSpacer());
        JButton but = 
          TemplateUIFactory.createRemoveButton(parent, "remove-" + id);
        hbox.add(but);
        hbox.add(Box.createHorizontalGlue());
        pHeader = hbox;
      }
      
      
      {
        pAddBox = TemplateUIFactory.createHorizontalBox();
        pAddBox.add(TemplateUIFactory.createSecondLevelIndent());
        JButton but = 
          TemplateUIFactory.createPanelButton
            ("Add Context", "add", this, "Add another External Sequence context.");
        pAddBox.add(but);
        pAddBox.add(Box.createHorizontalGlue());
      }
      
      {
        pReplaceHeader = TemplateUIFactory.createHorizontalBox();
        pReplaceHeader.add(TemplateUIFactory.createSecondLevelIndent());
        JLabel l1 = UIFactory.createFixedLabel("External Context", 150, SwingConstants.LEFT);
        pReplaceHeader.add(l1);
        pReplaceHeader.add(Box.createHorizontalGlue());
      }

      for (String context : old) {
       if (pAllContexts.contains(context)) {
        addExternalContext(context); 
       }
      }
      
      this.add(pInsideBox);
      
      relayout();
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
      pInsideBox.add(TemplateUIFactory.createVerticalGap());
      pInsideBox.add(pReplaceHeader);
      pInsideBox.add(TemplateUIFactory.createVerticalGap());
      for (int i : pReplaceOrder) {
        ExternalContextEntry entry = pContextFields.get(i);
        pInsideBox.add(entry);
        pInsideBox.add(TemplateUIFactory.createVerticalGap());
      }
      pInsideBox.add(TemplateUIFactory.createVerticalGap());
      pInsideBox.add(pAddBox);
      
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
        pContextFields.remove(id);
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
        
        this.add(TemplateUIFactory.createHorizontalIndent());
        this.add(TemplateUIFactory.createSecondLevelIndent());

        pContext = 
          UIFactory.createCollectionField(pAllContexts, pDialog, 150);
        pContext.setMaximumSize(pContext.getPreferredSize());
        if (context != null)
          pContext.setSelected(context);
        
        this.add(pContext);
        
        this.add(TemplateUIFactory.createHorizontalSpacer());
        
        {
          JButton but = 
            TemplateUIFactory.createRemoveButton(parent, "remove-" + id);
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
    private Box pAddBox;

    private int pReplaceID;
    
    private JParamNameField pExternal;
    private LinkedList<Integer> pReplaceOrder;

    private TreeMap<Integer, ExternalContextEntry> pContextFields;
    
    private static final long serialVersionUID = 561167289784883755L;
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
  private Box pTitleBox;
  private Box pButtonBox;
  private Box pHeaderBox;
  private Box pButtonBox2;
  
  private boolean pHasMissing;
}
