package SecuGen.Demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import SecuGen.FDxSDKPro.SGFingerPresentEvent;

public class HomeActivity extends Activity {

    Button nextbtn, regBtn;
    EditText cost;
    float payable;

    TextView nameTV, amountTV, templateTV;
    Button buttonRetrive,scanFpBtn;
    DatabaseReference reff;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        nextbtn = (Button) findViewById(R.id.nextbtn);
        regBtn = (Button) findViewById(R.id.regBtn);
        cost = (EditText) findViewById(R.id.cost);

        nameTV = (TextView) findViewById(R.id.textViewName);
        amountTV = (TextView) findViewById(R.id.textViewAmount);
//        templateTV = (TextView) findViewById(R.id.textViewTemplate);
        buttonRetrive = (Button) findViewById(R.id.buttonRetrive);

        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payFunction();

            }
        });

        buttonRetrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reff = FirebaseDatabase.getInstance().getReference().child("Member").child("1");
                reff.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("nameStr").getValue().toString();
                        String amount = dataSnapshot.child("amtFloat").getValue().toString();
                        String template = dataSnapshot.child("template").getValue().toString();
                        nameTV.setText(name);
                        amountTV.setText(amount);
//                        templateTV.setText(template);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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
