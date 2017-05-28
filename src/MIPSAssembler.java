import java.util.HashMap;

/**
 * Created by yousefalabdullah on 3/8/17.
 *
 * This is a MIPS 16-bit assembler for KU CPE469 Lab.
 *
 */
public class MIPSAssembler
{


//    public static void main(String args[]) throws FileNotFoundException {
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Enter the input file:");
//        String inputFilename = scanner.next();
//        String outputFilename = inputFilename.substring(0, inputFilename.lastIndexOf('.')) + ".machine";
//        scanner.close();
//        File inputFile = new File(inputFilename);
//        if (!inputFile.exists())
//        {
//            System.err.println("File not found");
//            System.exit(1);
//        }
//
//        String content = new Scanner(inputFile).useDelimiter("\\Z").next();
//        File outputFile = new File(outputFilename);
//        MIPSAssembler mipsAssembler = new MIPSAssembler(content);
//        PrintWriter writer = new PrintWriter(outputFile);
//        writer.print(mipsAssembler.compile());
//        writer.close();
//    }


    private String[] mipsCode;
    private String[] machineCode;

    private HashMap<String, Integer>  labelsAddress;


    /**
     * Constructor For the assembler class
     *
     * @param mipsCode
     */
    public MIPSAssembler(String mipsCode) {
        //this wrap up construct was added for convince
        this(mipsCode.split("\\r?\\n"));
    }


    public MIPSAssembler(String[] mipsCode) {
        //Initializing the global variables
        this.mipsCode = mipsCode;
        machineCode = new String[mipsCode.length];
        //Creating a map for the labels
        labelsAddress = new HashMap<String, Integer>();
    }


    /**
     * This function is used to clean the code from any heading or trailing spaces
     */
    private void cleanCode() {
        for (int i = 0; i < mipsCode.length; ++i) {
            mipsCode[i] = mipsCode[i].trim();
        }
    }

    /**
     * This function search for label's declarations and save their addresses then removes them from the code.
     */
    private void fetchLabels() {
        for (int i = 0; i < mipsCode.length; ++i) {
            if (mipsCode[i].contains(":")) {
                String label = mipsCode[i].substring(0 , mipsCode[i].indexOf(':'));
                mipsCode[i] = mipsCode[i].substring(mipsCode[i].indexOf(":")+1, mipsCode[i].length()).trim();
                int address = i;
                if (labelsAddress.containsKey(label)) {
                    throw new RuntimeException("Label Already Exist");
                }
                labelsAddress.put(label, address);
            }
        }
    }


    /**
     * Search for label used for jump addresses and replace the lable with actual value
     * depending on the instruction.
     * (e.g if it's jmp it uses absolute address, while beq uses relative address)
     */
    private void replaceLabelsWithAddresses() {
        for (int i = 0; i < mipsCode.length; ++i) {
            for (String key : labelsAddress.keySet()) {
                if (mipsCode[i].contains(key) && !mipsCode[i].contains(key + ":")) {
                    if (mipsCode[i].startsWith("jmp")
                            || mipsCode[i].startsWith("jal")
                            || mipsCode[i].startsWith("ret")) {
                        int address = labelsAddress.get(key);
                        mipsCode[i] = mipsCode[i].replace(key, String.valueOf(address));
                    } else {
                        int relativeAddress = labelsAddress.get(key) - (i + 1);
                        mipsCode[i] = mipsCode[i].replace(key, String.valueOf(relativeAddress));
                    }
                }
            }
        }
    }

    /**
     * Get a register binary address by accesssing it with $ in front of the number
     * @param s
     * The register you want to get
     * @return
     * The binary address of that register
     */
    private String getRegister(String s) {
        s = s.replace("$", "");
        switch (s) {
            case "0": {return "000";}
            case "1": {return "001";}
            case "2": {return "010";}
            case "3": {return "011";}
            case "4": {return "100";}
            case "5": {return "101";}
            case "6": {return "110";}
            case "7": {return "111";}

            default: {
                throw new RuntimeException("Unknown Register at " + s);
            }
        }
    }

    /**
     * Assemble the given instruction and put the code in the global array
     * @param operation
     * instruction name
     * @param index
     * instruction address
     */
    private void rtype(String operation, int index) {
        String opCode = "0001";
        String[] values = mipsCode[index].substring(mipsCode[index].indexOf(' ') + 1, mipsCode[index].length()).split(",");
        String rd = getRegister(values[0]);
        String rs = getRegister(values[1]);
        String rt  = getRegister(values[2]);
        String functionField = "";
        switch (operation) {
            case "and": {
                functionField = "000";
            } break;
            case "or": {
                functionField = "001";
            } break;
            case "xor": {
                functionField = "010";
            } break;
            case "not": {
                functionField = "011";
            } break;
            case "srl": {
                functionField = "100";
            } break;
            case "add": {
                functionField = "110";
            } break;
            case "sub": {
                functionField = "111";
            } break;
        }
        machineCode[index] = opCode + rs + rt + rd + functionField;
    }

