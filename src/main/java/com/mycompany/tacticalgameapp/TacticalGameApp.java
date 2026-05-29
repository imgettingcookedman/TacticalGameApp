package com.mycompany.tacticalgameapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TacticalGameApp {
    private static Squad mainArmy = new Squad();
    private static JTextArea logArea;
    private static JLabel turnLabel;
    private static JLabel armyPowerLabel;
    private static DefaultListModel<String> unitListModel;
    private static List<GameEntity> trackedUnits = new ArrayList<>();

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        // Create main frame
        JFrame frame = new JFrame("Tactical Game Engine Framework - Theme C");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 550);
        frame.setLayout(new BorderLayout(10, 10));

        // --- TOP PANEL: Singleton Control ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Pattern 1: Singleton (Game Control)"));
        
        GameEngine engine = GameEngine.getInstance();
        turnLabel = new JLabel("Current Engine Turn: 1   ");
        turnLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JButton nextTurnBtn = new JButton("Advance Turn (Singleton)");
        nextTurnBtn.addActionListener(e -> {
            engine.nextTurn();
            turnLabel.setText("Current Engine Turn: " + engine.getCurrentTurn() + "   ");
            logArea.append(">> GameEngine Singleton shifted system state to Turn " + engine.getCurrentTurn() + "\n");
        });
        
        topPanel.add(turnLabel);
        topPanel.add(nextTurnBtn);
        frame.add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL: Composite & Decorator Workspace ---
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        
        // Left Column: Hierarchy Management
        JPanel leftColumn = new JPanel(new BorderLayout(5, 5));
        leftColumn.setBorder(BorderFactory.createTitledBorder("Pattern 2 & 3: Army Composition & Upgrades"));
        
        unitListModel = new DefaultListModel<>();
        JList<String> unitList = new JList<>(unitListModel);
        JScrollPane listScrollPane = new JScrollPane(unitList);
        leftColumn.add(listScrollPane, BorderLayout.CENTER);
        
        // Control buttons for structural changes
        JPanel actionButtonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JButton addSoldierBtn = new JButton("Add Soldier (Leaf)");
        JButton addSquadBtn = new JButton("Add Squad (Composite)");
        JButton upgradeBtn = new JButton("Apply Upgrade (Decorator)");
        JButton attackBtn = new JButton("EXECUTE ALL ATTACKS");
        
        actionButtonPanel.add(addSoldierBtn);
        actionButtonPanel.add(addSquadBtn);
        actionButtonPanel.add(upgradeBtn);
        actionButtonPanel.add(attackBtn);
        leftColumn.add(actionButtonPanel, BorderLayout.SOUTH);
        
        centerPanel.add(leftColumn);

        // Right Column: Live Output & Engine Console Log
        JPanel rightColumn = new JPanel(new BorderLayout(5, 5));
        rightColumn.setBorder(BorderFactory.createTitledBorder("Real-Time Engine Console Log"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        rightColumn.add(logScrollPane, BorderLayout.CENTER);
        
        armyPowerLabel = new JLabel("Total Army Combined Power: 0 AP");
        armyPowerLabel.setFont(new Font("Arial", Font.BOLD, 13));
        rightColumn.add(armyPowerLabel, BorderLayout.SOUTH);
        
        centerPanel.add(rightColumn);
        frame.add(centerPanel, BorderLayout.CENTER);

        // --- BUTTON ACTION LISTENERS ---

        // Add Leaf Unit
        addSoldierBtn.addActionListener(e -> {
            Soldier s = new Soldier();
            mainArmy.addUnit(s);
            trackedUnits.add(s);
            unitListModel.addElement("Soldier (Base AP: 10)");
            updateArmyMetrics();
            logArea.append("[Composite] Created and added an isolated Soldier Leaf node.\n");
        });

        // Add Composite Unit
        addSquadBtn.addActionListener(e -> {
            Squad customSquad = new Squad();
            customSquad.addUnit(new Soldier());
            customSquad.addUnit(new Soldier());
            
            mainArmy.addUnit(customSquad);
            trackedUnits.add(customSquad);
            unitListModel.addElement("Squad Group (2 Soldiers - Base AP: 20)");
            updateArmyMetrics();
            logArea.append("[Composite] Composed structural Sub-Tree (Squad container housing 2 Soldiers).\n");
        });

        // Dynamic Decorator Wrapper Application
        upgradeBtn.addActionListener(e -> {
            int selectedIdx = unitList.getSelectedIndex();
            if (selectedIdx != -1) {
                GameEntity target = trackedUnits.get(selectedIdx);
                
                // Wrap target inside a concrete decorator runtime instance
                WeaponUpgrade upgradedTarget = new WeaponUpgrade(target, 5);
                
                // Swap old structural references out for the new decorated object wrapper
                mainArmy.removeUnit(target);
                mainArmy.addUnit(upgradedTarget);
                trackedUnits.set(selectedIdx, upgradedTarget);
                
                // Update interface display text to map change visually
                String oldName = unitListModel.get(selectedIdx);
                unitListModel.set(selectedIdx, oldName + " + [Weapon Decorator +5]");
                
                updateArmyMetrics();
                logArea.append("[Decorator] Wrapped entity runtime instance structural reference with a WeaponUpgrade dynamic layer (+5 AP).\n");
            } else {
                JOptionPane.showMessageDialog(frame, "Please highlight a unit from the structural list first to decorate it.");
            }
        });

        // Combined Action Trigger
        attackBtn.addActionListener(e -> {
            logArea.append("\n=== COMPOSITE COMBAT ITERATION INITIATED ===\n");
            mainArmy.attack();
            logArea.append("============================================\n\n");
        });

        // Display Window frame safely centered
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        logArea.append("Tactical Framework initialized. System core idle on state turn 1.\n");
    }

    private static void updateArmyMetrics() {
        armyPowerLabel.setText("Total Army Combined Power: " + mainArmy.getAttackPower() + " AP");
    }

    // Capture logs seamlessly to UI output view layout
    public static void logCombatMessage(String message) {
        if (logArea != null) {
            logArea.append(message + "\n");
        }
    }
}

