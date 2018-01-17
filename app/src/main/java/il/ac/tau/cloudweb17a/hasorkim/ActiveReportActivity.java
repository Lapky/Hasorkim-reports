package il.ac.tau.cloudweb17a.hasorkim;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ActiveReportActivity extends AppCompatActivity {

    private Report report;
    private static final String TAG = "Send Report";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_active_report);

        report = (Report) getIntent().getSerializableExtra("Report");

        final TextView activeReportStatus = findViewById(R.id.activeReportStatus);
        activeReportStatus.setText(report.statusInHebrew());

        //TextView activeReportExtraText = (TextView) findViewById(R.id.activeReportExtraText);
        //activeReportExtraText.setText((CharSequence) report.getFreeText());

        TextView activeReportPhoneNumber = findViewById(R.id.activeReportPhoneNumber);
        activeReportPhoneNumber.setText(report.getPhoneNumber());

        TextView activeReportLocation = findViewById(R.id.activeReportLocation);
        activeReportLocation.setText(report.getAddress());

        if (report.getExtraPhoneNumber() != null) {
            LinearLayout linearLayout = findViewById(R.id.addAnotherPhoneLinearLayout);
            linearLayout.setVisibility(LinearLayout.VISIBLE);
            EditText addAnotherPhoneNumber = findViewById(R.id.addAnotherPhoneNumber);
            addAnotherPhoneNumber.setText(report.getExtraPhoneNumber());
        }

        if (report.getPhotoPath() != null) {
            ImageView openReportImage = findViewById(R.id.activeReportImageView);
            openReportImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(report.getPhotoPath()).into(openReportImage);
        }

        ImageButton addPhoneNumber = findViewById(R.id.addPhoneNumber);

        addPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout linearLayout = findViewById(R.id.addAnotherPhoneLinearLayout);
                if (linearLayout.getVisibility() == LinearLayout.GONE)
                    linearLayout.setVisibility(LinearLayout.VISIBLE);
                else
                    linearLayout.setVisibility(LinearLayout.GONE);
            }

        });

        ImageButton savePhoneNumber = findViewById(R.id.saveAnotherPhoneNumber);

        savePhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView extraPhoneNumber = findViewById(R.id.addAnotherPhoneNumber);
                String extraPhoneNumberString = extraPhoneNumber.getText().toString();
                String error = report.validatePhone(extraPhoneNumberString);
                if (error.equals("")) {
                    report.setExtraPhoneNumber(extraPhoneNumberString);
                    report.reportUpdateExtraPhoneNumber();

                    Drawable myIcon = getResources().getDrawable(R.drawable.ic_done_24pp);
                    myIcon.setBounds(0, 0, myIcon.getIntrinsicWidth(), myIcon.getIntrinsicHeight());
                    extraPhoneNumber.setError("הטלפון נשמר", myIcon);
                } else {
                    extraPhoneNumber.setError(error);
                }


            }

        });

        final Button whatNowInfo = findViewById(R.id.whatNowInfo);

        whatNowInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActiveReportActivity.this);
                LayoutInflater inflater = ActiveReportActivity.this.getLayoutInflater();

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder.setView(inflater.inflate(R.layout.what_now_pop, null));
                builder.setTitle(R.string.thanks_for_reporting);
                builder.create().show();

            }
        });

        Button cancelReportButton = findViewById(R.id.cancelReport);

        cancelReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView title = new TextView(ActiveReportActivity.this);
                final EditText editText = new EditText(ActiveReportActivity.this);

                title.setText(R.string.cancel_dialog_title);
                title.setPadding(10, 50, 64, 9);
                title.setTextColor(Color.BLACK);
                title.setTextSize(20);
                title.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

                new AlertDialog.Builder(ActiveReportActivity.this)
                        .setMessage(R.string.cancel_dialog_message)
                        .setCustomTitle(title)
                        .setView(editText)
                        .setPositiveButton(R.string.cancel_report, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String cancelReportText = editText.getText().toString();

                                report.reportUpdateCancellationText(cancelReportText);
                                report.reportUpdateCancellationUserType();
                                report.reportUpdateStatus("CANCELED");

                                startActivity(new Intent(ActiveReportActivity.this, ReportListActivity.class));
                            }
                        })
                        .setNegativeButton(R.string.back_report, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .create().show();
            }
        });

        Button vetListButton = findViewById(R.id.VetListButton);
        vetListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent vetIntent = new Intent(getApplicationContext(), VetListActivity.class);
                vetIntent.putExtra("lat", report.getLatitude());
                vetIntent.putExtra("long", report.getLongitude());
                vetIntent.putExtra("from", "menu");
                startActivity(vetIntent);
            }
        });


        final View reporter_buttons = findViewById(R.id.reporter_buttons);


        DatabaseReference statusManagerRef = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("status");

        statusManagerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status = dataSnapshot.getValue(String.class);
                if (status != null) {
                    report.setStatus(status);
                    activeReportStatus.setText(report.statusInHebrew());
                }


                if ((Objects.equals(status, "CANCELED")) || Objects.equals(status, "CLOSED")) {
                    reporter_buttons.setVisibility(View.GONE);
                    whatNowInfo.setVisibility(View.GONE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();//clear memory
    }
}



