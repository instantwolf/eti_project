package ab1.impl.gruppe_29_prett_fischer_szolderits;

import ab1.NFA;
import ab1.Transition;

import java.util.*;
import java.util.stream.Collectors;


public class NFATransformator {


    public static NFA nfaToDeterministicNFA(NFA eps,boolean skipTotalization){
        NFA nfa = transformEpsilonNFAtoNFA(eps);
        return calculateDeterministicNFA(nfa,skipTotalization);
    }


    public static NFA calculateDeterministicNFA(NFA nfa, boolean skipTotalization){
        Collection<Transition> transitions = nfa.getTransitions();
        Collection<Collection<Collection<Transition>>> stateAndCharGroupedSuperSuperSet = getFromStateAndCharGroupedTransitions(transitions);
       //We now have a 3 level set , where: on the first level within the set there is a set for each state, on the second level there is a set per Symbol
        while(isNFANonDeterministic(stateAndCharGroupedSuperSuperSet)){
            //1. Union of Transitions
            Collection<Transition> sameStateAndCharGroupedTransitions = findNonDeterministicTransitions(stateAndCharGroupedSuperSuperSet);
            //Introduce a new State that is UNION of states and substitute old transitions in the set
            String superState =  getNewSuperStateFromTransitions(sameStateAndCharGroupedTransitions);
            //remove the above transitions from the original set
            transitions.removeAll(sameStateAndCharGroupedTransitions);
            transitions.addAll(createNewTransitionsToSuperState(sameStateAndCharGroupedTransitions, superState));
            //copy transitions from the merged states onto the superstate
            transitions.addAll(copyTransitionsForMergedStates(transitions,superState));
            //recalculate set based on transitions
            stateAndCharGroupedSuperSuperSet = getFromStateAndCharGroupedTransitions(transitions);
        }
        Collection<Transition> clearedTransitions = removeUnreachableStates(transitions, nfa.getInitialState());
        //add state and complete the "totalization" of transition function
        if(!skipTotalization){
            Collection<Transition> missingTransitionsForTotality= addFangStateAndTransitions(clearedTransitions);
            clearedTransitions.addAll(missingTransitionsForTotality);
        }
        //get the new acceptingStates (maybe some accepting states were not reachable (anymore))
        Collection<String>  newAcceptingStates = calculateAcceptingStates(clearedTransitions,  nfa.getAcceptingStates());
        return createNFAFromTransitionSet(clearedTransitions, nfa.getInitialState(), newAcceptingStates);
    }

    private static Collection<Transition> addFangStateAndTransitions(Collection<Transition> transitions) {
        Random random = new Random();
        String fangState = "qFang-"+random.nextInt(1, 1024*1024)+"-"+random.nextInt(1, 1024*1024);
        Collection<Character> allChars = generateAllChars();
        Collection<String> states = collectStatesFromTransitions(transitions,false);
        Collection<Transition> missingTransitions =  states.stream().flatMap(x -> getCharacterTransitionsMissing(x, allChars, transitions, fangState).stream()).collect(Collectors.toSet());
        allChars.forEach(x -> missingTransitions.add(new Transition(fangState,x,fangState)));
        return missingTransitions;
    }

    public static Collection<Character> generateAllChars(){
        char i = 0;
        Collection<Character> allChars = new HashSet<>();

        while (i <= 255){
            allChars.add(Character.valueOf(i++));
        }
        return allChars;
    }

    private static Collection<Transition> getCharacterTransitionsMissing(String state,Collection<Character> charSet,Collection<Transition> transitions, String qFang){
        Collection<Character> missingChars =   charSet.stream().filter(x -> transitions.stream().noneMatch(y -> y.fromState().equals(state) && y.readSymbol() == x)).collect(Collectors.toSet());
        return missingChars.stream().map(x -> new Transition(state,x,qFang)).collect(Collectors.toSet());
    }

    private static Collection<Transition> removeUnreachableStates(Collection<Transition> transitions, String initialState){
            Collection<String> states;
            Collection<String> removableStates;
            do{
                states = collectStatesFromTransitions(transitions,false);
                Collection<String> targetStates = transitions.stream().map(Transition::toState).collect(Collectors.toSet());
                removableStates = states.stream().filter(x -> targetStates.stream().noneMatch(y -> y.equals(x)) && !x.equals(initialState)).collect(Collectors.toSet());

                removableStates.forEach(x -> transitions
                        .removeAll(transitions.stream().filter(y -> y.toState().equals(x) || y.fromState().equals(x))
                                .collect(Collectors.toSet())));

            }while(!removableStates.isEmpty() && !transitions.isEmpty());
            return transitions;
    }