// -----------------------------------------------------------------------------
// core structural pattern interfaces & implementations (Package-Private Scope)
// -----------------------------------------------------------------------------

interface GameEntity {
    void attack();
    int getAttackPower();
}

class Soldier implements GameEntity {
    private int basePower = 10;

    @Override
    public void attack() {
        TacticalGameApp.logCombatMessage(" -> [Leaf Soldier] Lunges ahead! Deals " + getAttackPower() + " damage.");
    }

    @Override
    public int getAttackPower() {
        return basePower;
    }
}

class Squad implements GameEntity {
    private List<GameEntity> units = new ArrayList<>();

    public void addUnit(GameEntity unit) {
        units.add(unit);
    }

    public void removeUnit(GameEntity unit) {
        units.remove(unit);
    }

    @Override
    public void attack() {
        TacticalGameApp.logCombatMessage(" -> [Composite Node Execution] Distributing combat signals downstream to elements:");
        for (GameEntity unit : units) {
            unit.attack();
        }
    }

    @Override
    public int getAttackPower() {
        return units.stream().mapToInt(GameEntity::getAttackPower).sum();
    }
}

abstract class EntityDecorator implements GameEntity {
    protected GameEntity decoratedEntity;

    public EntityDecorator(GameEntity entity) {
        this.decoratedEntity = entity;
    }
    
    @Override
    public void attack() {
        decoratedEntity.attack();
    }
    
    @Override
    public int getAttackPower() {
        return decoratedEntity.getAttackPower();
    }
}

class WeaponUpgrade extends EntityDecorator {
    private int bonusDamage;

    public WeaponUpgrade(GameEntity entity, int bonusDamage) {
        super(entity);
        this.bonusDamage = bonusDamage;
    }

    @Override
    public void attack() {
        super.attack();
        TacticalGameApp.logCombatMessage("    ╚══> [Decorator Modifier] Empowering weapon critical active! Adding +" + bonusDamage + " plasma impact.");
    }

    @Override
    public int getAttackPower() {
        return super.getAttackPower() + bonusDamage;
    }
}

class GameEngine {
    private volatile static GameEngine uniqueInstance;
    private int currentTurn;

    private GameEngine() {
        this.currentTurn = 1;
    }

    public static GameEngine getInstance() {
        if (uniqueInstance == null) {
            synchronized (GameEngine.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new GameEngine();
                }
            }
        }
        return uniqueInstance;
    }

    public void nextTurn() {
        currentTurn++;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }
}