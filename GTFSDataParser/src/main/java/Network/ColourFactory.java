package Network;

import java.awt.*;
import java.util.Random;

/**
 * Created by micha on 08.01.2017.
 */
public class ColourFactory {
    private static Random random = new Random(3268798974L);

    private ColourFactory() {
    }

    public static Color createColour(Random random) {
        return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    public static Color createColour() {
        return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }
}
