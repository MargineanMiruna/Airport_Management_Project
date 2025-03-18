package service;

import domain.Plane;
import repository.PlaneRepository;

import java.util.List;

public class PlaneService {
    private final PlaneRepository planeRepository;

    public PlaneService(PlaneRepository planeRepository) {
        this.planeRepository = planeRepository;
    }

    public void createPlane(Plane plane) {
        planeRepository.save(plane);
    }

    public Plane getPlane(int id) {
        return planeRepository.findById(id);
    }

    public List<Plane> getAllPlanes() {
        return planeRepository.findAll();
    }

    public void updatePlane(Plane plane) {
        planeRepository.update(plane);
    }

    public void deletePlane(int id) {
        planeRepository.delete(id);
    }
}
