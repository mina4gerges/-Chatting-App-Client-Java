/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * When the server sends "SUBMITNAME" the client replies with the desired screen
 * name. The server will keep sending "SUBMITNAME" requests as long as the
 * client submits screen names that are already in use. When the server sends a
 * line beginning with "NAMEACCEPTED" the client is now allowed to start sending
 * the server arbitrary strings to be broadcast to all chatters connected to the
 * server. When the server sends a line beginning with "MESSAGE" then all
 * characters following this string should be displayed in its message area.
 *
 * @author mina2
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
            "Port (Use Port 5000):", portField
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
            if ((this.machine != null || !this.machine.trim().equals("")) && this.machine.toLowerCase().trim().equals("localhost")) { //if user insert localhost instead of 127.0.0.1
                this.machine = "127.0.0.1";
            }
            if (this.surnom == null || this.machine == null || this.port == null || this.surnom.trim().equals("") || this.machine.trim().equals("") || this.port.trim().equals("")) {//test if all input are not empty

                JOptionPane.showMessageDialog(frame,
                        "One or more field are empty",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                getUserInfo();//when user clicks on "ok" (error msg) --> redisplay panel to enter new information
            } else {
                try {
                    run();
                } catch (IOException ex) {
                    frame.setVisible(true);
                    String errorMsgDisplay = ex.toString().contains("HostException")
                            ? "Machine Is Unavailable"
                            : (ex.toString().contains("ConnectException") ? "Port Is Unavailable" : "Machine / Port Is Unavailable");
                    int erroMsgRes = JOptionPane.showConfirmDialog(frame,
                            errorMsgDisplay,
                            "Error",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.ERROR_MESSAGE
                    );
                    if (erroMsgRes == 0) { //when user clicks on "ok" (error msg) --> redisplay panel to enter new information
                        getUserInfo();
                    }
                }
            }
        } else {//if user clicks on cancel
            exit(0);
        }
    }

    private void run() throws IOException {
        try {
            //port: 5000
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
                } else if (line.startsWith("MESSAGE") && !line.substring(8).equals("")) {//add user msg to the panel
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
