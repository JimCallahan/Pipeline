package com.sony.scea.pipeline.tools;

import java.util.*;

import us.temerity.pipeline.FrameRange;
import us.temerity.pipeline.PipelineException;

/**
 * Utility class containing static methods used to parse command line arguments
 * to certain standalong Pipeline tools. <p>
 * Supports arguments in two formats, a --<i>param</i>=<i>value</i> format 
 * and a --<i>flag</i>.
 *  
 * @author Jesse Clemens
 *
 */
public class GlobalsArgs
{

  private static final String argsFormat = "^--\\w+=.*";
  private static final String argsFormat2 = "^--.*";

  /**
   * Sorts through a list of strings and extracts all the command line arguments
   * from it.<p>
   * @param args
   * 	The list of command line args to look at
   * @return a list of all the arguments and their values, if any
   * @throws PipelineException
   */
  public static TreeMap<String, LinkedList<String>> argParser(String args[])
    throws PipelineException
  {
    TreeMap<String, LinkedList<String>> toReturn = new TreeMap<String, LinkedList<String>>();
    for (String arg : args)
    {
      if ( arg.matches(argsFormat) )
      {
	arg = arg.replace("--", "");
	String buffer[] = arg.split("=");
	LinkedList<String> hold = toReturn.get(buffer[0]);
	if ( hold == null )
	{
	  hold = new LinkedList<String>();
	}
	hold.add(buffer[1]);
	toReturn.put(buffer[0], hold);
      } else if ( arg.matches(argsFormat2) )
      {
	arg = arg.replace("--", "");
	toReturn.put(arg, null);
      } else
      {
	throw new PipelineException(arg + " is not a valid argument");
      }
    }
    return toReturn;
  }

  /**
   * A method for extracting an int value from a parameter list. 
   * <p>
   * This method assumes that only one instance of that flag is allowable. If more than one
   * instance of that flag was passed on the command line, this method will
   * throw a {@link PipelineException PipelineException}. Note that if the
   * <code>required</code> flag is set to true, the method will throw a
   * {@link PipelineException PipelineException} if it cannot find the flag. If
   * it is set to false, it will return null if the flag does not exist.
   * 
   * @param arg
   *           The name of the argument
   * @param required
   *           Is this flag required?
   * @param parsedArgs
   *           A TreeMap of command line args in the format <argname, list of
   *           values>
   * @return the value of the arg
   * @throws PipelineException
   */
  public static Integer getIntValue(String arg, boolean required,
      TreeMap<String, LinkedList<String>> parsedArgs) throws PipelineException
  {
    LinkedList<String> values = parsedArgs.remove(arg);
    if ( values == null ) {
      if ( required )
	throw new PipelineException("ERROR: You must use the --" + arg + " flag.");
      return null;
    }
    if ( values.size() > 1 )
      throw new PipelineException("ERROR: You cannot use the --" + arg
	  + " flag more than once.");

    int toReturn;
    try
    {
      toReturn = Integer.parseInt(values.getFirst());
    } catch ( NumberFormatException ex )
    {
      throw new PipelineException("ERROR: The values passed to the --" + arg + " flag"
	  + " must be an integer");
    }
    return toReturn;
  }

  /**
   * A method for extracting an string value from a parameter list. <p>This method
   * assumes that only one instance of that flag is allowable. If more than one
   * instance of that flag was passed on the command line, this method will
   * throw a {@link PipelineException PipelineException}. Note that if the
   * <code>required</code> flag is set to true, the method will throw a
   * {@link PipelineException PipelineException} if it cannot find the flag. If
   * it is set to false, it will return null if the flag does not exist.
   * 
   * @param arg
   *           The name of the argument
   * @param required
   *           Is this flag required?
   * @param parsedArgs
   *           A TreeMap of command line args in the format <argname, list of
   *           values>
   * @return the value of the arg
   * @throws PipelineException
   */
  public static String getStringValue(String arg, boolean required,
      TreeMap<String, LinkedList<String>> parsedArgs) throws PipelineException
  {
    LinkedList<String> values = parsedArgs.remove(arg);
    if ( values == null ) {
      if ( required )
	throw new PipelineException("ERROR: You must use the --" + arg + " flag.");
      return null;
    }
    if ( values.size() > 1 )
      throw new PipelineException("ERROR: You cannot use the --" + arg
	  + " flag more than once.");
    return values.getFirst();
  }

