package ab1.impl.GRUPPE;

import ab1.NFA;

/**
 *  Für alle binären Operationen gilt:
 *  - Die Zustandsmengen Q1 und Q2 müssen disjunkt (gemacht) werden.
 *  - alle Transitionen müssen entsprechend abgeändert werden.
 *  -  das Alphabet ist implizit definiert und kann disjunkt oder  ident sein -> egal
 *   -
 */
public class NFAOperatorStrategy implements NFAOperator{
    @Override
    public NFA union(NFA nfa1, NFA nfa2) {
        return null;
        //erstelle neuen startzustand
        //mit eps - übergangen zu den beiden alten startzuständen
        //jeder zustand im neuen automaten wird gepräfixed

    }

    @Override
    public NFA intersection(NFA nfa1, NFA nfa2) {
        return null;
        //vereinigung von complement automaten
    }

    @Override
    public NFA concatenation(NFA nfa1, NFA nfa2) {
        return null;
        //eps transition von jedem endzustand auf den zweiten nfa
    }

    @Override
    public NFA kleeneStar(NFA nfa) {
        return null;
        //von jedem Endzustand eps transition zu start
        //startzustand ist nun endzustand
    }

    @Override
    public NFA plusOperator(NFA nfa) {
        return null;
        //concatenation mit Stern automat
    }

    @Override
    public NFA complement(NFA nfa) {
        return null;
        //determinisierung notwendig?
        //tausche end und nicht-endzustände
    }
}
