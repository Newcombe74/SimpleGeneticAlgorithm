/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simplegeneticalgorithm;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 *
 * @author c2-newcombe
 */
public class SimpleGeneticAlgorithm {

    //Hyperparameters
    private static final int POP_SIZE = 100,
            N_GENS = 50,
            N_RUNS = 10,
            MUT_RES = 101,
            CRS_RES = 100;

    //Task Option Indexes
    private static final int TASK_COUNT_ONES = 1,
            TASK_X_SQUARED = 2,
            TASK_XY_FUNC = 3;

    //Test Option Indexes
    private static final int TEST_MUT = 1,
            TEST_CRS = 2,
            TEST_TOUR = 3,
            TEST_ROUL = 4;

    //Selection Type Indexes
    private static final int SEL_TOUR = 1,
            SEL_ROUL = 2;

    //Result Indexes 
    private static final int RES_BEST = 0,
            RES_AVG = 1,
            RES_SUM = 2,
            RES_GEN_TO_GO = 3;

    private static int chromSize = 50;

    //First Generation to reach Global Optimum
    private static int globalOptimum = 0;
    private static boolean reachGO = false;
    private static int genToReachGO = N_GENS * 2;

    private static double[][] runResults = new double[4][N_RUNS];

    //Mutation
    private static double mutationRates[];
    private static int currPm = 0;

    //Crossover
    private static double crossoverRates[];
    private static int currPc = 0;

    private static Individual population[];
    private static Individual offspring[];

    private static PrintWriter pw;

    private static int selectedTestOption, selectedTaskOption;
    private static int percComplete = 10;

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        //Init variables
        population = new Individual[POP_SIZE];
        offspring = new Individual[POP_SIZE];
        initMutationRates();
        initCrossoverRates();