  /**
   * A method for extracting a boolean value from a parameter list.<p> This method
   * assumes that only one instance of that flag is allowable. If more than one
   * instance of that flag was passed on the command line, this method will
   * throw a {@link PipelineException PipelineException}. Note that if the
   * <code>required</code> flag is set to true, the method will throw a
   * {@link PipelineException PipelineException} if it cannot find the flag. If
   * it is set to false, it will return null if the flag does not exist.
   * 
   * @param arg
   *           The name of the argument
   * @param required
   *           Is this flag required?
   * @param parsedArgs
   *           A TreeMap of command line args in the format <argname, list of
   *           values>
   * @return the value of the arg
   * @throws PipelineException
   */
  public static Boolean getBooleanValue(String arg, boolean required,
      TreeMap<String, LinkedList<String>> parsedArgs) throws PipelineException
  {
    LinkedList<String> values = parsedArgs.remove(arg);
    if ( values == null ) {
      if ( required )
	throw new PipelineException("ERROR: You must use the --" + arg + " flag.");
      return null;
    }
    if ( values.size() > 1 )
      throw new PipelineException("ERROR: You cannot use the --" + arg
	  + " flag more than once.");
    String first = values.getFirst().toLowerCase();
    boolean toReturn;
    if ( first.equals("true") )
      toReturn = true;
    else if ( first.equals("false") )
      toReturn = false;
    else
      throw new PipelineException("ERROR: The value (" + values.getFirst()
	  + ") is not a valid boolean argument");
    return toReturn;
  }

  /**
   * A method for extracting multiple string value from a parameter list.<p> 
   * 
   * Note that if the <code>required</code> flag is set to true, the method will throw a
   * {@link PipelineException PipelineException} if it cannot find the flag. If
   * it is set to false, it will return null if the flag does not exist.
   * 
   * @param arg
   *           The name of the argument
   * @param required
   *           Is this flag required?
   * @param parsedArgs
   *           A TreeMap of command line args in the format <argname, list of
   *           values>
   * @return a list of values for the arg
   * @throws PipelineException
   */
  public static ArrayList<String> getStringValues(String arg, boolean required,
      TreeMap<String, LinkedList<String>> parsedArgs) throws PipelineException
  {
    LinkedList<String> values = parsedArgs.remove(arg);
    if ( values == null )
    {
      if ( required )
	throw new PipelineException("ERROR: You must have at least one instance "
	    + "of the --" + arg + " flag.");
      return null;
    }
    return new ArrayList<String>(values);
  }

  /**
   * Extracts a {@link FrameRange} from a list of command line arguments.<p>
   * @param startS the name of the param that represents the start frame.  required.
   * @param endS the name of the param that represents the end frame. 
   * 	required if there is a step
   * @param byS the name of the param that represents the step. 
   * 	required if there an end frame.
   * @param parsedArgs the list of arguments
   * @return the constructed {@link FrameRange}
   * @throws PipelineException
   */
  public static FrameRange getFrameRange(String startS, String endS, String byS,
      TreeMap<String, LinkedList<String>> parsedArgs) throws PipelineException
  {
    Integer start, end, by;

    start = getIntValue(startS, true, parsedArgs);
    end = getIntValue(endS, false, parsedArgs);
    by = getIntValue(byS, false, parsedArgs);
    if ( end == null && by != null )
      throw new PipelineException("The --by flag cannot be "
	  + "used unless the --end flag is used as well");

    if ( end == null )
      return new FrameRange(start);
    return new FrameRange(start, end, by);
  }

  /**
   * Prints a list of args to standard error.
   * @param parsedArgs the list of args to print.
   */
  public static void printExtraArgs(TreeMap<String, LinkedList<String>> parsedArgs)
  {
    String badArgs = "";
    for (String each : parsedArgs.keySet())
      badArgs += each + " ";
    System.err.println("The arguments (" + badArgs + ") are unknown");
  }
}
