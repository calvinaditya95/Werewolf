import org.json.JSONException;
import org.json.JSONObject;

public class Clients {
    public int player_id;
    public int is_alive;
    public String address;
    public int port;
    public String username;
    public String role;
    
    public Clients(JSONObject o) {
        try {
            player_id = o.getInt("player_id");
            is_alive = o.getInt("is_alive");
            address = o.getString("address");
            port = o.getInt("port");
            username = o.getString("username");
            if (o.has("role"))
                role = o.getString("role");
            else
                role = "unknown";
        }
        catch (JSONException e) {
            System.out.println(e);
        }
    }
}
