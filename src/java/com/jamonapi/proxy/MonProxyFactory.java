package com.jamonapi.proxy;


import com.jamonapi.*;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;

/**
 * MonProxyFactory allows developers to monitor ANY interface by simply passing the Object implementing 
 * the interface to the monitor method.  (note the object passed MUST implement an interface or it will 
 * be a runtime error).  A great use of this is to monitor jdbc interfaces and to aid in this there are 
 * overloaded methods that take Connections, Statements, PreparedStatements, CallableStatements, and 
 * ResultSets.  These overloaded methods take advantage of knowledege of SQL and track additional statistics.   
 * The following capabilities can be acquired by using MonProxyFactory.  All can individually be enabled/disabled.
 * 
 *  0) Overall monitoring can be enabled/disabled by calling MonProxyFactory.enable(...).  This will enable/disable monitors.
 *  You can start out by enabling all monitors and disabling the ones you wish or vice versa.
 *  
 *  1) All methods of a given interface will have a jamon rows.  Any jamon row will have the label (in this 
 *  case the method signature), hits, avg/min/max execution time, active/avg active/max active, last value and 
 *  more. By default interface monitoring is on.  It can be enabled/disabled by calling MonProxyFactory.enableInterfaceM(...). 
 *  JDBC classes such as Connections, Statements, PreparedStatemetns, CallableStatements, and ResultsSets
 *  will also automatically be monitored if the class returning them was monitored.  If you don't wish this 
 *  to occur then you can use the monitor method that takes an Object.  
 *  
 *  2) ResultSet interfaces can be monitored however methods like getObject and next are called so much and 
 *  typically don't cause performance problems, so there is a seperate enable/disable capability for them.  Note for 
 *  ResultSet monitoring to occur interface monitoring must also be enabled.  
 *  However it can be enabled if interface monitoring is enabled and MonProxyFactory.enabledResultSets(true) is called.
 *  ResultSet's are by default not monitored.  
 *  
 *  3) SQLSummary will add a jamon row for all sql text issued against a PreparedStatement, CallableStatement,
 *  or Statement.  Argument values are replaced with ? for Statements to ensure the logical queries are matched.
 *  PreparedStatements need not be changed to do this, but Statements may look a little different
 *  For example:  select * from table where name='jeff beck' would become select * from table where name=?
 *  This is a powerful monitor as it allows you to see hits, avg/min/max query times for all queries in your 
 *  application.  This is enabled/disabled by calling MonProxyFactory.enableSQLSummary(...).
 *  
 *  4) SQLDetail puts the last N (configurable) queries that have been run into a rolling buffer.   The SQL buffer
 *  will have the actual query in the case of a Statement and the version with ? for PreparedStatements.  In addition
 *  other stats will be in this buffer such as how long the query took to execute, how many times has the PreparedStatement
 *  been reused, the jdbc method that executed the sql, and the exception stack trace if it occured.  This can be enabled/disabled
 *  by calling MonProxyFactory.enableSQLDetail(...)
 *  
 *  5) Exception Summary will add several jamon rows when an exception occurs.  Note the Exception buffer is used for any kind of Exception
 *  including SQLExceptions.  The exceptions added are 1. One containing the method that through the exception as well as the exception.
 *  2. One indicating how many exceptions total have been thrown through proxies, 3) One containing the exception type that was thrown.  
 *  This can be enabled/disabled by calling MonProxyFactory.enableExceptionSummary(...)
 *  
 *  6) ExceptionDetail puts the last N (configurable) exceptions that have occured to any interface that is being monitored into a buffer.
 *  The stack trace is in the row as well as when it was thrown and what method threw it.  This can be enabled/disabled by calling 
 *  MonProxyFactory.enableExceptionDetail(...).
 *  
  * Sample code:
 *   ResultSet rs= MonProxyFactory.monitor(resultSet);
 *   Connection conn=MonProxyFactory.monitor(connection);
 *   MyInterface my=(MyInterface) MonProxyFactory.monitor(myObject);//myObject implements MyInterface
 *   YourInterface your=(YourInterface) MonProxyFactory.monitor(yourObject);//myObject implements MyInterface
 *
 *  
 * @author steve souza
 *
 */


public class MonProxyFactory {
        private static final Class[] CLASS_ARRAY=new Class[0];
        private static Params params=new Params();
       
	    
	     /** By passing any interface to the monitor method, all public method calls and exceptions
	      *  will be monitored. It will be a runtime error if the Object passed does not implement an interface. Note that
          *  only interfaces implemented directly by the passed in Object are monitored.  Should you want to cast to an interface
          *  implemented by a base class to the passed in Object you should call one of the more explicit monitor(..) methods
	      *  
	      *    Sample call:
	      *     MyInterface myProxyObject=(MyInterface) MonProxyFactory.monitor(myObject);
	      */

