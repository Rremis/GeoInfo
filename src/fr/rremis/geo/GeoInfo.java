package fr.rremis.geo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

import fr.rremis.geo.util.GeoIPData;

public class GeoInfo extends JavaPlugin {
	
	@Override
	public void onEnable(){
		instance = this;
		
		getCommand("geo").setExecutor(this);
		log("GeoInfo ON");
	}
	
	@Override
	public void onDisable(){
		log("GeoInfo OFF");
	}
	
	private static GeoInfo instance;
	public static GeoInfo getInstance(){
		return instance;
	}
	
	public String getPrefix(){
		return ChatColor.DARK_GREEN+"GeoInfo"+ChatColor.DARK_GRAY+"» "+ChatColor.GRAY;
	}
	
	public void log(String message){
		System.out.println("[GI] "+message);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lab, String[] args) {
		Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("geo")) {
			if(havePerm(p)){
				if(args.length == 0){
		        	GeoIPData Geo = new GeoIPData();
					try {
						Geo = getGeo(getIP(p));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					p.sendMessage(ChatColor.AQUA+""+ChatColor.STRIKETHROUGH+"----------------------");
					p.sendMessage(getPrefix()+ChatColor.GOLD+""+ChatColor.UNDERLINE+"Connection information");
					p.sendMessage(" ");
					p.sendMessage(ChatColor.DARK_RED + "--> IP : " + ChatColor.WHITE + getIPName(p));
					try {
						p.sendMessage(ChatColor.DARK_RED + "--> Ping : " + ChatColor.WHITE + getPing(p) + " ms");
					} catch (Exception e) {
						e.printStackTrace();
					}
	                p.sendMessage(ChatColor.DARK_RED + "--> UUID : " + ChatColor.WHITE + p.getUniqueId());
	                p.sendMessage(ChatColor.DARK_RED + "--> Country : " + ChatColor.WHITE + Geo.country);
	                p.sendMessage(ChatColor.DARK_RED + "--> Region : " + ChatColor.WHITE + Geo.regionName);
	                p.sendMessage(ChatColor.DARK_RED + "--> City : " + ChatColor.WHITE + Geo.city);
	                p.sendMessage(ChatColor.DARK_RED + "--> ISP : " + ChatColor.WHITE + Geo.isp);
	                p.sendMessage(ChatColor.DARK_RED + "--> Network : " + ChatColor.WHITE + Geo.as);
	                p.sendMessage(" ");
					p.sendMessage(ChatColor.AQUA+""+ChatColor.STRIKETHROUGH+"----------------------");
					
				} else if(args.length == 1){
					Player target = Bukkit.getPlayer(args[0]);
					if(target == null){
						p.sendMessage("§cCe joueur n'est pas connecté.");
						return true;
					}
					
					GeoIPData Geo = new GeoIPData();
					try {
						Geo = getGeo(getIP(target));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					p.sendMessage(ChatColor.AQUA+""+ChatColor.STRIKETHROUGH+"----------------------");
					p.sendMessage(getPrefix()+ChatColor.GOLD+""+ChatColor.UNDERLINE+"Connection information of "+target.getName());
					p.sendMessage(" ");
					p.sendMessage(ChatColor.DARK_RED + "--> IP : " + ChatColor.WHITE + getIPName(target));
	                try {
						p.sendMessage(ChatColor.DARK_RED + "--> Ping : " + ChatColor.WHITE + getPing(target) + " ms");
					} catch (Exception e) {
						e.printStackTrace();
					}
	                p.sendMessage(ChatColor.DARK_RED + "--> UUID : " + ChatColor.WHITE + p.getUniqueId());
	                p.sendMessage(ChatColor.DARK_RED + "--> Country : " + ChatColor.WHITE + Geo.country);
	                p.sendMessage(ChatColor.DARK_RED + "--> Region : " + ChatColor.WHITE + Geo.regionName);
	                p.sendMessage(ChatColor.DARK_RED + "--> City : " + ChatColor.WHITE + Geo.city);
	                p.sendMessage(ChatColor.DARK_RED + "--> ISP : " + ChatColor.WHITE + Geo.isp);
	                p.sendMessage(ChatColor.DARK_RED + "--> Network : " + ChatColor.WHITE + Geo.as);
	                p.sendMessage(" ");
					p.sendMessage(ChatColor.AQUA+""+ChatColor.STRIKETHROUGH+"----------------------");
				}
				return true;
			}
		}
		return true;
	}
	
	public static String getServerVersion() {
		
	    Pattern brand = Pattern.compile("(v|)[0-9][_.][0-9][_.][R0-9]*");
	    String version = null;
	    String pkg = Bukkit.getServer().getClass().getPackage().getName();
	    String version0 = pkg.substring(pkg.lastIndexOf('.') + 1);
	    
	    if (!brand.matcher(version0).matches()) {
	      version0 = "";
	    }
	    version = version0;
	    return !"".equals(version) ? version + "." : "";
	}
	
	public static int getPing(Player player) throws Exception {
		int ping = 0;Class<?> craftPlayer = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + "entity.CraftPlayer");
		Object converted = craftPlayer.cast(player);Method handle = converted.getClass().getMethod("getHandle", new Class[0]);
		Object entityPlayer = handle.invoke(converted, new Object[0]);Field pingField = entityPlayer.getClass().getField("ping");
		ping = pingField.getInt(entityPlayer);
		return ping;
	}
	
	public GeoIPData getGeo(String IP) throws InterruptedException {
		final String[] msg = new String[1];
		
		Thread t = new Thread() {
			public void run() {
				try {
					URL obj = new URL("http://ip-api.com/json/" + IP);
					HttpURLConnection con = (HttpURLConnection) obj.openConnection();
					 
					// optional default is GET
			        con.setRequestMethod("GET");

			        @SuppressWarnings("unused")
					int responseCode = con.getResponseCode();

			        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			        String inputLine;
			        StringBuffer response = new StringBuffer();
			        while ((inputLine = in.readLine()) != null) {
			        	response.append(inputLine);
			        }
			        in.close();
			         
			        msg[0] = response.toString();
			    } catch (Exception e) {
			    	System.out.print("ERREUR GEOIP API: " + IP);
			    }
			}
		};
		t.start();
		t.join();

		if(msg[0] == null){
			return new GeoIPData();
		} else {
			return new Gson().fromJson(msg[0], GeoIPData.class);
		}
	}
	
	public boolean isOnline(String name){
		Player p = Bukkit.getPlayer(name);
		
		boolean result = true;
		if(p == null) result = false;
		
		return result;
	}
	
	public String getIP(Player p){
		return p.getAddress().getHostName();
	}
	
	public static String getIPName(Player p) {
		return p.getAddress().getAddress().getHostAddress().toString().split(":")[0].replace("/", "");
	}
	
	public static boolean havePerm(Player p){
		return p.isOp() || p.hasPermission("geo.info");
	}
}
