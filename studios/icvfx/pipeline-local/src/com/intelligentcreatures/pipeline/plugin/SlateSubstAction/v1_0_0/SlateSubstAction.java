// $Id: SlateSubstAction.java,v 1.5 2008/03/19 22:43:30 jim Exp $

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
 * following parameters.  Each parameter lists the exact string that will be replaced.  If 
 * no matching string is found in the template Nuke script for a parameter, it will be 
 * ignored.<P> 
 * 
 * The TemplateScript should be project specific and contain all constant information such
 * as the project name, IC logos and titles hardcoded into the Nuke script.  Information 
 * such as shot lengths and frame counters can be built into this template Nuke script using
 * Nuke expressions.  For best results, the template script should be written with expressions
 * that make it adaptive to the resolution of the source images. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Template Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the template slate Nuke script. 
 *   </DIV> <BR>
 * 
 *   Delivery Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     The reason the deliverable was created. The value of this parameter will be 
 *     substituted for all instances of "@IC_DELIVERY_TYPE@" in the template Nuke script.
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
 *     The client revision number.  This revision number is unrelated to Pipeline's revision
 *     number for the source images and is purely for external client use.  This value will 
 *     be substituted for all instances of "@IC_CLIENT_VERSION@" in the template Nuke script.
 *   </DIV> 
 *   <P> 
 * 
 * 
 *   Source Images <BR>
 *   <DIV style="margin-left: 40px;">
 *     The file sequence name (prefix.#.suffix,1-10x1) of the source images being delivered. 
 *     This value will be substituted for all instances of "@IC_SOURCE_IMAGES@" in the 
 *     template Nuke script.
 *   </DIV> <BR>
 * 
 *   Source Node <BR>
 *   <DIV style="margin-left: 40px;">
 *     The fully resolved node name of the source images being delivered.  This value will 
 *     be substituted for all instances of "@IC_SOURCE_NODE@" in the template Nuke script.
 *   </DIV> <BR>
 * 
 *   Source Version <BR>
 *   <DIV style="margin-left: 40px;">
 *     The revision number of the source images node being delivered.  This value will 
 *     be substituted for all instances of "@IC_SOURCE_VERSION@" in the template Nuke
 *     script.
 *   </DIV> <BR>
 *   <P> 
 * 
 * 
 *   Created On <BR>
 *   <DIV style="margin-left: 40px;">
 *     The date when the Deliverable was created. This value will be substituted for all 
 *     instances of "@IC_CREATED_ON@" in the template Nuke script.
 *   </DIV> 
 * 
 *   Created By <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the artist responsible for the creating the images being reviewed. This 
 *     value will be substituted for all instances of "@IC_CREATED_BY@" in the template 
 *     Nuke script.
 *   </DIV> 
 * 
 *   Notes <BR>
 *   <DIV style="margin-left: 40px;">
 *     A short description of the Deliverable.  This value will be substituted for all 
 *     instances of "@IC_NOTES@" in the template Nuke script.
 *   </DIV> 
 *   <P> 
 * 
 *   Slate Hold <BR> 
 *   <DIV style="margin-left: 40px;">
 *     The number of frames to hold the constant slate image before the images being 
 *     reviewed begin animating.  This value will be substituted for all instances of 
 *     "@IC_SLATE_HOLD@" in the template Nuke script.
 *   </DIV> 
 * </DIV> <P> 
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
        (aDeliveryType,
	 "The reason the deliverable was created. The value of this parameter will be " + 
	 "substituted for all instances of \"@IC_DELIVERY_TYPE@\" in the template Nuke " + 
	 "script.",
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
	 "revision number for the source images and is purely for external client use. " + 
	 "This value will be substituted for all instances of \"@IC_CLIENT_VERSION@\" in " + 
	 "the template Nuke script.", 
         null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
        new StringActionParam
        (aSourceImages, 
	 "The file sequence name (prefix.#.suffix,1-10x1) of the source images being " + 
	 "delivered. This value will be substituted for all instances of " + 
	 "\"@IC_SOURCE_IMAGES@\" in the template Nuke script.", 
         null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
        new StringActionParam
	(aSourceNode,
	 "The fully resolved node name of the source images being delivered.  This value " + 
	 "will be substituted for all instances of \"@IC_SOURCE_NODE@\" in the template " +
	 "Nuke script.", 
         null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
        new StringActionParam
        (aSourceVersion,
	 "The revision number of the source images node being delivered.  This value will " + 
	 "be substituted for all instances of \"@IC_SOURCE_VERSION@\" in the template Nuke " +
	 "script.", 
         null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
        new StringActionParam
        (aCreatedOn,
	 "The date when the Deliverable was created.  This value will be substituted for " + 
	 "all instances of \"@IC_CREATED_ON@\" in the template Nuke script.", 
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
        new TextAreaActionParam
        (aNotes,
	 "A short description of the Deliverable.  This value will be substituted for all " + 
	 "instances of \"@IC_NOTES@\" in the template Nuke script.", 
         null, 8);
      addSingleParam(param);
    }


    {
      ActionParam param = 
        new IntegerActionParam
        (aSlateHold,
	 "The number of frames to hold the constant slate image before the images being " +
	 "reviewed begin animating.  This value will be substituted for all instances of " + 
	 "\"@IC_SLATE_HOLD@\" in the template Nuke script.", 
         null);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aTemplateScript);     
      layout.addSeparator(); 
      layout.addEntry(aDeliveryType);  
      layout.addEntry(aDeliverable);      
      layout.addEntry(aClientVersion);    
      layout.addSeparator();       
      layout.addEntry(aSourceImages); 
      layout.addEntry(aSourceNode); 
      layout.addEntry(aSourceVersion);     
      layout.addSeparator();    
      layout.addEntry(aCreatedOn);  
      layout.addEntry(aCreatedBy);  
      layout.addEntry(aNotes);    
      layout.addSeparator();      
      layout.addEntry(aSlateHold);    

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
    String deliveryType  = getSingleStringParamValue(aDeliveryType); 
    if(deliveryType == null) 
      deliveryType = "";

    String deliverable = getSingleStringParamValue(aDeliverable); 
    if(deliverable == null) 
      deliverable = "";

    String clientVersion = getSingleStringParamValue(aClientVersion); 
    if(clientVersion == null) 
      clientVersion = "";

    String sourceImages = getSingleStringParamValue(aSourceImages); 
    if(sourceImages == null) 
      sourceImages = "";

    String sourceNode = getSingleStringParamValue(aSourceNode); 
    if(sourceNode == null) 
      sourceNode = "";

    String sourceVersion = getSingleStringParamValue(aSourceVersion); 
    if(sourceVersion == null) 
      sourceVersion = "";

    String createdBy = getSingleStringParamValue(aCreatedBy); 
    if(createdBy == null) 
      createdBy = "";

    String createdOn = getSingleStringParamValue(aCreatedOn); 
    if(createdOn == null) 
      createdOn = "";

    int slateHold = getSingleIntegerParamValue(aSlateHold); 

    /* escape unprintable characters in the notes */ 
    String notes = null; 
    {
      StringBuilder buf = new StringBuilder(); 

      String value = getSingleStringParamValue(aNotes); 
      if(value != null) {
	char[] cs = value.toCharArray();
	int wk;
	for(wk=0; wk<cs.length; wk++) {
	  switch(cs[wk]) {
	  case '\\':
	    buf.append("\\\\\\\\");
	    break;

	  case '\t':
	    buf.append("        ");
	    break;

	  case '\n':
	  case '\r':
	  case '\f':
	    buf.append("\\\\n");
	    break;
	    
	  case '\"':
	    buf.append("\\\\\"");
	    break;
	    
	  default:
	    buf.append(cs[wk]);
	  }
	}
	
	notes = buf.toString();
      }
    }


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

	  line = sDeliveryType.matcher(line).replaceAll(deliveryType);
	  line = sDeliverable.matcher(line).replaceAll(deliverable);
	  line = sClientVersion.matcher(line).replaceAll(clientVersion);
	  line = sSourceImages.matcher(line).replaceAll(sourceImages);
	  line = sSourceNode.matcher(line).replaceAll(sourceNode);
	  line = sSourceVersion.matcher(line).replaceAll(sourceVersion);
	  line = sCreatedBy.matcher(line).replaceAll(createdBy);
	  line = sCreatedOn.matcher(line).replaceAll(createdOn);
	  line = sNotes.matcher(line).replaceAll(notes);
	  line = sSlateHold.matcher(line).replaceAll(Integer.toString(slateHold));
	  
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

  private static Pattern sDeliveryType  = Pattern.compile("@IC_DELIVERY_TYPE@"); 
  private static Pattern sDeliverable   = Pattern.compile("@IC_DELIVERABLE@"); 
  private static Pattern sClientVersion = Pattern.compile("@IC_CLIENT_VERSION@"); 
  private static Pattern sSourceImages  = Pattern.compile("@IC_SOURCE_IMAGES@"); 
  private static Pattern sSourceNode    = Pattern.compile("@IC_SOURCE_NODE@"); 
  private static Pattern sSourceVersion = Pattern.compile("@IC_SOURCE_VERSION@"); 
  private static Pattern sCreatedOn     = Pattern.compile("@IC_CREATED_ON@"); 
  private static Pattern sCreatedBy     = Pattern.compile("@IC_CREATED_BY@"); 
  private static Pattern sNotes         = Pattern.compile("@IC_NOTES@"); 
  private static Pattern sSlateHold     = Pattern.compile("@IC_SLATE_HOLD@"); 

  private static final long serialVersionUID = 4067697635457036452L;

  public static final String aTemplateScript = "TemplateScript"; 
  public static final String aDeliveryType   = "DeliveryType"; 
  public static final String aDeliverable    = "Deliverable"; 
  public static final String aClientVersion  = "ClientVersion"; 
  public static final String aSourceImages   = "SourceImages"; 
  public static final String aSourceNode     = "SourceNode"; 
  public static final String aSourceVersion  = "SourceVersion"; 
  public static final String aCreatedOn      = "CreatedOn"; 
  public static final String aCreatedBy      = "CreatedBy"; 
  public static final String aNotes          = "Notes"; 
  public static final String aSlateHold      = "SlateHold"; 

}

