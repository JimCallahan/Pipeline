package com.sony.scea.pipeline.plugin.ModelReplaceAction.v1_2_0;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/**
 * Replaces the assets referenced into the selected source node with different 
 * models referenced into the target node. The replacement is done based on search 
 * suffixes for the source and target assets, e.g lr, syf, etc 
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.2.0
 */
public class 
ModelReplaceAction  
  extends BaseAction 
{
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
	
  private static final long serialVersionUID = 6832777372147046613L;
  private static final String aPreReplace = "PreReplaceMEL";
  private static final String aSource = "Source";
  private static final String aPostReplace = "PostReplaceMEL";
  private static final String aSrcSuff = "SourceSuffix";
  private static final String aTrgtSuff = "TargetSuffix";
  private static final String aResponse = "Response";
  private static final String aRemove = "Remove";
  private static final String aIgnore = "Ignore";
	

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  ModelReplaceAction() 
  {
    super("ModelReplace", new VersionID("1.2.0"), "SCEA",
          "Gives the ability to get the sources of selected source node and reference " +
          "other versions of those sources such as cloth, hr models to a target node. " +
          "The user can choose the response should a source node not have any matching " +
          "nodes.");
    {
      ActionParam param = new LinkActionParam
        (aPreReplace, "The mel script to be run before replacing references.", null);
      addSingleParam(param);
    }
    {
      ActionParam param = new LinkActionParam
        (aSource, "The source node which contains the files to replace.", null);
      addSingleParam(param);
    }
    {
      ActionParam param = new LinkActionParam
        (aPostReplace, "The mel script to be run after replacing references.", null);
      addSingleParam(param);
    }
    {
      ActionParam param = new StringActionParam
        (aSrcSuff,"The suffix of files to replace, e.g lr, syf.","lr");
      addSingleParam(param);
    }
    {
      ActionParam param = new StringActionParam
        (aTrgtSuff,"The suffix of files to be used as replacement","");
      addSingleParam(param);
    }
    {
      ArrayList<String> values = new ArrayList<String>();
      values.add(aIgnore);
      values.add(aRemove);
      ActionParam param = new EnumActionParam
        (aResponse,"Action to take if a matching replacement cannot be found",
         aIgnore, values);
      addSingleParam(param);
    }
		
		
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aSource);
      layout.addSeparator();   
      layout.addEntry(aPreReplace);
      layout.addEntry(aPostReplace);
      layout.addSeparator();
      layout.addEntry(aSrcSuff);
      layout.addEntry(aTrgtSuff);
      layout.addSeparator();
      layout.addEntry(aResponse);  

      setSingleLayout(layout);   
    }
    underDevelopment();

  }

  /**
   * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will 
   * fulfill the given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda or a general failure of the prep method code.
   */
  @Override
  public SubProcessHeavy 
  prep(ActionAgenda agenda, File outFile, File errFile) 
    throws PipelineException 
  {
    File debug = createTemp(agenda, 0644, "err");
    PrintWriter err = null;
    try {
      err = new PrintWriter (new BufferedWriter (new FileWriter(debug)));
    } catch (IOException e) {
      throw new PipelineException(e);
    }

    FileSeq target = agenda.getPrimaryTarget();
    String replacePattern = ".*/assets/(character|set|prop)/";
    String modelPattern = replacePattern + ".*";

    String sname = (String) getSingleParamValue(aSource);
    String preMel = (String) getSingleParamValue(aPreReplace);
    String postMel = (String) getSingleParamValue(aPostReplace);
    String srcSuff = (String) getSingleParamValue(aSrcSuff);
    String trgtSuff = (String) getSingleParamValue(aTrgtSuff);
    String resp = (String) ((EnumActionParam) getSingleParam(aResponse)).getValue();


    err.println("Source suffix is: "+srcSuff);
    err.println("Target suffix is: "+trgtSuff+"\n");

    if(srcSuff.equals(trgtSuff))		
      throw new PipelineException("The source suffix is identical to the target suffix");

    /*--The source fileName--*/
    NodeID snodeID = new NodeID(agenda.getNodeID(),sname);
    Path nodePath = 
      new Path(PackageInfo.sProdPath,
               snodeID.getWorkingParent() + "/" + agenda.getPrimarySource(sname).getPath(0));
    String fileName = nodePath.toOsString();


    /*---the target fileName---*/
    Path trgtNode = new Path(PackageInfo.sProdPath, 
                             agenda.getNodeID().getWorkingParent() + "/" + target.getPath(0));
    String targetFileName = trgtNode.toOsString();


    if(sname == null) 
      throw new PipelineException ("The Source node was not set!");

    if(!agenda.getSourceNames().contains(sname))
      throw new PipelineException 
        ("The Source node (" + sname + ") is not linked to the target node " + 
         "(" + target + ")!");

    TreeSet<String> nomatchRef = new TreeSet<String>();

    /*--get models connected to the source----------------*/
    ActionInfo srcInfo = agenda.getSourceActionInfo(sname);
    TreeSet<String> srcLinks = new TreeSet<String>(srcInfo.getSourceNames());

    /*
      err.println("The original sources are: ");
      for (String s : srcLinks)
      err.println("\t" + s);
    */

    for(String link : srcInfo.getSourceNames()){
      if(!(link.endsWith(srcSuff) && link.matches(modelPattern))){
        err.println(link+" is not the appropriate type: "+srcSuff);
        srcLinks.remove(link);

        nomatchRef.add(link);
      }//end if
      if((srcSuff.length()==0)&&(link.indexOf('_')>0))
        srcLinks.remove(link);

    }//end for

    err.println("Source Models are: ");
    for (String s : srcLinks)
      err.println("\t" + s);
    err.println("");

    /*--get models connected to the target---------------------*/
    TreeSet<String> trgtLinks = new TreeSet<String>(agenda.getSourceNames());

    for(String link: agenda.getSourceNames()) {
      if(!(link.endsWith(trgtSuff) && link.matches(modelPattern))){
        err.println(link+" is not the appropriate type");
        trgtLinks.remove(link);
      }//end if
    }//end for
    err.println("Target Models are: ");
    for (String s : trgtLinks)
      err.println("\t" + s);
    err.println("");


    /*--Check for the models linked to both nodes---------------------*/

    TreeMap<String,String> actualLinks = new TreeMap<String,String>();
    for(String src: srcLinks){

      String check = src.replaceFirst(replacePattern, "");			
      int end = check.indexOf('/');
      if(end<0)
        end = check.length();

      check = check.substring(0, end);
      err.println("Pattern is "+check);

      for(String trgt: trgtLinks){
        err.println("The matched string was \" " + (replacePattern+check+"/.*\"") );
        err.println("The trgt string was: " + trgt);
        if(trgt.matches(replacePattern+check+"/.*")) {
          actualLinks.put(src,trgt);
          nomatchRef.remove(src);
        } else {					
          if(resp.equals(aRemove) && (!actualLinks.keySet().contains(src)))
            nomatchRef.add(src);
        }
      }
    }

    err.println("The common ones are:\n"+actualLinks+"\n");
    err.println("The ones to remove or skip are\n"+nomatchRef);		

    /*--Write the sed file--------------*/
    File sedFile = createTemp(agenda, 0755, "sed");
    try {
      FileWriter out = new FileWriter(sedFile);

      out.write("#!/bin/sed -f\n\n");
      String trgtRef = null;
      for(String srcRef: actualLinks.keySet()){
        trgtRef = actualLinks.get(srcRef);
        out.write("s:"+srcRef+":"+trgtRef+":g\n");			
      }//end for
      out.close();
    } catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary script file (" + sedFile + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }//end catch


    err.close();

    /*--Write the mel script----*/
    File melScript = null;
    if((preMel!=null) || (postMel != null) || (!nomatchRef.isEmpty())){
      melScript = createTemp(agenda, 0755,"mel");

      try {
        PrintWriter out = new PrintWriter(new BufferedWriter(new 
                                                             FileWriter(melScript)));	    

        if(preMel!=null) {
          NodeID melID = new NodeID(agenda.getNodeID(),preMel);
          Path melPath = new Path(PackageInfo.sProdPath,
                                  melID.getWorkingParent() + "/" + agenda.getPrimarySource(preMel).getPath(0));
          out.println("source \""+melPath.toOsString()+"\";");
        }

        for(String asset : nomatchRef) {
          out.println("print (\"dereferencing file: $WORKING"+asset+".ma\\n\");");
          out.println("catchQuiet(`file -rr \"$WORKING"+asset+".ma\"`);");
        }//end for

        out.println("string $refs[] = `file -q -r`;");
        out.println("string $ref;");
        out.println("for ($ref in $refs) {");
        out.println("	string $nmSpc = `file -q -rfn $ref`;");
        out.println("	file -lr $nmSpc $ref;");
        out.println("}");

        if(postMel!=null) {
          NodeID melID = new NodeID(agenda.getNodeID(),postMel);
          Path melPath = new Path(PackageInfo.sProdPath,
                                  melID.getWorkingParent() + "/" + agenda.getPrimarySource(postMel).getPath(0));			
          out.println("source \""+melPath.toOsString()+"\";");
        }
				
        out.println("// SAVE");
        out.println("file -save;");
				
        out.close();
      }//end try
      catch(IOException ex) {
        throw new PipelineException("Unable to write the temporary MEL script(" 
                                    + melScript + ") used add the references!");
      }//end catch

    }	

    /*--Write the bash script----*/
    File script = createTemp(agenda, 0755, "bash");

    try {      
      FileWriter out = new FileWriter(script);
      out.write("#!/bin/bash\n\n");
      out.write("cat "+fileName+
                " | sed -f "+sedFile.getPath()+ " "+ fileName+
                " > "+ targetFileName +"\n");
      if((preMel!=null) || (postMel != null) || (!nomatchRef.isEmpty())){ 
        out.write("maya -batch -script "+melScript.getPath()+" ");
        out.write("-command \"file -lnr -open \\\""+targetFileName+"\\\"\"\n");
      }
      out.close();
    } catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    } //end catch


    try {
      return new SubProcessHeavy
        (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
         script.getPath(), new ArrayList<String>(), 
         agenda.getEnvironment(), agenda.getWorkingDir(), 
         outFile, errFile);
    }//end try
    catch(Exception ex) {
      throw new PipelineException
        ("Unable to generate the SubProcess to perform this Action!\n" 
         + ex.getMessage());
    }//end catch   

  }//end prep
}//end class ModelReplaceAction
