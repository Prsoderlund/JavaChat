import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;

public class ChatGUI extends JFrame {
    private static final String MULTICAST_IP = "234.235.236.237";
    private static final int PORT = 12540;

    private MulticastSocket multicastSocket;
    private InetAddress multicastGroup;
    private JTextArea chatTextArea;
    private JTextField messageTextField;
    private String nick;

    public ChatGUI() { //  ansvarar för att initiera det grafiska användargränssnittet för chatten genom att anropa tre metoder: initializeNick(), initializeMulticastSocket(), createAndShowGUI()
        initializeNick();
        initializeMulticastSocket();
        createAndShowGUI();
    }

    private void initializeNick() { //definierar en func som kallas initializeNick som gör så att användaren måste ange ett namn och initierar nickvariabeln
        nick = JOptionPane.showInputDialog(this, "Enter your nickname:");
        if (nick == null || nick.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nickname cannot be empty. Exiting...");
            System.exit(0);
        }
    }

    private void initializeMulticastSocket() { //koden definierar en func som kallas initializeMulticastSocket som initierar en mcsocket för nätverkskommunikation.

        try {
            multicastGroup = InetAddress.getByName(MULTICAST_IP);
            multicastSocket = new MulticastSocket(PORT);
            multicastSocket.joinGroup(multicastGroup);

            Thread listenerThread = new Thread(new MessageListener());
            listenerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void createAndShowGUI() { //Denna koden definierar en  func som kallas createAndShowGUI som ställer in och visar ett grafiskt användargränssnitt (GUI) för enchatten.

        setTitle("Class Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatTextArea = new JTextArea();
        chatTextArea.setEditable(false);

        messageTextField = new JTextField();
        messageTextField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String message = messageTextField.getText();
                if (!message.isEmpty()) {
                    sendMessage(nick + ": " + message);
                    messageTextField.setText("");

                }
            }
        });

        JButton disconnectButton = new JButton("Disconnect"); //ställer in det grafiska användargränssnittet (GUI) för en chattapplikation,  en disconnect-knapp, en textfield och messagetextfield.
        disconnectButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                disconnect();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chatTextArea, BorderLayout.CENTER);
        panel.add(messageTextField, BorderLayout.SOUTH);
        panel.add(disconnectButton, BorderLayout.NORTH);

        add(panel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

    }

    private void sendMessage(String message) { //Syftet med denna func är att skicka ett meddelande över ett nätverk med hjälp av udp multicast
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastGroup, PORT);
            multicastSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() { //disconnect func som visar hur man disconectar från mcsocketen
        try {
            multicastSocket.leaveGroup(multicastGroup);
            multicastSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);

    }

    private class MessageListener implements Runnable {

        public void run() {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (true) {
                    multicastSocket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            chatTextArea.append(message + "\n"); // meddelandet på skärmen + ny rad
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ChatGUI();
            }
        });
    }
}

