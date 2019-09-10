package Client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.System.exit;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A simple Swing-based client for the chat server. Graphically it is a frame
 * with a text field for entering messages and a textarea to see the whole
 * dialog.
 *
 * The client follows the following Chat Protocol. When the server sends
 * "SUBMITNAME" the client replies with the desired screen name. The server will
 * keep sending "SUBMITNAME" requests as long as the client submits screen names
 * that are already in use. When the server sends a line beginning with
 * "NAMEACCEPTED" the client is now allowed to start sending the server
 * arbitrary strings to be broadcast to all chatters connected to the server.
 * When the server sends a line beginning with "MESSAGE" then all characters
 * following this string should be displayed in its message area.
 */
public class ClientTCP {

    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16, 50);

    String machine;
    String surnom;
    String port;

    /**
     * Constructs the client by laying out the GUI and registering a listener
     * with the textfield so that pressing Return in the listener sends the
     * textfield contents to the server. Note however that the textfield is
     * initially NOT editable, and only becomes editable AFTER the client
     * receives the NAMEACCEPTED message from the server.
     */
    public ClientTCP() {

        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Send on enter then clear to prepare for next message
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
        getUserInfo();//open panel for user to insert name, machine IP and port
    }

    private void getUserInfo() {

        JTextField surnomField = new JTextField(5);
        JTextField machineField = new JTextField(5);
        JTextField portField = new JTextField(5);

        Object[] inputFields = { // Fields to display (user insert values)
            "Surnom :", surnomField,
            "Machine :", machineField,
            "Port :", portField
        };

        int option = JOptionPane.showConfirmDialog(//open confirm modal
                frame, //parent component
                inputFields,//panel
                "Please Enter Your Information",//title
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {//if user clicks on OK
            this.surnom = surnomField.getText();//get name
            this.machine = machineField.getText();//get machine
            this.port = portField.getText();//get port
            try {
                run();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            exit(0);
        }
    }

    private void run() throws IOException {
        try {
            //port: 59001
            Socket socket = new Socket(machine, Integer.parseInt(port));
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);//true for auto flush

            while (in.hasNextLine()) {//waiting for users input
                String line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {//submating
                    String finalString = this.surnom + "~" + this.machine + "~" + this.port;
                    // String finalString = "_connect <" + surnom + "> <" + machine + "> <" + port + ">";
                    out.println(finalString);
                } else if (line.startsWith("NAMEACCEPTED")) {//change title of the panel with the user name
                    this.frame.setTitle("Chatter - " + line.substring(13)); //13 bcz length of NAMEACCEPTED
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {//add user msg to the panel
                    messageArea.append(line.substring(8) + "\n");//8 bcz length of MESSAGE
                }
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        ClientTCP client = new ClientTCP();
    }
}
