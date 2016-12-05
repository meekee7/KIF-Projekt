import org.onebusaway.gtfs.model.Route;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/**
 * Created by micha on 20.11.2016.
 */
public class CityFilter {
    private CityFilter() {
    }

    public static boolean VBB(Route route) {
        return true;
    }

    private static String getID(Route route) {
        return route.getAgency().getId().replaceAll("-|_", "");
    }

    public static boolean BerlinStreet(Route route) {
        return getID(route).equals("796") && (route.getType() == 700 || route.getType() == 900 || route.getType() == 3); //BVG, Bus & Tram
    }

    public static boolean BerlinFull(Route route) {
        return getID(route).equals("796") || getID(route).equals("1"); //BVG || S-Bahn Berlin
    }

    public static boolean Potsdam(Route route) {
        return getID(route).equals("150");
    }

    public static boolean Cottbus(Route route) {
        return getID(route).equals("516");
    }

    public static boolean Brandenburg(Route route) {
        return getID(route).equals("47");
    }

    public static boolean Frankfurt(Route route) {
        return getID(route).equals("84");
    }

    /*

    public static boolean BerlinStreet(Route route) {
        return getID(route).equals("BVB") || getID(route).equals("BVT");
    }

    public static boolean BerlinFull(Route route) {
        return getID(route).equals("BVB") || getID(route).equals("BVT") ||
                getID(route).equals("BVU") || getID(route).equals("SEV") || getID(route).equals("DBS");
    }

    public static boolean Potsdam(Route route) {
        return getID(route).equals("VIB") || getID(route).equals("VIT");
    }

    public static boolean Cottbus(Route route) {
        return getID(route).equals("CNB") || getID(route).equals("CNT");
    }

    public static boolean Brandenburg(Route route) {
        return getID(route).equals("BRB") || getID(route).equals("BRT");
    }

    public static boolean Frankfurt(Route route) {
        return getID(route).equals("FFB") || getID(route).equals("FFT");
    }
    */
}
