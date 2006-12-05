package no.knubo.mud;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

public class PersistantStore {

	private final String sessionId;

	PersistantStore(String sessionId) {
		this.sessionId = sessionId;

	}

	public String sendAliases(Map aliases, boolean merge) throws IOException {
		// URL of CGI-Bin script.
		URL url = new URL(
				"http://community.vikingmud.org/profiles/alias_write.php");
		// URL connection channel.
		URLConnection urlConn = url.openConnection();
		// Let the run-time system (RTS) know that we want input.
		urlConn.setDoInput(true);
		// Let the RTS know that we want to do output.
		urlConn.setDoOutput(true);
		// No caching, we want the real thing.
		urlConn.setUseCaches(false);
		// Specify the content type.
		urlConn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		// Send POST output.
		DataOutputStream printout = new DataOutputStream(urlConn
				.getOutputStream());
		printout.writeBytes("sessionID=" + sessionId);

		if (merge) {
			printout.writeBytes("&aliasSYNC=1");
		}
		
		for (Iterator i = aliases.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();

			printout.writeBytes("&"
					+ URLEncoder.encode((String) entry.getKey(), "UTF-8") + "="
					+ URLEncoder.encode((String) entry.getValue(), "UTF-8"));

		}

		printout.flush();
		printout.close();
		// Get response data.
		BufferedReader input = new BufferedReader(new InputStreamReader(urlConn
				.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String str = null;
		while (null != ((str = input.readLine()))) {
			sb.append(str + "\n");
		}
		input.close();
		return sb.toString();
	}

	public String getInventoryData() throws IOException {
		// URL of CGI-Bin script.
		URL url = new URL(
				"http://community.vikingmud.org/profiles/alias_read.php");
		// URL connection channel.
		URLConnection urlConn = url.openConnection();
		// Let the run-time system (RTS) know that we want input.
		urlConn.setDoInput(true);
		// Let the RTS know that we want to do output.
		urlConn.setDoOutput(true);
		// No caching, we want the real thing.
		urlConn.setUseCaches(false);
		// Specify the content type.
		urlConn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		// Send POST output.
		DataOutputStream printout = new DataOutputStream(urlConn
				.getOutputStream());
		String content = "sessionID=" + URLEncoder.encode(sessionId, "UTF-8");
		printout.writeBytes(content);
		printout.flush();
		printout.close();
		// Get response data.
		BufferedReader input = new BufferedReader(new InputStreamReader(urlConn
				.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String str = null;
		while (null != ((str = input.readLine()))) {
			sb.append(str + "\n");
		}
		input.close();

		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		PersistantStore store = new PersistantStore("knubo");

		store.getInventoryData();
	}
}
