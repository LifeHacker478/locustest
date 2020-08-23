package service;

import java.util.List;

public interface DistanceService {

    List<String> formPath(String source, String destination, double frequency);
}
