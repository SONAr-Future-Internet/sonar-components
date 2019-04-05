package br.ufu.facom.mehar.sonar.core.util;
public class Pair<F, S> extends java.util.AbstractMap.SimpleImmutableEntry<F, S> {

	private static final long serialVersionUID = 2232968898931501585L;

	public  Pair( F f, S s ) {
        super( f, s );
    }

    public F getFirst() {
        return getKey();
    }

    public S getSecond() {
        return getValue();
    }

    public String toString() {
        return "["+getKey()+","+getValue()+"]";
    }

}