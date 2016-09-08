package com.i3cnam.gofast.communication;

import com.i3cnam.gofast.model.Carpooling;
import com.i3cnam.gofast.model.DriverCourse;
import com.i3cnam.gofast.model.PassengerTravel;
import com.i3cnam.gofast.model.User;

import java.net.ConnectException;
import java.util.List;


public interface CommInterface {


    /**
     * Declares a new course into the system
     * @param driverCourse
     * @return the id of the course
     */
    int declareCourse(DriverCourse driverCourse) throws GofastCommunicationException;

    /**
     * Declares a new travel into the system
     * @param passengerTravel
     * @return the id of the travel
     */
    int declareTravel(PassengerTravel passengerTravel);

    /**
     * retrieves all the potential carpools for this travel
     * @param travel
     * @return
     */
    List<Carpooling> findCarpoolingPossibilities(PassengerTravel travel);

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
     * Refuse a requested carpool
     * @param carpooling the carpool refused
     */
    void refuseCarpool(Carpooling carpooling);

    /**
     * Cancel a requested carpool
     * @param carpooling the carpool canceled
     */
    void cancelRequest(Carpooling carpooling);

    /**
     * Abort a requested carpool
     * @param carpooling the carpool aborted
     */
    void abortCarpool(Carpooling carpooling);

    /**
     * Validate a requested carpooling
     * @param carpooling the carpooling aborted
     */
    void validateCarpool(Carpooling carpooling, String role);

    /**
     * Abort a requested course
     * @param course the course aborted
     */
    void abortCourse(DriverCourse course) throws GofastCommunicationException;

    /**
     * Abort a requested travel
     * @param travel the travel aborted
     */
    void abortTravel(PassengerTravel travel);

    /**
     * Updates the new position of the car
     * @param driverCourse the travel to be updated
     */
    void updatePosition(DriverCourse driverCourse) throws GofastCommunicationException;

    /**
     * Updates the path of the course
     * @param driverCourse
     */
    void updateCourse(DriverCourse driverCourse) throws GofastCommunicationException;

    /**
     * Request to keep informed about the carpooling of the course
     * @param driverCourse the course to be observed
     */
    void observeCarpoolCourse(DriverCourse driverCourse);

    /**
     * Stop keeping informed about the carpooling of the course
     * @param driverCourse the course to be unobserved
     */
    void unobserveCarpoolCourse(DriverCourse driverCourse);

    /**
     * Request to keep informed about the carpooling of the travel
     * @param passengerTravel the travel to be observed
     */
    void observeCarpoolTravel(PassengerTravel passengerTravel);

    /**
     * Stop keeping informed about the carpooling of the travel
     * @param passengerTravel the travel to be unobserved
     */
    void unobserveCarpoolTravel(PassengerTravel passengerTravel);

    /**
     * Returns the carpooling of the travel
     * @param passengerTravel the travel to be observed
     */
    List<Carpooling> getCarpoolTravelState(PassengerTravel passengerTravel);


    /**
     * Returns the carpooling of the course
     * @param driverCourse the travel to be observed
     */
    List<Carpooling> getCarpoolCourseState(DriverCourse driverCourse) throws GofastCommunicationException;

    /**
     * Returns current course for user.
     * @param driver the user to get course from
     */
    DriverCourse getDriverCourse(User driver) throws GofastCommunicationException;

    /**
     * Returns current travel for user.
     * @param passenger the user to get course from
     */
    PassengerTravel getPassengerTravel(User passenger);


    /**
     * Declares a new user into the system
     * @param user
     * @return "ok" if done "taken" if nickname is already taken "existing" if user already exists
     */
    String declareUser(User user) throws GofastCommunicationException;


    /**
     * Research the phone number into the system
     * @param phoneNumber
     * @return nullstring if not found, nickname of user if found
     */
    String retrieveAccount(String phoneNumber) throws GofastCommunicationException;
}