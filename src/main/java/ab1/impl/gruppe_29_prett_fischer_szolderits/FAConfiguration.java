package ab1.impl.gruppe_29_prett_fischer_szolderits;

import lombok.Builder;

/**
 * Describes a transition of one character
 *
 * @param currentState  - the state the automaton currently holds
 * @param word - the input that still must be read
 */
@Builder
public record FAConfiguration(String currentState, String word) {
}
