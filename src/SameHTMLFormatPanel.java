import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SameHTMLFormatPanel extends JPanel implements SportsFrameInterface {
	
	String sport = "none";
	JFrame parent;
	Font titleFont = new Font("Elephant", Font.PLAIN, 55);
	Font matchupFont = new Font("Georgia", Font.PLAIN, 32); // 23, 22 to fit
	
	// main panel
	JPanel matchesBelow;
	
	// set icon sizes
	int iconWidth = 96;
	int iconHeight = 96;
	
	// background image
	BufferedImage backgroundImage;
	
	public SameHTMLFormatPanel(JFrame parent, String sport) { // sport written in abbreviation, (i.e. nba, nfl, mlb, soccer, etc.)
		// set parent, object layout
		this.parent = parent;
		this.sport = sport;
		parent.getContentPane().add(this);
		this.setLayout(new BoxLayout(this, 1));
	}
	
	public void initializePanel() throws IOException {
		// panel title
		JPanel titleSection = new JPanel();
		titleSection.setLayout(new FlowLayout());
		
		JLabel lblUpcomingLol = new JLabel("Upcoming " + sport.toUpperCase() + " Games:"); // , SwingConstants.CENTER
		lblUpcomingLol.setFont(titleFont);
		titleSection.add(lblUpcomingLol);
		
		this.add(titleSection);
		
		matchesBelow = new JPanel();
		matchesBelow.setLayout(new BoxLayout(matchesBelow, 1));
		
		this.add(matchesBelow);
		
		refreshInfo();
	}
	
	public void refreshInfo() throws IOException {	
		if (matchesBelow.getComponents().length > 0) { // clear, then readd
			matchesBelow.removeAll();
		}
		
		System.out.println(sport + " Schedules: ");
		
		Document doc = Jsoup.connect("http://www.espn.com/" + sport + "/schedule").get();
		
		Elements espnTables = doc.select("div#sched-container");
		ArrayList<String> dailyTitles = new ArrayList<String>();
		
		for (Element dailyTitle: espnTables.select("h2.table-caption")) { // Tuesday, April 2, * located before HTML table
			dailyTitles.add(dailyTitle.text());
		}
		
		int dayTitleIndex = 0; // index to place correct date title w/ corresponding games
		
		for (Element dailyScheduleTable: espnTables.select("div.responsive-table-wrap")) { // HTML table, from Tuesday, April 2 to Friday, April 3
			Elements match = dailyScheduleTable.select("table.schedule").select("tbody"); // HTML table body
			
			JPanel dailySchedulePanel = new JPanel(); // add a panel to hold Title, then Daily Matchups, then Separator
			dailySchedulePanel.setLayout(new BoxLayout(dailySchedulePanel, 1));
			
			JPanel subScheduleTitle = new JPanel(); // title panel
			subScheduleTitle.setLayout(new FlowLayout());
			
			// two potential checks to ensure that correct dates are given even if games are postponed or completed
			boolean rewoundPostpone = false;
			boolean rewoundComplete = false;
			
			JLabel dailyTimeLabel = new JLabel("", SwingConstants.CENTER); // outside in case of postponed matches
			// if statement check if game has been postponed
			if (dayTitleIndex < dailyTitles.size()) {
				dailyTimeLabel.setText("Games for: " + dailyTitles.get(dayTitleIndex));
				dailyTimeLabel.setFont(matchupFont);
				subScheduleTitle.add(dailyTimeLabel); // add to title panel
				dayTitleIndex++; // increment amount so next day schedule title can apply
			}
			
			dailySchedulePanel.add(subScheduleTitle); // add to panel object
			
			Elements htmlRows = match.select("tr"); // HTML table row - ex: Philadelphia @ Dallas at 8:30 PM Tickets as low as $6
			
			// create panel for matchups for that certain day
			JPanel dailyMatchups = new JPanel();
			dailyMatchups.setLayout(new GridBagLayout());
			GridBagConstraints gridC = new GridBagConstraints();
			int positionY = 0;
			
			for (Element matchRow: htmlRows) { // HTML table row - ex: Cincinnati CIN at Pittsburgh PIT 8:30 PM Tickets as low as $6
				if (matchRow.children().size() > 2) { // obtain teams where applicable (don't pull from empty schedules)
					Elements cells = matchRow.select("td");
					
					Element awayTeam = cells.get(0); // Tampa Bay
					String awayTeamImgString = awayTeam.select("[^src]").toString(); // Raw HTML Tampa Bay Img location (i.e. <img src="https:...)
					Element homeTeam = cells.get(1); // @ Boston
					String homeTeamImgString = homeTeam.select("[^src]").toString(); // Raw HTML Boston Img location
					Elements gameTime = cells.get(2).select("[^data-date]"); // time isn't in text, needs to be obtained from data
					
					String stringContainingUnformattedDate = gameTime.toString(); // obtain string
					// and obtain substring of xxxx-xx-xxTxx:xxZ, hardcoded #s as length of date + index marker never change
					int startCharAt = stringContainingUnformattedDate.lastIndexOf("data-date=");
					
					Date gameStartTime = null;
					if (startCharAt > 0) {
						String unformattedDate = stringContainingUnformattedDate.substring(startCharAt + 11, startCharAt + 28);
						 gameStartTime = returnFormattedDate(unformattedDate, "yyyy-MM-dd'T'HH:mmZ");
					}
					
					JLabel timeLabel = new JLabel("", SwingConstants.CENTER); // create time label, alter according to what is found
					timeLabel.setFont(matchupFont);
					gridC.gridx = 0;
					gridC.gridy = positionY;
					//gridC.ipady = 60;
					gridC.gridheight = 1; 
					//gridC.anchor = GridBagConstraints.PAGE_END; //bottom of space
					
					if (gameStartTime != null) {
						System.out.println(gameStartTime);
						timeLabel.setText("Match starting at: " + turnDateIntoString(gameStartTime));
						dailyMatchups.add(timeLabel, gridC); // add live time
					} else if (cells.get(2).text().equals("Postponed")) { // NOTE
						// if a game is postponed, the parser has mistakenly taken this as a separate day, as it is in a separate table
						// thus, the counter is rewound once to ensure the correct date is placed for postponed game, and carries on w/ correct date
						// doesn't not check > 0 because if postponed/finished games are leading, it will throw off correct date times
						if (dayTitleIndex > 1 && !rewoundPostpone) {
							dayTitleIndex--;
							rewoundPostpone = true;
						}
						dailyTimeLabel.setText("Postponed Games");
						timeLabel.setText("Postponed");
						dailyMatchups.add(timeLabel, gridC);
					} else if (cells.get(2).text().equals("LIVE")){
						timeLabel.setText("Playing live");
						dailyMatchups.add(timeLabel, gridC);
					} else {
						// similar thing must occur if game is completed - it is placed in a separate html table
						if (dayTitleIndex > 1 && !rewoundComplete) {
							dayTitleIndex--;
							rewoundComplete = true;
						}
						dailyTimeLabel.setText("Completed Games");
						timeLabel.setText("Completed: " + cells.get(2).text());
						dailyMatchups.add(timeLabel, gridC);
					}
					
					System.out.println(awayTeam.text() + " at " + homeTeam.text() + "\nStarting at: " + gameStartTime);
					
					try {
						addImages(dailyMatchups, gridC, positionY, awayTeamImgString, awayTeam.text(), homeTeamImgString, homeTeam.text(), iconWidth, iconHeight); // add images to panel
					} catch (IOException e) {
						e.printStackTrace();
					} 

					dailySchedulePanel.add(dailyMatchups); // add to matchup object
					
					positionY++; // increment y position

				}
			}
			
			JPanel separatorPanel = new JPanel();
			separatorPanel.setLayout(new FlowLayout());
			JLabel separatorLabel = new JLabel("--------------------------------------------------------------------------------", SwingConstants.CENTER);
			
			separatorPanel.add(separatorLabel); // add label to panel
			dailySchedulePanel.add(separatorPanel); // add panel to main to separate from next day
			
			matchesBelow.add(dailySchedulePanel);
		}
		
	}

	public void addImages(JPanel matchesBelow, GridBagConstraints gridBagC, int positionY, String teamOneUnformattedImgHTML, String teamOneName, String teamTwoUnformattedImgHTML, String teamTwoName, int iconWidth, int iconHeight) throws IOException {
		JPanel matchup = new JPanel();
		matchup.setLayout(new FlowLayout());
		
		if (teamOneUnformattedImgHTML != null) {
			String imgLocation = reformatInputHTMLToImgLocation(teamOneUnformattedImgHTML); // obtain img link
			
			URL imageOne = new URL(imgLocation); // pull img and add to panel
			BufferedImage pulledImageOne = ImageIO.read(imageOne);
			JLabel picLabelAway = new JLabel();
			picLabelAway.setIcon(new ImageIcon(new ImageIcon(pulledImageOne).getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_DEFAULT)));
			matchup.add(picLabelAway);
		}
		
		JLabel vsLabel = new JLabel(teamOneName + " vs. " + teamTwoName);
		vsLabel.setFont(matchupFont);
		matchup.add(vsLabel);
		
		if (teamTwoUnformattedImgHTML != null) {
			String imgLocation = reformatInputHTMLToImgLocation(teamTwoUnformattedImgHTML); // obtain img link
			
			URL imageTwo = new URL(imgLocation); // pull img and add to panel
			BufferedImage pulledImageTwo = ImageIO.read(imageTwo);
			JLabel picLabelHome = new JLabel();
			picLabelHome.setIcon(new ImageIcon(new ImageIcon(pulledImageTwo).getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_DEFAULT)));		
			matchup.add(picLabelHome);
		}
		
		gridBagC.gridx = 1;
		gridBagC.gridy = positionY;
		gridBagC.ipadx = 60;
		// add everything to panel
		matchesBelow.add(matchup, gridBagC);
	}
	
	/**
	 * // transform images given in format: <img src="..." class="...">
	 * // to obtain the image link		
	 * @param input
	 * @return
	 */
	private String reformatInputHTMLToImgLocation(String input) {
		int startCharAt = input.lastIndexOf("<img src=");
		int endCharAt = input.lastIndexOf("\" class=");
		return input.substring(startCharAt + 10, endCharAt);
	}
	
	public JPanel returnPanel() {
		return this;
	}
	
	/**
	 * method which takes an input url, and obtains the team's record in text
	 * @param URL
	 * @return
	 * @throws IOException
	 */
	public JPanel obtainTeamRecord(String team, String URL) throws IOException {
		JPanel teamRecord = new JPanel();
		teamRecord.setLayout(new FlowLayout());
		
		Document doc = Jsoup.connect(URL).get();
		
		Elements teamStringRecord = doc.select("ul.ClubhouseHeader__Record");
		
		JLabel teamRecordLabel = new JLabel(team + ": " + teamStringRecord.text()); // , SwingConstants.CENTER
		teamRecordLabel.setFont(matchupFont);
		teamRecord.add(teamRecordLabel);
		
		System.out.println(team + ": " + teamStringRecord.text());
		
		return teamRecord; 
	}
	
	public void setBackground() {
		try {
			backgroundImage = ImageIO.read(new URL(""));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Date returnFormattedDate(String unformattedDate, String formatStyle) {
		SimpleDateFormat formatter = new SimpleDateFormat(formatStyle);
		try {
            Date formattedDate = formatter.parse(unformattedDate.replaceAll("Z$", "+0000"));
            return formattedDate;
        } catch (ParseException e) {
        	System.out.println("Input String format doesn't follow input format style");
            e.printStackTrace();
            return null;
        }
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(backgroundImage, 0, 0, null);
	}

	@Override
	public String turnDateIntoString(Date input) {
		DateFormat df = new SimpleDateFormat("HH:ss");
		return df.format(input).toString();
	}

}