        //GET users task selection
        Scanner scanner = new Scanner(System.in);
        boolean inputValid = false;
        while (!inputValid) {
            System.out.println("Please enter the number of the task you wish to run:");
            System.out.println(TASK_COUNT_ONES + ". Counting ones task");
            System.out.println(TASK_X_SQUARED + ". X squared task");
            System.out.println(TASK_XY_FUNC + ". XY function task");

            selectedTaskOption = scanner.nextInt();

            switch (selectedTaskOption) {
                case TASK_COUNT_ONES:
                    globalOptimum = 50;
                    getTestSelection();
                    inputValid = true;
                    break;
                case TASK_X_SQUARED:
                    chromSize = 8;
                    globalOptimum = 65025;
                    getTestSelection();
                    inputValid = true;
                    break;
                case TASK_XY_FUNC:
                    chromSize = 10;
                    globalOptimum = 225;
                    getTestSelection();
                    inputValid = true;
                    break;
                default:
                    System.out.println("Input invalid");
            }
        }
    }

    private static void runSimpleGA(int selectionType) {
        //SET each individuals genes to be 1 or 0 at random
        for (int i = 0; i < population.length; i++) {
            int[] genes = new int[chromSize];

            for (int j = 0; j < genes.length; j++) {
                genes[j] = (int) ((Math.random() * 2) % 2);
            }
            population[i] = new Individual(genes);
        }

        for (int g = 0; g < N_GENS; g++) {
            population = calcFitness(population);

            //Check if global optimum has been reached
            if (bestFitness(population) == globalOptimum && reachGO == false) {
                reachGO = true;
                genToReachGO = g + 1;
            }

            //System.out.println("Parent pop best fitness = " + bestFitness(population));
            offspring = crossover();
            offspring = mutate();

            offspring = calcFitness(offspring);

            //System.out.println("Offspring pop avg fitness = " + avgFitness(offspring));
            population = selection(selectionType);

        }
    }

    private static void getTestSelection() throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);
        boolean inputValid = false;

        while (!inputValid) {
            System.out.println("Please enter the number of the test you wish to run:");
            System.out.println(TEST_MUT + ". Mutation rate variance test");
            System.out.println(TEST_CRS + ". Crossover rate variance test");
            System.out.println(TEST_TOUR + ". Tournament selection test");
            System.out.println(TEST_ROUL + ". Roulette-wheel selection test");

            selectedTestOption = scanner.nextInt();

            switch (selectedTestOption) {
                case TEST_MUT:
                    System.out.println("Starting mutation rate variance test");
                    runMutationVarianceTest();
                    inputValid = true;
                    break;
                case TEST_CRS:
                    System.out.println("Starting crossover rate variance test");
                    runCrossoverRateVarianceTest();
                    inputValid = true;
                    break;
                case TEST_TOUR:
                    System.out.println("Starting tournament selection test");
                    runTournamentSelectionTest();
                    inputValid = true;
                    break;
                case TEST_ROUL:
                    System.out.println("Starting roulette-wheel selection test");
                    runRouletteWheelSelectionTest();
                    inputValid = true;
                    break;
                default:
                    System.out.println("Input invalid");
            }
        }
    }

    //START_TESTS
    private static void runMutationVarianceTest() throws FileNotFoundException {
        initMutationsCSV("MutationRateVarianceResults.csv");

        //Crossover probability = 100%
        currPc = CRS_RES - 1;

        for (int m = 0; m < MUT_RES; m++) {
            for (int r = 0; r < N_RUNS; r++) {
                reachGO = false;
                genToReachGO = N_GENS * 2;

                runSimpleGA(SEL_TOUR);
                population = calcFitness(population);
                //writeRunResults(r + 1);
                runResults[RES_BEST][r] = bestFitness(population);
                runResults[RES_AVG][r] = avgFitness(population);
                runResults[RES_SUM][r] = sumFitness(population);
                runResults[RES_GEN_TO_GO][r] = genToReachGO;
            }
            writeResults(m + 1, mutationRates[m]);
            currPm++;

            if (calcPerc(m, MUT_RES) > percComplete) {
                System.out.println("Test " + percComplete + "% complete");
                percComplete += 10;
            }
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void runCrossoverRateVarianceTest() throws FileNotFoundException {
        initCrossoversCSV("CrossoverRateVarianceResults.csv");

        for (int c = 0; c < CRS_RES; c++) {
            for (int r = 0; r < N_RUNS; r++) {
                reachGO = false;
                genToReachGO = N_GENS * 2;

                runSimpleGA(SEL_TOUR);
                population = calcFitness(population);
                //writeRunResults(r + 1);
                runResults[RES_BEST][r] = bestFitness(population);
                runResults[RES_AVG][r] = avgFitness(population);
                runResults[RES_SUM][r] = sumFitness(population);
                runResults[RES_GEN_TO_GO][r] = genToReachGO;
            }
            writeResults(c + 1, crossoverRates[c]);
            currPc++;

            if (calcPerc(c, 100) > percComplete) {
                System.out.println("Test " + percComplete + "% complete");
                percComplete += 10;
            }
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void runTournamentSelectionTest() throws FileNotFoundException {
        initRunCSV("TournementSelectionResults.csv");

        //Crossover probability = 100%
        currPc = CRS_RES - 1;

        for (int r = 0; r < N_RUNS; r++) {
            reachGO = false;
            genToReachGO = N_GENS * 2;

            runSimpleGA(SEL_TOUR);
            population = calcFitness(population);
            //writeRunResults(r + 1);
            runResults[RES_BEST][r] = bestFitness(population);
            runResults[RES_AVG][r] = avgFitness(population);
            runResults[RES_SUM][r] = sumFitness(population);
            runResults[RES_GEN_TO_GO][r] = genToReachGO;

            writeRunResults(r + 1);

            if (calcPerc(r, N_RUNS) > percComplete) {
                System.out.println("Test " + percComplete + "% complete");
                percComplete += 10;
            }
        }

        System.out.println("Test complete");
        pw.close();
    }

    private static void runRouletteWheelSelectionTest() throws FileNotFoundException {
        initRunCSV("RouletteWheelSelectionResults.csv");

        //Crossover probability = 100%
        currPc = CRS_RES - 1;

        for (int r = 0; r < N_RUNS; r++) {
            reachGO = false;
            genToReachGO = N_GENS * 2;

            runSimpleGA(SEL_ROUL);
            population = calcFitness(population);
            //writeRunResults(r + 1);
            runResults[RES_BEST][r] = bestFitness(population);
            runResults[RES_AVG][r] = avgFitness(population);
            runResults[RES_SUM][r] = sumFitness(population);
            runResults[RES_GEN_TO_GO][r] = genToReachGO;

            writeRunResults(r + 1);

            if (calcPerc(r, N_RUNS) > percComplete) {
                System.out.println("Test " + percComplete + "% complete");
                percComplete += 10;
            }
        }

        System.out.println("Test complete");
        pw.close();
    }
    //END_TESTS

    //START_CSV
    private static void initRunCSV(String name) throws FileNotFoundException {
        pw = new PrintWriter(new File(name));
        StringBuilder sb = new StringBuilder();
        sb.append("Population Size = ");
        sb.append(String.valueOf(POP_SIZE));
        sb.append('\n');
        sb.append("Chromosome Size = ");
        sb.append(String.valueOf(chromSize));
        sb.append('\n');
        sb.append("No of Generations = ");
        sb.append(String.valueOf(N_GENS));
        sb.append('\n');
        sb.append("Mutation Rate = ");
        sb.append(mutationRates[currPm]);
        sb.append('\n');
        sb.append("Crossover Rate = ");
        sb.append(crossoverRates[currPc]);
        sb.append('\n');
        sb.append('\n');
        sb.append("RunId");
        sb.append(',');
        sb.append("Best Fitness");
        sb.append(',');
        sb.append("Avg Fitness");
        sb.append(',');
        sb.append("Total Fitness");
        sb.append(',');
        sb.append("First Gen to Reach GO");
        sb.append('\n');
        pw.write(sb.toString());
    }

    private static void initMutationsCSV(String name) throws FileNotFoundException {
        pw = new PrintWriter(new File(name));
        StringBuilder sb = new StringBuilder();
        sb.append("Population Size = ");
        sb.append(String.valueOf(POP_SIZE));
        sb.append('\n');
        sb.append("Chromosome Size = ");
        sb.append(String.valueOf(chromSize));
        sb.append('\n');
        sb.append("No of Generations = ");
        sb.append(String.valueOf(N_GENS));
        sb.append('\n');
        sb.append("No of Runs = ");
        sb.append(String.valueOf(N_RUNS));
        sb.append('\n');
        sb.append('\n');
        sb.append("Id");
        sb.append(',');
        sb.append("MutationRate");
        sb.append(',');
        sb.append("Avg Best Fitness");
        sb.append(',');
        sb.append("Avg Avg Fitness");
        sb.append(',');
        sb.append("Avg Total Fitness");
        sb.append(',');
        sb.append("First Gen to Reach GO");
        sb.append('\n');
        pw.write(sb.toString());
    }

    private static void initCrossoversCSV(String name) throws FileNotFoundException {
        pw = new PrintWriter(new File(name));
        StringBuilder sb = new StringBuilder();
        sb.append("Population Size = ");
        sb.append(String.valueOf(POP_SIZE));
        sb.append('\n');
        sb.append("Chromosome Size = ");
        sb.append(String.valueOf(chromSize));
        sb.append('\n');
        sb.append("No of Generations = ");
        sb.append(String.valueOf(N_GENS));
        sb.append('\n');
        sb.append("No of Runs = ");
        sb.append(String.valueOf(N_RUNS));
        sb.append('\n');
        sb.append('\n');
        sb.append("Id");
        sb.append(',');
        sb.append("CrossoverRate");
        sb.append(',');
        sb.append("Avg Best Fitness");
        sb.append(',');
        sb.append("Avg Avg Fitness");
        sb.append(',');
        sb.append("Avg Total Fitness");
        sb.append(',');
        sb.append("First Gen to Reach GO");
        sb.append('\n');
        pw.write(sb.toString());
    }

    private static void writeRunResults(int runId) {
        StringBuilder sb = new StringBuilder();
        sb.append(runId);
        sb.append(',');
        sb.append(bestFitness(population));
        sb.append(',');
        sb.append(avgFitness(population));
        sb.append(',');
        sb.append(sumFitness(population));
        sb.append(',');
        sb.append(genToReachGO);
        sb.append('\n');
        pw.write(sb.toString());

    }

    private static void writeResults(int id, double rate) {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append(',');
        sb.append(rate);
        sb.append(',');
        sb.append(calcAvg(runResults[RES_BEST]));
        sb.append(',');
        sb.append(calcAvg(runResults[RES_AVG]));
        sb.append(',');
        sb.append(calcAvg(runResults[RES_SUM]));
        sb.append(',');
        sb.append(calcAvg(runResults[RES_GEN_TO_GO]));
        sb.append('\n');
        pw.write(sb.toString());
    }
    //END_CSV

    //START_Selection
    private static Individual[] selection(int selectionType) {
        switch (selectionType) {
            case SEL_TOUR:
                return tournementSelection();
            case SEL_ROUL:
                return rouletteWheelSelection();
            default:
                System.err.println("Selection type not found: " + selectionType);
                return null;
        }
    }

    private static Individual[] tournementSelection() {
        Individual[] nextGen = new Individual[POP_SIZE];

        if (offspring.length > 0) {
            for (int i = 0; i < POP_SIZE; i++) {
                int parent = (int) ((Math.random() * population.length) % population.length);
                int child = (int) ((Math.random() * offspring.length) % offspring.length);

                if (population[parent].getFitness() >= offspring[child].getFitness()) {
                    nextGen[i] = new Individual(population[parent].getChromosome());
                } else {
                    nextGen[i] = new Individual(offspring[child].getChromosome());
                }
            }

            return nextGen;
        } else {
            return population;
        }
    }

    private static Individual[] rouletteWheelSelection() {
        Individual[] nextGen = new Individual[POP_SIZE];

        //Put parents and children into a single population
        Individual[] currentGen = new Individual[population.length + offspring.length];
        int n = 0;
        for (Individual inividual : population) {
            currentGen[n] = inividual;
            n++;
        }
        for (Individual inividual : offspring) {
            currentGen[n] = inividual;
            n++;
        }

        int totalFitness = sumFitness(currentGen);
        for (int i = 0; i < POP_SIZE; i++) {
            int runningTotal = 0, j = 0;

            int selectionPoint = (int) ((Math.random() * totalFitness) % totalFitness);

            while (runningTotal <= selectionPoint) {
                runningTotal += currentGen[j].getFitness();
                j++;
            }
            nextGen[i] = currentGen[j - 1];
        }

        return nextGen;
    }
    //END_Selection

//START_Crossover
    private static Individual[] crossover() {
        ArrayList<Individual> children = new ArrayList<>();

        for (int i = 0; i < POP_SIZE - 1; i++) {
            double c = Math.random() * CRS_RES;
            if (crossoverRates[currPc] > c) {
                children.addAll(singlePointCrossover(
                        population[i].getChromosome(), population[(i + 1)].getChromosome()));
            }
        }

        Individual[] ret = new Individual[children.size()];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = children.get(i);
        }
        return ret;
    }

    private static ArrayList<Individual> singlePointCrossover(int[] parent1, int[] parent2) {
        ArrayList<Individual> children = new ArrayList<>();
        int[][] crossoverGenes = new int[2][chromSize];
        int child1 = 0, child2 = 1;
        int crossoverPoint = (int) (Math.random() * chromSize) - 1;

        for (int i = 0; i < chromSize; i++) {
            if (i < crossoverPoint) {
                crossoverGenes[child1][i] = parent1[i];
            } else {
                crossoverGenes[child1][i] = parent2[i];
            }
            if (i < crossoverPoint) {
                crossoverGenes[child2][i] = parent2[i];
            } else {
                crossoverGenes[child2][i] = parent1[i];
            }
        }

        children.add(new Individual(crossoverGenes[child1]));
        children.add(new Individual(crossoverGenes[child2]));

        return children;
    }

    private static void initCrossoverRates() {
        crossoverRates = new double[CRS_RES];

        for (int i = 0; i < CRS_RES; i++) {
            crossoverRates[i] = i + 1;
        }
    }
    //END_Crossover

    //START_Mutation
    private static Individual[] mutate() {
        for (int i = 0; i < offspring.length; i++) {
            offspring[i].setChromosome(
                    mutateChromosome(offspring[i].getChromosome()));
        }
        return offspring;
    }

    private static int calcRandMutationRate() {
        int min = 1 / POP_SIZE;
        int max = 1 / chromSize;

        if (min >= max) {
            throw new IllegalArgumentException(
                    "population size must be greater than chromosome size");
        }
        return (int) (Math.random() * ((max - min) + 1)) + min;
    }

    private static void initMutationRates() {
        mutationRates = new double[MUT_RES];

        double min = (double) 1 / POP_SIZE;
        double max = (double) 1 / chromSize;

        if (min >= max) {
            throw new IllegalArgumentException(
                    "population size must be greater than chromosome size");
        }

        double step = (max - min) / MUT_RES;

        mutationRates[0] = min;
        for (int i = 1; i < MUT_RES; i++) {
            mutationRates[i] = mutationRates[i - 1] + step;
        }
    }

    private static int[] mutateChromosome(int[] chrom) {
        int[] mutatedGenes = chrom;

        for (int i = 0; i < chromSize; i++) {
            double m = Math.random();
            if (mutationRates[currPm] > m) {
                mutatedGenes[i] = invert(chrom[i]);
            }
        }

        return mutatedGenes;
    }

    private static int invert(int gene) {
        if (gene == 1) {
            return 0;
        } else {
            return 1;
        }
    }
    //END_Mutation

    //START_Fitness
    private static Individual[] calcFitness(Individual[] pop) {
        switch(selectedTaskOption){
            case TASK_COUNT_ONES:
                return calcCountOnesFitness(pop);
            case TASK_X_SQUARED:
                return calcXSquaredFitness(pop);
            case TASK_XY_FUNC:
                return calcXYFuncFitness(pop);
            default:
                System.err.println("Task option not found: " + selectedTaskOption);
                return null;
        }
    }

    private static Individual[] calcCountOnesFitness(Individual[] pop){
        Individual[] newPop = new Individual[pop.length];

        for (int i = 0; i < pop.length; i++) {
            int fitness = 0;
            int[] genes = pop[i].getChromosome();
            for (int j = 0; j < genes.length; j++) {
                if (genes[j] == 1) {
                    fitness++;
                }
            }
            newPop[i] = new Individual(genes, fitness);
        }
        return newPop;
    }
    
    private static Individual[] calcXSquaredFitness(Individual[] pop){
        Individual[] newPop = new Individual[pop.length];

        for (int i = 0; i < pop.length; i++) {
            int fitness = 0;
            int[] genes = pop[i].getChromosome();
            for (int j = 0; j < genes.length; j++) {
                if (genes[j] == 1) {
                    fitness += Math.pow(2, j);
                }
            }
            fitness = (int) Math.pow(fitness, 2);
            newPop[i] = new Individual(genes, fitness);
        }
        return newPop;
    }
    
    private static Individual[] calcXYFuncFitness(Individual[] pop){
        Individual[] newPop = new Individual[pop.length];
        
        for (int i = 0; i < pop.length; i++) {
            int fitness;
            int[] genes = pop[i].getChromosome();
            int x = 0, y = 0, exp = 0, fp;
            
            //GET X
            fp = genes[0];
            for (int j = 1; j < 5; j++) {
                if(genes[j] == 1){
                    x += Math.pow(2, exp);
                }
                exp++;
            }
            if(fp == 1){
                x = (0 - x);
            }
            
            //GET Y
            exp = 0;
            fp = genes[5];
            for (int j = 6; j < 10; j++) {
                if(genes[j] == 1){
                    y += Math.pow(2, exp);
                }
                exp++;
            }
            if(fp == 1){
                y = (0 - y);
            }
            
            fitness = (int) (0.26*(Math.pow(x, 2) + Math.pow(y, 2)) - 0.48*x*y);
            newPop[i] = new Individual(genes, fitness);
        }
        return newPop;
    }
    
    private static int avgFitness(Individual[] pop) {
        return sumFitness(pop) / pop.length;
    }

    private static int bestFitness(Individual[] pop) {
        int ret = 0;
        for (Individual i : pop) {
            if (i.getFitness() > ret) {
                ret = i.getFitness();
            }
        }
        return ret;
    }

    private static int sumFitness(Individual[] pop) {
        int ret = 0;
        for (Individual i : pop) {
            ret += i.getFitness();
        }
        return ret;
    }
    //END_Fitness

    //START_Utils
    private static double calcAvg(double[] arr) {
        if (arr.length == 0) {
            return 0;
        }

        double ret = 0;

        for (int i = 0; i < arr.length; i++) {
            ret += arr[i];
        }
        return ret / arr.length;
    }

    private static double calcPerc(double a, double b) {
        return (100 / b) * a;
    }
    //END_Utils
}
