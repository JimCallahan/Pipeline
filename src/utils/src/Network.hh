// $Id: Network.hh,v 1.1 2004/04/06 08:58:09 jim Exp $

#ifndef PIPELINE_NETWORK_HH
#define PIPELINE_NETWORK_HH

#ifdef HAVE_CONFIG_H
#  include "config.h"
#endif

#ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
#endif

#ifdef HAVE_SYS_SOCKET_H
#  include <sys/socket.h>
#endif

#ifdef HAVE_NETINET_IN_H
#  include <netinet/in.h>
#endif

#ifdef HAVE_NETDB_H
#  include <netdb.h>
#endif

#ifdef HAVE_ARPA_INET_H
#  include <arpa/inet.h>	
#endif

#ifdef HAVE_UNISTD_H
#  include <unistd.h>
#endif

#ifdef HAVE_ERRNO_H
#  include <errno.h>
#endif


#include <FB.hh>

namespace Pipeline {

/*------------------------------------------------------------------------------------------*/
/*   N E T W O R K                                                                          */
/*                                                                                          */
/*     A collection of static networking convience methods.                                 */
/*------------------------------------------------------------------------------------------*/

class Network
{
public:
  /*----------------------------------------------------------------------------------------*/
  /*   N A M E   R E S O L U T I O N                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Lookup the (first) IP address for the given hostname.
   * 
   * param hostname
   *   The name of the host to lookup.
   * 
   * return 
   *   A dynamically allocated address string.
   */ 
  static char* 
  resolve
  (
   const char* hostname
  ) 
  {
    struct hostent* hent;
    if((hent = gethostbyname(hostname)) == NULL) {
      char msg[1024];
      sprintf(msg, "Unable to determine host address: %s", hstrerror(h_errno));
      FB::error(msg);
    }

    char* addr = new char[INET_ADDRSTRLEN];
    if(::inet_ntop(AF_INET, *(hent->h_addr_list), addr, sizeof(addr)) == NULL) {
      delete[] addr;
      char msg[1024];
      sprintf(msg, "Unable to translate address into a human readable format: %s", 
	      *(hent->h_addr_list));
      FB::error(msg);
    }

    return addr;
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S O C K E T S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create an endpoint for communication (TCP only).
   * 
   * return 
   *   The socket descriptor.
   */ 
  static int
  socket() 
  { 
    int sd;
    if((sd = ::socket(AF_INET, SOCK_STREAM, 0)) == -1) {
      char msg[1024];
      sprintf(msg, "Unable to create a socket: %s", strerror(errno));
      FB::error(msg);
    }
    return sd;
  }
  
  /**
   * Set the reuse local address option for the given socket.
   * 
   * param sd
   *   The socket descriptor.
   * 
   * param tf
   *   Whether to turn the option on or off.
   */ 
  static void
  setReuseAddr
  (
   int sd,    
   bool tf
  ) 
  {
    int val = (int) tf;
    if(setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &val, sizeof(val)) == -1) {
      char msg[1024];
      sprintf(msg, "Unable to set the reuse local address option: %s", strerror(errno));
      FB::error(msg);
    }
  }
  
  /**
   * Assign a local protocol address to a socket.
   * 
   * param sd
   *   The socket descriptor.
   * 
   * param port
   *   The network port number.
   */ 
  static void
  bind
  ( 
   int sd,          
   int port   
  ) 
  { 
    struct sockaddr_in addr;

    bzero(&addr, sizeof(addr));
    addr.sin_family      = AF_INET;
    addr.sin_addr.s_addr = htonl(INADDR_ANY);
    addr.sin_port        = htons(port);	

    if(::bind(sd, (struct sockaddr *) &addr, sizeof(addr)) == -1) {
      char msg[1024];
      sprintf(msg, "Unable to bind the socket: %s", strerror(errno));
      FB::error(msg);
    }
  }

  /**
   * Listen for connections on the given socket.
   * 
   * param sd
   *   The socket descriptor.
   * 
   * param backlog
   *   The size of request queue.
   */
  static void
  listen 
  (
   int sd,           
   int backlog = 100   
  ) 
  {
    if(::listen(sd, backlog) == -1)  {
      char msg[1024];
      sprintf(msg, "Unable to listen to the socket: %s", strerror(errno));
      FB::error(msg);
    }
  }
  
  /**
   * Accept an incoming connection on the given socket.
   * 
   * param sd
   *   The primary socket descriptor.
   * 
   * return 
   *   The connected socket descriptor.
   */
  static int
  accept
  (   
   int sd       
  ) 
  {
    int csd;

    if((csd = ::accept(sd, (struct sockaddr *) NULL, NULL)) == -1) {
      char msg[1024];
      sprintf(msg, "Unable to accept a connection: %s", strerror(errno));
      FB::error(msg);
    }

    return csd;
  }
  
