import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PullTests {
	
	/*
	 * class created for api/result testing, not used in frame, but much code is reused
	 */

	private final static String PANDA_SCORE_TOKEN = "W0e0u3xQomzT3q8dRgSEBnLqXpN-ydqsY3W8N3cmACvU4orHMAU";
	
	public static void main(String[] args) throws IOException{
		// league of legends schedule - from api
		//obtainLeagueOfLegendsSchedule();
		
		// sports - scraping espn schedule site
		//scrapeESPNNHLSchedule();
		//scrapeESPNNBASchedule();
		//scrapeESPNMLBSchedule();
		//scrapeESPNNFLSchedule();
		
		
		// general schedule: Done
		// http://www.espn.com/nba/schedule
		// http://www.espn.com/mlb/schedule
		// http://www.espn.com/nfl/schedule
		// http://www.espn.com/nhl/schedule
		// works for soccer, but not used
		
		// TODO: 
		// create for:
		// specific teams:
		// http://www.espn.com/nba/team/schedule/_/name/bos
		// http://www.espn.com/nfl/team/schedule/_/name/ne
		// http://www.espn.com/nhl/team/schedule/_/name/bos
		// http://www.espn.com/mlb/team/schedule/_/name/bos
		
		// playoffs for any sport?
		// eventually, make it editable (add in a personal text, it will search it for you), and check for certain user picked elements

	}
	
	/**
	 * https://jsoup.org/cookbook/extracting-data/selector-syntax
	 * scrape ESPN NHL schedule for nhl game teams, times + dates
	 * @throws IOException
	 */
	public static void scrapeESPNNHLSchedule() throws IOException {
		System.out.println("NHL Schedules: ");
		
		Document doc = Jsoup.connect("http://www.espn.com/nhl/schedule").get();
		
		Elements espnTables = doc.select("div.mt3");
		
		for (Element dailyScheduleTable: espnTables.select("div.ScheduleTables")) { // HTML table, from Tuesday, April 2 to Friday, April 3
			Elements dailyTitle = dailyScheduleTable.select("div.Table2__Title"); // Tuesday, April 2
			System.out.println(dailyTitle.text());
			
			Elements match = dailyScheduleTable.select("table.Table2__table__wrapper").select("tbody.Table2__tbody"); // HTML table body
				
			for (Element matchRow: match.select("tr.Table2__tr")) { // HTML table row - ex: Philadelphia @ Dallas at 8:30 PM Tickets as low as $6
				Element awayTeam = matchRow.select("td.Table2__td").get(0); // Tampa Bay
				Element homeTeam = matchRow.select("td.Table2__td").get(1); // @ Boston
				Element gameTime = matchRow.select("td.Table2__td").get(2); // 1:00 PM
				System.out.println("playing: " + awayTeam.text() + " " + homeTeam.text() + " at " + gameTime.text());
			}
		}
	}
	
	/**
	 * scraping method is different because site is sustained differently
	 * @throws IOException
	 */
	public static void scrapeESPNNBASchedule() throws IOException {
		System.out.println("NBA Schedules: ");
		
		Document doc = Jsoup.connect("http://www.espn.com/nba/schedule").get();
		
		Elements espnTables = doc.select("div#sched-container");
		
		for (Element dailyTitle: espnTables.select("h2.table-caption")) { // Tuesday, April 2, * located before HTML table
			System.out.println(dailyTitle.text());
		} 
		
		for (Element dailyScheduleTable: espnTables.select("div.responsive-table-wrap")) { // HTML table, from Tuesday, April 2 to Friday, April 3
			Elements match = dailyScheduleTable.select("table.schedule").select("tbody"); // HTML table body
				
			for (Element matchRow: match.select("tr")) { // HTML table row - ex: Los Angeles LAL Oklahoma City OKC 8:30 PM Tickets as low as $6
				if (matchRow.children().size() > 2) { // obtain teams where applicable (don't pull from empty schedules)
					Elements cells = matchRow.select("td");
					
					Element awayTeam = cells.get(0); // Tampa Bay
					Element homeTeam = cells.get(1); // @ Boston
					Elements gameTime = cells.get(2).select("[^data-date]"); // time isn't in text, needs to be obtained from data
					
					String stringContainingUnformattedDate = gameTime.toString(); // obtain string
					// and obtain substring of xxxx-xx-xxTxx:xxZ, hardcoded #s as length of date + index marker never change
					int startCharAt = stringContainingUnformattedDate.lastIndexOf("data-date=");
					
					Date gameStartTime = null;
					if (startCharAt > 0) {
						String unformattedDate = stringContainingUnformattedDate.substring(startCharAt + 11, startCharAt + 28);
						 gameStartTime = returnFormattedDate(unformattedDate, "yyyy-MM-dd'T'HH:mmZ");
					}
					
					System.out.println(awayTeam.text() + " at " + homeTeam.text() + "\nStarting at: " + gameStartTime);
				}
			}
			System.out.println();
		}
	}
	
	/**
	 * this method is extremely similar to the NBA espn schedule scraper - 
	 * it is left in its own method in case anything changes and for clarity
	 * @throws IOException
	 */
	public static void scrapeESPNMLBSchedule() throws IOException {
		System.out.println("MLB Schedules: ");
		
		Document doc = Jsoup.connect("http://www.espn.com/mlb/schedule").get();
		
		Elements espnTables = doc.select("div#sched-container");
		
		for (Element dailyTitle: espnTables.select("h2.table-caption")) { // Tuesday, April 2, * located before HTML table
			System.out.println(dailyTitle.text());
		} 
		
		for (Element dailyScheduleTable: espnTables.select("div.responsive-table-wrap")) { // HTML table, from Tuesday, April 2 to Friday, April 3
			Elements match = dailyScheduleTable.select("table.schedule").select("tbody"); // HTML table body
				
			for (Element matchRow: match.select("tr")) { // HTML table row - ex: Cincinnati CIN at Pittsburgh PIT 8:30 PM Tickets as low as $6
				if (matchRow.children().size() > 2) { // obtain teams where applicable (don't pull from empty schedules)
					Elements cells = matchRow.select("td");
					
					Element awayTeam = cells.get(0); // Tampa Bay
					Element homeTeam = cells.get(1); // @ Boston
					Elements gameTime = cells.get(2).select("[^data-date]"); // time isn't in text, needs to be obtained from data
					
					String stringContainingUnformattedDate = gameTime.toString(); // obtain string
					// and obtain substring of xxxx-xx-xxTxx:xxZ, hardcoded #s as length of date + index marker never change
					
					int startCharAt = stringContainingUnformattedDate.lastIndexOf("data-date=");
					
					Date gameStartTime = null;
					if (startCharAt > 0) {
						String unformattedDate = stringContainingUnformattedDate.substring(startCharAt + 11, startCharAt + 28);
						gameStartTime = returnFormattedDate(unformattedDate, "yyyy-MM-dd'T'HH:mmZ");
					}
					
					System.out.println(awayTeam.text() + " at " + homeTeam.text() + "\nStarting at: " + gameStartTime);
				}
			}
			System.out.println();
		}
	}
	
	/**
	 * this method is extremely similar to the NBA espn schedule scraper - 
	 * it is left in its own method in case anything changes and for clarity
	 * @throws IOException
	 */
	public static void scrapeESPNNFLSchedule() throws IOException {
		System.out.println("NFL Schedules: ");
		
		Document doc = Jsoup.connect("http://www.espn.com/nfl/schedule").get();
		
		Elements espnTables = doc.select("div#sched-container");
		
		for (Element dailyTitle: espnTables.select("h2.table-caption")) { // Tuesday, April 2, * located before HTML table
			System.out.println(dailyTitle.text());
		} 
		
		for (Element dailyScheduleTable: espnTables.select("div.responsive-table-wrap")) { // HTML table, from Tuesday, April 2 to Friday, April 3
			Elements match = dailyScheduleTable.select("table.schedule").select("tbody"); // HTML table body
				
			for (Element matchRow: match.select("tr")) { // HTML table row - ex: Cincinnati CIN at Pittsburgh PIT 8:30 PM Tickets as low as $6
				if (matchRow.children().size() > 2) { // obtain teams where applicable (don't pull from empty schedules)
					Elements cells = matchRow.select("td");
					
					Element awayTeam = cells.get(0); // Tampa Bay
					Element homeTeam = cells.get(1); // @ Boston
					Elements gameTime = cells.get(2).select("[^data-date]"); // time isn't in text, needs to be obtained from data
					
					String stringContainingUnformattedDate = gameTime.toString(); // obtain string
					// and obtain substring of xxxx-xx-xxTxx:xxZ, hardcoded #s as length of date + index marker never change
					int startCharAt = stringContainingUnformattedDate.lastIndexOf("data-date=");
					
					Date gameStartTime = null;
					if (startCharAt > 0) {
						String unformattedDate = stringContainingUnformattedDate.substring(startCharAt + 11, startCharAt + 28);
						 gameStartTime = returnFormattedDate(unformattedDate, "yyyy-MM-dd'T'HH:mmZ");
					}
					
					System.out.println(awayTeam.text() + " at " + homeTeam.text() + "\nStarting at: " + gameStartTime);
				}
			}
			System.out.println();
		}
	}
	

	public static Date returnFormattedDate(String unformattedDate, String formatStyle) {
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
	
	public static void obtainLeagueOfLegendsSchedule() throws IOException {
		// obtained from
		// https://api.pandascore.co/lol/matches/upcoming
		// https://api.pandascore.co/
		
		OkHttpClient client = new OkHttpClient();

		// grabs an aggregated average on CryptoCompare - can add '&e=Coinbase' to specify a single exchange
		Request request = new Request.Builder()
				.url("https://api.pandascore.co/lol/matches/upcoming")
				.addHeader("Authorization", "Bearer " + PANDA_SCORE_TOKEN)
				.build();

		Response response = client.newCall(request).execute();

		// JSON interior (needs to get parsed through for vars)
		String JSONstring = response.body().string();
		
		JSONArray upcomingMatches = new JSONArray(JSONstring);
		
		for (int i = 0; i < upcomingMatches.length(); i++) {
			JSONObject leagueObject = upcomingMatches.getJSONObject(i);
			String league = leagueObject.getJSONObject("league").getString("name");
			
			//System.out.println(leagueObject);
			
			if ((league.equals("LCK") || league.equals("LEC") || league.equals("LCS"))) {
				// obtain league image url
				
				// obtain teams informations
				JSONArray match = leagueObject.getJSONArray("opponents");
				
				// opponents information
				String opponentOneName = "unknown";
				String opponentTwoName = "unknown";
				String opponentOneImg = null;
				String opponentTwoImg = null;
				
				String matchName = "unknown";
				String liveTime = "unknown";
				String matchBegin = "unknown";
				
				matchName = leagueObject.getString("name");
				liveTime = leagueObject.getJSONObject("live").getString("opens_at");
				matchBegin = leagueObject.getString("begin_at");
				
				if (match.length() == 2) {
					JSONObject opponentOne = match.optJSONObject(0).getJSONObject("opponent");
					JSONObject opponentTwo = match.optJSONObject(1).getJSONObject("opponent");
					opponentOneName = opponentOne.getString("name"); // get opponent 1 name
					opponentTwoName = opponentTwo.getString("name"); // get opponent 2 name
					
					// get opponents image urls:
					opponentOneImg = opponentOne.getString("image_url");
					opponentTwoImg = opponentTwo.getString("image_url");
					
				} else if (match.length() == 1){
					JSONObject opponentOne = match.optJSONObject(0).getJSONObject("opponent");
					opponentOneName = opponentOne.getString("name");
					opponentTwoName = "*winner";
					
					// get img urls
					opponentOneImg = opponentOne.getString("image_url");
					
					// TODO: if the match.length is zero...
					// TODO: move each 'get' method to its own method
					// TODO: make sure to add a try/catch to every single 'get' here (done when moved to its own method) so that if one fails, not all do
					// TODO: potential things to add to - 'opponent' - picture attached w/ opponent, 
				} else { // if match.length() = 0, meaning no opponents are known yet
					
				}
				
				System.out.println(matchName);
				System.out.println(opponentOneImg + "\n" + opponentTwoImg);
				System.out.println(league + ": " + opponentOneName + " vs. " + opponentTwoName);
				
				//Date liveDate = returnFormattedDate(liveTime);
				Date matchDate = returnFormattedDate(matchBegin, "yyyy-MM-dd'T'HH:mm:ssZ");
				
	            //System.out.println("Going live at " + TimeZone.getDefault().getID() + ": " + liveDate);
	            System.out.println("Match starting at " + TimeZone.getDefault().getID() + ": " + matchDate);
				
				if (!liveTime.equals("unknown")) {
					
				}
				System.out.println();
			}
		}
	}
	
	/*
	 * 
	 * https://developers.pandascore.co/doc/#operation/get_lol_matches_upcoming
	 * https://api.pandascore.co/lol/matches/upcoming
	 * [
  {
    "begin_at": null,
    "draw": true,
    "games": [
      {
        "begin_at": null,
        "finished": null,
        "id": 1,
        "length": null,
        "match_id": 1,
        "position": 1,
        "winner": {
          "id": null,
          "type": null
        },
        "winner_type": null
      }
    ],
    "id": 1,
    "league": {
      "id": 1,
      "image_url": null,
      "live_supported": true,
      "modified_at": null,
      "name": "string",
      "slug": "string",
      "url": null
    },
    "league_id": 1,
    "live": {
      "opens_at": null,
      "supported": true,
      "url": null
    },
    "match_type": null,
    "modified_at": null,
    "name": "string",
    "number_of_games": null,
    "opponents": [
      {
        "opponent": {
          "first_name": null,
          "hometown": null,
          "id": 1,
          "image_url": null,
          "last_name": null,
          "name": "string",
          "role": null,
          "slug": null
        },
        "type": "Player"
      }
    ],
    "results": [
      {
        "score": 0,
        "team_id": 1
      }
    ],
    "serie": {
      "begin_at": null,
      "description": null,
      "end_at": null,
      "full_name": "string",
      "id": 1,
      "league_id": 1,
      "modified_at": null,
      "name": null,
      "prizepool": null,
      "season": null,
      "slug": "string",
      "winner_id": null,
      "winner_type": null,
      "year": 1912
    },
    "serie_id": 1,
    "slug": null,
    "status": "not_started",
    "tournament": {
      "begin_at": null,
      "end_at": null,
      "id": 1,
      "league_id": 1,
      "modified_at": null,
      "name": "string",
      "serie_id": 1,
      "slug": "string",
      "winner_id": null,
      "winner_type": null
    },
    "tournament_id": 1,
    "videogame": {
      "id": 1,
      "name": "LoL",
      "slug": "league-of-legends"
    },
    "videogame_version": null,
    "winner": null,
    "winner_id": null
  }
]

	*
	*/
}