    /**
     * Convert an integer to 6-bits binary
     * @param number
     * @return
     */
    private String getImmediate6bits(int number) {
        String s = Integer.toBinaryString(number) + "";
        if (s.length() > 6) {
            s = s.substring(s.length() - 7, s.length());
        } else if (s.length() < 6) {
            s = String.format("%06d", Integer.parseInt(s));
        }
        return s;
    }

    /**
     * Convert an integer to 12-bits binary
     * @param number
     * @return
     */
    private String getImmediate12bits(int number) {
        String s = Integer.toBinaryString(number) + "";
        if (s.length() > 12) {
            s = s.substring(s.length() - 7, s.length());
        } else if (s.length() < 12) {
            s = String.format("%012d", Integer.parseInt(s));
        }
        return s;
    }


    /**
     * Assemble the given instruction and put the code in the global array
     *
     * @param operation instruction name
     * @param index     instruction address
     */
    private void itype(String operation, int index) {
        String opCode = "";
        String rt = "";
        String rs = "";
        String immediate = "";
        String[] values = mipsCode[index].substring(mipsCode[index].indexOf(' ') + 1, mipsCode[index].length()).split(",");
        switch (operation) {
            case "addi": {
                opCode = "0010";
                rt = getRegister(values[0]);
                rs = getRegister(values[1]);
                immediate = getImmediate6bits(Integer.parseInt(values[2]));
            } break;
            case "lw": {
                opCode = "0011";
                rt = getRegister(values[0]);
                rs = getRegister(values[1].substring(values[1].indexOf('(') + 1, values[1].indexOf(')')));
                immediate = getImmediate6bits(Integer.parseInt(values[1].substring(0, values[1].indexOf('('))));
            } break;
            case "sw": {
                opCode = "0100";
                rt = getRegister(values[0]);
                rs = getRegister(values[1].substring(values[1].indexOf('(') + 1, values[1].indexOf(')')));
                immediate = getImmediate6bits(Integer.parseInt(values[1].substring(0, values[1].indexOf('('))));
            } break;
            case "beq": {
                opCode = "0101";
                rt = getRegister(values[0]);
                rs = getRegister(values[1]);
                immediate = getImmediate6bits(Integer.parseInt(values[2]));
            } break;
        }

        machineCode[index] = opCode + rs + rt + immediate;
    }

    /**
     * Assemble the given instruction and put the code in the global array
     *
     * @param operation instruction name
     * @param index     instruction address
     */
    private void jtype(String operation, int index) {
        String opCode = "";
        String immediate = "";

        switch (operation) {
            case "jmp": {
                opCode = "0110";
                immediate = getImmediate12bits(Integer.parseInt(mipsCode[index].substring(mipsCode[index].indexOf(' ') + 1), mipsCode[index].length()));
            } break;
            case "jal": {
                opCode = "0111";
                int x = Integer.parseInt(mipsCode[index].substring(mipsCode[index].indexOf(' ') + 1, mipsCode[index].length()));
                immediate = getImmediate12bits(x);
            } break;
            case "ret": {
                opCode = "1000";
                immediate = getImmediate12bits(0);
            } break;
        }
        machineCode[index] = opCode + immediate;
    }

    /**
     * Assemble the given instruction and put the code in the global array
     *
     * @param operation instruction name
     * @param index     instruction address
     */
    private void nop(String operation, int index) {
        machineCode[index] = "0000000000000000";
    }

    /**
     * Converting the instructions to binary.
     */
    private void encode() {
        for (int i = 0; i < mipsCode.length; ++i) {
            String op = "";
            if (mipsCode[i].equalsIgnoreCase("ret") || mipsCode[i].equalsIgnoreCase("nop")) {
                op = mipsCode[i];
            } else {
                op = mipsCode[i].substring(0, mipsCode[i].indexOf(' '));
            }
            switch (op) {
                case "and":
                case "or":
                case "xor":
                case "not":
                case "srl":
                case "add":
                case "sub": {
                    rtype(op, i);
                } break;

                case "addi":
                case "lw":
                case "sw":
                case "beq": {
                    itype(op, i);
                } break;

                case "jmp":
                case "jal":
                case "ret": {
                    jtype(op, i);
                } break;

                case "nop": {
                    nop(op, i);
                }
                break;
                default: {
                    throw new RuntimeException("Unknown instruction at " + (i+1));
                }
            }
        }
    }


    /**
     * @return
     */
    public String compile() {
        cleanCode();
        fetchLabels();
        replaceLabelsWithAddresses();
        encode();

        String output = "";
        for (String s : machineCode) {
            output += s + "\n";
        }
        return output;
    }

    /**
     *
     * @return
     */
    public String[] Compile() {
        compile();
        return machineCode;
    }

}