    private static NFA createNFAFromTransitionSet(Collection<Transition> transitions, String initialState, Collection<String> acceptingStates) {
        NFA nfa = new NFAImpl(initialState);
        transitions.forEach(nfa::addTransition);
        acceptingStates.forEach(nfa::addAcceptingState);
        return nfa;
    }

    private static Collection<String> calculateAcceptingStates(Collection<Transition> transitions, Collection<String> oldAcceptingStates) {

        Collection<String> states =  collectStatesFromTransitions(transitions,false);
        Collection<String> newAcceptingStates = new HashSet<>();

        for(String state : states){
            if(isAcceptingState(state,oldAcceptingStates)) newAcceptingStates.add(state);
        }
        return newAcceptingStates;
    }

    private static boolean isAcceptingState(String state, Collection<String> acceptingStates) {
      Collection<String> separatedStates = decomposeSuperState(state);
      return separatedStates.stream().anyMatch(acceptingStates::contains);
    }


    private static Collection<Collection<Collection<Transition>>> getFromStateAndCharGroupedTransitions(Collection<Transition> transitions){
        Collection<Collection<Transition>> sourceGroupedTransitions = groupTransitionSetBySourceState(transitions);
        return sourceGroupedTransitions.stream().map(NFATransformator::groupTransitionSetByReadChar).collect(Collectors.toSet());
    }

    private static Collection<Transition> copyTransitionsForMergedStates(Collection<Transition> transitions, String superState) {
        Collection<String> states = decomposeSuperState(superState);
        Collection<Transition> copiedTransitions = new HashSet<>();
        states.forEach(x -> findTransitionsForState(x,transitions).stream()
                .map(y -> copyTransitionWithChangedSourceState(y,superState))
                .forEach(copiedTransitions::add));
        return copiedTransitions;
    }

    private static Transition copyTransitionWithChangedSourceState(Transition t, String superState){
        return new Transition(superState, t.readSymbol(),t.toState());
    }

    private static Collection<Transition> findTransitionsForState(String fromState, Collection<Transition> transitions) {
        return transitions.stream().filter(y -> y.fromState().equals(fromState)).collect(Collectors.toSet());
    }


    private static Collection<String> decomposeSuperState(String superState){
        return Arrays.stream(superState.split("\\s*,\\s*")).toList();
    }
    private static Collection<Transition> createNewTransitionsToSuperState(Collection<Transition> sameStateAndCharGroupedTransitions, String superState) {
        return sameStateAndCharGroupedTransitions.stream().map(x-> new Transition(x.fromState(),x.readSymbol(),superState)).collect(Collectors.toSet());
    }


    private static String getNewSuperStateFromTransitions(Collection<Transition> sameStateAndCharGroupedTransitions) {
        return sameStateAndCharGroupedTransitions.stream().map(Transition::toState).collect(Collectors.joining(", "));
    }


    private static Collection<Transition> findNonDeterministicTransitions(Collection<Collection<Collection<Transition>>> transitionSuperSuperSet){
       Optional<Collection<Collection<Transition>>> nonDetStateSuperSet =  transitionSuperSuperSet.stream().filter(NFATransformator::hasNonDeterministicSet).findFirst();
       if(nonDetStateSuperSet.isEmpty())
           throw new RuntimeException();
       Optional<Collection<Transition>> nonDetTransitions = nonDetStateSuperSet.get().stream().filter(NFATransformator::setHasMultipleTransitions).findAny();
        if(nonDetTransitions.isEmpty())
            throw new RuntimeException();
        return nonDetTransitions.get();
    }


    private static boolean setHasMultipleTransitions(Collection<Transition> set){
        return set.size() > 1;
    }
    private static boolean hasNonDeterministicSet(Collection<Collection<Transition>> stateBasedTransitionSet) {
        return stateBasedTransitionSet.stream().anyMatch(x->x.size()>1);
    }

    private static boolean isNFANonDeterministic(Collection<Collection<Collection<Transition>>> transitionSuperSuperSet){
        return transitionSuperSuperSet.stream().anyMatch(x -> x.stream().anyMatch(y -> y.size() > 1));
    }

