package enigma;


import java.util.ArrayList;


/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Lesly Serrano
 */
class Permutation {

    /**
     * Set this Permutation to that specified by CYCLES, a string in the
     * form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     * is interpreted as a permutation in cycle notation.  Characters in the
     * alphabet that are not included in any cycle map to themselves.
     * Whitespace is ignored.
     */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        myCycles = new ArrayList<>();
        _derangement = true;

        cycles = cycles.replace("(", " ");
        cycles = cycles.replace(")", " ");
        cycles = cycles.trim();

        addCycle(cycles);
    }

    /**
     * Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     * c0c1...cm.
     */
    private void addCycle(String cycle) {
        String[] splits = cycle.split(" ");
        ArrayList<Character> letters = new ArrayList<>();

        for (String split : splits) {
            if (split.length() == 1) {
                _derangement = false;
            }
            for (int k = 0; k < split.length(); k++) {
                char c1 = split.charAt(k);
                if (letters.contains(c1)) {
                    throw new EnigmaException("No duplicates in cycle.");
                }
                letters.add(c1);
                if (!_alphabet.contains(c1)) {
                    throw new EnigmaException("Item not in alphabet.");
                }
                if (split.indexOf(' ') != -1) {
                    throw new EnigmaException("Whitespace present in cycle.");
                }
            }
            cycle = cycle.trim();
            if (split.equals("")) {
                continue;
            } else {
                myCycles.add(split);
            }
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int permIndex = p;
        wrap(p);
        char c = _alphabet.toChar(p);

        for (String str : myCycles) {
            int index = str.indexOf(c);
            if (index != -1) {
                if (index == str.length() - 1) {
                    permIndex = _alphabet.toInt(str.charAt(0));
                } else {
                    permIndex = _alphabet.toInt(str.charAt(index + 1));
                }
                break;
            }
            if (_alphabet.contains(c)) {
                permIndex = _alphabet.toInt(c);
                _derangement = false;
            }
        }
        return permIndex;
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int invIndex = c;
        wrap(c);
        char ch = _alphabet.toChar(c);

        for (String str : myCycles) {
            int index = str.indexOf(ch);
            if (index != -1) {
                if (index == 0) {
                    invIndex = _alphabet.toInt(str.charAt(str.length() - 1));
                } else {
                    invIndex = _alphabet.toInt(str.charAt(index - 1));
                }
                break;
            }
            if (_alphabet.contains(ch)) {
                invIndex = _alphabet.toInt(ch);
                _derangement = false;
            }
        }
        return invIndex;
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int intChar = _alphabet.toInt(p);
        int permChar = permute(intChar);
        return _alphabet.toChar(permChar);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int intChar = _alphabet.toInt(c);
        int invChar = invert(intChar);
        return _alphabet.toChar(invChar);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        return _derangement;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** The cycles of this permutation. */
    private ArrayList<String> myCycles;

    /** Returns true if cycle is a derangement. */
    private boolean _derangement;

}