	    public static Object monitor(Object object) {
            if (!isEnabled() || object==null) // if not enabled return the original object unchanged, not the proxy
              return object;
            else 
              return monitorNoCheck (object, getInterfaces(object.getClass()));// proxy will implement ALL interfaces of this class
        }
        

         /** By passing any interface to the monitor method, and an array of interfaces to implement then all public method calls and exceptions
          *  will be monitored. It will be a runtime error if the Object passed does not implement an interface.
          *  
          *    Sample call:
          *     MyInterface myProxyObject=(MyInterface) MonProxyFactory.monitor(myObject, ineterfaces);
          */
        public static Object monitor(Object object, Class[] interfaces) {
            if (!isEnabled() || object==null) // if not enabled return the original object unchanged, not the proxy
              return object;
            else 
              return monitorNoCheck(object, interfaces);
        }
        
        private static Object monitorNoCheck(Object object, Class[] interfaces) {
            return Proxy.newProxyInstance(
                    object.getClass().getClassLoader(),
                    interfaces,// proxy will implement ALL interfaces in array
                    new MonProxy(object, params)
             );
            
        }    
      

        /** By passing any interface to the monitor method, and an interface to implement then all public method calls and exceptions
         *  will be monitored. It will be a runtime error if the Object passed does not implement an interface.
         *  
         *    Sample call:
         *     MyInterface myProxyObject=(MyInterface) MonProxyFactory.monitor(myObject, com.mypackage.MyInterface.class);
         */
        
        public static Object monitor(Object object, Class iface) {
            return monitor (object, new Class[] {iface});
        }

	      /** Note if a connection object is monitored any Statements, PreparedStatements, CallableStatements, and
	       * optionally ResultSets that it creates will automatically be monitored.  So for jdbc interfaces usually it will be sufficient 
	       * to simply monitor the connection.   You could also call MonProxyFactory.monitor((Object)conn);
	       * and the connection would be monitored however other child objects wouldn't be and recently executed sql would not be put 
	       * in a buffer.  The same applies to the other overloaded sql monitor(...) method calls below.  For sql monitored objects
	       * to be monitored both the overall monitoring must be enabled.  Monitoring rules apply as discussed in the top of this document.

           * 
	       */
	      public static Connection monitor(Connection conn) {
	    	  return (Connection) monitorJDBC(conn);
	      }
	      
	      /** Monitor a resultSets methods.  Note the version that takes an explicit class is used for when the class is a proxy*/
	      public static ResultSet monitor(ResultSet rs) {
	    	  return (ResultSet) monitorJDBC(rs);
	      }
          
 
	      /** Monitor a Statements methods, as well as any ResultSets it returns (assuming the proper monitoring options are enabled) */
	      public static Statement monitor(Statement statement) {
	          return (Statement) monitorJDBC(statement);
	      }
	
	      /** Monitor a PreparedStatements methods, as well as any ResultSets it returns (assuming the proper monitoring options are enabled) */
	 	  public static PreparedStatement monitor(PreparedStatement statement) {
	    	  return (PreparedStatement) monitorJDBC(statement);
	      }

	 	  /** Monitor a CallableStatements methods, as well as any ResultSets it returns (assuming the proper monitoring options are enabled) */
	 	  public static CallableStatement monitor(CallableStatement statement) {
	    	  return (CallableStatement) monitorJDBC(statement);
	      }
 

	      static Object monitorJDBC(Object object) {

	    	  // if monitoring is not enabled, sql monitoring is not enabled, the object is a null or already an instance of a proxy then return
	    	  // the original object unchanged.  (it would have to be a proxy object with a JDBCMonProxy invocation handler
              if (!params.isEnabled || (!params.isSQLSummaryEnabled && !params.isSQLDetailEnabled) || object==null || (object instanceof Proxy && Proxy.getInvocationHandler(object) instanceof JDBCMonProxy)) // if not enabled return the original object unchanged, not the proxy
	            return object;
	          else 
	            return Proxy.newProxyInstance(
	              object.getClass().getClassLoader(),
	              getInterfaces(object.getClass()),// proxy will implement passed in interfaces
	              new JDBCMonProxy(object, params)
	            );
	        }
	 
            
            /** For every class in the Object/Interface heirarchy find its implemented interfaces.  All interfaces this class 
             * implements are returned.  Either the Class of an Object or interfaces Class may be passed to 
             *  this method.  The difference between this method and the method 'Class.getInterfaces()' is 
             *  that this one returns ALL implemented interfaces whereas that one only returns interfaces that 
             *  are directly implemented by the Class.  
             */
            public static Class[] getInterfaces(Class cls) {
                if (cls==null) 
                    return null;
                
                Set interfaceHeirarchy=new HashSet();
                // Get class heirarchy and loop through it and its interfaces adding each interface to the passed
                // in Set.
                Class[] objTree=getClassHeirarchy(cls);
                for (int i=0;i<objTree.length;i++) 
                   getInterfaces(objTree[i], interfaceHeirarchy);
                    
                return toClassArray(interfaceHeirarchy);
                
            }
            
