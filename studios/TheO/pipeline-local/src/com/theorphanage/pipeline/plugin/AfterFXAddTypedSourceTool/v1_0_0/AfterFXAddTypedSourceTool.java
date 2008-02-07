package com.theorphanage.pipeline.plugin.AfterFXAddTypedSourceTool.v1_0_0;

import java.awt.Component;
import java.awt.Dimension;
import java.io.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

public 
class AfterFXAddTypedSourceTool 
  extends BaseTool 
{
  public 
  AfterFXAddTypedSourceTool()
  {
    super("AfterFXAddTypedSource", new VersionID("1.0.0"), "TheO",
          "Added one or more sources to an AfterFX scene, making them either a Roto or a Plate.");
    
    underDevelopment();
    removeSupport(OsType.Unix);
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Create and show graphical user interface components to collect information from the 
   * user to use as input in the next phase of execution for the tool. <P> 
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException 
   *   If unable to validate the given user input.
   */  
  public synchronized String
  collectPhaseInput() 
    throws PipelineException 
  {
    if(pSelected.size() < 2 || pPrimary == null)
      throw new PipelineException
        ("At least two nodes must be selected for this tool to work, including a primary node.");
    
    pStatus = pSelected.get(pPrimary);
    FileSeq seq = pStatus.getDetails().getWorkingVersion().getPrimarySequence();
    String suffix = seq.getFilePattern().getSuffix();
    if (suffix == null || !suffix.equals("aep"))
      throw new PipelineException("Please select an AfterFX composite node for this script to run on.");
    
    pNodeID = pStatus.getNodeID();
    
    if(!pNodeID.getAuthor().equals(PackageInfo.sUser)) 
      throw new PipelineException
        ("In order for the " + getName() + " tool to have the required permissions to " +
         "modify the target Maya scene file you must be the owner of this file!");
    
    TreeSet<String> sources = new TreeSet<String>(pSelected.keySet());
    sources.remove(pPrimary);
    
    ArrayList<String> choices = new ArrayList<String>();
    choices.add(aPlate);
    choices.add(aRoto);
    
    pSourceTypeFields = new TreeMap<String, JCollectionField>();
    
    /* create the UI components */ 
    JScrollPane scroll = null;
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);
      {
        Box hbox = new Box(BoxLayout.X_AXIS);
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
        
        pLaunchField = 
          UIFactory.createTitledBooleanField
            (tpanel, "Launch AfterFX?", sTSize, 
             vpanel, sVSize, "Do you want AfterFX to stay open after it adds the sources.");
        
        hbox.add(comps[2]);
        vbox.add(hbox);
        
      }
      
      vbox.add(Box.createVerticalStrut(3));

      for(String sname : sources) {
        Box hbox = new Box(BoxLayout.X_AXIS);
        {
          Component comps[] = UIFactory.createTitledPanels();
          JPanel tpanel = (JPanel) comps[0];
          JPanel vpanel = (JPanel) comps[1];
          
          UIFactory.createTitledTextField
            (tpanel, "Source Node:", sTSize, vpanel,
             sname, sVSize, 
             "The image sequence to be added as a source.");
          
          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
          
          JCollectionField field =
            UIFactory.createTitledCollectionField
            (tpanel, "Source Type:", sTSize, vpanel,
             choices, sVSize, 
             "The Type of source that this sequence represents.");
          
          pSourceTypeFields.put(sname, field);
          
          hbox.add(comps[2]);
        }
        
        JDrawer drawer = new JDrawer("Source Node: " + sname, hbox, true);
        vbox.add(drawer);
      }

      vbox.add(UIFactory.createFiller(sTSize + sVSize));

      {
        scroll = new JScrollPane(vbox);

        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        Dimension size = new Dimension(sTSize + sVSize, 500);
        scroll.setMinimumSize(size);
      }
    }

    JToolDialog diag = new JToolDialog("AfterFX Add Sources Tool", scroll, "Confirm");

    diag.setVisible(true);
    if(diag.wasConfirmed()) 
      return ": Adding Sources to Comp...";
    
    return null;
  }
  
  /**
   * Perform one phase in the execution of the tool. <P> 
   *    
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @return 
   *   Whether to continue and collect user input for the next phase of the tool.
   * 
   * @throws PipelineException 
   *   If unable to successfully execute this phase of the tool.
   */ 
  public synchronized boolean
  executePhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
     NodeMod targetMod = mclient.getWorkingVersion(pNodeID);
     
     if (targetMod.isActionEnabled())
       throw new PipelineException("Cannot run this tool on a node whose action is enabled.");
     
     for (String sname : targetMod.getSourceNames()) {
       if (pSourceTypeFields.containsKey(sname))
         pSourceTypeFields.remove(sname);
     }
     
     File script = null; 
     
     Path targetPath = getWorkingNodeFilePath(pNodeID, targetMod.getPrimarySequence());
     
     TreeMap<String, String> env =
       mclient.getToolsetEnvironment(getAuthor(), getView(), targetMod.getToolset(),
           PackageInfo.sOsType);
     
     try {
       script = File.createTempFile(getName() + ".", ".jsx", 
                                       PackageInfo.sTempPath.toFile());
       FileCleaner.add(script);
       
       FileWriter out = new FileWriter(script);
       out.write("app.beginSuppressDialogs();\n" +
                 "app.exitAfterLaunchAndEval = false;" +
                 "var f = new File(\"" + escPath(targetPath.toOsString()) + "\");\n" +
                 "app.open(f);\n");
       
       String workingStart = PackageInfo.sWorkPath.toOsString().replaceAll("\\\\", "/");
       String currentWorking = env.get("WORKING").replaceAll("\\\\", "/");
       
       out.write("var workingStart = \"" + workingStart + "/\";\n" +
           "var currentWorking = \"" + currentWorking + "/\";\n" +
           "var proj = app.project;\n" +
           "var list = proj.items;\n" +
           "var regExp = new RegExp(workingStart, \"g\");\n" + 
           "for (j=1; j <= list.length; j++)\n" + 
           "{\n" + 
           "  var item = list[j];\n" +
           "  if (item instanceof FootageItem)\n" + 
           "  {\n" + 
           "     var file = item.file;\n" + 
           "     if (regExp.test(file))\n" + 
           "     {\n" + 
           "       var fileName = file.fullName;\n" + 
           "       var endName = fileName.replace(workingStart, \"\");\n" + 
           "       var split = endName.split(\"/\");\n" + 
           "       var newEnd = \"\";\n" + 
           "       for (i=2; i < split.length; i++)\n" + 
           "       {\n" + 
           "         newEnd += split[i];\n" + 
           "         if (i != split.length -1)\n" + 
           "           newEnd += \"/\";\n" + 
           "       }\n" + 
           "       var newFileName = currentWorking + newEnd;\n" + 
           "       var newFile = new File(newFileName);\n" + 
           "       item.replaceWithSequence(newFile, false);\n" + 
           "    }\n" + 
           "  }\n" + 
           "}");
                 
        out.write("var root = app.project.rootFolder;\r\n" + 
                  "\r\n" + 
                  "var sourceFolder;\r\n" + 
                  "for (i = 1; i <= root.numItems ; i++) {\r\n" + 
                  "  if (root.item(i).name == \"Source\")\r\n" + 
                  "    sourceFolder = root.item(i);\r\n" + 
                  "}\r\n" + 
                  "\r\n" + 
                  "var platesFolder;\r\n" + 
                  "var rotoFolder;\r\n" + 
                  "for (i = 1; i <= sourceFolder.numItems ; i++) {\r\n" + 
                  "  if (sourceFolder.item(i).name == \"Roto\")\r\n" + 
                  "    rotoFolder = sourceFolder.item(i);\r\n" + 
                  " else if (sourceFolder.item(i).name == \"Plates\")\r\n" + 
                  "    platesFolder = sourceFolder.item(i);\r\n" + 
                  "}\r\n");
        
        for (String sname : pSourceTypeFields.keySet()) {
          String type = pSourceTypeFields.get(sname).getSelected();
          
          NodeID snodeID = new NodeID(pNodeID, sname);
          Path sourcePath = getWorkingNodeFilePath(snodeID , mclient.getWorkingVersion(snodeID).getPrimarySequence());
          
          if (type.equals(aRoto)) {
            out.write("{\n" +
                      "                var rotoFile = new File(\"" + escPath(sourcePath.toOsString()) + "\");\r\n" + 
                      "                var rotoImportOptions = new ImportOptions(rotoFile);\r\n" + 
                      "                rotoImportOptions.importAs = ImportAsType.FOOTAGE\r\n" + 
                      "                rotoImportOptions.sequence = true;\r\n" + 
                      "                var rotoFootageItem = app.project.importFile(rotoImportOptions);\r\n" + 
                      "                rotoFootageItem.parentFolder = rotoFolder;\r\n" + 
                      "}\n");
          }
          else {
            out.write("{\n" +
                      "                var plateFile = new File(\"" + escPath(sourcePath.toOsString()) + "\");\r\n" + 
                      "                var plateImportOptions = new ImportOptions(plateFile);\r\n" + 
                      "                plateImportOptions.importAs = ImportAsType.FOOTAGE\r\n" + 
                      "                plateImportOptions.sequence = true;\r\n" + 
                      "                var plateFootageItem = app.project.importFile(plateImportOptions);\r\n" + 
                      "                plateFootageItem.parentFolder = plateFolder;\r\n" +
                      "}\n");
          }
        }
        
        out.write("app.project.save(f);\n");
        if (!pLaunchField.getValue())
          out.write("app.quit());\n");
        else
          out.write("app.endSuppressDialogs();\n");
              
        out.close();
     }
     catch(IOException ex) {
       throw new PipelineException
         ("Unable to write the temporary JSX script (" + script + ") for the " + 
          getName() + " tool!"); 
     }
     
     BaseAction action = targetMod.getAction();
     for (String sname : pSourceTypeFields.keySet()) {
       mclient.link(getAuthor(), getView(), pPrimary, sname, LinkPolicy.Association);
       if (action != null && action.getName().equals("AfterFXTemplate")) {
         action.initSourceParams(sname);
         action.setSourceParamValue(sname, aSourceType, pSourceTypeFields.get(sname).getSelected());
       }
     }
     if (action != null) {
       targetMod.setAction(action);
       mclient.modifyProperties(getAuthor(), getView(), targetMod);
     }

     {
       String program = "AfterFX.exe";

       ArrayList<String> args = new ArrayList<String>();
       args.add("-m");
       args.add("-r");
       args.add(script.getAbsolutePath());

       Path wpath = new Path(PackageInfo.sProdPath, pNodeID.getWorkingParent());


       SubProcessLight proc =
         new SubProcessLight(getName() + "Tool-FixAfterFX", program, args, env, wpath.toFile());
       
       if (pLaunchField.getValue())
         proc.start();
       else {
         try {
           proc.start();
           proc.join();
           if(!proc.wasSuccessful()) {
             for (String sname : pSourceTypeFields.keySet()) 
               mclient.unlink(getAuthor(), getView(), pPrimary, sname);
             throw new PipelineException
               ("Did not correctly add the sources due to a AfterFX error.!\n\n" +
                proc.getStdOut() + "\n\n" + 
                proc.getStdErr());
           }
         }
         catch(Exception ex) {
           throw new PipelineException(ex);
         }
       }
     }

     return false;
  }
  
  /**
   * Get the abstract working area file system path to the given node file.
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @param file
   *   The name of the file relative to the directory containing the node.
   */
  private Path
  getWorkingNodeFilePath
  (
   NodeID nodeID,
   FileSeq file
  ) 
  {
    return new Path(PackageInfo.sProdPath, nodeID.getWorkingParent() + "/" + file.getPath(0).toString());     
  }
  
  /**
   * Escape any backslashes in the given file system path string.
   */ 
  private final String 
  escPath
  (
   String str
  ) 
  {
    return str.replaceAll("\\\\", "\\\\\\\\");
  }

    /*----------------------------------------------------------------------------------------*/
    /*   S T A T I C   I N T E R N A L S                                                      */
    /*----------------------------------------------------------------------------------------*/
    
    public static final String aSourceType = "SourceType";
    public static final String aPlate = "Plate";
    public static final String aRoto = "Roto";

    private static final int sTSize = 150;
    private static final int sVSize = 300;
    
    private static final long serialVersionUID = 7048361022904956306L;

    
    /*----------------------------------------------------------------------------------------*/
    /*   I N T E R N A L S                                                                    */
    /*----------------------------------------------------------------------------------------*/
    
    private NodeStatus pStatus;
    private NodeID pNodeID;
    private TreeMap<String, JCollectionField> pSourceTypeFields;
    private JBooleanField pLaunchField;
}
