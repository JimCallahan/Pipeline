// $Id: JNodeAnnotationsPanel.java,v 1.26 2009/08/19 23:51:37 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   A N N O T A T I O N   P A N E L                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the annotations associated with a node. <P> 
 */ 
public  
class JNodeAnnotationsPanel
  extends JBaseNodeDetailPanel
  implements ComponentListener, DocumentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  public 
  JNodeAnnotationsPanel()
  {
    super();
    initUI();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  public 
  JNodeAnnotationsPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  private void 
  initUI()
  {
    /* initialize fields */ 
    {
      pAnnotationsPanels = new TreeMap<String,JAnnotationPanel>();
      pDeadAnnotations = new TreeSet<String>();
      pDocToAnnotation = new ListMap<Document, String>();
    }

    /* initialize the popup menus */ 
    {
      initBasicMenus(true, true); 
      updateMenuToolTips();
    }

    /* initialize the panel components */ 
    {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  

      /* header */ 
      {
        pApplyToolTipText   = "Apply the changes to node annotations.";
        pUnApplyToolTipText = "There are no unsaved changes to Apply at this time."; 

	JPanel panel = initHeader(true);
	add(panel);
      }

      add(Box.createRigidArea(new Dimension(0, 4)));

      /* full node name */ 
      {
        initNameField(this);
        pNodeNameField.setFocusable(true);     
        pNodeNameField.addKeyListener(this);   
        pNodeNameField.addMouseListener(this); 
      }

      add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
        Box vbox = new Box(BoxLayout.Y_AXIS);
        pAnnotationsBox = vbox;

	{
	  JScrollPane scroll = UIFactory.createVertScrollPane(vbox);
	  add(scroll);
	}
      }

      Dimension size = new Dimension(sTSize+sVSize+58, 120);
      setMinimumSize(size);
      setPreferredSize(size); 

      setFocusable(true);
      addKeyListener(this);
      addMouseListener(this); 
    }

    updateNodeStatus(null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the title of this type of panel.
   */
  public String 
  getTypeName() 
  {
    return "Node Annotations";
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the group ID. <P> 
   * 
   * Group ID values must be in the range: [1-9]
   * 
   * @param groupID
   *   The new group ID or (0) for no group assignment.
   */ 
  public void
  setGroupID
  (
   int groupID
  )
  {
    UIMaster master = UIMaster.getInstance();

    PanelGroup<JNodeAnnotationsPanel> panels = master.getNodeAnnotationsPanels();

    if(pGroupID > 0)
      panels.releaseGroup(pGroupID);

    pGroupID = 0;
    if((groupID > 0) && panels.isGroupUnused(groupID)) {
      panels.assignGroup(this, groupID);
      pGroupID = groupID;
    }

    master.updateOpsBar();
  }

  /**
   * Is the given group currently unused for this type of panel.
   */ 
  public boolean
  isGroupUnused
  (
   int groupID
  ) 
  {
    PanelGroup<JNodeAnnotationsPanel> panels = 
      UIMaster.getInstance().getNodeAnnotationsPanels();
    return panels.isGroupUnused(groupID);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the contents of the panel read-only. <P> 
   */ 
  public boolean
  isLocked() 
  {
    return !pPrivilegeDetails.isAnnotator();
  }

  /**
   * Set the author and view.
   */ 
  public synchronized void 
  setAuthorView
  (
   String author, 
   String view 
  ) 
  {
    super.setAuthorView(author, view);    

    updatePanels();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update all panels which share the current update channel.
   */ 
  @Override
  public void 
  updatePanels() 
  {
    if (pGroupID != 0) {
      PanelUpdater pu = new PanelUpdater(this);
      pu.execute();
    }
  }

  /**
   * Apply the updated information to this panel.
   * 
   * @param author
   *   Owner of the current working area.
   * 
   * @param view
   *   Name of the current working area view.
   * 
   * @param status
   *   The current status for the node being displayed. 
   */
  public synchronized void 
  applyPanelUpdates
  (
   String author, 
   String view,
   NodeStatus status
  )
  {
    if(!pAuthor.equals(author) || !pView.equals(view)) 
      super.setAuthorView(author, view);    

    updateNodeStatus(status);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components to reflect the current check-in log messages.
   * 
   * @param status
   *   The current node status.
   */
  protected synchronized void 
  updateNodeStatus
  (
   NodeStatus status
  ) 
  {
    super.updateNodeStatus(status, true);

    NodeDetailsLight details = null;
    TreeMap<String,BaseAnnotation> annotations = null;
    if(pStatus != null) {
      details     = pStatus.getLightDetails();
      annotations = pStatus.getAnnotations(); 
    }

    /* annotations */ 
    {
      pAnnotationsBox.removeAll();
      pAnnotationsPanels.clear(); 

      if(annotations != null) {
        for(String aname: annotations.keySet()) {
          BaseAnnotation annot = annotations.get(aname);

          JAnnotationPanel panel = new JAnnotationPanel(this, aname, annot);
          pAnnotationsBox.add(panel);

          pAnnotationsPanels.put(aname, panel);
        }
      }
      
      pAnnotationsBox.add(UIFactory.createFiller(sTSize+sVSize+30));
    }
      
    pAnnotationsBox.revalidate();
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- COMPONENT LISTENER METHODS ----------------------------------------------------------*/

  /**
   * Invoked when the component has been made invisible.
   */ 
  public void 	
  componentHidden(ComponentEvent e) {} 

  /**
   * Invoked when the component's position changes.
   */ 
  public void 
  componentMoved(ComponentEvent e) {} 

  /**
   * Invoked when the component's size changes.
   */ 
  public void 
  componentResized
  (
   ComponentEvent e
  )
  {
    Box box = (Box) e.getComponent();
    
    Dimension size = box.getComponent(1).getSize();

    JPanel spacer = (JPanel) box.getComponent(0);
    spacer.setMaximumSize(new Dimension(7, size.height));
    spacer.revalidate();
    spacer.repaint();
  }
  
  /**
   * Invoked when the component has been made visible.
   */
  public void 
  componentShown(ComponentEvent e) {}



  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    super.actionPerformed(e); 

    String cmd = e.getActionCommand();
    if(cmd.equals("add-annotation"))
      doAddAnnotation();
    else if(cmd.startsWith("annotation-changed:"))
      doAnnotationChanged(cmd.substring(19));
    else if(cmd.startsWith("remove-annotation:"))
      doRemoveAnnotation(cmd.substring(18));
    else if(cmd.startsWith("rename-annotation:"))
      doRenameAnnotation(cmd.substring(18));

    else if(cmd.startsWith("param-changed:"))
      doAnnotationParamChanged(cmd.substring(14));
  }
  
  /*-- DOCUMENT LISTENER METHODS -----------------------------------------------------------*/

  public void 
  changedUpdate
  (
    DocumentEvent e
  )
  {}

  public void 
  insertUpdate
  (
    DocumentEvent e
  )
  {
    Document doc = e.getDocument();
    doAnnotationParamChanged(doc);
  }

  public void 
  removeUpdate
  (
    DocumentEvent e
  )
  {
    Document doc = e.getDocument();
    doAnnotationParamChanged(doc);
  } 


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply any changes to the annotations based on the current settings if the 
   * UI components.
   */ 
  @Override
  public void 
  doApply()
  {
    super.doApply();

    TreeSet<String> dead = new TreeSet<String>(pDeadAnnotations); 
    pDeadAnnotations.clear();

    TreeMap<String,BaseAnnotation> modified = new TreeMap<String,BaseAnnotation>();
    for(String aname : pAnnotationsPanels.keySet()) {
      JAnnotationPanel panel = pAnnotationsPanels.get(aname);
      if(panel.isModified()) {
        modified.put(panel.getName(), panel.getAnnotation());
        if(!aname.equals(panel.getName()))
          dead.add(aname); 
      }
    }
    
    if(!modified.isEmpty() || !dead.isEmpty()) {
      ApplyTask task = new ApplyTask(pStatus.getName(), modified, dead);
      task.start();
    }
  }

  /**
   * Add a new annotation to the node.
   */ 
  public void 
  doAddAnnotation()
  {
    JNewIdentifierDialog diag = 
      new JNewIdentifierDialog(getTopFrame(), "New Annotation", "New Annotation Name:", 
                               null, "Add");
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      String aname = diag.getName();
      if((aname != null) && (aname.length() > 0)) {
        if(pAnnotationsPanels.get(aname) != null) {
          UIMaster.getInstance().showErrorDialog
            ("Error:", 
             "The new annotation name (" + aname + ") is already being used by an " +
             "existing annotation!");
          return;
        }

        JAnnotationPanel panel = new JAnnotationPanel(this, aname); 
        pAnnotationsPanels.put(aname, panel); 

        pDeadAnnotations.remove(aname);
        unsavedChange("Annotation Added: " + aname);

        pAnnotationsBox.remove(pAnnotationsBox.getComponentCount()-1);
        pAnnotationsBox.add(panel);
        pAnnotationsBox.add(UIFactory.createFiller(sTSize+sVSize+30));
        pAnnotationsBox.revalidate();        
      }
    }
  }

  /**
   * Update the appearance of the annotation fields after a change of value.
   */ 
  public void 
  doAnnotationChanged
  (
   String aname
  ) 
  {
    JAnnotationPanel panel = pAnnotationsPanels.get(aname);
    if(panel != null) 
      panel.doAnnotationChanged();
  }

  /**
   * Remove the given annotation panel.
   */ 
  public void 
  doRemoveAnnotation
  (
   String aname
  ) 
  {
    JAnnotationPanel panel = pAnnotationsPanels.get(aname);
    if(panel != null) {
      pAnnotationsBox.remove(panel);
      pAnnotationsBox.revalidate();   

      pDeadAnnotations.add(aname);     
      unsavedChange("Annotation Removed: " + aname);
    }
  }
  
  /**
   * Rename the given annotation panel.
   */ 
  public void 
  doRenameAnnotation
  (
   String aname
  ) 
  {
    JAnnotationPanel panel = pAnnotationsPanels.get(aname);
    if(panel != null) {
      JNewIdentifierDialog diag = 
	new JNewIdentifierDialog(getTopFrame(), "Rename Annotation", "New Annotation Name:", 
				 aname, "Rename");
      diag.setVisible(true);
      if(diag.wasConfirmed()) {
        String nname = diag.getName();
        if((nname != null) && (nname.length() > 0) && !nname.equals(aname)) {
          panel.renameAnnotation(nname);
          pDeadAnnotations.remove(nname);
          unsavedChange("Annotation Renamed from: " + aname + " to " + nname);
        }
      }
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Notify the panel that an annotation parameter has changed value.
   */ 
  public void 
  doAnnotationParamChanged
  (
   String args
  ) 
  {
    String parts[] = args.split(":");
    if((parts.length == 2) && (parts[0].length() > 0) && (parts[1].length() > 0)) {
      String name  = parts[0];
      String aname = parts[1];

      JAnnotationPanel panel = pAnnotationsPanels.get(name);
      if(panel != null) 
        panel.annotationParamChanged(name, aname); 
    }
  }
  
  /**
   * Notify the panel that an annotation parameter has changed value.
   */ 
  public void 
  doAnnotationParamChanged
  (
   Document doc
  ) 
  {
    String name = pDocToAnnotation.get(doc);
    JAnnotationPanel panel = pAnnotationsPanels.get(name);
    if(panel != null) 
      panel.annotationParamChanged(name, doc); 
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A component representing a node annotation plugin instance.
   */ 
  private 
  class JAnnotationPanel
    extends JPanel
  {
    /**
     * Construct a new annotation panel.
     */ 
    public 
    JAnnotationPanel
    (
     JNodeAnnotationsPanel parent, 
     String name
    ) 
    {
      this(parent, name, null);
    }

    /**
     * Construct a new annotation panel.
     */ 
    public 
    JAnnotationPanel
    (
     JNodeAnnotationsPanel parent, 
     String name,
     BaseAnnotation annot
    ) 
    {
      super();

      /* initialize fields */ 
      {
	pName = name; 
	pAnnotation = annot; 
        pParent = parent; 
        pParamComponents = new TreeMap<String,Component>();
        pDocToParamName = new ListMap<Document, String>();
      }

      /* panel components */ 
      {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	
        Box vbox = new Box(BoxLayout.Y_AXIS);
        {
          Component comps[] = UIFactory.createTitledPanels();
          JPanel tpanel = (JPanel) comps[0];
          JPanel vpanel = (JPanel) comps[1];
          
          if(pPrivilegeDetails.isAnnotator()) {
            tpanel.add(Box.createRigidArea(new Dimension(0, 19)));
            
            Box hbox = new Box(BoxLayout.X_AXIS);
            
            {
              JButton btn = new JButton("Rename...");
              btn.setName("ValuePanelButton");
              btn.setRolloverEnabled(false);
              btn.setFocusable(false);
              
              Dimension size = new Dimension(sTSize/2-2, 19);
              btn.setMinimumSize(size);
              btn.setPreferredSize(size);
              btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
              
              btn.setActionCommand("rename-annotation:" + pName);
              btn.addActionListener(pParent);
              
              hbox.add(btn);
            }
            
            hbox.add(Box.createRigidArea(new Dimension(4, 0)));
            
            {
              JButton btn = new JButton("Remove...");
              btn.setName("ValuePanelButton");
              btn.setRolloverEnabled(false);
              btn.setFocusable(false);
              
              Dimension size = new Dimension(sTSize/2-2, 19);
              btn.setMinimumSize(size);
              btn.setPreferredSize(size);
              btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
              
              btn.setActionCommand("remove-annotation:" + pName);
              btn.addActionListener(pParent);
              
              hbox.add(btn);
            }
            
            vpanel.add(hbox);
          
            UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
          }
	
          {
            JLabel label = 
              UIFactory.createFixedLabel
              ("Annotation:", sTSize, JLabel.RIGHT, 
               "The name of the Annotation plugin.");
            
            tpanel.add(label);

            JPluginSelectionField field =  
              UIMaster.getInstance().createAnnotationSelectionField(pGroupID, sVSize);
            pAnnotationField = field;
            
            field.setActionCommand("annotation-changed:" + pName);
            field.addActionListener(pParent);

            field.setEnabled(pPrivilegeDetails.isAnnotator());
            
            vpanel.add(field);
          }
	  
          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
          {
            JTextField field = 
              UIFactory.createTitledTextField
              (tpanel, "Version:", sTSize, 
               vpanel, "-", sVSize, 
               "The revision number of the Annotation plugin.");
            pVersionField = field;
          }
          
          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
          
          {
            JTextField field = 
              UIFactory.createTitledTextField
              (tpanel, "Vendor:", sTSize, 
               vpanel, "-", sVSize, 
               "The name of the Annotation plugin vendor.");
            pVendorField = field;
          }
          
          vbox.add(comps[2]);
        }
        
        {
          Box hbox = new Box(BoxLayout.X_AXIS);
          hbox.addComponentListener(pParent);
          
          hbox.add(UIFactory.createSidebar());
          
          {
            JDrawer drawer = 
              new JDrawer("Annotation Parameters:", null, true);
            drawer.setToolTipText(UIFactory.formatToolTip("Annotation plugin parameters."));
            pParamsDrawer = drawer;

            hbox.add(drawer);
          }  
          
          vbox.add(hbox);
        }

        JDrawer drawer = new JDrawer("Annotation: " + pName, vbox, true);
        drawer.setToolTipText(UIFactory.formatToolTip("Node Annotation."));
        pTopDrawer = drawer;
        add(drawer);
      }
      
      updateAnnotation();
    }

    /**
     * Whether the annotation has been modified since the panel was created.
     */ 
    public boolean 
    isModified() 
    {
      return pIsModified; 
    }

    /**
     * The name of the annotation plugin instance.
     */ 
    public String
    getName() 
    {
      return pName;
    }

    /**
     * Get the updated annotation plugin instance.
     */
    public BaseAnnotation
    getAnnotation() 
    {
      if(pAnnotation != null) {
        for(AnnotationParam aparam : pAnnotation.getParams()) {
          if(!pAnnotation.isParamConstant(aparam.getName())) {
            Component comp = pParamComponents.get(aparam.getName()); 
            Comparable value = null;
            if(aparam instanceof BooleanAnnotationParam) {
              JBooleanField field = (JBooleanField) comp;
              value = field.getValue();
            }
            else if(aparam instanceof DoubleAnnotationParam) {
              JDoubleField field = (JDoubleField) comp;
              value = field.getValue();
            }
            else if(aparam instanceof EnumAnnotationParam) {
              JCollectionField field = (JCollectionField) comp;
              EnumAnnotationParam eparam = (EnumAnnotationParam) aparam;
              value = eparam.getValueOfIndex(field.getSelectedIndex());
            }
            else if(aparam instanceof IntegerAnnotationParam) {
              JIntegerField field = (JIntegerField) comp;
              value = field.getValue();
            }
            else if(aparam instanceof TextAreaAnnotationParam) {
              JTextArea field = (JTextArea) comp;
              value = field.getText();	  
            }
            else if(aparam instanceof StringAnnotationParam) {
              JTextField field = (JTextField) comp;
              value = field.getText();	  
            }
            else if(aparam instanceof ParamNameAnnotationParam) {
              JTextField field = (JTextField) comp;
              value = field.getText();    
            }
            else if(aparam instanceof PathAnnotationParam) {
              JPathField field = (JPathField) comp;
              value = field.getPath();	  
            }
            else if(aparam instanceof ToolsetAnnotationParam) {
              JCollectionField field = (JCollectionField) comp;
              String toolset = field.getSelected();
              if(toolset.equals("-") || (toolset.length() == 0))
                value = null;
              else 
                value = toolset;
            }
            else if(aparam instanceof WorkGroupAnnotationParam) {
              JCollectionField field = (JCollectionField) comp;
              String ugname = field.getSelected(); 
              if(ugname.equals("-") || (ugname.length() == 0))
                value = null;
              else if(ugname.startsWith("[") && ugname.endsWith("]"))
                value = ugname.substring(1, ugname.length()-1);
              else 
                value = ugname;
            }
            else if(aparam instanceof BuilderIDAnnotationParam) {
              JBuilderIDSelectionField field = (JBuilderIDSelectionField) comp;
              value = field.getBuilderID();
            }
            else {
              assert(false) : "Unknown annotation parameter type!";
            }

            pAnnotation.setParamValue(aparam.getName(), value);
          }
        }
      }
      
      return pAnnotation;
    }

    /**
     * Rename the annotation.
     */ 
    public void 
    renameAnnotation
    (
     String aname
    ) 
    {
      pName = aname; 
      pAnnotationField.setActionCommand("annotation-changed:" + pName);
      pTopDrawer.setTitle("Annotation: " + pName);
      pIsModified = true;
    }

    /**
     * Notify the panel that an annotation parameter has changed value.
     */ 
    public void 
    annotationParamChanged
    (
     String name, 
     String aname
    ) 
    {
      unsavedChange("Parameter Changed: " + name + " (" + aname + ")"); 
      pIsModified = true;
    }
    
    /**
     * Notify the panel that an annotation parameter has changed value.
     */ 
    public void 
    annotationParamChanged
    (
     String name,
     Document doc
    ) 
    {
      String aname = pDocToParamName.get(doc);
      unsavedChange("Parameter Changed: " + name + " (" + aname + ")"); 
      pIsModified = true;
    }

    /**
     * Update the UI components.
     */
    private void 
    updateAnnotation() 
    {
      UIMaster.getInstance().updateAnnotationPluginField(pGroupID, pAnnotationField); 

      updateAnnotationFields();
      updateAnnotationParams();
    }

    /**
     * Update the annotation name, version and vendor fields.
     */ 
    private void 
    updateAnnotationFields()
    {
      pAnnotationField.removeActionListener(pParent);
      {
        pAnnotationField.setPlugin(pAnnotation);
        if(pAnnotation != null) {
          pVersionField.setText("v" + pAnnotation.getVersionID());
          pVendorField.setText(pAnnotation.getVendor());
        }
        else {
          pVersionField.setText("-");
          pVendorField.setText("-");
        }
      }
      pAnnotationField.addActionListener(pParent);
    }
  
    /**
     * Update the UI components associated annotation parameters.
     */ 
    private void 
    updateAnnotationParams() 
    {
      /* lookup common server info... */ 
      TreeSet<String> toolsets = null; 
      Set<String> workUsers  = null;
      Set<String> workGroups = null;
      if(pAnnotation != null) {
        UIMaster master = UIMaster.getInstance();
        MasterMgrClient mclient = master.acquireMasterMgrClient();
        UICache cache = master.getUICache(pGroupID);
        try {
          boolean needsToolsets = false;
          boolean needsWorkGroups = false;
          for(AnnotationParam aparam : pAnnotation.getParams()) {
            if(aparam instanceof ToolsetAnnotationParam) 
              needsToolsets = true;
            else if(aparam instanceof WorkGroupAnnotationParam) 
              needsWorkGroups = true;
          }
          
          if(needsToolsets) {
            toolsets = new TreeSet<String>();
            toolsets.add("-");
            try {
              toolsets.addAll(cache.getCachedActiveToolsetNames());
            }
            catch(PipelineException ex) {
            }
          }
          
          if(needsWorkGroups) {
            try {
              WorkGroups wgroups = cache.getCachedWorkGroups();
              workGroups = wgroups.getGroups();
              workUsers  = wgroups.getUsers();
            }
            catch(PipelineException ex) {
              workGroups = new TreeSet<String>(); 
              workUsers  = new TreeSet<String>(); 
            }
          }
        }
        finally {
          master.releaseMasterMgrClient(mclient);
        }
      }

      pParamComponents.clear();
      
      boolean first = true;
      if((pAnnotation != null) && pAnnotation.hasParams()) {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];

        for(String pname : pAnnotation.getLayout()) {
          if(pname == null) {
            UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
          }
          else {
            if(!first) 
              UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
            
            AnnotationParam aparam = pAnnotation.getParam(pname);

            boolean paramEnabled = 
              (!pAnnotation.isParamConstant(pname) && 
               (pPrivilegeDetails.isAnnotator() ||
                pAnnotation.isParamModifiable(pname, PackageInfo.sUser, pPrivilegeDetails))); 

            if(aparam != null) {
              if(aparam instanceof BooleanAnnotationParam) {
                Boolean value = (Boolean) aparam.getValue();
                JBooleanField field = 
                  UIFactory.createTitledBooleanField 
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, sVSize, 
                   aparam.getDescription());
                field.setValue(value);

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);
                
                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);
              }
              else if(aparam instanceof DoubleAnnotationParam) {
                Double value = (Double) aparam.getValue();
                JDoubleField field = 
                  UIFactory.createTitledDoubleField 
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, 
                   aparam.getDescription());

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);
              }
              else if(aparam instanceof EnumAnnotationParam) {
                EnumAnnotationParam eparam = (EnumAnnotationParam) aparam;
	      
                JCollectionField field = 
                  UIFactory.createTitledCollectionField
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, eparam.getValues(), sVSize, 
                   aparam.getDescription());
	      
                field.setSelected((String) eparam.getValue());

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);
              }
              else if(aparam instanceof IntegerAnnotationParam) {
                Integer value = (Integer) aparam.getValue();
                JIntegerField field = 
                  UIFactory.createTitledIntegerField 
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, 
                   aparam.getDescription());

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);
              }
              else if(aparam instanceof TextAreaAnnotationParam) {
        	TextAreaAnnotationParam bparam = (TextAreaAnnotationParam) aparam; 
                String value = (String) aparam.getValue();
                JTextArea field = 
                  UIFactory.createTitledEditableTextArea
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, bparam.getRows(), true,
                   aparam.getDescription());
                
                Document doc = field.getDocument();
                doc.addDocumentListener(pParent);
                pDocToParamName.put(doc, pname);
                pDocToAnnotation.put(doc, pName);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);	      
              }
              else if(aparam instanceof StringAnnotationParam) {
                String value = (String) aparam.getValue();
                JTextField field = 
                  UIFactory.createTitledEditableTextField 
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, 
                   aparam.getDescription());

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);	      
              }
              else if(aparam instanceof ParamNameAnnotationParam) {
                String value = (String) aparam.getValue();
                JTextField field = 
                  UIFactory.createTitledParamNameField 
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, 
                   aparam.getDescription());

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);           
              }
              else if(aparam instanceof PathAnnotationParam) {
                Path value = (Path) aparam.getValue();
                JPathField field = 
                  UIFactory.createTitledPathField 
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, 
                   aparam.getDescription());

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);	      
              }
              else if(aparam instanceof ToolsetAnnotationParam) {
                String value = (String) aparam.getValue();

                TreeSet<String> values = new TreeSet<String>(toolsets);
                if((value != null) && !values.contains(value))
                  values.add(value); 

                JCollectionField field = 
                  UIFactory.createTitledCollectionField
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, values, sVSize, 
                   aparam.getDescription());

                if(value != null) 
                  field.setSelected(value);
                else 
                  field.setSelected("-");

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);	      
              }
              else if(aparam instanceof WorkGroupAnnotationParam) {
                WorkGroupAnnotationParam wparam = (WorkGroupAnnotationParam) aparam;
                String value = (String) aparam.getValue();

                TreeSet<String> values = new TreeSet<String>();
                values.add("-");
                if(wparam.allowsGroups()) {
                  for(String gname : workGroups) 
                    values.add("[" + gname + "]"); 
                }
                if(wparam.allowsUsers()) 
                  values.addAll(workUsers);
                
                JCollectionField field = 
                  UIFactory.createTitledCollectionField
                  (tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, values, sVSize, 
                   aparam.getDescription());
                
                if(value == null) 
                  field.setSelected("-");
                else {                  
                  if(wparam.allowsGroups() && workGroups.contains(value))
                    field.setSelected("[" + value + "]");
                  else if(wparam.allowsUsers() && workUsers.contains(value))
                    field.setSelected(value);
                  else 
                    field.setSelected("-");
                }

                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);	                      
              }
              else if(aparam instanceof BuilderIDAnnotationParam) {
                BuilderID value = (BuilderID) aparam.getValue();
                JBuilderIDSelectionField field = 
                  UIMaster.getInstance().createTitledBuilderIDSelectionField
                  (pGroupID, tpanel, aparam.getNameUI() + ":", sTSize-7, 
                   vpanel, value, sVSize, 
                   aparam.getDescription());
                   
                field.setActionCommand("param-changed:" + pName + ":" + pname);
                field.addActionListener(pParent);

                field.setEnabled(paramEnabled); 

                pParamComponents.put(pname, field);	    
              }
              else {
                assert(false) : 
                  ("Unknown annotation parameter type (" + aparam.getName() + ")!");
              }
            }
          }
	
          first = false;
        }

        UIFactory.addVerticalGlue(tpanel, vpanel);

        pParamsDrawer.setContents((JComponent) comps[2]);
      }
      else {
        pParamsDrawer.setContents(null);
      }
    }    

    /**
     * Update the appearance of the annotation fields after a change of value.
     */ 
    private void 
    doAnnotationChanged()
    {
      BaseAnnotation oannot = getAnnotation();

      String aname = pAnnotationField.getPluginName();
      if(aname == null) {
        pAnnotation = null;
      }
      else {
        VersionID avid = pAnnotationField.getPluginVersionID();
        String avendor = pAnnotationField.getPluginVendor();
        
        if((oannot == null) || 
           !oannot.getName().equals(aname) ||
           !oannot.getVersionID().equals(avid) ||
           !oannot.getVendor().equals(avendor)) {
          try {
            pAnnotation = PluginMgrClient.getInstance().newAnnotation(aname, avid, avendor);
            if(oannot != null)
              pAnnotation.setParamValues(oannot);
            unsavedChange("Annotation Changed: " + aname);
          }
          catch(PipelineException ex) {
            UIMaster.getInstance().showErrorDialog(ex);
            pAnnotation = null;	    
          }
        }
      }
      
      updateAnnotationFields();
      updateAnnotationParams();

      pIsModified = true;
    }

    

    private static final long serialVersionUID = -391904734639424578L;

    private String          pName; 
    private BaseAnnotation  pAnnotation; 
    private boolean         pIsModified; 

    private JNodeAnnotationsPanel  pParent; 
    private JDrawer                pTopDrawer;
 
    private JPluginSelectionField  pAnnotationField;
    private JTextField             pVersionField; 
    private JTextField             pVendorField; 

    private JDrawer                    pParamsDrawer; 
    private TreeMap<String,Component>  pParamComponents; 
    
    private ListMap<Document, String> pDocToParamName;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Apply the annotations changes. 
   */ 
  private
  class ApplyTask
    extends Thread
  {
    public 
    ApplyTask
    (
     String name, 
     TreeMap<String,BaseAnnotation> modified, 
     TreeSet<String> dead
    ) 
    {
      super("JNodeAnnotationsPanel:ApplyTask");

      pNodeName = name; 
      pModified = modified;
      pDead = dead;
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      if(master.beginPanelOp(pGroupID, "Applying Annotation Changes...")) {
        MasterMgrClient mclient = master.acquireMasterMgrClient();
	try {
          for(String aname : pDead) 
            mclient.removeAnnotation(pNodeName, aname); 

          for(String aname : pModified.keySet()) {
            BaseAnnotation annot = pModified.get(aname); 
            mclient.addAnnotation(pNodeName, aname, annot);
          }
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.releaseMasterMgrClient(mclient);
	  master.endPanelOp(pGroupID, "Done.");
	}

	updatePanels();
      }
    }

    private String                          pNodeName; 
    private TreeMap<String,BaseAnnotation>  pModified; 
    private TreeSet<String>                 pDead; 
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1091065151346911428L;
  
  private static final int  sTSize = 150;
  private static final int  sVSize = 150;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The top-level annotations box.
   */ 
  private Box pAnnotationsBox;

  /**
   * The annotation UI components indexed by annotation name.
   */
  private TreeMap<String,JAnnotationPanel>  pAnnotationsPanels; 

  /**
   * The names of obsolete annotations. 
   */ 
  private TreeSet<String>  pDeadAnnotations;

  /**
   * The scroll panel containing the messages.
   */ 
  private JScrollPane  pScroll;
  
  /**
   * The annotation names indexed by the TextArea parameter documents.
   */ 
  private ListMap<Document, String> pDocToAnnotation;
}
