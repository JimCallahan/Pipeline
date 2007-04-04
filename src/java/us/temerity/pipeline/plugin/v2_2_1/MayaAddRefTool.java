package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

import java.util.*;
import java.io.*;
import java.awt.*;

import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   A D D   R E F   T O O L                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Adds references to a Maya scene and makes the correct links in Pipeline.<P> 
 * 
 * If the target Maya scene uses a MayaBuild Action, this tool will also add set the 
 * appropriate action parameters to that the MayaBuild Action will correctly rebuild the 
 * target scene with the new references if it is enabled.  <P> 
 * 
 */
public 
class MayaAddRefTool
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public MayaAddRefTool() 
  {
    super("MayaAddRef", new VersionID("2.2.1"), "Temerity",
          "Adds references to a Maya scene and makes the correct links in Pipeline.");

    pNameSpaceFields = new TreeMap<String, JTextField>();
    pSourcePaths = new TreeMap<String, Path>();

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
  public String 
  collectPhaseInput() 
    throws PipelineException
  {
    /* validate target node */ 
    { 
      if(pPrimary == null)
	throw new PipelineException
          ("The " + getName() + " tool requires a target node!"); 

      NodeStatus status = pSelected.get(pPrimary);
      NodeID nodeID = status.getNodeID();

      if(!nodeID.getAuthor().equals(PackageInfo.sUser)) 
        throw new PipelineException
          ("In order for the " + getName() + " tool to have the required permissions to " +
           "modify the target Maya scene file you must be the owner of this file!");
      pView = nodeID.getView();
      
      {
        NodeMod mod = status.getDetails().getWorkingVersion();
        FileSeq fseq = mod.getPrimarySequence();
        String suffix = fseq.getFilePattern().getSuffix();
        if(!fseq.isSingle() || (suffix == null)
           || (!suffix.equals("ma") && !suffix.equals("mb")))
          throw new PipelineException
            ("The target node (" + pPrimary + ") must be a Maya scene!");
        
        pTargetPath = new Path(PackageInfo.sProdPath, 
                               nodeID.getWorkingParent() + "/" + fseq.getFile(0));
      }
    }

    /* validate source nodes */ 
    { 
      int size = pSelected.keySet().size();
      if(size < 2)
	throw new PipelineException
          ("The " + getName() + "tool requires at least one source node is selected in " + 
           "addition to the target node!"); 

      NodeStatus status = pSelected.get(pPrimary);
      Set<String> snames = status.getSourceNames();
      for(String sname : pSelected.keySet()) {
	if(!sname.equals(pPrimary)) {
          if(snames.contains(sname))
            throw new PipelineException
              ("The selected source node (" + sname + ") is already linked to the target " + 
               "node (" + pPrimary + ")!");
          
          NodeMod sourceMod = pSelected.get(sname).getDetails().getWorkingVersion();
          FileSeq fseq = sourceMod.getPrimarySequence();
          String suffix = fseq.getFilePattern().getSuffix();
          if(!fseq.isSingle() || 
             (suffix == null) || !(suffix.equals("ma") || suffix.equals("mb")))
            throw new PipelineException
              ("The source node (" + sname + ") must be a Maya scene!");
          
          Path path = new Path(sname);
          Path sourcePath = new Path(path.getParent() + "/" + fseq.getPath(0));
          pSourcePaths.put(sname, sourcePath);
        }
      }
    }

    /* create the UI components */ 
    JScrollPane scroll = null;
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      for(String sname : pSourcePaths.keySet()) {
        Box hbox = new Box(BoxLayout.X_AXIS);
        {
          Component comps[] = UIFactory.createTitledPanels();
          JPanel tpanel = (JPanel) comps[0];
          JPanel vpanel = (JPanel) comps[1];
          
          String mname = pSourcePaths.get(sname).getName();

          UIFactory.createTitledTextField
            (tpanel, "Maya Scene:", sTSize, vpanel,
             mname, sVSize, 
             "The Maya scene file to be added as a reference.");
          
          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
          
          JTextField field =
            UIFactory.createTitledEditableTextField
            (tpanel, "Name Space:", sTSize, vpanel,
             mname.substring(0, mname.length() - 3), sVSize, 
             "The Maya name space for the referenced scene.");
          
          pNameSpaceFields.put(sname, field);
          
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

    JToolDialog diag = new JToolDialog("Maya Reference Tool", scroll, "Confirm");

    diag.setVisible(true);
    if(diag.wasConfirmed()) 
      return ": Adding References";

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
   *   If unable to sucessfully execute this phase of the tool.
   */ 
  public boolean 
  executePhase
  (
   MasterMgrClient mclient, 
   QueueMgrClient qclient
  )
    throws PipelineException
  {
    NodeID targetID   = new NodeID(PackageInfo.sUser, pView, pPrimary);
    NodeMod targetMod = pSelected.get(pPrimary).getDetails().getWorkingVersion();

    /* create a temporary MEL script to add the references */ 
    Path script = null; 
    try {
      File file = File.createTempFile(getName() + ".", ".mel", 
                                      PackageInfo.sTempPath.toFile());
      FileCleaner.add(file);
      script = new Path(file);

      FileWriter out = new FileWriter(file);
      for(String sname : pSourcePaths.keySet()) {
        Path path = pSourcePaths.get(sname);

        String ns = pNameSpaceFields.get(sname).getText();
        if((ns == null) || (ns.length() == 0))
          throw new PipelineException
            ("No Name Space was specified for source node (" +  sname + ")!");

        out.write
          ("// MODEL: " + sname + "\n" +
           "print (\"Adding Reference: " + path + "\");\n" +
           "file -reference -namespace \"" + ns + "\" \"$WORKING" + path + "\";\n\n");
      }

      out.write
        ("// SAVE\n" + 
         "file -save;\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write the temporary MEL script (" + script + ") for the " + 
         getName() + " tool!"); 
    }
    
    /* add the references to the Maya scene */ 
    try {
      String program = "maya";
      if(PackageInfo.sOsType == OsType.Windows) 
        program = "mayabatch.exe";

      ArrayList<String> args = new ArrayList<String>();
      args.add("-batch");
      args.add("-script");
      args.add(script.toString());
      args.add("-file");
      args.add(pTargetPath.toString());

      Path wpath = new Path(PackageInfo.sProdPath, targetID.getWorkingParent());

      TreeMap<String, String> env =
        mclient.getToolsetEnvironment(PackageInfo.sUser, pView, targetMod.getToolset(),
                                      PackageInfo.sOsType);
      
      Map<String,String> nenv = MayaActionUtils.getMiCustomShaderEnv(targetID, env); 

      SubProcessLight proc =
        new SubProcessLight(getName() + "Tool-FixMaya", program, args, nenv, wpath.toFile());
      try {
        proc.start();
        proc.join();
        if(!proc.wasSuccessful()) {
          throw new PipelineException
            ("Did not correctly add the reference due to a Maya error.!\n\n" +
             proc.getStdOut() + "\n\n" + 
             proc.getStdErr());
        }
      }
      catch(InterruptedException ex) {
        throw new PipelineException(ex);
      }
    }
    catch(Exception ex) {
      throw new PipelineException(ex);
    }

    /* link the source nodes to the target node */ 
    for(String sname : pSourcePaths.keySet()) 
      mclient.link(PackageInfo.sUser, pView, pPrimary, sname, 
                   LinkPolicy.Reference, LinkRelationship.All, null);

    /* modify the target's MayaBuild action parameters */ 
    {
      BaseAction action = targetMod.getAction();
      if((action != null) && action.getName().equals("MayaBuild")) {
        for(String sname : pSourcePaths.keySet()) {
          String ns = pNameSpaceFields.get(sname).getText();

          action.initSourceParams(sname);
          action.setSourceParamValue(sname, "BuildType", "Reference"); 
          action.setSourceParamValue(sname, "NameSpace", true);
          action.setSourceParamValue(sname, "PrefixName", ns); 
        }

        targetMod.setAction(action);
        mclient.modifyProperties(PackageInfo.sUser, pView, targetMod);
      }
    }

    /* hide the added nodes */ 
    pRoots.removeAll(pSourcePaths.keySet());

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5407518201267754382L;

  private static final int sTSize = 150;
  private static final int sVSize = 300;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current working area view name.
   */ 
  private String  pView;

  /**
   * The abstract path to the target Maya scene.
   */ 
  private Path  pTargetPath; 

  /**
   * The abstract paths to the source Maya scene indexed by source node name.
   */ 
  private TreeMap<String,Path>  pSourcePaths;

  /**
   * The name space text fields indexed by source node name.
   */ 
  private TreeMap<String,JTextField>  pNameSpaceFields;

}
