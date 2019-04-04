import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

public class NHLPanel extends JPanel implements SportsFrameInterface {
	
	JFrame parent;
	Font titleFont = new Font("Elephant", Font.PLAIN, 55);
	Font matchupFont = new Font("Georgia", Font.PLAIN, 32);
	
	// main panel
	JPanel matchesBelow;
	
	// set icon sizes
	int iconWidth = 96;
	int iconHeight = 96;
	
	// background image
	BufferedImage backgroundImage;
	
	public NHLPanel(JFrame parent) {
		// set parent, object layout
		this.parent = parent;
		parent.getContentPane().add(this);
		this.setLayout(new BoxLayout(this, 1));
	}
	
	public void initializePanel() throws IOException {
		// panel title
		JPanel titleSection = new JPanel();
		titleSection.setLayout(new FlowLayout());
		
		JLabel lblUpcomingLol = new JLabel("Upcoming NHL Games:"); // , SwingConstants.CENTER
		lblUpcomingLol.setFont(titleFont);
		titleSection.add(lblUpcomingLol);
		
		this.add(titleSection);
		
		matchesBelow = new JPanel();
		matchesBelow.setLayout(new BoxLayout(matchesBelow, 1));
		
		this.add(matchesBelow);
		
		refreshInfo();
	}
	
	public void refreshInfo() throws IOException {
		if (matchesBelow.getComponents().length > 0) { // if exists, clear, and add again
			matchesBelow.removeAll();
		}
		
		System.out.println("NHL Schedules: ");
		
		Document doc = Jsoup.connect("http://www.espn.com/nhl/schedule").get();
		
		Elements espnTables = doc.select("div.mt3");
		Elements htmlTable = espnTables.select("div.ScheduleTables"); // HTML table, from Tuesday, April 2 to Friday, April 3
		
		for (Element dailyScheduleTable: htmlTable) { // check each schedule by parsing through its HTML table
			JPanel dailySchedulePanel = new JPanel(); // add a panel to hold Title, then Daily Matchups, then Separator
			//dailySchedulePanel.setLayout(new GridLayout(3, 1, 0, 0));
			dailySchedulePanel.setLayout(new BoxLayout(dailySchedulePanel, 1));
			
			JPanel subScheduleTitle = new JPanel(); // title panel
			subScheduleTitle.setLayout(new FlowLayout());
			Elements dailyTitle = dailyScheduleTable.select("div.Table2__Title"); // Tuesday, April 2
			
			JLabel dailyTimeLabel = new JLabel("Games for: " + dailyTitle.text(), SwingConstants.CENTER);
			dailyTimeLabel.setFont(matchupFont);
			subScheduleTitle.add(dailyTimeLabel); // add to title panel
			
			dailySchedulePanel.add(subScheduleTitle); // add to panel object
			
			System.out.println(dailyTitle.text());
			
			Elements match = dailyScheduleTable.select("table.Table2__table__wrapper").select("tbody.Table2__tbody"); // HTML table body
			Elements htmlRows = match.select("tr.Table2__tr"); // HTML table row - ex: Philadelphia @ Dallas at 8:30 PM Tickets as low as $6
			
			// create panel for matchups for that certain day
			JPanel dailyMatchups = new JPanel();
			dailyMatchups.setLayout(new GridLayout(htmlRows.size(), 1, 0, 0));
			
			for (Element matchRow: htmlRows) { // parse through each HTML table row, obtain necessary info
				Element awayTeam = matchRow.select("td.Table2__td").get(0); // Tampa Bay
				String awayTeamImgString = awayTeam.select("[^data-srcset]").toString(); // Raw HTML Tampa Bay Img location (i.e. <source data-srcset="https:...)
				Element homeTeam = matchRow.select("td.Table2__td").get(1); // @ Boston
				String homeTeamImgString = homeTeam.select("[^data-srcset]").toString(); // Raw HTML Boston Img location
				Element gameTime = matchRow.select("td.Table2__td").get(2); // 1:00 PM

				JLabel timeLabel = new JLabel("Match starting at: " + gameTime.text(), SwingConstants.CENTER);
				timeLabel.setFont(matchupFont);
				dailyMatchups.add(timeLabel); // add live time
				System.out.println(timeLabel.getText() + " " + awayTeam.text() + " " + homeTeam.text());
				
				try {
					addImages(dailyMatchups, awayTeamImgString, awayTeam.text(), homeTeamImgString, homeTeam.text(), iconWidth, iconHeight); // add images to panel
				} catch (IOException e) {
					e.printStackTrace();
				} 

				dailySchedulePanel.add(dailyMatchups); // add to matchup object
			}
			
			JPanel separatorPanel = new JPanel();
			separatorPanel.setLayout(new FlowLayout());
			JLabel separatorLabel = new JLabel("--------------------------------------------------------------------------------", SwingConstants.CENTER);
			
			separatorPanel.add(separatorLabel); // add label to panel
			dailySchedulePanel.add(separatorPanel); // add panel to main to separate from next day
			
			matchesBelow.add(dailySchedulePanel);
		}
		
	}
	
	public void addImages(JPanel matchesBelow, String teamOneUnformattedImgHTML, String teamOneName, String teamTwoUnformattedImgHTML, String teamTwoName, int iconWidth, int iconHeight) throws IOException {
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
		
		// add everything to panel
		matchesBelow.add(matchup);
	}
	
	/**
	 * // transform images given in format: <source data-srcset="https://a.espncdn.com/combiner/i?img=/i/teamlogos/nhl/500/scoreboard/vgs.png&amp;w=40&amp;h=40&amp;scale=crop" srcSet="https://a.espncdn.com/combiner/i?img=/i/teamlogos/nhl/500/scoreboard/vgs.png&amp;w=40&amp;h=40&amp;scale=crop">
	// to obtain the image link		
	 * @param input
	 * @return
	 */
	private String reformatInputHTMLToImgLocation(String input) {
		int startCharAt = input.lastIndexOf("data-srcset=");
		int endCharAt = input.lastIndexOf("\" srcSet=");
		return input.substring(startCharAt + 13, endCharAt);
	}
	
	public JPanel returnPanel() {
		return this;
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
		DateFormat df = new SimpleDateFormat("EEEE, HH:ss, MM/dd/yyyy");
		return df.format(input).toString();
	}

}
