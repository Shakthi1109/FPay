package SecuGen.Demo;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class HomeActivity extends Activity {

    Button nextbtn, regBtn;
    EditText cost;
    float payable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);




        nextbtn = (Button) findViewById(R.id.nextbtn);
        regBtn = (Button) findViewById(R.id.regBtn);
        cost = (EditText) findViewById(R.id.cost);

        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payFunction();

            }
        });





        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(HomeActivity.this, RegisterActivity.class);
                startActivity(intent1);
            }
        });
    }

    void payFunction(){
        payable = Float.valueOf(cost.getText().toString().trim());
        Intent intent = new Intent(HomeActivity.this, JSGDActivity.class);
        intent.putExtra("payable",payable);
        startActivity(intent);
    }
}
