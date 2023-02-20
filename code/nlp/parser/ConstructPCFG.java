package nlp.parser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class ConstructPCFG {
    protected HashMap<String, Double> counts; //a hash map with counts of each string
    protected HashMap<String, HashMap<GrammarRule, Double>> rules; //a hash map with the strings as keys and rules
    // and their counts as values


    /**
     * Given a file name, the method constructs a PCFG by reading through a file and running several functions on it.
     *
     * @param filename the file name of the file we are working with
     *
     */
    public ConstructPCFG(String filename) {
        // initialize counts & rules
        counts = new HashMap<>();
        rules = new HashMap<>();

        // create bufferedReader, go through every line, and update the counts
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                ParseTree pt = new ParseTree(line);
                updateCounts(pt);
            }

            //update the weights and close the reader
            updateWeights();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        //goes over every String in the rules
        for (String key : rules.keySet()) {

            //creates the hashmaps
            HashMap<GrammarRule, Double> temp = rules.get(key);
            HashMap<GrammarRule, Double> updatedRules = new HashMap<>();

            //updates the weight by taking the count of the specific string and dividing it by the total
            //number of counts of strings following that string
            for (GrammarRule rule : temp.keySet()){
                double numerator = temp.get(rule);
                double denominator = counts.get(key);
                double weight = numerator / denominator;
                GrammarRule updatedRule = new GrammarRule(rule.getLhs(), rule.getRhs(), rule.isLexical());
                updatedRule.setWeight(weight);
                updatedRules.put(updatedRule, numerator);
            }
            rules.put(key, updatedRules);
        }
    }

    /**
     * Binarizes the PCFG (starts with the leftmost pair of symbols and replace them with a new nonterminal,
     * not already in the grammar, creates a new PCFG rule from X to those two symbols
     * with probability 1.0., and continues this process until the rule is binary) and generates file accordingly.
     */
    public void binarizePCFG() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("short.binary.pcfg"));

            //initializes the prefix and total count
            String prefix = "X";
            int totalCount = 1;

            //goes over all String representations of rules
            for (String key : rules.keySet()) {
                for (GrammarRule rule : rules.get(key).keySet()) {

                    //creates a variabloe for the left and right hand sides
                    String lhs = rule.getLhs();
                    ArrayList<String> rhs = rule.getRhs();

                    //enters this section if needs to be binarized
                    if (rhs.size() > 2) {

                        String prev = rhs.get(0);

                        // Binarize rule and write the new rule to the file
                        for (int i = 1; i < rhs.size() - 1; i++) {
                            String new_lhs = prefix + totalCount;
                            ArrayList<String> new_rhs = new ArrayList<>();
                            new_rhs.add(prev);
                            new_rhs.add(rhs.get(i));
                            GrammarRule g = new GrammarRule(new_lhs, new_rhs, 1.0);
                            writer.write(g.toString());
                            writer.newLine();
                            totalCount++;
                            prev = new_lhs;
                        }

                        //writes the rule to the file for the last case
                        ArrayList<String> new_rhs = new ArrayList<>();
                        new_rhs.add(prev);
                        new_rhs.add(rhs.get(rhs.size() - 1));
                        GrammarRule g2 = new GrammarRule(lhs, new_rhs, rule.getWeight());
                        writer.write(g2.toString());
                        writer.newLine();

                    // write rule to file if already binary
                    } else {
                        writer.write(rule.toString());
                        writer.newLine();
                    }

                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Binarizes the PCFG similarly to binarizePCFG(), but without duplicate intermediary rules with the
     * same right hand side.
     */
    public void betterBinarizePCFG() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("short.binary.shared.pcfg"));

            //initializes the prefix and total count
            String prefix = "X";
            int totalCount = 1;

            //initialize memory
            HashMap<ArrayList<String>, String> memory = new HashMap<>();

            //goes over all String representations of rules
            for (String key : rules.keySet()) {
                for (GrammarRule rule : rules.get(key).keySet()) {

                    //creates a variabloe for the left and right hand sides
                    String lhs = rule.getLhs();
                    ArrayList<String> rhs = rule.getRhs();

                    //enters this section if needs to be binarized
                    if (rhs.size() > 2) {

                        String prev = rhs.get(0);

                        // Binarize rule and write the new rule to the file
                        for (int i = 1; i < rhs.size() - 1; i++) {
                            String new_lhs = prefix + totalCount;
                            ArrayList<String> new_rhs = new ArrayList<>();
                            new_rhs.add(prev);
                            new_rhs.add(rhs.get(i));

                            // If already seen the rhs point to old rule instead of generating a new rule
                            if (memory.containsKey(new_rhs)) {
                                prev = memory.get(new_rhs);
                            } else {
                                memory.put(new_rhs, new_lhs);
                                GrammarRule g = new GrammarRule(new_lhs, new_rhs, 1.0);
                                writer.write(g.toString());
                                writer.newLine();
                                totalCount++;
                                prev = new_lhs;
                            }
                        }

                        //writes the rule to the file for the last case
                        ArrayList<String> new_rhs = new ArrayList<>();
                        new_rhs.add(prev);
                        new_rhs.add(rhs.get(rhs.size() - 1));
                        GrammarRule g2 = new GrammarRule(lhs, new_rhs, rule.getWeight());
                        writer.write(g2.toString());
                        writer.newLine();

                    // write rule to file if already binary
                    } else {
                        writer.write(rule.toString());
                        writer.newLine();
                    }

                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Generates the file.
     */
    public void printProbs() {

        //creates BufferedWriter
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("short.binary.pcfg"));

            //goes over every rule and writes it to the new file
            for (String key : rules.keySet()) {
                for (GrammarRule rule : rules.get(key).keySet()) {
                    writer.write(rule.toString());
                    writer.newLine();
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the counts hashmap
     *
     * @return the counts hashmap for the pcfg
     */
    public HashMap<String, Double> getCounts() { return counts; }

    /**
     * Get the rules hashmap
     *
     * @return the rules hashmap for the pcfg
     */
    public HashMap<String, HashMap<GrammarRule, Double>> getRules() { return rules; }

//    public static void main(String[] args) {
//        String filename = "/Users/ezraford/git/assign3-starter/data/simple.parsed";
//        String filename = "/Users/talmordoch/Desktop/NLP/assign3-starter/data/short.parsed";
//        String filename = "/Users/talmordoch/Desktop/NLP/assign3-starter/data/simple.parsed";
//
//        ConstructPCFG pcfg = new ConstructPCFG(filename);
//
//        pcfg.binarizePCFG();
//        pcfg.betterBinarizePCFG();
//        pcfg.printProbs();
//
//    }
}