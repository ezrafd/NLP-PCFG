package nlp.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;


public class ConstructPCFG {
    protected HashMap<String, Double> counts; //a hash map with counts of each string
    protected HashMap<String, HashMap<GrammarRule, Double>> rules; //a hash map with the strings as keys and rules
    // and their counts as values

    public ConstructPCFG(String filename) {
        // initialize counts & rules
        counts = new HashMap<>();
        rules = new HashMap<>();

        // process file
        File file = new File(filename);

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                ParseTree pt = new ParseTree(scanner.nextLine());
                updateCounts(pt);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + file);
        }

        updateWeights();
    }

    /**
     * Given a ParseTree,the method updates the counts of each string and rules following it
     *
     * @param pt the ParseTree whose counts are updated.
     *
     */
    public void updateCounts(ParseTree pt) {

        //gets the label of the tree and updates the count/creates a new hashmap
        String label = pt.getLabel();
        counts.put(label, counts.getOrDefault(label, 0.0) + 1.0);

        //checks the tree has children, creates a GrammarRule using the label and children, and puts them in the
        // hashnmap of rules
        if (pt.getChildren() != null){
            GrammarRule rule = new GrammarRule(label, pt.getChildrenLabels());

            if (!rules.containsKey(label)) {
                rules.put(label, new HashMap<>());
            }

            rules.get(label).put(rule, rules.get(label).getOrDefault(rule, 0.0) + 1.0);

            //recursively goes over every child and updates the rest of the counts
            for (ParseTree child : pt.getChildren()) {
                updateCounts(child);
            }
        }
    }


    /**
     * Updates the weights of each GrammarRule in the tree
     */
    public void updateWeights() {
        for (String key : rules.keySet()) {
            for (GrammarRule rule : rules.get(key).keySet()){
                if (rules.get(key).get(rule) != null) {

                    //updates the weight by taking the count of the specific string and dividing it by the total
                    //number of counts of strings following that string
                    rule.setWeight(rules.get(key).get(rule) / counts.get(key));
                }
            }
        }
    }

    /**
     * Prints the probabilities of each rule.
     */
    public void printProbs() {
        for (String key : rules.keySet()) {
            for (GrammarRule rule : rules.get(key).keySet()) {
                System.out.println(rule);
            }
        }
    }


    public HashMap<String, Double> getCounts() { return counts; }

    public HashMap<String, HashMap<GrammarRule, Double>> getRules() { return rules; }

    public static void main(String[] args) {
        String filename = "/Users/ezraford/git/assign3-starter/example/example.parsed";
        //String filename = "/Users/talmordoch/Desktop/NLP/assign3-starter/example/example.parsed";
        //String filename = "/Users/talmordoch/Desktop/NLP/assign3-starter/data/simple.parsed";


        ConstructPCFG pcfg = new ConstructPCFG(filename);

        System.out.println(pcfg.getCounts());
        System.out.println(pcfg.getRules());
        //pcfg.updateWeight("PP");

        pcfg.printProbs();
    }
}