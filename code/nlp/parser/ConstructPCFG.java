package nlp.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
        if (!pt.isTerminal()){

            //gets the label of the tree and updates the count/creates a new hashmap
            String label = pt.getLabel();
            counts.put(label, counts.getOrDefault(label, 0.0) + 1.0);

            GrammarRule rule;

            // if there is only one child, and the child is terminal, set the correct isLexical value
            if (pt.getChild(0).isTerminal()){
                rule = new GrammarRule(label, pt.getChildrenLabels(), true);
            } else {
                rule = new GrammarRule(label, pt.getChildrenLabels(), false);
            }

            //adds new rule to rules
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
            HashMap<GrammarRule, Double> temp = rules.get(key);
            HashMap<GrammarRule, Double> updatedRules = new HashMap<>();
            for (GrammarRule rule : temp.keySet()){
                //updates the weight by taking the count of the specific string and dividing it by the total
                //number of counts of strings following that string
                double numerator = temp.get(rule);
                double denominator = counts.get(key);
                double weight = numerator / denominator;
                GrammarRule updatedRule = new GrammarRule(rule.getLhs(), rule.getRhs(), rule.isLexical());
                updatedRule.setWeight(weight);
                updatedRules.put(updatedRule, numerator);
                System.out.println(rules.get(key).get(rule));
            }
            rules.put(key, updatedRules);
        }
    }

    public void binarizePCFG() {
        //To binarize a rule, start with the leftmost pair of symbols and replace them with a new nonterminal,
        //not already in your grammar. For this assignment, we’ll use X followed by some number
        //to represent these new non-terminals. Then, create a new PCFG rule from X to those two symbols
        //with probability 1.0. Continue this process until the rule is binary

        String prefix = "X";
        int totalCount = 0;

        for (String key : rules.keySet()) {
            for (GrammarRule rule : rules.get(key).keySet()) {
                String lhs = rule.getLhs();
                ArrayList<String> rhs = rule.getRhs();
                if (rhs.size() > 2) {
                    String prev = rhs.get(0);
                    for (int i = 1; i < rhs.size() - 1; i++) {
                        String new_lhs = "X" + i;
                        ArrayList<String> new_rhs = new ArrayList<>();
                        new_rhs.add(prev);
                        new_rhs.add(rhs.get(i));
                        GrammarRule g = new GrammarRule(new_lhs, new_rhs, 1.0);
                        System.out.println(g);
                        totalCount++;
                        prev = new_lhs;
                    }
                } else {
                    System.out.println(rule);
                }

            }
        }
    }


        /*
        int i = 0;
        HashMap<String, Double> updatedCounts = new HashMap<>();
        HashMap<String, HashMap<GrammarRule, Double>> updatedRules = new HashMap<>();

        for (String key : rules.keySet()) {
            for (GrammarRule rule : rules.get(key).keySet()){
                if (rule.getRhs().size() > 2) {
                    for (int j = 0; j < rule.getRhs().size() / 2; j++) {
                        String xKey;
                        if (i == 0) {
                            xKey = "X";
                        } else {
                            xKey = "X" + i;
                        }
                        ArrayList<String> subList = new ArrayList<>(rule.getRhs().subList(j, j+2));
                        GrammarRule updatedRule = new GrammarRule(xKey, subList, rule.isLexical());
                        updatedCounts.put(xKey, 1.0);
                        updatedRules.put(xKey, new HashMap<>());
                        updatedRules.get(xKey).put(updatedRule, 1.0);
                        i++;
                    }

                    //rule.getRhs().subList(0,2)
                }
            }
        }

        rules.putAll(updatedRules);
    }

         */

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

        pcfg.binarizePCFG();

        pcfg.printProbs();
    }
}