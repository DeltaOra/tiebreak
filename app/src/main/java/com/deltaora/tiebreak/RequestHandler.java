package com.deltaora.tiebreak;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.widget.Toast;


public class RequestHandler {
    public String getTargetRate(String strURL, MainActivity sf) {
        StringBuilder b = new StringBuilder();
        InputStream is;
        URL url;
        HttpURLConnection con;
        ThreadPolicy tp = ThreadPolicy.LAX;
        StrictMode.setThreadPolicy(tp);
        String site = "";
        try {
            url = new URL(strURL);
            con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(10000);
            con.setConnectTimeout(25000);
            con.setRequestMethod("GET");
            con.setDoInput(true);
            // this.page=url.getPath();
            site = url.getQuery();

            con.connect();
            is = con.getInputStream();

            int ch;
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            if (is != null) {
                is.close();
            }

            if (con != null) {
                con.disconnect();
            }
        } catch (Exception e) {
            Toast.makeText(sf, "error is " + e.toString() + " " + site, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        // this.page=b.toString();
        // category = b.toString().split("#");

        return b.toString();
    }
}
