package com.thirteen_lab.wifi_searcher.utls.heat_map;

public class WifiNetwork {
    private String bssid;
    private String ssid;

    public String getBssid() {
        return bssid;
    }

    public String getSsid() {
        return ssid;
    }

    WifiNetwork(String bssid, String ssid) {
        this.bssid = bssid;
        this.ssid = ssid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null)
            return false;

        if (getClass() != o.getClass())
            return false;

        WifiNetwork other = (WifiNetwork) o;

        return getBssid().equals(other.getBssid()) &&
                getSsid().equals(other.getSsid());
    }

    @Override
    public int hashCode() {
        int result = bssid.hashCode();
        result = 31 * result + ssid.hashCode();
        return result;
    }

    public String toString() {
        return getSsid() + " (" + getBssid() + ")";
    }
}
