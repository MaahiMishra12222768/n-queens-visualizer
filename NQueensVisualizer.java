import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class NQueensVisualizer extends JFrame {
    private int size;
    private int delay;
    private JTextField[][] cells;
    private int[][] board;
    private boolean solving;
    private JButton startStopButton;
    private JButton nextSolutionButton;
    private JSlider speedSlider;
    private int solutionCount;
    private List<int[][]> solutions;

    public NQueensVisualizer() {
        setTitle("N-Queens Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        size = getSizeFromUser();
        delay = getSpeedFromUser();

        cells = new JTextField[size][size];
        board = new int[size][size];
        solutions = new ArrayList<>();
        solving = false;
        solutionCount = 0;

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(size, size));
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(new Font("Arial", Font.BOLD, 20));
                cells[row][col].setEditable(false);
                cells[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.BLACK);
                cells[row][col].setForeground((row + col) % 2 == 0 ? Color.BLACK : Color.WHITE);
                gridPanel.add(cells[row][col]);
                board[row][col] = 0;
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));

        startStopButton = new JButton("Start");
        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (solving) {
                    solving = false;
                    startStopButton.setText("Start");
                } else {
                    solving = true;
                    startStopButton.setText("Stop");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            solvePuzzle();
                        }
                    }).start();
                }
            }
        });
        buttonPanel.add(startStopButton);

        nextSolutionButton = new JButton("Next Solution");
        nextSolutionButton.setEnabled(false);
        nextSolutionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solving = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        clearBoard();
                        solutionCount++;
                        solvePuzzle();
                    }
                }).start();
            }
        });
        buttonPanel.add(nextSolutionButton);

        controlPanel.add(buttonPanel, BorderLayout.NORTH);

        speedSlider = new JSlider(0, 1000, delay);
        speedSlider.setMajorTickSpacing(200);
        speedSlider.setMinorTickSpacing(50);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(e -> delay = speedSlider.getValue());
        controlPanel.add(speedSlider, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.SOUTH);

        setSize(600, 600);
    }

    private int getSizeFromUser() {
        String input = JOptionPane.showInputDialog(this, "Enter the number of queens:", "N-Queens", JOptionPane.QUESTION_MESSAGE);
        return Integer.parseInt(input);
    }

    private int getSpeedFromUser() {
        String input = JOptionPane.showInputDialog(this, "Enter the initial speed in milliseconds:", "Speed", JOptionPane.QUESTION_MESSAGE);
        return Integer.parseInt(input);
    }

    private void solvePuzzle() {
        if (findSolution()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(NQueensVisualizer.this, "N-Queens Solved! Solution #" + (solutionCount + 1), "Success", JOptionPane.INFORMATION_MESSAGE);
                    nextSolutionButton.setEnabled(true);
                }
            });
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(NQueensVisualizer.this, "No more solutions exist for the given N-Queens board.", "Error", JOptionPane.ERROR_MESSAGE);
                    nextSolutionButton.setEnabled(false);
                }
            });
        }
        solving = false;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                startStopButton.setText("Start");
            }
        });
    }

    private boolean findSolution() {
        return solveNQueens(0);
    }

    private void clearBoard() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                cells[row][col].setText("");
                cells[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.BLACK);
                cells[row][col].setForeground((row + col) % 2 == 0 ? Color.BLACK : Color.WHITE);
                board[row][col] = 0;
            }
        }
    }

    private boolean isSafe(int row, int col) {
        for (int i = 0; i < col; i++) {
            if (board[row][i] == 1) {
                return false;
            }
        }

        for (int i = row, j = col; i >= 0 && j >= 0; i--, j--) {
            if (board[i][j] == 1) {
                return false;
            }
        }

        for (int i = row, j = col; j >= 0 && i < size; i++, j--) {
            if (board[i][j] == 1) {
                return false;
            }
        }

        return true;
    }

    private boolean solveNQueens(int col) {
        if (col >= size) {
            if (!isDuplicateSolution()) {
                storeSolution();
                return true;
            }
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (!solving) {
                return false;
            }
            if (isSafe(i, col)) {
                board[i][col] = 1;
                updateGUI(i, col, true);
                delay(delay); // Delay to visualize steps

                if (solveNQueens(col + 1)) {
                    return true;
                }

                board[i][col] = 0;
                updateGUI(i, col, false);
                delay(delay); // Delay to visualize steps
            }
        }

        return false;
    }

    private void storeSolution() {
        int[][] solution = new int[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(board[i], 0, solution[i], 0, size);
        }
        solutions.add(solution);
    }

    private boolean isDuplicateSolution() {
        for (int[][] solution : solutions) {
            if (areBoardsEqual(solution, board)) {
                return true;
            }
        }
        return false;
    }

    private boolean areBoardsEqual(int[][] board1, int[][] board2) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board1[i][j] != board2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateGUI(int row, int col, boolean placeQueen) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (placeQueen) {
                    cells[row][col].setText("Q");
                    cells[row][col].setBackground(Color.ORANGE);
                    cells[row][col].setForeground(Color.BLACK);
                } else {
                    cells[row][col].setText("");
                    cells[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.BLACK);
                    cells[row][col].setForeground((row + col) % 2 == 0 ? Color.BLACK : Color.WHITE);
                }
            }
        });
    }

    private void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NQueensVisualizer visualizer = new NQueensVisualizer();
                visualizer.setVisible(true);
            }
        });
    }
}
