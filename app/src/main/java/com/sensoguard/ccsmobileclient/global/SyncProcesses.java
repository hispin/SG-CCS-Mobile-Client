package com.sensoguard.ccsmobileclient.global;


import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

public class SyncProcesses<T> {
    private static SyncProcesses ourInstance = new SyncProcesses();

    private SyncProcesses() {
    }

    public static SyncProcesses getInstance() {
        return ourInstance;
    }


    public void syncAddItemToList(ArrayList<T> items, T item) throws ConcurrentModificationException {
        if (items != null) {
            List<T> tmpList = Collections.synchronizedList(items);
            synchronized (tmpList) {
                tmpList.add(item);
            }
        }
    }


    public void syncRemoveItems(ArrayList<T> items) {
        if (items != null) {
            List<T> tmpList = Collections.synchronizedList(items);
            synchronized (tmpList) {
                tmpList.clear();
            }
        }
    }

    void syncRemoveAreaFromList(ArrayList<T> items, T index) throws ConcurrentModificationException {
        if (items != null) {
            List<T> tmpList = Collections.synchronizedList(items);
            synchronized (tmpList) {
                tmpList.remove(index);
            }
        }
    }
}
