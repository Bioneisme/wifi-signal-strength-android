package com.thirteen_lab.wifi_searcher.utls.access_points;

import android.net.wifi.ScanResult;

import com.thirteen_lab.wifi_searcher.R;

import java.util.HashSet;
import java.util.List;

public class Filter {
    private final HashSet<Integer> filters;
    private final List<ScanResult> scanResults;


    public Filter(HashSet<Integer> filters, List<ScanResult> scanResults) {
        this.filters = filters;
        this.scanResults = scanResults;
    }


    public void filterResults() {
        if (!filters.isEmpty()) {
            filterByStrength();
            filterByMHz();
            filterByCapabilities();
        }
    }


    public void filterByStrength() {

        if (!(filters.contains(R.id.filterStrength0)
                || filters.contains(R.id.filterStrength1)
                || filters.contains(R.id.filterStrength2)
                || filters.contains(R.id.filterStrength3)
                || filters.contains(R.id.filterStrength4))) {

            return;
        }


        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult results = scanResults.get(i);
            if (!(filters.contains(R.id.filterStrength0) && results.level <= -120 && results.level >= -200)
                    && !(filters.contains(R.id.filterStrength1) && results.level <= -80 && results.level > -120)
                    && !(filters.contains(R.id.filterStrength2) && results.level <= -50 && results.level > -80)
                    && !(filters.contains(R.id.filterStrength3) && results.level <= -40 && results.level > -50)
                    && !(filters.contains(R.id.filterStrength4) && results.level >= -40)) {
                scanResults.remove(results);
                i--;
            }
        }
    }

    public void filterByMHz() {
        if (!(filters.contains(R.id.filterWifiBand6)
                || filters.contains(R.id.filterWifiBand2)
                || filters.contains(R.id.filterWifiBand5))) {
            return;
        }
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult results = scanResults.get(i);
            int MHz = results.frequency / 1000;
            if (!(filters.contains(R.id.filterWifiBand2) && MHz == 2)
                    && !(filters.contains(R.id.filterWifiBand5) && MHz == 5)
                    && !(filters.contains(R.id.filterWifiBand6) && MHz == 6)) {
                scanResults.remove(results);
                i--;
            }
        }
    }

    public void filterByCapabilities() {
        if (!(filters.contains(R.id.filterSecurityNone)
                || filters.contains(R.id.filterSecurityWEP)
                || filters.contains(R.id.filterSecurityWPA)
                || filters.contains(R.id.filterSecurityWPA2)
                || filters.contains(R.id.filterSecurityWPA3)
                || filters.contains(R.id.filterSecurityWPS))) {
            return;
        }

        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult results = scanResults.get(i);
            String security = results.capabilities;
            if (!(filters.contains(R.id.filterSecurityNone) && security == null)
                    && !(filters.contains(R.id.filterSecurityWEP) && security.contains("WEP"))
                    && !(filters.contains(R.id.filterSecurityWPA) && security.contains("WPA"))
                    && !(filters.contains(R.id.filterSecurityWPA2) && security.contains("WPA2"))
                    && !(filters.contains(R.id.filterSecurityWPA3) && security.contains("WPA3"))
                    && !(filters.contains(R.id.filterSecurityWPS) && security.contains("WPS"))) {
                scanResults.remove(results);
                i--;
            }
        }
    }

    public List<ScanResult> getResults() {
        return scanResults;
    }
}