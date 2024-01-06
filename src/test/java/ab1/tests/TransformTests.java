package ab1.tests;

import ab1.NFA;
import ab1.NFAFactory;
import ab1.NFAProvider;
import ab1.Transition;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class TransformTests {
    private final NFAFactory factory = NFAProvider.provideFactory();

    @Test
    public void epsNFAToNFATest() {
        var instance = factory.buildNFA("s0");

        instance.addTransition(new Transition("s0",'a',"s0"));
        instance.addTransition(new Transition("s1",'b',"s1"));
        instance.addTransition(new Transition("s2",'c',"s2"));

        instance.addTransition(new Transition("s0",null,"s1"));
        instance.addTransition(new Transition("s1",null,"s2"));

        instance.addAcceptingState("s2");

        instance.finalizeAutomaton();

       var newNFA =  instance.complement();
       assertTrue(newNFA.getAcceptingStates().isEmpty());
       assertEquals(6, newNFA.getTransitions().size());
       assertTrue(nfaContainsTransition(new Transition("s0",'a',"s0"),newNFA));
       assertTrue(nfaContainsTransition(new Transition("s1",'b',"s1"),newNFA));
       assertTrue(nfaContainsTransition(new Transition("s2",'c',"s2"),newNFA));
       assertTrue(nfaContainsTransition(new Transition("s0",'b',"s1"),newNFA));
       assertTrue(nfaContainsTransition(new Transition("s0",'c',"s2"),newNFA));
       assertTrue(nfaContainsTransition(new Transition("s1",'c',"s2"),newNFA));
    }


    private boolean nfaContainsTransition(Transition transition, NFA nfa){
        return nfa.getTransitions().contains(transition);
    }

    private NFA buildCharStarLanguage(char c) {
        var instance = factory.buildNFA("START");
        instance.addTransition(
                Transition.builder()
                        .fromState("START")
                        .readSymbol(c)
                        .toState("START")
                        .build()
        );
        instance.addAcceptingState("START");
        instance.finalizeAutomaton();

        return instance;
    }

    private NFA buildCharLanguage(char c) {
        var instance = factory.buildNFA("START");
        instance.addTransition(
            Transition.builder()
                .fromState("START")
                .readSymbol(c)
                .toState("ACCEPT")
                .build()
        );
        instance.addAcceptingState("ACCEPT");
        instance.finalizeAutomaton();

        assertTrue(instance.acceptsWord(String.valueOf(c)));

        return instance;
    }
}
