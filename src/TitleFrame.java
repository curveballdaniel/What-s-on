import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TitleFrame {

	private JFrame frame;

	Font titleFont = new Font("Elephant", Font.PLAIN, 55);
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TitleFrame window = new TitleFrame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public TitleFrame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1400, 1200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(2, 3, 0, 0));
		
		LoLPanel leagueSchedules = new LoLPanel(frame);
		try {
			leagueSchedules.initializePanel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JScrollPane lolPanelPane = new JScrollPane(leagueSchedules.returnPanel());
		lolPanelPane.getVerticalScrollBar().setUnitIncrement(16); // speed up scrolling
		frame.getContentPane().add(lolPanelPane);
		
		// nfl 
		SameHTMLFormatPanel nflSchedules = new SameHTMLFormatPanel(frame, "nfl");
		try {
			nflSchedules.initializePanel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JScrollPane nflPanelPane = new JScrollPane(nflSchedules.returnPanel());
		nflPanelPane.getVerticalScrollBar().setUnitIncrement(16); // speed up scrolling
		frame.getContentPane().add(nflPanelPane);
		
		// mlb 
		SameHTMLFormatPanel mlbSchedules = new SameHTMLFormatPanel(frame, "mlb");
		try {
			mlbSchedules.initializePanel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JScrollPane mlbPanelPane = new JScrollPane(mlbSchedules.returnPanel());
		mlbPanelPane.getVerticalScrollBar().setUnitIncrement(16); // speed up scrolling
		frame.getContentPane().add(mlbPanelPane);
		
		// nba
		SameHTMLFormatPanel nbaSchedules = new SameHTMLFormatPanel(frame, "nba");
		try {
			nbaSchedules.initializePanel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JScrollPane nbaPanelPane = new JScrollPane(nbaSchedules.returnPanel());
		nbaPanelPane.getVerticalScrollBar().setUnitIncrement(16); // speed up scrolling
		frame.getContentPane().add(nbaPanelPane);
		
		// nhl
		NHLPanel nhlSchedules = new NHLPanel(frame);
		try {
			nhlSchedules.initializePanel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JScrollPane nhlPanelPane = new JScrollPane(nhlSchedules.returnPanel());
		nhlPanelPane.getVerticalScrollBar().setUnitIncrement(16); // speed up scrolling
		frame.getContentPane().add(nhlPanelPane);


		// extra info + refresh button
		JPanel extras = new JPanel();
		extras.setLayout(new GridLayout(6, 1, 0, 0));
		
		// create refresh button
		JButton btnRefresh = new JButton("Refresh all");
		btnRefresh.setFont(new Font("Elephant", Font.PLAIN, 55));
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnRefresh.setText("Refreshing... League Schedules");
				try {
					leagueSchedules.refreshInfo();
				} catch (IOException e) {
					System.out.println("Error Updating League of Legends Schedules");
					e.printStackTrace();
				}
				btnRefresh.setText("Refreshing... NFL Schedules");
				try {
					nflSchedules.refreshInfo();
				} catch (IOException e) {
					System.out.println("Error Updating NFL Schedules");
					e.printStackTrace();
				}
				btnRefresh.setText("Refreshing... MLB Schedules");
				try {
					mlbSchedules.refreshInfo();
				} catch (IOException e) {
					System.out.println("Error Updating MLB Schedules");
					e.printStackTrace();
				}
				btnRefresh.setText("Refreshing... NBA Schedules");
				try {
					nbaSchedules.refreshInfo();
				} catch (IOException e) {
					System.out.println("Error Updating NBA Schedules");
					e.printStackTrace();
				}
				btnRefresh.setText("Refreshing... NHL Schedules");
				try {
					nhlSchedules.refreshInfo();
				} catch (IOException e) {
					System.out.println("Error Updating NHL Schedules");
					e.printStackTrace();
				}
				btnRefresh.setText("Refresh");
			}
		});
		
		// add title to final panel
		JLabel favTeamTitle = new JLabel("Favorite Team Standings: ");
		favTeamTitle.setFont(titleFont);
		extras.add(favTeamTitle);
		
		// add team records next to refresh button
		try {
			extras.add(nbaSchedules.obtainTeamRecord("New England Patriots", "http://www.espn.com/nfl/team/_/name/ne/new-england-patriots"));		
			extras.add(nbaSchedules.obtainTeamRecord("Boston Red Sox", "http://www.espn.com/mlb/team/_/name/bos/boston-red-sox"));
			extras.add(nbaSchedules.obtainTeamRecord("Boston Celtics", "http://www.espn.com/nba/team/_/name/bos/boston-celtics"));
			extras.add(nbaSchedules.obtainTeamRecord("Boston Bruins", "http://www.espn.com/nhl/team/_/name/bos/boston-bruins"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// add refresh button
		extras.add(btnRefresh);
		
		frame.getContentPane().add(extras);
	}
}
