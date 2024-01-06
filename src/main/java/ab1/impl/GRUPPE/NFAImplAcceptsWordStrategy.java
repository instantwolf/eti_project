package ab1.impl.GRUPPE;

import ab1.NFA;
import ab1.Transition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class NFAImplAcceptsWordStrategy {

    public static boolean acceptsWord(String word, NFA nfa){
        //a word is in the language of an NFA if the automaton CAN result in a configuration that
        //has a final state and an empty word (no characters left to read)
        Collection<FAConfiguration> predecessorSet ;
        Collection<FAConfiguration> successorSet = new HashSet<>();
        Collection<FAConfiguration> resultSet = new HashSet<>();
        Collection<Transition> transitions = nfa.getTransitions();

        FAConfiguration initialconfig = new FAConfiguration(nfa.getInitialState(),word);
        successorSet.add(initialconfig); //is going to be copied into predecessorSet

        //while we can find new configurations we proceed, once we cant , we are finished or stuck in a loop
        while(successorSet.stream().anyMatch(x -> !resultSet.contains(x))){
            //now check all resulting configurations for the ones that have no word left to read
            resultSet.addAll(successorSet);
            predecessorSet = new HashSet<>(successorSet);
            successorSet.clear();
            predecessorSet.forEach(x -> successorSet.addAll(getSuccessors(x, transitions)));
        }

        //now check if we have a final state within the configurations
        Collection<String> finalStates = nfa.getAcceptingStates();
        return resultSet.stream().anyMatch(x -> x.word().isEmpty() && finalStates.contains(x.currentState()));
    }

    private static Collection<FAConfiguration> getSuccessors(FAConfiguration config, Collection<Transition> transitions){
            Set<FAConfiguration> res = new HashSet<>();

            Character currentChar = getFirstCharacter(config.word());
            String remainingWord = getRemainingWord(config.word());

            //Get all transitions for the current state
            //Add all the epsilon transitions of every state
            Collection<Transition> epsilonTransitions = transitions.parallelStream()
                    .filter(x -> x.fromState().equals(config.currentState()))
                    .filter(x -> x.readSymbol() == null).collect(Collectors.toSet());

            epsilonTransitions.forEach(x -> res.add(new FAConfiguration(x.toState(), config.word())));

            Collection<Transition> stateTransitions = transitions.parallelStream()
                    .filter(x -> x.fromState().equals(config.currentState()))
                    .filter(x -> x.readSymbol() == currentChar).collect(Collectors.toSet());

            stateTransitions
                    .forEach(x -> res.add(new FAConfiguration(x.toState(), remainingWord)));

            return res;
    }

    private static String getRemainingWord(String word ){
        if(word != null && word.length() > 1){
            return word.substring(1);
        }
        return "";
    }

    private static Character getFirstCharacter(String word ){
        if(word != null && !word.isEmpty()){
            return word.charAt(0);
        }
        return null;
    }
}
