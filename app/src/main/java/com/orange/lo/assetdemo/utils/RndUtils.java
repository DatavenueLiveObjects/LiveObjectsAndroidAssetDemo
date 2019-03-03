package com.orange.lo.assetdemo.utils;

import java.util.Random;

/**
 * Created by zlgp6287 on 26/10/2016.
 */

public class RndUtils {
    public static int randInt(int min, int max) {

        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((max - min) + 1) + min;

    }
}
