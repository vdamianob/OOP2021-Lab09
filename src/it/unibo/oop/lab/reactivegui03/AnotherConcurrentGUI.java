package it.unibo.oop.lab.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *  blablabla
 *
 */
public final class AnotherConcurrentGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.2;
    private final JLabel display = new JLabel();
    private final JLabel display2 = new JLabel();
    private final JButton stop = new JButton("stop");
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private boolean timeToStop;

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        this.timeToStop = false;
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display); //added display, where the number is
        panel.add(display2);
        panel.add(stop);
        panel.add(up);
        panel.add(down);
        this.getContentPane().add(panel); //added JPanel "panel" to the Pane
        this.setVisible(true);
        /*
         * Create the counter agent and start it. This is actually not so good:
         * thread management should be left to
         * java.util.concurrent.ExecutorService
         */
        final Agent agent = new Agent();
        final AgentTimer agentT = new AgentTimer();
        new Thread(agent).start();
        new Thread(agentT).start();
        /*
         * Register a listener that stops it
         */
        stop.addActionListener(new ActionListener() {
            /**
             * event handler associated to action event on button stop.
             * 
             * @param e
             *            the action event that will be handled by this listener
             */
            @Override
            public void actionPerformed(final ActionEvent e) {
                // Agent should be final
                agent.stopCounting();
                stop.setEnabled(false);
                up.setEnabled(false);
                down.setEnabled(false);
            }
        });

        up.addActionListener(new ActionListener() {
            /**
             * event handler associated to action event on button up.
             * 
             * @param e
             *            the action event that will be handled by this listener
             */
            @Override
            public void actionPerformed(final ActionEvent e) {
                // Agent should be final
                agent.trendUp();
            }
        });

        down.addActionListener(new ActionListener() {
            /**
             * event handler associated to action event on button down.
             * 
             * @param e
             *            the action event that will be handled by this listener
             */
            @Override
            public void actionPerformed(final ActionEvent e) {
                // Agent should be final
                agent.trendDown();
            }
        });
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         * 
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         * 
         * For more details on how to use volatile:
         * 
         * http://archive.is/4lsKW
         * 
         */
        private volatile boolean stop; //di default è false
        private volatile int counter;
        private volatile boolean trend;

        @Override
        public void run() {
            while (!this.stop && !timeToStop) {
                try {
                    /*
                     * All the operations on the GUI must be performed by the
                     * Event-Dispatch Thread (EDT)!
                     */
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            // This will happen in the EDT: since i'm reading counter it needs to be volatile.
                            AnotherConcurrentGUI.this.display.setText(Integer.toString(Agent.this.counter));
                        }
                    });
                    /*
                     * SpotBugs shows a warning because the increment of a volatile variable is not atomic,
                     * so the concurrent access is potentially not safe. In the specific case of this exercise,
                     * we do synchronization with invokeAndWait, so it can be ignored.
                     *
                     * EXERCISE: Can you think of a solution that doesn't require counter to be volatile? (without
                     * using synchronized or locks)
                     */
                    if (!trend) {
                        this.counter++;
                    }
                    else {
                        this.counter--;
                    }
                    Thread.sleep(100);

                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        public void trendUp() {
            this.trend = false;
        }

        public void trendDown() {
            this.trend = true;
        }
    }

    private class AgentTimer implements Runnable {

        @Override
        public void run() {
                try {
                    Thread.sleep(5000);
                    SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        AnotherConcurrentGUI.this.timeToStop = true;
                        AnotherConcurrentGUI.this.stop.setEnabled(false);
                        AnotherConcurrentGUI.this.up.setEnabled(false);
                        AnotherConcurrentGUI.this.down.setEnabled(false);
                        AnotherConcurrentGUI.this.display2.setText("STOPPED");
                    }
                });

                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        
}

