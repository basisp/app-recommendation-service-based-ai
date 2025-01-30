package com.example.springjwt.dto;

import java.util.List;

public class AppListDTO {
    private List<AppDTO> apps;

    // Getter와 Setter
    public List<AppDTO> getApps() {
        return apps;
    }

    public void setApps(List<AppDTO> apps) {
        this.apps = apps;
    }
}
