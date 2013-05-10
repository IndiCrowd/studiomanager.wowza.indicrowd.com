package com.indicrowd.wowza.studiomanager;

import java.io.*;

import com.indicrowd.wowza.studiomanager.stream.StreamInfo;
import com.wowza.wms.application.IApplication;
import com.wowza.wms.application.IApplicationInstance;
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
			
			retStr += concertId;
			
			IApplication appl = vhost.getApplication("live");
			
			IApplicationInstance appinst = appl.getAppInstance("_definst_");
			
			IMediaStream ms = appinst.getStreams().getStream(concertId);
			
			StreamInfo streamInfo = new StreamInfo(concertId);
			
			if (ms == null) {
				streamInfo.setState(StreamInfo.StreamInfoState.FAILED);
			} else {
				streamInfo.setState(StreamInfo.StreamInfoState.PLAY);
				streamInfo.setNowCount(appinst.getPlayStreamCount(concertId));
			}
			
			out.write(streamInfo.serializeJSON().getBytes());
			
		} catch (Exception e) { 
			WMSLoggerFactory.getLogger(null).error(
					"HTTPProvider: " + e.toString());
		}

	}

}
