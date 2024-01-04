package ab1.impl.GRUPPE;

import lombok.Builder;
import ab1.NFA;

/**
 * Describes a transition of one character
 *
 * @param currentState  - the state the automaton currently holds
 * @param word - the input that still must be read
 */
@Builder
public record FAConfiguration(String currentState, String word) {
}
