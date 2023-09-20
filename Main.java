package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Lesly Serrano
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine m = readConfig();
        String settings;
        String msg;
        String line;

        if (!_input.hasNext("\\*")) {
            throw new EnigmaException("Input not starting with settings.");
        }

        while (_input.hasNext()) {
            if (_input.hasNext("\\*")) {
                settings = _input.nextLine();
                if (settings.equals("")) {
                    settings = _input.nextLine();
                    System.out.println();
                }
                setUp(m, settings);
            } else {
                msg = _input.nextLine();
                printMessageLine(m.convert(msg));
            }
        }

    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alpha = _config.nextLine();
            int numRotors = _config.nextInt();
            int pawls = _config.nextInt();

            allRotors = new ArrayList<>();
            _alphabet = new Alphabet(alpha);
            while (_config.hasNext()) {
                Rotor r = readRotor();
                allRotors.add(r);
            }
            return new Machine(_alphabet, numRotors, pawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String rotorName = _config.next();
            String setting = _config.next();
            String cycle = "";

            while (_config.hasNext("\\(.*")) {
                cycle = cycle + _config.next("\\(.*");
            }
            Permutation p = new Permutation(cycle, _alphabet);

            char rotorType = setting.charAt(0);
            String notches = setting.substring(1);
            if (rotorType == 'M') {
                return new MovingRotor(rotorName, p, notches);
            } else if (rotorType == 'N') {
                return new Rotor(rotorName, p);
            } else {
                return new Reflector(rotorName, p);
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        settings = settings.replace("*", "");
        settings = settings.trim();
        String[] splits = settings.split("\\s");
        int numRotors = M.numRotors();
        String[] rotorName = new String[numRotors];
        String plugCycles = "";
        String initSettings = "";
        Permutation p;

        for (int i = 0; i <= numRotors; i++) {
            if (i == numRotors) {
                initSettings = splits[i];
            } else {
                rotorName[i] = splits[i];
            }
        }

        if (initSettings.length() != numRotors - 1) {
            throw new EnigmaException("String SETTING wrong size");
        }

        if (splits.length > numRotors + 1) {
            for (int i = numRotors + 1; i < splits.length; i++) {
                plugCycles = plugCycles + splits[i];
            }
            p = new Permutation(plugCycles, _alphabet);
        } else {
            p = new Permutation("", _alphabet);
        }
        M.setPlugboard(p);
        M.insertRotors(rotorName);
        M.setRotors(initSettings);
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        msg = msg.replace(" ", "");
        msg = msg.trim();
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (i != 0 && i % 5 == 0) {
                _output.print(" ");
            }
            _output.print(c);
        }
        System.out.println();
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;

    /** Stores all rotors. */
    private ArrayList<Rotor> allRotors;
}
