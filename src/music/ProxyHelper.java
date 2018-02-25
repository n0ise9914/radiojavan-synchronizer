package music;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ProxyHelper {
    private static Gson gson = new Gson();

    public static void save(ProxyInfo proxyInfo) {
        File file = new File(System.getProperty("user.dir") + "/proxy.json");
        if (file.delete())
            System.out.println("proxy.json deleted successfully.");
        try {
            Files.write(Paths.get(System.getProperty("user.dir") + "/proxy.json"), gson.toJson(proxyInfo).getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void enable() {
        try {
            if (new File(System.getProperty("user.dir") + "/proxy.json").exists()) {
                String data = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/proxy.json")));
                ProxyInfo proxyInfo = gson.fromJson(data, ProxyInfo.class);
                if (proxyInfo.host != null & proxyInfo.port != null & proxyInfo.user != null & proxyInfo.pass != null) {
                    if (!proxyInfo.host.equals("") & !proxyInfo.port.equals("") & !proxyInfo.user.equals("") & !proxyInfo.pass.equals("")) {
                        System.getProperties().put("jdk.http.auth.tunneling.disabledSchemes", "");
                        System.getProperties().put("jdk.http.auth.proxying.disabledSchemes", "");
                        System.getProperties().put("https.proxyHost", proxyInfo.host);
                        System.getProperties().put("https.proxyPort", proxyInfo.port);
                        String user = System.getProperty("https.proxyUser", proxyInfo.user);
                        String password = System.getProperty("https.proxyPassword", proxyInfo.pass);
                        Authenticator.setDefault(
                                new Authenticator() {
                                    public PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(user, password.toCharArray());
                                    }
                                }
                        );
                        System.out.println("updated system proxy.");
                    }
                }

            }
        } catch (Exception ignored) {
        }
    }

    public static void disable() {
        System.clearProperty("jdk.http.auth.tunneling.disabledSchemes");
        System.clearProperty("jdk.http.auth.proxying.disabledSchemes");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("https.proxyUser");
        System.clearProperty("https.proxyPassword");
    }


}
