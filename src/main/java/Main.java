import service.DistanceService;
import service.DistanceServiceImpl;

import java.util.List;

public class Main {

    private static DistanceService distanceService;

    static {
        distanceService = new DistanceServiceImpl();
    }

    public static void main(String[] args) {

        List<String> points =
                distanceService.formPath("26.8323,80.9214", "26.8426,80.9228", 0.050);
        for(String point : points) {
            System.out.println(point);
        }


    }
}
