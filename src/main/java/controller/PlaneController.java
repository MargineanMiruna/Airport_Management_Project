package controller;

import domain.Plane;
import service.PlaneService;

import java.util.List;

public class PlaneController {
    private final PlaneService planeService;

    public PlaneController(PlaneService planeService) {
        this.planeService = planeService;
    }

    public void createPlane(String code, String airlineId, String numOfSeats, String firstClass, String businessClass, String premiumEconomy) {
        planeService.createPlane(code, airlineId, numOfSeats, firstClass, businessClass, premiumEconomy);
    }

    public void getAllPlanes() {
        List<Plane> planes = planeService.getAllPlanes();
        for (Plane plane : planes) {
            System.out.println(plane.getPlaneId() + ") " + plane.getPlaneCode());
        }
    }

    public void updatePlane(String id, String planeCode) {
        planeService.updatePlane(id, planeCode);
    }

    public void deletePlane(String id) {
        planeService.deletePlane(id);
    }
}
