package si.uni_lj.fe.tnuv.taskman;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @noinspection ALL*/
public class ProjectActivity_v2 extends AppCompatActivity {

    private ListView lv;
    private List<Map<String, String>> programTasks;
    private JSONArray tasksArray;
    String userID = null;
    String token = null;
    JSONObject requestInfo;
    private CustomSimpleAdapter adapter;
    String URL;
    String projectID;
    String username;
    String projectIme;
    String projectOpis;
    TextView UsernameField;
    TextView ProjectImeField;
    TextView ProjectOpisField;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_activity_v2);

        SharedPreferences prefs = ProjectActivity_v2.this.getSharedPreferences("TMan", Context.MODE_PRIVATE);
        projectID = prefs.getString("projectID", null);
        programTasks = new ArrayList<>();
        lv = findViewById(R.id.task_list);

        UsernameField = findViewById(R.id.username);
        ProjectImeField = findViewById(R.id.project_name);
        ProjectOpisField = findViewById(R.id.project_description);

        URL = getString(R.string.URL_base_storitve) + getString(R.string.projectsAPI) + projectID + getString(R.string.tasksAPI);
        Log.d("ProjectAct L76", URL);
        Log.d("ProjectAct L77", projectID);


        userID = prefs.getString("userID", null);
        token = prefs.getString("token", null);
        Log.d("ProjectAct L97", URL);
        tasksArray = new JSONArray();
        requestInfo = new JSONObject();
        try {
            requestInfo.put("token", token);
            requestInfo.put("userID", userID);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        lv.setOnItemClickListener((adapterView, view, i, l) -> {
            try {
                JSONObject task = tasksArray.getJSONObject(i);
                String taskID = task.getString("id");

                Intent intent = new Intent(ProjectActivity_v2.this, TaskActivity.class);
                intent.putExtra("taskID", taskID);
                intent.putExtra("projectID", projectID);
                intent.putExtra("projectIme", projectIme);
                intent.putExtra("projectOpis", projectOpis);
                intent.putExtra("username", username);
                startActivity(intent);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void populateListView() {
        programTasks.clear();
        for (int i = 0; i < tasksArray.length(); i++) {
            try {
                JSONObject task = tasksArray.getJSONObject(i);
                Map<String, String> item = new HashMap<>();
                item.put("name", task.getString("ime"));
                item.put("description", task.getString("opis"));
                item.put("koncano", task.getString("koncano"));
                programTasks.add(item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    @Override
    public void onStart() {
        super.onStart();

        GetUserNick getUserNick = new GetUserNick(this);
        getUserNick.getUserNick(userID, requestInfo, new GetUserNick.GetUserNickCallback() {
            @Override
            public void onResponse(String userInfo) {
                Log.d("UserInfo", userInfo);
                username = userInfo;
                Log.d("Project L140 ","userNick: " + username);
                UsernameField.setText(username);
            }
            @Override
            public void onError(String error) {
                Log.e("UserInfoError", "Error: " + error);}
        });

        GetProjectInfo getProjectInfo = new GetProjectInfo(this);
        getProjectInfo.getProjectInfo(projectID, requestInfo, new GetProjectInfo.GetProjectInfoCallback() {
            @Override
            public void onResponse(JSONObject projectInfo) throws JSONException {
                Log.d("ProjectInfo", projectInfo.toString());
                projectIme = projectInfo.getString("ime");
                projectOpis = projectInfo.getString("opis");
                ProjectImeField.setText(projectIme);
                ProjectOpisField.setText(projectOpis);
            }
            @Override
            public void onError(String error) {
                Log.e("UserInfoError", "Error: " + error);}
        });


        Log.d("ProjectAct L145", "Tukaj smo");
        useAPI apiProject = new useAPI("GET", URL, requestInfo, true);
        apiProject.uporabi(output -> {
            Log.d("Project L147 API Resp", "Response code: " + output.getResponseCode());
            int responseCode = Integer.parseInt(output.getResponseCode());
            if (responseCode == 200) {
                tasksArray = output.getJsonArray();
                ProjectActivity_v2.this.runOnUiThread(() -> {
                    Log.d("ProjAct L153", "Tukaj smo");
                    populateListView();
                });
                Log.d("ProjtAct L155 Vseb spor", "Sporocilo: " + tasksArray);
            } else {
                ProjectActivity_v2.this.runOnUiThread(Toast.makeText(ProjectActivity_v2.this, "Response code: " + output.getResponseCode(), Toast.LENGTH_LONG)::show);
            }
        });

        adapter = new CustomSimpleAdapter(
                this,
                programTasks,
                R.layout.list_item_layout,
                new String[]{"name", "description", "koncano"},
                new int[]{R.id.projectTask, R.id.projectTaskDescription}
        );
        lv.setAdapter(adapter);
    }




    public void startTaskNewActivity(View v) {
        Intent intent = new Intent(ProjectActivity_v2.this, TaskNewActivity.class);
        intent.putExtra("projectID", projectID);
        intent.putExtra("username", username);
        intent.putExtra("projectIme", projectIme);
        intent.putExtra("projectOpis", projectOpis);
        startActivity(intent);
    }

    public void removeProjectActivity(View v) {
        URL = getString(R.string.URL_base_storitve) + getString(R.string.projectsAPI) + projectID;
        try {
            requestInfo.put("koncan",1);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        useAPI projectDelete = new useAPI("PUT", URL, requestInfo, true);
        projectDelete.uporabi(output -> {
            Log.d("Project L192 API Resp", "Response code: " + output.getResponseCode());
            int responseCode = Integer.parseInt(output.getResponseCode());
            if (responseCode == 204) {
                ProjectActivity_v2.this.runOnUiThread(Toast.makeText(ProjectActivity_v2.this, "Projekt uspešno izbrisan", Toast.LENGTH_SHORT)::show);

                Intent intent = new Intent(ProjectActivity_v2.this, UserviewActivity.class);
                startActivity(intent);
            } else {
                ProjectActivity_v2.this.runOnUiThread(Toast.makeText(ProjectActivity_v2.this, "Ups, nekaj je šlo narobe!" + output.getResponseCode(), Toast.LENGTH_SHORT)::show);
            }
        });
    }
}
