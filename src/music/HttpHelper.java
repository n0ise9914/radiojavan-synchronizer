package music;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * n0ise on 2/2/2017.
 */

class HttpHelper {

    private static HttpHelper instance;

    private HttpHelper() {
    }

    static HttpHelper getInstance() {
        if (instance == null)
            instance = new HttpHelper();
        return instance;
    }

    String fetch(String url) {
        try {
            URL obj = new URL(url);
            //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyInfo.host, Integer.parseInt(proxyInfo.port)));
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
