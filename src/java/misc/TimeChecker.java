// $Id: TimeChecker.java,v 1.1 2005/01/14 16:01:57 jim Exp $

import java.io.*;
import java.util.*;
import java.net.*;

/*------------------------------------------------------------------------------------------*/
/*   T I M E   C H E C K E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A very simple NTP client which compares the local system to a known time server.
 */ 
class TimeChecker
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The top-level function of all Pipeline applications. <P> 
   * 
   * The first element of the command line arguments (<CODE>args[0]</CODE>) is the fully 
   * qualified name of the application class to launch.  This application class must be 
   * subclass of {@link BootApp BootApp}.  The remaining command-line arguments are passed 
   * unaltered to the application's {@link BootApp#run run} method. <P> 
   * 
   * @param args 
   *   The command line arguments. 
   */ 
  public static void 
  main
  (
   String[] args 
  )
  {
    try {
      if(args.length != 2) {
	System.out.print
	  ("usage: TimeChecker hostname interval\n" + 
	   "\n" + 
	   "  Contact the NTP server at (hostname) every (interval) milliseconds\n" +
	   "  and compares the time reported by the NTP server with the local\n" +
	   "  clock.  The results are printed to STDOUT.\n" + 
	   "\n" + 
	   "  Here are some NTP servers to try:\n");

	int wk;
	for(wk=0; wk<sNtpServers.length; wk++) 
	  System.out.print("    " + sNtpServers[wk] + "\n");
	System.out.print("\n");

	System.exit(1);
      }

      String hostname = args[0];
      long delay = Long.parseLong(args[1]);

      System.out.print
	("Checking (" + hostname + ") every (" + delay + ") milliseconds...\n\n" + 
	 "TIME DELTA:    NTP TIME:              LOCAL TIME:\n");
      
      while(true) {
	try {
	  long nstamp = getTime(hostname);
	  
	  Date ldate = new Date();
	  long lstamp = ldate.getTime();
	  
	  System.out.print
	    (String.format("%1$+11d    " +
			   "%2$tD %2$tH:%2$tM:%2$tS:%2$tL  " + 
			   "%3$tD %3$tH:%3$tM:%3$tS:%3$tL\n", 
			   (lstamp - nstamp), nstamp, lstamp));
	}
	catch(IOException ex) {
	  System.out.print(ex.getMessage() + "\n");
	}

	Thread.sleep(delay);
      }
    }
    catch(Exception ex) {
      System.out.print("INTERNAL-ERROR:\n");
      ex.printStackTrace(System.out);
      System.exit(1);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the current time according to the given NTP server.
   * 
   * @param hostname [<B>in<B>]
   *   The hostname of the NTP server.
   * 
   * @throws IOException 
   *   If unable to successfully query the NTP server.
   */ 
  public static long
  getTime
  (
   String hostname
  ) 
    throws IOException
  {
    try {
      DatagramSocket socket = new DatagramSocket();
      InetAddress address = InetAddress.getByName(hostname);
      
      byte[] buf = new byte[48];
      buf[0] = (3 << 3 | 3);
      
      DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);
      socket.send(packet);
      
      socket.setSoTimeout(5000);
      socket.receive(packet);
    
      return decode(packet.getData(), 40);
    }
    catch(Exception ex) {
      throw new IOException("Unable to get time from (" + hostname + "):\n" + 
			    "  " + ex.getMessage());
    }      
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Convert from a UTC timestamp to a 64-bit unsigned NPT timestamp format.
   */
  private static void 
  encode
  (
   byte[] buf, 
   int idx, 
   long stamp
  ) 
  {
    double dstamp = ((double) (stamp + 2208988800L)) / 1000.0;

    int wk;
    for(wk=0; wk<8; wk++) {
      double base = Math.pow(2, (3-wk)*8);
      buf[idx+wk] = (byte) (dstamp / base);
      dstamp = dstamp - (double) (ubyteToShort(buf[idx+wk]) * base);
    }
		
    buf[idx+7] = (byte) (Math.random()*255.0);
  }

  /** 
   * Convert from a 64-bit unsigned NPT timestamp format to a UTC timestamp.
   */
  private static long
  decode
  (
   byte[] buf, 
   int idx
  ) 
  {
    double dstamp = 0.0;
    
    int wk;
    for(wk=0; wk<8; wk++) 
      dstamp += ubyteToShort(buf[idx+wk]) * Math.pow(2, (3-wk)*8);
    
    return (long) ((dstamp - 2208988800.0) * 1000.0);
  }

  /**
   * Convert from a unsigned byte to a short.
   */
  private static short 
  ubyteToShort
  ( 
   byte b
  ) 
  {
    if((b & 0x80)==0x80) 
      return (short) (128 + (b & 0x7f));
    return (short) b;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The IP addresses of some trusted NTP servers.
   */
  private static final String[] sNtpServers = {
    "216.218.192.202",
    "216.218.254.202",
    "63.192.96.2",
    "63.192.96.3",
    "204.74.68.55",
    "204.87.183.6",
    "216.27.190.202",
    "130.126.24.53",
    "140.221.9.20",
    "130.126.24.44",
    "140.221.9.6",
    "199.240.130.1",
    "199.240.130.12",
    "216.204.156.2",
    "128.101.101.101",
    "134.84.84.84",
    "152.2.21.1",
    "64.35.195.62",
    "65.211.109.1",
    "65.211.109.11",
    "131.216.1.101",
    "131.216.22.15",
    "131.216.22.17",
    "209.51.161.238",
    "66.250.131.180",
    "128.59.59.177",
    "128.59.16.20",
    "128.118.25.3",
    "128.182.58.100",
    "146.186.218.60",
    "209.144.20.76",
    "128.249.1.10",
    "198.82.162.213",
    "198.82.161.227"
  };


}


