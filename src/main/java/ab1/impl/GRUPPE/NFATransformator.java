package ab1.impl.GRUPPE;

import ab1.NFA;
import ab1.Transition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NFATransformator {

    public static NFA transformEpsilonNFAtoNFA(NFA nfa){
        Collection<Transition> transitionSet = nfa.getTransitions();
        Collection<String> acceptingStates = nfa.getAcceptingStates();

        //um epsilon verbindungen zu elimieren:
        // iteriere über alle transitionen.
        //lösche Epsilonschleifen der länge 0 (reflex. transitionen)
        // für jede Epislonkante:
        // 1. Alle eingehenden Kanten des Ursprungszustandes der eps-Kante werden an den Zielzustand der epsilon-kante-kopiert.
        // 2. Alle ausgehenden Kanten des Zielzustandes der eps-Kante werden an den Ursprungszustand der epsilon-kante-kopiert.
        // Nach dem Ausführen der zwei Schritte kann die Kante gelöscht werden

        while(transitionSet.stream().anyMatch(x -> x.readSymbol() == null)){

            transitionSet.removeAll(getEpsilonLoops(transitionSet));

            //get the first epsilon-edge
            Optional<Transition> optionalTransition = transitionSet.stream()
                    .filter(x -> x.readSymbol() == null).findFirst();

            if(optionalTransition.isEmpty()) break;

            Transition eps = optionalTransition.get();

           transitionSet.addAll(bridgeInboundTransitions(eps,transitionSet));
           transitionSet.addAll(bridgeOutBoundTransitions(eps, transitionSet));
           //when target of eps transition is acceptingState , so is source
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