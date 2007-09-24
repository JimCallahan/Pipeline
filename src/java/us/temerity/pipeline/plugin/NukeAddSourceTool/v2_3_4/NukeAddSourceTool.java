package us.temerity.pipeline.plugin.NukeAddSourceTool.v2_3_4;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.ui.*;

import java.util.*;
import java.io.*;
import java.awt.*;

import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   A D D   S O U R C E   T O O L                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Adds source sequences to a Nuke script, and makes the correct links in Pipeline.<P> 
 * 
 */
public 
class NukeAddSourceTool
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * 
   */

  public NukeAddSourceTool() 
  {
    super("NukeAddSource", new VersionID("2.3.4"), "Temerity",
          "Adds source sequences to a Nuke script and makes the correct links in Pipeline.");

    underDevelopment();

    pSourceSeqs = new MappedLinkedList<String, FileSeq>();

    pNodeNameFields = new TreeMap<String, JTextField>();
    pPremultFields = new TreeMap<FileSeq, JBooleanField>();

    /* TODO:  we're testing for hard-coded suffixes in order to determine
     * which sources to consider image sequences.  This is brittle.  
     * At the very least, the list of accepted image formats should be moved
     * somewhere higher up in the package, and be made configurable or something.
     */
    pImageFormats = new ArrayList<String>(); 
    pImageFormats.add("bmp");
    pImageFormats.add("iff");
    pImageFormats.add("gif");
    pImageFormats.add("hdr");
    pImageFormats.add("jpg");
    pImageFormats.add("jpeg");
    pImageFormats.add("map");
    pImageFormats.add("png");
    pImageFormats.add("ppm");
    pImageFormats.add("psd");
    pImageFormats.add("rgb");
    pImageFormats.add("rgba");
    pImageFormats.add("sgi");
    pImageFormats.add("bw");
    pImageFormats.add("tga");
    pImageFormats.add("tif");
    pImageFormats.add("tiff");

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
          ("The " + getName() + " tool requires a target Nuke script node!"); 

      NodeStatus status = pSelected.get(pPrimary);
      NodeID nodeID = status.getNodeID();

      if(!nodeID.getAuthor().equals(PackageInfo.sUser)) 
        throw new PipelineException
          ("In order for the " + getName() + " tool to have the required permissions to " +
           "modify the target Nuke script file you must be the owner of this file!");
      pView = nodeID.getView();
      
      {
        NodeMod mod = status.getDetails().getWorkingVersion();
        FileSeq fseq = mod.getPrimarySequence();
        String suffix = fseq.getFilePattern().getSuffix();
        if(!fseq.isSingle() || (suffix == null)
           || (!suffix.equals("nk") && !suffix.equals("nuke")))
          throw new PipelineException
            ("The target node (" + pPrimary + ") must be a Nuke script!");
        
        pNukeScriptPath = new Path(PackageInfo.sProdPath, 
                               nodeID.getWorkingParent() + "/" + fseq.getFile(0));
      }
    }

    /* validate source nodes */ 
    { 
      int size = pSelected.keySet().size();
      if(size < 2)
	throw new PipelineException
          ("The " + getName() + "tool requires at least one source sequence node selected, " +
           "in addition to the target Nuke script node!"); 

      NodeStatus status = pSelected.get(pPrimary);
      Set<String> snames = status.getSourceNames();
      for(String sname : pSelected.keySet()) {
	if(!sname.equals(pPrimary)) {
          if(snames.contains(sname))
            throw new PipelineException
              ("The selected source node (" + sname + ") is already linked to the target " + 
               "node (" + pPrimary + ")!");
          
          NodeMod sourceMod = pSelected.get(sname).getDetails().getWorkingVersion();
          for (FileSeq fseq : sourceMod.getSequences()) {
            String suffix = fseq.getFilePattern().getSuffix();
            if((suffix == null) || !pImageFormats.contains(suffix))
              throw new PipelineException
              ("The source node (" + sname + ") must contain an image sequence!");

            pSourceSeqs.put(sname, fseq);
          }
        }
      }
    }

    /* create the UI components */ 
    JScrollPane scroll = null;
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);

      for(String sname : pSourceSeqs.keySet()) {

	  Box hbox = new Box(BoxLayout.X_AXIS);
	  {
	    Component comps[] = UIFactory.createTitledPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];

	    boolean first = true;
	    for(FileSeq fseq : pSourceSeqs.get(sname)) {
	      FilePattern fpat = fseq.getFilePattern();

	      if (!first)
		UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
	      first = false;

	      UIFactory.createTitledTextField
	      (tpanel, "Sequence:", sTSize, vpanel,
		sname + "/" + NukeActionUtils.toNukeFilePattern(fpat), sVSize, 
	      "The image sequence to be added.");

	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	      JTextField field =
		UIFactory.createTitledEditableTextField
		(tpanel, "Nuke Node Name:", sTSize, vpanel,
		  fpat.getPrefix(), sVSize, 
		"The name of the Nuke read node to be created.");
	      
	      pNodeNameFields.put(sname, field);

	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
		  
	      JBooleanField bfield = 
		UIFactory.createTitledBooleanField
		(tpanel, "Premultiplied:", sTSize-7, 
		  vpanel, sVSize, 
		"Whether or not this sequence consists of premultiplied images.");
	      bfield.setValue(true);

	      pPremultFields.put(fseq, bfield);

	      hbox.add(comps[2]);
	    }
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

    JToolDialog diag = new JToolDialog("Nuke Add Source Tool", scroll, "Confirm");

    diag.setVisible(true);
    diag.pack();
    if(diag.wasConfirmed()) 
      return ": Adding Sources";

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
    NodeMod targetMod = pSelected.get(pPrimary).getDetails().getWorkingVersion();

    /* create a temporary TCL script to add the references */ 
    Path tclScript = null; 
    try {
      File file = File.createTempFile(getName() + ".", ".tcl", 
                                      PackageInfo.sTempPath.toFile());
      //FileCleaner.add(file);
      tclScript = new Path(file);

      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
      
      /* open the target script */
      out.write("script_open "+pNukeScriptPath+"\n");

      /* create the Read nodes */
      for(String sname : pSourceSeqs.keySet()) { 
	for(FileSeq fseq : pSourceSeqs.get(sname)) {
	  JBooleanField field = pPremultFields.get(fseq);
	  Boolean value = field == null ? true : field.getValue();
	  writeSourceToScript(out, fseq, sname, value);
	}
      }

      /* save the script */
      out.write("script_save "+pNukeScriptPath+"\n");
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write the temporary TCL script (" + tclScript + ") for the " + 
         getName() + " tool!"); 
    }
    
    /* Create a temporary Python script to run Nuke piping the script to STDIN 
     * NOTE:  we're passing a "-d :0" argument to Nuke, which defines the X Display.
     * This shouldn't be necessary in Terminal Mode, but for some reason, Nuke spits
     * out a 'can't open DISPLAY ""' error if we don't.
     * 
     * TODO:  turns out that the Nuke "save_script" function is trying to open the
     * X display, even in terminal mode.  This causes this action to barf on machines
     * with no X servers (um, like anything in a renderfarm).  This is a known bug, 
     * fixed in versions higher than 4.7v3.
     */ 
    File pyScript = null;
    TreeMap<String, String> env =
	mclient.getToolsetEnvironment(PackageInfo.sUser, pView, targetMod.getToolset(),
	  			      PackageInfo.sOsType);
    try {
      pyScript = File.createTempFile(getName() + ".", ".py", 
					  PackageInfo.sTempPath.toFile());
      //FileCleaner.add(pyscript);
      FileWriter out = new FileWriter(pyScript); 

      String nukeApp = NukeActionUtils.getNukeProgram(env); 
      out.write
      ("import subprocess\n" + 
	"tclScript = open('" + tclScript + "', 'r')\n" + 
	"p = subprocess.Popen(['" + nukeApp + "', '-t', '-d :0'], stdin=tclScript)\n" + 
      	"p.communicate()\n");
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
      ("Unable to write temporary Python script file for the " + 
        getName() + " tool!\n" +
	ex.getMessage());
    }

    ArrayList<String> args = new ArrayList<String>();
    String python = PythonActionUtils.getPythonProgram(env);
    args.add(pyScript.getPath());

    SubProcessLight proc =
      new SubProcessLight(getName() + "NukeAddSourceTool-AddSources", python, args, 
			  env, PackageInfo.sTempPath.toFile());
    try {
      proc.start();
      proc.join();
      if(!proc.wasSuccessful()) {
	throw new PipelineException
	("Failed to add source sequences to Nuke script!\n\n" +
	  proc.getStdErr());
      }
    }
    catch(InterruptedException ex) {
      throw new PipelineException(ex);
    }

    /* link the source nodes to the target node */ 
    for(String sname : pSourceSeqs.keySet()) 
      mclient.link(PackageInfo.sUser, pView, pPrimary, sname, 
                   LinkPolicy.Dependency, LinkRelationship.All, null);

    /* TODO:  if the script has a NukeBuildAction, set the source parameters
     * for the sequences we just added.  (layer, pass, order. blend, alpha)
     * Hmmm...  maybe only a subset of those make sense.  Just alpha?
    {
      BaseAction action = targetMod.getAction();
      if((action != null) && action.getName().equals("MayaBuild")) {
        for(String sname : pSourceSeqs.keySet()) {
          String ns = pNodeNameFields.get(sname).getText();

          action.initSourceParams(sname);
          action.setSourceParamValue(sname, "BuildType", "Reference"); 
          action.setSourceParamValue(sname, "NameSpace", true);
          action.setSourceParamValue(sname, "PrefixName", ns); 
        }

        targetMod.setAction(action);
        mclient.modifyProperties(PackageInfo.sUser, pView, targetMod);
      }
    }*/

    /* hide the added nodes */ 
    pRoots.removeAll(pSourceSeqs.keySet());

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /** writeSourceToScript - spit out the code to read a source sequence into Nuke
   */
  private void
  writeSourceToScript
  (
    PrintWriter out,
    FileSeq fSeq,
    String sourceName,
    Boolean premult
  )
  throws PipelineException
  {
    //String alphaMode = (String) getSourceParamValue(sourceName, aAlphaMode);

    //int order = (Integer) getSourceParamValue(sourceName, aOrder);
    //double offset = orderStart.get(order);

    FilePattern fpat = fSeq.getFilePattern();
    FrameRange range = fSeq.getFrameRange();      
    Path readPath = new Path("WORKING" + (new Path(sourceName)).getParent() + "/" + 
      NukeActionUtils.toNukeFilePattern(fpat).toString());
    
    String cmd = "Read -New file " + readPath.toOsString();
    if (range != null)
      cmd += " first " + range.getStart() +
      	     " last " + range.getEnd();
    cmd += "\n";
    out.write(cmd);

    out.write("Reformat -New \n");

    //if (offset != 0)
    //  out.write("TimeOffset -New time_offset "+offset+"\n");

    if (premult)
      out.write("Unpremult -New \n");

    out.write("Grade -New \n");

    if (premult)
      out.write("Premult -New \n");
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7264503705741397535L;

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
   * The abstract path to the target Nuke script.
   */ 
  private Path  pNukeScriptPath; 

  /**
   * The source sequences indexed by source node name.
   */ 
  private MappedLinkedList<String,FileSeq>  pSourceSeqs;

  /**
   * The name space text fields indexed by source node name.
   */ 
  private TreeMap<String,JTextField>  pNodeNameFields;
  
  /**
   * The premult fields indexed by sequence.
   */ 
  private TreeMap<FileSeq,JBooleanField>  pPremultFields;
  
  /**
   * The filename suffixes recognized as images.
   */ 
  private ArrayList<String> pImageFormats;
  

}
