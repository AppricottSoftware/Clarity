package appricottsoftware.clarity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Wiring up what the button does
        // .. get the button
        Button btn = findViewById(R.id.runSampleApi);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("1", "Testing sample API");
                // These code snippets use an open-source library. http://unirest.io/java
                HttpResponse<JsonNode> response = Unirest.get("https://listennotes.p.mashape.com/api/v1/search?offset=0&q=star+wars&sort_by_date=0&type=episode")
                        .header("X-Mashape-Key", "MFfamrC24Vmshh30jLIhFrGoEqwRp1wn04qjsncdkIni1cISDf")
                        .header("Accept", "application/json")
                        .asJson();

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Object json = mapper.readValue(response.toString(), Object.class);
                    String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);

                    Log.i("1", pretty);

                } catch (Exception e) {
                    Log.i("1","Sorry, pretty print didn't work");
                }

            }
        });


    }
}