            /** Convert a Collection to a Class[] array */
            private static Class[] toClassArray(Collection coll) {
                if (coll==null || coll.size()==0)
                   return null;
                else
                  return (Class[]) coll.toArray(CLASS_ARRAY);// convert the Set to Class[]
                
            }

            /*
             *  Returns the inheritance heirarchy of the specified Class that was passed in.  For example if there
             *  are three levels such as Base1, Base2, Base3 then it would return an array of these 3 Class elements.
             */

            private static Class[] getClassHeirarchy(Class cls) {
                if (cls==null) 
                  return null;
                
                // classes will contain the inheritance chain of Objects.
                List classes=new ArrayList();
                // Loop through super classes until null is found which indicates there are no more Objects
                // in the inheritance chain.
                while (cls!=null) {
                  classes.add(cls);
                  cls = cls.getSuperclass();
                } 
                    
                return toClassArray(classes);
            }

            /** A recursive method called for each Object or interface in the heirarchy.  All interfaces are added into the 
             * passed in Set.  When either a Class is null, or it implements no other interfaces the recursive method bubbles
             * up the chain.
             * 
             */
            private static void getInterfaces(Class cls, Set heirarchy) {
                if (cls != null) {
                  Class[] heir = cls.getInterfaces();// gets immediate implemented interfaces of passed in Class or interface
                  int len=(heir==null) ? 0 : heir.length;
                  for (int i = 0; i < len; i++) {
                    heirarchy.add(heir[i]);
                    getInterfaces(heir[i],heirarchy);// recursive
                  }
                 }
                 
             }         
	      
	      // Standard and Exception monitoring methods
	   
         /** 
	       * Get the number of Exceptions that can be stored in the buffer before the oldest entries must
	       * be removed.
	       * 
	       */
	      public static int getExceptionBufferSize() {
	        return params.exceptionBuffer.getBufferSize();
	      }
	      
	      /** 
	       * Set the number of Exceptions that can be stored in the buffer before the oldest entries must
	       * be removed.  A value of 0 will disable collecting of Exceptions in the buffer. 
	       * 
	       */
	      
	      public static void setExceptionBufferSize(int exceptionBufferSize) {
	    	  params.exceptionBuffer.setBufferSize(exceptionBufferSize);
	      }
	      
	      /** 
	       * Remove all Exceptions from the buffer.
	       *
	       */
	      public static void resetExceptionDetail() {
	    	  params.exceptionBuffer.reset();  
	      }
	      


	      /** Inidicates whether methods of the interface are monitored or not */
	      public static boolean isInterfaceEnabled() {
	    	  return params.isInterfaceEnabled;
	      }
	      /** Enables/disables whether methods of the interface are monitored or not */
	      public static void enableInterface(boolean enable) {
	    	  params.isInterfaceEnabled=enable;
              if (enable)
                enable(true);
	      }



	      /** Indicates whether jamon summary stats are kept for exceptions */
          public static boolean isExceptionSummaryEnabled() {
	    	  return params.isExceptionSummaryEnabled;
	      }
	      /** Enables/disables jamon summary stats for exceptions */
	      public static void enableExceptionSummary(boolean enable) {
	    	  params.isExceptionSummaryEnabled=enable;
              if (enable)
                  enable(true);
	      }

	      /** Indicates whether exceptions are tracked in a rolling buffer */
	      public static boolean isExceptionDetailEnabled() {
	    	  return params.isExceptionDetailEnabled;
	      }
	      /** Enables/Disables whether exceptions are tracked in a rolling buffer */
		  public static void enableExceptionDetail(boolean enable) {
			  params.isExceptionDetailEnabled=enable;
			  if (enable)
				  params.exceptionBuffer.enable(); 	  
			  else
				  params.exceptionBuffer.disable();
              
              if (enable)
                  enable(true);

		  }



		  /** Indicates whether jamon summary stats are kept for SQL */
		  public static boolean isSQLSummaryEnabled() {
		    	  return params.isSQLSummaryEnabled;
		      }
		  /** Enables/Disables jamon summary stats for SQL */
		  public static void enableSQLSummary(boolean enable) {
			  params.isSQLSummaryEnabled=enable;
              if (enable)
                  enable(true);
		  }


