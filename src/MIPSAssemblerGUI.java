import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Created by yousefalabdullah on 5/28/17.
 */
public class MIPSAssemblerGUI extends JFrame {

    private JLabel lblInputFile;
    private JTextField txtInputFile;
    private JButton btnInputFile;
    private JPanel pnlInput;

    private JLabel lblOutputFile;
    private JTextField txtOutputFile;
    private JButton btnOutputFile;
    private JPanel pnlOutput;

    private JComboBox<String> cmbOutputFormat;
    private JButton btnAssemble;
    private JPanel pnlControl;

    public MIPSAssemblerGUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new FlowLayout());

        lblInputFile = new JLabel("Input File:");
        txtInputFile = new JTextField();
        txtInputFile.setColumns(20);
        btnInputFile = new JButton("Browse ...");

        pnlInput = new JPanel();
        pnlInput.setLayout(new FlowLayout());

        pnlInput.add(lblInputFile);
        pnlInput.add(txtInputFile);
        pnlInput.add(btnInputFile);

        txtInputFile.setEnabled(false);
        btnInputFile.addActionListener((e) ->
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int value = chooser.showOpenDialog(this);
            if (value == JFileChooser.APPROVE_OPTION) {
                txtInputFile.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        add(pnlInput);

        lblOutputFile = new JLabel("Output File:");
        txtOutputFile = new JTextField();
        txtOutputFile.setColumns(20);
        btnOutputFile = new JButton("Browse ...");

        pnlOutput = new JPanel();
        pnlOutput.setLayout(new FlowLayout());

        txtOutputFile.setEnabled(false);
        btnOutputFile.addActionListener((e) ->
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int value = chooser.showSaveDialog(this);
            if (value == JFileChooser.APPROVE_OPTION) {
                txtOutputFile.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        pnlOutput.add(lblOutputFile);
        pnlOutput.add(txtOutputFile);
        pnlOutput.add(btnOutputFile);

        add(pnlOutput);

        pnlControl = new JPanel();
        pnlControl.setLayout(new FlowLayout());

        cmbOutputFormat = new JComboBox<String>(new String[]{"Text File", "mif File"});
        btnAssemble = new JButton("Assemble");

        btnAssemble.addActionListener((e) ->
        {
            String inputFile = txtInputFile.getText();
            String outputFile = txtOutputFile.getText();

            if (inputFile.isEmpty() || outputFile.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Check input and output files", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Scanner scanner = new Scanner(new File(inputFile));
                String content = scanner.useDelimiter("\\Z").next();
                scanner.close();
                MIPSAssembler mipsAssembler = new MIPSAssembler(content);
                String[] machineCode = mipsAssembler.Compile();
                Object selectedItem = cmbOutputFormat.getSelectedItem();
                if (selectedItem instanceof String) {
                    String selectedString = (String) selectedItem;
                    switch (selectedString) {
                        case "Text File": {
                            outputTextFile(outputFile, machineCode);
                        }
                        break;

                        case "mif File": {
                            outputMIFFile(outputFile, machineCode);
                        }
                        break;
                    }
                } else {
                    return;
                }

                JOptionPane.showConfirmDialog(null, "Done assembling !");
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                System.exit(1);
            }

        });

        pnlControl.add(cmbOutputFormat);
        pnlControl.add(btnAssemble);

        add(pnlControl);

        setSize(450, 150);
    }

    public static void main(String args[]) {
        MIPSAssemblerGUI gui = new MIPSAssemblerGUI();
        gui.setVisible(true);
    }

    private void outputMIFFile(String outputFile, String[] machineCode) {
        try {
            PrintWriter writer = new PrintWriter(new File(outputFile + ".mif"));
            int depth = Integer.parseInt(JOptionPane.showInputDialog("Enter the number of rows:"));
            int width = 16;
            writer.print(String.format("DEPTH = %d;\nWIDTH = %d;\nADDRESS_RADIX = DEC;\nDATA_RADIX = BIN;\n" +
                    "CONTENT\nBEGIN\n", depth, width));
            for (int i = 0; i < depth; i++) {
                if (i < machineCode.length) {
                    writer.print(String.format("[%d]:\t%s;\n", i, machineCode[i]));
                } else {
                    writer.print(String.format("[%d..%d]:\t%s;\n", i, (depth - 1), "0000000000000000"));
                    break;
                }
            }
            writer.println("END;");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void outputTextFile(String outputFile, String[] machineCode) {
        try {
            PrintWriter writer = new PrintWriter(new File(outputFile + ".txt"));
            for (String inst : machineCode) {
                writer.println(inst);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
