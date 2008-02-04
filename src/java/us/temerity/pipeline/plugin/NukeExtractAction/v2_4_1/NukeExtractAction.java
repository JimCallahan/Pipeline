// $Id: NukeExtractAction.java,v 1.1 2008/02/04 12:09:57 jim Exp $

package us.temerity.pipeline.plugin.NukeExtractAction.v2_4_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.regex.*; 
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   E X T R A C T   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Extracts script fragments containing Nuke nodes from a larger Nuke script by scanning 
 * for nodes who's name and/or type match a given regular expression pattern.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Nuke Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Nuke script to scan.
 *   </DIV> <BR>
 * 
 *   Type Pattern <BR>
 *   <DIV style="margin-left: 40px;">
 *     The regular expression to use to match the types of Nuke nodes to extract.
 *   </DIV> <BR>
 * 
 *   Name Pattern <BR>
 *   <DIV style="margin-left: 40px;">
 *     The regular expression to use to match the names of Nuke nodes to extract.  
 *   </DIV> <BR>
 * 
 *   Match Unnamed<BR> 
 *   <DIV style="margin-left: 40px;">
 *     Whether to match node's which do not have a name. 
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class NukeExtractAction
  extends NukeActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeExtractAction() 
  {
    super("NukeExtract", new VersionID("2.4.1"), "Temerity",
	  "Extracts script fragments containing Nuke nodes from a larger Nuke script " + 
          "by scanning for nodes who's name and/or type match a given regular expression " + 
          "pattern.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aNukeScript,
	 "The source node which contains the Nuke script to scan.", 
	 null);
      addSingleParam(param);
    } 

    { 
      ActionParam param = 
	new StringActionParam
	(aTypePattern,
	 "The regular expression to use to match the types of Nuke nodes to extract.", 
         ".*"); 
      addSingleParam(param);
    }

    { 
      ActionParam param = 
	new StringActionParam
	(aNamePattern,
	 "The regular expression to use to match the names of Nuke nodes to extract.", 
         ".*"); 
      addSingleParam(param);
    }

    {
      ActionParam param =
	new BooleanActionParam
	(aMatchUnnamed, 
	 "Whether to match node's which do not have a name.",
	 false);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aNukeScript);       
      layout.addSeparator();  
      layout.addEntry(aTypePattern);   
      layout.addEntry(aNamePattern);   
      layout.addEntry(aMatchUnnamed); 

      setSingleLayout(layout);  
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
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
    /* the source Nuke script to scan */
    Path sourceScript = null;
    {
      ArrayList<String> suffixes = new ArrayList<String>();
      suffixes.add("nk");
      suffixes.add("nuke");

      sourceScript = getPrimarySourcePath(aNukeScript, agenda, suffixes, "Nuke script");
      if(sourceScript == null) 
        throw new PipelineException("The NukeScript node was not specified!");
    }

    /* target Nuke script */ 
    Path nukePath = 
      getPrimaryTargetPath(agenda, NukeActionUtils.getNukeExtensions(), "Nuke Script");

    /* get precompiled node type regular expression */ 
    Pattern typePattern = null;
    {
      String pat = getSingleStringParamValue(aTypePattern, true);
      if(pat != null) {
        try {
          typePattern = Pattern.compile(pat); 
        }
        catch(PatternSyntaxException ex) {
          throw new PipelineException 
            ("Illegal TypePattern (" + pat + "):\n\n" + ex.getMessage());
        }
      }
    }

    /* get precompiled node name regular expression */ 
    Pattern namePattern = null;
    {
      String pat = getSingleStringParamValue(aNamePattern, true);
      if(pat != null) {
        try {
          namePattern = Pattern.compile(pat); 
        }
        catch(PatternSyntaxException ex) {
          throw new PipelineException 
            ("Illegal NamePattern (" + pat + "):\n\n" + ex.getMessage());
        }
      }
    }

    /* whether to match unnamed nodes */ 
    boolean matchUnnamed = getSingleBooleanParamValue(aMatchUnnamed); 

    /* create a temporary Nuke script containing the matching Nuke nodes */ 
    File script = createTemp(agenda, "nk");
    try {
      BufferedReader in = new BufferedReader(new FileReader(sourceScript.toFile())); 
      FileWriter out = new FileWriter(script); 

      int lnum = 0;
      try { 
        boolean matchedName = false; 
        StringBuilder buf = null; 
        while(true) {
          String line = in.readLine();
          if(line == null) 
            break;
          lnum++;

          /* if we are not currently processing a node block... */ 
          if(buf == null) {
            /* is this the start of a new block? 
                 if not, just skip the line */ 
            Matcher m = sNodeStart.matcher(line);
            if(m.matches()) {   
              String typeName = m.group(1);
              if((typeName == null) || (typeName.length() == 0)) 
                throw new PipelineException
                  ("Syntax Error in (" + sourceScript + ") at line [" + lnum + "]:  " + 
                   "Illegal start of node block.\n  " + line);

              if((typePattern == null) || typePattern.matcher(typeName).matches()) {
                matchedName = false; 
                buf = new StringBuilder();
                buf.append(line + "\n");
              }
            }
          }
          
          /* we are in the middle of processing a node block... */ 
          else {
            buf.append(line + "\n"); 
            
            /* is this the "name" property? */ 
            Matcher m = sNodeName.matcher(line);
            if(m.matches()) {
              String nodeName = m.group(1);
              if((nodeName == null) || (nodeName.length() == 0))
                throw new PipelineException
                  ("Syntax Error in (" + sourceScript + ") at line [" + lnum + "]:  " + 
                   "Illegal node name specification.\n  " + line);
               
              if((namePattern == null) || namePattern.matcher(nodeName).matches()) 
                matchedName = true;
              else 
                buf = null;
            }

            /* or have finished a block? */ 
            else if(line.equals("}")) {
              if(matchedName || matchUnnamed) 
                out.write(buf.toString()); 

              matchedName = false; 
              buf = null;
            }
          }
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
      
    /* just copy the temporary Nuke script to the target location */ 
    return createTempCopySubProcess(agenda, script, nukePath, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static Pattern sNodeStart = 
    Pattern.compile("([A-Z][A-Za-z0-9]*)[ \\t]+\\{"); 

  private static Pattern sNodeName = 
    Pattern.compile("[ \\t]*name[ \\t]+([A-Za-z0-9_/.\\-]+)"); 

  private static final long serialVersionUID = -5515331307231488310L;

  public static final String aNukeScript   = "NukeScript";
  public static final String aTypePattern  = "TypePattern";
  public static final String aNamePattern  = "NamePattern";
  public static final String aMatchUnnamed = "MatchUnnamed";

}
