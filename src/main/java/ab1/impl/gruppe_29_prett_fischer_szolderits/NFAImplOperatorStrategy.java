package ab1.impl.gruppe_29_prett_fischer_szolderits;

import ab1.NFA;
import ab1.Transition;

import java.util.Collection;


public class NFAImplOperatorStrategy {

    public static NFA union(NFA nfa1, NFA nfa2) {
        String p1 = "prefix1-";
        String p2 = "prefix2-";

        //erstelle neuen startzustand
        NFA result = new NFAImpl("START");

        //copy prefixed states and transitions without final states
         copyStatesAndTransitions(nfa1,result,p1,true);
         copyStatesAndTransitions(nfa2,result,p2,true);

        nfa1.getAcceptingStates().stream().map(x-> p1+x).forEach(result::addAcceptingState);
        nfa2.getAcceptingStates().stream().map(x-> p2+x).forEach(result::addAcceptingState);

        //mit eps - übergangen zu den beiden alten startzuständen
        result.addTransition(new Transition(result.getInitialState(), null, p1+nfa1.getInitialState()));
        result.addTransition(new Transition(result.getInitialState(), null, p2+nfa2.getInitialState()));

        result.finalizeAutomaton();
        return result;
    }

    public static NFA intersection(NFA nfa1, NFA nfa2) {
        //complement von der Vereinigung von complement-Automaten
        NFA compNfa1 = complement(nfa1,true);
        NFA compNfa2 = complement(nfa2,true);

        NFA unified = union(compNfa1,compNfa2);
        return complement(unified,false);
    }

    public static NFA concatenation(NFA nfa1, NFA nfa2) {
        //to avoid conflicts concerning states prefix both
       String prefix = "prefix2-";
       String prefixedStart = prefix+nfa2.getInitialState();

        NFA res = new NFAImpl(nfa1.getInitialState());
        //cannot copy accepting-states
        copyStatesAndTransitions(nfa1, res,"",true);
        //add eps transitions to second start state
        nfa1.getAcceptingStates().forEach(x -> res.addTransition(
                new Transition(x,null,prefixedStart)
        ));

        //copy states and transitions of the second automaton
        copyStatesAndTransitions(nfa2, res, prefix, false);

        res.finalizeAutomaton();

        return res;
    }

    public static NFA kleeneStar(NFA nfa) {
        NFA nfa1 = copyAutomaton(nfa, "");
        //füge zusätzliche Epsilon-Transitionen ein
        String start = nfa.getInitialState();
        nfa.getAcceptingStates().forEach(x -> nfa1.addTransition(new Transition(x, null,start)));
        //setze den StartZustand als EndZustand
        nfa1.addAcceptingState(start);

        nfa1.finalizeAutomaton();
        return nfa1;
    }

    public static NFA plusOperator(NFA nfa) {
        NFA l = copyAutomaton(nfa,"");

        NFA l2 = copyAutomaton(nfa,"prefix-");
        l2.finalizeAutomaton();

        NFA lStar = kleeneStar(l2);

        l.finalizeAutomaton();
        lStar.finalizeAutomaton();

        return concatenation(l, lStar);
    }

    public static NFA complement(NFA epsNFA, boolean skipTotalization) {
        //everytime a FA is used for calculations , it needs to be de-determined
        NFA detFA = NFATransformator.nfaToDeterministicNFA(epsNFA,skipTotalization);
        Collection<String> oldFinalStates = detFA.getAcceptingStates();

        NFA newNFA = new NFAImpl(detFA.getInitialState());
        copyStatesAndTransitions(detFA,newNFA,"",true);

        detFA.getStates().stream()
                .filter(x -> !oldFinalStates.contains(x))
                .forEach(newNFA::addAcceptingState);


        newNFA.finalizeAutomaton();
        return newNFA;
        //FINALIZE NFA HERE BEFORE RETURNING
    }


    /** Helper methods */
    public static NFA copyAutomaton(NFA from, String prefix){
        String p = (prefix == null? "" : prefix);
        NFA to = new NFAImpl(p+from.getInitialState());

        copyStatesAndTransitions(from,to,p,false);
        return to;
    }

    public static void copyStatesAndTransitions(NFA from, NFA to, String prefix, boolean skipAcceptingStates){
        String p = (prefix == null? "" : prefix);

        if(!skipAcceptingStates){
            //"kopiere" alle Endzustände
            from.getAcceptingStates().forEach(x -> to.addAcceptingState(p+x));
        }

        //"kopiere" alle transitionen
        from.getTransitions().forEach(x -> to.addTransition(
                new Transition(p+x.fromState(),x.readSymbol(), p+x.toState())));

    }



}
