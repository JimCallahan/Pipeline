package com.sony.scea.pipeline.plugins.v1_0_0;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/**
 * Packages a selected node, and all source nodes used to create it,
 * into a tar file for delivery.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class DeliveryPackageTool extends BaseTool
{
   //static String pPrimary;
   //static TreeMap<String, NodeStatus> pSelected;
   
   /*-----------------------------------------------*/
   /*                  STATIC INTERNALS                    */
   /*-----------------------------------------------*/
   private static final long serialVersionUID = 4095489317343749489L;
   
   
   /*-----------------------------------------------*/
   /*                  INTERNALS                    */
   /*-----------------------------------------------*/
   
   /**
    * The root repository directory 
    */
   private String REPOSITORY = "/kronos/csg/pipeline/repository";
   private String pWORKING = "/kronos/csg/pipeline/working/";
   /**
    * A collection of the nodes in the network.
    */
   private TreeMap<Integer, NodeStatus> pNodes;
   private TreeMap<Integer, NodeStatus> pExclusions;
   
   /**
    * A list of links from one node to another
    */
   private TreeSet<String> pLinks;
   
   private int pPack;
   private int pDiag;
   private int pNodeCount;
   @SuppressWarnings("unused")
   private TreeSet<String> pSuffixExc;


   
   /**
    * The current working area view.
    */
   private String pView;
   

   /**
    * The current working area user.
    */
   private String pUser;   
   
   
   /*-----------------------------------------------*/
   /*                 CONSTRUCTOR                   */
   /*-----------------------------------------------*/   
   /**
    * Packages a selected node, and all source nodes used to create it,
    * into a tar file for delivery.
    */
   public DeliveryPackageTool(){
      super("Delivery Package Tool", new VersionID("1.0.0"), "SCEA",
	 "Packages a selected node, and all source nodes used to create it,"
	 +" into a tar file for delivery.");
      underDevelopment();
      pNodes = new TreeMap<Integer, NodeStatus>();
      pExclusions = new TreeMap<Integer, NodeStatus>();
      pLinks = new TreeSet<String>();
   
      pNodeCount = 0;
      pPack = 0;
      pDiag = 0;
      pSuffixExc = new TreeSet<String>();
   }//end default constructor
   
   
   /**
    * Check that the user has properly selected a target node for this tool <P>
    * 
    * @return 
    *   The phase progress message or <CODE>null</CODE> to abort early.
    * 
    * @throws PipelineException
    *   If unable to validate the given user input.
    */
   public synchronized String collectPhaseInput() throws PipelineException
   {
      if ( pPrimary == null )
	 throw new PipelineException("The primary selection must be the Target Node!");

      if ( pSelected.size() != 1 )
	 throw new PipelineException("Only one Target Node may be selected.");

      NodeStatus status = pSelected.get(pPrimary);
      NodeID id = status.getNodeID();
      pView = id.getView();
      pUser = id.getAuthor();
      
      OverallNodeState state = status.getDetails().getOverallNodeState();
	
      if(!state.equals(OverallNodeState.Identical)){
	 throw new PipelineException("This node has not been checked in. You can only " +
	 	"package a checked in node!!");
      }//end if
      

      JTextField excFld = null;
      JIntegerField packFld = null;
      JIntegerField diagFld = null;
      
      
      
      /* DO GUI DRAWING STUFF*/
      JScrollPane scroll = null;
      {
	 int twidth=120;
	 int vwidth=200;
	 Box ibox = new Box(BoxLayout.Y_AXIS);
	 {
	    Component comps[] = UIFactory.createTitledPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];
	    JDrawer drawer = new JDrawer("Package Options: " , (JComponent) comps[2], true);
	    ibox.add(drawer);

	    excFld = UIFactory.createTitledEditableTextField(tpanel, 
	       "Excluded Extensions:", twidth, vpanel, null, vwidth);
	    packFld = UIFactory.createTitledIntegerField(tpanel, 
	       "Levels to Pack", twidth, vpanel, null, vwidth);
	    diagFld = UIFactory.createTitledIntegerField(tpanel, 
	       "Levels to Diagram", twidth, vpanel, null, vwidth);
  		  	
	    UIFactory.addVerticalSpacer(tpanel, vpanel, 3);		    		  
	 }		      
	     
	 {
	    JPanel spanel = new JPanel();
	    spanel.setName("Spacer");

	    spanel.setMinimumSize(new Dimension(twidth + vwidth, 7));
	    spanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	    spanel.setPreferredSize(new Dimension(twidth + vwidth, 7));

	    ibox.add(spanel);
	 }

	 {
	    scroll = new JScrollPane(ibox);
	    scroll
	    .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroll
	    .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    
	    Dimension size = new Dimension(twidth + vwidth + 52, 150);
	    scroll.setMinimumSize(size);
	    scroll.setPreferredSize(size);

	    scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
	 }
	}

	/* query the user */
	JToolDialog diag = new JToolDialog("Delivery Package Options", scroll,
							"Confirm");      
	diag.setVisible(true);
	diag.setResizable(false);
	
      if(!diag.wasConfirmed())
	 return null;
 
      pPack = (packFld.getValue()==null)? 0:packFld.getValue().intValue();
      pDiag = (diagFld.getValue()==null)? 0:diagFld.getValue().intValue();
      
      if(pPack>pDiag)
	 throw new PipelineException("You cannot pack more levels than you diagram!!");
      
      String exc = excFld.getText();
      StringTokenizer tok = new StringTokenizer(exc,",. ");
      while(tok.hasMoreTokens()){
	 pSuffixExc.add(tok.nextToken());
      }           
      
      return ": packaging node tree.";
   }

   /**
    * Perform execution of the tool.<P>
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
   public synchronized boolean executePhase(MasterMgrClient mclient, 
	 QueueMgrClient qclient) throws PipelineException
   {
      NodeStatus stat  = pSelected.get(pPrimary);
      getNodeList(pPrimary, stat, 1);   
      String fName = makeTxtFile();
      makeTarFile(mclient,fName);
      return false;
   }

   
   /*-----------------------------------------------*/
   /*              HELPER FUNCTIONS                 */
   /*-----------------------------------------------*/
   
   
   /**
    * Populate the master list of nodes, with the source nodes of the 
    * argument <i>stat</i>. If a node is already in the master list, it is skipped.
    * It also keeps track of the links between nodes. 
    * 
    * @param nodeName The name of the current node
    * @param stat The NodeStatus for that node.
    * @return The node number, which is then used for keeping track of links between 
    * nodes.
    */   
   public int getNodeList(String nodeName, NodeStatus stat, int level){
      
      if((!pNodes.containsValue(stat))&&(!(pExclusions.containsValue(stat)))){
	 FileSeq fseq = stat.getDetails().getWorkingVersion().getPrimarySequence();
	 String suffix = fseq.getFilePattern().getSuffix();

	 if((level<=pDiag)||(pDiag==0)) {
	    int cur = ++pNodeCount;
	    /*Pack*/
	    if((level<=pPack)||(pPack==0)) {
	       if(suffix!=null) {
		  if(pSuffixExc.contains(suffix)) {
		     pExclusions.put(cur,stat);
		  } else {
		     pNodes.put(cur,stat);
		  }
	       } else {
		  pNodes.put(cur,stat);
	       }
	    }
	    
	    Set<String> srcs = stat.getSourceNames();
	    for(String src: srcs) {
	       int nodeNum = getNodeList(src,stat.getSource(src),level+1);
	    
	       /*Link*/
	       if((nodeNum!=0)&&(level<=pDiag))
		  pLinks.add(cur+">"+nodeNum);
	    }//end for
	    return cur;
	 }//end if
      }
      return 0;
   }//end processNode
   
   
   /**
    * Creates a text file with a list of all nodes in the network, as well as 
    * the links between them.
    * 
    * @return 
    * @throws PipelineException
    */
   private String makeTxtFile() throws PipelineException{
      	
      NodeDetails det = pSelected.get(pPrimary).getDetails();
      VersionID mainID = det.getWorkingVersion().getWorkingID();
      
      String verNum = mainID.toString();
      verNum = verNum.replace(".", "_");
      String a = det.toString();
      a = a.substring(0, a.indexOf("."));
      
      
      /*--Write the txt file--------------*/
      String fName = "/usr/tmp/"+a+"_delivery_v"+verNum+".txt";		
      try {
	 File txtFile = new File(fName);
	 BufferedWriter out = new BufferedWriter( new FileWriter(txtFile));
	 
	 /*add the included files*/
	 for(Integer key: pNodes.keySet()){
	    NodeDetails cur = pNodes.get(key).getDetails();
	    VersionID id = cur.getWorkingVersion().getWorkingID();	 
	    
	    FileSeq fseq = cur.getWorkingVersion().getPrimarySequence();
	    if(fseq.numFrames()>1){
	       out.write(key+"\t"+cur.getName()+"\t"+fseq.toString() +"\t"+id+"\n");
	       
	    } else {
	       //System.err.println("Not a sequence");
	       out.write(key+"\t"+cur.getName()+"\t"+cur.getWorkingVersion()+"\t"+id+"\n");	 
	    }
	 }//end for
	 
	 /*add the excluded files*/
	 for(Integer key: pExclusions.keySet()){
	    NodeDetails cur = pExclusions.get(key).getDetails();
	    VersionID id = cur.getWorkingVersion().getWorkingID();	 
	    FileSeq fseq = cur.getWorkingVersion().getPrimarySequence();
	    if(fseq.numFrames()>1){
	       out.write(key+"\t"+cur.getName()+"\t"+fseq.toString() +"\t"+id+"\t[Excluded]\n");
	       
	    } else {
	       //System.err.println("Not a sequence");
	       out.write(key+"\t"+cur.getName()+"\t"+cur.getWorkingVersion()+"\t"+id+"\t[Excluded]\n");	 
	    }
	 }//end for
	 
	 out.write("\n");
	 for(String key: pLinks){
	    out.write(key+"\n");
	 }//end for
	      
	 out.close();
      } catch(IOException ex) {
	 throw new PipelineException
	 	("Unable to write temporary script file (" + fName + ")!\n" +
	  		ex.getMessage());
      }//end catch     
      return fName;
   }//end makeTxtFile
   
   
   /**
    * Creates and executes a bash script that will package all the files being 
    * delivered into a compressed .tar file. It will also add the txt file 
    * into this package.
    * 
    * @param mclient
    * @param txtFile The .txt file containing the list of files that have been 
    * 	packaged. 
    * @return 
    * @throws PipelineException
    */
   private boolean makeTarFile(MasterMgrClient mclient, String txtFile) throws PipelineException{
      NodeDetails det = pSelected.get(pPrimary).getDetails();
      NodeMod mod = det.getWorkingVersion();
      NodeID id = pSelected.get(pPrimary).getNodeID();
      
      String a = det.toString();
      a = a.substring(0, a.indexOf("."));
      
      /*---Choose the location of the file-----------------*/
      JToolDialog tool = new JToolDialog("DeliveryPackage", new JPanel(), "Continue");
      JFileSelectDialog dialog = 
	 new JFileSelectDialog(tool, "Choose File","","Target File",25,"Create");
      dialog.updateTargetFile(new File(pWORKING+pUser+"/"));
      dialog.setVisible(true);
      String tarFile = dialog.getSelectedFile().toString();
      if((tarFile==null)||(tarFile.equals(""))||tarFile.equals("/")){
	 return false;
      }//end if
      if(!tarFile.endsWith(".tgz")){
	 tarFile+=".tgz";
      }//end if
      
      
      /*---Write the bash script for making the tar file---*/
      String fName = "/usr/tmp/"+a+"_delivery.bash";
      File script = null;    
      
      try {      
	 script = new File(fName);
	 BufferedWriter out = new BufferedWriter( new FileWriter(script));

	 out.write("#!/bin/bash\n\n");
	 out.write("cd "+REPOSITORY+"\n");
	 out.write("tar -czf "+tarFile+" "+txtFile);
	 for(Integer key: pNodes.keySet()){
	    NodeDetails cur = pNodes.get(key).getDetails();
	    VersionID curVer = cur.getWorkingVersion().getWorkingID();
	    
	    String name = cur.getName();
	    name = name.substring(1,name.length());
	    
	    FileSeq fseq = cur.getWorkingVersion().getPrimarySequence();
	    FilePattern pat = fseq.getFilePattern();
	    
	    if(fseq.numFrames()>1){
	       //it is a file sequence
	       int start = fseq.getFrameRange().getStart();
	       int inc = fseq.getFrameRange().getBy();
	       int end = fseq.getFrameRange().getEnd();
	       for (int i = start; i <= end; i+=inc)
	       {		  
		  out.write(" "+name+"/"+curVer+"/"+pat.getFile(i));
	       }//end for
	       
	    } else {	       
	       out.write(" "+name+"/"+curVer+"/"+cur.getWorkingVersion());	 
	    }//end else
	 }//end for
	 
	 out.write("\n");
	 out.close();
      
      } catch(IOException ex) {
	 throw new PipelineException
	 ("Unable to write temporary script file (" + fName + ")!\n" +
	    ex.getMessage());
      } //end catch
      
      
      /*---Create the tar file--------------------------------*/

      try {
	 ArrayList<String> args = new ArrayList<String>();
	 args.add(script.toString());
	 TreeMap<String,String> env = 
	    mclient.getToolsetEnvironment
	    (PackageInfo.sUser, pView, mod.getToolset(), PackageInfo.sOsType);
		
	 Path wpath = new Path(PackageInfo.sProdPath, id.getWorkingParent());
	 SubProcessLight proc = 
	    new SubProcessLight("DeliveryPackage-Tar", "bash", args, env, wpath.toFile());
	 
	 try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) {
	       throw new PipelineException
	       ("Failed to create the tar file for delivery" +
		  proc.getStdOut() + "\n\n" + 
		  proc.getStdErr());
	    }//end if
	 }
	 catch(InterruptedException ex) {
	    throw new PipelineException(ex);
	 }
     } catch(Exception ex) {
	 throw new PipelineException(ex);
     }
      return true;
   }
   
   
   /*-----------------------------------------------*/
   /*                  TEST MAIN                    */
   /*-----------------------------------------------*/
   public static void main(String[] args)
   {
      /*try {
	MasterMgrClient mclient = new MasterMgrClient();
	QueueMgrClient qclient = new QueueMgrClient();
	PluginMgrClient.init();
	pSelected = new TreeMap<String, NodeStatus>();
	DeliveryPackageTool test = new DeliveryPackageTool();
	
	String author = "iojomo";
	String view = "default";
	pPrimary = "/projects/lr/production/lair/unihead/knowlton/img/test/unihead_knowlton_200";
	NodeStatus value = mclient.status(author,view, pPrimary);
	pSelected.put(pPrimary, value);			

	while(test.collectPhaseInput()!=null){
	   if(!test.executePhase(mclient,qclient))
	      break;
	}

      } catch(PipelineException e){
	e.printStackTrace();
      }
      System.exit(0);
      */
   }

}