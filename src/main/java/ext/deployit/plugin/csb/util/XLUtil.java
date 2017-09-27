package ext.deployit.plugin.csb.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class XLUtil {

	public static String getCurrentTimeStampInGMT() {
		final Date currentTime = new Date();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(currentTime);
	}
}
