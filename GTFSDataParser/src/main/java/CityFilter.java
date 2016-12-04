import org.onebusaway.gtfs.model.Route;

import java.lang.annotation.Annotation;

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
        return getID(route).equals("BVB") || getID(route).equals("BVT");
    }

    public static boolean BerlinFull(Route route) {
        return BerlinNoFerry(route) || getID(route).equals("BVF");
    }

    public static boolean BerlinNoFerry(Route route) {
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
}
