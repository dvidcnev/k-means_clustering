package src;

import java.io.FileReader;

import com.google.gson.JsonArray;

public class MainProgram {
    public static void main(String[] args) {

        // If args length is not 1, then exit
        if (args.length < 4) {
            System.out.println("ERROR! Not enough arguments provided!");
            System.exit(1);
        }

        final int Clusters = Integer.valueOf(args[2]);
        final int Sites = Integer.valueOf(args[3]);

        MapGUI mapGui = new MapGUI();
        // GUI interface
        if (Boolean.valueOf(args[0])) {
            mapGui.launchMap();
        }
        // SEQUENTIAL
        if (Integer.valueOf(args[1]) == 0) {

        }

    }
}
