// $Id: GenUserPrefsApp.java,v 1.12 2004/08/30 06:52:37 jim Exp $

import java.io.*; 
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   G E N   U S E R   P R E F S   A P P                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates the <CODE>UserPrefs.java</CODE> and <CODE>JUserPrefsDialog.java</CODE> source
 * files.
 */ 
public 
class GenUserPrefsApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GenUserPrefsApp() 
  {
    pPrefs = new TreeMap<String,BasePref[]>();

    {
      BasePref prefs[] = {
	new BoundedDoublePref
	("the horizontal distance between nodes", 
	 "NodeSpaceX", "Horizontal Space:", 2.5, 4.5, 2.75),

	new BoundedDoublePref
	("the vertical distance between nodes", 
	 "NodeSpaceY", "Vertical Space:", 1.5, 3.0, 2.0),

	new BoundedDoublePref
	("the vertical offset distance for nodes with an odd depth level",
	 "NodeOffset", "Vertical Offset:", 0.0, 1.0, 0.45),
	
	new BasePref(),

	new BooleanPref
	("whether to draw graphics representating disabled actions",
	 "DrawDisabledAction", "Draw Disabled Action:", true), 

	new BoundedDoublePref
	("the size of disabled action graphics", 
	 "DisabledActionSize", "Disabled Action Size:", 0.05, 0.2, 0.15)
      };

      pPrefs.put("Panel|Node Viewer|Node|Appearance", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("update connected node details panels",
	 "NodeDetails", "Details:"),	

	new BasePref(),

	new HotKeyPref
	("make the current primary selection the only root node", 
	 "NodeMakeRoot", "Make Root:"),

	new HotKeyPref
	("add the current primary selection to the set of root nodes",
	 "NodeAddRoot", "Add Root:"),

	new HotKeyPref
	("replace the root node of the current primary selection with the primary selection",
	 "NodeReplaceRoot", "Replace Root:"),

	new HotKeyPref
	("remove the root node of the current primary selection from the set of roots nodes",
	 "NodeRemoveRoot", "Remove Root:"),

	new HotKeyPref
	("remove all of the roots nodes",
	 "NodeRemoveAllRoots", "Remove All Roots:"), 

	new BasePref(),
	
	new HotKeyPref
	("edit primary file sequences of the current primary selection",
	 "NodeEdit", "Edit:"), 

	new BasePref(),
	
	new HotKeyPref
	("link the secondary selected nodes to the current primary selection",
	 "NodeLink", "Link:"), 

	new HotKeyPref
	("unlink the secondary selected nodes from the current primary selection",
	 "NodeUnlink", "Unlink:"), 

	new BasePref(),
	
	new HotKeyPref
	("add a secondary file sequence to the current primary selection",
	 "NodeAddSecondary", "Add Secondary:"), 

	new BasePref(),

	new HotKeyPref
	("submit jobs to the queue for the current primary selection",
	 "NodeQueueJobs", "Queue Jobs:"), 

	new HotKeyPref
	("kill all jobs associated with the selected nodes",
	 "NodeKillJobs", "Kill Jobs:"), 

	new HotKeyPref
	("pause all jobs associated with the selected nodes",
	 "NodePauseJobs", "Pause Jobs:"), 

	new HotKeyPref
	("resume execution of all jobs associated with the selected nodes",
	 "NodeResumeJobs", "Resume Jobs:"), 

	new BasePref(),
	
	new HotKeyPref
	("check-in the current primary selection",
	 "NodeCheckIn", "Check-In:"), 

	new HotKeyPref
	("check-out the current primary selection",
	 "NodeCheckOut", "Check-Out:"), 

	new BasePref(),

	new HotKeyPref
	("register a new node which is a clone of the current primary selection",
	 "NodeClone", "Clone:"), 

	new HotKeyPref
	("release the current primary selection",
	 "NodeRelease", "Release:"),

	new HotKeyPref
	("remove all the primary/secondary files associated with the selected nodes",
	 "NodeRemoveFiles", "Remove Files:"), 

	new BasePref(),

	new HotKeyPref
	("rename the current primary selection",
	 "NodeRename", "Rename:"), 

	new HotKeyPref
	("renumber the current primary selection",
	 "NodeRenumber", "Renumber:"), 
      };

      pPrefs.put("Panel|Node Viewer|Node|Hot Keys", prefs);
    }

    {
      LinkedList<String> colors = new LinkedList<String>();
      colors.add("DarkGrey");
      colors.add("LightGrey");
      colors.add("White");
      colors.add("Yellow");

      BasePref prefs[] = {
	new ChoicePref
	("the name of the simple color texture to use for link lines", 
	 "LinkColorName", "Line Color:", colors, "LightGrey"),

	new BooleanPref
	("whether to anti-alias link lines", 
	 "LinkAntiAlias", "Antialiased:", true), 
	
	new BoundedDoublePref
	("the thickness of link lines", 
	 "LinkThickness", "Line Thickness:", 0.25, 3.0, 1.0),

	new BasePref(),

	new BoundedDoublePref
	("the distance between node and the start/end of link", 
	 "LinkGap", "Node/Link Gap:", 0.0, 0.2, 0.05),

	new BoundedDoublePref
	("the position of the vertical crossbar as a percentage of the horizontal space", 
	 "LinkVerticalCrossbar", "Vertical Crossbar:", 0.35, 0.55, 0.45), 

	new BasePref(),
	
	new BooleanPref
	("whether to draw arrow heads showing the direction of the link",
	 "DrawArrowHeads", "Draw Arrowheads:", true), 
	
	new BoundedDoublePref
	("the length of the link arrow head", 
	 "ArrowHeadLength", "Arrowhead Length:", 0.08, 0.4, 0.2),
	
	new BoundedDoublePref
	("the width of the link arrow head", 
	 "ArrowHeadWidth", "Arrowhead Width:", 0.04, 0.2, 0.08),

	new BasePref(),
	
	new BooleanPref
	("whether to draw graphics representating LinkRelationship",
	 "DrawLinkRelationship", "Draw Link Relationship:", true), 
	 
	new BooleanPref
	("whether to draw graphics representating LinkPolicy",
	 "DrawLinkPolicy", "Draw Link Policy:", true), 

	new BoundedDoublePref
	("the size of LinkPolicy graphics", 
	 "LinkPolicySize", "Link Policy Size:", 0.05, 0.2, 0.15)
      };

      pPrefs.put("Panel|Node Viewer|Links|Appearance", prefs);
    }


    {
      BasePref prefs[] = {
	new HotKeyPref
	("edit the properties of the selected link", 
	 "LinkEdit", "Edit Link:"),	

	new HotKeyPref
	("remove the selected link", 
	 "LinkUnlink", "UnLink:"),	
      };

      pPrefs.put("Panel|Node Viewer|Links|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("update the status of all nodes", 
	 "UpdateNodes", "Update Nodes:", 
	 false, false, false, 32), /* Space */ 
	
	new HotKeyPref
	("register a new node",
	 "RegisterNewNode", "Register New Node:"),	

	new BasePref(),

	new HotKeyPref
	("move the camera so that it is centered on current mouse position",
	 "CameraCenter", "Center:", 
	 false, false, false, 67),  /* C */ 
	 
	
	new HotKeyPref
	("move the camera to frame the bounds of the currently selected nodes",
	 "CameraFrameSelection", "Frame Selection:",
	 false, false, false, 70),  /* F */ 
	
	new HotKeyPref
	("move the camera to frame all active nodes",
	 "CameraFrameAll", "Frame All:", 
	 false, false, false, 71),  /* G */ 
	
	new BasePref(),

	new HotKeyPref
	("automatically expand the first occurance of a node",
	 "AutomaticExpandNodes", "Automatic Expand:", 
	 false, false, false, 65),  /* A */

	new HotKeyPref
	("expand all nodes",
	 "ExpandAllNodes", "Expand All:", 
	 false, false, false, 69),  /* E */

	new HotKeyPref
	("collapse all nodes",
	 "CollapseAllNodes", "Collapse All:", 
	 false, false, false, 67),  /* C */

	new BasePref(),

	new HotKeyPref
	("show/hide nodes downstream of the focus node",
	 "ShowHideDownstreamNodes", "Show/Hide Downstream:", 
	 false, false, false, 68),  /* D */
      };

      pPrefs.put("Panel|Node Viewer|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("apply the changes to the working version", 
	 "NodeDetailsApplyChanges", "Apply Changes:",
	 false, false, false, 155)   /* Insert */ 
      };

      pPrefs.put("Panel|Node Details|Hot Keys", prefs);
    }

    
    {
      BasePref prefs[] = {
	new HotKeyPref
	("update the status of all jobs and job groups.", 
	 "JobBrowserUpdate", "Update Jobs:",
	 false, false, false, 32)  /* Space */ 
      };

      pPrefs.put("Panel|Job Browser|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new BoundedDoublePref
	("the width of a job", 
	 "JobSizeX", "Width:", 1.0, 4.0, 2.0),

	new BoundedDoublePref
	("the height of a job", 
	 "JobSizeY", "Height:", 0.5, 2.0, 1.0),

	new BoundedDoublePref
	("the distance between jobs",
	 "JobSpace", "Job Space:", 0.0, 0.3, 0.1), 
	
	new BasePref(),

	new BoundedDoublePref
	("the distance between jobs",
	 "JobGroupSpace", "Group Space:", 0.25, 1.0, 0.5), 
	
	new BasePref(),

	new BooleanPref
	("whether to anti-alias lines", 
	 "JobViewerLineAntiAlias", "Antialiased Lines:", true), 
	
	new BoundedDoublePref
	("the thickness of lines", 
	 "JobViewerLineThickness", "Line Thickness:", 0.25, 2.0, 1.0)
      };

      pPrefs.put("Panel|Job Viewer|Job|Appearance", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("update connected job details panels",
	 "JobDetails", "Details:"),
      
	new BasePref(),
	
	new HotKeyPref
	("kill all selected jobs", 
	 "JobKillJobs", "Kill Jobs:"), 

	new HotKeyPref
	("pause all selected jobs",
	 "JobPauseJobs", "Pause Jobs:"), 

	new HotKeyPref
	("resume execution of all selected jobs",
	 "JobResumeJobs", "Resume Jobs:")
      };      

      pPrefs.put("Panel|Job Viewer|Job|Hot Keys", prefs);
    }

    {
      BasePref prefs[] = {
	new HotKeyPref
	("update the status of all jobs", 
	 "JobViewerUpdate", "Update Jobs:", 
	 false, false, false, 32), /* Space */ 
	
	new BasePref(),

	new HotKeyPref
	("move the camera so that it is centered on current mouse position",
	 "JobViewerCameraCenter", "Center:", 
	 false, false, false, 67),  /* C */ 
	 
	
	new HotKeyPref
	("move the camera to frame the bounds of the currently selected jobs",
	 "JobViewerCameraFrameSelection", "Frame Selection:",
	 false, false, false, 70),  /* F */ 
	
	new HotKeyPref
	("move the camera to frame all active jobs",
	 "JobViewerCameraFrameAll", "Frame All:", 
	 false, false, false, 71),  /* G */ 
	
	new BasePref(),

	new HotKeyPref
	("automatically expand the first occurance of a job",
	 "JobViewerAutomaticExpandJobs", "Automatic Expand:", 
	 false, false, false, 65),  /* A */

	new HotKeyPref
	("expand all jobs",
	 "JobViewerExpandAllJobs", "Expand All:", 
	 false, false, false, 69),  /* E */

	new HotKeyPref
	("collapse all jobs",
	 "JobViewerCollapseAllJobs", "Collapse All:", 
	 false, false, false, 67),  /* C */
      };

      pPrefs.put("Panel|Job Viewer|Hot Keys", prefs);
    }
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The top-level function.
   */ 
  public static void 
  main
  (
   String[] args 
  )
  {
    GenUserPrefsApp app = new GenUserPrefsApp();
    app.generateUserPrefsClass(new File("ui/UserPrefs.java"));
    app.generateJUserPrefsDialogClass(new File("ui/JUserPrefsDialog.java"));
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generates the <CODE>UserPrefs.java</CODE> source file.
   */ 
  public void 
  generateUserPrefsClass
  (
   File file
  ) 
  {
    StringBuffer buf = new StringBuffer();
    
    buf.append
      ("// $Id: GenUserPrefsApp.java,v 1.12 2004/08/30 06:52:37 jim Exp $\n" +
       "\n" + 
       "package us.temerity.pipeline.ui;\n" + 
       "\n" + 
       "import us.temerity.pipeline.*;\n" + 
       "import us.temerity.pipeline.core.*;\n" + 
       "import us.temerity.pipeline.glue.*;\n" + 
       "\n" + 
       "import java.io.*;\n" + 
       "\n" + 
       genHeader("USER PREFS") + 
       "/**\n" + 
       " * The user preferences for various aspects of the user interface.\n" + 
       " */\n" +
       "public\n" + 
       "class UserPrefs\n" + 
       "  implements Glueable\n" + 
       "{\n" + 
       genMinorHeader("CONSTRUCTOR") +
       "\n" + 
       "  /**\n" +
       "   * Construct the sole instance.\n" + 
       "   */\n" + 
       "  public\n" + 
       "  UserPrefs()\n" + 
       "  {\n" + 
       "    reset();\n" + 
       "  }\n" + 
       "\n" +
       "\n" + 
       "\n" + 
       genMinorHeader("ACCESS") +
       "\n" + 
       "  /**\n" + 
       "   * Get the UserPrefs instance.\n" + 
       "   */\n" + 
       "   public static UserPrefs\n" + 
       "   getInstance()\n" + 
       "   {\n" + 
       "     return sUserPrefs;\n" + 
       "   }\n" +
       "\n" +
       "\n");

    /* generate accessors */ 
    for(String group : pPrefs.keySet()) {
      BasePref prefs[] = pPrefs.get(group);
      
      {
	String gtitle = group.replace("|", " - ");
	buf.append("  /*-- " + gtitle + " ");
	int wk;
	for(wk=0; wk<(84 - gtitle.length()); wk++) 
	  buf.append("-");
	buf.append("*/\n\n");
      }

      int wk;
      for(wk=0; wk<prefs.length; wk++) 
	prefs[wk].genAccessors(buf, 1);

      buf.append("\n");
    }
       
    /* generate reset method */ 
    {
      buf.append
	(genMinorHeader("RESET") +
	 "\n" +
	 "  public void\n" + 
	 "  reset()\n" + 
	 "  {\n");
      
      for(String group : pPrefs.keySet()) {
	BasePref prefs[] = pPrefs.get(group);
	
	String gtitle = group.replace("|", " - ");
	buf.append("    /* " + gtitle + " */\n" + 
		   "    {\n");
	int wk;
	for(wk=0; wk<prefs.length; wk++) 
	  prefs[wk].genReset(buf, 3);

	buf.append("    }\n\n");
      }
      
      buf.append
	("  }\n" + 
	 "\n" + 
	 "\n" + 
	 "\n");
    }

    /* generate I/O methods */ 
    {
      buf.append
	(genMinorHeader("I/O") +
	 "\n" + 
	 "  /**\n" + 
	 "   * Save the preferences to disk.\n" + 
	 "   */\n" + 
	 "  public static void\n" + 
	 "  save()\n" + 
	 "    throws GlueException, GlueLockException\n" + 
	 "  {\n" + 
	 "    File file = new File(PackageInfo.sHomeDir,\n" + 
	 "		           PackageInfo.sUser + \"/.pipeline/preferences\");\n" + 
	 "    LockedGlueFile.save(file, \"UserPreferences\", sUserPrefs);\n" + 
	 "  }\n" + 
	 "\n" + 
	 "  /**\n" + 
	 "   * Load the preferences from disk.\n" + 
	 "   */\n" + 
	 "  public static void\n" + 
	 "  load()\n" + 
	 "    throws GlueException, GlueLockException\n" + 
	 "  {\n" + 
	 "    File file = new File(PackageInfo.sHomeDir,\n" + 
	 "		           PackageInfo.sUser + \"/.pipeline/preferences\");\n" + 
	 "    sUserPrefs = (UserPrefs) LockedGlueFile.load(file);\n" + 
	 "  }\n" + 
	 "\n" + 
	 "\n" + 
	 "\n");
    }

    /* generate glueable methods */ 
    {
      buf.append
	(genMinorHeader("GLUEABLE") +
	 "\n" + 
	 "  public void\n" + 
	 "  toGlue\n" + 
	 "  (\n" + 
	 "    GlueEncoder encoder\n" +    
	 "  )\n" + 
	 "    throws GlueException\n" + 
	 "  {\n");

      for(String group : pPrefs.keySet()) {
	BasePref prefs[] = pPrefs.get(group);
	
	String gtitle = group.replace("|", " - ");
	buf.append("    /* " + gtitle + " */\n" + 
		   "    {\n");
	int wk;
	for(wk=0; wk<prefs.length; wk++) 
	  prefs[wk].genToGlue(buf, 3);

	buf.append("    }\n\n");	
      }

      buf.append
	("  }\n" + 
	 "\n" + 
	 "  public void\n" + 
	 "  fromGlue\n" + 
	 "  (\n" + 
	 "    GlueDecoder decoder\n" +    
	 "  )\n" + 
	 "    throws GlueException\n" + 
	 "  {\n");
	 
      for(String group : pPrefs.keySet()) {
	BasePref prefs[] = pPrefs.get(group);
	
	String gtitle = group.replace("|", " - ");
	buf.append("    /* " + gtitle + " */\n" + 
		   "    {\n");
	int wk;
	for(wk=0; wk<prefs.length; wk++) 
	  prefs[wk].genFromGlue(buf, 3);

	buf.append("    }\n\n");	
      }

      buf.append
	("  }\n" + 
	 "\n" + 
	 "\n" + 
	 "\n");
    }

    /* static internals */ 
    buf.append
      (genMinorHeader("STATIC INTERNALS") +
       "\n" + 
       "  /**\n" + 
       "   * The sole instance of this class.\n" + 
       "   */\n" + 
       "  private static UserPrefs sUserPrefs = new UserPrefs();\n" + 
       "\n" + 
       "\n" + 
       "\n");
    
    /* internals */ 
    {
      buf.append
	(genMinorHeader("INTERNALS") +
	 "\n");

      for(String group : pPrefs.keySet()) {
	BasePref prefs[] = pPrefs.get(group);
	
	{
	  String gtitle = group.replace("|", " - ");
	  buf.append("  /*-- " + gtitle + " ");
	  int wk;
	  for(wk=0; wk<(84 - gtitle.length()); wk++) 
	    buf.append("-");
	  buf.append("*/\n\n");
	}

	int wk;
	for(wk=0; wk<prefs.length; wk++) {
	  prefs[wk].genDeclare(buf, 1);
	  buf.append("\n");
	}
	
	buf.append("\n");
      }
    }

    buf.append("}\n");


    /* write the file */ 
    try {
      FileWriter out = new FileWriter(file);
      out.write(buf.toString());
      out.close();
    }
    catch(IOException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Generates the <CODE>UserPrefs.java</CODE> source file.
   */ 
  public void 
  generateJUserPrefsDialogClass
  (
   File file
  ) 
  {
    StringBuffer buf = new StringBuffer();
    
    buf.append
      ("// $Id: GenUserPrefsApp.java,v 1.12 2004/08/30 06:52:37 jim Exp $\n" +
       "\n" + 
       "package us.temerity.pipeline.ui;\n" + 
       "\n" + 
       "import us.temerity.pipeline.*;\n" + 
       "\n" + 
       "import java.awt.*;\n" + 
       "import java.awt.event.*;\n" + 
       "import java.util.*;\n" + 
       "import java.io.*;\n" + 
       "import javax.swing.*;\n" + 
       "import javax.swing.tree.*;\n" + 
       "import javax.swing.event.*;\n" + 
       "\n" + 
       genHeader("USER PREFS DIALOG") + 
       "\n" +
       "/**\n" +
       " * The user preferences dialog.\n" + 
       " */\n" + 
       "public\n" + 
       "class JUserPrefsDialog\n" + 
       "  extends JBaseUserPrefsDialog\n" + 
       "{\n" + 
       genMinorHeader("CONSTRUCTOR") +
       "\n" + 
       "  /**\n" + 
       "   * Construct a new user preferences dialog.\n" + 
       "   */ \n" + 
       "  public \n" + 
       "  JUserPrefsDialog()\n" + 
       "  {\n" + 
       "    super();\n" + 
       "    initUI();\n" + 
       "  }\n" + 
       "\n" +
       "\n" +
       "  " + genBar(92) + 
       "\n" +
       "  /**\n" + 
       "   * Initialize the common user interface components.\n" + 
       "   */\n" + 
       "  protected void\n" + 
       "  initUI()\n" + 
       "  {\n" + 
       "    super.initUI();\n" + 
       "\n");
    
    for(String group : pPrefs.keySet()) {
      String gtitle = group.replace("|", " - ");

      buf.append
	("    /* " + gtitle + " */\n" + 
	 "    {\n" +
	 "      createTreeNodes(\"" + group + "\");\n" + 
	 "\n" + 
	 "      Component comps[] = createCommonPanels();\n" + 
	 "      JPanel tpanel = (JPanel) comps[0];\n" +
	 "      JPanel vpanel = (JPanel) comps[1];\n" + 
	 "\n");
	 
      BasePref prefs[] = pPrefs.get(group);
      int wk;
      for(wk=0; wk<prefs.length; wk++) 
	prefs[wk].genUI(buf, 3, wk==(prefs.length-1));
      
      buf.append
	("      UIMaster.addVerticalGlue(tpanel, vpanel);\n" + 
	 "\n" + 
	 "      pCardPanel.add(comps[2], \"" + gtitle + "\");\n" + 
	 "    }\n" +
	 "\n");
    }

    buf.append
      ("    {\n" + 
       "      int wk;\n" + 
       "      for(wk=0; wk<pTree.getRowCount(); wk++)\n" + 
       "        pTree.expandRow(wk);\n" + 
       "    }\n" + 
       "  }\n" + 
       "\n" + 
       "\n" +
       "\n" +
       genMinorHeader("ACCESS") +
       "\n" + 
       "  /**\n" + 
       "    * Set the user preferences from the current UI settings and save the prefs.\n" + 
       "    */ \n" + 
       "  public void \n" + 
       "  savePrefs()\n" + 
       "  {\n" + 
       "    UIMaster master = UIMaster.getInstance();\n" +
       "    UserPrefs prefs = UserPrefs.getInstance();\n" + 
       "\n");
    
    for(String group : pPrefs.keySet()) {
      String gtitle = group.replace("|", " - ");

      buf.append
	("    /* " + gtitle + " */\n" + 
	 "    {\n");

      BasePref prefs[] = pPrefs.get(group);
      int wk;
      for(wk=0; wk<prefs.length; wk++) 
	prefs[wk].genSavePrefs(buf, 3);

      buf.append
	("    }\n" + 
	 "\n");
    }

    buf.append
      ("    try {\n" +
       "      UserPrefs.save();\n" + 
       "    }\n" +     
       "    catch(Exception ex) {\n" + 
       "      master.showErrorDialog(ex);\n" + 
       "      return;\n" + 
       "    }\n" +
       "\n" + 
       "    master.updateUserPrefs();\n" + 
       "  }\n" + 
       "\n" + 
       "  /**\n" + 
       "   * Update the current UI settings from the user preferences.\n" + 
       "   */ \n" + 
       "  public void \n" + 
       "  updatePrefs()\n" + 
       "  {\n" + 
       "    UserPrefs prefs = UserPrefs.getInstance();\n" +
       "\n");

    for(String group : pPrefs.keySet()) {
      String gtitle = group.replace("|", " - ");
      
      buf.append
	("    /* " + gtitle + " */\n" + 
	 "    {\n");

      BasePref prefs[] = pPrefs.get(group);
      int wk;
      for(wk=0; wk<prefs.length; wk++) 
	prefs[wk].genUpdatePrefs(buf, 3);

      buf.append
	("    }\n" + 
	 "\n");
    }

    buf.append
      ("  }\n" + 
       "\n" + 
       "\n" +
       "\n" +
       genMinorHeader("STATIC INTERNALS") +
       "\n" +
       "  private static final long serialVersionUID = -7086626876853073990L;\n" + 
       "\n" + 
       "\n" + 
       "\n" + 
       genMinorHeader("INTERNALS") +
       "\n");
    
    for(String group : pPrefs.keySet()) {
      String gtitle = group.replace("|", " - ");
      
      buf.append
	("  /**\n" +
	 "   * " + gtitle + "\n" + 
	 "   */\n");
      
      BasePref prefs[] = pPrefs.get(group);
      int wk;
      for(wk=0; wk<prefs.length; wk++) 
	prefs[wk].genDeclareUI(buf, 1);
      
      buf.append
	("\n");
    }
	 
    buf.append
      ("}\n");

    /* write the file */ 
    try {
      FileWriter out = new FileWriter(file);
      out.write(buf.toString());
      out.close();
    }
    catch(IOException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  private String 
  genHeader
  (
   String title
  ) 
  {
    StringBuffer buf = new StringBuffer();

    buf.append(genBar(94) + 
	       "/*  ");
    
    char cs[] = title.toCharArray();
    int wk;
    for(wk=0; wk<cs.length; wk++) 
      buf.append(" " + cs[wk]);
    for(wk=0; wk<(88 - cs.length*2); wk++) 
      buf.append(" ");
    buf.append("*/\n");

    buf.append(genBar(94));

    return (buf.toString());
  }

  private String 
  genMinorHeader
  (
   String title
  ) 
  {
    StringBuffer buf = new StringBuffer();

    buf.append("  " + genBar(92) + 
	       "  /*  ");
    
    char cs[] = title.toCharArray();
    int wk;
    for(wk=0; wk<cs.length; wk++) 
      buf.append(" " + cs[wk]);
    for(wk=0; wk<(86 - cs.length*2); wk++) 
      buf.append(" ");
    buf.append("*/\n");

    buf.append("  " + genBar(92));

    return (buf.toString());
  }

  private String 
  genBar
  (
   int size
  ) 
  {
    StringBuffer buf = new StringBuffer();

    buf.append("/*");
    int wk;
    for(wk=0; wk<size-4; wk++) 
      buf.append("-");
    buf.append("*/\n");
    
    return (buf.toString());
  }
  
  private String 
  indent
  (
   int level
  ) 
  {
    StringBuffer buf = new StringBuffer();
    int wk;
    for(wk=0; wk<level; wk++) 
      buf.append("  ");
    return buf.toString();
  }




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Base class of all preferences.
   */ 
  private 
  class BasePref
  {
    public 
    BasePref()
    {}

    public void 
    genAccessors
    (
     StringBuffer buf,
     int level
    ) 
    {} 
      
    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {}

    public void 
    genToGlue
    (
     StringBuffer buf,
     int level
    )
    {} 

    public void
    genFromGlue
    (
     StringBuffer buf,
     int level
    )
    {}

    public void 
    genDeclare
    (
     StringBuffer buf,
     int level
    )
    {} 

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      if(!isLast) 
	buf.append(indent(level) + "UIMaster.addVerticalSpacer(tpanel, vpanel, 9);\n\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {} 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {} 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {} 
  }

  /**
   * 
   */ 
  private abstract
  class ValuedPref
    extends BasePref
  {
    public 
    ValuedPref
    (
     String desc, 
     String title, 
     String label, 
     String atype, 
     String gtype
    ) 
    {
      pDesc       = desc;
      pTitle      = title;
      pLabel      = label;
      pAtomicType = atype;
      pGlueType   = gtype;
    }

    public void 
    genAccessors
    (
     StringBuffer buf,
     int level
    ) 
    {
      buf.append
	(indent(level) + "/**\n" +
	 indent(level) + " * Get " + pDesc + ".\n" + 
	 indent(level) + " */\n" +
	 indent(level) + "public " + pAtomicType + "\n" +
	 indent(level) + "get" + pTitle + "()\n" +
	 indent(level) + "{\n" + 
	 indent(level+1) + "return p" + pTitle + ";\n" + 
	 indent(level) + "}\n" + 
	 "\n" +
	 indent(level) + "/**\n" +
	 indent(level) + " * Set " + pDesc + ".\n" + 
	 indent(level) + " */\n" +
	 indent(level) + "public void\n" +
	 indent(level) + "set" + pTitle + "\n" + 
	 indent(level) + "(\n" + 
	 indent(level) + " " + pAtomicType + " v\n" +
	 indent(level) + ")\n" + 
	 indent(level) + "{\n" + 
	 indent(level+1) + "p" + pTitle + " = v;\n" + 
	 indent(level) + "}\n" +
	 "\n" + 
	 "\n");
    }
      
    public void 
    genToGlue
    (
     StringBuffer buf,
     int level
    ) 
    {
      buf.append
	(indent(level) + "encoder.encode(\"" + pTitle + "\", p" + pTitle + ");\n");
    }

    public void
    genFromGlue
    (
     StringBuffer buf,
     int level
    ) 
    {
      buf.append
	(indent(level) + "{\n" + 
	 indent(level+1) + pGlueType + " v = (" + pGlueType + ") " + 
	                   "decoder.decode(\"" + pTitle + "\");\n" + 
	 indent(level+1) + "if(v != null)\n" + 
	 indent(level+2) + "p" + pTitle + " = v;\n" +
	 indent(level) + "}\n");
    }

    public void 
    genDeclare
    (
     StringBuffer buf,
     int level
    ) 
    {
      char cs[] = pDesc.toCharArray();
      cs[0] = Character.toUpperCase(cs[0]);
      String desc = new String(cs);

      buf.append
	(indent(level) + "/**\n" +
	 indent(level) + " * " + desc + ".\n" + 
	 indent(level) + " */\n" +
	 indent(level) + "private " + pAtomicType + "  p" + pTitle + ";\n");	 
    }

    protected String  pDesc; 
    protected String  pTitle;
    protected String  pLabel;
    protected String  pAtomicType;
    protected String  pGlueType;
  }

  /**
   * Boolean preference.
   */ 
  private 
  class BooleanPref
    extends ValuedPref
  {
    public 
    BooleanPref
    (
     String desc, 
     String title, 
     String label, 
     boolean defaultValue
    ) 
    {
      super(desc, title, label, "boolean", "Boolean");
      pDefaultValue = defaultValue;
    }

    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {      
      buf.append
	(indent(level) + "p" + pTitle + " = " + String.valueOf(pDefaultValue) + ";\n");
    }

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + " =\n" + 
	 indent(level+1) + "UIMaster.createTitledBooleanField\n" + 
	 indent(level+2) + "(tpanel, \"" + pLabel + "\", sTSize, vpanel, sVSize);\n" + 
	 "\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIMaster.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "prefs.set" + pTitle + "(p" + pTitle + ".getValue());\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "p" + pTitle + ".setValue(prefs.get" + pTitle + "());\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "private JBooleanField  p" + pTitle + ";\n");
    } 

    protected boolean pDefaultValue;
  }

  /**
   * Unbounded Integer preference.
   */ 
  private 
  class IntegerPref
    extends ValuedPref
  {
    public 
    IntegerPref
    (
     String desc, 
     String title, 
     String label, 
     int defaultValue
    ) 
    {
      super(desc, title, label, "int", "Integer");
      pDefaultValue = defaultValue;
    }

    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {      
      buf.append
	(indent(level) + "p" + pTitle + " = " + String.valueOf(pDefaultValue) + ";\n");
    }

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + " = null;  // Not yet implemented!\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIMaster.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "// " + pTitle + " - Not yet implemented!\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "// " + pTitle + " - Not yet implemented!\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "// " + pTitle + " - Not yet implemented!\n");
    } 

    protected int  pDefaultValue;
  }

  /**
   * Bounded Integer preference.
   */ 
  private 
  class BoundedIntegerPref
    extends IntegerPref
  {
    public 
    BoundedIntegerPref
    (
     String desc, 
     String title, 
     String label, 
     int minValue, 
     int maxValue, 
     int defaultValue
    ) 
    {
      super(desc, title, label, defaultValue);
      pMinValue = minValue;
      pMaxValue = maxValue;
    }
      
    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + " =\n" + 
	 indent(level+1) + "UIMaster.createTitledSlider\n" + 
	 indent(level+2) + "(tpanel, \"" + pLabel + "\", sTSize,\n" + 
	 indent(level+2) + " vpanel, " + pMinValue + ", " + pMaxValue + ", sVSize);\n" + 
	 "\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIMaster.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "prefs.set" + pTitle + "(p" + pTitle + ".getValue());\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "p" + pTitle + ".setValue(prefs.get" + pTitle + "());\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "private JSlider  p" + pTitle + ";\n");
    } 

    protected int  pMinValue;      
    protected int  pMaxValue;      
  }

  /**
   * Unbounded Double preference.
   */ 
  private 
  class DoublePref
    extends ValuedPref
  {
    public 
    DoublePref
    (
     String desc, 
     String title, 
     String label, 
     double defaultValue
    ) 
    {
      super(desc, title, label, "double", "Double");
      pDefaultValue = defaultValue;
    }

    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {      
      buf.append
	(indent(level) + "p" + pTitle + " = " + String.valueOf(pDefaultValue) + ";\n");
    }

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + " = null;  // Not yet implemented!\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIMaster.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "// " + pTitle + " - Not yet implemented!\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "// " + pTitle + " - Not yet implemented!\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "// " + pTitle + " - Not yet implemented!\n");
    } 

    protected double  pDefaultValue;
  }

  /**
   * Bounded Double preference.
   */ 
  private 
  class BoundedDoublePref
    extends DoublePref
  {
    public 
    BoundedDoublePref
    (
     String desc, 
     String title, 
     String label, 
     double minValue, 
     double maxValue, 
     double defaultValue
    ) 
    {
      super(desc, title, label, defaultValue);
      pMinValue = minValue;
      pMaxValue = maxValue;
    }
      
    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + " =\n" + 
	 indent(level+1) + "UIMaster.createTitledSlider\n" + 
	 indent(level+2) + "(tpanel, \"" + pLabel + "\", sTSize,\n" + 
	 indent(level+2) + " vpanel, " + pMinValue + ", " + pMaxValue + ", sVSize);\n" + 
	 "\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIMaster.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "prefs.set" + pTitle + 
		 "(((double) p" + pTitle + ".getValue())/1000.0);\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "p" + pTitle + ".setValue(" + 
		 "(int) (prefs.get" + pTitle + "()*1000.0));\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "private JSlider  p" + pTitle + ";\n");
    } 

    protected double  pMinValue;      
    protected double  pMaxValue;      
  }

  /**
   * String choice preference.
   */ 
  private 
  class ChoicePref
    extends ValuedPref
  {
    public 
    ChoicePref
    (
     String desc, 
     String title, 
     String label, 
     Collection<String> values, 
     String defaultValue
    ) 
    {
      super(desc, title, label, "String", "String");
      pValues       = values;
      pDefaultValue = defaultValue;
    }

    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {      
      buf.append
	(indent(level) + "p" + pTitle + " = \"" + pDefaultValue + "\";\n");
    }

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "{\n" + 
	 indent(level+1) + "ArrayList<String> values = new ArrayList<String>();\n");

      for(String v : pValues) 
	buf.append(indent(level+1) + "values.add(\"" + v + "\");\n");

      buf.append
	("\n" + 
	 indent(level+1) + "p" + pTitle + " =\n" + 
	 indent(level+2) + "UIMaster.createTitledCollectionField\n" + 
	 indent(level+3) + "(tpanel, \"" + pLabel + "\", sTSize,\n" + 
	 indent(level+3) + " vpanel, values, sVSize);\n" + 
	 indent(level) + "}\n" + 
	 "\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIMaster.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "prefs.set" + pTitle + "(p" + pTitle + ".getSelected());\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "p" + pTitle + ".setSelected(prefs.get" + pTitle + "());\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "private JCollectionField  p" + pTitle + ";\n");
    } 

    protected Collection<String>  pValues;
    protected String              pDefaultValue;
  }

  /**
   * HotKey preference.
   */ 
  private 
  class HotKeyPref
    extends ValuedPref
  {
    public 
    HotKeyPref
    (
     String desc, 
     String title, 
     String label
    ) 
    {
      super(desc, title, label, "HotKey", "HotKey");
    }

    public 
    HotKeyPref
    (
     String desc, 
     String title, 
     String label, 
     boolean shiftDown, 
     boolean altDown, 
     boolean ctrlDown, 
     int keyCode
    ) 
    {
      super(desc, title, label, "HotKey", "HotKey");

      pShiftDown = shiftDown;
      pAltDown   = altDown;
      pCtrlDown  = ctrlDown;
      pKeyCode   = keyCode;
    }

    public void 
    genReset
    (
     StringBuffer buf,
     int level
    )
    {      
      buf.append(indent(level) + "p" + pTitle + " = ");
      if(pShiftDown != null) {	 
	buf.append("new HotKey(" + 
		   pShiftDown + ", " + pAltDown + ", " + pCtrlDown + ", " + pKeyCode + 
		   ");\n");
      }
      else {
	buf.append("null;\n");
      }
    }

    public void 
    genUI
    (
     StringBuffer buf,
     int level,
     boolean isLast
    )
    {
      buf.append
	(indent(level) + "p" + pTitle + " =\n" + 
	 indent(level+1) + "UIMaster.createTitledHotKeyField\n" + 
	 indent(level+2) + "(tpanel, \"" + pLabel + "\", sTSize,\n" + 
	 indent(level+2) + " vpanel, sVSize);\n" + 
	 "\n");

      if(!isLast) 
	buf.append
	  (indent(level) + "UIMaster.addVerticalSpacer(tpanel, vpanel, 3);\n" + 
	   "\n");
    } 

    public void 
    genSavePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "prefs.set" + pTitle + "(p" + pTitle + ".getHotKey());\n");
    } 
    
    public void 
    genUpdatePrefs
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "p" + pTitle + ".setHotKey(prefs.get" + pTitle + "());\n");
    } 

    public void 
    genDeclareUI
    (
     StringBuffer buf,
     int level
    )
    {
      buf.append(indent(level) + "private JHotKeyField  p" + pTitle + ";\n");
    } 

    protected Boolean  pShiftDown;
    protected Boolean  pAltDown;
    protected Boolean  pCtrlDown;
    protected Integer  pKeyCode;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of user preferences indexed by grouping name.
   */ 
  private TreeMap<String,BasePref[]>  pPrefs;

}


