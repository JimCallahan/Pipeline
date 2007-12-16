// $Id: JKeyChooserConfigDialog.java,v 1.1 2007/12/16 06:32:49 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

import us.temerity.pipeline.*;
import us.temerity.pipeline.param.key.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   K E Y   C H O O S E R   C O N F I G   D I A L O G                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Dialog used to create/modify key chooser configurations.
 */ 
public 
class JKeyChooserConfigDialog
  extends JFullDialog
  implements ComponentListener, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JKeyChooserConfigDialog
  (
   Frame owner, 
   String title
  )
  {
    super(owner, title + " Key Configuration");
    
    /* initialize fields */ 
    {
      pParamComponents = new TreeMap<String,Component>();
      pParamGroupsOpen = new TreeMap<String,Boolean>();
    }

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));  

      {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
      
        {
          JLabel label = 
            UIFactory.createFixedLabel
            (title + " Key Chooser:", sTSize, JLabel.RIGHT, 
             "The name of the " + title + " Key Chooser plugin.");

          tpanel.add(label);

          JPluginSelectionField field = createKeyChooserPluginField(); 
          pKeyChooserField = field;
          
          field.setActionCommand("chooser-changed");
          field.addActionListener(this);
          
          vpanel.add(field);
        }
          
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
          
        {
          JTextField field = 
            UIFactory.createTitledTextField
            (tpanel, "Version:", sTSize, 
             vpanel, "-", sVSize, 
             "The revision number of the " + title + " Key Chooser plugin.");
          pVersionField = field;
        }

        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        {
          JTextField field = 
            UIFactory.createTitledTextField
            (tpanel, "Vendor:", sTSize, 
             vpanel, "-", sVSize, 
             "The name of the " + title + " Key Chooser plugin vendor.");
          pVendorField = field;
        }
        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        body.add(comps[2]);
      }

      {
        Box pbox = new Box(BoxLayout.Y_AXIS);
        pParamsBox = pbox;
        
        body.add(pbox);
      }

      {
        JPanel spanel = new JPanel();
        spanel.setName("Spacer");
        
        spanel.setMinimumSize(new Dimension(sTSize+sVSize, 7));
        spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        spanel.setPreferredSize(new Dimension(sTSize+sVSize, 7));
        
        body.add(spanel);
      }

      JScrollPane scroll = UIFactory.createVertScrollPane(body, sTSize+sVSize+52, 300);
      
      super.initUI(title + " Key Chooser Configuration:", scroll, 
                   "Confirm", null, null, "Cancel");
      pack();
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the key chooser.
   */ 
  public void
  setKeyChooser
  (
   BaseKeyChooser plugin
  ) 
  {
    pKeyChooser = plugin;
    updateKeyChooser();
  }
  
  /**
   * Get the extension plugin instance.
   */
  public BaseKeyChooser
  getKeyChooser()
  {
    if(pKeyChooser != null) {
      for(KeyParam aparam : pKeyChooser.getParams()) {
        Component comp = pParamComponents.get(aparam.getName()); 
        Comparable value = null;
        if(aparam instanceof BooleanKeyParam) {
          JBooleanField field = (JBooleanField) comp;
          value = field.getValue();
        }
        else if(aparam instanceof ByteSizeKeyParam) {
          JByteSizeField field = (JByteSizeField) comp;
          value = field.getValue();       
        }
        else if(aparam instanceof DoubleKeyParam) {
          JDoubleField field = (JDoubleField) comp;
          value = field.getValue();
        }
        else if(aparam instanceof EnumKeyParam) {
          JCollectionField field = (JCollectionField) comp;
          EnumKeyParam eparam = (EnumKeyParam) aparam;
          value = eparam.getValueOfIndex(field.getSelectedIndex());
        }
        else if(aparam instanceof UserNameKeyParam) {
          JCollectionField field = (JCollectionField) comp;
          String ugname = field.getSelected(); 
          if(ugname.equals("-") || (ugname.length() == 0))
            value = null;
          else if(ugname.startsWith("[") && ugname.endsWith("]"))
            value = ugname.substring(1, ugname.length()-1);
          else 
            value = ugname;
        }
        else if(aparam instanceof IntegerKeyParam) {
          JIntegerField field = (JIntegerField) comp;
          value = field.getValue();
        }
        else if(aparam instanceof StringKeyParam) {
          JTextField field = (JTextField) comp;
          value = field.getText();        
        }
        else if(aparam instanceof PathKeyParam) {
          JPathField field = (JPathField) comp;
          value = field.getPath();        
        }
        else {
          assert(false) : "Unknown extension parameter type!";
        }

        pKeyChooser.setParamValue(aparam.getName(), value);
      }
    }

    return pKeyChooser;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create the field for editing the server extension plugin.
   */ 
  private JPluginSelectionField
  createKeyChooserPluginField()
  {
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = 
      PluginMgrClient.getInstance().getKeyChoosers();
    
    PluginMenuLayout layout = new PluginMenuLayout();
    for(String avendor : plugins.keySet()) {
      PluginMenuLayout vmenu = new PluginMenuLayout(avendor);
      layout.add(vmenu);
      
      for(String aname : plugins.keySet(avendor)) {
        PluginMenuLayout nmenu = new PluginMenuLayout(aname);
        vmenu.add(nmenu);
        
        for(VersionID avid : plugins.keySet(avendor, aname)) {
          PluginMenuLayout item = new PluginMenuLayout("v" + avid, aname, avid, avendor);
          nmenu.add(item);
        }
      }
    }
    
    return UIFactory.createPluginSelectionField(layout, plugins, sVSize);
  }
  
  /**
   * Create the field for editing the server extension plugin.
   */ 
  private void 
  updateKeyChooserField()
  {
    TripleMap<String,String,VersionID,TreeSet<OsType>> plugins = 
      PluginMgrClient.getInstance().getKeyChoosers();
    
    PluginMenuLayout layout = new PluginMenuLayout();
    for(String avendor : plugins.keySet()) {
      PluginMenuLayout vmenu = new PluginMenuLayout(avendor);
      layout.add(vmenu);
      
      for(String aname : plugins.keySet(avendor)) {
        PluginMenuLayout nmenu = new PluginMenuLayout(aname);
        vmenu.add(nmenu);
        
        for(VersionID avid : plugins.keySet(avendor, aname)) {
          PluginMenuLayout item = new PluginMenuLayout("v" + avid, aname, avid, avendor);
          nmenu.add(item);
        }
      }
    }
    pKeyChooserField.updatePlugins(layout, plugins);

    updateKeyChooserFields();
    updateKeyChooserParams();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components.
   */
  private void 
  updateKeyChooser() 
  { 
    updateKeyChooserField();
    updateKeyChooserFields();
    updateKeyChooserParams();

    updateButton();
  }

  /**
   * Update the extension name, version and vendor fields.
   */ 
  private void 
  updateKeyChooserFields()
  {
    pKeyChooserField.removeActionListener(this);
    {
      pKeyChooserField.setPlugin(pKeyChooser);
      if(pKeyChooser != null) {
        pVersionField.setText("v" + pKeyChooser.getVersionID());
        pVendorField.setText(pKeyChooser.getVendor());
      }
      else {
        pVersionField.setText("-");
        pVendorField.setText("-");
      }
    }
    pKeyChooserField.addActionListener(this);
  }
  
  /**
   * Update the UI components associated archiver parameters.
   */ 
  private void 
  updateKeyChooserParams() 
  {
    pParamsBox.removeAll();
    pParamComponents.clear();

    {
      Box hbox = new Box(BoxLayout.X_AXIS);
      hbox.addComponentListener(this);
      
      {
        JPanel spanel = new JPanel();
        spanel.setName("Spacer");
        
        spanel.setMinimumSize(new Dimension(7, 0));
        spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
        spanel.setPreferredSize(new Dimension(7, 0));
        
        hbox.add(spanel);
      }

      if((pKeyChooser != null) && pKeyChooser.hasParams()) {
        Set<String> workUsers  = null;
        Set<String> workGroups = null;
        boolean needsWorkGroups = false;
        for(KeyParam aparam : pKeyChooser.getParams()) {
          if(aparam instanceof UserNameKeyParam) 
            needsWorkGroups = true;
        }
        if(needsWorkGroups) {
          try {
            UIMaster master = UIMaster.getInstance();
            MasterMgrClient mclient = master.getMasterMgrClient();
            WorkGroups wgroups = mclient.getWorkGroups();
            workGroups = wgroups.getGroups();
            workUsers  = wgroups.getUsers();
          }
          catch(PipelineException ex) {
            workGroups = new TreeSet<String>(); 
            workUsers  = new TreeSet<String>(); 
          }
        }
        updateParamsHelper(pKeyChooser.getLayout(), hbox, 1, workGroups, workUsers);
      }
      else {
        Box dbox = new Box(BoxLayout.Y_AXIS);  
        hbox.add(new JDrawer("Key Chooser Parameters:", dbox, false));
      }

      pParamsBox.add(hbox);
    }

    pParamsBox.revalidate();
    pParamsBox.repaint();    
  }

  
  /**
   * Recursively create drawers containing the extension parameters.
   */ 
  private void 
  updateParamsHelper
  (
   LayoutGroup group, 
   Box sbox, 
   int level,
   Set<String> workGroups,
   Set<String> workUsers
  ) 
  {
    Box dbox = new Box(BoxLayout.Y_AXIS);    
    if(!group.getEntries().isEmpty()) {
      Component comps[] = UIFactory.createTitledPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];

      boolean first = true;
      for(String pname : group.getEntries()) {
        if(pname == null) {
          UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
        }
        else {
          if(!first) 
            UIFactory.addVerticalSpacer(tpanel, vpanel, 4);

          int tsize = sTSize-7*level;

          KeyParam aparam = pKeyChooser.getParam(pname);
          if(aparam != null) {
            if(aparam instanceof BooleanKeyParam) {
              Boolean value = (Boolean) aparam.getValue();
              JBooleanField field = 
                UIFactory.createTitledBooleanField 
                (tpanel, aparam.getNameUI() + ":", tsize, 
                 vpanel, sVSize, 
                 aparam.getDescription());
              field.setValue(value);

              pParamComponents.put(pname, field);
            }
            else if(aparam instanceof ByteSizeKeyParam) {
              Long value = (Long) aparam.getValue();
              JByteSizeField field = 
                UIFactory.createTitledByteSizeField 
                (tpanel, aparam.getNameUI() + ":", tsize, 
                 vpanel, value, sVSize, 
                 aparam.getDescription());

              pParamComponents.put(pname, field);
            }
            else if(aparam instanceof DoubleKeyParam) {
              Double value = (Double) aparam.getValue();
              JDoubleField field = 
                UIFactory.createTitledDoubleField 
                (tpanel, aparam.getNameUI() + ":", tsize, 
                 vpanel, value, sVSize, 
                 aparam.getDescription());

              pParamComponents.put(pname, field);
            }
            else if(aparam instanceof EnumKeyParam) {
              EnumKeyParam eparam = (EnumKeyParam) aparam;
              
              JCollectionField field = 
                UIFactory.createTitledCollectionField
                (tpanel, aparam.getNameUI() + ":", tsize, 
                 vpanel, eparam.getValues(), sVSize, 
                 aparam.getDescription());
              
              field.setSelected((String) eparam.getValue());

              pParamComponents.put(pname, field);
            }
            else if(aparam instanceof IntegerKeyParam) {
              Integer value = (Integer) aparam.getValue();
              JIntegerField field = 
                UIFactory.createTitledIntegerField 
                (tpanel, aparam.getNameUI() + ":", tsize, 
                 vpanel, value, sVSize, 
                 aparam.getDescription());

              pParamComponents.put(pname, field);
            }
            else if(aparam instanceof PathKeyParam) {
              Path value = (Path) aparam.getValue();
              JPathField field = 
                UIFactory.createTitledPathField
                (tpanel, aparam.getNameUI() + ":", tsize, 
                 vpanel, value, sVSize,  
                 aparam.getDescription());

              pParamComponents.put(pname, field);             
            }
            else if(aparam instanceof StringKeyParam) {
              String value = (String) aparam.getValue();
              JTextField field = 
                UIFactory.createTitledEditableTextField 
                (tpanel, aparam.getNameUI() + ":", tsize, 
                 vpanel, value, sVSize, 
                 aparam.getDescription());

              pParamComponents.put(pname, field);             
            }
            else if(aparam instanceof UserNameKeyParam) {
              UserNameKeyParam wparam = (UserNameKeyParam) aparam;
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
            }
            else {
              assert(false) : "Unknown key parameter type!";
            }
          }
        }
        
        first = false;
      }

      dbox.add(comps[2]);
    }

    if(!group.getSubGroups().isEmpty())  {
      Box hbox = new Box(BoxLayout.X_AXIS);
      hbox.addComponentListener(this);

      {
        JPanel spanel = new JPanel();
        spanel.setName("Spacer");
        
        spanel.setMinimumSize(new Dimension(7, 0));
        spanel.setMaximumSize(new Dimension(7, Integer.MAX_VALUE));
        spanel.setPreferredSize(new Dimension(7, 0));
        
        hbox.add(spanel);
      }

      {
        Box vbox = new Box(BoxLayout.Y_AXIS);
        for(LayoutGroup sgroup : group.getSubGroups()) 
          updateParamsHelper(sgroup, vbox, level+1, workGroups, workUsers);

        hbox.add(vbox);
      }

      dbox.add(hbox);
    }

    {
      JDrawer drawer = new JDrawer(group.getNameUI() + ":", dbox, true);
      drawer.addActionListener(new UpdateParamGroupsOpen(group.getName(), drawer));
      drawer.setToolTipText(UIFactory.formatToolTip(group.getDescription()));
      sbox.add(drawer);
      
      Boolean isOpen = pParamGroupsOpen.get(group.getName());
      if(isOpen == null) {
        isOpen = group.isOpen();
        pParamGroupsOpen.put(group.getName(), isOpen);
      }
      drawer.setIsOpen(isOpen);
    }
  }

  /**
   * Update the enable status of the update button.
   */ 
  private void 
  updateButton()
  {
    BaseKeyChooser ext = getKeyChooser();
    pConfirmButton.setEnabled(ext != null);
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
    String cmd = e.getActionCommand();
    if(cmd.equals("chooser-changed")) 
      doKeyChooserChanged();
    else
      super.actionPerformed(e);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get a new instance of the given key chooser plugin.
   */ 
  private BaseKeyChooser
  newKeyChooserPlugin
  (
   String ename, 
   VersionID evid, 
   String evendor
  )
    throws PipelineException
  {
    return PluginMgrClient.getInstance().newKeyChooser(ename, evid, evendor);
  }


  /**
   * Update the appearance of the key chooser fields after a change of value.
   */ 
  private void 
  doKeyChooserChanged()
  {
    BaseKeyChooser oext = getKeyChooser();
    
    String ename = pKeyChooserField.getPluginName();
    if(ename == null) {
      pKeyChooser = null;
      
      pParamComponents.clear();
      pParamGroupsOpen.clear();
    }
    else {
      VersionID evid = pKeyChooserField.getPluginVersionID();
      String evendor = pKeyChooserField.getPluginVendor();
      if((oext == null) || !oext.getName().equals(ename) ||
         (evid == null) || !evid.equals(oext.getVersionID()) ||
         (evendor == null) || !evendor.equals(oext.getVendor())) {
        try {
          pKeyChooser = newKeyChooserPlugin(ename, evid, evendor);
          if(oext != null) 
            pKeyChooser.setParamValues(oext);
        }
        catch(PipelineException ex) {
          showErrorDialog(ex);

          pKeyChooser = null;        

          pKeyChooserField.removeActionListener(this);
          pKeyChooserField.setPlugin(null);
          pKeyChooserField.addActionListener(this);
        }
      
        pParamComponents.clear();
        pParamGroupsOpen.clear();
      }
    }
    
    updateKeyChooserFields();
    updateKeyChooserParams();

    updateButton();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Update the table of whether each parameter group is open when the drawer is opened
   * or closed.
   */ 
  private
  class UpdateParamGroupsOpen
    implements ActionListener
  {
    public 
    UpdateParamGroupsOpen
    (
     String name, 
     JDrawer drawer
    ) 
    {
      pName   = name;
      pDrawer = drawer;
    }

    /** 
     * Invoked when an action occurs. 
     */ 
    public void 
    actionPerformed
    (
     ActionEvent e
    ) 
    {
      pParamGroupsOpen.put(pName, pDrawer.isOpen());
    }
    
    private String   pName;
    private JDrawer  pDrawer;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -629592434639424578L;
  
  protected static final int sTSize = 180;
  protected static final int sVSize = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current key chooser instance.
   */ 
  private BaseKeyChooser pKeyChooser; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the extension plugin. 
   */ 
  private JPluginSelectionField  pKeyChooserField; 

  /**
   * The revision number of the key chooser plugin.
   */ 
  private JTextField  pVersionField;

  /**
   * The name of the key chooser plugin vendor. 
   */ 
  private JTextField  pVendorField; 


  /**
   * The box containing key chooser parameter components.
   */ 
  private Box  pParamsBox;

  /**
   * The key chooser parameter components indexed by parameter name.
   */ 
  private TreeMap<String,Component>  pParamComponents; 

  /**
   * Whether the drawers containing the key chooser parameter components are
   * open indexed by parameter group name.
   */ 
  private TreeMap<String,Boolean>  pParamGroupsOpen; 

}
