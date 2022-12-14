
package com.jamonapi;



/**
 *
 * The following should be put in the Web Application's web.xml file to enable servlet monitoring.
 * This servlet filter will enable any file access to html, jpg, jsp, servlet or any other file 
 * resource that is part of the web application.   You can change the filter-mapping element below
 * in the web.xml file in include/not include different files from monitoring.  Use JAMonAdmin.jsp
 * to display any collected data.
 *
<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE web-app
    PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
    "http://java.sun.com/dtd/web-app_2_3.dtd">

<web-app>
   <display-name>fdsapi</display-name>
   <filter>
    <filter-name>JAMonFilter</filter-name>
    <filter-class>com.jamonapi.JAMonFilter</filter-class>
   </filter>
	
   <filter-mapping>
     <filter-name>JAMonFilter</filter-name>
     <url-pattern>/*</url-pattern>
   </filter-mapping>

<!--
   <servlet>
    <servlet-name>demo</servlet-name>
    <jsp-file>/demo.jsp</jsp-file>
   </servlet>
-->

</web-app>
 */

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;

public class JAMonFilter extends HttpServlet implements Filter {

private static final long serialVersionUID = 1L;
    


   public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
     Monitor allPages = MonitorFactory.start(new MonKeyImp("com.jamonapi.allPages",getURI(request),"ms."));
	 Monitor monitor = MonitorFactory.start(getURI(request));

     try {
      filterChain.doFilter(request, response);
     } finally {
      monitor.stop();
      allPages.stop();
     }
   }

   protected String getURI(ServletRequest request) {
     if (request instanceof HttpServletRequest) {
       return ((HttpServletRequest) request).getRequestURI();
     } 	else {
       return "Not an HttpServletRequest";
     }
   }

   private FilterConfig filterConfig = null;
   
   public void init(FilterConfig filterConfig) 
      throws ServletException {
      this.filterConfig = filterConfig;
   }
   
   public void destroy() {
      this.filterConfig = null;
   }
}
