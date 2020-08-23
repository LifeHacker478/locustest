package external;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;

import java.io.IOException;
import java.time.Instant;

public class GoogleApi {

    private static final String ORIGIN = "origin";
    private static final String DESTINATION = "destination";
    private static final String apiKey = "AIzaSyAb8ohmBXqtK4y2_a5CFnFnfLGiOsuwjIo";

    private GeoApiContext context;

    public GoogleApi() {
        context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }


    public DirectionsResult getDistanceApiResponse(final String source, final String destination) throws IOException, InterruptedException, ApiException {
        return DirectionsApi.getDirections(context, source, destination).departureTime(Instant.now()).await();
    }

}
