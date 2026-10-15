package com.example.phonestore.model;

import java.io.Serializable;

public class Brand implements Serializable {
    private String name;
    private String logoAssetPath;
    private boolean isSelected;

    public Brand(String name, String logoAssetPath) {
        this.name = name;
        this.logoAssetPath = logoAssetPath;
        this.isSelected = false;
    }

    public Brand(String name, String logoAssetPath, boolean isSelected) {
        this.name = name;
        this.logoAssetPath = logoAssetPath;
        this.isSelected = isSelected;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogoAssetPath() { return logoAssetPath; }
    public void setLogoAssetPath(String logoAssetPath) { this.logoAssetPath = logoAssetPath; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