    private static Collection<Collection<Transition>> groupTransitionSetByReadChar
            (Collection<Transition> stateBasedTransitionSet){
        Collection<Collection<Transition>> transitionSuperset = new HashSet<>();
        Collection<Character> readChars = collectReadSymbolFromTransitions(stateBasedTransitionSet);
        readChars.forEach(x -> transitionSuperset.add(stateBasedTransitionSet.stream().filter(y -> y.readSymbol() == x).collect(Collectors.toSet())));
        return transitionSuperset;
    }

    private static Collection<Character> collectReadSymbolFromTransitions(Collection<Transition> stateBasedTransitionSet) {
        return stateBasedTransitionSet.stream().map(Transition::readSymbol).collect(Collectors.toSet());
    }

    private static Collection<Collection<Transition>> groupTransitionSetBySourceState(Collection<Transition> transitions) {
        Collection<Collection<Transition>> transitionSuperset = new HashSet<>();
        Collection<String> sourceStates = collectStatesFromTransitions(transitions, true);
        sourceStates.forEach(x -> transitionSuperset.add(
                transitions.stream().filter(y -> y.fromState().equals(x)).collect(Collectors.toSet())));

        return transitionSuperset;
    }

    private static Collection<String> collectStatesFromTransitions(Collection<Transition> transitions, boolean skipDestinationStates) {
        //add sourceStates
        Collection<String> stateSet = transitions.stream().map(Transition::fromState).collect(Collectors.toSet());
        //add destinationStates
        if(!skipDestinationStates)
            stateSet.addAll(transitions.stream().map(Transition::toState).collect(Collectors.toSet()));
        return stateSet;
    }


    public static NFA transformEpsilonNFAtoNFA(NFA nfa){
        Collection<Transition> transitionSet = nfa.getTransitions();
        Collection<String> acceptingStates = nfa.getAcceptingStates();

        while(transitionSet.stream().anyMatch(x -> x.readSymbol() == null)){

            transitionSet.removeAll(getEpsilonLoops(transitionSet));

            //get the first epsilon-edge
            Optional<Transition> optionalTransition = transitionSet.stream()
                    .filter(x -> x.readSymbol() == null).findFirst();

            if(optionalTransition.isEmpty()) break;

            Transition eps = optionalTransition.get();

           transitionSet.addAll(bridgeInboundTransitions(eps,transitionSet));
           transitionSet.addAll(bridgeOutBoundTransitions(eps, transitionSet));
           //when target of eps transition is acceptingState , so is sourced
            if(acceptingStates.contains(eps.toState())){
                acceptingStates.add(eps.fromState());
            }
            transitionSet.remove(eps);
        }

        NFA nfa1 = new NFAImpl(nfa.getInitialState());
        transitionSet.forEach(nfa1::addTransition);
        acceptingStates.forEach(nfa1::addAcceptingState);

        return nfa1;
    }

    private static Collection<Transition> getEpsilonLoops(Collection<Transition> transitions){
        return transitions.stream()
                        .filter(x -> x.toState().equals(x.fromState()) && x.readSymbol() == null)
                        .collect(Collectors.toSet());
    }

    private static Collection<Transition> bridgeInboundTransitions(Transition epsilonTransition, Collection<Transition> transitions){
        // 1. Alle eingehenden Kanten des Ursprungszustandes der eps-Kante werden an den Zielzustand der epsilon-kante-kopiert.
        return transitions.stream()
                        .filter(x -> x.toState().equals(epsilonTransition.fromState()) && !x.toState().equals(x.fromState()))
                        .map(x -> new Transition(x.fromState(),x.readSymbol(),epsilonTransition.toState())).collect(Collectors.toSet());
    }

    private static Collection<Transition> bridgeOutBoundTransitions(Transition epsilonTransition, Collection<Transition> transitions) {
        // 2. Alle ausgehenden Kanten des Zielzustandes der eps-Kante werden an den Ursprungszustand der epsilon-kante-kopiert.
        return transitions.stream()
                .filter(x -> x.fromState().equals(epsilonTransition.toState()))
                .map(x -> new Transition(epsilonTransition.fromState(),x.readSymbol(),x.toState())).collect(Collectors.toSet());
    }
}