		  /** Indicates whether sql command details (time, sql, stack trace, ...) are kept in a rolling buffer */
	      public static boolean isSQLDetailEnabled() {
		   	  return params.isSQLDetailEnabled;
		  }
		  /** Enables/disables whether sql command details (time, sql, stack trace, ...) are kept in a rolling buffer */
		  public static void enableSQLDetail(boolean enable) {
			  params.isSQLDetailEnabled=enable;
			  if (enable)
				  params.sqlBuffer.enable();
			  else
				  params.sqlBuffer.disable();

              if (enable)
                  enable(true);
           }

		  

		  /** Indicates whether ResultSet methods are monitored.  Note interface monitoring must also be enabled for ResultSet monitoring to occur */ 
		  public static boolean isResultSetEnabled() {	 
		  	  return params.isResultSetEnabled;
		  }
		  /** Enables/disables whether ResultSet methods are monitored.  Note interface monitoring must also be enabled for ResultSet monitoring to occur */ 
		  public static void enableResultSet(boolean enable) {
			  params.isResultSetEnabled=enable;
              if (enable) {
                  enableInterface(true);
                  enable(true);
              }
		  }
	      
	      /** Returns true if MonProxyFactory is enabled.  */
	      public static boolean isEnabled() {
	    	return params.isEnabled;
	      }
	      
	      /** Enables all monitors.  This overrides all other monitor booleans.  It never enables ResultSet monitoring
	       * That must be done as a separates step as that is not the enabled monitoring by default.  All other monitors will be disabled/enabled when
	       * calling this method. 
	       * */
	      public static void enableAll(boolean enable) {
              enable(enable);
              enableInterface(enable);
              enableExceptionSummary(enable);
              enableExceptionDetail(enable);
              enableSQLSummary(enable);
              enableSQLDetail(enable);
              enableResultSet(enable);
	      }
          
          public static boolean isAllEnabled() {
              return (params.isEnabled && params.isExceptionDetailEnabled && params.isExceptionSummaryEnabled 
                      && params.isSQLSummaryEnabled && params.isSQLDetailEnabled && params.isInterfaceEnabled && params.isResultSetEnabled);
          }

          /** Enables all monitors except ResultSet monitoring.  This overrides all other monitor booleans.  It never enables ResultSet monitoring
           * That must be done as a separates step as that is not the enabled monitoring by default.  All other monitors will be disabled/enabled when
           * calling this method. 
           * */
          public static void enable(boolean enable) {
              params.isEnabled=enable;
          }
       
	      static Params getParams() {
	    	  return params;
	      }
	      
		      
	      /** Get the header that can be used to display the Exceptions buffer */
	      public static String[] getExceptionDetailHeader() {
	        return params.exceptionBuffer.getHeader();
	      }
	      
	      /** Get the exception buffer as an array, so it can be displayed */
	      public static Object[][] getExceptionDetail() {
	    	  return params.exceptionBuffer.getData();
	      }
	      
	   
		
		
		// JDBC Monitoroing methods
	      
		/**
		 * Get the number of SQL statements that can be stored in the buffer before the
		 * oldest entries must be removed.
		 * 
		 */
		public static int getSQLBufferSize() {
			return params.sqlBuffer.getBufferSize();
		}
		
		/** 
		 * Set the number of SQL Statements that can be stored in the buffer before the oldest entries must
		 * be removed.  A value of 0 will disable the collection of Exceptions in the buffer.  Note if 
		 * monitoring is disabled exceptions will also not be put in the buffer.
		 * 
		 */
		
		public static void setSQLBufferSize(int sqlBufferSize) {
			params.sqlBuffer.setBufferSize(sqlBufferSize);
		}
		
		/** 
		 * Remove all SQL from the buffer.
		 *
		 */
		public static void resetSQLDetail() {
			params.sqlBuffer.reset();  
		}
		
		
	
		/** Get the header that can be used to display the SQL buffer */
		public static String[] getSQLDetailHeader() {
		  return params.sqlBuffer.getHeader();
		}
		
		/** Get the sql buffer as an array, so it can be displayed */
		public static Object[][] getSQLDetail() {
			  return params.sqlBuffer.getData();
		}
		
		
		/** Get a list of the strings to match in the parsed query.  This can be used to count tables hits and times of queries that hit them for example
		 * 
		 * 
		 */
		
		public static List getMatchStrings() {
			return params.matchStrings;
		}
		
		/** Set the strings to match */
		public static void setMatchStrings(List ms) {
			params.matchStrings=ms;
		}
		
	
		
  	 
}
