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

        Collection<FAConfiguration> startSet ;


        Collection<FAConfiguration> workingSet = new HashSet<>();
        workingSet.add(new FAConfiguration(nfa.getInitialState(),word));

        Collection<FAConfiguration> resultSet = new HashSet<>();


        Collection<Transition> transitions = nfa.getTransitions();

        //if this condition is true, word has been processed and no more transitional changes can be applied


        while(!workingSet.isEmpty()){
            startSet = new HashSet<>(workingSet);
            workingSet.clear();

            startSet.forEach(x -> workingSet.addAll(getSuccessors(x, transitions)));

            //now check all resulting configurations for the ones that have no word left to read
            workingSet.stream().filter(x -> x.word() == null).forEach(resultSet::add);
        }

        //now check if we have a final state within the configurations
        Collection<String> finalStates = nfa.getAcceptingStates();
        return resultSet.stream().anyMatch(x -> finalStates.contains(x.currentState()));
    }


    private static Collection<FAConfiguration> getSuccessors(FAConfiguration config, Collection<Transition> transitions){
            Set<FAConfiguration> res = new HashSet<>();

            if(config.word().isEmpty()){
                res.add(config);
            }

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
