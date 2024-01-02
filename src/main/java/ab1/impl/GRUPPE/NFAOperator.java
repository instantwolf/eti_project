package ab1.impl.GRUPPE;

import ab1.NFA;

public interface NFAOperator {

    NFA union(NFA nfa1, NFA nfa2);

    NFA intersection(NFA nfa1, NFA nfa2);
    NFA concatenation(NFA nfa1, NFA nfa2);

    NFA kleeneStar(NFA nfa);

    NFA plusOperator(NFA nfa);

    NFA complement(NFA nfa);

}

