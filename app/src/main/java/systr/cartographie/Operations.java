package systr.cartographie;


import com.google.android.gms.maps.model.LatLng;
/**
 * operations avec des coordonnees GPS
 * SySTR project
 * @author Nestor
 *
 */
public class Operations {

    static double rayonTerrestre = 6367445; // metres

    /**
     * Retourne la distance entre 2 points en m�tres
     * @param p1
     * @param p2
     * @return distance en metres
     */
    public static double dist2PointsEnM(LatLng p1, LatLng p2) {
        return dist2PointsEnRadians(p1, p2) * rayonTerrestre;
    }

    /**
     * Retourne la distance entre 2 points en radians (rayon de la terre)
     * @param p1
     * @param p2
     * @return distance en radians
     */
    public static double dist2PointsEnRadians(LatLng p1, LatLng p2) {
        double sinLatA = Math.sin(toRadians(p1.latitude));
        double sinLatB = Math.sin(toRadians(p2.latitude));
        double cosLatA = Math.cos(toRadians(p1.latitude));
        double cosLatB = Math.cos(toRadians(p2.latitude));
        double cosLongAmoinsLongB = Math.cos(toRadians(p2.longitude) - toRadians(p1.longitude));

        return  Math.acos(sinLatA * sinLatB + cosLatA * cosLatB * cosLongAmoinsLongB);
    }

    /**
     * Retourne le cap (en Radians) pour aller d'un point � un autre
     * @param source
     * @param dest
     * @return cap en radians
     */
    public static double cap(LatLng source, LatLng dest) {
//		sin(lon 2- lon 1) * cos(lat 2);
        double y = Math.sin(toRadians(dest.longitude - source.longitude)) * Math.cos(toRadians(dest.latitude));
//		cos(lat 1) * sin(lat 2) - sin(lat 1) * cos(lat 2) * cos(lon 2-lon 1);
        double x = (Math.cos(toRadians(source.latitude)) * Math.sin(toRadians(dest.latitude))) -
                (Math.sin(toRadians(source.latitude)) * Math.cos(toRadians(dest.latitude)) * Math.cos(toRadians(dest.longitude - source.longitude)));
        return Math.atan2(y,x);
    }

    /*
     * calcule la position suivante partant d'un point X sur un cap C parcourant une distance D
     * @param positionInitiale
     * @param cap
     * @param distance
     * @return position suivante
     **/
    /*
    static public PointGPS positionSuivante(PointGPS positionInitiale, double cap, double distance) {
		// var lat 2 = Math.asin( Math.sin(lat 1)*Math.cos(d/R) +
        // 				Math.cos(lat 1)*Math.sin(d/R)*Math.cos(brng) );
        double latSuiv = Math.asin(Math.sin(positionInitiale.getLatitudeRad()) * Math.cos(distance / rayonTerrestre) +
                Math.cos(positionInitiale.getLatitudeRad()) * Math.sin(distance / rayonTerrestre) * Math.cos(cap));
        // var lon 2 = lon 1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat 1),
        // 				Math.cos(d/R)-Math.sin(lat 1)*Math.sin(lat 2));
        double lonSuiv = positionInitiale.getLongitudeRad() + Math.atan2(Math.sin(cap) * Math.sin(distance / rayonTerrestre) * Math.cos(positionInitiale.getLatitudeRad()),
                Math.cos(distance / rayonTerrestre ) - Math.sin(positionInitiale.getLatitudeRad() * Math.sin(latSuiv)));
        return new PointGPS(latSuiv, lonSuiv);
    }
    */

    public static double toRadians(double degrees) {
        return degrees * Math.PI / 180;
    }
    public static double toDegrees(double radians) {
        return radians / (Math.PI / 180);
    }

    /**
     * find the nearest point of a segment to one point
     * @param segmentBegin end point 1
     * @param segmentEnd end point 2
     * @param point reference point
     * @return
     */
    public static LatLng nearestPoint(LatLng segmentBegin, LatLng segmentEnd, LatLng point) {

        double a1 = segmentBegin.latitude ;
        double a2 = segmentBegin.longitude ;
        double b1 = segmentEnd.latitude ;
        double b2 = segmentEnd.longitude ;
        double m1 = point.latitude ;
        double m2 = point.longitude ;
        // calcul savant
        double x = ((b1 - a1) * (m1 - b1) + (b2 - a2) * (m2 - b2)) / (Math.pow((b1 - a1), 2) + Math.pow((b2 - a2), 2));
        // nearest point
        double p1 = b1 + (b1 - a1) * x;
        double p2 = b2 + (b2 - a2) * x;
        // search if new point is in the segment
        if (m1 < (Math.min(a1 , b1))) { // point is more western than the segment
            if (a1 < b1) {
                // most west point is a
                p1 = a1;
                p2 = a2;
            }
            else {
                // most west point is b
                p1 = b1;
                p2 = b2;
            }
        }
        else if (m1 > (Math.max(a1 , b1))) { // point is more eastern than the segment
            if (a1 > b1) {  // most east point is a
                p1 = a1;
                p2 = a2;
            }
            else {  // most east point is b
                p1 = b1;
                p2 = b2;
            }
        }
        return new LatLng(p1,p2);
    }

}
