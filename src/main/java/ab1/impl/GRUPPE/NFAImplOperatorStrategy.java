package ab1.impl.GRUPPE;

import ab1.NFA;
import ab1.Transition;


public class NFAImplOperatorStrategy {

    public static NFA union(NFA nfa1, NFA nfa2) {
        String p1 = "prefix1-";
        String p2 = "prefix2-";

        //erstelle neuen startzustand
        NFA result = new NFAImpl("START");

        //copy prefixed states and transitions without final states
        copyStatesAndTransitions(nfa1,result,p1,true);
        copyStatesAndTransitions(nfa2,result,p2,true);

        // insert new final state and create epsilon transitions from all the old final states
        String newEndState = "ACCEPT";
        result.addAcceptingState(newEndState);
        nfa1.getAcceptingStates().stream().map(x-> p1+x).forEach(y ->
                result.addTransition(new Transition(y,null,newEndState)));
        nfa2.getAcceptingStates().stream().map(x-> p2+x).forEach(y ->
                result.addTransition(new Transition(y,null,newEndState)));

        //mit eps - übergangen zu den beiden alten startzuständen
        result.addTransition(new Transition(result.getInitialState(), null, nfa1.getInitialState()));
        result.addTransition(new Transition(result.getInitialState(), null, nfa2.getInitialState()));

        result.finalizeAutomaton();
        return result;
    }

    public static NFA intersection(NFA nfa1, NFA nfa2) {
        //complement von der Vereinigung von complement-Automaten
        NFA compNfa1 = complement(nfa1);
        NFA compNfa2 = complement(nfa2);

        NFA unified = union(compNfa1,compNfa2);
        return complement(unified);
    }

    public static NFA concatenation(NFA nfa1, NFA nfa2) {
        //to avoid conflicts concerning states prefix both
       String prefix = "prefix2-";
       String prefixedStart = prefix+nfa2.getInitialState();

        NFA res = new NFAImpl(nfa1.getInitialState());
        //cannot copy acceptingstates
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

        nfa1.finalizeAutomaton();
        return nfa1;
    }

    public static NFA plusOperator(NFA nfa) {
        NFA l = copyAutomaton(nfa,"");

        NFA l2 = copyAutomaton(nfa,"prefix");
        l2.finalizeAutomaton();

        NFA lStar = kleeneStar(l2);

        l.finalizeAutomaton();
        lStar.finalizeAutomaton();

        return concatenation(l, lStar);
    }

    public static NFA complement(NFA nfa) {
        return null;
        //determinisierung notwendig?
        //1.DFA via Potenzmengenkonstruktion
        //determinisierungsalgorithmus


        //2.tausche end und nicht-endzustände

        //FINALIZE NFA HERE BEFORE RETURNING
    }



    /** Helper methods */
    private static NFA copyAutomaton(NFA from, String prefix){
        String p = (prefix == null? "" : prefix);
        NFA to = new NFAImpl(p+from.getInitialState());

        copyStatesAndTransitions(from,to,p,false);
        return to;
    }

    private static NFA copyStatesAndTransitions(NFA from, NFA to, String prefix, boolean skipAcceptingStates){
        String p = (prefix == null? "" : prefix);

        if(!skipAcceptingStates){
            //"kopiere" alle Endzustände
            from.getAcceptingStates().forEach(x -> to.addAcceptingState(p+x));
        }

        //"kopiere" alle transitionen
        from.getTransitions().forEach(x -> to.addTransition(
                new Transition(p+x.fromState(),x.readSymbol(), p+x.toState())));

        return to;
    }



}
