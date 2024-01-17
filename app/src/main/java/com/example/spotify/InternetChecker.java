package com.example.spotify;

import java.net.InetAddress;

public class InternetChecker {
    public static Boolean isConnectedToInternet()
    {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }
    }
}
