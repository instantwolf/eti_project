package ab1.impl.gruppe_29_prett_fischer_szolderits;

import ab1.NFA;
import ab1.Transition;
import java.util.HashSet;
import java.util.Set;

public class NFAFiniteCheckerStrategy {

    public static boolean isFinite(NFA nfa) {
        Set<String> visited = new HashSet<>();

        for (String state : nfa.getStates()) {
            if (!visited.contains(state)) {
                if (hasCycle(nfa, state, new HashSet<>(), visited)) {
                    return false; // language is infinite
                }
            }
        }

        return true; // language is finite
    }

    private static boolean hasCycle(NFA nfa, String currentState, Set<String> currentPath, Set<String> visited) {
        visited.add(currentState);
        currentPath.add(currentState);

        for (Transition transition : nfa.getTransitions()) {
            if (transition.fromState().equals(currentState)) {
                String nextState = transition.toState();

                if (currentPath.contains(nextState)) {
                    return true; // found a cycle
                }

                if (!visited.contains(nextState) && hasCycle(nfa, nextState, currentPath, visited)) {
                    return true; // recursive call found a cycle
                }
            }
        }

        currentPath.remove(currentState);
        return false;
    }
}