package service;

import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.LatLng;
import external.GoogleApi;
import utils.CommonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sqrt;

public class DistanceServiceImpl implements DistanceService {


    private final GoogleApi googleApi;

    private double delta = 0;

    public DistanceServiceImpl() {
        this.googleApi = new GoogleApi();
    }

    @Override
    public List<String> formPath(final String source, final String destination, double frequency) {

        if (CommonUtils.isStringEmpty(source) || CommonUtils.isStringEmpty(destination)) {
            throw new RuntimeException("invalid data");
        }

        DirectionsResult response = null;
        try {
            response = googleApi.getDistanceApiResponse(source, destination);
        } catch (IOException | InterruptedException | ApiException e) {
            return null;
        }

        return formPath(response, frequency);

    }

    private List<String> formPath(final DirectionsResult response, double frequency) {

        if (response.routes.length == 0 || response.routes[0].legs.length == 0) {
            return new ArrayList<>();
        }

        List<LatLng> latLngs = new ArrayList<>();
        for (DirectionsStep directionsStep : response.routes[0].legs[0].steps) {
            latLngs.addAll(directionsStep.polyline.decodePath());
        }

        List<LatLng> pathLatLng = new ArrayList<>();
        LatLng prevLatLng = latLngs.get(0);
        pathLatLng.add(prevLatLng);
        for (int i = 1; i < latLngs.size(); ++i) {
            LatLng curLatLng = latLngs.get(i);
            pathLatLng.add(latLngs.get(i));
            if (getDist(prevLatLng, curLatLng) >= (frequency - delta)
                    && getDist(prevLatLng, curLatLng) <= (frequency + delta)) {
                prevLatLng = curLatLng;
                pathLatLng.add(curLatLng);
            } else if (getDist(prevLatLng, curLatLng) > frequency + delta) {
                LatLng latLng = getPointAtDistanceAlongLine(prevLatLng, curLatLng, frequency);
                prevLatLng = latLng;
                pathLatLng.add(latLng);
                i--;
            }
        }
        List<String> path = new ArrayList<>();

        for (LatLng latLng : pathLatLng) {
            path.add(convertLatLngToString(latLng));
        }
        return path;
    }

    /*
    returns the point along line joining origin and  dest with distance frequency from the origin.
     */
    private LatLng getPointAtDistanceAlongLine(final LatLng origin, final LatLng dest, double frequency) {
        return interpolate(origin, dest, frequency);
    }

    private double getDist(final LatLng prevLatLng, final LatLng curLatLng) {
        return getHaversineDistance(prevLatLng, curLatLng);
    }

    private String convertLatLngToString(LatLng latLng) {
        return String.valueOf(latLng.lat) + "," + String.valueOf(latLng.lng);
    }

    private double getHaversineDistance(LatLng origin, LatLng destination) {
        double lat1 = origin.lat;
        double lon1 = origin.lng;
        double lat2 = destination.lat;
        double lon2 = destination.lng;

        double dLat = toRadians(lat2 - lat1);
        double dLon = toRadians(lon2 - lon1);

        // convert to radians
        lat1 = toRadians(lat1);
        lat2 = toRadians(lat2);

        // apply formulae
        double a = Math.pow(sin(dLat / 2), 2) +
                Math.pow(sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
    }

    private static LatLng interpolate(LatLng from, LatLng to, double fraction) {
        // http://en.wikipedia.org/wiki/Slerp
        double fromLat = toRadians(from.lat);
        double fromLng = toRadians(from.lng);
        double toLat = toRadians(to.lat);
        double toLng = toRadians(to.lng);
        double cosFromLat = cos(fromLat);
        double cosToLat = cos(toLat);

        // Computes Spherical interpolation coefficients.
        double angle = computeAngleBetween(from, to);
        double sinAngle = sin(angle);
        if (sinAngle < 1E-6) {
            return new LatLng(
                    from.lat + fraction * (to.lat - from.lat),
                    from.lng + fraction * (to.lng - from.lng));
        }
        double a = sin((1 - fraction) * angle) / sinAngle;
        double b = sin(fraction * angle) / sinAngle;

        // Converts from polar to vector and interpolate.
        double x = a * cosFromLat * cos(fromLng) + b * cosToLat * cos(toLng);
        double y = a * cosFromLat * sin(fromLng) + b * cosToLat * sin(toLng);
        double z = a * sin(fromLat) + b * sin(toLat);

        // Converts interpolated vector back to polar.
        double lat = atan2(z, sqrt(x * x + y * y));
        double lng = atan2(y, x);
        return new LatLng(toDegrees(lat), toDegrees(lng));
    }

    static double computeAngleBetween(LatLng from, LatLng to) {
        return distanceRadians(toRadians(from.lat), toRadians(from.lng),
                toRadians(to.lat), toRadians(to.lng));
    }

    private static double distanceRadians(double lat1, double lng1, double lat2, double lng2) {
        return arcHav(havDistance(lat1, lat2, lng1 - lng2));
    }

    static double havDistance(double lat1, double lat2, double dLng) {
        return hav(lat1 - lat2) + hav(dLng) * Math.cos(lat1) * Math.cos(lat2);
    }

    static double hav(double x) {
        double sinHalf = sin(x * 0.5);
        return sinHalf * sinHalf;
    }

    static double arcHav(double x) {
        return 2 * asin(Math.sqrt(x));
    }

}
