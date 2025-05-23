package Client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main menu/lobby window for the chess game client. Handles player name input
 * and connection to the game server.
 */
public class home extends javax.swing.JFrame {

    // Network client instance
    private CClient client;

    private static home instance;

    /**
     * Default constructor that initializes the GUI components
     */
    public home() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectButton = new javax.swing.JButton();
        infromLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        connectButton.setText("Connect");
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });

        infromLabel.setText("Welcome to the Game");

        nameField.setText("Player name");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(177, 177, 177)
                        .addComponent(connectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(161, 161, 161)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                            .addComponent(infromLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(183, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(infromLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 85, Short.MAX_VALUE)
                .addComponent(connectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(108, 108, 108))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event handler for the connect button click Validates player name and
     * attempts connection to server
     *
     * @param evt The action event from button click
     */

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
        // Get and validate player name input
        String playerName = nameField.getText().trim();
        if (playerName.isEmpty() || playerName.equals("Player name")) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Please enter a valid player name!",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Disable button and show connecting status
        connectButton.setEnabled(false);
        connectButton.setText("Connecting...");

        // Try to connect in a separate thread to avoid blocking UI
        new Thread(() -> {
            try {
                // Attempt connection to server (change IP to localhost if running locally)
                client = new CClient("ec2-16-171-10-71.eu-north-1.compute.amazonaws.com", 6000, playerName);
                client.setHomeFrame(this);
                client.Listen(); // Start listening for server messages

            } catch (IOException ex) {
                // Log connection failure
                Logger.getLogger(home.class.getName()).log(Level.SEVERE, null, ex);

                // Show error message and reset button on UI thread
                java.awt.EventQueue.invokeLater(() -> {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Failed to connect to server!\nMake sure the server is running and try again.",
                            "Connection Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);

                    // Reset button to allow retry
                    connectButton.setEnabled(true);
                    connectButton.setText("Connect");
                });
            }
        }).start();

    }//GEN-LAST:event_connectButtonActionPerformed

    /**
     * Called by client when server confirms game start Transitions from lobby
     * to the actual chess game
     *
     * @param playerColor Assigned color for this player ("W" or "B")
     * @param opponentName Name of the opponent player
     */
    public void startGame(String playerColor, String opponentName) {
        // Hide home window as we're starting the game
        this.setVisible(false);

        // Start the chess game with network support on UI thread
        java.awt.EventQueue.invokeLater(() -> {
            NetworkGame game = new NetworkGame(client, playerColor, opponentName);
            game.setVisible(true);
        });
    }

    /**
     * Utility method to reset the connect button if connection fails Called
     * from other parts of the application
     */
    public void resetConnectButton() {
        java.awt.EventQueue.invokeLater(() -> {
            connectButton.setEnabled(true);
            connectButton.setText("Connect");
        });
    }

    /**
     * Override to add cleanup when window is closed Ensures proper
     * disconnection from server
     *
     * @param operation The close operation type
     */
    @Override
    public void setDefaultCloseOperation(int operation) {
        super.setDefaultCloseOperation(operation);

        // Add window listener to handle cleanup on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Clean disconnect from server before exiting
                if (client != null) {
                    client.disconnect();
                }
                System.exit(0);
            }
        });
    }

    public static home getInstance() {
        if (instance == null) {
            instance = new home();
        }
        return instance;
    }

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new home().setVisible(true);
            }
        });
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton connectButton;
    private javax.swing.JLabel infromLabel;
    private javax.swing.JTextField nameField;
    // End of variables declaration//GEN-END:variables
}
