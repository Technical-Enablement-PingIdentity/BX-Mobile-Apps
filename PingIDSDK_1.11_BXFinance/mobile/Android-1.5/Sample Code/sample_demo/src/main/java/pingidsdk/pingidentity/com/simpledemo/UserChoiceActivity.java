package pingidsdk.pingidentity.com.simpledemo;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Calendar;

import pingidsdk.pingidentity.com.simpledemo.models.User;
import pingidsdk.pingidentity.com.simpledemo.ui.IosTextView;
import pingidsdk.pingidentity.com.simpledemo.ui.UserSelectionAdapter;
import pingidsdkclient.PIDUserSelectionObject;
import pingidsdkclient.PingID;

import static pingidsdk.pingidentity.com.simpledemo.PingIDSdkDemoApplication.AUTH_TOKEN_PARAM_PENDING_USER_SELECTION_AND_APPROVAL;
import static pingidsdk.pingidentity.com.simpledemo.PingIDSdkDemoApplication.RESPONSE_AUTH_TOKEN_STATUS_PARAM;

/**
 * This activity will represent a list of users
 * Created by Ping Identity on 11/6/18.
 */

public class UserChoiceActivity extends BaseActivity {

    private RelativeLayout successLayout;
    //small flag to prevent quick double click on list
    private boolean isButtonAlreadyClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        IosTextView version = (IosTextView) findViewById(R.id.version);
        version.setText(String.format("v%s",BuildConfig.VERSION_NAME));

        int year = Calendar.getInstance().get(Calendar.YEAR);
        TextView copyrightView = findViewById(R.id.copyright);
        copyrightView.setText(String.format(getString(R.string.copyright), year));

        ListView listView = (ListView) findViewById(R.id.list);
        listView.setDivider(getResources().getDrawable(R.drawable.divider));
        listView.setDividerHeight(1);
        if(getIntent().getExtras() != null) {
            final ArrayList<User> users = parseUsersFromBundle(getIntent().getExtras());
            UserSelectionAdapter adapter = new UserSelectionAdapter(getApplicationContext(), users);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(isButtonAlreadyClicked) {
                        return;
                    }
                    view.setSelected(true);
                    changeScreenAvailability(false);
                    User user = users.get(position);
                    if(user.getStatus().equalsIgnoreCase("suspended")) {
                        changeScreenAvailability(true);
                        return;
                    } else {
                        String status = getIntent().getExtras().getString(RESPONSE_AUTH_TOKEN_STATUS_PARAM);
                        //check if the user approval is needed
                        if(status.equalsIgnoreCase(AUTH_TOKEN_PARAM_PENDING_USER_SELECTION_AND_APPROVAL)) {
                            Bundle bundle = new Bundle();
                            bundle.putAll(getIntent().getExtras());
                            bundle.putString("username", user.getUsername());
                            requestUserApproval(bundle);
                            finish();
                        } else {
                            PIDUserSelectionObject userSelectionObject = new PIDUserSelectionObject();
                            userSelectionObject.setPidTrustLevel(PingID.PIDTrustLevel.PIDTrustLevelTrusted);
                            userSelectionObject.setPidActionType(PingID.PIDActionType.PIDActionTypeApprove);
                            userSelectionObject.setPidUsername(user.getUsername());
                            try {
                                PingID.getInstance().setAuthenticationUserSelection(userSelectionObject);
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }

    private ArrayList<User> parseUsersFromBundle(Bundle extras) {
        ArrayList<User> users = new ArrayList<>();
        if(extras.containsKey("users")) {
            JsonArray jsonUsers = (JsonArray) new JsonParser().parse(extras.getString("users"));
            for(JsonElement jsonUser : jsonUsers){
                JsonObject jsonObjectUser = jsonUser.getAsJsonObject();
                User user = new User(jsonObjectUser.get("username").getAsString(),
                        jsonObjectUser.get("status").getAsString());
                if(user.getStatus().equalsIgnoreCase("suspended")) {
                    continue;
                }
                if(jsonObjectUser.has("firstName")) {
                    user.setFirstname(jsonObjectUser.get("firstName").getAsString());
                }
                if(jsonObjectUser.has("lastName")) {
                    user.setLastname(jsonObjectUser.get("lastName").getAsString());
                }
                users.add(user);
            }
        }
        return users;
    }


    @Override
    void changeScreenAvailability(boolean available) {
        isButtonAlreadyClicked =! available;
    }

    @Override
    void hideScanButton(boolean hide) {

    }
}
