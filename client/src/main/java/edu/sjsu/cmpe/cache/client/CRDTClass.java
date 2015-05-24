package edu.sjsu.cmpe.cache.client;

/**
 * Created by Vaidehi on 5/23/15.
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CRDTClass {

    public ConcurrentHashMap<String, String> writeMap = new ConcurrentHashMap<String, String>();
    public ConcurrentHashMap<String, String> readMap = new ConcurrentHashMap<String, String>();
    private ArrayList<DistributedCacheService> serverList = new ArrayList<DistributedCacheService>();

    public void add(String serverURL) {
        serverList.add(new DistributedCacheService(serverURL, this));
    }


    public void writeFunction(long key, String value) {

        int errors = 0;

        for (DistributedCacheService ser : serverList) {
            ser.put(key, value);
        }
        do {
            if (writeMap.size() >= serverList.size()) {
                for (DistributedCacheService server : serverList) {
                    System.out.println("Writing to server URL " + server.getCacheServerURL() + " - " + writeMap.get(server.getCacheServerURL()));
                    if (writeMap.get(server.getCacheServerURL()).equalsIgnoreCase("Error."))
                        errors++;
                }
                if (errors > 1) {
                    System.out.println("Doing Rollback...");
                    for (DistributedCacheService server : serverList) {
                        server.delete(key);
                    }
                }
                writeMap.clear();
                break;
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (true);
    }

    public String readFunction(long key) throws InterruptedException {

        for (DistributedCacheService server : serverList) {
            server.get(key);
        }
        Set<DistributedCacheService> failedSet = new HashSet<DistributedCacheService>();
        Set<DistributedCacheService> serverSet = new HashSet<DistributedCacheService>(serverList);
        serverSet.addAll(serverList);
        while (true) {
            if (readMap.size() < 3) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println(readMap);
                for (DistributedCacheService server : serverList) {

                    if (readMap.get(server.getCacheServerURL()).equalsIgnoreCase("Error")) {
                        System.out.println("Error : " + server.getCacheServerURL());
                        failedSet.add(server);
                    }
                }
                serverSet.removeAll(failedSet);
                System.out.println("Consistent: " + serverSet);
                Thread.sleep(500);
                String valueToAdd = null;
                if (failedSet.size() > 0) {
                    System.out.println("Failed set: " + failedSet);
                    ArrayList<String> values = new ArrayList<String>();
                    ArrayList<DistributedCacheService> allServers = new ArrayList<DistributedCacheService>();
                    for (DistributedCacheService consistent : serverSet) {
                        String temp = consistent.getSynchronous(key);
                        values.add(temp);
                        allServers.add(values.indexOf(temp), consistent);
                    }
                    Set<String> unique = new HashSet<String>(values);
                    int max = Integer.MIN_VALUE;
                    DistributedCacheService maxServer = null;
                    //Find max weight
                    for (String val : unique) {
                        int temp = Collections.frequency(values, val);
                        if (temp > max) {
                            max = temp;
                            valueToAdd = val;
                        }
                    }
                    System.out.println("Consistent servers.");
                    for (DistributedCacheService ser : failedSet) {
                        System.out.println("Vaule stored is: " + ser.getCacheServerURL() + " : " + valueToAdd);
                        ser.putSynchronous(key, valueToAdd);
                    }
                    failedSet.clear();
                    readMap.clear();
                    return valueToAdd;
                }
                failedSet.clear();
            }
        }
    }

}