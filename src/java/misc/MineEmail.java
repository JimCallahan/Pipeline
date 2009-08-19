// $Id: MineEmail.java,v 1.1 2009/08/19 23:58:21 jim Exp $

import java.io.*; 
import java.util.*; 
import java.util.regex.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   M I N E   E M A I L                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Read my Thunderbird mailbox to gather a list of email addresses which are Pipeline 
 * related.
 */ 
class MineEmail
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args  /* IN: command line arguments */
  )
  {
    try {
      if(args.length < 2) {
	System.out.print
          ("usage: MineEmail address-output mailbox-dir1 [maibox-dir2 ...]\n\n"); 
	System.exit(1);
      }

      ArrayList<File> mailboxes = new ArrayList<File>();
      for(String arg : args) 
        mailboxes.add(new File(arg));

      MineEmail app = new MineEmail(new File(args[0]), mailboxes); 
      app.mine(); 
    }
    catch(Exception ex) {
      System.out.print("INTERNAL-ERROR:\n");
      ex.printStackTrace(System.out);
      System.exit(1);
    }
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  MineEmail
  (
   File addressOutput,
   List<File> mailboxes 
  ) 
  {
    pAddressOutput = addressOutput; 
    pMailboxes = mailboxes; 

    pAddrLine = Pattern.compile("(?:From|To|CC):\\s+(.+)");

    pLongAddrs = new ArrayList<Pattern>();
    pLongAddrs.add(Pattern.compile("\"[^\"]+\"\\s?<([\\p{Alnum}\\.]+)@([\\p{Alnum}\\.]+)>")); 
    pLongAddrs.add(Pattern.compile("[^<]*<([\\p{Alnum}\\.]+)@([\\p{Alnum}\\.]+)>")); 
    pLongAddrs.add(Pattern.compile("([\\p{Alnum}\\.]+)@([\\p{Alnum}\\.]+)")); 
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   *
   */ 
  public void 
  mine() 
    throws IOException 
  {
    pAddrs = new TreeMap<String,TreeSet<String>>();

    for(File file : pMailboxes) {
      if(file.isDirectory()) 
        processMailbox(file); 
      else if(!file.getName().endsWith(".msf")) 
        processFolder(file); 
    }

    System.out.print("Writing: " + pAddressOutput + "\n"); 
    BufferedWriter out = new BufferedWriter(new FileWriter(pAddressOutput));
    try {
      for(String domain : pAddrs.keySet()) {
        for(String name : pAddrs.get(domain)) 
          out.write(name + "@" + domain + "\n"); 
      }
    }
    finally {
      out.close();
    }

    System.out.print("DONE.\n"); 
  }
  
  /**
   *
   */
  private void 
  processMailbox
  (
   File dir
  ) 
    throws IOException 
  {
    for(File file : dir.listFiles()) {
      if(file.isDirectory())
        processMailbox(file);
      else if(!file.getName().endsWith(".msf")) 
        processFolder(file); 
    }
  }
  
  /**
   *
   */
  private void 
  processFolder
  (
   File file
  )
    throws IOException 
  {
    System.out.print("Mining: " + file + "\n"); 

    BufferedReader in = new BufferedReader(new FileReader(file));
    try {
      while(true) {
        String line = in.readLine(); 
        if(line == null) 
          break;

        Matcher m = pAddrLine.matcher(line);
        if(m.matches()) {
          int acnt = m.groupCount(); 
          int ak;
          for(ak=1; ak<=acnt; ak++) {
            String aline = m.group(ak);

            System.out.print("Aline: " + aline + "\n"); 

            if((aline != null) && (aline.length() > 0)) {
              String parts[] = aline.split("[,;]");
              for(String part : parts) {
                String trimmed = part.trim();

                System.out.print("  Trimmed: " + trimmed + "\n"); 

                if(trimmed.length() > 0) {
                  int pk = 1;
                  for(Pattern pat : pLongAddrs) {                    
                    System.out.print("    Pattern: " + pk + "\n"); 

                    Matcher m2 = pat.matcher(trimmed);
                    if(m2.matches()) {
                      if(m2.groupCount() == 2) {
                        String name   = m2.group(1);
                        String domain = m2.group(2); 
                        if((name != null) && (name.length() > 0) &&
                           (domain != null) && (domain.length() > 0)) {

                          String name2   = name.toLowerCase();
                          String domain2 = domain.toLowerCase();

                          TreeSet<String> names = pAddrs.get(domain2); 
                          if(names == null) {
                            names = new TreeSet<String>();
                            pAddrs.put(domain2, names);
                          }
                          names.add(name2); 

                          System.out.print("      Addr: " + name2 + " (at) " + domain2 + "\n"); 
                          break;
                        }
                      }
                    }

                    pk++;
                  }
                }
              }
            }
          }
        }
      }
    }
    finally {
      in.close();
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private File pAddressOutput;
  private List<File> pMailboxes; 

  private Pattern pAddrLine; 
  private ArrayList<Pattern> pLongAddrs; 

  private TreeMap<String,TreeSet<String>> pAddrs; 
}
