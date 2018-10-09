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
            CHROM_SIZE = 50,
            N_GENS = 50,
            N_RUNS = 10,
            MUT_RES = 101,
            CRS_RES = 100;

    //Indexes 
    private static final int RES_BEST = 0,
            RES_AVG = 1,
            RES_SUM = 2,
            RES_GEN_TO_GO = 3;

    //First Generation to reach Global Optimum
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

        Scanner scanner = new Scanner(System.in);
        boolean inputValid = false;
        int option = 0, percComplete = 10;

        while (!inputValid) {
            System.out.println("Please enter the number of the test you wish to run:");
            System.out.println("1. Mutation rate variance test");
            System.out.println("2. Crossover rate variance test");

            option = scanner.nextInt();

            switch (option) {
                case 1:
                    System.out.println("Starting mutation rate variance test");
                    inputValid = true;
                    break;
                case 2:
                    System.out.println("Starting crossover rate variance test");
                    inputValid = true;
                    break;
                default:
                    System.out.println("Input invalid");
            }
        }

        if (option == 1) {
            initMutationsCSV("MutationRates.csv");

            currPc = CRS_RES - 1;

            for (int m = 0; m < MUT_RES; m++) {
                for (int r = 0; r < N_RUNS; r++) {
                    reachGO = false;
                    genToReachGO = N_GENS * 2;

                    runGA();
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
        } else if (option == 2) {
            initCrossoversCSV("CrossoverRates.csv");

            for (int c = 0; c < CRS_RES; c++) {
                for (int r = 0; r < N_RUNS; r++) {
                    reachGO = false;
                    genToReachGO = N_GENS * 2;

                    runGA();
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

    }

    private static void runGA() {
        for (int i = 0; i < population.length; i++) {
            int[] genes = new int[CHROM_SIZE];

            for (int j = 0; j < genes.length; j++) {
                genes[j] = (int) ((Math.random() * 2) % 2);
            }
            population[i] = new Individual(genes);
        }

        for (int g = 0; g < N_GENS; g++) {
            population = calcFitness(population);

            if (bestFitness(population) == 50 && reachGO == false) {
                reachGO = true;
                genToReachGO = g + 1;
            }

            //System.out.println("Parent pop best fitness = " + bestFitness(population));
            offspring = crossover();
            offspring = mutate();

            offspring = calcFitness(offspring);

            //System.out.println("Offspring pop avg fitness = " + avgFitness(offspring));
            population = selectNewPopulation();
        }
    }

    //START_CSV
    private static void initRunCSV(String name) throws FileNotFoundException {
        pw = new PrintWriter(new File(name));
        StringBuilder sb = new StringBuilder();
        sb.append("RunId");
        sb.append(',');
        sb.append("Best Fitness");
        sb.append(',');
        sb.append("Avg Fitness");
        sb.append(',');
        sb.append("Total Fitness");
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
        sb.append(String.valueOf(CHROM_SIZE));
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
        sb.append(String.valueOf(CHROM_SIZE));
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
    private static Individual[] selectNewPopulation() {
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
        int[][] crossoverGenes = new int[2][CHROM_SIZE];
        int child1 = 0, child2 = 1;
        int crossoverPoint = (int) (Math.random() * CHROM_SIZE) - 1;

        for (int i = 0; i < CHROM_SIZE; i++) {
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
        int max = 1 / CHROM_SIZE;

        if (min >= max) {
            throw new IllegalArgumentException(
                    "population size must be greater than chromosome size");
        }
        return (int) (Math.random() * ((max - min) + 1)) + min;
    }

    private static void initMutationRates() {
        mutationRates = new double[MUT_RES];

        double min = (double) 1 / POP_SIZE;
        double max = (double) 1 / CHROM_SIZE;

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

        for (int i = 0; i < CHROM_SIZE; i++) {
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
        for (Individual individual : pop) {
            individual.setFitness(0);
        }

        for (Individual individual : pop) {
            int[] genes = individual.getChromosome();
            for (int j = 0; j < genes.length; j++) {
                if (genes[j] == 1) {
                    individual.incFitness();
                }
            }
        }
        return pop;
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
