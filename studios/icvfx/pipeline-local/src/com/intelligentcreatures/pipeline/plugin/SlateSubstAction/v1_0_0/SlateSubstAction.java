// $Id: SlateSubstAction.java,v 1.1 2008/03/10 05:54:44 jim Exp $

package com.intelligentcreatures.pipeline.plugin.SlateSubstAction.v1_0_0; 

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.regex.*; 
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S L A T E   S U B S T   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a Nuke script used to add slate information to images shown to clients by 
 * by performing string replacements on a template slate Nuke script.<P> 
 * 
 * The strings in the template Nuke script that will be replaced correspond to one of the
 * following parameters.  Each parameter list the exact string that will be replaced.  If 
 * no matching string is found in the template Nuke script for a parameter, it will be 
 * ignored.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Template Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the template slate Nuke script. 
 *   </DIV> <BR>
 * 
 *   Deliverable <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name for the content of the images being delivered to the client.  Typically this
 *     will be based on a combination of the shot (or asset) and Pipeline task which 
 *     generated the images such as: "ri120-blot" or "rorschach-model".  The value of this 
 *     parameter will be substituted for all instances of "@IC_DELIVERABLE@" in the template 
 *     Nuke script.
 *   </DIV> <BR>
 * 
 *   Client Version <BR>
 *   <DIV style="margin-left: 40px;">
 *     The client revision number.  This revision number is unrelated to Pipeline's VersionID
 *     for the source images and is purely for external client use.  This value will be 
 *     substituted for all instances of "@IC_CLIENT_VERSION@" in the template Nuke script.
 *   </DIV> 
 *   <P> 
 * 
 *   Created By <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the artist responsible for the creating the images being reviewed. This 
 *     value will be substituted for all instances of "@IC_CREATED_BY@" in the template 
 *     Nuke script.
 *   </DIV> 
 * 
 *   Supervisor <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the supervisor resposible for the images being reviewed. This value
 *     will be substituted for all instances of "@IC_SUPERVISOR@" in the template 
 *     Nuke script.
 *   </DIV> 
 * </DIV> 
 */
public
class SlateSubstAction
  extends NukeActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  SlateSubstAction() 
  {
    super("SlateSubst", new VersionID("1.0.0"), "ICVFX", 
          "Generates a Nuke script used to add slate information to images shown to " + 
	  "clients by by performing string replacements on a template slate Nuke script.");
    
    {
      ActionParam param = 
        new LinkActionParam
        (aTemplateScript,
         "The source which contains the template slate Nuke script.", 
         null);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
        new StringActionParam
        (aDeliverable,
	 "The name for the content of the images being delivered to the client. Typically " + 
	 "this will be based on a combination of the shot (or asset) and Pipeline task " + 
	 "which generated the images such as: \"ri120-blot\" or \"rorschach-model\".  The " + 
	 "value of this parameter will be substituted for all instances of " + 
	 "\"@IC_DELIVERABLE@\" in the template Nuke script.", 
         null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
        new StringActionParam
        (aClientVersion,
	 "The client revision number.  This revision number is unrelated to Pipeline's " + 
	 "VersionID for the source images and is purely for external client use.  This " + 
	 "value will be substituted for all instances of \"@IC_CLIENT_VERSION@\" in the " + 
	 "template Nuke script.", 
         null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
        new StringActionParam
        (aCreatedBy,
	 "The name of the artist responsible for the creating the images being reviewed. " + 
	 "This value will be substituted for all instances of \"@IC_CREATED_BY@\" in the " + 
	 "template Nuke script.", 
         null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
        new StringActionParam
        (aSupervisor,
	 "The name of the supervisor resposible for the images being reviewed. This value " +
	 "will be substituted for all instances of \"@IC_SUPERVISOR@\" in the template " + 
	 "Nuke script.", 
         null);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aTemplateScript);     
      layout.addSeparator();
      layout.addEntry(aDeliverable);     
      layout.addEntry(aClientVersion);     
      layout.addSeparator();     
      layout.addEntry(aCreatedBy);  
      layout.addEntry(aSupervisor);  

      setSingleLayout(layout);  
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    underDevelopment(); 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

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
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  { 
    /* target Nuke script */ 
    Path target = getPrimaryTargetPath(agenda, getNukeExtensions(), "Nuke Script");

    /* the template Nuke script */
    Path templateScript = null;
    {
      ArrayList<String> suffixes = new ArrayList<String>();
      suffixes.add("nk");
      suffixes.add("nuke");

      templateScript = getPrimarySourcePath(aTemplateScript, agenda, suffixes, "Nuke script");
      if(templateScript == null) 
        throw new PipelineException
          ("The " + aTemplateScript + " node was not specified!");
    }

    /* replacement values */ 
    String deliverable   = getSingleStringParamValue(aDeliverable); 
    String clientVersion = getSingleStringParamValue(aClientVersion); 
    String createdBy     = getSingleStringParamValue(aCreatedBy); 
    String supervisor    = getSingleStringParamValue(aSupervisor); 

    /* create a temporary Nuke script by string replacing the template script */  
    File script = createTemp(agenda, "nk");
    try {
      FileWriter out = new FileWriter(script); 
      BufferedReader in = new BufferedReader(new FileReader(templateScript.toFile())); 
      try {
	while(true) {
	  String line = in.readLine();
	  if(line == null) 
	    break;

	  if(deliverable != null) 
	    line = sDeliverable.matcher(line).replaceAll(deliverable);

	  if(clientVersion != null) 
	    line = sClientVersion.matcher(line).replaceAll(clientVersion);
	  
	  if(createdBy != null) 
	    line = sCreatedBy.matcher(line).replaceAll(createdBy);

	  if(supervisor != null) 
	    line = sSupervisor.matcher(line).replaceAll(supervisor);

	  out.write(line + "\n"); 
	}
      }
      finally {
	in.close();
        out.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary Nuke script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    /* create the process to run the action */ 
    return createTempCopySubProcess(agenda, script, target, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static Pattern sDeliverable   = Pattern.compile("@IC_DELIVERABLE@"); 
  private static Pattern sClientVersion = Pattern.compile("@IC_CLIENT_VERSION@"); 
  private static Pattern sCreatedBy     = Pattern.compile("@IC_CREATED_BY@"); 
  private static Pattern sSupervisor    = Pattern.compile("@IC_SUPERVISOR@"); 

  private static final long serialVersionUID = 4067697635457036452L;

  public static final String aTemplateScript = "TemplateScript"; 
  public static final String aDeliverable    = "Deliverable"; 
  public static final String aClientVersion  = "ClientVersion"; 
  public static final String aCreatedBy      = "CreatedBy"; 
  public static final String aSupervisor     = "Supervisor"; 

}

