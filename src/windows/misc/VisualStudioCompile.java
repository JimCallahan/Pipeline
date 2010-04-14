import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   V I S U A L   S T U D I O   C O M P I L E                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Runs Visual Studio compiler from the command-line with the appropriate environment 
 * via SSH.
 */ 
class VisualStudioCompile
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args 
  )
  {
    try {
      if(args.length < 1) {
        System.err.print("usage: VisualStudioCompile ...");
        System.exit(1);
      }

      String user = System.getProperty("user.name");
      String host = "skink";

      LinkedList<String> cmd = new LinkedList<String>();
      cmd.add("C:\\Program Files (x86)\\Microsoft Visual Studio 10.0\\Common7\\IDE\\devenv.com"); 
      for(String arg : args) 
        cmd.add(arg); 

      System.out.print("-------------------------------------------------\n"); 
      System.out.print("Command:\n  "); 
      for(String arg : cmd)
        System.out.print(arg + " ");
      System.out.print("\n\n"); 
      


      ProcessBuilder builder = new ProcessBuilder(cmd); 
      builder.redirectErrorStream(true); 

      Map<String, String> env = builder.environment();
      env.clear();

      env.put("ALLUSERSPROFILE", "C:\\ProgramData"); 
      env.put("APPDATA", "C:\\Users\\" + user + "\\AppData\\Roaming"); 
      env.put("CommonProgramFiles", "C:\\Program Files\\Common Files"); 
      env.put("CommonProgramFiles(x86)", "C:\\Program Files (x86)\\Common Files"); 
      env.put("CommonProgramW6432", "C:\\Program Files\\Common Files"); 
      env.put("COMPUTERNAME", host);  
      env.put("ComSpec", "C:\\Windows\\system32\\cmd.exe"); 
      env.put("FP_NO_HOST_CHECK", "NO"); 
      env.put("HOMEDRIVE", "C:"); 
      env.put("HOMEPATH", "\\Users\\" + user); 
      env.put("LOCALAPPDATA", "C:\\Users\\" + user + "\\AppData\\Local"); 
      env.put("LOGONSERVER", "\\\\" + host); 
      env.put("NUMBER_OF_PROCESSORS", "1"); 
      env.put("OS", "Windows_NT"); 
      env.put("Path", ("C:\\Windows\\system32;C:\\Windows;C:\\Windows\\System32\\Wbem;" + 
                       "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\;" + 
                       "c:\\Program Files (x86)\\Microsoft SQL Server\\100\\Tools\\Binn\\;" + 
                       "c:\\Program Files\\Microsoft SQL Server\\100\\Tools\\Binn\\;" + 
                       "c:\\Program Files\\Microsoft SQL Server\\100\\DTS\\Binn\\")); 
      env.put("PATHEXT", ".COM;.EXE;.BAT;.CMD;.VBS;.VBE;.JS;.JSE;.WSF;.WSH;.MSC"); 
      env.put("PROCESSOR_ARCHITECTURE", "AMD64"); 
      env.put("PROCESSOR_IDENTIFIER", "Intel64 Family 6 Model 23 Stepping 7, GenuineIntel"); 
      env.put("PROCESSOR_LEVEL", "6"); 
      env.put("PROCESSOR_REVISION", "1707"); 
      env.put("ProgramData", "C:\\ProgramData"); 
      env.put("ProgramFiles", "C:\\Program Files"); 
      env.put("ProgramFiles(x86)", "C:\\Program Files (x86)"); 
      env.put("ProgramW6432", "C:\\Program Files"); 
      env.put("PROMPT", "$P$G"); 
      env.put("PSModulePath", "C:\\Windows\\system32\\WindowsPowerShell\\v1.0\\Modules\\"); 
      env.put("PUBLIC", "C:\\Users\\Public"); 
      env.put("SESSIONNAME", "Console"); 
      env.put("SystemDrive", "C:"); 
      env.put("SystemRoot", "C:\\Windows"); 
      env.put("TEMP", "C:\\Users\\" + user + "\\AppData\\Local\\Temp"); 
      env.put("TMP", "C:\\Users\\" + user + "\\AppData\\Local\\Temp"); 
      env.put("USERDOMAIN", "" + host); 
      env.put("USERNAME", user); 
      env.put("USERPROFILE", "C:\\Users\\" + user); 
      env.put("VS100COMNTOOLS",
              "C:\\Program Files (x86)\\Microsoft Visual Studio 10.0\\Common7\\Tools\\"); 
      env.put("windir", "C:\\Windows"); 

      System.out.print("-------------------------------------------------\n"); 
      System.out.print("Environment:\n"); 
      for(Map.Entry<String,String> entry : env.entrySet())
        System.out.print("  " + entry.getKey() + " = " + entry.getValue() + "\n");
      System.out.print("\n"); 

      System.out.print("-------------------------------------------------\n" + 
                       "Running...\n"); 

      Process proc = builder.start();

      BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream())); 
      while(true) {
        String line = reader.readLine();
        if(line == null) 
          break;

        System.out.print(line + "\n"); 
      }

      proc.waitFor(); 
      System.exit(proc.exitValue()); 
    }
    catch(Exception ex) {
      System.out.print("INTERNAL-ERROR:\n");
      ex.printStackTrace(System.out);
      System.exit(1);
    }
  }

}


