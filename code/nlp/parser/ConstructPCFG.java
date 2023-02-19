package nlp.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;


public class ConstructPCFG {
    protected HashMap<String, Double> counts;
    protected HashMap<String, HashMap<GrammarRule, Double>> rules;

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

    public void updateCounts(ParseTree pt) {
        String label = pt.getLabel();
        counts.put(label, counts.getOrDefault(label, 0.0) + 1.0);
        if (pt.getChildren() != null){
            GrammarRule rule = new GrammarRule(label, pt.getChildrenLabels());

            if (!rules.containsKey(label)) {
                rules.put(label, new HashMap<>());
            }

            rules.get(label).put(rule, rules.get(label).getOrDefault(rule, 0.0) + 1.0);

            for (ParseTree child : pt.getChildren()) {
                updateCounts(child);
            }
        }
    }

    public void updateWeights() {
        for (String key : rules.keySet()) {
            for (GrammarRule rule : rules.get(key).keySet()){
                if (rules.get(key).get(rule) != null) {
                    //System.out.println(rule + "rule counts: " + rules.get(key).get(rule) + " / counts: " + counts.get(key));
                    rule.setWeight(rules.get(key).get(rule) / counts.get(key));
                }
            }
        }
    }

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
        String filename = "/Users/talmordoch/Desktop/NLP/assign3-starter/example/example.parsed";
        //String filename = "/Users/talmordoch/Desktop/NLP/assign3-starter/data/simple.parsed";


        ConstructPCFG pcfg = new ConstructPCFG(filename);

        System.out.println(pcfg.getCounts());
        System.out.println(pcfg.getRules());
        //pcfg.updateWeight("PP");

        pcfg.printProbs();
    }
}