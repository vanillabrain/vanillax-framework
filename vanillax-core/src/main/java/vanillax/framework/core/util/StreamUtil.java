/*
 * Copyright (C) 2016 Vanilla Brain, Team - All Rights Reserved
 *
 * This file is part of 'VanillaTopic'
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Vanilla Brain Team and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Vanilla Brain Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Vanilla Brain Team.
 */

package vanillax.framework.core.util;

import java.io.*;

public class StreamUtil {

	public static String file2str(File file)throws Exception {
		FileInputStream fin = null;
		String ret = null;
		try{
			fin = new FileInputStream(file);
			ret = stream2str(fin);
		}finally{
			try{}catch(Exception ignore){}
		}
		return ret;
	}

	public static String file2str(String file)throws Exception {
		return file2str(new File(file));
	}
	
	public static String stream2str(InputStream in)throws Exception {
		if (in == null)
			return null;
		ByteArrayOutputStream bout = null;
		try{
			byte[] b = new byte[1024];
			int n = 0;
			bout = new ByteArrayOutputStream();
			while( (n= in.read(b)) >-1 ){
				if(n==0) continue;
				bout.write(b,0,n);
			}
			
		}catch(Exception e){
			throw e;
		}finally{
			try{bout.close();}catch(Exception ig){}
			try{in.close();}catch(Exception ig){}
		}
		String s = new String(bout.toByteArray(),"UTF-8");
		return s;
	}
	
	public static InputStream str2stream(String str)throws Exception {
		if (str == null)
			return null;
		ByteArrayInputStream bin = null;
		bin = new ByteArrayInputStream(str.getBytes());
			
		return bin;
	}

}
