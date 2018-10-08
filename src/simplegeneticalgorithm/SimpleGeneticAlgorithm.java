/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simplegeneticalgorithm;

import java.util.ArrayList;

/**
 *
 * @author c2-newcombe
 */
public class SimpleGeneticAlgorithm {

    private static final int populationSize = 50,
            chromSize = 50,
            nGenerations = 50,
            nRuns = 10;

    private static double mutationRate = .1;

    private static Individual population[];
    private static Individual offspring[];

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //Init variables
        population = new Individual[populationSize];
        offspring = new Individual[populationSize];
        //mutationRate = calcMutationRate();
        //System.out.println("Pm = " + mutationRate);

        for(int r = 0; r < nRuns; r++){
        for (int i = 0; i < population.length; i++) {
            int[] genes = new int[chromSize];

            for (int j = 0; j < genes.length; j++) {
                genes[j] = (int) ((Math.random() * 2) % 2);
            }
            population[i] = new Individual(genes);
        }

        for (int g = 0; g < nGenerations; g++) {
            population = calcFitness(population);

            //System.out.println("Parent pop best fitness = " + bestFitness(population));

            offspring = crossover();
            offspring = mutate();

            offspring = calcFitness(offspring);

            //System.out.println("Offspring pop avg fitness = " + avgFitness(offspring));

            population = selectNewPopulation();
        }
        
        population = calcFitness(population);
        System.out.println("Parent pop best fitness = " + bestFitness(population));
        System.out.println("Parent pop avg fitness = " + avgFitness(population));
        System.out.println("Parent pop sum fitness = " + sumFitness(population));
        }
    }

    private static Individual[] selectNewPopulation() {
        Individual[] nextGen = new Individual[populationSize];

        for (int i = 0; i < populationSize; i++) {
            int parent = (int) ((Math.random() * population.length) % population.length);
            int child = (int) ((Math.random() * offspring.length) % offspring.length);

            if (population[parent].getFitness() >= offspring[child].getFitness()) {
                nextGen[i] = new Individual(population[parent].getChromosome());
            } else {
                nextGen[i] = new Individual(offspring[child].getChromosome());
            }
        }
        return nextGen;
    }

    private static Individual[] mutate() {
        for (int i = 0; i < offspring.length; i++) {
            offspring[i].setChromosome(
                    mutateChromosome(offspring[i].getChromosome()));
        }
        return offspring;
    }

    private static int calcMutationRate() {
        int min = 1 / populationSize;
        int max = 1 / chromSize;

        if (min >= max) {
            throw new IllegalArgumentException(
                    "population size must be greater than chromosome size");
        }
        return (int) (Math.random() * ((max - min) + 1)) + min;
    }

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

    private static Individual[] crossover() {
        ArrayList<Individual> children = new ArrayList<>();

        for (int i = 0; i < populationSize - 1; i++) {
            children.addAll(singlePointCrossover(
                    population[i].getChromosome(), population[(i + 1)].getChromosome()));
        }

        Individual[] ret = new Individual[children.size()];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = children.get(i);
        }
        return ret;
    }

    private static ArrayList<Individual> singlePointCrossover(int[] parent1, int[] parent2) {
        ArrayList<Individual> children = new ArrayList<Individual>();
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

    private static int[] mutateChromosome(int[] chrom) {
        int[] mutatedGenes = chrom;

        for (int i = 0; i < chromSize; i++) {
            double m = Math.random();
            if (mutationRate > m) {
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
}
