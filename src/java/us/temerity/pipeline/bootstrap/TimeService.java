// $Id: TimeService.java,v 1.5 2005/01/12 13:07:15 jim Exp $

package us.temerity.pipeline.bootstrap;

import java.io.*;
import java.util.*;
import java.net.*;

/*------------------------------------------------------------------------------------------*/
/*   T I M E   S E R V I C E                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A very simple NTP client which bypasses the local system to determine the current time.
 */ 
class TimeService
{  
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

  /**
   * Get the current time from one of a trusted list of NTP time servers.
   *
   * @throws IOException 
   *   If unable to successfully query any of the NTP servers.
   */ 
  public static long
  getTime() 
    throws IOException
  {
    StringBuffer buf = new StringBuffer();

    int wk;
    for(wk=0; wk<sNtpServers.length; wk++) {
      try {
	return getTime(sNtpServers[wk]);
      }
      catch(IOException ex) {
	buf.append(ex.getMessage());
      }
    }

    throw new IOException(buf.toString());
  }
  
  /**
   * Wheterh the current time is within the given interval. <P> 
   * 
   * If the time reported by a time server is outside the interval, check the next time
   * server until successful or all time servers have been exhasted.
   * 
   * @param start
   *   The timestamp of the start of the time interval.
   * 
   * @param end
   *   The timestamp of the end of the time interval.
   * 
   * @throws IOException 
   *   If unable to successfully query any of the NTP servers.
   */
  public static boolean
  isValid
  (
   long start, 
   long end
  ) 
    throws IOException
  {
    StringBuffer buf = new StringBuffer();

    int checked = 0;
    int wk;
    for(wk=0; wk<sNtpServers.length; wk++) {
      try {
	long stamp = getTime(sNtpServers[wk]);
	if((stamp > start) && (stamp < end)) 
	  return true;
	checked++;
      }
      catch(IOException ex) {
	buf.append(ex.getMessage());
      }
    }

    if(checked == 0) 
      throw new IOException(buf.toString());

    return false;
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
    "dewey.lib.ci.phoenix.az.us",
    "clock.develooper.com",
    "clock.fmt.he.net",
    "clock.sjc.he.net",
    "hydrogen.cert.ucr.edu",
    "ntp1.linuxmedialabs.com",
    "louie.udel.edu",
    "ntp-1.cso.uiuc.edu",
    "ntp-2.cso.uiuc.edu",
    "gilbreth.ecn.purdue.edu",
    "harbor.ecn.purdue.edu",
    "ntp1.kansas.net",
    "ntp2.kansas.net",
    "ntp.ourconcord.net",
    "time.johnstalker.ca",
    "clock1.unc.edu",
    "ntp.uhfradio.com",
    "clock.nyc.he.net",
    "reva.sixgirls.org",
    "sundial.columbia.edu",
    "timex.cs.columbia.edu",
    "ntp-1.ece.cmu.edu",
    "ntp-2.ece.cmu.edu",
    "chrono.cis.sac.accd.edu",
    "ntppub.tamu.edu",
    "sundial.cis.sac.accd.edu",
    "ticker.cis.sac.accd.edu",
    "ntp-4.vt.edu",
    "ntp-1.vt.edu",
    "ntp-3.vt.edu",
    "ntp-2.vt.edu",
    "ntp1.cs.wisc.edu",
    "ntp3.cs.wisc.edu"
  };

}
