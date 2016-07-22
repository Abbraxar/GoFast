package com.i3cnam.gofast.communication;

import com.google.android.gms.maps.model.LatLng;
import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.PassengerTravel;

import java.util.List;


public interface CommInterface {

    /**
     * retrieves all the potential carpools for this travel
     * @param travel
     * @return
     */
    List<Carpooling> findCarpoolingPossibilities(PassengerTravel travel);

    /**
     * Declares a new course into the system
     * @param driverCourse
     * @return the id of the course
     */
    int declareCourse(DriverCourse driverCourse);

    /**
     * Sends a carpooling request th the server
     * @param carpooling the carpool requested
     */
    void requestCarpool(Carpooling carpooling);

    /**
     * Accept a requested carpool
     * @param carpooling the carpool accepted
     */
    void acceptCarpool(Carpooling carpooling);

    /**
     * Updates the new position of the car
     * @param driverCourse the travel to be updated
     */
    void updatePosition(DriverCourse driverCourse);

    /**
     * Updates the path of the course
     * @param driverCourse
     */
    void updateCourse(DriverCourse driverCourse);

    /**
     * Request to keep informed about the carpooling of the course
     * @param driverCourse the course to be observed
     */
    void observeCourse(DriverCourse driverCourse);

    /**
     * Stop keeping informed about the carpooling of the course
     * @param driverCourse the course to be unobserved
     */
    void unobserveCourse(DriverCourse driverCourse);

    /**
     * Request to keep informed about the carpooling of the travel
     * @param passengerTravel the travel to be observed
     */
    void observeTravel(PassengerTravel passengerTravel);

    /**
     * Stop keeping informed about the carpooling of the travel
     * @param passengerTravel the travel to be unobserved
     */
    void unobserveTravel(PassengerTravel passengerTravel);

}