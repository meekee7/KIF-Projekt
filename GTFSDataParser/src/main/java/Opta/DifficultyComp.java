package Opta;

import java.util.Comparator;

/**
 * Created by micha on 09.02.2017.
 */
public class DifficultyComp implements Comparator<OptaPassenger> {
    @Override
    public int compare(OptaPassenger o1, OptaPassenger o2) {
        double diff = o1.getAirdist() - o2.getAirdist();
        if (diff == 0.0)
            return 0;
        return diff > 0 ? 1 : -1;
    }
}
