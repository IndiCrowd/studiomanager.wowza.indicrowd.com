package com.indicrowd.wowza.studiomanager;

import java.io.*;

import com.wowza.wms.application.IApplication;
import com.wowza.wms.http.*;
import com.wowza.wms.logging.*;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.MediaStreamMap;
import com.wowza.wms.vhost.*;

public class HTTPProvider extends HTTProvider2Base {

	public void onHTTPRequest(IVHost vhost, IHTTPRequest req, IHTTPResponse resp) {
		//if (!doHTTPAuthentication(vhost, req, resp))
		//	return;

		String retStr = "OK + ";

		try {
			OutputStream out = resp.getOutputStream();
			
			String concertId = req.getParameter("concertId");
			String eventType = req.getParameter("eventType");
			
			retStr += concertId + " ";
			retStr += eventType;
			
			IApplication appl = vhost.getApplication("live");
			for (String data : appl.getAppInstanceNames()) {
				
				MediaStreamMap map = appl.getAppInstance(data).getStreams();
				
				map.clearStreamName("testing");
			}
			
			byte[] outBytes = retStr.getBytes();
			out.write(outBytes);
			
			
			
		} catch (Exception e) { 
			WMSLoggerFactory.getLogger(null).error(
					"HTTPProvider: " + e.toString());
		}

	}

}
