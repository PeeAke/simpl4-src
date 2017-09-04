/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ms123.common.utils;

import java.util.Iterator;
import java.io.BufferedReader;
import java.util.regex.*;
import groovy.lang.*;
import java.io.StringReader;

public class Utils {

	public static String __formatGroovyException(Throwable e) {
		String msg = e.getMessage();
		System.out.println("--->");
		System.out.println(msg);
		System.out.println("<---");
		int index = msg.indexOf("org.codehaus.groovy.syntax.SyntaxException");
		if (index > 0) {
			msg = msg.substring(0, index - 1);
		} else {
			StackTrace st = new StackTrace(e);
			Iterator it = st.iterator();
			String lineNumber = null;
			while (it.hasNext()) {
				StackTrace.Entry en = (StackTrace.Entry) it.next();
				System.out.println("Entry:" + en);
				String sf = en.getSourceFileName();
				if (sf != null && sf.startsWith("Script") && sf.endsWith(".groovy")) {
					lineNumber = en.getLineNumber();
					break;
				}
			}
			if (lineNumber != null) {
				msg = "Line:" + lineNumber + ": " + msg;
			}
		}
		return msg;
	}

	public static String formatGroovyException(Throwable e, String script) {
		if( e instanceof org.apache.camel.CamelExecutionException){
			e = e.getCause() != null ? e.getCause() : e;
		}

		Object lnMsg[] = getLineNumberFromMsg(e.getMessage());
		String lineNumber = null;
		String msg=null;
		String secondMsg = "";
		String delim="";
		String lnBreak="";
		if( lnMsg == null){
			msg = "<b>"+e.getMessage()+"</b>";
			Throwable t = e.getCause();
			while(t != null){
				secondMsg += delim+t.getMessage();
				t = t.getCause();
				delim = "\n-------------------------------\n";
			}

			if( msg == null){
				 StackTrace st = new StackTrace(e);
				 Iterator it = st.iterator();
				 msg = e.toString() +"/"+ it.next();
			}
			int index = msg.indexOf("org.codehaus.groovy.syntax.SyntaxException");
			if (index != -1) {
				msg = msg.substring(0, index - 1);
				Object[] res = getLineNumberFromMsg(msg);
				if (res != null) {
					lineNumber = (String) res[0];
					msg = (String) res[1];
				}
			} else {
				StackTrace st = new StackTrace(e);
				Iterator it = st.iterator();
				while (it.hasNext()) {
					StackTrace.Entry en = (StackTrace.Entry) it.next();
					System.out.println("Entry:" + en);
					String sf = en.getSourceFileName();
					if (sf != null && sf.startsWith("Script") && sf.endsWith(".groovy")) {
						lineNumber = en.getLineNumber();
						try{
							Integer.parseInt(lineNumber);
							break;
						}catch(Exception x){
						}
					}
				}
			}
		}else{
			lineNumber=(String)lnMsg[0];
			msg = "<b>"+lnMsg[1]+"</b>";
			lnBreak="<br/>";
		}
		String ret = insertLineNumbers(script, lineNumber);
		if (lineNumber != null) {
			msg = lnBreak+"Line:" + lineNumber + ": "+ msg;
		}
		delim = "\n-------------------------------\n";
		return msg + 
				delim + 
				ret +
				("".equals(secondMsg) ? "" : (delim + secondMsg));
	}

	private static Object[] getLineNumberFromMsg(String msg) {
		Pattern p = Pattern.compile(".*Script\\d{1,5}.groovy: (\\d{1,5}):(.*)", Pattern.DOTALL);
		Matcher m = p.matcher(msg);
		Object[] ret = new Object[2];
		if (m.find()) {
			ret[0] = m.group(1);
			ret[1] = m.group(2);
			return ret;
		}
		return null;
	}

	private static String insertLineNumbers(String lines, String lineNumber) {
		if( lines == null ) return "";
		int lnr = -1;
		int end = 100000;
		int start = 0;
		if (lineNumber != null) {
			try{
				lnr = Integer.parseInt(lineNumber);
				start = lnr - 7;
				end = lnr + 7;
			}catch(Exception e){
			}
		}
		if( start < 0) start = 0;
		BufferedReader br = new BufferedReader(new StringReader(lines));
		StringBuilder sb = new StringBuilder();
		try {
			int count = 1;
			String line = br.readLine();
			while (line != null) {
				if( count<start || count > end ){
					line = br.readLine();
					count++;
					continue;
				}
				if (lnr == count) {
					sb.append("->");
				} else {
					sb.append("  ");
				}
				sb.append(count++);
				sb.append(" ");
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {
			}
		}
		return sb.toString();
	}
}
