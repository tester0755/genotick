package com.alphatica.genotick.ui;

import com.alphatica.genotick.data.MainAppData;
import com.alphatica.genotick.genotick.Main;
import com.alphatica.genotick.genotick.MainSettings;
import com.alphatica.genotick.genotick.RandomGenerator;
import com.alphatica.genotick.timepoint.TimePoint;

import java.util.Random;

@SuppressWarnings("unused")
class RandomParametersInput extends BasicUserInput {
    private Random r = RandomGenerator.assignRandom();
    private final UserOutput output = UserInputOutputFactory.getUserOutput();

    @Override
    public MainSettings getSettings() {
        MainSettings defaults = MainSettings.getSettings();
        defaults.populationDesiredSize = 20000;
        defaults.populationDAO = "";
        defaults.requireSymmetricalRobots = false;
        defaults.killNonPredictingRobots = true;
        defaults.performTraining = true;
        MainAppData data = getData(Main.DEFAULT_DATA_DIR);
        assignTimePoints(defaults, data);
        return assignRandom(defaults);
    }

    private void assignTimePoints(MainSettings defaults, MainAppData data) {
        TimePoint first = data.getFirstTimePoint();
        TimePoint last = data.getLastTimePoint();
        long diff = last.getValue() - first.getValue();
        long count = Math.abs(r.nextLong() % diff);
        //defaults.startTmePoint = new TimePoint(last.getValue() - count);
        defaults.endTimePoint = last;
    }

    private MainSettings assignRandom(MainSettings settings) {

        settings.dataMaximumOffset = r.nextInt(256) + 1;
        settings.processorInstructionLimit = r.nextInt(256) + 1;
        settings.maximumDeathByAge = 0.25 * r.nextDouble();
        settings.maximumDeathByWeight = 0.25 * r.nextDouble();
        settings.probabilityOfDeathByAge = r.nextDouble();
        settings.probabilityOfDeathByWeight = r.nextDouble();
        settings.inheritedChildWeight = r.nextDouble();
        settings.protectRobotsUntilOutcomes = r.nextInt(100);
        settings.protectBestRobots = 0.25 * r.nextDouble();
        settings.newInstructionProbability = 0.25 * r.nextDouble();
        settings.instructionMutationProbability = 0.25 * r.nextDouble();
        settings.skipInstructionProbability = 0.25 * settings.newInstructionProbability;
        settings.minimumOutcomesToAllowBreeding = r.nextInt(50);
        settings.minimumOutcomesBetweenBreeding = r.nextInt(50);
        settings.randomRobotsAtEachUpdate = 0.25 * r.nextDouble();
        settings.resultThreshold = 1 + (r.nextDouble() * 9);
        return settings;

    }

}
