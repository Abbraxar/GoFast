package com.i3cnam.gofast.model;

import java.util.List;

/**
 * Created by Nestor on 18/07/2016.
 */
public class DriverCourse {
    private User user;
    private PlaceClass origin;
    private PlaceClass destination;
    private List<Step> steps;
    private String encodedPoints;
}
