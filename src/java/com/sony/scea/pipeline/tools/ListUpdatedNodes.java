/*
 * Created on Aug 10, 2006
 * Created by jesse
 * For Use in com.sony.scea.pipeline.tools
 * 
 */
package com.sony.scea.pipeline.tools;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import us.temerity.pipeline.*;

/**
 * Stand alone program that can be used to list all the nodes in pipeline that
 * have had versions created between two dates. <p>
 * It can also take a regular expression representing a subset of nodes to search,
 * <p>
 * The args are <ul>
 * <li> --start=DD.MM.YYYY
 * <li> --end=DD.MM.YYYY
 * <li> --name="regexp"
 * </ul>
 * <p>
 * This functionality has since been added in plscript and this class is probably
 * no longer needed.  It remains a good example of how to extract information from 
 * Pipeline.
 * 
 * @author Jesse Clemens
 */
public class ListUpdatedNodes
{

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      start = null;
      end = null;
      regexp = "/.*";
      //System.err.println("parsing");
      boolean go = parseArgs(args);
      //System.err.println("done parsing");
      if ( !go )
	 System.exit(0);
      //System.err.println(regexp);
      try
      {
	 PluginMgrClient.init();
	 MasterMgrClient client = new MasterMgrClient();
	 ArrayList<ArchiveInfo> infos = client.archiveQuery(regexp, null);
	 //System.err.println(infos.size());
	 DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
	 for (ArchiveInfo info : infos)
	 {
	    Date stamp = new Date(info.getCheckedInStamp());
	    boolean write = true;
	    if ( start != null )
	       if ( stamp.before(start) )
		  write = false;
	    if ( end != null )
	       if ( stamp.after(end) )
		  write = false;

	    if ( write )
	       System.out.println(info.getName() + " " + info.getVersionID() + " "
		     + df.format(stamp));
	 }
      } catch ( PipelineException ex )
      {
	 ex.printStackTrace();
      }
   }

   public static boolean parseArgs(String[] args)
   {
      boolean toReturn = true;
      DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
      for (String arg : args)
      {
	 if ( arg.matches(argsFormat) )
	 {
	    arg = arg.replace("--", "");
	    String buffer[] = arg.split("=");
	    String command = buffer[0];
	    String value = buffer[1];
	    if ( command.equals("start") )
	    {
	       try
	       {
		  start = df.parse(value);
	       } catch ( Exception ex )
	       {
		  ex.printStackTrace();
		  return false;
	       }
	    } else if ( command.equals("end") )
	    {
	       try
	       {
		  end = df.parse(value);
	       } catch ( Exception ex )
	       {
		  ex.printStackTrace();
		  return false;
	       }
	    } else if ( command.equals("name") )
	    {
	       regexp = value;
	    } else
	       return false;
	 } else
	    toReturn = false;
      }
      return toReturn;
   }

   private static Date start;
   private static Date end;
   private static String regexp;

   private static final String argsFormat = "^--\\w+=.*";

}
