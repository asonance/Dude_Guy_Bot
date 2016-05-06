package com.keegan.Dude_Guy_Bot;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.NoSuchElementException;
import sx.blah.discord.handle.AudioChannel;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;

public class BotAudio extends Command {
	
	

	public void run() {
		/**
		 * Implement commands for playing audio
		 */
		// Check for permissions
		if (isAdmin()) {
			// Get arguments passed
			String[] args = getArgs();
			// Handle no parameters
			if(args.length == 0){
				displayMessage("No parameters passed");
				String toDisp = "Correct usage " + Instance.getKey(message) + Instance.getCmd(message) + " \"parameters\"";
				displayMessage(toDisp);
			} else {
				// Initialize audio channel
				AudioChannel audio_chn = null;
				try {
					audio_chn = bot.getGuilds().get(0).getAudioChannel();
				} catch (DiscordException e1) {
					e1.printStackTrace();
				}

				if(args[0].equals("queue")){
					URL url = null;
					// Initialize the given url
					try {
						// Remove extra parameters in the url
						url = new URL(args[1].substring(0, args[1].indexOf('&')));
					} catch(ArrayIndexOutOfBoundsException e){
						displayMessage("No URL provided");
					} catch (MalformedURLException e) {
						displayMessage("Invalid URL");
					}
					if (url != null) {
						// Queue up the url
						if (args[1].contains("youtube.com")){
							// Get the youtube audio to queue
							File music = loadYoutubeMP3(url, audio_chn);
							audio_chn.queueFile(music);
							displayMessage(music.getName());
							// Delete the downloaded youtube file
							try{
								Files.delete(music.toPath());
							}catch (IOException x){
								displayMessage(x.getMessage());
							}
						}else{
							displayMessage(url.toString());
							audio_chn.queueUrl(url);
						}
						displayMessage(args[1] + " queued.");
					}
				} // Handle pausing music
				else if (args[0].equals("pause")){
					// Bot must have something queued to play
					if(audio_chn.getQueueSize() == 0){
						displayMessage("There is nothing playing");
					}
					else{
						audio_chn.pause();
					}
				}else if (args[0].equals("play")){
					if(audio_chn.getQueueSize() == 0){
						displayMessage("There is nothing to play currently.");
					}
					else{
						audio_chn.resume();
					}
				}else if (args[0].equals("clear")){
					// Bot must have something queued to clear
					if(audio_chn.getQueueSize() != 0){
						audio_chn.clearQueue();
						displayMessage("Queue Cleared.");
					}else{
						displayMessage("Nothing to clear");
					}
				}else if (args[0].equals("skip")){
					// Bot must have something queued to play
					if(audio_chn.getQueueSize() == 0){
						displayMessage("There is nothing to skip currently.");
					}
					else{
						audio_chn.skip();
						displayMessage("Song Skipped.");
					}
				}
			}

		}else{
			displayMessage("No permission");
		}

	}

	public void joinChannel(){
		/**
		 * Attempts to have the bot join the voice channel that user is in
		 */
		IVoiceChannel curr_chn = null;
		try{
			// Get channel of sender
			curr_chn = message.getAuthor().getVoiceChannel().get();
			// Connect to sender's channel
			if (!bot.getConnectedVoiceChannels().contains(curr_chn)){
				curr_chn.join();
			}
		} 
		// Make sure that there is a channel to join to
		catch (NoSuchElementException e){
			displayMessage("You are not currently in a valid channel to perform this command");
		}
	}
	
	private File loadYoutubeMP3(URL url, AudioChannel audio_chn){
		/**
		 * Queues a music file from a youtube url
		 */
		File music = null;
		try {
			// Download youtube and convert to mp3 using youtube-dl
			Process py = Runtime.getRuntime().exec("python ../youtube-dl -x --audio-format mp3 " + url);
			displayMessage("Downloading File");
			BufferedReader in = new BufferedReader(new InputStreamReader(py.getInputStream()));
			String input;
			String title = null;
			boolean correct_line;
			do{
				input = in.readLine();
				correct_line = input.startsWith("[download] Destination: ");
				title = processYoutubeTitle(input);
			}while((!(input == null) || correct_line));
			py.waitFor();
			displayMessage("Now Loading File");
			
			// Load the mp3 file into the bot
			File dir = new File(System.getProperty("user.dir"));
			for (File file : dir.listFiles()){
				if (file.getName().endsWith(".mp3")){
					music = file;
				}
			}
		} catch (Exception e) {
			displayMessage(e.getMessage());
		}
		return music;
	}
	
	private String processYoutubeTitle(String input){
		/**
		 * Strips the video title out of a download log message
		 */
		int startIndex = "[download] Destination: ".length();
		int endIndex = input.lastIndexOf('.');
		String title = input.substring(startIndex, endIndex);
		return title;
	}
}
