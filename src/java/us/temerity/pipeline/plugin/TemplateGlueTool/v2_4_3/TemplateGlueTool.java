// $Id: TemplateGlueTool.java,v 1.2 2008/11/19 04:34:48 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_3;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.builder.v2_4_3.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   G L U E   T O O L                                                    */
/*------------------------------------------------------------------------------------------*/

public 
class TemplateGlueTool
  extends TaskToolUtils
{

  public
  TemplateGlueTool()
  {
    super("TemplateGlue", new VersionID("2.4.3"), "Temerity", 
    "Tool for creating and editing a template glue node.");

    addPhase(new ValidInputPass());
    addPhase(new InputTemplateSettings());
    
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
    
    underDevelopment();
  }
  

  
  private void
  doAdd()
  {
    switch(pPhase) {
    case StringReplace:
      {
        UIFactory.addVerticalSpacer(pTpanel, pVpanel, 6);
        JTextField field = makeStringReplacementField("");
        pStringReplaceFields.add(field);
        pVbox.validate();
        break;
      }
    case FrameRanges:
      {
        UIFactory.addVerticalSpacer(pTpanel, pVpanel, 6);
        JTextField field = makeFrameRangeField("");
        pFrameRangeFields.add(field);
        pVbox.validate();
        break;
      }
    case ContextNames:
      {
        UIFactory.addVerticalSpacer(pTpanel, pVpanel, 6);
        JTextField field = makeContextNameField("");
        pContextNameFields.add(field);
        pVbox.validate();
        break;
      }
    case ContextValues:
      {
        UIFactory.addVerticalSpacer(pTpanel, pVpanel, 6);
        JTextField field = makeContextValueField("");
        pContextValueFields.put(pCurrentContext, field);
        pVbox.validate();
        break;
      }
    case ContextDefaults:
      {
        JDrawer draw = makeContextDefaultFields(pContextValues.get(pCurrentContext));
        pVbox2.add(draw);
        pVbox.validate();
      }
    }
  }
  
  
  
  private static
  String[][] getButtons()
  {
    String extra[][] = {
      { "Add",  "add" }
    };
    return extra;
  }
  
  public JTextField 
  makeStringReplacementField
  (
    String value  
  )
  {
    return UIFactory.createTitledEditableTextField
      (pTpanel, "Replacement:", sTSize, pVpanel, value, sVSize, 
      "The name of the string that is going to be replaced in the template.");  
  }
  
  public JTextField 
  makeContextNameField
  (
    String value  
  )
  {
    return UIFactory.createTitledEditableTextField
      (pTpanel, "ContextName:", sTSize, pVpanel, value, sVSize, 
      "The name of the context.");  
  }
  
  public JTextField 
  makeFrameRangeField
  (
    String value  
  )
  {
    return UIFactory.createTitledEditableTextField
      (pTpanel, "FrameRange:", sTSize, pVpanel, value, sVSize, 
      "The name of the frame range.");  
  }
  
  public void
  makeFrameRangeDefaultField
  (
    String name,
    FrameRange value  
  )
  {
    pTpanel.add(UIFactory.createFixedLabel(name + ":", sTSize, JLabel.RIGHT));
    Box hbox = new Box(BoxLayout.X_AXIS);
    
    JIntegerField startFrameField = UIFactory.createIntegerField(null, 60, JLabel.CENTER);
    if (value != null)
      startFrameField.setValue(value.getStart());
    hbox.add(startFrameField);
  
    hbox.add(Box.createHorizontalGlue());
    hbox.add(Box.createRigidArea(new Dimension(8, 0)));
  
    hbox.add(new JLabel("to")); 
  
    hbox.add(Box.createRigidArea(new Dimension(8, 0)));
    hbox.add(Box.createHorizontalGlue());
  
    JIntegerField endFrameField = UIFactory.createIntegerField(null, 60, JLabel.CENTER);
    if (value != null)
      endFrameField.setValue(value.getEnd());
    hbox.add(endFrameField);
  
    hbox.add(Box.createHorizontalGlue());
    hbox.add(Box.createRigidArea(new Dimension(8, 0)));
  
    hbox.add(new JLabel("by"));
  
    hbox.add(Box.createRigidArea(new Dimension(8, 0)));
    hbox.add(Box.createHorizontalGlue());
  
    JIntegerField byFrameField = UIFactory.createIntegerField(null, 60, JLabel.CENTER);
    if (value != null)
      byFrameField.setValue(value.getBy());
    hbox.add(byFrameField); 
  
    Dimension size = new Dimension(sVSize+1, 19);
    hbox.setMinimumSize(size);
    hbox.setMaximumSize(size);
    hbox.setPreferredSize(size);
  
    pVpanel.add(hbox);
    pFrameRangeStartFields.put(name, startFrameField);
    pFrameRangeEndFields.put(name, endFrameField);
    pFrameRangeByFields.put(name, byFrameField);

  }
  
  public JTextField 
  makeContextValueField
  (
    String value  
  )
  {
    return UIFactory.createTitledEditableTextField
      (pTpanel, pCurrentContext + " Value:", sTSize, pVpanel, value, sVSize, 
      "The string to replace in the context.");  
  }
  
  public JDrawer
  makeContextDefaultFields
  (
    TreeSet<String> values
  )
  {
    TreeMap<String, String> map = new TreeMap<String, String>();
    for (String key : values) {
      map.put(key, null);
    }
    return makeContextDefaultFields(map);
  }
  
  public JDrawer
  makeContextDefaultFields
  (
    TreeMap<String, String> values
  )
  {
    Component comps[] = UIFactory.createTitledPanels();
    JPanel tpanel = (JPanel) comps[0];
    JPanel vpanel = (JPanel) comps[1];
    
    TreeMap<String, JTextField> fields = new TreeMap<String, JTextField>();
    
    boolean first = true;
    for (String key : values.keySet()) {
      if (!first)
        UIFactory.addVerticalSpacer(tpanel, vpanel, 6);
      else
        first = false;
      JTextField field = UIFactory.createTitledEditableTextField
        (tpanel, key + ":", sTSize, vpanel, values.get(key), sVSize - 15, 
         "The default value for the string replacement in the context.");
      fields.put(key, field);
    }
    
    pContextDefaultFields.put(pCurrentContext, fields);
    
    JDrawer drawer = 
      new JDrawer("Default Context Values: " + pCurrentContext, (Box) comps[2], true);
    return drawer;  
  }

  
  public JTextField 
  makeStringDefaultField
  (
    String replace,
    String value  
  )
  {
    return UIFactory.createTitledEditableTextField
      (pTpanel, replace + ":", sTSize, pVpanel, value, sVSize, 
      "The default value for a string replacement.");  
  }

  private class
  ValidInputPass
    extends BaseTool.ToolPhase
  {
    @Override
    public String collectInput()
      throws PipelineException
    {
      if (pPrimary == null)
        throw new PipelineException
         ("This tool requires at least one node to be selected.");
      
      pNodesInTemplate = new TreeSet<String>(pSelected.keySet());
      pNodesInTemplate.remove(pPrimary);
     
      pAuthor = getAuthor();
      pView = getView();
      
      return ": Validating input";
    }
    
    @Override
    public NextPhase execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      
      NodeMod templateMod = mclient.getWorkingVersion(pAuthor, pView, pPrimary);
      FileSeq pSeq = templateMod.getPrimarySequence();
      if (!pSeq.isSingle())
        throw new PipelineException("The template node must be a single file");
      String suffix = pSeq.getFilePattern().getSuffix();
      if (suffix == null || !suffix.equals("glue"))
        throw new PipelineException("The template file sequence must have the (glue) suffix");
      
      if (pNodesInTemplate.isEmpty()) {
        for (String source : templateMod.getSourceNames()) {
          TreeMap<String, BaseAnnotation> annots = getTaskAnnotation(source, mclient);
          if (annots.isEmpty())
            throw new PipelineException
              ("There were no nodes selected as being in the template and the nodes " +
               "attached to the template definition node do not contain task annotations.  " +
               "Cowardly refusing to make a Template Glue file that will not build anything.");
          break;
        }
      }
      
      pOldSettings = null;
      
      Path p = getWorkingNodeFilePath(pPrimary, pSeq);
      pTemplateFile = p.toFile();
      if (pTemplateFile.exists()) {
        try {
          pOldSettings = 
            (TemplateGlueInformation) GlueDecoderImpl.decodeFile(aTemplateGlueInfo, pTemplateFile);
        }
        catch (GlueException ex) {
          String error = Exceptions.getFullMessage
            ("Error reading the glue file from disk.  The template tool will continue as if it didn't exist.", ex);
          LogMgr.getInstance().log(Kind.Glu, Level.Warning, error);
        }
      }
      return NextPhase.Continue;
    }
  }

  private class
  InputTemplateSettings
    extends BaseTool.ToolPhase
  {
    /**
     * @throws PipelineException  
     */
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      pStringReplaceFields = new ArrayList<JTextField>();
      pStringDefaultFields = new TreeMap<String, JTextField>();
      pContextNameFields = new ArrayList<JTextField>();
      pContextValueFields = new MappedArrayList<String, JTextField>();
      pContextDefaultFields = new MappedArrayList<String, TreeMap<String,JTextField>>();
      pFrameRangeFields = new ArrayList<JTextField>();
      pFrameRangeStartFields = new TreeMap<String, JIntegerField>();
      pFrameRangeEndFields = new TreeMap<String, JIntegerField>();
      pFrameRangeByFields = new TreeMap<String, JIntegerField>();
      
      
      pVbox = new Box(BoxLayout.Y_AXIS);
      {
        prepStringReplaceDialog();
        pDialog.setVisible(true);
        
        if (!pDialog.wasConfirmed())
          return null;
      }
      
      pStringReplacements = new TreeSet<String>();
      for (JTextField field : pStringReplaceFields) {
        String value = field.getText();
        if (value != null && !value.equals(""))
          pStringReplacements.add(value);
      }
      
      if (!pStringReplacements.isEmpty() ) {
        prepStringDefaultDialog();
        pDialog.setVisible(true);
        
        if (!pDialog.wasConfirmed())
          return null;
      }
      
      pStringDefaults = new TreeMap<String, String>();
      for (String replace : pStringDefaultFields.keySet()) {
        String value = pStringDefaultFields.get(replace).getText();
        pStringDefaults.put(replace, value);
      }
      
      prepFrameRangeDialog();
      pDialog.setVisible(true);
      
      if (!pDialog.wasConfirmed())
        return null;
      
      pFrameRanges = new TreeSet<String>();
      for (JTextField field : pFrameRangeFields) {
        String value = field.getText();
        if (value != null && !value.equals(""))
          pFrameRanges.add(value);
      }
      
      if (!pFrameRanges.isEmpty() ) {
        prepFrameRangeDefaultDialog();
        pDialog.setVisible(true);

        if (!pDialog.wasConfirmed())
          return null;
      }
      
      pFrameRangeDefaults = new TreeMap<String, FrameRange>();
      for (String name : pFrameRanges) {
        Integer start = pFrameRangeStartFields.get(name).getValue();
        Integer end = pFrameRangeEndFields.get(name).getValue();
        Integer by = pFrameRangeByFields.get(name).getValue();
        if (start == null || end == null || by == null)
          continue;
        FrameRange range = new FrameRange(start, end, by);
        pFrameRangeDefaults.put(name, range);
      }
      
      prepContextNameDialog();
      pDialog.setVisible(true);
      if (!pDialog.wasConfirmed())
        return null;
      
      pContextNames = new TreeSet<String>();
      for (JTextField field : pContextNameFields) {
        String value = field.getText();
        if (value != null && !value.equals(""))
          pContextNames.add(value);
      }
      
      if (!pContextNames.isEmpty()) {
        pContextValues = new MappedSet<String, String>();
        for (String context : pContextNames) {
          pCurrentContext = context;
          prepContextValuesDialog();
          pDialog.setVisible(true);
          if (!pDialog.wasConfirmed())
            return null;
          for (JTextField field : pContextValueFields.get(pCurrentContext)) {
            String value = field.getText();
            if (value != null && !value.equals(""))
              pContextValues.put(context, value);
          }
        }
        pContextDefaults = new MappedArrayList<String, TreeMap<String,String>>();
        for (String context : pContextNames) {
          TreeSet<String> cValues = pContextValues.get(context);
          if (cValues != null && !cValues.isEmpty()) {
            pCurrentContext = context;
            prepContextDefaultsDialog();
            pDialog.setVisible(true);
            if (!pDialog.wasConfirmed())
              return null;
            for (TreeMap<String, JTextField> fields : pContextDefaultFields.get(context)) {
              TreeMap<String, String> values = new TreeMap<String, String>();
              for (String key : fields.keySet()) {
                String value = fields.get(key).getText();
                if (value == null)
                  value = "";
                values.put(key, value);
              }
              pContextDefaults.put(context, values);
            }
          }
        }
      }
      
      return ": Writing Template";
    }
    
    @Override
    public NextPhase execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      TemplateGlueInformation info = new TemplateGlueInformation
        ("Template", "Built with the TemplateGlueTool");
      
      info.setNodesInTemplate(pNodesInTemplate);
      info.setReplacements(pStringReplacements);
      info.setReplacementDefaults(pStringDefaults);
      info.setContexts(pContextValues);
      info.setContextDefaults(pContextDefaults);
      info.setFrameRanges(pFrameRanges);
      info.setFrameRangeDefaults(pFrameRangeDefaults);
      
      pTemplateFile.delete();
      try {
        GlueEncoderImpl.encodeFile(aTemplateGlueInfo, info, pTemplateFile);
      }
      catch (GlueException ex) 
      {
        String error = Exceptions.getFullMessage("Error writing Glue Template File.", ex);
        throw new PipelineException(error);
      }
      
      return NextPhase.Finish;
    }

    /**
     * @param vbox
     */
    private void 
    prepStringReplaceDialog()
    {
      JScrollPane scroll;
      {
        Component comps[] = UIFactory.createTitledPanels();
        pTpanel = (JPanel) comps[0];
        pVpanel = (JPanel) comps[1];
        pBody = (Box) comps[2];
    
        pVbox.add(pBody);
      }

      String instructions = 
        "Enter the list of strings that are going to be replaced in the Template.  " +
        "Use the add button to add additional fields to the dialog.  Fields can be left empty" +
        "if too many are added; empty fields will have their values ignored.";
      UIFactory.createTitledTextArea(pTpanel, "Instructions", sTSize, pVpanel, instructions, sVSize, 5, true);
      UIFactory.addVerticalSpacer(pTpanel, pVpanel, 12);
      
      pPhase = TemplatePhase.StringReplace;
      if (pOldSettings != null && !pOldSettings.getReplacements().isEmpty()) {
        for (String value : pOldSettings.getReplacements()) {
          JTextField field = makeStringReplacementField(value);
          pStringReplaceFields.add(field);
          UIFactory.addVerticalSpacer(pTpanel, pVpanel, 6);
        }
      }

      JTextField field = makeStringReplacementField("");
      pStringReplaceFields.add(field);
    
      pVbox.add(UIFactory.createFiller(sTSize +sVSize ));
    
      {
        scroll = new JScrollPane(pVbox);
    
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    
        scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
    
        Dimension size = new Dimension(sTSize + sVSize + 35, 350);
        scroll.setMinimumSize(size);
      }
    
      pDialog = new JTemplateDialog("Set Replacements", scroll);
      pDialog.setMinimumSize(new Dimension(pDialog.getSize().width, 400));
    }

    /**
     * @param vbox
     */
    private void 
    prepStringDefaultDialog()
    {
      pVbox.removeAll();
      {
        Component comps[] = UIFactory.createTitledPanels();
        pTpanel = (JPanel) comps[0];
        pVpanel = (JPanel) comps[1];
        pBody = (Box) comps[2];

        pVbox.add(pBody);
      }
      
      String instructions = 
        "Enter the default values for each string replacement in the template.  " +
        "If you do not wish to have a default value for a particular replacement, simply " +
        "leave the field blank.";
      UIFactory.createTitledTextArea(pTpanel, "Instructions", sTSize, pVpanel, instructions, sVSize, 5, true);
      UIFactory.addVerticalSpacer(pTpanel, pVpanel, 12);
      
      pPhase = TemplatePhase.StringDefaults;
      
      TreeMap<String, String> oldValues = new TreeMap<String, String>();
      if (pOldSettings != null)
        oldValues = pOldSettings.getReplacementDefaults();
      for (String replace : pStringReplacements) {
        JTextField field = makeStringDefaultField(replace, oldValues.get(replace));
        pStringDefaultFields.put(replace, field);
        UIFactory.addVerticalSpacer(pTpanel, pVpanel, 6);
      }
      pVbox.add(UIFactory.createFiller(sTSize +sVSize + 35));
      pDialog.setTitle("Set Replacement Defaults");
      pDialog.pack();
    }
    
    private void 
    prepFrameRangeDialog()
    {
      pVbox.removeAll();
      {
        Component comps[] = UIFactory.createTitledPanels();
        pTpanel = (JPanel) comps[0];
        pVpanel = (JPanel) comps[1];
        pBody = (Box) comps[2];
    
        pVbox.add(pBody);
      }
      
      String instructions = 
        "Enter the names of all the frame ranges that are going to be used in the template.  " +
        "A frame range defines a start and end frame for a node or for the Action Parameters " +
        "of a node.  After the frame ranges are defined, you will be prompted for default " +
        "values  The add button can be used if additional fields are needed.";
      UIFactory.createTitledTextArea(pTpanel, "Instructions", sTSize, pVpanel, instructions, sVSize, 5, true);
      UIFactory.addVerticalSpacer(pTpanel, pVpanel, 12);
      
      pPhase = TemplatePhase.FrameRanges;
      if (pOldSettings != null && !pOldSettings.getFrameRanges().isEmpty() ) {
        for (String frameRange : pOldSettings.getFrameRanges()) {
          JTextField field = makeFrameRangeField(frameRange);
          pFrameRangeFields.add(field);
          UIFactory.addVerticalSpacer(pTpanel, pVpanel, 6);
        }
      }
      JTextField field = makeFrameRangeField("");
      pFrameRangeFields.add(field);
      
      pVbox.add(UIFactory.createFiller(sTSize +sVSize + 35));
      pDialog.setTitle("Set Frame Ranges");
      pDialog.pack();

    }
    
    private void 
    prepFrameRangeDefaultDialog()
    {
      pVbox.removeAll();
      {
        Component comps[] = UIFactory.createTitledPanels();
        pTpanel = (JPanel) comps[0];
        pVpanel = (JPanel) comps[1];
        pBody = (Box) comps[2];
    
        pVbox.add(pBody);
      }
      
      String instructions = 
        "Enter the default values for each frame range in the template.  " +
        "If you do not wish to have a default value for a particular frame range, simply " +
        "leave the fields blank.";
      UIFactory.createTitledTextArea(pTpanel, "Instructions", sTSize, pVpanel, instructions, sVSize, 5, true);
      UIFactory.addVerticalSpacer(pTpanel, pVpanel, 12);
      
      pPhase = TemplatePhase.FrameRangeDefaults;
      
      for (String frameRange : pFrameRanges) {
        FrameRange defaults = null;
        if (pOldSettings != null)
          defaults = pOldSettings.getFrameRangeDefaults().get(frameRange);
        makeFrameRangeDefaultField(frameRange, defaults);
        UIFactory.addVerticalSpacer(pTpanel, pVpanel, 6);
      }
      pVbox.add(UIFactory.createFiller(sTSize +sVSize + 35));
      pDialog.setTitle("Set Frame Range Defaults");
      pDialog.pack();
    }

    private void 
    prepContextNameDialog()
    {
      pVbox.removeAll();
      {
        Component comps[] = UIFactory.createTitledPanels();
        pTpanel = (JPanel) comps[0];
        pVpanel = (JPanel) comps[1];
        pBody = (Box) comps[2];
    
        pVbox.add(pBody);
      }
      
      String instructions = 
        "Enter the names of all the contexts that are going to be used in the template.  " +
        "A context is a scope inside which an additional set of string replacements will " +
        "occur.  After the contexts are defined, you will be prompted for which patterns will " +
        "be replaced in each context.  The add button can be used if additional fields " +
        "are needed.";
      UIFactory.createTitledTextArea(pTpanel, "Instructions", sTSize, pVpanel, instructions, sVSize, 5, true);
      UIFactory.addVerticalSpacer(pTpanel, pVpanel, 12);
      
      pPhase = TemplatePhase.ContextNames;
      if (pOldSettings != null && !pOldSettings.getContexts().keySet().isEmpty() ) {
        for (String context : pOldSettings.getContexts().keySet()) {
          JTextField field = makeContextNameField(context);
          pContextNameFields.add(field);
          UIFactory.addVerticalSpacer(pTpanel, pVpanel, 6);
        }
      }
      JTextField field = makeContextNameField("");
      pContextNameFields.add(field);
      
      pVbox.add(UIFactory.createFiller(sTSize +sVSize + 35));
      pDialog.setTitle("Set Context Names");
      pDialog.pack();

    }
    
    private void 
    prepContextValuesDialog()
    {
      pVbox.removeAll();
      {
        Component comps[] = UIFactory.createTitledPanels();
        pTpanel = (JPanel) comps[0];
        pVpanel = (JPanel) comps[1];
        pBody = (Box) comps[2];
    
        pVbox.add(pBody);
      }
      
      String instructions = 
        "Enter the list of strings that are going to be replaced in the " +
        "(" + pCurrentContext + ") context.  Use the add button to add additional fields to " +
        "the dialog.";
      UIFactory.createTitledTextArea(pTpanel, "Instructions", sTSize, pVpanel, instructions, sVSize, 5, true);
      UIFactory.addVerticalSpacer(pTpanel, pVpanel, 12);
      
      pPhase = TemplatePhase.ContextValues;
      
      if (pOldSettings != null && pOldSettings.getContexts().get(pCurrentContext) != null) {
        for (String value : pOldSettings.getContexts().get(pCurrentContext)) {
          JTextField field = makeContextValueField(value);
          pContextValueFields.put(pCurrentContext, field);
        }
      }
      
      JTextField field = makeContextValueField("");
      pContextValueFields.put(pCurrentContext, field);
      pDialog.setTitle("Set Context (" + pCurrentContext + ") Values");
      pDialog.pack();
      
      pVbox.add(UIFactory.createFiller(sTSize +sVSize + 35));
    }

    private void 
    prepContextDefaultsDialog()
    {
      pVbox.removeAll();
      pPhase = TemplatePhase.ContextDefaults;
      
      Box hBox = new Box(BoxLayout.X_AXIS);
      pVbox2 = new Box(BoxLayout.Y_AXIS);
      
      hBox.add(pVbox2);
      
      {
        Component comps[] = UIFactory.createTitledPanels();
        pTpanel = (JPanel) comps[0];
        pVpanel = (JPanel) comps[1];
        pBody = (Box) comps[2];
    
        pVbox2.add(pBody);
      }
      
      String instructions = 
        "Enter the default replacement values for the string replacements in the " +
        "(" + pCurrentContext + ") context. ";
      UIFactory.createTitledTextArea(pTpanel, "Instructions", sTSize, pVpanel, instructions, sVSize, 5, true);
      
      ArrayList<TreeMap<String, String>> defaults = new ArrayList<TreeMap<String,String>>();
      if (pOldSettings != null)
        defaults = pOldSettings.getContextDefaults().get(pCurrentContext);
      if (defaults != null && !defaults.isEmpty()) {
        for (TreeMap<String, String> values : defaults) {
          JDrawer draw = makeContextDefaultFields(values);
          pVbox2.add(draw);
        }
      }
      JDrawer draw = makeContextDefaultFields(pContextValues.get(pCurrentContext));
      pVbox2.add(draw);
      
      
      pVbox.add(hBox);
      pVbox.add(UIFactory.createFiller(sTSize +sVSize + 35));
      pDialog.setTitle("Set Context (" + pCurrentContext + ") Defaults");
      pDialog.pack();
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L   C L A S S E S                                                       */
  /*----------------------------------------------------------------------------------------*/

  private class
  JTemplateDialog
    extends JToolDialog
  {
    private 
    JTemplateDialog
    (
      String title,
      JComponent body  
    )
    {
      super(title, body, "Confirm", null, getButtons() );
    }
    
    /*--------------------------------------------------------------------------------------*/
    /*   L I S T E N E R S                                                                  */
    /*--------------------------------------------------------------------------------------*/

    /*-- ACTION LISTENER METHODS -----------------------------------------------------------*/

    /** 
     * Invoked when an action occurs. 
     */ 
    @Override
    public void 
    actionPerformed
    (
     ActionEvent e
    ) 
    {
      String cmd = e.getActionCommand();
      if(cmd.equals("add")) 
        doAdd();
      else 
        super.actionPerformed(e);
    }
    private static final long serialVersionUID = -7200677273422567909L;
  }
  
  private enum
  TemplatePhase
  {
    StringReplace, 
    StringDefaults,
    FrameRanges,
    FrameRangeDefaults,
    ContextNames, 
    ContextValues, 
    ContextDefaults 
  }

    
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -711763397951071286L;

  public static final String aTemplateGlueInfo = "TemplateGlueInfo";
  private static final int sVSize = 300;
  private static final int sTSize = 150;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private TemplatePhase pPhase;
  
  private TreeSet<String> pNodesInTemplate;
  private String pAuthor;
  private String pView;
  
  private File pTemplateFile;
  
  private TemplateGlueInformation pOldSettings;
  
  private JTemplateDialog pDialog;
  private Box pBody;
  private Box pVbox;
  private Box pVbox2;

  private JPanel pTpanel;
  private JPanel pVpanel;
  
  private String pCurrentContext;
  
  private ArrayList<JTextField> pStringReplaceFields;
  private TreeMap<String, JTextField> pStringDefaultFields;
  private ArrayList<JTextField> pContextNameFields;
  private MappedArrayList<String, JTextField> pContextValueFields;
  private MappedArrayList<String, TreeMap<String, JTextField>> pContextDefaultFields;
  private ArrayList<JTextField> pFrameRangeFields;
  private TreeMap<String, JIntegerField> pFrameRangeStartFields;
  private TreeMap<String, JIntegerField> pFrameRangeEndFields;
  private TreeMap<String, JIntegerField> pFrameRangeByFields;
  
  private TreeSet<String> pStringReplacements;
  private TreeMap<String, String> pStringDefaults;
  private TreeSet<String> pContextNames;
  private MappedSet<String, String> pContextValues;
  private MappedArrayList<String, TreeMap<String, String>> pContextDefaults;
  private TreeSet<String> pFrameRanges;
  private TreeMap<String, FrameRange> pFrameRangeDefaults;
  
}