  /** 
   * Initiate a outgoing connection on the given socket.
   * 
   * Can retry (tries) number of times at (delay) intervals if server refuses connection.
   * 
   * param sd
   *   The socket descriptor.
   * 
   * param name
   *   Address of the host which which to connect -- see resolve().
   * 
   * param port
   *   The network port number.
   * 
   * param tries
   *   The number of attempts to establish the connection before giving up.
   * 
   * param delay
   *   The number of seconds between attempts to establish a connection.
   */ 
  static void
  connect
  (
   int sd,             
   const char* name,   
   int port,           
   unsigned tries = 1, 
   unsigned delay = 5  
  ) 
  {
    struct sockaddr_in addr;

    bzero(&addr, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port   = htons(port);

    if(::inet_pton(AF_INET, name, &addr.sin_addr) <= 0) {
      char msg[1024];
      sprintf(msg, "Unable to translate human readable address (%s) into INET format: %s", 
	      name, strerror(errno));
      FB::error(msg);
    }

    unsigned wk;
    for(wk=0; wk<=tries; wk++) {
      if(::connect(sd, (struct sockaddr *) &addr, sizeof(addr)) < 0) {
	char msg[1024];
	switch(errno) {
	case ECONNREFUSED: 
	  sprintf(msg, "Unable to connect to (%s): %s", name, strerror(errno));
	  FB::warn(msg);

	  if(wk < (tries-1)) {
	    sprintf(msg, "Will try again in (%d) seconds...", delay);
	    FB::warn(msg);
	  }

	  sleep(delay);
	  break;

	default:
	  sprintf(msg, "Connection to (%s) failed: %s", name, strerror(errno));
	  FB::error(msg);
	};
      }
      else {
	return;
      }
    }

    {
      char msg[1024];
      sprintf(msg, "Unable to connect to (%s) after (%d) tries!", name, tries);
      FB::error(msg);
    }
  }
   


  /*----------------------------------------------------------------------------------------*/
  /*   I / O   W R A P P E R S                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Read a fixed number of raw bytes from a socket.
   * 
   * param sd
   *   The socket descriptor.
   * 
   * param data
   *   The destination of the bytes read.
   * 
   * param size
   *   The number of bytes to read.
   * 
   * return 
   *   The number of bytes read.
   */ 
  static ssize_t
  read
  (
   int sd,       
   void* data,   
   size_t size  
  )
  {
    size_t n;
    if((n = readHelper(sd, data, size)) == -1) {
      char msg[1024];
      sprintf(msg, "Unable to read (%d) bytes from the socket (%d): %s", 
	      size, sd, strerror(errno));
      FB::error(msg);
    }

    return n;
  }

  /**
   * Write a fixed number of raw bytes to a socket.
   * 
   * param sd
   *   The socket descriptor.
   * 
   * param data
   *   The source of the bytes to write.
   * 
   * param size
   *   The number of bytes to write.
   * 
   * return 
   *   The number of bytes written.
   */ 
  static void
  write
  (
   int sd,          
   const void* data, 
   size_t size      
  )
  {
    if(writeHelper(sd, data, size) != size) {
      char msg[1024];
      sprintf(msg, "Unable to write (%d) bytes to the socket (%d): %s", 
	      size, sd, strerror(errno));
      FB::error(msg);
    }
  }



private: 
  /*----------------------------------------------------------------------------------------*/
  /*   I / O   H E L P E R S                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Read a fixed number of raw bytes from a socket.
   * 
   * param sd
   *   The socket descriptor.
   * 
   * param data
   *   The destination of the bytes read.
   * 
   * param size
   *   The number of bytes to read.
   * 
   * return 
   *   The number of bytes read.
   */ 
  static ssize_t
  readHelper
  (
   int sd,       
   void* data,    
   size_t size   
  )
  {
    size_t  nleft;
    ssize_t  nread;
    char  *p;

    p = (char*) data;
    nleft = size;

    while(nleft > 0) {
      if((nread = ::read(sd, p, nleft)) < 0) {
	if (errno == EINTR)
	  nread = 0;
	else
	  return -1;
      } else if (nread == 0) {
	break;			
      }
      
      nleft -= nread;
      p += nread;
    }

    return (size - nleft);
  }

  /**
   * Write a fixed number of raw bytes to a socket.
   * 
   * param sd
   *   The socket descriptor.
   * 
   * param data
   *   The source of the bytes to write.
   * 
   * param size
   *   The number of bytes to write.
   * 
   * return 
   *   The number of bytes written.
   */ 
  static ssize_t
  writeHelper
  (
   int sd,             
   const void* data,   
   size_t size    
  )
  {
    size_t  nleft;
    ssize_t  nwritten;
    const char* p;
    
    p = (const char*) data;
    nleft = size;

    while(nleft > 0) {
      if((nwritten = ::write(sd, p, nleft)) <= 0) {
	if (errno == EINTR)
	  nwritten = 0;
	else
	  return -1;	
      }
      
      nleft -= nwritten;
      p += nwritten;
    }

    return size;
  }

};

} // namespace Pipeline

#endif
