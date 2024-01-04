package ab1.impl.GRUPPE;

import ab1.FinalizedStateException;
import ab1.NFA;
import ab1.Transition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Implicit assumptions:
 *  - the alphabet is every single Character of the Character Datatype
 *    class including null (for eps) <= Transition-Record
 *      - it follows that: every String of length 1 including space
 *        is an element of the alphabet and hence is not a valid name
 *        for any state (diff datatype is not distinguished enough)
 *  - star1Test => in the context of strings , empty means epsilon (since a character cannot be
 *    empty , but a string can)
 *      - it follows that:
 *              every state must be of length >1
 *      -       no String can be NULL since epsilon is empty String
 *  - finalization: according to the tests (finalizeTests) the following applies:
 *          - after initialization an automaton is not finalized
 *          - an automaton is finalized once finalize method is called
 *          - a non-finalized automaton can be structurally modified (states /transitions) , a finalized one cannot
 *          - mathematical operations require all involved automatons to be finalized (callee as well as input parameters)
 *  - adding states:
 *          - not explicitly (publicly) implemented because tests require implicit adding of states through addTransition
 */
public class NFAImpl implements NFA {

    /** Member Var */
    Set<String> states;
    Set<Transition> transitions;
    Set<String> acceptingStates;
    String initialState;
    boolean isFinalized;

    /** initialization */
    public NFAImpl(String startState) {
        initMembers();
        setInitialState(startState);
    }

    private void initMembers(){
        states = new HashSet<>();
        transitions = new HashSet<>();
        acceptingStates = new HashSet<>();
        isFinalized = false;
    }


    /** getter / setter */
    @Override
    public boolean isFinalized() {
        return this.isFinalized;
    }

    @Override
    public Set<String> getStates() {
        return this.states;
    }

    @Override
    public Collection<Transition> getTransitions() {
        return this.transitions;
    }

    @Override
    public Set<String> getAcceptingStates() {
        return this.acceptingStates;
    }

    @Override
    public String getInitialState() {
        return this.initialState;
    }

    @Override
    public void addTransition(Transition transition) throws FinalizedStateException {
        throwExceptionIfFinalizedDeviates(false);
        checkValidTransition(transition);
        this.transitions.add(transition);
    }

    @Override
    public void addAcceptingState(String state) throws FinalizedStateException {
        throwExceptionIfFinalizedDeviates(false);
        this.acceptingStates.add(state);
    }

    @Override
    public void finalizeAutomaton() {
        this.isFinalized = true;
    }


    /** operations on NFAs */
    @Override
    public NFA union(NFA other) throws FinalizedStateException {
        throwExceptionIfFinalizedDeviates(other,true);
        return NFAImplOperatorStrategy.union(this,other);
    }

    @Override
    public NFA intersection(NFA other) throws FinalizedStateException {
        throwExceptionIfFinalizedDeviates(other,true);
        return NFAImplOperatorStrategy.concatenation(this,other);
    }

    @Override
    public NFA concatenation(NFA other) throws FinalizedStateException {
        throwExceptionIfFinalizedDeviates(other,true);
        return NFAImplOperatorStrategy.concatenation(this,other);
    }

    @Override
    public NFA kleeneStar() throws FinalizedStateException {
        throwExceptionIfFinalizedDeviates(true);
        return NFAImplOperatorStrategy.kleeneStar(this);
    }

    @Override
    public NFA plusOperator() throws FinalizedStateException {
        throwExceptionIfFinalizedDeviates(true);
        return NFAImplOperatorStrategy.plusOperator(this);
    }

    @Override
    public NFA complement() throws FinalizedStateException {
        throwExceptionIfFinalizedDeviates(true);
        return NFAImplOperatorStrategy.complement(this);
    }


    /** status methods */
    @Override
    public boolean isFinite() {
        return false;
    }

    @Override
    public boolean acceptsWord(String word) {
        return NFAImplAcceptsWordStrategy.acceptsWord(word,this);

    }



    /** Helper Methods */
    private void checkValidState(String state) throws IllegalArgumentException{
        if(state == null || state.length() < 2){
            throw new IllegalArgumentException("State cannot be null , empty string or single character");
        }
    }

    private void setInitialState(String state) throws IllegalArgumentException, FinalizedStateException{
        throwExceptionIfFinalizedDeviates(false);
        addState(state);
        this.initialState = state;
    }

    public void addState(String state) throws IllegalArgumentException, FinalizedStateException{
        throwExceptionIfFinalizedDeviates(false);
        checkValidState(state);
        this.states.add(state);
    }

    public void throwExceptionIfFinalizedDeviates(boolean requiredFinalizedState) throws FinalizedStateException{
        if(this.isFinalized != requiredFinalizedState){
            throw new FinalizedStateException(); //not allowed when NFA is finalized
        }
    }
    public void throwExceptionIfFinalizedDeviates(NFA other, boolean requiredFinalizedState) throws FinalizedStateException{
        throwExceptionIfFinalizedDeviates(requiredFinalizedState);
        if(other.isFinalized() != requiredFinalizedState){
            throw new FinalizedStateException(); //not allowed when NFA is finalized
        }
    }

    private void checkValidTransition(Transition test){
        checkValidState(test.fromState());
        checkValidState(test.toState());
        //the character in the transition cannot be invalid since there arent any restrictions from the assignment
        //and we arent allowed to just make some up
        //can be null (eps) or any character

        addState(test.fromState());
        addState(test.toState());
    }


}
