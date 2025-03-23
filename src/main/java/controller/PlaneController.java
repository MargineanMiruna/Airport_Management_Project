package controller;

import service.PlaneService;

public class PlaneController {
    private final PlaneService planeService;

    public PlaneController(PlaneService planeService) {
        this.planeService = planeService;
    }

    public void createPlane(String planeData) {
        planeService.createPlane(planeData);
    }

    public String getPlane(String id) {
        return planeService.getPlane(id);
    }

    public String getAllPlanes() {
        return planeService.getAllPlanes();
    }

    public void updatePlane(String planeData) {
        planeService.updatePlane(planeData);
    }

    public void deletePlane(String id) {
        planeService.deletePlane(id);
    }
}
