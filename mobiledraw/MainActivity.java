package edu.standord.cs108.mobiledraw;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import edu.standord.cs108.mobiledraw.customDrawView.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void updateView(View view){
        customDrawView custom = (customDrawView)findViewById(R.id.customView);
        custom.userInputUpdate();
        custom.invalidate();
    }
}
