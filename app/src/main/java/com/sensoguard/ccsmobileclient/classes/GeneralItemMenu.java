package com.sensoguard.ccsmobileclient.classes;


public class GeneralItemMenu {

    public static String selectedItem = "";
    private String key;
    private String title;
    private String value;
    private int iconLarge;
    private int iconSmall;

    public GeneralItemMenu(String key, int iconSmall, int iconLarge) {
        this.key = key;
        this.iconLarge = iconLarge;
        this.iconSmall = iconSmall;
    }

    public GeneralItemMenu(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getIconLarge() {
        return iconLarge;
    }

    public void setIconLarge(int icon) {
        this.iconLarge = icon;
    }

    public int getIconSmall() {
        return iconSmall;
    }

    public void setIconSmall(int iconSmall) {
        this.iconSmall = iconSmall;
    }
}
