package enigma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/** Class that represents a complete enigma machine.
 *  @author Lesly Serrano
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        myRotors = new HashMap<>();

        for (Rotor r : allRotors) {
            myRotors.put(r.name(), r);
        }
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return listRotors.get(k);
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        listRotors = new ArrayList<>();
        for (String r : rotors) {
            if (listRotors.contains(r)) {
                throw new EnigmaException("No duplicate rotors");
            } else if (myRotors.containsKey(r)) {
                Rotor rotor = myRotors.get(r);
                listRotors.add(rotor);
            } else {
                throw new EnigmaException("Rotor does not exist");
            }
        }

        if (listRotors.size() != rotors.length) {
            throw new EnigmaException("listRotors wrong size");
        }
        if (!listRotors.get(0).reflecting()) {
            throw new EnigmaException("Leftmost rotor not a reflector");
        }
        if (!listRotors.get(listRotors.size() - 1).rotates()) {
            throw new EnigmaException("Rightmost rotor not moving rotor");
        }

    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        settingChar = new Character[setting.length()];
        for (int i = 0; i < setting.length(); i++) {
            if (!_alphabet.contains(setting.charAt(i))) {
                throw new EnigmaException("Setting not in alphabet");
            }
            settingChar[i] = setting.charAt(i);
        }

        for (int i = 0; i < listRotors.size() - 1; i++) {
            listRotors.get(i + 1).set(settingChar[i]);
        }

    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        advanceRotors = new boolean[listRotors.size()];

        for (int i = listRotors.size() - 1; i > 0; i--) {
            Rotor rightmost = listRotors.get(i);
            Rotor leftRotor = listRotors.get(i - 1);
            if (rightmost.atNotch() && leftRotor.rotates()) {
                advanceRotors[i - 1] = true;
                if (rightmost.rotates()) {
                    advanceRotors[i] = true;
                }
            }
        }
        advanceRotors[_numRotors - 1] = true;
        advanceRotors[0] = false;

        for (int k = 1; k < advanceRotors.length; k++) {
            if (advanceRotors[k]) {
                listRotors.get(k).advance();
            }
        }
    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {
        int result = c;
        for (int i = listRotors.size() - 1; i >= 0; i--) {
            Rotor r = listRotors.get(i);
            result = r.convertForward(result);
        }

        for (int k = 1; k < listRotors.size(); k++) {
            Rotor r = listRotors.get(k);
            result = r.convertBackward(result);
        }
        return result;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String myMessage = "";
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c == ' ') {
                myMessage = myMessage + " ";
                continue;
            }
            int charInt = _alphabet.toInt(c);
            char convChar = _alphabet.toChar(convert(charInt));
            myMessage += convChar;
        }
        return myMessage;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Total number of rotors. */
    private int _numRotors;

    /** Number of pawls. */
    private int _pawls;

    /** HashMap that stores all the rotors. */
    private HashMap<String, Rotor> myRotors;

    /** Stores all rotors in order. */
    private ArrayList<Rotor> listRotors;

    /** Plugboard permutation. */
    private Permutation _plugboard;

    /** Stores all settings in character form. */
    private Character[] settingChar;

    /** Returns true if rotor should be advanced. */
    private boolean[] advanceRotors;

}
